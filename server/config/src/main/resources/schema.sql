-- ============================================================
-- Soda 数据库建表脚本
-- 兼容: MySQL 8.0+ / H2 (MODE=MySQL)
-- 表名统一使用 t_ 前缀，字段使用下划线命名
-- MyBatis-Plus 自动驼峰映射
-- ============================================================

-- ----------------------------
-- 1. 系统用户表
-- ----------------------------
DROP TABLE IF EXISTS t_sys_user;
CREATE TABLE t_sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username    VARCHAR(100) NOT NULL                COMMENT '用户名',
    password    VARCHAR(200) DEFAULT NULL             COMMENT '密码',
    nickname    VARCHAR(100) DEFAULT NULL             COMMENT '昵称',
    avatar      VARCHAR(500) DEFAULT NULL             COMMENT '头像URL',
    role        VARCHAR(50)  DEFAULT 'admin'          COMMENT '角色: admin/user',
    state       INT          DEFAULT 1                COMMENT '状态: 0-禁用 1-启用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

-- ----------------------------
-- 2. 业务方配置表
-- ----------------------------
DROP TABLE IF EXISTS t_business_side;
CREATE TABLE t_business_side (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name              VARCHAR(200) NOT NULL                COMMENT '业务方名称',
    business_side_key VARCHAR(200) NOT NULL                COMMENT '业务方标识',
    system_key        VARCHAR(200) DEFAULT NULL             COMMENT '接入系统标识',
    description       VARCHAR(500) DEFAULT NULL             COMMENT '描述',
    operator          VARCHAR(100) DEFAULT NULL             COMMENT '操作人',
    state             INT          DEFAULT 1                COMMENT '状态: 0-禁用 1-启用',
    create_time       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE (business_side_key)
);

-- ----------------------------
-- 3. 场景配置表
-- ----------------------------
DROP TABLE IF EXISTS t_scene;
CREATE TABLE t_scene (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name             VARCHAR(200) NOT NULL                COMMENT '场景名称',
    scene_key        VARCHAR(100) DEFAULT NULL             COMMENT '场景标识(唯一)',
    business_side_id BIGINT       DEFAULT NULL             COMMENT '业务方ID',
    business_side_key VARCHAR(200) DEFAULT NULL             COMMENT '业务方标识',
    pm_account       VARCHAR(100) DEFAULT NULL             COMMENT '产品联系人',
    rd_account       VARCHAR(100) DEFAULT NULL             COMMENT '研发联系人',
    state            INT          DEFAULT 1                COMMENT '状态: 0-禁用 1-启用',
    description      VARCHAR(500) DEFAULT NULL             COMMENT '描述',
    operator         VARCHAR(100) DEFAULT NULL             COMMENT '操作人',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

-- ----------------------------
-- 4. 策略配置表
-- ----------------------------
DROP TABLE IF EXISTS t_strategy;
CREATE TABLE t_strategy (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name          VARCHAR(200) NOT NULL                COMMENT '策略名称',
    strategy_key  VARCHAR(100) DEFAULT NULL             COMMENT '策略标识',
    scene_key     VARCHAR(100) DEFAULT NULL             COMMENT '关联场景标识',
    strategy_type INT          DEFAULT 0                COMMENT '策略类型: 0-默认 1-实时 2-准实时',
    type          INT          DEFAULT 0                COMMENT '策略类型兼容字段',
    expression    TEXT         DEFAULT NULL             COMMENT '策略表达式',
    expression_relation VARCHAR(255) DEFAULT NULL       COMMENT '规则关系: &&/||',
    priority      INT          DEFAULT 50               COMMENT '优先级',
    threshold     INT          DEFAULT NULL             COMMENT '命中阈值',
    score         DECIMAL(20,2) DEFAULT NULL            COMMENT '策略分值',
    return_code   VARCHAR(255) DEFAULT NULL             COMMENT '返回码',
    ability_source TEXT        DEFAULT NULL             COMMENT '能力来源',
    state         INT          DEFAULT 1                COMMENT '状态: 0-禁用 1-启用',
    description   VARCHAR(500) DEFAULT NULL             COMMENT '描述',
    operator      VARCHAR(100) DEFAULT NULL             COMMENT '操作人',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

-- ----------------------------
-- 5. 规则配置表
-- ----------------------------
DROP TABLE IF EXISTS t_rule;
CREATE TABLE t_rule (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(200) NOT NULL                COMMENT '规则名称',
    rule_key    VARCHAR(100) DEFAULT NULL             COMMENT '规则标识',
    scene_key   VARCHAR(100) DEFAULT NULL             COMMENT '场景标识',
    type        INT          DEFAULT 0                COMMENT '规则类型兼容字段',
    tool_id     BIGINT       DEFAULT NULL             COMMENT '旧版编辑器数据源工具ID',
    src_param_id BIGINT      DEFAULT NULL             COMMENT '旧版编辑器查询参数ID',
    dest_param_ids_json TEXT DEFAULT NULL             COMMENT '旧版编辑器目标字段ID列表',
    feature_id  BIGINT       DEFAULT NULL             COMMENT '特征ID',
    rule_express_left BIGINT DEFAULT NULL             COMMENT '表达式左值配置ID',
    rule_express_op VARCHAR(50) DEFAULT NULL           COMMENT '表达式操作符',
    rule_express_right TEXT DEFAULT NULL               COMMENT '表达式右值',
    expression  TEXT         DEFAULT NULL             COMMENT '规则表达式(Aviator语法)',
    ext_param   TEXT         DEFAULT NULL             COMMENT '扩展参数',
    rule_type   VARCHAR(50)  DEFAULT NULL             COMMENT '规则类型: EXPRESSION/LIST/THRESHOLD',
    state       INT          DEFAULT 1                COMMENT '状态: 0-禁用 1-启用',
    description VARCHAR(500) DEFAULT NULL             COMMENT '描述',
    operator    VARCHAR(100) DEFAULT NULL             COMMENT '操作人',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

-- ----------------------------
-- 6. 策略-规则关系表
-- ----------------------------
DROP TABLE IF EXISTS t_strategy_rule_relation;
CREATE TABLE t_strategy_rule_relation (
    id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    strategy_id BIGINT NOT NULL                COMMENT '策略ID(t_strategy.id)',
    rule_id     BIGINT NOT NULL                COMMENT '规则ID(t_rule.id)',
    priority    INT    DEFAULT 0               COMMENT '优先级(越小越先执行)',
    PRIMARY KEY (id)
);

-- ----------------------------
-- 7. 基础信息特征配置表
-- ----------------------------
DROP TABLE IF EXISTS t_base_info_feature;
CREATE TABLE t_base_info_feature (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name         VARCHAR(200) NOT NULL                COMMENT '特征名称',
    feature_key  VARCHAR(100) DEFAULT NULL             COMMENT '特征标识',
    feature_type VARCHAR(50)  DEFAULT NULL             COMMENT '特征类型: base/calculation/algorithm/list',
    data_type    VARCHAR(50)  DEFAULT NULL             COMMENT '数据类型: STRING/INT/DOUBLE',
    scene_key    VARCHAR(100) DEFAULT NULL             COMMENT '关联场景标识(t_scene.scene_key)',
    state        INT          DEFAULT 1                COMMENT '状态: 0-禁用 1-启用',
    description  VARCHAR(500) DEFAULT NULL             COMMENT '描述',
    operator     VARCHAR(100) DEFAULT NULL             COMMENT '操作人',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

-- ----------------------------
-- 7. 算法特征配置表
-- ----------------------------
DROP TABLE IF EXISTS t_algorithm_feature;
CREATE TABLE t_algorithm_feature (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name         VARCHAR(200) NOT NULL                COMMENT '特征名称',
    scene_key    VARCHAR(100) DEFAULT NULL             COMMENT '关联场景标识(t_scene.scene_key)',
    model_key    VARCHAR(100) DEFAULT NULL             COMMENT '模型标识',
    input_fields VARCHAR(500) DEFAULT NULL             COMMENT '输入字段(逗号分隔)',
    output_field VARCHAR(200) DEFAULT NULL             COMMENT '输出字段',
    description  VARCHAR(500) DEFAULT NULL             COMMENT '描述',
    state        INT          DEFAULT 1                COMMENT '状态: 0-禁用 1-启用',
    operator     VARCHAR(100) DEFAULT NULL             COMMENT '操作人',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

-- ----------------------------
-- 8. 处置方式配置表
-- ----------------------------
DROP TABLE IF EXISTS t_disposer_config;
CREATE TABLE t_disposer_config (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name          VARCHAR(200) NOT NULL                COMMENT '处置名称',
    disposer_type VARCHAR(50)  DEFAULT NULL             COMMENT '处置类型: LOCK/BAN/ALERT',
    disposer_key  VARCHAR(100) DEFAULT NULL             COMMENT '处置标识',
    state         INT          DEFAULT 1                COMMENT '状态: 0-禁用 1-启用',
    description   VARCHAR(500) DEFAULT NULL             COMMENT '描述',
    operator      VARCHAR(100) DEFAULT NULL             COMMENT '操作人',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

-- ----------------------------
-- 9. 风险决策配置表
-- ----------------------------
DROP TABLE IF EXISTS t_risk_config;
CREATE TABLE t_risk_config (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name            VARCHAR(200) NOT NULL                COMMENT '配置名称',
    risk_key        VARCHAR(100) DEFAULT NULL             COMMENT '风险标识',
    business_type   VARCHAR(100) DEFAULT NULL             COMMENT '业务类型',
    risk_level      INT          DEFAULT 0                COMMENT '风险等级: 1-低 2-中 3-高',
    score_threshold INT          DEFAULT 0                COMMENT '分数阈值',
    disposition     VARCHAR(200) DEFAULT NULL             COMMENT '处置方式(disposer_key)',
    state           INT          DEFAULT 1                COMMENT '状态: 0-禁用 1-启用',
    description     VARCHAR(500) DEFAULT NULL             COMMENT '描述',
    operator        VARCHAR(100) DEFAULT NULL             COMMENT '操作人',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

-- ----------------------------
-- 10. 黑白名单表
-- ----------------------------
DROP TABLE IF EXISTS t_black_white_list;
CREATE TABLE t_black_white_list (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    list_type   VARCHAR(20)  DEFAULT NULL             COMMENT '名单类型: BLACK/WHITE',
    list_key    VARCHAR(100) DEFAULT NULL             COMMENT '名单标识: ip/device/userId',
    list_value  VARCHAR(500) DEFAULT NULL             COMMENT '名单值',
    state       INT          DEFAULT 1                COMMENT '状态: 0-禁用 1-启用',
    description VARCHAR(500) DEFAULT NULL             COMMENT '描述',
    operator    VARCHAR(100) DEFAULT NULL             COMMENT '操作人',
    expire_time DATETIME     DEFAULT NULL             COMMENT '过期时间(空表示永不过期)',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

-- ----------------------------
-- 11. 返回码配置表
-- ----------------------------
DROP TABLE IF EXISTS t_return_code;
CREATE TABLE t_return_code (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    return_code VARCHAR(100) NOT NULL                COMMENT '返回码',
    name        VARCHAR(200) DEFAULT NULL             COMMENT '名称',
    description VARCHAR(500) DEFAULT NULL             COMMENT '描述',
    scene_key   VARCHAR(100) DEFAULT NULL             COMMENT '关联场景标识(t_scene.scene_key)',
    state       INT          DEFAULT 1                COMMENT '状态: 0-禁用 1-启用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

-- ----------------------------
-- 12. 处置记录表(记录用户被处置的历史)
-- ----------------------------
DROP TABLE IF EXISTS t_disposer_info;
CREATE TABLE t_disposer_info (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id       VARCHAR(100) NOT NULL                COMMENT '用户ID',
    disposer_type VARCHAR(50)  DEFAULT NULL             COMMENT '处置类型: LOCK/BAN/ALERT',
    disposer_key  VARCHAR(100) DEFAULT NULL             COMMENT '处置标识(t_disposer_config.disposer_key)',
    strategy_key  VARCHAR(100) DEFAULT NULL             COMMENT '触发策略标识(t_strategy.strategy_key)',
    scene_key     VARCHAR(100) DEFAULT NULL             COMMENT '场景标识(t_scene.scene_key)',
    state         INT          DEFAULT 1                COMMENT '状态: 0-已解除 1-生效中',
    description   VARCHAR(500) DEFAULT NULL             COMMENT '描述',
    operator      VARCHAR(100) DEFAULT NULL             COMMENT '操作人',
    start_time    DATETIME     DEFAULT NULL             COMMENT '处置开始时间',
    end_time      DATETIME     DEFAULT NULL             COMMENT '处置结束时间',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);
