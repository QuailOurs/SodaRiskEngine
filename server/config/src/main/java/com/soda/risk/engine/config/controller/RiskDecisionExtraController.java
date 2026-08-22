package com.soda.risk.engine.config.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 风险决策概览/命中日志/查询Controller - 匹配前端 risk-decision-config-center/overview/*, hitLog/*, query/* 路径
 */
@RestController
@RequestMapping("/api/risk-decision-config-center")
public class RiskDecisionExtraController {

    @PostMapping("/overview/hit")
    public Map<String, Object> overviewHit(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", Collections.emptyList());
        return result;
    }

    @PostMapping("/overview/level")
    public Map<String, Object> overviewLevel(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", Collections.emptyList());
        return result;
    }

    @PostMapping("/overview/risk")
    public Map<String, Object> overviewRisk(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", Collections.emptyList());
        return result;
    }

    @PostMapping("/hitLog/list")
    public Map<String, Object> hitLogList(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("records", Collections.emptyList());
        data.put("total", 0);
        data.put("current", 1);
        data.put("size", 20);
        result.put("data", data);
        return result;
    }

    @PostMapping("/query/riskIdentification/sceneKey/{sceneKey}")
    public Map<String, Object> queryRiskIdentification(@PathVariable String sceneKey,
                                                       @RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("records", Collections.emptyList());
        data.put("total", 0);
        data.put("current", 1);
        data.put("size", 20);
        result.put("data", data);
        return result;
    }
}
