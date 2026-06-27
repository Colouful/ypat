# 页面：发布约拍 (Module D)

## 1. 身份
| 新版 | 旧版 | 映射 |
|---|---|---|
| pages/publish/index → YpatPublishForm.vue | pages/home/publish + success(结果态) | MAPPED |

## 2. 业务对照
| 维度 | 旧 getError/isNendUserInfo | 新(修复前) | 处理 |
|---|---|---|---|
| 未登录 | — | KeepState 登录态 ✅ | 一致 |
| 发布前置(gender/wx/mobile/nickname/imgpath) | isNendUserInfo 拦截→userInfo | **缺失** | **已修复**: isPublishProfileReady 校验→modal 引导 edit-info |
| 描述 ≥6 字 | describe<6 拦截 | 仅非空 | **已修复**: describ≥6 校验 |
| 至少1张图 | ≥1 | canSubmit 含 localPaths.length ✅ | 一致 |
| 拍摄对象 target | 必填 | 默认0,radio ✅ | 一致 |
| 拍摄日期 patdate | 必填+不过期+可选 | **写死 today,无选择UI** | **已修复**: 新增 date picker(:start=today)+不过期校验 |
| 省市 | 必填 | region picker,canSubmit 校验 ✅ | 一致 |
| 合作方式 chargeway | 必填 | radio ✅ | 一致 |
| 收费金额 | 收费类必填>0 | chargeAmountValid ✅ | 一致 |
| 图片 base64(≤9,压缩) | chooseImage compressed→base64 | chooseImage compressed→filePathToBase64,≤9 ✅ | 一致 |
| 拍拍豆≥3 | client 预检+服务端 | client modal→recharge + 服务端 PUB_NEED_PPD=3 ✅ | 一致 |
| 提交 | ypat_submit | ypatApi.submit(POST form,pics base64)✅ | 一致 |
| 成功→审核中 | ?status=99 | toast "发布成功,等待审核"+reset+刷新资料 ✅ | 一致(状态由 my-publish 承载) |

## 3. 差异与处理
| 编号 | 级别 | 说明 | 处理 |
|---|---|---|---|
| GAP-D-01 | P2 | 描述 ≥6 未校验 | **FIXED** |
| GAP-D-02 | P2 | patdate 写死 today 不可选、不校验过期 | **FIXED**: date picker + 校验 |
| GAP-D-03 | P2 | 发布前置(含 wx)未校验 | **FIXED**: isPublishProfileReady + 引导 edit-info |
| GAP-A-EDIT-01 | P2 | edit-info 无 wx 字段 | **FIXED**: 新增微信号输入 |
| GAP-D-04 | P3 | 无 requestSubscribeMessage(审核通知订阅) | 记录: mp-weixin 通知,非阻塞;留待补 |
| GAP-D-05 | P3 | 无草稿保存/恢复+离开提醒(旧 publishData) | 记录: ypat store 有 draftForm(pinia持久) 但表单未接入;留待增强 |

## 4. 修改
- YpatPublishForm.vue: 新增拍摄日期 picker + changePatdate;submit 增加 发布前置/描述≥6/日期不过期 校验
- utils/profile.ts: isPublishProfileReady(对齐 isNendUserInfo)
- pages-sub/user/edit-info.vue: 新增微信号(wx)字段(表单/init/save/store 同步)
- utils/__tests__/profile.test.ts: isPublishProfileReady 用例(+5)
- 视觉: publish 增加一处"拍摄日期"field-card(与"城市"同款样式,最小新增);edit-info 增加"微信号"input(与"昵称"同款)

## 5. 验证
- type-check ✅ / test 47/47 ✅ / lint ✅
- 结论: 发布核心流程与旧版对齐(前置/校验/图片/拍拍豆/提交);P2 全部修复;P3(订阅消息/草稿)记录
