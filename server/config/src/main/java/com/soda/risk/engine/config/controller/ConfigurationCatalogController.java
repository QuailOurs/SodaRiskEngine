package com.soda.risk.engine.config.controller;

import com.soda.risk.engine.config.catalog.ConfigurationCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** 字段、统计特征和参数补全管理接口。 */
@RestController
@RequestMapping("/api/strategy-engine-config-center")
@RequiredArgsConstructor
public class ConfigurationCatalogController {

    private final ConfigurationCatalogService service;

    @PostMapping("/parameter/add")
    public Map<String, Object> addParameter(@RequestBody Map<String, Object> request) {
        return response(service.addParameter(request));
    }

    @PutMapping("/parameter/update")
    public Map<String, Object> updateParameter(@RequestBody Map<String, Object> request) {
        service.updateParameter(request); return response(true);
    }

    @DeleteMapping("/parameter/delete/{id}")
    public Map<String, Object> deleteParameter(@PathVariable long id) {
        service.deleteParameter(id); return response(true);
    }

    @GetMapping("/parameter/existedKey")
    public Map<String, Object> parameterKeyExists(@RequestParam String sceneKey, @RequestParam String paramKey) {
        return response(service.parameterExists(sceneKey, "param_key", paramKey));
    }

    @GetMapping("/parameter/existedName")
    public Map<String, Object> parameterNameExists(@RequestParam String sceneKey, @RequestParam String paramName) {
        return response(service.parameterExists(sceneKey, "name", paramName));
    }

    @PostMapping("/dataType/list")
    public Map<String, Object> dataTypes() { return response(service.dataTypes()); }

    @PostMapping("/featureStatistics/list")
    public Map<String, Object> featureList(@RequestBody(required = false) Map<String, Object> request) {
        return response(service.featureStatisticsList(request == null ? Map.of() : request));
    }

    @GetMapping("/featureStatistics/{id}")
    public Map<String, Object> feature(@PathVariable long id) { return response(service.featureStatistics(id)); }

    @PostMapping("/featureStatistics/add")
    public Map<String, Object> addFeature(@RequestBody Map<String, Object> request) {
        return response(service.addFeatureStatistics(request));
    }

    @PutMapping("/featureStatistics/update")
    public Map<String, Object> updateFeature(@RequestBody Map<String, Object> request) {
        service.updateFeatureStatistics(request); return response(true);
    }

    @PatchMapping("/featureStatistics/enable/{id}")
    public Map<String, Object> enableFeature(@PathVariable long id) { service.updateFeatureState(id, 1); return response(true); }

    @PatchMapping("/featureStatistics/forbidden/{id}")
    public Map<String, Object> disableFeature(@PathVariable long id) { service.updateFeatureState(id, 0); return response(true); }

    @DeleteMapping("/featureStatistics/{id}")
    public Map<String, Object> deleteFeature(@PathVariable long id) { service.deleteFeatureStatistics(id); return response(true); }

    @GetMapping("/featureStatistics/existed")
    public Map<String, Object> featureExists(@RequestParam String sceneKey, @RequestParam String featureStatisticsName) {
        return response(service.featureExists(sceneKey, featureStatisticsName) ? "existed" : "available");
    }

    @PostMapping("/complement/list")
    public Map<String, Object> complementList(@RequestBody(required = false) Map<String, Object> request) {
        return response(service.complementList(request == null ? Map.of() : request));
    }

    @PostMapping("/complement/complementKeyList")
    public Map<String, Object> complementKeys(@RequestBody(required = false) Map<String, Object> request) {
        Long toolId = request == null || request.get("toolId") == null ? null : Long.valueOf(request.get("toolId").toString());
        return response(service.complementKeys(toolId));
    }

    @PostMapping("/complement/addComplementKey")
    public Map<String, Object> addComplementKey(@RequestBody Map<String, Object> request) {
        return response(service.addComplementKey(request));
    }

    @PostMapping("/complement/add")
    public Map<String, Object> addComplement(@RequestBody Map<String, Object> request) {
        return response(service.addComplementRelation(request));
    }

    @PatchMapping("/complement/enable/{id}")
    public Map<String, Object> enableComplement(@PathVariable long id) { service.updateComplementState(id, 1); return response(true); }

    @PatchMapping("/complement/forbidden/{id}")
    public Map<String, Object> disableComplement(@PathVariable long id) { service.updateComplementState(id, 0); return response(true); }

    private Map<String, Object> response(Object data) {
        Map<String, Object> result = new LinkedHashMap<>(); result.put("code", 200); result.put("data", data); return result;
    }
}
