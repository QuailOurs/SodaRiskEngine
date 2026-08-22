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
