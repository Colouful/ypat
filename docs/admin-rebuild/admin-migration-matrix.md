# YPAT 后台管理端 - 迁移矩阵

> 创建日期：2026-06-30
> 开发分支：feature/admin-vue3-rebuild
> 状态说明：未开始 / 开发中 / 已完成 / 有缺口 / 已验证

## 一、迁移总览

| 统计项 | 数量 |
|--------|------|
| 旧菜单数量 | 11 |
| 旧页面数量 | 22（含弹窗/系统页） |
| 旧接口数量 | 40（system-web） |
| 旧权限数量 | 0（无细粒度权限） |
| 本次迁移范围 | 审计 + 基础架构 + user 模块样板 |

## 二、完整迁移矩阵

| 编号 | 旧菜单 | 旧页面 | 旧操作 | 旧接口 | 新路由 | 新组件 | 新API | 权限 | 状态 |
|------|--------|--------|--------|--------|--------|--------|-------|------|------|
| M001 | 审核系统-申请列表 | manage/index.html | 查询 | GET /manage/list | /manage/ypat-list | views/manage/ypat-list/index.vue | GET /admin/ypat/list | 登录即可 | 未开始 |
| M002 | 审核系统-申请列表 | manage/detail.html | 审核通过 | POST /manage/audit?flag=2 | 弹窗 | views/manage/ypat-list/AuditDialog.vue | POST /admin/ypat/audit | 登录即可 | 未开始 |
| M003 | 审核系统-申请列表 | manage/detail.html | 审核不通过 | POST /manage/audit?flag=3 | 弹窗 | views/manage/ypat-list/AuditDialog.vue | POST /admin/ypat/audit | 登录即可 | 未开始 |
| M004 | 审核系统-申请列表 | manage/index.html | 上推荐 | POST /manage/upRecom?recomflag=1 | 列表页内 | views/manage/ypat-list/index.vue | POST /admin/ypat/recom | 登录即可 | 未开始 |
| M005 | 审核系统-申请列表 | manage/index.html | 取消推荐 | POST /manage/upRecom?recomflag=0 | 列表页内 | views/manage/ypat-list/index.vue | POST /admin/ypat/recom | 登录即可 | 未开始 |
| M006 | 审核系统-实名列表 | manage/user/index.html | 查询 | GET /manage/user/list | /manage/user/index | views/manage/user-list/index.vue | GET /admin/user/list | 登录即可 | 开发中 |
| M007 | 审核系统-实名列表 | manage/user/detail.html | 审核通过 | GET /manage/user/audit?flag=2 | 弹窗 | views/manage/user-list/UserAuditDialog.vue | POST /admin/user/audit | 登录即可 | 开发中 |
| M008 | 审核系统-实名列表 | manage/user/detail.html | 审核不通过 | GET /manage/user/audit?flag=3 | 弹窗 | views/manage/user-list/UserAuditDialog.vue | POST /admin/user/audit | 登录即可 | 开发中 |
| M009 | 审核系统-产品列表 | manage/product/index.html | 查询 | GET /manage/product/list | /manage/product/index | views/manage/product-list/index.vue | GET /admin/product/list | 登录即可 | 未开始 |
| M010 | 审核系统-产品列表 | manage/product/edit.html | 新增/编辑 | GET /manage/product/edit + POST /manage/product/save | 弹窗 | views/manage/product-list/ProductEditDialog.vue | GET /admin/product/detail + POST /admin/product/save | 登录即可 | 未开始 |
| M011 | 审核系统-产品列表 | manage/product/index.html | 上架 | POST /manage/product/upDown?status=0 | 列表页内 | views/manage/product-list/index.vue | POST /admin/product/upDown | 登录即可 | 未开始 |
| M012 | 审核系统-产品列表 | manage/product/index.html | 下架 | POST /manage/product/upDown?status=1 | 列表页内 | views/manage/product-list/index.vue | POST /admin/product/upDown | 登录即可 | 未开始 |
| M013 | 审核系统-文章列表 | manage/article/index.html | 查询 | GET /article/list | /article/index | views/article/list/index.vue | GET /admin/article/list | 登录即可 | 未开始 |
| M014 | 审核系统-文章列表 | manage/article/edit.html | 编辑 | GET /article/edit + POST /article/save | /article/edit | views/article/edit/index.vue | GET /admin/article/detail + POST /admin/article/save | 登录即可 | 未开始 |
| M015 | 审核系统-文章列表 | manage/article/index.html | 发布 | POST /article/upDown?status=1 | 列表页内 | views/article/list/index.vue | POST /admin/article/upDown | 登录即可 | 未开始 |
| M016 | 审核系统-文章列表 | manage/article/index.html | 撤回 | POST /article/upDown?status=2 | 列表页内 | views/article/list/index.vue | POST /admin/article/upDown | 登录即可 | 未开始 |
| M017 | 审核系统-横幅列表 | manage/banner/index.html | 查询 | GET /banner/list | /banner/index | views/banner/list/index.vue | GET /admin/banner/list | 登录即可 | 未开始 |
| M018 | 审核系统-横幅列表 | manage/banner/edit.html | 新增/编辑 | GET /banner/edit + POST /banner/save | 弹窗 | views/banner/list/BannerEditDialog.vue | GET /admin/banner/detail + POST /admin/banner/save | 登录即可 | 未开始 |
| M019 | 审核系统-横幅列表 | manage/banner/index.html | 查看图片 | window.open(imgpath) | 列表页内 | views/banner/list/index.vue | - | 登录即可 | 未开始 |
| M020 | 审核系统-横幅列表 | manage/banner/index.html | 发布 | POST /banner/upDown?status=1 | 列表页内 | views/banner/list/index.vue | POST /admin/banner/upDown | 登录即可 | 未开始 |
| M021 | 审核系统-横幅列表 | manage/banner/index.html | 撤回 | POST /banner/upDown?status=2 | 列表页内 | views/banner/list/index.vue | POST /admin/banner/upDown | 登录即可 | 未开始 |
| M022 | 审核系统-发布作品 | manage/ypatinfo/edit.html | 提交 | POST /ypat/submit | /ypat/edit | views/ypat/edit/index.vue | POST /admin/ypat/submit | 登录即可 | 未开始 |
| M023 | 查询系统-用户列表 | manage/query/userindex.html | 查询 | GET /manage/user/list | /manage/query/index | views/query/user-list/index.vue | GET /admin/user/list | 登录即可 | 未开始 |
| M024 | 查询系统-约拍列表 | manage/query/ypatappindex.html | 查询 | GET /manage/list?status=2 | /manage/query/ypat/appindex | views/query/ypat-list/index.vue | GET /admin/ypat/list | 登录即可 | 未开始 |
| M025 | 查询系统-消息列表 | manage/query/messindex.html | 查询 | GET /manage/mess/list | /manage/query/mess/messindex | views/query/mess-list/index.vue | GET /admin/mess/list | 登录即可 | 未开始 |
| M026 | 查询系统-公众号关注 | manage/pubevent/index.html | 查询 | GET /pubevent/list | /pubevent/index | views/pubevent/list/index.vue | GET /admin/pubevent/list | 登录即可 | 未开始 |
| M027 | 订单系统-订单列表 | manage/order/index.html | 查询 | GET /manage/order/list | /manage/order/index | views/order/list/index.vue | GET /admin/order/list | 登录即可 | 未开始 |

## 三、系统页面迁移

| 编号 | 旧页面 | 旧接口 | 新路由 | 新组件 | 状态 |
|------|--------|--------|--------|--------|------|
| S001 | login.html | POST /manage/login | /login | views/login/index.vue | 开发中 |
| S002 | home.html | 无 | /dashboard | views/dashboard/index.vue | 开发中 |
| S003 | 403.html | 无 | /403 | views/error/403.vue | 开发中 |
| S004 | deny.html | 无 | /403 | views/error/403.vue | 开发中 |
| S005 | tologin.html | 无 | - | - | 已废弃（前端路由处理） |
| S006 | tosignout.html | 无 | - | - | 已废弃（前端路由处理） |

## 四、本次迁移范围（Phase 1）

本次仅完成以下内容：

| 任务 | 范围 | 状态 |
|------|------|------|
| 旧后台全量审计 | 4 份审计文档 | 已完成 |
| 后端 Admin API | 登录+验证码+用户列表/详情/审核+CORS | 开发中 |
| 前端工程脚手架 | Vite+Vue3+TS+ElementPlus+Pinia+Router+Axios | 开发中 |
| 请求层 | Axios+ResponseApiBody适配器+JWT | 开发中 |
| 登录认证 | 登录页+JWT+路由守卫 | 开发中 |
| 主布局 | 侧边栏+Header+面包屑+标签页 | 开发中 |
| 用户管理模块 | 实名列表+审核弹窗（M006/M007/M008） | 开发中 |
| 测试 | 单元测试+组件测试 | 未开始 |

## 五、后续迁移计划（Phase 2+）

| 优先级 | 模块 | 迁移项 | 预计工作量 |
|--------|------|--------|-----------|
| P1 | 产品管理 | M009-M012（列表+编辑+上下架） | 中 |
| P1 | 文章管理 | M013-M016（列表+编辑+发布撤回+UEditor→wangEditor） | 大 |
| P1 | 横幅管理 | M017-M021（列表+编辑+发布撤回+图片预览） | 中 |
| P2 | 申请列表 | M001-M005（列表+审核+推荐） | 中 |
| P2 | 发布作品 | M022（多图上传+水印） | 中 |
| P3 | 用户查询 | M023（只读列表） | 小 |
| P3 | 约拍查询 | M024（只读列表） | 小 |
| P3 | 消息查询 | M025（只读列表） | 小 |
| P3 | 公众号统计 | M026（只读统计） | 小 |
| P3 | 订单查询 | M027（只读列表） | 小 |

## 六、"已验证"标准

每个迁移项标记"已验证"必须同时满足：

- [ ] 页面存在且可访问
- [ ] 接口真实返回数据
- [ ] 查询条件有效
- [ ] 表格列正确
- [ ] 表单校验有效
- [ ] 权限控制有效
- [ ] 异常状态处理完整
- [ ] 构建通过
