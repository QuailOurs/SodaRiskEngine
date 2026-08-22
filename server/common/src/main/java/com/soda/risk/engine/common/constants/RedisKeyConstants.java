package com.soda.risk.engine.common.constants;

/**
 * Redis Key 常量，统一管理 Soda 的缓存命名空间。
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {}

    // ========== 策略引擎配置 ==========
    /** 策略配置 */
    public static final String STRATEGY_PREFIX = "soda:strategy:";
    /** 策略-规则关系 */
    public static final String STRATEGY_RULE_RELATION = "soda:strategy_rule_relation:";
    /** 规则配置 */
    public static final String RULE_PREFIX = "soda:rule:";
    /** 场景配置 */
    public static final String SCENE_PREFIX = "soda:scene:";
    /** 参数配置 */
    public static final String PARAMETER_PREFIX = "soda:parameter:";
    /** 工具配置 */
    public static final String TOOL_PREFIX = "soda:tool:";
    /** 数据类型配置 */
    public static final String DATA_TYPE_PREFIX = "soda:data_type:";
    /** 数据补全配置 */
    public static final String COMPLEMENT_PREFIX = "soda:complement:";

    // ========== 特征配置 ==========
    /** 基础信息特征 */
    public static final String FEATURE_BASEINFO = "soda:feature:baseinfo:";
    /** 基础信息特征工具字段 */
    public static final String FEATURE_BASEINFO_TOOL_FIELD = "soda:feature:baseinfo:tool_field:";
    /** 基础信息特征工具字段关系 */
    public static final String FEATURE_BASEINFO_TOOL_FIELD_RELATION = "soda:feature:baseinfo:tool_field_relation:";
    /** 算法特征 */
    public static final String FEATURE_ALGORITHM = "soda:feature:algorithm:";
    /** 名单特征 */
    public static final String FEATURE_LIST = "soda:feature:list:";
    /** 画像特征 */
    public static final String FEATURE_PORTRAIT = "soda:feature:portrait:";
    /** 画像关联特征 */
    public static final String FEATURE_PORTRAIT_RELEVANCE = "soda:feature:portrait_relevance:";
    /** 累计特征 */
    public static final String FEATURE_CALCULATION = "soda:feature:calculation:";
    /** 认证特征 */
    public static final String FEATURE_AUTH = "soda:feature:auth:";
    /** 统计特征-写 */
    public static final String FEATURE_STATISTICS_WRITE = "soda:feature:statistics:write:";
    /** 统计特征-查 */
    public static final String FEATURE_STATISTICS_QUERY = "soda:feature:statistics:query:";
    /** 统计特征参数关系 */
    public static final String FEATURE_STATISTICS_PARAM_RELATION = "soda:feature:statistics:param_relation:";

    // ========== 处置引擎配置 ==========
    /** 处置方式配置 */
    public static final String DISPOSER_PREFIX = "soda:disposer:";
    /** 处置策略配置 */
    public static final String DISPOSER_STRATEGY = "soda:disposer:strategy:";
    /** 处置内容模板 */
    public static final String DISPOSER_CONTENT_TEMPLATE = "soda:disposer:content_template:";
    /** 处置用户状态 */
    public static final String DISPOSER_USER = "soda:disposer:user:";

    // ========== 风险决策配置 ==========
    /** 风险配置 */
    public static final String RISK_PREFIX = "soda:risk:";
    /** 黑白名单 */
    public static final String BLACK_WHITE_LIST = "soda:black_white_list:";
    /** 返回码映射 */
    public static final String RETURN_CODE = "soda:return_code:";

    // ========== 业务方配置 ==========
    /** 业务方Key映射 */
    public static final String BUSINESS_SIDE_KEY = "soda:business_side_key:";
    /** 业务方OpenId映射 */
    public static final String BUSINESS_SIDE_OPENID = "soda:business_side_openid:";

    // ========== ID路由 ==========
    /** ID路由映射 */
    public static final String ID_ROUTE_MAP = "soda:id_route_map:";
    /** 场景路由映射 */
    public static final String SCENE_ROUTE_MAP = "soda:scene_route_map:";
    /** 策略ID集合 */
    public static final String STRATEGY_ID_SET = "soda:strategy_id_set";

    // ========== 离线锁定 ==========
    /** 离线锁定状态 */
    public static final String OFFLINE_LOCK = "soda:offline_lock:";

    // ========== 业务数据 ==========
    /** 业务数据Key */
    public static final String BUSINESS_DATA = "soda:business_data:";
}
