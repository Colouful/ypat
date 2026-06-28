# Endpoint Checks

| 接口 | 方法 | 结论 | 证据 |
| -- | -- | -- | -- |
| `/my/ypat/rec/list` | GET | 返回 `MessInfoQo` 分页 | `MypatInfoController.myRecList` 调 `messInfoServiceClient.findPage` |
| `/my/ypat/send/list` | GET | 返回 `MessInfoQo` 分页 | `MypatInfoController.mySendList` 调 `messInfoServiceClient.findPage`; `ypatid` 为约拍 ID |
| `/mess/get` | GET | 返回消息详情并带 `ypatid` | `MessInfoService.findById` 设置 `ypatid` |
| `/feedback/add` | POST form | 本轮新增 | WAP Controller 校验和限频,REST API 持久化 |
| `/oauth/add` | POST form | 兼容两照 dataURL | `OauthController.add` 支持 dataURL 和裸 Base64 |
| `/user/token` | GET | 本轮安全加固 | 必须带有效 Token,不再使用 mobile 签发 |
| `GET /**` | GET | 已移除匿名通配 | `WebSecurityConfig` 改为公开白名单 |

## 生产构建地址扫描

- `grep -R "82.156.14.216" dist || true`：命中。原因：本轮按要求未修改 `frontend/.env.production`。
- `grep -R "http://" dist || true`：命中。原因：当前生产配置仍为 HTTP IP。
- `grep -R "localhost" dist || true`：未发现业务地址命中。

结论：代码缺口已关闭，但正式生产发布前必须完成 HTTPS 域名配置并重新构建扫描。
