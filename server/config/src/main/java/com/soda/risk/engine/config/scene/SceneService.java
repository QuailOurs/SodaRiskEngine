package com.soda.risk.engine.config.scene;

import com.baomidou.mybatisplus.extension.service.IService;

public interface SceneService extends IService<Scene> {

    Scene getBySceneKey(String sceneKey);

    void syncToRedis();
}
