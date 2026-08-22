# 数据库初始化

Soda 的配置数据通过关系型数据库持久化。开发环境默认使用 H2 内存数据库，生产
环境建议使用 MySQL 8.0 或兼容数据库。

## 目录

```text
database/
└── mysql/
    ├── schema.sql        # 完整表结构
    └── sample-data.sql   # 可选的人工构造演示数据
```

后端开发环境使用的源脚本位于：

- `server/config/src/main/resources/schema.sql`
- `server/config/src/main/resources/catalog-schema.sql`
- `server/config/src/main/resources/data.sql`
- `server/config/src/main/resources/catalog-data.sql`

`database/mysql` 中的脚本是面向使用者的 MySQL 交付版本。

## H2 开发环境

使用 `dev` profile 启动后端时，Spring 会自动：

1. 创建 `jdbc:h2:mem:soda` 内存数据库；
2. 执行核心表和配置目录表结构；
3. 导入完全人工构造的示例数据；
4. 构建第一版可执行配置快照。

无需手动初始化。后端停止后，内存数据会被清除。

## MySQL 初始化

创建数据库：

```sql
CREATE DATABASE soda
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
```

导入表结构：

```bash
mysql -h 127.0.0.1 -u root -p soda < database/mysql/schema.sql
```

如需体验演示数据，再执行：

```bash
mysql -h 127.0.0.1 -u root -p soda < database/mysql/sample-data.sql
```

`sample-data.sql` 只能用于本地开发或测试，不能导入生产数据库。

## 启动生产 Profile

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=soda
export DB_USER=soda
export DB_PASS='replace-me'
java -jar soda-web.jar
```

生产 profile 不会自动执行初始化脚本，避免应用启动时意外修改数据库。

## 表分组

| 表前缀/表名 | 说明 |
| --- | --- |
| `t_business_side`、`t_scene` | 业务方与场景 |
| `t_rule`、`t_strategy`、`t_strategy_rule_relation` | 规则与策略 |
| `t_base_info_feature`、`t_algorithm_feature` | 可执行特征配置 |
| `t_catalog_*` | 参数、工具、编辑器和统计特征配置目录 |
| `t_risk_config`、`t_black_white_list`、`t_return_code` | 风险决策配置 |
| `t_disposer_config`、`t_disposer_info` | 处置配置与结果 |

## 变更约束

- 表结构变更必须同步更新 H2 和 MySQL 交付脚本。
- 初始化脚本只能包含结构和人工构造数据。
- 禁止提交生产导出、用户数据、内部地址、账号或凭据。
- 生产升级建议引入 Flyway 或 Liquibase 管理版本化迁移。
