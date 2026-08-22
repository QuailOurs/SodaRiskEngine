package com.soda.risk.engine.core.riskdecision.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.api.dto.RiskDecisionResult;
import com.soda.risk.engine.common.enums.CodeEnum;
import com.soda.risk.engine.common.exception.BizException;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import com.soda.risk.engine.core.riskdecision.service.DecisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * 风险决策引擎
 * 通过标准 Java 与 Spring API 对外提供能力
 *
 * 保留原始的三种业务场景处理模式：
 * 1. 登录保护 (LOGIN_PROTECTION)
 * 2. 注册保护 (REGISTER_PROTECTION)
 * 3. 风险识别 (RISK_IDENTIFICATION)
 */
@Slf4j
@Service
public class RiskDecisionEngine {

    private final DecisionService decisionService;
    private final ObjectMapper objectMapper;

    /** 业务类型对应的场景处理函数 */
    private final Map<String, Function<Map<String, Object>, RiskDecisionResult>> handlers = new HashMap<>();

    @Autowired
    public RiskDecisionEngine(DecisionService decisionService, ObjectMapper objectMapper) {
        this.decisionService = decisionService;
        this.objectMapper = objectMapper;
        initHandlers();
    }

    private void initHandlers() {
        // 登录保护 - 使用login_protection场景策略
        handlers.put("LOGIN_PROTECTION", data -> decisionService.decideWithStrategy(
                data, String.valueOf(data.getOrDefault("openId", "")), "login_protection"));
        // 注册保护 - 使用register_protection场景策略
        handlers.put("REGISTER_PROTECTION", data -> decisionService.decideWithStrategy(
                data, String.valueOf(data.getOrDefault("openId", "")), "register_protection"));
        // 风险识别 - 使用account_security场景策略
        handlers.put("RISK_IDENTIFICATION", data -> decisionService.decideWithStrategy(
                data, String.valueOf(data.getOrDefault("openId", "")), "account_security"));
    }

    /**
     * 执行风险决策
     */
    public Response<RiskDecisionResult> execute(String data, String openKey, String businessType) {
        long start = System.currentTimeMillis();

        try {
            // 1. 预处理
            Map<String, Object> dataMap = pretreatment(data, openKey, businessType);
            if (dataMap == null) {
                return Response.fail(CodeEnum.DATA_PARSING_FAILED);
            }

            // 2. 生成TraceId
            String traceId = UUID.randomUUID().toString().replace("-", "");
            dataMap.put("traceId", traceId);
            dataMap.put("openId", openKey);
            dataMap.put("businessType", businessType);

            // 3. 执行决策 - 按业务类型分发
            RiskDecisionResult result;
            String handlerKey = normalizeBusinessType(businessType);
            Function<Map<String, Object>, RiskDecisionResult> handler = handlers.get(handlerKey);

            if (handler != null) {
                result = handler.apply(dataMap);
            } else {
                // 默认走通用决策
                result = decisionService.decide(dataMap, openKey, businessType);
            }

            result.setTraceId(traceId);
            MonitorFacade.insert("[riskdecision]Execute", System.currentTimeMillis() - start);
            return Response.success(result);

        } catch (BizException e) {
            log.warn("RiskDecision auth failed: {}", e.getMessage());
            return Response.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("RiskDecisionEngine execute failed, openKey={}, type={}", openKey, businessType, e);
            MonitorFacade.insert("[riskdecision]Execute_Error", System.currentTimeMillis() - start);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    private Map<String, Object> pretreatment(String data, String openKey, String businessType) {
        try {
            if (StringUtils.isBlank(data)) {
                log.warn("RiskDecision data is empty, openKey={}", openKey);
                return null;
            }
            Map<String, Object> dataMap = objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
            if (dataMap.isEmpty()) {
                log.warn("RiskDecision data map is empty, openKey={}", openKey);
                return null;
            }
            // 补充服务器信息
            dataMap.put("serverIp", getServerIp());
            dataMap.put("processTime", System.currentTimeMillis());
            return dataMap;
        } catch (Exception e) {
            log.error("RiskDecision pretreatment failed, data={}", data, e);
            return null;
        }
    }

    private String normalizeBusinessType(String businessType) {
        if (StringUtils.isBlank(businessType)) {
            return "";
        }
        return businessType.toUpperCase().replace(" ", "_");
    }

    private String getServerIp() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
