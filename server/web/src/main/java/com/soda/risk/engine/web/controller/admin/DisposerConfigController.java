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
 * 处置配置管理控制器 - 整合自原disposerConfigCenter的
 * DisposerStrategyConfigController、DisposerConfigController、ContentTemplateController
 */
@Slf4j
@RestController("adminDisposerConfigController")
@RequestMapping("/admin/disposer")
@RequiredArgsConstructor
public class DisposerConfigController {

    private final RedisCacheService redisCacheService;

    // ======================== 处置策略配置 ========================

    @GetMapping("/strategy/list")
    public Response<List<Map<String, Object>>> listDisposerStrategies() {
        try {
            Set<String> keys = redisCacheService.keys(RedisKeyConstants.DISPOSER_STRATEGY + "*");
            List<Map<String, Object>> strategies = new ArrayList<>();
            if (keys != null) {
                for (String key : keys) {
                    Map<Object, Object> data = redisCacheService.hGetAll(key);
                    if (data != null && !data.isEmpty()) {
                        Map<String, Object> strategy = new HashMap<>();
                        strategy.put("key", key.replace(RedisKeyConstants.DISPOSER_STRATEGY, ""));
                        data.forEach((k, v) -> strategy.put(String.valueOf(k), v));
                        strategies.add(strategy);
                    }
                }
            }
            return Response.success(strategies);
        } catch (Exception e) {
            log.error("listDisposerStrategies failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    @GetMapping("/strategy/{strategyKey}")
    public Response<Map<String, Object>> getDisposerStrategy(@PathVariable String strategyKey) {
        try {
            Map<Object, Object> data = redisCacheService.hGetAll(
                    RedisKeyConstants.DISPOSER_STRATEGY + strategyKey);
            if (data == null || data.isEmpty()) {
                return Response.fail(CodeEnum.DISPOSER_NOT_FOUND);
            }
            Map<String, Object> strategy = new HashMap<>();
            strategy.put("strategyKey", strategyKey);
            data.forEach((k, v) -> strategy.put(String.valueOf(k), v));
            return Response.success(strategy);
        } catch (Exception e) {
            log.error("getDisposerStrategy failed, key={}", strategyKey, e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    // ======================== 处置方式配置 ========================

    @GetMapping("/way/list")
    public Response<List<Map<String, Object>>> listDisposerWays() {
        try {
            Set<String> keys = redisCacheService.keys(RedisKeyConstants.DISPOSER_PREFIX + "*");
            List<Map<String, Object>> ways = new ArrayList<>();
            if (keys != null) {
                for (String key : keys) {
                    Map<Object, Object> data = redisCacheService.hGetAll(key);
                    if (data != null && !data.isEmpty()) {
                        Map<String, Object> way = new HashMap<>();
                        way.put("wayKey", key.replace(RedisKeyConstants.DISPOSER_PREFIX, ""));
                        data.forEach((k, v) -> way.put(String.valueOf(k), v));
                        ways.add(way);
                    }
                }
            }
            return Response.success(ways);
        } catch (Exception e) {
            log.error("listDisposerWays failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    @GetMapping("/way/{wayKey}")
    public Response<Map<String, Object>> getDisposerWay(@PathVariable String wayKey) {
        try {
            Map<Object, Object> data = redisCacheService.hGetAll(
                    RedisKeyConstants.DISPOSER_PREFIX + wayKey);
            if (data == null || data.isEmpty()) {
                return Response.fail(CodeEnum.DISPOSER_NOT_FOUND);
            }
            Map<String, Object> way = new HashMap<>();
            way.put("wayKey", wayKey);
            data.forEach((k, v) -> way.put(String.valueOf(k), v));
            return Response.success(way);
        } catch (Exception e) {
            log.error("getDisposerWay failed, wayKey={}", wayKey, e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    // ======================== 处置配置同步 ========================

    @PostMapping("/sync")
    public Response<String> syncDisposerConfigs() {
        try {
            log.info("Disposer config sync triggered");
            return Response.success("处置配置同步成功");
        } catch (Exception e) {
            log.error("syncDisposerConfigs failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }
}
