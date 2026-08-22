# 参与贡献

感谢你帮助改进 Soda。

## 开始之前

1. 先搜索现有 Issue，确认问题尚未被讨论或解决。
2. 大型功能、公共 API 或数据模型变更请先提交设计 Issue。
3. 确保提交内容具备公开授权，不包含雇主或客户的专有代码和数据。

## 本地开发

```bash
mvn -f server/pom.xml clean test

cd apps/console
npm ci --legacy-peer-deps
npm run test:unit
npm run build
```

详细环境说明见 [docs/development.md](docs/development.md)。

## 代码约定

- Java 包名必须位于 `com.soda.risk.engine` 下。
- Maven 新模块使用 `soda-*` 构件名。
- 领域逻辑放在 `core`，基础设施适配放在 `service` 或 `config`。
- HTTP DTO 不直接复用数据库实体。
- 新规则函数、特征处理器和外部适配器必须包含测试。
- 注释说明当前职责、约束和原因，不记录私有项目迁移历史。
- 示例数据必须完全人工构造。

## Pull Request

一个 Pull Request 应聚焦一个主题，并包含：

- 变更动机和行为说明；
- 兼容性与安全影响；
- 自动化测试或无法测试的原因；
- 涉及 UI 时提供截图；
- 涉及 API 或数据库时同步更新文档和示例。

推荐使用 `feat:`、`fix:`、`docs:`、`refactor:`、`test:`、`build:` 等提交前缀，
但更重要的是让提交信息能够说明原因。

## 数据库变更

同时更新：

- `server/config/src/main/resources` 中的 H2 兼容脚本；
- `database/mysql` 中的 MySQL 交付脚本；
- `database/README.md` 和相关测试。

## 报告安全问题

请不要在公开 Issue 中披露未修复漏洞，具体流程见 [SECURITY.md](SECURITY.md)。
