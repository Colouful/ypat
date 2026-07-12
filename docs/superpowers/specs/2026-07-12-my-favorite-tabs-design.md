# 我的收藏分类展示设计

## 背景与根因

`pages-sub/ypat/my-favorite`（我的收藏页）目前只调用约拍收藏列表接口，页面状态也只有一套 `YpatInfo`（约拍信息）列表。作品模块已经具备收藏、取消收藏、作品收藏关系表和按用户分页查询仓储，但没有向小程序暴露“我的作品收藏”列表接口，因此页面无法获得作品收藏数据。

本次改动补齐作品收藏查询链路，并在“我的收藏”页面增加“约拍 / 作品”两个 tab（标签页）。默认显示“约拍”，保留当前入口行为。

## 已确认设计

- 采用方案 B：主题色下划线 tab（标签页）与统一横向单列卡片。
- 默认 tab（标签页）为“约拍”。
- “作品”数据在首次切换到该 tab（标签页）时按需加载。
- 两类收藏分别维护列表、页码、是否还有更多、首次加载和加载更多状态。
- 主题色使用现有 `$color-primary`（主题色变量），不新增颜色体系。
- 页面不提供卡片内取消收藏按钮；点击卡片进入对应详情页，取消收藏仍在详情页完成。

## 页面结构

页面继续使用 `KeepPageNav`（通用页面导航）显示“我的收藏”。导航下方是白色 tab（标签页）栏：

- 两个标签等宽排列。
- 激活标签使用 `$color-primary`（主题色变量）、较高字重和底部主题色指示线。
- 未激活标签使用 `$color-text-helper`（辅助文字色变量）。
- 标签栏下方内容区使用 `$color-bg-page`（页面背景色变量）。

约拍卡片沿用当前信息内容，并统一整理为横向卡片：左侧固定比例封面，右侧展示发布者、发布时间、描述、城市和费用。

作品卡片同样使用横向卡片：左侧固定比例封面，右侧展示最多两个主题标签、标题或描述、作者职业与城市、收藏数量。视频作品在封面角落显示“视频”标识。

每个 tab（标签页）拥有独立的加载、空状态和到底提示。空状态文案分别为“暂无收藏的约拍”和“暂无收藏的作品”。

## 前端状态与数据流

页面定义：

```ts
type FavoriteTab = 'ypat' | 'work'

interface ListState<T> {
  items: T[]
  page: number
  loading: boolean
  loadingMore: boolean
  hasMore: boolean
  loaded: boolean
}
```

页面初始化与 `onShow`（页面显示生命周期）时刷新当前 tab（标签页）。首次进入固定刷新“约拍”。切换到未加载的 tab（标签页）时执行首次加载；切换到已加载的 tab（标签页）时直接展示已有数据，避免重复请求。

下拉刷新只刷新当前 tab（标签页），触底加载只推进当前 tab（标签页）的页码。请求失败时保留已有列表和页码，并使用当前类型对应的失败提示。

约拍数据继续调用：

```text
GET /my/ypat/sc/list
```

作品数据新增调用：

```text
GET /work/favorites?page=1&size=10
```

前端作品接口继续使用现有 `WorkListResult`（作品列表结果）结构：

```ts
interface WorkListResult {
  page: number
  size: number
  total: number
  hasMore?: boolean
  items: WorkListItem[]
}
```

## 后端接口设计

调用链如下：

```text
小程序 GET /work/favorites
  -> SYSTEM-WAP（网页服务）WorkController
  -> Feign（声明式服务调用）WorkServiceClient
  -> SYSTEM-API（接口服务）WorkController
  -> WorkService.favoriteWorks
  -> WorkFavoriteRepository.findByUserIdOrderByCreatedAtDesc
```

WAP（网页服务）控制器从当前登录态读取用户编号，不接受前端传入的用户编号作为鉴权依据。接口分页沿用作品模块的 1 起始页码规则，单页数量限制为 1 到 20。

领域服务先按收藏时间倒序分页读取 `WorkFavorite`（作品收藏关系），再查询仍处于“审核通过且未删除”状态的作品。已删除、已下架或不存在的作品不进入返回列表。返回结构保持 `page / size / total / items`，并增加 `hasMore`（是否还有下一页）处理不可见作品边界；卡片字段与普通作品列表一致。

作品列表项包含封面、媒体类型、描述、阅读量、点赞量、收藏量、发布时间、发布者昵称/头像/性别/职业/城市和主题标签。收藏列表顺序严格跟随收藏关系的创建时间倒序。

## 异常与边界

- 未登录访问作品收藏接口时返回现有鉴权失败错误。
- 页码或单页数量非法时回退到 `page=1`、`size=10`。
- 当前页收藏关系对应的作品全部不可见时，返回空 `items`（作品列表项）但保留真实 `total`（收藏关系总数）；前端依据 `hasMore`（是否还有下一页）决定是否继续请求，避免最后一页循环加载。
- tab（标签页）切换期间不清空另一类数据。
- 当前 tab（标签页）正在请求时，重复下拉或触底不会发起并发请求。
- 页面重新显示时只刷新当前 tab（标签页），使详情页取消收藏后返回能及时更新。

## 修改范围

前端：

- `frontend/src/pages-sub/ypat/my-favorite.vue`：双 tab（标签页）、两套分页状态、两类卡片与主题样式。
- `frontend/src/api/modules/work.ts`：新增作品收藏列表请求。
- `frontend/src/api/types/work.ts`：为作品列表结果增加可选 `hasMore`（是否还有下一页）字段。

后端：

- `backend/system-wap/src/main/java/com/ypat/controller/WorkController.java`：新增登录用户作品收藏入口。
- `backend/system-wap/src/main/java/com/ypat/service/WorkServiceClient.java`：新增 Feign（声明式服务调用）方法。
- `backend/system-restapi/src/main/java/com/ypat/controller/WorkController.java`：新增服务端点。
- `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`：组装收藏作品分页结果。

现有 `WorkFavoriteRepository`（作品收藏仓储）已经具备所需分页查询方法，不修改表结构和数据库脚本。

## 验证策略

遵循项目约束，不下载依赖、不执行构建或自动化测试。实现后执行：

- 前端改动文件 ESLint（代码规范检查）。
- `git diff --check`（差异格式检查）。
- 静态核对前端路径、WAP（网页服务）路径、Feign（声明式服务调用）路径和 REST API（接口服务）路径完全一致。

手工验证覆盖：默认约拍、切换作品、两类分页、两类空状态、下拉刷新、详情返回刷新、作品详情跳转、接口错误提示和主题色视觉效果。

## 非目标

- 不合并约拍收藏表和作品收藏表。
- 不改变现有收藏/取消收藏写接口。
- 不在收藏列表中增加批量管理或直接取消收藏。
- 不重构通用作品瀑布流或其他列表页面。
