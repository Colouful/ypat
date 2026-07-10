# Realname Legacy Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore the legacy paid three-photo realname verification flow in the newer mini program, with backend payment enforcement, secure realname submission, and admin review support.

**Architecture:** Keep the old business state machine but implement it in the newer `frontend`, `frontend-admin`, and Java backend surfaces. Backend enforces all security-sensitive rules: 2900-fen fee, current-user order status, three-photo requirement, paid-or-rejected submission gate, and audit response safety. Frontend only presents the flow, calls existing OCR/payment/realname APIs, and waits for server-confirmed payment before submitting.

**Tech Stack:** Vue 3, uni-app, Pinia, TypeScript, Vitest, Element Plus, Spring Boot, Spring Cloud Feign, JUnit 4.

---

## Source Spec

- `docs/superpowers/specs/2026-07-10-realname-legacy-flow-design.md`

## File Structure

- Modify `backend/system-wap/src/main/java/com/ypat/controller/OrderController.java`: force realname order amount to 2900 fen on the server.
- Create `backend/system-wap/src/test/java/com/ypat/controller/RealnameOrderSecuritySourceTest.java`: source-level regression test for server-side amount enforcement.
- Modify `backend/system-domain/src/main/java/com/ypat/service/UserService.java`: require exactly three realname photos and gate submission by paid or rejected status.
- Create `backend/system-domain/src/test/java/com/ypat/service/UserServiceRealnameSecuritySourceTest.java`: source-level regression test for status and photo-count gates.
- Modify `backend/system-wap/src/main/java/com/ypat/controller/AdminUserController.java`: make audit tolerate empty downstream responses.
- Create `backend/system-wap/src/test/java/com/ypat/controller/AdminUserAuditSourceTest.java`: source-level regression test for empty audit response handling.
- Modify `frontend-admin/src/views/manage/user-list/UserAuditDialog.vue`: label three realname photos and keep preview behavior.
- Create `frontend-admin/src/views/manage/user-list/__tests__/UserAuditDialog.test.ts`: component test for photo labels and audited-state buttons.
- Modify `frontend/src/api/types/index.ts`: define realname payment and status types used by the page.
- Modify `frontend/src/api/modules/oauth.ts`: expose realname helpers while preserving `/oauth/ocr`, `/oauth/add`, and `/oauth/get`.
- Create `frontend/src/pages-sub/user/__tests__/realname-source.test.ts`: source-level guard for paid three-photo flow in the page.
- Modify `frontend/src/pages-sub/user/realname.vue`: implement three-photo upload, paid first submission, server-confirmed payment, direct retry after rejection, and secure display states.
- Modify `docs/decisions/ADR-REALNAME-PHOTO-AND-FEE-POLICY.md`: supersede the old two-photo-free decision with the approved paid three-photo decision.

## Commands

Run only targeted checks. Do not install dependencies or run full builds.

- Backend targeted tests:
  - `cd backend && mvn -pl system-wap -Dtest=RealnameOrderSecuritySourceTest,AdminUserAuditSourceTest test`
  - `cd backend && mvn -pl system-domain -Dtest=UserServiceRealnameSecuritySourceTest test`
- Frontend targeted tests:
  - `cd frontend && CI=true ./node_modules/.bin/vitest run src/pages-sub/user/__tests__/realname-source.test.ts src/api/__tests__/api-contracts.test.ts`
  - `cd frontend-admin && CI=true ./node_modules/.bin/vitest run src/views/manage/user-list/__tests__/UserAuditDialog.test.ts`
- Final diff hygiene:
  - `git diff --check`
  - `git status --short`

---

### Task 1: Backend Realname Order Amount Enforcement

**Files:**
- Create: `backend/system-wap/src/test/java/com/ypat/controller/RealnameOrderSecuritySourceTest.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/OrderController.java`

- [ ] **Step 1: Write the failing source test**

Create `backend/system-wap/src/test/java/com/ypat/controller/RealnameOrderSecuritySourceTest.java`:

```java
package com.ypat.controller;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class RealnameOrderSecuritySourceTest {

    @Test
    public void realnameOrderAmountIsForcedByServerInFen() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/controller/OrderController.java");

        assertTrue(source.contains("REALNAME_AUDIT_FEE_FEN = 2900"));
        assertTrue(source.contains("OrderType.REAL.value.equals(orderQo.getType())"));
        assertTrue(source.contains("orderQo.setTotal_fee(REALNAME_AUDIT_FEE_FEN)"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) {
            path = Paths.get(file.replace("backend/system-wap/", ""));
        }
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=RealnameOrderSecuritySourceTest test
```

Expected: FAIL because `REALNAME_AUDIT_FEE_FEN` is not defined yet.

- [ ] **Step 3: Implement server-forced realname amount**

In `backend/system-wap/src/main/java/com/ypat/controller/OrderController.java`, add the constant under the logger:

```java
private static final int REALNAME_AUDIT_FEE_FEN = 2900;
```

Replace the amount validation block inside `add(@Valid OrderQo orderQo)` with:

```java
if (OrderType.PPD.value.equals(orderQo.getType())) {
    if (StringUtils.isEmpty(orderQo.getProductid())) throw new SysException(ResponseCode.FAIL_PARA);
    ProductQo product = GsonUtils.fromJson(productServiceClient.get(orderQo.getProductid()), ProductQo.class);
    if (product == null || product.getOldval() == null || product.getOldval() < 1) throw new SysException(ResponseCode.FAIL_NOT);
    if (!isProductUpStatus(product.getStatus())) throw new SysException(ResponseCode.FAIL_VAL);
    orderQo.setTotal_fee(product.getOldval());
} else if (OrderType.REAL.value.equals(orderQo.getType())) {
    orderQo.setTotal_fee(REALNAME_AUDIT_FEE_FEN);
} else if (orderQo.getTotal_fee() == null || orderQo.getTotal_fee() < 1) {
    throw new SysException(ResponseCode.FAIL_PARA);
}
```

- [ ] **Step 4: Run the targeted test**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=RealnameOrderSecuritySourceTest test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/system-wap/src/main/java/com/ypat/controller/OrderController.java backend/system-wap/src/test/java/com/ypat/controller/RealnameOrderSecuritySourceTest.java
git commit -m "fix: enforce realname order amount" -m "Force the realname verification fee on the server so the mini program cannot lower or spoof the payment amount." -m "Constraint: Realname orders use 2900 fen and continue through the existing legacy order flow." -m "Tested: mvn -pl system-wap -Dtest=RealnameOrderSecuritySourceTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 2: Backend Realname Submission Gate

**Files:**
- Create: `backend/system-domain/src/test/java/com/ypat/service/UserServiceRealnameSecuritySourceTest.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/UserService.java`

- [ ] **Step 1: Write the failing source test**

Create `backend/system-domain/src/test/java/com/ypat/service/UserServiceRealnameSecuritySourceTest.java`:

```java
package com.ypat.service;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class UserServiceRealnameSecuritySourceTest {

    @Test
    public void realnameSubmitRequiresThreePhotosAndPaidOrRejectedStatus() throws Exception {
        String source = read("backend/system-domain/src/main/java/com/ypat/service/UserService.java");

        assertTrue(source.contains("REALNAME_PHOTO_COUNT = 3"));
        assertTrue(source.contains("pics == null || pics.size() != REALNAME_PHOTO_COUNT"));
        assertTrue(source.contains("UserStatus.zfcg.value.equals(old.getStatus())"));
        assertTrue(source.contains("UserStatus.shbtg.value.equals(old.getStatus())"));
        assertTrue(source.contains("throw new SysException(ResponseCode.FAIL_NOREAL)"));
        assertTrue(source.contains("old.setStatus(UserStatus.ytj.value)"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) {
            path = Paths.get(file.replace("backend/system-domain/", ""));
        }
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```bash
cd backend
mvn -pl system-domain -Dtest=UserServiceRealnameSecuritySourceTest test
```

Expected: FAIL because `REALNAME_PHOTO_COUNT` and the paid-or-rejected gate are not present yet.

- [ ] **Step 3: Implement the realname submission gate**

In `backend/system-domain/src/main/java/com/ypat/service/UserService.java`, add the constant near the logger:

```java
private static final int REALNAME_PHOTO_COUNT = 3;
```

In `oauth(OauthQo oauthQo)`, after loading `old` and before saving user info, replace the existing duplicate-realname-only gate with:

```java
if(YesNo.yes.value.equals(old.getRealnameflag())){
    throw new SysException(1007,"已经实名");
}
List<String> pics = oauthQo.getPics();
if(pics == null || pics.size() != REALNAME_PHOTO_COUNT){
    throw new SysException(ResponseCode.FAIL_REALNAME);
}
boolean canSubmit = UserStatus.zfcg.value.equals(old.getStatus()) || UserStatus.shbtg.value.equals(old.getStatus());
if(!canSubmit){
    throw new SysException(ResponseCode.FAIL_NOREAL);
}
```

Then remove the later local declaration:

```java
List<String> pics = oauthQo.getPics();
```

and keep the existing image save loop using the `pics` variable defined above.

- [ ] **Step 4: Run the targeted test**

Run:

```bash
cd backend
mvn -pl system-domain -Dtest=UserServiceRealnameSecuritySourceTest test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/system-domain/src/main/java/com/ypat/service/UserService.java backend/system-domain/src/test/java/com/ypat/service/UserServiceRealnameSecuritySourceTest.java
git commit -m "fix: gate realname submissions by payment" -m "Require the legacy three-photo payload and allow realname submission only after payment success or a rejected audit retry." -m "Constraint: Approved users cannot resubmit and first-time users cannot bypass the realname fee." -m "Tested: mvn -pl system-domain -Dtest=UserServiceRealnameSecuritySourceTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 3: Admin Audit Empty Response Safety

**Files:**
- Create: `backend/system-wap/src/test/java/com/ypat/controller/AdminUserAuditSourceTest.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminUserController.java`

- [ ] **Step 1: Write the failing source test**

Create `backend/system-wap/src/test/java/com/ypat/controller/AdminUserAuditSourceTest.java`:

```java
package com.ypat.controller;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminUserAuditSourceTest {

    @Test
    public void auditHandlesVoidOrEmptyDownstreamResponse() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/controller/AdminUserController.java");

        assertTrue(source.contains("parseAuditResponse(result)"));
        assertTrue(source.contains("private JsonElement parseAuditResponse(String result)"));
        assertTrue(source.contains("StringUtils.isBlank(result)"));
        assertFalse(source.contains("JsonElement resData = JsonParser.parseString(result);"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) {
            path = Paths.get(file.replace("backend/system-wap/", ""));
        }
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=AdminUserAuditSourceTest test
```

Expected: FAIL because audit still parses downstream response directly.

- [ ] **Step 3: Implement safe audit response parsing**

In `backend/system-wap/src/main/java/com/ypat/controller/AdminUserController.java`, replace:

```java
String result = oauthServiceClient.audit(id, flag);
JsonElement resData = JsonParser.parseString(result);
pushOauthAuditMessage(id, flag);
```

with:

```java
String result = oauthServiceClient.audit(id, flag);
JsonElement resData = parseAuditResponse(result);
pushOauthAuditMessage(id, flag);
```

Add this method before `pushOauthAuditMessage`:

```java
private JsonElement parseAuditResponse(String result) {
    if (StringUtils.isBlank(result)) {
        return JsonParser.parseString("{}");
    }
    return JsonParser.parseString(result);
}
```

- [ ] **Step 4: Run the targeted test**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=AdminUserAuditSourceTest test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/system-wap/src/main/java/com/ypat/controller/AdminUserController.java backend/system-wap/src/test/java/com/ypat/controller/AdminUserAuditSourceTest.java
git commit -m "fix: tolerate empty realname audit response" -m "Keep admin realname audit successful when the downstream service completes with a void or blank response." -m "Constraint: Message push remains best-effort and must not undo the audit result." -m "Tested: mvn -pl system-wap -Dtest=AdminUserAuditSourceTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 4: Admin Three-Photo Review UI

**Files:**
- Create: `frontend-admin/src/views/manage/user-list/__tests__/UserAuditDialog.test.ts`
- Modify: `frontend-admin/src/views/manage/user-list/UserAuditDialog.vue`

- [ ] **Step 1: Write the failing component test**

Create `frontend-admin/src/views/manage/user-list/__tests__/UserAuditDialog.test.ts`:

```ts
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import UserAuditDialog from '../UserAuditDialog.vue'

const { getUserDetailMock } = vi.hoisted(() => ({
  getUserDetailMock: vi.fn(),
}))

vi.mock('@/api/modules/user', () => ({
  getUserDetail: getUserDetailMock,
}))

describe('实名审核弹窗', () => {
  beforeEach(() => {
    getUserDetailMock.mockResolvedValue({
      data: {
        userid: 7,
        name: '张三',
        certcode: '330102199001011234',
        status: '1',
        pics: ['front.jpg', 'back.jpg', 'hand.jpg'],
      },
    })
  })

  it('展示身份证正面、反面和手持身份证三张照片标签', async () => {
    const wrapper = mount(UserAuditDialog, {
      props: {
        visible: true,
        user: { userid: 7, status: '1' },
        loading: false,
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    await flushPromises()

    const text = wrapper.text()
    expect(text).toContain('身份证正面')
    expect(text).toContain('身份证反面')
    expect(text).toContain('手持身份证')
    expect(text).toContain('审核通过')
    expect(text).toContain('审核不通过')
  })
})
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```bash
cd frontend-admin
CI=true ./node_modules/.bin/vitest run src/views/manage/user-list/__tests__/UserAuditDialog.test.ts
```

Expected: FAIL because the dialog does not render semantic labels for the three images.

- [ ] **Step 3: Implement three-photo labels**

In `frontend-admin/src/views/manage/user-list/UserAuditDialog.vue`, add this helper after `getUserId`:

```ts
function getPhotoLabel(index: number): string {
  return ['身份证正面', '身份证反面', '手持身份证'][index] || `证件照片${index + 1}`
}
```

Replace the image item template inside `v-for` with:

```vue
<div
  v-for="(pic, index) in detail.pics"
  :key="index"
  class="image-item"
  @click="handlePreview(pic)"
>
  <el-image
    :src="pic"
    fit="cover"
    style="width: 120px; height: 120px"
    :preview-src-list="detail.pics"
    :initial-index="index"
    preview-teleported
  >
    <template #error>
      <div class="image-error">
        <el-icon><Picture /></el-icon>
        <span>加载失败</span>
      </div>
    </template>
    <template #placeholder>
      <div class="image-placeholder">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
    </template>
  </el-image>
  <div class="image-label">{{ getPhotoLabel(index) }}</div>
</div>
```

Add this style inside the existing `<style scoped lang="scss">`:

```scss
.image-label {
  width: 120px;
  padding: 6px 0 2px;
  color: $text-secondary;
  font-size: $font-size-small;
  line-height: 1.3;
  text-align: center;
}
```

- [ ] **Step 4: Run the targeted test**

Run:

```bash
cd frontend-admin
CI=true ./node_modules/.bin/vitest run src/views/manage/user-list/__tests__/UserAuditDialog.test.ts
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add frontend-admin/src/views/manage/user-list/UserAuditDialog.vue frontend-admin/src/views/manage/user-list/__tests__/UserAuditDialog.test.ts
git commit -m "feat: label realname audit photos" -m "Show the legacy three-photo review semantics in the admin audit dialog so reviewers can distinguish front, back, and hand-held ID images." -m "Constraint: Keep the existing audit list and preview behavior." -m "Tested: CI=true ./node_modules/.bin/vitest run src/views/manage/user-list/__tests__/UserAuditDialog.test.ts" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 5: Frontend Realname API Contract

**Files:**
- Modify: `frontend/src/api/types/index.ts`
- Modify: `frontend/src/api/modules/oauth.ts`
- Modify: `frontend/src/api/__tests__/api-contracts.test.ts`

- [ ] **Step 1: Write the failing API contract tests**

In `frontend/src/api/__tests__/api-contracts.test.ts`, add this import with the other API imports:

```ts
import * as oauthApi from '../modules/oauth'
```

Add these tests inside `describe('API contracts', () => { ... })`:

```ts
it('realname OCR posts cardfront to /oauth/ocr', async () => {
  await oauthApi.ocrIdCard('data:image/jpeg;base64,abc')
  expect(requestMocks.post).toHaveBeenCalledWith('/oauth/ocr', { cardfront: 'data:image/jpeg;base64,abc' })
})

it('realname submit posts name certcode and photos to /oauth/add', async () => {
  await oauthApi.submitAuth({
    name: '张三',
    certcode: '330102199001011234',
    pics: [
      'data:image/jpeg;base64,front',
      'data:image/jpeg;base64,back',
      'data:image/jpeg;base64,hand',
    ],
  })

  expect(requestMocks.post).toHaveBeenCalledWith('/oauth/add', {
    name: '张三',
    certcode: '330102199001011234',
    pics: [
      'data:image/jpeg;base64,front',
      'data:image/jpeg;base64,back',
      'data:image/jpeg;base64,hand',
    ],
  })
})
```

- [ ] **Step 2: Run the tests to verify current contract**

Run:

```bash
cd frontend
CI=true ./node_modules/.bin/vitest run src/api/__tests__/api-contracts.test.ts
```

Expected: PASS. These tests lock the existing API shape before the page starts depending on it.

- [ ] **Step 3: Add realname status typing**

In `frontend/src/api/types/index.ts`, add this near the realname types:

```ts
export type RealnameStatus = '0' | '1' | '2' | '3' | '4' | string

export interface RealnamePaymentState {
  paid: boolean
  rejectedRetry: boolean
  canSubmitWithoutPay: boolean
}
```

Then change `OauthInfo.status` to use the new type:

```ts
status: RealnameStatus
```

- [ ] **Step 4: Add local constants in oauth module**

In `frontend/src/api/modules/oauth.ts`, add exports near the imports:

```ts
export const REALNAME_ORDER_TYPE = '1'
export const REALNAME_AUDIT_FEE_YUAN = 29
export const REALNAME_PHOTO_COUNT = 3
```

No endpoint changes are needed in this task.

- [ ] **Step 5: Run the targeted tests**

Run:

```bash
cd frontend
CI=true ./node_modules/.bin/vitest run src/api/__tests__/api-contracts.test.ts
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/api/types/index.ts frontend/src/api/modules/oauth.ts frontend/src/api/__tests__/api-contracts.test.ts
git commit -m "test: lock realname api contracts" -m "Document and guard the OCR and realname submit contracts before wiring the paid three-photo page flow." -m "Constraint: OCR and submit continue to use existing endpoints." -m "Tested: CI=true ./node_modules/.bin/vitest run src/api/__tests__/api-contracts.test.ts" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 6: Mini Program Paid Three-Photo Realname Page

**Files:**
- Create: `frontend/src/pages-sub/user/__tests__/realname-source.test.ts`
- Modify: `frontend/src/pages-sub/user/realname.vue`

- [ ] **Step 1: Write the failing source guard**

Create `frontend/src/pages-sub/user/__tests__/realname-source.test.ts`:

```ts
import { describe, expect, it } from 'vitest'
import fs from 'node:fs'
import path from 'node:path'

describe('realname page legacy paid flow source', () => {
  const source = fs.readFileSync(path.resolve(__dirname, '../realname.vue'), 'utf8')

  it('requires three photos and labels hand-held ID upload', () => {
    expect(source).toContain(\"handPath\")
    expect(source).toContain(\"chooseImage('hand')\")
    expect(source).toContain('手持身份证')
    expect(source).toContain('REALNAME_PHOTO_COUNT')
  })

  it('pays before first submission and waits for server confirmation', () => {
    expect(source).toContain('createRealnameOrder')
    expect(source).toContain('waitForRealnamePayment')
    expect(source).toContain('getOrderStatus')
    expect(source).toContain('submitAfterPaymentConfirmed')
    expect(source).not.toContain(\"pics: [frontPath.value, backPath.value]\")
  })
})
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```bash
cd frontend
CI=true ./node_modules/.bin/vitest run src/pages-sub/user/__tests__/realname-source.test.ts
```

Expected: FAIL because the page currently has only front and back photos and no payment-first flow.

- [ ] **Step 3: Replace the script with paid-flow logic**

In `frontend/src/pages-sub/user/realname.vue`, replace the `<script setup lang="ts">` block with:

```ts
<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { computed, reactive, ref } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import * as oauthApi from '@/api/modules/oauth'
import * as paymentApi from '@/api/modules/payment'
import { useUserStore } from '@/stores/user'
import type { OauthInfo, OrderInfo } from '@/api/types'

const REALNAME_PHOTO_COUNT = oauthApi.REALNAME_PHOTO_COUNT
const PAYMENT_POLL_LIMIT = 20
const PAYMENT_POLL_INTERVAL_MS = 1500

const userStore = useUserStore()
const loading = ref(true)
const submitting = ref(false)
const paying = ref(false)
const authInfo = ref<OauthInfo | null>(null)
const frontPath = ref('')
const backPath = ref('')
const handPath = ref('')
const form = reactive({ name: '', certcode: '' })

const status = computed(() => authInfo.value?.status || userStore.userInfo?.status || '0')
const canSubmitWithoutPay = computed(() => status.value === '3' || status.value === '4')
const needsPayment = computed(() => status.value === '0' || !status.value)
const busy = computed(() => submitting.value || paying.value)

const submitDisabled = computed(() => (
  busy.value
  || !form.name.trim()
  || !/^\\d{15}$|^\\d{17}[\\dXx]$/.test(form.certcode.trim())
  || selectedPhotos.value.length !== REALNAME_PHOTO_COUNT
))

const selectedPhotos = computed(() => [frontPath.value, backPath.value, handPath.value].filter(Boolean))

const maskedName = computed(() => {
  const value = authInfo.value?.name || ''
  return value.length > 1 ? `${value[0]}${'*'.repeat(value.length - 1)}` : value
})

const maskedCode = computed(() => {
  const value = authInfo.value?.certcode || ''
  return value.length > 8 ? `${value.slice(0, 3)}***********${value.slice(-4)}` : value
})

async function loadDetail(): Promise<void> {
  loading.value = true
  try {
    authInfo.value = (await oauthApi.getAuthDetail()).data || null
  } catch {
    authInfo.value = null
  } finally {
    loading.value = false
  }
}

function chooseImage(side: 'front' | 'back' | 'hand'): void {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async ({ tempFilePaths }) => {
      const path = tempFilePaths[0]
      if (!path) return
      if (side === 'front') {
        frontPath.value = path
        await recognizeFront(path)
      } else if (side === 'back') {
        backPath.value = path
      } else {
        handPath.value = path
      }
    },
  })
}

async function recognizeFront(path: string): Promise<void> {
  uni.showLoading({ title: '识别中...' })
  try {
    const result = await oauthApi.ocrIdCard(path)
    form.name = result.data?.name || ''
    form.certcode = result.data?.certcode || ''
  } catch {
    uni.showToast({ title: '识别失败，请手动填写', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

async function submit(): Promise<void> {
  if (submitDisabled.value) return
  if (canSubmitWithoutPay.value) {
    await submitAfterPaymentConfirmed()
    return
  }
  if (needsPayment.value) {
    await confirmAndPay()
    return
  }
  uni.showToast({ title: '当前状态暂不可提交', icon: 'none' })
}

async function confirmAndPay(): Promise<void> {
  const confirmed = await confirmRealnamePayment()
  if (!confirmed) return

  paying.value = true
  try {
    const order = await createRealnameOrder()
    await invokeWechatPayment(order)
    uni.showLoading({ title: '服务端确认中...' })
    const paid = await waitForRealnamePayment(order.out_trade_no)
    uni.hideLoading()
    if (!paid) {
      uni.showToast({ title: '支付确认中，请稍后重试', icon: 'none' })
      return
    }
    await userStore.updateUserInfo()
    await loadDetail()
    await submitAfterPaymentConfirmed()
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: error instanceof Error ? error.message : '支付失败', icon: 'none' })
  } finally {
    paying.value = false
  }
}

function confirmRealnamePayment(): Promise<boolean> {
  return new Promise((resolve) => {
    uni.showModal({
      title: '实名认证',
      content: `实名信息需人工审核，将收取 ${oauthApi.REALNAME_AUDIT_FEE_YUAN} 元审核费。审核失败后重新提交无需再次支付。`,
      confirmText: '去支付',
      cancelText: '取消',
      success: ({ confirm }) => resolve(Boolean(confirm)),
      fail: () => resolve(false),
    })
  })
}

async function createRealnameOrder() {
  const order = await paymentApi.createOrder({
    type: oauthApi.REALNAME_ORDER_TYPE,
    total_fee: oauthApi.REALNAME_AUDIT_FEE_YUAN,
  })
  if (!order.data?.package || !order.data.timeStamp || !order.data.nonceStr || !order.data.paySign || !order.data.out_trade_no) {
    throw new Error('支付参数不完整')
  }
  return order.data
}

function invokeWechatPayment(data: {
  timeStamp: string
  nonceStr: string
  package: string
  signType: string
  paySign: string
}): Promise<void> {
  return new Promise((resolve, reject) => {
    uni.requestPayment({
      provider: 'wxpay',
      orderInfo: {},
      timeStamp: data.timeStamp,
      nonceStr: data.nonceStr,
      package: data.package,
      signType: data.signType as 'MD5' | 'HMAC-SHA256',
      paySign: data.paySign,
      success: () => resolve(),
      fail: (error) => reject(new Error(error.errMsg || '支付失败')),
    })
  })
}

async function waitForRealnamePayment(outTradeNo: string): Promise<boolean> {
  for (let index = 0; index < PAYMENT_POLL_LIMIT; index += 1) {
    const result = await paymentApi.getOrderStatus(outTradeNo)
    const paid = (result.data?.content || []).some(isPaidRealnameOrder)
    if (paid) return true
    await delay(PAYMENT_POLL_INTERVAL_MS)
  }
  return false
}

function isPaidRealnameOrder(order: OrderInfo): boolean {
  return order.type === oauthApi.REALNAME_ORDER_TYPE && (order.status === '1' || order.result_code === 'SUCCESS')
}

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

async function submitAfterPaymentConfirmed(): Promise<void> {
  if (submitDisabled.value) return
  submitting.value = true
  try {
    await oauthApi.submitAuth({
      name: form.name.trim(),
      certcode: form.certcode.trim().toUpperCase(),
      pics: [frontPath.value, backPath.value, handPath.value],
    })
    clearForm()
    await userStore.updateUserInfo()
    uni.showToast({ title: '提交成功', icon: 'success' })
    await loadDetail()
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function clearForm(): void {
  form.name = ''
  form.certcode = ''
  frontPath.value = ''
  backPath.value = ''
  handPath.value = ''
}

onLoad(loadDetail)
onUnload(clearForm)
</script>
```

- [ ] **Step 4: Update the template for three-photo and payment states**

In `frontend/src/pages-sub/user/realname.vue`, keep the existing loading/audited state blocks and replace the editable form area with:

```vue
<view v-else class="card">
  <view v-if="authInfo?.status === '3'" class="warning">审核未通过，请核对资料后重新提交。本次重新提交无需再次支付。</view>

  <text class="label">身份证正面</text>
  <view class="picker" @tap="chooseImage('front')">
    <image v-if="frontPath" :src="frontPath" mode="aspectFill" />
    <text v-else>点击选择身份证正面</text>
  </view>

  <text class="label">身份证反面</text>
  <view class="picker" @tap="chooseImage('back')">
    <image v-if="backPath" :src="backPath" mode="aspectFill" />
    <text v-else>点击选择身份证反面</text>
  </view>

  <text class="label">手持身份证</text>
  <view class="picker" @tap="chooseImage('hand')">
    <image v-if="handPath" :src="handPath" mode="aspectFill" />
    <text v-else>点击选择手持身份证照片</text>
  </view>

  <text class="label">真实姓名</text>
  <input v-model="form.name" class="input" maxlength="30" placeholder="请输入真实姓名" />

  <text class="label">证件号码</text>
  <input v-model="form.certcode" class="input" maxlength="18" placeholder="请输入证件号码" />

  <button class="submit" :disabled="submitDisabled" :loading="busy" @tap="submit">
    {{ busy ? '处理中...' : (canSubmitWithoutPay ? '提交认证' : '支付并提交') }}
  </button>
  <text class="privacy">实名认证审核费 29 元；审核失败后重新提交无需再次支付。页面退出后会清理本地临时资料。</text>
</view>
```

- [ ] **Step 5: Run the source guard**

Run:

```bash
cd frontend
CI=true ./node_modules/.bin/vitest run src/pages-sub/user/__tests__/realname-source.test.ts
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/pages-sub/user/realname.vue frontend/src/pages-sub/user/__tests__/realname-source.test.ts
git commit -m "feat: restore paid realname page flow" -m "Restore the legacy mini program realname flow with three photos, first-time payment, server-confirmed payment polling, and free retry after audit rejection." -m "Constraint: The page does not trust client-side payment success without /order/status confirmation." -m "Tested: CI=true ./node_modules/.bin/vitest run src/pages-sub/user/__tests__/realname-source.test.ts" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 7: Decision Record Update

**Files:**
- Modify: `docs/decisions/ADR-REALNAME-PHOTO-AND-FEE-POLICY.md`

- [ ] **Step 1: Update the ADR status and decision**

Replace the status line:

```md
状态：SUPERSEDED_BY_2026_07_10_LEGACY_PAID_FLOW
```

Replace the decision section with:

```md
## 决策

2026-07-10 起，项目恢复旧版三照付费实名认证：

- 恢复身份证正面。
- 恢复身份证反面。
- 恢复手持身份证。
- 恢复 29 元实名认证审核费。
- 审核失败后重新提交无需再次支付。
- OCR 仍只识别身份证正面。
- `/oauth/add` 继续接受图片数组，并要求新版业务提交三张图片。
```

Add this note after the background section:

```md
## 变更原因

项目仍处于开发初期，没有历史用户需要兼容。业务方已确认实名认证完全沿用旧版逻辑，因此原“两照免费”策略不再作为当前实现依据。
```

- [ ] **Step 2: Verify the ADR names the new spec**

Run:

```bash
rg -n "SUPERSEDED_BY_2026_07_10_LEGACY_PAID_FLOW|2026-07-10|三照|29 元|审核失败后重新提交无需再次支付" docs/decisions/ADR-REALNAME-PHOTO-AND-FEE-POLICY.md
```

Expected: all listed phrases appear.

- [ ] **Step 3: Commit**

```bash
git add docs/decisions/ADR-REALNAME-PHOTO-AND-FEE-POLICY.md
git commit -m "docs: supersede realname policy adr" -m "Align the prior realname photo and fee decision with the approved legacy paid three-photo flow." -m "Constraint: Fresh project state means no two-photo-free historical migration is needed." -m "Tested: rg -n \"SUPERSEDED_BY_2026_07_10_LEGACY_PAID_FLOW|2026-07-10|三照|29 元|审核失败后重新提交无需再次支付\" docs/decisions/ADR-REALNAME-PHOTO-AND-FEE-POLICY.md" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 8: Final Targeted Verification And Scope Audit

**Files:**
- Inspect only: all files modified by Tasks 1-7

- [ ] **Step 1: Run backend targeted tests**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=RealnameOrderSecuritySourceTest,AdminUserAuditSourceTest test
mvn -pl system-domain -Dtest=UserServiceRealnameSecuritySourceTest test
```

Expected: both commands PASS.

- [ ] **Step 2: Run frontend targeted tests**

Run:

```bash
cd frontend
CI=true ./node_modules/.bin/vitest run src/api/__tests__/api-contracts.test.ts src/pages-sub/user/__tests__/realname-source.test.ts
cd ../frontend-admin
CI=true ./node_modules/.bin/vitest run src/views/manage/user-list/__tests__/UserAuditDialog.test.ts
```

Expected: all commands PASS.

- [ ] **Step 3: Check whitespace and scope**

Run:

```bash
git diff --check
git status --short
git log --oneline -8
```

Expected:

- `git diff --check` prints no output.
- `git status --short` only shows expected local files, plus the existing `.omx/state/session.json` if it remains locally modified.
- Recent commits show the task commits from this plan.

- [ ] **Step 4: Manual test notes for the user**

Report these manual test paths to the user:

```text
1. 小程序首次实名：选择正面、反面、手持身份证，OCR 自动回填，点击支付并提交，微信支付成功后进入待审核。
2. 后台审核拒绝：管理后台实名详情能看三照并拒绝，用户回到小程序后可不付费重新提交。
3. 后台审核通过：用户认证状态变为已认证，姓名和身份证号脱敏展示。
4. 安全检查：前端不能改低价格绕过后端；未支付直接调用 /oauth/add 应失败。
```

- [ ] **Step 5: Final commit if verification creates changes**

If verification causes no file changes, do not create an empty commit. If a small fix is required, commit it with:

```bash
git add <changed-files>
git commit -m "fix: complete realname legacy verification" -m "Address final targeted verification findings for the paid three-photo realname flow." -m "Constraint: Keep the implementation inside the approved spec scope." -m "Tested: targeted backend and frontend tests from Task 8" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

## Self-Review

- Spec coverage: Tasks 1 and 2 cover server-side amount, payment gate, three-photo requirement, duplicate submission, and bypass prevention. Task 3 covers admin audit empty response safety. Task 4 covers admin three-photo detail review. Tasks 5 and 6 cover the mini program OCR, payment, server-confirmed order status, three-photo submit, rejection retry, and display states. Task 7 updates the superseded ADR. Task 8 covers final targeted verification and manual handoff notes.
- Placeholder scan: no unresolved placeholder text remains in this plan.
- Type consistency: `REALNAME_ORDER_TYPE`, `REALNAME_AUDIT_FEE_YUAN`, `REALNAME_PHOTO_COUNT`, `OauthInfo.status`, and `OrderInfo` are used consistently across API and page tasks.
