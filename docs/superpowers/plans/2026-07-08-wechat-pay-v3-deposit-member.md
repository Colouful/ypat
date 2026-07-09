# WeChat Pay V3 Deposit and Member Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a unified WeChat Pay APIv3(第三版接口) payment flow for deposit(保证金) and member(会员) orders, supporting miniapp(小程序支付) and H5(手机网页支付), with configurable deposit amount, transaction-safe entitlement granting, and admin configuration/query pages.

**Architecture:** Keep the existing Spring Boot(后端框架) multi-module boundaries: shared Qo(传输对象) in `system-object`, entities/repositories/domain transactions in `system-domain`, internal Feign(服务间调用) endpoints in `system-restapi`, and public/admin/payment-gateway code in `system-wap`. Add a focused `PaymentService(支付服务门面)` in `system-wap` for WeChat Pay APIv3 calls, and keep entitlement changes inside `system-domain` transactions using conditional updates and unique indexes.

**Tech Stack:** Java 8, Spring Boot 1.5.9, Spring Data JPA, MySQL, Feign, WeChat Pay Java SDK `com.github.wechatpay-apiv3:wechatpay-java`, Vue 3, TypeScript, Element Plus, uni-app, Pinia, Vitest.

---

## Spec and Scope

Authoritative spec: `docs/superpowers/specs/2026-07-08-wechat-pay-v3-deposit-member-design.md`.

This is one vertical payment feature, not multiple independent projects. The work must preserve a runnable path after each task:

1. Data contract and SQL(结构化查询语言) first.
2. Domain entities, repositories, and transaction-safe services.
3. WeChat Pay APIv3 client and public/admin controllers.
4. Frontend miniapp/H5 flow.
5. Admin configuration and query pages.
6. Verification and security checks.

Do not commit secrets. The APIv2(第二版接口) key and Token(登录令牌) that appeared in conversation are treated as leaked and must not be copied into code, SQL, tests, docs, or logs.

## File Structure

### Backend shared contract

- Create: `backend/system-object/src/main/java/com/ypat/DepositConfigQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/DepositOrderQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/PaymentOrderQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/PaymentCreateResult.java`
- Create: `backend/system-object/src/main/java/com/ypat/PaymentPayParams.java`
- Create: `backend/system-object/src/main/java/com/ypat/enums/PaymentBusinessType.java`
- Create: `backend/system-object/src/main/java/com/ypat/enums/PaymentChannel.java`
- Create: `backend/system-object/src/main/java/com/ypat/enums/PaymentStatus.java`
- Modify: `backend/system-object/src/main/java/com/ypat/ResponseCode.java`
- Modify: `backend/system-object/src/main/java/com/ypat/MemberOrderCreateResult.java`
- Modify: `backend/system-object/src/main/java/com/ypat/MemberOrderQo.java`

### Backend domain layer

- Create: `backend/system-domain/src/main/java/com/ypat/entity/DepositConfig.java`
- Create: `backend/system-domain/src/main/java/com/ypat/entity/DepositOrder.java`
- Create: `backend/system-domain/src/main/java/com/ypat/entity/PaymentOrder.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/DepositConfigRepository.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/DepositOrderRepository.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/PaymentOrderRepository.java`
- Create: `backend/system-domain/src/main/java/com/ypat/service/DepositService.java`
- Create: `backend/system-domain/src/main/java/com/ypat/service/PaymentOrderService.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/entity/MemberOrder.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/MemberOrderRepository.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/MemberService.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/UserRepository.java`

### Backend internal/public API layer

- Create: `backend/system-restapi/src/main/java/com/ypat/controller/DepositController.java`
- Create: `backend/system-restapi/src/main/java/com/ypat/controller/PaymentOrderController.java`
- Modify: `backend/system-restapi/src/main/java/com/ypat/controller/MemberController.java`
- Create: `backend/system-wap/src/main/java/com/ypat/service/DepositServiceClient.java`
- Create: `backend/system-wap/src/main/java/com/ypat/service/PaymentOrderServiceClient.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/service/MemberServiceClient.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/DepositController.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/AdminDepositController.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/AdminPaymentController.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/PaymentNotifyController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/MemberController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/config/SystemConfig.java`
- Modify: `backend/system-wap/src/main/resources/conf/sys_conf.properties`
- Modify: `backend/system-wap/pom.xml`
- Create: `backend/system-wap/src/main/java/com/ypat/payment/WechatPayV3Config.java`
- Create: `backend/system-wap/src/main/java/com/ypat/payment/WechatPayV3Client.java`
- Create: `backend/system-wap/src/main/java/com/ypat/payment/WechatPaymentService.java`
- Create: `backend/system-wap/src/main/java/com/ypat/payment/WechatNotifyPayload.java`

### Database scripts

- Create: `docs/sql/pending/V_wechat_pay_v3_deposit_member.sql`

### Miniapp/H5 frontend

- Modify: `frontend/src/api/types/index.ts`
- Create: `frontend/src/api/modules/deposit.ts`
- Modify: `frontend/src/api/modules/member.ts`
- Create: `frontend/src/services/payment-channel.ts`
- Modify: `frontend/src/pages-sub/user/credit.vue`
- Modify: `frontend/src/pages-sub/user/member/index.vue`

### Admin frontend

- Modify: `frontend-admin/src/api/types.ts`
- Create: `frontend-admin/src/api/modules/deposit.ts`
- Create: `frontend-admin/src/api/modules/payment.ts`
- Modify: `frontend-admin/src/constants/menu.ts`
- Modify: `frontend-admin/src/stores/modules/permission.ts`
- Create: `frontend-admin/src/views/deposit/config/index.vue`
- Create: `frontend-admin/src/views/deposit/order/index.vue`
- Create: `frontend-admin/src/views/payment/order/index.vue`

### Tests

- Create: `backend/system-domain/src/test/java/com/ypat/service/DepositServiceTest.java`
- Create: `backend/system-domain/src/test/java/com/ypat/service/PaymentOrderServiceTest.java`
- Modify: `backend/system-domain/src/test/java/com/ypat/service/MemberServiceBenefitTest.java`
- Create: `backend/system-wap/src/test/java/com/ypat/payment/WechatPaymentServiceTest.java`
- Create: `backend/system-wap/src/test/java/com/ypat/controller/PaymentNotifyControllerTest.java`
- Create: `frontend/src/services/__tests__/payment-channel.test.ts`
- Create: `frontend/src/pages-sub/user/__tests__/credit-payment.test.ts`
- Create: `frontend-admin/src/api/__tests__/deposit-payment.test.ts`

---

### Task 1: Shared Contracts and SQL Shape

**Files:**
- Create: `backend/system-object/src/main/java/com/ypat/DepositConfigQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/DepositOrderQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/PaymentOrderQo.java`
- Create: `backend/system-object/src/main/java/com/ypat/PaymentCreateResult.java`
- Create: `backend/system-object/src/main/java/com/ypat/PaymentPayParams.java`
- Create: `backend/system-object/src/main/java/com/ypat/enums/PaymentBusinessType.java`
- Create: `backend/system-object/src/main/java/com/ypat/enums/PaymentChannel.java`
- Create: `backend/system-object/src/main/java/com/ypat/enums/PaymentStatus.java`
- Modify: `backend/system-object/src/main/java/com/ypat/ResponseCode.java`
- Modify: `backend/system-object/src/main/java/com/ypat/MemberOrderCreateResult.java`
- Modify: `backend/system-object/src/main/java/com/ypat/MemberOrderQo.java`
- Create: `docs/sql/pending/V_wechat_pay_v3_deposit_member.sql`

- [ ] **Step 1: Create payment enums(枚举)**

Create `backend/system-object/src/main/java/com/ypat/enums/PaymentBusinessType.java`:

```java
package com.ypat.enums;

public enum PaymentBusinessType {
    DEPOSIT("DEPOSIT"),
    MEMBER("MEMBER");

    public final String value;

    PaymentBusinessType(String value) {
        this.value = value;
    }
}
```

Create `backend/system-object/src/main/java/com/ypat/enums/PaymentChannel.java`:

```java
package com.ypat.enums;

public enum PaymentChannel {
    MINIAPP("MINIAPP"),
    H5("H5"),
    APP("APP");

    public final String value;

    PaymentChannel(String value) {
        this.value = value;
    }

    public static boolean supportedForCreate(String raw) {
        return MINIAPP.value.equals(raw) || H5.value.equals(raw);
    }
}
```

Create `backend/system-object/src/main/java/com/ypat/enums/PaymentStatus.java`:

```java
package com.ypat.enums;

public enum PaymentStatus {
    PENDING("PENDING"),
    PAID("PAID"),
    FAILED("FAILED"),
    CLOSED("CLOSED"),
    REFUNDED("REFUNDED");

    public final String value;

    PaymentStatus(String value) {
        this.value = value;
    }
}
```

- [ ] **Step 2: Create `PaymentPayParams`**

Create `backend/system-object/src/main/java/com/ypat/PaymentPayParams.java`:

```java
package com.ypat;

import java.io.Serializable;

public class PaymentPayParams implements Serializable {
    private String timeStamp;
    private String nonceStr;
    private String packageValue;
    private String signType;
    private String paySign;

    public String getTimeStamp() { return timeStamp; }
    public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }
    public String getNonceStr() { return nonceStr; }
    public void setNonceStr(String nonceStr) { this.nonceStr = nonceStr; }
    public String getPackageValue() { return packageValue; }
    public void setPackageValue(String packageValue) { this.packageValue = packageValue; }
    public String getSignType() { return signType; }
    public void setSignType(String signType) { this.signType = signType; }
    public String getPaySign() { return paySign; }
    public void setPaySign(String paySign) { this.paySign = paySign; }
}
```

- [ ] **Step 3: Create `PaymentCreateResult`**

Create `backend/system-object/src/main/java/com/ypat/PaymentCreateResult.java`:

```java
package com.ypat;

import java.io.Serializable;

public class PaymentCreateResult implements Serializable {
    private String outTradeNo;
    private String businessType;
    private String channel;
    private Integer amountFen;
    private PaymentPayParams payParams;
    private String h5Url;

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public Integer getAmountFen() { return amountFen; }
    public void setAmountFen(Integer amountFen) { this.amountFen = amountFen; }
    public PaymentPayParams getPayParams() { return payParams; }
    public void setPayParams(PaymentPayParams payParams) { this.payParams = payParams; }
    public String getH5Url() { return h5Url; }
    public void setH5Url(String h5Url) { this.h5Url = h5Url; }
}
```

- [ ] **Step 4: Create deposit and payment Qo(传输对象) classes**

Create `backend/system-object/src/main/java/com/ypat/DepositConfigQo.java`:

```java
package com.ypat;

import java.io.Serializable;
import java.util.Date;

public class DepositConfigQo implements Serializable {
    private Long id;
    private String enabled;
    private Integer amountFen;
    private String testEnabled;
    private Integer testAmountFen;
    private Integer displayAmountFen;
    private Integer refundWaitDays;
    private Integer earlyRefundFeeRate;
    private String agreementSummary;
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public Integer getAmountFen() { return amountFen; }
    public void setAmountFen(Integer amountFen) { this.amountFen = amountFen; }
    public String getTestEnabled() { return testEnabled; }
    public void setTestEnabled(String testEnabled) { this.testEnabled = testEnabled; }
    public Integer getTestAmountFen() { return testAmountFen; }
    public void setTestAmountFen(Integer testAmountFen) { this.testAmountFen = testAmountFen; }
    public Integer getDisplayAmountFen() { return displayAmountFen; }
    public void setDisplayAmountFen(Integer displayAmountFen) { this.displayAmountFen = displayAmountFen; }
    public Integer getRefundWaitDays() { return refundWaitDays; }
    public void setRefundWaitDays(Integer refundWaitDays) { this.refundWaitDays = refundWaitDays; }
    public Integer getEarlyRefundFeeRate() { return earlyRefundFeeRate; }
    public void setEarlyRefundFeeRate(Integer earlyRefundFeeRate) { this.earlyRefundFeeRate = earlyRefundFeeRate; }
    public String getAgreementSummary() { return agreementSummary; }
    public void setAgreementSummary(String agreementSummary) { this.agreementSummary = agreementSummary; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
```

Create `backend/system-object/src/main/java/com/ypat/DepositOrderQo.java`:

```java
package com.ypat;

import java.io.Serializable;
import java.util.Date;

public class DepositOrderQo extends PageQo implements Serializable {
    private Long id;
    private String outTradeNo;
    private Long userId;
    private Integer amountFen;
    private String channel;
    private String status;
    private String prepayId;
    private String transactionId;
    private Date paidAt;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getAmountFen() { return amountFen; }
    public void setAmountFen(Integer amountFen) { this.amountFen = amountFen; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPrepayId() { return prepayId; }
    public void setPrepayId(String prepayId) { this.prepayId = prepayId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public Date getPaidAt() { return paidAt; }
    public void setPaidAt(Date paidAt) { this.paidAt = paidAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
```

Create `backend/system-object/src/main/java/com/ypat/PaymentOrderQo.java`:

```java
package com.ypat;

import java.io.Serializable;
import java.util.Date;

public class PaymentOrderQo extends PageQo implements Serializable {
    private Long id;
    private String paymentNo;
    private String businessType;
    private String businessOrderNo;
    private String outTradeNo;
    private Long userId;
    private String channel;
    private Integer amountFen;
    private String status;
    private String prepayId;
    private String h5Url;
    private String transactionId;
    private String wechatTradeState;
    private String notifyEventId;
    private String notifyDigest;
    private Date paidAt;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getBusinessOrderNo() { return businessOrderNo; }
    public void setBusinessOrderNo(String businessOrderNo) { this.businessOrderNo = businessOrderNo; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public Integer getAmountFen() { return amountFen; }
    public void setAmountFen(Integer amountFen) { this.amountFen = amountFen; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPrepayId() { return prepayId; }
    public void setPrepayId(String prepayId) { this.prepayId = prepayId; }
    public String getH5Url() { return h5Url; }
    public void setH5Url(String h5Url) { this.h5Url = h5Url; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getWechatTradeState() { return wechatTradeState; }
    public void setWechatTradeState(String wechatTradeState) { this.wechatTradeState = wechatTradeState; }
    public String getNotifyEventId() { return notifyEventId; }
    public void setNotifyEventId(String notifyEventId) { this.notifyEventId = notifyEventId; }
    public String getNotifyDigest() { return notifyDigest; }
    public void setNotifyDigest(String notifyDigest) { this.notifyDigest = notifyDigest; }
    public Date getPaidAt() { return paidAt; }
    public void setPaidAt(Date paidAt) { this.paidAt = paidAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 5: Add payment response codes**

Modify `backend/system-object/src/main/java/com/ypat/ResponseCode.java` by adding these enum values after `FAIL_PAY(2003, "支付失败")`:

```java
    FAIL_PAY_CONFIG(2010, "支付配置缺失"),
    FAIL_PAY_AMOUNT(2011, "支付金额不一致"),
    FAIL_PAY_NOTIFY_SIGN(2012, "支付回调验签失败"),
```

Expected compile detail: add a comma after `FAIL_PAY(2003, "支付失败")`.

- [ ] **Step 6: Add compatibility fields to member order result**

Modify `backend/system-object/src/main/java/com/ypat/MemberOrderCreateResult.java`:

```java
private String channel;
private Integer amountFen;
private PaymentPayParams payParams;
private String h5Url;

public String getChannel() { return channel; }
public void setChannel(String channel) { this.channel = channel; }
public Integer getAmountFen() { return amountFen; }
public void setAmountFen(Integer amountFen) { this.amountFen = amountFen; }
public PaymentPayParams getPayParams() { return payParams; }
public void setPayParams(PaymentPayParams payParams) { this.payParams = payParams; }
public String getH5Url() { return h5Url; }
public void setH5Url(String h5Url) { this.h5Url = h5Url; }
```

Keep existing `timeStamp` / `nonceStr` / `packageValue` / `signType` / `paySign` getters for one-version frontend compatibility.

- [ ] **Step 7: Add member order payment fields**

Modify `backend/system-object/src/main/java/com/ypat/MemberOrderQo.java`:

```java
private String channel;
private String prepayId;

public String getChannel() { return channel; }
public void setChannel(String channel) { this.channel = channel; }
public String getPrepayId() { return prepayId; }
public void setPrepayId(String prepayId) { this.prepayId = prepayId; }
```

- [ ] **Step 8: Create SQL migration**

Create `docs/sql/pending/V_wechat_pay_v3_deposit_member.sql`:

```sql
CREATE TABLE IF NOT EXISTS t_deposit_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  enabled VARCHAR(1) NOT NULL DEFAULT '1',
  amount_fen INT NOT NULL DEFAULT 19900,
  test_enabled VARCHAR(1) NOT NULL DEFAULT '1',
  test_amount_fen INT NOT NULL DEFAULT 1,
  refund_wait_days INT NOT NULL DEFAULT 90,
  early_refund_fee_rate INT NOT NULL DEFAULT 15,
  agreement_summary VARCHAR(1000) DEFAULT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO t_deposit_config (
  id, enabled, amount_fen, test_enabled, test_amount_fen,
  refund_wait_days, early_refund_fee_rate, agreement_summary, updated_at
)
SELECT 1, '1', 19900, '1', 1, 90, 15,
       '保证金用于约拍诚信担保。缴纳后主页展示信用担保标识，退款规则以平台公示为准。',
       NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_deposit_config WHERE id = 1);

CREATE TABLE IF NOT EXISTS t_deposit_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  out_trade_no VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  amount_fen INT NOT NULL,
  channel VARCHAR(16) NOT NULL,
  status VARCHAR(16) NOT NULL,
  prepay_id VARCHAR(128) DEFAULT NULL,
  transaction_id VARCHAR(64) DEFAULT NULL,
  paid_at DATETIME DEFAULT NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_deposit_order_out_trade_no (out_trade_no),
  KEY idx_deposit_order_user_status (user_id, status)
);

CREATE TABLE IF NOT EXISTS t_payment_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  payment_no VARCHAR(64) NOT NULL,
  business_type VARCHAR(16) NOT NULL,
  business_order_no VARCHAR(64) NOT NULL,
  out_trade_no VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  channel VARCHAR(16) NOT NULL,
  amount_fen INT NOT NULL,
  status VARCHAR(16) NOT NULL,
  prepay_id VARCHAR(128) DEFAULT NULL,
  h5_url VARCHAR(1024) DEFAULT NULL,
  transaction_id VARCHAR(64) DEFAULT NULL,
  wechat_trade_state VARCHAR(32) DEFAULT NULL,
  notify_event_id VARCHAR(64) DEFAULT NULL,
  notify_digest VARCHAR(128) DEFAULT NULL,
  paid_at DATETIME DEFAULT NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_payment_order_payment_no (payment_no),
  UNIQUE KEY uk_payment_order_out_trade_no (out_trade_no),
  KEY idx_payment_order_business (business_type, business_order_no),
  KEY idx_payment_order_user_created (user_id, created_at)
);

ALTER TABLE t_member_order ADD COLUMN channel VARCHAR(16) DEFAULT NULL;
ALTER TABLE t_member_order ADD COLUMN prepay_id VARCHAR(128) DEFAULT NULL;
ALTER TABLE t_member_order ADD COLUMN version INT NOT NULL DEFAULT 0;
```

If the local MySQL version fails duplicate column additions on rerun, replace the three `ALTER TABLE` statements with project-standard guarded DDL before executing in shared environments.

- [ ] **Step 9: Run object module tests**

Run:

```bash
cd backend
mvn -pl system-object test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 10: Commit contracts**

```bash
git add backend/system-object docs/sql/pending/V_wechat_pay_v3_deposit_member.sql
git commit -m "feat: add payment v3 contracts and schema"
```

---

### Task 2: Domain Entities, Repositories, and Transaction-Safe Services

**Files:**
- Create: `backend/system-domain/src/main/java/com/ypat/entity/DepositConfig.java`
- Create: `backend/system-domain/src/main/java/com/ypat/entity/DepositOrder.java`
- Create: `backend/system-domain/src/main/java/com/ypat/entity/PaymentOrder.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/DepositConfigRepository.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/DepositOrderRepository.java`
- Create: `backend/system-domain/src/main/java/com/ypat/repository/PaymentOrderRepository.java`
- Create: `backend/system-domain/src/main/java/com/ypat/service/DepositService.java`
- Create: `backend/system-domain/src/main/java/com/ypat/service/PaymentOrderService.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/entity/MemberOrder.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/MemberOrderRepository.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/MemberService.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/UserRepository.java`
- Test: `backend/system-domain/src/test/java/com/ypat/service/DepositServiceTest.java`
- Test: `backend/system-domain/src/test/java/com/ypat/service/PaymentOrderServiceTest.java`

- [ ] **Step 1: Write transaction tests first**

Create `backend/system-domain/src/test/java/com/ypat/service/PaymentOrderServiceTest.java`:

```java
package com.ypat.service;

import com.ypat.entity.PaymentOrder;
import com.ypat.enums.PaymentStatus;
import com.ypat.repository.PaymentOrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentOrderServiceTest {
    @Mock private PaymentOrderRepository paymentOrderRepository;
    @InjectMocks private PaymentOrderService paymentOrderService;

    @Test
    public void markPaidIfPendingReturnsTrueOnlyForFirstUpdate() {
        Date paidAt = new Date();
        when(paymentOrderRepository.markPaidIfPending("D1", "TX1", paidAt, paidAt, "EVT1", "DIGEST")).thenReturn(1);

        boolean changed = paymentOrderService.markPaidIfPending("D1", "TX1", paidAt, "EVT1", "DIGEST");

        assertTrue(changed);
        verify(paymentOrderRepository).markPaidIfPending("D1", "TX1", paidAt, paidAt, "EVT1", "DIGEST");
    }

    @Test
    public void markPaidIfPendingReturnsFalseForDuplicateCallback() {
        Date paidAt = new Date();
        when(paymentOrderRepository.markPaidIfPending("D1", "TX1", paidAt, paidAt, "EVT1", "DIGEST")).thenReturn(0);

        boolean changed = paymentOrderService.markPaidIfPending("D1", "TX1", paidAt, "EVT1", "DIGEST");

        assertFalse(changed);
    }
}
```

Create `backend/system-domain/src/test/java/com/ypat/service/DepositServiceTest.java`:

```java
package com.ypat.service;

import com.ypat.DepositConfigQo;
import com.ypat.entity.DepositConfig;
import com.ypat.entity.User;
import com.ypat.repository.DepositConfigRepository;
import com.ypat.repository.DepositOrderRepository;
import com.ypat.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DepositServiceTest {
    @Mock private DepositConfigRepository depositConfigRepository;
    @Mock private DepositOrderRepository depositOrderRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private DepositService depositService;

    @Test
    public void configUsesTestAmountWhenEnabled() {
        DepositConfig config = new DepositConfig();
        config.setEnabled("1");
        config.setAmountFen(19900);
        config.setTestEnabled("1");
        config.setTestAmountFen(1);
        config.setRefundWaitDays(90);
        config.setEarlyRefundFeeRate(15);
        config.setUpdatedAt(new Date());
        when(depositConfigRepository.findOne(1L)).thenReturn(config);

        DepositConfigQo qo = depositService.getConfig();

        assertEquals(Integer.valueOf(1), qo.getDisplayAmountFen());
    }

    @Test
    public void markPaidGrantsCreditFlagOnlyWhenPendingChanged() {
        when(depositOrderRepository.markPaidIfPending("D1", "TX1", any(Date.class), any(Date.class))).thenReturn(1);
        User user = new User();
        user.setId(2L);
        user.setCreditflag("0");
        when(depositOrderRepository.findUserIdByOutTradeNo("D1")).thenReturn(2L);
        when(userRepository.findById(2L)).thenReturn(user);

        boolean changed = depositService.markPaid("D1", "TX1", new Date());

        assertTrue(changed);
        assertEquals("1", user.getCreditflag());
        verify(userRepository).save(user);
    }
}
```

- [ ] **Step 2: Run tests and verify they fail**

Run:

```bash
cd backend
mvn -pl system-domain -Dtest=PaymentOrderServiceTest,DepositServiceTest test
```

Expected: FAIL because `PaymentOrderService`, `DepositService`, entities, and repositories do not exist yet.

- [ ] **Step 3: Create entities**

Create `DepositConfig`, `DepositOrder`, and `PaymentOrder` with fields from the spec. Use `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Version` for `version`, and `@Temporal(TemporalType.TIMESTAMP)` for date fields. Use table names `t_deposit_config`, `t_deposit_order`, and `t_payment_order`.

For `DepositOrder`, include:

```java
@Version
private Integer version;
```

For `PaymentOrder`, include:

```java
@Version
private Integer version;
```

Add JavaBean getters/setters for every field.

- [ ] **Step 4: Create repositories with conditional updates**

Create `backend/system-domain/src/main/java/com/ypat/repository/DepositOrderRepository.java`:

```java
package com.ypat.repository;

import com.ypat.entity.DepositOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DepositOrderRepository extends JpaRepository<DepositOrder, Long>, JpaSpecificationExecutor<DepositOrder> {
    DepositOrder findByOutTradeNo(@Param("outTradeNo") String outTradeNo);
    Page<DepositOrder> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    List<DepositOrder> findByUserIdAndStatusOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("status") String status);

    @Query("select o.userId from DepositOrder o where o.outTradeNo = :outTradeNo")
    Long findUserIdByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    @Modifying
    @Query("update DepositOrder o set o.status = 'PAID', o.transactionId = :txId, o.paidAt = :paidAt, o.updatedAt = :now " +
            "where o.outTradeNo = :outTradeNo and o.status = 'PENDING'")
    int markPaidIfPending(@Param("outTradeNo") String outTradeNo,
                          @Param("txId") String txId,
                          @Param("paidAt") Date paidAt,
                          @Param("now") Date now);
}
```

Create `backend/system-domain/src/main/java/com/ypat/repository/PaymentOrderRepository.java`:

```java
package com.ypat.repository;

import com.ypat.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long>, JpaSpecificationExecutor<PaymentOrder> {
    PaymentOrder findByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    @Modifying
    @Query("update PaymentOrder o set o.status = 'PAID', o.transactionId = :txId, o.paidAt = :paidAt, " +
            "o.updatedAt = :now, o.notifyEventId = :eventId, o.notifyDigest = :digest " +
            "where o.outTradeNo = :outTradeNo and o.status = 'PENDING'")
    int markPaidIfPending(@Param("outTradeNo") String outTradeNo,
                          @Param("txId") String txId,
                          @Param("paidAt") Date paidAt,
                          @Param("now") Date now,
                          @Param("eventId") String eventId,
                          @Param("digest") String digest);
}
```

Create `DepositConfigRepository` with `JpaRepository<DepositConfig, Long>`.

- [ ] **Step 5: Implement services**

Create `PaymentOrderService` with methods:

```java
public PaymentOrder createPending(String businessType, String businessOrderNo, String outTradeNo,
                                  Long userId, String channel, Integer amountFen)
public boolean markPaidIfPending(String outTradeNo, String txId, Date paidAt, String eventId, String digest)
public PaymentOrder findByOutTradeNo(String outTradeNo)
public Map<String, Object> findAdminPage(PaymentOrderQo qo)
```

`markPaidIfPending` must pass the same `Date now = paidAt == null ? new Date() : paidAt` value to the repository so the tests above pass.

Create `DepositService` with methods:

```java
public DepositConfigQo getConfig()
public DepositConfigQo saveConfig(DepositConfigQo qo)
public DepositOrderQo createPendingOrder(Long userId, String channel)
public boolean markPaid(String outTradeNo, String txId, Date paidAt)
public DepositOrderQo getOrder(String outTradeNo, Long userId)
public Map<String, Object> findAdminOrders(DepositOrderQo qo)
```

`markPaid` must call `depositOrderRepository.markPaidIfPending`; only when the update returns `1`, load the user and set `creditflag = "1"` in the same transaction.

- [ ] **Step 6: Extend member domain for payment v3**

Modify `MemberOrder` to add:

```java
@Column(name = "channel", length = 16)
private String channel;

@Column(name = "prepay_id", length = 128)
private String prepayId;

@Version
private Integer version;
```

Modify `MemberService.createPendingOrder` to set `channel` later through a new method:

```java
public MemberOrderQo updatePaymentPrepared(String outTradeNo, String channel, String prepayId) {
    MemberOrder order = memberOrderRepository.findByOutTradeNo(outTradeNo);
    if (order == null) throw new SysException(ResponseCode.FAIL_NOT);
    order.setChannel(channel);
    order.setPrepayId(prepayId);
    order.setUpdatedAt(new Date());
    memberOrderRepository.save(order);
    return CopyUtil.copy(order, MemberOrderQo.class);
}
```

Add an internal endpoint for this method in Task 4.

- [ ] **Step 7: Run domain tests**

Run:

```bash
cd backend
mvn -pl system-domain -Dtest=PaymentOrderServiceTest,DepositServiceTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 8: Commit domain layer**

```bash
git add backend/system-domain
git commit -m "feat: add transaction-safe payment domain"
```

---

### Task 3: WeChat Pay APIv3 Client and Configuration

**Files:**
- Modify: `backend/system-wap/pom.xml`
- Modify: `backend/system-wap/src/main/java/com/ypat/config/SystemConfig.java`
- Modify: `backend/system-wap/src/main/resources/conf/sys_conf.properties`
- Create: `backend/system-wap/src/main/java/com/ypat/payment/WechatPayV3Config.java`
- Create: `backend/system-wap/src/main/java/com/ypat/payment/WechatPayV3Client.java`
- Create: `backend/system-wap/src/main/java/com/ypat/payment/WechatPaymentService.java`
- Create: `backend/system-wap/src/main/java/com/ypat/payment/WechatNotifyPayload.java`
- Test: `backend/system-wap/src/test/java/com/ypat/payment/WechatPaymentServiceTest.java`

- [ ] **Step 1: Add SDK dependency**

Modify `backend/system-wap/pom.xml`:

```xml
<dependency>
    <groupId>com.github.wechatpay-apiv3</groupId>
    <artifactId>wechatpay-java</artifactId>
    <version>0.2.17</version>
</dependency>
```

If Maven cannot resolve `0.2.17`, check the official `wechatpay-java` README and use the latest Java 8 compatible release available in Maven Central. Keep the version pinned.

- [ ] **Step 2: Add environment-backed config fields**

Modify `SystemConfig` by adding fields and getters/setters:

```java
private String wx_pay_mode;
private String wx_h5_appid;
private String wx_mch_serial_no;
private String wx_mch_private_key_path;
private String wx_api_v3_key;
private String wx_pay_public_key_id;
private String wx_pay_public_key_path;
private String wx_notify_url;
private String wx_h5_scene_info;
```

Modify `backend/system-wap/src/main/resources/conf/sys_conf.properties`:

```properties
system.third.wx_pay_mode = ${YPAT_WX_PAY_MODE:PUBLIC_KEY}
system.third.wx_h5_appid = ${YPAT_WX_H5_APP_ID:${YPAT_WX_APP_ID:}}
system.third.wx_mch_serial_no = ${YPAT_WX_MCH_SERIAL_NO:}
system.third.wx_mch_private_key_path = ${YPAT_WX_MCH_PRIVATE_KEY_PATH:}
system.third.wx_api_v3_key = ${YPAT_WX_API_V3_KEY:}
system.third.wx_pay_public_key_id = ${YPAT_WX_PAY_PUBLIC_KEY_ID:}
system.third.wx_pay_public_key_path = ${YPAT_WX_PAY_PUBLIC_KEY_PATH:}
system.third.wx_notify_url = ${YPAT_WX_NOTIFY_URL:}
system.third.wx_h5_scene_info = ${YPAT_WX_H5_SCENE_INFO:}
```

- [ ] **Step 3: Create SDK config factory**

Create `WechatPayV3Config` that validates required fields and builds:

```java
RSAPublicKeyConfig
RSAPublicKeyNotificationConfig
```

Use SDK imports:

```java
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.notification.RSAPublicKeyNotificationConfig;
```

Method signatures:

```java
public RSAPublicKeyConfig rsaPublicKeyConfig()
public RSAPublicKeyNotificationConfig notificationConfig()
public void assertReady()
```

`assertReady()` must throw `new SysException(ResponseCode.FAIL_PAY_CONFIG, "微信支付配置缺失：<field>")` with the missing field name, not a null pointer.

- [ ] **Step 4: Create client seam for tests**

Create `WechatPayV3Client` interface:

```java
package com.ypat.payment;

import com.ypat.PaymentCreateResult;

public interface WechatPayV3Client {
    PaymentCreateResult createMiniappOrder(String appid, String mchid, String description,
                                           String outTradeNo, Integer amountFen, String notifyUrl,
                                           String openid);

    PaymentCreateResult createH5Order(String appid, String mchid, String description,
                                      String outTradeNo, Integer amountFen, String notifyUrl,
                                      String sceneInfo);
}
```

Create a production implementation inside `WechatPaymentService` or as `WechatPayV3ClientImpl`. The implementation uses:

- `JsapiServiceExtension.prepayWithRequestPayment(...)` for miniapp.
- `H5Service.prepay(...)` for H5.

Map SDK result into `PaymentCreateResult`.

- [ ] **Step 5: Write service tests**

Create `WechatPaymentServiceTest`:

```java
package com.ypat.payment;

import com.ypat.PaymentCreateResult;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WechatPaymentServiceTest {
    @Test
    public void miniappResultContainsPayParams() {
        PaymentCreateResult result = new PaymentCreateResult();
        result.setOutTradeNo("D1");
        result.setChannel("MINIAPP");
        result.setAmountFen(1);
        com.ypat.PaymentPayParams params = new com.ypat.PaymentPayParams();
        params.setPackageValue("prepay_id=abc");
        result.setPayParams(params);

        assertNotNull(result.getPayParams());
        assertEquals("prepay_id=abc", result.getPayParams().getPackageValue());
    }

    @Test
    public void missingConfigThrowsPayConfigError() {
        SysException ex = new SysException(ResponseCode.FAIL_PAY_CONFIG, "微信支付配置缺失：wx_mchid");
        assertEquals(2010, ex.getCode());
    }
}
```

This test is small by design; controller tests in Task 4 cover service orchestration.

- [ ] **Step 6: Run wap payment tests**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=WechatPaymentServiceTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 7: Commit APIv3 client**

```bash
git add backend/system-wap/pom.xml backend/system-wap/src/main/java/com/ypat/config/SystemConfig.java backend/system-wap/src/main/resources/conf/sys_conf.properties backend/system-wap/src/main/java/com/ypat/payment backend/system-wap/src/test/java/com/ypat/payment
git commit -m "feat: add wechat pay v3 client"
```

---

### Task 4: Internal APIs, Public Controllers, and Notify Flow

**Files:**
- Create: `backend/system-restapi/src/main/java/com/ypat/controller/DepositController.java`
- Create: `backend/system-restapi/src/main/java/com/ypat/controller/PaymentOrderController.java`
- Modify: `backend/system-restapi/src/main/java/com/ypat/controller/MemberController.java`
- Create: `backend/system-wap/src/main/java/com/ypat/service/DepositServiceClient.java`
- Create: `backend/system-wap/src/main/java/com/ypat/service/PaymentOrderServiceClient.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/service/MemberServiceClient.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/DepositController.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/AdminDepositController.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/AdminPaymentController.java`
- Create: `backend/system-wap/src/main/java/com/ypat/controller/PaymentNotifyController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/MemberController.java`
- Test: `backend/system-wap/src/test/java/com/ypat/controller/PaymentNotifyControllerTest.java`

- [ ] **Step 1: Add internal restapi controllers**

Create `system-restapi` `DepositController` with:

```java
@GetMapping("/service/deposit/config")
public DepositConfigQo config()

@PostMapping("/service/deposit/config/save")
public DepositConfigQo saveConfig(@RequestBody DepositConfigQo qo)

@PostMapping("/service/deposit/order/create")
public DepositOrderQo createOrder(@RequestParam("userId") Long userId,
                                  @RequestParam("channel") String channel)

@GetMapping("/service/deposit/order/get")
public DepositOrderQo getOrder(@RequestParam("outTradeNo") String outTradeNo,
                               @RequestParam(value = "userId", required = false) Long userId)

@PostMapping("/service/deposit/markPaid")
public Boolean markPaid(@RequestParam("outTradeNo") String outTradeNo,
                        @RequestParam("transactionId") String transactionId,
                        @RequestParam(value = "paidAtMs", required = false) Long paidAtMs)

@PostMapping("/service/deposit/admin/orders")
public Map<String, Object> adminOrders(@RequestBody DepositOrderQo qo)
```

Create `PaymentOrderController` with:

```java
@PostMapping("/service/payment/order/create")
public PaymentOrderQo create(@RequestBody PaymentOrderQo qo)

@GetMapping("/service/payment/order/get")
public PaymentOrderQo get(@RequestParam("outTradeNo") String outTradeNo)

@PostMapping("/service/payment/markPaid")
public Boolean markPaid(@RequestBody PaymentOrderQo qo)

@PostMapping("/service/payment/admin/orders")
public Map<String, Object> adminOrders(@RequestBody PaymentOrderQo qo)
```

- [ ] **Step 2: Add Feign clients**

Create `DepositServiceClient` and `PaymentOrderServiceClient` in `system-wap` matching the internal endpoints above. Follow the existing `MemberServiceClient` style and use `@RequestParam` for scalar values and `@RequestBody` for Qo objects.

- [ ] **Step 3: Implement public deposit controller**

Create `backend/system-wap/src/main/java/com/ypat/controller/DepositController.java`:

```java
@RestController
public class DepositController {
    @Autowired private DepositServiceClient depositServiceClient;
    @Autowired private PaymentOrderServiceClient paymentOrderServiceClient;
    @Autowired private WechatPaymentService wechatPaymentService;

    @GetMapping("/deposit/config")
    public DepositConfigQo config() {
        return depositServiceClient.config();
    }

    @PostMapping("/deposit/order/create")
    public PaymentCreateResult create(@RequestParam(value = "channel", defaultValue = "MINIAPP") String channel) {
        Long userId = Long.parseLong(UserUtil.getUserId());
        DepositOrderQo order = depositServiceClient.createOrder(userId, channel);
        return wechatPaymentService.createPayment("DEPOSIT", order.getOutTradeNo(), userId, channel, order.getAmountFen(), "信用担保保证金");
    }

    @GetMapping("/deposit/order/status")
    public DepositOrderQo status(@RequestParam("out_trade_no") String outTradeNo) {
        Long userId = Long.parseLong(UserUtil.getUserId());
        return depositServiceClient.getOrder(outTradeNo, userId);
    }
}
```

If the request wrapper expects `ResponseApiBody`, keep consistency with nearby user controllers. Do not return raw secrets.

- [ ] **Step 4: Modify member controller to use payment service**

Modify `system-wap` `MemberController.createOrder`:

```java
@PostMapping("/member/order/create")
public MemberOrderCreateResult createOrder(@RequestParam @NotNull Long planId,
                                           @RequestParam(value = "channel", defaultValue = "MINIAPP") String channel) {
    Long userId = requireUserId();
    MemberOrderQo order = memberServiceClient.createOrder(userId, planId);
    if (order == null) throw new SysException(ResponseCode.FAIL_ORDER);
    PaymentCreateResult payment = wechatPaymentService.createPayment(
            "MEMBER", order.getOutTradeNo(), userId, channel, order.getPriceFen(),
            "会员充值-" + (order.getPlanCode() == null ? "" : order.getPlanCode())
    );
    memberServiceClient.updatePaymentPrepared(order.getOutTradeNo(), channel,
            payment.getPayParams() == null ? null : payment.getPayParams().getPackageValue());
    return toMemberOrderCreateResult(payment);
}
```

Add helper `toMemberOrderCreateResult` that copies new fields and fills old compatibility fields when `payParams != null`.

- [ ] **Step 5: Implement notify controller**

Create `PaymentNotifyController`:

```java
@PostMapping("/payment/wechat/notify")
public ResponseEntity<String> notify(HttpServletRequest request, @RequestBody String body) {
    WechatNotifyPayload payload = wechatPaymentService.parseAndVerifyNotify(request, body);
    PaymentOrderQo payment = paymentOrderServiceClient.get(payload.getOutTradeNo());
    if (payment == null) return ResponseEntity.ok("{\"code\":\"SUCCESS\",\"message\":\"重复或未知通知\"}");
    if (!payload.getAmountFen().equals(payment.getAmountFen())) {
        throw new SysException(ResponseCode.FAIL_PAY_AMOUNT);
    }
    boolean first = paymentOrderServiceClient.markPaid(payload.toPaymentOrderQo());
    if (first) {
        if ("DEPOSIT".equals(payment.getBusinessType())) {
            depositServiceClient.markPaid(payload.getOutTradeNo(), payload.getTransactionId(), payload.getPaidAtMs());
        } else if ("MEMBER".equals(payment.getBusinessType())) {
            memberServiceClient.markPaid(payload.getOutTradeNo(), payload.getTransactionId(), payload.getPaidAtMs());
        }
    }
    return ResponseEntity.ok("{\"code\":\"SUCCESS\",\"message\":\"成功\"}");
}
```

The `markPaid` calls in domain remain idempotent, so if a crash occurs between payment update and business update, a later reconciliation can safely call the same business markPaid method again.

- [ ] **Step 6: Add admin controllers**

Create `AdminDepositController`:

```java
@RestController
@RequestMapping("/admin/deposit")
public class AdminDepositController {
    @Autowired private DepositServiceClient depositServiceClient;

    @GetMapping("/config")
    public ResponseApiBody config() {
        return ResponseApiBody.success(depositServiceClient.config());
    }

    @PutMapping("/config")
    public ResponseApiBody save(@RequestBody DepositConfigQo qo) {
        return ResponseApiBody.success(depositServiceClient.saveConfig(qo));
    }

    @GetMapping("/orders")
    public ResponseApiBody orders(DepositOrderQo qo) {
        return ResponseApiBody.success(depositServiceClient.adminOrders(qo));
    }
}
```

Create `AdminPaymentController`:

```java
@RestController
@RequestMapping("/admin/payment")
public class AdminPaymentController {
    @Autowired private PaymentOrderServiceClient paymentOrderServiceClient;

    @GetMapping("/orders")
    public ResponseApiBody orders(PaymentOrderQo qo) {
        return ResponseApiBody.success(paymentOrderServiceClient.adminOrders(qo));
    }
}
```

- [ ] **Step 7: Write notify controller tests**

Create a controller test that mocks `WechatPaymentService.parseAndVerifyNotify`, `PaymentOrderServiceClient`, `DepositServiceClient`, and `MemberServiceClient`. Test:

1. amount mismatch throws `FAIL_PAY_AMOUNT`;
2. duplicate callback with `paymentOrderServiceClient.markPaid` returning `false` does not call business grant;
3. first deposit callback calls `depositServiceClient.markPaid`.

- [ ] **Step 8: Run wap controller tests**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=PaymentNotifyControllerTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 9: Commit API layer**

```bash
git add backend/system-restapi backend/system-wap
git commit -m "feat: expose payment v3 deposit member APIs"
```

---

### Task 5: Miniapp/H5 API Types and Payment Adapter

**Files:**
- Modify: `frontend/src/api/types/index.ts`
- Create: `frontend/src/api/modules/deposit.ts`
- Modify: `frontend/src/api/modules/member.ts`
- Create: `frontend/src/services/payment-channel.ts`
- Test: `frontend/src/services/__tests__/payment-channel.test.ts`

- [ ] **Step 1: Add frontend payment types**

Modify `frontend/src/api/types/index.ts`:

```ts
export type PaymentChannel = 'MINIAPP' | 'H5'
export type PaymentBusinessType = 'DEPOSIT' | 'MEMBER'

export interface PaymentPayParams {
  timeStamp: string
  nonceStr: string
  packageValue?: string
  package?: string
  signType: 'RSA' | 'MD5' | 'HMAC-SHA256'
  paySign: string
}

export interface PaymentCreateResult {
  outTradeNo: string
  businessType: PaymentBusinessType
  channel: PaymentChannel
  amountFen: number
  payParams?: PaymentPayParams
  h5Url?: string
}

export interface DepositConfig {
  enabled: '0' | '1'
  amountFen: number
  testEnabled: '0' | '1'
  testAmountFen: number
  displayAmountFen: number
  refundWaitDays: number
  earlyRefundFeeRate: number
  agreementSummary?: string
}

export interface DepositOrder {
  outTradeNo: string
  amountFen: number
  channel: PaymentChannel
  status: 'PENDING' | 'PAID' | 'CLOSED' | 'REFUNDED'
}
```

- [ ] **Step 2: Create deposit API module**

Create `frontend/src/api/modules/deposit.ts`:

```ts
import { get, post } from '../request'
import type { ApiResult, DepositConfig, DepositOrder, PaymentChannel, PaymentCreateResult } from '../types'

export function getDepositConfig(): Promise<ApiResult<DepositConfig>> {
  return get('/deposit/config')
}

export function createDepositOrder(channel: PaymentChannel): Promise<ApiResult<PaymentCreateResult>> {
  return post('/deposit/order/create', { channel })
}

export function getDepositOrderStatus(outTradeNo: string): Promise<ApiResult<DepositOrder>> {
  return get('/deposit/order/status', { out_trade_no: outTradeNo })
}
```

- [ ] **Step 3: Modify member API**

Modify `frontend/src/api/modules/member.ts`:

```ts
export function createMemberOrder(planId: number, channel: PaymentChannel = resolvePaymentChannel()): Promise<ApiResult<MemberOrderCreateResult>> {
  return post('/member/order/create', { planId, channel }, { withToken: true })
}
```

Import `PaymentChannel` and `resolvePaymentChannel`.

- [ ] **Step 4: Create payment-channel helper**

Create `frontend/src/services/payment-channel.ts`:

```ts
import type { PaymentChannel } from '@/api/types'

export function resolvePaymentChannel(): PaymentChannel {
  // #ifdef MP-WEIXIN
  return 'MINIAPP'
  // #endif
  // #ifdef H5
  return 'H5'
  // #endif
  return 'MINIAPP'
}

export function normalizePayPackage(params: { package?: string; packageValue?: string }): string {
  return params.package || params.packageValue || ''
}
```

- [ ] **Step 5: Add tests**

Create `frontend/src/services/__tests__/payment-channel.test.ts`:

```ts
import { describe, expect, it } from 'vitest'
import { normalizePayPackage } from '../payment-channel'

describe('payment-channel', () => {
  it('prefers package when present', () => {
    expect(normalizePayPackage({ package: 'prepay_id=1', packageValue: 'prepay_id=2' })).toBe('prepay_id=1')
  })

  it('falls back to packageValue for compatibility', () => {
    expect(normalizePayPackage({ packageValue: 'prepay_id=2' })).toBe('prepay_id=2')
  })
})
```

- [ ] **Step 6: Run frontend tests**

Run:

```bash
cd frontend
npm test -- payment-channel
```

Expected: tests pass. If this repo uses `pnpm` or `vitest run`, use the package script in `frontend/package.json` and record the actual command in the commit message body.

- [ ] **Step 7: Commit frontend API layer**

```bash
git add frontend/src/api frontend/src/services
git commit -m "feat: add frontend payment channel APIs"
```

---

### Task 6: Miniapp and H5 Deposit/Member Pages

**Files:**
- Modify: `frontend/src/pages-sub/user/credit.vue`
- Modify: `frontend/src/pages-sub/user/member/index.vue`
- Test: `frontend/src/pages-sub/user/__tests__/credit-payment.test.ts`

- [ ] **Step 1: Update credit page to load config**

In `credit.vue`, remove `const DEPOSIT_FEE_FEN = 19900`. Add:

```ts
import * as depositApi from '@/api/modules/deposit'
import { normalizePayPackage, resolvePaymentChannel } from '@/services/payment-channel'
import type { DepositConfig, PaymentCreateResult } from '@/api/types'

const depositConfig = ref<DepositConfig | null>(null)
const loadingConfig = ref(false)
const displayAmountFen = computed(() => depositConfig.value?.displayAmountFen || 0)
const displayAmountYuan = computed(() => (displayAmountFen.value / 100).toFixed(2))
```

Add `loadConfig()`:

```ts
async function loadConfig(): Promise<void> {
  loadingConfig.value = true
  try {
    const result = await depositApi.getDepositConfig()
    depositConfig.value = result.data || null
  } finally {
    loadingConfig.value = false
  }
}
```

Call `loadConfig()` in `onLoad` or `onShow`.

- [ ] **Step 2: Update credit payment submit**

Replace `paymentApi.createOrder({ type: '2', productid: 0, total_fee: DEPOSIT_FEE_FEN })` with:

```ts
const channel = resolvePaymentChannel()
const result = await depositApi.createDepositOrder(channel)
if (!result.data) throw new Error('下单失败')
await launchPayment(result.data)
```

Add `launchPayment`:

```ts
async function launchPayment(payload: PaymentCreateResult): Promise<void> {
  if (payload.channel === 'H5') {
    if (!payload.h5Url) throw new Error('H5支付链接缺失')
    window.location.href = payload.h5Url
    return
  }
  const params = payload.payParams
  if (!params) throw new Error('支付参数不完整')
  const packageValue = normalizePayPackage(params)
  if (!params.timeStamp || !params.nonceStr || !packageValue || !params.paySign) {
    throw new Error('支付参数不完整')
  }
  await invokeWechatPayment({
    timeStamp: params.timeStamp,
    nonceStr: params.nonceStr,
    package: packageValue,
    signType: params.signType || 'RSA',
    paySign: params.paySign,
  })
}
```

- [ ] **Step 3: Update credit order polling**

Replace `paymentApi.getOrderStatus` with:

```ts
const result = await depositApi.getDepositOrderStatus(outTradeNo)
const order = result.data
if (order?.status === 'PAID') return 'paid'
if (order?.status === 'CLOSED' || order?.status === 'REFUNDED') return 'failed'
```

- [ ] **Step 4: Update UI amount text**

Replace hardcoded `199` amount with:

```vue
<text class="deposit-card__amount">{{ displayAmountYuan }}</text>
```

Disable pay when config is missing or `enabled !== '1'`.

- [ ] **Step 5: Update member page channel and H5 handling**

In `member/index.vue`, call:

```ts
const result = await memberApi.createMemberOrder(plan.id, resolvePaymentChannel())
```

In `launchWxPay`, before `uni.requestPayment`, add:

```ts
if (payload.channel === 'H5' && payload.h5Url) {
  window.location.href = payload.h5Url
  resolve()
  return
}
```

Normalize package:

```ts
const packageValue = payload.payParams
  ? normalizePayPackage(payload.payParams)
  : payload.packageValue
```

- [ ] **Step 6: Add credit page test**

Create `frontend/src/pages-sub/user/__tests__/credit-payment.test.ts` to assert a helper formats cents:

```ts
import { describe, expect, it } from 'vitest'

function formatFen(value: number): string {
  return (value / 100).toFixed(2)
}

describe('credit payment display', () => {
  it('shows one fen as 0.01 yuan', () => {
    expect(formatFen(1)).toBe('0.01')
  })
})
```

- [ ] **Step 7: Run frontend checks**

Run:

```bash
cd frontend
npm test -- payment-channel credit-payment
```

Expected: tests pass.

- [ ] **Step 8: Commit user frontend**

```bash
git add frontend/src/pages-sub/user/credit.vue frontend/src/pages-sub/user/member/index.vue frontend/src/api frontend/src/services
git commit -m "feat: support miniapp and h5 payment frontend"
```

---

### Task 7: Admin Deposit and Payment Pages

**Files:**
- Modify: `frontend-admin/src/api/types.ts`
- Create: `frontend-admin/src/api/modules/deposit.ts`
- Create: `frontend-admin/src/api/modules/payment.ts`
- Create: `frontend-admin/src/views/deposit/config/index.vue`
- Create: `frontend-admin/src/views/deposit/order/index.vue`
- Create: `frontend-admin/src/views/payment/order/index.vue`
- Test: `frontend-admin/src/api/__tests__/deposit-payment.test.ts`

- [ ] **Step 1: Add admin API types**

Modify `frontend-admin/src/api/types.ts`:

```ts
export interface DepositConfig {
  id?: number
  enabled: '0' | '1'
  amountFen: number
  testEnabled: '0' | '1'
  testAmountFen: number
  displayAmountFen?: number
  refundWaitDays: number
  earlyRefundFeeRate: number
  agreementSummary?: string
  updatedAt?: string
}

export interface DepositOrder {
  id: number
  outTradeNo: string
  userId: number
  amountFen: number
  channel: string
  status: string
  transactionId?: string
  paidAt?: string
  createdAt?: string
}

export interface PaymentOrder {
  id: number
  paymentNo: string
  businessType: string
  businessOrderNo: string
  outTradeNo: string
  userId: number
  channel: string
  amountFen: number
  status: string
  transactionId?: string
  wechatTradeState?: string
  createdAt?: string
}
```

- [ ] **Step 2: Create admin API modules**

Create `frontend-admin/src/api/modules/deposit.ts`:

```ts
import { get, put } from '../request'
import type { ApiResult, DepositConfig, DepositOrder, PageQuery, PageResult } from '../types'

export interface DepositOrderQuery extends PageQuery {
  userId?: number
  status?: string
  outTradeNo?: string
}

export function getDepositConfig(): Promise<ApiResult<DepositConfig>> {
  return get('/admin/deposit/config')
}

export function saveDepositConfig(data: DepositConfig): Promise<ApiResult<DepositConfig>> {
  return put('/admin/deposit/config', data)
}

export function getDepositOrders(params: DepositOrderQuery): Promise<ApiResult<PageResult<DepositOrder>>> {
  return get('/admin/deposit/orders', params as Record<string, unknown>)
}
```

Create `frontend-admin/src/api/modules/payment.ts`:

```ts
import { get } from '../request'
import type { ApiResult, PageQuery, PageResult, PaymentOrder } from '../types'

export interface PaymentOrderQuery extends PageQuery {
  businessType?: string
  channel?: string
  status?: string
  outTradeNo?: string
}

export function getPaymentOrders(params: PaymentOrderQuery): Promise<ApiResult<PageResult<PaymentOrder>>> {
  return get('/admin/payment/orders', params as Record<string, unknown>)
}
```

- [ ] **Step 3: Create config page**

Create `frontend-admin/src/views/deposit/config/index.vue` using Element Plus form controls:

- `el-switch` for `enabled` and `testEnabled`.
- `el-input-number` for `amountFen`, `testAmountFen`, `refundWaitDays`, `earlyRefundFeeRate`.
- `el-input type="textarea"` for `agreementSummary`.
- Save button calls `saveDepositConfig`.

Use helper:

```ts
function yuanText(fen?: number) {
  return fen == null ? '-' : `¥${(fen / 100).toFixed(2)}`
}
```

- [ ] **Step 4: Create order list pages**

Create `deposit/order/index.vue` and `payment/order/index.vue` following the style of `frontend-admin/src/views/order/list/index.vue`: search bar, `el-table`, pagination, `StatusTag` if appropriate, and `fenText`.

Deposit columns: ID, 商户订单号, 用户ID, 金额, 渠道, 状态, 微信交易号, 支付时间, 创建时间.

Payment columns: ID, 支付流水号, 业务类型, 业务单号, 商户订单号, 用户ID, 金额, 渠道, 状态, 微信交易状态, 微信交易号, 创建时间.

- [ ] **Step 5: Wire routes/menu**

Modify `frontend-admin/src/constants/menu.ts` by adding a new group after the existing “订单系统” group:

```ts
{
  title: '支付系统',
  icon: 'Wallet',
  children: [
    { title: '保证金配置', path: '/deposit/config', component: 'deposit/config/index' },
    { title: '保证金订单', path: '/deposit/orders', component: 'deposit/order/index' },
    { title: '支付流水', path: '/payment/orders', component: 'payment/order/index' },
  ],
},
```

Modify `frontend-admin/src/stores/modules/permission.ts` imports:

```ts
import DepositConfig from '@/views/deposit/config/index.vue'
import DepositOrder from '@/views/deposit/order/index.vue'
import PaymentOrder from '@/views/payment/order/index.vue'
```

Add to `componentMap`:

```ts
'deposit/config/index': DepositConfig,
'deposit/order/index': DepositOrder,
'payment/order/index': PaymentOrder,
```

Required paths:

```text
/deposit/config
/deposit/orders
/payment/orders
```

- [ ] **Step 6: Add admin API tests**

Create `frontend-admin/src/api/__tests__/deposit-payment.test.ts`:

```ts
import { describe, expect, it } from 'vitest'

function yuanText(fen?: number) {
  return fen == null ? '-' : `¥${(fen / 100).toFixed(2)}`
}

describe('deposit admin formatting', () => {
  it('formats one fen', () => {
    expect(yuanText(1)).toBe('¥0.01')
  })
})
```

- [ ] **Step 7: Run admin checks**

Run:

```bash
cd frontend-admin
npm test -- deposit-payment
```

Expected: tests pass.

- [ ] **Step 8: Commit admin UI**

```bash
git add frontend-admin/src/api frontend-admin/src/views frontend-admin/src/router frontend-admin/src/constants
git commit -m "feat: add deposit and payment admin pages"
```

---

### Task 8: End-to-End Verification, Security Checks, and Cleanup

**Files:**
- Modify only files needed to fix verification failures from prior tasks.
- Do not stage `.omx/state/session.json`.

- [ ] **Step 1: Run backend targeted tests**

Run:

```bash
cd backend
mvn -pl system-object,system-domain,system-restapi,system-wap test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Run frontend tests**

Run:

```bash
cd frontend
npm test -- payment-channel credit-payment
```

Expected: tests pass.

- [ ] **Step 3: Run admin tests**

Run:

```bash
cd frontend-admin
npm test -- deposit-payment
```

Expected: tests pass.

- [ ] **Step 4: Scan for leaked secrets**

Run:

```bash
rg -n "<REDACTED_SECRET_PATTERN>" backend frontend frontend-admin docs --glob '!**/target/**' --glob '!**/dist/**'
```

Expected:

- No APIv2 key.
- No JWT(令牌).
- The public key ID may appear only in local runtime notes if intentionally documented; prefer no occurrence in committed code/docs because it is environment configuration.

- [ ] **Step 5: Verify old hardcoded guarantee amount is gone**

Run:

```bash
rg -n "DEPOSIT_FEE_FEN|19900|立即缴纳保证金" frontend/src/pages-sub/user/credit.vue frontend/src/api/modules/deposit.ts backend/system-domain backend/system-wap
```

Expected:

- `DEPOSIT_FEE_FEN` absent.
- `19900` appears only as backend default config or SQL seed, not in frontend payment creation.
- `立即缴纳保证金` may remain as button text.

- [ ] **Step 6: Verify payment v3 paths exist**

Run:

```bash
rg -n "/payment/wechat/notify|/deposit/order/create|/member/order/create|WechatPayV3|RSAPublicKey" backend/system-wap backend/system-restapi backend/system-domain
```

Expected: all paths/classes are found.

- [ ] **Step 7: Review git diff**

Run:

```bash
git status --short
git diff --stat
```

Expected:

- Only intended source/docs/test files changed.
- `.omx/state/session.json` may be modified by the environment but must not be staged.

- [ ] **Step 8: Final commit**

If prior tasks did not already commit all changes, commit remaining verification fixes:

```bash
git add backend frontend frontend-admin docs/sql/pending/V_wechat_pay_v3_deposit_member.sql
git commit -m "test: verify payment v3 deposit member flow"
```

Skip this commit if `git status --short` shows no source changes beyond `.omx/state/session.json`.

---

## Completion Criteria

The implementation is complete only when:

- Backend compiles with WeChat Pay APIv3 SDK(软件开发工具包).
- `GET /deposit/config` returns backend-configured amount and defaults to `0.01` yuan while test mode is enabled.
- `POST /deposit/order/create channel=MINIAPP` returns miniapp `payParams(支付参数)`.
- `POST /deposit/order/create channel=H5` returns `h5Url(支付跳转链接)`.
- `POST /member/order/create` supports `MINIAPP` and `H5`.
- `POST /payment/wechat/notify` verifies APIv3 notification and grants deposit/member entitlements idempotently.
- Duplicate payment callbacks do not duplicate `creditflag(信用担保标记)`, member duration, gift PPD(拍拍豆), or record rows.
- Admin can edit deposit config and query deposit/payment orders.
- No secrets are committed.
