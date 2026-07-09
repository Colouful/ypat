# Invite Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete configurable invitation loop across the new mini-program frontend, admin frontend, and backend.

**Architecture:** Add a single global invite config in the backend, expose it through public and admin APIs, and make invite reward issuance depend on successful relation creation. The admin app manages config and records; the mini-program reads the config to render the invite activity page and invite landing page.

**Tech Stack:** Spring Cloud Java backend, Spring Data JPA, JUnit 4 tests, Vue 3 + TypeScript + Element Plus admin frontend, UniApp Vue 3 mini-program frontend, Vitest.

---

## File Structure

Backend object/domain:

- Create `backend/system-object/src/main/java/com/ypat/InviteConfigQo.java`: DTO for admin config and public rule fields.
- Modify `backend/system-object/src/main/java/com/ypat/InviteSummaryQo.java`: add `enabled`, `ruleText`, `shareTitle`, `landingTitle`.
- Modify `backend/system-object/src/main/java/com/ypat/InviteRelationQo.java`: add optional inviter/invitee snapshot fields if needed by admin list.
- Create `backend/system-domain/src/main/java/com/ypat/entity/InviteConfig.java`: JPA entity for `t_invite_config`.
- Create `backend/system-domain/src/main/java/com/ypat/repository/InviteConfigRepository.java`: repository for the single global invite config.
- Modify `backend/system-domain/src/main/java/com/ypat/service/InviteService.java`: config defaults, config save, public rule, admin records, relation-created result.
- Modify `backend/system-domain/src/main/java/com/ypat/service/UserService.java`: issue invite reward only after relation creation succeeds.
- Create `backend/system-domain/src/test/java/com/ypat/service/InviteServiceConfigTest.java`: default config and validation tests.
- Create or modify `backend/system-domain/src/test/java/com/ypat/service/InviteServiceRewardTest.java`: idempotent relation and reward-precondition tests.

Backend APIs:

- Modify `backend/system-restapi/src/main/java/com/ypat/controller/InviteController.java`: add config and admin records service endpoints.
- Modify `backend/system-wap/src/main/java/com/ypat/service/InviteServiceClient.java`: add Feign methods for config and admin records.
- Modify `backend/system-wap/src/main/java/com/ypat/controller/InviteController.java`: return configured rule fields.
- Create `backend/system-wap/src/main/java/com/ypat/controller/AdminInviteController.java`: admin config and records endpoints.
- Modify `backend/system-wap/src/test/java/com/ypat/controller/InviteControllerTest.java`: update public rule assertions.
- Create `backend/system-wap/src/test/java/com/ypat/controller/AdminInviteControllerTest.java`: admin endpoint forwarding tests.

Database script:

- Create `backend/dev/mysql/20260709_create_invite_config.sql`: DDL for `t_invite_config`.

Admin frontend:

- Create `frontend-admin/src/api/modules/invite.ts`: admin invite API functions and types.
- Modify `frontend-admin/src/api/types.ts`: shared invite types if local module types are not enough.
- Modify `frontend-admin/src/constants/menu.ts`: add “邀请运营” menu group.
- Modify `frontend-admin/src/stores/modules/permission.ts`: import and map invite pages.
- Modify `frontend-admin/src/stores/modules/__tests__/permission.test.ts`: assert invite routes are real pages.
- Create `frontend-admin/src/views/invite/config/index.vue`: invite config form.
- Create `frontend-admin/src/views/invite/records/index.vue`: invite records table.

Mini-program frontend:

- Modify `frontend/src/api/types/index.ts`: add `enabled`, `shareTitle`, `landingTitle` to invite types.
- Modify `frontend/src/api/modules/invite.ts`: keep existing functions, no path change required.
- Modify `frontend/src/pages-sub/user/invite.vue`: upgrade activity layout and use rule config.
- Modify `frontend/src/pages-sub/content/invite-landing.vue`: upgrade landing page and preserve invite context.
- Modify `frontend/src/pages-sub/user/invite-records.vue`: align empty state/reward text.
- Add or modify source-level tests under `frontend/src/pages-sub/user` and `frontend/src/pages-sub/content` if existing test style supports it.

## Task 1: Backend Invite Config Model

**Files:**
- Create: `backend/system-object/src/main/java/com/ypat/InviteConfigQo.java`
- Modify: `backend/system-object/src/main/java/com/ypat/InviteSummaryQo.java`
- Create: `backend/system-domain/src/main/java/com/ypat/entity/InviteConfig.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/InviteConfigRepository.java`
- Create: `backend/dev/mysql/20260709_create_invite_config.sql`
- Test: `backend/system-domain/src/test/java/com/ypat/service/InviteServiceConfigTest.java`

- [ ] **Step 1: Write the DTO**

Create `backend/system-object/src/main/java/com/ypat/InviteConfigQo.java`:

```java
package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

public class InviteConfigQo implements Serializable {
    private Long id;
    private String enabled;
    private Integer rewardPpd;
    private String rewardUnit;
    private String ruleText;
    private String shareTitle;
    private String landingTitle;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
    public String getRewardUnit() { return rewardUnit; }
    public void setRewardUnit(String rewardUnit) { this.rewardUnit = rewardUnit; }
    public String getRuleText() { return ruleText; }
    public void setRuleText(String ruleText) { this.ruleText = ruleText; }
    public String getShareTitle() { return shareTitle; }
    public void setShareTitle(String shareTitle) { this.shareTitle = shareTitle; }
    public String getLandingTitle() { return landingTitle; }
    public void setLandingTitle(String landingTitle) { this.landingTitle = landingTitle; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 2: Extend invite summary**

Modify `backend/system-object/src/main/java/com/ypat/InviteSummaryQo.java` by adding these fields and accessors:

```java
    private String enabled;
    private String ruleText;
    private String shareTitle;
    private String landingTitle;

    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public String getRuleText() { return ruleText; }
    public void setRuleText(String ruleText) { this.ruleText = ruleText; }
    public String getShareTitle() { return shareTitle; }
    public void setShareTitle(String shareTitle) { this.shareTitle = shareTitle; }
    public String getLandingTitle() { return landingTitle; }
    public void setLandingTitle(String landingTitle) { this.landingTitle = landingTitle; }
```

- [ ] **Step 3: Create the entity**

Create `backend/system-domain/src/main/java/com/ypat/entity/InviteConfig.java`:

```java
package com.ypat.entity;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "t_invite_config")
public class InviteConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enabled", length = 1, nullable = false)
    private String enabled;

    @Column(name = "reward_ppd", nullable = false)
    private Integer rewardPpd;

    @Column(name = "rule_text", length = 300, nullable = false)
    private String ruleText;

    @Column(name = "share_title", length = 120, nullable = false)
    private String shareTitle;

    @Column(name = "landing_title", length = 160, nullable = false)
    private String landingTitle;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
    public String getRuleText() { return ruleText; }
    public void setRuleText(String ruleText) { this.ruleText = ruleText; }
    public String getShareTitle() { return shareTitle; }
    public void setShareTitle(String shareTitle) { this.shareTitle = shareTitle; }
    public String getLandingTitle() { return landingTitle; }
    public void setLandingTitle(String landingTitle) { this.landingTitle = landingTitle; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 4: Create the repository**

Create `backend/system-domain/src/main/java/com/ypat/repository/InviteConfigRepository.java`:

```java
package com.ypat.repository;

import com.ypat.entity.InviteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteConfigRepository extends JpaRepository<InviteConfig, Long> {
}
```

- [ ] **Step 5: Add the DDL script**

Create `backend/dev/mysql/20260709_create_invite_config.sql`:

```sql
CREATE TABLE IF NOT EXISTS `t_invite_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `enabled` varchar(1) NOT NULL DEFAULT '1',
  `reward_ppd` int(11) NOT NULL DEFAULT 3,
  `rule_text` varchar(300) NOT NULL DEFAULT '好友通过你的邀请码注册后，自动到账 3 拍拍豆。',
  `share_title` varchar(120) NOT NULL DEFAULT '好友邀请你加入爱去拍，找摄影师、找模特更方便',
  `landing_title` varchar(160) NOT NULL DEFAULT '我正在使用爱去拍，找摄影师、找模特特别方便，推荐你也来体验。',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 6: Commit**

```bash
git add backend/system-object/src/main/java/com/ypat/InviteConfigQo.java \
  backend/system-object/src/main/java/com/ypat/InviteSummaryQo.java \
  backend/system-domain/src/main/java/com/ypat/entity/InviteConfig.java \
  backend/system-domain/src/main/java/com/ypat/repository/InviteConfigRepository.java \
  backend/dev/mysql/20260709_create_invite_config.sql
git commit -m "feat: add invite config model" \
  -m "Introduce a single global invite configuration contract shared by public invite rules and admin operations." \
  -m "Constraint: Data model only; no runtime behavior changed in this commit." \
  -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

## Task 2: Backend Config Service and Public Rule

**Files:**
- Modify: `backend/system-domain/src/main/java/com/ypat/service/InviteService.java`
- Modify: `backend/system-restapi/src/main/java/com/ypat/controller/InviteController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/service/InviteServiceClient.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/InviteController.java`
- Test: `backend/system-domain/src/test/java/com/ypat/service/InviteServiceConfigTest.java`
- Test: `backend/system-wap/src/test/java/com/ypat/controller/InviteControllerTest.java`

- [ ] **Step 1: Add config defaults and save methods to `InviteService`**

In `InviteService`, inject `InviteConfigRepository` and add these constants and methods:

```java
    public static final String INVITE_ENABLED = "1";
    public static final String INVITE_DISABLED = "0";
    private static final String REWARD_UNIT = "拍拍豆";
    private static final String DEFAULT_RULE_TEXT = "好友通过你的邀请码注册后，自动到账 3 拍拍豆。";
    private static final String DEFAULT_SHARE_TITLE = "好友邀请你加入爱去拍，找摄影师、找模特更方便";
    private static final String DEFAULT_LANDING_TITLE = "我正在使用爱去拍，找摄影师、找模特特别方便，推荐你也来体验。";

    @Autowired
    private InviteConfigRepository inviteConfigRepository;

    public InviteConfigQo getConfig() {
        InviteConfig config = loadConfigEntity();
        InviteConfigQo qo = new InviteConfigQo();
        qo.setId(config.getId());
        qo.setEnabled(config.getEnabled());
        qo.setRewardPpd(config.getRewardPpd());
        qo.setRewardUnit(REWARD_UNIT);
        qo.setRuleText(config.getRuleText());
        qo.setShareTitle(config.getShareTitle());
        qo.setLandingTitle(config.getLandingTitle());
        qo.setCreatedAt(config.getCreatedAt());
        qo.setUpdatedAt(config.getUpdatedAt());
        return qo;
    }

    public InviteConfigQo saveConfig(InviteConfigQo qo) {
        validateConfig(qo);
        Date now = new Date();
        InviteConfig config = loadConfigEntity();
        if (config.getCreatedAt() == null) config.setCreatedAt(now);
        config.setUpdatedAt(now);
        config.setEnabled(qo.getEnabled());
        config.setRewardPpd(qo.getRewardPpd());
        config.setRuleText(qo.getRuleText().trim());
        config.setShareTitle(qo.getShareTitle().trim());
        config.setLandingTitle(qo.getLandingTitle().trim());
        inviteConfigRepository.save(config);
        return getConfig();
    }

    private InviteConfig loadConfigEntity() {
        List<InviteConfig> configs = inviteConfigRepository.findAll();
        if (!configs.isEmpty()) return fillDefaultConfig(configs.get(0));
        return fillDefaultConfig(new InviteConfig());
    }

    private InviteConfig fillDefaultConfig(InviteConfig config) {
        if (StringUtils.isEmpty(config.getEnabled())) config.setEnabled(INVITE_ENABLED);
        if (config.getRewardPpd() == null) config.setRewardPpd(Constant.INVITE_NEED_PPD);
        if (StringUtils.isEmpty(config.getRuleText())) config.setRuleText(DEFAULT_RULE_TEXT);
        if (StringUtils.isEmpty(config.getShareTitle())) config.setShareTitle(DEFAULT_SHARE_TITLE);
        if (StringUtils.isEmpty(config.getLandingTitle())) config.setLandingTitle(DEFAULT_LANDING_TITLE);
        return config;
    }

    private void validateConfig(InviteConfigQo qo) {
        if (qo == null) throw new SysException(ResponseCode.FAIL_PARA);
        if (!INVITE_ENABLED.equals(qo.getEnabled()) && !INVITE_DISABLED.equals(qo.getEnabled())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (qo.getRewardPpd() == null || qo.getRewardPpd() < 0 || qo.getRewardPpd() > 999) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (StringUtils.isEmpty(qo.getRuleText()) || qo.getRuleText().trim().length() > 300) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (StringUtils.isEmpty(qo.getShareTitle()) || qo.getShareTitle().trim().length() > 120) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (StringUtils.isEmpty(qo.getLandingTitle()) || qo.getLandingTitle().trim().length() > 160) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
    }
```

Also import `InviteConfigQo`, `InviteConfig`, `InviteConfigRepository`, `ResponseCode`, and `SysException`.

- [ ] **Step 2: Make summary use config**

In `InviteService.getSummary`, replace direct `Constant.INVITE_NEED_PPD` use with config:

```java
        InviteConfigQo config = getConfig();
        int rewardPpd = config.getRewardPpd() == null ? Constant.INVITE_NEED_PPD : config.getRewardPpd();
        summary.setInviteCode(InviteCodeCodec.encode(userId));
        summary.setRewardPpd(rewardPpd);
        summary.setEnabled(config.getEnabled());
        summary.setRuleText(config.getRuleText());
        summary.setShareTitle(config.getShareTitle());
        summary.setLandingTitle(config.getLandingTitle());
```

Set `totalReward` to `(int) count * rewardPpd`.

- [ ] **Step 3: Add restapi service endpoints**

Modify `backend/system-restapi/src/main/java/com/ypat/controller/InviteController.java`:

```java
    @GetMapping("/service/invite/config")
    public InviteConfigQo config() {
        return inviteService.getConfig();
    }

    @PostMapping("/service/invite/config/save")
    public InviteConfigQo saveConfig(@RequestBody InviteConfigQo qo) {
        return inviteService.saveConfig(qo);
    }

    @PostMapping("/service/invite/admin/findPage")
    public Map<String, Object> adminFindPage(@RequestBody InviteRelationQo qo) {
        return inviteService.findPage(qo);
    }
```

- [ ] **Step 4: Add Feign methods**

Modify `backend/system-wap/src/main/java/com/ypat/service/InviteServiceClient.java`:

```java
    @GetMapping("/service/invite/config")
    InviteConfigQo config();

    @PostMapping("/service/invite/config/save")
    InviteConfigQo saveConfig(@RequestBody InviteConfigQo qo);

    @PostMapping("/service/invite/admin/findPage")
    Map<String, Object> adminFindPage(@RequestBody InviteRelationQo qo);
```

- [ ] **Step 5: Update public rule controller**

Modify `backend/system-wap/src/main/java/com/ypat/controller/InviteController.java` `rule()`:

```java
    @GetMapping(value = {"/invite/rule"})
    public Map<String, Object> rule() {
        InviteConfigQo config = inviteServiceClient.config();
        Map<String, Object> body = new HashMap<>();
        body.put("enabled", config.getEnabled());
        body.put("rewardPpd", config.getRewardPpd());
        body.put("rewardUnit", config.getRewardUnit() == null ? "拍拍豆" : config.getRewardUnit());
        body.put("ruleText", config.getRuleText());
        body.put("shareTitle", config.getShareTitle());
        body.put("landingTitle", config.getLandingTitle());
        return body;
    }
```

- [ ] **Step 6: Update tests**

In `InviteControllerTest.ruleAlwaysReturnsRewardPpd`, assert the new fields:

```java
        assertEquals("1", rule.get("enabled"));
        assertEquals(3, rule.get("rewardPpd"));
        assertEquals("拍拍豆", rule.get("rewardUnit"));
        assertEquals("好友邀请你加入爱去拍，找摄影师、找模特更方便", rule.get("shareTitle"));
        assertNotNull(rule.get("landingTitle"));
```

Update the fake client to implement `config`, `saveConfig`, and `adminFindPage`.

- [ ] **Step 7: Run focused backend tests**

Run:

```bash
mvn -pl backend/system-wap -Dtest=InviteControllerTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 8: Commit**

```bash
git add backend/system-domain/src/main/java/com/ypat/service/InviteService.java \
  backend/system-restapi/src/main/java/com/ypat/controller/InviteController.java \
  backend/system-wap/src/main/java/com/ypat/service/InviteServiceClient.java \
  backend/system-wap/src/main/java/com/ypat/controller/InviteController.java \
  backend/system-wap/src/test/java/com/ypat/controller/InviteControllerTest.java
git commit -m "feat: serve configurable invite rules" \
  -m "Route invite rule data through backend configuration so public pages and admin operations share one source of truth." \
  -m "Tested: mvn -pl backend/system-wap -Dtest=InviteControllerTest test" \
  -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

## Task 3: Backend Admin Invite APIs

**Files:**
- Create: `backend/system-wap/src/main/java/com/ypat/controller/AdminInviteController.java`
- Test: `backend/system-wap/src/test/java/com/ypat/controller/AdminInviteControllerTest.java`

- [ ] **Step 1: Create admin controller**

Create `backend/system-wap/src/main/java/com/ypat/controller/AdminInviteController.java`:

```java
package com.ypat.controller;

import com.ypat.InviteConfigQo;
import com.ypat.InviteRelationQo;
import com.ypat.InviteSummaryQo;
import com.ypat.ResponseApiBody;
import com.ypat.service.InviteServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/invite")
public class AdminInviteController {

    @Autowired
    private InviteServiceClient inviteServiceClient;

    @GetMapping("/config")
    public ResponseApiBody config() {
        return ResponseApiBody.success(inviteServiceClient.config());
    }

    @PutMapping("/config")
    public ResponseApiBody saveConfig(@RequestBody InviteConfigQo qo) {
        return ResponseApiBody.success(inviteServiceClient.saveConfig(qo));
    }

    @GetMapping("/records")
    public ResponseApiBody records(InviteRelationQo qo) {
        return ResponseApiBody.success(inviteServiceClient.adminFindPage(qo));
    }
}
```

- [ ] **Step 2: Create admin controller test**

Create `backend/system-wap/src/test/java/com/ypat/controller/AdminInviteControllerTest.java`:

```java
package com.ypat.controller;

import com.ypat.InviteConfigQo;
import com.ypat.InviteRelationQo;
import com.ypat.ResponseApiBody;
import com.ypat.service.InviteServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AdminInviteControllerTest {
    private AdminInviteController controller;
    private FakeInviteServiceClient client;

    @Before
    public void setUp() {
        controller = new AdminInviteController();
        client = new FakeInviteServiceClient();
        ReflectionTestUtils.setField(controller, "inviteServiceClient", client);
    }

    @Test
    public void configWrapsFeignResult() {
        ResponseApiBody body = controller.config();
        assertNotNull(body);
        assertEquals("1", ((InviteConfigQo) body.getRes()).getEnabled());
    }

    @Test
    public void saveConfigForwardsBody() {
        InviteConfigQo qo = new InviteConfigQo();
        qo.setEnabled("0");
        qo.setRewardPpd(5);
        qo.setRuleText("规则");
        qo.setShareTitle("分享");
        qo.setLandingTitle("落地页");
        controller.saveConfig(qo);
        assertEquals("0", client.savedConfig.getEnabled());
        assertEquals(Integer.valueOf(5), client.savedConfig.getRewardPpd());
    }

    @Test
    public void recordsForwardsFilters() {
        InviteRelationQo qo = new InviteRelationQo();
        qo.setInviterUserid(12L);
        qo.setInviteCode("IVC");
        controller.records(qo);
        assertEquals(Long.valueOf(12L), client.lastRecordsQo.getInviterUserid());
        assertEquals("IVC", client.lastRecordsQo.getInviteCode());
    }

    private static class FakeInviteServiceClient implements InviteServiceClient {
        InviteConfigQo savedConfig;
        InviteRelationQo lastRecordsQo;

        public InviteSummaryQo summary(Long userid) { return null; }

        public Map<String, Object> findPage(InviteRelationQo qo) {
            this.lastRecordsQo = qo;
            return new HashMap<String, Object>();
        }

        public InviteConfigQo config() {
            InviteConfigQo qo = new InviteConfigQo();
            qo.setEnabled("1");
            qo.setRewardPpd(3);
            return qo;
        }

        public InviteConfigQo saveConfig(InviteConfigQo qo) {
            this.savedConfig = qo;
            return qo;
        }

        public Map<String, Object> adminFindPage(InviteRelationQo qo) {
            this.lastRecordsQo = qo;
            return new HashMap<String, Object>();
        }
    }
}
```

If the existing `ResponseApiBody` accessor is named differently, inspect `backend/system-object/src/main/java/com/ypat/ResponseApiBody.java` and use the real getter.

- [ ] **Step 3: Run focused test**

Run:

```bash
mvn -pl backend/system-wap -Dtest=AdminInviteControllerTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add backend/system-wap/src/main/java/com/ypat/controller/AdminInviteController.java \
  backend/system-wap/src/test/java/com/ypat/controller/AdminInviteControllerTest.java
git commit -m "feat: add admin invite endpoints" \
  -m "Expose invite configuration and relation records through the admin gateway for the rebuilt management UI." \
  -m "Tested: mvn -pl backend/system-wap -Dtest=AdminInviteControllerTest test" \
  -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

## Task 4: Backend Reward Idempotency

**Files:**
- Modify: `backend/system-domain/src/main/java/com/ypat/service/InviteService.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/UserService.java`
- Test: `backend/system-domain/src/test/java/com/ypat/service/InviteServiceRewardTest.java`

- [ ] **Step 1: Change relation binding to return creation result**

In `InviteService`, add a result type:

```java
    public static class BindRelationResult {
        private final boolean created;
        private final InviteRelation relation;

        public BindRelationResult(boolean created, InviteRelation relation) {
            this.created = created;
            this.relation = relation;
        }

        public boolean isCreated() { return created; }
        public InviteRelation getRelation() { return relation; }
    }
```

Add a new method:

```java
    public BindRelationResult bindRelationIfAbsent(Long inviterUserid, Long inviteeUserid, String inviteCode, String source, Integer rewardPpd) {
        if (inviterUserid == null || inviteeUserid == null) return new BindRelationResult(false, null);
        if (inviterUserid.equals(inviteeUserid)) {
            logger.warn("invite.bind.self_reject inviter={} invitee={}", inviterUserid, inviteeUserid);
            return new BindRelationResult(false, null);
        }
        InviteRelation existing = inviteRelationRepository.findByInviteeUserid(inviteeUserid);
        if (existing != null) return new BindRelationResult(false, existing);

        InviteRelation relation = new InviteRelation();
        relation.setInviterUserid(inviterUserid);
        relation.setInviteeUserid(inviteeUserid);
        relation.setInviteCode(inviteCode);
        relation.setSource(StringUtils.isEmpty(source) ? "register" : source);
        relation.setRewardPpd(rewardPpd);
        relation.setCredate(new Date());
        try {
            return new BindRelationResult(true, inviteRelationRepository.save(relation));
        } catch (DataIntegrityViolationException ex) {
            logger.warn("invite.bind.race_skip inviter={} invitee={}", inviterUserid, inviteeUserid);
            return new BindRelationResult(false, inviteRelationRepository.findByInviteeUserid(inviteeUserid));
        }
    }
```

Keep the old `bindRelation` method as a delegating compatibility wrapper:

```java
    public void bindRelation(Long inviterUserid, Long inviteeUserid, String inviteCode, String source) {
        bindRelationIfAbsent(inviterUserid, inviteeUserid, inviteCode, source, Constant.INVITE_NEED_PPD);
    }
```

- [ ] **Step 2: Move reward after relation creation**

In `UserService.save`, replace the current reward block with:

```java
            if(inviterId != null){
                InviteConfigQo inviteConfig = inviteService.getConfig();
                Integer rewardPpd = inviteConfig.getRewardPpd() == null ? Constant.INVITE_NEED_PPD : inviteConfig.getRewardPpd();
                boolean inviteEnabled = InviteService.INVITE_ENABLED.equals(inviteConfig.getEnabled());
                if(inviteEnabled){
                    InviteService.BindRelationResult bindResult =
                            inviteService.bindRelationIfAbsent(inviterId, user.getId(), inviteCodeUsed, inviteSource, rewardPpd);
                    if(bindResult.isCreated() && rewardPpd > 0){
                        User recUser = get(inviterId);
                        recUser.setPpd(recUser.getPpd() + rewardPpd);
                        userRepository.save(recUser);
                        Record record = new Record();
                        record.setCredate(new Date());
                        record.setPpd(rewardPpd);
                        record.setUserid(recUser.getId());
                        record.setType(RecordType.FRI.value);
                        recordRepository.save(record);
                    }
                }
            }
```

Import `InviteConfigQo` if needed.

- [ ] **Step 3: Add idempotency unit test**

Create `backend/system-domain/src/test/java/com/ypat/service/InviteServiceRewardTest.java` with a focused test for `bindRelationIfAbsent`:

```java
package com.ypat.service;

import com.ypat.entity.InviteRelation;
import com.ypat.repository.InviteRelationRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InviteServiceRewardTest {
    private InviteService inviteService;
    private InviteRelationRepository relationRepository;

    @Before
    public void setUp() {
        inviteService = new InviteService();
        relationRepository = relationRepository();
        ReflectionTestUtils.setField(inviteService, "inviteRelationRepository", relationRepository);
    }

    @Test
    public void bindRelationOnlyCreatesFirstRelationForInvitee() {
        InviteService.BindRelationResult first =
                inviteService.bindRelationIfAbsent(1L, 2L, "IV1", "share", 3);
        InviteService.BindRelationResult second =
                inviteService.bindRelationIfAbsent(1L, 2L, "IV1", "share", 3);

        assertTrue(first.isCreated());
        assertFalse(second.isCreated());
    }

    @Test
    public void bindRelationRejectsSelfInvite() {
        InviteService.BindRelationResult result =
                inviteService.bindRelationIfAbsent(2L, 2L, "IV2", "share", 3);

        assertFalse(result.isCreated());
    }

    private static InviteRelationRepository relationRepository() {
        List<InviteRelation> records = new ArrayList<InviteRelation>();
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findByInviteeUserid".equals(method.getName())) {
                    Long inviteeUserid = (Long) args[0];
                    for (InviteRelation item : records) {
                        if (inviteeUserid.equals(item.getInviteeUserid())) return item;
                    }
                    return null;
                }
                if ("save".equals(method.getName())) {
                    InviteRelation relation = (InviteRelation) args[0];
                    records.add(relation);
                    return relation;
                }
                if ("countByInviterUserid".equals(method.getName())) return 0L;
                throw new UnsupportedOperationException(method.getName());
            }
        };
        return (InviteRelationRepository) Proxy.newProxyInstance(
                InviteRelationRepository.class.getClassLoader(),
                new Class[]{InviteRelationRepository.class},
                handler
        );
    }
}
```

- [ ] **Step 4: Run focused test**

Run:

```bash
mvn -pl backend/system-domain -Dtest=InviteServiceRewardTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add backend/system-domain/src/main/java/com/ypat/service/InviteService.java \
  backend/system-domain/src/main/java/com/ypat/service/UserService.java \
  backend/system-domain/src/test/java/com/ypat/service/InviteServiceRewardTest.java
git commit -m "fix: make invite rewards relation-bound" \
  -m "Award invite beans only after a new relation is created so repeated or concurrent invite handling cannot duplicate rewards." \
  -m "Tested: mvn -pl backend/system-domain -Dtest=InviteServiceRewardTest test" \
  -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

## Task 5: Admin Frontend Invite Module

**Files:**
- Create: `frontend-admin/src/api/modules/invite.ts`
- Modify: `frontend-admin/src/constants/menu.ts`
- Modify: `frontend-admin/src/stores/modules/permission.ts`
- Modify: `frontend-admin/src/stores/modules/__tests__/permission.test.ts`
- Create: `frontend-admin/src/views/invite/config/index.vue`
- Create: `frontend-admin/src/views/invite/records/index.vue`

- [ ] **Step 1: Create admin invite API module**

Create `frontend-admin/src/api/modules/invite.ts`:

```ts
import { get, put } from '../request'
import type { ApiResult, PageQuery, PageResult } from '../types'

export interface InviteConfig {
  id?: number
  enabled: string
  rewardPpd: number
  rewardUnit?: string
  ruleText: string
  shareTitle: string
  landingTitle: string
  createdAt?: string
  updatedAt?: string
}

export interface InviteRecord {
  id: number
  inviterUserid: number
  inviteeUserid: number
  inviteCode?: string
  source?: string
  rewardPpd?: number
  credate?: string
  inviteeNickname?: string
  inviteeMobileMask?: string
}

export interface InviteRecordQuery extends PageQuery {
  inviterUserid?: number
  inviteeUserid?: number
  inviteCode?: string
  source?: string
}

export function getInviteConfig(): Promise<ApiResult<InviteConfig>> {
  return get<InviteConfig>('/admin/invite/config')
}

export function saveInviteConfig(data: InviteConfig): Promise<ApiResult<InviteConfig>> {
  return put<InviteConfig>('/admin/invite/config', data)
}

export function getInviteRecords(params: InviteRecordQuery): Promise<ApiResult<PageResult<InviteRecord>>> {
  return get<PageResult<InviteRecord>>('/admin/invite/records', params as Record<string, unknown>)
}
```

- [ ] **Step 2: Add menu group**

Modify `frontend-admin/src/constants/menu.ts`:

```ts
  {
    title: '邀请运营',
    icon: 'Promotion',
    children: [
      { title: '邀请配置', path: '/invite/config', component: 'invite/config/index' },
      { title: '邀请记录', path: '/invite/records', component: 'invite/records/index' },
    ],
  },
```

- [ ] **Step 3: Map route components**

Modify `frontend-admin/src/stores/modules/permission.ts`:

```ts
import InviteConfig from '@/views/invite/config/index.vue'
import InviteRecords from '@/views/invite/records/index.vue'
```

Add to `viewModules`:

```ts
  'invite/config/index': InviteConfig,
  'invite/records/index': InviteRecords,
```

- [ ] **Step 4: Create config page**

Create `frontend-admin/src/views/invite/config/index.vue`:

```vue
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getInviteConfig, saveInviteConfig, type InviteConfig } from '@/api/modules/invite'

const loading = ref(false)
const saving = ref(false)
const form = reactive<InviteConfig>({
  enabled: '1',
  rewardPpd: 3,
  ruleText: '好友通过你的邀请码注册后，自动到账 3 拍拍豆。',
  shareTitle: '好友邀请你加入爱去拍，找摄影师、找模特更方便',
  landingTitle: '我正在使用爱去拍，找摄影师、找模特特别方便，推荐你也来体验。',
})

async function fetchConfig() {
  loading.value = true
  try {
    const res = await getInviteConfig()
    if (res.data) Object.assign(form, res.data)
  } finally {
    loading.value = false
  }
}

function validate(): string {
  if (form.rewardPpd < 0 || form.rewardPpd > 999) return '奖励拍拍豆必须在 0 到 999 之间'
  if (!form.ruleText.trim()) return '请输入规则说明'
  if (!form.shareTitle.trim()) return '请输入分享标题'
  if (!form.landingTitle.trim()) return '请输入落地页主文案'
  return ''
}

async function submit() {
  const message = validate()
  if (message) {
    ElMessage.warning(message)
    return
  }
  saving.value = true
  try {
    const res = await saveInviteConfig({ ...form })
    if (res.data) Object.assign(form, res.data)
    ElMessage.success('邀请配置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(fetchConfig)
</script>

<template>
  <div class="invite-config-page">
    <el-card v-loading="loading" shadow="never">
      <template #header>
        <span>邀请基础配置</span>
      </template>
      <el-form :model="form" label-width="130px" @submit.prevent>
        <el-form-item label="启用邀请">
          <el-switch v-model="form.enabled" active-value="1" inactive-value="0" />
        </el-form-item>
        <el-form-item label="奖励拍拍豆">
          <el-input-number v-model="form.rewardPpd" :min="0" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="规则说明">
          <el-input v-model="form.ruleText" type="textarea" :rows="3" maxlength="300" show-word-limit />
        </el-form-item>
        <el-form-item label="分享标题">
          <el-input v-model="form.shareTitle" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="落地页主文案">
          <el-input v-model="form.landingTitle" type="textarea" :rows="3" maxlength="160" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submit">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.invite-config-page {
  max-width: 900px;
}
</style>
```

- [ ] **Step 5: Create records page**

Create `frontend-admin/src/views/invite/records/index.vue`:

```vue
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { getInviteRecords, type InviteRecord, type InviteRecordQuery } from '@/api/modules/invite'

const query = reactive<InviteRecordQuery>({ page: 0, size: 10, inviterUserid: undefined, inviteeUserid: undefined, inviteCode: '', source: '' })
const list = ref<InviteRecord[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = computed(() => (query.page ?? 0) + 1)

async function fetchList() {
  loading.value = true
  try {
    const res = await getInviteRecords(query)
    list.value = res.data?.content || []
    total.value = res.data?.totalElements || 0
  } finally {
    loading.value = false
  }
}

function search() { query.page = 0; fetchList() }
function reset() {
  Object.assign(query, { page: 0, inviterUserid: undefined, inviteeUserid: undefined, inviteCode: '', source: '' })
  fetchList()
}
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }

onMounted(fetchList)
</script>

<template>
  <div class="invite-records-page">
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="邀请人ID"><el-input-number v-model="query.inviterUserid" :min="1" controls-position="right" /></el-form-item>
        <el-form-item label="被邀请人ID"><el-input-number v-model="query.inviteeUserid" :min="1" controls-position="right" /></el-form-item>
        <el-form-item label="邀请码"><el-input v-model="query.inviteCode" clearable placeholder="请输入" /></el-form-item>
        <el-form-item label="来源"><el-input v-model="query.source" clearable placeholder="share / timeline" /></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="inviterUserid" label="邀请人ID" width="110" align="center" />
      <el-table-column prop="inviteeUserid" label="被邀请人ID" width="120" align="center" />
      <el-table-column prop="inviteeNickname" label="被邀请人昵称" min-width="140" />
      <el-table-column prop="inviteeMobileMask" label="被邀请人手机号" min-width="150" />
      <el-table-column prop="inviteCode" label="邀请码" width="120" align="center" />
      <el-table-column prop="source" label="来源" width="120" align="center" />
      <el-table-column prop="rewardPpd" label="奖励拍拍豆" width="120" align="center" />
      <el-table-column prop="credate" label="创建时间" min-width="170" />
    </el-table>
    <div class="pagination-wrapper">
      <el-pagination :current-page="currentPage" :page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next,jumper" background @current-change="pageChange" @size-change="sizeChange" />
    </div>
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

- [ ] **Step 6: Update permission test**

Add this case to `frontend-admin/src/stores/modules/__tests__/permission.test.ts`:

```ts
  it('maps invite menu entries to real pages instead of placeholders', () => {
    const store = usePermissionStore()
    const routes = store.generateRoutes()
    const invitePaths = ['invite/config', 'invite/records']

    for (const path of invitePaths) {
      const route = routes.find((item) => item.path === path)

      expect(route, `${path} route should exist`).toBeTruthy()
      expect(route?.meta?.placeholder, `${path} should not use PagePlaceholder`).toBeUndefined()
    }
  })
```

- [ ] **Step 7: Run focused admin test**

Run:

```bash
pnpm --dir frontend-admin test src/stores/modules/__tests__/permission.test.ts
```

Expected: test passes.

- [ ] **Step 8: Commit**

```bash
git add frontend-admin/src/api/modules/invite.ts \
  frontend-admin/src/constants/menu.ts \
  frontend-admin/src/stores/modules/permission.ts \
  frontend-admin/src/stores/modules/__tests__/permission.test.ts \
  frontend-admin/src/views/invite/config/index.vue \
  frontend-admin/src/views/invite/records/index.vue
git commit -m "feat: add admin invite operations" \
  -m "Add invite configuration and relation records to the rebuilt admin UI with route coverage." \
  -m "Tested: pnpm --dir frontend-admin test src/stores/modules/__tests__/permission.test.ts" \
  -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

## Task 6: Mini-Program Invite Pages

**Files:**
- Modify: `frontend/src/api/types/index.ts`
- Modify: `frontend/src/pages-sub/user/invite.vue`
- Modify: `frontend/src/pages-sub/content/invite-landing.vue`
- Modify: `frontend/src/pages-sub/user/invite-records.vue`

- [ ] **Step 1: Extend invite types**

Modify `frontend/src/api/types/index.ts`:

```ts
export interface InviteSummary {
  inviteCode: string
  totalInvited: number
  totalReward: number
  rewardPpd: number
  enabled?: string
  ruleText?: string
  shareTitle?: string
  landingTitle?: string
}

export interface InviteRule {
  enabled?: string
  rewardPpd: number
  rewardUnit: string
  ruleText: string
  shareTitle?: string
  landingTitle?: string
}
```

- [ ] **Step 2: Update invite page state**

In `frontend/src/pages-sub/user/invite.vue`, add:

```ts
import type { InviteRule, InviteSummary } from '@/api/types'

const rule = ref<InviteRule | null>(null)
const inviteEnabled = computed(() => (rule.value?.enabled ?? summary.value?.enabled ?? '1') === '1')
const rewardPpd = computed(() => summary.value?.rewardPpd ?? rule.value?.rewardPpd ?? 3)
const displayRuleText = computed(() => rule.value?.ruleText || summary.value?.ruleText || '好友通过你的邀请码注册后，自动到账 3 拍拍豆。')
const shareTitle = computed(() => rule.value?.shareTitle || '好友邀请你加入爱去拍，找摄影师、找模特更方便')
```

Change `loadRule` to assign `rule.value = result.data`.

- [ ] **Step 3: Update invite page share payload**

In `onShareAppMessage`, use:

```ts
onShareAppMessage(() => {
  const code = summary.value?.inviteCode
  return {
    title: shareTitle.value,
    path: code
      ? `/pages-sub/content/invite-landing?inviteCode=${code}&source=share`
      : '/pages-sub/content/invite-landing',
    imageUrl: '/static/logo.png',
  }
})
```

In `onShareTimeline`, use:

```ts
onShareTimeline(() => {
  const code = summary.value?.inviteCode
  return {
    title: shareTitle.value,
    query: code ? `inviteCode=${code}&source=timeline` : '',
  }
})
```

- [ ] **Step 4: Replace invite page template with activity layout**

Use this structure in `frontend/src/pages-sub/user/invite.vue`:

```vue
<view class="page">
  <KeepPageNav title="好友邀请" />
  <view class="hero">
    <view class="hero__badge">爱去拍邀请计划</view>
    <text class="hero__title">邀好友一起爱去拍</text>
    <text class="hero__subtitle">{{ displayRuleText }}</text>
  </view>
  <view class="reward-card" :class="{ 'reward-card--disabled': !inviteEnabled }">
    <text class="reward-card__label">{{ inviteEnabled ? '每成功邀请 1 位好友' : '邀请活动暂未开启' }}</text>
    <text class="reward-card__value">{{ inviteEnabled ? `+${rewardPpd} 拍拍豆` : '敬请期待' }}</text>
  </view>
  <view v-if="!isLoggedIn" class="card card--login">
    <text class="card__line">登录后即可生成你的专属邀请码</text>
    <button class="card__cta" @tap="goLogin">去登录</button>
  </view>
  <view v-else class="card card--code">
    <view class="stats">
      <view class="stat"><text class="stat__value">{{ summary?.totalInvited ?? 0 }}</text><text class="stat__label">成功邀请</text></view>
      <view class="stat-divider" />
      <view class="stat"><text class="stat__value">{{ summary?.totalReward ?? 0 }}</text><text class="stat__label">累计奖励</text></view>
    </view>
    <view class="code-row">
      <text class="code-row__label">我的邀请码</text>
      <text class="code-row__value">{{ summary?.inviteCode || '--' }}</text>
      <button class="code-row__copy" :disabled="!summary?.inviteCode" @tap="copyCode">复制</button>
    </view>
  </view>
</view>
```

Keep the existing action buttons and rules section, but disable share/copy with `!inviteEnabled`.

- [ ] **Step 5: Update landing page**

In `frontend/src/pages-sub/content/invite-landing.vue`, load `InviteRule` into `rule`, compute:

```ts
const inviteEnabled = computed(() => (rule.value?.enabled ?? '1') === '1')
const landingTitle = computed(() => rule.value?.landingTitle || '我正在使用爱去拍，找摄影师、找模特特别方便，推荐你也来体验。')
const rewardText = computed(() => inviteEnabled.value ? `注册后邀请人可获得 ${rule.value?.rewardPpd ?? 3} 拍拍豆` : '邀请活动暂未开启，也欢迎你体验爱去拍')
```

Change `goLogin` to preserve redirect and context:

```ts
function goLogin(): void {
  uni.navigateTo({ url: '/pages/login/index?redirect=' + encodeURIComponent('/pages/home/index') })
}
```

Use a green brand layout with a prominent primary button:

```vue
<view class="landing-hero">
  <view class="landing-hero__logo"><image src="/static/logo.png" mode="aspectFit" /></view>
  <text class="landing-hero__title">{{ landingTitle }}</text>
  <text class="landing-hero__reward">{{ rewardText }}</text>
</view>
<button class="cta" @tap="goLogin">马上体验</button>
```

- [ ] **Step 6: Run focused frontend tests**

Run:

```bash
pnpm --dir frontend test src/services/__tests__/invite-context.test.ts
```

Expected: test passes.

- [ ] **Step 7: Commit**

```bash
git add frontend/src/api/types/index.ts \
  frontend/src/pages-sub/user/invite.vue \
  frontend/src/pages-sub/content/invite-landing.vue \
  frontend/src/pages-sub/user/invite-records.vue
git commit -m "feat: upgrade mini program invite flow" \
  -m "Render configured invite rewards and route shared users through a branded invite landing page before login." \
  -m "Tested: pnpm --dir frontend test src/services/__tests__/invite-context.test.ts" \
  -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

## Task 7: Final Verification

**Files:**
- Inspect all changed files.

- [ ] **Step 1: Check worktree status**

Run:

```bash
git status --short --branch
```

Expected: branch `codex/invite-flow` with no unstaged surprises except intentional final edits before commit.

- [ ] **Step 2: Run source-level diff check**

Run:

```bash
git diff --check
```

Expected: no whitespace errors.

- [ ] **Step 3: Run focused tests**

Run available focused tests only:

```bash
mvn -pl backend/system-wap -Dtest=InviteControllerTest,AdminInviteControllerTest test
mvn -pl backend/system-domain -Dtest=InviteCodeCodecTest,InviteServiceRewardTest test
pnpm --dir frontend-admin test src/stores/modules/__tests__/permission.test.ts
pnpm --dir frontend test src/services/__tests__/invite-context.test.ts
```

Expected: all commands pass. If a command fails because local dependencies or Maven module paths differ, capture the exact failure and inspect the affected source manually.

- [ ] **Step 4: Verify acceptance criteria by search**

Run:

```bash
rg -n "/admin/invite/config|/admin/invite/records|/invite/rule|invite-landing|source=share|bindRelationIfAbsent|t_invite_config" backend frontend frontend-admin
```

Expected: results cover backend routes, admin routes, mini-program share path, idempotent bind method, and database script.

- [ ] **Step 5: Final commit if verification-only edits were made**

If verification caused edits, commit them:

```bash
git add <changed-files>
git commit -m "test: cover invite flow integration points" \
  -m "Add focused checks for the configurable invite loop across backend, admin UI, and mini-program entry points." \
  -m "Tested: focused invite verification commands from the implementation plan" \
  -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

If no edits were made, do not create an empty commit.
