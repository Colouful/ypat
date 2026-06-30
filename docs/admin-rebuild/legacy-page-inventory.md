# YPAT 旧后台管理端 - 页面清单

> 审计日期：2026-06-30
> 审计范围：`backend/system-web/src/main/resources/templates/` 全部模板
> 源码路径：`backend/system-web/src/main/resources/templates/`

## 一、页面总览

旧后台共包含 **22 个页面**，其中菜单页面 11 个、弹窗/子页面 5 个、非菜单系统页面 6 个。

排除布局/片段模板 4 个（layout.html、nav.html、footer.html、static/index.html），不作为业务页面统计。

## 二、菜单页面清单

菜单结构来源于 `templates/fragments/nav.html`，包含 3 个系统菜单，通过 `sysflag` 变量控制左侧菜单显示。

| 编号 | 一级菜单 | 二级菜单 | 页面名称 | 页面路径 | 页面类型 | 权限标识 |
|------|----------|----------|----------|----------|----------|----------|
| P001 | 审核系统(sysflag=1) | 申请列表 | 申请列表 | manage/index.html | 列表 | 无（登录即可访问） |
| P002 | 审核系统(sysflag=1) | 实名列表 | 实名列表 | manage/user/index.html | 列表 | 无 |
| P003 | 审核系统(sysflag=1) | 产品列表 | 产品列表 | manage/product/index.html | 列表 | 无 |
| P004 | 审核系统(sysflag=1) | 文章列表 | 文章列表 | manage/article/index.html | 列表 | 无 |
| P005 | 审核系统(sysflag=1) | 横幅列表 | 横幅列表 | manage/banner/index.html | 列表 | 无 |
| P006 | 审核系统(sysflag=1) | 发布作品 | 发布作品 | manage/ypatinfo/edit.html | 新增 | 无 |
| P007 | 查询系统(sysflag=2) | 用户列表 | 用户列表 | manage/query/userindex.html | 列表 | 无 |
| P008 | 查询系统(sysflag=2) | 约拍列表 | 约拍列表 | manage/query/ypatappindex.html | 列表 | 无 |
| P009 | 查询系统(sysflag=2) | 消息列表 | 消息列表 | manage/query/messindex.html | 列表 | 无 |
| P010 | 查询系统(sysflag=2) | 公众号关注 | 公众号关注 | manage/pubevent/index.html | 统计 | 无 |
| P011 | 订单系统(sysflag=3) | 订单列表 | 订单列表 | manage/order/index.html | 列表 | 无 |

## 三、弹窗/子页面

以下页面不作为独立菜单项，通过 art.dialog 弹窗或页面跳转方式打开。

| 编号 | 所属页面 | 页面名称 | 页面路径 | 页面类型 | 权限标识 |
|------|----------|----------|----------|----------|----------|
| P012 | P001 申请列表 | 约拍审核详情 | manage/detail.html | 审核（弹窗） | 无 |
| P013 | P002 实名列表 | 实名审核详情 | manage/user/detail.html | 审核（弹窗） | 无 |
| P014 | P003 产品列表 | 产品编辑 | manage/product/edit.html | 编辑（弹窗） | 无 |
| P015 | P004 文章列表 | 文章编辑 | manage/article/edit.html | 编辑（页面跳转） | 无 |
| P016 | P005 横幅列表 | 横幅编辑 | manage/banner/edit.html | 编辑（页面跳转） | 无 |

## 四、非菜单系统页面

| 编号 | 页面名称 | 页面路径 | 页面类型 | 说明 |
|------|----------|----------|----------|------|
| P017 | 登录页 | login.html | 登录 | 表单提交到 `/manage/login`，字段：mobile + password |
| P018 | 系统首页 | home.html | 仪表盘 | 3 个系统入口卡片，链接到审核/查询/订单系统 |
| P019 | 403错误页 | 403.html | 错误页 | 找不到页面提示 |
| P020 | 拒绝访问页 | deny.html | 错误页 | 没有权限访问提示 |
| P021 | 重定向登录 | tologin.html | 跳转页 | JS 自动跳转到 /login |
| P022 | 退出登录 | tosignout.html | 跳转页 | JS 自动跳转到 SSO signout 页面 |

## 五、排除页面（布局/片段）

| 页面路径 | 说明 |
|----------|------|
| fragments/layout.html | 布局模板，包含 header、左侧菜单（nav）、内容区域、footer |
| fragments/nav.html | 菜单片段，包含 nav（审核系统）、nav2（查询系统）、nav3（订单系统） |
| fragments/footer.html | 页脚片段 |
| static/index.html | 静态占位页 |

## 六、页面与 JS 对应关系

| 页面 | 对应 JS 文件 | 主要功能 |
|------|-------------|----------|
| manage/index.html（申请列表） | scripts/manage/list.js | 分页查询、审核弹窗、上推荐/取消推荐 |
| manage/detail.html（约拍审核详情） | scripts/manage/detail.js | 通过/不通过审核 |
| manage/user/index.html（实名列表） | scripts/manage/user/list.js | 分页查询、审核弹窗 |
| manage/user/detail.html（实名审核详情） | scripts/manage/user/detail.js | 通过/不通过审核 |
| manage/product/index.html（产品列表） | scripts/manage/product/list.js | 分页查询、新增/编辑弹窗、上架/下架 |
| manage/product/edit.html（产品编辑） | scripts/manage/product/edit.js | 表单提交保存 |
| manage/article/index.html（文章列表） | scripts/manage/article/list.js | 分页查询、编辑跳转、发布/撤回 |
| manage/article/edit.html（文章编辑） | scripts/manage/article/edit.js | UEditor富文本、表单提交保存 |
| manage/banner/index.html（横幅列表） | scripts/manage/banner/list.js | 分页查询、查看图片、发布/撤回 |
| manage/banner/edit.html（横幅编辑） | scripts/manage/banner/edit.js | 文件上传、表单提交保存 |
| manage/ypatinfo/edit.html（发布作品） | scripts/manage/ypatinfo/edit.js | 文件上传、表单提交 |
| manage/pubevent/index.html（公众号关注） | scripts/manage/pubevent/list.js | 分页查询统计 |
| manage/query/userindex.html（用户列表） | scripts/manage/query/userlist.js | 分页查询 |
| manage/query/ypatappindex.html（约拍列表） | scripts/manage/query/ypatlist.js | 分页查询 |
| manage/query/messindex.html（消息列表） | scripts/manage/query/messlist.js | 分页查询 |
| manage/order/index.html（订单列表） | scripts/manage/order/list.js | 分页查询（只读，无操作） |

## 七、各页面查询条件与表格列详情

### P001 申请列表（manage/index.html）

- **查询条件**：status(1待审核/2审核通过/3审核未通过)、nickname(用户名)、mobile(手机号)、recomflag(是否推荐)
- **表格列**：ID、性别(genderTxt)、昵称(nickname)、职业(professTxt)、约拍对象(targetTxt)、发布地区(city)、发布时间(pubdate)、状态(statusTxt)、是否推荐(recomflag)、操作
- **操作**：审核(detail弹窗)、上推荐(upRecom recomflag=1)、取消推荐(upRecom recomflag=0)

### P002 实名列表（manage/user/index.html）

- **查询条件**：status(1待审核/2审核通过/3审核未通过)
- **表格列**：ID、姓名(name)、证件号码(certcode)、状态(statusTxt)、操作
- **操作**：审核(detail弹窗)

### P003 产品列表（manage/product/index.html）

- **查询条件**：name(产品名称)
- **表格列**：ID、名称(name)、当前值(currval)、原值(oldval)、状态(status 0上架/1下架)、操作
- **操作**：修改(edit弹窗)、上架(upDown status=0)、下架(upDown status=1)

### P004 文章列表（manage/article/index.html）

- **查询条件**：name(文章标题)
- **表格列**：ID、标题(title)、描述(describ)、创建时间(credate)、是否推荐(flag)、状态(statusTxt)、操作
- **操作**：编辑(edit跳转)、发布(upDown status=1)、撤回(upDown status=2)

### P005 横幅列表（manage/banner/index.html）

- **查询条件**：name(横幅标题)
- **表格列**：ID、标题(title)、创建时间(credate)、状态(statusTxt)、操作
- **操作**：查看(edit/imgpath 新窗口打开图片)、发布(upDown status=1)、撤回(upDown status=2)

### P006 发布作品（manage/ypatinfo/edit.html）

- **页面类型**：新增表单
- **表单字段**：nickname、gender、profess、patdate、patstyleList、describ、file(头像)、files(作品图片数组)
- **操作**：提交(submit)

### P007 用户列表（manage/query/userindex.html）

- **查询条件**：nickname(昵称)、regisdate(注册日期)、mobile(手机号)、gender(性别)、id(用户ID)
- **表格列**：ID、昵称(nickname)、手机号(mobile)、微信(wx)、QQ(qq)、职业(professTxt)、性别(genderTxt)、生日(birthday)、注册日期(regisdate)、城市(city)、渠道(channelTxt)
- **操作**：无（只读列表）

### P008 约拍列表（manage/query/ypatappindex.html）

- **查询条件**：userid(用户ID)、city(城市)，固定 status=2（只查审核通过的）
- **表格列**：ID、昵称(userQo.nickname)、手机号(userQo.mobile)、性别(userQo.genderTxt)、约拍对象(targetTxt)、城市(city)、发布时间(pubdate)、拍摄次数(pattimes)、阅读次数(readtimes)、收藏次数(coltimes)
- **操作**：无（只读列表）

### P009 消息列表（manage/query/messindex.html）

- **查询条件**：ypatid(约拍ID)、sendperid(发送者ID)、recperid(接收者ID)，至少输入一个条件
- **表格列**：约拍ID(ypatid)、发送者ID(sendperid)、昵称(nickname)、创建时间(credate)、内容(content)
- **操作**：无（只读列表）

### P010 公众号关注（manage/pubevent/index.html）

- **查询条件**：dateStrStart(开始日期)、dateStrEnd(结束日期)、eventKey(事件关键词)
- **表格列**：ID、日期(dateStr)、事件Key(eventKey)、事件描述(eventKeyTxt)、消息次数(msgTimes)
- **操作**：无（只读统计列表）

### P011 订单列表（manage/order/index.html）

- **查询条件**：status(订单状态)
- **表格列**：ID、创建时间(credate)、用户ID(userid)、订单类型(typeTxt)、金额(total_fee)、支付状态(status 0否/1是)
- **操作**：无（只读列表）

## 八、分页机制

所有列表页使用统一的 jQuery pagination 插件：

- **每页条数**：10（items_per_page: 10）
- **页码**：0-based（current_page: 0）
- **请求参数**：page(页码, 0-based)、size(每页条数)
- **响应解析**：`data.res.content`（数据数组）、`data.res.totalElements`（总记录数）
- **首次加载**：load_first_page: true，首页数据直接从初始化请求获取
