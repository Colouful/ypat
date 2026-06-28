# YPAT 仓库审计报告

## 1. 仓库模块结构

```
ypat/
├── backend/                  # 后端业务服务 (Spring Cloud)
│   ├── system-restapi/       # REST API 服务 (端口 9081) - 数据层+业务逻辑
│   ├── system-web/           # 管理后台 (端口 8082) - 后台管理界面
│   ├── system-wap/           # 移动端 API 网关 (端口 8081) - 面向前端
│   ├── system-sso/           # 单点登录 (端口 8000) - OAuth2 + JWT
│   ├── system-domain/        # 领域层 - 实体、仓储、服务
│   ├── system-object/        # DTO/QO 数据传输对象
│   └── system-security/      # 安全认证模块
├── backend-base/             # 基础设施服务
│   ├── base-eureka/          # 注册中心 (端口 8761)
│   ├── base-config/          # 配置中心 (端口 8888)
│   ├── base-zipkin/          # 链路追踪 (端口 9987)
│   ├── base-hystrix/         # 熔断监控 (端口 7979)
│   └── base-turbine/         # 熔断聚合 (端口 8989)
├── frontend/                 # 前端项目 (UniApp 3.0 空脚手架)
├── 91pai-master/             # 旧版前端 (Vue 2 + UniApp 2.0, 仅参考)
└── design-references/        # UI 设计参考
```

## 2. 后端服务职责

| 服务 | 端口 | 职责 |
|------|------|------|
| system-wap | 8081 | 移动端 API 网关，转发请求到 system-restapi |
| system-web | 8082 | 管理后台，含登录拦截、文件上传、图片水印 |
| system-restapi | 9081 | REST API，直接操作数据库，提供 /service/ 前缀接口 |
| system-sso | 8000 | OAuth2 认证服务，JWT 令牌，Zuul 网关代理 |
| system-domain | - | 领域层 JAR，JPA 实体、Repository、Service |
| system-object | - | DTO 层 JAR，QO 对象定义 |

## 3. 前端当前完成度

当前 `frontend/` 目录为 UniApp 3.0 + Vue 3 + TypeScript + Vite 空脚手架：
- 仅有 1 个 Hello World 页面 (`pages/index/index.vue`)
- 无业务代码、无路由配置、无状态管理、无网络请求
- 依赖已配置：`@dcloudio/uni-app 3.0.0-4080420251103001`, `vue ^3.4.21`
- TypeScript 已配置但无业务类型定义

## 4. API 网关和接口前缀

前端请求流向：
```
前端 → system-wap (8081) → [Feign Client] → system-restapi (9081)
```

system-wap 对外暴露的接口前缀：
- 用户：`/user/`
- 约拍：`/ypat/`
- 我的约拍：`/my/ypat/`
- 订单：`/order/`
- 账单：`/bill/`
- 实名认证：`/oauth/`
- 产品：`/product/`
- 文章：`/article/`
- Banner：`/banner/`
- 消息：`/mess/`
- 记录：`/record/`
- 区域：`/area/`
- 参数：`/param/`
- 模板ID：`/tmplid/`
- 二维码：`/qr/`
- 百度登录：`/bd/`
- 微信支付回调：`/wxpay/notify`

生产域名：`https://www.91qupaier.com`

## 5. 登录认证方式

### 微信小程序登录流程
1. 前端调用 `wx.login()` 获取 code
2. 前端请求 `GET /user/code?code=xxx` 换取 openid + session_key
3. 前端请求 `POST /user/login` 提交用户信息（encryptedData, sessionKey, iv）
4. 后端解密获取 unionId/openId，查找或创建用户
5. 后端生成 JWT Token 返回

### 百度小程序登录流程
1. 前端获取百度 code
2. 前端请求 `POST /bd/code` 换取 openid
3. 前端请求 `POST /bd/login` 完成登录

## 6. Token 获取与刷新方式

- Token 格式：JWT (Header: `Token`)
- Token 前缀：`Tgbnhy`
- 有效期：432000 毫秒 (5天)
- 密钥：硬编码在 `Const.java`
- 刷新方式：`GET /user/token` 获取新 Token
- 存储方式：前端 localStorage (`UNI_LOCAL_token`)

## 7. 用户角色

| 编码 | 角色 |
|------|------|
| 0 | 摄影师 |
| 1 | 模特 |
| 2 | 妆造师 |
| 3 | 修图师 |
| 4 | 个人 |
| 5 | 演员 |
| 6 | 商家 |
| 7 | 其他 |

## 8. 用户状态

| 编码 | 状态 | 说明 |
|------|------|------|
| 0 | 正常 | 已注册未认证 |
| 1 | 待审核 | 已提交实名认证 |
| 2 | 审核通过 | 实名认证通过 |
| 3 | 审核不通过 | 实名认证被拒 |
| 9 | 已缴纳保证金 | 信用认证完成 |

## 9. 约拍状态

| 编码 | 状态 | 说明 |
|------|------|------|
| zc | 暂存/草稿 | 未提交 |
| ytj | 已提交 | 等待审核 |
| shtg | 审核通过 | 已发布 |
| shbtg | 审核不通过 | 被拒绝 |

## 10. 订单状态

| 编码 | 状态 |
|------|------|
| 0 | 已支付 |
| 1 | 未支付 |

订单类型：
| 编码 | 类型 |
|------|------|
| 0 | 拍拍豆充值 |
| 1 | 实名认证充值 |
| 2 | 保证金充值 |

## 11. 支付流程

```
1. 用户选择充值套餐 (Product)
2. 前端调用 POST /order/create 创建订单
3. 后端返回 prepay_id 等微信支付参数
4. 前端调用 wx.requestPayment() 发起支付
5. 微信回调 POST /wxpay/notify 通知后端
6. 后端验证签名，更新订单状态
7. 根据订单类型处理：
   - type=0: 增加拍拍豆
   - type=1: 更新实名认证状态
   - type=2: 更新保证金状态
```

微信支付配置：
- AppID: wx94b432e0db7c29be
- 商户号: 1561830771
- 支付密钥: 硬编码在 WXConfig.java

## 12. 拍拍豆规则

| 操作 | 拍拍豆变动 |
|------|-----------|
| 充值 | +N (根据套餐) |
| 好友邀请 | +N |
| 系统赠送 | +N |
| 发布约拍 | -3 |
| 申请约拍 | -1 |
| 查看联系方式 | -1 |

Record type 编码：
- 0: 充值
- 1: 好友邀请
- 2: 系统赠送
- 3: 发布约拍
- 4: 申请约拍
- 5: 查看联系方式

## 13. 消息类型

| 类型 | 说明 | 模板ID |
|------|------|--------|
| audit | 发布信息审核结果 | TEMP_2 |
| oauth | 实名认证审核结果 | TEMP_1 |
| rec | 收到约拍申请 | TEMP_0 |
| order | 新订单通知 | TEMP_3 |

消息字段：
- messviewflag: 消息是否已查看
- linkwayflag: 联系方式是否已查看

## 14. 图片上传流程

1. 前端选择图片 (最多9张)
2. 前端调用上传接口
3. 后端接收 MultipartFile
4. 后端通过 FastDFSClient 上传到 FastDFS
5. 返回文件路径 (前缀: `https://www.91qupaier.com/`)

## 15. 图片水印流程

1. 约拍图片上传时自动加水印
2. 水印文字: "91去拍"
3. 字体: 微软雅黑, 粗体, 50px
4. 颜色: 白色, 透明度 0.6
5. 位置: (10, 10)
6. 处理流程: InputStream → ImageMarkUtil.waterMake() → FastDFS

## 16. 实名认证流程

1. 用户上传身份证正反面照片
2. 调用 `POST /oauth/ocr` 进行 OCR 识别 (百度AI)
3. 识别结果展示给用户确认
4. 用户确认姓名和身份证号
5. 调用 `POST /oauth/add` 提交认证 (userid, name, certcode, pics)
6. 后端保存认证信息，更新用户状态为"待审核"
7. 管理员在后台审核
8. 审核结果通过微信模板消息通知用户

## 17. OCR 流程

- 使用百度 AI OCR 服务
- 配置: SystemConfig (bd_api, bd_ak, bd_sk)
- 接口: `POST /oauth/ocr` 上传身份证图片
- 返回: 识别的姓名和身份证号
- 错误码: FAIL_OCR(1008), FAIL_LIMIT(1014)

## 18. 当前已知技术债

1. **Spring Boot 1.5.9** - 已严重过时，存在安全漏洞
2. **Spring Cloud Edgware** - 已停止维护
3. **Hystrix** - 已弃用，应迁移到 Resilience4j
4. **Eureka 单点** - 无高可用配置
5. **JWT 密钥硬编码** - 安全风险
6. **微信配置硬编码** - AppSecret、支付密钥直接写在代码中
7. **Druid 监控弱密码** - 默认密码 12345678
8. **无数据库版本管理** - 依赖 JPA DDL auto-update
9. **FastDFS 无 CDN** - 直接暴露存储服务
10. **前后端未分离** - system-web 包含 Thymeleaf 模板

## 19. 当前已知安全风险

1. **WXConfig 硬编码密钥** - AppSecret、支付密钥在源码中
2. **JWT Secret 硬编码** - 可被逆向
3. **Druid 管理后台暴露** - 默认账号密码
4. **Token 有效期过长** - 5天无强制刷新
5. **无请求频率限制** - 可被暴力攻击
6. **生产数据库密码在配置文件中** - <历史生产数据库密码，已脱敏>
7. **无 HTTPS 强制** - 明文传输风险
8. **身份证信息无加密存储** - certcode 明文
9. **CustomUserDetailsService 硬编码** - 固定密码 123456

## 20. 当前已知接口风险

1. **分页无上限** - size 参数无最大限制
2. **无接口签名** - 请求可被伪造
3. **联系方式扣费无并发控制** - 可能重复扣费
4. **订单创建无幂等** - 重复请求创建多个订单
5. **支付回调无重放保护** - 可能重复到账

## 21. 当前已知上线风险

1. 微信小程序 AppID 需要确认是否仍有效
2. 微信支付商户号需要确认状态
3. 百度 OCR 配额需要确认
4. FastDFS 服务器需要确认可用性
5. 生产域名 91qupaier.com 需要确认 SSL 证书
6. 微信审核需要隐私协议、用户协议页面
7. 小程序包体积需要控制在限制内
