# 页面：约拍详情 / 用户主页 (Module C)

## 1. 身份
| 新版 | 旧版 | 映射 |
|---|---|---|
| pages-sub/ypat/detail → YpatDetailView.vue | pages/home/desc + orderShe(报名) + linkway(联系方式) | MERGED |
| pages-sub/user/profile | pages/mine/homepage | MAPPED |

## 2. 详情页业务对照
| 维度 | 旧 | 新 | 结论 |
|---|---|---|---|
| 加载详情 | ypat_get(id) | getDetail(id) GET ✅ | 一致 |
| 浏览量+1 | ypat_yd_add | put /ypat/yd/add(showError:false) ✅ | 一致 |
| 图片轮播/预览 | swiper | images + uni.previewImage ✅ | 一致 |
| 收藏(只增) | my_ypat_sc_add,server dedupe 1006 | addFavorite,favorited 守卫+登录守卫 ✅ | 一致(均只增) |
| 报名前置(实名/保证金/余额/重复) | orderShe 客户端预检 + 服务端 | **服务端强制** + request.ts showBusinessGuide(1010→实名,1009→充值,1011→保证金,1006→已报名) | 等价且更稳健 ✅ |
| 报名理由 ≥6字 | orderShe content.length<6 拦截(用户填写) | **已修复**: uni.showModal editable 收集理由 + ≥6字校验 | 一致 |
| 自己发布判断 | — | userid===自己 → 拦截 ✅ | 新增更稳 |
| 登录判断 | token 检查→登录 | requireLogin→login ✅ | 一致 |

## 3. 用户主页对照
| 维度 | 旧 homepage | 新 profile | 结论 |
|---|---|---|---|
| 用户资料 | user_get | getUserInfo(id) ✅ | 一致 |
| 过往作品 | head/pub list | getMyPublishList({userid}) GET ✅ | 一致(GET 修复后) |
| 分页 | 有 | number/totalPages 分页 ✅ | 一致 |
| 自己/他人 | — | isOwnProfile ✅ | 新增 |
| 详情跳转 | navigateTo desc | navigateTo detail?id ✅ | 一致 |

## 4. 差异与处理
| 编号 | 级别 | 说明 | 处理 |
|---|---|---|---|
| GAP-C-01 | P2 | 报名理由被硬编码,丢失用户自填 | **已修复**: editable modal + ≥6字 |
| GAP-C-02 | P3 | share() 仅 toast,无真实 onShareAppMessage | 已新增页面级 onShareAppMessage,H5 复制真实链接,状态 FIXED |
| GAP-C-03 | P3 | "私信"按钮 stub(showToast 打开私信);"关注"本地切换无后端 | 已移除假关注,私信改真实主页入口,状态 FIXED |

## 5. 修改
- frontend/src/components/business/YpatDetailView.vue: apply() 增加理由输入(editable modal)+≥6字校验,移除硬编码 content
- 未改视觉(仅 script;弹窗为原生 modal)

## 6. 验证
- type-check ✅ / test 42/42 ✅
- 结论: 详情+主页核心业务与旧版一致;报名前置由服务端+引导承载;无 P0/P1/P2 遗留。联系方式解锁在 Module F(message-detail)核验。
