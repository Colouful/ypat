# Banner 跳转字段 Implementation Plan(实施计划)

> **For agentic workers(给代理工作者):** REQUIRED SUB-SKILL(必需子技能): Use `superpowers:subagent-driven-development`(子代理驱动开发，推荐) or `superpowers:executing-plans`(执行计划) to implement this plan task-by-task(逐任务实施)。Steps use checkbox(复选框) `- [ ]` syntax for tracking(用于跟踪)。

**Goal(目标):** 为 banner(横幅) 增加可配置跳转字段，让 `frontend-admin`(管理后台) 可维护跳转行为，让 `frontend`(新版 UniApp 前端) 可打开小程序页面或外部网页，并在不可打开时复制链接降级。

**Architecture(架构):** 在现有 banner(横幅) 契约上新增 `jumpflag`(是否跳转)、`jumptype`(跳转类型)、`jumpurl`(跳转目标) 三个持久化字段。后端负责数据校验和规范化，管理后台负责友好录入，新版前端负责点击解析、跳转和降级。`91pai-master`(旧小程序) 明确不在本次范围内。

**Tech Stack(技术栈):** Java 8、Spring Boot(Spring 应用框架)、Spring Cloud(微服务框架)、JPA(Java 持久化规范)、MySQL(关系型数据库)、Vue 3(前端框架)、TypeScript(类型脚本)、Element Plus(组件库)、UniApp(跨端框架)、Vitest(单元测试框架)。

---

## 文件结构

- 修改 `backend/system-domain/src/main/java/com/ypat/entity/Banner.java`：增加 banner(横幅) 持久化字段。
- 修改 `backend/system-object/src/main/java/com/ypat/BannerQo.java`：增加 API(接口) 传输字段。
- 修改 `backend/system-wap/src/main/java/com/ypat/controller/AdminBannerController.java`：后台保存校验和字段规范化。
- 修改 `backend/system-wap/src/test/java/com/ypat/controller/AdminPublishControllerTest.java`：后端校验测试。
- 新建 `backend/dev/mysql/20260708_alter_banner_link_fields.sql`：MySQL(关系型数据库) DDL(数据定义语言) 脚本。
- 修改 `frontend-admin/src/api/modules/banner.ts`：管理后台 banner(横幅) 类型。
- 修改 `frontend-admin/src/views/banner/list/index.vue`：列表展示跳转状态。
- 修改 `frontend-admin/src/views/banner/list/BannerEditDialog.vue`：编辑跳转配置。
- 修改 `frontend/src/api/types/index.ts`：公开 banner(横幅) 类型。
- 新建 `frontend/src/utils/banner-link.ts`：banner(横幅) 跳转解析和打开工具。
- 新建 `frontend/src/utils/__tests__/banner-link.test.ts`：新版前端跳转逻辑测试。
- 修改 `frontend/src/components/business/HomeBanner.vue`：接入正式跳转工具。
- 新建 `frontend/src/pages-sub/content/web-view.vue`：外部 URL(统一资源定位符) 容器和复制降级页。
- 修改 `frontend/src/pages.json`：注册 `web-view`(小程序网页容器) 页面。

## Task 1: 后端契约、校验和 DDL(数据定义语言)

**文件：**
- 修改：`backend/system-domain/src/main/java/com/ypat/entity/Banner.java`
- 修改：`backend/system-object/src/main/java/com/ypat/BannerQo.java`
- 修改：`backend/system-wap/src/main/java/com/ypat/controller/AdminBannerController.java`
- 修改：`backend/system-wap/src/test/java/com/ypat/controller/AdminPublishControllerTest.java`
- 新建：`backend/dev/mysql/20260708_alter_banner_link_fields.sql`

- [ ] **Step 1: 新增 DDL(数据定义语言) 脚本**

创建 `backend/dev/mysql/20260708_alter_banner_link_fields.sql`：

```sql
ALTER TABLE `t_banner`
  ADD COLUMN `jumpflag` VARCHAR(1) NOT NULL DEFAULT '0' COMMENT '是否可跳转：0否，1是' AFTER `status`,
  ADD COLUMN `jumptype` VARCHAR(20) DEFAULT NULL COMMENT '跳转类型：miniapp小程序页面，web外部地址' AFTER `jumpflag`,
  ADD COLUMN `jumpurl` VARCHAR(500) DEFAULT NULL COMMENT '跳转目标：小程序页面路径或外部URL' AFTER `jumptype`;
```

- [ ] **Step 2: 给 `Banner`(横幅实体) 增加字段**

在 `backend/system-domain/src/main/java/com/ypat/entity/Banner.java` 的 `status` 后添加：

```java
    private String jumpflag;//是否可跳转（0.否、1.是）
    private String jumptype;//跳转类型（miniapp.小程序页面、web.外部地址）
    private String jumpurl;//跳转目标
```

在类结尾前添加：

```java
    public String getJumpflag() {
        return jumpflag;
    }

    public void setJumpflag(String jumpflag) {
        this.jumpflag = jumpflag;
    }

    public String getJumptype() {
        return jumptype;
    }

    public void setJumptype(String jumptype) {
        this.jumptype = jumptype;
    }

    public String getJumpurl() {
        return jumpurl;
    }

    public void setJumpurl(String jumpurl) {
        this.jumpurl = jumpurl;
    }
```

- [ ] **Step 3: 给 `BannerQo`(横幅传输对象) 增加字段**

在 `backend/system-object/src/main/java/com/ypat/BannerQo.java` 的 `statusTxt` 后添加：

```java
    private String jumpflag;
    private String jumptype;
    private String jumpurl;
```

在类结尾前添加：

```java
    public String getJumpflag() {
        return jumpflag;
    }

    public void setJumpflag(String jumpflag) {
        this.jumpflag = jumpflag;
    }

    public String getJumptype() {
        return jumptype;
    }

    public void setJumptype(String jumptype) {
        this.jumptype = jumptype;
    }

    public String getJumpurl() {
        return jumpurl;
    }

    public void setJumpurl(String jumpurl) {
        this.jumpurl = jumpurl;
    }
```

- [ ] **Step 4: 先写后端失败测试**

在 `backend/system-wap/src/test/java/com/ypat/controller/AdminPublishControllerTest.java` 增加 import(导入)：

```java
import com.ypat.SysException;
```

在 `bannerSaveAndUpDownTreatVoidServiceResponseAsSuccess` 后添加测试：

```java
    @Test
    public void bannerSaveAcceptsMiniappJumpConfig() throws Exception {
        AdminBannerController controller = new AdminBannerController();
        RecordingBannerServiceClient client = new RecordingBannerServiceClient();
        setField(controller, "bannerServiceClient", client);

        BannerQo qo = new BannerQo();
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");
        qo.setJumpflag("1");
        qo.setJumptype("miniapp");
        qo.setJumpurl("/pages/work/index");

        assertSuccessWithoutPayload(controller.save(qo));
        assertEquals("1", client.saved.getJumpflag());
        assertEquals("miniapp", client.saved.getJumptype());
        assertEquals("/pages/work/index", client.saved.getJumpurl());
    }

    @Test
    public void bannerSaveAcceptsWebJumpConfig() throws Exception {
        AdminBannerController controller = new AdminBannerController();
        RecordingBannerServiceClient client = new RecordingBannerServiceClient();
        setField(controller, "bannerServiceClient", client);

        BannerQo qo = new BannerQo();
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");
        qo.setJumpflag("1");
        qo.setJumptype("web");
        qo.setJumpurl("https://example.com/activity");

        assertSuccessWithoutPayload(controller.save(qo));
        assertEquals("1", client.saved.getJumpflag());
        assertEquals("web", client.saved.getJumptype());
        assertEquals("https://example.com/activity", client.saved.getJumpurl());
    }

    @Test(expected = SysException.class)
    public void bannerSaveRejectsEnabledJumpWithoutTarget() {
        AdminBannerController controller = new AdminBannerController();

        BannerQo qo = new BannerQo();
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");
        qo.setJumpflag("1");
        qo.setJumptype("miniapp");
        qo.setJumpurl("");

        controller.save(qo);
    }

    @Test(expected = SysException.class)
    public void bannerSaveRejectsNonHttpWebTarget() {
        AdminBannerController controller = new AdminBannerController();

        BannerQo qo = new BannerQo();
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");
        qo.setJumpflag("1");
        qo.setJumptype("web");
        qo.setJumpurl("javascript:alert(1)");

        controller.save(qo);
    }
```

- [ ] **Step 5: 运行后端测试，确认先失败**

运行：

```bash
mvn -pl backend/system-wap -Dtest=AdminPublishControllerTest test
```

预期：FAIL(失败)，因为 `BannerQo`(横幅传输对象) 尚无字段访问器，`AdminBannerController`(后台横幅控制器) 尚无跳转校验。

- [ ] **Step 6: 实现 `AdminBannerController`(后台横幅控制器) 校验**

在 `backend/system-wap/src/main/java/com/ypat/controller/AdminBannerController.java` 的 `DEFAULT_SIZE` 后添加常量：

```java
    private static final String JUMP_DISABLED = "0";
    private static final String JUMP_ENABLED = "1";
    private static final String JUMP_TYPE_MINIAPP = "miniapp";
    private static final String JUMP_TYPE_WEB = "web";
    private static final int MAX_JUMP_URL_LENGTH = 500;
```

在 `save` 方法中、日志前添加：

```java
        normalizeAndValidateJump(bannerQo);
```

在 `upDown` 方法前添加：

```java
    private void normalizeAndValidateJump(BannerQo bannerQo) {
        String jumpflag = StringUtils.defaultIfBlank(bannerQo.getJumpflag(), JUMP_DISABLED).trim();
        bannerQo.setJumpflag(jumpflag);

        if (!JUMP_ENABLED.equals(jumpflag)) {
            bannerQo.setJumpflag(JUMP_DISABLED);
            bannerQo.setJumptype(null);
            bannerQo.setJumpurl(null);
            return;
        }

        String jumptype = StringUtils.defaultIfBlank(bannerQo.getJumptype(), JUMP_TYPE_MINIAPP).trim();
        String jumpurl = StringUtils.trimToEmpty(bannerQo.getJumpurl());

        if (!JUMP_TYPE_MINIAPP.equals(jumptype) && !JUMP_TYPE_WEB.equals(jumptype)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "跳转类型不正确");
        }
        if (StringUtils.isBlank(jumpurl)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请输入跳转目标");
        }
        if (jumpurl.length() > MAX_JUMP_URL_LENGTH) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "跳转目标不能超过500个字符");
        }
        if (JUMP_TYPE_MINIAPP.equals(jumptype) && !isMiniappPath(jumpurl)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请输入 /pages 或 /pages-sub 开头的小程序页面路径");
        }
        if (JUMP_TYPE_WEB.equals(jumptype) && !isHttpUrl(jumpurl)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请输入 http 或 https 开头的外部地址");
        }

        bannerQo.setJumptype(jumptype);
        bannerQo.setJumpurl(jumpurl);
    }

    private boolean isMiniappPath(String value) {
        return value.startsWith("/pages/") || value.startsWith("/pages-sub/");
    }

    private boolean isHttpUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }
```

- [ ] **Step 7: 运行后端测试，确认通过**

运行：

```bash
mvn -pl backend/system-wap -Dtest=AdminPublishControllerTest test
```

预期：PASS(通过)。

- [ ] **Step 8: 提交后端契约**

运行：

```bash
git add backend/system-domain/src/main/java/com/ypat/entity/Banner.java backend/system-object/src/main/java/com/ypat/BannerQo.java backend/system-wap/src/main/java/com/ypat/controller/AdminBannerController.java backend/system-wap/src/test/java/com/ypat/controller/AdminPublishControllerTest.java backend/dev/mysql/20260708_alter_banner_link_fields.sql
git commit -m "feat: add banner jump fields backend contract"
```

## Task 2: 管理后台跳转配置

**文件：**
- 修改：`frontend-admin/src/api/modules/banner.ts`
- 修改：`frontend-admin/src/views/banner/list/index.vue`
- 修改：`frontend-admin/src/views/banner/list/BannerEditDialog.vue`

- [ ] **Step 1: 扩展管理后台 API(接口) 类型**

把 `frontend-admin/src/api/modules/banner.ts` 中的 `Banner`(横幅) interface(接口类型) 替换为：

```ts
export interface Banner {
  id: number
  title: string
  imgpath: string
  credate: string
  status: string
  statusTxt: string
  jumpflag?: string
  jumptype?: 'miniapp' | 'web' | string
  jumpurl?: string
}
```

- [ ] **Step 2: 增加列表展示辅助函数和跳转列**

在 `frontend-admin/src/views/banner/list/index.vue` 的 `preview` 后添加：

```ts
function getJumpText(row: Banner): string {
  if (row.jumpflag !== '1') return '不跳转'
  if (row.jumptype === 'miniapp') return '小程序页面'
  if (row.jumptype === 'web') return '外部地址'
  return '未配置'
}

function getJumpTagType(row: Banner): 'info' | 'success' | 'warning' {
  if (row.jumpflag !== '1') return 'info'
  if (row.jumptype === 'miniapp') return 'success'
  if (row.jumptype === 'web') return 'warning'
  return 'info'
}
```

在状态列后添加：

```vue
      <el-table-column label="跳转" width="130" align="center">
        <template #default="{row}">
          <el-tag :type="getJumpTagType(row as unknown as Banner)">{{ getJumpText(row as unknown as Banner) }}</el-tag>
        </template>
      </el-table-column>
```

如果操作按钮换行，把操作列宽度从 `220` 调整为 `240`。

- [ ] **Step 3: 扩展编辑表单状态**

在 `frontend-admin/src/views/banner/list/BannerEditDialog.vue` 中把 `form` 替换为：

```ts
const form = reactive({
  id: undefined as number | undefined,
  title: '',
  imgpath: '',
  jumpflag: '0',
  jumptype: 'miniapp',
  jumpurl: '',
})
```

在 watcher(监听器) 中追加：

```ts
    form.jumpflag = props.data?.jumpflag || '0'
    form.jumptype = props.data?.jumptype || 'miniapp'
    form.jumpurl = props.data?.jumpurl || ''
```

- [ ] **Step 4: 增加管理后台表单校验**

在 `submit` 前添加：

```ts
function validateJumpConfig(): boolean {
  if (form.jumpflag !== '1') {
    form.jumptype = 'miniapp'
    form.jumpurl = ''
    return true
  }
  const target = form.jumpurl.trim()
  if (!target) {
    ElMessage.warning('请输入跳转目标')
    return false
  }
  if (target.length > 500) {
    ElMessage.warning('跳转目标不能超过500个字符')
    return false
  }
  if (form.jumptype === 'miniapp' && !target.startsWith('/pages/') && !target.startsWith('/pages-sub/')) {
    ElMessage.warning('请输入 /pages 或 /pages-sub 开头的小程序页面路径')
    return false
  }
  if (form.jumptype === 'web' && !target.startsWith('http://') && !target.startsWith('https://')) {
    ElMessage.warning('请输入 http 或 https 开头的外部地址')
    return false
  }
  form.jumpurl = target
  return true
}
```

在 `submit` 中、`await formRef.value.validate()` 后添加：

```ts
  if (!validateJumpConfig()) return
```

- [ ] **Step 5: 增加编辑表单控件**

在图片表单项后添加：

```vue
      <el-form-item label="是否跳转">
        <el-switch v-model="form.jumpflag" active-value="1" inactive-value="0" />
      </el-form-item>
      <template v-if="form.jumpflag === '1'">
        <el-form-item label="跳转类型">
          <el-radio-group v-model="form.jumptype">
            <el-radio-button label="miniapp">小程序页面</el-radio-button>
            <el-radio-button label="web">外部地址</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="跳转目标">
          <el-input
            v-model="form.jumpurl"
            :maxlength="500"
            show-word-limit
            :placeholder="form.jumptype === 'miniapp' ? '/pages/work/index 或 /pages-sub/work/detail?id=1' : 'https://example.com/activity'"
          />
        </el-form-item>
      </template>
```

- [ ] **Step 6: 类型检查管理后台**

运行：

```bash
pnpm --dir frontend-admin type-check
```

预期：PASS(通过)。

- [ ] **Step 7: 提交管理后台改动**

运行：

```bash
git add frontend-admin/src/api/modules/banner.ts frontend-admin/src/views/banner/list/index.vue frontend-admin/src/views/banner/list/BannerEditDialog.vue
git commit -m "feat(admin): support banner jump configuration"
```

## Task 3: 新版前端跳转工具和测试

**文件：**
- 修改：`frontend/src/api/types/index.ts`
- 新建：`frontend/src/utils/banner-link.ts`
- 新建：`frontend/src/utils/__tests__/banner-link.test.ts`

- [ ] **Step 1: 扩展公开 banner(横幅) 类型**

在 `frontend/src/api/types/index.ts` 的 `export interface Banner` 中添加：

```ts
  jumpflag?: string
  jumptype?: 'miniapp' | 'web' | string
  jumpurl?: string
  jumpUrl?: string
  linkUrl?: string
  linkurl?: string
  url?: string
  path?: string
```

- [ ] **Step 2: 先写工具测试**

创建 `frontend/src/utils/__tests__/banner-link.test.ts`：

```ts
import { describe, expect, it } from 'vitest'
import type { Banner } from '@/api/types'
import { resolveBannerAction } from '../banner-link'

function banner(overrides: Partial<Banner>): Banner {
  return {
    id: 1,
    imgpath: 'https://example.com/banner.png',
    ...overrides,
  }
}

describe('resolveBannerAction', () => {
  it('returns preview when jump is disabled', () => {
    expect(resolveBannerAction(banner({ jumpflag: '0', jumptype: 'miniapp', jumpurl: '/pages/work/index' }))).toEqual({
      type: 'preview',
    })
  })

  it('returns miniapp action for enabled miniapp path', () => {
    expect(resolveBannerAction(banner({ jumpflag: '1', jumptype: 'miniapp', jumpurl: '/pages/work/index' }))).toEqual({
      type: 'miniapp',
      url: '/pages/work/index',
    })
  })

  it('returns web action for enabled http url', () => {
    expect(resolveBannerAction(banner({ jumpflag: '1', jumptype: 'web', jumpurl: 'https://example.com/a' }))).toEqual({
      type: 'web',
      url: 'https://example.com/a',
    })
  })

  it('falls back to copy for invalid web url', () => {
    expect(resolveBannerAction(banner({ jumpflag: '1', jumptype: 'web', jumpurl: 'ftp://example.com/a' }))).toEqual({
      type: 'copy',
      url: 'ftp://example.com/a',
    })
  })

  it('keeps legacy jumpUrl compatibility when formal fields are absent', () => {
    expect(resolveBannerAction(banner({ jumpUrl: '/pages/work/index' }))).toEqual({
      type: 'miniapp',
      url: '/pages/work/index',
    })
  })
})
```

- [ ] **Step 3: 运行工具测试，确认先失败**

运行：

```bash
pnpm --dir frontend test -- src/utils/__tests__/banner-link.test.ts
```

预期：FAIL(失败)，因为 `frontend/src/utils/banner-link.ts` 尚不存在。

- [ ] **Step 4: 实现 `banner-link`(横幅跳转工具)**

创建 `frontend/src/utils/banner-link.ts`：

```ts
import type { Banner } from '@/api/types'

export type BannerAction =
  | { type: 'preview' }
  | { type: 'miniapp'; url: string }
  | { type: 'web'; url: string }
  | { type: 'copy'; url: string }

const TAB_PAGE_URLS = new Set(['/pages/home/index', '/pages/work/index', '/pages/message/index', '/pages/publish/index', '/pages/mine/index'])

function trim(value: unknown): string {
  return typeof value === 'string' ? value.trim() : ''
}

function getLegacyTarget(item: Banner): string {
  const record = item as unknown as Record<string, unknown>
  const candidates = ['jumpUrl', 'linkUrl', 'linkurl', 'url', 'path']
  return candidates.map((key) => trim(record[key])).find(Boolean) || ''
}

function isMiniappPath(value: string): boolean {
  return value.startsWith('/pages/') || value.startsWith('/pages-sub/')
}

function isHttpUrl(value: string): boolean {
  return value.startsWith('http://') || value.startsWith('https://')
}

export function resolveBannerAction(item: Banner): BannerAction {
  const jumpflag = trim(item.jumpflag)
  const jumptype = trim(item.jumptype)
  const jumpurl = trim(item.jumpurl)

  if (jumpflag === '1') {
    if (jumptype === 'miniapp') {
      return isMiniappPath(jumpurl) ? { type: 'miniapp', url: jumpurl } : { type: 'preview' }
    }
    if (jumptype === 'web') {
      return isHttpUrl(jumpurl) ? { type: 'web', url: jumpurl } : { type: 'copy', url: jumpurl }
    }
    return jumpurl ? { type: 'copy', url: jumpurl } : { type: 'preview' }
  }

  if (jumpflag === '0') return { type: 'preview' }

  const legacyTarget = getLegacyTarget(item)
  if (isMiniappPath(legacyTarget)) return { type: 'miniapp', url: legacyTarget }
  if (isHttpUrl(legacyTarget)) return { type: 'web', url: legacyTarget }
  return legacyTarget ? { type: 'copy', url: legacyTarget } : { type: 'preview' }
}

export function getBannerWebViewUrl(url: string): string {
  return `/pages-sub/content/web-view?url=${encodeURIComponent(url)}`
}

export function openBannerAction(action: BannerAction, preview: () => void): void {
  if (action.type === 'preview') {
    preview()
    return
  }
  if (action.type === 'miniapp') {
    const open = TAB_PAGE_URLS.has(action.url) ? uni.switchTab : uni.navigateTo
    open({
      url: action.url,
      fail: () => preview(),
    })
    return
  }
  if (action.type === 'web') {
    uni.navigateTo({
      url: getBannerWebViewUrl(action.url),
      fail: () => copyUrl(action.url),
    })
    return
  }
  copyUrl(action.url)
}

export function copyUrl(url: string): void {
  if (!url) return
  uni.setClipboardData({
    data: url,
    success: () => uni.showToast({ title: '链接已复制，请在浏览器打开', icon: 'none' }),
    fail: () => uni.showToast({ title: '复制失败，请稍后重试', icon: 'none' }),
  })
}
```

- [ ] **Step 5: 运行工具测试和类型检查**

运行：

```bash
pnpm --dir frontend test -- src/utils/__tests__/banner-link.test.ts
pnpm --dir frontend type-check
```

预期：PASS(通过)。

- [ ] **Step 6: 提交跳转工具**

运行：

```bash
git add frontend/src/api/types/index.ts frontend/src/utils/banner-link.ts frontend/src/utils/__tests__/banner-link.test.ts
git commit -m "feat(frontend): add banner link resolver"
```

## Task 4: 新版前端 HomeBanner(首页横幅组件) 和 web-view(小程序网页容器) 页面

**文件：**
- 修改：`frontend/src/components/business/HomeBanner.vue`
- 新建：`frontend/src/pages-sub/content/web-view.vue`
- 修改：`frontend/src/pages.json`

- [ ] **Step 1: 注册 `web-view`(小程序网页容器) 页面**

在 `frontend/src/pages.json` 的 `pages-sub/content.pages` 中、`invite-landing` 后添加：

```json
        {
          "path": "web-view",
          "style": {
            "navigationStyle": "custom"
          }
        }
```

- [ ] **Step 2: 创建外部网页容器**

创建 `frontend/src/pages-sub/content/web-view.vue`：

```vue
<template>
  <view class="web-page">
    <web-view v-if="safeUrl" :src="safeUrl" />
    <view v-else class="web-page__fallback">
      <text class="web-page__title">链接暂时无法打开</text>
      <text class="web-page__desc">可以复制链接后在浏览器中访问</text>
      <button class="web-page__button" @tap="copy">复制链接</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const rawUrl = ref('')

const safeUrl = computed(() => {
  const value = rawUrl.value.trim()
  if (value.startsWith('http://') || value.startsWith('https://')) return value
  return ''
})

onLoad((query) => {
  rawUrl.value = decodeURIComponent(String(query?.url || ''))
})

function copy(): void {
  const value = rawUrl.value.trim()
  if (!value) {
    uni.showToast({ title: '暂无可复制链接', icon: 'none' })
    return
  }
  uni.setClipboardData({
    data: value,
    success: () => uni.showToast({ title: '链接已复制，请在浏览器打开', icon: 'none' }),
  })
}
</script>

<style scoped lang="scss">
.web-page {
  min-height: 100vh;
  background: #f5f6f8;
}

.web-page__fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 48rpx;
  text-align: center;
}

.web-page__title {
  color: #1a1d1f;
  font-size: 36rpx;
  font-weight: 800;
}

.web-page__desc {
  margin-top: 18rpx;
  color: #6f767e;
  font-size: 26rpx;
}

.web-page__button {
  margin-top: 36rpx;
  padding: 0 48rpx;
  border-radius: 999rpx;
  color: #ffffff;
  background: #1a1d1f;
  font-size: 28rpx;
}
</style>
```

- [ ] **Step 3: 更新 `HomeBanner`(首页横幅组件)**

在 `frontend/src/components/business/HomeBanner.vue` 中新增 import(导入)：

```ts
import { openBannerAction, resolveBannerAction } from '@/utils/banner-link'
```

删除本地 `getBannerLink` 函数，并从 `BannerView` 中删除 `link?: string`。

在 `loadBanners` 中删除：

```ts
        link: getBannerLink(item),
```

把 `handleTap` 替换为：

```ts
function previewBanner(item: BannerView): void {
  uni.previewImage({
    urls: visibleBanners.value.map((banner) => banner.image),
    current: item.image,
  })
}

function handleTap(item: BannerView): void {
  openBannerAction(resolveBannerAction(item), () => previewBanner(item))
}
```

- [ ] **Step 4: 运行类型检查和跳转工具测试**

运行：

```bash
pnpm --dir frontend test -- src/utils/__tests__/banner-link.test.ts
pnpm --dir frontend type-check
```

预期：PASS(通过)。

- [ ] **Step 5: 提交新版前端集成**

运行：

```bash
git add frontend/src/components/business/HomeBanner.vue frontend/src/pages-sub/content/web-view.vue frontend/src/pages.json
git commit -m "feat(frontend): open banner external links"
```

## Task 5: 最终验证和范围保护

**文件：**
- 检查：`git diff dev6...HEAD`
- 检查：`91pai-master`(旧小程序)

- [ ] **Step 1: 验证未修改旧小程序**

运行：

```bash
git diff --name-only dev6...HEAD | rg '^91pai-master/' || true
```

预期：无输出。

- [ ] **Step 2: 运行后端定向测试**

运行：

```bash
mvn -pl backend/system-wap -Dtest=AdminPublishControllerTest test
```

预期：PASS(通过)。

- [ ] **Step 3: 运行管理后台类型检查**

运行：

```bash
pnpm --dir frontend-admin type-check
```

预期：PASS(通过)。

- [ ] **Step 4: 运行新版前端测试和类型检查**

运行：

```bash
pnpm --dir frontend test -- src/utils/__tests__/banner-link.test.ts
pnpm --dir frontend type-check
```

预期：PASS(通过)。

- [ ] **Step 5: 检查最终变更**

运行：

```bash
git status --short
git log --oneline --decorate -n 8
git diff --stat dev6...HEAD
```

预期：

- 工作树没有未暂存的实施变更。
- 提交包含设计、计划、后端、管理后台、新版前端跳转工具、新版前端集成。
- 变更文件与本计划的文件结构一致。

## 自检

- 设计覆盖：设计文档中的每一项都有任务承接。后端字段和校验由 Task 1 完成；后台编辑和列表展示由 Task 2 完成；新版前端跳转解析由 Task 3 完成；`web-view`(小程序网页容器) 和 `HomeBanner`(首页横幅组件) 集成由 Task 4 完成；范围保护和验证由 Task 5 完成。
- 占位扫描：计划中没有未决占位、未说明实现方式的泛化步骤或延后实现项。
- 类型一致：字段名统一为 `jumpflag`、`jumptype`、`jumpurl`；跳转类型值统一为 `miniapp` 和 `web`。
