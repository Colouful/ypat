# 91pai-master（旧版用户端代码）

> ⚠️ **此代码已废弃，仅作参考保留**

## 说明

这是「爱去拍」(91pai) 旧版用户端的前端代码，基于 uni-app 开发。

### 技术栈
- 框架：uni-app (Vue 2)
- UI 组件：Thor UI
- 状态管理：Vuex
- 样式：SCSS

### 主要功能模块
- 首页（发布、下单、邀请等）
- 登录注册（手机号、个人信息填写）
- 个人中心（信用分、实名认证、消息等）

### 为什么废弃
1. 代码结构混乱，缺乏规范
2. Vue 2 已停止维护
3. 组件复用性差，耦合度高
4. 没有 TypeScript，类型安全缺失

### 保留目的
- 参考原有业务逻辑和页面结构
- 对比新旧实现差异
- 复用部分设计素材（static/images/）

## 本地预览旧版小程序

旧版小程序的接口已切到本地 `wap(用户端接口服务)`：`http://127.0.0.1:8081`。

1. 在仓库根目录启动本地后端：

   ```bash
   docker compose up -d mysql redis eureka restapi wap
   ```

   微信登录需要在根目录 `.env` 中配置 `YPAT_WX_APP_ID(微信小程序 AppID)` 和 `YPAT_WX_APP_SECRET(微信小程序密钥)`。AppID 已默认使用当前旧版小程序的 `wx37ee5a90fc7ecb21`，AppSecret 需要从微信公众平台获取后填入本地 `.env`。

2. 在旧版小程序目录安装依赖并编译微信小程序：

   ```bash
   cd 91pai-master
   npm install
   npm run dev:mp-weixin
   ```

3. 用微信开发者工具打开 `91pai-master`，项目配置里的 `miniprogramRoot(小程序根目录)` 已指向 `dist/dev/mp-weixin/`。

4. 如果还看到“request 合法域名校验出错”，请在微信开发者工具里确认本项目已开启“不校验合法域名、web-view 域名、TLS 版本以及 HTTPS 证书”。

### 主要页面

- 首页：`pages/home/home/index`
- 约拍详情：`pages/home/desc/index`
- 发布约拍：`pages/home/publish/index`
- 约拍请求：`pages/home/linkway/index`
- 约拍她：`pages/home/orderShe/index`
- 我的：`pages/mine/mine/index`
- 个人主页：`pages/mine/homepage/index`
- 个人信息：`pages/mine/userInfo/index`
- 我的消息列表：`pages/mine/message/index`
- 发布信息审核：`pages/mine/infoaudit/index`
- 爱去拍列表：`pages/mine/yplist/index`
- 实名认证：`pages/mine/realname/index`
- 信用担保：`pages/mine/credit/index`
- 我的拍拍豆：`pages/mine/ppd/index`
- 收支记录：`pages/mine/records/index`
- 邀请好友：`pages/mine/invitation/index`
- 帮助中心：`pages/mine/helpcenter/index`
- 登录：`pages/login/login/index`
- 登录信息：`pages/login/logininfo/index`
- 完善资料：`pages/login/gender/index`
- 注册协议：`pages/login/agreement/index`

### 重做计划
新版用户端将基于：
- Vue 3 + TypeScript
- 更规范的组件设计
- 更好的状态管理方案

---

> 此目录不会被新项目引用，仅作为历史参考存档。
