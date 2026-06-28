# 02 路由与跳转矩阵 (Navigation Matrix)

> 扫描 frontend/src 中所有 uni.navigateTo/redirectTo/reLaunch/switchTab/navigateBack。
> TabBar 页(home/message/publish/mine)用 switchTab,其余用 navigateTo。

## A. TabBar 与方法正确性
| 目标 | 是否 TabBar | 应用方法 | 实际 | 结论 |
|---|---|---|---|---|
| pages/home/index | 是 | switchTab | logout/goHome 用 switchTab ✅ | 正确 |
| pages/message/index | 是 | switchTab | KeepTabBar ✅ | 正确 |
| pages/publish/index | 是 | switchTab | KeepTabBar ✅ | 正确 |
| pages/mine/index | 是 | switchTab | KeepTabBar ✅ | 正确 |
| pages/discover/index | **否** | navigateTo | home/message goDiscover navigateTo ✅ | 正确(discover 非 tab) |
| pages-sub/** | 否 | navigateTo | 各页 navigateTo ✅ | 正确 |

## B. 关键跳转
| 来源 | 操作 | 目标 | 参数 | 登录要求 | 返回 | 状态 |
|---|---|---|---|---|---|---|
| home | 搜索 | search | keyword? | 否 | back | ✅ |
| home | AI/发现 | discover | — | 否 | back | ✅ |
| home/discover/search | 卡片/标签 | ypat/detail | id | 否 | back | ✅ |
| home quickItem/discover chip | 风格 | search | keyword=风格 | 否 | back | ✅(search 命中风格→patstyle) |
| detail | 报名/收藏 | (动作) | — | 需登录(requireLogin→login) | — | ✅ |
| detail | 作者 | user/profile | id | 否 | back | ✅ |
| login | 成功(资料全) | navigateBack 或 switchTab home | — | — | 回跳原目标 | ✅(登录回跳) |
| login | 成功(资料缺) | redirectTo complete-info | — | — | — | ✅(首登门禁) |
| message received | 点击 | content/message-detail | id(消息id) | 需登录 | back | ✅(修复:原误指 ypat detail) |
| message sent | 点击 | ypat/detail | ypatid | 需登录 | back | ✅ item.id 为消息ID,item.ypatid 为约拍ID;缺失时补查 /mess/get |
| message-detail | 关联约拍 | ypat/detail | ypatid | — | back | ✅ |
| message-detail | 解锁联系方式 | (动作,3豆) | sendperid,messid | 需登录+余额 | — | ✅(费用修正) |
| publish | 未登录 | login | — | — | back | ✅ |
| publish | 资料缺(含wx) | edit-info | — | 需登录 | back | ✅(发布前置) |
| publish/detail | 余额不足 | recharge | — | 需登录 | back | ✅ |
| 请求拦截器 | 401/1001 刷新失败 | reLaunch login | — | — | 单飞防死循环 | ✅ |
| request showBusinessGuide | 1010 | realname | — | — | — | ✅ |
| request showBusinessGuide | 1009 | recharge | — | — | — | ✅ |
| mine 各项 | — | profile/edit-info/realname/wallet/records/bills/settings/about/feedback/my-* | — | 视项 | back | ✅ |
| recharge | 成功(服务端确认) | navigateBack | — | — | — | ✅ |

## C. 重点检查结论
- TabBar/navigateTo 用法正确,无错配。
- 登录回跳: 资料完整 navigateBack 回原目标;资料缺 redirect complete-info ✅。
- 详情 id 传递: home/discover/search/profile/message-received/message-detail 均正确;message sent tab 已确认使用 `ypatid`。
- 401 刷新单飞 + redirectToLogin 单飞,避免死循环 ✅。
- 死链: `constants/pages.ts` 中 INVITE 悬空路由已删除;邀请体系当前版本 RETIRED。
- H5 刷新参数: 详情/搜索/文章均 onLoad 读 query,H5 刷新 URL 保留参数 ✅。

## D. 本轮关闭项
- GAP-F-03 (P1): /my/ypat/rec|send/list 返回 MessInfo 已确认并修复;sent 使用 ypatid,received 使用消息 id。
