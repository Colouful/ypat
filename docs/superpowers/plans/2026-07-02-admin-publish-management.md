# Admin Publish Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build backend and frontend-admin management support for user-published works and enhanced appointment review so backend services, admin management, and user-facing publish flows use consistent data.

**Architecture:** Keep works (`t_work`) and appointments (`t_ypat_info`) as separate domain models. Add backend admin-only work APIs for governance, enhance existing admin appointment APIs, and align frontend-admin enum mappings with backend enum values. Preserve user-facing public list behavior: only approved content is public.

**Tech Stack:** Java 8, Spring Boot 1.5, Spring Data JPA, Feign, Vue 3, TypeScript, Pinia, Vue Router, Element Plus, Vitest, Vite.

---

## File Structure

- Modify `backend/system-object/src/main/java/com/ypat/WorkListQo.java`: add admin query fields used by backend work management.
- Create `backend/system-object/src/main/java/com/ypat/WorkAdminListItem.java`: admin work list DTO.
- Modify `backend/system-domain/src/main/java/com/ypat/repository/WorkRepository.java`: add status update with audit reason.
- Modify `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`: add admin list/detail/audit/offline methods.
- Create `backend/system-domain/src/test/java/com/ypat/service/WorkServiceAdminSourceTest.java`: source-level regression checks for admin-only query and status behavior.
- Modify `backend/system-restapi/src/main/java/com/ypat/controller/WorkController.java`: expose `/service/work/admin/*` internal endpoints.
- Modify `backend/system-wap/src/main/java/com/ypat/service/WorkServiceClient.java`: add Feign methods for admin work endpoints.
- Create `backend/system-wap/src/main/java/com/ypat/controller/AdminWorkController.java`: public admin work endpoints for frontend-admin.
- Create `backend/system-wap/src/test/java/com/ypat/controller/AdminWorkControllerSourceTest.java`: source-level route and behavior checks.
- Modify `backend/system-object/src/main/java/com/ypat/YpatInfoQo.java`: ensure admin query fields are available as strings.
- Modify `backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java`: add appointment query predicates for target, patstyle, chargeway, workId.
- Modify `backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java`: accept enhanced appointment filters.
- Modify `frontend-admin/src/constants/enums.ts`: align appointment/work enums and helper functions.
- Create `frontend-admin/src/api/modules/work.ts`: frontend-admin work API module.
- Modify `frontend-admin/src/api/modules/ypat.ts`: add enhanced appointment fields and filters.
- Modify `frontend-admin/src/constants/menu.ts`: add work management menu and rename backend appointment publishing menu.
- Modify `frontend-admin/src/stores/modules/permission.ts`: register work management component.
- Create `frontend-admin/src/views/manage/work-list/index.vue`: work management table page.
- Create `frontend-admin/src/views/manage/work-list/WorkAuditDialog.vue`: work audit dialog.
- Create `frontend-admin/src/views/manage/work-list/WorkDetailDrawer.vue`: work detail drawer.
- Modify `frontend-admin/src/views/manage/ypat-list/index.vue`: add filters/columns/detail entry.
- Modify `frontend-admin/src/views/manage/ypat-list/AuditDialog.vue`: show enhanced appointment fields.
- Modify `frontend-admin/src/views/query/ypat-list/index.vue`: show enhanced appointment query fields.
- Modify `frontend-admin/src/views/ypat/edit/index.vue`: rename to backend appointment publishing and align select options.
- Create `frontend-admin/tests/unit/admin-enums.test.ts`: enum consistency tests.
- Create `frontend-admin/tests/unit/admin-work-api.test.ts`: API route tests.

## Task 1: Backend Work Admin Domain

**Files:**
- Modify: `backend/system-object/src/main/java/com/ypat/WorkListQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/WorkAdminListItem.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/WorkRepository.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`
- Test: `backend/system-domain/src/test/java/com/ypat/service/WorkServiceAdminSourceTest.java`

- [ ] **Step 1: Write the source regression test**

Create `backend/system-domain/src/test/java/com/ypat/service/WorkServiceAdminSourceTest.java`:

```java
package com.ypat.service;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class WorkServiceAdminSourceTest {
    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    @Test
    public void adminWorkMethodsUseAdminScopeAndDoNotReusePublicApprovalFilter() throws Exception {
        String source = read("src/main/java/com/ypat/service/WorkService.java");

        assertTrue(source.contains("public Map<String, Object> adminPageList(WorkListQo qo)"));
        assertTrue(source.contains("public Map<String, Object> adminDetail(Long workId)"));
        assertTrue(source.contains("public void adminAudit(Long workId, String flag, String reason)"));
        assertTrue(source.contains("public void adminOffline(Long workId, String reason)"));
        assertTrue(source.contains("WorkStatus.isValid(flag)"));
        assertTrue(source.contains("workRepository.updateStatusAndAuditReason(workId, flag, reason)"));
        assertTrue(source.contains("workRepository.updateStatusAndAuditReason(workId, WorkStatus.xj.value, reason)"));
    }

    @Test
    public void repositorySupportsAuditReasonStatusUpdate() throws Exception {
        String source = read("src/main/java/com/ypat/repository/WorkRepository.java");

        assertTrue(source.contains("int updateStatusAndAuditReason"));
        assertTrue(source.contains("auditReason = :auditReason"));
        assertTrue(source.contains("deletedFlag = 0"));
    }
}
```

- [ ] **Step 2: Run the failing domain source test**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=WorkServiceAdminSourceTest
```

Expected: fail because `WorkServiceAdminSourceTest` and the checked methods do not exist yet.

- [ ] **Step 3: Add admin fields to `WorkListQo`**

Modify `backend/system-object/src/main/java/com/ypat/WorkListQo.java` so it contains these fields and accessors:

```java
private String status;
private String nickname;
private String mobile;
private String mediaType;

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

public String getNickname() {
    return nickname;
}

public void setNickname(String nickname) {
    this.nickname = nickname;
}

public String getMobile() {
    return mobile;
}

public void setMobile(String mobile) {
    this.mobile = mobile;
}

public String getMediaType() {
    return mediaType;
}

public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
}
```

Keep existing `category`、`city`、`gender`、`profession`、`tagIds`、`viewerUserId` fields unchanged.

- [ ] **Step 4: Create admin list DTO**

Create `backend/system-object/src/main/java/com/ypat/WorkAdminListItem.java`:

```java
package com.ypat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class WorkAdminListItem implements Serializable {
    private Long id;
    private String description;
    private String coverUrl;
    private String mediaType;
    private String mediaTypeTxt;
    private String status;
    private String statusTxt;
    private String auditReason;
    private Integer readCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Date publishTime;
    private Long userId;
    private String nickname;
    private String mobile;
    private String gender;
    private String profession;
    private String city;
    private String area;
    private List<String> tags;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getMediaTypeTxt() { return mediaTypeTxt; }
    public void setMediaTypeTxt(String mediaTypeTxt) { this.mediaTypeTxt = mediaTypeTxt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusTxt() { return statusTxt; }
    public void setStatusTxt(String statusTxt) { this.statusTxt = statusTxt; }
    public String getAuditReason() { return auditReason; }
    public void setAuditReason(String auditReason) { this.auditReason = auditReason; }
    public Integer getReadCount() { return readCount; }
    public void setReadCount(Integer readCount) { this.readCount = readCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
```

- [ ] **Step 5: Add status update with audit reason**

Modify `backend/system-domain/src/main/java/com/ypat/repository/WorkRepository.java` by adding this method below `updateStatus`:

```java
@Modifying
@Query("update Work w set w.status = :status, w.auditReason = :auditReason, w.updatedAt = CURRENT_TIMESTAMP where w.id = :id and w.deletedFlag = 0")
int updateStatusAndAuditReason(@Param("id") Long id, @Param("status") String status, @Param("auditReason") String auditReason);
```

- [ ] **Step 6: Add admin work methods to `WorkService`**

Modify `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`:

```java
import com.ypat.WorkAdminListItem;
```

Add these methods before `myWorks`:

```java
public Map<String, Object> adminPageList(WorkListQo qo) {
    if (qo == null) qo = new WorkListQo();
    final int page = qo.getPage() == null || qo.getPage() < 1 ? 1 : qo.getPage();
    final int size = qo.getSize() == null || qo.getSize() < 1 ? 10 : Math.min(qo.getSize(), 50);
    final String status = qo.getStatus();
    final String city = qo.getCity();
    final String mediaType = qo.getMediaType();
    final String nickname = qo.getNickname();
    final String mobile = qo.getMobile();
    final String tagIds = qo.getTagIds();

    Specification<Work> spec = (root, query, cb) -> {
        List<Predicate> ps = new ArrayList<>();
        ps.add(cb.equal(root.get("deletedFlag"), 0));
        if (StringUtils.isNotBlank(status)) ps.add(cb.equal(root.get("status"), status));
        if (StringUtils.isNotBlank(city)) ps.add(cb.equal(root.get("city"), city));
        if (StringUtils.isNotBlank(mediaType)) ps.add(cb.equal(root.get("mediaType"), mediaType));
        if (StringUtils.isNotBlank(nickname) || StringUtils.isNotBlank(mobile)) {
            Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
            if (StringUtils.isNotBlank(nickname)) ps.add(cb.like(userJoin.get("nickname"), "%" + nickname + "%"));
            if (StringUtils.isNotBlank(mobile)) ps.add(cb.equal(userJoin.get("mobile"), mobile));
        }
        return cb.and(ps.toArray(new Predicate[0]));
    };

    Page<Work> result = workRepository.findAll(spec,
        new PageRequest(page - 1, size, new Sort(new Sort.Order(Sort.Direction.DESC, "publishTime"))));
    List<WorkAdminListItem> items = new ArrayList<>();
    for (Work work : result.getContent()) {
        WorkAdminListItem item = toAdminListItem(work, parseTagIds(tagIds));
        if (item != null) items.add(item);
    }
    Map<String, Object> res = new HashMap<>();
    res.put("content", items);
    res.put("totalElements", result.getTotalElements());
    res.put("totalPages", result.getTotalPages());
    return res;
}

public Map<String, Object> adminDetail(Long workId) {
    if (workId == null) throw new SysException(ResponseCode.FAIL_PARA);
    Work work = workRepository.findOne(workId);
    if (work == null || work.getDeletedFlag() != null && work.getDeletedFlag() == 1) {
        throw new SysException(ResponseCode.FAIL_WORK_NOT_FOUND);
    }
    Map<String, Object> detail = new HashMap<>();
    detail.put("id", work.getId());
    detail.put("description", work.getDescription());
    detail.put("device", work.getDevice());
    detail.put("shootLocation", work.getShootLocation());
    detail.put("returnPhotoFlag", work.getReturnPhotoFlag());
    detail.put("mediaType", work.getMediaType());
    detail.put("mediaTypeTxt", "2".equals(work.getMediaType()) ? "视频" : "图片");
    detail.put("status", work.getStatus());
    detail.put("statusTxt", WorkStatus.getNameByCode(work.getStatus()));
    detail.put("auditReason", work.getAuditReason());
    detail.put("readCount", work.getReadCount());
    detail.put("likeCount", work.getLikeCount());
    detail.put("favoriteCount", work.getFavoriteCount());
    detail.put("publishTime", work.getPublishTime());
    detail.put("city", work.getCity());
    detail.put("area", work.getArea());
    detail.put("medias", workMediaRepository.findByWorkIdOrderBySortNoAsc(workId));
    detail.put("tags", loadWorkTagNames(workId, Collections.emptyList()));
    User user = userRepository.findById(work.getUserid());
    if (user != null) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("nickname", user.getNickname());
        userMap.put("mobile", user.getMobile());
        userMap.put("gender", user.getGender());
        userMap.put("profession", user.getProfess());
        userMap.put("city", user.getCity());
        userMap.put("area", user.getArea());
        detail.put("user", userMap);
    }
    return detail;
}

public void adminAudit(Long workId, String flag, String reason) {
    if (workId == null || StringUtils.isBlank(flag) || !WorkStatus.isValid(flag)) {
        throw new SysException(ResponseCode.FAIL_PARA);
    }
    if (!WorkStatus.shtg.value.equals(flag) && !WorkStatus.shbtg.value.equals(flag)) {
        throw new SysException(ResponseCode.FAIL_PARA);
    }
    workRepository.updateStatusAndAuditReason(workId, flag, reason);
}

public void adminOffline(Long workId, String reason) {
    if (workId == null) throw new SysException(ResponseCode.FAIL_PARA);
    workRepository.updateStatusAndAuditReason(workId, WorkStatus.xj.value, reason);
}
```

Add these private helpers near other private methods:

```java
private WorkAdminListItem toAdminListItem(Work work, List<Long> filterTagIds) {
    List<String> tags = loadWorkTagNames(work.getId(), filterTagIds);
    if (!filterTagIds.isEmpty() && tags.isEmpty()) return null;
    WorkAdminListItem item = new WorkAdminListItem();
    item.setId(work.getId());
    item.setDescription(work.getDescription());
    item.setMediaType(work.getMediaType());
    item.setMediaTypeTxt("2".equals(work.getMediaType()) ? "视频" : "图片");
    item.setStatus(work.getStatus());
    item.setStatusTxt(WorkStatus.getNameByCode(work.getStatus()));
    item.setAuditReason(work.getAuditReason());
    item.setReadCount(work.getReadCount());
    item.setLikeCount(work.getLikeCount());
    item.setFavoriteCount(work.getFavoriteCount());
    item.setPublishTime(work.getPublishTime());
    List<WorkMedia> medias = workMediaRepository.findByWorkIdOrderBySortNoAsc(work.getId());
    if (!medias.isEmpty()) item.setCoverUrl(medias.get(0).getUrl());
    User user = userRepository.findById(work.getUserid());
    if (user != null) {
        item.setUserId(user.getId());
        item.setNickname(user.getNickname());
        item.setMobile(user.getMobile());
        item.setGender(user.getGender());
        item.setProfession(user.getProfess());
        item.setCity(user.getCity());
        item.setArea(user.getArea());
    }
    item.setTags(tags);
    return item;
}

private List<Long> parseTagIds(String tagIds) {
    List<Long> ids = new ArrayList<>();
    if (StringUtils.isBlank(tagIds)) return ids;
    for (String raw : tagIds.split(",")) {
        try { ids.add(Long.parseLong(raw.trim())); } catch (NumberFormatException ignored) {}
    }
    return ids;
}

private List<String> loadWorkTagNames(Long workId, List<Long> filterTagIds) {
    List<String> names = new ArrayList<>();
    for (WorkTagRel rel : workTagRelRepository.findByWorkId(workId)) {
        if (!filterTagIds.isEmpty() && !filterTagIds.contains(rel.getTagId())) continue;
        WorkTag tag = workTagRepository.findOne(rel.getTagId());
        if (tag != null) names.add(tag.getName());
    }
    return names;
}
```

- [ ] **Step 7: Run domain test**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=WorkServiceAdminSourceTest
```

Expected: pass.

- [ ] **Step 8: Commit backend domain changes**

```bash
git add backend/system-object/src/main/java/com/ypat/WorkListQo.java \
  backend/system-object/src/main/java/com/ypat/WorkAdminListItem.java \
  backend/system-domain/src/main/java/com/ypat/repository/WorkRepository.java \
  backend/system-domain/src/main/java/com/ypat/service/WorkService.java \
  backend/system-domain/src/test/java/com/ypat/service/WorkServiceAdminSourceTest.java
git commit -m "feat: add admin work domain support" -m "Add admin scoped work listing, detail, audit, and offline domain capabilities without changing public work list behavior." -m "Constraint: Public work APIs keep approved-only semantics; admin APIs can inspect all non-deleted statuses." -m "Tested: mvn test -Dtest=WorkServiceAdminSourceTest in backend/system-domain." -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

## Task 2: Backend Admin Work API

**Files:**
- Modify: `backend/system-restapi/src/main/java/com/ypat/controller/WorkController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/service/WorkServiceClient.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/AdminWorkController.java`
- Test: `backend/system-wap/src/test/java/com/ypat/controller/AdminWorkControllerSourceTest.java`

- [ ] **Step 1: Write route source test**

Create `backend/system-wap/src/test/java/com/ypat/controller/AdminWorkControllerSourceTest.java`:

```java
package com.ypat.controller;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class AdminWorkControllerSourceTest {
    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    @Test
    public void adminWorkControllerExposesManagementRoutes() throws Exception {
        String source = read("src/main/java/com/ypat/controller/AdminWorkController.java");

        assertTrue(source.contains("@RequestMapping(\"/admin/work\")"));
        assertTrue(source.contains("@GetMapping(\"/list\")"));
        assertTrue(source.contains("@GetMapping(\"/detail\")"));
        assertTrue(source.contains("@PostMapping(\"/audit\")"));
        assertTrue(source.contains("@PostMapping(\"/offline\")"));
        assertTrue(source.contains("workServiceClient.adminList"));
        assertTrue(source.contains("workServiceClient.adminDetail"));
        assertTrue(source.contains("workServiceClient.adminAudit"));
        assertTrue(source.contains("workServiceClient.adminOffline"));
    }
}
```

- [ ] **Step 2: Run failing route source test**

Run:

```bash
cd backend/system-wap
mvn test -Dtest=AdminWorkControllerSourceTest
```

Expected: fail because `AdminWorkController.java` does not exist.

- [ ] **Step 3: Add internal restapi endpoints**

Modify `backend/system-restapi/src/main/java/com/ypat/controller/WorkController.java` by adding:

```java
@PostMapping("/admin/list")
public ResponseApiBody adminList(@RequestBody WorkListQo qo) {
    Map<String, Object> page = workService.adminPageList(qo);
    return ResponseApiBody.success(page);
}

@GetMapping("/admin/detail")
public ResponseApiBody adminDetail(@RequestParam("id") Long id) {
    return ResponseApiBody.success(workService.adminDetail(id));
}

@PostMapping("/admin/audit")
public ResponseApiBody adminAudit(@RequestParam("id") Long id,
                                  @RequestParam("flag") String flag,
                                  @RequestParam(value = "reason", required = false) String reason) {
    workService.adminAudit(id, flag, reason);
    return ResponseApiBody.success("审核完成");
}

@PostMapping("/admin/offline")
public ResponseApiBody adminOffline(@RequestParam("id") Long id,
                                    @RequestParam(value = "reason", required = false) String reason) {
    workService.adminOffline(id, reason);
    return ResponseApiBody.success("已下架");
}
```

- [ ] **Step 4: Add Feign client methods**

Modify `backend/system-wap/src/main/java/com/ypat/service/WorkServiceClient.java`:

```java
@PostMapping("/service/work/admin/list")
String adminList(@RequestBody WorkListQo qo);

@GetMapping("/service/work/admin/detail")
String adminDetail(@RequestParam("id") Long id);

@PostMapping("/service/work/admin/audit")
String adminAudit(@RequestParam("id") Long id,
                  @RequestParam("flag") String flag,
                  @RequestParam("reason") String reason);

@PostMapping("/service/work/admin/offline")
String adminOffline(@RequestParam("id") Long id,
                    @RequestParam("reason") String reason);
```

- [ ] **Step 5: Create `AdminWorkController`**

Create `backend/system-wap/src/main/java/com/ypat/controller/AdminWorkController.java`:

```java
package com.ypat.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.WorkListQo;
import com.ypat.service.WorkServiceClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/work")
public class AdminWorkController {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    @Autowired
    private WorkServiceClient workServiceClient;

    @GetMapping("/list")
    public ResponseApiBody list(WorkListQo qo) {
        if (qo == null) qo = new WorkListQo();
        Integer page = qo.getPage();
        Integer size = qo.getSize();
        qo.setPage(page == null || page < 0 ? DEFAULT_PAGE + 1 : page + 1);
        qo.setSize(size == null || size <= 0 ? DEFAULT_SIZE : size);
        String json = workServiceClient.adminList(qo);
        JsonElement data = JsonParser.parseString(json);
        return ResponseApiBody.success(data.getAsJsonObject().has("res") ? data.getAsJsonObject().get("res") : data);
    }

    @GetMapping("/detail")
    public ResponseApiBody detail(@RequestParam("id") Long id) {
        if (id == null) throw new SysException(ResponseCode.FAIL_PARA);
        String json = workServiceClient.adminDetail(id);
        JsonElement data = JsonParser.parseString(json);
        return ResponseApiBody.success(data.getAsJsonObject().has("res") ? data.getAsJsonObject().get("res") : data);
    }

    @PostMapping("/audit")
    public ResponseApiBody audit(@RequestParam("id") Long id,
                                 @RequestParam("flag") String flag,
                                 @RequestParam(value = "reason", required = false) String reason) {
        if (id == null || StringUtils.isBlank(flag)) throw new SysException(ResponseCode.FAIL_PARA);
        String json = workServiceClient.adminAudit(id, flag, reason);
        JsonElement data = JsonParser.parseString(json);
        return ResponseApiBody.success(data);
    }

    @PostMapping("/offline")
    public ResponseApiBody offline(@RequestParam("id") Long id,
                                   @RequestParam(value = "reason", required = false) String reason) {
        if (id == null) throw new SysException(ResponseCode.FAIL_PARA);
        String json = workServiceClient.adminOffline(id, reason);
        JsonElement data = JsonParser.parseString(json);
        return ResponseApiBody.success(data);
    }
}
```

- [ ] **Step 6: Run wap route test**

Run:

```bash
cd backend/system-wap
mvn test -Dtest=AdminWorkControllerSourceTest
```

Expected: pass.

- [ ] **Step 7: Commit backend API changes**

```bash
git add backend/system-restapi/src/main/java/com/ypat/controller/WorkController.java \
  backend/system-wap/src/main/java/com/ypat/service/WorkServiceClient.java \
  backend/system-wap/src/main/java/com/ypat/controller/AdminWorkController.java \
  backend/system-wap/src/test/java/com/ypat/controller/AdminWorkControllerSourceTest.java
git commit -m "feat: expose admin work api" -m "Expose admin work list, detail, audit, and offline routes for the separated Vue admin frontend." -m "Constraint: Routes are admin scoped and do not change user-facing /work routes." -m "Tested: mvn test -Dtest=AdminWorkControllerSourceTest in backend/system-wap." -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

## Task 3: Backend Appointment Admin Filters

**Files:**
- Modify: `backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java`
- Test: `backend/system-domain/src/test/java/com/ypat/service/YpatInfoAdminFilterSourceTest.java`

- [ ] **Step 1: Write filter source test**

Create `backend/system-domain/src/test/java/com/ypat/service/YpatInfoAdminFilterSourceTest.java`:

```java
package com.ypat.service;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class YpatInfoAdminFilterSourceTest {
    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    @Test
    public void ypatAdminFiltersIncludeNewPublishFields() throws Exception {
        String source = read("src/main/java/com/ypat/service/YpatInfoService.java");

        assertTrue(source.contains("queryQo.getTarget()"));
        assertTrue(source.contains("root.get(\"target\")"));
        assertTrue(source.contains("queryQo.getPatstyle()"));
        assertTrue(source.contains("root.get(\"patstyle\")"));
        assertTrue(source.contains("queryQo.getChargeway()"));
        assertTrue(source.contains("root.get(\"chargeway\")"));
        assertTrue(source.contains("queryQo.getWorkId()"));
        assertTrue(source.contains("root.get(\"workId\")"));
    }
}
```

- [ ] **Step 2: Run failing appointment filter test**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=YpatInfoAdminFilterSourceTest
```

Expected: fail until predicates are added.

- [ ] **Step 3: Add appointment query predicates**

Modify `backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java` inside `findPageByPredicate` after existing city/status predicates:

```java
if(CommonUtils.isNotNull(queryQo.getTarget())){
    predicatesList.add(criteriaBuilder.equal(root.get("target"), queryQo.getTarget()));
}
if(CommonUtils.isNotNull(queryQo.getPatstyle())){
    predicatesList.add(criteriaBuilder.like(root.get("patstyle"), "%" + queryQo.getPatstyle() + "%"));
}
if(CommonUtils.isNotNull(queryQo.getChargeway())){
    predicatesList.add(criteriaBuilder.equal(root.get("chargeway"), queryQo.getChargeway()));
}
if(CommonUtils.isNotNull(queryQo.getWorkId())){
    predicatesList.add(criteriaBuilder.equal(root.get("workId"), Long.valueOf(queryQo.getWorkId())));
}
```

- [ ] **Step 4: Accept enhanced admin request parameters**

Modify `backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java` list method signature:

```java
@RequestParam(value = "target", required = false) String target,
@RequestParam(value = "patstyle", required = false) String patstyle,
@RequestParam(value = "chargeway", required = false) String chargeway,
@RequestParam(value = "city", required = false) String city,
@RequestParam(value = "workId", required = false) String workId,
```

Set values on `YpatInfoQo`:

```java
if (StringUtils.isNotBlank(target)) {
    qo.setTarget(target);
}
if (StringUtils.isNotBlank(patstyle)) {
    qo.setPatstyle(patstyle);
}
if (StringUtils.isNotBlank(chargeway)) {
    qo.setChargeway(chargeway);
}
if (StringUtils.isNotBlank(city)) {
    qo.setCity(city);
}
if (StringUtils.isNotBlank(workId)) {
    qo.setWorkId(workId);
}
```

- [ ] **Step 5: Run appointment filter tests**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=YpatInfoAdminFilterSourceTest
```

Expected: pass.

- [ ] **Step 6: Commit appointment backend changes**

```bash
git add backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java \
  backend/system-domain/src/test/java/com/ypat/service/YpatInfoAdminFilterSourceTest.java \
  backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java
git commit -m "feat: enhance admin appointment filters" -m "Allow admin appointment management to filter by target, style, charge mode, city, and source work." -m "Constraint: Appointment review continues to update only status, recommendation, and reason." -m "Tested: mvn test -Dtest=YpatInfoAdminFilterSourceTest in backend/system-domain." -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

## Task 4: Frontend Admin Enums and API Modules

**Files:**
- Modify: `frontend-admin/src/constants/enums.ts`
- Create: `frontend-admin/src/api/modules/work.ts`
- Modify: `frontend-admin/src/api/modules/ypat.ts`
- Test: `frontend-admin/tests/unit/admin-enums.test.ts`
- Test: `frontend-admin/tests/unit/admin-work-api.test.ts`

- [ ] **Step 1: Write enum tests**

Create `frontend-admin/tests/unit/admin-enums.test.ts`:

```ts
import { describe, expect, it } from 'vitest'
import {
  YpatTarget,
  YpatPatstyle,
  YpatChargeWay,
  WorkStatus,
  getYpatTargetOptions,
  getYpatPatstyleOptions,
  getYpatChargeWayOptions,
  getWorkStatusOptions,
} from '@/constants/enums'

describe('后台发布枚举一致性', () => {
  it('约拍对象值应对齐后端 6 类枚举', () => {
    expect(YpatTarget.PHOTOGRAPHER.value).toBe('0')
    expect(YpatTarget.MODEL.value).toBe('1')
    expect(YpatTarget.VIDEOGRAPHER.value).toBe('2')
    expect(YpatTarget.MERCHANT.value).toBe('3')
    expect(YpatTarget.MAKEUP.value).toBe('4')
    expect(YpatTarget.RETOUCHER.value).toBe('5')
    expect(getYpatTargetOptions().map((o) => o.value)).toEqual(['0', '1', '2', '3', '4', '5'])
  })

  it('约拍风格应对齐新版用户端风格', () => {
    expect(getYpatPatstyleOptions().map((o) => o.label)).toEqual([
      '复古', 'INS', '胶片', '少女', '暗黑', '情绪', '夜景', '欧美', '商务', '韩系', '日系', '情侣', '样片',
    ])
    expect(YpatPatstyle.INS.value).toBe('1')
  })

  it('收费方式和作品状态应对齐后端', () => {
    expect(getYpatChargeWayOptions().map((o) => o.value)).toEqual(['0', '1', '2', '3'])
    expect(YpatChargeWay.FREE.value).toBe('0')
    expect(getWorkStatusOptions().map((o) => o.value)).toEqual(['1', '2', '3', '4'])
    expect(WorkStatus.OFFLINE.value).toBe('4')
  })
})
```

- [ ] **Step 2: Write work API route tests**

Create `frontend-admin/tests/unit/admin-work-api.test.ts`:

```ts
import { describe, expect, it, vi } from 'vitest'

vi.mock('@/api/request', () => ({
  get: vi.fn((url: string, params?: Record<string, unknown>) => Promise.resolve({ url, params })),
  post: vi.fn((url: string, data?: unknown, config?: unknown) => Promise.resolve({ url, data, config })),
}))

describe('后台作品 API', () => {
  it('应使用 admin work 路由', async () => {
    const api = await import('@/api/modules/work')

    await expect(api.getWorkList({ page: 0, size: 10, status: '1' }) as unknown as Promise<unknown>).resolves.toMatchObject({ url: '/admin/work/list' })
    await expect(api.getWorkDetail(12) as unknown as Promise<unknown>).resolves.toMatchObject({ url: '/admin/work/detail', params: { id: 12 } })
    await expect(api.auditWork(12, '2', 'ok') as unknown as Promise<unknown>).resolves.toMatchObject({ url: '/admin/work/audit' })
    await expect(api.offlineWork(12, '违规') as unknown as Promise<unknown>).resolves.toMatchObject({ url: '/admin/work/offline' })
  })
})
```

- [ ] **Step 3: Run failing frontend tests**

Run:

```bash
cd frontend-admin
pnpm test -- tests/unit/admin-enums.test.ts tests/unit/admin-work-api.test.ts
```

Expected: fail until enum/API module changes are implemented.

- [ ] **Step 4: Replace backend-aligned enum definitions**

Modify the appointment and work sections in `frontend-admin/src/constants/enums.ts`:

```ts
export const YpatTarget = {
  PHOTOGRAPHER: { value: '0', name: '约摄影师' },
  MODEL: { value: '1', name: '约模特' },
  VIDEOGRAPHER: { value: '2', name: '约摄像师' },
  MERCHANT: { value: '3', name: '约商家' },
  MAKEUP: { value: '4', name: '约化妆师' },
  RETOUCHER: { value: '5', name: '约修图师' },
} as const
export const getYpatTargetOptions = () => Object.values(YpatTarget).map((o) => ({ label: o.name, value: o.value }))

export const YpatPatstyle = {
  RETRO: { value: '0', name: '复古' },
  INS: { value: '1', name: 'INS' },
  FILM: { value: '2', name: '胶片' },
  GIRL: { value: '3', name: '少女' },
  DARK: { value: '4', name: '暗黑' },
  MOOD: { value: '5', name: '情绪' },
  NIGHT: { value: '6', name: '夜景' },
  EURO: { value: '7', name: '欧美' },
  BUSINESS: { value: '8', name: '商务' },
  KOREAN: { value: '9', name: '韩系' },
  JAPANESE: { value: '10', name: '日系' },
  COUPLE: { value: '11', name: '情侣' },
  SAMPLE: { value: '12', name: '样片' },
} as const
export const getYpatPatstyleOptions = () => Object.values(YpatPatstyle).map((o) => ({ label: o.name, value: o.value }))

export const YpatChargeWay = {
  FREE: { value: '0', name: '免费互拍' },
  CHARGE: { value: '1', name: '收费拍摄' },
  CAN_PAY: { value: '2', name: '可付费' },
  NEGOTIATE: { value: '3', name: '费用面议' },
} as const
export const getYpatChargeWayOptions = () => Object.values(YpatChargeWay).map((o) => ({ label: o.name, value: o.value }))

export const WorkStatus = {
  DRAFT: { value: '0', name: '暂存', type: 'info' as const },
  PENDING: { value: '1', name: '待审核', type: 'warning' as const },
  APPROVED: { value: '2', name: '审核通过', type: 'success' as const },
  REJECTED: { value: '3', name: '审核未通过', type: 'danger' as const },
  OFFLINE: { value: '4', name: '已下架', type: 'info' as const },
} as const
export const getWorkStatusOptions = () => [
  { label: WorkStatus.PENDING.name, value: WorkStatus.PENDING.value },
  { label: WorkStatus.APPROVED.name, value: WorkStatus.APPROVED.value },
  { label: WorkStatus.REJECTED.name, value: WorkStatus.REJECTED.value },
  { label: WorkStatus.OFFLINE.name, value: WorkStatus.OFFLINE.value },
]
```

- [ ] **Step 5: Add frontend-admin work API module**

Create `frontend-admin/src/api/modules/work.ts`:

```ts
import { get, post } from '../request'
import type { ApiResult, PageQuery, PageResult } from '../types'

export interface WorkAdminInfo {
  id: number
  description: string
  coverUrl?: string
  mediaType: string
  mediaTypeTxt?: string
  status: string
  statusTxt?: string
  auditReason?: string
  readCount?: number
  likeCount?: number
  favoriteCount?: number
  publishTime?: string
  userId?: number
  nickname?: string
  mobile?: string
  gender?: string
  profession?: string
  city?: string
  area?: string
  tags?: string[]
  medias?: Array<{ id: number; type: string; url: string }>
  user?: Record<string, unknown>
}

export interface WorkListQuery extends PageQuery {
  status?: string
  nickname?: string
  mobile?: string
  city?: string
  mediaType?: string
  tagIds?: string
}

export function getWorkList(params: WorkListQuery): Promise<ApiResult<PageResult<WorkAdminInfo>>> {
  return get<PageResult<WorkAdminInfo>>('/admin/work/list', params as Record<string, unknown>)
}

export function getWorkDetail(id: number): Promise<ApiResult<WorkAdminInfo>> {
  return get<WorkAdminInfo>('/admin/work/detail', { id })
}

export function auditWork(id: number, flag: string, reason?: string): Promise<ApiResult<unknown>> {
  return post('/admin/work/audit', undefined, { params: { id, flag, reason } })
}

export function offlineWork(id: number, reason?: string): Promise<ApiResult<unknown>> {
  return post('/admin/work/offline', undefined, { params: { id, reason } })
}
```

- [ ] **Step 6: Enhance appointment API types**

Modify `frontend-admin/src/api/modules/ypat.ts`:

```ts
export interface YpatInfo {
  id: number
  describ: string
  nickname: string
  mobile: string
  gender: string
  genderTxt: string
  profess: string
  professTxt: string
  target: string
  targetTxt: string
  patstyle?: string
  patstyleTxt?: string
  chargeway?: string
  chargewayTxt?: string
  chargeamt?: number
  province?: string
  city: string
  area?: string
  isNationwide?: string
  workId?: string
  patdate?: string
  patarea?: string
  patslice?: string
  creditflag?: string
  realnameflag?: string
  pubdate: string
  status: string
  statusTxt: string
  recomflag: string
  reason: string
  pics: string[]
  userQo?: {
    id: number
    nickname: string
    openid: string
  }
}

export interface YpatListQuery extends PageQuery {
  status?: string
  nickname?: string
  mobile?: string
  city?: string
  recomflag?: string
  target?: string
  patstyle?: string
  chargeway?: string
  workId?: string
}
```

- [ ] **Step 7: Run frontend enum/API tests**

Run:

```bash
cd frontend-admin
pnpm test -- tests/unit/admin-enums.test.ts tests/unit/admin-work-api.test.ts
```

Expected: pass.

- [ ] **Step 8: Commit frontend enum/API changes**

```bash
git add frontend-admin/src/constants/enums.ts \
  frontend-admin/src/api/modules/work.ts \
  frontend-admin/src/api/modules/ypat.ts \
  frontend-admin/tests/unit/admin-enums.test.ts \
  frontend-admin/tests/unit/admin-work-api.test.ts
git commit -m "feat: align admin publish enums and api" -m "Align frontend-admin publish enums with backend values and add admin work API bindings." -m "Constraint: Values follow backend enums; display labels may match newer user-facing wording." -m "Tested: pnpm test -- tests/unit/admin-enums.test.ts tests/unit/admin-work-api.test.ts in frontend-admin." -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

## Task 5: Frontend Admin Work Management Page

**Files:**
- Modify: `frontend-admin/src/constants/menu.ts`
- Modify: `frontend-admin/src/stores/modules/permission.ts`
- Create: `frontend-admin/src/views/manage/work-list/index.vue`
- Create: `frontend-admin/src/views/manage/work-list/WorkAuditDialog.vue`
- Create: `frontend-admin/src/views/manage/work-list/WorkDetailDrawer.vue`

- [ ] **Step 1: Add menu route**

Modify `frontend-admin/src/constants/menu.ts` in the “审核系统” group:

```ts
{ title: '作品管理', path: '/manage/work/index', component: 'manage/work-list/index' },
{ title: '申请列表', path: '/manage/ypat-list', component: 'manage/ypat-list/index' },
```

Change:

```ts
{ title: '发布作品', path: '/ypat/edit', component: 'ypat/edit/index' },
```

to:

```ts
{ title: '后台代发约拍', path: '/ypat/edit', component: 'ypat/edit/index' },
```

- [ ] **Step 2: Register route component**

Modify `frontend-admin/src/stores/modules/permission.ts`:

```ts
import ManageWorkList from '@/views/manage/work-list/index.vue'
```

Add to `viewModules`:

```ts
'manage/work-list/index': ManageWorkList,
```

- [ ] **Step 3: Create work audit dialog**

Create `frontend-admin/src/views/manage/work-list/WorkAuditDialog.vue`:

```vue
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { auditWork, type WorkAdminInfo } from '@/api/modules/work'
import { AuditFlag } from '@/constants/enums'

const props = defineProps<{ visible: boolean; data: WorkAdminInfo | null }>()
const emit = defineEmits(['update:visible', 'success'])
const localVisible = computed({ get: () => props.visible, set: (v) => emit('update:visible', v) })
const reason = ref('')
const loading = ref(false)

watch(() => props.visible, (value) => {
  if (value) reason.value = ''
})

async function handleAudit(flag: string) {
  if (!props.data) return
  loading.value = true
  try {
    await auditWork(props.data.id, flag, reason.value)
    ElMessage.success(flag === AuditFlag.PASS ? '审核通过' : '审核不通过')
    emit('success')
    localVisible.value = false
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-dialog v-model="localVisible" title="作品审核" width="640px">
    <div v-if="data" class="work-audit">
      <p><strong>ID：</strong>{{ data.id }}</p>
      <p><strong>作者：</strong>{{ data.nickname || '-' }}</p>
      <p><strong>描述：</strong>{{ data.description }}</p>
      <el-form label-width="90px">
        <el-form-item label="审核理由">
          <el-input v-model="reason" type="textarea" :rows="3" placeholder="审核不通过或下架时填写" />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="localVisible = false">取消</el-button>
      <el-button type="danger" :loading="loading" @click="handleAudit(AuditFlag.REJECT)">不通过</el-button>
      <el-button type="primary" :loading="loading" @click="handleAudit(AuditFlag.PASS)">通过</el-button>
    </template>
  </el-dialog>
</template>
```

- [ ] **Step 4: Create work detail drawer**

Create `frontend-admin/src/views/manage/work-list/WorkDetailDrawer.vue`:

```vue
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getWorkDetail, type WorkAdminInfo } from '@/api/modules/work'

const props = defineProps<{ visible: boolean; id?: number }>()
const emit = defineEmits(['update:visible'])
const localVisible = computed({ get: () => props.visible, set: (v) => emit('update:visible', v) })
const detail = ref<WorkAdminInfo | null>(null)
const loading = ref(false)

watch(() => [props.visible, props.id] as const, async ([visible, id]) => {
  if (!visible || !id) return
  loading.value = true
  try {
    const res = await getWorkDetail(id)
    detail.value = res.data
  } finally {
    loading.value = false
  }
}, { immediate: true })
</script>

<template>
  <el-drawer v-model="localVisible" title="作品详情" size="640px">
    <div v-loading="loading">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusTxt || detail.status }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ detail.nickname || detail.user?.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="城市">{{ detail.city || '-' }}</el-descriptions-item>
          <el-descriptions-item label="媒体类型">{{ detail.mediaTypeTxt || detail.mediaType }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ detail.publishTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="阅读">{{ detail.readCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="点赞/收藏">{{ detail.likeCount ?? 0 }} / {{ detail.favoriteCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="审核理由" :span="2">{{ detail.auditReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detail.description }}</el-descriptions-item>
        </el-descriptions>
        <div class="media-grid">
          <el-image v-for="media in detail.medias || []" :key="media.id" :src="media.url" fit="cover" class="media-item" />
        </div>
      </template>
    </div>
  </el-drawer>
</template>

<style scoped lang="scss">
.media-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 16px;
}
.media-item {
  width: 100%;
  height: 120px;
  border-radius: 6px;
}
</style>
```

- [ ] **Step 5: Create work list page**

Create `frontend-admin/src/views/manage/work-list/index.vue` using the existing table/page patterns:

```vue
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/common/StatusTag.vue'
import WorkAuditDialog from './WorkAuditDialog.vue'
import WorkDetailDrawer from './WorkDetailDrawer.vue'
import { getWorkList, offlineWork, type WorkAdminInfo, type WorkListQuery } from '@/api/modules/work'
import { getWorkStatusOptions, WorkStatus } from '@/constants/enums'

const query = reactive<WorkListQuery>({ status: '', nickname: '', mobile: '', city: '', mediaType: '', page: 0, size: 10 })
const list = ref<WorkAdminInfo[]>([])
const total = ref(0)
const loading = ref(false)
const auditVisible = ref(false)
const detailVisible = ref(false)
const current = ref<WorkAdminInfo | null>(null)
const currentPage = computed(() => (query.page ?? 0) + 1)

async function fetchList() {
  loading.value = true
  try {
    const res = await getWorkList(query)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}
function search() { query.page = 0; fetchList() }
function reset() { query.status = ''; query.nickname = ''; query.mobile = ''; query.city = ''; query.mediaType = ''; query.page = 0; fetchList() }
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
function openAudit(row: WorkAdminInfo) { current.value = row; auditVisible.value = true }
function openDetail(row: WorkAdminInfo) { current.value = row; detailVisible.value = true }
async function doOffline(row: WorkAdminInfo) {
  await ElMessageBox.confirm('确定要下架该作品吗？', '提示', { type: 'warning' })
  await offlineWork(row.id, '后台下架')
  ElMessage.success('下架成功')
  fetchList()
}
onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部">
            <el-option v-for="o in getWorkStatusOptions()" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="昵称"><el-input v-model="query.nickname" placeholder="请输入昵称" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="query.mobile" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="query.city" placeholder="请输入城市" /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column label="封面" width="90" align="center">
        <template #default="{ row }"><el-image v-if="row.coverUrl" :src="row.coverUrl" fit="cover" style="width:56px;height:56px;border-radius:4px" /></template>
      </el-table-column>
      <el-table-column prop="description" label="作品描述" min-width="220" show-overflow-tooltip />
      <el-table-column prop="nickname" label="作者" min-width="120" />
      <el-table-column prop="mobile" label="手机号" min-width="120" />
      <el-table-column prop="city" label="城市" width="110" />
      <el-table-column prop="mediaTypeTxt" label="媒体" width="90" align="center" />
      <el-table-column label="状态" width="120" align="center"><template #default="{ row }"><StatusTag :status="row.status" type="ypat" /></template></el-table-column>
      <el-table-column prop="publishTime" label="发布时间" min-width="160" />
      <el-table-column label="操作" width="190" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
          <el-button type="success" link size="small" @click="openAudit(row)">审核</el-button>
          <el-button v-if="row.status === WorkStatus.APPROVED.value" type="danger" link size="small" @click="doOffline(row)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper"><el-pagination :current-page="currentPage" :page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next,jumper" background @current-change="pageChange" @size-change="sizeChange" /></div>
    <WorkAuditDialog v-model:visible="auditVisible" :data="current" @success="fetchList" />
    <WorkDetailDrawer v-model:visible="detailVisible" :id="current?.id" />
  </div>
</template>

<style scoped lang="scss">
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}
</style>
```

- [ ] **Step 6: Run frontend typecheck**

Run:

```bash
cd frontend-admin
pnpm type-check
```

Expected: pass.

- [ ] **Step 7: Commit work page**

```bash
git add frontend-admin/src/constants/menu.ts \
  frontend-admin/src/stores/modules/permission.ts \
  frontend-admin/src/views/manage/work-list/index.vue \
  frontend-admin/src/views/manage/work-list/WorkAuditDialog.vue \
  frontend-admin/src/views/manage/work-list/WorkDetailDrawer.vue
git commit -m "feat: add admin work management page" -m "Add the frontend-admin work management route, table, detail drawer, and audit dialog." -m "Constraint: Follow existing Element Plus admin table patterns without restructuring unrelated menus." -m "Tested: pnpm type-check in frontend-admin." -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

## Task 6: Frontend Appointment Admin Enhancements

**Files:**
- Modify: `frontend-admin/src/views/manage/ypat-list/index.vue`
- Modify: `frontend-admin/src/views/manage/ypat-list/AuditDialog.vue`
- Modify: `frontend-admin/src/views/query/ypat-list/index.vue`
- Modify: `frontend-admin/src/views/ypat/edit/index.vue`

- [ ] **Step 1: Enhance manage appointment filters and columns**

Modify `frontend-admin/src/views/manage/ypat-list/index.vue` imports:

```ts
import { getYpatStatusOptions, getRecomOptions, getYpatTargetOptions, getYpatPatstyleOptions, getYpatChargeWayOptions, RecomFlag } from '@/constants/enums'
```

Initialize query:

```ts
const query = reactive<YpatListQuery>({ status: '', nickname: '', mobile: '', recomflag: '', target: '', patstyle: '', chargeway: '', city: '', workId: '', page: 0, size: 10 })
```

Update `reset`:

```ts
function reset() {
  query.status = ''
  query.nickname = ''
  query.mobile = ''
  query.recomflag = ''
  query.target = ''
  query.patstyle = ''
  query.chargeway = ''
  query.city = ''
  query.workId = ''
  query.page = 0
  fetchList()
}
```

Add form fields:

```vue
<el-form-item label="约拍对象"><el-select v-model="query.target" clearable placeholder="全部"><el-option v-for="o in getYpatTargetOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
<el-form-item label="风格"><el-select v-model="query.patstyle" clearable placeholder="全部"><el-option v-for="o in getYpatPatstyleOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
<el-form-item label="收费"><el-select v-model="query.chargeway" clearable placeholder="全部"><el-option v-for="o in getYpatChargeWayOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
<el-form-item label="城市"><el-input v-model="query.city" placeholder="请输入城市"/></el-form-item>
<el-form-item label="来源作品"><el-input v-model="query.workId" placeholder="作品ID"/></el-form-item>
```

Add table columns:

```vue
<el-table-column prop="targetTxt" label="约拍对象" min-width="120"/>
<el-table-column prop="patstyleTxt" label="风格" min-width="140" show-overflow-tooltip/>
<el-table-column prop="chargewayTxt" label="收费方式" min-width="120"/>
<el-table-column label="地区" min-width="150"><template #default="{row}">{{ row.isNationwide === '1' ? '全国' : [row.province, row.city, row.area].filter(Boolean).join(' / ') }}</template></el-table-column>
<el-table-column prop="workId" label="来源作品" width="100" align="center"/>
```

- [ ] **Step 2: Enhance appointment audit dialog**

Modify `frontend-admin/src/views/manage/ypat-list/AuditDialog.vue` body:

```vue
<p><strong>ID：</strong>{{ data.id }}</p>
<p><strong>昵称：</strong>{{ data.nickname || data.userQo?.nickname }}</p>
<p><strong>约拍对象：</strong>{{ data.targetTxt || data.target }}</p>
<p><strong>风格：</strong>{{ data.patstyleTxt || '-' }}</p>
<p><strong>收费方式：</strong>{{ data.chargewayTxt || data.chargeway || '-' }}</p>
<p><strong>地区：</strong>{{ data.isNationwide === '1' ? '全国' : [data.province, data.city, data.area].filter(Boolean).join(' / ') }}</p>
<p><strong>来源作品：</strong>{{ data.workId || '-' }}</p>
<p><strong>描述：</strong>{{ data.describ }}</p>
```

Add image preview after description:

```vue
<div v-if="data.pics && data.pics.length" class="ypat-pics">
  <el-image v-for="pic in data.pics" :key="pic" :src="pic" fit="cover" class="ypat-pic" />
</div>
```

Add scoped style:

```scss
.ypat-pics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin: 12px 0;
}
.ypat-pic {
  width: 100%;
  height: 88px;
  border-radius: 4px;
}
```

- [ ] **Step 3: Enhance query appointment list**

Modify `frontend-admin/src/views/query/ypat-list/index.vue` to use enhanced columns:

```vue
<el-table-column prop="targetTxt" label="约拍对象" min-width="120"/>
<el-table-column prop="patstyleTxt" label="风格" min-width="140" show-overflow-tooltip/>
<el-table-column prop="chargewayTxt" label="收费方式" min-width="120"/>
<el-table-column label="地区" min-width="150"><template #default="{row}">{{ row.isNationwide === '1' ? '全国' : [row.province, row.city, row.area].filter(Boolean).join(' / ') }}</template></el-table-column>
<el-table-column prop="workId" label="来源作品" width="100" align="center"/>
```

- [ ] **Step 4: Rename and align backend appointment publishing page**

Modify `frontend-admin/src/views/ypat/edit/index.vue`:

Change header:

```vue
<template #header>后台代发约拍</template>
```

Change submit success:

```ts
ElMessage.success('代发约拍成功')
```

Keep submit target:

```ts
router.push('/manage/ypat-list')
```

- [ ] **Step 5: Run frontend tests and typecheck**

Run:

```bash
cd frontend-admin
pnpm test -- tests/unit/admin-enums.test.ts tests/unit/admin-work-api.test.ts
pnpm type-check
```

Expected: pass.

- [ ] **Step 6: Commit appointment frontend changes**

```bash
git add frontend-admin/src/views/manage/ypat-list/index.vue \
  frontend-admin/src/views/manage/ypat-list/AuditDialog.vue \
  frontend-admin/src/views/query/ypat-list/index.vue \
  frontend-admin/src/views/ypat/edit/index.vue
git commit -m "feat: enhance admin appointment management" -m "Expose newer appointment fields in admin review and query pages and clarify backend appointment publishing wording." -m "Constraint: Existing appointment review actions remain status/reason/recommendation only." -m "Tested: pnpm test -- tests/unit/admin-enums.test.ts tests/unit/admin-work-api.test.ts && pnpm type-check in frontend-admin." -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

## Task 7: Final Verification

**Files:**
- Inspect all changed files.
- No new feature files unless verification exposes defects.

- [ ] **Step 1: Run backend targeted tests**

Run:

```bash
cd backend/system-domain
mvn test -Dtest=WorkServiceAdminSourceTest,YpatInfoAdminFilterSourceTest
cd ../system-wap
mvn test -Dtest=AdminWorkControllerSourceTest
```

Expected: all pass.

- [ ] **Step 2: Run frontend tests**

Run:

```bash
cd frontend-admin
pnpm test -- tests/unit/admin-enums.test.ts tests/unit/admin-work-api.test.ts
pnpm type-check
pnpm build
```

Expected: all pass.

- [ ] **Step 3: Run static consistency checks**

Run:

```bash
rg -n "PHOTOGRAPHER: \\{ value: '0'|VIDEOGRAPHER: \\{ value: '2'|RETOUCHER: \\{ value: '5'" frontend-admin/src/constants/enums.ts
rg -n "约摄影师|约模特|约摄像师|约商家|约化妆师|约修图师" backend/system-object/src/main/java/com/ypat/enums/YpatTarget.java frontend/src/constants/enums.ts frontend-admin/src/constants/enums.ts
rg -n "后台代发约拍|作品管理" frontend-admin/src/constants/menu.ts frontend-admin/src/views/ypat/edit/index.vue
rg -n "/admin/work|adminPageList|adminAudit|adminOffline" backend/system-wap/src/main/java backend/system-restapi/src/main/java backend/system-domain/src/main/java frontend-admin/src
```

Expected: each command prints matching lines in the expected backend, user frontend, and admin frontend files.

- [ ] **Step 4: Review git status and diff**

Run:

```bash
git status --short
git log --oneline --decorate -8
```

Expected: only `.superpowers/` remains untracked from visual brainstorming, or no untracked files if it has been cleaned; feature commits are present on `codex/admin-publish-management`.

- [ ] **Step 5: Invoke finishing skill**

Use `superpowers:finishing-a-development-branch` to decide final integration, rerun required checks, fix failures, and summarize evidence.
