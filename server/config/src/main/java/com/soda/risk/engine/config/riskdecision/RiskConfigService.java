package com.soda.risk.engine.config.riskdecision;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskConfigService extends ServiceImpl<RiskConfigMapper, RiskConfig> {

    private final RedisCacheService redisCacheService;

    public List<RiskConfig> getByBusinessType(String businessType) {
        return list(new LambdaQueryWrapper<RiskConfig>()
                .eq(RiskConfig::getBusinessType, businessType)
                .eq(RiskConfig::getState, 1));
    }

    public void syncToRedis() {
        List<RiskConfig> configs = list(new LambdaQueryWrapper<RiskConfig>().eq(RiskConfig::getState, 1));
        for (RiskConfig config : configs) {
            String key = RedisKeyConstants.RISK_PREFIX + config.getBusinessType();
            redisCacheService.hSet(key, config.getRiskKey(), String.valueOf(config.getScoreThreshold()));
        }
        log.info("Synced {} risk configs to Redis", configs.size());
    }
}
