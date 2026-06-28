# CI 与构建证据

更新时间：2026-06-28 15:10 +0800

## 本地验证

| 命令 | 目录 | 退出码 | 结果 |
| --- | --- | ---: | --- |
| `node --version` | `frontend` | 0 | `v22.18.0` |
| `pnpm --version` | `frontend` | 0 | `11.7.0` |
| `pnpm install --frozen-lockfile --registry=https://registry.npmjs.org` | `frontend` | 0 | 依赖安装成功。 |
| `pnpm run type-check` | `frontend` | 0 | 通过。 |
| `pnpm run lint` | `frontend` | 0 | 通过。 |
| `pnpm run test` | `frontend` | 0 | 8 个测试文件，57 个测试通过。 |
| `pnpm run build:h5` | `frontend` | 0 | 通过。 |
| `pnpm run build:mp-weixin` | `frontend` | 0 | 通过。 |
| `pnpm run check` | `frontend` | 0 | 综合检查通过，包含类型、lint、测试和构建。 |
| `mvn test` | `backend` | 0 | 20 个后端测试通过。 |
| `mvn -pl system-wap -am package -DskipTests` | `backend` | 0 | WAP jar 打包通过。 |
| `bash -n scripts/release/preflight-check.sh` | 根目录 | 0 | 语法检查通过。 |
| `scripts/release/preflight-check.sh --skip-build` | 根目录 | 1 | 按预期失败，原因是 `.env.production` 仍为 HTTP IP。 |
| `scripts/release/api-security-smoke.sh http://localhost:18081` | 根目录 | 0 | 当前 WAP 匿名/私有接口冒烟通过。 |

完整记录：`docs/release/pre-release-verification/artifacts/test-results.txt`。

## GitHub Actions

新增 `.github/workflows/ci.yml`：

- Frontend validation(前端验证)：Node 22、pnpm 11.7、install、type-check、lint、test、build:h5、build:mp-weixin。
- Backend validation(后端验证)：Temurin JDK 17、Maven 缓存、`mvn test`。
- Security scan(安全扫描)：preflight 脚本语法、禁止提交真实 `.env`、禁止 keystore/私钥、扫描历史密钥。

删除旧的 `.github/workflows/frontend-ci.yml`，避免只跑前端而漏掉后端和安全扫描。

## 构建地址扫描

扫描命令：

```bash
grep -R "82.156.14.216" frontend/dist || true
grep -R "http://" frontend/dist || true
grep -R "localhost" frontend/dist || true
```

结果：构建产物包含 `82.156.14.216` 和 `http://`，来源是本轮按要求未修改的 `frontend/.env.production`。

状态：`OPS_BLOCKED`。该构建不能用于正式生产发布。必须先配置正式 HTTPS API 域名和 HTTPS 图片域名，再重新构建并扫描为零。
