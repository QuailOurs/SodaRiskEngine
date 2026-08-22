# 示例数据

Soda 仓库只包含人工构造的演示数据，不包含任何生产数据库导出或真实用户数据。

## 数据集组成

核心示例围绕三个场景构造：

| 业务方 | 场景 | 用途 |
| --- | --- | --- |
| `demo_business` | `login_protection` | 登录黑名单、频率和设备风险决策 |
| `demo_business` | `register_protection` | 批量注册和 IP 风险决策 |
| `demo_business` | `account_security` | 账号操作风险决策 |

配置目录还提供少量工具、参数、统计特征、数据补全和编辑器选项，用于保证管理
控制台的各配置页面在首次启动后即可操作。

## 脚本位置

- 核心表结构：`server/config/src/main/resources/schema.sql`
- 配置目录表结构：`server/config/src/main/resources/catalog-schema.sql`
- 核心示例数据：`server/config/src/main/resources/data.sql`
- 配置目录示例数据：`server/config/src/main/resources/catalog-data.sql`
- MySQL 交付版本：`database/mysql/`

## 数据要求

新增示例数据必须满足以下要求：

- 由贡献者人工构造，不从生产环境抽样或脱敏转换；
- 使用文档保留地址段，例如 `192.0.2.0/24`；
- 不出现真实姓名、邮箱、手机号、账号、内部域名和公司标识；
- 规则表达式和关联关系能够由自动化测试覆盖；
- 记录规模保持足够小，便于代码审查和问题定位。

## 重置

开发 profile 使用 H2 内存数据库。重启后端即可恢复仓库中的初始示例数据。
