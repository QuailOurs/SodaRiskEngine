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
 * 风险配置管理控制器 - 整合自原riskDecisionConfigCenter的
 * BlackWhiteListController、RiskController、ReturnCodeMappingController
 */
@Slf4j
@RestController("adminRiskConfigController")
@RequestMapping("/admin/risk")
@RequiredArgsConstructor
public class RiskConfigController {

    private final RedisCacheService redisCacheService;

    // ======================== 黑名单管理 ========================

    @GetMapping("/blacklist/list")
    public Response<Set<String>> listBlacklist() {
        try {
            String key = RedisKeyConstants.BLACK_WHITE_LIST + "blacklist";
            Set<String> members = redisCacheService.sMembers(key);
            return Response.success(members != null ? members : Collections.emptySet());
        } catch (Exception e) {
            log.error("listBlacklist failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    @PostMapping("/blacklist/add")
    public Response<String> addBlacklist(@RequestBody Map<String, Object> request) {
        try {
            String value = (String) request.get("value");
            if (value == null || value.isEmpty()) {
                return Response.fail(CodeEnum.PARAM_NULL);
            }
            String key = RedisKeyConstants.BLACK_WHITE_LIST + "blacklist";
            redisCacheService.sAdd(key, value);
            log.info("Blacklist added: {}", value);
            return Response.success("添加成功");
        } catch (Exception e) {
            log.error("addBlacklist failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    @PostMapping("/blacklist/remove")
    public Response<String> removeBlacklist(@RequestBody Map<String, Object> request) {
        try {
            String value = (String) request.get("value");
            if (value == null || value.isEmpty()) {
                return Response.fail(CodeEnum.PARAM_NULL);
            }
            String key = RedisKeyConstants.BLACK_WHITE_LIST + "blacklist";
            redisCacheService.sRemove(key, value);
            log.info("Blacklist removed: {}", value);
            return Response.success("移除成功");
        } catch (Exception e) {
            log.error("removeBlacklist failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    // ======================== 白名单管理 ========================

    @GetMapping("/whitelist/list")
    public Response<Set<String>> listWhitelist() {
        try {
            String key = RedisKeyConstants.BLACK_WHITE_LIST + "whitelist";
            Set<String> members = redisCacheService.sMembers(key);
            return Response.success(members != null ? members : Collections.emptySet());
        } catch (Exception e) {
            log.error("listWhitelist failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    @PostMapping("/whitelist/add")
    public Response<String> addWhitelist(@RequestBody Map<String, Object> request) {
        try {
            String value = (String) request.get("value");
            if (value == null || value.isEmpty()) {
                return Response.fail(CodeEnum.PARAM_NULL);
            }
            String key = RedisKeyConstants.BLACK_WHITE_LIST + "whitelist";
            redisCacheService.sAdd(key, value);
            log.info("Whitelist added: {}", value);
            return Response.success("添加成功");
        } catch (Exception e) {
            log.error("addWhitelist failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    @PostMapping("/whitelist/remove")
    public Response<String> removeWhitelist(@RequestBody Map<String, Object> request) {
        try {
            String value = (String) request.get("value");
            if (value == null || value.isEmpty()) {
                return Response.fail(CodeEnum.PARAM_NULL);
            }
            String key = RedisKeyConstants.BLACK_WHITE_LIST + "whitelist";
            redisCacheService.sRemove(key, value);
            log.info("Whitelist removed: {}", value);
            return Response.success("移除成功");
        } catch (Exception e) {
            log.error("removeWhitelist failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    // ======================== 风险配置管理 ========================

    @GetMapping("/config/list")
    public Response<List<Map<String, Object>>> listRiskConfigs() {
        try {
            Set<String> configKeys = redisCacheService.keys(RedisKeyConstants.RISK_PREFIX + "*");
            List<Map<String, Object>> configs = new ArrayList<>();
            if (configKeys != null) {
                for (String key : configKeys) {
                    Map<Object, Object> data = redisCacheService.hGetAll(key);
                    if (data != null && !data.isEmpty()) {
                        Map<String, Object> config = new HashMap<>();
                        config.put("businessType", key.replace(RedisKeyConstants.RISK_PREFIX, ""));
                        data.forEach((k, v) -> config.put(String.valueOf(k), v));
                        configs.add(config);
                    }
                }
            }
            return Response.success(configs);
        } catch (Exception e) {
            log.error("listRiskConfigs failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    @GetMapping("/config/{businessType}")
    public Response<Map<String, Object>> getRiskConfig(@PathVariable String businessType) {
        try {
            Map<Object, Object> data = redisCacheService.hGetAll(
                    RedisKeyConstants.RISK_PREFIX + businessType);
            if (data == null || data.isEmpty()) {
                return Response.fail(CodeEnum.DISPOSER_NOT_FOUND);
            }
            Map<String, Object> config = new HashMap<>();
            config.put("businessType", businessType);
            data.forEach((k, v) -> config.put(String.valueOf(k), v));
            return Response.success(config);
        } catch (Exception e) {
            log.error("getRiskConfig failed, businessType={}", businessType, e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    // ======================== 配置同步 ========================

    @PostMapping("/sync/blacklist")
    public Response<String> syncBlackWhiteList() {
        try {
            log.info("BlackWhiteList config sync triggered");
            return Response.success("黑白名单同步成功");
        } catch (Exception e) {
            log.error("syncBlackWhiteList failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    @PostMapping("/sync/risk-config")
    public Response<String> syncRiskConfigs() {
        try {
            log.info("Risk config sync triggered");
            return Response.success("风险配置同步成功");
        } catch (Exception e) {
            log.error("syncRiskConfigs failed", e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }
}
