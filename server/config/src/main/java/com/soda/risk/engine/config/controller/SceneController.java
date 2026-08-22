package com.soda.risk.engine.config.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soda.risk.engine.config.business.BusinessSide;
import com.soda.risk.engine.config.business.BusinessSideService;
import com.soda.risk.engine.config.scene.Scene;
import com.soda.risk.engine.config.scene.SceneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 场景配置管理Controller - 匹配前端 strategy-engine-config-center/scene/* 路径
 */
@RestController
@RequestMapping("/api/strategy-engine-config-center/scene")
@RequiredArgsConstructor
public class SceneController {

    private final SceneService sceneService;
    private final BusinessSideService businessSideService;

    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        LambdaQueryWrapper<Scene> query = new LambdaQueryWrapper<>();
        if (reqData != null) {
            filter(reqData, "name").ifPresent(value -> query.like(Scene::getName, value));
            filter(reqData, "sceneKey").ifPresent(value -> query.like(Scene::getSceneKey, value));
            filter(reqData, "businessSideKey").ifPresent(value -> query.eq(Scene::getBusinessSideKey, value));
            filter(reqData, "state").map(Integer::valueOf).ifPresent(value -> query.eq(Scene::getState, value));
        }
        List<Scene> scenes = sceneService.list(query.orderByAsc(Scene::getId));
        Map<String, String> businessNames = new HashMap<>();
        for (BusinessSide business : businessSideService.list()) {
            businessNames.put(business.getBusinessSideKey(), business.getName());
        }
        scenes.forEach(scene -> scene.setBusinessSideName(businessNames.get(scene.getBusinessSideKey())));
        result.put("data", pageIfRequested(scenes, reqData));
        return result;
    }

    @GetMapping("/list/{systemKey}")
    public Map<String, Object> listBySystemKey(@PathVariable String systemKey) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        List<String> businessKeys = businessSideService.list(new LambdaQueryWrapper<BusinessSide>()
                        .eq(BusinessSide::getSystemKey, systemKey))
                .stream().map(BusinessSide::getBusinessSideKey).toList();
        List<Scene> scenes = businessKeys.isEmpty() ? Collections.emptyList()
                : sceneService.list(new LambdaQueryWrapper<Scene>().in(Scene::getBusinessSideKey, businessKeys));
        result.put("data", scenes);
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", sceneService.getById(id));
        return result;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Scene scene) {
        Map<String, Object> result = new HashMap<>();
        scene.setCreateTime(LocalDateTime.now());
        scene.setUpdateTime(LocalDateTime.now());
        if (scene.getState() == null) scene.setState(1);
        scene.setOperator(defaultOperator(scene.getOperator()));
        scene.setId(null);
        sceneService.save(scene);
        sceneService.syncToRedis();
        result.put("code", 200);
        result.put("data", scene);
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Scene scene) {
        Map<String, Object> result = new HashMap<>();
        scene.setUpdateTime(LocalDateTime.now());
        scene.setOperator(defaultOperator(scene.getOperator()));
        sceneService.updateById(scene);
        sceneService.syncToRedis();
        result.put("code", 200);
        result.put("data", scene);
        return result;
    }

    @GetMapping("/existed")
    public Map<String, Object> existed(@RequestParam String businessSide, @RequestParam String name) {
        Map<String, Object> result = new HashMap<>();
        long count = sceneService.count(new LambdaQueryWrapper<Scene>()
                .eq(Scene::getBusinessSideKey, businessSide)
                .eq(Scene::getName, name));
        result.put("code", 200);
        result.put("data", count > 0);
        return result;
    }

    @GetMapping("/sceneName/list")
    public Map<String, Object> sceneNameList() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Scene scene : sceneService.list()) {
            Map<String, Object> item = new HashMap<>();
            item.put("sceneKey", scene.getSceneKey());
            item.put("sceneName", scene.getName());
            item.put("businessKey", scene.getBusinessSideKey());
            list.add(item);
        }
        result.put("data", list);
        return result;
    }

    @GetMapping("/sceneName/list/{businessSideKey}")
    public Map<String, Object> sceneNameListByBusinessSide(@PathVariable String businessSideKey) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Scene scene : sceneService.list(new LambdaQueryWrapper<Scene>()
                .eq(Scene::getBusinessSideKey, businessSideKey)
                .orderByAsc(Scene::getId))) {
            Map<String, Object> item = new HashMap<>();
            item.put("sceneKey", scene.getSceneKey());
            item.put("sceneName", scene.getName());
            item.put("businessKey", scene.getBusinessSideKey());
            list.add(item);
        }
        result.put("data", list);
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", sceneService.removeById(id));
        sceneService.syncToRedis();
        return result;
    }

    private Optional<String> filter(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null || value.toString().isBlank() ? Optional.empty() : Optional.of(value.toString());
    }

    private String defaultOperator(String operator) {
        return operator == null || operator.isBlank() ? "admin" : operator;
    }

    private Object pageIfRequested(List<Scene> scenes, Map<String, Object> request) {
        if (request == null || request.get("currentPage") == null || request.get("pageSize") == null) return scenes;
        int current = Math.max(1, Integer.parseInt(request.get("currentPage").toString()));
        int size = Math.max(1, Integer.parseInt(request.get("pageSize").toString()));
        int from = Math.min(scenes.size(), (current - 1) * size); int to = Math.min(scenes.size(), from + size);
        Map<String, Object> page = new LinkedHashMap<>(); page.put("records", scenes.subList(from, to));
        page.put("current", current); page.put("size", size); page.put("total", scenes.size());
        page.put("pages", (scenes.size() + size - 1) / size); return page;
    }
}
