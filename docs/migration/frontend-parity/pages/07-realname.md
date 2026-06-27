# 页面：实名认证 (Module G)

## 1. 身份
| 新版 | 旧版 | 映射 |
|---|---|---|
| pages-sub/user/realname | mine/realname + home/success(结果轮询) | MERGED |

## 2. 业务对照
| 维度 | 旧 | 新 | 结论 |
|---|---|---|---|
| OCR | oauth_ocr({cardfront: base64}) | ocrIdCard(path)→normalizeImage→filePathToBase64 ✅ | 一致(自动转 base64) |
| 提交 | oauth_add({name,certcode,pics base64[]}) | submitAuth(paths)→normalizeImage 转 base64 ✅ | 一致 |
| 证件照 | 正/反/**手持**(3张) | 正/反(2张) | **差异**: 缺手持(GAP-G-01) |
| 姓名/证件号校验 | name + 身份证正则 | name + /\d{15}|\d{17}[\dXx]/ ✅ | 一致 |
| OCR 自动填充 | 成功填充,1014 保留手填 | 成功填充 name/certcode,失败 toast 手填 ✅ | 一致 |
| 脱敏 | — | maskedName/maskedCode ✅ | 新版更好(隐私) |
| 审核状态 | status 0/3 失败,1 审核中,2 通过 | status 1 审核中 / 2 通过 / 3 未通过(0→显示表单可重提) ✅ | 基本一致 |
| 结果轮询 | success 页轮询 oauth_get | onLoad loadDetail + 提交后 loadDetail ✅ | 等价(进入即查状态) |
| 隐私 | — | 不向文档输出真实证件;脱敏展示 ✅ | 合规 |

## 3. 差异与处理
| 编号 | 级别 | 说明 | 处理 |
|---|---|---|---|
| GAP-G-01 | P2 | 仅 2 张证件照(缺手持),旧需 3 张 | 后端 OauthQo.pics 为 List 可变;是否需手持为产品决策。记录,留待确认 |
| GAP-IMG-01 | P1(待验证) | 图片 base64 格式不一致: publish/realname 发**裸 base64**(filePathToBase64 去头),edit-info 头像加 `data:image/...;base64,` 前缀;旧版统一带前缀 | **跨模块**: 需后端确认 /oauth/add、/ypat/submit、/user/upd 接受的 base64 格式;确认后统一(不盲改,避免破坏已联调路径) |

## 4. 修改
- 无代码改动(实名流程功能完整: OCR/转码/脱敏/校验/审核态)
- 仅文档

## 5. 验证
- type-check ✅ / test 47/47 ✅(沿用)
- 结论: 实名核心流程与旧版一致且隐私更好;GAP-G-01(手持照)产品待定;GAP-IMG-01(base64 格式)需后端联调确认,列入跨切关注。
