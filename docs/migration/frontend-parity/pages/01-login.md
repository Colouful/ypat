# 页面：登录 (Module A)

## 1. 页面身份
- 新版路由：pages/login/index
- 新版文件：frontend/src/pages/login/index.vue + src/stores/user.ts
- 对应旧版：pages/login/login/index + pages/login/logininfo/index (+ getNextUrl.js)
- 映射类型：MERGED (旧两页合并为单页)
- 模块：A

## 2. 页面入口
- 进入：未登录拦截(request.ts redirectToLogin reLaunch)、个人中心/各需登录动作
- 登录要求：本页即登录
- 平台：#ifdef MP-WEIXIN(微信一键登录 getPhoneNumber) / #ifdef H5(手机号+短信) / #ifdef APP-PLUS(暂未开放卡片)

## 3. 出口
- 成功后：getCurrentPages>1 → navigateBack;否则 switchTab home
- 协议/隐私：navigateTo content/agreement, content/privacy

## 4. 平台能力对比
| 能力 | 旧版 | 新版 | 结论 |
|---|---|---|---|
| 微信登录 | uni.login→/user/code→getPhoneNumber encryptedData→/user/login | getPhoneNumber→uni.login(code)→/user/login(code,encryptedData,iv,channel:0) | ✅ 页面正确;缺 encryptedData 有明确提示 |
| H5 手机号 | 无 | /user/sms/code + /user/login(channel:2) | 新增 ✅ |
| App | 无 | 明确"暂未开放"卡片 | ✅ 降级正确 |
| 协议勾选 | 有 | agreed 勾选,未勾选 toast 拦截 | ✅ |

## 5. 接口
| 场景 | 旧 | 新 | 方法 | 状态 |
|---|---|---|---|---|
| 微信登录 | /user/code + /user/login | /user/login(code,encryptedData,iv) | POST form | ✅(后端 UserQo 接受) |
| 发短信 | — | /user/sms/code | POST form mobile | ✅ |
| 手机登录 | — | /user/login(mobile,smsCode,channel:2) | POST form | ✅ |
| 拉资料 | user_get | /user/get?id | GET | ✅ |

## 6. 差异与处理
| 编号 | 类型 | 级别 | 旧 | 新 | 处理 |
|---|---|---|---|---|---|
| GAP-AUTH-01 | 安全 | P0 | — | login() 硬编码测试账号 | **已修复**: 生产透传真实凭证,测试账号仅 dev;store 回归测试 |
| GAP-AUTH-03 | 流程 | P1 | getNextUrl 强制完善资料 | complete-info 无人跳转(死页) | **已修复**: utils/profile.isProfileComplete + 登录成功后不完整 redirectTo complete-info;加单测 |
| GAP-A-EDIT-01 | 资料 | P2 | userInfo 可编辑 wx | edit-info 无 wx 字段 | **FIXED**: edit-info 已补微信号字段 |
| GAP-AUTH-02 | 缓存 | P2 | UNI_LOCAL_token | ypat_token | 暂定强制重登(更安全),见 04 |

## 7. 修改
- frontend/src/stores/user.ts: login() 真实凭证透传 + applyLoginResult 抽取 + envConfig dev 门禁
- frontend/src/api/modules/ypat.ts: 列表 GET(GAP-API-01)
- frontend/src/utils/profile.ts: isProfileComplete 登录完善门禁(对齐 getNextUrl)
- frontend/src/pages/login/index.vue: redirectAfterLogin — 不完整 redirectTo complete-info,完整保留回跳
- 新增测试: user-login.test.ts(3) + api-contracts ypat 契约(2) + profile.test.ts(7)
- 未改视觉: login/index.vue 模板/样式零改动(仅 script 回跳逻辑)

## 8. 验证
- type-check ✅ / test 42/42 ✅ / lint ✅(0 error)
- 微信/H5 真机联调: 待后端环境(记录,非本次静态可完成)
- 结论: P0(GAP-AUTH-01)、P1(GAP-AUTH-03) 已清除

## 9. complete-info / edit-info 审计
- complete-info: 收集 gender/profess/birthday/region,字段对齐旧 introduce 多步;保留"暂时跳过"(新 UX),发布另有前置门禁兜底 → 可接受
- edit-info: nickname/gender/profess/birthday/region/avatar(base64 走 /user/upd pics)。**缺 wx(微信号)**编辑 → GAP-A-EDIT-01,Module D 处理
- 结论: Module A 主体完成;edit-info 的 wx 字段在 Module D 随发布前置门禁补齐
