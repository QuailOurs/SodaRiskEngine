# 技术栈

本文记录 Soda 当前实际使用的技术组件。版本以构建文件为准，不以文档中的表格
作为依赖锁定来源。

## 后端

| 组件 | 当前版本 | 用途 | 是否为运行必需 |
| --- | ---: | --- | --- |
| Java | 17 | 后端语言与运行时基线 | 是 |
| Spring Boot | 3.4.5 | 应用容器、HTTP、配置与健康检查 | 是 |
| Spring MVC | 由 Boot 管理 | REST API | 是 |
| MyBatis-Plus | 3.5.7 | 配置数据访问 | 是 |
| H2 | 由 Boot 管理 | 开发环境内存数据库 | 仅开发环境 |
| MySQL Connector/J | 8.0.33 | 生产配置数据库连接 | 生产环境 |
| Druid | 1.2.21 | 数据库连接池与监控 | 是 |
| Aviator | 5.4.3 | 规则表达式编译与执行 | 是 |
| Caffeine | 3.1.8 | 本地缓存 | 是 |
| Jedis | 5.1.0 | Redis 客户端 | 可选 |
| Spring Kafka | 3.1.4 | 决策消息消费和日志输出 | 可选 |
| Elasticsearch Java Client | 8.13.0 | 决策日志检索 | 可选 |
| Resilience4j | 2.3.0 | 外部调用容错 | 可选 |
| Micrometer / Prometheus | 1.12.5 | 指标采集与导出 | 可选 |
| SpringDoc OpenAPI | 2.5.0 | OpenAPI 与 Swagger UI | 是 |
| JUnit 5 / Mockito | 5.10.2 / Boot 管理 | 单元和集成测试 | 仅构建阶段 |

## 管理控制台

| 组件 | 当前版本 | 用途 | 备注 |
| --- | ---: | --- | --- |
| Vue | 2.6.11 | UI 框架 | 后续计划升级到 Vue 3 |
| Vue Router | 3.1.6 | 前端路由 | Hash 模式 |
| Vuex | 3.1.3 | 状态管理 | 管理用户和页面状态 |
| View Design / iView | 4.4.0 / 3.5.4 | UI 组件 | 保留上游 MIT 许可 |
| Axios | 0.18.0 | HTTP 客户端 | 通过 `/api` 代理后端 |
| CodeMirror | 5.52.2 | 表达式编辑 | 规则编辑器 |
| ECharts | 4.7.0 | 图表 | 运行概览 |
| Vue CLI / Webpack | 3.12.1 / 4 | 构建工具 | Node 17+ 需要 OpenSSL 兼容参数 |
| Mocha / Chai | CLI 插件 / 4.1.2 | 前端单元测试 | 通过 `npm run test:unit` 执行 |

## 基础设施

| 组件 | 用途 | 默认开发环境 |
| --- | --- | --- |
| Docker Compose | 一键启动后端与控制台 | 可选 |
| Nginx | 托管控制台并代理 `/api` | 仅容器部署 |
| Redis | 分布式配置缓存和部分业务数据 | 未启用，自动回退本地缓存 |
| Kafka | 异步消息和日志 | 未启用 |
| Elasticsearch | 决策日志检索 | 未启用 |

## 版本策略

- 后端以 JDK 17 为最低兼容版本。
- Maven 依赖集中在 `server/pom.xml` 管理。
- 前端通过 `package-lock.json` 固定依赖树。
- 新依赖必须说明用途、许可证、体积和可替代方案。
- 可选基础设施不能成为开发环境运行的隐式前提。
