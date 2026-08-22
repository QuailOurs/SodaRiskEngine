package com.soda.risk.engine.config.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/** 参数、统计特征和数据补全配置的目录服务。 */
@Service
@RequiredArgsConstructor
public class ConfigurationCatalogService {

    private final JdbcTemplate jdbcTemplate;

    public Object parameterList(Map<String, Object> request) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT p.*, d.type_name, COALESCE(s.scene_key,p.scene_key) mapped_scene_key, " +
                        "COALESCE(s.name,ls.name) scene_name, COALESCE(s.business_side_key,ls.business_side_key) business_side_key, " +
                        "COALESCE(b.name,lb.name) business_side_name " +
                        "FROM t_catalog_param p " +
                        "LEFT JOIN t_catalog_data_type d ON CAST(d.id AS VARCHAR)=p.type_id " +
                        "LEFT JOIN t_catalog_scene ls ON ls.scene_key=p.scene_key " +
                        "LEFT JOIN t_scene s ON s.id=ls.id+100000 OR s.scene_key=p.scene_key " +
                        "LEFT JOIN t_business_side b ON b.business_side_key=s.business_side_key " +
                        "LEFT JOIN t_catalog_business_side lb ON lb.business_side_key=ls.business_side_key ORDER BY p.id")
                .stream().map(this::parameterRow).filter(row -> matchesParameter(row, request)).toList();
        return page(rows, request);
    }

    public List<Map<String, Object>> parameterListByScene(String sceneKey) {
        Map<String, Object> request = new HashMap<>();
        request.put("sceneKey", sceneKey);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) parameterList(request);
        return result;
    }

    @Transactional
    public long addParameter(Map<String, Object> request) {
        long id = nextId("t_catalog_param");
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update("INSERT INTO t_catalog_param " +
                        "(id,name,param_key,type_id,scene_key,description,operator,state,reference_times,update_time,create_time) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?)", id, text(request, "name"), text(request, "paramKey"),
                text(request, "typeId"), catalogSceneKey(text(request, "sceneKey")), text(request, "description"), "admin", 1, 0, now, now);
        return id;
    }

    public void updateParameter(Map<String, Object> request) {
        jdbcTemplate.update("UPDATE t_catalog_param SET name=?, type_id=?, description=?, operator=?, update_time=? WHERE id=?",
                text(request, "name"), text(request, "typeId"), text(request, "description"), "admin",
                Timestamp.valueOf(LocalDateTime.now()), number(request.get("id")));
    }

    public boolean parameterExists(String sceneKey, String column, String value) {
        String safeColumn = "name".equals(column) ? "name" : "param_key";
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_catalog_param WHERE scene_key=? AND " + safeColumn + "=?",
                Integer.class, catalogSceneKey(sceneKey), value);
        return count != null && count > 0;
    }

    public List<Map<String, Object>> dataTypes() {
        return jdbcTemplate.queryForList("SELECT * FROM t_catalog_data_type ORDER BY id").stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.get("ID"));
            item.put("typeName", row.get("TYPE_NAME"));
            item.put("description", row.get("DESCRIPTION"));
            item.put("state", stateText(row.get("STATE")));
            return item;
        }).toList();
    }

    public void updateToolState(long id, int state) {
        jdbcTemplate.update("UPDATE t_catalog_tool SET state=?,update_time=? WHERE id=?", state,
                Timestamp.valueOf(LocalDateTime.now()), id);
    }

    public Object featureStatisticsList(Map<String, Object> request) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT f.*, COALESCE(s.scene_key,f.scene_key) mapped_scene_key, COALESCE(s.name,ls.name) scene_name, " +
                        "COALESCE(s.business_side_key,ls.business_side_key) business_side_key, COALESCE(b.name,lb.name) business_side_name " +
                        "FROM t_catalog_feature_statistics f LEFT JOIN t_catalog_scene ls ON ls.scene_key=f.scene_key " +
                        "LEFT JOIN t_scene s ON s.id=ls.id+100000 OR s.scene_key=f.scene_key " +
                        "LEFT JOIN t_business_side b ON b.business_side_key=s.business_side_key " +
                        "LEFT JOIN t_catalog_business_side lb ON lb.business_side_key=ls.business_side_key ORDER BY f.id")
                .stream().map(row -> featureRow(row, false)).filter(row -> matchesFeature(row, request)).toList();
        return page(rows, request);
    }

    public Map<String, Object> featureStatistics(long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT f.*, COALESCE(s.scene_key,f.scene_key) mapped_scene_key, COALESCE(s.name,ls.name) scene_name, " +
                        "COALESCE(s.business_side_key,ls.business_side_key) business_side_key, COALESCE(b.name,lb.name) business_side_name " +
                        "FROM t_catalog_feature_statistics f LEFT JOIN t_catalog_scene ls ON ls.scene_key=f.scene_key " +
                        "LEFT JOIN t_scene s ON s.id=ls.id+100000 OR s.scene_key=f.scene_key " +
                        "LEFT JOIN t_business_side b ON b.business_side_key=s.business_side_key " +
                        "LEFT JOIN t_catalog_business_side lb ON lb.business_side_key=ls.business_side_key WHERE f.id=?", id);
        return rows.isEmpty() ? null : featureRow(rows.get(0), true);
    }

    @Transactional
    public long addFeatureStatistics(Map<String, Object> request) {
        long id = nextId("t_catalog_feature_statistics");
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update("INSERT INTO t_catalog_feature_statistics " +
                        "(id,name,scene_key,feature_id,val_id,before_minute,write_state,write_strategy_id,query_strategy_id," +
                        "description,operator,state,reference_times,update_time,create_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                id, text(request, "name"), catalogSceneKey(text(request, "sceneKey")), number(request.get("featureId")),
                number(request.get("valId")), integer(request.get("beforeMinute")), stateCode(request.get("writeState")),
                number(request.get("writeStrategyId")), number(request.get("queryStrategyId")), text(request, "description"),
                "admin", stateCode(request.get("state")), 0, now, now);
        replaceFeatureParameters(id, request.get("identificationParas"));
        return id;
    }

    @Transactional
    public void updateFeatureStatistics(Map<String, Object> request) {
        long id = number(request.get("id"));
        jdbcTemplate.update("UPDATE t_catalog_feature_statistics SET name=?,scene_key=?,feature_id=?,val_id=?,before_minute=?," +
                        "write_state=?,write_strategy_id=?,query_strategy_id=?,description=?,operator=?,state=?,update_time=? WHERE id=?",
                text(request, "name"), catalogSceneKey(text(request, "sceneKey")), number(request.get("featureId")), number(request.get("valId")),
                integer(request.get("beforeMinute")), stateCode(request.get("writeState")), number(request.get("writeStrategyId")),
                number(request.get("queryStrategyId")), text(request, "description"), "admin", stateCode(request.get("state")),
                Timestamp.valueOf(LocalDateTime.now()), id);
        replaceFeatureParameters(id, request.get("identificationParas"));
    }

    public void updateFeatureState(long id, int state) {
        jdbcTemplate.update("UPDATE t_catalog_feature_statistics SET state=?,update_time=? WHERE id=?", state,
                Timestamp.valueOf(LocalDateTime.now()), id);
    }

    @Transactional
    public void deleteFeatureStatistics(long id) {
        jdbcTemplate.update("DELETE FROM t_catalog_feature_statistics_param_relation WHERE statistics_feature_id=?", id);
        jdbcTemplate.update("DELETE FROM t_catalog_feature_statistics WHERE id=?", id);
    }

    public boolean featureExists(String sceneKey, String name) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_catalog_feature_statistics WHERE scene_key=? AND name=?",
                Integer.class, catalogSceneKey(sceneKey), name);
        return count != null && count > 0;
    }

    public Object complementList(Map<String, Object> request) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT r.id,r.state,r.operator,r.update_time,p.scene_key,p.param_key,c.complement_key,c.tool_id," +
                        "t.name tool_name,COALESCE(s.scene_key,p.scene_key) mapped_scene_key,COALESCE(s.name,ls.name) scene_name," +
                        "COALESCE(s.business_side_key,ls.business_side_key) business_side_key,COALESCE(b.name,lb.name) business_side_name " +
                        "FROM t_catalog_param_complement_key_relation r " +
                        "JOIN t_catalog_param p ON p.id=r.param_id JOIN t_catalog_complement_key c ON c.id=r.complement_key_id " +
                        "LEFT JOIN t_catalog_tool t ON t.id=c.tool_id LEFT JOIN t_catalog_scene ls ON ls.scene_key=p.scene_key " +
                        "LEFT JOIN t_scene s ON s.id=ls.id+100000 OR s.scene_key=p.scene_key " +
                        "LEFT JOIN t_business_side b ON b.business_side_key=s.business_side_key " +
                        "LEFT JOIN t_catalog_business_side lb ON lb.business_side_key=ls.business_side_key ORDER BY r.id")
                .stream().map(this::complementRow).filter(row -> matchesComplement(row, request)).toList();
        return page(rows, request);
    }

    public List<Map<String, Object>> complementKeys(Long toolId) {
        String sql = "SELECT * FROM t_catalog_complement_key" + (toolId == null ? "" : " WHERE tool_id=?") + " ORDER BY id";
        List<Map<String, Object>> rows = toolId == null ? jdbcTemplate.queryForList(sql) : jdbcTemplate.queryForList(sql, toolId);
        return rows.stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.get("ID"));
            item.put("toolId", row.get("TOOL_ID"));
            item.put("complementKey", row.get("COMPLEMENT_KEY"));
            item.put("description", row.get("DESCRIPTION"));
            item.put("state", stateText(row.get("STATE")));
            return item;
        }).toList();
    }

    @Transactional
    public long addComplementKey(Map<String, Object> request) {
        long id = nextId("t_catalog_complement_key");
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update("INSERT INTO t_catalog_complement_key " +
                        "(id,tool_id,complement_key,state,operator,description,create_time,update_time) VALUES (?,?,?,?,?,?,?,?)",
                id, number(request.get("toolId")), text(request, "complementKey"), 1, "admin",
                text(request, "description"), now, now);
        return id;
    }

    @Transactional
    public long addComplementRelation(Map<String, Object> request) {
        long id = nextId("t_catalog_param_complement_key_relation");
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update("INSERT INTO t_catalog_param_complement_key_relation " +
                        "(id,param_id,complement_key_id,state,operator,create_time,update_time) VALUES (?,?,?,?,?,?,?)",
                id, number(request.get("paramId")), number(request.get("complementKeyId")), 1, "admin", now, now);
        return id;
    }

    public void updateComplementState(long id, int state) {
        jdbcTemplate.update("UPDATE t_catalog_param_complement_key_relation SET state=?,update_time=? WHERE id=?", state,
                Timestamp.valueOf(LocalDateTime.now()), id);
    }

    public void deleteParameter(long id) {
        jdbcTemplate.update("DELETE FROM t_catalog_param_complement_key_relation WHERE param_id=?", id);
        jdbcTemplate.update("DELETE FROM t_catalog_param WHERE id=?", id);
    }

    private Map<String, Object> parameterRow(Map<String, Object> row) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", row.get("ID")); item.put("name", row.get("NAME")); item.put("paramKey", row.get("PARAM_KEY"));
        item.put("typeId", row.get("TYPE_ID")); item.put("typeName", row.get("TYPE_NAME")); item.put("sceneKey", row.get("MAPPED_SCENE_KEY"));
        item.put("sceneName", Objects.toString(row.get("SCENE_NAME"), Objects.toString(row.get("SCENE_KEY"), "")));
        item.put("businessSideKey", row.get("BUSINESS_SIDE_KEY")); item.put("businessSideName", row.get("BUSINESS_SIDE_NAME"));
        item.put("description", row.get("DESCRIPTION")); item.put("operator", row.get("OPERATOR")); item.put("state", stateText(row.get("STATE")));
        item.put("updateTime", row.get("UPDATE_TIME")); item.put("createTime", row.get("CREATE_TIME"));
        return item;
    }

    private Map<String, Object> featureRow(Map<String, Object> row, boolean details) {
        Map<String, Object> item = new LinkedHashMap<>();
        long id = number(row.get("ID"));
        item.put("id", id); item.put("name", row.get("NAME")); item.put("sceneKey", row.get("MAPPED_SCENE_KEY"));
        item.put("sceneName", Objects.toString(row.get("SCENE_NAME"), Objects.toString(row.get("SCENE_KEY"), "")));
        item.put("businessSideKey", row.get("BUSINESS_SIDE_KEY")); item.put("businessSideName", row.get("BUSINESS_SIDE_NAME"));
        item.put("featureId", row.get("FEATURE_ID")); item.put("valId", row.get("VAL_ID")); item.put("beforeMinute", row.get("BEFORE_MINUTE"));
        item.put("writeState", stateText(row.get("WRITE_STATE"))); item.put("writeStrategyId", row.get("WRITE_STRATEGY_ID"));
        item.put("queryStrategyId", row.get("QUERY_STRATEGY_ID")); item.put("writeStrategy", strategySummary(row.get("WRITE_STRATEGY_ID")));
        item.put("queryStrategy", strategySummary(row.get("QUERY_STRATEGY_ID"))); item.put("description", row.get("DESCRIPTION"));
        item.put("operator", row.get("OPERATOR")); item.put("state", stateText(row.get("STATE"))); item.put("updateTime", row.get("UPDATE_TIME"));
        if (details) item.put("identificationParas", jdbcTemplate.queryForList(
                "SELECT param_id FROM t_catalog_feature_statistics_param_relation WHERE statistics_feature_id=? ORDER BY id", Long.class, id));
        return item;
    }

    private Map<String, Object> strategySummary(Object rawId) {
        Long id = rawId instanceof Number n ? n.longValue() : null;
        if (id == null || id <= 0) return null;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id,name,state,expression FROM t_strategy WHERE id=?", id);
        if (rows.isEmpty()) return null;
        Map<String, Object> row = rows.get(0); Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", row.get("ID")); result.put("name", row.get("NAME")); result.put("expression", row.get("EXPRESSION"));
        result.put("stateName", switch (integer(row.get("STATE"))) { case 1 -> "预上线"; case 2 -> "上线"; default -> "下线"; });
        return result;
    }

    private Map<String, Object> complementRow(Map<String, Object> row) {
        Map<String, Object> item = new LinkedHashMap<>();
        for (String key : List.of("ID", "MAPPED_SCENE_KEY", "PARAM_KEY", "COMPLEMENT_KEY", "TOOL_ID", "TOOL_NAME", "SCENE_NAME", "BUSINESS_SIDE_KEY", "BUSINESS_SIDE_NAME", "OPERATOR", "UPDATE_TIME")) {
            String camel = Character.toLowerCase(key.charAt(0)) + toCamel(key.substring(1).toLowerCase(Locale.ROOT)); item.put(camel, row.get(key));
        }
        item.put("sceneKey", item.remove("mappedSceneKey"));
        item.put("state", stateText(row.get("STATE"))); return item;
    }

    private String toCamel(String value) {
        StringBuilder result = new StringBuilder(); boolean upper = false;
        for (char c : value.toCharArray()) { if (c == '_') upper = true; else { result.append(upper ? Character.toUpperCase(c) : c); upper = false; } }
        return result.toString();
    }

    private boolean matchesParameter(Map<String, Object> row, Map<String, Object> request) {
        return contains(row, "name", request, "name") && contains(row, "paramKey", request, "fuzzyParamKey") &&
                equalsIfSet(row, "sceneKey", request, "sceneKey") && equalsIfSet(row, "businessSideKey", request, "businessSideKey");
    }

    private boolean matchesFeature(Map<String, Object> row, Map<String, Object> request) {
        return contains(row, "name", request, "name") && equalsIfSet(row, "sceneKey", request, "sceneKey") &&
                equalsIfSet(row, "businessSideKey", request, "businessSideKey") && equalsIfSet(row, "featureId", request, "featureId") &&
                equalsIfSet(row, "state", request, "state");
    }

    private boolean matchesComplement(Map<String, Object> row, Map<String, Object> request) {
        return contains(row, "toolName", request, "toolName") && contains(row, "paramKey", request, "paramKey") &&
                contains(row, "complementKey", request, "complementKey") && equalsIfSet(row, "sceneKey", request, "sceneKey") &&
                equalsIfSet(row, "businessSideKey", request, "businessSideKey") && equalsIfSet(row, "state", request, "state");
    }

    private boolean contains(Map<String, Object> row, String rowKey, Map<String, Object> request, String requestKey) {
        String expected = text(request, requestKey); return expected.isBlank() || Objects.toString(row.get(rowKey), "").toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
    }

    private boolean equalsIfSet(Map<String, Object> row, String rowKey, Map<String, Object> request, String requestKey) {
        String expected = text(request, requestKey); return expected.isBlank() || Objects.toString(row.get(rowKey), "").equals(expected);
    }

    private Object page(List<Map<String, Object>> rows, Map<String, Object> request) {
        if (request == null || request.get("currentPage") == null || request.get("pageSize") == null) return rows;
        int current = Math.max(1, integer(request.get("currentPage"))); int size = Math.max(1, integer(request.get("pageSize")));
        int from = Math.min(rows.size(), (current - 1) * size); int to = Math.min(rows.size(), from + size);
        Map<String, Object> page = new LinkedHashMap<>(); page.put("records", rows.subList(from, to)); page.put("current", current);
        page.put("size", size); page.put("total", rows.size()); page.put("pages", (rows.size() + size - 1) / size); return page;
    }

    private void replaceFeatureParameters(long featureId, Object rawIds) {
        jdbcTemplate.update("DELETE FROM t_catalog_feature_statistics_param_relation WHERE statistics_feature_id=?", featureId);
        if (!(rawIds instanceof Collection<?> ids)) return;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        for (Object rawId : ids) jdbcTemplate.update("INSERT INTO t_catalog_feature_statistics_param_relation " +
                        "(id,statistics_feature_id,param_id,state,update_time,create_time) VALUES (?,?,?,?,?,?)",
                nextId("t_catalog_feature_statistics_param_relation"), featureId, number(rawId), 1, now, now);
    }

    private long nextId(String table) {
        Long max = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id),0) FROM " + table, Long.class); return Objects.requireNonNullElse(max, 0L) + 1;
    }

    private String catalogSceneKey(String sceneKey) {
        List<String> keys = jdbcTemplate.queryForList(
                "SELECT ls.scene_key FROM t_catalog_scene ls JOIN t_scene s ON s.id=ls.id+100000 WHERE s.scene_key=?",
                String.class, sceneKey);
        return keys.isEmpty() ? sceneKey : keys.get(0);
    }

    private String text(Map<String, Object> request, String key) { Object value = request == null ? null : request.get(key); return value == null ? "" : value.toString().trim(); }
    private long number(Object value) { return value instanceof Number n ? n.longValue() : Long.parseLong(Objects.toString(value, "0")); }
    private int integer(Object value) { return value instanceof Number n ? n.intValue() : Integer.parseInt(Objects.toString(value, "0")); }
    private int stateCode(Object value) { String text = Objects.toString(value, ""); return "启用".equals(text) || "1".equals(text) || "true".equalsIgnoreCase(text) ? 1 : 0; }
    private String stateText(Object value) { return integer(value) == 0 ? "禁用" : "启用"; }
}
