# YPAT 旧后台管理端 - 接口清单

> 审计日期：2026-06-30
> 审计范围：system-web、system-restapi、system-wap、system-sso 全部 Controller
> 响应格式：`ResponseApiBody { code: int, msg: String, res: Object }`，分页为 Spring Data Page（`res.content` + `res.totalElements`）

## 一、system-web 模块接口（旧后台前端直接调用）

system-web 是旧后台前端入口，Controller 返回视图（Thymeleaf 模板）或 JSON（@ResponseBody）。

### 1.1 ManageController（@RequestMapping("/manage")）

| 编号 | HTTP方法 | URL路径 | 请求参数 | 返回类型 | 说明 | 前端调用位置 |
|------|----------|---------|----------|----------|------|-------------|
| W001 | ANY | /manage/home | 无 | String(home.html) | 系统首页 | layout.html 首页链接 |
| W002 | GET | /manage/ | 无 | String(login/home) | 登录页/已登录跳首页 | 地址栏访问 |
| W003 | POST | /manage/login | UserQo(mobile, password) | ModelAndView(home/login) | 管理员登录 | login.html 表单提交 |
| W004 | ANY | /manage/logout | 无 | String(redirect:/manage/) | 退出登录 | layout.html #logoutform |
| W005 | ANY | /manage/index | 无 | String(manage/index) | 审核系统首页-申请列表 | nav.html 审核系统菜单 |
| W006 | GET | /manage/detail | id(Long) | ModelAndView(manage/detail) | 约拍审核详情页 | list.js detail() |
| W007 | GET | /manage/list | YpatInfoQo(status, mobile, nickname, recomflag, page, size) | String(JSON) | 申请列表分页 | list.js pageaction() |
| W008 | POST | /manage/audit | id(Long), flag(String), recomflag(String), reason(String), messflag(String) | String(JSON) | 约拍审核 | detail.js .tgBtn/.btgBtn |
| W009 | POST | /manage/upRecom | id(Long), recomflag(String) | String(JSON) | 上推荐/取消推荐 | list.js upRecom() |
| W010 | GET | /manage/product/index | 无 | String(manage/product/index) | 产品列表页 | nav.html |
| W011 | GET | /manage/product/edit | id(Long) | ModelAndView(manage/product/edit) | 产品编辑页 | product/list.js edit() |
| W012 | GET | /manage/product/get | id(Long) | String(JSON) | 获取产品详情 | product/edit.js |
| W013 | POST | /manage/product/save | ProductQo | String(JSON) | 保存产品 | product/edit.js |
| W014 | POST | /manage/product/upDown | ProductQo(id, status) | String(JSON) | 产品上架/下架 | product/list.js upDown() |
| W015 | GET | /manage/product/list | ProductQo(name, page, size) | String(JSON) | 产品列表分页 | product/list.js pageaction() |
| W016 | GET | /manage/user/index | 无 | String(manage/user/index) | 实名列表页 | nav.html |
| W017 | GET | /manage/user/detail | id(Long) | ModelAndView(manage/user/detail) | 实名审核详情页 | user/list.js detail() |
| W018 | GET | /manage/user/list | UserQo(status, page, size) | String(JSON) | 实名列表分页 | user/list.js pageaction() |
| W019 | GET | /manage/user/audit | id(Long), flag(String) | String(JSON) | 用户实名审核 | user/detail.js .tgBtn/.btgBtn |
| W020 | GET | /manage/query/index | 无 | String(manage/query/userindex) | 查询系统-用户列表 | nav.html |
| W021 | GET | /manage/query/ypat/appindex | 无 | String(manage/query/ypatappindex) | 查询系统-约拍列表 | nav.html |
| W022 | GET | /manage/query/mess/messindex | 无 | String(manage/query/messindex) | 查询系统-消息列表 | nav.html |
| W023 | GET | /manage/mess/list | MessInfoQo(ypatid, sendperid, recperid, page, size) | String(JSON) | 消息列表分页 | query/messlist.js |
| W024 | GET | /manage/order/index | 无 | String(manage/order/index) | 订单系统首页 | nav.html |
| W025 | GET | /manage/order/list | OrderQo(status, page, size) | String(JSON) | 订单列表分页 | order/list.js |

### 1.2 ArticleController（@RequestMapping("/article")）

| 编号 | HTTP方法 | URL路径 | 请求参数 | 返回类型 | 说明 | 前端调用位置 |
|------|----------|---------|----------|----------|------|-------------|
| W026 | GET | /article/index | 无 | String(manage/article/index) | 文章列表页 | nav.html |
| W027 | GET | /article/edit | id(Long) | ModelAndView(manage/article/edit) | 文章编辑页 | article/list.js edit() |
| W028 | GET | /article/list | ArticleQo(name, page, size) | String(JSON) | 文章列表分页 | article/list.js |
| W029 | POST | /article/save | ArticleQo + MultipartFile(file) | String(JSON) | 保存文章（含图片上传FastDFS） | article/edit.js |
| W030 | POST | /article/upDown | ArticleQo(id, status) | String(JSON) | 文章发布/撤回 | article/list.js upDown() |

### 1.3 BannerController（@RequestMapping("/banner")）

| 编号 | HTTP方法 | URL路径 | 请求参数 | 返回类型 | 说明 | 前端调用位置 |
|------|----------|---------|----------|----------|------|-------------|
| W031 | GET | /banner/index | 无 | String(manage/banner/index) | 横幅列表页 | nav.html |
| W032 | GET | /banner/edit | id(Long) | ModelAndView(manage/banner/edit) | 横幅编辑页 | banner/list.js edit() |
| W033 | GET | /banner/list | BannerQo(name, page, size) | String(JSON) | 横幅列表分页 | banner/list.js |
| W034 | POST | /banner/save | BannerQo + MultipartFile | String(JSON) | 保存横幅 | banner/edit.js |
| W035 | POST | /banner/upDown | BannerQo(id, status) | String(JSON) | 横幅发布/撤回 | banner/list.js upDown() |

### 1.4 YpatInfoController（@RequestMapping("/ypat")）

| 编号 | HTTP方法 | URL路径 | 请求参数 | 返回类型 | 说明 | 前端调用位置 |
|------|----------|---------|----------|----------|------|-------------|
| W036 | GET | /ypat/edit | id(Long) | ModelAndView(manage/ypatinfo/edit) | 发布作品页 | nav.html |
| W037 | POST | /ypat/submit | YpatInfoQo + MultipartFile(file) + MultipartFile[](files) | String(JSON) | 提交约拍作品（含头像+多图上传FastDFS+水印） | ypatinfo/edit.js |

### 1.5 PubEventController（@RequestMapping("/pubevent")）

| 编号 | HTTP方法 | URL路径 | 请求参数 | 返回类型 | 说明 | 前端调用位置 |
|------|----------|---------|----------|----------|------|-------------|
| W038 | GET | /pubevent/index | 无 | String(manage/pubevent/index) | 公众号关注统计页 | nav.html |
| W039 | GET | /pubevent/list | dateStrStart, dateStrEnd, eventKey, page, size | String(JSON) | 公众号关注统计分页 | pubevent/list.js |

### 1.6 UeditorController

| 编号 | HTTP方法 | URL路径 | 请求参数 | 返回类型 | 说明 | 前端调用位置 |
|------|----------|---------|----------|----------|------|-------------|
| W040 | ANY | /ueditor/* | UEditor标准参数 | String(JSON) | UEditor富文本后端（图片上传等） | article/edit.html UEditor |

## 二、system-wap 模块接口（H5/小程序用，已有 JWT 认证）

system-wap 是 H5/小程序前端接口，使用 JWT Token 认证（Header: `Token`），无状态。

### 2.1 LoginController

| 编号 | HTTP方法 | URL路径 | 请求参数 | 返回类型 | 说明 | 认证 |
|------|----------|---------|----------|----------|------|------|
| A001 | GET | /user/code | code(String) | ResponseApiBody | 微信授权登录 | 公开 |
| A002 | POST | /user/sms/code | mobile(String) | String(JSON) | 发送短信验证码 | 公开 |
| A003 | POST | /user/login | UserQo(mobile, code, channel) | String(JSON) | 手机号+验证码登录 | 公开 |
| A004 | GET | /user/token | UserQo | String(JSON) | 刷新Token | 已认证 |
| A005 | POST | /bd/code | code(String) | String(JSON) | 百度授权码 | 公开 |
| A006 | POST | /bd/login | UserQo(code, channel) | String(JSON) | 百度授权登录 | 公开 |

### 2.2 其他 system-wap Controller（H5业务接口，均需JWT认证）

| Controller | 主要接口 | 说明 |
|------------|----------|------|
| UserController | /service/user/get, /service/user/findPage, /service/user/findByMobile | 用户信息（内部微服务接口） |
| YpatInfoController | /ypat/tc/list, /ypat/zx/list, /ypat/get, /ypat/submit | 约拍信息 |
| OrderController | /order/list, /order/get | 订单 |
| ArticleController | /article/list, /article/get | 文章 |
| BannerController | /banner/list | 横幅 |
| ProductController | /product/list | 产品 |
| MessInfoController | /mess/list | 消息 |
| FeedbackController | /feedback/add | 反馈 |
| FileUploadController | /file/upload | 文件上传 |
| MypatInfoController | /mypat/** | 我的约拍 |
| OauthController | /oauth/** | 实名认证 |
| ParamController | /param/** | 参数配置 |
| RecordController | /record/** | 记录 |
| BillController | /bill/** | 账单 |
| WXPayNotifyController | /wxpay/notify, /wxpub/notify | 微信支付回调 |
| WXQrCodeController | /wxqrcode/** | 微信二维码 |

## 三、system-restapi 模块接口（微服务内部接口）

system-restapi 是微服务内部接口（`/service/**`），供 system-web 通过 Feign Client 调用，不直接对外暴露。

| Controller | 主要接口 | 说明 |
|------------|----------|------|
| UserController | GET /service/user/get, POST /service/user/findPage | 用户信息 |
| ArticleController | GET /service/article/get, POST /service/article/findPage | 文章 |
| BannerController | GET /service/banner/get, POST /service/banner/findPage | 横幅 |
| OrderController | GET /service/order/get, POST /service/order/findPage | 订单 |
| ProductController | GET /service/product/get, POST /service/product/findPage | 产品 |
| PubEventController | POST /service/pubevent/findPage | 公众号关注 |
| MessInfoController | POST /service/mess/findPage | 消息 |
| YpatInfoController | GET /service/ypat/get, POST /service/ypat/findPage | 约拍信息 |
| OauthController | GET /service/oauth/getAuth | 实名认证信息 |
| RecordController | POST /service/record/findPage | 记录 |
| BillController | POST /service/bill/findPage | 账单 |
| FeedbackController | POST /service/feedback/findPage | 反馈 |
| MypatInfoController | POST /service/mypat/findPage | 我的约拍 |

> 注意：system-restapi 返回裸对象/Map（无 ResponseApiBody 包装），system-web Controller 通过 ServiceClient 调用后透传 JSON 字符串。

## 四、system-sso 模块接口

| 编号 | HTTP方法 | URL路径 | 请求参数 | 返回类型 | 说明 |
|------|----------|---------|----------|----------|------|
| S001 | ANY | /login | 无 | String(login) | SSO登录页 |
| S002 | ANY | /signout | 无 | String(tologin) | SSO退出 |
| S003 | ANY | / | 无 | String(home) | SSO首页 |
| S004 | GET | /images/imagecode | 无 | BufferedImage(JPEG) | 图片验证码（存Session） |
| S005 | GET | /checkcode | checkCode(String) | String | 验证码校验（5分钟有效） |
| S006 | GET | /service/{name} | name(String) | String | 服务发现 |

## 五、关键接口链路分析

### 5.1 申请列表（P001）完整链路

```
前端 list.js $.get('/manage/list', {status, mobile, nickname, recomflag, page, size})
  → ManageController.list(YpatInfoQo)
    → ypatServiceClient.findPage(ypatInfoQo)  [Feign调用]
      → system-restapi YpatInfoController POST /service/ypat/findPage
        → YpatService.findPage() → 数据库查询
    ← 返回 JSON 字符串（ResponseApiBody包装的Page）
  ← @ResponseBody 透传 JSON
前端解析 data.res.content + data.res.totalElements
```

### 5.2 实名审核（P002）完整链路

```
列表: user/list.js $.get('/manage/user/list', {status, page, size})
  → ManageController.findPage(UserQo)
    → userServiceClient.findPage(userQo) [Feign]
      → system-restapi UserController POST /service/user/findPage

详情: user/list.js $.get('/manage/user/detail?id=')
  → ManageController.userDetail(id)
    → oauthServiceClient.getAuth(id) [Feign]
      → system-restapi OauthController GET /service/oauth/getAuth

审核: user/detail.js $.post('/manage/user/audit?flag=2', formData)
  → ManageController.userAudit(id, flag)
    → oauthServiceClient.audit(id, flag) [Feign]
    → 微信消息推送 wxMessClient.sendMsg()
```

### 5.3 登录链路

```
login.html 表单 POST /manage/login (mobile + password)
  → ManageController.login(UserQo)
    → userService.manageLogin(userQo)
      → userServiceClient.findByMobile(mobile) [Feign]
      → MD5Util.encode(password).toUpperCase() 比对
    → session.setAttribute(USER_SESSION_KEY, userDetails)
  → 返回 home.html
```

## 六、新版后端 API 映射建议

旧后台接口是服务端渲染（Session认证 + Feign内部调用），新版需前后端分离。建议在 system-wap 新增 `/admin/**` 接口：

| 旧接口 | 新接口 | 说明 |
|--------|--------|------|
| POST /manage/login | POST /admin/login | 账号密码+验证码→JWT |
| GET /images/imagecode + /checkcode | GET /admin/captcha | UUID+图片验证码 |
| 无（Session中获取） | GET /admin/user/info | 获取当前管理员信息 |
| POST /manage/logout | POST /admin/logout | 退出登录 |
| GET /manage/list | GET /admin/ypat/list | 申请列表 |
| GET /manage/detail | GET /admin/ypat/detail | 申请详情 |
| POST /manage/audit | POST /admin/ypat/audit | 约拍审核 |
| POST /manage/upRecom | POST /admin/ypat/recom | 上/取消推荐 |
| GET /manage/user/list | GET /admin/user/list | 实名列表 |
| GET /manage/user/detail | GET /admin/user/detail | 实名详情 |
| GET /manage/user/audit | POST /admin/user/audit | 实名审核 |
| GET /manage/product/list | GET /admin/product/list | 产品列表 |
| GET /manage/product/edit | GET /admin/product/detail | 产品详情 |
| POST /manage/product/save | POST /admin/product/save | 保存产品 |
| POST /manage/product/upDown | POST /admin/product/upDown | 产品上下架 |
| GET /article/list | GET /admin/article/list | 文章列表 |
| GET /article/edit | GET /admin/article/detail | 文章详情 |
| POST /article/save | POST /admin/article/save | 保存文章 |
| POST /article/upDown | POST /admin/article/upDown | 文章发布/撤回 |
| GET /banner/list | GET /admin/banner/list | 横幅列表 |
| GET /banner/edit | GET /admin/banner/detail | 横幅详情 |
| POST /banner/save | POST /admin/banner/save | 保存横幅 |
| POST /banner/upDown | POST /admin/banner/upDown | 横幅发布/撤回 |
| GET /manage/mess/list | GET /admin/mess/list | 消息列表 |
| GET /manage/order/list | GET /admin/order/list | 订单列表 |
| GET /pubevent/list | GET /admin/pubevent/list | 公众号关注统计 |
| POST /ypat/submit | POST /admin/ypat/submit | 发布作品 |
