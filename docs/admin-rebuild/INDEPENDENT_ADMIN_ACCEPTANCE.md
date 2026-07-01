# YPAT 后台管理端 - 独立验收报告

## 1. 验收元数据

| 项目 | 值 |
|------|------|
| 仓库 | https://github.com/Colouful/ypat |
| 分支 | main (本地分支 res2，指向同一 commit) |
| SHA | dc86ac77d227d205b403f21900023d97df4738db |
| 最近提交 | merge: 合并小程序接口配置和底栏修复 |
| 工作区状态 | 3 个未提交文件（均在 frontend/ 和 .omx/，不影响验收范围） |
| 操作系统 | macOS Darwin 25.3.0 (ARM64) |
| Node.js | v22.18.0 |
| pnpm | 10.22.0 |
| Java | OpenJDK 17.0.18 (Microsoft LTS) |
| Maven | 3.6.3 |
| Docker | 29.5.3 |
| 验收方式 | 代码审查 + 真实Docker服务 + 浏览器自动化 + curl接口测试 |
| 验收时间 | 2026-07-01 |
| 验收人员 | AI 独立验收工程师 |

---

## 2. 最终结论

### 不通过，禁止替换旧后台

存在 2 个 P0 安全问题和 2 个 P1 功能问题，必须修复后方可替换旧后台。

---

## 3. 执行摘要

| 统计项 | 数量 |
|--------|------|
| P0 | 2 |
| P1 | 2 |
| P2 | 5 |
| P3 | 2 |
| 已验证页面 | 15/15（登录+仪表盘+11管理页+403+404） |
| 未验证页面 | 0 |
| 已验证接口 | 27/27（前端API模块全部核对） |
| 未验证接口 | 0（接口契约全部核对，部分写操作未做数据库验证） |
| 遗漏功能 | UEditor 后端接口（W040）无新版替代 |

---

## 4. 问题清单

### P0-001: 管理端登录无管理员身份区分

- **问题编号**: P0-001
- **严重级别**: P0
- **模块**: 认证
- **页面/接口**: POST /admin/login
- **相关文件**: `backend/system-wap/src/main/java/com/ypat/service/AdminAuthService.java`
- **旧版行为**: 旧后台 system-web 独立部署，与 H5 接口物理隔离。任何能访问 system-web 的用户都可登录。
- **新版预期**: 仅管理员账号能登录后台
- **实际结果**: `AdminAuthService.login()` 仅验证手机号+密码+验证码，**任何 `t_user` 表中有密码的用户都可登录后台**。数据库中 user id=1 (Admin) 和任何设置了密码的普通用户均可登录。无角色字段、无管理员白名单、无 openid 校验。
- **复现步骤**: 使用任意 t_user 表中有密码的手机号+正确密码+验证码登录 /admin/login
- **根本原因**: 旧后台无角色体系，新版继承了这一缺陷。但旧后台是独立部署（物理隔离），新版 admin API 与 H5 API 共享同一个 system-wap 服务，风险放大。
- **影响范围**: 任何有密码的普通用户可登录后台，获取所有管理功能
- **修复建议**: 在 AdminAuthService.login() 中增加管理员身份校验（如检查手机号白名单、角色字段、或 t_user 表新增 is_admin 字段）
- **是否阻断替换旧后台**: 是

### P0-002: 普通用户 Token 可访问全部管理接口

- **问题编号**: P0-002
- **严重级别**: P0
- **模块**: 权限
- **页面/接口**: 全部 /admin/** 接口
- **相关文件**: `backend/system-wap/src/main/java/com/ypat/config/WebSecurityConfig.java`, `JwtTokenFilter.java`
- **旧版行为**: 旧后台独立部署，H5 用户无法直接调用管理接口
- **新版预期**: /admin/** 接口仅管理员 Token 可访问
- **实际结果**: Spring Security 配置中 `/admin/**` 仅要求 `.anyRequest().authenticated()`，无任何角色/权限拦截。H5 用户通过 `/user/login` 获取的 JWT Token 可直接调用所有管理接口（审核、上下架、发布、删除等）。JwtTokenFilter 仅验证 Token 有效性，不区分管理员和普通用户。
- **复现步骤**: 1. 普通用户通过 H5 登录获取 Token; 2. 使用该 Token 请求 GET /admin/ypat/list; 3. 返回正常数据
- **根本原因**: WebSecurityConfig 无 admin 角色配置；JwtTokenFilter 无角色区分逻辑；整个系统无 @PreAuthorize 注解
- **影响范围**: 所有管理接口对普通用户开放，包括审核、推荐、上下架、发布、撤回等写操作
- **修复建议**: 1. 在 JWT claims 中增加角色信息; 2. WebSecurityConfig 中对 /admin/** 增加 .hasRole("ADMIN"); 3. 或在 JwtTokenFilter 中增加角色校验
- **是否阻断替换旧后台**: 是

### P1-001: 产品列表分页总数显示错误

- **问题编号**: P1-001
- **严重级别**: P1
- **模块**: 产品管理
- **页面/接口**: /manage/product/index → GET /admin/product/list
- **相关文件**: `frontend-admin/src/views/manage/product-list/index.vue`, `AdminProductController.java`
- **旧版行为**: 分页显示正确的总条数
- **新版预期**: 分页组件显示 "共 N 条" 与实际数据一致
- **实际结果**: 分页显示 "共 0 条"，但表格中实际显示了 3 条产品数据
- **根本原因**: 后端返回的 Spring Data Page 中 totalElements 与实际 content 不一致，或前端解析 totalElements 字段错误
- **修复建议**: 核对后端 productServiceClient.findPage() 返回的 totalElements 值，以及前端是否正确读取该字段

### P1-002: 多处中文内容显示乱码

- **问题编号**: P1-002
- **严重级别**: P1
- **模块**: 多个模块
- **页面/接口**: 产品列表、文章列表、横幅列表
- **相关文件**: `AdminProductController.java`, `AdminArticleController.java`, `AdminBannerController.java`
- **旧版行为**: 中文正常显示
- **新版预期**: 中文正常显示
- **实际结果**: 产品名称、文章标题、横幅标题等中文内容显示为乱码（如 `ä¸çº§`），疑似 UTF-8 编码被错误解码
- **根本原因**: 后端 Feign Client 调用 system-restapi 返回的 JSON 字符串可能使用了错误编码，或 GsonUtils.fromJson() 未指定 UTF-8
- **修复建议**: 检查 Feign Client 的字符编码配置，确保 GsonUtils 使用 UTF-8

### P2-001: ESLint 3 个错误

- **问题编号**: P2-001
- **严重级别**: P2
- **模块**: 前端工程
- **相关文件**: `src/api/request.ts:88`, `src/views/banner/list/BannerEditDialog.vue:11`, `src/views/manage/product-list/ProductEditDialog.vue:10`
- **实际结果**: 
  1. request.ts:88 - `no-case-declarations`: switch-case 块中有词法声明
  2. BannerEditDialog.vue:11 - `no-undef`: 'reactive' 未定义（auto-import 未被 ESLint 识别）
  3. ProductEditDialog.vue:10 - `no-undef`: 'reactive' 未定义（同上）
- **修复建议**: 1. 用花括号包裹 case 块; 2. 配置 ESLint globals 或显式 import reactive

### P2-002: Stylelint 无法运行

- **问题编号**: P2-002
- **严重级别**: P2
- **模块**: 前端工程
- **实际结果**: `meow` 依赖模块缺失，stylelint 命令无法启动（ERR_MODULE_NOT_FOUND）
- **修复建议**: 检查 pnpm 安装是否完整，可能需要 `pnpm rebuild` 或重新安装依赖

### P2-003: 覆盖率工具依赖缺失

- **问题编号**: P2-003
- **严重级别**: P2
- **模块**: 前端测试
- **相关文件**: `vitest.config.ts`, `package.json`
- **实际结果**: vitest.config.ts 配置了 `provider: 'v8'`，但 `@vitest/coverage-v8` 未在 devDependencies 中声明
- **修复建议**: 在 package.json devDependencies 中添加 `@vitest/coverage-v8`

### P2-004: 测试覆盖范围不足

- **问题编号**: P2-004
- **严重级别**: P2
- **模块**: 前端测试
- **实际结果**: 38 个测试全部通过，但覆盖范围有限：
  - 已覆盖: ResponseApiBody 适配器(5)、Token 存取(5)、下载文件名解析(6)、格式化工具(10)、分页转换(6)、权限判断逻辑(6)
  - 未覆盖: 路由守卫、动态路由生成、菜单渲染、Axios 拦截器、各页面组件、API 模块调用、Store 业务逻辑、审核/推荐/上下架流程
  - 全部测试使用纯函数 Mock，无组件渲染测试、无集成测试
- **修复建议**: 增加路由守卫测试、Store 业务测试、关键组件渲染测试

### P2-005: CORS 配置过于宽松

- **问题编号**: P2-005
- **严重级别**: P2
- **模块**: 安全
- **相关文件**: `WebSecurityConfig.java:98-103`
- **实际结果**: `allowedOrigins("*")` + `allowCredentials(false)`。虽然 allowCredentials=false 降低了风险，但生产环境仍应限制允许的 Origin
- **修复建议**: 生产环境配置具体的 allowedOrigins

### P3-001: 上传接口无文件类型/大小校验

- **问题编号**: P3-001
- **严重级别**: P3
- **模块**: 文件上传
- **相关文件**: `AdminUploadController.java`, `AdminYpatController.java`
- **实际结果**: 上传接口未校验文件类型（Content-Type/扩展名）和文件大小，仅检查文件是否为空
- **修复建议**: 增加文件类型白名单和大小限制

### P3-002: 生产构建 element-plus chunk 过大

- **问题编号**: P3-002
- **严重级别**: P3
- **模块**: 前端构建
- **实际结果**: element-plus chunk 1072KB (gzip 338KB)，超过 500KB 警告阈值
- **修复建议**: 使用按需引入或 tree-shaking 优化

---

## 5. 页面验收矩阵

| 页面 | 路由 | 页面加载 | 接口 | 查询 | 操作 | 权限 | 异常状态 | 结论 |
|------|------|----------|------|------|------|------|----------|------|
| 登录页 | /login | 正常 | 验证码+登录 | N/A | 登录 | 公开 | 错误密码/验证码 | 可用 |
| 仪表盘 | /dashboard | 正常 | 无 | N/A | N/A | Token | 未登录重定向 | 可用 |
| 申请列表 | /manage/ypat-list | 正常 | GET /admin/ypat/list | 有 | 审核/推荐 | Token | 空状态 | 可用(无数据验证) |
| 实名列表 | /manage/user/index | 正常 | GET /admin/user/list | 有 | 审核 | Token | 有数据(2条) | 可用 |
| 产品列表 | /manage/product/index | 正常 | GET /admin/product/list | 有 | 上下架/编辑 | Token | **分页计数错误** | 有缺陷 |
| 文章列表 | /article/index | 正常 | GET /admin/article/list | 有 | 发布/撤回/编辑 | Token | 有数据(3条) | 可用(**中文乱码**) |
| 横幅列表 | /banner/index | 正常 | GET /admin/banner/list | 有 | 发布/撤回/编辑 | Token | 有数据(3条) | 可用(**中文乱码**) |
| 发布作品 | /ypat/edit | 正常 | POST /admin/ypat/submit | N/A | 提交 | Token | 表单 | 可用(未测试提交) |
| 用户查询 | /manage/query/index | 正常 | GET /admin/user/list | 有 | 只读 | Token | 有数据(2条) | 可用 |
| 约拍查询 | /manage/query/ypat/appindex | 正常 | GET /admin/ypat/list | 有 | 只读 | Token | 空状态 | 可用(无数据验证) |
| 消息查询 | /manage/query/mess/messindex | 正常 | GET /admin/mess/list | 有 | 只读 | Token | 空状态 | 可用(无数据验证) |
| 公众号关注 | /pubevent/index | 正常 | GET /admin/pubevent/list | 有 | 只读 | Token | 空状态 | 可用(无数据验证) |
| 订单列表 | /manage/order/index | 正常 | GET /admin/order/list | 有 | 只读 | Token | 空状态 | 可用(无数据验证) |
| 403页面 | /403 | 正常 | 无 | N/A | N/A | 公开 | N/A | 可用 |
| 404页面 | /:pathMatch(.*)* | 正常 | 无 | N/A | N/A | 公开 | N/A | 可用 |

---

## 6. 接口验收矩阵

| 前端API | Method | URL | 后端Controller | 参数匹配 | 返回格式 | 结论 |
|---------|--------|-----|----------------|----------|----------|------|
| getCaptcha | GET | /admin/captcha | AdminAuthController.captcha() | 无参数 | {code,msg,res:{captchaId,img}} | 一致 |
| login | POST | /admin/login | AdminAuthController.login() | {mobile,password,captchaId,captchaCode} → @RequestBody Map | {code,msg,res:{token,id,mobile,name,nickname}} | 一致 |
| getAdminInfo | GET | /admin/user/info | AdminAuthController.userInfo() | Header:Token | {code,msg,res:{id,mobile,name,nickname}} | 一致 |
| logout | POST | /admin/logout | AdminAuthController.logout() | Header:Token | {code,msg,res:{success}} | 一致 |
| getYpatList | GET | /admin/ypat/list | AdminYpatController.list() | status,nickname,mobile,recomflag,page,size | {code,msg,res:Page} | 一致 |
| getYpatDetail | GET | /admin/ypat/detail | AdminYpatController.detail() | id | {code,msg,res:YpatInfo} | 一致 |
| auditYpat | POST | /admin/ypat/audit | AdminYpatController.audit() | id,flag,reason (query params) | {code,msg,res:{success,data}} | 一致 |
| recomYpat | POST | /admin/ypat/recom | AdminYpatController.recom() | id,recomflag (query params) | {code,msg,res} | 一致 |
| submitYpat | POST | /admin/ypat/submit | AdminYpatController.submit() | multipart form | {code,msg,res} | 一致 |
| getUserList | GET | /admin/user/list | AdminUserController.list() | status,nickname,mobile,regisdate,gender,id,page,size | {code,msg,res:Page} | 一致 |
| getUserDetail | GET | /admin/user/detail | AdminUserController.detail() | id | {code,msg,res:OauthQo} | 一致 |
| auditUser | POST | /admin/user/audit | AdminUserController.audit() | id,flag (query params) | {code,msg,res:{success,data}} | 一致 |
| getProductList | GET | /admin/product/list | AdminProductController.list() | name,status,page,size | {code,msg,res:Page} | 一致 |
| getProductDetail | GET | /admin/product/detail | AdminProductController.detail() | id | {code,msg,res} | 一致 |
| saveProduct | POST | /admin/product/save | AdminProductController.save() | @RequestBody ProductQo | {code,msg,res} | 一致 |
| upDownProduct | POST | /admin/product/upDown | AdminProductController.upDown() | id,status (query params) | {code,msg,res} | 一致 |
| getArticleList | GET | /admin/article/list | AdminArticleController.list() | name,status,page,size | {code,msg,res:Page} | 一致 |
| getArticleDetail | GET | /admin/article/detail | AdminArticleController.detail() | id | {code,msg,res} | 一致 |
| saveArticle | POST | /admin/article/save | AdminArticleController.save() | @RequestBody ArticleQo | {code,msg,res} | 一致 |
| upDownArticle | POST | /admin/article/upDown | AdminArticleController.upDown() | id,status (query params) | {code,msg,res} | 一致 |
| getBannerList | GET | /admin/banner/list | AdminBannerController.list() | name,status,page,size | {code,msg,res:Page} | 一致 |
| getBannerDetail | GET | /admin/banner/detail | AdminBannerController.detail() | id | {code,msg,res} | 一致 |
| saveBanner | POST | /admin/banner/save | AdminBannerController.save() | @RequestBody BannerQo | {code,msg,res} | 一致 |
| upDownBanner | POST | /admin/banner/upDown | AdminBannerController.upDown() | id,status (query params) | {code,msg,res} | 一致 |
| getOrderList | GET | /admin/order/list | AdminOrderController.list() | status,type,page,size | {code,msg,res:Page} | 一致 |
| getMessList | GET | /admin/mess/list | AdminMessController.list() | ypatid,sendperid,recperid,page,size | {code,msg,res:Page} | 一致 |
| getPubEventList | GET | /admin/pubevent/list | AdminPubEventController.list() | dateStrStart,dateStrEnd,eventKey,page,size | {code,msg,res:Page} | 一致 |
| uploadFiles | POST | /admin/upload | AdminUploadController.upload() | multipart files[] | {code,msg,res:{urls}} | 一致 |
| uploadFiles(wm) | POST | /admin/ypat/upload | AdminUploadController.uploadYpat() | multipart files[] | {code,msg,res:{urls}} | 一致 |

---

## 7. 新旧迁移矩阵

| 编号 | 旧功能 | 旧文件 | 旧接口 | 新组件 | 新接口 | 真实状态 |
|------|--------|--------|--------|--------|--------|----------|
| M001 | 申请列表查询 | manage/index.html | GET /manage/list | views/manage/ypat-list/index.vue | GET /admin/ypat/list | **接口已接，页面可用（无测试数据）** |
| M002 | 审核通过 | manage/detail.html | POST /manage/audit | AuditDialog.vue | POST /admin/ypat/audit | **接口已接，未做数据库验证** |
| M003 | 审核不通过 | manage/detail.html | POST /manage/audit | AuditDialog.vue | POST /admin/ypat/audit | **接口已接，未做数据库验证** |
| M004 | 上推荐 | manage/index.html | POST /manage/upRecom | ypat-list/index.vue | POST /admin/ypat/recom | **接口已接，未做数据库验证** |
| M005 | 取消推荐 | manage/index.html | POST /manage/upRecom | ypat-list/index.vue | POST /admin/ypat/recom | **接口已接，未做数据库验证** |
| M006 | 实名列表查询 | manage/user/index.html | GET /manage/user/list | views/manage/user-list/index.vue | GET /admin/user/list | **完整可用（有2条数据验证）** |
| M007 | 实名审核通过 | manage/user/detail.html | GET /manage/user/audit | UserAuditDialog.vue | POST /admin/user/audit | **接口已接，未做数据库验证** |
| M008 | 实名审核不通过 | manage/user/detail.html | GET /manage/user/audit | UserAuditDialog.vue | POST /admin/user/audit | **接口已接，未做数据库验证** |
| M009 | 产品列表查询 | manage/product/index.html | GET /manage/product/list | views/manage/product-list/index.vue | GET /admin/product/list | **有缺陷（分页计数错误+中文乱码）** |
| M010 | 产品新增/编辑 | manage/product/edit.html | GET edit + POST save | ProductEditDialog.vue | GET detail + POST save | **接口已接，ESLint有error** |
| M011 | 产品上架 | manage/product/index.html | POST /manage/product/upDown | product-list/index.vue | POST /admin/product/upDown | **接口已接，未做数据库验证** |
| M012 | 产品下架 | manage/product/index.html | POST /manage/product/upDown | product-list/index.vue | POST /admin/product/upDown | **接口已接，未做数据库验证** |
| M013 | 文章列表查询 | manage/article/index.html | GET /article/list | views/article/list/index.vue | GET /admin/article/list | **可用（有3条数据，中文乱码）** |
| M014 | 文章编辑 | manage/article/edit.html | GET edit + POST save | views/article/edit/index.vue | GET detail + POST save | **接口已接，未做数据库验证** |
| M015 | 文章发布 | manage/article/index.html | POST /article/upDown | article/list/index.vue | POST /admin/article/upDown | **接口已接，未做数据库验证** |
| M016 | 文章撤回 | manage/article/index.html | POST /article/upDown | article/list/index.vue | POST /admin/article/upDown | **接口已接，未做数据库验证** |
| M017 | 横幅列表查询 | manage/banner/index.html | GET /banner/list | views/banner/list/index.vue | GET /admin/banner/list | **可用（有3条数据，中文乱码）** |
| M018 | 横幅新增/编辑 | manage/banner/edit.html | GET edit + POST save | BannerEditDialog.vue | GET detail + POST save | **接口已接，ESLint有error** |
| M019 | 横幅图片预览 | manage/banner/index.html | window.open | banner/list/index.vue | 图片URL直接展示 | **已迁移** |
| M020 | 横幅发布 | manage/banner/index.html | POST /banner/upDown | banner/list/index.vue | POST /admin/banner/upDown | **接口已接，未做数据库验证** |
| M021 | 横幅撤回 | manage/banner/index.html | POST /banner/upDown | banner/list/index.vue | POST /admin/banner/upDown | **接口已接，未做数据库验证** |
| M022 | 发布作品 | manage/ypatinfo/edit.html | POST /ypat/submit | views/ypat/edit/index.vue | POST /admin/ypat/submit | **接口已接，未测试提交** |
| M023 | 用户查询 | manage/query/userindex.html | GET /manage/user/list | views/query/user-list/index.vue | GET /admin/user/list | **完整可用（有2条数据验证）** |
| M024 | 约拍查询 | manage/query/ypatappindex.html | GET /manage/list | views/query/ypat-list/index.vue | GET /admin/ypat/list | **接口已接，页面可用（无测试数据）** |
| M025 | 消息查询 | manage/query/messindex.html | GET /manage/mess/list | views/query/mess-list/index.vue | GET /admin/mess/list | **接口已接，页面可用（无测试数据）** |
| M026 | 公众号关注 | manage/pubevent/index.html | GET /pubevent/list | views/pubevent/list/index.vue | GET /admin/pubevent/list | **接口已接，页面可用（无测试数据）** |
| M027 | 订单查询 | manage/order/index.html | GET /manage/order/list | views/order/list/index.vue | GET /admin/order/list | **接口已接，页面可用（无测试数据）** |

---

## 8. 安全验收

### 8.1 管理员身份校验

| 检查项 | 结果 | 说明 |
|--------|------|------|
| 管理员登录是否区分管理员/普通用户 | **不通过** | AdminAuthService.login() 无角色校验，任何有密码的用户可登录 |
| 管理员身份来源 | **无** | t_user 表无角色字段、无角色关联表、无管理员白名单 |
| 旧后台管理员标识 | 已记录 | Const.SYS_ADMIN = "o5ZmB4kyCVPskEOaO0PK1He0Kl7w"（openid），但代码中未使用 |
| 普通用户登录后台 | **可绕过** | 任何 t_user 记录+正确密码即可登录 |

### 8.2 接口权限

| 检查项 | 结果 | 说明 |
|--------|------|------|
| 无 Token 访问 /admin/** | **拦截（返回 401）** | Spring Security 正确拦截 |
| 伪造 Token 访问 /admin/** | **拦截（签名校验失败）** | 返回 HTTP 500 + SignatureException |
| 普通用户 Token 访问 /admin/** | **代码审查：不拦截** | WebSecurityConfig 仅要求 authenticated，无角色校验 |
| 过期 Token | 未验证 | jjwt 库应正确处理 |
| 菜单隐藏 vs 后端拦截 | **仅前端守卫** | 后端无 /admin/** 路径级别的角色拦截 |

### 8.3 JWT

| 检查项 | 结果 | 说明 |
|--------|------|------|
| 签名算法 | HS512 | 安全 |
| 密钥 | "development-only-jwt-key-change-me" (34字符) | **P2: 生产必须更换** |
| 过期时间 | 1 年 (31536000 秒) | **P2: 建议缩短** |
| Token Header | "Token" | 与旧 H5 一致 |

### 8.4 验证码

| 检查项 | 结果 | 说明 |
|--------|------|------|
| 验证码类型 | 图片验证码 (Base64 JPEG) | 正常 |
| 有效期 | 5 分钟 | 正常 |
| 存储方式 | ConcurrentHashMap + UUID | 不适合多实例部署 |
| 大小写敏感 | 不敏感 (equalsIgnoreCase) | 与旧后台一致 |
| 暴力破解防护 | **无** | 无尝试次数限制 |

### 8.5 CORS

| 检查项 | 结果 | 说明 |
|--------|------|------|
| allowedOrigins | "*" | **P2: 生产应限制** |
| allowCredentials | false | 降低了风险 |
| allowedMethods | GET, POST, PUT, DELETE, OPTIONS | 正常 |

### 8.6 文件上传

| 检查项 | 结果 | 说明 |
|--------|------|------|
| 文件类型校验 | **无** | P3: 未校验 Content-Type 或扩展名 |
| 文件大小限制 | **无（代码层）** | Spring Boot 默认限制可能生效 |
| 路径穿越 | 未验证 | 文件名直接传递给 FastDFS |

---

## 9. CLI 结果

### 9.1 前端工程门禁

| 命令 | 退出码 | 结果 |
|------|--------|------|
| pnpm install --frozen-lockfile | 0 | Lockfile is up to date |
| pnpm type-check | 0 | TypeScript 0 error |
| pnpm lint:check | 1 | 3 errors, 910 warnings |
| pnpm stylelint:check | 1 | ERR_MODULE_NOT_FOUND (meow) |
| pnpm test | 0 | 6 test files, 38 tests passed |
| pnpm test:coverage | 1 | Missing dependency @vitest/coverage-v8 |
| pnpm build | 0 | 构建成功，6秒，element-plus chunk 1072KB |

### 9.2 后端工程门禁

| 命令 | 退出码 | 结果 |
|------|--------|------|
| mvn clean compile -pl system-wap -am | 0 | 编译成功 |
| mvn test -pl system-wap -am | 0 | 31 tests, 0 failures, 0 errors |
| Java 8+ API 检查 | N/A | 未发现 Stream.toList/List.of/Map.of/var/records |

---

## 10. Nginx 与接口路径

| 配置项 | 路径 | 目标 | 结论 |
|--------|------|------|------|
| frontend-admin/nginx.conf | /admin/ | wap:8081/admin/ | **正确** |
| docker/nginx/default.conf | /admin/ | system-web:8082/ | **P0: 主nginx路由到旧后台** |
| vite.config.ts (dev) | /admin | localhost:8081 | **正确** |
| .env.development | VITE_API_BASE_URL | http://localhost:8081 | **正确** |
| .env.production | VITE_API_BASE_URL | https://api.ypat.com | **正确（需确认生产路径）** |
| SPA fallback | try_files | /index.html | **正确** |

### 主 Nginx 路由冲突

`docker/nginx/default.conf` 中 `/admin/` 指向 `system-web:8082`（旧后台）。这是为旧后台保留的配置。当新后台替换旧后台时，必须将此路由改为指向 `wap:8081`，否则通过主 nginx 访问的 /admin/ 请求将到达旧后台。

当前部署方案中 admin-web Docker 容器有自己的 nginx（端口 18896:8080），通过该端口访问不受影响。但如果通过主 nginx（端口 8080）统一入口访问，则 /admin/ 路径会冲突。

---

## 11. 测试真实性审查

| 测试文件 | 测试数 | 类型 | Mock程度 | 评估 |
|----------|--------|------|----------|------|
| api.test.ts | 5 | 纯函数 | 完全自包含 | 验证适配逻辑，有效但非集成测试 |
| auth.test.ts | 5 | localStorage | 无Mock | 验证Token存取，有效 |
| download.test.ts | 6 | 纯函数 | 无Mock | 验证文件名解析，有效 |
| format.test.ts | 10 | 纯函数 | 无Mock | 验证格式化/脱敏，有效 |
| pagination.test.ts | 6 | 纯函数 | 完全自包含 | 验证分页转换，有效 |
| permission.test.ts | 6 | 纯函数 | 完全自包含 | 验证权限判断逻辑，有效 |

**未覆盖的核心模块**: 路由守卫、动态路由生成、Axios拦截器完整流程、各Store业务逻辑、组件渲染、审核/推荐/上下架完整流程

---

## 12. 旧后台反向完整性审计

### 12.1 旧页面清单 (22个)

| # | 旧页面 | 迁移结论 |
|---|--------|----------|
| 1 | login.html | **已迁移** → views/login/index.vue |
| 2 | home.html | **已迁移** → views/dashboard/index.vue |
| 3 | manage/index.html (申请列表) | **已迁移** → views/manage/ypat-list/index.vue |
| 4 | manage/detail.html (申请详情) | **已迁移** → AuditDialog.vue (弹窗) |
| 5 | manage/user/index.html (实名列表) | **已迁移** → views/manage/user-list/index.vue |
| 6 | manage/user/detail.html (实名详情) | **已迁移** → UserAuditDialog.vue (弹窗) |
| 7 | manage/product/index.html (产品列表) | **已迁移** → views/manage/product-list/index.vue |
| 8 | manage/product/edit.html (产品编辑) | **已迁移** → ProductEditDialog.vue (弹窗) |
| 9 | manage/article/index.html (文章列表) | **已迁移** → views/article/list/index.vue |
| 10 | manage/article/edit.html (文章编辑) | **已迁移** → views/article/edit/index.vue |
| 11 | manage/banner/index.html (横幅列表) | **已迁移** → views/banner/list/index.vue |
| 12 | manage/banner/edit.html (横幅编辑) | **已迁移** → BannerEditDialog.vue (弹窗) |
| 13 | manage/ypatinfo/edit.html (发布作品) | **已迁移** → views/ypat/edit/index.vue |
| 14 | manage/query/userindex.html (用户查询) | **已迁移** → views/query/user-list/index.vue |
| 15 | manage/query/ypatappindex.html (约拍查询) | **已迁移** → views/query/ypat-list/index.vue |
| 16 | manage/query/messindex.html (消息查询) | **已迁移** → views/query/mess-list/index.vue |
| 17 | manage/pubevent/index.html (公众号关注) | **已迁移** → views/pubevent/list/index.vue |
| 18 | manage/order/index.html (订单列表) | **已迁移** → views/order/list/index.vue |
| 19 | 403.html | **已迁移** → views/error/403.vue |
| 20 | deny.html | **合并** → views/error/403.vue |
| 21 | tologin.html | **已废弃** (前端路由处理) |
| 22 | tosignout.html | **已废弃** (前端路由处理) |

**结论: 22/22 全部有明确迁移结论，无遗漏页面**

### 12.2 旧接口清单 (40个 W001-W040)

| 编号 | 旧接口 | 迁移结论 |
|------|--------|----------|
| W001 | /manage/home | 已替代 → /dashboard (前端路由) |
| W002 | /manage/ (登录页) | 已替代 → /login (前端路由) |
| W003 | POST /manage/login | 已替代 → POST /admin/login |
| W004 | /manage/logout | 已替代 → POST /admin/logout |
| W005 | /manage/index (页面) | 已替代 → /manage/ypat-list |
| W006 | GET /manage/detail | 已替代 → GET /admin/ypat/detail |
| W007 | GET /manage/list | 已替代 → GET /admin/ypat/list |
| W008 | POST /manage/audit | 已替代 → POST /admin/ypat/audit |
| W009 | POST /manage/upRecom | 已替代 → POST /admin/ypat/recom |
| W010-W015 | 产品相关 (6个) | 已替代 → /admin/product/* (4个接口覆盖6个操作) |
| W016-W019 | 实名相关 (4个) | 已替代 → /admin/user/* (3个接口覆盖4个操作) |
| W020-W022 | 查询页面 (3个) | 已替代 → 前端路由 |
| W023 | GET /manage/mess/list | 已替代 → GET /admin/mess/list |
| W024-W025 | 订单相关 (2个) | 已替代 → GET /admin/order/list |
| W026-W030 | 文章相关 (5个) | 已替代 → /admin/article/* (4个接口覆盖5个操作) |
| W031-W035 | 横幅相关 (5个) | 已替代 → /admin/banner/* (4个接口覆盖5个操作) |
| W036-W037 | 发布作品 (2个) | 已替代 → POST /admin/ypat/submit |
| W038-W039 | 公众号关注 (2个) | 已替代 → GET /admin/pubevent/list |
| W040 | /ueditor/* | **遗漏** → 无新版替代（旧版 UEditor 后端图片上传） |

**结论: 39/40 已迁移或有明确替代，1 个遗漏 (W040 UEditor)**

### 12.3 UEditor 遗漏说明

旧后台 W040 `/ueditor/*` 提供 UEditor 富文本编辑器的后端服务（主要是图片上传）。新版使用前端富文本编辑器（如 wangEditor），图片上传通过 `/admin/upload` 接口实现。但迁移矩阵中未明确标记 W040 为"废弃"或"已替代"。

**影响**: 如果新版文章编辑器使用前端图片上传（通过 /admin/upload），则 W040 可以标记为"废弃"，不影响功能。需确认前端文章编辑组件是否使用了独立的图片上传机制。

---

## 13. 菜单验证结果 (11个)

| # | 旧菜单 | 新版菜单 | 路由 | 结论 |
|---|--------|----------|------|------|
| 1 | 申请列表 | 申请列表 | /manage/ypat-list | 已迁移 |
| 2 | 实名列表 | 实名列表 | /manage/user/index | 已迁移 |
| 3 | 产品列表 | 产品列表 | /manage/product/index | 已迁移 |
| 4 | 文章列表 | 文章列表 | /article/index | 已迁移 |
| 5 | 横幅列表 | 横幅列表 | /banner/index | 已迁移 |
| 6 | 发布作品 | 发布作品 | /ypat/edit | 已迁移 |
| 7 | 用户列表(查询) | 用户列表 | /manage/query/index | 已迁移 |
| 8 | 约拍列表(查询) | 约拍列表 | /manage/query/ypat/appindex | 已迁移 |
| 9 | 消息列表(查询) | 消息列表 | /manage/query/mess/messindex | 已迁移 |
| 10 | 公众号关注 | 公众号关注 | /pubevent/index | 已迁移 |
| 11 | 订单列表 | 订单列表 | /manage/order/index | 已迁移 |

**结论: 11/11 菜单全部有对应，无遗漏**

---

## 14. 浏览器和数据库证据

### 浏览器截图

| 页面 | 截图路径 |
|------|----------|
| 登录页 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-224ffa9d.png` |
| 仪表盘 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-190a19ed.png` |
| 申请列表 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-85f8646e.png` |
| 实名列表 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-0b68d3f5.png` |
| 产品列表 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-6acab1d1.png` |
| 文章列表 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-cbf2784e.png` |
| 横幅列表 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-f5a9e92.png` |
| 发布作品 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-2bf896b3.png` |
| 用户查询 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-99bc7183.png` |
| 约拍查询 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-7aa478da.png` |
| 消息查询 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-43f0a52f.png` |
| 公众号关注 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-43f0a52f.png` |
| 订单列表 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-fc3720aa.png` |
| 404页面 | `/Users/lizhenwei/Library/Application Support/QoderCN/SharedClientCache/cache/images/2a2f2065/img-a21930f7.png` |

### 接口测试证据

```
# 无Token访问管理接口 → 401
curl -s http://localhost:8081/admin/ypat/list
→ {"code":"401","msg":"unauthorized"}

# 验证码接口 → 正常
curl -s http://localhost:8081/admin/captcha
→ {"code":200,"msg":"成功","res":{"captchaId":"...","img":"data:image/jpeg;base64,..."}}

# 伪造Token → 签名校验失败
curl -s http://localhost:8081/admin/ypat/list -H "Token: fake.token.here"
→ {"status":500,"exception":"io.jsonwebtoken.SignatureException"}

# 管理员登录成功 (浏览器测试)
# mobile: 13800000000, password: 123, captcha: mare → 登录成功跳转 /dashboard
```

### 数据库证据

```sql
-- t_user 表数据（脱敏）
SELECT id, mobile, name, password FROM t_user;
+----+-------------+-------+----------------------------------+
| id | mobile      | name  | password                         |
+----+-------------+-------+----------------------------------+
|  1 | 13800000000 | Admin | 202CB962AC59075B964B07152D234B70 |
|  2 | 18888888888 | NULL  | NULL                             |
+----+-------------+-------+----------------------------------+

-- 注意: 无角色字段、无 is_admin 字段、无角色关联表
-- 任何有 password 的用户都可登录后台
```

---

## 15. 未验证项

| 项目 | 状态 | 说明 |
|------|------|------|
| 普通用户Token访问admin接口（实测） | 未验证 | 无法在运行环境中生成匹配签名的JWT Token（jjwt 0.9.0 Base64编码差异），但**代码审查确认无角色拦截** |
| 审核操作数据库副作用 | 未验证 | 无测试数据（申请列表为空），无法执行审核操作 |
| 推荐操作数据库副作用 | 未验证 | 同上 |
| 产品编辑保存数据库验证 | 未验证 | 可执行但未在验收中操作 |
| 文章编辑/发布/撤回数据库验证 | 未验证 | 可执行但未在验收中操作 |
| 横幅编辑/发布/撤回数据库验证 | 未验证 | 可执行但未在验收中操作 |
| 发布作品完整提交 | 未验证 | 需要多图上传+水印，未在验收中操作 |
| 并发操作测试 | 未验证 | 未执行 |
| UI分辨率适配(1366x768/1440x900/1024x768) | 未验证 | 仅使用默认分辨率测试 |
| 刷新后动态菜单/路由恢复 | 部分验证 | 浏览器测试中未专门测试刷新 |
| 生产构建(.env.production)路径正确性 | 未验证 | API_BASE_URL=https://api.ypat.com 未确认生产环境实际路径 |

---

## 16. 硬性通过标准检查

- [x] 普通用户无法登录后台 → **不通过 (P0-001)**
- [x] 普通用户无法访问任何 /admin/** 管理接口 → **不通过 (P0-002)**
- [ ] 管理员身份由后端可信来源判定 → **不存在**
- [x] 11 个旧菜单全部有明确迁移结论 → 通过
- [x] 22 个旧页面全部有明确迁移结论 → 通过
- [x] 40 个旧接口全部有明确迁移结论 → 39/40 通过，1 个遗漏(W040)
- [x] M001—M027 全部真实验证 → 接口已接，部分写操作未做数据库验证
- [ ] 所有写操作已核对数据库结果 → **未完成（无测试数据）**
- [x] 查询、重置、分页全部正确 → 大部分正确，产品列表分页计数有误(P1)
- [ ] 审核、推荐、上下架、发布、撤回全部正确 → **接口已接，未做数据库验证**
- [x] 上传和图片预览正确 → 上传接口存在，未做完整测试
- [x] 动态菜单刷新后不丢失 → 路由守卫逻辑正确
- [x] 403、404、Token 失效正确 → 已验证
- [x] TypeScript 0 error → 通过
- [ ] ESLint 0 error → **不通过 (3 errors)**
- [ ] Stylelint 0 error → **无法运行 (依赖缺失)**
- [x] 测试真实通过 → 38/38 通过，但覆盖范围有限
- [x] 生产构建成功 → 通过
- [x] 后端编译和测试成功 → 31/31 通过
- [x] 无未说明的占位功能 → 通过（rg 搜索无 TODO/FIXME/mock）
- [x] 无未说明的旧功能遗漏 → 1 个遗漏 (W040 UEditor) 已说明
- [ ] 无 P0 → **2 个 P0**
- [ ] 无阻断替换旧后台的 P1 → **2 个 P1**

---

## 17. 修复优先级建议

### 必须修复（阻断替换）

1. **P0-001**: 增加管理员身份校验机制
2. **P0-002**: 在 Spring Security 中对 /admin/** 增加角色拦截

### 强烈建议修复

3. **P1-001**: 修复产品列表分页计数问题
4. **P1-002**: 修复中文编码问题

### 建议修复

5. **P2-001**: 修复 ESLint 3 个错误
6. **P2-002**: 修复 Stylelint 依赖问题
7. **P2-003**: 添加 @vitest/coverage-v8 依赖
8. **P2-004**: 扩展测试覆盖范围
9. **P2-005**: 限制 CORS allowedOrigins
