package com.soda.risk.engine.config.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soda.risk.engine.config.riskdecision.ReturnCodeService;
import com.soda.risk.engine.config.rule.RuleService;
import com.soda.risk.engine.config.catalog.ConfigurationCatalogSupport;
import com.soda.risk.engine.config.scene.Scene;
import com.soda.risk.engine.config.scene.SceneService;
import com.soda.risk.engine.config.strategy.Strategy;
import com.soda.risk.engine.config.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 策略配置管理Controller - 匹配前端 strategy-engine-config-center/strategy/* 路径
 */
@RestController
@RequestMapping("/api/strategy-engine-config-center/strategy")
@RequiredArgsConstructor
public class StrategyController {

    private final StrategyService strategyService;
    private final ReturnCodeService returnCodeService;
    private final RuleService ruleService;
    private final SceneService sceneService;
    private final ConfigurationCatalogSupport catalogSupport;

    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        LambdaQueryWrapper<Strategy> query = new LambdaQueryWrapper<>();
        if (reqData != null) {
            filterLong(reqData, "id").ifPresent(value -> query.eq(Strategy::getId, value));
            filterString(reqData, "name").ifPresent(value -> query.like(Strategy::getName, value));
            filterString(reqData, "sceneKey").ifPresent(value -> query.eq(Strategy::getSceneKey, value));
            filterString(reqData, "returnCode").ifPresent(value -> query.like(Strategy::getReturnCode, value));
            filterString(reqData, "operator").ifPresent(value -> query.like(Strategy::getOperator, value));
            filterInteger(reqData, "state").ifPresent(value -> query.eq(Strategy::getState, value));
            filterInteger(reqData, "type").ifPresent(value -> query.eq(Strategy::getType, value));
            filterString(reqData, "businessSideKey").ifPresent(value -> {
                List<String> sceneKeys = sceneService.list(new LambdaQueryWrapper<Scene>()
                                .eq(Scene::getBusinessSideKey, value))
                        .stream().map(Scene::getSceneKey).filter(Objects::nonNull).toList();
                if (sceneKeys.isEmpty()) query.eq(Strategy::getSceneKey, "__none__");
                else query.in(Strategy::getSceneKey, sceneKeys);
            });
        }
        List<Strategy> strategies = strategyService.list(query.orderByAsc(Strategy::getPriority)
                .orderByAsc(Strategy::getId));
        Map<Long, List<Long>> ruleIdsByStrategy = strategyService.getRuleIdsMap(
                strategies.stream().map(Strategy::getId).toList());
        strategies.forEach(strategy -> strategy.setRuleIds(
                ruleIdsByStrategy.getOrDefault(strategy.getId(), Collections.emptyList())));
        enrichStrategies(strategies);
        result.put("data", pageIfRequested(strategies, reqData));
        return result;
    }

    @GetMapping("/id/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Strategy strategy = strategyService.getById(id);
        if (strategy != null) {
            strategy.setRuleIds(strategyService.getRuleIds(id));
            List<com.soda.risk.engine.config.rule.Rule> rules = ruleService.getByStrategyId(id);
            rules.forEach(catalogSupport::enrichRule);
            strategy.setRules(rules);
            enrichStrategies(List.of(strategy));
            result.put("code", 200);
            result.put("data", strategy);
        } else {
            result.put("code", 404);
            result.put("msg", "策略不存在");
        }
        return result;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Strategy strategy) {
        Map<String, Object> result = new HashMap<>();
        strategy.setCreateTime(java.time.LocalDateTime.now());
        strategy.setUpdateTime(java.time.LocalDateTime.now());
        if (strategy.getState() == null) strategy.setState(1);
        normalizeType(strategy);
        strategy.setOperator(defaultOperator(strategy.getOperator()));
        strategy.setId(null);
        strategyService.save(strategy);
        strategyService.replaceRules(strategy.getId(), strategy.getRuleIds());
        ruleService.syncToRedis();
        result.put("code", 200);
        result.put("data", strategy);
        return result;
    }

    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody Strategy strategy) {
        Map<String, Object> result = new HashMap<>();
        strategy.setUpdateTime(java.time.LocalDateTime.now());
        normalizeType(strategy);
        strategy.setOperator(defaultOperator(strategy.getOperator()));
        strategyService.updateById(strategy);
        strategyService.replaceRules(strategy.getId(), strategy.getRuleIds());
        ruleService.syncToRedis();
        result.put("code", 200);
        result.put("data", strategy);
        return result;
    }

    @PostMapping("/update/state")
    public Map<String, Object> updateState(@RequestBody Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        Long id = Long.valueOf(reqData.get("id").toString());
        Integer state = Integer.valueOf(reqData.get("state").toString());
        Strategy strategy = strategyService.getById(id);
        if (strategy != null) {
            strategy.setState(state);
            strategy.setUpdateTime(java.time.LocalDateTime.now());
            strategyService.updateById(strategy);
            strategyService.syncToRedis(id);
            ruleService.syncToRedis();
            result.put("code", 200);
            result.put("data", true);
        } else {
            result.put("code", 404);
            result.put("msg", "策略不存在");
        }
        return result;
    }

    @GetMapping("/sceneKey/{sceneKey}")
    public Map<String, Object> getBySceneKey(@PathVariable String sceneKey) {
        Map<String, Object> result = new HashMap<>();
        Strategy strategy = strategyService.getBySceneKey(sceneKey);
        result.put("code", 200);
        result.put("data", strategy);
        return result;
    }

    @GetMapping("/return-code/list/sceneKey/{sceneKey}")
    public Map<String, Object> getReturnCodeListBySceneKey(@PathVariable String sceneKey) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", returnCodeService.getAllReturnCodeBySceneKey(sceneKey));
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        strategyService.replaceRules(id, Collections.emptyList());
        boolean removed = strategyService.removeById(id);
        ruleService.syncToRedis();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("data", removed);
        return result;
    }

    private void normalizeType(Strategy strategy) {
        if (strategy.getType() == null) strategy.setType(strategy.getStrategyType() == null ? 0 : strategy.getStrategyType());
        if (strategy.getStrategyType() == null) strategy.setStrategyType(strategy.getType());
    }

    private Optional<String> filterString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null || value.toString().isBlank() ? Optional.empty() : Optional.of(value.toString());
    }

    private Optional<Long> filterLong(Map<String, Object> data, String key) {
        return filterString(data, key).map(Long::valueOf);
    }

    private Optional<Integer> filterInteger(Map<String, Object> data, String key) {
        return filterString(data, key).map(Integer::valueOf);
    }

    private String defaultOperator(String operator) {
        return operator == null || operator.isBlank() ? "admin" : operator;
    }

    private void enrichStrategies(List<Strategy> strategies) {
        Set<String> sceneKeys = new HashSet<>();
        strategies.stream().map(Strategy::getSceneKey).filter(Objects::nonNull).forEach(sceneKeys::add);
        Map<String, String> businessByScene = new HashMap<>();
        if (!sceneKeys.isEmpty()) {
            for (Scene scene : sceneService.list(new LambdaQueryWrapper<Scene>().in(Scene::getSceneKey, sceneKeys))) {
                businessByScene.putIfAbsent(scene.getSceneKey(), scene.getBusinessSideKey());
            }
        }
        for (Strategy strategy : strategies) {
            strategy.setBusinessSideKey(businessByScene.get(strategy.getSceneKey()));
            strategy.setTypeName(Objects.equals(strategy.getType(), 50) ? "累计过滤" : "普通");
            strategy.setStateName(switch (Objects.requireNonNullElse(strategy.getState(), 0)) {
                case 1 -> "预上线";
                case 2 -> "上线";
                default -> "下线";
            });
            strategy.setExpressionView(strategy.getExpression());
        }
    }

    private Object pageIfRequested(List<Strategy> strategies, Map<String, Object> request) {
        if (request == null || request.get("currentPage") == null || request.get("pageSize") == null) return strategies;
        int current = Math.max(1, Integer.parseInt(request.get("currentPage").toString()));
        int size = Math.max(1, Integer.parseInt(request.get("pageSize").toString()));
        int from = Math.min(strategies.size(), (current - 1) * size);
        int to = Math.min(strategies.size(), from + size);
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("records", strategies.subList(from, to));
        page.put("current", current);
        page.put("size", size);
        page.put("total", strategies.size());
        page.put("pages", (strategies.size() + size - 1) / size);
        return page;
    }
}
