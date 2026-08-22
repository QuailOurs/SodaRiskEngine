package com.soda.risk.engine.config.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.config.rule.Rule;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 为规则编辑器提供工具、参数、特征级联选项及字段转换能力。
 * 数据来自 {@code t_catalog_*} 配置目录表。
 */
@Service
@RequiredArgsConstructor
public class ConfigurationCatalogSupport {

    private static final Map<Integer, String> RULE_TYPE_NAMES = Map.of(
            1, "基础参数", 2, "名单", 4, "画像", 5, "累计特征",
            6, "画像关联规模", 14, "算法", 15, "认证状态", 16, "计算", 50, "累计过滤基础参数");

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public List<Map<String, Object>> listTools(Integer type) {
        String sql = "SELECT id, name, type, state, description FROM t_catalog_tool";
        List<Object> args = new ArrayList<>();
        if (type != null) {
            sql += " WHERE type = ?";
            args.add(type);
        }
        sql += " ORDER BY id";
        return jdbcTemplate.queryForList(sql, args.toArray()).stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.get("ID"));
            item.put("name", row.get("NAME"));
            item.put("type", row.get("TYPE"));
            item.put("stateCode", row.get("STATE"));
            item.put("state", Objects.equals(number(row.get("STATE")), 0L) ? "禁用" : "启用");
            item.put("description", row.get("DESCRIPTION"));
            return item;
        }).toList();
    }

    public List<Map<String, Object>> listParameters(String sceneKey) {
        return jdbcTemplate.queryForList(
                "SELECT p.id,p.name,p.param_key,p.type_id,COALESCE(s.scene_key,p.scene_key) scene_key,p.description,p.state " +
                        "FROM t_catalog_param p LEFT JOIN t_catalog_scene ls ON ls.scene_key=p.scene_key " +
                        "LEFT JOIN t_scene s ON s.id=ls.id+100000 OR s.scene_key=p.scene_key " +
                        "WHERE COALESCE(s.scene_key,p.scene_key)=? ORDER BY p.id", sceneKey).stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.get("ID"));
            item.put("name", row.get("NAME"));
            item.put("paramKey", row.get("PARAM_KEY"));
            item.put("typeId", row.get("TYPE_ID"));
            item.put("sceneKey", row.get("SCENE_KEY"));
            item.put("description", row.get("DESCRIPTION"));
            item.put("state", row.get("STATE"));
            return item;
        }).toList();
    }

    public List<Map<String, Object>> parameterCascader(String sceneKey, boolean useKey, boolean multiple) {
        List<Map<String, Object>> rows = listParameters(sceneKey);
        if (rows.isEmpty()) return Collections.emptyList();
        List<Map<String, Object>> children = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> child = new LinkedHashMap<>();
            child.put("value", useKey ? row.get("paramKey") : row.get("id"));
            child.put("label", label(row.get("name"), useKey ? row.get("paramKey") : row.get("description")));
            if (multiple) child.put("multiple", true);
            children.add(child);
        }
        return List.of(sceneNode(sceneKey, sceneName(sceneKey), children));
    }

    public List<Map<String, Object>> ruleOperandCascader(String sceneKey, int ruleType) {
        if (ruleType == 1 || ruleType == 16 || ruleType == 50) {
            return parameterCascader(sceneKey, false, false);
        }
        if (ruleType == 5) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT f.id,f.name,f.description FROM t_catalog_feature_statistics f " +
                            "LEFT JOIN t_catalog_scene ls ON ls.scene_key=f.scene_key " +
                            "LEFT JOIN t_scene s ON s.id=ls.id+100000 OR s.scene_key=f.scene_key " +
                            "WHERE COALESCE(s.scene_key,f.scene_key)=? ORDER BY f.id",
                    sceneKey);
            if (rows.isEmpty()) return Collections.emptyList();
            List<Map<String, Object>> children = rows.stream().map(row -> {
                Map<String, Object> child = new LinkedHashMap<String, Object>();
                child.put("value", row.get("ID"));
                child.put("label", label(row.get("NAME"), row.get("DESCRIPTION")));
                return child;
            }).toList();
            return List.of(sceneNode(sceneKey, sceneName(sceneKey), children));
        }
        return Collections.emptyList();
    }

    public List<Map<String, Object>> toolFields(long toolId, boolean multiple) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, name, description FROM t_catalog_feature_baseinfo_tool_field " +
                        "WHERE tool_id = ? ORDER BY id", toolId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> option = new LinkedHashMap<>();
            option.put("value", row.get("ID"));
            option.put("label", label(row.get("NAME"), row.get("DESCRIPTION")));
            if (multiple) option.put("multiple", true);
            result.add(option);
        }
        return result;
    }

    public Rule enrichRule(Rule rule) {
        if (rule == null) return null;
        rule.setTypeName(RULE_TYPE_NAMES.getOrDefault(rule.getType(), "未知"));
        rule.setStateName(Objects.equals(rule.getState(), 0) ? "禁用" : "启用");
        rule.setExpressionView(rule.getExpression() == null || rule.getExpression().isBlank()
                ? operandExpression(rule) : rule.getExpression());
        parseDestParamIds(rule);
        if (rule.getToolId() == null) loadCatalogFeatureFields(rule);
        return rule;
    }

    public void prepareRuleForSave(Rule rule) {
        if (rule.getDestParamIds() != null) {
            try {
                rule.setDestParamIdsJson(objectMapper.writeValueAsString(rule.getDestParamIds()));
            } catch (JsonProcessingException ignored) {
                rule.setDestParamIdsJson("[]");
            }
        }
        if (rule.getFeatureId() == null) rule.setFeatureId(rule.getRuleExpressLeft());
        if (rule.getExpression() == null || rule.getExpression().isBlank()) {
            rule.setExpression(operandExpression(rule));
        }
    }

    private void loadCatalogFeatureFields(Rule rule) {
        try {
            if (Objects.equals(rule.getType(), 2)) {
                Map<String, Object> row = jdbcTemplate.queryForMap(
                        "SELECT tool_id, src_param_id FROM t_catalog_feature_list WHERE id = ?",
                        rule.getRuleExpressLeft());
                rule.setToolId(number(row.get("TOOL_ID")));
                rule.setSrcParamId(number(row.get("SRC_PARAM_ID")));
                return;
            }
            if (Set.of(4, 6, 14, 15, 16).contains(rule.getType())) {
                Long featureId = rule.getFeatureId() == null ? rule.getRuleExpressLeft() : rule.getFeatureId();
                Map<String, Object> row = jdbcTemplate.queryForMap(
                        "SELECT tool_id, src_param_id FROM t_catalog_feature_baseinfo WHERE id = ?", featureId);
                rule.setToolId(number(row.get("TOOL_ID")));
                rule.setSrcParamId(number(row.get("SRC_PARAM_ID")));
                List<Long> fieldIds = jdbcTemplate.queryForList(
                        "SELECT param_id FROM t_catalog_feature_baseinfo_tool_field_relation " +
                                "WHERE feature_baseinfo_id = ? ORDER BY id", Long.class, featureId);
                rule.setDestParamIds(fieldIds);
            }
        } catch (RuntimeException ignored) {
            // 可选的特征关联缺失时，仍保留规则主体供用户编辑。
        }
    }

    private void parseDestParamIds(Rule rule) {
        if (rule.getDestParamIds() != null || rule.getDestParamIdsJson() == null) return;
        try {
            Long[] ids = objectMapper.readValue(rule.getDestParamIdsJson(), Long[].class);
            rule.setDestParamIds(Arrays.asList(ids));
        } catch (JsonProcessingException ignored) {
            rule.setDestParamIds(Collections.emptyList());
        }
    }

    private String sceneName(String sceneKey) {
        List<String> names = jdbcTemplate.queryForList(
                "SELECT name FROM t_scene WHERE scene_key = ? ORDER BY id LIMIT 1", String.class, sceneKey);
        return names.isEmpty() ? sceneKey : names.get(0);
    }

    private Map<String, Object> sceneNode(String sceneKey, String name, List<Map<String, Object>> children) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("value", sceneKey);
        node.put("label", name);
        node.put("children", children);
        return node;
    }

    private String operandExpression(Rule rule) {
        if (rule.getRuleExpressLeft() == null || rule.getRuleExpressOp() == null) return "";
        return rule.getRuleExpressLeft() + " " + rule.getRuleExpressOp() + " " +
                Objects.toString(rule.getRuleExpressRight(), "");
    }

    private String label(Object name, Object description) {
        String first = Objects.toString(name, "");
        String second = Objects.toString(description, "");
        return second.isBlank() ? first : first + "(" + second + ")";
    }

    private Long number(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }
}
