# 拍豆场景与会员权益统一配置实施计划

> **给执行代理：** 必须使用 `superpowers:executing-plans`(按计划执行) 逐任务实现。当前仓库存在用户未提交修改，每次暂存和提交都只能包含任务明确列出的文件。

**目标：** 让发布约拍、发起约拍申请、查看联系方式三个场景的基础拍豆与会员优惠均可后台配置，并让后台、小程序展示和服务端实际扣费使用同一报价。

**架构：** 新增拍豆场景基础配置表，继续使用现有会员权益规则表。`MemberService`(会员服务) 聚合基础费用、会员状态与权益规则，向后台返回场景配置，向业务端返回用户报价；三个扣费服务在事务内重新报价。管理后台按场景分栏，小程序费用底栏使用已确认的 55% 费用摘要与 45% 提交按钮布局。

**技术栈：** Java 8、Spring Boot(Spring 启动框架)、Spring Data JPA(Spring 数据持久化)、MySQL(MySQL 数据库)、Vue 3(Vue 前端框架)、Pinia(Pinia 状态管理)、Element Plus(Element Plus 组件库)、uni-app(跨端应用框架)、Vitest(Vitest 测试框架)、JUnit 4(JUnit 测试框架)。

---

## 文件结构

### 后端数据与契约

- 新建 `backend/system-object/src/main/java/com/ypat/enums/PpdBenefitScene.java`：三个场景编码与中文名称。
- 新建 `backend/system-object/src/main/java/com/ypat/enums/MemberLevelCode.java`：会员等级编码与中文名称。
- 新建 `backend/system-object/src/main/java/com/ypat/PpdSceneConfigQo.java`：基础费用配置传输对象。
- 新建 `backend/system-object/src/main/java/com/ypat/MemberBenefitConfigQo.java`：后台场景聚合配置传输对象。
- 修改 `backend/system-object/src/main/java/com/ypat/MemberBenefitRuleQo.java`：增加中文名称字段。
- 修改 `backend/system-object/src/main/java/com/ypat/MemberBenefitQuoteQo.java`：增加场景与等级中文名称。
- 新建 `backend/system-domain/src/main/java/com/ypat/entity/PpdSceneConfig.java`：基础费用实体。
- 新建 `backend/system-domain/src/main/java/com/ypat/repository/PpdSceneConfigRepository.java`：按场景查询基础费用。
- 修改 `backend/system-domain/src/main/java/com/ypat/entity/Record.java`：记录扣费场景和中文说明。
- 新建 `backend/dev/mysql/20260713_create_ppd_scene_config.sql`：创建配置、补规则、扩展流水。
- 新建 `backend/dev/mysql/20260713_create_ppd_scene_config_rollback.sql`：回滚本次结构。

### 后端服务与接口

- 修改 `backend/system-domain/src/main/java/com/ypat/service/MemberService.java`：统一报价、后台聚合查询和事务保存。
- 修改 `backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java`：发布约拍按统一报价扣费。
- 修改 `backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java`：约拍申请按统一报价扣费。
- 修改 `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`：作品快捷申请按统一报价扣费。
- 修改 `backend/system-domain/src/main/java/com/ypat/service/UserService.java`：查看联系方式按统一报价扣费。
- 修改 `backend/system-restapi/src/main/java/com/ypat/controller/MemberController.java`：内部聚合配置接口。
- 修改 `backend/system-wap/src/main/java/com/ypat/service/MemberServiceClient.java`：Feign(声明式服务调用) 契约。
- 修改 `backend/system-wap/src/main/java/com/ypat/controller/MemberController.java`：三个场景报价白名单。
- 修改 `backend/system-wap/src/main/java/com/ypat/controller/AdminMemberController.java`：后台统一配置接口。

### 管理后台

- 修改 `frontend-admin/src/api/types.ts`：场景配置与中文名称类型。
- 修改 `frontend-admin/src/api/modules/member.ts`：统一配置查询和保存接口。
- 重写 `frontend-admin/src/views/member/rule/index.vue`：按场景分栏的中文权益配置。
- 新建 `frontend-admin/src/views/member/rule/member-rule.test.ts`：中文展示与交互契约。

### 小程序

- 修改 `frontend/src/api/types/index.ts`：三个场景联合类型和报价中文字段。
- 修改 `frontend/src/api/modules/member.ts`：放开三个报价场景。
- 修改 `frontend/src/stores/member.ts`：按场景缓存报价。
- 修改 `frontend/src/stores/__tests__/member.test.ts`：按场景缓存回归测试。
- 修改 `frontend/src/components/business/AppointmentPublishForm.vue`：发布页底部费用摘要。
- 修改 `frontend/src/components/business/__tests__/appointment-member-benefit.test.ts`：发布底栏报价测试。
- 修改 `frontend/src/components/business/__tests__/appointment-publish-form.test.ts`：发布布局源代码契约。
- 修改 `frontend/src/pages-sub/work/apply.vue`：报名页真实报价与 45% 按钮。
- 修改 `frontend/src/pages-sub/work/apply.test.ts`：报名报价、余额与样式契约。
- 修改 `frontend/src/pages-sub/content/message-detail.vue`：查看联系方式真实报价。
- 新建 `frontend/src/pages-sub/content/message-detail.test.ts`：联系方式报价测试。

---

### 任务 1：建立场景编码、数据契约与迁移

**文件：**

- 新建：`backend/system-object/src/main/java/com/ypat/enums/PpdBenefitScene.java`
- 新建：`backend/system-object/src/main/java/com/ypat/enums/MemberLevelCode.java`
- 新建：`backend/system-object/src/main/java/com/ypat/PpdSceneConfigQo.java`
- 新建：`backend/system-object/src/main/java/com/ypat/MemberBenefitConfigQo.java`
- 修改：`backend/system-object/src/main/java/com/ypat/MemberBenefitRuleQo.java`
- 修改：`backend/system-object/src/main/java/com/ypat/MemberBenefitQuoteQo.java`
- 新建：`backend/system-domain/src/main/java/com/ypat/entity/PpdSceneConfig.java`
- 新建：`backend/system-domain/src/main/java/com/ypat/repository/PpdSceneConfigRepository.java`
- 修改：`backend/system-domain/src/main/java/com/ypat/entity/Record.java`
- 新建：`backend/dev/mysql/20260713_create_ppd_scene_config.sql`
- 新建：`backend/dev/mysql/20260713_create_ppd_scene_config_rollback.sql`
- 测试：`backend/system-object/src/test/java/com/ypat/enums/PpdBenefitSceneTest.java`

- [ ] **步骤 1：先写场景和中文映射失败测试**

```java
public class PpdBenefitSceneTest {
    @Test
    public void mapsSupportedCodesToChineseNames() {
        assertEquals("发布约拍", PpdBenefitScene.fromCode("SUBMIT_YPAT").getLabel());
        assertEquals("发起约拍申请", PpdBenefitScene.fromCode("APPLY_YPAT").getLabel());
        assertEquals("查看联系方式", PpdBenefitScene.fromCode("VIEW_CONTACT").getLabel());
        assertNull(PpdBenefitScene.fromCode("UNKNOWN"));
    }
}
```

- [ ] **步骤 2：运行测试并确认因枚举不存在而失败**

运行：

```bash
cd backend
mvn -o -pl system-object -am -Dtest=PpdBenefitSceneTest -Dsurefire.failIfNoSpecifiedTests=false test
```

预期：失败原因包含 `PpdBenefitScene` 不存在。`-o`(离线模式) 禁止下载依赖；若本机缓存不完整，只记录环境阻塞，不执行联网下载。

- [ ] **步骤 3：实现固定场景和会员等级枚举**

```java
public enum PpdBenefitScene {
    SUBMIT_YPAT("发布约拍"),
    APPLY_YPAT("发起约拍申请"),
    VIEW_CONTACT("查看联系方式");

    private final String label;
    PpdBenefitScene(String label) { this.label = label; }
    public String getCode() { return name(); }
    public String getLabel() { return label; }
    public static PpdBenefitScene fromCode(String code) {
        if (code == null) return null;
        for (PpdBenefitScene item : values()) if (item.name().equals(code)) return item;
        return null;
    }
}
```

`MemberLevelCode`(会员等级编码)采用相同结构，固定映射 `BASIC`→基础会员、`PLUS`→高级会员、`PRO`→专业会员。

- [ ] **步骤 4：增加聚合配置和报价中文字段**

`MemberBenefitConfigQo`(会员权益聚合配置)必须包含：

```java
private String scene;
private String sceneName;
private Integer originalPpd;
private String description;
private Long version;
private List<MemberBenefitRuleQo> rules;
```

`MemberBenefitRuleQo` 增加 `levelName`、`sceneName`、`benefitTypeName`；`MemberBenefitQuoteQo` 增加 `sceneName`、`levelName`。逐一增加 `getLevelName/setLevelName`、`getSceneName/setSceneName`、`getBenefitTypeName/setBenefitTypeName` 方法，类型均为 `String`。

- [ ] **步骤 5：创建基础配置实体和仓储**

```java
@Entity
@Table(name = "t_ppd_scene_config", uniqueConstraints =
        @UniqueConstraint(name = "uk_ppd_scene", columnNames = "scene"))
public class PpdSceneConfig implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 32)
    private String scene;
    @Column(name = "original_ppd", nullable = false)
    private Integer originalPpd;
    @Column(length = 256)
    private String description;
    @Version
    private Long version;
    @Column(name = "updated_at") @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}
```

实体必须增加 `getId/setId`、`getScene/setScene`、`getOriginalPpd/setOriginalPpd`、`getDescription/setDescription`、`getVersion/setVersion`、`getUpdatedAt/setUpdatedAt`，返回值和参数类型与字段一致。

仓储提供 `PpdSceneConfig findByScene(String scene)` 和 `List<PpdSceneConfig> findAllByOrderBySceneAsc()`。

- [ ] **步骤 6：扩展拍豆流水实体**

在 `Record` 增加 `scene` 与 `description` 字段及访问方法。历史记录允许为空，新扣费记录必须写入中文说明。

- [ ] **步骤 7：编写前向与回滚迁移**

前向脚本完成：创建 `t_ppd_scene_config`、插入三个 3 拍豆场景、为 `t_record` 增加 `scene` 与 `description`、为三个会员等级补齐三个场景的拍豆减免规则。已有 `BASIC + SUBMIT_YPAT` 规则使用幂等插入保留原值；新增规则默认优惠 0、最低实扣 0、生效且启用。

回滚脚本按相反顺序删除新增规则、流水字段和配置表，并在文件头说明先备份与校验。

- [ ] **步骤 8：重新运行枚举测试与格式检查**

运行任务 1 的离线 Maven(Maven 构建工具) 定向测试，并运行：

```bash
git diff --check -- backend/system-object backend/system-domain/src/main/java/com/ypat/entity/Record.java backend/dev/mysql
```

预期：测试通过，格式检查无输出。

- [ ] **步骤 9：提交任务 1**

```bash
git add backend/system-object/src/main/java/com/ypat/enums/PpdBenefitScene.java \
  backend/system-object/src/main/java/com/ypat/enums/MemberLevelCode.java \
  backend/system-object/src/main/java/com/ypat/PpdSceneConfigQo.java \
  backend/system-object/src/main/java/com/ypat/MemberBenefitConfigQo.java \
  backend/system-object/src/main/java/com/ypat/MemberBenefitRuleQo.java \
  backend/system-object/src/main/java/com/ypat/MemberBenefitQuoteQo.java \
  backend/system-object/src/test/java/com/ypat/enums/PpdBenefitSceneTest.java \
  backend/system-domain/src/main/java/com/ypat/entity/PpdSceneConfig.java \
  backend/system-domain/src/main/java/com/ypat/repository/PpdSceneConfigRepository.java \
  backend/system-domain/src/main/java/com/ypat/entity/Record.java \
  backend/dev/mysql/20260713_create_ppd_scene_config.sql \
  backend/dev/mysql/20260713_create_ppd_scene_config_rollback.sql
git commit -m "feat(member): add configurable ppd scenes"
```

### 任务 2：实现统一报价和后台聚合保存

**文件：**

- 修改：`backend/system-domain/src/main/java/com/ypat/service/MemberService.java`
- 修改：`backend/system-domain/src/main/java/com/ypat/service/MemberBenefitCalculator.java`
- 修改：`backend/system-domain/src/main/java/com/ypat/repository/MemberBenefitRuleRepository.java`
- 测试：`backend/system-domain/src/test/java/com/ypat/service/MemberServiceBenefitTest.java`
- 测试：`backend/system-domain/src/test/java/com/ypat/service/MemberBenefitCalculatorTest.java`

- [ ] **步骤 1：先补三个场景报价失败测试**

在 `MemberServiceBenefitTest` 增加断言：配置原价 7 时非会员实扣 7；有效基础会员优惠 2 时申请场景实扣 5；查看联系方式规则停用时实扣原价。假仓储必须同时记录查询的场景编码。

```java
MemberBenefitQuoteQo quote = service.quoteBenefit(1L, "APPLY_YPAT");
assertEquals("发起约拍申请", quote.getSceneName());
assertEquals("基础会员", quote.getLevelName());
assertEquals(Integer.valueOf(7), quote.getOriginalPpd());
assertEquals(Integer.valueOf(2), quote.getDiscountPpd());
assertEquals(Integer.valueOf(5), quote.getActualPpd());
```

- [ ] **步骤 2：运行定向测试并确认当前申请场景原价为 0 导致失败**

```bash
cd backend
mvn -o -pl system-domain -am \
  -Dtest=MemberBenefitCalculatorTest,MemberServiceBenefitTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

- [ ] **步骤 3：让报价从配置仓储读取基础费用**

`quoteBenefit` 必须先通过 `PpdBenefitScene.fromCode(scene)` 校验白名单，再读取 `PpdSceneConfig`。迁移兼容期配置缺失时按当前常量 3 回退并写错误日志；不允许未知场景回退为 0。

```java
PpdBenefitScene supported = PpdBenefitScene.fromCode(scene);
if (supported == null) throw new SysException(ResponseCode.FAIL_PARA);
PpdSceneConfig config = ppdSceneConfigRepository.findByScene(scene);
int originalPpd = config == null ? Constant.PUB_NEED_PPD : config.getOriginalPpd();
```

报价完成后填充场景中文名和会员等级中文名。无会员时 `levelName` 为空，不伪装成基础会员。

- [ ] **步骤 4：完善计算边界**

`MemberBenefitCalculator` 保持固定减免模型，并明确：有效优惠不超过原价；最低实扣限制在 0 到原价之间；最终实扣在 0 到原价之间。增加优惠大于原价、最低实扣大于原价的测试。

- [ ] **步骤 5：实现后台聚合查询**

新增 `listBenefitConfigs()`，按 `PpdBenefitScene.values()` 固定顺序返回三个场景。每条规则复制后填充中文等级、中文场景和“拍豆减免”。规则仓储新增：

```java
List<MemberBenefitRule> findBySceneAndBenefitTypeOrderByLevelCodeAsc(
    String scene, String benefitType);
```

- [ ] **步骤 6：实现事务聚合保存与版本校验**

新增 `saveBenefitConfig(MemberBenefitConfigQo qo)`：校验场景、版本、非负整数、最低实扣不高于原价、等级与权益类型白名单；保存基础配置和当前场景全部规则。任何规则失败时事务整体回滚。

- [ ] **步骤 7：运行测试并提交**

运行步骤 2 的测试，预期全部通过。然后只提交本任务文件：

```bash
git add backend/system-domain/src/main/java/com/ypat/service/MemberService.java \
  backend/system-domain/src/main/java/com/ypat/service/MemberBenefitCalculator.java \
  backend/system-domain/src/main/java/com/ypat/repository/MemberBenefitRuleRepository.java \
  backend/system-domain/src/test/java/com/ypat/service/MemberServiceBenefitTest.java \
  backend/system-domain/src/test/java/com/ypat/service/MemberBenefitCalculatorTest.java
git commit -m "feat(member): unify ppd benefit quotes"
```

### 任务 3：开放三个场景报价与后台统一配置接口

**文件：**

- 修改：`backend/system-restapi/src/main/java/com/ypat/controller/MemberController.java`
- 修改：`backend/system-wap/src/main/java/com/ypat/service/MemberServiceClient.java`
- 修改：`backend/system-wap/src/main/java/com/ypat/controller/MemberController.java`
- 修改：`backend/system-wap/src/main/java/com/ypat/controller/AdminMemberController.java`
- 修改：`backend/system-wap/src/test/java/com/ypat/controller/MemberControllerTest.java`
- 修改：`backend/system-wap/src/test/java/com/ypat/controller/AdminMemberControllerTest.java`

- [ ] **步骤 1：先写接口失败测试**

测试公开报价允许三个场景并拒绝 `UNKNOWN`；后台查询返回三个聚合配置；后台保存必须把路径场景写入请求对象并转发。

```java
ResponseApiBody response = controller.updateBenefitConfig("APPLY_YPAT", qo);
assertEquals("APPLY_YPAT", client.lastSavedConfig.getScene());
assertEquals(qo, response.getRes());
```

- [ ] **步骤 2：运行 WAP(WAP 服务) 定向测试并确认接口不存在而失败**

```bash
cd backend
mvn -o -pl system-wap -am \
  -Dtest=MemberControllerTest,AdminMemberControllerTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

- [ ] **步骤 3：扩展内部接口与 Feign(声明式服务调用) 契约**

内部控制器新增：

```text
GET  /service/member/admin/benefit-configs
POST /service/member/admin/benefit-config/save
```

Feign 客户端增加 `List<MemberBenefitConfigQo> adminBenefitConfigs()` 与 `MemberBenefitConfigQo saveBenefitConfig(MemberBenefitConfigQo qo)`。

- [ ] **步骤 4：扩展后台接口**

`AdminMemberController` 新增：

```text
GET /admin/member/benefit-configs
PUT /admin/member/benefit-configs/{scene}
```

路径场景覆盖请求体场景，避免二者不一致。保留旧规则接口供回滚。

- [ ] **步骤 5：公开报价严格校验场景**

`MemberController.quote` 在调用 Feign 前使用场景枚举校验，不允许未知场景进入内部服务。

- [ ] **步骤 6：运行测试并提交**

运行步骤 2 的测试。预期全部通过，然后提交本任务六个文件，提交信息：

```bash
git add backend/system-restapi/src/main/java/com/ypat/controller/MemberController.java \
  backend/system-wap/src/main/java/com/ypat/service/MemberServiceClient.java \
  backend/system-wap/src/main/java/com/ypat/controller/MemberController.java \
  backend/system-wap/src/main/java/com/ypat/controller/AdminMemberController.java \
  backend/system-wap/src/test/java/com/ypat/controller/MemberControllerTest.java \
  backend/system-wap/src/test/java/com/ypat/controller/AdminMemberControllerTest.java
git commit -m "feat(member): expose ppd benefit configuration api"
```

### 任务 4：让三个业务入口按事务内报价扣费

**文件：**

- 修改：`backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java`
- 修改：`backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java`
- 修改：`backend/system-domain/src/main/java/com/ypat/service/WorkService.java`
- 修改：`backend/system-domain/src/main/java/com/ypat/service/UserService.java`
- 新建：`backend/system-domain/src/test/java/com/ypat/service/PpdBenefitChargeSourceTest.java`

- [ ] **步骤 1：写固定常量仍被使用的失败测试**

读取四个服务源代码并断言扣费段不再包含 `Constant.APPLY_NEED_PPD`、`Constant.VIEW_NEED_PPD` 或发布常量，且分别调用 `quoteBenefit` 的三个场景。

```java
assertTrue(messSource.contains("quoteBenefit(userid, PpdBenefitScene.APPLY_YPAT.getCode())"));
assertTrue(workSource.contains("quoteBenefit(viewerId, PpdBenefitScene.APPLY_YPAT.getCode())"));
assertTrue(userSource.contains("quoteBenefit(id, PpdBenefitScene.VIEW_CONTACT.getCode())"));
```

- [ ] **步骤 2：运行测试并确认当前固定常量导致失败**

```bash
cd backend
mvn -o -pl system-domain -am -Dtest=PpdBenefitChargeSourceTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

- [ ] **步骤 3：注入会员服务并替换固定扣费**

四个服务均在锁定用户并完成重复操作校验后获取报价：

```java
MemberBenefitQuoteQo quote = memberService.quoteBenefit(userId, scene.getCode());
int actualPpd = quote.getActualPpd() == null ? 0 : quote.getActualPpd();
if (balance < actualPpd) throw new SysException(ResponseCode.FAIL_BALANCE);
user.setPpd(balance - actualPpd);
```

流水写 `-actualPpd`、场景编码和中文说明。0 拍豆仍写业务结果，但不创建 0 值拍豆流水。

- [ ] **步骤 4：保持去重顺序**

约拍申请、作品快捷申请和联系方式解锁必须先检查是否已完成，再报价扣费。联系方式已解锁时直接返回，不调用报价。

- [ ] **步骤 5：运行测试和精确搜索**

```bash
rg -n "Constant\.(PUB_NEED_PPD|APPLY_NEED_PPD|VIEW_NEED_PPD)" \
  backend/system-domain/src/main/java/com/ypat/service/{YpatInfoService,MessInfoService,WorkService,UserService}.java
```

预期：四个扣费段无固定常量。运行步骤 2 测试后提交任务文件：

```bash
git add backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java \
  backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java \
  backend/system-domain/src/main/java/com/ypat/service/WorkService.java \
  backend/system-domain/src/main/java/com/ypat/service/UserService.java \
  backend/system-domain/src/test/java/com/ypat/service/PpdBenefitChargeSourceTest.java
git commit -m "feat(ppd): charge configured member quotes"
```

### 任务 5：扩展管理后台类型和接口

**文件：**

- 修改：`frontend-admin/src/api/types.ts`
- 修改：`frontend-admin/src/api/modules/member.ts`
- 修改：`frontend-admin/src/api/__tests__/member.test.ts`

- [ ] **步骤 1：先写统一接口失败测试**

```ts
it('loads and saves aggregated benefit configs', async () => {
  const list = await getMemberBenefitConfigs()
  const saved = await saveMemberBenefitConfig('APPLY_YPAT', payload)
  expect((list.data as MockResponseData).url).toBe('/admin/member/benefit-configs')
  expect((saved.data as MockResponseData).url).toBe('/admin/member/benefit-configs/APPLY_YPAT')
})
```

- [ ] **步骤 2：运行定向测试并确认导出函数不存在而失败**

```bash
cd frontend-admin
CI=true pnpm exec vitest run src/api/__tests__/member.test.ts
```

- [ ] **步骤 3：增加明确类型**

新增 `PpdBenefitScene`(拍豆权益场景) 联合类型、`PpdSceneConfig`(拍豆场景配置) 和 `MemberBenefitConfig`(会员权益聚合配置)。现有规则类型增加三个中文名称字段。

- [ ] **步骤 4：实现接口函数**

```ts
export function getMemberBenefitConfigs(): Promise<ApiResult<MemberBenefitConfig[]>> {
  return get<MemberBenefitConfig[]>('/admin/member/benefit-configs')
}

export function saveMemberBenefitConfig(
  scene: PpdBenefitScene,
  data: MemberBenefitConfig,
): Promise<ApiResult<MemberBenefitConfig>> {
  return put<MemberBenefitConfig>(`/admin/member/benefit-configs/${scene}`, data)
}
```

- [ ] **步骤 5：运行测试、代码规范检查并提交**

```bash
CI=true pnpm exec vitest run src/api/__tests__/member.test.ts
pnpm exec eslint src/api/types.ts src/api/modules/member.ts src/api/__tests__/member.test.ts
git add src/api/types.ts src/api/modules/member.ts src/api/__tests__/member.test.ts
git commit -m "feat(admin): add member benefit config api"
```

### 任务 6：重做中文权益配置页面

**文件：**

- 修改：`frontend-admin/src/views/member/rule/index.vue`
- 新建：`frontend-admin/src/views/member/rule/member-rule.test.ts`

- [ ] **步骤 1：写中文场景和无英文编码失败测试**

测试源代码包含“发布约拍、发起约拍申请、查看联系方式、基础消耗拍豆、基础会员、高级会员、专业会员”，且模板不直接插值 `row.levelCode`、`row.scene`、`row.benefitType`。

- [ ] **步骤 2：运行测试并确认旧页面仍显示英文编码而失败**

```bash
cd frontend-admin
CI=true pnpm exec vitest run src/views/member/rule/member-rule.test.ts
```

- [ ] **步骤 3：实现按场景分栏布局**

页面顶部使用三个 `el-tabs`(标签页)。当前标签下展示基础消耗行和会员规则表，不使用分页。表格列为：会员等级、优惠拍豆、最低实扣、生效、状态、说明、操作。

- [ ] **步骤 4：实现编辑与保存状态**

基础消耗使用数字步进器；规则弹窗只读展示中文等级、中文场景、中文权益类型；生效和状态使用开关。保存当前场景时一次提交基础配置与全部规则，并携带版本号。

- [ ] **步骤 5：实现错误反馈**

校验最低实扣不高于基础消耗。保存成功重新加载当前场景；并发冲突提示刷新；保存期间只禁用当前场景控件。

- [ ] **步骤 6：运行测试、定向规范检查并提交**

```bash
CI=true pnpm exec vitest run src/views/member/rule/member-rule.test.ts src/api/__tests__/member.test.ts
pnpm exec eslint src/views/member/rule/index.vue src/views/member/rule/member-rule.test.ts
git add src/views/member/rule/index.vue src/views/member/rule/member-rule.test.ts
git commit -m "feat(admin): redesign benefit config by scene"
```

### 任务 7：让小程序按场景缓存真实报价

**文件：**

- 修改：`frontend/src/api/types/index.ts`
- 修改：`frontend/src/api/modules/member.ts`
- 修改：`frontend/src/stores/member.ts`
- 修改：`frontend/src/stores/__tests__/member.test.ts`

- [ ] **步骤 1：写按场景缓存失败测试**

连续刷新发布与申请报价，断言两个结果同时保留，并验证接口分别收到两个场景。

```ts
expect(store.quotes.SUBMIT_YPAT?.actualPpd).toBe(1)
expect(store.quotes.APPLY_YPAT?.actualPpd).toBe(2)
```

- [ ] **步骤 2：运行测试并确认当前单一报价被覆盖或方法不存在**

```bash
cd frontend
CI=true pnpm exec vitest run src/stores/__tests__/member.test.ts
```

- [ ] **步骤 3：扩展接口场景联合类型**

```ts
export type PpdBenefitScene = 'SUBMIT_YPAT' | 'APPLY_YPAT' | 'VIEW_CONTACT'
```

报价类型增加 `sceneName`、`levelName`。接口参数改用该联合类型。

- [ ] **步骤 4：重构状态仓库**

使用 `ref<Partial<Record<PpdBenefitScene, MemberBenefitQuote>>>({})` 保存报价，提供 `refreshBenefitQuote(scene)`。保留 `submitYpatQuote` 计算属性作为过渡兼容，直到发布组件迁移完成。

- [ ] **步骤 5：运行测试、规范检查并提交**

```bash
CI=true pnpm exec vitest run src/stores/__tests__/member.test.ts
pnpm exec eslint src/api/types/index.ts src/api/modules/member.ts src/stores/member.ts src/stores/__tests__/member.test.ts --quiet
git add src/api/types/index.ts src/api/modules/member.ts src/stores/member.ts src/stores/__tests__/member.test.ts
git commit -m "feat(frontend): cache ppd quotes by scene"
```

### 任务 8：将发布约拍费用移入 45% 按钮底栏

**文件：**

- 修改：`frontend/src/components/business/AppointmentPublishForm.vue`
- 修改：`frontend/src/components/business/__tests__/appointment-member-benefit.test.ts`
- 修改：`frontend/src/components/business/__tests__/appointment-publish-form.test.ts`

- [ ] **步骤 1：写底栏费用失败测试**

挂载有效报价，断言表单中部不再有 `appointment-publish-form__benefit`，固定底栏包含“本次实扣 1 拍豆、原价 3、会员优惠 -2、发布约拍”。源代码测试断言按钮宽度为 45%。

- [ ] **步骤 2：运行测试并确认旧费用卡片和全宽按钮导致失败**

```bash
cd frontend
CI=true pnpm exec vitest run \
  src/components/business/__tests__/appointment-member-benefit.test.ts \
  src/components/business/__tests__/appointment-publish-form.test.ts
```

- [ ] **步骤 3：加载发布报价并处理错误状态**

挂载时调用 `refreshBenefitQuote('SUBMIT_YPAT')`。报价加载中或失败时禁用发布；失败摘要可点击重试，不允许未知价格提交。

- [ ] **步骤 4：实现已确认底栏**

底栏为横向费用摘要与按钮。费用摘要宽度自适应，按钮 `width: 45%`。实扣金额使用 `$color-primary`(主题色)，存在优惠时原价加删除线，无优惠时展示“暂无会员优惠”。正文占位按底栏实际高度加安全区。

- [ ] **步骤 5：运行测试、规范检查并提交**

```bash
CI=true pnpm exec vitest run \
  src/components/business/__tests__/appointment-member-benefit.test.ts \
  src/components/business/__tests__/appointment-publish-form.test.ts
pnpm exec eslint src/components/business/AppointmentPublishForm.vue \
  src/components/business/__tests__/appointment-member-benefit.test.ts \
  src/components/business/__tests__/appointment-publish-form.test.ts --quiet
git add src/components/business/AppointmentPublishForm.vue \
  src/components/business/__tests__/appointment-member-benefit.test.ts \
  src/components/business/__tests__/appointment-publish-form.test.ts
git commit -m "feat(frontend): move publish quote into action bar"
```

### 任务 9：让报名底栏展示会员报价并按实扣判断余额

**文件：**

- 修改：`frontend/src/pages-sub/work/apply.vue`
- 修改：`frontend/src/pages-sub/work/apply.test.ts`

- [ ] **步骤 1：写真实报价和主题色失败测试**

断言页面使用 `APPLY_YPAT` 报价、展示余额/原价/优惠/实扣、余额不足比较 `actualPpd`、充值提示使用实扣、按钮宽度 45%，并断言 `.bottom-bar__value` 使用 `$color-primary`。

- [ ] **步骤 2：运行测试并确认当前固定 `APPLY_PPD` 和橙色导致失败**

```bash
cd frontend
CI=true pnpm exec vitest run src/pages-sub/work/apply.test.ts
```

- [ ] **步骤 3：接入申请报价**

页面显示时刷新 `APPLY_YPAT` 报价。`applyCost` 改为报价 `actualPpd`，`originalApplyCost` 与 `discountPpd` 分别用于第二层文案。报价失败时禁用提交并提供重试。

- [ ] **步骤 4：调整余额和提交逻辑**

余额不足、充值弹窗和提交前检查全部使用当前报价实扣。服务端仍负责最终重新报价；前端不把报价金额传入业务接口。

- [ ] **步骤 5：实现 45% 按钮与主题色**

保留固定底栏安全区，费用摘要 55%、按钮 45%。删除 `.bottom-bar__value--cost` 的正文色覆盖，所有关键数值统一使用主题色。

- [ ] **步骤 6：运行测试、规范检查并提交**

```bash
CI=true pnpm exec vitest run src/pages-sub/work/apply.test.ts
pnpm exec eslint src/pages-sub/work/apply.vue src/pages-sub/work/apply.test.ts --quiet
git add src/pages-sub/work/apply.vue src/pages-sub/work/apply.test.ts
git commit -m "feat(frontend): show member quote on apply bar"
```

### 任务 10：让查看联系方式展示并使用真实报价

**文件：**

- 修改：`frontend/src/pages-sub/content/message-detail.vue`
- 新建：`frontend/src/pages-sub/content/message-detail.test.ts`

- [ ] **步骤 1：写报价确认失败测试**

测试源代码不再引用 `VIEW_CONTACT_PPD` 固定常量，加载 `VIEW_CONTACT` 报价，确认弹窗包含原价、会员优惠与实扣，余额不足比较实扣。

- [ ] **步骤 2：运行测试并确认固定常量导致失败**

```bash
cd frontend
CI=true pnpm exec vitest run src/pages-sub/content/message-detail.test.ts
```

- [ ] **步骤 3：加载联系方式报价**

页面加载消息详情时并行刷新 `VIEW_CONTACT` 报价。报价失败时“查看联系方式”按钮展示费用加载失败并提供重试，不调用解锁接口。

- [ ] **步骤 4：更新按钮和确认弹窗**

按钮显示实扣金额。存在优惠时弹窗为“本次实扣 N 拍豆；原价 N 拍豆，某会员优惠 N 拍豆”；无优惠时显示“暂无会员优惠”。已解锁后不显示费用按钮。

- [ ] **步骤 5：运行测试、规范检查并提交**

```bash
CI=true pnpm exec vitest run src/pages-sub/content/message-detail.test.ts
pnpm exec eslint src/pages-sub/content/message-detail.vue src/pages-sub/content/message-detail.test.ts --quiet
git add src/pages-sub/content/message-detail.vue src/pages-sub/content/message-detail.test.ts
git commit -m "feat(frontend): quote contact unlock cost"
```

### 任务 11：执行端到端定向核对

**文件：**

- 核对：`docs/superpowers/specs/2026-07-13-ppd-scene-member-benefit-design.md`
- 核对：本计划所有修改文件

- [ ] **步骤 1：检查需求关键字和固定常量残留**

```bash
rg -n "SUBMIT_YPAT|APPLY_YPAT|VIEW_CONTACT|originalPpd|discountPpd|actualPpd" \
  backend frontend frontend-admin
rg -n "Constant\.(PUB_NEED_PPD|APPLY_NEED_PPD|VIEW_NEED_PPD)|const applyCost = APPLY_PPD" \
  backend/system-domain/src/main/java/com/ypat/service frontend/src
```

预期：三个场景贯通；业务扣费与页面余额判断不再依赖固定常量。常量定义本身可以保留用于迁移回退。

- [ ] **步骤 2：运行后端定向测试**

```bash
cd backend
mvn -o -pl system-domain,system-wap -am \
  -Dtest=PpdBenefitSceneTest,MemberBenefitCalculatorTest,MemberServiceBenefitTest,PpdBenefitChargeSourceTest,MemberControllerTest,AdminMemberControllerTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

不运行完整构建，不允许 Maven(Maven 构建工具) 联网下载。

- [ ] **步骤 3：运行管理后台定向测试和规范检查**

```bash
cd frontend-admin
CI=true pnpm exec vitest run \
  src/api/__tests__/member.test.ts \
  src/views/member/rule/member-rule.test.ts
pnpm exec eslint \
  src/api/types.ts \
  src/api/modules/member.ts \
  src/api/__tests__/member.test.ts \
  src/views/member/rule/index.vue \
  src/views/member/rule/member-rule.test.ts
```

- [ ] **步骤 4：运行小程序定向测试和规范检查**

```bash
cd frontend
CI=true pnpm exec vitest run \
  src/stores/__tests__/member.test.ts \
  src/components/business/__tests__/appointment-member-benefit.test.ts \
  src/components/business/__tests__/appointment-publish-form.test.ts \
  src/pages-sub/work/apply.test.ts \
  src/pages-sub/content/message-detail.test.ts
pnpm exec eslint \
  src/api/types/index.ts \
  src/api/modules/member.ts \
  src/stores/member.ts \
  src/stores/__tests__/member.test.ts \
  src/components/business/AppointmentPublishForm.vue \
  src/components/business/__tests__/appointment-member-benefit.test.ts \
  src/components/business/__tests__/appointment-publish-form.test.ts \
  src/pages-sub/work/apply.vue \
  src/pages-sub/work/apply.test.ts \
  src/pages-sub/content/message-detail.vue \
  src/pages-sub/content/message-detail.test.ts --quiet
```

- [ ] **步骤 5：检查差异和验收条目**

```bash
git diff --check
git status --short
```

逐项核对设计文档第 18 节八条验收标准。明确记录未执行完整构建和真机测试，由用户手动完成。

- [ ] **步骤 6：提交最终测试补充或文档修正**

只有步骤 1 至 5 发现并修正了遗漏时才创建最终提交；不得把工作区中与本计划无关的现有修改加入提交。
