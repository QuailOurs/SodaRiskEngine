package com.soda.risk.engine.config.feature;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BaseInfoFeatureService extends IService<BaseInfoFeature> {

    List<BaseInfoFeature> getBySceneKey(String sceneKey);

    void syncToRedis();
}
