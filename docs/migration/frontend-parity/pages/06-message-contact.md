# 页面：消息中心 / 消息详情 / 联系方式解锁 (Module F)

## 1. 身份
| 新版 | 旧版 | 映射 |
|---|---|---|
| pages/message/index | mine/message | MAPPED |
| pages-sub/content/message-detail | mine/message(详情) + home/linkway(联系方式) | MERGED |

## 2. 业务对照
| 维度 | 旧 linkway/message | 新 message-detail | 结论 |
|---|---|---|---|
| 收到/申请分类 | 列表 | received/sent tab(getMyReceivedList/getMySentList GET)✅ | 一致 |
| 消息详情 | mess_get | getMessageDetail(/mess/get?id&userid)✅ | 一致 |
| 联系方式解锁 | user_linkway_get(userid,messid) | getLinkWay(sendperid, message.id)✅ | 一致 |
| 解锁费用 | **3 豆**(VIEW_NEED_PPD=3) | (修复前)显示/预检 **1 豆** ❌ | **已修复→3 豆** |
| 防重复扣费 | linkwayflag 服务端去重 | 后端 linkwayflag=yes 不再扣费(前端依赖服务端)✅ | 一致(服务端保护) |
| 余额不足 | 弹窗→充值 | ppd<3 弹窗→recharge ✅ | 一致(阈值修正后) |
| 脱敏/展示 | 手机/微信/QQ/微博 | contactItems 过滤展示 ✅ | 一致 |
| 跳转来源 | 列表→linkway | 列表 received→message-detail ✅ | **已修复(原不可达)** |

## 3. 差异与处理
| 编号 | 级别 | 说明 | 处理 |
|---|---|---|---|
| GAP-F-01 | **P0** | 解锁费用显示/预检 1 豆,后端实扣 3 豆(金额错误) | **FIXED**: 引入 VIEW_CONTACT_PPD=3(对齐后端),阈值与文案改 3 |
| GAP-F-02 | **P1** | message-detail 无人跳转(死页),联系方式解锁不可达 | **FIXED**: 消息中心 received tab → message-detail?id=item.id |
| GAP-F-03 | P2 | rec/send 列表项以 YpatInfo 类型承载,实为 MessInfo 形;sent tab 用 item.id 跳约拍详情,若 id 为消息id 则应为 ypatid | 记录待确认: 需后端 rec/send/list 响应实体确认;received 不受影响(message-detail 用消息id) |

## 4. 修改
- constants/enums.ts: 新增 VIEW_CONTACT_PPD/PUBLISH_PPD/APPLY_PPD=3(对齐后端 Constant)
- pages-sub/content/message-detail.vue: 解锁费用 1→3,文案"已解锁不再扣费"
- pages/message/index.vue: openDetail 按 tab 分流,received→message-detail(修复联系方式解锁入口)

## 5. 验证
- type-check ✅ / test 47/47 ✅
- 结论: P0 金额错误已修复;P1 解锁入口打通;防重复扣费由服务端 linkwayflag 保障。GAP-F-03 待后端响应实体确认(不阻塞 received 解锁主流程)。
