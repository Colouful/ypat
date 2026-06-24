# YPAT 前端迁移进度

## 当前阶段
阶段 3：核心业务开发

## 已完成内容

### 阶段 1：仓库审计 ✅
- [x] repository-audit.md - 仓库审计报告
- [x] feature-matrix.md - 功能迁移矩阵
- [x] api-contract.md - 接口契约文档
- [x] backend-gap-analysis.md - 后端缺口分析
- [x] design-system.md - 设计系统文档

### 阶段 2：基础架构 ✅
- [x] 工程配置 (package.json, tsconfig, vite, eslint, prettier)
- [x] 目录结构 (api/, stores/, composables/, components/, pages/, styles/, etc.)
- [x] 网络请求层 (api/request.ts - Token刷新、错误处理、拦截器)
- [x] API 模块 (user, ypat, payment, oauth, content, message)
- [x] API 适配器 (图片URL、分页、时间格式、手机号脱敏)
- [x] 登录态 (stores/user.ts - 登录、退出、Token刷新、持久化)
- [x] 状态管理 (app, user, ypat, message stores)
- [x] 路由 (pages.json - 主包+3个分包)
- [x] 环境配置 (config/env.ts, .env.example)
- [x] Design Token (styles/tokens.scss)
- [x] 全局样式 (reset, mixins, global)
- [x] 常量枚举 (enums.ts, pages.ts)
- [x] 工具函数 (format, validate, platform)
- [x] Composables (useAuth, useNavigation, usePagination, useForm)

### 阶段 3：核心业务 (进行中)
- [x] 登录页 (微信授权、H5手机号、用户协议)
- [x] 首页 (Banner、推荐/最新/同城Tab、约拍卡片列表、下拉刷新、骨架屏)
- [x] 消息页 (收到/发出Tab、未读角标、消息列表)
- [x] 发布约拍 (表单、图片上传、风格选择、扣费提示)
- [x] 我的页 (个人信息、统计、菜单导航)
- [x] 约拍详情 (图片轮播、发布者信息、收藏、申请)
- [x] 搜索约拍
- [x] 我的发布/申请/收藏列表
- [x] 编辑资料
- [x] 完善资料引导
- [x] 实名认证 (OCR、提交、状态)
- [x] 钱包/充值/收支记录/账单
- [x] 设置/关于/反馈
- [x] 用户协议/隐私政策
- [x] 文章详情/消息详情

## 正在处理
- 构建验证和类型检查修复

## 未完成内容
- [ ] 单元测试编写
- [ ] 微信小程序构建验证
- [ ] H5 构建验证
- [ ] 性能优化报告
- [ ] 安全审查报告
- [ ] 发布检查清单

## 已确认业务规则
1. 发布约拍消耗 3 拍拍豆
2. 申请约拍消耗 1 拍拍豆
3. 查看联系方式消耗 1 拍拍豆
4. 约拍发布后需管理员审核
5. 实名认证需管理员审核
6. Token 有效期 5 天
7. 用户角色: 摄影师/模特/妆造师/修图师/个人/演员/商家/其他
8. 约拍状态: 暂存→已提交→审核通过/不通过
9. 收费方式: 互免/收费/可付费/协商
10. 图片上传自动加水印 (后端处理)

## 待确认业务规则
1. 收藏接口是否为 toggle (添加/取消)
2. 微信支付回调后前端如何确认支付结果 (轮询?)
3. 图片上传是通过后端还是直传 OSS
4. H5 环境下的登录方式 (目前设计为手机号)

## 已发现问题
1. 后端 WXConfig 硬编码了敏感密钥
2. JWT Secret 硬编码在 Const.java
3. Spring Boot 1.5.9 过时，有安全漏洞
4. 无请求频率限制
5. 分页无上限限制

## 已修改文件
- frontend/package.json
- frontend/tsconfig.json
- frontend/vite.config.ts
- frontend/src/main.ts
- frontend/src/App.vue
- frontend/src/pages.json
- frontend/src/manifest.json
- frontend/src/config/env.ts
- frontend/src/styles/ (tokens, mixins, reset, global)
- frontend/src/constants/ (enums, pages)
- frontend/src/utils/ (format, validate, platform)
- frontend/src/api/ (request, types, modules, adapters)
- frontend/src/stores/ (index, user, app, ypat, message)
- frontend/src/composables/ (useAuth, useNavigation, usePagination, useForm)
- frontend/src/pages/home/index.vue
- frontend/src/pages/login/index.vue
- frontend/src/pages/mine/index.vue
- frontend/src/pages/message/index.vue
- frontend/src/pages/publish/index.vue
- frontend/src/pages-sub/ (all sub-pages)
- frontend/.eslintrc.js
- frontend/.prettierrc
- frontend/.env.example
- docs/migration/ (all docs)

## 测试结果
- 待执行

## 构建结果
- 待执行

## 下一步任务
1. 安装依赖 (pnpm install)
2. 修复 TypeScript 类型错误
3. 运行构建检查
4. 编写核心单元测试
5. 生成发布检查清单

## 当前阻塞项
- 缺少真实微信 AppID (不阻塞开发，阻塞真机调试)
- 缺少微信支付商户配置 (不阻塞开发，阻塞支付联调)
- 缺少百度 OCR 密钥 (不阻塞开发，阻塞 OCR 联调)
- 后端服务未部署 (可使用 Mock)
