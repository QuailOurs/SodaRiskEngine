-- Soda configuration catalog schema.
-- Compatible with H2 MODE=MySQL; the MySQL delivery scripts are under database/mysql.

CREATE TABLE IF NOT EXISTS `t_catalog_business_side` (
  `id` BIGINT PRIMARY KEY,
  `name` VARCHAR(1000),
  `business_side_key` VARCHAR(1000),
  `system_key` VARCHAR(1000),
  `description` VARCHAR(1000),
  `operator` VARCHAR(1000),
  `state` SMALLINT,
  `update_time` TIMESTAMP,
  `create_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_complement_key` (
  `id` BIGINT PRIMARY KEY,
  `tool_id` BIGINT,
  `complement_key` VARCHAR(1000),
  `state` SMALLINT,
  `operator` VARCHAR(1000),
  `description` VARCHAR(1000),
  `create_time` TIMESTAMP,
  `update_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_data_type` (
  `id` BIGINT PRIMARY KEY,
  `type_name` VARCHAR(1000),
  `description` VARCHAR(1000),
  `operator` VARCHAR(1000),
  `state` SMALLINT,
  `update_time` TIMESTAMP,
  `create_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_feature_baseinfo` (
  `id` BIGINT PRIMARY KEY,
  `name` VARCHAR(1000),
  `tool_id` INT,
  `scene_key` VARCHAR(1000),
  `src_param_id` INT,
  `ext_param` CLOB,
  `description` VARCHAR(1000),
  `operator` VARCHAR(1000),
  `state` SMALLINT,
  `update_time` TIMESTAMP,
  `create_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_feature_baseinfo_tool_field` (
  `id` BIGINT PRIMARY KEY,
  `tool_id` BIGINT,
  `name` VARCHAR(1000),
  `description` VARCHAR(1000),
  `operator` VARCHAR(1000),
  `state` SMALLINT,
  `update_time` TIMESTAMP,
  `create_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_feature_baseinfo_tool_field_relation` (
  `id` BIGINT PRIMARY KEY,
  `feature_baseinfo_id` BIGINT,
  `param_id` BIGINT,
  `state` SMALLINT,
  `update_time` TIMESTAMP,
  `create_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_feature_list` (
  `id` BIGINT PRIMARY KEY,
  `name` VARCHAR(1000),
  `tool_id` INT,
  `scene_key` VARCHAR(1000),
  `src_param_id` INT,
  `description` VARCHAR(1000),
  `operator` VARCHAR(1000),
  `state` SMALLINT,
  `update_time` TIMESTAMP,
  `create_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_feature_statistics` (
  `id` BIGINT PRIMARY KEY,
  `name` VARCHAR(1000),
  `scene_key` VARCHAR(1000),
  `feature_id` INT,
  `val_id` BIGINT,
  `before_minute` INT,
  `write_state` SMALLINT,
  `write_strategy_id` BIGINT,
  `query_strategy_id` BIGINT,
  `description` VARCHAR(1000),
  `operator` VARCHAR(1000),
  `state` SMALLINT,
  `reference_times` INT,
  `update_time` TIMESTAMP,
  `create_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_feature_statistics_param_relation` (
  `id` BIGINT PRIMARY KEY,
  `statistics_feature_id` BIGINT,
  `param_id` BIGINT,
  `state` SMALLINT,
  `update_time` TIMESTAMP,
  `create_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_param` (
  `id` BIGINT PRIMARY KEY,
  `name` VARCHAR(1000),
  `param_key` VARCHAR(1000),
  `type_id` VARCHAR(1000),
  `scene_key` VARCHAR(1000),
  `description` VARCHAR(1000),
  `operator` VARCHAR(1000),
  `state` SMALLINT,
  `reference_times` INT,
  `update_time` TIMESTAMP,
  `create_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_param_complement_key_relation` (
  `id` BIGINT PRIMARY KEY,
  `param_id` BIGINT,
  `complement_key_id` BIGINT,
  `state` SMALLINT,
  `operator` VARCHAR(1000),
  `create_time` TIMESTAMP,
  `update_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_rule` (
  `id` BIGINT PRIMARY KEY,
  `name` VARCHAR(1000),
  `scene_key` VARCHAR(1000),
  `type` SMALLINT,
  `feature_id` BIGINT,
  `rule_express_left` BIGINT,
  `rule_express_op` VARCHAR(1000),
  `rule_express_right` CLOB,
  `expression` CLOB,
  `ext_param` CLOB,
  `description` CLOB,
  `operator` VARCHAR(1000),
  `state` SMALLINT,
  `update_time` TIMESTAMP,
  `create_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_scene` (
  `id` BIGINT PRIMARY KEY,
  `name` VARCHAR(1000),
  `scene_key` VARCHAR(1000),
  `business_side_key` VARCHAR(1000),
  `description` VARCHAR(1000),
  `operator` VARCHAR(1000),
  `state` SMALLINT,
  `update_time` TIMESTAMP,
  `create_time` TIMESTAMP,
  `pm_account` VARCHAR(1000),
  `rd_account` VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS `t_catalog_strategy` (
  `id` BIGINT PRIMARY KEY,
  `name` VARCHAR(1000),
  `scene_key` VARCHAR(1000),
  `type` SMALLINT,
  `expression` CLOB,
  `expression_relation` VARCHAR(1000),
  `priority` SMALLINT,
  `threshold` INT,
  `score` DECIMAL(20,2),
  `return_code` VARCHAR(1000),
  `ability_source` CLOB,
  `description` VARCHAR(1000),
  `operator` VARCHAR(1000),
  `state` SMALLINT,
  `update_time` TIMESTAMP,
  `create_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_strategy_rule_relation` (
  `id` BIGINT PRIMARY KEY,
  `rule_id` BIGINT,
  `strategy_id` BIGINT,
  `state` SMALLINT,
  `update_time` TIMESTAMP,
  `create_time` TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `t_catalog_tool` (
  `id` BIGINT PRIMARY KEY,
  `name` VARCHAR(1000),
  `type` SMALLINT,
  `state` SMALLINT,
  `description` VARCHAR(1000),
  `operator` VARCHAR(1000),
  `reference_times` INT,
  `create_time` TIMESTAMP,
  `update_time` TIMESTAMP
);
