# YPAT 旧后台管理端 - 权限清单

> 审计日期：2026-06-30
> 审计范围：system-web、system-wap、system-sso 认证与权限体系

## 一、认证机制

### 1.1 旧后台 system-web（Session 认证）

| 项目 | 详情 |
|------|------|
| 认证方式 | HttpSession |
| 登录接口 | POST `/manage/login`（表单提交） |
| 登录参数 | mobile(手机号) + password(MD5加密大写) |
| 密码加密 | `MD5Util.encode(password, "UTF-8").toUpperCase()` |
| Session Key | `Const.USER_SESSION_KEY` |
| 拦截器 | `LoginInterceptor`（preHandle 检查 Session） |
| 未登录行为 | 重定向到 `/manage/`（登录页） |
| 退出 | POST `/manage/logout`（session.invalidate()） |
| 用户信息存储 | `SecurityUserDetails`（userId, username, mobile） |

### 1.2 system-wap（JWT 认证，H5/小程序用）

| 项目 | 详情 |
|------|------|
| 认证方式 | JWT Token（无状态） |
| Token Header | `Token`（Const.HEADER_STRING） |
| Token 前缀 | 无（JwtTokenFilter 直接使用 header 原始值） |
| 签名算法 | HS512 |
| 密钥 | 环境变量 `YPAT_WAP_JWT_SECRET`（默认 development-only-jwt-key-change-me） |
| 过期时间 | 31536000 秒（1年） |
| Token Filter | `JwtTokenFilter`（OncePerRequestFilter） |
| Token 工具 | `JwtTokenUtil`（generateToken/validateToken/refreshToken） |
| Session 策略 | `SessionCreationPolicy.STATELESS` |
| 登录接口 | POST `/user/login`（手机号+短信验证码） |

### 1.3 system-sso（图片验证码）

| 项目 | 详情 |
|------|------|
| 验证码生成 | GET `/images/imagecode`（ImageCode.getImageCode 60x20 JPEG） |
| 验证码存储 | Session（key: `simpleCaptcha`） |
| 验证码有效期 | 5 分钟 |
| 验证码校验 | GET `/checkcode?checkCode=xxx`（成功返回 "1"） |
| 大小写 | 不区分（equalsIgnoreCase） |

## 二、菜单权限

### 2.1 菜单来源

菜单**硬编码**在 `templates/fragments/nav.html` 中，**非数据库驱动**，通过 3 个 Thymeleaf 片段实现：

| 片段 | 系统名称 | sysflag | 菜单项 |
|------|----------|---------|--------|
| nav | 审核系统 | 1 | 申请列表、实名列表、产品列表、文章列表、横幅列表、发布作品 |
| nav2 | 查询系统 | 2 | 用户列表、约拍列表、消息列表、公众号关注 |
| nav3 | 订单系统 | 3 | 订单列表 |

### 2.2 菜单显示控制

- `sysflag` 通过 `ManageController.setSystemFlag()` 设置为 Thymeleaf 静态变量
- `layout.html` 根据 `sysflag` 值（1/2/3）选择渲染对应的 nav 片段
- **无动态菜单**，无菜单权限分配，无角色-菜单关联

## 三、路由权限

### 3.1 system-web 路由拦截

| 项目 | 详情 |
|------|------|
| 拦截器 | `LoginInterceptor` |
| 拦截规则 | 所有 `/manage/**` 路径（排除登录页和静态资源） |
| 未登录处理 | `response.sendRedirect(contextPath + "/manage/")` |
| 已登录放行 | 直接返回 true |

### 3.2 system-wap Spring Security 配置

| 路径 | HTTP方法 | 权限 | 说明 |
|------|----------|------|------|
| OPTIONS | * | permitAll | 预检请求 |
| /user/login, /user/sms/code | POST | permitAll | 登录相关 |
| /bd/login, /bd/code | POST | permitAll | 百度登录 |
| /wxpay/notify, /wxpub/notify | POST/GET | permitAll | 支付回调 |
| /user/code | GET | permitAll | 微信授权 |
| /ypat/tc/list, /ypat/zx/list, /ypat/get | GET | permitAll | 约拍公开接口 |
| /banner/list, /article/list, /article/get | GET | permitAll | 内容公开接口 |
| /area/list, /tmplid/list, /param/list, /product/list | GET | permitAll | 配置公开接口 |
| /** | * | authenticated | 其余需认证 |

### 3.3 静态资源放行

```
/favicon.ico, /css/**, /webjars/**, /styles/**, /scripts/**, /images/**, /**.html
```

## 四、按钮权限

**旧后台无细粒度按钮权限。**

- 登录后即可访问所有功能和操作
- 无 `@PreAuthorize`、`@RequiresPermissions` 等权限注解
- 无按钮级别权限标识
- 所有操作按钮（审核、上架/下架、发布/撤回、上推荐等）对登录用户均可见可操作

## 五、角色权限

**旧后台无角色体系。**

- 无角色表（无 sys_role 表）
- 无角色-用户关联
- 无角色-菜单关联
- 无角色-权限关联
- `system-wap` 虽然有 `@EnableGlobalMethodSecurity(prePostEnabled=true)`，但**未实际使用** `@PreAuthorize` 注解

## 六、管理员特权

| 项目 | 详情 |
|------|------|
| 管理员标识 | `Const.SYS_ADMIN = "o5ZmB4kyCVPskEOaO0PK1He0Kl7w"`（硬编码 openid） |
| 管理员登录 | 通过 `/manage/login`（手机号+密码），无角色区分 |
| 特权判断 | 代码中未见基于 SYS_ADMIN 的特权逻辑 |
| 管理员账号 | 数据库中手动配置的管理员手机号 |

## 七、数据权限

**旧后台无数据权限控制。**

- 无全部数据/本人数据/部门数据等范围控制
- 所有管理员可查看所有数据
- 无数据过滤拦截器
- 无基于用户ID的数据隔离

## 八、文件上传/下载权限

| 操作 | 接口 | 权限 | 说明 |
|------|------|------|------|
| 文章图片上传 | POST `/article/save`（MultipartFile） | 登录即可 | 上传到 FastDFS |
| 横幅图片上传 | POST `/banner/save`（MultipartFile） | 登录即可 | 上传到 FastDFS |
| 作品图片上传 | POST `/ypat/submit`（MultipartFile[]） | 登录即可 | 上传到 FastDFS + 水印 |
| UEditor 图片上传 | ANY `/ueditor/*` | 登录即可 | UEditor 后端处理 |
| H5 文件上传 | POST `/file/upload` | JWT认证 | system-wap 文件上传 |

## 九、安全分析

### 9.1 安全风险

| 风险项 | 现状 | 严重程度 |
|--------|------|----------|
| 密码加密 | MD5（不安全，应使用 BCrypt） | 高 |
| Session 认证 | 无 CSRF 防护（csrf 未显式配置） | 中 |
| 无按钮权限 | 所有操作对登录用户开放 | 中 |
| 管理员标识硬编码 | openid 硬编码在代码中 | 低 |
| 无数据权限 | 所有管理员可见所有数据 | 低（单管理员场景可接受） |
| 验证码 Session 存储 | 不适用于前后端分离 | 中 |

### 9.2 新版权限设计建议

基于旧后台权限现状，新版建议：

1. **认证**：复用 system-wap JWT 体系，新增 `/admin/login` 接口
2. **菜单**：前端静态菜单配置（与旧后台一致，3 个系统）
3. **路由权限**：路由守卫检查 JWT Token，未登录跳转登录页
4. **按钮权限**：预留 `v-permission` 指令（旧后台无按钮权限，新版统一开放，后续可扩展）
5. **角色权限**：暂不实现角色体系（旧后台无角色），后续可扩展
6. **数据权限**：暂不实现（旧后台无数据权限）
7. **验证码**：改用 UUID + ConcurrentHashMap + TTL 方案（适配无状态）
8. **密码加密**：保持 MD5 兼容旧数据（后续可迁移 BCrypt）
