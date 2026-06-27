# 页面：我的发布 / 我的申请 / 我的收藏 (Module E)

## 1. 身份
| 新版 | 旧版 | 映射 |
|---|---|---|
| pages-sub/ypat/my-publish | mine/yplist + mine/infoaudit(审核态) | MERGED |
| pages-sub/ypat/my-apply | mine/yplist(我报名的) | SPLIT |
| pages-sub/ypat/my-favorite | mine/yplist(收藏) | SPLIT |

## 2. 业务对照
| 维度 | my-publish | my-apply | my-favorite |
|---|---|---|---|
| 接口 | getMyPublishList GET ✅ | getMySentList(/send/list, 我报名的) GET ✅ | getMyFavoriteList GET ✅ |
| 分页 | number/totalPages ✅ | ✅ | ✅ |
| 下拉刷新 | onPullDownRefresh ✅ | ✅ | ✅ |
| 上拉加载 | onReachBottom ✅ | ✅ | ✅ |
| Loading | "加载中..." ✅ | ✅ | ✅ |
| Empty | "暂无发布记录" ✅ | "暂无报名记录" ✅ | "暂无收藏" ✅ |
| Error | toast ✅ | ✅ | ✅ |
| 没有更多 | footer ✅ | ✅ | ✅ |
| 审核状态 | YPAT_STATUS_LABELS(1审核中/2通过/3驳回)+色class ✅ | — | — |
| 详情回跳 | detail?id ✅ | ✅ | ✅ |
| onShow 刷新 | load(true) ✅ | ✅ | ✅ |

> **GAP-API-01 修复(列表 POST→GET)是这三页能正常工作的前提**,否则全部 405。

## 3. 关键结论
- **infoaudit(发布信息审核)已合并**进 my-publish 的状态标签(审核中/通过/驳回),路由映射#20 关闭 ✅。
- **删除/取消/编辑/取消收藏**: 后端 YpatInfoController/MypatInfoController **无** delete/cancel/un-favorite 端点(grep 确认);收藏仅 sc/add(只增,服务端 1006 去重)。新版正确地不提供这些动作 → 与旧版一致(旧版亦无取消收藏)。GAP-FAV-01 关闭 ✅。
- "我的申请"= send list(我报名别人)= getMySentList,映射正确。

## 4. 差异与处理
| 编号 | 级别 | 说明 | 处理 |
|---|---|---|---|
| GAP-E-01 | P3 | 无状态 Tab 筛选(仅标签展示) | 全量+标签可用,旧 infoaudit 亦以列表呈现;记录 |
| GAP-E-02 | P3 | 无编辑发布入口(后端有 /ypat/upd) | 旧列表未暴露编辑;记录,留待增强 |
| GAP-FAV-01 | — | 取消收藏 | 关闭: 后端无 un-favorite,旧版亦只增 |

## 5. 修改
- 无代码改动(三页业务已正确,依赖 GAP-API-01 的 GET 修复)
- 仅文档

## 6. 验证
- type-check ✅ / test 47/47 ✅(沿用)
- 结论: 三个列表页与旧版一致,状态/分页/刷新/空态完整,无 P0/P1/P2 遗留
