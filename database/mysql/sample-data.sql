-- Soda optional sample data for MySQL 8.0+
-- Do not load this file into a production database.
SET NAMES utf8mb4;

-- ============================================================
-- Soda 初始化演示数据
-- 数据按表分组，按业务依赖顺序排列
-- ============================================================

-- ----------------------------
-- 1. 业务方配置表 (t_business_side)
-- ----------------------------
INSERT INTO t_business_side (id, name, business_side_key, system_key, description, operator, state)
VALUES (1, '示例业务', 'demo_business', 'demo-system', '开源演示业务方', 'admin', 1);
INSERT INTO t_business_side (id, name, business_side_key, system_key, description, operator, state)
VALUES (2, '支付业务', 'payment_business', 'payment-system', '支付风控演示', 'admin', 1);

-- ----------------------------
-- 3. 场景配置表 (t_scene)
--    scene_key 为全局唯一标识，被策略/特征/返回码等表引用
-- ----------------------------
INSERT INTO t_scene (id, name, scene_key, business_side_id, business_side_key, pm_account, rd_account, description, operator, state)
VALUES (1, '登录保护', 'login_protection', 1, 'demo_business', 'product-owner', 'developer', '登录场景风控', 'admin', 1);
INSERT INTO t_scene (id, name, scene_key, business_side_id, business_side_key, pm_account, rd_account, description, operator, state)
VALUES (2, '注册保护', 'register_protection', 1, 'demo_business', 'product-owner', 'developer', '注册场景风控', 'admin', 1);
INSERT INTO t_scene (id, name, scene_key, business_side_id, business_side_key, pm_account, rd_account, description, operator, state)
VALUES (3, '账号安全', 'account_security', 1, 'demo_business', 'product-owner', 'developer', '账号安全风控', 'admin', 1);

-- ----------------------------
-- 3. 策略配置表 (t_strategy)
--    strategy_key 为全局唯一标识，被处置记录表引用
-- ----------------------------
INSERT INTO t_strategy (id, name, strategy_key, scene_key, strategy_type, type, expression_relation, priority, threshold, score, return_code, ability_source, description, operator, state)
VALUES (1, '登录异常检测策略', 'LOGIN_ABNORMAL', 'login_protection', 1, 1, '||', 10, 1, 80, 'VERIFY', 'RULE_ENGINE', '检测异常登录行为', 'admin', 2);
INSERT INTO t_strategy (id, name, strategy_key, scene_key, strategy_type, type, expression_relation, priority, threshold, score, return_code, ability_source, description, operator, state)
VALUES (2, '注册风控策略', 'REGISTER_RISK', 'register_protection', 1, 1, '||', 20, 1, 80, 'REJECT', 'RULE_ENGINE', '检测恶意注册行为', 'admin', 2);
INSERT INTO t_strategy (id, name, strategy_key, scene_key, strategy_type, type, expression_relation, priority, threshold, score, return_code, ability_source, description, operator, state)
VALUES (3, '账号异常策略', 'ACCOUNT_ABNORMAL', 'account_security', 1, 1, '||', 30, 1, 90, 'LOCK', 'RULE_ENGINE', '检测账号异常操作', 'admin', 2);

-- ----------------------------
-- 4. 规则配置表 (t_rule)
--    rule_type: EXPRESSION=表达式  LIST=名单匹配  THRESHOLD=阈值比较
-- ----------------------------
INSERT INTO t_rule (id, name, rule_key, scene_key, type, rule_type, rule_express_op, rule_express_right, expression, description, operator, state)
VALUES (1, 'IP黑名单规则', 'ip_blacklist', 'login_protection', 0, 'EXPRESSION', '==', 'true', 'blacklisted == true', '检查IP是否在黑名单中', 'admin', 1);
INSERT INTO t_rule (id, name, rule_key, scene_key, type, rule_type, rule_express_op, rule_express_right, expression, description, operator, state)
VALUES (2, '频繁登录规则', 'frequent_login', 'login_protection', 0, 'EXPRESSION', '>', '5', 'login_count > 5', '检测短时间内频繁登录', 'admin', 1);
INSERT INTO t_rule (id, name, rule_key, scene_key, type, rule_type, rule_express_op, rule_express_right, expression, description, operator, state)
VALUES (3, '异常设备规则', 'abnormal_device', 'account_security', 0, 'EXPRESSION', '>', '80', 'device_risk_score > 80', '检测异常设备登录', 'admin', 1);
INSERT INTO t_rule (id, name, rule_key, scene_key, type, rule_type, rule_express_op, rule_express_right, expression, description, operator, state)
VALUES (4, '批量注册规则', 'batch_register', 'register_protection', 0, 'EXPRESSION', '>', '10', 'register_count > 10', '检测批量注册行为', 'admin', 1);
INSERT INTO t_rule (id, name, rule_key, scene_key, type, rule_type, rule_express_op, rule_express_right, expression, description, operator, state)
VALUES (5, '高风险IP注册', 'high_risk_ip_reg', 'register_protection', 0, 'EXPRESSION', '==', 'high', 'ip_risk_level == ''high''', '高风险IP注册检测', 'admin', 1);

-- ----------------------------
-- 5. 策略-规则关系表 (t_strategy_rule_relation)
--    关联关系: strategy_id → t_strategy.id,  rule_id → t_rule.id
--
--    策略1(登录异常检测): 规则1(IP黑名单) → 规则2(频繁登录) → 规则3(异常设备)
--    策略2(注册风控):     规则4(批量注册) → 规则5(高风险IP)
--    策略3(账号异常):     规则1(IP黑名单) → 规则3(异常设备)
-- ----------------------------
INSERT INTO t_strategy_rule_relation (strategy_id, rule_id, priority) VALUES (1, 1, 1);
INSERT INTO t_strategy_rule_relation (strategy_id, rule_id, priority) VALUES (1, 2, 2);
INSERT INTO t_strategy_rule_relation (strategy_id, rule_id, priority) VALUES (1, 3, 3);
INSERT INTO t_strategy_rule_relation (strategy_id, rule_id, priority) VALUES (2, 4, 1);
INSERT INTO t_strategy_rule_relation (strategy_id, rule_id, priority) VALUES (2, 5, 2);
INSERT INTO t_strategy_rule_relation (strategy_id, rule_id, priority) VALUES (3, 1, 1);
INSERT INTO t_strategy_rule_relation (strategy_id, rule_id, priority) VALUES (3, 3, 2);

-- ----------------------------
-- 6. 基础信息特征配置表 (t_base_info_feature)
--    feature_type: base=基础特征  calculation=累计特征  algorithm=算法特征  list=名单特征
--    关联关系: scene_key → t_scene.scene_key
-- ----------------------------
INSERT INTO t_base_info_feature (id, name, feature_key, feature_type, data_type, scene_key, description, state)
VALUES (1,  'IP地址',     'ip_address',     'base',         'STRING',  'login_protection',    '请求来源IP',           1);
INSERT INTO t_base_info_feature (id, name, feature_key, feature_type, data_type, scene_key, description, state)
VALUES (2,  '设备ID',     'device_id',      'base',         'STRING',  'login_protection',    '设备唯一标识',         1);
INSERT INTO t_base_info_feature (id, name, feature_key, feature_type, data_type, scene_key, description, state)
VALUES (3,  '登录次数',   'login_count',    'calculation',  'INT',     'login_protection',    '近N分钟登录次数',      1);
INSERT INTO t_base_info_feature (id, name, feature_key, feature_type, data_type, scene_key, description, state)
VALUES (4,  'IP风险分数', 'ip_risk_score',  'algorithm',    'DOUBLE',  'login_protection',    'IP风险评分(0-100)',    1);
INSERT INTO t_base_info_feature (id, name, feature_key, feature_type, data_type, scene_key, description, state)
VALUES (5,  '注册手机号', 'register_mobile','base',         'STRING',  'register_protection', '注册手机号码',         1);
INSERT INTO t_base_info_feature (id, name, feature_key, feature_type, data_type, scene_key, description, state)
VALUES (6,  '注册IP',     'register_ip',    'base',         'STRING',  'register_protection', '注册来源IP',           1);
INSERT INTO t_base_info_feature (id, name, feature_key, feature_type, data_type, scene_key, description, state)
VALUES (7,  '注册次数',   'register_count', 'calculation',  'INT',     'register_protection', '近N分钟注册次数',      1);

-- ----------------------------
-- 7. 算法特征配置表 (t_algorithm_feature)
--    关联关系: scene_key → t_scene.scene_key
--    model_key: 第三方算法模型标识
--    input_fields: 逗号分隔的输入字段列表
-- ----------------------------
INSERT INTO t_algorithm_feature (id, name, scene_key, model_key, input_fields, output_field, description, operator, state)
VALUES (1, '恶意注册模型', 'login_protection',    'evil_register_v1',        'ip,device_id,mobile',             'evil_score',   '恶意注册识别模型', 'admin', 1);
INSERT INTO t_algorithm_feature (id, name, scene_key, model_key, input_fields, output_field, description, operator, state)
VALUES (2, '撞库攻击模型', 'login_protection',    'credential_stuffing_v1',  'ip,login_count,fail_count',       'stuffing_score','撞库攻击识别模型', 'admin', 1);
INSERT INTO t_algorithm_feature (id, name, scene_key, model_key, input_fields, output_field, description, operator, state)
VALUES (3, '批量注册模型', 'register_protection', 'batch_register_v1',       'ip,mobile,device_id,register_count','batch_score',  '批量注册识别模型', 'admin', 1);

-- ----------------------------
-- 8. 处置方式配置表 (t_disposer_config)
--    disposer_type 决定执行哪种处置方式
-- ----------------------------
INSERT INTO t_disposer_config (id, name, disposer_type, disposer_key, description, operator, state)
VALUES (1, '账号锁定', 'LOCK',  'lock_account',  '临时锁定账号(自动解除)', 'admin', 1);
INSERT INTO t_disposer_config (id, name, disposer_type, disposer_key, description, operator, state)
VALUES (2, '账号封禁', 'BAN',   'ban_account',   '永久封禁账号(手动解除)', 'admin', 1);
INSERT INTO t_disposer_config (id, name, disposer_type, disposer_key, description, operator, state)
VALUES (3, '告警通知', 'ALERT', 'alert_notify',  '发送告警通知(不处置)',   'admin', 1);
INSERT INTO t_disposer_config (id, name, disposer_type, disposer_key, description, operator, state)
VALUES (4, '短信验证', 'VERIFY','sms_verify',    '要求短信二次验证',       'admin', 1);

-- ----------------------------
-- 9. 风险决策配置表 (t_risk_config)
--    risk_level: 1=低风险 2=中风险 3=高风险
--    disposition: 对应 t_disposer_config.disposer_key
-- ----------------------------
INSERT INTO t_risk_config (id, name, risk_key, business_type, risk_level, score_threshold, disposition, description, operator, state)
VALUES (1, '登录保护风险配置', 'login_risk',    'LOGIN',    2, 80, 'lock_account',  '登录场景风险配置', 'admin', 1);
INSERT INTO t_risk_config (id, name, risk_key, business_type, risk_level, score_threshold, disposition, description, operator, state)
VALUES (2, '注册保护风险配置', 'register_risk', 'REGISTER', 2, 80, 'alert_notify',  '注册场景风险配置', 'admin', 1);
INSERT INTO t_risk_config (id, name, risk_key, business_type, risk_level, score_threshold, disposition, description, operator, state)
VALUES (3, '账号安全风险配置', 'account_risk',  'ACCOUNT',  3, 90, 'ban_account',   '账号安全风险配置', 'admin', 1);

-- ----------------------------
-- 10. 黑白名单表 (t_black_white_list)
--     list_type: BLACK=黑名单(命中扣分)  WHITE=白名单(命中加分)
--     list_key: 名单标识维度 ip/device/userId 等
-- ----------------------------
INSERT INTO t_black_white_list (id, list_type, list_key, list_value, description, operator, state)
VALUES (1, 'BLACK', 'ip',      '192.0.2.100',   '示例恶意IP',       'admin', 1);
INSERT INTO t_black_white_list (id, list_type, list_key, list_value, description, operator, state)
VALUES (2, 'WHITE', 'ip',      '198.51.100.1',  '示例可信IP',       'admin', 1);
INSERT INTO t_black_white_list (id, list_type, list_key, list_value, description, operator, state)
VALUES (3, 'BLACK', 'device',  'device_001',    '已知恶意设备',     'admin', 1);
INSERT INTO t_black_white_list (id, list_type, list_key, list_value, description, operator, state)
VALUES (4, 'WHITE', 'userId',  'admin',         '管理员白名单',     'admin', 1);

-- ----------------------------
-- 11. 返回码配置表 (t_return_code)
--     关联关系: scene_key → t_scene.scene_key
--     return_code 值含义由业务场景决定
-- ----------------------------

-- 登录保护场景返回码
INSERT INTO t_return_code (id, return_code, name, description, scene_key, state)
VALUES (1,  'PASS',   '通过', '风控通过，允许操作',   'login_protection', 1);
INSERT INTO t_return_code (id, return_code, name, description, scene_key, state)
VALUES (2,  'REJECT', '拒绝', '风控拒绝，禁止操作',   'login_protection', 1);
INSERT INTO t_return_code (id, return_code, name, description, scene_key, state)
VALUES (3,  'VERIFY', '验证', '需要二次验证',         'login_protection', 1);

-- 注册保护场景返回码
INSERT INTO t_return_code (id, return_code, name, description, scene_key, state)
VALUES (4,  'PASS',   '通过', '风控通过，允许操作',   'register_protection', 1);
INSERT INTO t_return_code (id, return_code, name, description, scene_key, state)
VALUES (5,  'REJECT', '拒绝', '风控拒绝，禁止操作',   'register_protection', 1);

-- 账号安全场景返回码
INSERT INTO t_return_code (id, return_code, name, description, scene_key, state)
VALUES (6,  'LOCK',   '锁定', '账号锁定',             'account_security', 1);
INSERT INTO t_return_code (id, return_code, name, description, scene_key, state)
VALUES (7,  'BAN',    '封禁', '账号封禁',             'account_security', 1);


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

