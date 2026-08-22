package com.soda.risk.engine.config.controller;

import com.soda.risk.engine.config.strategy.Strategy;
import com.soda.risk.engine.config.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 策略引擎顶级路由 - 匹配前端 strategy-engine-config-center/list/* 路径
 */
@RestController
@RequestMapping("/api/strategy-engine-config-center")
@RequiredArgsConstructor
public class StrategyEngineTopController {

    private final StrategyService strategyService;

    @GetMapping("/list/sceneKey/{sceneKey}")
    public Map<String, Object> getListBySceneKey(@PathVariable String sceneKey) {
        Map<String, Object> result = new HashMap<>();
        Strategy strategy = strategyService.getBySceneKey(sceneKey);
        result.put("code", 200);
        result.put("data", strategy != null ? Collections.singletonList(strategy) : Collections.emptyList());
        return result;
    }
}
