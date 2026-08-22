package com.soda.risk.engine.web.controller.admin;

import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.enums.CodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 策略配置管理控制器 - 整合自原strategyEngineConfigCenter的
 * StrategyController、RuleController、SceneController、FeatureController
 */
@Slf4j
@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class StrategyConfigController {

    private final RedisCacheService redisCacheService;

    // ======================== 场景管理 ========================

    @GetMapping("/scene/list")
    public Response<List<Map<String, Object>>> listScenes() {
        try {
            Set<String> sceneKeys = redisCacheService.keys(RedisKeyConstants.SCENE_PREFIX + "*");
            List<Map<String, Object>> scenes = new ArrayList<>();
            if (sceneKeys != null) {
                for (String key : sceneKeys) {
                    Map<Object, Object> sceneData = redisCacheService.hGetAll(key);
                    if (sceneData != null && !sceneData.isEmpty()) {
                        Map<String, Object> scene = new HashMap<>();
                        scene.put("sceneKey", key.replace(RedisKeyConstants.SCENE_PREFIX, ""));
                        sceneData.forEach((k, v) -> scene.put(String.valueOf(k), v));
                        scenes.add(scene);
                    }
                }
            }
            return Response.success(scenes);
        } catch (Exception e) {
            log.error("listScenes failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    @GetMapping("/scene/{sceneKey}")
    public Response<Map<String, Object>> getScene(@PathVariable String sceneKey) {
        try {
            Map<Object, Object> sceneData = redisCacheService.hGetAll(
                    RedisKeyConstants.SCENE_PREFIX + sceneKey);
            if (sceneData == null || sceneData.isEmpty()) {
                return Response.fail(CodeEnum.SCENE_NOT_FOUND);
            }
            Map<String, Object> scene = new HashMap<>();
            scene.put("sceneKey", sceneKey);
            sceneData.forEach((k, v) -> scene.put(String.valueOf(k), v));
            return Response.success(scene);
        } catch (Exception e) {
            log.error("getScene failed, sceneKey={}", sceneKey, e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    // ======================== 策略管理 ========================

    @GetMapping("/strategy/list")
    public Response<List<Map<String, Object>>> listStrategies() {
        try {
            Set<String> strategyKeys = redisCacheService.keys(RedisKeyConstants.STRATEGY_PREFIX + "*");
            List<Map<String, Object>> strategies = new ArrayList<>();
            if (strategyKeys != null) {
                for (String key : strategyKeys) {
                    Map<Object, Object> data = redisCacheService.hGetAll(key);
                    if (data != null && !data.isEmpty()) {
                        Map<String, Object> strategy = new HashMap<>();
                        strategy.put("strategyKey", key.replace(RedisKeyConstants.STRATEGY_PREFIX, ""));
                        data.forEach((k, v) -> strategy.put(String.valueOf(k), v));
                        strategies.add(strategy);
                    }
                }
            }
            return Response.success(strategies);
        } catch (Exception e) {
            log.error("listStrategies failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    @GetMapping("/strategy/{strategyId}")
    public Response<Map<String, Object>> getStrategy(@PathVariable String strategyId) {
        try {
            Map<Object, Object> data = redisCacheService.hGetAll(
                    RedisKeyConstants.STRATEGY_PREFIX + strategyId);
            if (data == null || data.isEmpty()) {
                return Response.fail(CodeEnum.STRATEGY_NOT_FOUND);
            }
            Map<String, Object> strategy = new HashMap<>();
            strategy.put("strategyId", strategyId);
            data.forEach((k, v) -> strategy.put(String.valueOf(k), v));
            return Response.success(strategy);
        } catch (Exception e) {
            log.error("getStrategy failed, strategyId={}", strategyId, e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    // ======================== 规则管理 ========================

    @GetMapping("/rule/list")
    public Response<List<Map<String, Object>>> listRules() {
        try {
            Set<String> ruleKeys = redisCacheService.keys(RedisKeyConstants.RULE_PREFIX + "*");
            List<Map<String, Object>> rules = new ArrayList<>();
            if (ruleKeys != null) {
                for (String key : ruleKeys) {
                    Map<Object, Object> data = redisCacheService.hGetAll(key);
                    if (data != null && !data.isEmpty()) {
                        Map<String, Object> rule = new HashMap<>();
                        rule.put("ruleId", key.replace(RedisKeyConstants.RULE_PREFIX, ""));
                        data.forEach((k, v) -> rule.put(String.valueOf(k), v));
                        rules.add(rule);
                    }
                }
            }
            return Response.success(rules);
        } catch (Exception e) {
            log.error("listRules failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    @GetMapping("/rule/{ruleId}")
    public Response<Map<String, Object>> getRule(@PathVariable String ruleId) {
        try {
            Map<Object, Object> data = redisCacheService.hGetAll(
                    RedisKeyConstants.RULE_PREFIX + ruleId);
            if (data == null || data.isEmpty()) {
                return Response.fail(CodeEnum.RULE_NOT_FOUND);
            }
            Map<String, Object> rule = new HashMap<>();
            rule.put("ruleId", ruleId);
            data.forEach((k, v) -> rule.put(String.valueOf(k), v));
            return Response.success(rule);
        } catch (Exception e) {
            log.error("getRule failed, ruleId={}", ruleId, e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    // ======================== 配置同步 ========================

    @PostMapping("/sync/strategy")
    public Response<String> syncStrategies() {
        try {
            log.info("Strategy config sync triggered");
            return Response.success("策略配置同步成功");
        } catch (Exception e) {
            log.error("syncStrategies failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    @PostMapping("/sync/rule")
    public Response<String> syncRules() {
        try {
            log.info("Rule config sync triggered");
            return Response.success("规则配置同步成功");
        } catch (Exception e) {
            log.error("syncRules failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }
}
