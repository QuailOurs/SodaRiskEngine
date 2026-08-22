package com.soda.risk.engine.config.controller;

import com.soda.risk.engine.config.riskdecision.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 风险配置管理Controller - 匹配前端 risk-decision-config-center/risk/* 路径
 */
@RestController
@RequestMapping("/api/risk-decision-config-center/risk")
@RequiredArgsConstructor
public class RiskConfigController {

    private final RiskConfigService riskConfigService;

    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<RiskConfig> query = new LambdaQueryWrapper<>();
        if (reqData != null) {
            Object name = reqData.get("name");
            Object riskKey = reqData.get("riskKey");
            Object businessType = reqData.get("businessType");
            if (name != null && !name.toString().isBlank()) query.like(RiskConfig::getName, name.toString());
            if (riskKey != null && !riskKey.toString().isBlank()) query.like(RiskConfig::getRiskKey, riskKey.toString());
            if (businessType != null && !businessType.toString().isBlank()) {
                query.eq(RiskConfig::getBusinessType, businessType.toString());
            }
        }
        result.put("code", 200);
        result.put("data", riskConfigService.list(query.orderByAsc(RiskConfig::getId)));
        return result;
    }

    @PostMapping("/list/param")
    public Map<String, Object> listByParam(@RequestBody(required = false) Map<String, Object> reqData) {
        return list(reqData);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", riskConfigService.getById(id));
        return result;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody RiskConfig config) {
        Map<String, Object> result = new HashMap<>();
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        if (config.getState() == null) config.setState(1);
        result.put("code", 200);
        result.put("data", riskConfigService.save(config));
        riskConfigService.syncToRedis();
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody RiskConfig config) {
        Map<String, Object> result = new HashMap<>();
        config.setUpdateTime(LocalDateTime.now());
        result.put("code", 200);
        result.put("data", riskConfigService.updateById(config));
        riskConfigService.syncToRedis();
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", riskConfigService.removeById(id));
        riskConfigService.syncToRedis();
        return result;
    }

    @GetMapping("/validExist/name/{name}")
    public Map<String, Object> validNameExist(@PathVariable String name) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", riskConfigService.count(new LambdaQueryWrapper<RiskConfig>()
                .eq(RiskConfig::getName, name)) > 0);
        return result;
    }

    @GetMapping("/validExist/code/{code}")
    public Map<String, Object> validCodeExist(@PathVariable String code) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", riskConfigService.count(new LambdaQueryWrapper<RiskConfig>()
                .eq(RiskConfig::getRiskKey, code)) > 0);
        return result;
    }
}
