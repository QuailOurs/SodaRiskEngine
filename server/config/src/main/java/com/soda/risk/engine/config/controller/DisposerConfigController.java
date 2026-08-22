package com.soda.risk.engine.config.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soda.risk.engine.config.disposer.DisposerConfig;
import com.soda.risk.engine.config.disposer.DisposerConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 处置配置管理Controller - 匹配前端 disposer-config-center/disposerConfig/* 路径
 */
@RestController
@RequestMapping("/api/disposer-config-center/disposerConfig")
@RequiredArgsConstructor
public class DisposerConfigController {

    private final DisposerConfigService disposerConfigService;

    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<DisposerConfig> query = new LambdaQueryWrapper<>();
        if (reqData != null) {
            Object name = reqData.get("name");
            Object disposerKey = reqData.get("disposerKey");
            Object disposerType = reqData.get("disposerType");
            if (name != null && !name.toString().isBlank()) query.like(DisposerConfig::getName, name.toString());
            if (disposerKey != null && !disposerKey.toString().isBlank()) {
                query.like(DisposerConfig::getDisposerKey, disposerKey.toString());
            }
            if (disposerType != null && !disposerType.toString().isBlank()) {
                query.eq(DisposerConfig::getDisposerType, disposerType.toString());
            }
        }
        result.put("code", 200);
        result.put("data", disposerConfigService.list(query.orderByAsc(DisposerConfig::getId)));
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> listGet() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", disposerConfigService.list());
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", disposerConfigService.getById(id));
        return result;
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody DisposerConfig config) {
        Map<String, Object> result = new HashMap<>();
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        if (config.getState() == null) config.setState(1);
        if (config.getOperator() == null || config.getOperator().isBlank()) config.setOperator("admin");
        config.setId(null);
        disposerConfigService.save(config);
        disposerConfigService.syncToRedis();
        result.put("code", 200);
        result.put("data", config);
        return result;
    }

    @PutMapping
    public Map<String, Object> update(@RequestBody DisposerConfig config) {
        Map<String, Object> result = new HashMap<>();
        config.setUpdateTime(LocalDateTime.now());
        if (config.getOperator() == null || config.getOperator().isBlank()) config.setOperator("admin");
        disposerConfigService.updateById(config);
        disposerConfigService.syncToRedis();
        result.put("code", 200);
        result.put("data", config);
        return result;
    }

    @PutMapping("/status")
    public Map<String, Object> updateStatus(@RequestBody Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        Long id = Long.valueOf(reqData.get("id").toString());
        Integer state = Integer.valueOf(reqData.get("state").toString());
        DisposerConfig config = disposerConfigService.getById(id);
        if (config != null) {
            config.setState(state);
            config.setUpdateTime(LocalDateTime.now());
            disposerConfigService.updateById(config);
            disposerConfigService.syncToRedis();
            result.put("code", 200);
            result.put("data", true);
        } else {
            result.put("code", 404);
            result.put("msg", "配置不存在");
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", disposerConfigService.removeById(id));
        disposerConfigService.syncToRedis();
        return result;
    }
}
