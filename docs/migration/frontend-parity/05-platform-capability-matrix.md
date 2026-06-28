# 05 跨端能力矩阵 (Platform Capability)

> 条件编译统计: #ifdef MP-WEIXIN×3, H5×4, APP-PLUS×2; #ifndef H5×1, MP-WEIXIN×1。
> mp-weixin appid: **wx37ee5a90fc7ecb21**(与旧版相同 → 同一小程序,见 Storage 兼容)。

| 能力 | MP-WEIXIN | H5 | APP | 实现/降级 | 结论 |
|---|---|---|---|---|---|
| 登录 | getPhoneNumber+uni.login(code/encryptedData/iv) | 手机号+短信(channel:2) | "暂未开放"卡片 | login/index.vue #ifdef 三分支 | ✅ 各端正确,H5 不伪造微信登录 |
| 微信支付 | uni.requestPayment | #ifndef MP-WEIXIN 降级(非微信端不展示支付) | 同 H5 | RechargePanel #ifdef MP-WEIXIN | ✅ H5 不调不存在的微信支付 |
| 图片转 base64 | getFileSystemManager readFile | fetch+FileReader | getFileSystemManager | file-base64 #ifdef H5 | ✅ 跨端一致(均带 dataURL 前缀) |
| 图片选择/预览 | chooseImage/previewImage | 同 | 同 | uni API 通用 | ✅ |
| OCR | /oauth/ocr(base64) | 同 | 同 | 走后端,无端依赖 | ✅ |
| 定位 | getSetting/authorize/getLocation | getSetting 失败→catch→全国 | 同 | home getLocation,5s 超时保护 | ⚠️ 未 #ifdef 包裹但优雅降级全国(GAP-STATE-01,P3);旧版本无 GPS |
| 分享 | onShareAppMessage(旧有) | 复制真实详情路径 | — | 页面级 onShareAppMessage + H5复制链接 | ✅ |
| 订阅消息 | requestSubscribeMessage(旧有) | — | — | 新版未请求 | GAP-D-04 P3(审核通知) |
| 安全区/自定义导航 | statusBarHeight/navBarHeight(appStore) | 同 | 同 | app store initApp 多 API 兜底 | ✅ |
| TabBar | 自定义 KeepTabBar + hideTabBar | 同 | 同 | onShow hideTabBar | ✅ |
| 下拉刷新/上拉 | onPullDownRefresh/onReachBottom | 同 | 同 | 各列表页 | ✅ |

## Storage 兼容(GAP-AUTH-02)
- **同 appid** → 旧用户升级后本地仍有 `UNI_LOCAL_token`/`UNI_LOCAL_userInfo`,但新版读 `ypat_token`/`ypat_user_info` → 视为未登录,**强制重新登录**。
- 结论: **ACCEPTED 且更安全**。旧 token 强制重登;本轮 `/user/token` 已改为有效当前 Token 续签,不再凭 mobile 重签。旧 key 残留无害(不清理亦可)。
- 若产品要求无感升级: 实现一次性幂等迁移(读 UNI_LOCAL_userInfo.mobile → /user/token 重签 → 写 ypat_*),并加版本标记。当前按强制重登处理(ACCEPTED)。

## 要求核对
- 微信能力仅 mp-weixin 启用 ✅ / H5 不伪造微信登录/支付 ✅ / App 不进入无法完成的授权(明确"暂未开放")✅ / 权限拒绝有降级(定位→全国)✅ / 安全区正确 ✅ / 分享参数含真实页面 id ✅。
