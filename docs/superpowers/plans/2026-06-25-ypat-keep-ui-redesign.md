# 爱去拍 Keep 风格 UI 重构 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将爱去拍 `uni-app` 前端迁移到参考 HTML 和截图定义的 Keep 风格，核心三页 1:1 还原，其余页面统一延展。

**Architecture:** 先建立共享视觉层和可复用业务组件，再迁移首页、发布页、我的页，随后迁移详情、登录和其余子页面。保留现有接口、状态仓库、登录校验、发布提交、分页和错误处理，只替换视图结构与样式。

**Tech Stack:** `uni-app`、`Vue 3`、`TypeScript`、`SCSS`、`Pinia`、`Vitest`、可选 `@lucide/vue`。

---

## 基线

工作树：`/Users/lizhenwei/.config/superpowers/worktrees/ypat-workspace/codex-ypat-keep-ui-redesign`

分支：`codex/ypat-keep-ui-redesign`

已完成基线验证：

- `npm run type-check`：通过
- `npm run lint`：通过
- `npm run test`：26 个测试通过
- `npm run build:h5`：通过，仅有现有 Sass `@import` 弃用警告

## 文件结构

### 共享视觉层

- Modify: `frontend/src/styles/tokens.scss`
  - 统一 Keep 风格颜色、间距、圆角、阴影和字体变量。
- Modify: `frontend/src/styles/mixins.scss`
  - 新增页面容器、卡片、胶囊、底部操作区、文本省略等样式混入。
- Modify: `frontend/src/styles/global.scss`
  - 注册全局 Keep 风格辅助类。
- Create: `frontend/src/components/business/KeepIcon.vue`
  - 提供跨端稳定的线性图标渲染入口，先支持本次页面需要的图标名称。
- Create: `frontend/src/components/business/KeepState.vue`
  - 统一加载、空状态、错误状态。
- Create: `frontend/src/components/business/KeepYpatCard.vue`
  - 首页、搜索、我的发布、我的申请、我的收藏共用的约拍列表卡片。
- Create: `frontend/src/components/business/KeepFilterSheet.vue`
  - 首页筛选底部弹层。

### 核心页面

- Modify: `frontend/src/pages/home/index.vue`
  - 迁移为参考图 1 和 HTML 的首页布局。
- Modify: `frontend/src/components/business/YpatPublishForm.vue`
  - 重构发布表单为参考图 2 和 HTML 的发布页布局。
- Modify: `frontend/src/pages/publish/index.vue`
  - 对齐发布页容器、未登录态和固定提交区。
- Modify: `frontend/src/pages/mine/index.vue`
  - 迁移为参考图 4 和 HTML 的个人中心布局。

### HTML 已有页面

- Modify: `frontend/src/components/business/YpatDetailView.vue`
  - 迁移为 HTML 的详情页视觉。
- Modify: `frontend/src/pages-sub/ypat/detail.vue`
  - 保持路由取参，承载详情组件。
- Modify: `frontend/src/pages/login/index.vue`
  - 迁移为 HTML 的登录页视觉，同时保留微信授权逻辑。

### 延展页面

- Modify: `frontend/src/pages/message/index.vue`
- Modify: `frontend/src/pages-sub/ypat/search.vue`
- Modify: `frontend/src/pages-sub/ypat/my-publish.vue`
- Modify: `frontend/src/pages-sub/ypat/my-apply.vue`
- Modify: `frontend/src/pages-sub/ypat/my-favorite.vue`
- Modify: `frontend/src/pages-sub/user/profile.vue`
- Modify: `frontend/src/pages-sub/user/edit-info.vue`
- Modify: `frontend/src/pages-sub/user/complete-info.vue`
- Modify: `frontend/src/pages-sub/user/realname.vue`
- Modify: `frontend/src/pages-sub/user/wallet.vue`
- Modify: `frontend/src/pages-sub/user/recharge.vue`
- Modify: `frontend/src/pages-sub/user/records.vue`
- Modify: `frontend/src/pages-sub/user/bills.vue`
- Modify: `frontend/src/pages-sub/user/settings.vue`
- Modify: `frontend/src/pages-sub/user/about.vue`
- Modify: `frontend/src/pages-sub/user/feedback.vue`
- Modify: `frontend/src/pages-sub/content/article.vue`
- Modify: `frontend/src/pages-sub/content/agreement.vue`
- Modify: `frontend/src/pages-sub/content/privacy.vue`
- Modify: `frontend/src/pages-sub/content/message-detail.vue`

---

## Task 1: 共享 Keep 视觉基础

**Files:**
- Modify: `frontend/src/styles/tokens.scss`
- Modify: `frontend/src/styles/mixins.scss`
- Modify: `frontend/src/styles/global.scss`
- Create: `frontend/src/components/business/KeepIcon.vue`
- Create: `frontend/src/components/business/KeepState.vue`

- [ ] **Step 1: 扩展设计变量**

在 `tokens.scss` 中保持现有变量名，并补齐：

```scss
$color-bg-chip: #F1F3F5;
$color-orange-soft: #FFF3DF;
$color-blue-soft: #EEF3FA;
$color-gold-soft: #F8F1E3;
$shadow-keep-card: 0 6rpx 24rpx rgba(20, 24, 31, 0.04);
$shadow-keep-button: 0 20rpx 44rpx rgba(35, 194, 104, 0.28);
$radius-keep-card: 32rpx;
$radius-keep-field: 32rpx;
```

- [ ] **Step 2: 新增通用样式混入**

在 `mixins.scss` 中新增 `keep-page`、`keep-card`、`keep-chip`、`keep-primary-button`、`line-clamp`，用于页面迁移。

- [ ] **Step 3: 新增全局辅助类**

在 `global.scss` 中新增 `.keep-page`、`.keep-card`、`.keep-chip`、`.keep-safe-bottom`，用于简单页面快速统一风格。

- [ ] **Step 4: 创建 `KeepIcon.vue`**

实现 `name`、`size`、`color`、`strokeWidth` props。用 `view` 和 `text` 组合输出线性图标；图标名称至少覆盖 `home`、`compass`、`plus-circle`、`mail`、`user`、`camera`、`map-pin`、`search`、`sliders`、`star`、`shield`、`wallet`、`check`、`chevron-left`、`chevron-right`、`grid`、`menu`。

- [ ] **Step 5: 创建 `KeepState.vue`**

支持 `type="loading" | "empty" | "error" | "login"`、`title`、`description`、`buttonText`，点击按钮时 emit `action`。

- [ ] **Step 6: 验证基础构建**

Run: `cd frontend && npm run type-check && npm run lint`

Expected: 两个命令都通过。

- [ ] **Step 7: Commit**

```bash
git add frontend/src/styles frontend/src/components/business/KeepIcon.vue frontend/src/components/business/KeepState.vue
git commit -m "feat(ui): add keep visual foundation"
```

## Task 2: 列表卡片与筛选弹层组件

**Files:**
- Create: `frontend/src/components/business/KeepYpatCard.vue`
- Create: `frontend/src/components/business/KeepFilterSheet.vue`
- Modify: `frontend/src/api/types/index.ts` only if type narrowing is needed for existing fields.

- [ ] **Step 1: 创建 `KeepYpatCard.vue`**

Props:

```ts
interface KeepYpatCardItem {
  id?: number
  title: string
  targetLabel: string
  chargeLabel: string
  city: string
  name: string
  image: string
  avatar: string
  time: string
  applyCount: number
  realname: boolean
  credit: boolean
}
```

组件输出左图右文卡片，点击卡片 emit `tap`。

- [ ] **Step 2: 创建 `KeepFilterSheet.vue`**

Props:

```ts
interface KeepFilterGroup {
  key: string
  title: string
  multiple: boolean
  options: Array<{ label: string; value: string }>
}
```

组件支持 `v-model:visible`、`modelValue`，点击重置 emit `reset`，点击确定 emit `confirm`。

- [ ] **Step 3: 添加最小组件测试**

Create: `frontend/src/components/business/__tests__/keep-components.test.ts`

测试 `KeepYpatCard` 渲染标题和标签，`KeepState` 渲染操作按钮。

- [ ] **Step 4: 运行组件测试**

Run: `cd frontend && npm run test -- keep-components`

Expected: 新增测试通过。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/components/business
git commit -m "feat(ui): add keep list and filter components"
```

## Task 3: 首页 1:1 还原

**Files:**
- Modify: `frontend/src/pages/home/index.vue`
- Use: `frontend/src/components/business/KeepYpatCard.vue`
- Use: `frontend/src/components/business/KeepFilterSheet.vue`
- Use: `frontend/src/components/business/KeepIcon.vue`
- Use: `frontend/src/components/business/KeepState.vue`

- [ ] **Step 1: 保留并整理现有数据逻辑**

保留 `loadBanners`、`loadList`、`switchTab`、`goDetail`、`goSearch`、`goMessage`、`getLocation`、下拉刷新和触底加载。

- [ ] **Step 2: 新增前端卡片适配 computed**

把 `YpatInfo` 映射为 `KeepYpatCardItem`，缺图使用 `/static/default-cover.png`，缺头像使用 `/static/default-avatar.png`。

- [ ] **Step 3: 重写 template**

实现顶部搜索、AI 圆形入口、5 个功能入口、推荐/同城/最新标签、筛选入口、横向 chips、列表卡片、筛选弹层。

- [ ] **Step 4: 重写 style**

按 HTML 首页样式迁移：搜索条高度约 `84rpx`、功能入口白色圆角方块、标签选中浅绿下划线、列表卡片左图右文。

- [ ] **Step 5: 验证首页构建**

Run: `cd frontend && npm run type-check && npm run lint`

Expected: 通过。

- [ ] **Step 6: Commit**

```bash
git add frontend/src/pages/home/index.vue
git commit -m "feat(ui): redesign home page in keep style"
```

## Task 4: 发布页 1:1 还原

**Files:**
- Modify: `frontend/src/components/business/YpatPublishForm.vue`
- Modify: `frontend/src/pages/publish/index.vue`
- Use: `frontend/src/components/business/KeepIcon.vue`
- Use: `frontend/src/components/business/KeepState.vue`

- [ ] **Step 1: 保留发布业务逻辑**

保留 `chooseImages`、`removeImage`、`convertImages`、`submit`、余额校验、区域选择、日期选择和提交状态。

- [ ] **Step 2: 扩展表单模型**

增加前端 `title` 和 `styles` 状态；提交时将标题合并到 `describ`，将风格数组写入 `patstyle`。

- [ ] **Step 3: 重写 `YpatPublishForm` template**

实现双分段大卡、标题字段、详细描述、合作方式胶囊、风格多选、城市字段、参考图三列上传。

- [ ] **Step 4: 重写 `pages/publish/index.vue`**

实现顶部返回 + 居中标题、未登录态同风格卡片、底部固定提交区域。

- [ ] **Step 5: 验证发布页逻辑**

Run: `cd frontend && npm run type-check && npm run lint && npm run test`

Expected: 通过。

- [ ] **Step 6: Commit**

```bash
git add frontend/src/components/business/YpatPublishForm.vue frontend/src/pages/publish/index.vue
git commit -m "feat(ui): redesign publish page in keep style"
```

## Task 5: 个人中心 1:1 还原

**Files:**
- Modify: `frontend/src/pages/mine/index.vue`
- Use: `frontend/src/components/business/KeepIcon.vue`
- Use: `frontend/src/components/business/KeepState.vue`

- [ ] **Step 1: 保留用户数据逻辑**

保留 `userStore`、`appStore`、`isLoggedIn`、`userInfo`、`isVerified`、`professLabel` 和所有跳转方法。

- [ ] **Step 2: 重写 template**

实现顶部菜单/扫码/消息、用户资料、身份胶囊、会员卡、快捷入口、数据概览卡。

- [ ] **Step 3: 重写 style**

按参考图 4 对齐：浅灰背景、白色胶囊、深色会员卡、黄色开通按钮、白色数据卡、底部安全区。

- [ ] **Step 4: 验证**

Run: `cd frontend && npm run type-check && npm run lint`

Expected: 通过。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/pages/mine/index.vue
git commit -m "feat(ui): redesign mine page in keep style"
```

## Task 6: 详情页和登录页按 HTML 还原

**Files:**
- Modify: `frontend/src/components/business/YpatDetailView.vue`
- Modify: `frontend/src/pages-sub/ypat/detail.vue`
- Modify: `frontend/src/pages/login/index.vue`

- [ ] **Step 1: 重构详情组件视觉**

保留 `load`、`preview`、`favorite`、`apply`、`requireLogin`。视觉迁移为大图、白色 sheet、标签、作者卡、底部操作栏。

- [ ] **Step 2: 重构登录页视觉**

保留微信小程序手机号授权逻辑、协议勾选、H5/App 不支持提示。视觉迁移为品牌 logo、手机号授权主按钮、协议底部说明和白卡提示。

- [ ] **Step 3: 验证**

Run: `cd frontend && npm run type-check && npm run lint && npm run test`

Expected: 通过。

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/business/YpatDetailView.vue frontend/src/pages-sub/ypat/detail.vue frontend/src/pages/login/index.vue
git commit -m "feat(ui): redesign detail and login pages"
```

## Task 7: 列表类页面延展

**Files:**
- Modify: `frontend/src/pages/message/index.vue`
- Modify: `frontend/src/pages-sub/ypat/search.vue`
- Modify: `frontend/src/pages-sub/ypat/my-publish.vue`
- Modify: `frontend/src/pages-sub/ypat/my-apply.vue`
- Modify: `frontend/src/pages-sub/ypat/my-favorite.vue`

- [ ] **Step 1: 消息中心迁移**

保留收到/发出的数据逻辑，改为白底 tab、消息白卡、统一未登录/空状态。

- [ ] **Step 2: 搜索中心迁移**

使用 Keep 搜索框、筛选 chips、列表卡片和空状态。

- [ ] **Step 3: 我的发布/申请/收藏迁移**

复用 `KeepYpatCard` 和 `KeepState`，保留现有分页和跳转。

- [ ] **Step 4: 验证**

Run: `cd frontend && npm run type-check && npm run lint`

Expected: 通过。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/pages/message/index.vue frontend/src/pages-sub/ypat
git commit -m "feat(ui): extend keep style to list pages"
```

## Task 8: 用户类、钱包类、内容类页面延展

**Files:**
- Modify all remaining files under `frontend/src/pages-sub/user`
- Modify all files under `frontend/src/pages-sub/content`
- Modify: `frontend/src/components/business/RechargePanel.vue`

- [ ] **Step 1: 用户资料和表单页迁移**

迁移 `profile`、`edit-info`、`complete-info`、`realname`、`settings`、`feedback`，使用白卡字段块和绿色主按钮。

- [ ] **Step 2: 钱包和账单页迁移**

迁移 `wallet`、`recharge`、`records`、`bills`、`RechargePanel`，使用数据卡、金额强调、列表行。

- [ ] **Step 3: 关于和内容页迁移**

迁移 `about`、`article`、`agreement`、`privacy`、`message-detail`，使用内容卡和统一正文排版。

- [ ] **Step 4: 验证**

Run: `cd frontend && npm run type-check && npm run lint`

Expected: 通过。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/pages-sub/user frontend/src/pages-sub/content frontend/src/components/business/RechargePanel.vue
git commit -m "feat(ui): extend keep style to account and content pages"
```

## Task 9: 最终验证与兼容检查

**Files:**
- Modify only files needed to fix verification failures.

- [ ] **Step 1: 跑完整验证**

Run:

```bash
cd frontend
npm run type-check
npm run lint
npm run test
npm run build:h5
```

Expected: 全部通过。`build:h5` 可以保留现有 Sass 弃用警告。

- [ ] **Step 2: 微信小程序构建验证**

Run: `cd frontend && npm run build:mp-weixin`

Expected: 通过。如果图标实现导致小程序构建失败，替换为本地 `KeepIcon` 文本/样式实现后重跑。

- [ ] **Step 3: 检查改动范围**

Run: `git status --short && git diff --stat HEAD~8..HEAD`

Expected: 仅包含前端 UI、计划文档和必要测试。

- [ ] **Step 4: Commit 修复**

如果验证中有修复：

```bash
git add frontend docs/superpowers/plans/2026-06-25-ypat-keep-ui-redesign.md
git commit -m "fix(ui): polish keep redesign verification"
```

## 自检

- 覆盖规格文档全部范围：核心三页、详情、登录、消息、约拍子页、用户子页、内容页、验证。
- 没有新增后端字段；发布页标题并入 `describ`，风格写入 `patstyle`。
- 不回滚主工作区已有未提交改动。
- 实现分支在全局 worktree 中独立完成。
