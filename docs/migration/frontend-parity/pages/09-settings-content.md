# 页面：设置 / 关于 / 反馈 / 文章 / 协议 / 隐私 (Module I)

## 1. 身份
| 新版 | 旧版 | 映射 |
|---|---|---|
| pages-sub/user/settings | (旧散落 mine) | NEW(聚合) |
| pages-sub/user/about | mine/about | MAPPED |
| pages-sub/user/feedback | (无) | NEW |
| pages-sub/content/article | (帮助中心内容?) | NEW |
| pages-sub/content/agreement | login/agreement(注册协议) | MAPPED |
| pages-sub/content/privacy | (从 agreement 拆) | SPLIT |

## 2. 业务对照
| 维度 | 旧 | 新 | 结论 |
|---|---|---|---|
| 退出登录 | clearStorage + reLaunch | 弹窗确认→userStore.logout(clearAuth+switchTab home) ✅ | 一致 |
| 清缓存(保留登录) | logoutNotClear | clearStorageSync 后回写 ypat_token/ypat_user_info ✅ | 一致 |
| 手机号脱敏 | — | maskedPhone 3****后4 ✅ | 新增更好 |
| 修改手机号 | (旧不可改) | "暂不支持修改手机号" toast | P3,一致(不可改) |
| 实名入口 | — | goRealname ✅ | — |
| 关于 | about 静态 | about 静态 + 协议/隐私入口 ✅ | 一致 |
| 协议/隐私 | 静态 | getArticleDetail(1) 失败/空回退 staticContent ✅ | 一致(更稳:可后台配置+兜底) |
| 文章详情 | — | getArticleDetail(id) + 设标题 ✅ | 新增 |
| 意见反馈 | (无) | post /feedback/add | **GAP-I-01: 后端无该端点** |

## 3. 差异与处理
| 编号 | 级别 | 说明 | 处理 |
|---|---|---|---|
| GAP-I-01 | P2 | 反馈提交 /feedback/add 后端不存在,提交必失败 | 新功能无 parity 基线;诚实失败(toast),**不伪造成功**。建议: 后端新增 /feedback/add 或上线前下线该入口(产品/后端决策) |
| GAP-I-02 | P3 | 不支持修改手机号 | 与旧一致(手机号来自登录),记录 |

## 4. 修改
- 无代码改动(避免为缺失后端伪造成功;协议/隐私已有兜底)
- 仅文档

## 5. 验证
- type-check ✅ / test 50/50 ✅(沿用)
- 结论: 设置/关于/协议/隐私/文章与旧版一致或更稳;反馈待后端端点(GAP-I-01,P2,不阻塞核心流程)。
