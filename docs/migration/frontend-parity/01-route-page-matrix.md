# 01 路由与页面映射矩阵 (Route / Page Matrix)

> 基线 commit: `bd7d3f9` (= origin/main)
> 旧版: `91pai-master` (Vue2/Vuex/vue-cli) — 26 注册页面
> 新版: `frontend` (Vue3/TS/Pinia/Vite) — 6 主包 + 20 分包 = 26 路由
> 数据来源: 双方 `pages.json` 实际解析 + 子代理代码审计

## A. 旧版 → 新版 映射

映射状态: `MAPPED` 一对一 / `MERGED` 合并 / `SPLIT` 拆分 / `REPLACED` 被新流程替代 / `RETIRED` 废弃 / `MISSING` 新版缺失 / `UNKNOWN` 待确认

| # | 旧版路由 | 旧版含义 | 新版去向 | 状态 | 备注 |
|---|---|---|---|---|---|
| 1 | pages/home/home/index | 首页 | pages/home/index (+ pages/discover/index) | SPLIT | 首页+发现页拆分;旧版单首页 |
| 2 | pages/home/introduce/index | 完善信息(多步) | pages-sub/user/complete-info | MERGED | 旧多步(性别/职业/生日/地址)合并为单页 |
| 3 | pages/login/gender/index | 完善信息-性别 | pages-sub/user/complete-info | MERGED | 同上 |
| 4 | pages/login/login/index | 登录(选择provider) | pages/login/index | MAPPED | 新增 H5 手机号登录 |
| 5 | pages/login/logininfo/index | 登录-手机号(getPhoneNumber) | pages/login/index | MERGED | 合并到单登录页 |
| 6 | pages/login/agreement/index | 注册协议 | pages-sub/content/agreement (+ privacy) | SPLIT | 拆为用户协议/隐私政策 |
| 7 | pages/home/publish/index | 发布约拍 | pages/publish/index | MAPPED | |
| 8 | pages/home/success/index | 发布成功/审核中/实名结果轮询 | pages/publish/index (成功态) + pages-sub/user/realname(结果) | SPLIT | **需确认新版是否承载审核轮询** |
| 9 | pages/home/desc/index | 约拍详情 | pages-sub/ypat/detail | MAPPED | |
| 10 | pages/home/orderShe/index | 约拍她(报名) | pages-sub/ypat/detail(报名动作) | MERGED | 报名内联到详情;**需确认** |
| 11 | pages/home/linkway/index | 约拍请求(查看联系方式) | pages-sub/ypat/detail / message-detail(解锁) | MERGED | 联系方式解锁内联;**需确认** |
| 12 | pages/mine/mine/index | 个人中心 | pages/mine/index | MAPPED | |
| 13 | pages/mine/homepage/index | 个人主页(他人) | pages-sub/user/profile | MAPPED | |
| 14 | pages/mine/userInfo/index | 个人信息编辑 | pages-sub/user/edit-info | MAPPED | |
| 15 | pages/mine/message/index | 我的消息列表 | pages/message/index | MAPPED | 新版 message 为 tabBar |
| 16 | pages/mine/realname/index | 实名认证 | pages-sub/user/realname | MAPPED | |
| 17 | pages/mine/ppd/index | 我的拍拍豆(余额+充值) | pages-sub/user/wallet + recharge | SPLIT | 余额/充值拆分 |
| 18 | pages/mine/records/index | 收支记录 | pages-sub/user/records (+ bills) | SPLIT | records + bills |
| 19 | pages/mine/yplist/index | 我的约拍列表 | pages-sub/ypat/my-publish / my-apply / my-favorite | SPLIT | 三个列表页 |
| 20 | pages/mine/infoaudit/index | 发布信息审核 | pages-sub/ypat/my-publish (状态Tab) | MERGED | 审核态并入我的发布;**需确认** |
| 21 | pages/mine/about/index | 关于我们 | pages-sub/user/about | MAPPED | |
| 22 | pages/mine/credit/index | 信用担保(保证金充值) | — | **UNKNOWN** | 模块J审计;新版无对应页 |
| 23 | pages/mine/creditagreement/index | 保证金协议 | — | **UNKNOWN** | 模块J审计 |
| 24 | pages/mine/invitation/index | 邀请好友 | — | **UNKNOWN** | 模块J审计;旧版#ifndef H5 |
| 25 | pages/mine/invitationdesc/index | 邀请说明 | — | **UNKNOWN** | 模块J审计 |
| 26 | pages/mine/helpcenter/index | 帮助中心 | — | **UNKNOWN** | 模块J审计 |

## B. 新版页面来源

| 新版路由 | 来源旧页面 | 模块 |
|---|---|---|
| pages/home/index | home/home | B |
| pages/discover/index | home/home(拆分) **新增** | B |
| pages/message/index | mine/message | F |
| pages/publish/index | home/publish | D |
| pages/mine/index | mine/mine | I |
| pages/login/index | login/login + logininfo | A |
| pages-sub/ypat/detail | home/desc + orderShe + linkway | C |
| pages-sub/ypat/search | (新增) | B |
| pages-sub/ypat/my-publish | mine/yplist + infoaudit | E |
| pages-sub/ypat/my-apply | mine/yplist | E |
| pages-sub/ypat/my-favorite | mine/yplist | E |
| pages-sub/user/profile | mine/homepage | C |
| pages-sub/user/edit-info | mine/userInfo | A |
| pages-sub/user/complete-info | home/introduce + login/gender | A |
| pages-sub/user/realname | mine/realname + home/success(结果) | G |
| pages-sub/user/wallet | mine/ppd | H |
| pages-sub/user/recharge | mine/ppd(充值) | H |
| pages-sub/user/records | mine/records | H |
| pages-sub/user/bills | (新增,收支细分) | H |
| pages-sub/user/settings | (新增,旧版散落在mine) | I |
| pages-sub/user/about | mine/about | I |
| pages-sub/user/feedback | (新增) | I |
| pages-sub/content/article | (新增/帮助中心内容?) | I |
| pages-sub/content/agreement | login/agreement | I |
| pages-sub/content/privacy | (新增,从agreement拆分) | I |
| pages-sub/content/message-detail | mine/message(详情) + linkway | F |

## C. 关键差异速览

1. **discover 页入口**: 新版使用自定义 `KeepTabBar`，首页和消息空态也可进入 `/pages/discover/index`，GAP-NAV-01 已关闭。
2. **报名 / 联系方式解锁** 旧版是独立页面(orderShe/linkway),新版疑似内联到 detail/message-detail — 需逐一验证业务逻辑未丢失。
3. **信用担保 / 保证金** (credit/creditagreement) 旧版存在完整充值流程(order type=2, 199元),新版**当前无对应页面** — 模块J 必须给出结论(迁移/合并/产品下线)。
4. **邀请好友 / 帮助中心** 旧版 #ifndef H5,新版无 — 模块J。
5. 新版 26 路由数量与旧版相同纯属巧合,业务含义不同。
