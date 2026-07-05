# Member System Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the `BASIC(基础会员)` membership loop with configurable plans, configurable submit-ypat PPD discount, payment-safe member granting, admin management, and miniapp display.

**Architecture:** Use the existing Spring Boot(后端框架) multi-module shape: shared Qo(传输对象) classes in `system-object`, entities/repositories/services in `system-domain`, internal Feign(服务调用) endpoints in `system-restapi`, and public/admin endpoints in `system-wap`. Add admin Vue(管理后台框架) pages under `frontend-admin`, and miniapp uni-app(小程序框架) updates under `frontend`.

**Tech Stack:** Java 8, Spring Boot 1.5, Spring Data JPA, Feign, MySQL, Vue 3, TypeScript, Element Plus, uni-app, Pinia, Vitest.

---

## File Structure

### 后端共享对象

- Modify: `backend/system-object/src/main/java/com/ypat/MemberPlanQo.java`
  - Add `giftPpd`, `levelCode`, `recommended`.
- Modify: `backend/system-object/src/main/java/com/ypat/MemberOrderQo.java`
  - Add order snapshot fields: `planNameSnapshot`, `levelCodeSnapshot`, `originPriceFen`, `giftPpd`.
- Create: `backend/system-object/src/main/java/com/ypat/MemberBenefitRuleQo.java`
  - Admin/user transport for benefit rules.
- Create: `backend/system-object/src/main/java/com/ypat/MemberBenefitQuoteQo.java`
  - User-facing quote for original, discount, and actual PPD.
- Create: `backend/system-object/src/main/java/com/ypat/MemberOperationLogQo.java`
  - Admin log list/detail transport.
- Create: `backend/system-object/src/main/java/com/ypat/MemberUserAdminQo.java`
  - Admin user membership view and action payload.

### 后端领域层

- Modify: `backend/system-domain/src/main/java/com/ypat/entity/MemberPlan.java`
  - Add fields matching new plan Qo(传输对象).
- Modify: `backend/system-domain/src/main/java/com/ypat/entity/MemberOrder.java`
  - Add snapshot fields.
- Modify: `backend/system-domain/src/main/java/com/ypat/entity/UserMember.java`
  - Keep current `level`; add optional `status` if cancellation must be queryable separately from expiry.
- Create: `backend/system-domain/src/main/java/com/ypat/entity/MemberBenefitRule.java`
- Create: `backend/system-domain/src/main/java/com/ypat/entity/MemberOperationLog.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/MemberBenefitRuleRepository.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/MemberOperationLogRepository.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/MemberPlanRepository.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/MemberOrderRepository.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/MemberService.java`
- Create: `backend/system-domain/src/main/java/com/ypat/service/MemberBenefitCalculator.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java`

### 后端接口层

- Modify: `backend/system-restapi/src/main/java/com/ypat/controller/MemberController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/service/MemberServiceClient.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/MemberController.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/AdminMemberController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/config/WebSecurityConfig.java`

### 数据库

- Create: `docs/sql/pending/V_member_system_redesign.sql`
  - Idempotent DDL(数据库结构变更) and seed data for new fields/tables.

### 管理后台

- Create: `frontend-admin/src/api/modules/member.ts`
- Modify: `frontend-admin/src/api/types.ts`
- Modify: `frontend-admin/src/constants/menu.ts`
- Create: `frontend-admin/src/views/member/plan/index.vue`
- Create: `frontend-admin/src/views/member/plan/PlanEditDialog.vue`
- Create: `frontend-admin/src/views/member/rule/index.vue`
- Create: `frontend-admin/src/views/member/user/index.vue`
- Create: `frontend-admin/src/views/member/user/MemberActionDialog.vue`
- Create: `frontend-admin/src/views/member/order/index.vue`
- Create: `frontend-admin/src/views/member/log/index.vue`

### 小程序前端

- Modify: `frontend/src/api/types/index.ts`
- Modify: `frontend/src/api/modules/member.ts`
- Modify: `frontend/src/stores/member.ts`
- Modify: `frontend/src/pages/mine/index.vue`
- Modify: `frontend/src/pages-sub/user/member/index.vue`
- Modify: `frontend/src/components/business/AppointmentPublishForm.vue`

### 测试

- Create: `backend/system-domain/src/test/java/com/ypat/service/MemberBenefitCalculatorTest.java`
- Create: `backend/system-domain/src/test/java/com/ypat/service/MemberServiceBenefitTest.java`
- Modify: `backend/system-wap/src/test/java/com/ypat/controller/MemberControllerTest.java`
- Create: `backend/system-wap/src/test/java/com/ypat/controller/AdminMemberControllerTest.java`
- Create: `frontend/src/stores/__tests__/member.test.ts`
- Create: `frontend/src/components/business/__tests__/appointment-member-benefit.test.ts`
- Create: `frontend-admin/src/api/__tests__/member.test.ts`

---

## Scope Check

This is one vertical feature, not independent subsystems. The implementation order must keep a working path after each commit: data model first, then backend business rules, then backend endpoints, then admin UI, then miniapp UI.

---

### Task 1: Data Contract and Database Shape

**Files:**
- Modify: `backend/system-object/src/main/java/com/ypat/MemberPlanQo.java`
- Modify: `backend/system-object/src/main/java/com/ypat/MemberOrderQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/MemberBenefitRuleQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/MemberBenefitQuoteQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/MemberOperationLogQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/MemberUserAdminQo.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/entity/MemberPlan.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/entity/MemberOrder.java`
- Create: `backend/system-domain/src/main/java/com/ypat/entity/MemberBenefitRule.java`
- Create: `backend/system-domain/src/main/java/com/ypat/entity/MemberOperationLog.java`
- Create: `docs/sql/pending/V_member_system_redesign.sql`

- [ ] **Step 1: Add shared Qo(传输对象) fields**

Add these fields with JavaBean getters/setters:

```java
// MemberPlanQo.java
private Integer giftPpd;
private String levelCode;
private String recommended;

// MemberOrderQo.java
private String planNameSnapshot;
private String levelCodeSnapshot;
private Integer originPriceFen;
private Integer giftPpd;
```

- [ ] **Step 2: Create `MemberBenefitRuleQo`**

```java
package com.ypat;

import java.io.Serializable;

public class MemberBenefitRuleQo extends PageQo implements Serializable {
    private Long id;
    private String levelCode;
    private String scene;
    private String benefitType;
    private Integer discountPpd;
    private Integer minActualPpd;
    private String effective;
    private String status;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getBenefitType() { return benefitType; }
    public void setBenefitType(String benefitType) { this.benefitType = benefitType; }
    public Integer getDiscountPpd() { return discountPpd; }
    public void setDiscountPpd(Integer discountPpd) { this.discountPpd = discountPpd; }
    public Integer getMinActualPpd() { return minActualPpd; }
    public void setMinActualPpd(Integer minActualPpd) { this.minActualPpd = minActualPpd; }
    public String getEffective() { return effective; }
    public void setEffective(String effective) { this.effective = effective; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
```

- [ ] **Step 3: Create `MemberBenefitQuoteQo`**

```java
package com.ypat;

import java.io.Serializable;

public class MemberBenefitQuoteQo implements Serializable {
    private String scene;
    private Boolean memberActive;
    private String levelCode;
    private Integer originalPpd;
    private Integer discountPpd;
    private Integer actualPpd;
    private Boolean ruleEffective;

    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public Boolean getMemberActive() { return memberActive; }
    public void setMemberActive(Boolean memberActive) { this.memberActive = memberActive; }
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    public Integer getOriginalPpd() { return originalPpd; }
    public void setOriginalPpd(Integer originalPpd) { this.originalPpd = originalPpd; }
    public Integer getDiscountPpd() { return discountPpd; }
    public void setDiscountPpd(Integer discountPpd) { this.discountPpd = discountPpd; }
    public Integer getActualPpd() { return actualPpd; }
    public void setActualPpd(Integer actualPpd) { this.actualPpd = actualPpd; }
    public Boolean getRuleEffective() { return ruleEffective; }
    public void setRuleEffective(Boolean ruleEffective) { this.ruleEffective = ruleEffective; }
}
```

- [ ] **Step 4: Create admin Qo(传输对象) classes**

`MemberOperationLogQo.java`:

```java
package com.ypat;

import java.io.Serializable;
import java.util.Date;

public class MemberOperationLogQo extends PageQo implements Serializable {
    private Long id;
    private Long userId;
    private Long operatorId;
    private String actionType;
    private String reason;
    private String beforeValue;
    private String afterValue;
    private String sourceOrderNo;
    private Date createdAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getBeforeValue() { return beforeValue; }
    public void setBeforeValue(String beforeValue) { this.beforeValue = beforeValue; }
    public String getAfterValue() { return afterValue; }
    public void setAfterValue(String afterValue) { this.afterValue = afterValue; }
    public String getSourceOrderNo() { return sourceOrderNo; }
    public void setSourceOrderNo(String sourceOrderNo) { this.sourceOrderNo = sourceOrderNo; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
```

`MemberUserAdminQo.java`:

```java
package com.ypat;

import java.io.Serializable;
import java.util.Date;

public class MemberUserAdminQo extends PageQo implements Serializable {
    private Long userId;
    private String mobile;
    private String nickname;
    private String levelCode;
    private Date expireAt;
    private String memberStatus;
    private Integer days;
    private String reason;
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    public Date getExpireAt() { return expireAt; }
    public void setExpireAt(Date expireAt) { this.expireAt = expireAt; }
    public String getMemberStatus() { return memberStatus; }
    public void setMemberStatus(String memberStatus) { this.memberStatus = memberStatus; }
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
```

- [ ] **Step 5: Add entity fields**

Add matching `@Column` fields and getters/setters to `MemberPlan` and `MemberOrder`:

```java
// MemberPlan.java
@Column(name = "gift_ppd")
private Integer giftPpd;
@Column(name = "level_code", length = 16)
private String levelCode;
@Column(length = 1)
private String recommended;

// MemberOrder.java
@Column(name = "plan_name_snapshot", length = 64)
private String planNameSnapshot;
@Column(name = "level_code_snapshot", length = 16)
private String levelCodeSnapshot;
@Column(name = "origin_price_fen")
private Integer originPriceFen;
@Column(name = "gift_ppd")
private Integer giftPpd;
```

- [ ] **Step 6: Create `MemberBenefitRule` entity**

```java
package com.ypat.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_member_benefit_rule",
        uniqueConstraints = @UniqueConstraint(name = "uk_level_scene_type", columnNames = {"level_code", "scene", "benefit_type"}))
public class MemberBenefitRule implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "level_code", nullable = false, length = 16)
    private String levelCode;
    @Column(nullable = false, length = 32)
    private String scene;
    @Column(name = "benefit_type", nullable = false, length = 32)
    private String benefitType;
    @Column(name = "discount_ppd", nullable = false)
    private Integer discountPpd;
    @Column(name = "min_actual_ppd", nullable = false)
    private Integer minActualPpd;
    @Column(nullable = false, length = 1)
    private String effective;
    @Column(nullable = false, length = 1)
    private String status;
    @Column(length = 256)
    private String description;
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getBenefitType() { return benefitType; }
    public void setBenefitType(String benefitType) { this.benefitType = benefitType; }
    public Integer getDiscountPpd() { return discountPpd; }
    public void setDiscountPpd(Integer discountPpd) { this.discountPpd = discountPpd; }
    public Integer getMinActualPpd() { return minActualPpd; }
    public void setMinActualPpd(Integer minActualPpd) { this.minActualPpd = minActualPpd; }
    public String getEffective() { return effective; }
    public void setEffective(String effective) { this.effective = effective; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 7: Create `MemberOperationLog` entity**

Use the same field names as `MemberOperationLogQo`, with table `t_member_operation_log`, index `idx_user_created_at(user_id, created_at)`, and index `idx_operator_created_at(operator_id, created_at)`.

- [ ] **Step 8: Write SQL migration**

Create `docs/sql/pending/V_member_system_redesign.sql` with:

```sql
SET NAMES utf8mb4;
SET time_zone = '+08:00';

ALTER TABLE `t_member_plan`
  ADD COLUMN `gift_ppd` INT DEFAULT 0 COMMENT '开通赠送拍拍豆',
  ADD COLUMN `level_code` VARCHAR(16) NOT NULL DEFAULT 'BASIC' COMMENT '绑定会员等级',
  ADD COLUMN `recommended` VARCHAR(1) NOT NULL DEFAULT '0' COMMENT '0 否 1 是';

ALTER TABLE `t_member_order`
  ADD COLUMN `plan_name_snapshot` VARCHAR(64) DEFAULT NULL COMMENT '套餐名称快照',
  ADD COLUMN `level_code_snapshot` VARCHAR(16) DEFAULT NULL COMMENT '等级快照',
  ADD COLUMN `origin_price_fen` INT DEFAULT NULL COMMENT '划线价快照',
  ADD COLUMN `gift_ppd` INT DEFAULT 0 COMMENT '赠送拍拍豆快照';

CREATE TABLE IF NOT EXISTS `t_member_benefit_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `level_code` VARCHAR(16) NOT NULL,
  `scene` VARCHAR(32) NOT NULL,
  `benefit_type` VARCHAR(32) NOT NULL,
  `discount_ppd` INT NOT NULL DEFAULT 0,
  `min_actual_ppd` INT NOT NULL DEFAULT 0,
  `effective` VARCHAR(1) NOT NULL DEFAULT '0',
  `status` VARCHAR(1) NOT NULL DEFAULT '1',
  `description` VARCHAR(256) DEFAULT NULL,
  `updated_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level_scene_type` (`level_code`, `scene`, `benefit_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员权益规则';

CREATE TABLE IF NOT EXISTS `t_member_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `operator_id` BIGINT DEFAULT NULL,
  `action_type` VARCHAR(32) NOT NULL,
  `reason` VARCHAR(256) DEFAULT NULL,
  `before_value` VARCHAR(1024) DEFAULT NULL,
  `after_value` VARCHAR(1024) DEFAULT NULL,
  `source_order_no` VARCHAR(64) DEFAULT NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_created_at` (`user_id`, `created_at`),
  KEY `idx_operator_created_at` (`operator_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员操作日志';

INSERT INTO `t_member_benefit_rule`
  (`level_code`, `scene`, `benefit_type`, `discount_ppd`, `min_actual_ppd`, `effective`, `status`, `description`, `updated_at`)
VALUES
  ('BASIC', 'SUBMIT_YPAT', 'PPD_DISCOUNT', 2, 0, '1', '1', '提交约拍会员减免', NOW())
ON DUPLICATE KEY UPDATE `updated_at` = NOW();
```

If MySQL(数据库) rejects duplicate `ALTER TABLE ADD COLUMN`, convert each add into guarded statements used by the repo's deployment SQL style before execution.

- [ ] **Step 9: Compile shared modules**

Run:

```bash
cd backend
mvn -pl system-object,system-domain -DskipTests compile
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 10: Commit**

```bash
git add backend/system-object/src/main/java/com/ypat \
  backend/system-domain/src/main/java/com/ypat/entity \
  docs/sql/pending/V_member_system_redesign.sql
git commit -m "feat(member): add membership data contracts" -m "Add fields and data contracts for plan gifts, order snapshots, benefit rules, and operation logs." -m "Tested: mvn -pl system-object,system-domain -DskipTests compile" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 2: Benefit Calculator and Domain Repositories

**Files:**
- Create: `backend/system-domain/src/test/java/com/ypat/service/MemberBenefitCalculatorTest.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/MemberBenefitRuleRepository.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/MemberOperationLogRepository.java`
- Create: `backend/system-domain/src/main/java/com/ypat/service/MemberBenefitCalculator.java`

- [ ] **Step 1: Write failing calculator tests**

Create `MemberBenefitCalculatorTest.java`:

```java
package com.ypat.service;

import com.ypat.MemberBenefitQuoteQo;
import com.ypat.entity.MemberBenefitRule;
import com.ypat.entity.UserMember;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

public class MemberBenefitCalculatorTest {
    @Test
    public void activeBasicMemberGetsSubmitDiscount() {
        MemberBenefitCalculator c = new MemberBenefitCalculator();
        MemberBenefitRule rule = rule(2, 0, "1", "1");
        MemberBenefitQuoteQo q = c.calculate("SUBMIT_YPAT", 5, activeMember(), rule);
        assertTrue(q.getMemberActive());
        assertEquals(Integer.valueOf(5), q.getOriginalPpd());
        assertEquals(Integer.valueOf(2), q.getDiscountPpd());
        assertEquals(Integer.valueOf(3), q.getActualPpd());
        assertTrue(q.getRuleEffective());
    }

    @Test
    public void discountNeverDropsBelowMinimum() {
        MemberBenefitCalculator c = new MemberBenefitCalculator();
        MemberBenefitQuoteQo q = c.calculate("SUBMIT_YPAT", 5, activeMember(), rule(9, 0, "1", "1"));
        assertEquals(Integer.valueOf(0), q.getActualPpd());
    }

    @Test
    public void expiredMemberGetsNoDiscount() {
        MemberBenefitCalculator c = new MemberBenefitCalculator();
        MemberBenefitQuoteQo q = c.calculate("SUBMIT_YPAT", 5, expiredMember(), rule(2, 0, "1", "1"));
        assertFalse(q.getMemberActive());
        assertEquals(Integer.valueOf(0), q.getDiscountPpd());
        assertEquals(Integer.valueOf(5), q.getActualPpd());
    }

    @Test
    public void disabledRuleGetsNoDiscount() {
        MemberBenefitCalculator c = new MemberBenefitCalculator();
        MemberBenefitQuoteQo q = c.calculate("SUBMIT_YPAT", 5, activeMember(), rule(2, 0, "1", "0"));
        assertEquals(Integer.valueOf(0), q.getDiscountPpd());
        assertEquals(Integer.valueOf(5), q.getActualPpd());
        assertFalse(q.getRuleEffective());
    }

    private static MemberBenefitRule rule(int discount, int min, String effective, String status) {
        MemberBenefitRule r = new MemberBenefitRule();
        r.setLevelCode("BASIC");
        r.setScene("SUBMIT_YPAT");
        r.setBenefitType("PPD_DISCOUNT");
        r.setDiscountPpd(discount);
        r.setMinActualPpd(min);
        r.setEffective(effective);
        r.setStatus(status);
        return r;
    }

    private static UserMember activeMember() {
        UserMember m = new UserMember();
        m.setUserId(1L);
        m.setLevel("BASIC");
        m.setExpireAt(daysFromNow(1));
        return m;
    }

    private static UserMember expiredMember() {
        UserMember m = activeMember();
        m.setExpireAt(daysFromNow(-1));
        return m;
    }

    private static Date daysFromNow(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, days);
        return c.getTime();
    }
}
```

- [ ] **Step 2: Run failing tests**

Run:

```bash
cd backend
mvn -pl system-domain -Dtest=MemberBenefitCalculatorTest test
```

Expected: compilation failure because `MemberBenefitCalculator` does not exist.

- [ ] **Step 3: Add repositories**

`MemberBenefitRuleRepository.java`:

```java
package com.ypat.repository;

import com.ypat.entity.MemberBenefitRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberBenefitRuleRepository extends JpaRepository<MemberBenefitRule, Long>, JpaSpecificationExecutor<MemberBenefitRule> {
    MemberBenefitRule findByLevelCodeAndSceneAndBenefitType(@Param("levelCode") String levelCode,
                                                            @Param("scene") String scene,
                                                            @Param("benefitType") String benefitType);
    List<MemberBenefitRule> findByLevelCodeOrderBySceneAsc(@Param("levelCode") String levelCode);
}
```

`MemberOperationLogRepository.java`:

```java
package com.ypat.repository;

import com.ypat.entity.MemberOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MemberOperationLogRepository extends JpaRepository<MemberOperationLog, Long>, JpaSpecificationExecutor<MemberOperationLog> {
}
```

- [ ] **Step 4: Implement `MemberBenefitCalculator`**

```java
package com.ypat.service;

import com.ypat.MemberBenefitQuoteQo;
import com.ypat.entity.MemberBenefitRule;
import com.ypat.entity.UserMember;

import java.util.Date;

public class MemberBenefitCalculator {
    public static final String LEVEL_BASIC = "BASIC";
    public static final String BENEFIT_TYPE_PPD_DISCOUNT = "PPD_DISCOUNT";

    public MemberBenefitQuoteQo calculate(String scene, int originalPpd, UserMember member, MemberBenefitRule rule) {
        MemberBenefitQuoteQo q = new MemberBenefitQuoteQo();
        q.setScene(scene);
        q.setOriginalPpd(originalPpd);
        boolean memberActive = isActiveBasic(member);
        q.setMemberActive(memberActive);
        q.setLevelCode(memberActive ? member.getLevel() : null);
        boolean ruleEffective = memberActive
                && rule != null
                && scene.equals(rule.getScene())
                && BENEFIT_TYPE_PPD_DISCOUNT.equals(rule.getBenefitType())
                && "1".equals(rule.getEffective())
                && "1".equals(rule.getStatus());
        q.setRuleEffective(ruleEffective);
        int discount = ruleEffective && rule.getDiscountPpd() != null ? Math.max(0, rule.getDiscountPpd()) : 0;
        int min = ruleEffective && rule.getMinActualPpd() != null ? Math.max(0, rule.getMinActualPpd()) : 0;
        q.setDiscountPpd(discount);
        q.setActualPpd(Math.max(min, Math.max(0, originalPpd - discount)));
        return q;
    }

    private boolean isActiveBasic(UserMember member) {
        return member != null
                && LEVEL_BASIC.equals(member.getLevel())
                && member.getExpireAt() != null
                && member.getExpireAt().after(new Date());
    }
}
```

- [ ] **Step 5: Run calculator tests**

Run:

```bash
cd backend
mvn -pl system-domain -Dtest=MemberBenefitCalculatorTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```bash
git add backend/system-domain/src/main/java/com/ypat/repository/MemberBenefitRuleRepository.java \
  backend/system-domain/src/main/java/com/ypat/repository/MemberOperationLogRepository.java \
  backend/system-domain/src/main/java/com/ypat/service/MemberBenefitCalculator.java \
  backend/system-domain/src/test/java/com/ypat/service/MemberBenefitCalculatorTest.java
git commit -m "feat(member): add benefit calculator" -m "Calculate BASIC member PPD discounts for submit-ypat using configurable benefit rules." -m "Tested: mvn -pl system-domain -Dtest=MemberBenefitCalculatorTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 3: Member Service Payment, Quote, and Admin Operations

**Files:**
- Modify: `backend/system-domain/src/main/java/com/ypat/service/MemberService.java`
- Create: `backend/system-domain/src/test/java/com/ypat/service/MemberServiceBenefitTest.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java`

- [ ] **Step 1: Write service tests for quote and idempotent payment**

Create `MemberServiceBenefitTest.java` using Spring-free unit style with fake repositories and `ReflectionTestUtils.setField`, following `MemberControllerTest`. The test file must include these test methods:

```java
@Test
public void quoteSubmitYpatUsesConfiguredDiscount() {
    MemberService service = new MemberService();
    FakeUserMemberRepository userMembers = new FakeUserMemberRepository();
    FakeMemberBenefitRuleRepository rules = new FakeMemberBenefitRuleRepository();
    userMembers.member = activeMember();
    rules.rule = submitRule(2, 0, "1", "1");
    ReflectionTestUtils.setField(service, "userMemberRepository", userMembers);
    ReflectionTestUtils.setField(service, "memberBenefitRuleRepository", rules);

    MemberBenefitQuoteQo quote = service.quoteBenefit(1L, "SUBMIT_YPAT");

    assertEquals(Integer.valueOf(5), quote.getOriginalPpd());
    assertEquals(Integer.valueOf(2), quote.getDiscountPpd());
    assertEquals(Integer.valueOf(3), quote.getActualPpd());
    assertTrue(quote.getMemberActive());
}

@Test
public void markPaidDoesNotGrantTwiceWhenRepositoryUpdateReturnsZero() {
    MemberService service = new MemberService();
    FakeMemberOrderRepository orders = new FakeMemberOrderRepository();
    FakeUserMemberRepository userMembers = new FakeUserMemberRepository();
    FakeRecordRepository records = new FakeRecordRepository();
    orders.markPaidRows = 0;
    ReflectionTestUtils.setField(service, "memberOrderRepository", orders);
    ReflectionTestUtils.setField(service, "userMemberRepository", userMembers);
    ReflectionTestUtils.setField(service, "recordRepository", records);

    boolean result = service.markPaid("M202607050001", "wx-tx", new Date());

    assertFalse(result);
    assertEquals(0, userMembers.saveCount);
    assertEquals(0, records.saveCount);
}
```

Add helper methods `activeMember()` and `submitRule(int discount, int minActualPpd, String effective, String status)` with the same field values used in `MemberBenefitCalculatorTest`. Add fake repository classes that override only the methods exercised by these two tests.

- [ ] **Step 2: Run service tests to verify failure**

Run:

```bash
cd backend
mvn -pl system-domain -Dtest=MemberServiceBenefitTest test
```

Expected: FAIL because `quoteBenefit` and gift-PPD logic are missing.

- [ ] **Step 3: Extend `MemberService` constants**

Add:

```java
private static final String SCENE_SUBMIT_YPAT = "SUBMIT_YPAT";
private static final String BENEFIT_TYPE_PPD_DISCOUNT = "PPD_DISCOUNT";
private static final int SUBMIT_YPAT_ORIGINAL_PPD = Constant.PUB_NEED_PPD;
```

Import `com.ypat.constant.Constant`.

- [ ] **Step 4: Add `quoteBenefit` to `MemberService`**

```java
public MemberBenefitQuoteQo quoteBenefit(Long userId, String scene) {
    int originalPpd = SCENE_SUBMIT_YPAT.equals(scene) ? SUBMIT_YPAT_ORIGINAL_PPD : 0;
    UserMember member = userId == null ? null : userMemberRepository.findOne(userId);
    String level = member == null ? MemberBenefitCalculator.LEVEL_BASIC : member.getLevel();
    MemberBenefitRule rule = memberBenefitRuleRepository.findByLevelCodeAndSceneAndBenefitType(
            level == null ? MemberBenefitCalculator.LEVEL_BASIC : level,
            scene,
            MemberBenefitCalculator.BENEFIT_TYPE_PPD_DISCOUNT
    );
    return new MemberBenefitCalculator().calculate(scene, originalPpd, member, rule);
}
```

- [ ] **Step 5: Snapshot new order fields**

In `createPendingOrder`, after setting `planCode`, set:

```java
order.setPlanNameSnapshot(plan.getName());
order.setLevelCodeSnapshot(plan.getLevelCode() == null ? LEVEL_BASIC : plan.getLevelCode());
order.setOriginPriceFen(plan.getOriginPriceFen());
order.setGiftPpd(plan.getGiftPpd() == null ? 0 : plan.getGiftPpd());
```

- [ ] **Step 6: Grant gift PPD in `markPaid`**

After the `grantMemberDuration(order.getUserId(), order.getDurationDays(), outTradeNo)` call, load the paid order and if `giftPpd > 0`, increment user balance and save a `Record`:

```java
private void grantGiftPpd(MemberOrder order) {
    if (order.getGiftPpd() == null || order.getGiftPpd() <= 0) return;
    User user = userRepository.findById(order.getUserId());
    if (user == null) throw new SysException(ResponseCode.FAIL_NOT);
    user.setPpd((user.getPpd() == null ? 0 : user.getPpd()) + order.getGiftPpd());
    userRepository.save(user);

    Record record = new Record();
    record.setCredate(new Date());
    record.setPpd(order.getGiftPpd());
    record.setUserid(order.getUserId());
    record.setType(RecordType.PAY.value);
    recordRepository.save(record);
}
```

If `User.getPpd()` is primitive `int`, remove the null guard.

- [ ] **Step 7: Log member operations**

Add helper:

```java
private void saveOperationLog(Long userId, Long operatorId, String actionType, String reason, String beforeValue, String afterValue, String sourceOrderNo) {
    MemberOperationLog log = new MemberOperationLog();
    log.setUserId(userId);
    log.setOperatorId(operatorId);
    log.setActionType(actionType);
    log.setReason(reason);
    log.setBeforeValue(beforeValue);
    log.setAfterValue(afterValue);
    log.setSourceOrderNo(sourceOrderNo);
    log.setCreatedAt(new Date());
    memberOperationLogRepository.save(log);
}
```

Call it from payment grant with `actionType="PAY_GRANT"` and from admin methods with `ADMIN_GRANT`, `ADMIN_EXTEND`, `ADMIN_CANCEL`.

- [ ] **Step 8: Add admin service methods**

Add methods with these signatures and validation behavior:

```java
public boolean adminGrant(Long userId, int days, Long operatorId, String reason) {
    validateManualAction(userId, days, reason);
    grantMemberDuration(userId, days, "ADMIN-GRANT-" + userId + "-" + System.currentTimeMillis());
    saveOperationLog(userId, operatorId, "ADMIN_GRANT", reason, null, "days=" + days, null);
    return true;
}

public boolean adminExtend(Long userId, int days, Long operatorId, String reason) {
    validateManualAction(userId, days, reason);
    grantMemberDuration(userId, days, "ADMIN-EXTEND-" + userId + "-" + System.currentTimeMillis());
    saveOperationLog(userId, operatorId, "ADMIN_EXTEND", reason, null, "days=" + days, null);
    return true;
}

public boolean adminCancel(Long userId, Long operatorId, String reason) {
    if (userId == null) throw new SysException(ResponseCode.FAIL_PARA);
    if (reason == null || reason.trim().isEmpty()) throw new SysException(ResponseCode.FAIL_PARA);
    UserMember member = userMemberRepository.findOne(userId);
    if (member == null) return false;
    member.setLevel(LEVEL_NONE);
    member.setExpireAt(new Date());
    member.setUpdatedAt(new Date());
    userMemberRepository.save(member);
    saveOperationLog(userId, operatorId, "ADMIN_CANCEL", reason, null, "level=NONE", null);
    return true;
}

public Map<String, Object> findAdminUsers(MemberUserAdminQo qo) {
    return new HashMap<String, Object>();
}

public Map<String, Object> findOperationLogs(MemberOperationLogQo qo) {
    return new HashMap<String, Object>();
}

public MemberPlanQo savePlan(MemberPlanQo qo) {
    if (qo == null || qo.getName() == null || qo.getName().trim().isEmpty()) throw new SysException(ResponseCode.FAIL_PARA);
    MemberPlan entity = qo.getId() == null ? new MemberPlan() : memberPlanRepository.findById(qo.getId());
    if (entity == null) throw new SysException(ResponseCode.FAIL_NOT);
    CopyUtil.copy(qo, entity);
    entity.setUpdatedAt(new Date());
    return CopyUtil.copy(memberPlanRepository.save(entity), MemberPlanQo.class);
}

public MemberBenefitRuleQo saveBenefitRule(MemberBenefitRuleQo qo) {
    if (qo == null || qo.getId() == null) throw new SysException(ResponseCode.FAIL_PARA);
    MemberBenefitRule entity = memberBenefitRuleRepository.findOne(qo.getId());
    if (entity == null) throw new SysException(ResponseCode.FAIL_NOT);
    CopyUtil.copy(qo, entity);
    entity.setUpdatedAt(new Date());
    return CopyUtil.copy(memberBenefitRuleRepository.save(entity), MemberBenefitRuleQo.class);
}
```

Add this validation helper:

```java
if (userId == null) throw new SysException(ResponseCode.FAIL_PARA);
if (days <= 0) throw new SysException(ResponseCode.FAIL_PARA);
if (reason == null || reason.trim().isEmpty()) throw new SysException(ResponseCode.FAIL_PARA);
```

- [ ] **Step 9: Modify `YpatInfoService.submit` to use quote**

Replace direct `Constant.PUB_NEED_PPD` checks and record creation with:

```java
MemberBenefitQuoteQo quote = memberService.quoteBenefit(ypatInfo.getUserid(), "SUBMIT_YPAT");
int actualPpd = quote.getActualPpd() == null ? Constant.PUB_NEED_PPD : quote.getActualPpd();
if (user.getPpd() < actualPpd) {
    throw new SysException(ResponseCode.FAIL_BALANCE);
}
this.save(ypatInfo);
YpatInfo info = get(ypatInfo.getId());
if (info == null) {
    throw new SysException(ResponseCode.FAIL_NOT);
}
info.setStatus(YpatStatus.ytj.value);
info.setPubdate(new Date());
ypatInfoRepository.save(info);

user.setPpd(user.getPpd() - actualPpd);
user.setPubtimes(user.getPubtimes() + 1);
userRepository.save(user);

record.setPpd(-1 * actualPpd);
```

Inject `MemberService memberService` into `YpatInfoService`.

- [ ] **Step 10: Run domain tests**

Run:

```bash
cd backend
mvn -pl system-domain -Dtest=MemberBenefitCalculatorTest,MemberServiceBenefitTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 11: Commit**

```bash
git add backend/system-domain/src/main/java/com/ypat/service/MemberService.java \
  backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java \
  backend/system-domain/src/test/java/com/ypat/service/MemberServiceBenefitTest.java
git commit -m "feat(member): apply benefits in domain services" -m "Quote submit-ypat discounts, snapshot member orders, grant gift PPD on paid orders, and apply actual PPD on ypat submission." -m "Tested: mvn -pl system-domain -Dtest=MemberBenefitCalculatorTest,MemberServiceBenefitTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 4: Public, Internal, and Admin Backend Endpoints

**Files:**
- Modify: `backend/system-restapi/src/main/java/com/ypat/controller/MemberController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/service/MemberServiceClient.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/MemberController.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/AdminMemberController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/config/WebSecurityConfig.java`
- Modify: `backend/system-wap/src/test/java/com/ypat/controller/MemberControllerTest.java`
- Create: `backend/system-wap/src/test/java/com/ypat/controller/AdminMemberControllerTest.java`

- [ ] **Step 1: Add security test intent to `MemberControllerTest`**

Extend `plansReturnsActivePlans` with assertion that it does not require authentication:

```java
@Test
public void plansReturnsActivePlansWithoutAuthentication() {
    SecurityContextHolder.clearContext();
    List<MemberPlanQo> result = controller.plans();
    assertEquals(1, result.size());
}
```

- [ ] **Step 2: Add `/member/plans` to public GET whitelist**

In `WebSecurityConfig.configure(HttpSecurity http)`, add `"/member/plans"` to the GET `permitAll(公开白名单)` list.

- [ ] **Step 3: Add internal restapi endpoints**

In `backend/system-restapi/src/main/java/com/ypat/controller/MemberController.java`, add:

```java
@GetMapping("/service/member/benefit/quote")
public MemberBenefitQuoteQo quote(@RequestParam("userId") Long userId, @RequestParam("scene") String scene) {
    return memberService.quoteBenefit(userId, scene);
}

@PostMapping("/service/member/admin/plan/save")
public MemberPlanQo savePlan(@RequestBody MemberPlanQo qo) {
    return memberService.savePlan(qo);
}

@PostMapping("/service/member/admin/rule/save")
public MemberBenefitRuleQo saveRule(@RequestBody MemberBenefitRuleQo qo) {
    return memberService.saveBenefitRule(qo);
}
```

Also add internal endpoints for admin grant/extend/cancel, admin user list, admin order list, and logs. Keep them under `/service/member/admin/**`.

- [ ] **Step 4: Extend `MemberServiceClient`**

Add Feign methods matching the internal endpoints:

```java
@GetMapping("/service/member/benefit/quote")
MemberBenefitQuoteQo quote(@RequestParam("userId") Long userId, @RequestParam("scene") String scene);

@PostMapping("/service/member/admin/plan/save")
MemberPlanQo savePlan(@RequestBody MemberPlanQo qo);
```

Repeat for rule save, admin user list, grant, extend, cancel, orders, and logs.

- [ ] **Step 5: Add user quote endpoint**

In `system-wap` `MemberController`:

```java
@GetMapping("/member/benefit/quote")
public MemberBenefitQuoteQo quote(@RequestParam String scene) {
    Long userId = requireUserId();
    return memberServiceClient.quote(userId, scene);
}
```

- [ ] **Step 6: Create `AdminMemberController`**

Use `@RestController` and route prefix by method paths:

```java
@RestController
public class AdminMemberController {
    @Autowired
    private MemberServiceClient memberServiceClient;

    @GetMapping("/admin/member/plans")
    public Object plans(MemberPlanQo qo) { return memberServiceClient.adminPlans(qo); }

    @PostMapping("/admin/member/plans")
    public MemberPlanQo savePlan(@RequestBody MemberPlanQo qo) { return memberServiceClient.savePlan(qo); }

    @PutMapping("/admin/member/plans/{id}")
    public MemberPlanQo updatePlan(@PathVariable Long id, @RequestBody MemberPlanQo qo) {
        qo.setId(id);
        return memberServiceClient.savePlan(qo);
    }

    @GetMapping("/admin/member/benefit-rules")
    public Object rules(MemberBenefitRuleQo qo) { return memberServiceClient.adminRules(qo); }

    @PutMapping("/admin/member/benefit-rules/{id}")
    public MemberBenefitRuleQo updateRule(@PathVariable Long id, @RequestBody MemberBenefitRuleQo qo) {
        qo.setId(id);
        return memberServiceClient.saveRule(qo);
    }
}
```

Add user, order, and log methods in the same controller, using `UserUtil.getUserId()` as `operatorId` for grant/extend/cancel.

- [ ] **Step 7: Write `AdminMemberControllerTest`**

Test that grant requires a reason:

```java
@Test(expected = SysException.class)
public void grantRequiresReason() {
    MemberUserAdminQo qo = new MemberUserAdminQo();
    qo.setDays(7);
    qo.setReason("");
    controller.grant(2L, qo);
}
```

Test that plan save forwards payload:

```java
@Test
public void savePlanForwardsPayload() {
    MemberPlanQo qo = new MemberPlanQo();
    qo.setName("月卡");
    qo.setPriceFen(4800);
    MemberPlanQo result = controller.savePlan(qo);
    assertEquals("月卡", fakeClient.lastSavedPlan.getName());
    assertEquals(Integer.valueOf(4800), result.getPriceFen());
}
```

- [ ] **Step 8: Run backend endpoint tests**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=MemberControllerTest,AdminMemberControllerTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 9: Commit**

```bash
git add backend/system-restapi/src/main/java/com/ypat/controller/MemberController.java \
  backend/system-wap/src/main/java/com/ypat/service/MemberServiceClient.java \
  backend/system-wap/src/main/java/com/ypat/controller/MemberController.java \
  backend/system-wap/src/main/java/com/ypat/controller/AdminMemberController.java \
  backend/system-wap/src/main/java/com/ypat/config/WebSecurityConfig.java \
  backend/system-wap/src/test/java/com/ypat/controller/MemberControllerTest.java \
  backend/system-wap/src/test/java/com/ypat/controller/AdminMemberControllerTest.java
git commit -m "feat(member): expose member APIs" -m "Expose public plans, authenticated quote/status/order APIs, and admin membership management endpoints." -m "Tested: mvn -pl system-wap -Dtest=MemberControllerTest,AdminMemberControllerTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 5: Admin API and Menu

**Files:**
- Create: `frontend-admin/src/api/modules/member.ts`
- Modify: `frontend-admin/src/api/types.ts`
- Modify: `frontend-admin/src/constants/menu.ts`
- Create: `frontend-admin/src/api/__tests__/member.test.ts`

- [ ] **Step 1: Add admin member types**

In `frontend-admin/src/api/types.ts`, add:

```ts
export interface MemberPlan {
  id: number
  code: string
  name: string
  durationDays: number
  priceFen: number
  originPriceFen?: number
  giftPpd: number
  levelCode: string
  recommended: string
  status: string
  sortNo?: number
  updatedAt?: string
}

export interface MemberBenefitRule {
  id: number
  levelCode: string
  scene: string
  benefitType: string
  discountPpd: number
  minActualPpd: number
  effective: string
  status: string
  description?: string
}
```

- [ ] **Step 2: Create API module**

`frontend-admin/src/api/modules/member.ts`:

```ts
import { get, post, put } from '../request'
import type { ApiResult, PageResult, PageQuery, MemberPlan, MemberBenefitRule } from '../types'

export interface MemberPlanQuery extends PageQuery {
  name?: string
  status?: string
}

export function getMemberPlans(params: MemberPlanQuery): Promise<ApiResult<PageResult<MemberPlan>>> {
  return get<PageResult<MemberPlan>>('/admin/member/plans', params as Record<string, unknown>)
}

export function saveMemberPlan(data: Partial<MemberPlan>): Promise<ApiResult<MemberPlan>> {
  if (data.id) return put<MemberPlan>(`/admin/member/plans/${data.id}`, data)
  return post<MemberPlan>('/admin/member/plans', data)
}

export function getMemberBenefitRules(params: PageQuery): Promise<ApiResult<PageResult<MemberBenefitRule>>> {
  return get<PageResult<MemberBenefitRule>>('/admin/member/benefit-rules', params as Record<string, unknown>)
}

export function saveMemberBenefitRule(data: Partial<MemberBenefitRule>): Promise<ApiResult<MemberBenefitRule>> {
  return put<MemberBenefitRule>(`/admin/member/benefit-rules/${data.id}`, data)
}
```

Add functions for member users, orders, and logs with paths from the design doc.

- [ ] **Step 3: Add member menu**

In `frontend-admin/src/constants/menu.ts`, append:

```ts
{
  title: '会员系统',
  icon: 'Medal',
  children: [
    { title: '套餐管理', path: '/member/plan', component: 'member/plan/index' },
    { title: '权益配置', path: '/member/rule', component: 'member/rule/index' },
    { title: '会员用户', path: '/member/user', component: 'member/user/index' },
    { title: '会员订单', path: '/member/order', component: 'member/order/index' },
    { title: '操作日志', path: '/member/log', component: 'member/log/index' },
  ],
}
```

- [ ] **Step 4: Add API tests**

Create `frontend-admin/src/api/__tests__/member.test.ts` with tests that mock request module and assert paths:

```ts
import { describe, expect, it, vi } from 'vitest'

vi.mock('../request', () => ({
  get: vi.fn((url, params) => Promise.resolve({ success: true, data: { url, params } })),
  post: vi.fn((url, data) => Promise.resolve({ success: true, data: { url, data } })),
  put: vi.fn((url, data) => Promise.resolve({ success: true, data: { url, data } })),
}))

import { getMemberPlans, saveMemberPlan, saveMemberBenefitRule } from '../modules/member'

describe('admin member api', () => {
  it('queries member plans from admin path', async () => {
    const res = await getMemberPlans({ page: 0, size: 10, status: '1' })
    expect((res.data as any).url).toBe('/admin/member/plans')
  })

  it('creates plan with POST and updates plan with PUT', async () => {
    const created = await saveMemberPlan({ name: '月卡' })
    expect((created.data as any).url).toBe('/admin/member/plans')
    const updated = await saveMemberPlan({ id: 7, name: '季卡' })
    expect((updated.data as any).url).toBe('/admin/member/plans/7')
  })

  it('updates benefit rule by id', async () => {
    const res = await saveMemberBenefitRule({ id: 3, discountPpd: 2 })
    expect((res.data as any).url).toBe('/admin/member/benefit-rules/3')
  })
})
```

- [ ] **Step 5: Run admin API tests and type check**

Run:

```bash
cd frontend-admin
pnpm test -- member
pnpm type-check
```

Expected: both pass.

- [ ] **Step 6: Commit**

```bash
git add frontend-admin/src/api/modules/member.ts frontend-admin/src/api/types.ts frontend-admin/src/constants/menu.ts frontend-admin/src/api/__tests__/member.test.ts
git commit -m "feat(admin): add member API contracts" -m "Add admin membership API helpers, shared types, and menu entries for the member system." -m "Tested: pnpm test -- member; pnpm type-check" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 6: Admin Member Pages

**Files:**
- Create: `frontend-admin/src/views/member/plan/index.vue`
- Create: `frontend-admin/src/views/member/plan/PlanEditDialog.vue`
- Create: `frontend-admin/src/views/member/rule/index.vue`
- Create: `frontend-admin/src/views/member/user/index.vue`
- Create: `frontend-admin/src/views/member/user/MemberActionDialog.vue`
- Create: `frontend-admin/src/views/member/order/index.vue`
- Create: `frontend-admin/src/views/member/log/index.vue`

- [ ] **Step 1: Build plan list page**

Use `frontend-admin/src/views/manage/product-list/index.vue` as the pattern. Columns: ID, name, durationDays, priceFen, originPriceFen, giftPpd, levelCode, recommended, status, sortNo, updatedAt, actions.

- [ ] **Step 2: Build plan edit dialog**

Use `ProductEditDialog.vue` as the pattern. Required fields: `code`, `name`, `durationDays`, `priceFen`, `giftPpd`, `levelCode`. Numeric fields have `:min="0"`. `levelCode` defaults to `BASIC`.

- [ ] **Step 3: Build rule page**

Use an editable table. Columns: levelCode, scene, benefitType, discountPpd, minActualPpd, effective, status, description. Edit opens an Element Plus dialog and calls `saveMemberBenefitRule`.

- [ ] **Step 4: Build user page and action dialog**

User page filters by mobile, nickname, memberStatus, and expireAt range. `MemberActionDialog` takes action type `grant | extend | cancel`, requires `reason`, and requires positive `days` for grant/extend.

- [ ] **Step 5: Build order and log read-only pages**

Order page has no edit buttons. Log page has filters only. Both use normal pagination.

- [ ] **Step 6: Run admin build**

Run:

```bash
cd frontend-admin
pnpm type-check
pnpm build
```

Expected: both pass.

- [ ] **Step 7: Commit**

```bash
git add frontend-admin/src/views/member
git commit -m "feat(admin): add member management pages" -m "Add plan, rule, user, order, and operation-log pages for membership operations." -m "Tested: pnpm type-check; pnpm build" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 7: Miniapp Member APIs, Store, and Pages

**Files:**
- Modify: `frontend/src/api/types/index.ts`
- Modify: `frontend/src/api/modules/member.ts`
- Modify: `frontend/src/stores/member.ts`
- Modify: `frontend/src/pages-sub/user/member/index.vue`
- Modify: `frontend/src/pages/mine/index.vue`
- Create: `frontend/src/stores/__tests__/member.test.ts`

- [ ] **Step 1: Add frontend types**

Extend `MemberPlan`, `MemberOrder`, and add:

```ts
export interface MemberBenefitQuote {
  scene: string
  memberActive: boolean
  levelCode?: string
  originalPpd: number
  discountPpd: number
  actualPpd: number
  ruleEffective: boolean
}
```

- [ ] **Step 2: Add quote API**

In `frontend/src/api/modules/member.ts`:

```ts
export function getMemberBenefitQuote(scene: 'SUBMIT_YPAT'): Promise<ApiResult<MemberBenefitQuote>> {
  return get('/member/benefit/quote', { scene })
}
```

Keep `getMemberPlans()` as `withToken: false`.

- [ ] **Step 3: Extend member store**

Add:

```ts
const submitYpatQuote = ref<MemberBenefitQuote | null>(null)

async function refreshSubmitYpatQuote(): Promise<MemberBenefitQuote | null> {
  const result = await memberApi.getMemberBenefitQuote('SUBMIT_YPAT')
  if (result.success && result.data) {
    submitYpatQuote.value = result.data
    return result.data
  }
  submitYpatQuote.value = null
  return null
}
```

Return `submitYpatQuote` and `refreshSubmitYpatQuote`.

- [ ] **Step 4: Write store test**

Mock `memberApi.getMemberBenefitQuote` and assert the quote is cached:

```ts
it('caches submit ypat quote', async () => {
  vi.spyOn(memberApi, 'getMemberBenefitQuote').mockResolvedValue({
    success: true,
    code: '200',
    message: '',
    data: { scene: 'SUBMIT_YPAT', memberActive: true, levelCode: 'BASIC', originalPpd: 5, discountPpd: 2, actualPpd: 3, ruleEffective: true },
  })
  const store = useMemberStore()
  const quote = await store.refreshSubmitYpatQuote()
  expect(quote?.actualPpd).toBe(3)
  expect(store.submitYpatQuote?.discountPpd).toBe(2)
})
```

- [ ] **Step 5: Update member center UI**

In `pages-sub/user/member/index.vue`, show gift PPD and selected plan. Make the bottom CTA use selected plan instead of tapping each card. Show status line: `有效期至 YYYY-MM-DD` when active.

- [ ] **Step 6: Update mine member card**

In `frontend/src/pages/mine/index.vue`, make the card read from `memberStore.status`: active users see effective date, inactive users see “提交约拍可省拍拍豆”.

- [ ] **Step 7: Run miniapp tests**

Run:

```bash
cd frontend
pnpm test -- member
pnpm type-check
```

Expected: both pass.

- [ ] **Step 8: Commit**

```bash
git add frontend/src/api/types/index.ts frontend/src/api/modules/member.ts frontend/src/stores/member.ts frontend/src/pages-sub/user/member/index.vue frontend/src/pages/mine/index.vue frontend/src/stores/__tests__/member.test.ts
git commit -m "feat(frontend): show member value and quote state" -m "Expose member benefit quotes and improve membership status and plan displays in the miniapp." -m "Tested: pnpm test -- member; pnpm type-check" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 8: Submit Ypat Quote Display

**Files:**
- Modify: `frontend/src/components/business/AppointmentPublishForm.vue`
- Create: `frontend/src/components/business/__tests__/appointment-member-benefit.test.ts`

- [ ] **Step 1: Write component test**

Test that quote data renders:

```ts
it('shows member discount when quote is active', async () => {
  // mount AppointmentPublishForm with mocked member store quote:
  // { originalPpd: 5, discountPpd: 2, actualPpd: 3, memberActive: true }
  // expect text: 原价：5 拍拍豆
  // expect text: BASIC 会员优惠：-2 拍拍豆
  // expect text: 本次实扣：3 拍拍豆
})
```

Use the existing component test setup in `frontend/src/components/business/__tests__/appointment-publish-form.test.ts` as the pattern.

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd frontend
pnpm test -- appointment-member-benefit
```

Expected: FAIL because the component does not render member benefit quote.

- [ ] **Step 3: Load quote on mount**

In `AppointmentPublishForm.vue`, import the store:

```ts
import { useMemberStore } from '@/stores/member'

const memberStore = useMemberStore()
const memberQuote = computed(() => memberStore.submitYpatQuote)

onMounted(async () => {
  try {
    await memberStore.refreshSubmitYpatQuote()
  } catch {
    // Quote failure must not block publishing; submit endpoint still enforces actual balance.
  }
  try {
    const res = await getWorkTags()
    const data = (res && res.data) || []
    tagOptions.value = resolveWorkTagOptions(data)
  } catch (e) {
    tagOptions.value = resolveWorkTagOptions([])
  }
})
```

Merge this with the existing `onMounted` block rather than creating two competing blocks.

- [ ] **Step 4: Render quote before submit button**

Add a card above `bottom-spacer`:

```vue
<view v-if="memberQuote" class="appointment-publish-form__card appointment-publish-form__benefit">
  <view class="appointment-publish-form__benefit-row">
    <text>原价</text>
    <text>{{ memberQuote.originalPpd }} 拍拍豆</text>
  </view>
  <view v-if="memberQuote.discountPpd > 0" class="appointment-publish-form__benefit-row appointment-publish-form__benefit-row--discount">
    <text>BASIC 会员优惠</text>
    <text>-{{ memberQuote.discountPpd }} 拍拍豆</text>
  </view>
  <view class="appointment-publish-form__benefit-row appointment-publish-form__benefit-row--actual">
    <text>本次实扣</text>
    <text>{{ memberQuote.actualPpd }} 拍拍豆</text>
  </view>
</view>
```

Add SCSS for `__benefit-row`, `--discount`, and `--actual` with existing colors.

- [ ] **Step 5: Run component test**

Run:

```bash
cd frontend
pnpm test -- appointment-member-benefit
pnpm type-check
```

Expected: both pass.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/components/business/AppointmentPublishForm.vue frontend/src/components/business/__tests__/appointment-member-benefit.test.ts
git commit -m "feat(frontend): show member submit discount" -m "Show original, discounted, and actual PPD costs on the submit-ypat form using backend benefit quotes." -m "Tested: pnpm test -- appointment-member-benefit; pnpm type-check" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 9: Full Verification and Release Notes

**Files:**
- Modify: `docs/sql/pending/README.md`
- Create: `docs/release/member-system-redesign-checklist.md`

- [ ] **Step 1: Update SQL pending README**

Add `V_member_system_redesign.sql` to the file list and document execution after `V_pending_member.sql`.

- [ ] **Step 2: Create release checklist**

`docs/release/member-system-redesign-checklist.md` must include:

```markdown
# 会员系统上线检查清单

- [ ] 测试库执行 `V_pending_member.sql`
- [ ] 测试库执行 `V_member_system_redesign.sql`
- [ ] 验证 `GET /member/plans` 未登录返回套餐列表
- [ ] 验证 `GET /member/status` 未登录返回 401
- [ ] 后台配置 BASIC 提交约拍优惠
- [ ] 后台上架至少一个会员套餐
- [ ] 小程序会员页显示套餐
- [ ] 提交约拍页显示原价、优惠、实扣
- [ ] 微信支付回调重复发送不会重复发豆
- [ ] 会员入口开关可关闭
```

- [ ] **Step 3: Run backend checks**

Run:

```bash
cd backend
mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Run frontend checks**

Run:

```bash
cd frontend
pnpm test
pnpm type-check
```

Expected: all pass.

- [ ] **Step 5: Run admin checks**

Run:

```bash
cd frontend-admin
pnpm test
pnpm type-check
pnpm build
```

Expected: all pass.

- [ ] **Step 6: Commit**

```bash
git add docs/sql/pending/README.md docs/release/member-system-redesign-checklist.md
git commit -m "docs(member): add rollout checklist" -m "Document database execution order and release verification for the membership system." -m "Tested: mvn test; pnpm test/type-check/build as listed in checklist" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

## Final Verification

Run the complete checks from the repository root:

```bash
cd backend && mvn test
cd ../frontend && pnpm check
cd ../frontend-admin && pnpm test && pnpm build
```

Expected:

- Backend Maven(后端构建工具) reports `BUILD SUCCESS`.
- Miniapp frontend(小程序前端) reports passing env validation, type check, lint, tests, and H5 build.
- Admin frontend(管理后台前端) reports passing tests and production build.

## Handoff Notes

- Existing worktree has unrelated dirty files. Each task must stage only files listed in that task.
- `.superpowers/(可视化辅助缓存目录)` is not part of implementation and must not be committed.
- The first production-facing smoke test after backend deploy is:

```bash
curl -sS 'http://localhost:8081/member/plans'
curl -sS 'http://localhost:8081/member/status'
```

Expected:

- `/member/plans` returns plan data without `Token(登录令牌)`.
- `/member/status` returns `401 unauthorized(未授权)` without `Token(登录令牌)`.
