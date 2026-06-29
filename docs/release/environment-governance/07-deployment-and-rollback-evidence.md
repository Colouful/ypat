Deployment & Rollback Evidence
==============================

日期: 2026-06-29

1. 本地脚本语法验证
   - 13 个 deploy/*.sh 脚本全部通过 bash -n

2. 本地 compose config 验证
   - development compose config 通过
   - staging compose config 通过 (注入完整变量)
   - production compose config 通过 (注入完整变量)
   - fastdfs staging compose config 通过
   - fastdfs production compose config 通过

3. fail-closed 验证
   - production compose 不注入 YPAT_MYSQL_URL → 启动失败 (exit 1)
   - 校验脚本: production 引用 panghu.work → fail
   - preflight.sh: .env 缺必填变量 → exit 1
   - preflight.sh: :latest 镜像 → exit 1

4. 服务器部署
   - 本次任务未在服务器执行部署
   - 部署命令预览已就绪，详见 scripts/deploy/

5. 回滚验证
   - rollback-staging.sh --list 可列出 release
   - rollback-staging.sh 回滚到 previous 链接
   - rollback-production.sh 必须 --confirm-rollback

后续建议：
- 在 staging 服务器实际跑一次 deploy-staging.sh --skip-build 验证
- 验证 deploy-staging.sh 失败时的自动回滚
- 验证 rollback-staging.sh --to <sha>