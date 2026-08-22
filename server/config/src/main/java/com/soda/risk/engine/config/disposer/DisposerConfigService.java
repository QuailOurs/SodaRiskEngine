package com.soda.risk.engine.config.disposer;

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
public class DisposerConfigService extends ServiceImpl<DisposerConfigMapper, DisposerConfig> {

    private final RedisCacheService redisCacheService;

    public List<DisposerConfig> getActive() {
        return list(new LambdaQueryWrapper<DisposerConfig>().eq(DisposerConfig::getState, 1));
    }

    public void syncToRedis() {
        List<DisposerConfig> configs = getActive();
        for (DisposerConfig config : configs) {
            String key = RedisKeyConstants.DISPOSER_PREFIX + config.getDisposerKey();
            redisCacheService.setJson(key, config, 24, TimeUnit.HOURS);
        }
        log.info("Synced {} disposer configs to Redis", configs.size());
    }
}
