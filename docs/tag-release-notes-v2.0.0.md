# Soda v2.0.0 候选 Tag 发布说明

发布日期：待定  
验证基线：`2.0.0-SNAPSHOT`（2026-08-24）  
建议 Tag：`v2.0.0`

## 发布结论

本候选版本已完成后端、控制台和构建链路回归，功能验证通过，可进入 tag 发布评审。
本结论针对仓库内开发环境和人工构造测试数据；生产上线仍需完成真实中间件、供应商契约、
容量与安全检查。

## 版本亮点

- 主决策链路统一为“数据补全 → 并行特征作业 → 规则 → 策略”，同时保留不可变运行时快照原子切换。
- 新增可插拔补全处理器和按特征类型扩展的处理器模型，单个外部数据源失败不会中断决策。
- 特征作业使用有界线程池和共享总超时，异常、超时、无处理器及队列拒绝均返回结构化降级诊断。
- 配置中心新增统一同步协调器，手工与定时同步共用顺序、逐域隔离失败并防止重叠执行。
- 开发模式无需 Redis、Kafka、Elasticsearch 即可完成核心功能和 HTTP 集成验证。

## 新增功能

- 数据补全接口、IP/设备补全处理器、组合执行与失败隔离。
- `FeatureQueryResult` 降级结果，包含失败类型、超时类型和特征阶段耗时。
- `ConfigSyncContributor`、`ConfigSyncCoordinator` 和 `ConfigSyncReport`，支持全量及选择性同步。
- 覆盖缓存、规则函数、特征、策略、风险、处置、日志、配置中心和公开 API 的自动化测试套件。
- 控制台配置管理 API 契约测试及调试台运行时操作测试。

## 行为与架构调整

- HTTP 决策结果的 `detail` 增加 `dataPipelineDegraded`、`featureCostMs`、
  `failedComplementHandlers`、`failedFeatureTypes` 和 `timedOutFeatureTypes` 诊断字段。
- 全量配置同步默认顺序调整并固定为：
  `scene → feature → rule → strategy → disposer → risk → black-white`。
- 配置同步接口的 `data` 从简单文本结果升级为结构化 `ConfigSyncReport`；调用方应读取
  `success`、`skipped`、`costMs` 和 `steps`，不要依赖旧文本。
- Redis 兼容缓存同步前清理旧场景路由、规则集合和策略数据，避免重复发布残留。

## 缺陷修复

- 补齐内存缓存的 Hash、Set、List、TTL、原子锁和通配 key 语义。
- 修复 LOCK/BAN 处置写入与状态查询 key 不一致、用户 ID 来源不统一的问题。
- 修复统计特征处理器被计算特征处理器抢占，以及标准差计算恒为通过的问题。
- 修复规则函数在空值、基本类型数组、字段存在和长度表达式场景下的错误。
- 修复坏规则 ID 导致整批规则评估中断的问题。
- 修复在线策略未同步、重同步残留旧规则和路由的问题。
- 修复处置/风险日志无法查询、风险类型路由不稳定和账号安全入口缺失的问题。
- 修复健康接口项目名/版本元数据错误及控制台锁文件无法从旧镜像复现安装的问题。

## 自动化验证结果

| 检查项 | 结果 | 明细 |
| --- | --- | --- |
| 后端 Maven 全量测试 | 通过 | 96 项，0 failure，0 error，0 skipped |
| 后端发布打包 | 通过 | 7 个 reactor 模块成功，Web 可执行 JAR 生成成功 |
| 控制台单元测试 | 通过 | 7 项 |
| 控制台 ESLint | 通过 | 0 error |
| 控制台生产构建 | 通过 | 构建成功，保留包体积告警 |
| 开源内容检查 | 通过 | 未发现构建产物、敏感路径或受限内容 |

后端测试分布：`common 21`、`api 4`、`core 36`、`config 4`、`service 16`、`web 15`。

本轮新增 4 个发布风险回归场景：

1. 特征线程池拒绝任务时返回降级结果，不向主决策链路抛出异常。
2. 配置同步重叠时第二轮明确返回 `skipped=true`，贡献者只执行一次。
3. HTTP 决策遇到未知特征类型时保持原策略命中，并返回失败类型诊断。
4. 全量及风险同步接口返回稳定、有序的逐配置域报告。

## 验证环境

- Windows 11 amd64
- Oracle JDK 23.0.1，Java 编译目标 17
- Apache Maven 3.9.6
- Node.js 24.14.0、npm 11.9.0
- Spring `dev` profile、H2 内存数据库、内存缓存与日志降级

验证命令：

```powershell
mvn -f server/pom.xml test

Set-Location apps/console
$env:NODE_OPTIONS = '--openssl-legacy-provider'
npm run test:unit
npm run lint
npm run build

Set-Location ../..
python tools/check_open_source.py
```

## 配置与兼容提示

| 参数 | 默认值 | 发布建议 |
| --- | ---: | --- |
| `soda.engine.feature-workers` | 8 | 按外部服务并发上限和实例 CPU 校准 |
| `soda.engine.feature-queue-capacity` | 256 | 监控队列拒绝率，避免无限排队 |
| `soda.engine.feature-timeout-ms` | 200 | 按供应商 P95/P99 延迟设定总预算 |
| `soda.engine.config-refresh-ms` | 30000 | 结合配置生效时效与数据库压力调整 |

Node.js 18 及以上验证当前 Webpack 4 构建时需要设置
`NODE_OPTIONS=--openssl-legacy-provider`。该设置仅用于构建工具兼容，不影响运行时 API。

## 已知限制与发布风险

- 百度、数美、腾讯等适配器本轮只验证本地确定性降级契约，未验证真实供应商凭据与网络连通性。
- 生产 MySQL、Redis、Kafka 环境的并发、TTL、消息投递、故障恢复和多节点一致性仍需专项验证。
- Vue 2 既有依赖树存在已知安全问题，公网发布前需完成依赖安全评估和升级计划。
- 控制台入口约 2.71 MiB，生产构建有资源体积告警；建议后续分包和按需加载。
- 外部特征服务容量未知，生产参数需通过压测确定，并为超时、失败和队列拒绝配置告警。

## Tag 发布清单

- [ ] 将 Maven `2.0.0-SNAPSHOT` 更新为正式版本，确认控制台版本策略。
- [ ] 在 MySQL、Redis、Kafka 预发布环境执行全量回归和故障恢复测试。
- [ ] 使用真实供应商沙箱凭据执行外部适配器契约测试。
- [ ] 复核 Vue 2 依赖安全风险并记录接受人、范围和到期时间。
- [ ] 确认数据库变更、配置默认值和结构化同步响应已通知调用方。
- [ ] 执行本文验证命令，确认工作树不包含 `target`、`dist`、`node_modules` 等生成目录。
- [ ] 更新 `CHANGELOG.md` 正式发布日期并创建带注释 tag：`v2.0.0`。
- [ ] 发布后检查健康接口版本、运行时快照版本、配置同步报告和核心决策样例。

更完整的测试数据、修复编号和生产边界见
[下一 Tag 功能验证与修复记录](verification-and-fixes-next-tag.md)。
