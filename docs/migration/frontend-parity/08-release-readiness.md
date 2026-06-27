# 08 上线就绪检查 (Release Readiness)

> 基线 commit bd7d3f9 · 分支 claude/frontend-legacy-parity

## 检查项
| 项 | 状态 | 说明 |
|---|---|---|
| 26 个新版正式路由审计 | ✅ | A–J 全覆盖 |
| 26 个旧版路由有明确去向 | ✅ | 见 01 + 10(MERGED/MAPPED/SPLIT/MISSING) |
| 旧版特殊功能处理结论 | ✅ | 邀请/帮助/保证金 见 J(MISSING/REPLACED,产品决策) |
| 页面入口可达 | ✅ | complete-info、message-detail 死页已打通 |
| 主要出口可达/无死链 | ✅ | INVITE 悬空常量无引用(P3) |
| 登录回跳正确 | ✅ | 资料全 navigateBack;资料缺 complete-info |
| TabBar 跳转正确 | ✅ | switchTab/navigateTo 无错配 |
| API 方法正确 | ✅ | GAP-API-01 列表 POST→GET 已修;PUT/POST 双支持已核 |
| API 参数正确 | ✅ | 全 form 绑定,与后端契约一致(03) |
| API 响应处理正确 | ✅ | {code,msg,res} envelope + falsy 保留(TEST-FIX-01) |
| Token 逻辑正确 | ✅ | Token 头 + 401 单飞刷新 + 防死循环 |
| Storage 兼容策略明确 | ✅ | 同 appid → 强制重登(ACCEPTED,见 05) |
| 图片上传可用 | ✅ | base64 带 dataURL 前缀(GAP-IMG-01 已修) |
| 发布流程可用 | ✅ | 前置/校验/日期/图片/拍拍豆/提交(D) |
| 报名流程可用 | ✅ | 理由弹窗+服务端门禁引导(C) |
| 收藏流程可用 | ✅ | 只增+服务端去重(C/E) |
| 消息流程可用 | ✅ | received→message-detail(F) |
| 联系方式解锁不重复扣费 | ✅ | 后端 linkwayflag 去重;费用 3 豆已修(F) |
| 实名隐私安全 | ✅ | 脱敏展示,不输出真实证件(G) |
| 支付仅服务端确认后成功 | ✅ | RechargePanel 轮询确认,不前端自增(H) |
| H5 降级正确 | ✅ | 不伪造微信登录/支付(05) |
| 微信小程序能力正确 | ✅ | #ifdef 守卫,appid 一致 |
| TypeScript 通过 | ✅ | type-check 0 |
| ESLint 通过 | ✅ | lint --quiet 0 error |
| 单元测试通过 | ✅ | 50/50 |
| H5 构建通过 | ✅ | build:h5 DONE |
| 微信小程序构建通过 | ✅ | build:mp-weixin DONE |
| 无生产 Mock | ✅ | 微信登录测试账号仅 development(GAP-AUTH-01) |
| 无敏感信息进仓库 | ✅ | 文档无 token/证件/密钥 |
| 新版视觉未被旧版覆盖 | ✅ | 仅 script/最小字段(publish 日期、edit-info 微信号),无旧 CSS/DOM |
| **P0 = 0** | ✅ | GAP-AUTH-01 / GAP-F-01 已清零 |
| **P1 = 0(前端)** | ✅ | GAP-API-01 / GAP-AUTH-03 / GAP-F-02 / GAP-IMG-01 已清零 |

## 阻塞/条件项
| 项 | 级别 | 类型 | 说明 |
|---|---|---|---|
| GAP-API-03 | P1 | **部署阻塞** | 生产 .env.production 为 http IP,env.ts 生产强制 HTTPS → 运行崩溃。**用户决策: 保持守卫,运维改 HTTPS 域名**。前端无法独立完成;上线前运维必须提供 HTTPS 域名并改 .env.production。 |
| GAP-J-01 | P2 | 产品决策 | 保证金充值页缺失;默认 creditflag=0 主路径不受阻,要求保证金的约拍报名会卡。 |
| GAP-I-01 | P2 | 后端/产品 | /feedback/add 后端不存在;反馈提交失败(诚实失败),需后端补端点或下线入口。 |
| GAP-F-03 | P2 | 待确认 | message sent tab 跳详情 id 形态需后端 rec/send/list 响应确认。 |
| GAP-G-01 | P2 | 产品决策 | 实名 2 照 vs 旧 3 照(手持);后端 List 可变。 |
| 其余 P3 | P3 | 记录 | 分享 stub、订阅消息、草稿、邀请、定位逆地理 等(见 06)。 |

## 结论
**CONDITIONALLY_READY**
- 前端业务迁移与对齐完成: P0=0, P1(前端)=0, type-check/lint/test/H5/小程序构建全绿。
- 上线前置(非前端代码可独立解决): **GAP-API-03 运维提供 HTTPS 域名**(P1 部署阻塞)。
- 待产品/后端决策(不阻塞核心主路径): GAP-J-01 保证金、GAP-I-01 反馈端点、GAP-F-03 列表实体确认、GAP-G-01 实名照数。
- 满足以上 HTTPS 部署前置后即可达 READY;P2 项建议在灰度前逐项关闭。
