# 开源发布检查

Soda 对外发布前应完成以下检查。

## 代码与数据

- [ ] 仓库只包含 Soda 源码、文档和人工构造的示例数据。
- [ ] 不包含生产 SQL、数据库 ZIP、运行日志、构建产物或 IDE 文件。
- [ ] 不包含原单位名称、内部系统名、内部域名、私网地址和员工账号。
- [ ] 不包含 Access Key、Token、证书、私钥或非空生产密码。
- [ ] Java 根包统一为 `com.soda.risk.engine`。
- [ ] Maven 构件、应用名和前端标题统一使用 Soda。

## 许可证与来源

- [ ] 已确认所有自主代码、UI 修改、数据库结构和文档具备公开授权。
- [ ] 根目录保留 MIT License。
- [ ] `apps/console/LICENSE` 中的上游 iView Admin MIT 声明继续保留。
- [ ] 新增依赖已检查许可证兼容性。

## 质量

- [ ] `mvn -f server/pom.xml clean test` 通过。
- [ ] `npm run test:unit` 通过。
- [ ] `npm run build` 通过。
- [ ] Docker Compose 可在干净环境中启动。
- [ ] 引擎调试台能够通过前端代理完成一次真实规则决策。
- [ ] MySQL 表结构和可选示例数据可在空库中导入。

## 安全边界

- [ ] 生产 profile 不注册演示认证控制器。
- [ ] 配置刷新接口受到管理权限保护。
- [ ] 对外 API 使用 HTTPS，并具备认证、限流、超时和审计。
- [ ] Redis、Kafka、数据库和日志系统凭据只通过环境变量或密钥系统注入。
- [ ] 详细决策结果不会记录不必要的敏感请求字段。

## Git 发布

- [ ] 使用干净的新 Git 历史，不携带私有仓库远程地址或旧作者邮箱。
- [ ] 默认分支启用 CI、代码审查和 secret scanning。
- [ ] 发布版本包含 CHANGELOG、tag 和可复现的构建产物。

可运行 `python tools/check_open_source.py` 完成基础静态检查。自动扫描不能替代
人工代码审查和法律授权确认。
