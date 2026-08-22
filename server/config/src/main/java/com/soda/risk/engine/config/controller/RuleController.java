package com.soda.risk.engine.config.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soda.risk.engine.config.rule.Rule;
import com.soda.risk.engine.config.rule.RuleService;
import com.soda.risk.engine.config.catalog.ConfigurationCatalogSupport;
import com.soda.risk.engine.config.strategy.Strategy;
import com.soda.risk.engine.config.strategy.StrategyRuleRelation;
import com.soda.risk.engine.config.strategy.StrategyRuleRelationMapper;
import com.soda.risk.engine.config.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 规则配置管理Controller - 匹配前端 strategy-engine-config-center/rule/* 路径
 */
@RestController
@RequestMapping("/api/strategy-engine-config-center/rule")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;
    private final StrategyRuleRelationMapper relationMapper;
    private final StrategyService strategyService;
    private final ConfigurationCatalogSupport catalogSupport;

    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        LambdaQueryWrapper<Rule> query = new LambdaQueryWrapper<>();
        if (reqData != null) {
            filterString(reqData, "id").map(Long::valueOf).ifPresent(value -> query.eq(Rule::getId, value));
            filterString(reqData, "name").ifPresent(value -> query.like(Rule::getName, value));
            filterString(reqData, "sceneKey").ifPresent(value -> query.eq(Rule::getSceneKey, value));
            filterString(reqData, "ruleType").ifPresent(value -> query.eq(Rule::getRuleType, value));
            filterString(reqData, "type").map(Integer::valueOf).ifPresent(value -> query.eq(Rule::getType, value));
            filterString(reqData, "expression").ifPresent(value -> query.like(Rule::getExpression, value));
            filterString(reqData, "description").ifPresent(value -> query.like(Rule::getDescription, value));
            filterString(reqData, "state").map(Integer::valueOf).ifPresent(value -> query.eq(Rule::getState, value));
            filterString(reqData, "strategyType").map(Integer::valueOf).ifPresent(value -> {
                if (value == 50) query.eq(Rule::getType, 50);
                else query.ne(Rule::getType, 50);
            });
        }
        List<Rule> rules = ruleService.list(query.orderByAsc(Rule::getId));
        rules.forEach(catalogSupport::enrichRule);
        result.put("data", pageIfRequested(rules, reqData));
        return result;
    }

    @GetMapping("/list/strategyId/{strategyId}")
    public Map<String, Object> listByStrategyId(@PathVariable Long strategyId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        List<Rule> rules = ruleService.getByStrategyId(strategyId);
        rules.forEach(catalogSupport::enrichRule);
        result.put("data", rules);
        return result;
    }

    @GetMapping("/detail/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", catalogSupport.enrichRule(ruleService.getById(id)));
        return result;
    }

    @GetMapping("/freelist/sceneKey/{sceneKey}")
    public Map<String, Object> freeListBySceneKey(@PathVariable String sceneKey) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        Set<Long> usedRuleIds = relationMapper.selectList(null).stream()
                .map(StrategyRuleRelation::getRuleId)
                .collect(java.util.stream.Collectors.toSet());
        result.put("data", ruleService.list(new LambdaQueryWrapper<Rule>()
                .eq(Rule::getSceneKey, sceneKey)
                .notIn(!usedRuleIds.isEmpty(), Rule::getId, usedRuleIds)
                .orderByAsc(Rule::getId)));
        return result;
    }

    @GetMapping("/list/sceneKey/{sceneKey}")
    public Map<String, Object> listBySceneKey(@PathVariable String sceneKey) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", ruleService.list(new LambdaQueryWrapper<Rule>()
                .eq(Rule::getSceneKey, sceneKey)
                .orderByAsc(Rule::getId)));
        return result;
    }

    @PostMapping("/list/ruleIds")
    public Map<String, Object> listByRuleIds(@RequestBody Object reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        Object rawRuleIds = reqData instanceof Map<?, ?> map ? map.get("ruleIds") : reqData;
        if (rawRuleIds instanceof Collection<?> values) {
            List<Long> ids = values.stream().map(value -> Long.valueOf(value.toString())).toList();
            List<Rule> rules = ids.isEmpty() ? Collections.emptyList() : ruleService.listByIds(ids);
            rules.forEach(catalogSupport::enrichRule);
            result.put("data", rules);
        } else {
            result.put("data", Collections.emptyList());
        }
        return result;
    }

    @GetMapping("/getCascaderDataGroupByRuleType/{sceneKey}/ruleType/{ruleType}")
    public Map<String, Object> getCascaderData(@PathVariable String sceneKey, @PathVariable String ruleType) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        int type = Integer.parseInt(ruleType);
        result.put("data", catalogSupport.ruleOperandCascader(sceneKey, type));
        return result;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Rule rule) {
        Map<String, Object> result = new HashMap<>();
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        if (rule.getState() == null) rule.setState(1);
        normalizeRule(rule);
        catalogSupport.prepareRuleForSave(rule);
        rule.setId(null);
        ruleService.save(rule);
        ruleService.syncToRedis();
        result.put("code", 200);
        result.put("data", rule);
        return result;
    }

    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody Rule rule) {
        Map<String, Object> result = new HashMap<>();
        rule.setUpdateTime(LocalDateTime.now());
        normalizeRule(rule);
        catalogSupport.prepareRuleForSave(rule);
        ruleService.updateById(rule);
        ruleService.syncToRedis();
        result.put("code", 200);
        result.put("data", rule);
        return result;
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        relationMapper.delete(new LambdaQueryWrapper<StrategyRuleRelation>()
                .eq(StrategyRuleRelation::getRuleId, id));
        result.put("data", ruleService.removeById(id));
        ruleService.syncToRedis();
        return result;
    }

    @GetMapping("/validExist/{sceneKey}/{name}")
    public Map<String, Object> validExist(@PathVariable String sceneKey, @PathVariable String name) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", ruleService.count(new LambdaQueryWrapper<Rule>()
                .eq(Rule::getSceneKey, sceneKey)
                .eq(Rule::getName, name)) > 0);
        return result;
    }

    @GetMapping("/relation/strategy/{ruleId}")
    public Map<String, Object> relationStrategy(@PathVariable Long ruleId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        List<Long> strategyIds = relationMapper.selectList(new LambdaQueryWrapper<StrategyRuleRelation>()
                        .eq(StrategyRuleRelation::getRuleId, ruleId))
                .stream().map(StrategyRuleRelation::getStrategyId).toList();
        result.put("data", strategyIds.isEmpty() ? Collections.emptyList() : strategyService.listByIds(strategyIds));
        return result;
    }

    private void normalizeRule(Rule rule) {
        if (rule.getRuleType() == null || rule.getRuleType().isBlank()) rule.setRuleType("EXPRESSION");
        if (rule.getRuleKey() == null || rule.getRuleKey().isBlank()) {
            rule.setRuleKey("rule_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        }
        if (rule.getType() == null) rule.setType(0);
        if (rule.getOperator() == null || rule.getOperator().isBlank()) rule.setOperator("admin");
    }

    private Optional<String> filterString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null || value.toString().isBlank() ? Optional.empty() : Optional.of(value.toString());
    }

    private Object pageIfRequested(List<Rule> rules, Map<String, Object> request) {
        if (request == null || request.get("currentPage") == null || request.get("pageSize") == null) return rules;
        int current = Math.max(1, Integer.parseInt(request.get("currentPage").toString()));
        int size = Math.max(1, Integer.parseInt(request.get("pageSize").toString()));
        int from = Math.min(rules.size(), (current - 1) * size);
        int to = Math.min(rules.size(), from + size);
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("records", rules.subList(from, to));
        page.put("current", current);
        page.put("size", size);
        page.put("total", rules.size());
        page.put("pages", (rules.size() + size - 1) / size);
        return page;
    }
}
