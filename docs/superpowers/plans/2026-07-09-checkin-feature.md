# Checkin Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建每日签到完整链路：小程序签到入口、后端签到事务、拍豆流水同步、后台规则维护和签到记录查询。

**Architecture:** 使用独立 `t_checkin_rule` 和 `t_checkin_record` 承载签到规则与业务记录，签到成功时在同一事务内更新 `t_user.ppd` 并写入 `t_record`。小程序通过 `/checkin/today` 和 `/checkin/do` 接入，后台通过 `/admin/checkin/rule` 和 `/admin/checkin/records` 管理。

**Tech Stack:** Java Spring Boot(后端框架), Spring Data JPA(数据访问), Feign(服务间调用), Vue 3 + uni-app(小程序), Vue 3 + Element Plus(后台管理), Vitest(前端测试), Maven(后端测试)。

---

## 前置注意

当前工作区可能存在用户已有未提交改动。每个任务提交时只 stage(暂存)该任务列出的文件，不要使用 `git add .`。

推荐每个任务开始前运行：

```bash
git status --short
```

如果看到本计划之外的脏文件，保留它们，不要回滚。

## 文件结构

### 后端公共对象

- `backend/system-object/src/main/java/com/ypat/CheckinRuleQo.java`：签到规则 Qo(数据传输对象)。
- `backend/system-object/src/main/java/com/ypat/CheckinRecordQo.java`：后台签到记录查询和列表 Qo(数据传输对象)。
- `backend/system-object/src/main/java/com/ypat/CheckinTodayQo.java`：小程序今日状态返回 Qo(数据传输对象)。
- `backend/system-object/src/main/java/com/ypat/CheckinResultQo.java`：小程序执行签到返回 Qo(数据传输对象)。
- `backend/system-object/src/main/java/com/ypat/enums/RecordType.java`：新增 `CHECKIN("6", "每日签到")`。

### 后端领域层

- `backend/system-domain/src/main/java/com/ypat/entity/CheckinRule.java`：`t_checkin_rule` 实体。
- `backend/system-domain/src/main/java/com/ypat/entity/CheckinRecord.java`：`t_checkin_record` 实体。
- `backend/system-domain/src/main/java/com/ypat/repository/CheckinRuleRepository.java`：规则仓储。
- `backend/system-domain/src/main/java/com/ypat/repository/CheckinRecordRepository.java`：记录仓储。
- `backend/system-domain/src/main/java/com/ypat/service/CheckinService.java`：规则、今日状态、签到事务、后台记录查询。
- `backend/system-domain/src/test/java/com/ypat/service/CheckinServiceTest.java`：核心服务单元测试。

### 后端接口层

- `backend/system-restapi/src/main/java/com/ypat/controller/CheckinController.java`：`/service/checkin/*` 内部服务接口。
- `backend/system-wap/src/main/java/com/ypat/service/CheckinServiceClient.java`：Feign(服务间调用)客户端。
- `backend/system-wap/src/main/java/com/ypat/controller/CheckinController.java`：小程序接口。
- `backend/system-wap/src/main/java/com/ypat/controller/AdminCheckinController.java`：后台管理接口。

### 数据库脚本

- `backend/dev/mysql/20260709_create_checkin.sql`：开发库建表和默认规则。
- `docs/sql/pending/V_checkin_feature.sql`：发布待执行 SQL(数据库脚本)。

### 小程序前端

- `frontend/src/api/types/index.ts`：新增签到接口类型。
- `frontend/src/api/modules/checkin.ts`：小程序签到 API(接口)模块。
- `frontend/src/constants/enums.ts`：新增 `RecordType.CHECKIN` 和文案。
- `frontend/src/components/business/KeepIcon.vue`：新增 `calendar-check` 签到 icon(图标)。
- `frontend/src/pages/mine/index.vue`：我的页签到入口、状态加载、confirm(确认弹窗)、执行签到。
- `frontend/src/pages/mine/index.test.ts`：源码契约测试。
- `frontend/src/pages-sub/user/wallet.vue`：每日签到任务接入真实状态。
- `frontend/src/pages-sub/user/wallet.test.ts`：源码契约测试。
- `frontend/src/pages-sub/user/records.vue`：只需要确认枚举文案自动生效，通常不改。

### 后台管理前端

- `frontend-admin/src/api/types.ts`：新增签到规则和记录类型。
- `frontend-admin/src/api/modules/checkin.ts`：后台签到 API(接口)模块。
- `frontend-admin/src/views/checkin/index.vue`：规则维护和记录查询页面。
- `frontend-admin/src/constants/menu.ts`：新增“签到管理”菜单项。
- `frontend-admin/src/stores/modules/permission.ts`：显式导入并注册签到页面组件。
- `frontend-admin/tests/unit/admin-checkin-api.test.ts`：API(接口)源码契约测试。
- `frontend-admin/tests/unit/admin-checkin-page.test.ts`：页面源码契约测试。

---

### Task 1: 后端公共 Qo 和拍豆记录类型

**Files:**
- Create: `backend/system-object/src/main/java/com/ypat/CheckinRuleQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/CheckinRecordQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/CheckinTodayQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/CheckinResultQo.java`
- Modify: `backend/system-object/src/main/java/com/ypat/enums/RecordType.java`

- [ ] **Step 1: 新增 `CheckinRuleQo`**

Create `backend/system-object/src/main/java/com/ypat/CheckinRuleQo.java`:

```java
package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class CheckinRuleQo implements Serializable {
    private Long id;
    private String enabled;
    private Integer rewardPpd;
    private String confirmTitle;
    private String confirmContent;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
    public String getConfirmTitle() { return confirmTitle; }
    public void setConfirmTitle(String confirmTitle) { this.confirmTitle = confirmTitle; }
    public String getConfirmContent() { return confirmContent; }
    public void setConfirmContent(String confirmContent) { this.confirmContent = confirmContent; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 2: 新增 `CheckinRecordQo`**

Create `backend/system-object/src/main/java/com/ypat/CheckinRecordQo.java`:

```java
package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class CheckinRecordQo extends PageQo implements Serializable {
    private Long id;
    private Long userid;
    private String mobile;
    private String nickname;
    private String checkinDate;
    private String dateFrom;
    private String dateTo;
    private Integer rewardPpd;
    private Long recordId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserid() { return userid; }
    public void setUserid(Long userid) { this.userid = userid; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getCheckinDate() { return checkinDate; }
    public void setCheckinDate(String checkinDate) { this.checkinDate = checkinDate; }
    public String getDateFrom() { return dateFrom; }
    public void setDateFrom(String dateFrom) { this.dateFrom = dateFrom; }
    public String getDateTo() { return dateTo; }
    public void setDateTo(String dateTo) { this.dateTo = dateTo; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 3: 新增小程序返回 Qo**

Create `backend/system-object/src/main/java/com/ypat/CheckinTodayQo.java`:

```java
package com.ypat;

import java.io.Serializable;

public class CheckinTodayQo implements Serializable {
    private Boolean enabled;
    private Boolean checkedIn;
    private Integer rewardPpd;
    private String confirmTitle;
    private String confirmContent;
    private String checkinDate;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Boolean getCheckedIn() { return checkedIn; }
    public void setCheckedIn(Boolean checkedIn) { this.checkedIn = checkedIn; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
    public String getConfirmTitle() { return confirmTitle; }
    public void setConfirmTitle(String confirmTitle) { this.confirmTitle = confirmTitle; }
    public String getConfirmContent() { return confirmContent; }
    public void setConfirmContent(String confirmContent) { this.confirmContent = confirmContent; }
    public String getCheckinDate() { return checkinDate; }
    public void setCheckinDate(String checkinDate) { this.checkinDate = checkinDate; }
}
```

Create `backend/system-object/src/main/java/com/ypat/CheckinResultQo.java`:

```java
package com.ypat;

import java.io.Serializable;

public class CheckinResultQo implements Serializable {
    private Boolean checkedIn;
    private Integer rewardPpd;
    private Integer currentPpd;
    private Long recordId;
    private String message;

    public Boolean getCheckedIn() { return checkedIn; }
    public void setCheckedIn(Boolean checkedIn) { this.checkedIn = checkedIn; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
    public Integer getCurrentPpd() { return currentPpd; }
    public void setCurrentPpd(Integer currentPpd) { this.currentPpd = currentPpd; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
```

- [ ] **Step 4: 新增拍豆流水类型**

Modify `backend/system-object/src/main/java/com/ypat/enums/RecordType.java`:

```java
/**
 * 0.充值、1.好友邀请、2.系统赠送、3.发布约拍、4.申请约拍、5.查看联系方式、6.每日签到
 */
public enum RecordType {

    PAY("0","充值"),
    FRI("1","好友邀请"),
    SYS("2","系统赠送"),
    PUB("3","发布约拍"),
    APP("4","申请约拍"),
    VIEW("5","查看联系方式"),
    CHECKIN("6","每日签到");
```

- [ ] **Step 5: 运行轻量校验**

Run:

```bash
./mvnw -pl backend/system-object -DskipTests compile
```

Expected: 编译通过；如果本地没有 Maven wrapper(包装脚本)，改用：

```bash
mvn -pl backend/system-object -DskipTests compile
```

- [ ] **Step 6: Commit**

```bash
git add backend/system-object/src/main/java/com/ypat/CheckinRuleQo.java \
  backend/system-object/src/main/java/com/ypat/CheckinRecordQo.java \
  backend/system-object/src/main/java/com/ypat/CheckinTodayQo.java \
  backend/system-object/src/main/java/com/ypat/CheckinResultQo.java \
  backend/system-object/src/main/java/com/ypat/enums/RecordType.java
git commit -m "feat: add checkin transfer objects"
```

---

### Task 2: 后端实体、仓储和 SQL

**Files:**
- Create: `backend/system-domain/src/main/java/com/ypat/entity/CheckinRule.java`
- Create: `backend/system-domain/src/main/java/com/ypat/entity/CheckinRecord.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/CheckinRuleRepository.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/CheckinRecordRepository.java`
- Create: `backend/dev/mysql/20260709_create_checkin.sql`
- Create: `docs/sql/pending/V_checkin_feature.sql`

- [ ] **Step 1: 新增签到规则实体**

Create `backend/system-domain/src/main/java/com/ypat/entity/CheckinRule.java`:

```java
package com.ypat.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_checkin_rule")
public class CheckinRule implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 1)
    private String enabled;
    @Column(name = "reward_ppd", nullable = false)
    private Integer rewardPpd;
    @Column(name = "confirm_title", nullable = false, length = 64)
    private String confirmTitle;
    @Column(name = "confirm_content", nullable = false, length = 256)
    private String confirmContent;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
    public String getConfirmTitle() { return confirmTitle; }
    public void setConfirmTitle(String confirmTitle) { this.confirmTitle = confirmTitle; }
    public String getConfirmContent() { return confirmContent; }
    public void setConfirmContent(String confirmContent) { this.confirmContent = confirmContent; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 2: 新增签到记录实体**

Create `backend/system-domain/src/main/java/com/ypat/entity/CheckinRecord.java`:

```java
package com.ypat.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_checkin_record",
        uniqueConstraints = @UniqueConstraint(name = "uniq_user_checkin_date", columnNames = {"userid", "checkin_date"}))
public class CheckinRecord implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userid;
    @Column(name = "checkin_date", nullable = false, length = 10)
    private String checkinDate;
    @Column(name = "reward_ppd", nullable = false)
    private Integer rewardPpd;
    @Column(name = "record_id")
    private Long recordId;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserid() { return userid; }
    public void setUserid(Long userid) { this.userid = userid; }
    public String getCheckinDate() { return checkinDate; }
    public void setCheckinDate(String checkinDate) { this.checkinDate = checkinDate; }
    public Integer getRewardPpd() { return rewardPpd; }
    public void setRewardPpd(Integer rewardPpd) { this.rewardPpd = rewardPpd; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 3: 新增仓储**

Create `backend/system-domain/src/main/java/com/ypat/repository/CheckinRuleRepository.java`:

```java
package com.ypat.repository;

import com.ypat.entity.CheckinRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CheckinRuleRepository extends JpaRepository<CheckinRule, Long>, JpaSpecificationExecutor<CheckinRule> {
    CheckinRule findTopByOrderByIdAsc();
}
```

Create `backend/system-domain/src/main/java/com/ypat/repository/CheckinRecordRepository.java`:

```java
package com.ypat.repository;

import com.ypat.entity.CheckinRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

public interface CheckinRecordRepository extends JpaRepository<CheckinRecord, Long>, JpaSpecificationExecutor<CheckinRecord> {
    CheckinRecord findByUseridAndCheckinDate(@Param("userid") Long userid, @Param("checkinDate") String checkinDate);
}
```

- [ ] **Step 4: 新增 SQL 脚本**

Create `backend/dev/mysql/20260709_create_checkin.sql` and `docs/sql/pending/V_checkin_feature.sql` with identical content:

```sql
CREATE TABLE IF NOT EXISTS `t_checkin_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `enabled` varchar(1) NOT NULL DEFAULT '1' COMMENT '是否启用: 1启用 0关闭',
  `reward_ppd` int(11) NOT NULL DEFAULT 1 COMMENT '每日奖励拍豆数',
  `confirm_title` varchar(64) NOT NULL DEFAULT '每日签到' COMMENT '确认弹窗标题',
  `confirm_content` varchar(256) NOT NULL DEFAULT '签到成功可获得 1 拍豆' COMMENT '确认弹窗内容',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到规则表';

CREATE TABLE IF NOT EXISTS `t_checkin_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userid` bigint(20) NOT NULL COMMENT '用户ID',
  `checkin_date` varchar(10) NOT NULL COMMENT '签到日期, Asia/Shanghai, yyyy-MM-dd',
  `reward_ppd` int(11) NOT NULL DEFAULT 0 COMMENT '当次奖励拍豆数',
  `record_id` bigint(20) DEFAULT NULL COMMENT '对应t_record.id',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_checkin_date` (`userid`, `checkin_date`),
  KEY `idx_checkin_date` (`checkin_date`),
  KEY `idx_checkin_userid` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

INSERT INTO `t_checkin_rule` (`id`, `enabled`, `reward_ppd`, `confirm_title`, `confirm_content`, `created_at`, `updated_at`)
SELECT 1, '1', 1, '每日签到', '签到成功可获得 1 拍豆', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `t_checkin_rule` WHERE `id` = 1);
```

- [ ] **Step 5: 运行轻量校验**

Run:

```bash
rg -n "t_checkin_rule|t_checkin_record|uniq_user_checkin_date" backend/dev/mysql/20260709_create_checkin.sql docs/sql/pending/V_checkin_feature.sql
```

Expected: 两个 SQL 文件都命中表名和唯一索引。

- [ ] **Step 6: Commit**

```bash
git add backend/system-domain/src/main/java/com/ypat/entity/CheckinRule.java \
  backend/system-domain/src/main/java/com/ypat/entity/CheckinRecord.java \
  backend/system-domain/src/main/java/com/ypat/repository/CheckinRuleRepository.java \
  backend/system-domain/src/main/java/com/ypat/repository/CheckinRecordRepository.java \
  backend/dev/mysql/20260709_create_checkin.sql \
  docs/sql/pending/V_checkin_feature.sql
git commit -m "feat: add checkin persistence model"
```

---

### Task 3: 后端签到服务和单元测试

**Files:**
- Create: `backend/system-domain/src/main/java/com/ypat/service/CheckinService.java`
- Create: `backend/system-domain/src/test/java/com/ypat/service/CheckinServiceTest.java`

- [ ] **Step 1: 写服务测试骨架**

Create `backend/system-domain/src/test/java/com/ypat/service/CheckinServiceTest.java`:

```java
package com.ypat.service;

import com.ypat.CheckinResultQo;
import com.ypat.CheckinTodayQo;
import com.ypat.entity.CheckinRecord;
import com.ypat.entity.CheckinRule;
import com.ypat.entity.Record;
import com.ypat.entity.User;
import com.ypat.repository.CheckinRecordRepository;
import com.ypat.repository.CheckinRuleRepository;
import com.ypat.repository.RecordRepository;
import com.ypat.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CheckinServiceTest {
    private CheckinRuleRepository ruleRepository;
    private CheckinRecordRepository checkinRecordRepository;
    private RecordRepository recordRepository;
    private UserRepository userRepository;
    private CheckinService service;

    @Before
    public void setUp() {
        ruleRepository = mock(CheckinRuleRepository.class);
        checkinRecordRepository = mock(CheckinRecordRepository.class);
        recordRepository = mock(RecordRepository.class);
        userRepository = mock(UserRepository.class);
        service = new CheckinService(ruleRepository, checkinRecordRepository, recordRepository, userRepository);
    }

    @Test
    public void todayReturnsUncheckedWhenNoRecord() {
        when(ruleRepository.findTopByOrderByIdAsc()).thenReturn(rule("1", 1));
        when(checkinRecordRepository.findByUseridAndCheckinDate(eq(10L), anyString())).thenReturn(null);

        CheckinTodayQo today = service.today(10L);

        assertTrue(today.getEnabled());
        assertFalse(today.getCheckedIn());
        assertEquals(Integer.valueOf(1), today.getRewardPpd());
        assertEquals("每日签到", today.getConfirmTitle());
    }

    @Test
    public void doCheckinAddsPpdAndRecordOnce() {
        User user = new User();
        user.setId(10L);
        user.setPpd(3);
        when(ruleRepository.findTopByOrderByIdAsc()).thenReturn(rule("1", 1));
        when(checkinRecordRepository.findByUseridAndCheckinDate(eq(10L), anyString())).thenReturn(null);
        when(userRepository.findById(10L)).thenReturn(user);
        when(checkinRecordRepository.save(any(CheckinRecord.class))).thenAnswer(invocation -> {
            CheckinRecord record = invocation.getArgument(0);
            if (record.getId() == null) record.setId(100L);
            return record;
        });
        when(recordRepository.save(any(Record.class))).thenAnswer(invocation -> {
            Record record = invocation.getArgument(0);
            assignRecordId(record, 200L);
            return record;
        });

        CheckinResultQo result = service.doCheckin(10L);

        assertTrue(result.getCheckedIn());
        assertEquals(Integer.valueOf(1), result.getRewardPpd());
        assertEquals(Integer.valueOf(4), result.getCurrentPpd());
        assertEquals(Long.valueOf(200L), result.getRecordId());
        verify(userRepository).save(user);
        ArgumentCaptor<Record> recordCaptor = ArgumentCaptor.forClass(Record.class);
        verify(recordRepository).save(recordCaptor.capture());
        assertEquals("6", recordCaptor.getValue().getType());
        assertEquals(Integer.valueOf(1), recordCaptor.getValue().getPpd());
    }

    @Test
    public void doCheckinReturnsAlreadyCheckedWithoutAddingPpd() {
        when(ruleRepository.findTopByOrderByIdAsc()).thenReturn(rule("1", 1));
        CheckinRecord existing = new CheckinRecord();
        existing.setRecordId(55L);
        when(checkinRecordRepository.findByUseridAndCheckinDate(eq(10L), anyString())).thenReturn(existing);

        CheckinResultQo result = service.doCheckin(10L);

        assertTrue(result.getCheckedIn());
        assertEquals(Integer.valueOf(0), result.getRewardPpd());
        assertEquals("今日已签到", result.getMessage());
        verify(recordRepository, never()).save(any(Record.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void doCheckinReturnsClosedWhenRuleDisabled() {
        when(ruleRepository.findTopByOrderByIdAsc()).thenReturn(rule("0", 1));

        CheckinResultQo result = service.doCheckin(10L);

        assertFalse(result.getCheckedIn());
        assertEquals(Integer.valueOf(0), result.getRewardPpd());
        assertEquals("签到活动暂未开启", result.getMessage());
        verify(checkinRecordRepository, never()).save(any(CheckinRecord.class));
    }

    private CheckinRule rule(String enabled, Integer rewardPpd) {
        CheckinRule rule = new CheckinRule();
        rule.setId(1L);
        rule.setEnabled(enabled);
        rule.setRewardPpd(rewardPpd);
        rule.setConfirmTitle("每日签到");
        rule.setConfirmContent("签到成功可获得 " + rewardPpd + " 拍豆");
        return rule;
    }

    private void assignRecordId(Record record, Long id) {
        try {
            java.lang.reflect.Field field = Record.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(record, id);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
mvn -pl backend/system-domain -Dtest=CheckinServiceTest test
```

Expected: FAIL，原因是 `CheckinService` 不存在或构造方法不存在。

- [ ] **Step 3: 实现 `CheckinService`**

Create `backend/system-domain/src/main/java/com/ypat/service/CheckinService.java`:

```java
package com.ypat.service;

import com.ypat.CheckinRecordQo;
import com.ypat.CheckinResultQo;
import com.ypat.CheckinRuleQo;
import com.ypat.CheckinTodayQo;
import com.ypat.SysException;
import com.ypat.entity.CheckinRecord;
import com.ypat.entity.CheckinRule;
import com.ypat.entity.Record;
import com.ypat.entity.User;
import com.ypat.enums.RecordType;
import com.ypat.enums.YesNo;
import com.ypat.repository.CheckinRecordRepository;
import com.ypat.repository.CheckinRuleRepository;
import com.ypat.repository.RecordRepository;
import com.ypat.repository.UserRepository;
import com.ypat.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class CheckinService {
    private static final ZoneId CHECKIN_ZONE = ZoneId.of("Asia/Shanghai");
    private static final String DEFAULT_TITLE = "每日签到";
    private static final int DEFAULT_REWARD = 1;

    private final CheckinRuleRepository ruleRepository;
    private final CheckinRecordRepository checkinRecordRepository;
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    @Autowired
    public CheckinService(CheckinRuleRepository ruleRepository,
                          CheckinRecordRepository checkinRecordRepository,
                          RecordRepository recordRepository,
                          UserRepository userRepository) {
        this.ruleRepository = ruleRepository;
        this.checkinRecordRepository = checkinRecordRepository;
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    public CheckinTodayQo today(Long userId) {
        CheckinRule rule = getOrDefaultRule();
        String today = todayText();
        CheckinRecord record = userId == null ? null : checkinRecordRepository.findByUseridAndCheckinDate(userId, today);
        CheckinTodayQo qo = new CheckinTodayQo();
        qo.setEnabled(YesNo.yes.value.equals(rule.getEnabled()));
        qo.setCheckedIn(record != null);
        qo.setRewardPpd(normalizeReward(rule.getRewardPpd()));
        qo.setConfirmTitle(rule.getConfirmTitle());
        qo.setConfirmContent(rule.getConfirmContent());
        qo.setCheckinDate(today);
        return qo;
    }

    public CheckinRuleQo getRule() {
        return CopyUtil.copy(getOrDefaultRule(), CheckinRuleQo.class);
    }

    public CheckinRuleQo saveRule(CheckinRuleQo qo) {
        validateRule(qo);
        CheckinRule rule = getOrDefaultRule();
        Date now = new Date();
        rule.setEnabled(qo.getEnabled());
        rule.setRewardPpd(qo.getRewardPpd());
        rule.setConfirmTitle(qo.getConfirmTitle());
        rule.setConfirmContent(qo.getConfirmContent());
        if (rule.getCreatedAt() == null) rule.setCreatedAt(now);
        rule.setUpdatedAt(now);
        return CopyUtil.copy(ruleRepository.save(rule), CheckinRuleQo.class);
    }

    public CheckinResultQo doCheckin(Long userId) {
        if (userId == null) throw new SysException(ResponseCode.FAIL_PARA);
        CheckinRule rule = getOrDefaultRule();
        if (!YesNo.yes.value.equals(rule.getEnabled())) {
            return result(false, 0, null, null, "签到活动暂未开启");
        }
        String today = todayText();
        CheckinRecord existing = checkinRecordRepository.findByUseridAndCheckinDate(userId, today);
        if (existing != null) {
            return result(true, 0, null, existing.getRecordId(), "今日已签到");
        }
        User user = userRepository.findById(userId);
        if (user == null) throw new SysException(ResponseCode.FAIL_NOT);
        int reward = normalizeReward(rule.getRewardPpd());
        try {
            CheckinRecord checkinRecord = new CheckinRecord();
            checkinRecord.setUserid(userId);
            checkinRecord.setCheckinDate(today);
            checkinRecord.setRewardPpd(reward);
            checkinRecord.setCreatedAt(new Date());
            checkinRecord = checkinRecordRepository.save(checkinRecord);

            Record record = new Record();
            record.setCredate(new Date());
            record.setPpd(reward);
            record.setUserid(userId);
            record.setType(RecordType.CHECKIN.value);
            record = recordRepository.save(record);

            user.setPpd((user.getPpd() == null ? 0 : user.getPpd()) + reward);
            userRepository.save(user);

            checkinRecord.setRecordId(record.getId());
            checkinRecordRepository.save(checkinRecord);
            return result(true, reward, user.getPpd(), record.getId(), "签到成功");
        } catch (DataIntegrityViolationException ex) {
            return result(true, 0, user.getPpd(), null, "今日已签到");
        }
    }

    public Map<String, Object> findRecords(CheckinRecordQo queryQo) {
        if (queryQo == null) queryQo = new CheckinRecordQo();
        int page = queryQo.getPage() == null || queryQo.getPage() < 0 ? 0 : queryQo.getPage();
        int size = queryQo.getSize() == null || queryQo.getSize() <= 0 ? 10 : queryQo.getSize();
        Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "id"));
        CheckinRecordQo finalQueryQo = queryQo;
        Page<CheckinRecord> recordPage = checkinRecordRepository.findAll((Specification<CheckinRecord>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (finalQueryQo.getUserid() != null) predicates.add(cb.equal(root.get("userid"), finalQueryQo.getUserid()));
            if (!StringUtils.isEmpty(finalQueryQo.getDateFrom())) predicates.add(cb.greaterThanOrEqualTo(root.get("checkinDate"), finalQueryQo.getDateFrom()));
            if (!StringUtils.isEmpty(finalQueryQo.getDateTo())) predicates.add(cb.lessThanOrEqualTo(root.get("checkinDate"), finalQueryQo.getDateTo()));
            if (!StringUtils.isEmpty(finalQueryQo.getMobile())) {
                Join<CheckinRecord, User> userJoin = root.join("userid", JoinType.LEFT);
                predicates.add(cb.equal(userJoin.get("mobile"), finalQueryQo.getMobile()));
            }
            query.where(predicates.toArray(new Predicate[predicates.size()]));
            return query.getRestriction();
        }, pageable);

        List<CheckinRecordQo> content = new ArrayList<>();
        for (CheckinRecord item : recordPage.getContent()) {
            CheckinRecordQo qo = CopyUtil.copy(item, CheckinRecordQo.class);
            User user = userRepository.findById(item.getUserid());
            if (user != null) {
                qo.setMobile(user.getMobile());
                qo.setNickname(user.getNickname());
            }
            content.add(qo);
        }
        Map<String, Object> pageMap = new HashMap<>();
        pageMap.put("content", content);
        pageMap.put("totalElements", recordPage.getTotalElements());
        pageMap.put("totalPages", recordPage.getTotalPages());
        pageMap.put("number", page);
        pageMap.put("size", size);
        return pageMap;
    }

    private CheckinRule getOrDefaultRule() {
        CheckinRule rule = ruleRepository.findTopByOrderByIdAsc();
        if (rule != null) return rule;
        rule = new CheckinRule();
        rule.setEnabled(YesNo.yes.value);
        rule.setRewardPpd(DEFAULT_REWARD);
        rule.setConfirmTitle(DEFAULT_TITLE);
        rule.setConfirmContent("签到成功可获得 1 拍豆");
        Date now = new Date();
        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);
        return rule;
    }

    private void validateRule(CheckinRuleQo qo) {
        if (qo == null || StringUtils.isEmpty(qo.getEnabled()) || qo.getRewardPpd() == null
                || qo.getRewardPpd() < 0 || StringUtils.isEmpty(qo.getConfirmTitle())
                || StringUtils.isEmpty(qo.getConfirmContent())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (qo.getConfirmTitle().length() > 64 || qo.getConfirmContent().length() > 256) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
    }

    private CheckinResultQo result(Boolean checkedIn, Integer rewardPpd, Integer currentPpd, Long recordId, String message) {
        CheckinResultQo qo = new CheckinResultQo();
        qo.setCheckedIn(checkedIn);
        qo.setRewardPpd(rewardPpd);
        qo.setCurrentPpd(currentPpd);
        qo.setRecordId(recordId);
        qo.setMessage(message);
        return qo;
    }

    private int normalizeReward(Integer rewardPpd) {
        return rewardPpd == null ? DEFAULT_REWARD : Math.max(0, rewardPpd);
    }

    private String todayText() {
        return LocalDate.now(CHECKIN_ZONE).toString();
    }
}
```

- [ ] **Step 4: 修正 `findRecords` 的手机号查询**

The `root.join("userid")` in Step 3 will not work because `CheckinRecord.userid` is a scalar field. Replace the `findRecords` mobile-filter block with a subquery:

```java
if (!StringUtils.isEmpty(finalQueryQo.getMobile())) {
    javax.persistence.criteria.Subquery<Long> userSubquery = query.subquery(Long.class);
    javax.persistence.criteria.Root<User> userRoot = userSubquery.from(User.class);
    userSubquery.select(userRoot.get("id"));
    userSubquery.where(cb.equal(userRoot.get("mobile"), finalQueryQo.getMobile()));
    predicates.add(root.get("userid").in(userSubquery));
}
```

Also remove unused imports:

```java
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
```

- [ ] **Step 5: 运行测试确认通过**

Run:

```bash
mvn -pl backend/system-domain -Dtest=CheckinServiceTest test
```

Expected: PASS。

- [ ] **Step 6: Commit**

```bash
git add backend/system-domain/src/main/java/com/ypat/service/CheckinService.java \
  backend/system-domain/src/test/java/com/ypat/service/CheckinServiceTest.java
git commit -m "feat: add checkin service"
```

---

### Task 4: 后端内部接口、小程序接口和后台接口

**Files:**
- Create: `backend/system-restapi/src/main/java/com/ypat/controller/CheckinController.java`
- Create: `backend/system-wap/src/main/java/com/ypat/service/CheckinServiceClient.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/CheckinController.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/AdminCheckinController.java`

- [ ] **Step 1: 新增内部接口 Controller**

Create `backend/system-restapi/src/main/java/com/ypat/controller/CheckinController.java`:

```java
package com.ypat.controller;

import com.ypat.CheckinRecordQo;
import com.ypat.CheckinResultQo;
import com.ypat.CheckinRuleQo;
import com.ypat.CheckinTodayQo;
import com.ypat.service.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CheckinController {
    @Autowired
    private CheckinService checkinService;

    @GetMapping("/service/checkin/today")
    public CheckinTodayQo today(@RequestParam("userId") Long userId) {
        return checkinService.today(userId);
    }

    @PostMapping("/service/checkin/do")
    public CheckinResultQo doCheckin(@RequestParam("userId") Long userId) {
        return checkinService.doCheckin(userId);
    }

    @GetMapping("/service/checkin/rule")
    public CheckinRuleQo rule() {
        return checkinService.getRule();
    }

    @PostMapping("/service/checkin/rule/save")
    public CheckinRuleQo saveRule(@RequestBody CheckinRuleQo qo) {
        return checkinService.saveRule(qo);
    }

    @PostMapping("/service/checkin/records")
    public Map<String, Object> records(@RequestBody CheckinRecordQo qo) {
        return checkinService.findRecords(qo);
    }
}
```

- [ ] **Step 2: 新增 Feign 客户端**

Create `backend/system-wap/src/main/java/com/ypat/service/CheckinServiceClient.java`:

```java
package com.ypat.service;

import com.ypat.CheckinRecordQo;
import com.ypat.CheckinResultQo;
import com.ypat.CheckinRuleQo;
import com.ypat.CheckinTodayQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("SYSTEM-API")
public interface CheckinServiceClient {
    @GetMapping("/service/checkin/today")
    CheckinTodayQo today(@RequestParam("userId") Long userId);

    @PostMapping("/service/checkin/do")
    CheckinResultQo doCheckin(@RequestParam("userId") Long userId);

    @GetMapping("/service/checkin/rule")
    CheckinRuleQo rule();

    @PostMapping("/service/checkin/rule/save")
    CheckinRuleQo saveRule(@RequestBody CheckinRuleQo qo);

    @PostMapping("/service/checkin/records")
    Map<String, Object> records(@RequestBody CheckinRecordQo qo);
}
```

- [ ] **Step 3: 新增小程序接口**

Create `backend/system-wap/src/main/java/com/ypat/controller/CheckinController.java`:

```java
package com.ypat.controller;

import com.ypat.CheckinResultQo;
import com.ypat.CheckinTodayQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.CheckinServiceClient;
import com.ypat.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckinController {
    @Autowired
    private CheckinServiceClient checkinServiceClient;

    @GetMapping("/checkin/today")
    public ResponseApiBody today() {
        Long userId = currentUserId();
        CheckinTodayQo qo = checkinServiceClient.today(userId);
        return ResponseApiBody.success(qo);
    }

    @PostMapping("/checkin/do")
    public ResponseApiBody doCheckin() {
        Long userId = currentUserId();
        CheckinResultQo qo = checkinServiceClient.doCheckin(userId);
        return ResponseApiBody.success(qo);
    }

    private Long currentUserId() {
        String raw = UserUtil.getUserId();
        if (StringUtils.isBlank(raw)) throw new SysException(ResponseCode.FAIL_AUTH);
        return Long.parseLong(raw);
    }
}
```

- [ ] **Step 4: 新增后台管理接口**

Create `backend/system-wap/src/main/java/com/ypat/controller/AdminCheckinController.java`:

```java
package com.ypat.controller;

import com.ypat.CheckinRecordQo;
import com.ypat.CheckinRuleQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.CheckinServiceClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/checkin")
public class AdminCheckinController {
    @Autowired
    private CheckinServiceClient checkinServiceClient;

    @GetMapping("/rule")
    public ResponseApiBody rule() {
        return ResponseApiBody.success(checkinServiceClient.rule());
    }

    @PutMapping("/rule")
    public ResponseApiBody saveRule(@RequestBody CheckinRuleQo qo) {
        if (qo == null || StringUtils.isBlank(qo.getEnabled()) || qo.getRewardPpd() == null
                || qo.getRewardPpd() < 0 || StringUtils.isBlank(qo.getConfirmTitle())
                || StringUtils.isBlank(qo.getConfirmContent())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        return ResponseApiBody.success(checkinServiceClient.saveRule(qo));
    }

    @GetMapping("/records")
    public ResponseApiBody records(CheckinRecordQo qo) {
        return ResponseApiBody.success(checkinServiceClient.records(qo));
    }
}
```

- [ ] **Step 5: 运行轻量校验**

Run:

```bash
rg -n "/checkin/today|/checkin/do|/admin/checkin/rule|/admin/checkin/records|/service/checkin" backend/system-restapi/src/main/java backend/system-wap/src/main/java
```

Expected: 以上路径都能命中。

- [ ] **Step 6: Commit**

```bash
git add backend/system-restapi/src/main/java/com/ypat/controller/CheckinController.java \
  backend/system-wap/src/main/java/com/ypat/service/CheckinServiceClient.java \
  backend/system-wap/src/main/java/com/ypat/controller/CheckinController.java \
  backend/system-wap/src/main/java/com/ypat/controller/AdminCheckinController.java
git commit -m "feat: expose checkin APIs"
```

---

### Task 5: 小程序 API、枚举和 icon

**Files:**
- Modify: `frontend/src/api/types/index.ts`
- Create: `frontend/src/api/modules/checkin.ts`
- Modify: `frontend/src/constants/enums.ts`
- Modify: `frontend/src/components/business/KeepIcon.vue`

- [ ] **Step 1: 新增小程序类型**

Append to `frontend/src/api/types/index.ts` near record/payment types:

```ts
export interface CheckinToday {
  enabled: boolean
  checkedIn: boolean
  rewardPpd: number
  confirmTitle: string
  confirmContent: string
  checkinDate: string
}

export interface CheckinResult {
  checkedIn: boolean
  rewardPpd: number
  currentPpd?: number
  recordId?: number
  message?: string
}
```

- [ ] **Step 2: 新增小程序 API 模块**

Create `frontend/src/api/modules/checkin.ts`:

```ts
import { get, post } from '../request'
import type { ApiResult, CheckinResult, CheckinToday } from '../types'

export function getCheckinToday(): Promise<ApiResult<CheckinToday>> {
  return get('/checkin/today')
}

export function doCheckin(): Promise<ApiResult<CheckinResult>> {
  return post('/checkin/do')
}
```

- [ ] **Step 3: 新增前端拍豆记录类型**

Modify `frontend/src/constants/enums.ts`:

```ts
export const RecordType = {
  TOPUP: '0',
  INVITE: '1',
  SYSTEM: '2',
  PUBLISH: '3',
  APPLY: '4',
  VIEW_CONTACT: '5',
  CHECKIN: '6',
} as const;
```

And:

```ts
export const RECORD_TYPE_LABELS: Record<string, string> = {
  [RecordType.TOPUP]: '充值',
  [RecordType.INVITE]: '邀请奖励',
  [RecordType.SYSTEM]: '系统赠送',
  [RecordType.PUBLISH]: '发布约拍',
  [RecordType.APPLY]: '报名约拍',
  [RecordType.VIEW_CONTACT]: '查看联系方式',
  [RecordType.CHECKIN]: '每日签到',
};
```

- [ ] **Step 4: 给 `KeepIcon` 增加签到 icon**

Add to `iconMap` in `frontend/src/components/business/KeepIcon.vue`:

```ts
  'calendar-check': [
    { tag: 'path', attrs: { d: 'M8 2v4' } },
    { tag: 'path', attrs: { d: 'M16 2v4' } },
    { tag: 'rect', attrs: { width: 18, height: 18, x: 3, y: 4, rx: 2 } },
    { tag: 'path', attrs: { d: 'M3 10h18' } },
    { tag: 'path', attrs: { d: 'm9 16 2 2 4-4' } },
  ],
```

- [ ] **Step 5: 运行轻量校验**

Run:

```bash
rg -n "CheckinToday|doCheckin|calendar-check|RecordType.CHECKIN|每日签到" frontend/src/api frontend/src/constants frontend/src/components/business/KeepIcon.vue
```

Expected: 类型、API(接口)、枚举和 icon(图标)均命中。

- [ ] **Step 6: Commit**

```bash
git add frontend/src/api/types/index.ts \
  frontend/src/api/modules/checkin.ts \
  frontend/src/constants/enums.ts \
  frontend/src/components/business/KeepIcon.vue
git commit -m "feat: add miniapp checkin API"
```

---

### Task 6: 小程序我的页签到入口

**Files:**
- Modify: `frontend/src/pages/mine/index.vue`
- Modify: `frontend/src/pages/mine/index.test.ts`

- [ ] **Step 1: 写源码契约测试**

Append tests to `frontend/src/pages/mine/index.test.ts`:

```ts
it('renders checkin entry beside the menu icon', () => {
  expect(source).toContain('mine-top__left')
  expect(source).toContain('mine-top__checkin')
  expect(source).toContain('calendar-check')
})

it('loads checkin status and executes checkin through confirm modal', () => {
  expect(source).toContain('getCheckinToday')
  expect(source).toContain('doCheckin')
  expect(source).toContain('uni.showModal')
  expect(source).toContain('checkinSubmitting')
  expect(source).toContain('签到成功，获得')
})
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd frontend && ./node_modules/.bin/vitest run src/pages/mine/index.test.ts
```

Expected: FAIL，因为页面还没有签到入口和 API(接口)调用。

- [ ] **Step 3: 修改模板**

In `frontend/src/pages/mine/index.vue`, replace:

```vue
<view class="mine-top">
  <view class="mine-top__icon" @tap="goCenter">
    <KeepIcon name="menu" :size="42" />
  </view>
```

with:

```vue
<view class="mine-top">
  <view class="mine-top__left">
    <view class="mine-top__icon" @tap="goCenter">
      <KeepIcon name="menu" :size="42" />
    </view>
    <view
      v-if="showCheckinEntry"
      class="mine-top__checkin"
      :class="{ 'mine-top__checkin--done': checkinToday?.checkedIn }"
      @tap="openCheckinConfirm"
    >
      <KeepIcon name="calendar-check" :size="34" :color="checkinToday?.checkedIn ? '#9CA3AF' : '#17A857'" />
      <text v-if="checkinToday?.checkedIn" class="mine-top__checkin-text">已签到</text>
    </view>
  </view>
```

- [ ] **Step 4: 修改 script**

In `frontend/src/pages/mine/index.vue`, add import:

```ts
import * as checkinApi from '@/api/modules/checkin'
import type { CheckinToday } from '@/api/types'
```

Add state after `activeTab`:

```ts
const checkinToday = ref<CheckinToday | null>(null)
const checkinSubmitting = ref(false)
```

Add computed:

```ts
const showCheckinEntry = computed(() => isLoggedIn.value && checkinToday.value?.enabled !== false)
```

Update `loadMineData` Promise list:

```ts
await Promise.all([
  userStore.updateUserInfo(),
  userStore.refreshUnreadCount(),
  memberStore.refreshStatus(),
  loadReceivedCount(),
  loadCheckinToday(),
])
```

Add functions:

```ts
async function loadCheckinToday(): Promise<void> {
  if (!isLoggedIn.value) {
    checkinToday.value = null
    return
  }
  try {
    const result = await checkinApi.getCheckinToday()
    checkinToday.value = result.data || null
  } catch {
    checkinToday.value = null
  }
}

function openCheckinConfirm(): void {
  if (!requireLogin()) return
  if (!checkinToday.value?.enabled) return
  if (checkinToday.value.checkedIn) {
    uni.showToast({ title: '今日已签到', icon: 'none' })
    return
  }
  uni.showModal({
    title: checkinToday.value.confirmTitle || '每日签到',
    content: checkinToday.value.confirmContent || `签到成功可获得 ${checkinToday.value.rewardPpd || 1} 拍豆`,
    confirmText: '签到',
    success: (res) => {
      if (res.confirm) void submitCheckin()
    },
  })
}

async function submitCheckin(): Promise<void> {
  if (checkinSubmitting.value) return
  checkinSubmitting.value = true
  try {
    const result = await checkinApi.doCheckin()
    if (result.data?.checkedIn) {
      checkinToday.value = {
        ...(checkinToday.value || {
          enabled: true,
          rewardPpd: result.data.rewardPpd || 1,
          confirmTitle: '每日签到',
          confirmContent: '签到成功可获得 1 拍豆',
          checkinDate: '',
        }),
        checkedIn: true,
      }
      await userStore.updateUserInfo()
      const reward = Number(result.data.rewardPpd || 0)
      uni.showToast({ title: reward > 0 ? `签到成功，获得 ${reward} 拍豆` : '今日已签到', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '签到失败，请稍后重试', icon: 'none' })
  } finally {
    checkinSubmitting.value = false
  }
}
```

- [ ] **Step 5: 修改样式**

Add styles near `.mine-top`:

```scss
.mine-top__left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.mine-top__checkin {
  @include flex-center;
  gap: 8rpx;
  min-width: 76rpx;
  height: 76rpx;
  padding: 0 20rpx;
  border-radius: $radius-round;
  color: $color-primary;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.mine-top__checkin--done {
  color: $color-text-tertiary;
  background: #f4f5f6;
}

.mine-top__checkin-text {
  color: $color-text-tertiary;
  font-size: 24rpx;
  font-weight: 800;
  white-space: nowrap;
}
```

- [ ] **Step 6: 运行测试确认通过**

Run:

```bash
cd frontend && ./node_modules/.bin/vitest run src/pages/mine/index.test.ts
```

Expected: PASS。

- [ ] **Step 7: Commit**

```bash
git add frontend/src/pages/mine/index.vue frontend/src/pages/mine/index.test.ts
git commit -m "feat: add mine checkin entry"
```

---

### Task 7: 小程序钱包页签到任务和记录文案

**Files:**
- Modify: `frontend/src/pages-sub/user/wallet.vue`
- Modify: `frontend/src/pages-sub/user/wallet.test.ts`

- [ ] **Step 1: 更新钱包页测试**

Modify existing wallet tests so “每日签到” no longer expects static `done: true`. Add:

```ts
it('uses real checkin status for daily checkin task', () => {
  expect(source).toContain('getCheckinToday')
  expect(source).toContain('doCheckin')
  expect(source).toContain('checkinToday')
  expect(source).toContain('去签到')
  expect(source).not.toContain("title: '每日签到',\n        reward: 1,\n        desc: '每日签到获得拍豆',\n        actionText: '已签到',\n        doneText: '已签到',\n        done: true")
})
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd frontend && ./node_modules/.bin/vitest run src/pages-sub/user/wallet.test.ts
```

Expected: FAIL，因为钱包页还没有真实签到状态。

- [ ] **Step 3: 修改钱包页 script**

In `frontend/src/pages-sub/user/wallet.vue`, add:

```ts
import * as checkinApi from '@/api/modules/checkin'
import type { CheckinToday } from '@/api/types'
```

Add state:

```ts
const checkinToday = ref<CheckinToday | null>(null)
const checkinSubmitting = ref(false)
```

Replace daily checkin item in `earnGroups` with:

```ts
{
  title: '每日签到',
  reward: checkinToday.value?.rewardPpd || 1,
  desc: '每日签到获得拍豆',
  actionText: '去签到',
  doneText: '已签到',
  done: Boolean(checkinToday.value?.checkedIn),
  action: openCheckinConfirm,
},
```

Add functions:

```ts
async function loadCheckinToday(): Promise<void> {
  try {
    const result = await checkinApi.getCheckinToday()
    checkinToday.value = result.data || null
  } catch {
    checkinToday.value = null
  }
}

function openCheckinConfirm(): void {
  if (!checkinToday.value?.enabled) {
    uni.showToast({ title: '签到活动暂未开启', icon: 'none' })
    return
  }
  if (checkinToday.value.checkedIn) {
    uni.showToast({ title: '今日已签到', icon: 'none' })
    return
  }
  uni.showModal({
    title: checkinToday.value.confirmTitle || '每日签到',
    content: checkinToday.value.confirmContent || `签到成功可获得 ${checkinToday.value.rewardPpd || 1} 拍豆`,
    confirmText: '签到',
    success: (res) => {
      if (res.confirm) void submitCheckin()
    },
  })
}

async function submitCheckin(): Promise<void> {
  if (checkinSubmitting.value) return
  checkinSubmitting.value = true
  try {
    const result = await checkinApi.doCheckin()
    if (result.data?.checkedIn) {
      checkinToday.value = {
        ...(checkinToday.value || {
          enabled: true,
          rewardPpd: result.data.rewardPpd || 1,
          confirmTitle: '每日签到',
          confirmContent: '签到成功可获得 1 拍豆',
          checkinDate: '',
        }),
        checkedIn: true,
      }
      await refreshWalletData()
      const reward = Number(result.data.rewardPpd || 0)
      uni.showToast({ title: reward > 0 ? `签到成功，获得 ${reward} 拍豆` : '今日已签到', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '签到失败，请稍后重试', icon: 'none' })
  } finally {
    checkinSubmitting.value = false
  }
}
```

Update the page lifecycle from:

```ts
onLoad(() => {
  void refreshWalletData(true)
})
onShow(() => {
  if (walletLoaded.value) void refreshWalletData()
})
```

to:

```ts
onLoad(() => {
  void refreshWalletData(true)
  void loadCheckinToday()
})
onShow(() => {
  if (walletLoaded.value) void refreshWalletData()
  void loadCheckinToday()
})
```

- [ ] **Step 4: 确认记录页文案自动生效**

Run:

```bash
rg -n "RECORD_TYPE_LABELS|RecordType.CHECKIN|每日签到" frontend/src/pages-sub/user/wallet.vue frontend/src/pages-sub/user/records.vue frontend/src/constants/enums.ts
```

Expected: `wallet.vue` and constants 命中；`records.vue` 使用 `RECORD_TYPE_LABELS` 即可。

- [ ] **Step 5: 运行测试确认通过**

Run:

```bash
cd frontend && ./node_modules/.bin/vitest run src/pages-sub/user/wallet.test.ts
```

Expected: PASS。

- [ ] **Step 6: Commit**

```bash
git add frontend/src/pages-sub/user/wallet.vue frontend/src/pages-sub/user/wallet.test.ts
git commit -m "feat: wire wallet checkin task"
```

---

### Task 8: 后台签到 API 类型和模块

**Files:**
- Modify: `frontend-admin/src/api/types.ts`
- Create: `frontend-admin/src/api/modules/checkin.ts`
- Create: `frontend-admin/tests/unit/admin-checkin-api.test.ts`

- [ ] **Step 1: 写 API 源码契约测试**

Create `frontend-admin/tests/unit/admin-checkin-api.test.ts`:

```ts
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

describe('admin checkin api source contract', () => {
  const source = readFileSync(resolve(__dirname, '../../src/api/modules/checkin.ts'), 'utf-8')
  const types = readFileSync(resolve(__dirname, '../../src/api/types.ts'), 'utf-8')

  it('defines checkin admin endpoints', () => {
    expect(source).toContain('/admin/checkin/rule')
    expect(source).toContain('/admin/checkin/records')
    expect(source).toContain('getCheckinRule')
    expect(source).toContain('saveCheckinRule')
    expect(source).toContain('getCheckinRecords')
  })

  it('defines checkin rule and record types', () => {
    expect(types).toContain('interface CheckinRule')
    expect(types).toContain('interface CheckinRecord')
    expect(types).toContain('rewardPpd')
    expect(types).toContain('checkinDate')
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd frontend-admin && ./node_modules/.bin/vitest run tests/unit/admin-checkin-api.test.ts
```

Expected: FAIL，因为 API 文件和类型还不存在。

- [ ] **Step 3: 新增后台类型**

Append to `frontend-admin/src/api/types.ts`:

```ts
export interface CheckinRule {
  id?: number
  enabled: string
  rewardPpd: number
  confirmTitle: string
  confirmContent: string
  createdAt?: string
  updatedAt?: string
}

export interface CheckinRecord {
  id: number
  userid: number
  mobile?: string
  nickname?: string
  checkinDate: string
  rewardPpd: number
  recordId?: number
  createdAt?: string
}
```

- [ ] **Step 4: 新增后台 API 模块**

Create `frontend-admin/src/api/modules/checkin.ts`:

```ts
import { get, put } from '../request'
import type { ApiResult, CheckinRecord, CheckinRule, PageQuery, PageResult } from '../types'

export interface CheckinRecordQuery extends PageQuery {
  userid?: number
  mobile?: string
  dateFrom?: string
  dateTo?: string
}

export function getCheckinRule(): Promise<ApiResult<CheckinRule>> {
  return get<CheckinRule>('/admin/checkin/rule')
}

export function saveCheckinRule(data: CheckinRule): Promise<ApiResult<CheckinRule>> {
  return put<CheckinRule>('/admin/checkin/rule', data)
}

export function getCheckinRecords(
  params: CheckinRecordQuery,
): Promise<ApiResult<PageResult<CheckinRecord>>> {
  return get<PageResult<CheckinRecord>>('/admin/checkin/records', params as Record<string, unknown>)
}
```

- [ ] **Step 5: 运行测试确认通过**

Run:

```bash
cd frontend-admin && ./node_modules/.bin/vitest run tests/unit/admin-checkin-api.test.ts
```

Expected: PASS。

- [ ] **Step 6: Commit**

```bash
git add frontend-admin/src/api/types.ts \
  frontend-admin/src/api/modules/checkin.ts \
  frontend-admin/tests/unit/admin-checkin-api.test.ts
git commit -m "feat: add admin checkin API"
```

---

### Task 9: 后台签到管理页面和菜单

**Files:**
- Create: `frontend-admin/src/views/checkin/index.vue`
- Modify: `frontend-admin/src/constants/menu.ts`
- Modify: `frontend-admin/src/stores/modules/permission.ts`
- Create: `frontend-admin/tests/unit/admin-checkin-page.test.ts`

- [ ] **Step 1: 写页面源码契约测试**

Create `frontend-admin/tests/unit/admin-checkin-page.test.ts`:

```ts
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

describe('admin checkin page source contract', () => {
  const page = readFileSync(resolve(__dirname, '../../src/views/checkin/index.vue'), 'utf-8')
  const menu = readFileSync(resolve(__dirname, '../../src/constants/menu.ts'), 'utf-8')
  const permission = readFileSync(resolve(__dirname, '../../src/stores/modules/permission.ts'), 'utf-8')

  it('contains rule form and record table', () => {
    expect(page).toContain('签到规则')
    expect(page).toContain('签到记录')
    expect(page).toContain('rewardPpd')
    expect(page).toContain('confirmTitle')
    expect(page).toContain('getCheckinRecords')
  })

  it('registers checkin menu and route component', () => {
    expect(menu).toContain('签到管理')
    expect(menu).toContain('checkin/index')
    expect(permission).toContain('CheckinIndex')
    expect(permission).toContain("'checkin/index': CheckinIndex")
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd frontend-admin && ./node_modules/.bin/vitest run tests/unit/admin-checkin-page.test.ts
```

Expected: FAIL，因为页面和菜单未接入。

- [ ] **Step 3: 新增后台页面**

Create `frontend-admin/src/views/checkin/index.vue`:

```vue
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCheckinRecords, getCheckinRule, saveCheckinRule, type CheckinRecordQuery } from '@/api/modules/checkin'
import type { CheckinRecord, CheckinRule } from '@/api/types'

const ruleLoading = ref(false)
const recordsLoading = ref(false)
const saving = ref(false)
const records = ref<CheckinRecord[]>([])
const total = ref(0)
const currentPage = computed(() => (query.page ?? 0) + 1)

const rule = reactive<CheckinRule>({
  enabled: '1',
  rewardPpd: 1,
  confirmTitle: '每日签到',
  confirmContent: '签到成功可获得 1 拍豆',
})

const query = reactive<CheckinRecordQuery>({
  userid: undefined,
  mobile: '',
  dateFrom: '',
  dateTo: '',
  page: 0,
  size: 10,
})

async function loadRule() {
  ruleLoading.value = true
  try {
    const res = await getCheckinRule()
    Object.assign(rule, res.data || {})
  } finally {
    ruleLoading.value = false
  }
}

async function submitRule() {
  if (rule.rewardPpd < 0) {
    ElMessage.warning('奖励拍豆数不能小于 0')
    return
  }
  saving.value = true
  try {
    const res = await saveCheckinRule(rule)
    Object.assign(rule, res.data || {})
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

async function fetchRecords() {
  recordsLoading.value = true
  try {
    const res = await getCheckinRecords(query)
    records.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally {
    recordsLoading.value = false
  }
}

function search() {
  query.page = 0
  fetchRecords()
}

function reset() {
  query.userid = undefined
  query.mobile = ''
  query.dateFrom = ''
  query.dateTo = ''
  query.page = 0
  fetchRecords()
}

function pageChange(page: number) {
  query.page = page - 1
  fetchRecords()
}

function sizeChange(size: number) {
  query.size = size
  query.page = 0
  fetchRecords()
}

onMounted(() => {
  loadRule()
  fetchRecords()
})
</script>

<template>
  <div class="checkin-page">
    <el-card class="checkin-card" shadow="never">
      <template #header><span>签到规则</span></template>
      <el-form v-loading="ruleLoading" :model="rule" label-width="120px" @submit.prevent>
        <el-form-item label="是否启用">
          <el-switch v-model="rule.enabled" active-value="1" inactive-value="0" />
        </el-form-item>
        <el-form-item label="每日奖励拍豆">
          <el-input-number v-model="rule.rewardPpd" :min="0" />
        </el-form-item>
        <el-form-item label="弹窗标题">
          <el-input v-model="rule.confirmTitle" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="弹窗内容">
          <el-input v-model="rule.confirmContent" type="textarea" maxlength="256" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submitRule">保存规则</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="checkin-card" shadow="never">
      <template #header><span>签到记录</span></template>
      <div class="search-bar">
        <el-form :inline="true" :model="query" @submit.prevent>
          <el-form-item label="用户ID"><el-input-number v-model="query.userid" :min="1" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="query.mobile" clearable placeholder="请输入手机号" /></el-form-item>
          <el-form-item label="开始日期"><el-date-picker v-model="query.dateFrom" value-format="YYYY-MM-DD" type="date" /></el-form-item>
          <el-form-item label="结束日期"><el-date-picker v-model="query.dateTo" value-format="YYYY-MM-DD" type="date" /></el-form-item>
          <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
        </el-form>
      </div>
      <el-table v-loading="recordsLoading" :data="records" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="userid" label="用户ID" width="100" align="center" />
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column prop="mobile" label="手机号" width="140" />
        <el-table-column prop="checkinDate" label="签到日期" width="130" align="center" />
        <el-table-column prop="rewardPpd" label="奖励拍豆" width="120" align="center" />
        <el-table-column prop="recordId" label="流水ID" width="120" align="center" />
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center" />
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          :current-page="currentPage"
          :page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total,sizes,prev,pager,next,jumper"
          background
          @current-change="pageChange"
          @size-change="sizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.checkin-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.checkin-card {
  border-radius: $border-radius-base;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}
</style>
```

- [ ] **Step 4: 接入菜单和动态路由**

Modify `frontend-admin/src/constants/menu.ts`, in 查询系统 children add:

```ts
{ title: '签到管理', path: '/checkin/index', component: 'checkin/index' },
```

Modify `frontend-admin/src/stores/modules/permission.ts`:

```ts
import CheckinIndex from '@/views/checkin/index.vue'
```

Add to `viewModules`:

```ts
  'checkin/index': CheckinIndex,
```

- [ ] **Step 5: 运行测试确认通过**

Run:

```bash
cd frontend-admin && ./node_modules/.bin/vitest run tests/unit/admin-checkin-page.test.ts
```

Expected: PASS。

- [ ] **Step 6: Commit**

```bash
git add frontend-admin/src/views/checkin/index.vue \
  frontend-admin/src/constants/menu.ts \
  frontend-admin/src/stores/modules/permission.ts \
  frontend-admin/tests/unit/admin-checkin-page.test.ts
git commit -m "feat: add admin checkin page"
```

---

### Task 10: 端到端轻量验收

**Files:**
- No source changes expected.

- [ ] **Step 1: 后端关键字符串检查**

Run:

```bash
rg -n "CHECKIN|每日签到|t_checkin_rule|t_checkin_record|Asia/Shanghai|/service/checkin|/admin/checkin|/checkin/do" backend docs/sql
```

Expected: 后端枚举、SQL(数据库脚本)、服务、接口均命中。

- [ ] **Step 2: 小程序关键字符串检查**

Run:

```bash
rg -n "calendar-check|mine-top__checkin|getCheckinToday|doCheckin|RecordType.CHECKIN|签到成功，获得|今日已签到" frontend/src
```

Expected: 我的页、钱包页、API(接口)、枚举均命中。

- [ ] **Step 3: 后台关键字符串检查**

Run:

```bash
rg -n "签到管理|checkin/index|getCheckinRule|saveCheckinRule|getCheckinRecords|/admin/checkin" frontend-admin/src frontend-admin/tests
```

Expected: 菜单、路由、页面、API(接口)、测试均命中。

- [ ] **Step 4: 定向测试**

Run only targeted tests; do not install dependencies or run full builds:

```bash
cd frontend && ./node_modules/.bin/vitest run src/pages/mine/index.test.ts src/pages-sub/user/wallet.test.ts
cd ../frontend-admin && ./node_modules/.bin/vitest run tests/unit/admin-checkin-api.test.ts tests/unit/admin-checkin-page.test.ts
cd .. && mvn -pl backend/system-domain -Dtest=CheckinServiceTest test
```

Expected: targeted tests PASS. If local dependencies are missing, stop and report missing dependency; do not download dependencies.

- [ ] **Step 5: Final status check**

Run:

```bash
git status --short
```

Expected: only unrelated pre-existing dirty files remain, or clean if all task files were committed.

## Self-Review Checklist

- Spec coverage: 小程序入口、confirm(确认弹窗)、拍豆奖励、拍豆流水、后台规则、后台记录、北京时间、重复签到幂等均有任务覆盖。
- Placeholder scan: 未发现禁用词或模糊实现步骤。
- Type consistency: 后端使用 `rewardPpd/checkinDate/recordId`，前端和后台使用相同 camelCase(小驼峰)字段。
- Scope check: 不包含连续签到、补签、撤销、通用任务系统。
