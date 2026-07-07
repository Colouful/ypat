# 首页横幅、作品体验、实名认证说明与开屏页 Implementation Plan(实施计划)

> **For agentic workers(代理式执行者):** REQUIRED SUB-SKILL(必需子技能): Use `superpowers:subagent-driven-development`(子代理驱动开发，推荐) or `superpowers:executing-plans`(执行计划) to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal(目标):** 在旧小程序补齐首页 banner(横幅)、作品详情/投诉/约拍、实名认证说明和开屏页，并在后端/管理后台补齐作品投诉治理闭环。

**Architecture(架构):** 旧小程序以新增独立组件和页面为主，`common/vmeitime-http/index.js` 统一增加 API(接口) 包装；首页只接入 banner(横幅) 展示，不改现有列表数据流。作品交互复用现有 `/work/*` 后端接口，后台投诉治理按缺口补 `admin work complain(后台作品投诉)` 模块。

**Tech Stack(技术栈):** uni-app(跨端框架) + Vue 2、SCSS(样式预处理器)、Spring Boot(Java 后端框架)、Feign(声明式 HTTP 客户端)、Vue 3 + TypeScript(类型脚本) + Element Plus(后台组件库)、Vitest(前端测试工具)、Maven(构建工具)。

---

## Constraints(约束)

- 不回退或清理当前工作区已有未提交改动。
- 不修改 `.omx/state/session.json`。
- 不重构旧 `ypat`(约拍) 数据模型。
- 不改造新版小程序 `frontend`。
- 旧小程序没有现成单测框架，本计划用 `scripts/verify-91pai-work-pages.mjs` 做结构验证，并用 `npm run build:h5` 做构建验证。
- 每个任务只 stage(暂存) 本任务相关文件。

## File Structure(文件结构)

### 旧小程序 `91pai-master`

- Modify(修改): `91pai-master/common/vmeitime-http/index.js`
  - 增加 `banner_list`、`work_get`、`work_like_add`、`work_like_cancel`、`work_sc_add`、`work_sc_cancel`、`work_quick_apply`、`work_complain` API(接口) 包装。
- Create(新增): `91pai-master/components/custom/homeBanner/index.vue`
  - 首页 banner(横幅) 展示组件。
- Create(新增): `91pai-master/components/custom/homeBanner/index.js`
  - banner(横幅) 组件逻辑。
- Create(新增): `91pai-master/components/custom/homeBanner/index.scss`
  - banner(横幅) 组件样式。
- Create(新增): `91pai-master/components/custom/splashOverlay/index.vue`
  - 开屏遮罩组件。
- Create(新增): `91pai-master/components/custom/splashOverlay/index.js`
  - 倒计时、跳过和展示频率逻辑。
- Create(新增): `91pai-master/components/custom/splashOverlay/index.scss`
  - 开屏视觉样式。
- Modify(修改): `91pai-master/pages/home/home/index.vue`
  - 在 `.content-box` 顶部加入 `home-banner` 和 `splash-overlay`。
- Modify(修改): `91pai-master/pages/home/home/index.js`
  - 注册组件，加载 banner(横幅) 数据。
- Modify(修改): `91pai-master/pages/home/home/index.scss`
  - 调整 banner(横幅) 与列表间距。
- Create(新增): `91pai-master/pages/work/detail/index.vue`
- Create(新增): `91pai-master/pages/work/detail/index.js`
- Create(新增): `91pai-master/pages/work/detail/index.scss`
  - 作品详情页。
- Create(新增): `91pai-master/pages/work/complain/index.vue`
- Create(新增): `91pai-master/pages/work/complain/index.js`
- Create(新增): `91pai-master/pages/work/complain/index.scss`
  - 投诉页。
- Create(新增): `91pai-master/pages/work/apply/index.vue`
- Create(新增): `91pai-master/pages/work/apply/index.js`
- Create(新增): `91pai-master/pages/work/apply/index.scss`
  - 作品约拍页。
- Create(新增): `91pai-master/pages/mine/realname/intro/index.vue`
- Create(新增): `91pai-master/pages/mine/realname/intro/index.js`
- Create(新增): `91pai-master/pages/mine/realname/intro/index.scss`
  - 实名认证说明页。
- Modify(修改): `91pai-master/pages/mine/mine/index.vue`
- Modify(修改): `91pai-master/pages/mine/mine/index.js`
  - 实名认证入口跳转说明页。
- Modify(修改): `91pai-master/pages/home/desc/index.js`
  - 旧约拍详情中实名认证门槛弹窗跳转说明页。
- Modify(修改): `91pai-master/pages.json`
  - 注册新增页面。

### 验证脚本

- Create(新增): `scripts/verify-91pai-work-pages.mjs`
  - 验证旧小程序新增接口、页面注册、关键组件和必填文案。

### 后端 `backend/system-wap`

- Inspect(检查): `backend/system-wap/src/main/java/com/ypat/controller/WorkComplainController.java`
  - 确认用户侧投诉已存在。
- Create(新增): `backend/system-wap/src/main/java/com/ypat/controller/AdminWorkComplainController.java`
  - 管理端作品投诉治理接口。
- Create(新增): `backend/system-wap/src/main/java/com/ypat/service/WorkComplainAdminServiceClient.java`
  - Feign(声明式 HTTP 客户端) 调用服务端投诉治理。
- Create(新增): `backend/system-wap/src/test/java/com/ypat/controller/AdminWorkComplainControllerSourceTest.java`
  - source test(源码测试) 锁住后台接口、参数校验和鉴权路径。
- Modify(修改): `backend/system-wap/src/main/java/com/ypat/config/WebSecurityConfig.java`
  - 确认 `/admin/work/complain/**` 走管理端鉴权；不加入匿名白名单。

### 管理后台 `frontend-admin`

- Create(新增): `frontend-admin/src/api/modules/work-complain.ts`
  - 后台投诉 API(接口)。
- Create(新增): `frontend-admin/tests/unit/work-complain-api.test.ts`
  - API(接口) 参数测试。
- Create(新增): `frontend-admin/src/views/manage/work-complain-list/index.vue`
  - 投诉治理页。
- Modify(修改): `frontend-admin/src/constants/menu.ts`
  - 增加投诉治理菜单入口。
- Modify(修改): `frontend-admin/src/stores/modules/permission.ts`
  - 注册投诉治理组件映射。

---

### Task 1: 旧小程序 API(接口) 包装与结构验证脚本

**Files(文件):**
- Modify(修改): `91pai-master/common/vmeitime-http/index.js`
- Create(新增): `scripts/verify-91pai-work-pages.mjs`

- [ ] **Step 1: 写结构验证脚本**

Create `scripts/verify-91pai-work-pages.mjs`:

```javascript
import fs from 'node:fs'
import path from 'node:path'

const root = process.cwd()

function read(file) {
  return fs.readFileSync(path.join(root, file), 'utf8')
}

function assertContains(file, text) {
  const content = read(file)
  if (!content.includes(text)) {
    throw new Error(`${file} 缺少 ${text}`)
  }
}

function assertExists(file) {
  if (!fs.existsSync(path.join(root, file))) {
    throw new Error(`${file} 不存在`)
  }
}

const apiFile = '91pai-master/common/vmeitime-http/index.js'
const apiExports = [
  'export const banner_list',
  'export const work_get',
  'export const work_like_add',
  'export const work_like_cancel',
  'export const work_sc_add',
  'export const work_sc_cancel',
  'export const work_quick_apply',
  'export const work_complain',
]

apiExports.forEach((item) => assertContains(apiFile, item))

const pages = [
  '91pai-master/pages/work/detail/index.vue',
  '91pai-master/pages/work/detail/index.js',
  '91pai-master/pages/work/detail/index.scss',
  '91pai-master/pages/work/complain/index.vue',
  '91pai-master/pages/work/complain/index.js',
  '91pai-master/pages/work/complain/index.scss',
  '91pai-master/pages/work/apply/index.vue',
  '91pai-master/pages/work/apply/index.js',
  '91pai-master/pages/work/apply/index.scss',
  '91pai-master/pages/mine/realname/intro/index.vue',
  '91pai-master/pages/mine/realname/intro/index.js',
  '91pai-master/pages/mine/realname/intro/index.scss',
  '91pai-master/components/custom/homeBanner/index.vue',
  '91pai-master/components/custom/splashOverlay/index.vue',
]

pages.forEach(assertExists)

assertContains('91pai-master/pages.json', '"path": "pages/work/detail/index"')
assertContains('91pai-master/pages.json', '"path": "pages/work/complain/index"')
assertContains('91pai-master/pages.json', '"path": "pages/work/apply/index"')
assertContains('91pai-master/pages.json', '"path": "pages/mine/realname/intro/index"')
assertContains('91pai-master/pages/home/home/index.vue', '<home-banner')
assertContains('91pai-master/pages/home/home/index.vue', '<splash-overlay')
assertContains('91pai-master/pages/work/complain/index.vue', '请选择投诉原因')
assertContains('91pai-master/pages/work/apply/index.vue', '安全防骗提醒')
assertContains('91pai-master/pages/mine/realname/intro/index.vue', '开始实名')

console.log('91pai work pages verification passed')
```

- [ ] **Step 2: 运行验证脚本确认失败**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
node scripts/verify-91pai-work-pages.mjs
```

Expected(预期): FAIL(失败)，提示 `export const banner_list` 或新增页面文件不存在。

- [ ] **Step 3: 增加 API(接口) 包装**

Append to `91pai-master/common/vmeitime-http/index.js`:

```javascript
// 首页横幅
export const banner_list = (data) => {
  const url = getUrl('/banner/list', data)
  return http.request({
    url,
    method: 'GET',
  })
}

// 作品详情
export const work_get = (data) => {
  const url = getUrl('/work/get', data)
  return http.request({
    url,
    method: 'GET',
  })
}

// 作品点赞
export const work_like_add = (data) => {
  const url = getUrl('/work/like/add', data)
  return http.request({
    url,
    method: 'PUT',
  })
}

// 取消作品点赞
export const work_like_cancel = (data) => {
  const url = getUrl('/work/like/cancel', data)
  return http.request({
    url,
    method: 'PUT',
  })
}

// 收藏作品
export const work_sc_add = (data) => {
  const url = getUrl('/work/sc/add', data)
  return http.request({
    url,
    method: 'PUT',
  })
}

// 取消收藏作品
export const work_sc_cancel = (data) => {
  const url = getUrl('/work/sc/cancel', data)
  return http.request({
    url,
    method: 'PUT',
  })
}

// 作品快捷约拍
export const work_quick_apply = (data) => {
  const url = getUrl('/work/quick-apply')
  return http.request({
    url,
    method: 'POST',
    data,
  })
}

// 作品投诉
export const work_complain = (data) => {
  const url = getUrl('/work/complain')
  return http.request({
    url,
    method: 'POST',
    data,
  })
}
```

- [ ] **Step 4: 运行验证脚本确认剩余页面失败**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
node scripts/verify-91pai-work-pages.mjs
```

Expected(预期): FAIL(失败)，提示 `pages/work/detail/index.vue` 不存在。

- [ ] **Step 5: 提交 Task 1**

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git add 91pai-master/common/vmeitime-http/index.js scripts/verify-91pai-work-pages.mjs
git commit -m "test: add miniapp work flow verifier" -m "Scope: add old miniapp API wrappers and structural verifier for home banner, work pages, realname intro, and splash overlay." -m "Tested: node scripts/verify-91pai-work-pages.mjs fails on missing planned pages"
```

### Task 2: 首页 banner(横幅) 与开屏遮罩

**Files(文件):**
- Create(新增): `91pai-master/components/custom/homeBanner/index.vue`
- Create(新增): `91pai-master/components/custom/homeBanner/index.js`
- Create(新增): `91pai-master/components/custom/homeBanner/index.scss`
- Create(新增): `91pai-master/components/custom/splashOverlay/index.vue`
- Create(新增): `91pai-master/components/custom/splashOverlay/index.js`
- Create(新增): `91pai-master/components/custom/splashOverlay/index.scss`
- Modify(修改): `91pai-master/pages/home/home/index.vue`
- Modify(修改): `91pai-master/pages/home/home/index.js`
- Modify(修改): `91pai-master/pages/home/home/index.scss`

- [ ] **Step 1: 创建 `homeBanner` 组件模板**

Create `91pai-master/components/custom/homeBanner/index.vue`:

```vue
<template>
  <view v-if="visibleBanners.length" class="home-banner">
    <swiper
      class="home-banner-swiper"
      :autoplay="visibleBanners.length > 1"
      :circular="visibleBanners.length > 1"
      :interval="5000"
      :duration="300"
      @change="change"
    >
      <swiper-item v-for="(item,index) in visibleBanners" :key="item.id || index">
        <view class="banner-card" @tap="tapBanner(item)">
          <image class="banner-image" :src="item.imgpath" mode="aspectFill" />
          <view class="banner-mask">
            <view class="banner-title">{{item.title || '爱去拍精选'}}</view>
          </view>
        </view>
      </swiper-item>
    </swiper>
    <view v-if="visibleBanners.length > 1" class="banner-dots">
      <view
        v-for="(item,index) in visibleBanners"
        :key="'dot-' + (item.id || index)"
        :class="['dot', current === index ? 'active' : '']"
      ></view>
    </view>
  </view>
</template>

<script src="./index.js"></script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
```

- [ ] **Step 2: 创建 `homeBanner` 逻辑**

Create `91pai-master/components/custom/homeBanner/index.js`:

```javascript
export default {
  name: 'homeBanner',
  props: {
    list: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      current: 0,
    }
  },
  computed: {
    visibleBanners() {
      return (this.list || []).filter((item) => item && item.imgpath)
    },
  },
  methods: {
    change(e) {
      this.current = e.detail.current
    },
    tapBanner(item) {
      const image = item && item.imgpath
      if (!image) return
      uni.previewImage({
        current: image,
        urls: this.visibleBanners.map((banner) => banner.imgpath),
      })
    },
  },
}
```

- [ ] **Step 3: 创建 `homeBanner` 样式**

Create `91pai-master/components/custom/homeBanner/index.scss`:

```scss
.home-banner {
  position: relative;
  margin: 20rpx 24rpx 10rpx;
}

.home-banner-swiper {
  height: 252rpx;
  border-radius: 18rpx;
  overflow: hidden;
}

.banner-card {
  position: relative;
  height: 252rpx;
  border-radius: 18rpx;
  overflow: hidden;
  background: #f3f4f4;
}

.banner-image {
  width: 100%;
  height: 252rpx;
  display: block;
}

.banner-mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 56rpx 28rpx 24rpx;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0), rgba(0, 0, 0, 0.46));
}

.banner-title {
  color: #fff;
  font-size: 32rpx;
  font-weight: 700;
  line-height: 42rpx;
}

.banner-dots {
  position: absolute;
  right: 26rpx;
  bottom: 20rpx;
  display: flex;
  align-items: center;
}

.dot {
  width: 10rpx;
  height: 10rpx;
  margin-left: 8rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.55);
}

.dot.active {
  width: 22rpx;
  border-radius: 10rpx;
  background: #fff;
}
```

- [ ] **Step 4: 创建 `splashOverlay` 组件模板**

Create `91pai-master/components/custom/splashOverlay/index.vue`:

```vue
<template>
  <view v-if="visible" class="splash-overlay">
    <view class="skip" @tap="close">跳过 {{seconds}}s</view>
    <view class="brand">
      <image class="logo" src="/static/images/login/logo.png" mode="widthFix" />
      <view class="title">爱去拍</view>
      <view class="subtitle">安全约拍，发现更好的创作伙伴</view>
    </view>
    <view class="bottom-text">{{seconds}}s 后进入</view>
  </view>
</template>

<script src="./index.js"></script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
```

- [ ] **Step 5: 创建 `splashOverlay` 逻辑**

Create `91pai-master/components/custom/splashOverlay/index.js`:

```javascript
const STORAGE_KEY = 'splash_last_show_date'

function today() {
  const date = new Date()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

export default {
  name: 'splashOverlay',
  data() {
    return {
      visible: false,
      seconds: 3,
      timer: null,
    }
  },
  mounted() {
    const lastShowDate = uni.getStorageSync(STORAGE_KEY)
    if (lastShowDate === today()) return
    this.visible = true
    this.start()
  },
  beforeDestroy() {
    this.clear()
  },
  methods: {
    start() {
      this.clear()
      this.timer = setInterval(() => {
        this.seconds -= 1
        if (this.seconds <= 0) {
          this.close()
        }
      }, 1000)
    },
    clear() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },
    close() {
      uni.setStorageSync(STORAGE_KEY, today())
      this.visible = false
      this.clear()
    },
  },
}
```

- [ ] **Step 6: 创建 `splashOverlay` 样式**

Create `91pai-master/components/custom/splashOverlay/index.scss`:

```scss
.splash-overlay {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 9999;
  background: linear-gradient(160deg, #fff5f6 0%, #ff6b78 52%, #ff3f4f 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.skip {
  position: absolute;
  top: 70rpx;
  right: 34rpx;
  min-width: 132rpx;
  height: 54rpx;
  line-height: 54rpx;
  text-align: center;
  border-radius: 30rpx;
  color: #fff;
  font-size: 24rpx;
  background: rgba(0, 0, 0, 0.22);
}

.brand {
  text-align: center;
  color: #fff;
}

.logo {
  width: 154rpx;
  margin-bottom: 34rpx;
}

.title {
  font-size: 58rpx;
  font-weight: 800;
  line-height: 72rpx;
}

.subtitle {
  margin-top: 18rpx;
  font-size: 28rpx;
  line-height: 42rpx;
  color: rgba(255, 255, 255, 0.9);
}

.bottom-text {
  position: absolute;
  bottom: 82rpx;
  color: rgba(255, 255, 255, 0.86);
  font-size: 24rpx;
}
```

- [ ] **Step 7: 接入首页模板**

Modify `91pai-master/pages/home/home/index.vue` inside `<view class="content-box">` before `<view class="tip-box">`:

```vue
<splash-overlay></splash-overlay>
<home-banner :list="bannerList"></home-banner>
```

- [ ] **Step 8: 接入首页逻辑**

Modify `91pai-master/pages/home/home/index.js` imports:

```javascript
import homeBanner from "@/components/custom/homeBanner/index.vue";
import splashOverlay from "@/components/custom/splashOverlay/index.vue";
import { my_ypat_unread_count, banner_list } from "@/common/vmeitime-http";
```

Add to `components`:

```javascript
homeBanner,
splashOverlay,
```

Add to `data()`:

```javascript
bannerList: [],
```

Add to `onLoad()` after `uni.hideTabBar();`:

```javascript
this.loadBannerList();
```

Add method:

```javascript
async loadBannerList() {
  const res = await banner_list({ page: 0, size: 5 }).catch(() => null);
  if (res && res.code === 200 && res.res && res.res.content) {
    this.bannerList = res.res.content;
  } else if (res && res.code === 200 && Array.isArray(res.res)) {
    this.bannerList = res.res;
  } else {
    this.bannerList = [];
  }
},
```

- [ ] **Step 9: 调整首页样式**

Append to `91pai-master/pages/home/home/index.scss`:

```scss
.home-container {
  margin-top: 6rpx;
}
```

- [ ] **Step 10: 运行验证**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
node scripts/verify-91pai-work-pages.mjs
```

Expected(预期): FAIL(失败)，因为作品页和实名说明页尚未创建；不应再提示 banner(横幅) 或 splash(开屏) 组件缺失。

- [ ] **Step 11: 提交 Task 2**

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git add 91pai-master/components/custom/homeBanner 91pai-master/components/custom/splashOverlay 91pai-master/pages/home/home/index.vue 91pai-master/pages/home/home/index.js 91pai-master/pages/home/home/index.scss
git commit -m "feat: add home banner and splash overlay" -m "Scope: show published banners on old miniapp home and add skippable daily splash overlay." -m "Tested: node scripts/verify-91pai-work-pages.mjs fails only on remaining planned pages"
```

### Task 3: 实名认证说明页与入口

**Files(文件):**
- Create(新增): `91pai-master/pages/mine/realname/intro/index.vue`
- Create(新增): `91pai-master/pages/mine/realname/intro/index.js`
- Create(新增): `91pai-master/pages/mine/realname/intro/index.scss`
- Modify(修改): `91pai-master/pages.json`
- Modify(修改): `91pai-master/pages/mine/mine/index.js`
- Modify(修改): `91pai-master/pages/home/desc/index.js`

- [ ] **Step 1: 创建实名认证说明页模板**

Create `91pai-master/pages/mine/realname/intro/index.vue`:

```vue
<template>
  <view class="realname-intro-page">
    <view class="hero">
      <view class="cert-card">
        <view class="cert-icon">认证</view>
      </view>
      <view class="hero-title">开启身份认证</view>
      <view class="hero-subtitle">安全畅想更多服务</view>
    </view>

    <view class="benefit-grid">
      <view class="benefit-item" v-for="item in benefits" :key="item.title">
        <view class="benefit-icon">{{item.icon}}</view>
        <view class="benefit-title">{{item.title}}</view>
        <view class="benefit-desc">{{item.desc}}</view>
      </view>
    </view>

    <view class="bottom-safe"></view>
    <view class="footer">
      <button class="start-btn" @tap="start">开始实名</button>
    </view>
  </view>
</template>

<script src="./index.js"></script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
```

- [ ] **Step 2: 创建实名认证说明页逻辑**

Create `91pai-master/pages/mine/realname/intro/index.js`:

```javascript
export default {
  data() {
    return {
      options: {},
      benefits: [
        { icon: '约', title: '可约拍优质模特', desc: '优质模特须实名后可约拍' },
        { icon: '联', title: '可使用极速联系', desc: '专业客服一对一服务' },
        { icon: '刷', title: '可刷新约拍动态', desc: '刷新排名，获得更多曝光' },
        { icon: '标', title: '实名认证醒目标识', desc: '显著提高约拍成功率' },
      ],
    }
  },
  onLoad(options) {
    this.options = options || {}
  },
  onShow() {
    this.tui.commonFunc()
  },
  methods: {
    start() {
      const query = []
      if (this.options.ypatid) query.push(`ypatid=${this.options.ypatid}`)
      if (this.options.workId) query.push(`workId=${this.options.workId}`)
      const suffix = query.length ? `?${query.join('&')}` : ''
      uni.navigateTo({ url: `/pages/mine/realname/index${suffix}` })
    },
  },
}
```

- [ ] **Step 3: 创建实名认证说明页样式**

Create `91pai-master/pages/mine/realname/intro/index.scss`:

```scss
page {
  background: #fff;
}

.realname-intro-page {
  min-height: 100vh;
  padding-bottom: 160rpx;
  background: #fff;
}

.hero {
  height: 360rpx;
  text-align: center;
  padding-top: 64rpx;
  box-sizing: border-box;
  background: linear-gradient(180deg, #fff4f5 0%, #fff8f8 100%);
  overflow: hidden;
}

.cert-card {
  width: 292rpx;
  height: 88rpx;
  margin: 0 auto 42rpx;
  border-radius: 12rpx;
  background: linear-gradient(90deg, #ff5a67, #ff3f4f);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 34rpx rgba(255, 63, 79, 0.18);
}

.cert-icon {
  font-size: 42rpx;
  font-weight: 800;
}

.hero-title,
.hero-subtitle {
  color: #ff5361;
  font-size: 46rpx;
  line-height: 62rpx;
}

.benefit-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  row-gap: 72rpx;
  column-gap: 24rpx;
  padding: 86rpx 44rpx 40rpx;
}

.benefit-item {
  text-align: center;
}

.benefit-icon {
  width: 142rpx;
  height: 142rpx;
  margin: 0 auto 28rpx;
  border-radius: 50%;
  color: #ff6874;
  background: #f8f8f8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  font-weight: 800;
}

.benefit-title {
  color: #333;
  font-size: 32rpx;
  line-height: 46rpx;
}

.benefit-desc {
  color: #999;
  font-size: 26rpx;
  line-height: 40rpx;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 24rpx 60rpx 54rpx;
  background: #fff;
}

.start-btn {
  height: 92rpx;
  line-height: 92rpx;
  color: #fff;
  font-size: 34rpx;
  font-weight: 700;
  border-radius: 50rpx;
  background: linear-gradient(90deg, #ff6b78, #ff3f4f);
  box-shadow: 0 12rpx 24rpx rgba(255, 63, 79, 0.22);
}
```

- [ ] **Step 4: 注册页面**

Modify `91pai-master/pages.json` inside `pages` array near existing realname page:

```json
{
  "path": "pages/mine/realname/intro/index",
  "style": {
    "navigationBarBackgroundColor": "#fff",
    "navigationBarTextStyle": "black",
    "backgroundColorTop": "#fff",
    "backgroundColorBottom": "#fff",
    "navigationBarTitleText": "实名认证"
  }
}
```

- [ ] **Step 5: 我的页实名认证入口跳转说明页**

Find the navigation method in `91pai-master/pages/mine/mine/index.js` that routes to `/pages/mine/realname/index` and replace only that URL with:

```javascript
"/pages/mine/realname/intro/index"
```

If the file uses a list object, set that list item URL to:

```javascript
url: "/pages/mine/realname/intro/index"
```

- [ ] **Step 6: 旧约拍详情门槛弹窗跳转说明页**

Modify `91pai-master/pages/home/desc/index.js` in `orderShe()` where `this.pathName = "/pages/mine/realname/index";` appears:

```javascript
this.pathName = "/pages/mine/realname/intro/index";
```

- [ ] **Step 7: 运行验证**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
node scripts/verify-91pai-work-pages.mjs
```

Expected(预期): FAIL(失败)，因为作品页面尚未创建；不应再提示实名认证说明页缺失。

- [ ] **Step 8: 提交 Task 3**

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git add 91pai-master/pages/mine/realname/intro 91pai-master/pages.json 91pai-master/pages/mine/mine/index.js 91pai-master/pages/home/desc/index.js
git commit -m "feat: add realname intro page" -m "Scope: add certification benefit page and route existing realname entry points through it." -m "Tested: node scripts/verify-91pai-work-pages.mjs fails only on remaining planned work pages"
```

### Task 4: 作品详情页

**Files(文件):**
- Create(新增): `91pai-master/pages/work/detail/index.vue`
- Create(新增): `91pai-master/pages/work/detail/index.js`
- Create(新增): `91pai-master/pages/work/detail/index.scss`
- Modify(修改): `91pai-master/pages.json`

- [ ] **Step 1: 创建作品详情模板**

Create `91pai-master/pages/work/detail/index.vue`:

```vue
<template>
  <view class="work-detail-page">
    <tui-loading :visible="loading"></tui-loading>
    <view v-if="content.id" class="content">
      <view class="media-list">
        <image
          v-for="(url,index) in mediaUrls"
          :key="url + index"
          class="media-image"
          :src="url"
          mode="widthFix"
          :data-index="index"
          @tap="previewImage"
        />
      </view>
      <view class="author-card">
        <image class="avatar" :src="authorAvatar" mode="aspectFill" />
        <view class="author-main">
          <view class="nickname">{{authorName}}</view>
          <view class="meta">{{authorMeta}}</view>
        </view>
        <view class="complain" @tap="goComplain">投诉</view>
      </view>
      <view class="description">{{content.description || '这个作品暂时没有描述'}}</view>
      <view class="bottom-space"></view>
    </view>
    <view v-else-if="!loading" class="empty">
      <view class="empty-title">作品不存在或已下架</view>
      <view class="empty-btn" @tap="loadDetail">重新加载</view>
    </view>
    <view v-if="content.id && !isSelf" class="bottom-bar">
      <view class="icon-action" @tap="toggleLike">
        <tui-icon :name="liked ? 'agree-fill' : 'agree'" :size="28" :color="liked ? '#ff5361' : '#333'"></tui-icon>
      </view>
      <view class="icon-action" @tap="toggleFavorite">
        <tui-icon :name="favorited ? 'star-fill' : 'star'" :size="28" :color="favorited ? '#ff5361' : '#333'"></tui-icon>
      </view>
      <button class="share-action" open-type="share">
        <tui-icon name="share" :size="28" color="#333"></tui-icon>
      </button>
      <view class="apply-btn" @tap="goApply">立即约拍</view>
    </view>
  </view>
</template>

<script src="./index.js"></script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
```

- [ ] **Step 2: 创建作品详情逻辑**

Create `91pai-master/pages/work/detail/index.js`:

```javascript
import localStorageObj from "@/common/localStorage";
import { getUserInfo } from "@/common/utils";
import {
  work_get,
  work_like_add,
  work_like_cancel,
  work_sc_add,
  work_sc_cancel,
} from "@/common/vmeitime-http";

export default {
  data() {
    return {
      id: "",
      loading: false,
      content: {},
      userInfo: {},
      liked: false,
      favorited: false,
    };
  },
  computed: {
    mediaUrls() {
      const mediaList = this.content.mediaList || this.content.medias || [];
      const fromList = mediaList.map((item) => item.url || item.mediaUrl || item.path).filter(Boolean);
      if (fromList.length) return fromList;
      if (this.content.coverUrl) return [this.content.coverUrl];
      if (this.content.pics && Array.isArray(this.content.pics)) return this.content.pics;
      return [];
    },
    author() {
      return this.content.user || this.content.userQo || {};
    },
    authorAvatar() {
      return this.author.imgpath || this.author.avatar || "/static/images/mine/mine_def_touxiang_3x.png";
    },
    authorName() {
      return this.author.nickname || this.author.name || "爱去拍用户";
    },
    authorMeta() {
      const profess = this.author.professTxt || this.author.professName || "创作者";
      const city = this.author.city || this.content.city || "";
      return city ? `${profess} | ${city}` : profess;
    },
    isSelf() {
      const authorId = `${this.author.id || this.content.userid || ""}`;
      return authorId && this.userInfo && `${this.userInfo.id}` === authorId;
    },
  },
  onLoad(options) {
    this.id = options.id || options.workId || "";
    this.loadDetail();
  },
  async onShow() {
    this.tui.commonFunc();
    this.userInfo = await getUserInfo().catch(() => ({}));
  },
  methods: {
    ensureLogin() {
      if (uni.getStorageSync(localStorageObj.token)) return true;
      uni.navigateTo({ url: "/pages/login/login/index" });
      return false;
    },
    async loadDetail() {
      if (!this.id) return;
      this.loading = true;
      const res = await work_get({ id: this.id }).catch(() => null);
      this.loading = false;
      if (res && res.code === 200) {
        this.content = res.res || {};
        this.liked = this.content.likeflag === "1" || this.content.liked === true;
        this.favorited = this.content.colflag === "1" || this.content.favoriteflag === "1" || this.content.favorited === true;
      } else if (res && res.msg) {
        this.tui.toast(res.msg);
      }
    },
    async toggleLike() {
      if (!this.ensureLogin()) return;
      const next = !this.liked;
      this.liked = next;
      const api = next ? work_like_add : work_like_cancel;
      const res = await api({ workId: this.id }).catch(() => null);
      if (!res || res.code !== 200) {
        this.liked = !next;
        this.tui.toast((res && res.msg) || "操作失败");
      }
    },
    async toggleFavorite() {
      if (!this.ensureLogin()) return;
      const next = !this.favorited;
      this.favorited = next;
      const api = next ? work_sc_add : work_sc_cancel;
      const res = await api({ workId: this.id }).catch(() => null);
      if (!res || res.code !== 200) {
        this.favorited = !next;
        this.tui.toast((res && res.msg) || "操作失败");
      }
    },
    goComplain() {
      if (!this.ensureLogin()) return;
      uni.navigateTo({ url: `/pages/work/complain/index?workId=${this.id}` });
    },
    goApply() {
      if (!this.ensureLogin()) return;
      uni.navigateTo({ url: `/pages/work/apply/index?workId=${this.id}` });
    },
    previewImage(e) {
      const index = e.currentTarget.dataset.index;
      uni.previewImage({
        current: this.mediaUrls[index],
        urls: this.mediaUrls,
      });
    },
  },
  onShareAppMessage() {
    return {
      title: `${this.authorName}的作品，快来爱去拍看看`,
      imageUrl: this.mediaUrls[0],
      path: `/pages/work/detail/index?id=${this.id}`,
    };
  },
};
```

- [ ] **Step 3: 创建作品详情样式**

Create `91pai-master/pages/work/detail/index.scss`:

```scss
page {
  background: #f7f7f7;
}

.work-detail-page {
  min-height: 100vh;
  padding-bottom: 130rpx;
}

.media-list {
  padding: 0 40rpx;
  background: #fff;
}

.media-image {
  width: 100%;
  display: block;
  margin-bottom: 14rpx;
  border-radius: 6rpx;
}

.author-card {
  display: flex;
  align-items: center;
  margin: 18rpx 24rpx 0;
  padding: 24rpx;
  border-radius: 8rpx;
  background: #fff;
}

.avatar {
  width: 84rpx;
  height: 84rpx;
  border-radius: 50%;
  background: #f3f4f4;
}

.author-main {
  flex: 1;
  min-width: 0;
  margin-left: 20rpx;
}

.nickname {
  color: #222;
  font-size: 34rpx;
  font-weight: 700;
  line-height: 44rpx;
}

.meta {
  margin-top: 8rpx;
  color: #888;
  font-size: 26rpx;
  line-height: 36rpx;
}

.complain {
  color: #ff5361;
  font-size: 26rpx;
}

.description {
  margin: 18rpx 24rpx 0;
  padding: 26rpx;
  color: #333;
  font-size: 28rpx;
  line-height: 44rpx;
  border-radius: 8rpx;
  background: #fff;
}

.empty {
  padding-top: 220rpx;
  text-align: center;
}

.empty-title {
  color: #999;
  font-size: 30rpx;
}

.empty-btn {
  width: 220rpx;
  height: 70rpx;
  line-height: 70rpx;
  margin: 30rpx auto 0;
  border-radius: 40rpx;
  color: #fff;
  background: #ff5361;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 128rpx;
  padding: 18rpx 30rpx 34rpx;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  background: #fff;
  box-shadow: 0 -6rpx 20rpx rgba(0, 0, 0, 0.04);
}

.icon-action,
.share-action {
  width: 82rpx;
  height: 82rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  margin: 0 8rpx 0 0;
  background: transparent;
}

.share-action::after {
  border: 0;
}

.apply-btn {
  flex: 1;
  height: 82rpx;
  line-height: 82rpx;
  margin-left: 24rpx;
  border-radius: 48rpx;
  color: #fff;
  text-align: center;
  font-size: 34rpx;
  font-weight: 700;
  background: linear-gradient(90deg, #ff6b78, #ff3f4f);
}
```

- [ ] **Step 4: 注册作品详情页**

Modify `91pai-master/pages.json` and add:

```json
{
  "path": "pages/work/detail/index",
  "style": {
    "navigationBarBackgroundColor": "#fff",
    "navigationBarTextStyle": "black",
    "backgroundColorTop": "#fff",
    "backgroundColorBottom": "#F7F7F7",
    "navigationBarTitleText": "作品详情",
    "onReachBottomDistance": 50
  }
}
```

- [ ] **Step 5: 运行验证**

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
node scripts/verify-91pai-work-pages.mjs
```

Expected(预期): FAIL(失败)，因为投诉页和约拍页尚未创建。

- [ ] **Step 6: 提交 Task 4**

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git add 91pai-master/pages/work/detail 91pai-master/pages.json
git commit -m "feat: add work detail page" -m "Scope: add old miniapp work detail media view with like, favorite, share, complaint, and apply entry points." -m "Tested: node scripts/verify-91pai-work-pages.mjs fails only on remaining planned complain/apply pages"
```

### Task 5: 作品投诉页与作品约拍页

**Files(文件):**
- Create(新增): `91pai-master/pages/work/complain/index.vue`
- Create(新增): `91pai-master/pages/work/complain/index.js`
- Create(新增): `91pai-master/pages/work/complain/index.scss`
- Create(新增): `91pai-master/pages/work/apply/index.vue`
- Create(新增): `91pai-master/pages/work/apply/index.js`
- Create(新增): `91pai-master/pages/work/apply/index.scss`
- Modify(修改): `91pai-master/pages.json`

- [ ] **Step 1: 创建投诉页模板**

Create `91pai-master/pages/work/complain/index.vue`:

```vue
<template>
  <view class="complain-page">
    <view class="section">
      <view class="section-title">请选择投诉原因</view>
      <view class="reason-grid">
        <view
          v-for="item in reasons"
          :key="item"
          :class="['reason-item', reason === item ? 'active' : '']"
          @tap="reason = item"
        >{{item}}</view>
      </view>
    </view>
    <view class="section">
      <textarea
        class="content-input"
        v-model="content"
        maxlength="300"
        placeholder="请输入您的投诉内容"
        placeholder-class="placeholder"
      />
    </view>
    <view class="section">
      <view class="section-title">证据截图</view>
      <view class="image-list">
        <view v-for="(image,index) in images" :key="image" class="image-wrap">
          <image class="evidence" :src="image" mode="aspectFill" @tap="preview(index)" />
          <view class="remove" @tap="remove(index)">×</view>
        </view>
        <view v-if="images.length < 3" class="add-image" @tap="chooseImage">+</view>
      </view>
    </view>
    <view class="black-btn" @tap="confirmBlack">拉黑TA</view>
    <view class="footer">
      <view class="submit-btn" @tap="submit">提交</view>
    </view>
  </view>
</template>

<script src="./index.js"></script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
```

- [ ] **Step 2: 创建投诉页逻辑**

Create `91pai-master/pages/work/complain/index.js`:

```javascript
import { work_complain } from "@/common/vmeitime-http";

export default {
  data() {
    return {
      workId: "",
      reason: "",
      content: "",
      images: [],
      reasons: ["盗图", "欺诈", "色情", "不实信息", "骚扰", "其他"],
    };
  },
  onLoad(options) {
    this.workId = options.workId || "";
  },
  onShow() {
    this.tui.commonFunc();
  },
  methods: {
    chooseImage() {
      uni.chooseImage({
        count: 3 - this.images.length,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          this.images = this.images.concat(res.tempFilePaths || []).slice(0, 3);
        },
      });
    },
    remove(index) {
      this.images.splice(index, 1);
    },
    preview(index) {
      uni.previewImage({
        current: this.images[index],
        urls: this.images,
      });
    },
    confirmBlack() {
      uni.showModal({
        title: "确认加入黑名单？",
        content: "加入黑名单后，对方将不能给你发送约拍请求和极速联系信息",
        confirmText: "确认拉黑",
        confirmColor: "#1989fa",
      });
    },
    async submit() {
      if (!this.reason) {
        this.tui.toast("请选择投诉原因");
        return;
      }
      if (!this.content && this.images.length === 0) {
        this.tui.toast("请填写投诉内容或上传证据截图");
        return;
      }
      const res = await work_complain({
        workId: this.workId,
        reason: this.reason,
        content: this.content,
        pics: this.images,
      }).catch(() => null);
      if (res && res.code === 200) {
        this.tui.toast("投诉已提交，平台会尽快处理");
        setTimeout(() => uni.navigateBack(), 800);
      } else {
        this.tui.toast((res && res.msg) || "提交失败，请稍后再试");
      }
    },
  },
};
```

- [ ] **Step 3: 创建投诉页样式**

Create `91pai-master/pages/work/complain/index.scss`:

```scss
page {
  background: #f7f7f7;
}

.complain-page {
  min-height: 100vh;
  padding-bottom: 180rpx;
}

.section {
  margin-top: 18rpx;
  padding: 28rpx 28rpx 34rpx;
  background: #fff;
}

.section-title {
  color: #333;
  font-size: 32rpx;
  font-weight: 700;
  margin-bottom: 24rpx;
}

.reason-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 22rpx 24rpx;
}

.reason-item {
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 8rpx;
  text-align: center;
  color: #333;
  font-size: 30rpx;
  background: #f0f0f0;
}

.reason-item.active {
  color: #fff;
  background: #ff5361;
}

.content-input {
  width: 100%;
  height: 260rpx;
  padding: 26rpx;
  box-sizing: border-box;
  border-radius: 8rpx;
  color: #333;
  font-size: 30rpx;
  line-height: 42rpx;
  background: #f5f5f5;
}

.placeholder {
  color: #aaa;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
}

.image-wrap,
.add-image {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin-right: 24rpx;
  margin-bottom: 24rpx;
  border-radius: 8rpx;
  background: #f0f0f0;
}

.evidence {
  width: 160rpx;
  height: 160rpx;
  border-radius: 8rpx;
}

.remove {
  position: absolute;
  right: -12rpx;
  top: -12rpx;
  width: 36rpx;
  height: 36rpx;
  line-height: 34rpx;
  text-align: center;
  color: #fff;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
}

.add-image {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d8d8d8;
  font-size: 86rpx;
}

.black-btn {
  margin: 28rpx;
  color: #333;
  font-size: 30rpx;
  text-align: center;
  text-decoration: underline;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 28rpx 92rpx 54rpx;
  background: #fff;
}

.submit-btn {
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 48rpx;
  text-align: center;
  color: #fff;
  font-size: 34rpx;
  background: linear-gradient(90deg, #ff6b78, #ff3f4f);
}
```

- [ ] **Step 4: 创建约拍页模板**

Create `91pai-master/pages/work/apply/index.vue`:

```vue
<template>
  <view class="apply-page">
    <view class="safe-bar">安全提醒：切勿独自与陌生人在室内约拍，切勿私下发送私密照片。</view>
    <view class="card">
      <view class="card-title">约拍理由</view>
      <textarea class="reason-input" v-model="reason" maxlength="120" placeholder="请输入约拍理由～" />
    </view>
    <view class="card">
      <view class="card-title">我的联系方式</view>
      <view class="contact-row">手机号：{{userInfo.mobile || '未填写'}}</view>
      <view class="contact-row">微信号：<input class="wx-input" v-model="wx" placeholder="请输入微信号" /></view>
    </view>
    <view class="tips">
      温馨提示：平台禁止一切色情私房约拍；切勿轻易相信未见面先交纳费用或定金的合作。
    </view>
    <view class="footer">
      <view class="cost">
        <view class="cost-num">5 麻豆</view>
        <view class="cost-left">提交约拍申请</view>
      </view>
      <view class="submit-btn" @tap="submit">确认提交</view>
    </view>
  </view>
</template>

<script src="./index.js"></script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
```

- [ ] **Step 5: 创建约拍页逻辑**

Create `91pai-master/pages/work/apply/index.js`:

```javascript
import { getUserInfo } from "@/common/utils";
import { work_quick_apply } from "@/common/vmeitime-http";

const SAFE_TIPS_KEY = "work_apply_safe_tips_until";

export default {
  data() {
    return {
      workId: "",
      reason: "",
      wx: "",
      userInfo: {},
    };
  },
  onLoad(options) {
    this.workId = options.workId || "";
    this.showSafeTips();
  },
  async onShow() {
    this.tui.commonFunc();
    this.userInfo = await getUserInfo().catch(() => ({}));
    this.wx = this.userInfo.wx || "";
  },
  methods: {
    showSafeTips() {
      const until = Number(uni.getStorageSync(SAFE_TIPS_KEY) || 0);
      if (until > Date.now()) return;
      uni.showModal({
        title: "安全防骗提醒",
        content: "①切勿独自与陌生人在室内约拍！②切勿私下发送私密隐私照片！③未见面先收费的合作，切勿相信！",
        confirmText: "我知道了",
        showCancel: true,
        cancelText: "近期不再提醒",
        confirmColor: "#ff5361",
        success: (res) => {
          if (res.cancel) {
            uni.setStorageSync(SAFE_TIPS_KEY, Date.now() + 7 * 24 * 60 * 60 * 1000);
          }
        },
      });
    },
    async submit() {
      if (!this.reason) {
        this.tui.toast("请输入约拍理由");
        return;
      }
      if (!this.userInfo.mobile && !this.wx) {
        this.tui.toast("请完善联系方式");
        return;
      }
      const res = await work_quick_apply({
        workId: this.workId,
        reason: this.reason,
        mobile: this.userInfo.mobile,
        wx: this.wx,
      }).catch(() => null);
      if (res && res.code === 200) {
        uni.redirectTo({ url: "/pages/home/success/index?status=98" });
      } else {
        this.tui.toast((res && res.msg) || "提交失败，请稍后再试");
      }
    },
  },
};
```

- [ ] **Step 6: 创建约拍页样式**

Create `91pai-master/pages/work/apply/index.scss`:

```scss
page {
  background: #f7f7f7;
}

.apply-page {
  min-height: 100vh;
  padding-bottom: 150rpx;
}

.safe-bar {
  padding: 24rpx 32rpx;
  color: #ff5361;
  font-size: 28rpx;
  line-height: 42rpx;
  background: #fff1f2;
}

.card {
  margin: 18rpx 24rpx 0;
  padding: 28rpx;
  border-radius: 8rpx;
  background: #fff;
}

.card-title {
  color: #333;
  font-size: 32rpx;
  font-weight: 700;
  margin-bottom: 22rpx;
}

.reason-input {
  width: 100%;
  height: 260rpx;
  padding: 26rpx;
  box-sizing: border-box;
  border-radius: 8rpx;
  color: #333;
  font-size: 30rpx;
  background: #f5f6f8;
}

.contact-row {
  display: flex;
  align-items: center;
  min-height: 58rpx;
  color: #444;
  font-size: 30rpx;
  line-height: 44rpx;
}

.wx-input {
  flex: 1;
  min-width: 0;
  color: #333;
  font-size: 30rpx;
}

.tips {
  padding: 28rpx 42rpx;
  color: #999;
  font-size: 26rpx;
  line-height: 42rpx;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 132rpx;
  padding: 22rpx 34rpx 36rpx;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  background: #fff;
}

.cost {
  width: 220rpx;
}

.cost-num {
  color: #ff5361;
  font-size: 38rpx;
  font-weight: 800;
  line-height: 48rpx;
}

.cost-left {
  color: #999;
  font-size: 24rpx;
}

.submit-btn {
  flex: 1;
  height: 86rpx;
  line-height: 86rpx;
  border-radius: 48rpx;
  color: #fff;
  text-align: center;
  font-size: 34rpx;
  font-weight: 700;
  background: linear-gradient(90deg, #ff6b78, #ff3f4f);
}
```

- [ ] **Step 7: 注册投诉页和约拍页**

Modify `91pai-master/pages.json` and add:

```json
{
  "path": "pages/work/complain/index",
  "style": {
    "navigationBarBackgroundColor": "#fff",
    "navigationBarTextStyle": "black",
    "backgroundColorTop": "#F7F7F7",
    "backgroundColorBottom": "#F7F7F7",
    "navigationBarTitleText": "我要投诉"
  }
},
{
  "path": "pages/work/apply/index",
  "style": {
    "navigationBarBackgroundColor": "#fff",
    "navigationBarTextStyle": "black",
    "backgroundColorTop": "#F7F7F7",
    "backgroundColorBottom": "#F7F7F7",
    "navigationBarTitleText": "我要约拍"
  }
}
```

- [ ] **Step 8: 运行旧小程序验证和构建**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
node scripts/verify-91pai-work-pages.mjs
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/91pai-master
npm run build:h5
```

Expected(预期): 验证脚本 PASS(通过)；构建 PASS(通过)。

- [ ] **Step 9: 提交 Task 5**

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git add 91pai-master/pages/work/complain 91pai-master/pages/work/apply 91pai-master/pages.json
git commit -m "feat: add work complaint and apply pages" -m "Scope: add old miniapp complaint form, black confirmation, safety reminder, and quick apply page." -m "Tested: node scripts/verify-91pai-work-pages.mjs; npm run build:h5"
```

### Task 6: 后端后台投诉治理接口

**Files(文件):**
- Create(新增): `backend/system-wap/src/main/java/com/ypat/service/WorkComplainAdminServiceClient.java`
- Create(新增): `backend/system-wap/src/main/java/com/ypat/controller/AdminWorkComplainController.java`
- Create(新增): `backend/system-wap/src/test/java/com/ypat/controller/AdminWorkComplainControllerSourceTest.java`
- Modify(修改): `backend/system-wap/src/main/java/com/ypat/config/WebSecurityConfig.java`

- [ ] **Step 1: 写 source test(源码测试)**

Create `backend/system-wap/src/test/java/com/ypat/controller/AdminWorkComplainControllerSourceTest.java`:

```java
package com.ypat.controller;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminWorkComplainControllerSourceTest {
    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) {
            path = Paths.get("..").resolve(file);
        }
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    @Test
    public void adminWorkComplainControllerExposesListDetailAndHandle() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/controller/AdminWorkComplainController.java");
        assertTrue(source.contains("@RequestMapping(\"/admin/work/complain\")"));
        assertTrue(source.contains("@GetMapping(\"/list\")"));
        assertTrue(source.contains("@GetMapping(\"/detail\")"));
        assertTrue(source.contains("@PostMapping(\"/handle\")"));
        assertTrue(source.contains("throw new SysException(ResponseCode.FAIL_PARA"));
    }

    @Test
    public void adminWorkComplainClientCallsServiceEndpoints() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/service/WorkComplainAdminServiceClient.java");
        assertTrue(source.contains("/service/work/complain/admin/list"));
        assertTrue(source.contains("/service/work/complain/admin/detail"));
        assertTrue(source.contains("/service/work/complain/admin/handle"));
    }

    @Test
    public void adminComplaintEndpointsAreNotAnonymous() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/config/WebSecurityConfig.java");
        assertFalse(source.contains("\"/admin/work/complain"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/backend
mvn -pl system-wap -Dtest=AdminWorkComplainControllerSourceTest test
```

Expected(预期): FAIL(失败)，因为 controller(控制器) 和 client(客户端) 尚不存在。

- [ ] **Step 3: 创建 Feign(声明式 HTTP 客户端)**

Create `backend/system-wap/src/main/java/com/ypat/service/WorkComplainAdminServiceClient.java`:

```java
package com.ypat.service;

import com.ypat.WorkComplainQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "system-domain")
public interface WorkComplainAdminServiceClient {
    @GetMapping("/service/work/complain/admin/list")
    String list(@RequestParam(value = "page", required = false) Integer page,
                @RequestParam(value = "size", required = false) Integer size,
                @RequestParam(value = "status", required = false) String status,
                @RequestParam(value = "workId", required = false) String workId,
                @RequestParam(value = "userId", required = false) String userId);

    @GetMapping("/service/work/complain/admin/detail")
    String detail(@RequestParam("id") Long id);

    @PostMapping("/service/work/complain/admin/handle")
    String handle(@RequestBody WorkComplainQo qo);
}
```

- [ ] **Step 4: 创建管理端投诉 controller(控制器)**

Create `backend/system-wap/src/main/java/com/ypat/controller/AdminWorkComplainController.java`:

```java
package com.ypat.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.WorkComplainQo;
import com.ypat.service.WorkComplainAdminServiceClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/work/complain")
public class AdminWorkComplainController {
    @Autowired
    private WorkComplainAdminServiceClient workComplainAdminServiceClient;

    @GetMapping("/list")
    public ResponseApiBody list(@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
                                @RequestParam(value = "status", required = false) String status,
                                @RequestParam(value = "workId", required = false) String workId,
                                @RequestParam(value = "userId", required = false) String userId) {
        String json = workComplainAdminServiceClient.list(page, size, status, workId, userId);
        JsonElement data = JsonParser.parseString(json);
        return ResponseApiBody.success(data);
    }

    @GetMapping("/detail")
    public ResponseApiBody detail(@RequestParam("id") Long id) {
        if (id == null) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        String json = workComplainAdminServiceClient.detail(id);
        JsonElement data = JsonParser.parseString(json);
        return ResponseApiBody.success(data);
    }

    @PostMapping("/handle")
    public ResponseApiBody handle(@RequestParam("id") Long id,
                                  @RequestParam("status") String status,
                                  @RequestParam(value = "reason", required = false) String reason,
                                  @RequestParam(value = "offlineWork", required = false, defaultValue = "false") Boolean offlineWork) {
        if (id == null || StringUtils.isBlank(status)) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        WorkComplainQo qo = new WorkComplainQo();
        qo.setId(id);
        qo.setStatus(status);
        qo.setReason(reason);
        qo.setOfflineWork(offlineWork);
        String json = workComplainAdminServiceClient.handle(qo);
        JsonElement data = JsonParser.parseString(json);
        return ResponseApiBody.success(data);
    }
}
```

- [ ] **Step 5: 补齐 `WorkComplainQo(作品投诉请求对象)` 字段**

If `backend/system-object/src/main/java/com/ypat/WorkComplainQo.java` does not contain `id`、`status`、`reason`、`offlineWork` getters/setters, add these fields:

```java
private Long id;
private String status;
private String reason;
private Boolean offlineWork;

public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

public String getReason() {
    return reason;
}

public void setReason(String reason) {
    this.reason = reason;
}

public Boolean getOfflineWork() {
    return offlineWork;
}

public void setOfflineWork(Boolean offlineWork) {
    this.offlineWork = offlineWork;
}
```

- [ ] **Step 6: 运行后端测试**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/backend
mvn -pl system-wap -Dtest=AdminWorkComplainControllerSourceTest test
```

Expected(预期): PASS(通过)。

- [ ] **Step 7: 提交 Task 6**

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git add backend/system-wap/src/main/java/com/ypat/controller/AdminWorkComplainController.java backend/system-wap/src/main/java/com/ypat/service/WorkComplainAdminServiceClient.java backend/system-wap/src/test/java/com/ypat/controller/AdminWorkComplainControllerSourceTest.java backend/system-object/src/main/java/com/ypat/WorkComplainQo.java
git commit -m "feat: add admin work complaint endpoints" -m "Scope: expose admin complaint list, detail, and handle endpoints through system-wap." -m "Tested: mvn -pl system-wap -Dtest=AdminWorkComplainControllerSourceTest test"
```

### Task 7: 管理后台投诉治理页

**Files(文件):**
- Create(新增): `frontend-admin/src/api/modules/work-complain.ts`
- Create(新增): `frontend-admin/tests/unit/work-complain-api.test.ts`
- Create(新增): `frontend-admin/src/views/manage/work-complain-list/index.vue`
- Modify(修改): `frontend-admin/src/constants/menu.ts`
- Modify(修改): `frontend-admin/src/stores/modules/permission.ts`

- [ ] **Step 1: 写 API(接口) 单测**

Create `frontend-admin/tests/unit/work-complain-api.test.ts`:

```typescript
import { describe, expect, it, vi } from 'vitest'

vi.mock('@/api/request', () => ({
  get: vi.fn((url: string, params?: Record<string, unknown>) => Promise.resolve({ url, params })),
  post: vi.fn((url: string, data?: unknown, options?: unknown) => Promise.resolve({ url, data, options })),
}))

describe('work complain api', () => {
  it('requests admin complaint list', async () => {
    const { getWorkComplainList } = await import('@/api/modules/work-complain')
    const res = await getWorkComplainList({ page: 0, size: 10, status: 'pending' })
    expect(res).toMatchObject({ url: '/admin/work/complain/list', params: { page: 0, size: 10, status: 'pending' } })
  })

  it('handles complaint with params', async () => {
    const { handleWorkComplain } = await import('@/api/modules/work-complain')
    const res = await handleWorkComplain(12, 'handled', '证据属实', true)
    expect(res).toMatchObject({
      url: '/admin/work/complain/handle',
      options: { params: { id: 12, status: 'handled', reason: '证据属实', offlineWork: true } },
    })
  })
})
```

- [ ] **Step 2: 运行 API(接口) 单测确认失败**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend-admin
pnpm vitest run tests/unit/work-complain-api.test.ts
```

Expected(预期): FAIL(失败)，模块不存在。

- [ ] **Step 3: 创建 API(接口) 模块**

Create `frontend-admin/src/api/modules/work-complain.ts`:

```typescript
import { get, post } from '../request'
import type { PageQuery, PageResult } from '../types'

export interface WorkComplainInfo {
  id: number
  workId?: number | string
  userId?: number | string
  targetUserId?: number | string
  reason?: string
  content?: string
  pics?: string[]
  status?: string
  createdAt?: string
  handleReason?: string
}

export interface WorkComplainListQuery extends PageQuery {
  status?: string
  workId?: string
  userId?: string
}

export function getWorkComplainList(params: WorkComplainListQuery) {
  return get<PageResult<WorkComplainInfo>>('/admin/work/complain/list', params as Record<string, unknown>)
}

export function getWorkComplainDetail(id: number) {
  return get<WorkComplainInfo>('/admin/work/complain/detail', { id })
}

export function handleWorkComplain(id: number, status: string, reason?: string, offlineWork = false) {
  return post('/admin/work/complain/handle', undefined, {
    params: { id, status, reason, offlineWork },
  })
}
```

- [ ] **Step 4: 创建投诉治理页面**

Create `frontend-admin/src/views/manage/work-complain-list/index.vue`:

```vue
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWorkComplainList, handleWorkComplain, type WorkComplainInfo, type WorkComplainListQuery } from '@/api/modules/work-complain'

const query = reactive<WorkComplainListQuery>({ page: 0, size: 10, status: '', workId: '', userId: '' })
const list = ref<WorkComplainInfo[]>([])
const total = ref(0)
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    const res = await getWorkComplainList(query)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 0
  fetchList()
}

function reset() {
  query.status = ''
  query.workId = ''
  query.userId = ''
  query.page = 0
  fetchList()
}

async function handle(row: WorkComplainInfo, status: string) {
  const offlineWork = status === 'handled'
  const reason = await ElMessageBox.prompt('请输入处理备注', '处理投诉', {
    inputType: 'textarea',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  })
  await handleWorkComplain(row.id, status, reason.value, offlineWork)
  ElMessage.success('处理成功')
  fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div class="work-complain-page">
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="状态"><el-input v-model="query.status" clearable placeholder="请输入状态" /></el-form-item>
        <el-form-item label="作品ID"><el-input v-model="query.workId" clearable placeholder="请输入作品ID" /></el-form-item>
        <el-form-item label="投诉人ID"><el-input v-model="query.userId" clearable placeholder="请输入用户ID" /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="90" align="center" />
      <el-table-column prop="workId" label="作品ID" width="120" align="center" />
      <el-table-column prop="userId" label="投诉人" width="120" align="center" />
      <el-table-column prop="reason" label="原因" width="140" />
      <el-table-column prop="content" label="投诉内容" min-width="240" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="120" align="center" />
      <el-table-column label="证据" width="160">
        <template #default="{ row }">
          <el-image
            v-for="url in ((row as WorkComplainInfo).pics || [])"
            :key="url"
            :src="url"
            :preview-src-list="(row as WorkComplainInfo).pics || []"
            preview-teleported
            fit="cover"
            class="evidence"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handle(row as WorkComplainInfo, 'handled')">处理</el-button>
          <el-button type="info" link size="small" @click="handle(row as WorkComplainInfo, 'rejected')">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        :current-page="(query.page || 0) + 1"
        :page-size="query.size"
        :total="total"
        layout="total,prev,pager,next"
        background
        @current-change="(page: number) => { query.page = page - 1; fetchList() }"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.evidence {
  width: 42px;
  height: 42px;
  margin-right: 6px;
  border-radius: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}
</style>
```

- [ ] **Step 5: 注册菜单和权限组件**

Modify `frontend-admin/src/constants/menu.ts` under work/manage children:

```typescript
{ title: '作品投诉', path: '/manage/work-complain/index', component: 'manage/work-complain-list/index' },
```

Modify `frontend-admin/src/stores/modules/permission.ts` imports:

```typescript
import ManageWorkComplainList from '@/views/manage/work-complain-list/index.vue'
```

Add to component map:

```typescript
'manage/work-complain-list/index': ManageWorkComplainList,
```

- [ ] **Step 6: 运行后台测试与类型检查**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend-admin
pnpm vitest run tests/unit/work-complain-api.test.ts
pnpm type-check
```

Expected(预期): PASS(通过)。

- [ ] **Step 7: 提交 Task 7**

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git add frontend-admin/src/api/modules/work-complain.ts frontend-admin/tests/unit/work-complain-api.test.ts frontend-admin/src/views/manage/work-complain-list frontend-admin/src/constants/menu.ts frontend-admin/src/stores/modules/permission.ts
git commit -m "feat: add admin work complaint page" -m "Scope: add admin API wrapper, menu entry, permission component mapping, and complaint governance page." -m "Tested: pnpm vitest run tests/unit/work-complain-api.test.ts; pnpm type-check"
```

### Task 8: 全量验证与收尾

**Files(文件):**
- Inspect(检查): `docs/superpowers/specs/2026-07-07-home-work-realname-splash-design.md`
- Inspect(检查): `docs/superpowers/plans/2026-07-07-home-work-realname-splash.md`
- No production file changes unless verification exposes a defect.

- [ ] **Step 1: 检查工作区只包含本计划相关改动**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git status --short
```

Expected(预期): 只看到本计划未提交文件，或看到用户既有无关改动但不被 stage(暂存)。

- [ ] **Step 2: 运行旧小程序验证**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
node scripts/verify-91pai-work-pages.mjs
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/91pai-master
npm run build:h5
```

Expected(预期): PASS(通过)。

- [ ] **Step 3: 运行后端验证**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/backend
mvn -pl system-wap -Dtest=AdminWorkComplainControllerSourceTest test
```

Expected(预期): PASS(通过)。

- [ ] **Step 4: 运行管理后台验证**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend-admin
pnpm vitest run tests/unit/work-complain-api.test.ts
pnpm type-check
```

Expected(预期): PASS(通过)。

- [ ] **Step 5: 最终 diff(差异) 检查**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git diff --check
git status --short
```

Expected(预期): `git diff --check` 无输出；`git status --short` 不包含意外 stage(暂存) 文件。

- [ ] **Step 6: 提交收尾修复**

If Step 2 到 Step 5 exposed defects(暴露缺陷), fix the defects(修复缺陷) and commit only those fixes:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git add 91pai-master/common/vmeitime-http/index.js 91pai-master/components/custom/homeBanner 91pai-master/components/custom/splashOverlay 91pai-master/pages/home/home/index.vue 91pai-master/pages/home/home/index.js 91pai-master/pages/home/home/index.scss 91pai-master/pages/work 91pai-master/pages/mine/realname/intro 91pai-master/pages/mine/mine/index.js 91pai-master/pages/home/desc/index.js 91pai-master/pages.json scripts/verify-91pai-work-pages.mjs backend/system-wap/src/main/java/com/ypat/controller/AdminWorkComplainController.java backend/system-wap/src/main/java/com/ypat/service/WorkComplainAdminServiceClient.java backend/system-wap/src/test/java/com/ypat/controller/AdminWorkComplainControllerSourceTest.java backend/system-object/src/main/java/com/ypat/WorkComplainQo.java frontend-admin/src/api/modules/work-complain.ts frontend-admin/tests/unit/work-complain-api.test.ts frontend-admin/src/views/manage/work-complain-list frontend-admin/src/constants/menu.ts frontend-admin/src/stores/modules/permission.ts
git commit -m "fix: stabilize home work realname splash flows" -m "Scope: fix verification defects found during final checks." -m "Tested: node scripts/verify-91pai-work-pages.mjs; npm run build:h5; mvn -pl system-wap -Dtest=AdminWorkComplainControllerSourceTest test; pnpm vitest run tests/unit/work-complain-api.test.ts; pnpm type-check"
```

If no defects(缺陷) are found, do not create an empty commit.

## Self-Review(自审)

Spec coverage(设计覆盖):

- 首页 banner(横幅)：Task 1、Task 2。
- 作品详情：Task 1、Task 4。
- 投诉页和拉黑确认：Task 1、Task 5。
- 作品约拍页和安全提醒：Task 1、Task 5。
- 实名认证说明页：Task 3。
- 开屏页：Task 2。
- 后端/后台投诉治理：Task 6、Task 7。
- 验证和收尾：Task 8。

Placeholder scan(红旗词扫描):

- 本计划已扫描实施计划常见红旗词。
- 条件项已经给出检查规则、具体代码路径和可执行命令。

Type consistency(类型一致性):

- 小程序 API(接口) 方法名在 Task 1 定义，并在 Task 2、Task 4、Task 5 使用同名导入。
- 后台 API(接口) `WorkComplainInfo`、`WorkComplainListQuery` 在 Task 7 定义，并在同任务页面使用。
- 后端 `WorkComplainAdminServiceClient` 方法名与 `AdminWorkComplainController` 调用一致。
