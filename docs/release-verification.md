# 下一个版本验证与修复记录

本文档用于形成下一个 tag 的发布证据。只有 CI 产生的结果才能填写为“通过”；本地未执行或依赖外部环境的检查必须保持为“待验证”。

## 本轮修复

| 范围 | 修复内容 | 回归数据 |
| --- | --- | --- |
| 扩展计算 | 标准差判断从固定 `true` 改为读取 `feature:stats:{featureId}` 的 `mean`、`stddev`；缺失或非法统计数据按失败关闭处理 | 均值 100、标准差 10，覆盖范围内、范围外、缺失及非法数据 |
| Kafka 策略消费 | 处理异常不再确认 offset，由 Kafka 错误处理机制接管重试 | 正常未命中消息、引擎异常 |
| Kafka 处置消费 | 实现 `StrategyHitResult` JSON 反序列化及处置调用，成功后才确认 | 命中消息、非法 JSON |
| Kafka 风险决策消费 | 处理异常不再确认 offset | 引擎异常 |
| 配置管理 | 增加业务方键和名称唯一性单元测试 | 重复键、排除当前记录 |
| 认证 | 增加空授权码、Redis 映射、直接 openId、未知键及兼容场景键测试 | 固定模拟 Redis 数据 |
| 控制台 | E2E 冒烟测试由 Vue 模板文案改为真实引擎调试台路由 | `/#/operations/playground` |

## 发布验证矩阵

| 检查 | 命令 | 当前状态 | 发布要求 |
| --- | --- | --- | --- |
| 后端全模块测试 | `cd server && mvn test` | 待验证 | 全部通过 |
| 后端打包 | `cd server && mvn package` | 待验证 | 全部通过 |
| 控制台单元测试 | `cd apps/console && npm run test:unit` | 待验证 | 全部通过 |
| 控制台覆盖率 | `cd apps/console && npm run test:unit:coverage` | 待验证 | 生成报告且无失败 |
| 控制台静态检查 | `cd apps/console && npm run lint` | 待验证 | 无 error |
| 控制台生产构建 | `cd apps/console && npm run build` | 待验证 | 构建成功 |
| 控制台 E2E | `cd apps/console && npm run test:e2e` | 待验证 | 在已启动的测试环境通过 |

## 发布前人工确认

- 使用隔离的 MySQL、Redis 和 Kafka 测试环境，不复用生产凭据或数据。
- 验证 Kafka 重试次数及死信主题符合部署环境策略，并确认重复投递不会产生重复处置。
- 第三方风险适配器在未接入真实上游时会返回 `NOT_CONFIGURED`，不得将该状态当作低风险结果。
- 将 CI 运行链接、测试数量、覆盖率和遗留问题补充到本文件后再创建 tag。
