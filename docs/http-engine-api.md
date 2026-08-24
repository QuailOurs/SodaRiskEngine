# 规则引擎 HTTP 接入

规则引擎作为独立 Spring Boot 服务运行，默认地址为 `http://localhost:9999`。服务启动时从配置库构建不可变内存快照，决策请求只读取快照；默认每 30 秒刷新一次，刷新期间仍使用上一版本，不会读取到半成品配置。

## 单次决策

`POST /api/v1/engine/evaluate`

```json
{
  "requestId": "order-20260821-001",
  "businessKey": "demo_business",
  "sceneKey": "login_protection",
  "needDetail": true,
  "data": {
    "userId": "u-1001",
    "blacklisted": true,
    "login_count": 1,
    "device_risk_score": 0
  }
}
```

- `businessKey`：配置平台中的业务方标识。引擎会校验场景归属，但该字段不是访问密钥。
- `sceneKey`：配置平台中的场景标识。
- `data`：规则表达式使用的原始字段或调用方已计算好的特征。
- `needDetail`：为 `true` 时返回规则计算详情和输入参数快照；生产高流量调用可关闭。
- `requestId`：可选。不传时由引擎生成，响应中的 `traceId` 始终由引擎生成。

成功响应示例：

```json
{
  "code": 0,
  "msg": "成功",
  "data": {
    "requestId": "order-20260821-001",
    "traceId": "1dd9b1134f0c4df0a78b2423089de84e",
    "businessKey": "demo_business",
    "sceneKey": "login_protection",
    "status": "HIT",
    "hit": true,
    "score": 80,
    "preScore": 0,
    "returnCodes": ["VERIFY"],
    "strategies": [],
    "preStrategies": [],
    "detail": {
      "sceneName": "登录保护",
      "evaluatedStrategyCount": 1,
      "evaluatedRuleCount": 2,
      "dataPipelineDegraded": false,
      "featureCostMs": 1
    },
    "configVersion": 3,
    "costMs": 2
  },
  "timestamp": 1787310000000
}
```

`status` 有三种结果：

- `HIT`：命中至少一条上线策略（策略状态 `2`），`hit=true`。
- `PRE_HIT`：未命中上线策略，但命中至少一条预上线策略（策略状态 `1`），`hit=false`。
- `NOT_HIT`：上线和预上线策略均未命中，`hit=false`。

`score`、`preScore` 分别取已命中上线、预上线策略的最高分。`strategies` 与 `preStrategies` 分开返回，便于灰度策略观察而不影响正式决策。

`detail.dataPipelineDegraded` 表示数据补全或特征阶段是否发生降级，
`featureCostMs` 是本次全部特征作业的总耗时。降级时还会返回
`failedComplementHandlers`、`failedFeatureTypes` 或 `timedOutFeatureTypes`；它们只影响
对应外部数据源，其余特征和策略仍继续计算。特征线程池与一次决策的共享超时预算可用
`soda.engine.feature-workers`、`soda.engine.feature-queue-capacity` 和
`soda.engine.feature-timeout-ms` 调整。

## 批量决策

`POST /api/v1/engine/evaluate/batch`

请求体格式为 `{"requests":[...]}`，每一项与单次决策相同，一次最多 100 条。批量中任意一项业务校验失败时，整个请求返回该错误，不返回部分结果。

## 配置与健康状态

```http
GET  /api/v1/engine/health
GET  /api/v1/engine/config/status
POST /api/v1/engine/config/reload
```

状态接口返回当前快照版本、加载时间，以及场景、策略、规则和有效关联数量。手动刷新会先完整构建新快照，成功后一次性替换旧版本。定时刷新间隔可通过 Spring 属性 `soda.engine.config-refresh-ms` 调整。

## 业务错误码

HTTP 请求被正常接收但业务校验失败时，应以响应体的 `code` 为准：

| code | 含义 |
| ---: | --- |
| `0` | 成功 |
| `1001`–`1005` | 请求参数或数据错误 |
| `2003` | `businessKey` 与场景归属不匹配 |
| `3004` | 场景不存在或未启用 |
| `4001` | 规则引擎计算异常 |

## 上线前安全要求

`businessKey` 只用于配置隔离，不能当作认证凭证。生产部署必须在 API 网关或服务侧增加 API Key、签名或 OAuth2/mTLS 认证，并配置 HTTPS、限流、超时、审计日志和配置刷新接口的管理权限。`/config/reload` 不应直接暴露到公网。

可直接执行的本地请求位于 [`examples/requests/strategy.http`](../examples/requests/strategy.http)。
