# Open Items

**日期**: 2026-06-29

## 1. 代码问题（应在 PR 关闭前解决）

无。

## 2. 环境问题（需要后续基础设施）

- ❌ production 域名未购买（必须独立域名，不能用 panghu.work）
- ❌ production 服务器未准备
- ❌ production MySQL / Redis / FastDFS 未部署
- ❌ production 真实密钥未生成（微信 AppID、百度 AK/SK、SSO JWT 密钥等）
- ❌ production SSL 证书未签发
- ❌ production 监控告警未集成

## 3. 运维问题

- ❌ 数据库 migration 工具未实施（建议 Flyway 或手工 SHA256 + schema_migration 表）
- ❌ 数据库备份策略未自动化（当前依赖 `mysqldump` 手工执行）
- ❌ Redis 备份策略未实施（RDB snapshot + 异地）
- ❌ FastDFS 数据备份未实施（参考 `migrate-fastdfs-data.sh` 增强）
- ❌ 告警未配置（P0/P1 服务异常时无人通知）

## 4. 平台问题

- ⚠️ GitHub Branch Protection 必须由 owner 手动配置（AI 不能自动 apply）
  - 详见 `docs/release/GITHUB_BRANCH_PROTECTION.md`
- ⚠️ GitHub Rulesets 必须由 owner 手动配置
- ⚠️ GitHub Secrets（用于 CI）必须由 owner 手动配置

## 5. 人工配置问题

| 项目 | 负责人 | 截止日期 | 状态 |
|------|--------|----------|------|
| GitHub Branch Protection 配置 | @lizhenwei | — | 待办 |
| GitHub Rulesets 配置 | @lizhenwei | — | 待办 |
| production 域名申请 | — | — | 待办 |
| production SSL 证书 | — | — | 待办 |
| production 数据库迁移计划 | — | — | 待办 |

## 6. PR Reviewer 关注点

请重点 review：
1. `docker-compose.production.yml` 的 fail-closed `${VAR:?msg}` 是否完整
2. `backend/system-wap/src/main/java/com/ypat/config/EnvironmentConfigurationValidator.java` 的校验逻辑
3. `scripts/deploy/preflight-production.sh` 是否真的能阻止误部署
4. `.github/workflows/ci.yml` 9 个 job 是否完整覆盖三环境