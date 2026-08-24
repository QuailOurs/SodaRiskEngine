# 下一 Tag 功能验证与修复记录

本文档记录 2026-08-24 对 `2.0.0-SNAPSHOT` 的功能补全、测试数据验证和缺陷修复，供下一次 tag 发布说明、发布评审和回归检查使用。

## 验证结论

本轮覆盖功能矩阵中的配置管理、规则执行、特征查询、策略计算、风险决策、处置、日志、外部适配器契约和控制台 API 契约。自动化验证结果如下：

| 检查项 | 结果 | 说明 |
| --- | --- | --- |
| 后端全量测试 | 通过 | 96 项，0 failure，0 error，0 skipped |
| 后端发布打包 | 通过 | 7 个 Maven reactor 模块全部成功，Web 可执行 JAR 重打包成功 |
| 控制台单元测试 | 通过 | 7 项，覆盖调试台和配置管理 API |
| 控制台代码检查 | 通过 | 0 lint error |
| 控制台生产构建 | 通过 | 构建产物生成成功，存在体积告警 |
| 开源内容检查 | 通过 | 清理本地构建产物后执行检查 |

后端分模块结果：

| 模块 | 测试数 | 失败/错误/跳过 |
| --- | ---: | ---: |
| `api` | 4 | 0/0/0 |
| `common` | 21 | 0/0/0 |
| `core` | 36 | 0/0/0 |
| `config` | 4 | 0/0/0 |
| `service` | 16 | 0/0/0 |
| `web` | 15 | 0/0/0 |

`config` 包含 4 项同步编排单元测试；数据库读写、状态切换和缓存同步继续由 `web`
模块的 Spring Boot 集成测试覆盖。

## 验证环境与命令

- Windows 11 amd64
- Oracle JDK 23.0.1，Maven 编译目标为 Java 17
- Apache Maven 3.9.6
- Node.js 24.14.0、npm 11.9.0；旧 Webpack 验证进程启用 `--openssl-legacy-provider`
- Spring `dev` profile、H2 内存数据库、内存缓存降级；不依赖外部 Redis、Kafka 或 Elasticsearch

```bash
mvn -f server/pom.xml test

cd apps/console
npm ci --legacy-peer-deps
$env:NODE_OPTIONS = "--openssl-legacy-provider" # Node.js 18+ 验证旧 Webpack 时需要
npm run test:unit
npm run lint
npm run build

python tools/check_open_source.py
```

## 功能与测试数据矩阵

| 功能域 | 代表性测试数据 | 验证内容 | 主要测试 |
| --- | --- | --- | --- |
| 缓存降级 | `hash:1`、`set:1`、`list:1`、0 ms TTL | String/Hash/Set/List、计数、过期、锁、通配 key 和 JSON | `RedisCacheServiceTest` |
| 规则函数 | `soda-risk-engine`、`[safe,risk]`、`int[]{1,2,3}`、空值 | 21 个 Aviator 自定义函数、数组交集、字段、长度和合并指标操作符 | `CustomExpressionFunctionsTest` |
| 规则执行 | `amount > 100`、有效/损坏规则 ID | 单规则、多规则、异常隔离和命中明细 | `RuleServiceTest`、`RuleExpressionEvaluatorTest` |
| 数据补全 | `192.0.2.1`、`deviceId=d-1`、失败数据源 | IP/设备处理器组合、输入不变和单源异常隔离 | `DataComplementServiceTest` |
| 特征处理 | `192.0.2.1`、`count:10m:user-1=8`、两个 1 秒慢任务、拒绝执行 | 七类特征、并行执行、全局超时、队列拒绝和失败降级 | `FeatureServiceTest`、`FeatureHandlersTest` |
| 策略计算 | `blacklisted=true`、`login_count=6` | OR/AND/阈值计算、批量契约、无策略和损坏 JSON | `ComputeEngineTest`、`StrategyEngineServiceImplTest` |
| 配置管理 | `test_business`、`test_scene`、`test_rule`、`TEST_STRATEGY` | 业务方、场景、参数、工具、规则、策略、基础/统计特征、补全关系的查询和增删改 | `ConfigurationManagementIntegrationTest` |
| 配置同步编排 | `scene → rule → strategy`、`db down`、未知配置域、重叠同步 | 依赖顺序、失败隔离、防重入、选择性同步和结构化报告 | `ConfigSyncCoordinatorTest`、`ConfigurationManagementIntegrationTest` |
| 风险配置 | `test_risk`、`test-user`、`TEST_CODE` | 风险配置、黑白名单、返回码、阈值详情和名单优先级 | `ConfigurationManagementIntegrationTest`、`DecisionServiceTest` |
| 风险路由 | `riskIdentification`、`account-security` | camelCase/连字符路由归一化、账号安全和未知类型 | `RiskDecisionEngineTest`、`PublicControllerIntegrationTest` |
| 处置 | `user-lock`、`user-ban`、日锁定上限 200 | ALERT/LOCK/BAN、批量执行、状态查询、释放和未知方式 | `DisposerFlowServiceTest`、`DisposerServiceImplTest` |
| 日志 | 策略命中、处置、风险决策和空 trace ID | 三类日志写入/查询、trace ID 自动生成、24 小时 TTL | `LogStorageServiceTest` |
| 扩展计算 | `mean=100`、`stddev=10`、range `2` | 标准差区间内外、缺失和损坏统计数据 | `ExtendedCalculateServiceTest` |
| 鉴权 | `demo_business`、不存在的 open key | 映射命中、空 Hash 不再被误判为合法业务方 | `AuthenticationServiceTest` |
| 消息与外部适配 | 策略/处置消息、`192.0.2.1`、`user-1` | 消费成功与异常隔离、适配器选择及确定性降级契约 | `MessageConsumersTest`、`ThirdPartyAdaptersTest` |
| HTTP 公开接口 | `admin/admin`、`http-user`、`normal-user`、不支持的特征类型 | 登录、健康/版本、策略、风险、处置、降级决策和同步报告接口契约 | `EngineEvaluationControllerIntegrationTest`、`PublicControllerIntegrationTest`、`ConfigurationManagementIntegrationTest` |
| 控制台 | `login_protection`、`demo_business`、配置 CRUD 请求 | 调试台提交/同步、全部配置域路径、HTTP method、query/body 编码 | `EnginePlayground.spec.js`、`ConfigAdmin.spec.js` |

所有集成测试数据均来自仓库内人工构造的 H2 示例数据，测试新增数据在事务结束后回滚。

## 修复记录

| 编号 | 问题 | 修复及验证 |
| --- | --- | --- |
| FIX-01 | 无 Redis 时，内存降级仅完整支持 String；Hash/Set/List 无法统一删除、过期或枚举 | 补齐四类结构、TTL、原子 `setIfAbsent`、List 范围和 glob key 行为，并增加 Redis 兼容删除测试 |
| FIX-02 | LOCK/BAN 写入的 key 与公开状态查询使用的 key 不一致，执行成功后仍显示未处置 | 统一为 `soda:disposer:user:{userId}:{type}`，并优先读取 `StrategyHitResult.userId` |
| FIX-03 | 未配置业务方时，空 Hash 被当作有效 open ID | 鉴权回退仅在 Hash 非空时生效 |
| FIX-04 | 计算特征处理器错误声明支持 `statistics`，会抢占真正的统计处理器 | 限定为 `calculation`，用统计缓存数据验证正确处理器 |
| FIX-05 | 特殊规则表达式存在参数个数、变量引用和空值问题；原生数组交集失效 | 修正 FIELD_EXISTS、空对象和长度表达式生成，补齐空值、对象数组和基本类型数组处理 |
| FIX-06 | 规则集合中存在非数字 ID 时，异常处理会再次解析同一坏 ID，导致整批结果丢失 | 将 ID 解析与规则执行分段隔离，坏记录返回未命中明细，其余规则继续执行 |
| FIX-07 | 示例在线策略状态为 2，但旧 Redis 同步只加载状态 1；重复同步还会残留旧规则和路由 | 同步状态 1/2 策略，并在全量同步前清理旧策略、场景路由、规则和场景规则集合 |
| FIX-08 | 处置和风险日志只发送 Kafka，无法通过日志查询接口读取；Redis TTL 调用也未按时长语义设置 | 三类日志统一写入 Kafka 加 Redis/内存查询存储，缺失 trace ID 时自动生成，TTL 改为 24 小时 `Duration` |
| FIX-09 | 标准差区间判断为未实现占位，任何输入都返回 `true` | 从 `feature:stats:{id}` 读取 mean/stddev，校验有限数、范围和缺失数据后计算 |
| FIX-10 | `riskIdentification` 和 `account-security` 无法稳定匹配处理器，账号安全路由缺失 | 统一 camelCase、空格和连字符归一化，并注册 `ACCOUNT_SECURITY` 路由 |
| FIX-11 | 健康接口仍返回旧项目名和 `3.0.0` | 应用名读取 Spring 配置，版本读取 Maven `build-info`，tag 改版本后接口自动同步 |
| FIX-12 | 前端锁文件固化已失效的 `registry.npm.taobao.org` tarball，`npm ci` 无法复现 | 95 个旧下载地址归一到 npm 官方 registry，并用全新安装验证完整性 |
| FIX-13 | 主 HTTP 决策绕过了原设计中的数据补全和特征阶段，扩展处理器只在兼容路径生效 | 将补全、特征作业接入 `StrategyRuntimeEngine`，保留请求数据不变并在结果中返回降级诊断 |
| FIX-14 | 多类特征串行调用且没有总超时，一个或多个外部源变慢会线性放大决策耗时 | 使用有界线程池并行执行，全部任务共享 200 ms deadline，失败和超时按类型隔离 |
| FIX-15 | 手工同步和定时同步重复维护七项调用顺序，任一异常会中止后续配置域 | 引入贡献者/协调器，统一依赖顺序、防重入、逐项失败隔离和结构化同步报告 |

## 本轮新增回归用例

| 用例 | 防止的回归 |
| --- | --- |
| 特征线程池拒绝任务时返回降级结果 | 防止队列满载把异常抛到主决策链路 |
| 两轮配置同步重叠时第二轮返回 `skipped=true` | 防止贡献者重复执行和缓存互相覆盖 |
| HTTP 决策遇到未知特征类型仍保留策略命中 | 防止非关键特征故障改变核心风控结论 |
| 全量/风险同步接口返回有序逐域报告 | 防止发布工具无法判断具体同步步骤和结果 |

## 下一 Tag 发布说明建议

可直接摘录以下内容：

### Added

- 新增覆盖缓存、规则函数、特征处理、策略计算、风险决策、处置、日志、配置中心和公开 HTTP API 的自动化测试。
- 新增控制台配置管理 API 契约测试，覆盖全部配置域和运行时操作。
- 新增可插拔数据补全、并行特征作业、统一超时和结构化降级诊断。
- 新增可扩展配置同步协调器，统一手工与定时同步并返回逐配置域结果。

### Fixed

- 修复开发模式内存缓存对 Hash、Set、List、TTL 和锁语义支持不完整的问题。
- 修复 LOCK/BAN 处置状态无法查询、用户 ID 取值不一致的问题。
- 修复统计特征处理器被计算特征处理器抢占的问题。
- 修复规则函数在空值、数组交集、字段存在和长度表达式场景下的错误。
- 修复坏规则 ID 导致整批规则评估中断的问题。
- 修复在线策略未进入 Redis、配置重同步残留旧规则和旧路由的问题。
- 修复处置/风险日志不可查询、标准差计算恒为通过的问题。
- 修复风险业务类型路由兼容性和账号安全入口缺失的问题。
- 修复健康接口项目名/版本错误和控制台依赖锁文件不可重复安装的问题。
- 修复主决策链路未执行补全/特征阶段、特征串行无超时和配置同步编排重复的问题。

## 发布前仍需确认

- 本轮验证的是外部服务适配器的本地确定性降级契约，不代表百度、数美、腾讯等真实供应商连通性；生产发布前需在有凭据环境执行契约测试。
- 开发 profile 使用 H2 和内存降级。生产发布前仍需在 MySQL、Redis、Kafka 环境完成并发、TTL、消息投递和故障恢复验证。
- 当前 Vue 2 依赖树在 `npm audit` 中报告 192 个已知问题（15 low、75 moderate、79 high、23 critical）。这是既有依赖栈风险，功能回归通过不等于安全放行，正式公网发布前应单独安排依赖升级和安全回归。
- 控制台生产入口约 2.71 MiB，构建存在资源体积告警；不阻塞本轮功能验证，但建议后续进行分包和按需加载。
- 特征查询线程池默认 8 个线程、队列 256、总超时 200 ms；生产发布前需结合真实供应商延迟、并发和拒绝率压测校准。
