package com.soda.risk.engine.config.feature;

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
public class BaseInfoFeatureServiceImpl extends ServiceImpl<BaseInfoFeatureMapper, BaseInfoFeature>
        implements BaseInfoFeatureService {

    private final RedisCacheService redisCacheService;

    @Override
    public List<BaseInfoFeature> getBySceneKey(String sceneKey) {
        return list(new LambdaQueryWrapper<BaseInfoFeature>()
                .eq(BaseInfoFeature::getSceneKey, sceneKey)
                .eq(BaseInfoFeature::getState, 1));
    }

    @Override
    public void syncToRedis() {
        List<BaseInfoFeature> features = list(new LambdaQueryWrapper<BaseInfoFeature>()
                .eq(BaseInfoFeature::getState, 1));
        for (BaseInfoFeature feature : features) {
            String key = RedisKeyConstants.FEATURE_BASEINFO + feature.getId();
            redisCacheService.setJson(key, feature, 24, TimeUnit.HOURS);

            // 同步到场景特征集合
            if (feature.getSceneKey() != null) {
                String sceneFeatureKey = RedisKeyConstants.SCENE_PREFIX + feature.getSceneKey() + ":features";
                String featureKey = feature.getFeatureKey() == null ? feature.getName() : feature.getFeatureKey();
                String featureType = feature.getFeatureType() == null ? "base" : feature.getFeatureType();
                redisCacheService.hSet(sceneFeatureKey, featureKey, featureType + ":" + featureKey);
            }
        }
        log.info("Synced {} baseInfo features to Redis", features.size());
    }
}
