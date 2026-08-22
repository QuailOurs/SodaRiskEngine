package com.soda.risk.engine.config.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soda.risk.engine.config.feature.BaseInfoFeature;
import com.soda.risk.engine.config.feature.BaseInfoFeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 特征管理Controller - 匹配前端 feature-operation-center/feature/* 路径
 */
@RestController
@RequestMapping("/api/feature-operation-center/feature")
@RequiredArgsConstructor
public class FeatureController {

    private final BaseInfoFeatureService featureService;

    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        LambdaQueryWrapper<BaseInfoFeature> query = new LambdaQueryWrapper<>();
        if (reqData != null) {
            filter(reqData, "name").ifPresent(value -> query.like(BaseInfoFeature::getName, value));
            filter(reqData, "featureKey").ifPresent(value -> query.like(BaseInfoFeature::getFeatureKey, value));
            filter(reqData, "featureType").ifPresent(value -> query.eq(BaseInfoFeature::getFeatureType, value));
            filter(reqData, "sceneKey").ifPresent(value -> query.eq(BaseInfoFeature::getSceneKey, value));
            filter(reqData, "state").map(Integer::valueOf).ifPresent(value -> query.eq(BaseInfoFeature::getState, value));
        }
        result.put("data", featureService.list(query.orderByAsc(BaseInfoFeature::getId)));
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
        result.put("data", featureService.getById(id));
        return result;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody BaseInfoFeature feature) {
        Map<String, Object> result = new HashMap<>();
        feature.setCreateTime(LocalDateTime.now());
        feature.setUpdateTime(LocalDateTime.now());
        if (feature.getState() == null) feature.setState(1);
        if (feature.getOperator() == null || feature.getOperator().isBlank()) feature.setOperator("admin");
        feature.setId(null);
        featureService.save(feature);
        featureService.syncToRedis();
        result.put("code", 200);
        result.put("data", feature);
        return result;
    }

    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody BaseInfoFeature feature) {
        Map<String, Object> result = new HashMap<>();
        feature.setUpdateTime(LocalDateTime.now());
        if (feature.getOperator() == null || feature.getOperator().isBlank()) feature.setOperator("admin");
        featureService.updateById(feature);
        featureService.syncToRedis();
        result.put("code", 200);
        result.put("data", feature);
        return result;
    }

    @PostMapping("/update/state")
    public Map<String, Object> updateState(@RequestBody Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        Long id = Long.valueOf(reqData.get("id").toString());
        Integer state = Integer.valueOf(reqData.get("state").toString());
        BaseInfoFeature feature = featureService.getById(id);
        if (feature != null) {
            feature.setState(state);
            feature.setUpdateTime(LocalDateTime.now());
            featureService.updateById(feature);
            featureService.syncToRedis();
            result.put("code", 200);
            result.put("data", true);
        } else {
            result.put("code", 404);
            result.put("msg", "特征不存在");
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", featureService.removeById(id));
        featureService.syncToRedis();
        return result;
    }

    @GetMapping("/validExist/{featureKey}")
    public Map<String, Object> validExist(@PathVariable String featureKey) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", featureService.count(new LambdaQueryWrapper<BaseInfoFeature>()
                .eq(BaseInfoFeature::getFeatureKey, featureKey)) > 0);
        return result;
    }

    private Optional<String> filter(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null || value.toString().isBlank() ? Optional.empty() : Optional.of(value.toString());
    }
}
