package com.soda.risk.engine.core.riskdecision.service;

import com.soda.risk.engine.api.dto.RiskDecisionResult;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 决策服务
 * 综合特征计算结果进行风险决策
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DecisionService {

    private final RedisCacheService redisCacheService;

    /**
     * 基于策略引擎的决策分析 - 整合原riskDecision的Login/Register/RiskIdentification处理器模式
     * 先调用策略引擎计算分数，再结合黑白名单得出最终决策
     *
     * @param dataMap     数据
     * @param openId      业务方标识
     * @param sceneKey    场景标识（如login_protection）
     * @return 决策结果
     */
    public RiskDecisionResult decideWithStrategy(Map<String, Object> dataMap, String openId, String sceneKey) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("sceneKey", sceneKey);
        return decide(dataMap, openId, sceneKey);
    }

    /**
     * 执行决策分析
     */
    public RiskDecisionResult decide(Map<String, Object> dataMap, String openId, String businessType) {
        long start = System.currentTimeMillis();
        Map<String, Object> detail = new HashMap<>();

        try {
            // 1. 查询黑白名单
            int blacklistScore = checkBlackWhiteList(dataMap, openId);
            detail.put("blacklistScore", blacklistScore);

            // 2. 计算风险分数
            int score = calculateRiskScore(dataMap, blacklistScore, businessType);
            detail.put("riskScore", score);

            // 3. 确定风险等级
            String riskLevel = determineRiskLevel(score);
            detail.put("riskLevel", riskLevel);

            // 4. 获取风险配置
            Map<Object, Object> riskConfig = redisCacheService.hGetAll(
                    RedisKeyConstants.RISK_PREFIX + businessType);
            if (riskConfig != null) {
                detail.put("riskConfig", riskConfig);
            }

            MonitorFacade.insert("[riskdecision]Decide", System.currentTimeMillis() - start);

            return RiskDecisionResult.builder()
                    .score(score)
                    .riskLevel(riskLevel)
                    .detail(detail)
                    .originalData(dataMap)
                    .openId(openId)
                    .businessType(businessType)
                    .build();

        } catch (Exception e) {
            log.error("Decision failed, openId={}, type={}", openId, businessType, e);
            return RiskDecisionResult.builder()
                    .score(0)
                    .riskLevel("UNKNOWN")
                    .detail(detail)
                    .originalData(dataMap)
                    .openId(openId)
                    .businessType(businessType)
                    .build();
        }
    }

    private int checkBlackWhiteList(Map<String, Object> dataMap, String openId) {
        try {
            String userId = (String) dataMap.getOrDefault("userId", "");
            String ip = (String) dataMap.getOrDefault("ip", "");

            // 检查黑名单
            String blacklistKey = RedisKeyConstants.BLACK_WHITE_LIST + "blacklist";
            if (redisCacheService.sIsMember(blacklistKey, userId) ||
                redisCacheService.sIsMember(blacklistKey, ip)) {
                return 100;
            }

            // 检查白名单
            String whitelistKey = RedisKeyConstants.BLACK_WHITE_LIST + "whitelist";
            if (redisCacheService.sIsMember(whitelistKey, userId) ||
                redisCacheService.sIsMember(whitelistKey, ip)) {
                return -100;
            }

            return 0;
        } catch (Exception e) {
            log.error("checkBlackWhiteList failed", e);
            return 0;
        }
    }

    private int calculateRiskScore(Map<String, Object> dataMap, int blacklistScore, String businessType) {
        // 基础分数 + 黑白名单分数
        int baseScore = 50;
        return Math.max(0, Math.min(100, baseScore + blacklistScore));
    }

    private String determineRiskLevel(int score) {
        if (score >= 80) return "HIGH";
        if (score >= 60) return "MEDIUM";
        if (score >= 30) return "LOW";
        return "SAFE";
    }
}
