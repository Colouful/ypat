# Message Chain Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成新版小程序消息链路升级：记录站内消息和微信订阅推送事件，后台可查询推送记录，小程序补齐订阅授权、消息页视觉和未读刷新。

**Architecture:** 保留 `t_mess_info` 作为站内消息真源，新增 `t_message_push_log` 作为旁路审计表，微信订阅发送结果落库但不阻断主业务。前端继续使用 HTTP(超文本传输协议) 拉取，不引入 `WebSocket`(网页套接字)，通过关键触发点刷新未读数。

**Tech Stack:** Java Spring Boot(后端框架), Spring Data JPA(数据访问), Feign(服务间调用), Vue 3 + uni-app(小程序), Vue 3 + Element Plus(后台管理), Vitest(前端测试), Maven(后端测试)。

---

## 前置注意

当前开发目录是独立 `worktree`(工作树)：

```bash
/Users/lizhenwei/workspace/vueworkspace/ypat-workspace-worktrees/message-chain-upgrade
```

每个任务提交时只 stage(暂存)该任务列出的文件，不要使用 `git add .`。用户要求不下载依赖、不启动完整构建；实现阶段只运行改动相关的轻量测试、源码检查和格式检查。

推荐每个任务开始前运行：

```bash
git status --short
```

如果看到本计划之外的脏文件，保留它们，不要回滚。

## 文件结构

### 后端公共对象

- Create `backend/system-object/src/main/java/com/ypat/MessagePushLogQo.java`：推送日志查询和展示 Qo(数据传输对象)。
- Create `backend/system-object/src/main/java/com/ypat/enums/MessagePushEventType.java`：`IN_APP_CREATED`、`WECHAT_SUBSCRIBE_SENT`。

### 后端领域层

- Create `backend/system-domain/src/main/java/com/ypat/entity/MessagePushLog.java`：`t_message_push_log` 实体。
- Create `backend/system-domain/src/main/java/com/ypat/repository/MessagePushLogRepository.java`：推送日志仓储。
- Create `backend/system-domain/src/main/java/com/ypat/service/MessagePushLogService.java`：记录、查询、统计。
- Modify `backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java`：站内约拍申请消息创建后记录事件。
- Modify `backend/system-domain/src/main/java/com/ypat/service/UserService.java`：查看联系方式写 `view` 消息后记录事件。

### 后端接口层

- Modify `backend/system-restapi/src/main/java/com/ypat/controller/MessInfoController.java`：新增内部推送日志查询接口。
- Modify `backend/system-wap/src/main/java/com/ypat/service/MessServiceClient.java`：新增 Feign(服务间调用) 方法。
- Create `backend/system-wap/src/main/java/com/ypat/controller/AdminMessagePushLogController.java`：后台推送记录接口。
- Modify `backend/system-wap/src/main/java/com/ypat/controller/MypatInfoController.java`：申请约拍微信订阅发送结果落库。
- Modify `backend/system-wap/src/main/java/com/ypat/controller/YpatInfoController.java`：发布约拍推管理员结果落库。
- Modify `backend/system-wap/src/main/java/com/ypat/controller/OauthController.java`：实名认证提交推管理员结果落库。
- Modify `backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java`：约拍审核结果推用户结果落库。
- Modify `backend/system-web/src/main/java/com/ypat/controller/ManageController.java`：旧后台审核入口推送结果落库。

### 数据库脚本

- Create `backend/dev/mysql/20260709_create_message_push_log.sql`：开发库手工 SQL(数据库脚本)。

### 小程序前端

- Modify `frontend/src/api/types/index.ts`：模板 ID 类型、消息分项计数相关类型。
- Modify `frontend/src/api/modules/content.ts`：修正 `/tmplid/list` 返回类型。
- Create `frontend/src/utils/subscribe-message.ts`：订阅授权工具。
- Create `frontend/src/utils/__tests__/subscribe-message.test.ts`：订阅授权工具测试。
- Modify `frontend/src/pages-sub/work/apply.vue`：申请约拍前请求订阅授权，成功后刷新未读。
- Modify `frontend/src/components/business/AppointmentPublishForm.vue`：发布约拍最终提交前请求订阅授权。
- Modify `frontend/src/pages/message/index.vue`：视觉优化、提醒入口、分项未读和刷新策略。
- Modify `frontend/src/pages-sub/content/message-detail.vue`：查看联系方式成功后刷新未读并修正文案。
- Modify `frontend/src/components/business/KeepTabBar.vue`：消息红点接入 `userStore.unreadCount`。

### 后台管理前端

- Modify `frontend-admin/src/api/types.ts`：推送记录类型。
- Create `frontend-admin/src/api/modules/message-push-log.ts`：后台推送记录 API(接口)。
- Create `frontend-admin/src/views/query/message-push-log/index.vue`：推送记录页。
- Modify `frontend-admin/src/views/query/mess-list/index.vue`：增强消息列表列和跳转推送记录。
- Modify `frontend-admin/src/constants/menu.ts`：新增“推送记录”菜单。
- Create `frontend-admin/tests/unit/message-push-log-api.test.ts`：API(接口)测试。

---

### Task 1: 新增推送日志公共类型、实体、仓储和 SQL

**Files:**
- Create: `backend/system-object/src/main/java/com/ypat/MessagePushLogQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/enums/MessagePushEventType.java`
- Create: `backend/system-domain/src/main/java/com/ypat/entity/MessagePushLog.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/MessagePushLogRepository.java`
- Create: `backend/dev/mysql/20260709_create_message_push_log.sql`

- [ ] **Step 1: 创建事件类型枚举**

Create `backend/system-object/src/main/java/com/ypat/enums/MessagePushEventType.java`:

```java
package com.ypat.enums;

public enum MessagePushEventType {
    IN_APP_CREATED("IN_APP_CREATED", "站内消息创建"),
    WECHAT_SUBSCRIBE_SENT("WECHAT_SUBSCRIBE_SENT", "微信订阅发送");

    public final String value;
    public final String text;

    MessagePushEventType(String value, String text) {
        this.value = value;
        this.text = text;
    }
}
```

- [ ] **Step 2: 创建 `MessagePushLogQo`**

Create `backend/system-object/src/main/java/com/ypat/MessagePushLogQo.java`:

```java
package com.ypat;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class MessagePushLogQo implements Serializable {
    private Long id;
    private String eventType;
    private String businessType;
    private Long messageId;
    private Long ypatid;
    private Long sendperid;
    private Long recperid;
    private String touserOpenid;
    private String templateId;
    private String page;
    private String success;
    private String wechatErrcode;
    private String wechatErrmsg;
    private String responseBody;
    private String remark;
    private String dateStart;
    private String dateEnd;
    private Integer pageNo;
    private Integer page = 0;
    private Integer size = 10;
    private Long total;
    private Long successCount;
    private Long failedCount;
    private Long wechatTotal;
    private Long inAppTotal;
    private String failedRate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getYpatid() { return ypatid; }
    public void setYpatid(Long ypatid) { this.ypatid = ypatid; }
    public Long getSendperid() { return sendperid; }
    public void setSendperid(Long sendperid) { this.sendperid = sendperid; }
    public Long getRecperid() { return recperid; }
    public void setRecperid(Long recperid) { this.recperid = recperid; }
    public String getTouserOpenid() { return touserOpenid; }
    public void setTouserOpenid(String touserOpenid) { this.touserOpenid = touserOpenid; }
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getPage() { return page; }
    public void setPage(String page) { this.page = page; }
    public String getSuccess() { return success; }
    public void setSuccess(String success) { this.success = success; }
    public String getWechatErrcode() { return wechatErrcode; }
    public void setWechatErrcode(String wechatErrcode) { this.wechatErrcode = wechatErrcode; }
    public String getWechatErrmsg() { return wechatErrmsg; }
    public void setWechatErrmsg(String wechatErrmsg) { this.wechatErrmsg = wechatErrmsg; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getDateStart() { return dateStart; }
    public void setDateStart(String dateStart) { this.dateStart = dateStart; }
    public String getDateEnd() { return dateEnd; }
    public void setDateEnd(String dateEnd) { this.dateEnd = dateEnd; }
    public Integer getPageNo() { return pageNo; }
    public void setPageNo(Integer pageNo) { this.pageNo = pageNo; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
    public Long getSuccessCount() { return successCount; }
    public void setSuccessCount(Long successCount) { this.successCount = successCount; }
    public Long getFailedCount() { return failedCount; }
    public void setFailedCount(Long failedCount) { this.failedCount = failedCount; }
    public Long getWechatTotal() { return wechatTotal; }
    public void setWechatTotal(Long wechatTotal) { this.wechatTotal = wechatTotal; }
    public Long getInAppTotal() { return inAppTotal; }
    public void setInAppTotal(Long inAppTotal) { this.inAppTotal = inAppTotal; }
    public String getFailedRate() { return failedRate; }
    public void setFailedRate(String failedRate) { this.failedRate = failedRate; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 3: 创建实体和仓储**

Create `backend/system-domain/src/main/java/com/ypat/entity/MessagePushLog.java`:

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
@Table(name = "t_message_push_log")
public class MessagePushLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;
    @Column(name = "business_type", length = 32)
    private String businessType;
    @Column(name = "message_id")
    private Long messageId;
    private Long ypatid;
    private Long sendperid;
    private Long recperid;
    @Column(name = "touser_openid", length = 128)
    private String touserOpenid;
    @Column(name = "template_id", length = 128)
    private String templateId;
    @Column(length = 255)
    private String page;
    @Column(length = 8)
    private String success;
    @Column(name = "wechat_errcode", length = 32)
    private String wechatErrcode;
    @Column(name = "wechat_errmsg", length = 255)
    private String wechatErrmsg;
    @Column(name = "response_body", length = 1024)
    private String responseBody;
    @Column(length = 255)
    private String remark;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getYpatid() { return ypatid; }
    public void setYpatid(Long ypatid) { this.ypatid = ypatid; }
    public Long getSendperid() { return sendperid; }
    public void setSendperid(Long sendperid) { this.sendperid = sendperid; }
    public Long getRecperid() { return recperid; }
    public void setRecperid(Long recperid) { this.recperid = recperid; }
    public String getTouserOpenid() { return touserOpenid; }
    public void setTouserOpenid(String touserOpenid) { this.touserOpenid = touserOpenid; }
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getPage() { return page; }
    public void setPage(String page) { this.page = page; }
    public String getSuccess() { return success; }
    public void setSuccess(String success) { this.success = success; }
    public String getWechatErrcode() { return wechatErrcode; }
    public void setWechatErrcode(String wechatErrcode) { this.wechatErrcode = wechatErrcode; }
    public String getWechatErrmsg() { return wechatErrmsg; }
    public void setWechatErrmsg(String wechatErrmsg) { this.wechatErrmsg = wechatErrmsg; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
```

Create `backend/system-domain/src/main/java/com/ypat/repository/MessagePushLogRepository.java`:

```java
package com.ypat.repository;

import com.ypat.entity.MessagePushLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MessagePushLogRepository extends JpaRepository<MessagePushLog, Long>, JpaSpecificationExecutor<MessagePushLog> {
}
```

- [ ] **Step 4: 创建 SQL(数据库脚本)**

Create `backend/dev/mysql/20260709_create_message_push_log.sql`:

```sql
CREATE TABLE IF NOT EXISTS t_message_push_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  event_type VARCHAR(32) NOT NULL,
  business_type VARCHAR(32) DEFAULT NULL,
  message_id BIGINT DEFAULT NULL,
  ypatid BIGINT DEFAULT NULL,
  sendperid BIGINT DEFAULT NULL,
  recperid BIGINT DEFAULT NULL,
  touser_openid VARCHAR(128) DEFAULT NULL,
  template_id VARCHAR(128) DEFAULT NULL,
  page VARCHAR(255) DEFAULT NULL,
  success VARCHAR(8) DEFAULT NULL,
  wechat_errcode VARCHAR(32) DEFAULT NULL,
  wechat_errmsg VARCHAR(255) DEFAULT NULL,
  response_body VARCHAR(1024) DEFAULT NULL,
  remark VARCHAR(255) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_message_push_log_created_at (created_at),
  KEY idx_message_push_log_message_id (message_id),
  KEY idx_message_push_log_ypatid (ypatid),
  KEY idx_message_push_log_openid (touser_openid),
  KEY idx_message_push_log_result (event_type, business_type, success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 5: 验证和提交**

Run:

```bash
./mvnw -pl backend/system-object,backend/system-domain -DskipTests compile
```

Expected: `BUILD SUCCESS`。如果本地 Maven(构建工具) 不可用，记录错误输出，至少运行：

```bash
git diff --check -- backend/system-object/src/main/java/com/ypat/MessagePushLogQo.java backend/system-object/src/main/java/com/ypat/enums/MessagePushEventType.java backend/system-domain/src/main/java/com/ypat/entity/MessagePushLog.java backend/system-domain/src/main/java/com/ypat/repository/MessagePushLogRepository.java backend/dev/mysql/20260709_create_message_push_log.sql
```

Commit:

```bash
git add backend/system-object/src/main/java/com/ypat/MessagePushLogQo.java backend/system-object/src/main/java/com/ypat/enums/MessagePushEventType.java backend/system-domain/src/main/java/com/ypat/entity/MessagePushLog.java backend/system-domain/src/main/java/com/ypat/repository/MessagePushLogRepository.java backend/dev/mysql/20260709_create_message_push_log.sql
git commit -m "feat: add message push log model"
```

### Task 2: 实现推送日志服务和内部查询接口

**Files:**
- Create: `backend/system-domain/src/main/java/com/ypat/service/MessagePushLogService.java`
- Modify: `backend/system-restapi/src/main/java/com/ypat/controller/MessInfoController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/service/MessServiceClient.java`

- [ ] **Step 1: 实现服务**

Create `backend/system-domain/src/main/java/com/ypat/service/MessagePushLogService.java` with these methods:

```java
@Service
@Transactional(rollbackFor = Exception.class)
public class MessagePushLogService {
    private static final int MAX_TEXT_LENGTH = 255;
    private static final int MAX_BODY_LENGTH = 1024;

    @Autowired
    private MessagePushLogRepository messagePushLogRepository;

    public void record(MessagePushLogQo qo) {
        if (qo == null || isBlank(qo.getEventType())) return;
        MessagePushLog log = new MessagePushLog();
        log.setEventType(qo.getEventType());
        log.setBusinessType(qo.getBusinessType());
        log.setMessageId(qo.getMessageId());
        log.setYpatid(qo.getYpatid());
        log.setSendperid(qo.getSendperid());
        log.setRecperid(qo.getRecperid());
        log.setTouserOpenid(trim(qo.getTouserOpenid(), 128));
        log.setTemplateId(trim(qo.getTemplateId(), 128));
        log.setPage(trim(qo.getPage(), MAX_TEXT_LENGTH));
        log.setSuccess(qo.getSuccess());
        log.setWechatErrcode(trim(qo.getWechatErrcode(), 32));
        log.setWechatErrmsg(trim(qo.getWechatErrmsg(), MAX_TEXT_LENGTH));
        log.setResponseBody(trim(qo.getResponseBody(), MAX_BODY_LENGTH));
        log.setRemark(trim(qo.getRemark(), MAX_TEXT_LENGTH));
        log.setCreatedAt(new Date());
        messagePushLogRepository.save(log);
    }

    public Map<String, Object> findPage(MessagePushLogQo queryQo) {
        final MessagePushLogQo query = normalizeQuery(queryQo);
        Pageable pageable = new PageRequest(query.getPage(), query.getSize(), new Sort(Sort.Direction.DESC, "id"));
        Page<MessagePushLog> page = messagePushLogRepository.findAll(buildSpec(query), pageable);
        List<MessagePushLogQo> content = new ArrayList<MessagePushLogQo>();
        for (MessagePushLog log : page.getContent()) content.add(toQo(log));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("content", content);
        result.put("totalElements", page.getTotalElements());
        result.put("totalPages", page.getTotalPages());
        result.put("number", page.getNumber());
        result.put("size", page.getSize());
        return result;
    }

    public MessagePushLogQo stats(MessagePushLogQo queryQo) {
        MessagePushLogQo query = normalizeQuery(queryQo);
        List<MessagePushLog> logs = messagePushLogRepository.findAll(buildSpec(query));
        long total = logs.size();
        long success = 0L;
        long wechat = 0L;
        long inApp = 0L;
        for (MessagePushLog log : logs) {
            if ("yes".equals(log.getSuccess())) success++;
            if (MessagePushEventType.WECHAT_SUBSCRIBE_SENT.value.equals(log.getEventType())) wechat++;
            if (MessagePushEventType.IN_APP_CREATED.value.equals(log.getEventType())) inApp++;
        }
        long failed = total - success;
        MessagePushLogQo qo = new MessagePushLogQo();
        qo.setTotal(total);
        qo.setSuccessCount(success);
        qo.setFailedCount(failed);
        qo.setWechatTotal(wechat);
        qo.setInAppTotal(inApp);
        qo.setFailedRate(total == 0 ? "0%" : String.format("%.2f%%", failed * 100.0 / total));
        return qo;
    }
}
```

Add these helper methods in the same file:

```java
private MessagePushLogQo normalizeQuery(MessagePushLogQo queryQo) {
    MessagePushLogQo query = queryQo == null ? new MessagePushLogQo() : queryQo;
    if (query.getPage() == null || query.getPage() < 0) query.setPage(0);
    if (query.getSize() == null || query.getSize() <= 0) query.setSize(10);
    if (query.getSize() > 100) query.setSize(100);
    return query;
}

private Specification<MessagePushLog> buildSpec(final MessagePushLogQo query) {
    return new Specification<MessagePushLog>() {
        @Override
        public Predicate toPredicate(Root<MessagePushLog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<Predicate>();
            if (!isBlank(query.getEventType())) predicates.add(criteriaBuilder.equal(root.get("eventType"), query.getEventType().trim()));
            if (!isBlank(query.getBusinessType())) predicates.add(criteriaBuilder.equal(root.get("businessType"), query.getBusinessType().trim()));
            if (!isBlank(query.getSuccess())) predicates.add(criteriaBuilder.equal(root.get("success"), query.getSuccess().trim()));
            if (query.getMessageId() != null) predicates.add(criteriaBuilder.equal(root.get("messageId"), query.getMessageId()));
            if (query.getYpatid() != null) predicates.add(criteriaBuilder.equal(root.get("ypatid"), query.getYpatid()));
            if (query.getSendperid() != null) predicates.add(criteriaBuilder.equal(root.get("sendperid"), query.getSendperid()));
            if (query.getRecperid() != null) predicates.add(criteriaBuilder.equal(root.get("recperid"), query.getRecperid()));
            if (!isBlank(query.getTouserOpenid())) predicates.add(criteriaBuilder.equal(root.get("touserOpenid"), query.getTouserOpenid().trim()));
            if (!isBlank(query.getDateStart())) predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<Date>get("createdAt"), parseDate(query.getDateStart().trim())));
            if (!isBlank(query.getDateEnd())) predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<Date>get("createdAt"), parseDate(query.getDateEnd().trim())));
            criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
            return criteriaQuery.getRestriction();
        }
    };
}

private MessagePushLogQo toQo(MessagePushLog log) {
    MessagePushLogQo qo = new MessagePushLogQo();
    qo.setId(log.getId());
    qo.setEventType(log.getEventType());
    qo.setBusinessType(log.getBusinessType());
    qo.setMessageId(log.getMessageId());
    qo.setYpatid(log.getYpatid());
    qo.setSendperid(log.getSendperid());
    qo.setRecperid(log.getRecperid());
    qo.setTouserOpenid(log.getTouserOpenid());
    qo.setTemplateId(log.getTemplateId());
    qo.setPage(log.getPage());
    qo.setSuccess(log.getSuccess());
    qo.setWechatErrcode(log.getWechatErrcode());
    qo.setWechatErrmsg(log.getWechatErrmsg());
    qo.setResponseBody(log.getResponseBody());
    qo.setRemark(log.getRemark());
    qo.setCreatedAt(log.getCreatedAt());
    return qo;
}

private Date parseDate(String value) {
    try {
        return org.apache.commons.lang.time.DateUtils.parseDate(value, new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"});
    } catch (Exception e) {
        throw new IllegalArgumentException("日期格式错误：" + value);
    }
}

private String trim(String value, int maxLength) {
    if (value == null) return null;
    String trimmed = value.trim();
    return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
}

private boolean isBlank(String value) {
    return value == null || value.trim().length() == 0;
}
```

- [ ] **Step 2: 暴露内部接口**

Modify `backend/system-restapi/src/main/java/com/ypat/controller/MessInfoController.java`:

```java
@Autowired
private MessagePushLogService messagePushLogService;

@PostMapping("/service/message-push-log/record")
public void recordPushLog(@RequestBody MessagePushLogQo qo) {
    messagePushLogService.record(qo);
}

@PostMapping("/service/message-push-log/findPage")
public Map<String, Object> findPushLogPage(@RequestBody MessagePushLogQo qo) {
    return messagePushLogService.findPage(qo);
}

@PostMapping("/service/message-push-log/stats")
public MessagePushLogQo pushLogStats(@RequestBody MessagePushLogQo qo) {
    return messagePushLogService.stats(qo);
}
```

Add imports for `MessagePushLogQo` and `MessagePushLogService`。

- [ ] **Step 3: 补 Feign(服务间调用) 客户端**

Modify `backend/system-wap/src/main/java/com/ypat/service/MessServiceClient.java`:

```java
@PostMapping("/service/message-push-log/record")
void recordPushLog(@RequestBody MessagePushLogQo qo);

@PostMapping("/service/message-push-log/findPage")
String findPushLogPage(@RequestBody MessagePushLogQo qo);

@PostMapping("/service/message-push-log/stats")
MessagePushLogQo pushLogStats(@RequestBody MessagePushLogQo qo);
```

Add import `com.ypat.MessagePushLogQo`。

- [ ] **Step 4: 验证和提交**

Run:

```bash
./mvnw -pl backend/system-domain,backend/system-restapi,backend/system-wap -DskipTests compile
```

Expected: `BUILD SUCCESS`。If not available, run `git diff --check` for the modified files and capture the compile blocker.

Commit:

```bash
git add backend/system-domain/src/main/java/com/ypat/service/MessagePushLogService.java backend/system-restapi/src/main/java/com/ypat/controller/MessInfoController.java backend/system-wap/src/main/java/com/ypat/service/MessServiceClient.java
git commit -m "feat: add message push log service"
```

### Task 3: 接入站内消息创建事件和微信推送结果记录

**Files:**
- Modify: `backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/UserService.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/MypatInfoController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/YpatInfoController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/OauthController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java`

- [ ] **Step 1: 在 domain(领域服务) 记录站内创建事件**

Inject `MessagePushLogService` into `MessInfoService` and `UserService`。站内消息保存后调用：

```java
MessagePushLogQo logQo = new MessagePushLogQo();
logQo.setEventType(MessagePushEventType.IN_APP_CREATED.value);
logQo.setBusinessType(MessType.send.value);
logQo.setMessageId(messInfo.getId());
logQo.setYpatid(ypatid);
logQo.setSendperid(userid);
logQo.setRecperid(recper.getId());
logQo.setSuccess(YesNo.yes.value);
logQo.setRemark("约拍申请站内消息已创建");
messagePushLogService.record(logQo);
```

For `view` messages use `MessType.view.value` and remark `查看联系方式站内消息已创建`。

- [ ] **Step 2: 在 wap(小程序接口后端) 增加记录工具方法**

In each controller that sends WeChat messages, add a private helper like:

```java
private void recordWechatPush(String responseBody, Exception error, MessType type, Long ypatid, Long messageId,
                              Long sendperid, Long recperid, String openid, String templateId, String page) {
    try {
        MessagePushLogQo qo = new MessagePushLogQo();
        qo.setEventType(MessagePushEventType.WECHAT_SUBSCRIBE_SENT.value);
        qo.setBusinessType(type.value);
        qo.setYpatid(ypatid);
        qo.setMessageId(messageId);
        qo.setSendperid(sendperid);
        qo.setRecperid(recperid);
        qo.setTouserOpenid(openid);
        qo.setTemplateId(templateId);
        qo.setPage(page);
        qo.setResponseBody(responseBody);
        if (error != null) {
            qo.setSuccess(YesNo.no.value);
            qo.setRemark(error.getClass().getSimpleName() + ": " + error.getMessage());
        } else if (responseBody != null && responseBody.contains("\"errcode\":0")) {
            qo.setSuccess(YesNo.yes.value);
        } else {
            qo.setSuccess(YesNo.no.value);
            qo.setRemark("微信返回非成功响应");
        }
        messServiceClient.recordPushLog(qo);
    } catch (Exception ignored) {
        logger.warn("推送日志记录失败", ignored);
    }
}
```

When the controller already has a `logger`, reuse it. Do not throw from this helper.

- [ ] **Step 3: Replace fire-and-forget `sendMsg` calls with captured response**

Change:

```java
wxMessClient.sendMsg(accessToken, openid, MessType.send, page, contentMap);
```

To:

```java
String responseBody = wxMessClient.sendMsg(accessToken, openid, MessType.send, page, contentMap);
recordWechatPush(responseBody, null, MessType.send, messInfoQo.getYpatid(), null, userid,
        ypatInfoQo.getUserQo().getId(), openid, Const.TEMP_0, page);
```

Inside `catch (Exception e)`, call `recordWechatPush(null, e, ...)` before logging. Use the template constant matching the `MessType` switch in `WxMess`。

- [ ] **Step 4: 验证和提交**

Run targeted compile:

```bash
./mvnw -pl backend/system-domain,backend/system-wap -DskipTests compile
```

Expected: `BUILD SUCCESS`。If compile is unavailable, run:

```bash
git diff --check -- backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java backend/system-domain/src/main/java/com/ypat/service/UserService.java backend/system-wap/src/main/java/com/ypat/controller/MypatInfoController.java backend/system-wap/src/main/java/com/ypat/controller/YpatInfoController.java backend/system-wap/src/main/java/com/ypat/controller/OauthController.java backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java
```

Commit:

```bash
git add backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java backend/system-domain/src/main/java/com/ypat/service/UserService.java backend/system-wap/src/main/java/com/ypat/controller/MypatInfoController.java backend/system-wap/src/main/java/com/ypat/controller/YpatInfoController.java backend/system-wap/src/main/java/com/ypat/controller/OauthController.java backend/system-wap/src/main/java/com/ypat/controller/AdminYpatController.java
git commit -m "feat: record message push events"
```

### Task 4: 后台推送记录接口

**Files:**
- Create: `backend/system-wap/src/main/java/com/ypat/controller/AdminMessagePushLogController.java`
- Test: `backend/system-wap/src/test/java/com/ypat/controller/AdminMessagePushLogControllerTest.java`

- [ ] **Step 1: Write controller test**

Create a fake `MessServiceClient` and assert `list` and `stats` delegate the normalized query. Use the style from `FeedbackControllerTest` with `ReflectionTestUtils.setField`。

- [ ] **Step 2: Implement controller**

Create `AdminMessagePushLogController`:

```java
@RestController
@RequestMapping("/admin/message-push-log")
public class AdminMessagePushLogController {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    @Autowired
    private MessServiceClient messServiceClient;

    @GetMapping("/list")
    public ResponseApiBody list(MessagePushLogQo qo) {
        normalizePage(qo);
        if (StringUtils.isBlank(qo.getDateStart()) && StringUtils.isBlank(qo.getDateEnd())) {
            qo.setDateStart(DateFormatUtils.format(new Date(), "yyyy-MM-dd 00:00:00"));
            qo.setDateEnd(DateFormatUtils.format(new Date(), "yyyy-MM-dd 23:59:59"));
        }
        String json = messServiceClient.findPushLogPage(qo);
        return ResponseApiBody.success(JsonParser.parseString(json));
    }

    @GetMapping("/stats")
    public ResponseApiBody stats(MessagePushLogQo qo) {
        if (StringUtils.isBlank(qo.getDateStart()) && StringUtils.isBlank(qo.getDateEnd())) {
            qo.setDateStart(DateFormatUtils.format(new Date(), "yyyy-MM-dd 00:00:00"));
            qo.setDateEnd(DateFormatUtils.format(new Date(), "yyyy-MM-dd 23:59:59"));
        }
        return ResponseApiBody.success(messServiceClient.pushLogStats(qo));
    }
}
```

Complete `normalizePage` to clamp page `< 0` to `0`, size `<=0` to `10`, and size `>100` to `100`。

- [ ] **Step 3: Run test and commit**

Run:

```bash
./mvnw -pl backend/system-wap -Dtest=AdminMessagePushLogControllerTest test
```

Commit:

```bash
git add backend/system-wap/src/main/java/com/ypat/controller/AdminMessagePushLogController.java backend/system-wap/src/test/java/com/ypat/controller/AdminMessagePushLogControllerTest.java
git commit -m "feat: add admin message push log api"
```

### Task 5: 后台管理推送记录页面和消息列表增强

**Files:**
- Modify: `frontend-admin/src/api/types.ts`
- Create: `frontend-admin/src/api/modules/message-push-log.ts`
- Create: `frontend-admin/src/views/query/message-push-log/index.vue`
- Modify: `frontend-admin/src/views/query/mess-list/index.vue`
- Modify: `frontend-admin/src/constants/menu.ts`
- Create: `frontend-admin/tests/unit/message-push-log-api.test.ts`

- [ ] **Step 1: Add API types and module**

Add interfaces `MessagePushLog`, `MessagePushLogQuery`, `MessagePushLogStats` to `frontend-admin/src/api/types.ts`。

Create `frontend-admin/src/api/modules/message-push-log.ts`:

```ts
import { get } from '../request'
import type { ApiResult, PageResult, PageQuery, MessagePushLog, MessagePushLogStats } from '../types'

export interface MessagePushLogQuery extends PageQuery {
  eventType?: string
  businessType?: string
  success?: string
  messageId?: number
  ypatid?: number
  sendperid?: number
  recperid?: number
  openid?: string
  dateStart?: string
  dateEnd?: string
}

export const getMessagePushLogList = (params: MessagePushLogQuery): Promise<ApiResult<PageResult<MessagePushLog>>> =>
  get<PageResult<MessagePushLog>>('/admin/message-push-log/list', params as Record<string, unknown>)

export const getMessagePushLogStats = (params: Pick<MessagePushLogQuery, 'dateStart' | 'dateEnd'> = {}): Promise<ApiResult<MessagePushLogStats>> =>
  get<MessagePushLogStats>('/admin/message-push-log/stats', params)
```

- [ ] **Step 2: Add API test**

Create `frontend-admin/tests/unit/message-push-log-api.test.ts` mocking request `get` and asserting paths `/admin/message-push-log/list` and `/admin/message-push-log/stats`。

- [ ] **Step 3: Add page**

Create `frontend-admin/src/views/query/message-push-log/index.vue` with:

- `el-card` stat row for total, success, failed, failedRate.
- `el-form` filters.
- `el-table` fields from the design.
- `el-dialog` for `responseBody`。

Use existing `mess-list/index.vue` pagination style.

- [ ] **Step 4: Update menu and message list**

Add menu item next to “消息列表”:

```ts
{ title: '推送记录', path: '/manage/query/message-push-log', component: 'query/message-push-log/index' },
```

In `mess-list/index.vue`, add columns for `type`、`messviewflag`、`linkwayflag` and operation button:

```vue
<el-button type="primary" link @click="$router.push({ path: '/manage/query/message-push-log', query: { messageId: row.id } })">
  推送记录
</el-button>
```

- [ ] **Step 5: Run tests and commit**

Run:

```bash
cd frontend-admin
./node_modules/.bin/vitest run tests/unit/message-push-log-api.test.ts
```

Commit:

```bash
git add frontend-admin/src/api/types.ts frontend-admin/src/api/modules/message-push-log.ts frontend-admin/src/views/query/message-push-log/index.vue frontend-admin/src/views/query/mess-list/index.vue frontend-admin/src/constants/menu.ts frontend-admin/tests/unit/message-push-log-api.test.ts
git commit -m "feat: add admin message push log page"
```

### Task 6: 小程序订阅授权工具和接入点

**Files:**
- Modify: `frontend/src/api/types/index.ts`
- Modify: `frontend/src/api/modules/content.ts`
- Create: `frontend/src/utils/subscribe-message.ts`
- Create: `frontend/src/utils/__tests__/subscribe-message.test.ts`
- Modify: `frontend/src/pages-sub/work/apply.vue`
- Modify: `frontend/src/components/business/AppointmentPublishForm.vue`

- [ ] **Step 1: Fix template ID type**

Add:

```ts
export interface SubscribeTemplate {
  id: number
  name: string
  value: string
}
```

Change `getTemplateIds()` return type to `Promise<ApiResult<SubscribeTemplate[]>>`。

- [ ] **Step 2: Create subscribe utility**

Create `frontend/src/utils/subscribe-message.ts`:

```ts
import * as contentApi from '@/api/modules/content'
import type { SubscribeTemplate } from '@/api/types'

export type SubscribeScene = 'ypat-action' | 'admin-order'

const SCENE_TEMPLATE_IDS: Record<SubscribeScene, number[]> = {
  'ypat-action': [0, 1, 2],
  'admin-order': [3],
}

export async function requestMessageSubscribe(scene: SubscribeScene): Promise<{ accepted: boolean; skipped: boolean; message: string }> {
  // #ifndef MP-WEIXIN
  return { accepted: true, skipped: true, message: '当前平台无需订阅授权' }
  // #endif
  // #ifdef MP-WEIXIN
  const response = await contentApi.getTemplateIds()
  const templates = filterTemplates(response.data || [], SCENE_TEMPLATE_IDS[scene])
  if (templates.length === 0) return { accepted: true, skipped: true, message: '暂无可用订阅模板' }
  const tmplIds = templates.map((item) => item.value)
  const result = await wx.requestSubscribeMessage({ tmplIds })
  const accepted = tmplIds.some((id) => result[id] === 'accept')
  return {
    accepted,
    skipped: false,
    message: accepted ? '已开启消息提醒' : '未开启通知也可继续提交',
  }
  // #endif
}

export function filterTemplates(templates: SubscribeTemplate[], ids: number[]): SubscribeTemplate[] {
  return templates.filter((item) => ids.includes(item.id) && Boolean(item.value))
}
```

- [ ] **Step 3: Add tests**

Test `filterTemplates` and non-WeChat fallback. Use Vitest(前端测试) and mock `contentApi`。

- [ ] **Step 4: 接入申请和发布**

In `frontend/src/pages-sub/work/apply.vue`, before `ypatApi.applyYpat`, call:

```ts
const subscribe = await requestMessageSubscribe('ypat-action')
if (!subscribe.accepted && !subscribe.skipped) {
  uni.showToast({ title: subscribe.message, icon: 'none' })
}
```

Do not block the submit when rejected.

In `frontend/src/components/business/AppointmentPublishForm.vue`, add the same call before `submitYpat(submitData)`。

- [ ] **Step 5: Run tests and commit**

Run:

```bash
cd frontend
./node_modules/.bin/vitest run src/utils/__tests__/subscribe-message.test.ts src/api/__tests__/api-contracts.test.ts
```

Commit:

```bash
git add frontend/src/api/types/index.ts frontend/src/api/modules/content.ts frontend/src/utils/subscribe-message.ts frontend/src/utils/__tests__/subscribe-message.test.ts frontend/src/pages-sub/work/apply.vue
git add frontend/src/components/business/AppointmentPublishForm.vue
git commit -m "feat: add miniapp subscribe prompt"
```

### Task 7: 小程序消息页视觉、红点和刷新优化

**Files:**
- Modify: `frontend/src/pages/message/index.vue`
- Modify: `frontend/src/pages-sub/content/message-detail.vue`
- Modify: `frontend/src/components/business/KeepTabBar.vue`
- Test: `frontend/src/utils/__tests__/message-navigation.test.ts`

- [ ] **Step 1: Message page state**

Add local refs:

```ts
const recUnreadCount = ref(0)
const sendUnreadCount = ref(0)
const totalUnread = computed(() => recUnreadCount.value + sendUnreadCount.value)
```

Add:

```ts
async function refreshCounts(): Promise<void> {
  const userid = userStore.userInfo?.id
  if (!userid) return
  const [received, sent] = await Promise.all([
    messageApi.getRecUnreadCount('0', userid),
    messageApi.getSendUnreadCount('0', userid),
  ])
  recUnreadCount.value = Number(received.data || 0)
  sendUnreadCount.value = Number(sent.data || 0)
  await userStore.refreshUnreadCount()
}
```

Call `refreshCounts()` in `onShow`, `switchTab`, and pull-down refresh.

- [ ] **Step 2: Add reminder action**

Add a button in the message header:

```vue
<view class="message-reminder" @tap="enableReminder">
  <KeepIcon name="bell" :size="30" />
  <text>开启消息提醒</text>
</view>
```

Add:

```ts
async function enableReminder(): Promise<void> {
  const result = await requestMessageSubscribe('ypat-action')
  uni.showToast({ title: result.message, icon: result.accepted ? 'success' : 'none' })
}
```

- [ ] **Step 3: Update card visual states**

For each card derive classes:

```vue
<view :class="['message-card', `message-card--${tab}`, { 'message-card--unread': item.messviewflag === '0' || item.messviewflag === 'no' }]">
```

Add status pills for unread/read and linkway flag. Keep card width stable; do not nest page sections inside cards.

- [ ] **Step 4: KeepTabBar red dot**

Import `useUserStore` and show dot for message item:

```ts
const userStore = useUserStore()
const items = computed<TabItem[]>(() => [
  { key: 'home', label: '首页', icon: 'home', url: '/pages/home/index' },
  { key: 'work', label: '作品', icon: 'compass', url: '/pages/work/index' },
  { key: 'publish', label: '发布', icon: 'plus-circle' },
  { key: 'message', label: '消息', icon: 'mail', url: '/pages/message/index', dot: userStore.unreadCount > 0 },
  { key: 'mine', label: '我的', icon: 'user', url: '/pages/mine/index' },
])
```

- [ ] **Step 5: Message detail refresh**

In `revealContact`, after `userStore.updateUserInfo()` add:

```ts
await userStore.refreshUnreadCount()
if (message.value) message.value.linkwayflag = 'yes'
```

Also change button text to use `VIEW_CONTACT_PPD`:

```vue
查看联系方式（消耗 {{ VIEW_CONTACT_PPD }} PPD）
```

- [ ] **Step 6: Run frontend tests and commit**

Run:

```bash
cd frontend
./node_modules/.bin/vitest run src/utils/__tests__/message-navigation.test.ts src/utils/__tests__/subscribe-message.test.ts
```

Commit:

```bash
git add frontend/src/pages/message/index.vue frontend/src/pages-sub/content/message-detail.vue frontend/src/components/business/KeepTabBar.vue frontend/src/utils/__tests__/message-navigation.test.ts
git commit -m "feat: polish miniapp message experience"
```

### Task 8: Final verification and evidence

**Files:**
- Verify all changed files.
- No new docs required unless a command fails and needs evidence notes.

- [ ] **Step 1: Check worktree**

Run:

```bash
git status --short
```

Expected: clean or only files intentionally left for the next task.

- [ ] **Step 2: Run targeted tests**

Run backend tests if Maven(构建工具) is available:

```bash
./mvnw -pl backend/system-wap -Dtest=AdminMessagePushLogControllerTest test
```

Run frontend tests:

```bash
cd frontend
./node_modules/.bin/vitest run src/utils/__tests__/subscribe-message.test.ts src/utils/__tests__/message-navigation.test.ts src/api/__tests__/api-contracts.test.ts
```

Run admin tests:

```bash
cd frontend-admin
./node_modules/.bin/vitest run tests/unit/message-push-log-api.test.ts
```

- [ ] **Step 3: Source-level checks**

Run:

```bash
git diff --check
rg -n "requestSubscribeMessage|message-push-log|t_message_push_log|IN_APP_CREATED|WECHAT_SUBSCRIBE_SENT" backend frontend frontend-admin
```

Expected: no whitespace errors; search output shows the new chain exists across backend, miniapp frontend, and admin frontend.

- [ ] **Step 4: Final commit if needed**

If verification-only fixes were needed:

```bash
git add docs/superpowers/plans/2026-07-09-message-chain-upgrade.md
git commit -m "test: verify message chain upgrade"
```
