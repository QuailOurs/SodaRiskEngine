package com.soda.risk.engine.service.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soda.risk.engine.api.dto.*;
import com.soda.risk.engine.common.enums.CodeEnum;
import com.soda.risk.engine.config.rule.Rule;
import com.soda.risk.engine.config.scene.Scene;
import com.soda.risk.engine.config.scene.SceneService;
import com.soda.risk.engine.config.strategy.Strategy;
import com.soda.risk.engine.config.strategy.StrategyRuleRelation;
import com.soda.risk.engine.config.strategy.StrategyRuleRelationMapper;
import com.soda.risk.engine.config.strategy.StrategyService;
import com.soda.risk.engine.core.strategy.complement.DataComplementResult;
import com.soda.risk.engine.core.strategy.complement.DataComplementService;
import com.soda.risk.engine.core.strategy.feature.FeatureQueryResult;
import com.soda.risk.engine.core.strategy.feature.FeatureService;
import com.soda.risk.engine.core.strategy.rule.RuleExpressionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * 从配置数据库构建不可变运行时快照，并按原引擎语义计算规则与策略。
 * 快照整体替换，决策线程不会观察到加载一半的配置。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyRuntimeEngine {

    private final SceneService sceneService;
    private final StrategyService strategyService;
    private final com.soda.risk.engine.config.rule.RuleService ruleConfigService;
    private final StrategyRuleRelationMapper relationMapper;
    private final JdbcTemplate jdbcTemplate;
    private final RuleExpressionEvaluator expressionEvaluator;
    private final DataComplementService dataComplementService;
    private final FeatureService featureService;

    private final AtomicReference<RuntimeSnapshot> snapshot =
            new AtomicReference<>(RuntimeSnapshot.empty());
    private final AtomicLong versions = new AtomicLong();

    @EventListener(ApplicationReadyEvent.class)
    public void loadOnStartup() {
        reload();
    }

    @Scheduled(fixedDelayString = "${soda.engine.config-refresh-ms:30000}")
    public void scheduledReload() {
        reload();
    }

    public synchronized EngineConfigStatus reload() {
        List<Scene> scenes = sceneService.list(new LambdaQueryWrapper<Scene>()
                .eq(Scene::getState, 1));
        List<Strategy> strategies = strategyService.list(new LambdaQueryWrapper<Strategy>()
                .in(Strategy::getState, List.of(1, 2)));
        List<Rule> rules = ruleConfigService.list(new LambdaQueryWrapper<Rule>()
                .eq(Rule::getState, 1));
        List<StrategyRuleRelation> relations = relationMapper.selectList(null);

        Map<Long, String> parameterNames = loadNameMap(
                "SELECT id,param_key FROM t_catalog_param", "param_key");
        Map<Long, String> listFeatureNames = loadNameMap(
                "SELECT id,name FROM t_catalog_feature_list", "name");
        Map<Long, String> statisticsFeatureNames = loadNameMap(
                "SELECT id,name FROM t_catalog_feature_statistics", "name");
        Map<Long, BaseFeatureOperand> baseFeatureOperands = loadBaseFeatureOperands();
        Map<Long, String> currentFeatureNames = loadFeatureKeyMap();

        Map<Long, RuntimeRule> runtimeRules = new LinkedHashMap<>();
        for (Rule rule : rules) {
            RuntimeRule runtimeRule = toRuntimeRule(rule, parameterNames, listFeatureNames,
                    statisticsFeatureNames, baseFeatureOperands, currentFeatureNames);
            runtimeRules.put(runtimeRule.id(), runtimeRule);
        }

        Map<Long, List<StrategyRuleRelation>> relationsByStrategy = new LinkedHashMap<>();
        relations.stream()
                .sorted(Comparator.comparing(r -> Objects.requireNonNullElse(r.getPriority(), Integer.MAX_VALUE)))
                .forEach(relation -> relationsByStrategy
                        .computeIfAbsent(relation.getStrategyId(), ignored -> new ArrayList<>())
                        .add(relation));

        Map<String, List<RuntimeStrategy>> strategiesByScene = new LinkedHashMap<>();
        int loadedRelations = 0;
        for (Strategy strategy : strategies) {
            // 类型50是累计特征写入过滤策略，不参与普通HTTP场景决策。
            if (Objects.equals(strategy.getType(), 50) || Objects.equals(strategy.getStrategyType(), 50)) continue;
            List<RuntimeRule> strategyRules = new ArrayList<>();
            for (StrategyRuleRelation relation : relationsByStrategy
                    .getOrDefault(strategy.getId(), Collections.emptyList())) {
                RuntimeRule rule = runtimeRules.get(relation.getRuleId());
                if (rule != null) {
                    strategyRules.add(rule);
                    loadedRelations++;
                }
            }
            RuntimeStrategy runtimeStrategy = new RuntimeStrategy(
                    strategy.getId(), strategy.getName(), strategy.getStrategyKey(), strategy.getSceneKey(),
                    strategy.getState(), strategy.getExpression(), strategy.getExpressionRelation(),
                    strategy.getPriority(), strategy.getScore(), strategy.getReturnCode(),
                    strategy.getAbilitySource(), List.copyOf(strategyRules));
            strategiesByScene.computeIfAbsent(strategy.getSceneKey(), ignored -> new ArrayList<>())
                    .add(runtimeStrategy);
        }
        strategiesByScene.values().forEach(list -> list.sort(
                Comparator.comparing((RuntimeStrategy value) -> Objects.requireNonNullElse(value.priority(), 0))
                        .reversed().thenComparing(RuntimeStrategy::id)));

        Map<String, RuntimeScene> runtimeScenes = new LinkedHashMap<>();
        for (Scene scene : scenes) {
            runtimeScenes.put(scene.getSceneKey(), new RuntimeScene(
                    scene.getSceneKey(), scene.getName(), scene.getBusinessSideKey(),
                    List.copyOf(strategiesByScene.getOrDefault(scene.getSceneKey(), Collections.emptyList()))));
        }

        long version = versions.incrementAndGet();
        RuntimeSnapshot replacement = new RuntimeSnapshot(version, LocalDateTime.now(),
                Map.copyOf(runtimeScenes), Map.copyOf(runtimeRules), loadedRelations);
        snapshot.set(replacement);
        expressionEvaluator.clearCache();
        log.info("Engine configuration loaded: version={}, scenes={}, strategies={}, rules={}, relations={}",
                version, replacement.scenes().size(), replacement.strategyCount(),
                replacement.rules().size(), loadedRelations);
        return replacement.status();
    }

    public EngineConfigStatus status() {
        return snapshot.get().status();
    }

    public EngineDecisionResult evaluate(EngineEvaluateRequest request) {
        long start = System.currentTimeMillis();
        RuntimeSnapshot current = snapshot.get();
        if (request == null || request.getData() == null || request.getData().isEmpty()) {
            throw new EngineEvaluationException(CodeEnum.DATA_EMPTY, "决策数据不能为空");
        }
        RuntimeScene scene = current.scenes().get(request.getSceneKey());
        if (scene == null) {
            throw new EngineEvaluationException(CodeEnum.SCENE_NOT_FOUND,
                    "场景不存在或未启用: " + request.getSceneKey());
        }
        if (!Objects.equals(scene.businessKey(), request.getBusinessKey())) {
            throw new EngineEvaluationException(CodeEnum.AUTH_INVALID_KEY, "业务方无权访问该场景");
        }

        String requestId = isBlank(request.getRequestId())
                ? UUID.randomUUID().toString().replace("-", "") : request.getRequestId();
        String traceId = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> input = normalizeData(request.getData());
        input.putIfAbsent("requestId", requestId);
        input.put("traceId", traceId);

        // 沿用原引擎的“预处理/补全 -> 特征作业 -> 策略计算”模板，并保留结构化降级信息。
        DataComplementResult complementResult = dataComplementService.complete(request.getSceneKey(), input);
        input = new LinkedHashMap<>(complementResult.data());
        FeatureQueryResult featureResult = featureService.queryFeatures(input, request.getSceneKey());
        input.putAll(featureResult.values());

        List<StrategyMatchResult> onlineHits = new ArrayList<>();
        List<StrategyMatchResult> preOnlineHits = new ArrayList<>();
        int evaluatedRuleCount = 0;
        for (RuntimeStrategy strategy : scene.strategies()) {
            List<RuleHitResult> ruleResults = new ArrayList<>();
            Map<String, Object> ruleVariables = new LinkedHashMap<>();
            for (RuntimeRule rule : strategy.rules()) {
                RuleHitResult result = evaluateRule(rule, input);
                evaluatedRuleCount++;
                ruleResults.add(result);
                ruleVariables.put(rule.name(), result.isHit());
                ruleVariables.put(rule.key(), result.isHit());
                ruleVariables.put("rule_" + rule.id(), result.isHit());
            }
            if (!evaluateStrategy(strategy, ruleVariables, ruleResults)) continue;
            List<RuleHitResult> responseRules = request.isNeedDetail()
                    ? ruleResults
                    : ruleResults.stream().filter(RuleHitResult::isHit).toList();
            StrategyMatchResult match = StrategyMatchResult.builder()
                    .strategyId(strategy.id()).strategyName(strategy.name()).strategyKey(strategy.key())
                    .state(strategy.state()).score(defaultScore(strategy.score()))
                    .returnCode(strategy.returnCode()).abilitySource(strategy.abilitySource())
                    .expression(strategy.expression()).rules(responseRules).build();
            if (Objects.equals(strategy.state(), 2)) onlineHits.add(match);
            else preOnlineHits.add(match);
        }

        String status = !onlineHits.isEmpty() ? "HIT" : !preOnlineHits.isEmpty() ? "PRE_HIT" : "NOT_HIT";
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("sceneName", scene.name());
        detail.put("evaluatedStrategyCount", scene.strategies().size());
        detail.put("evaluatedRuleCount", evaluatedRuleCount);
        detail.put("dataPipelineDegraded", complementResult.degraded() || featureResult.degraded());
        detail.put("featureCostMs", featureResult.costMs());
        if (complementResult.degraded()) {
            detail.put("failedComplementHandlers", complementResult.failedHandlers());
        }
        if (featureResult.degraded()) {
            detail.put("failedFeatureTypes", featureResult.failedTypes());
            detail.put("timedOutFeatureTypes", featureResult.timedOutTypes());
        }
        if (request.isNeedDetail()) detail.put("input", request.getData());

        return EngineDecisionResult.builder()
                .requestId(requestId).traceId(traceId).businessKey(request.getBusinessKey())
                .sceneKey(request.getSceneKey()).status(status).hit(!onlineHits.isEmpty())
                .score(maxScore(onlineHits)).preScore(maxScore(preOnlineHits))
                .returnCodes(onlineHits.stream().map(StrategyMatchResult::getReturnCode)
                        .filter(value -> !isBlank(value)).distinct().toList())
                .strategies(onlineHits).preStrategies(preOnlineHits).detail(detail)
                .configVersion(current.version()).costMs(System.currentTimeMillis() - start).build();
    }

    private RuleHitResult evaluateRule(RuntimeRule rule, Map<String, Object> data) {
        boolean hit = false;
        String detail;
        Map<String, Object> values = new LinkedHashMap<>();
        if (!isBlank(rule.leftField()) && data.containsKey(rule.leftField())) {
            values.put(rule.leftField(), data.get(rule.leftField()));
        }
        try {
            if (!isBlank(rule.expression())) {
                hit = expressionEvaluator.evaluate(rule.expression(), data);
                detail = hit ? "表达式匹配" : "表达式不匹配";
            } else if (isBlank(rule.leftField())) {
                detail = "未解析左操作数字段";
            } else {
                hit = evaluateOperator(rule, data);
                detail = hit ? "条件匹配" : "条件不匹配";
            }
        } catch (Exception e) {
            detail = "规则评估异常: " + e.getMessage();
            log.debug("Rule evaluation failed, ruleId={}", rule.id(), e);
        }
        return RuleHitResult.builder().ruleId(rule.id()).ruleName(rule.name()).ruleKey(rule.key())
                .hit(hit).detail(detail).paramValues(values).build();
    }

    private boolean evaluateOperator(RuntimeRule rule, Map<String, Object> data) {
        String operator = Objects.requireNonNullElse(rule.operator(), "==").trim().toUpperCase(Locale.ROOT);
        Object left = data.get(rule.leftField());
        String rightText = unquote(rule.rightValue());
        Object right = rightText;
        if (operator.startsWith("FIELD_")) right = data.get(rule.rightField());

        return switch (operator) {
            case "FIELD_EXISTS" -> data.containsKey(rule.leftField()) && left != null;
            case "FIELD_NOT_EXISTS" -> !data.containsKey(rule.leftField()) || left == null;
            case "EQUAL_NULL_OBJECT" -> left == null || "null".equalsIgnoreCase(String.valueOf(left));
            case "EQUAL_STRING" -> Objects.equals(stringValue(left), stringValue(right));
            case "NOT_EQUAL_STRING" -> !Objects.equals(stringValue(left), stringValue(right));
            case "==" -> smartEquals(left, right);
            case "!=" -> !smartEquals(left, right);
            case ">" -> compare(left, right) > 0;
            case ">=" -> compare(left, right) >= 0;
            case "<" -> compare(left, right) < 0;
            case "<=" -> compare(left, right) <= 0;
            case "INCLUDE" -> includes(left, rightText);
            case "NOT_INCLUDE" -> !includes(left, rightText);
            case "STARTS_WITH" -> split(rightText).stream().anyMatch(value -> stringValue(left).startsWith(value));
            case "END_WITH" -> split(rightText).stream().anyMatch(value -> stringValue(left).endsWith(value));
            case "REGEXP" -> left != null && Pattern.compile(rightText).matcher(stringValue(left)).find();
            case "INTERSECTION" -> intersects(left, rightText);
            case "FIELD_EQUAL" -> smartEquals(left, right);
            case "FIELD_NOT_EQUAL" -> !smartEquals(left, right);
            case "FIELD_INCLUDE" -> includes(left, stringValue(right));
            case "FIELD_NOT_INCLUDE" -> !includes(left, stringValue(right));
            case "LENGTH_MORE_THAN_AND" -> stringValue(left).length() >= Integer.parseInt(rightText);
            case "LENGTH_LESS_THAN_AND" -> stringValue(left).length() <= Integer.parseInt(rightText);
            default -> false;
        };
    }

    private boolean evaluateStrategy(RuntimeStrategy strategy, Map<String, Object> variables,
                                     List<RuleHitResult> ruleResults) {
        if (ruleResults.isEmpty()) return false;
        if (!isBlank(strategy.expression())) {
            Object result = expressionEvaluator.evaluateValue(strategy.expression(), variables);
            if (result instanceof Boolean bool) return bool;
        }
        if ("||".equals(strategy.expressionRelation())) {
            return ruleResults.stream().anyMatch(RuleHitResult::isHit);
        }
        return ruleResults.stream().allMatch(RuleHitResult::isHit);
    }

    private RuntimeRule toRuntimeRule(Rule rule, Map<Long, String> parameterNames,
                                      Map<Long, String> listFeatureNames,
                                      Map<Long, String> statisticsFeatureNames,
                                      Map<Long, BaseFeatureOperand> baseFeatureOperands,
                                      Map<Long, String> currentFeatureNames) {
        Long leftId = rule.getRuleExpressLeft();
        Long featureId = rule.getFeatureId() != null ? rule.getFeatureId() : leftId;
        String leftField = switch (Objects.requireNonNullElse(rule.getType(), 0)) {
            case 1, 50 -> parameterNames.get(leftId);
            case 2 -> listFeatureNames.get(leftId);
            case 5 -> statisticsFeatureNames.get(featureId);
            case 4, 6 -> value(baseFeatureOperands.get(featureId), BaseFeatureOperand::compositeKey);
            case 15 -> value(baseFeatureOperands.get(featureId), BaseFeatureOperand::authKey);
            case 14, 16 -> value(baseFeatureOperands.get(featureId), BaseFeatureOperand::name);
            default -> currentFeatureNames.get(featureId);
        };
        if (isBlank(leftField)) {
            BaseFeatureOperand baseFeature = baseFeatureOperands.get(featureId);
            leftField = firstNonBlank(currentFeatureNames.get(featureId),
                    baseFeature == null ? null : baseFeature.name(), parameterNames.get(leftId));
        }
        String rightField = unquote(rule.getRuleExpressRight());
        if (!isBlank(rightField) && rightField.matches("\\d+")) {
            rightField = parameterNames.getOrDefault(Long.valueOf(rightField), rightField);
        }
        String expression = isGeneratedIdExpression(rule) ? null : rule.getExpression();
        return new RuntimeRule(rule.getId(), rule.getName(),
                isBlank(rule.getRuleKey()) ? "rule_" + rule.getId() : rule.getRuleKey(),
                leftField, rule.getRuleExpressOp(), rule.getRuleExpressRight(), rightField,
                expression);
    }

    private boolean isGeneratedIdExpression(Rule rule) {
        if (isBlank(rule.getExpression()) || rule.getRuleExpressLeft() == null
                || isBlank(rule.getRuleExpressOp())) return false;
        String generated = rule.getRuleExpressLeft() + rule.getRuleExpressOp()
                + Objects.toString(rule.getRuleExpressRight(), "");
        return rule.getExpression().replaceAll("\\s+", "").equals(generated.replaceAll("\\s+", ""));
    }

    private Map<Long, String> loadNameMap(String sql, String valueColumn) {
        Map<Long, String> result = new HashMap<>();
        try {
            jdbcTemplate.query(sql, (RowCallbackHandler) rs ->
                    result.put(rs.getLong("id"), rs.getString(valueColumn)));
        } catch (Exception e) {
            log.debug("Optional configuration table is unavailable for query: {}", sql, e);
        }
        return result;
    }

    private Map<Long, String> loadFeatureKeyMap() {
        Map<Long, String> result = new HashMap<>();
        try {
            jdbcTemplate.query("SELECT id,feature_key,name FROM t_base_info_feature", (RowCallbackHandler) rs -> {
                String key = rs.getString("feature_key");
                result.put(rs.getLong("id"), isBlank(key) ? rs.getString("name") : key);
            });
        } catch (Exception e) {
            log.debug("Base feature table is unavailable", e);
        }
        return result;
    }

    /** 按原配置中心规则生成画像、画像关联和认证特征在决策数据中的字段名。 */
    private Map<Long, BaseFeatureOperand> loadBaseFeatureOperands() {
        Map<Long, BaseFeatureOperand> result = new HashMap<>();
        String sql = "SELECT f.id,f.name,f.tool_id,f.ext_param,p.param_key,ff.name field_name " +
                "FROM t_catalog_feature_baseinfo f " +
                "LEFT JOIN t_catalog_param p ON p.id=f.src_param_id " +
                "LEFT JOIN t_catalog_feature_baseinfo_tool_field_relation r " +
                "ON r.feature_baseinfo_id=f.id " +
                "LEFT JOIN t_catalog_feature_baseinfo_tool_field ff ON ff.id=r.param_id " +
                "ORDER BY f.id,r.id";
        try {
            jdbcTemplate.query(sql, (RowCallbackHandler) rs -> {
                long id = rs.getLong("id");
                if (result.containsKey(id)) return;
                String name = rs.getString("name");
                String paramKey = rs.getString("param_key");
                String fieldName = rs.getString("field_name");
                String toolId = Objects.toString(rs.getObject("tool_id"), null);
                String composite = featureKey(paramKey, fieldName, toolId);
                String extParam = rs.getString("ext_param");
                String authField = fieldName;
                if (!isBlank(fieldName) && !isBlank(extParam) && !"-1_-1".equals(extParam)) {
                    authField = fieldName + "_" + extParam.replaceAll("[\\s:_-]", "");
                }
                String authKey = featureKey(paramKey, authField, toolId);
                result.put(id, new BaseFeatureOperand(name, composite, authKey));
            });
        } catch (Exception e) {
            log.debug("Configuration catalog feature tables are unavailable", e);
        }
        return result;
    }

    private String featureKey(String parameter, String field, String toolId) {
        if (isBlank(parameter) || isBlank(field) || isBlank(toolId)) return null;
        return parameter + "_" + field + "_" + toolId;
    }

    private <T> String value(T source, java.util.function.Function<T, String> getter) {
        return source == null ? null : getter.apply(source);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) if (!isBlank(value)) return value;
        return null;
    }

    private Map<String, Object> normalizeData(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(key, normalizeValue(value)));
        return result;
    }

    private Object normalizeValue(Object value) {
        if (!(value instanceof String text)) return value;
        String trimmed = text.trim();
        if (trimmed.matches("[-+]?\\d+")) {
            try { return Long.valueOf(trimmed); } catch (NumberFormatException ignored) { return value; }
        }
        if (trimmed.matches("[-+]?(\\d+\\.\\d*|\\d*\\.\\d+)")) {
            try { return new BigDecimal(trimmed); } catch (NumberFormatException ignored) { return value; }
        }
        if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
            return Boolean.valueOf(trimmed);
        }
        return value;
    }

    private boolean smartEquals(Object left, Object right) {
        if (left == null || right == null) return left == right;
        BigDecimal leftNumber = decimal(left); BigDecimal rightNumber = decimal(right);
        if (leftNumber != null && rightNumber != null) return leftNumber.compareTo(rightNumber) == 0;
        if (left instanceof Boolean || right instanceof Boolean) {
            return Boolean.parseBoolean(String.valueOf(left)) == Boolean.parseBoolean(String.valueOf(right));
        }
        return stringValue(left).equals(stringValue(right));
    }

    private int compare(Object left, Object right) {
        BigDecimal leftNumber = decimal(left); BigDecimal rightNumber = decimal(right);
        if (leftNumber != null && rightNumber != null) return leftNumber.compareTo(rightNumber);
        return stringValue(left).compareTo(stringValue(right));
    }

    private BigDecimal decimal(Object value) {
        if (value == null) return null;
        try { return new BigDecimal(String.valueOf(value)); } catch (NumberFormatException ignored) { return null; }
    }

    private boolean includes(Object left, String values) {
        if (left == null) return false;
        if (left instanceof Collection<?> collection) {
            return split(values).stream().anyMatch(value -> collection.stream()
                    .anyMatch(item -> smartEquals(item, value)));
        }
        String source = stringValue(left);
        return split(values).stream().anyMatch(source::contains);
    }

    private boolean intersects(Object left, String values) {
        Set<String> expected = new HashSet<>(split(values));
        if (left instanceof Collection<?> collection) {
            return collection.stream().map(this::stringValue).anyMatch(expected::contains);
        }
        return split(stringValue(left)).stream().anyMatch(expected::contains);
    }

    private List<String> split(String text) {
        if (text == null || text.isBlank()) return List.of("");
        return Arrays.stream(text.split(",")).map(String::trim).toList();
    }

    private String unquote(String value) {
        if (value == null) return "";
        String result = value.trim();
        while (result.length() >= 2 && ((result.startsWith("'") && result.endsWith("'"))
                || (result.startsWith("\"") && result.endsWith("\"")))) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    private String stringValue(Object value) { return value == null ? "" : String.valueOf(value); }
    private boolean isBlank(String value) { return value == null || value.isBlank(); }
    private BigDecimal defaultScore(BigDecimal score) { return score == null ? BigDecimal.ZERO : score; }
    private BigDecimal maxScore(List<StrategyMatchResult> values) {
        return values.stream().map(StrategyMatchResult::getScore).filter(Objects::nonNull)
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    private record RuntimeRule(Long id, String name, String key, String leftField,
                               String operator, String rightValue, String rightField,
                               String expression) {}

    private record BaseFeatureOperand(String name, String compositeKey, String authKey) {}

    private record RuntimeStrategy(Long id, String name, String key, String sceneKey,
                                   Integer state, String expression, String expressionRelation,
                                   Integer priority, BigDecimal score, String returnCode,
                                   String abilitySource, List<RuntimeRule> rules) {}

    private record RuntimeScene(String sceneKey, String name, String businessKey,
                                List<RuntimeStrategy> strategies) {}

    private record RuntimeSnapshot(long version, LocalDateTime loadedAt,
                                   Map<String, RuntimeScene> scenes,
                                   Map<Long, RuntimeRule> rules, int relationCount) {
        static RuntimeSnapshot empty() {
            return new RuntimeSnapshot(0, null, Map.of(), Map.of(), 0);
        }
        int strategyCount() {
            return scenes.values().stream().mapToInt(scene -> scene.strategies().size()).sum();
        }
        EngineConfigStatus status() {
            return EngineConfigStatus.builder().version(version).loadedAt(loadedAt)
                    .sceneCount(scenes.size()).strategyCount(strategyCount())
                    .ruleCount(rules.size()).relationCount(relationCount).build();
        }
    }
}
