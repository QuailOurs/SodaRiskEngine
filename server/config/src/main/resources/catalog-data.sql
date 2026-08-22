-- ============================================================
-- Soda 配置目录示例数据
--
-- 这组数据完全为开源演示而构造，用于支持参数、工具、特征、规则编辑器
-- 和数据补全页面。它不包含生产配置、真实用户数据或第三方系统标识。
-- ============================================================

INSERT INTO t_catalog_business_side
  (id, name, business_side_key, system_key, description, operator, state, update_time, create_time)
VALUES
  (1, '示例业务', 'demo_business', 'demo-system', 'Soda 通用决策示例', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, '支付业务', 'payment_business', 'payment-system', '支付决策示例', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_scene
  (id, name, scene_key, business_side_key, description, operator, state, update_time, create_time, pm_account, rd_account)
VALUES
  (1, '登录保护', 'login_protection', 'demo_business', '登录行为决策', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'product-owner', 'developer'),
  (2, '注册保护', 'register_protection', 'demo_business', '注册行为决策', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'product-owner', 'developer'),
  (3, '账号安全', 'account_security', 'demo_business', '账号操作决策', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'product-owner', 'developer');

INSERT INTO t_catalog_data_type
  (id, type_name, description, operator, state, update_time, create_time)
VALUES
  (1, 'Integer', '整数', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'Long', '长整数', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'Double', '小数', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 'Boolean', '布尔值', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, 'String', '字符串', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (6, 'DateTime', '日期时间', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (7, 'StringList', '字符串集合', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_tool
  (id, name, type, state, description, operator, reference_times, create_time, update_time)
VALUES
  (1, '请求参数', 1, 1, '读取决策请求中的字段', 'soda', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, '名单查询', 2, 1, '查询本地或外部名单', 'soda', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, '画像查询', 4, 1, '查询主体画像属性', 'soda', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, '窗口统计', 5, 1, '读取时间窗口统计结果', 'soda', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, '模型评分', 14, 1, '调用算法模型获取评分', 'soda', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (6, '数据补全', 16, 1, '根据已有字段补充派生字段', 'soda', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_param
  (id, name, param_key, type_id, scene_key, description, operator, state, reference_times, update_time, create_time)
VALUES
  (1, '用户标识', 'user_id', '5', 'login_protection', '发起登录的用户', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, '来源 IP', 'ip', '5', 'login_protection', '登录来源 IP', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, '设备标识', 'device_id', '5', 'login_protection', '客户端设备标识', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, '登录次数', 'login_count', '1', 'login_protection', '统计窗口内的登录次数', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, '黑名单命中', 'blacklisted', '4', 'login_protection', '是否命中黑名单', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (6, '设备风险分', 'device_risk_score', '3', 'login_protection', '设备风险评分', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (7, '手机号', 'mobile', '5', 'register_protection', '注册手机号', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (8, '注册 IP', 'register_ip', '5', 'register_protection', '注册来源 IP', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (9, '注册次数', 'register_count', '1', 'register_protection', '统计窗口内的注册次数', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (10, 'IP 风险等级', 'ip_risk_level', '5', 'register_protection', 'IP 风险分级', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (11, '操作类型', 'operation', '5', 'account_security', '账号操作类型', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (12, '请求时间', 'request_time', '6', 'account_security', '账号操作发生时间', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_complement_key
  (id, tool_id, complement_key, state, operator, description, create_time, update_time)
VALUES
  (1, 6, 'ip_region', 1, 'soda', '根据 IP 补充地区', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 6, 'device_type', 1, 'soda', '根据设备标识补充设备类型', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_param_complement_key_relation
  (id, param_id, complement_key_id, state, operator, create_time, update_time)
VALUES
  (1, 2, 1, 1, 'soda', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 3, 2, 1, 'soda', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_feature_baseinfo_tool_field
  (id, tool_id, name, description, operator, state, update_time, create_time)
VALUES
  (1, 3, 'account_age_days', '账号注册天数', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 3, 'trusted_device', '是否为可信设备', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 3, 'recent_failures', '近期失败次数', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 5, 'model_score', '模型评分', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, 6, 'ip_region', 'IP 所属地区', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (6, 6, 'device_type', '设备类型', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_feature_baseinfo
  (id, name, tool_id, scene_key, src_param_id, ext_param, description, operator, state, update_time, create_time)
VALUES
  (1, '账号基础画像', 3, 'login_protection', 1, '{}', '读取账号基础画像', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, '设备风险评分', 5, 'login_protection', 3, '{"model":"device-risk-demo"}', '计算设备风险评分', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'IP 地区补全', 6, 'login_protection', 2, '{}', '补充 IP 地区', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, '注册风险评分', 5, 'register_protection', 7, '{"model":"register-risk-demo"}', '计算注册风险评分', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_feature_baseinfo_tool_field_relation
  (id, feature_baseinfo_id, param_id, state, update_time, create_time)
VALUES
  (1, 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 1, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 2, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 3, 5, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, 4, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_feature_list
  (id, name, tool_id, scene_key, src_param_id, description, operator, state, update_time, create_time)
VALUES
  (1, 'IP 黑名单', 2, 'login_protection', 2, '检查 IP 是否命中黑名单', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, '设备白名单', 2, 'login_protection', 3, '检查设备是否命中白名单', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_feature_statistics
  (id, name, scene_key, feature_id, val_id, before_minute, write_state, write_strategy_id,
   query_strategy_id, description, operator, state, reference_times, update_time, create_time)
VALUES
  (1, '十分钟登录次数', 'login_protection', 1, 1, 10, 1, NULL, NULL, '用户十分钟内登录次数', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, '一小时失败次数', 'login_protection', 1, 1, 60, 1, NULL, NULL, '用户一小时内登录失败次数', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, '十分钟注册次数', 'register_protection', 1, 1, 10, 1, NULL, NULL, '设备十分钟内注册次数', 'soda', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_feature_statistics_param_relation
  (id, statistics_feature_id, param_id, state, update_time, create_time)
VALUES
  (1, 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 2, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 3, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_rule
  (id, name, scene_key, type, feature_id, rule_express_left, rule_express_op, rule_express_right,
   expression, ext_param, description, operator, state, update_time, create_time)
VALUES
  (1, 'IP 黑名单规则', 'login_protection', 1, NULL, 5, '==', 'true', 'blacklisted == true', '{}', '命中黑名单时成立', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, '频繁登录规则', 'login_protection', 1, NULL, 4, '>', '5', 'login_count > 5', '{}', '登录次数超过阈值时成立', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, '批量注册规则', 'register_protection', 1, NULL, 9, '>', '10', 'register_count > 10', '{}', '注册次数超过阈值时成立', 'soda', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_strategy
  (id, name, scene_key, type, expression, expression_relation, priority, threshold, score, return_code,
   ability_source, description, operator, state, update_time, create_time)
VALUES
  (1, '登录异常策略', 'login_protection', 1, '1 || 2', '||', 10, 1, 80, 'VERIFY', 'RULE_ENGINE', '登录保护示例策略', 'soda', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, '注册风险策略', 'register_protection', 1, '3', '&&', 10, 1, 80, 'REJECT', 'RULE_ENGINE', '注册保护示例策略', 'soda', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_catalog_strategy_rule_relation
  (id, rule_id, strategy_id, state, update_time, create_time)
VALUES
  (1, 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 2, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 3, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
