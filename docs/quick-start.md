# 快速开始

## 1. 准备环境

- JDK 17 或更高版本
- Maven 3.9 或更高版本
- Node.js 16 或更高版本
- npm

验证命令：

```bash
java -version
mvn -version
node -v
npm -v
```

## 2. 启动后端

在仓库根目录执行：

```bash
mvn -f server/pom.xml clean test
mvn -f server/pom.xml -pl web -am spring-boot:run
```

默认启用 `dev` profile：

- HTTP 端口：`9999`
- 数据库：H2 内存库 `jdbc:h2:mem:soda`
- 配置：启动时自动加载人工构造的演示数据
- Redis、Kafka、Elasticsearch：不要求启动

验证配置是否加载完成：

```bash
curl http://localhost:9999/api/v1/engine/config/status
```

当 `sceneCount`、`strategyCount` 和 `ruleCount` 大于 0 时，运行时已可处理请求。

## 3. 启动控制台

新开一个终端：

```bash
cd apps/console
npm ci --legacy-peer-deps
npm run dev
```

访问 <http://localhost:8888>。开发模式默认加载本地菜单并进入管理台；演示认证接口
只存在于后端 `dev` profile。

Webpack 4 在 Node.js 17+ 可能需要 OpenSSL 兼容参数：

```powershell
$env:NODE_OPTIONS='--openssl-legacy-provider'
npm run dev
```

Linux 或 macOS：

```bash
NODE_OPTIONS=--openssl-legacy-provider npm run dev
```

## 4. 验证规则决策

打开 <http://localhost:8888/#/operations/playground>，选择：

- 业务方：`demo_business`
- 场景：`login_protection`
- 请求字段：`"blacklisted": true`

执行后应返回 `HIT`，并展示命中策略、规则详情、得分、配置版本和 trace ID。

也可以直接执行 [strategy.http](../examples/requests/strategy.http) 中的请求。

## 5. Docker Compose

已安装 Docker 时，可在仓库根目录执行：

```bash
docker compose -f deploy/docker-compose.yml up --build
```

容器地址：

- 控制台：<http://localhost:8888>
- 后端：<http://localhost:9999>

停止服务：

```bash
docker compose -f deploy/docker-compose.yml down
```

## 常见问题

### `/actuator/health` 返回 DOWN

本地未启用某些可选基础设施时，聚合健康检查可能报告对应组件不可用。请优先使用
`/api/v1/engine/config/status` 和一次真实决策请求判断规则引擎功能是否正常。

### 控制台启动时报 OpenSSL 错误

设置 `NODE_OPTIONS=--openssl-legacy-provider`。这是 Vue CLI 3 / Webpack 4 与新版
Node.js 的兼容问题。

### 重启后修改的数据消失

`dev` profile 使用内存数据库。需要持久化时请使用 `prod` profile 并参考
[数据库说明](../database/README.md) 初始化 MySQL。
