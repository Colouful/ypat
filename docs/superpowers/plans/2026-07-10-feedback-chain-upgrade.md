# Feedback Chain Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完善新版小程序意见反馈提交、反馈图片上传、后端存储和新版管理后台反馈治理闭环。

**Architecture:** 采用“轻量治理闭环”方案：小程序提交固定类型、内容、联系方式和图片 URL；后端在现有 `t_feedback` 链路上扩展字段、上传入口、后台分页/详情/处理接口；管理后台新增独立意见反馈页面并复用现有菜单、权限、Element Plus（饿了么组件库）页面模式。

**Tech Stack:** Vue 3（前端框架）、uni-app（小程序框架）、TypeScript（类型脚本）、Element Plus（后台 UI 组件库）、Spring Boot（后端框架）、Spring Data JPA（数据访问）、Feign（服务调用）、MySQL（数据库）、Vitest（前端测试）、JUnit（后端测试）。

---

## File Structure

- Modify: `backend/system-object/src/main/java/com/ypat/FeedbackQo.java`，扩展反馈数据传输字段。
- Modify: `backend/system-domain/src/main/java/com/ypat/entity/Feedback.java`，扩展 `t_feedback` 实体字段。
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/FeedbackRepository.java`，增加待处理状态更新方法。
- Modify: `backend/system-domain/src/main/java/com/ypat/service/FeedbackService.java`，实现入库、后台列表、详情、处理。
- Modify: `backend/system-restapi/src/main/java/com/ypat/controller/FeedbackController.java`，暴露内部后台接口。
- Modify: `backend/system-wap/src/main/java/com/ypat/service/FeedbackServiceClient.java`，增加 Feign（服务调用）后台接口。
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/FeedbackController.java`，扩展小程序提交入口、增加反馈图片上传和管理端接口代理。
- Modify: `backend/system-wap/src/main/java/com/ypat/storage/StorageBizPath.java`，新增反馈图片存储路径。
- Create: `backend/dev/mysql/20260710_alter_feedback_chain.sql`，新增字段迁移脚本。
- Create: `backend/dev/mysql/20260710_alter_feedback_chain_rollback.sql`，新增字段回滚脚本。
- Modify: `frontend/src/api/types/index.ts`，扩展 `FeedbackAddParams`。
- Modify: `frontend/src/api/modules/feedback.ts`，增加反馈图片上传 API。
- Modify: `frontend/src/pages-sub/user/feedback.vue`，完善类型、图片、提交交互。
- Modify: `frontend/src/pages-sub/user/feedback.test.ts`，补页面源代码验证。
- Modify: `frontend/src/api/__tests__/api-contracts.test.ts`，补 API 契约验证。
- Create: `frontend-admin/src/api/modules/feedback.ts`，新增后台反馈 API。
- Create: `frontend-admin/src/views/manage/feedback-list/index.vue`，新增后台反馈列表页面。
- Modify: `frontend-admin/src/constants/menu.ts`，新增菜单。
- Modify: `frontend-admin/src/stores/modules/permission.ts`，注册动态路由组件。
- Create: `frontend-admin/src/views/manage/feedback-list/feedback-list.test.ts`，新增后台页面源代码验证。

## Task 1: 数据模型和数据库迁移

**Files:**
- Modify: `backend/system-object/src/main/java/com/ypat/FeedbackQo.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/entity/Feedback.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/FeedbackRepository.java`
- Create: `backend/dev/mysql/20260710_alter_feedback_chain.sql`
- Create: `backend/dev/mysql/20260710_alter_feedback_chain_rollback.sql`

- [ ] **Step 1: 更新数据传输对象字段**

在 `backend/system-object/src/main/java/com/ypat/FeedbackQo.java` 增加字段和 getter/setter：

```java
private String type;
private String pics;
private String handleReason;
private Long handledBy;
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private Date handledAt;
private String userNickname;
private String typeText;
private String statusText;
private Date createdAt;
private Date updatedAt;
```

字段映射说明：

```java
public Date getCreatedAt() {
    return createdAt == null ? credate : createdAt;
}

public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
}

public Date getUpdatedAt() {
    return updatedAt == null ? upddate : updatedAt;
}

public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
}
```

- [ ] **Step 2: 更新 JPA（Java 持久层）实体字段**

在 `backend/system-domain/src/main/java/com/ypat/entity/Feedback.java` 增加字段：

```java
@Column(length = 32)
private String type;
@Column(length = 1000)
private String pics;
@Column(name = "handle_reason", length = 500)
private String handleReason;
@Column(name = "handled_by")
private Long handledBy;
@Column(name = "handled_at")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@Temporal(TemporalType.TIMESTAMP)
private Date handledAt;
```

并补齐每个字段的 getter/setter，命名保持 `getHandleReason()`、`setHandleReason(String handleReason)`、`getHandledAt()`。

- [ ] **Step 3: 增加仓库状态更新方法**

在 `backend/system-domain/src/main/java/com/ypat/repository/FeedbackRepository.java` 增加导入：

```java
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Date;
```

在接口中增加方法：

```java
@Modifying
@Query("update Feedback f set f.status = :status, f.handleReason = :handleReason, f.handledBy = :handledBy, f.handledAt = :handledAt, f.upddate = :updatedAt where f.id = :id and f.status = '0'")
int updatePendingStatus(@Param("id") Long id,
                        @Param("status") String status,
                        @Param("handleReason") String handleReason,
                        @Param("handledBy") Long handledBy,
                        @Param("handledAt") Date handledAt,
                        @Param("updatedAt") Date updatedAt);
```

- [ ] **Step 4: 新增迁移脚本**

创建 `backend/dev/mysql/20260710_alter_feedback_chain.sql`：

```sql
-- 手工运维脚本：正式环境执行前必须完成备份，并确认 t_feedback 当前结构。
-- 校验：
--   SHOW CREATE TABLE `t_feedback`;
--   SHOW INDEX FROM `t_feedback`;
ALTER TABLE `t_feedback`
  ADD COLUMN `type` varchar(32) NOT NULL DEFAULT 'other' AFTER `userid`,
  ADD COLUMN `pics` varchar(1000) DEFAULT NULL AFTER `contact`,
  ADD COLUMN `handle_reason` varchar(500) DEFAULT NULL AFTER `status`,
  ADD COLUMN `handled_by` bigint(20) DEFAULT NULL AFTER `handle_reason`,
  ADD COLUMN `handled_at` datetime DEFAULT NULL AFTER `handled_by`;

CREATE INDEX `idx_feedback_type_credate` ON `t_feedback` (`type`, `credate`);
```

- [ ] **Step 5: 新增回滚脚本**

创建 `backend/dev/mysql/20260710_alter_feedback_chain_rollback.sql`：

```sql
-- 手工回滚脚本：仅在确认本次上线需要回滚且已备份 t_feedback 数据后执行。
-- 校验：
--   SHOW CREATE TABLE `t_feedback`;
DROP INDEX `idx_feedback_type_credate` ON `t_feedback`;

ALTER TABLE `t_feedback`
  DROP COLUMN `handled_at`,
  DROP COLUMN `handled_by`,
  DROP COLUMN `handle_reason`,
  DROP COLUMN `pics`,
  DROP COLUMN `type`;
```

- [ ] **Step 6: 运行格式检查**

Run:

```bash
git diff --check
```

Expected: no output, exit code 0.

- [ ] **Step 7: Commit**

```bash
git add backend/system-object/src/main/java/com/ypat/FeedbackQo.java backend/system-domain/src/main/java/com/ypat/entity/Feedback.java backend/system-domain/src/main/java/com/ypat/repository/FeedbackRepository.java backend/dev/mysql/20260710_alter_feedback_chain.sql backend/dev/mysql/20260710_alter_feedback_chain_rollback.sql
git commit -m "feat: extend feedback data model"
```

## Task 2: 后端提交入口、图片上传和后台接口

**Files:**
- Modify: `backend/system-domain/src/main/java/com/ypat/service/FeedbackService.java`
- Modify: `backend/system-restapi/src/main/java/com/ypat/controller/FeedbackController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/service/FeedbackServiceClient.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/FeedbackController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/storage/StorageBizPath.java`
- Modify: `backend/system-wap/src/test/java/com/ypat/controller/FeedbackControllerTest.java`

- [ ] **Step 1: 写后端控制器测试覆盖新增字段**

在 `backend/system-wap/src/test/java/com/ypat/controller/FeedbackControllerTest.java` 的 `addUsesAuthenticatedUserAndSanitizesInput` 中把调用改为：

```java
String result = controller.add("function", "  这里是一段合法反馈<script>  ", "  13800138000  ", "https://example.com/a.jpg");
```

增加断言：

```java
assertEquals("function", feedbackServiceClient.feedbackQo.getType());
assertEquals("https://example.com/a.jpg", feedbackServiceClient.feedbackQo.getPics());
```

新增图片数量测试：

```java
@Test(expected = SysException.class)
public void addRejectsTooManyPics() {
    controller.add("function", "这里是一段合法反馈内容", "", "a.jpg,b.jpg,c.jpg,d.jpg");
}
```

把其他 `controller.add(content, contact)` 调用改为：

```java
controller.add("other", "这里是一段合法反馈内容", "");
```

或：

```java
controller.add("other", "这里是一段合法反馈内容", "", "");
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd backend && mvn -pl system-wap -Dtest=FeedbackControllerTest test
```

Expected: FAIL，失败原因包含 `method add in class FeedbackController cannot be applied to given types` 或 `getType` 方法缺失。

- [ ] **Step 3: 扩展存储业务路径**

在 `backend/system-wap/src/main/java/com/ypat/storage/StorageBizPath.java` 将枚举改为：

```java
public enum StorageBizPath {
    ADMIN("admin"),
    YPAT("ypat"),
    WORK("work"),
    AVATAR("avatar"),
    REALNAME("realname"),
    FEEDBACK("feedback");
```

- [ ] **Step 4: 实现领域服务方法**

在 `backend/system-domain/src/main/java/com/ypat/service/FeedbackService.java` 增加常量：

```java
private static final String TYPE_OTHER = "other";
private static final String STATUS_PENDING = "0";
private static final String STATUS_HANDLED = "1";
private static final String STATUS_IGNORED = "2";
```

增加 `adminList`、`adminDetail`、`adminHandle`、`normalizeType`、`normalizeStatus`、`statusText`、`typeText`、`toAdminItem` 方法。核心逻辑：

```java
public Map<String, Object> adminList(Integer page, Integer size, String status, String type, Long userId) {
    final int currentPage = page == null || page < 0 ? 0 : page;
    final int pageSize = size == null || size <= 0 ? 10 : Math.min(size, 50);
    final String normalizedStatus = normalizeQueryStatus(status);
    final String normalizedType = normalizeQueryType(type);

    Specification<Feedback> spec = (root, query, cb) -> {
        List<Predicate> predicates = new ArrayList<>();
        if (normalizedStatus != null) predicates.add(cb.equal(root.get("status"), normalizedStatus));
        if (normalizedType != null) predicates.add(cb.equal(root.get("type"), normalizedType));
        if (userId != null) predicates.add(cb.equal(root.get("userid"), userId));
        return cb.and(predicates.toArray(new Predicate[0]));
    };

    Page<Feedback> result = feedbackRepository.findAll(
        spec,
        new PageRequest(currentPage, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "credate"))));

    List<Map<String, Object>> content = new ArrayList<>();
    for (Feedback feedback : result.getContent()) {
        content.add(toAdminItem(feedback));
    }

    Map<String, Object> res = new HashMap<>();
    res.put("content", content);
    res.put("totalElements", result.getTotalElements());
    res.put("totalPages", result.getTotalPages());
    return res;
}
```

需要新增导入：

```java
import com.ypat.entity.User;
import com.ypat.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
```

并注入：

```java
@Autowired
private UserRepository userRepository;
```

- [ ] **Step 5: 扩展 restapi（内部接口）控制器**

在 `backend/system-restapi/src/main/java/com/ypat/controller/FeedbackController.java` 增加接口：

```java
@GetMapping("/service/feedback/admin/list")
public ResponseApiBody adminList(@RequestParam("page") Integer page,
                                 @RequestParam("size") Integer size,
                                 @RequestParam(value = "status", required = false) String status,
                                 @RequestParam(value = "type", required = false) String type,
                                 @RequestParam(value = "userId", required = false) Long userId) {
    return ResponseApiBody.success(feedbackService.adminList(page, size, status, type, userId));
}

@GetMapping("/service/feedback/admin/detail")
public ResponseApiBody adminDetail(@RequestParam("id") Long id) {
    return ResponseApiBody.success(feedbackService.adminDetail(id));
}

@PostMapping("/service/feedback/admin/handle")
public ResponseApiBody adminHandle(@RequestBody FeedbackQo feedbackQo) {
    feedbackService.adminHandle(feedbackQo);
    return ResponseApiBody.success("处理完成");
}
```

- [ ] **Step 6: 扩展 Feign（服务调用）接口**

在 `backend/system-wap/src/main/java/com/ypat/service/FeedbackServiceClient.java` 增加：

```java
@GetMapping("/service/feedback/admin/list")
String adminList(@RequestParam("page") Integer page,
                 @RequestParam("size") Integer size,
                 @RequestParam(value = "status", required = false) String status,
                 @RequestParam(value = "type", required = false) String type,
                 @RequestParam(value = "userId", required = false) Long userId);

@GetMapping("/service/feedback/admin/detail")
String adminDetail(@RequestParam("id") Long id);

@PostMapping("/service/feedback/admin/handle")
String adminHandle(@RequestBody FeedbackQo feedbackQo);
```

- [ ] **Step 7: 扩展 wap（小程序/后台网关）控制器**

在 `backend/system-wap/src/main/java/com/ypat/controller/FeedbackController.java` 把提交入口签名改为：

```java
@PostMapping("/feedback/add")
public String add(String type, String content, String contact, String pics) {
```

增加类型和图片校验：

```java
private static final int MAX_PICS_LENGTH = 1000;
private static final int MAX_PIC_COUNT = 3;
```

```java
String normalizedType = normalizeType(type);
String normalizedPics = sanitize(StringUtils.trimToEmpty(pics));
if (normalizedPics.length() > MAX_PICS_LENGTH || countPics(normalizedPics) > MAX_PIC_COUNT) {
    throw new SysException(ResponseCode.FAIL_PARA);
}
feedbackQo.setType(normalizedType);
feedbackQo.setPics(normalizedPics);
```

增加后台代理接口：

```java
@GetMapping("/admin/feedback/list")
public ResponseApiBody adminList(@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                 @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
                                 @RequestParam(value = "status", required = false) String status,
                                 @RequestParam(value = "type", required = false) String type,
                                 @RequestParam(value = "userId", required = false) Long userId) {
    String json = feedbackServiceClient.adminList(normalizePage(page), normalizeSize(size), status, type, userId);
    return ResponseApiBody.success(parseResponseRes(json));
}
```

`parseResponseRes` 可从 `AdminWorkComplainController` 复制同名方法，并保留相同错误文案 `服务响应格式错误`。

- [ ] **Step 8: 增加反馈图片上传入口**

在 `FeedbackController` 注入：

```java
@Autowired
private StorageService storageService;
```

新增接口：

```java
@PostMapping("/feedback/upload/image")
public ResponseApiBody uploadImage(@RequestPart("file") MultipartFile file) throws IOException {
    String userId = UserUtil.getUserId();
    if (StringUtils.isBlank(userId)) throw new SysException(ResponseCode.FAIL_AUTH);
    if (file == null || file.isEmpty()) throw new SysException(ResponseCode.FAIL_PARA, "请选择要上传的文件");
    if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
        throw new SysException(ResponseCode.FAIL_PARA, "仅允许上传图片文件");
    }
    StoredObject storedObject = storageService.upload(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), StorageBizPath.FEEDBACK);
    Map<String, Object> res = new HashMap<>();
    res.put("url", storedObject.url());
    return ResponseApiBody.success(res);
}
```

- [ ] **Step 9: 运行后端局部测试**

Run:

```bash
cd backend && mvn -pl system-wap -Dtest=FeedbackControllerTest test
```

Expected: PASS，输出包含 `Tests run:` 且 `Failures: 0`。

- [ ] **Step 10: Commit**

```bash
git add backend/system-domain/src/main/java/com/ypat/service/FeedbackService.java backend/system-restapi/src/main/java/com/ypat/controller/FeedbackController.java backend/system-wap/src/main/java/com/ypat/service/FeedbackServiceClient.java backend/system-wap/src/main/java/com/ypat/controller/FeedbackController.java backend/system-wap/src/main/java/com/ypat/storage/StorageBizPath.java backend/system-wap/src/test/java/com/ypat/controller/FeedbackControllerTest.java
git commit -m "feat: add feedback backend workflow"
```

## Task 3: 小程序 API 和反馈页面

**Files:**
- Modify: `frontend/src/api/types/index.ts`
- Modify: `frontend/src/api/modules/feedback.ts`
- Modify: `frontend/src/pages-sub/user/feedback.vue`
- Modify: `frontend/src/pages-sub/user/feedback.test.ts`
- Modify: `frontend/src/api/__tests__/api-contracts.test.ts`

- [ ] **Step 1: 更新 API 契约测试**

在 `frontend/src/api/__tests__/api-contracts.test.ts` 的反馈用例中改为：

```ts
it('feedback add uses typed form endpoint', async () => {
  await feedbackApi.addFeedback({
    type: 'function',
    content: '这里是一条有效反馈内容',
    contact: '13800138000',
    pics: 'https://example.com/a.jpg',
  })
  expect(requestMocks.post).toHaveBeenCalledWith('/feedback/add', {
    type: 'function',
    content: '这里是一条有效反馈内容',
    contact: '13800138000',
    pics: 'https://example.com/a.jpg',
  })
})
```

- [ ] **Step 2: 更新页面源代码测试**

在 `frontend/src/pages-sub/user/feedback.test.ts` 增加：

```ts
it('renders feedback type and image upload controls', () => {
  const file = fileURLToPath(new URL('./feedback.vue', import.meta.url))
  const source = readFileSync(file, 'utf8')

  expect(source).toContain('feedbackTypes')
  expect(source).toContain('功能异常')
  expect(source).toContain('体验建议')
  expect(source).toContain('反馈图片')
  expect(source).toContain('chooseFeedbackImages')
  expect(source).toContain('previewFeedbackImage')
  expect(source).toContain('uploadFeedbackImage')
})
```

- [ ] **Step 3: 运行前端测试确认失败**

Run:

```bash
cd frontend && CI=true pnpm exec vitest run src/api/__tests__/api-contracts.test.ts src/pages-sub/user/feedback.test.ts
```

Expected: FAIL，失败原因包含 `expected ... to contain 'feedbackTypes'` 或提交参数缺少 `type`。

- [ ] **Step 4: 扩展小程序 API 类型**

在 `frontend/src/api/types/index.ts` 修改反馈类型：

```ts
export type FeedbackType = 'function' | 'experience' | 'account' | 'payment' | 'content' | 'other'

export interface FeedbackAddParams {
  type: FeedbackType
  content: string
  contact?: string
  pics?: string
}
```

- [ ] **Step 5: 增加反馈图片上传 API**

在 `frontend/src/api/modules/feedback.ts` 修改为：

```ts
import { post, upload } from '../request'
import type { ApiResult, FeedbackAddParams } from '../types'
import type { MediaUploadResult, UploadProgressEvent } from '../types/media'

export function addFeedback(data: FeedbackAddParams): Promise<ApiResult<null>> {
  return post('/feedback/add', data)
}

export function uploadFeedbackImage(
  filePath: string,
  onProgress?: (e: UploadProgressEvent) => void,
): Promise<ApiResult<MediaUploadResult>> {
  return upload<MediaUploadResult>({
    url: '/feedback/upload/image',
    filePath,
    name: 'file',
    showLoading: false,
    onProgress,
  })
}
```

- [ ] **Step 6: 重写反馈页面脚本状态**

在 `frontend/src/pages-sub/user/feedback.vue` 的 `<script setup lang="ts">` 中增加状态：

```ts
import { computed, ref } from 'vue'
import { chooseImages } from '@/utils/media-uploader'
import type { FeedbackType } from '@/api/types'

interface FeedbackImage {
  localPath: string
  url?: string
  uploading: boolean
  error?: string
}

const feedbackTypes: Array<{ label: string; value: FeedbackType }> = [
  { label: '功能异常', value: 'function' },
  { label: '体验建议', value: 'experience' },
  { label: '账号/资料', value: 'account' },
  { label: '支付/订单', value: 'payment' },
  { label: '内容/用户举报', value: 'content' },
  { label: '其他', value: 'other' },
]

const selectedType = ref<FeedbackType>('function')
const images = ref<FeedbackImage[]>([])
const hasUploadingImage = computed(() => images.value.some((item) => item.uploading))
const hasFailedImage = computed(() => images.value.some((item) => item.error))
```

将 `isValid` 改为：

```ts
const isValid = computed(() => {
  const trimmed = content.value.trim()
  return Boolean(selectedType.value)
    && trimmed.length >= 10
    && trimmed.length <= 500
    && contact.value.trim().length <= 100
    && !hasUploadingImage.value
    && !hasFailedImage.value
})
```

- [ ] **Step 7: 增加选择、上传、删除、预览方法**

在同一脚本中增加：

```ts
async function chooseFeedbackImages(): Promise<void> {
  const remain = 3 - images.value.length
  if (remain <= 0) return
  try {
    const files = await chooseImages({ count: remain })
    const pending = files.map((file) => ({ localPath: file.path, uploading: true }))
    images.value = images.value.concat(pending)
    await Promise.all(pending.map(uploadOneImage))
  } catch {
    uni.showToast({ title: '未选择图片', icon: 'none' })
  }
}

async function uploadOneImage(item: FeedbackImage): Promise<void> {
  try {
    const res = await feedbackApi.uploadFeedbackImage(item.localPath)
    item.url = res.data?.url
    item.error = item.url ? undefined : '图片上传失败'
  } catch (error) {
    item.error = error instanceof Error ? error.message : '图片上传失败'
  } finally {
    item.uploading = false
  }
}

function removeFeedbackImage(index: number): void {
  images.value.splice(index, 1)
}

function previewFeedbackImage(index: number): void {
  const urls = images.value.map((item) => item.url || item.localPath).filter(Boolean)
  uni.previewImage({ urls, current: urls[index] })
}
```

- [ ] **Step 8: 更新提交参数**

在 `handleSubmit` 中提交：

```ts
await feedbackApi.addFeedback({
  type: selectedType.value,
  content: content.value.trim(),
  contact: contact.value.trim(),
  pics: images.value.map((item) => item.url).filter(Boolean).join(','),
})
```

- [ ] **Step 9: 更新页面模板和样式**

在模板中增加类型和图片区域：

```vue
<view class="feedback-card">
  <view class="feedback-card__header">
    <text class="feedback-card__title">反馈类型</text>
    <text class="feedback-card__required">*</text>
  </view>
  <view class="type-grid">
    <view
      v-for="item in feedbackTypes"
      :key="item.value"
      class="type-grid__item"
      :class="{ 'type-grid__item--active': selectedType === item.value }"
      @tap="selectedType = item.value"
    >
      {{ item.label }}
    </view>
  </view>
</view>

<view class="feedback-card">
  <view class="feedback-card__header">
    <text class="feedback-card__title">反馈图片</text>
    <text class="feedback-card__hint">最多 3 张</text>
  </view>
  <view class="image-grid">
    <view v-for="(item, index) in images" :key="item.localPath" class="image-grid__item">
      <image class="image-grid__image" :src="item.url || item.localPath" mode="aspectFill" @tap="previewFeedbackImage(index)" />
      <view v-if="item.uploading" class="image-grid__mask">上传中</view>
      <view v-if="item.error" class="image-grid__mask image-grid__mask--error">失败</view>
      <view class="image-grid__remove" @tap.stop="removeFeedbackImage(index)">×</view>
    </view>
    <view v-if="images.length < 3" class="image-grid__add" @tap="chooseFeedbackImages">
      <text class="image-grid__plus">+</text>
      <text>上传图片</text>
    </view>
  </view>
</view>
```

新增样式类：`.type-grid`、`.type-grid__item`、`.type-grid__item--active`、`.image-grid`、`.image-grid__item`、`.image-grid__image`、`.image-grid__mask`、`.image-grid__remove`、`.image-grid__add`、`.image-grid__plus`，尺寸参考 `frontend/src/pages-sub/work/complain.vue` 的证据截图区域。

- [ ] **Step 10: 运行小程序局部测试**

Run:

```bash
cd frontend && CI=true pnpm exec vitest run src/api/__tests__/api-contracts.test.ts src/pages-sub/user/feedback.test.ts
```

Expected: PASS。

- [ ] **Step 11: Commit**

```bash
git add frontend/src/api/types/index.ts frontend/src/api/modules/feedback.ts frontend/src/pages-sub/user/feedback.vue frontend/src/pages-sub/user/feedback.test.ts frontend/src/api/__tests__/api-contracts.test.ts
git commit -m "feat: improve miniapp feedback form"
```

## Task 4: 管理后台接口和页面

**Files:**
- Create: `frontend-admin/src/api/modules/feedback.ts`
- Create: `frontend-admin/src/views/manage/feedback-list/index.vue`
- Modify: `frontend-admin/src/constants/menu.ts`
- Modify: `frontend-admin/src/stores/modules/permission.ts`
- Create: `frontend-admin/src/views/manage/feedback-list/feedback-list.test.ts`

- [ ] **Step 1: 写后台页面源测试**

创建 `frontend-admin/src/views/manage/feedback-list/feedback-list.test.ts`：

```ts
import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('feedback admin page source', () => {
  it('registers filters, detail preview and handle actions', () => {
    const file = fileURLToPath(new URL('./index.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toContain('getFeedbackList')
    expect(source).toContain('typeOptions')
    expect(source).toContain('意见反馈')
    expect(source).toContain('反馈图片')
    expect(source).toContain('handleFeedback')
    expect(source).toContain('el-image')
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd frontend-admin && CI=true pnpm exec vitest run src/views/manage/feedback-list/feedback-list.test.ts
```

Expected: FAIL，失败原因包含 `ENOENT` 或缺少 `index.vue`。

- [ ] **Step 3: 新增后台 API 模块**

创建 `frontend-admin/src/api/modules/feedback.ts`：

```ts
import { get, post } from '../request'
import type { ApiResult, PageQuery, PageResult } from '../types'

export interface FeedbackInfo {
  id: number
  userId?: number | string
  userNickname?: string
  type?: string
  typeText?: string
  content?: string
  contact?: string
  pics?: string[] | string
  status?: number | string
  statusText?: string
  handleReason?: string
  handledBy?: number | string
  handledAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface FeedbackListQuery extends PageQuery {
  status?: string
  type?: string
  userId?: string
}

export function getFeedbackList(params: FeedbackListQuery): Promise<ApiResult<PageResult<FeedbackInfo>>> {
  return get<PageResult<FeedbackInfo>>('/admin/feedback/list', params as Record<string, unknown>)
}

export function getFeedbackDetail(id: number): Promise<ApiResult<FeedbackInfo>> {
  return get<FeedbackInfo>('/admin/feedback/detail', { id })
}

export function handleFeedback(id: number, status: number | string, reason?: string): Promise<ApiResult<unknown>> {
  return post('/admin/feedback/handle', undefined, {
    params: { id, status, reason, handleReason: reason },
  })
}
```

- [ ] **Step 4: 新增后台页面**

创建 `frontend-admin/src/views/manage/feedback-list/index.vue`，结构复用 `work-complain-list` 的列表、详情弹窗和处理弹窗。必须包含：

```ts
const statusOptions = [
  { label: '待处理', value: '0' },
  { label: '已处理', value: '1' },
  { label: '已忽略', value: '2' },
]

const typeOptions = [
  { label: '功能异常', value: 'function' },
  { label: '体验建议', value: 'experience' },
  { label: '账号/资料', value: 'account' },
  { label: '支付/订单', value: 'payment' },
  { label: '内容/用户举报', value: 'content' },
  { label: '其他', value: 'other' },
]
```

图片解析函数：

```ts
function getImageList(row: FeedbackInfo): string[] {
  if (Array.isArray(row.pics)) {
    return row.pics.filter((item): item is string => typeof item === 'string' && Boolean(item.trim()))
  }
  if (typeof row.pics === 'string' && row.pics.trim()) {
    return row.pics.split(',').map((item) => item.trim()).filter(Boolean)
  }
  return []
}
```

处理提交：

```ts
async function submitHandle(): Promise<void> {
  if (!currentFeedback.value || submitLoading.value) return
  submitLoading.value = true
  try {
    await handleFeedback(currentFeedback.value.id, handleForm.status, handleForm.reason.trim() || undefined)
    ElMessage.success(handleForm.status === '1' ? '反馈处理成功' : '反馈已忽略')
    dialogVisible.value = false
    await fetchList()
  } finally {
    submitLoading.value = false
  }
}
```

- [ ] **Step 5: 注册菜单**

在 `frontend-admin/src/constants/menu.ts` 的审核系统 children 中，放在 `作品投诉` 后面：

```ts
{ title: '意见反馈', path: '/manage/feedback/index', component: 'manage/feedback-list/index' },
```

- [ ] **Step 6: 注册动态路由组件**

在 `frontend-admin/src/stores/modules/permission.ts` 增加导入：

```ts
import ManageFeedbackList from '@/views/manage/feedback-list/index.vue'
```

在 `viewModules` 增加：

```ts
'manage/feedback-list/index': ManageFeedbackList,
```

- [ ] **Step 7: 运行后台局部测试**

Run:

```bash
cd frontend-admin && CI=true pnpm exec vitest run src/views/manage/feedback-list/feedback-list.test.ts
```

Expected: PASS。

- [ ] **Step 8: Commit**

```bash
git add frontend-admin/src/api/modules/feedback.ts frontend-admin/src/views/manage/feedback-list/index.vue frontend-admin/src/views/manage/feedback-list/feedback-list.test.ts frontend-admin/src/constants/menu.ts frontend-admin/src/stores/modules/permission.ts
git commit -m "feat: add admin feedback management"
```

## Task 5: 端到端契约收口

**Files:**
- Review: `docs/superpowers/specs/2026-07-10-feedback-chain-upgrade-design.md`
- Review: changed files from Tasks 1-4

- [ ] **Step 1: 检查字段命名一致性**

Run:

```bash
rg -n "handleReason|handledAt|handledBy|typeText|statusText|pics|/feedback/upload/image|/admin/feedback" backend frontend frontend-admin
```

Expected: 输出覆盖 `backend/system-object`、`backend/system-domain`、`backend/system-restapi`、`backend/system-wap`、`frontend/src`、`frontend-admin/src`。

- [ ] **Step 2: 检查旧版前端未被修改**

Run:

```bash
git diff --name-only origin/main...HEAD | rg "^91pai-master/" || true
```

Expected: no output。

- [ ] **Step 3: 运行所有反馈相关局部测试**

Run:

```bash
cd backend && mvn -pl system-wap -Dtest=FeedbackControllerTest test
cd ../frontend && CI=true pnpm exec vitest run src/api/__tests__/api-contracts.test.ts src/pages-sub/user/feedback.test.ts
cd ../frontend-admin && CI=true pnpm exec vitest run src/views/manage/feedback-list/feedback-list.test.ts
```

Expected: 三组命令均 PASS。

- [ ] **Step 4: 运行格式检查**

Run:

```bash
git diff --check origin/main...HEAD
```

Expected: no output, exit code 0。

- [ ] **Step 5: 汇总最终状态**

Run:

```bash
git status --short --branch
git log --oneline --decorate -6
```

Expected: 工作区干净，最近提交包含设计文档、数据模型、后端链路、小程序页面、管理后台页面。

- [ ] **Step 6: Commit 收口文档或测试修正**

如果 Step 1-5 产生必要修正，提交：

```bash
git add backend frontend frontend-admin docs/superpowers/plans/2026-07-10-feedback-chain-upgrade.md
git commit -m "test: verify feedback chain upgrade"
```

如果没有产生修正，不创建空提交。

## Self-Review

- Spec coverage: 计划覆盖了设计文档中的小程序类型、内容、联系方式、最多 3 张图片、后端新增字段、后台列表/详情/处理、旧版前端不改、局部验证。
- Red-flag scan: 本计划没有未定项、空步骤、延后实现说明或缺少落地细节的任务。
- Type consistency: 统一使用 `type`、`pics`、`handleReason`、`handledBy`、`handledAt`、`createdAt`、`updatedAt`；前端类型使用 `FeedbackType`，后台类型使用 `FeedbackInfo`。
