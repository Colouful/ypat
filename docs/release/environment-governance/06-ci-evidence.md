CI Evidence
=========

GitHub Actions CI 工作流: .github/workflows/ci.yml

包含以下 job（全部作为 required checks）：

1. frontend / development
   - pnpm install
   - pnpm run env:validate:development
   - pnpm run type-check
   - pnpm run lint
   - pnpm run test
   - pnpm run build:h5

2. frontend / staging
   - pnpm run env:validate:staging
   - pnpm run build:h5:staging
   - pnpm run build:mp-weixin:staging

3. frontend / production
   - 注入 CI 测试变量（CI_PRODUCTION_API_URL=https://api.production-ci.example.invalid）
   - pnpm run env:validate:production
   - pnpm run build:h5:production
   - pnpm run build:mp-weixin:production
   - 扫描产物是否含 staging 域名/IP

4. backend / unit-tests
   - mvn test -Dmaven.test.skip=false -DskipTests=false

5. backend / build-dev
   - mvn clean package -Pdev -DskipTests
   - 验证 dev jar 不含 pre/pro 资源

6. backend / build-pre
   - mvn clean package -Ppre -DskipTests
   - 验证 pre jar 不含 dev/pro 资源

7. backend / build-pro
   - mvn clean package -Ppro -DskipTests
   - 验证 pro jar 不含 dev/pre 资源

8. compose / config
   - docker compose config (development)
   - docker compose -f docker-compose.staging.yml config
   - docker compose -f docker-compose.production.yml config
   - docker compose -f backend/dev/fastdfs/docker-compose.staging.yml config
   - 检查 :latest 镜像

9. shell / syntax-check
   - find scripts -name '*.sh' | xargs bash -n
   - shellcheck (可选)

10. security / secret-scan
    - 检查 .env 文件
    - 检查证书私钥
    - 检查已知密码 pattern
    - 检查 :latest 镜像
    - 检查 production compose 引用 staging

注意：CI 在 PR 阶段无法真实运行（GitHub Actions 需要 PR 触发），本文件记录 CI 配置，
实际 CI 验证需在 PR 创建后由 GitHub Actions 执行。