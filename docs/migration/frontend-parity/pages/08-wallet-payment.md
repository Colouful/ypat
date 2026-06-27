# 页面：钱包 / 充值 / 收支记录 / 账单 (Module H)

## 1. 身份
| 新版 | 旧版 | 映射 |
|---|---|---|
| pages-sub/user/wallet | mine/ppd(余额) | MAPPED |
| pages-sub/user/recharge → RechargePanel | mine/ppd(充值) | MAPPED |
| pages-sub/user/records | mine/records | MAPPED |
| pages-sub/user/bills | (新增, RMB 订单账单) | NEW |

## 2. 充值(P0 关键)对照
| 维度 | 旧 ppd | 新 RechargePanel | 结论 |
|---|---|---|---|
| 产品列表 | product_list({status:0}) | getProductList({status:'1'}) GET ✅ | 一致(状态值以后端为准) |
| 创建订单 | order_create({type:'0',productid,total_fee}) | createOrder({type:'0',productid,total_fee:oldval}) POST ✅ | 一致(后端对 PPD 强制 total_fee=product.oldval) |
| 调起支付 | mPay→uni.requestPayment | invokeWechatPayment→uni.requestPayment ✅ | 一致 |
| **服务端确认后到账** | 成功即 toast | **waitForServerConfirmation 轮询 getOrderStatus,仅 result_code=SUCCESS/status=1 才"充值成功",并 updateUserInfo 从服务端取余额;未确认→弹窗"系统不会在前端自行增加余额"** | ✅✅ **满足 P0**(禁止前端自增余额) |
| 防重复支付 | — | paying 守卫 + onUnload 取消轮询 ✅ | 新版更稳 |
| 支付取消/失败 | — | catch cancel→toast;failed→error ✅ | 一致 |
| 金额单位 | 分 | total_fee=oldval(分);formatPrice /100 元 ✅ | 一致 |

## 3. 余额/记录/账单对照
| 维度 | 旧 | 新 | 结论 |
|---|---|---|---|
| 余额 | userInfo.ppd | userStore.userInfo.ppd ✅ | 一致 |
| 收支记录 | my_ppd_list | getRecordList(/record/findPage GET)分页 ✅ | 一致 |
| 记录类型 | recordsType 0充/1邀/2赠/3发/4申/5查 | RECORD_TYPE_LABELS + 收入集合{0,1,2}+/支出- ✅ | 一致 |
| PPD 单位 | 豆(计数) | signedAmount 用 item.ppd 计数,不 /100 ✅ | 一致 |
| 账单(RMB) | — | getBillList(/bill/findPage POST)分页,formatPrice /100,类型0/1/2,状态 SUCCESS/FAIL/待支付 ✅ | 新增,单位正确 |

## 4. 差异与处理
| 编号 | 级别 | 说明 | 处理 |
|---|---|---|---|
| (无 P0/P1/P2) | — | 充值 P0 安全;余额/记录/账单单位与分页正确 | 仅文档 |
| GAP-H-01 | P3 | bills 为新增 RMB 账单(旧无独立账单页) | 新功能,保留 |

## 5. 修改
- 无代码改动(支付流程 P0 安全,展示页正确)
- 仅文档

## 6. 验证
- type-check ✅ / test 50/50 ✅(沿用)
- 结论: 充值仅服务端确认后到账、防重复支付、金额/拍拍豆单位正确;Module H 无 P0/P1/P2 遗留。
- 注: 真实微信支付联调需 mp-weixin 环境+后端+沙箱,静态审计确认逻辑正确;**不发起生产支付测试**。
