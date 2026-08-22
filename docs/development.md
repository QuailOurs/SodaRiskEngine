# 开发指南

## 环境

- JDK 17+
- Maven 3.9+
- Node.js 16+
- npm

## 后端开发

```bash
mvn -f server/pom.xml clean test
mvn -f server/pom.xml -pl web -am spring-boot:run
```

后端采用 Maven 多模块结构。依赖方向必须保持：

```text
common ← api ← core ← service ← web
             ↖ config ↗
```

- 纯领域计算放在 `core`。
- 数据库实体和 Mapper 放在 `config`。
- 跨模块编排与外部系统适配放在 `service`。
- HTTP Controller、异常转换和 OpenAPI 放在 `web`。
- 公共 DTO 进入 `api`，不要让 Controller 直接暴露数据库实体。

默认 `dev` profile 使用 H2 内存数据库。MySQL、Redis、Kafka 和外部服务必须通过
独立配置显式启用。

## 控制台开发

```bash
cd apps/console
npm ci --legacy-peer-deps
npm run dev
```

Webpack 4 在 Node.js 17+ 可能需要兼容参数：

```powershell
$env:NODE_OPTIONS='--openssl-legacy-provider'
npm run dev
```

控制台端口为 `8888`，开发代理将 `/api` 转发到 `9999`。本地验证入口为
<http://localhost:8888/#/operations/playground>。

## 注释与命名

- 产品名统一写作 `Soda`。
- Java 根包统一为 `com.soda.risk.engine`。
- 注释解释职责、边界、非显然决策和扩展约束。
- 不在注释中记录私有项目、原单位、内部框架或迁移历史。
- 避免 `Controller - 控制器` 一类重复注释。
- TODO 必须说明缺少的适配器或验收条件。

## 示例数据

示例数据必须由贡献者人工构造。不要从生产导出中挑选、脱敏或变形后提交。
具体约束见 [sample-data.md](sample-data.md)。

## 测试

后端：

```bash
mvn -f server/pom.xml clean test
```

控制台：

```bash
cd apps/console
npm run test:unit
npm run build
```

涉及决策链路时，至少验证：

1. 配置快照能够加载；
2. 命中、未命中和业务方不匹配请求；
3. 批量请求；
4. 配置重载版本递增；
5. 前端代理能够访问 `/api/v1/engine/evaluate`。

## 提交前检查

```bash
python tools/check_open_source.py
mvn -f server/pom.xml clean test

cd apps/console
npm run test:unit
npm run build
```

全量前端 lint 仍有来自旧版 UI 代码的技术债。修改文件不得新增 lint 错误，并应在
逐步升级控制台时收敛现有问题。
