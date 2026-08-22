package com.soda.risk.engine.config.scene;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SceneServiceImpl extends ServiceImpl<SceneMapper, Scene> implements SceneService {

    private final RedisCacheService redisCacheService;

    @Override
    public Scene getBySceneKey(String sceneKey) {
        return getOne(new LambdaQueryWrapper<Scene>()
                .eq(Scene::getSceneKey, sceneKey)
                .eq(Scene::getState, 1)
                .last("LIMIT 1"));
    }

    @Override
    public void syncToRedis() {
        var scenes = list(new LambdaQueryWrapper<Scene>().eq(Scene::getState, 1));
        for (Scene scene : scenes) {
            String key = RedisKeyConstants.SCENE_PREFIX + scene.getSceneKey();
            redisCacheService.setJson(key, scene, 24, TimeUnit.HOURS);
        }
        log.info("Synced {} scenes to Redis", scenes.size());
    }
}
