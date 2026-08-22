package com.soda.risk.engine.config.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 内容模板Controller - 匹配前端 disposer-config-center/contentTemplate/* 路径
 */
@RestController
@RequestMapping("/api/disposer-config-center/contentTemplate")
public class ContentTemplateController {

    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", Collections.emptyList());
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", null);
        return result;
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", true);
        return result;
    }

    @PutMapping
    public Map<String, Object> update(@RequestBody Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", true);
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", true);
        return result;
    }
}
