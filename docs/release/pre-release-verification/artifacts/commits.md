# 提交记录

| Commit | 内容 | 文件 | 测试 |
| ------ | -- | -- | -- |
# 预发验证提交记录

| Commit | 内容 | 文件 | 测试 |
| ------ | -- | -- | -- |
| `00babc7` | 预发验证、CI、生产 preflight、密钥外置和证据文档 | `.github/`、`scripts/release/`、`backend/`、`backend-base/`、`frontend/`、`docs/release/pre-release-verification/` | 前端 57 tests、后端 20 tests、migration、API 冒烟 |
| `1ce4031` | 修正 CI 安全扫描中过宽的 `.env` 规则 | `.github/workflows/ci.yml`、预发证据文档 | `git diff --check`、preflight 语法检查、本地 env 规则检查 |
