# YPAT（爱去拍）— 产品目录说明书

> 本文件面向 AI Agent 和新加入的开发者，快速了解项目每个目录的职责与状态。

---

## 项目总览

YPAT（爱去拍）是一个摄影约拍撮合平台，摄影师/模特/妆造师/修图师在平台上互相发现、发起约拍、完成交易。

**后端**: Spring Cloud 微服务 (Java 8+)  
**前端**: UniApp (Vue 3 + TypeScript + Vite)  
**目标平台**: 微信小程序 → H5 → Android/iOS

---

## 目录结构与说明

```
ypat-workspace/
├── PRODUCT.md                  ← 你在这里
├── PROJECT.md                  ← 当前开发焦点与阻塞项
├── IMPROVEMENT.md              ← 踩坑记录与历史教训
├── README.md                   ← 项目总说明（API/数据库/部署）
├── 后端项目入门指南.md           ← 后端零基础入门文档
│
├── backend/                    ⚡ 后端业务服务（Spring Cloud）
│   ├── system-wap/             # 移动端 API 服务（小程序/H5 调用）
│   ├── system-web/             # 管理后台服务（PC 端运营后台）
│   ├── system-restapi/         # REST API 服务
│   ├── system-sso/             # 单点登录服务（微信/百度授权）
│   ├── system-domain/          # 领域层（实体、Service、Repository）
│   ├── system-object/          # 数据传输对象（DTO/VO）
│   └── system-security/        # 安全认证模块（JWT/Token）
│
├── backend-base/               ⚡ 基础设施服务（Spring Cloud 组件）
│   ├── base-eureka/            # 注册中心（服务发现）
│   ├── base-config/            # 配置中心（统一配置管理）
│   ├── base-zipkin/            # 链路追踪（分布式调用链）
│   ├── base-hystrix/           # 熔断监控（服务降级）
│   └── base-turbine/           # 熔断聚合（集群监控）
│
├── frontend/                   🆕 当前前端项目（UniApp Vue 3 + TS）
│   ├── src/                    # 源代码（目前仅有脚手架骨架）
│   │   ├── pages/              # 页面目录（待开发）
│   │   ├── static/             # 静态资源
│   │   ├── App.vue             # 根组件
│   │   ├── main.ts             # 入口文件
│   │   ├── pages.json          # 页面路由配置
│   │   └── uni.scss            # 全局样式变量
│   ├── package.json            # 依赖：Vue 3 + UniApp 3.0 + Vite
│   └── vite.config.ts          # Vite 构建配置
│
├── 91pai-master/               📦 旧版前端代码（仅作业务逻辑参考）
│   ├── pages/                  # 旧页面目录
│   │   ├── home/               # 首页模块：home/desc/publish/success/linkway/orderShe/introduce/request
│   │   ├── mine/               # 我的模块：mine/userInfo/message/ppd/records/realname/credit/about/...
│   │   └── login/              # 登录模块：login/gender/agreement/address/birthday/profess/logininfo
│   ├── common/                 # 旧公共工具：request.js/utils.js/image.js/city.data.js 等
│   ├── components/             # 旧公共组件
│   ├── store/                  # Vuex 状态管理
│   ├── static/                 # 静态资源（图标/图片）
│   ├── pages.json              # 旧路由配置（含全部页面路径）
│   ├── manifest.json           # 应用配置
│   └── package.json            # 依赖：Vue 2 + UniApp 2.0（已废弃）
│
├── design-references/          🎨 UI 设计参考
│   ├── aiqupai-ui-keep.html    # ★ 新 UI 风格模版（Keep 风格，绿色主调）
│   ├── ypat-luxury-dark-glass-reference.html   # 旧参考：暗色玻璃拟态
│   ├── ypat-luxury-dark-glass-reference.png    # 旧参考截图
│   └── ypat-glass-dark-ui-example.png          # 旧参考截图
│
├── .omc/                       🤖 OpenCode 配置与会话
└── .omx/                       🤖 OpenCode 日志与状态
```

---

## 重点区分

### 业务服务 (后端)
| 目录 | 类型 | 说明 |
|------|------|------|
| `backend/` | 业务服务 | 7 个微服务模块，处理具体业务逻辑 |
| `backend-base/` | 基础设施 | 5 个 Spring Cloud 组件，提供注册/配置/监控 |

### 前端代码
| 目录 | 状态 | 说明 |
|------|------|------|
| `frontend/` | 🆕 当前开发 | UniApp 3.0 + Vue 3 + TypeScript + Vite，脚手架阶段 |
| `91pai-master/` | 📦 已废弃 | UniApp 2.0 + Vue 2，2020 年代码，仅作业务逻辑和页面结构参考 |

### 旧版前端页面清单 (91pai-master，参考用)

**首页模块 (home/)**
- home — 首页列表（约拍瀑布流）
- desc — 约拍详情页
- publish — 发布约拍
- success — 发布成功
- linkway — 约拍请求/联系方式
- orderShe — 约拍她
- introduce — 完善信息引导
- request — 请求页面

**我的模块 (mine/)**
- mine — 个人中心首页
- userInfo — 个人信息编辑
- homepage — 个人主页（他人可见）
- message — 消息列表
- ppd — 我的拍拍豆（虚拟货币）
- records — 收支记录
- realname — 实名认证
- credit / creditagreement — 信用担保/保证金协议
- infoaudit — 发布信息审核
- yplist — 约拍列表管理
- invitation / invitationdesc — 邀请好友
- helpcenter — 帮助中心
- about — 关于我们

**登录模块 (login/)**
- login — 登录页
- logininfo — 登录信息补充
- gender — 性别选择
- agreement — 注册协议
- birthday — 生日选择
- address — 地址选择
- profess — 职业选择

---

## 设计风格参考

**新 UI 风格 (Keep 风格)**: `design-references/aiqupai-ui-keep.html`

核心设计语言：
- 主色: #23C268 (Keep 绿)
- 白底清爽风格
- 细线图标 (stroke icons)
- 圆角卡片 (16px radius)
- 瀑布流列表布局
- 底部 Tabbar: 首页 / 约拍 / + / 消息 / 我的
- 字体: -apple-system, PingFang SC
- 5 个核心页面: 首页、约拍详情、发布、消息、我的

**旧参考 (暗色玻璃拟态)**: `design-references/ypat-luxury-dark-glass-*` (已弃用)
