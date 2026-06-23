# YPAT（爱去拍）- 摄影约拍服务平台

## 项目简介

YPAT（爱去拍）是一个面向摄影师、模特、妆造师、修图师等摄影行业从业者的约拍撮合平台，提供发布约拍、申请约拍、实名认证、消息通知、支付充值等核心功能。

## 项目结构

```
ypat/
├── README.md           # 项目说明文档
├── backend/            # 后端服务（Spring Cloud）
│   ├── system-wap/     # 移动端API服务
│   ├── system-web/     # 管理后台服务
│   ├── system-restapi/ # REST API服务
│   ├── system-sso/     # 单点登录服务
│   ├── system-domain/  # 领域层（实体、服务）
│   ├── system-object/  # 数据传输对象
│   └── system-security/# 安全认证模块
├── backend-base/       # 基础设施服务
│   ├── base-eureka/    # 注册中心
│   ├── base-config/    # 配置中心
│   ├── base-zipkin/    # 链路追踪
│   ├── base-hystrix/   # 熔断监控
│   └── base-turbine/   # 熔断聚合
└── frontend/           # 前端项目（UniApp）
    ├── src/            # 源代码
    ├── package.json    # 依赖配置
    └── vite.config.ts  # Vite配置
```

## 技术栈

### 后端
- **框架**: Spring Boot + Spring Cloud
- **语言**: Java 8+
- **ORM**: JPA/Hibernate
- **数据库**: MySQL
- **缓存**: Redis
- **文件存储**: FastDFS
- **消息推送**: 微信模板消息
- **支付**: 微信支付
- **AI**: 百度AI（OCR识别）

### 前端
- **框架**: UniApp (Vue 3 + TypeScript)
- **构建工具**: Vite
- **目标平台**: 微信小程序、H5、APP

## 核心功能

### 用户模块
- 微信/百度授权登录
- 实名认证（身份证OCR）
- 用户资料管理
- 拍拍豆虚拟货币系统

### 约拍模块
- 发布约拍（含图片水印）
- 申请约拍
- 收藏约拍
- 约拍审核机制

### 消息模块
- 约拍消息通知
- 审核结果通知
- 微信模板消息推送

### 支付模块
- 微信支付充值
- 拍拍豆消费记录
- 账单管理

### 内容模块
- 文章管理
- Banner管理
- 约拍列表展示

## 快速开始

### 环境要求
- JDK 8+
- Maven 3.6+
- MySQL 5.7+
- Redis 5.0+
- Node.js 16+
- npm/yarn/pnpm

### 后端启动
```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE ypat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 修改配置文件
# 编辑 backend-base/base-config/src/main/resources/application.yml
# 配置数据库连接、Redis连接、微信小程序配置等

# 3. 启动基础服务
cd backend-base
mvn spring-boot:run -pl base-eureka
mvn spring-boot:run -pl base-config

# 4. 启动业务服务
cd backend
mvn spring-boot:run -pl system-sso
mvn spring-boot:run -pl system-wap
```

### 前端启动
```bash
cd frontend

# 安装依赖
npm install

# 开发模式（微信小程序）
npm run dev:mp-weixin

# 开发模式（H5）
npm run dev:h5

# 构建微信小程序
npm run build:mp-weixin

# 构建H5
npm run build:h5
```

## API接口文档

### 用户相关
| 接口 | 方法 | 说明 |
|------|------|------|
| `/user/code` | GET | 微信授权code换取session |
| `/user/login` | POST | 用户登录 |
| `/user/token` | GET | 获取/刷新Token |
| `/user/get` | GET | 获取用户信息 |
| `/user/upd` | POST | 修改用户信息 |

### 约拍相关
| 接口 | 方法 | 说明 |
|------|------|------|
| `/ypat/get` | GET | 获取约拍详情 |
| `/ypat/submit` | POST | 提交约拍申请 |
| `/ypat/tc/list` | GET | 推荐约拍列表 |
| `/my/ypat/rec/add` | POST | 发起约拍申请 |
| `/my/ypat/sc/add` | POST | 收藏约拍 |

### 支付相关
| 接口 | 方法 | 说明 |
|------|------|------|
| `/order/create` | POST | 创建支付订单 |
| `/wxpay/notify` | POST | 微信支付回调 |
| `/bill/findPage` | POST | 账单列表 |

### 实名认证
| 接口 | 方法 | 说明 |
|------|------|------|
| `/oauth/ocr` | POST | 身份证OCR识别 |
| `/oauth/add` | POST | 提交实名认证 |
| `/oauth/audit` | POST | 审核实名认证 |

## 数据库表

| 表名 | 说明 |
|------|------|
| t_user | 用户表 |
| t_ypat_info | 约拍信息表 |
| t_mess_info | 消息表 |
| t_order | 订单表 |
| t_bill | 账单表 |
| t_record | 积分记录表 |
| t_product | 产品表 |
| t_article | 文章表 |
| t_banner | Banner表 |
| t_ypat_img | 约拍图片表 |
| t_user_img | 用户图片表 |
| t_user_ypat | 用户约拍关联表 |
| t_user_orig | 用户来源表 |
| t_pub_event | 公众号事件表 |

## 业务流程

### 发布约拍流程
```
用户填写约拍信息 → 上传图片（自动加水印） → 提交审核 
→ 扣除拍拍豆（3个） → 管理员审核 → 微信通知用户
```

### 申请约拍流程
```
用户浏览约拍列表 → 选择感兴趣的约拍 → 发起申请
→ 扣除拍拍豆（1个） → 微信通知发布者
```

### 充值流程
```
选择充值金额 → 创建订单 → 微信支付 → 支付回调 
→ 拍拍豆到账
```

## 部署说明

### Docker部署（推荐）
```bash
# 构建后端镜像
docker build -t ypat-backend ./backend

# 启动服务
docker-compose up -d
```

### 传统部署
```bash
# 打包后端
cd backend
mvn clean package -DskipTests

# 部署到服务器
scp target/*.jar user@server:/opt/ypat/
```

## 开发规范

### 代码规范
- 后端遵循阿里巴巴Java开发手册
- 前端遵循Vue官方风格指南
- 使用ESLint + Prettier格式化代码

### Git规范
- 分支命名: `feature/xxx`, `fix/xxx`, `hotfix/xxx`
- 提交信息: `feat: 新增功能`, `fix: 修复bug`, `docs: 更新文档`

## 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/xxx`)
3. 提交更改 (`git commit -m 'feat: 新增xxx功能'`)
4. 推送到分支 (`git push origin feature/xxx`)
5. 创建 Pull Request

## 许可证

MIT License

## 联系方式

- 项目地址: https://github.com/Colouful/ypat
- 问题反馈: Issues

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 用户模块（登录、注册、实名认证）
- 约拍模块（发布、申请、收藏）
- 支付模块（微信支付、拍拍豆）
- 消息模块（微信模板消息）
