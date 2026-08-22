# Changelog

本文档记录 Soda 的重要变更。版本格式遵循 Semantic Versioning。

## [Unreleased]

### Added

- Soda 项目说明、技术栈、架构、数据库、贡献和安全文档。
- 完全人工构造的配置目录示例数据。
- MySQL 8.0 初始化和可选示例数据脚本。
- 后端与控制台的持续集成工作流。
- Soda 项目 Logo、产品图标、横向组合、favicon、PWA 与应用图标体系。
- 可重复生成品牌图片尺寸并同步控制台资源的 PowerShell 脚本。

### Changed

- Java 根包统一为 `com.soda.risk.engine`。
- Maven 构件统一命名为 `soda-*`。
- Spring 应用名、配置前缀、缓存命名空间和控制台品牌统一为 Soda。
- 代码注释改为描述当前职责和扩展点。

### Removed

- 面向私有数据导出的导入流程和历史业务示例数据。
- 代码和文档中的私有框架迁移描述。

## [0.1.0] - TBD

首个计划公开发布版本。
