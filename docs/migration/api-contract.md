# YPAT API 接口契约

## 通用约定

### 请求基础
- 基础地址: `https://www.91qupaier.com` (生产)
- Content-Type: `application/json; charset=UTF-8`
- 认证头: `Token: {jwt_token}`
- 分页默认: page=0, size=10

### 响应结构
```json
{
  "code": 200,
  "message": "成功",
  "result": { ... }
}
```

### 错误码
| 码 | 说明 |
|----|------|
| 200 | 成功 |
| 1001 | 鉴权失败 |
| 1002 | 参数错误 |
| 1003 | JSON错误 |
| 1004 | 非法字符串 |
| 1005 | 数据不存在 |
| 1006 | 数据已存在 |
| 1008 | OCR识别失败 |
| 1009 | 余额不足 |
| 1010 | 未实名 |
| 1011 | 未缴纳保证金 |
| 1012 | 密码错误 |
| 1013 | 实名失败 |
| 1014 | 识别超限 |
| 1015 | 水印失败 |
| 2001 | 微信通信失败 |
| 2002 | 下单失败 |
| 2003 | 支付失败 |
| 401 | Token无效 |
| 403 | 无权限 |
| 404 | 未找到 |
| 500 | 内部错误 |

### 分页响应结构
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "number": 0,
  "size": 10
}
```

---

## 用户模块

### 1. 微信授权换取 Session
- **路径**: `GET /user/code`
- **登录**: 否
- **参数**: `code` (String, 微信 login code)
- **响应**: `{ openid, session_key }`
- **前端模块**: 登录

### 2. 用户登录
- **路径**: `POST /user/login`
- **登录**: 否
- **参数**:
  ```json
  {
    "openid": "string",
    "encryptedData": "string",
    "sessionKey": "string",
    "iv": "string",
    "nickname": "string",
    "avatarurl": "string",
    "gender": "string",
    "channel": "string",
    "recmobile": "string"
  }
  ```
- **响应**: `UserQo` (含 id, token, 用户信息)
- **前端模块**: 登录

### 3. 获取/刷新 Token
- **路径**: `GET /user/token`
- **登录**: 是
- **参数**: 无 (通过当前 Token 获取用户)
- **响应**: `{ token: "string" }`
- **前端模块**: 全局请求

### 4. 获取用户信息
- **路径**: `GET /user/get`
- **登录**: 是
- **参数**: `id` (Long)
- **响应**:
  ```json
  {
    "id": 1,
    "gender": "1",
    "nickname": "string",
    "profess": "0",
    "mobile": "string",
    "wx": "string",
    "qq": "string",
    "wb": "string",
    "name": "string",
    "ppd": 100,
    "avatarurl": "string",
    "realnameflag": "0",
    "creditflag": "0",
    "pubtimes": 5,
    "rectimes": 10,
    "coltimes": 3,
    "status": "0",
    "province": "string",
    "city": "string",
    "area": "string",
    "openid": "string",
    "birthday": "2000-01-01",
    "imgpath": "string",
    "channel": "0"
  }
  ```
- **前端模块**: 个人中心

### 5. 修改用户信息
- **路径**: `POST /user/upd`
- **登录**: 是
- **参数**: UserQo (部分字段)
  ```json
  {
    "id": 1,
    "nickname": "string",
    "gender": "1",
    "profess": "0",
    "province": "string",
    "city": "string",
    "area": "string",
    "birthday": "2000-01-01",
    "mobile": "string",
    "wx": "string",
    "qq": "string",
    "wb": "string"
  }
  ```
- **响应**: 无 (200)
- **前端模块**: 编辑资料

### 6. 获取用户联系方式
- **路径**: `GET /user/linkway/get`
- **登录**: 是
- **参数**: `id` (Long, 目标用户ID), `userid` (Long, 当前用户ID), `messid` (Long, 消息ID)
- **响应**:
  ```json
  {
    "nickname": "string",
    "profess": "string",
    "mobile": "string",
    "wx": "string",
    "qq": "string",
    "wb": "string",
    "name": "string"
  }
  ```
- **业务限制**: 扣除1个拍拍豆，余额不足返回 1009
- **前端模块**: 消息/约拍详情

---

## 约拍模块

### 7. 推荐约拍列表
- **路径**: `GET /ypat/tc/list`
- **登录**: 否
- **参数**: `page` (Integer), `size` (Integer), `status` (String, 固定 "shtg")
- **响应**: 分页 YpatInfoQo 列表
- **前端模块**: 首页推荐

### 8. 最新约拍列表
- **路径**: `GET /ypat/zx/list`
- **登录**: 否
- **参数**: `page`, `size`, `status`, `city`, `target`, `chargeway`, `patstyle`
- **响应**: 分页 YpatInfoQo 列表
- **前端模块**: 首页最新/筛选

### 9. 获取约拍详情
- **路径**: `GET /ypat/get`
- **登录**: 否 (但需要 userid 判断收藏状态)
- **参数**: `id` (Long), `userid` (Long, 可选)
- **响应**:
  ```json
  {
    "id": 1,
    "describ": "string",
    "target": "0",
    "patdate": "2024-01-01",
    "patarea": "string",
    "patslice": "string",
    "chargeway": "0",
    "chargeamt": 100.00,
    "province": "string",
    "city": "string",
    "area": "string",
    "creditflag": "0",
    "realnameflag": "0",
    "patstyle": "string",
    "status": "shtg",
    "longitude": 116.0,
    "latitude": 39.0,
    "pubdate": "2024-01-01 12:00:00",
    "readtimes": 100,
    "pattimes": 10,
    "coltimes": 5,
    "userQo": { ... },
    "pics": ["url1", "url2"],
    "colflag": "1",
    "recomflag": "0",
    "timeStr": "2小时前"
  }
  ```
- **前端模块**: 约拍详情

### 10. 提交约拍
- **路径**: `POST /ypat/submit`
- **登录**: 是
- **参数**:
  ```json
  {
    "describ": "string (必填)",
    "target": "0 (必填, 0=约摄影师, 1=约模特)",
    "patdate": "2024-01-01 (必填)",
    "patarea": "string",
    "chargeway": "0 (必填, 0=互免, 1=收费, 2=可付费, 3=协商)",
    "chargeamt": 100.00,
    "province": "string (必填)",
    "city": "string (必填)",
    "area": "string",
    "creditflag": "0",
    "realnameflag": "0",
    "patstyle": "string",
    "userid": 1,
    "pics": ["url1", "url2"]
  }
  ```
- **业务限制**: 扣除3个拍拍豆，余额不足返回 1009
- **响应**: 无 (200)
- **前端模块**: 发布约拍

### 11. 增加阅读数
- **路径**: `PUT /ypat/yd/add`
- **登录**: 否
- **参数**: `id` (Long)
- **响应**: 无
- **前端模块**: 约拍详情（进入时调用）

---

## 我的约拍

### 12. 申请约拍
- **路径**: `PUT /my/ypat/rec/add`
- **登录**: 是
- **参数**:
  ```json
  {
    "sendperid": 1,
    "recperid": 2,
    "ypatid": 3,
    "content": "string"
  }
  ```
- **业务限制**:
  - 扣除1个拍拍豆
  - 检查约拍是否要求实名认证 (FAIL_NOREAL: 1010)
  - 检查约拍是否要求信用保证 (FAIL_NOCRED: 1011)
  - 余额不足返回 1009
- **响应**: 无 (200)
- **前端模块**: 约拍详情

### 13. 收藏约拍
- **路径**: `PUT /my/ypat/sc/add`
- **登录**: 是
- **参数**: `userid` (Long), `ypatid` (Long)
- **响应**: 无 (200)
- **前端模块**: 约拍详情

### 14. 收到的约拍申请列表
- **路径**: `POST /my/ypat/rec/list`
- **登录**: 是
- **参数**: MessInfoQo (含 recperid, page, size)
- **响应**: 分页消息列表 (含发送者信息)
- **前端模块**: 消息中心

### 15. 发出的约拍申请列表
- **路径**: `POST /my/ypat/send/list`
- **登录**: 是
- **参数**: MessInfoQo (含 sendperid, page, size)
- **响应**: 分页消息列表
- **前端模块**: 我的申请

### 16. 我的发布列表
- **路径**: `POST /my/ypat/pub/list`
- **登录**: 是
- **参数**: YpatInfoQo (含 userid, page, size)
- **响应**: 分页约拍列表
- **前端模块**: 我的发布

### 17. 我的收藏列表
- **路径**: `POST /my/ypat/sc/list`
- **登录**: 是
- **参数**: YpatInfoQo (含 userid, page, size)
- **响应**: 分页约拍列表
- **前端模块**: 我的收藏

### 18. 未读消息数量
- **路径**: `GET /my/ypat/unread/count`
- **登录**: 是
- **参数**: `userid` (Long)
- **响应**: Long (未读总数)
- **前端模块**: 消息角标

### 19. 收到消息未读数
- **路径**: `GET /my/ypat/rec/unread/count`
- **登录**: 是
- **参数**: `type` (String), `userid` (Long)
- **响应**: Long
- **前端模块**: 消息分类角标

### 20. 发出消息未读数
- **路径**: `GET /my/ypat/send/unread/count`
- **登录**: 是
- **参数**: `type` (String), `userid` (Long)
- **响应**: Long
- **前端模块**: 消息分类角标

---

## 消息模块

### 21. 获取消息详情
- **路径**: `GET /mess/get`
- **登录**: 是
- **参数**: `id` (Long), `userid` (Long)
- **响应**: MessInfoQo (含发送者信息、约拍信息)
- **业务**: 自动标记为已读
- **前端模块**: 消息详情

---

## 实名认证

### 22. OCR 识别
- **路径**: `POST /oauth/ocr`
- **登录**: 是
- **Content-Type**: multipart/form-data
- **参数**: 身份证图片文件
- **响应**: `{ name: "string", certcode: "string" }`
- **错误**: FAIL_OCR(1008), FAIL_LIMIT(1014)
- **前端模块**: 实名认证

### 23. 提交实名认证
- **路径**: `POST /oauth/add`
- **登录**: 是
- **参数**:
  ```json
  {
    "userid": 1,
    "name": "string (必填)",
    "certcode": "string (必填)",
    "pics": ["正面url", "反面url"]
  }
  ```
- **响应**: 无 (200)
- **前端模块**: 实名认证

### 24. 获取认证状态
- **路径**: `GET /oauth/get`
- **登录**: 是
- **参数**: `id` (Long, 用户ID)
- **响应**: `{ status: "string" }`
- **前端模块**: 实名认证状态

### 25. 获取认证详情
- **路径**: `GET /oauth/getAuth`
- **登录**: 是
- **参数**: `id` (Long)
- **响应**: OauthQo (含 name, certcode, pics, status)
- **前端模块**: 实名认证详情

---

## 支付模块

### 26. 获取充值套餐列表
- **路径**: `POST /product/findPage` 或 `GET /product/list`
- **登录**: 是
- **参数**: ProductQo (page, size, status="1")
- **响应**: 分页产品列表
  ```json
  {
    "id": 1,
    "name": "30拍拍豆",
    "currval": 30,
    "oldval": 3000,
    "status": "1"
  }
  ```
- **说明**: currval=拍拍豆数量, oldval=金额(分)
- **前端模块**: 充值

### 27. 创建订单
- **路径**: `POST /order/create`
- **登录**: 是
- **参数**:
  ```json
  {
    "type": "0 (必填, 0=拍拍豆, 1=实名, 2=保证金)",
    "userid": 1,
    "productid": 1,
    "total_fee": 3000
  }
  ```
- **响应**: 微信支付参数 (prepay_id 等)
- **前端模块**: 充值/支付

### 28. 账单列表
- **路径**: `POST /bill/findPage`
- **登录**: 是
- **参数**: BillQo (page, size, openid)
- **响应**: 分页账单列表
- **前端模块**: 账单

---

## 积分记录

### 29. 拍拍豆记录列表
- **路径**: `POST /record/findPage` 或 `GET /my/ppd/list`
- **登录**: 是
- **参数**: RecordQo (page, size, userid)
- **响应**: 分页记录列表
  ```json
  {
    "id": 1,
    "type": "0",
    "typeTxt": "充值",
    "credate": "2024-01-01 12:00:00",
    "ppd": 30,
    "userid": 1
  }
  ```
- **前端模块**: 拍拍豆明细

---

## 内容模块

### 30. Banner 列表
- **路径**: `POST /banner/findPage` 或旧接口
- **登录**: 否
- **参数**: BannerQo (page, size, status="1")
- **响应**: 分页 Banner 列表
- **前端模块**: 首页 Banner

### 31. 文章列表
- **路径**: `POST /article/findPage`
- **登录**: 否
- **参数**: ArticleQo (page, size, status="1", plat)
- **响应**: 分页文章列表
- **前端模块**: 文章/帮助

### 32. 文章详情
- **路径**: `GET /article/get`
- **登录**: 否
- **参数**: `id` (Long)
- **响应**: ArticleQo (含 content HTML)
- **前端模块**: 文章详情/用户协议/隐私政策

---

## 其他接口

### 33. 获取区域列表
- **路径**: `GET /area/list`
- **登录**: 否
- **响应**: 省市区三级数据
- **前端模块**: 地区选择

### 34. 获取系统参数
- **路径**: `GET /param/list`
- **登录**: 否
- **响应**: 系统配置参数
- **前端模块**: 全局配置

### 35. 获取微信模板ID
- **路径**: `GET /tmplid/list`
- **登录**: 否
- **响应**: 消息模板ID列表
- **前端模块**: 订阅消息

### 36. 生成小程序码
- **路径**: `GET /qr/code`
- **登录**: 是
- **参数**: 场景值
- **响应**: 图片二进制
- **前端模块**: 邀请好友

### 37. 同城推荐用户
- **路径**: `GET /user/findByCityAndProfess`
- **登录**: 是
- **参数**: `userid` (Long), `city` (String)
- **响应**: 同城用户列表
- **前端模块**: 约拍她/他

---

## 百度小程序接口

### 38. 百度授权换取 OpenID
- **路径**: `POST /bd/code`
- **登录**: 否
- **参数**: `code` (String)
- **响应**: `{ openid: "string" }`

### 39. 百度用户登录
- **路径**: `POST /bd/login`
- **登录**: 否
- **参数**: 用户信息
- **响应**: UserQo + Token
