# 后台内测数据资源选择弹窗实施计划

> **供智能执行代理使用：** 必须使用 superpowers:subagent-driven-development(子代理驱动开发，推荐) 或 superpowers:executing-plans(计划执行) 逐项实施。使用复选框跟踪每一步状态。

**目标：** 将内测作品组和约拍图片改为可筛选、可分页的弹窗选择，并让一条约拍安全保存最多 9 张手动选择的图片。

**架构：** 前端新增共享 `ResourcePickerDialog`(资源选择弹窗组件)，作品模式渲染作品组单选表格，约拍模式渲染图片多选表格和只读视频表格。后端扩展作品组查询的占用筛选，限制一次只能提交一个作品组，并按请求顺序验证和保存多张约拍图片；现有事务和资源原子占用继续保证一致性。

**技术栈：** Vue 3.5(前端框架)、TypeScript(类型脚本)、Element Plus 2.14(组件库)、Vitest(前端测试框架)、Spring Boot(后端框架)、Spring Data JPA(数据访问框架)、JUnit 4(Java 测试框架)

**执行限制：** 不下载依赖，不执行完整构建。只运行与改动直接相关的前端单元测试和格式检查；Java 测试命令保留给开发者手动执行，实施代理不运行 Maven(项目构建工具)。

---

## 文件边界

新建：

- `frontend-admin/src/views/internal-test/generator/resource-picker-selection.ts`：作品单选和约拍 9 张上限的纯状态逻辑。
- `frontend-admin/src/views/internal-test/generator/ResourcePickerDialog.vue`：资源弹窗、独立筛选、分页、跨页选择和作品组详情。
- `frontend-admin/tests/unit/internal-test-resource-picker-selection.test.ts`：纯选择逻辑测试。
- `backend/system-object/src/test/java/com/ypat/SysExceptionTest.java`：跨服务错误消息测试。

修改：

- `frontend-admin/src/views/internal-test/generator/index.vue`：移除作品组下拉框，接入弹窗和资源摘要。
- `frontend-admin/src/api/modules/internal-test.ts`：补充作品组占用状态类型。
- `frontend-admin/tests/unit/internal-test-workbench-source.test.ts`：约束弹窗和生成页结构。
- `frontend-admin/tests/unit/internal-test-api.test.ts`：约束约拍资源编号请求。
- `backend/system-object/src/main/java/com/ypat/SysException.java`：保留跨服务自定义错误原因。
- `backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java`：资源组查询增加占用参数。
- `backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java`：资源组分页尊重占用筛选。
- `backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java`：作品单组校验和约拍多图片保存。
- `backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java`：后端源代码契约测试。

## 任务 1：保留跨服务详细错误消息

**文件：**

- 修改：`backend/system-object/src/main/java/com/ypat/SysException.java:21`
- 测试：`backend/system-object/src/test/java/com/ypat/SysExceptionTest.java`

- [ ] **步骤 1：编写失败测试**

```java
package com.ypat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SysExceptionTest {
    @Test
    public void customMessageIsExposedToCrossServiceErrorResponse() {
        SysException exception = new SysException(
                ResponseCode.FAIL_PARA, "约拍资源已被占用，请重新选择");

        assertEquals("{code=1002, msg='约拍资源已被占用，请重新选择'}", exception.getMessage());
        assertEquals("约拍资源已被占用，请重新选择", exception.getMsg());
    }
}
```

- [ ] **步骤 2：确认旧实现不能满足测试**

检查 `super(responseCode.toString())`，确认它只向 Spring(后端框架)错误响应暴露默认“参数错误”。遵照执行限制，不运行 Maven(项目构建工具)。

- [ ] **步骤 3：实施最小修复**

```java
public SysException(ResponseCode responseCode, String msg) {
    super("{code=" + responseCode.getCode() + ", msg='" + msg + "'}");
    this.code = responseCode.getCode();
    this.msg = msg;
    this.responseCode = responseCode;
}
```

- [ ] **步骤 4：执行静态检查**

```bash
git -c core.whitespace=cr-at-eol diff --check -- \
  backend/system-object/src/main/java/com/ypat/SysException.java \
  backend/system-object/src/test/java/com/ypat/SysExceptionTest.java
```

预期：退出码为 `0`。开发者可手动运行：

```bash
cd backend && mvn -pl system-object -Dtest=SysExceptionTest test
```

- [ ] **步骤 5：提交**

```bash
git add backend/system-object/src/main/java/com/ypat/SysException.java \
  backend/system-object/src/test/java/com/ypat/SysExceptionTest.java
git commit -m "fix: 保留跨服务业务错误详情"
```

## 任务 2：让作品组接口支持占用状态筛选

**文件：**

- 修改：`backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java:59-165`
- 修改：`backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java:108-181`
- 修改：`frontend-admin/src/api/modules/internal-test.ts:68-74`
- 测试：`backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java`

- [ ] **步骤 1：增加失败契约测试**

```java
@Test
public void resourceGroupQueryRespectsUsedFlag() throws Exception {
    String service = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java");
    String repository = read("backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java");

    assertFalse(service.contains("qo.setUsedFlag(0);"));
    assertTrue(service.contains("Integer usedFlag = qo.getUsedFlag();"));
    assertTrue(service.contains("group.put(\"usedFlag\""));
    assertTrue(repository.contains("(:usedFlag is null or r.used_flag = :usedFlag)"));
    assertTrue(repository.contains("@Param(\"usedFlag\") Integer usedFlag"));
}
```

- [ ] **步骤 2：确认旧查询固定为未占用**

```bash
rg -n "qo.setUsedFlag\(0\)|used_flag = 0" \
  backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java \
  backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java
```

预期：找到服务强制设为 `0` 和 SQL(数据库查询语句)固定 `used_flag = 0`。

- [ ] **步骤 3：参数化四个仓储方法**

以下方法均在 `keyword`(关键词)后增加 `Integer usedFlag`(占用状态)：

```java
List<String> findAvailableGroupNos(String mediaType, String styleCode, String profession,
        String province, String city, String area, String groupNo, String keyword,
        Integer usedFlag, int offset, int limit);

Long countAvailableGroups(String mediaType, String styleCode, String profession,
        String province, String city, String area, String groupNo, String keyword,
        Integer usedFlag);

List<InternalTestResource> findAvailableSingleResources(String mediaType, String styleCode,
        String profession, String province, String city, String area, String groupNo,
        String keyword, Integer usedFlag, int offset, int limit);

Long countAvailableSingleResources(String mediaType, String styleCode, String profession,
        String province, String city, String area, String groupNo, String keyword,
        Integer usedFlag);
```

四段 SQL(数据库查询语句)将固定条件替换为：

```sql
(:usedFlag is null or r.used_flag = :usedFlag)
```

并补充：

```java
@Param("usedFlag") Integer usedFlag
```

- [ ] **步骤 4：服务层传递筛选并返回组状态**

移除 `qo.setUsedFlag(0)`，保留作品用途和启用状态固定约束：

```java
Integer usedFlag = qo.getUsedFlag();
long groupCount = valueOrZero(internalTestResourceRepository.countAvailableGroups(
        mediaType, styleCode, profession, province, city, area, filterGroupNo, keyword, usedFlag));
long singleCount = valueOrZero(internalTestResourceRepository.countAvailableSingleResources(
        mediaType, styleCode, profession, province, city, area, filterGroupNo, keyword, usedFlag));
```

两个分页调用同样传入 `usedFlag`。将完整性判断改为按筛选匹配：

```java
private boolean matchesWorkGroupFilter(InternalTestResource resource, Integer usedFlag) {
    return resource != null
            && InternalTestResourceUsageType.work.value.equals(resource.getUsageType())
            && InternalTestResourceStatus.enabled.value.equals(resource.getStatus())
            && (usedFlag == null || Integer.valueOf(usedFlag).equals(resource.getUsedFlag()));
}
```

组视图增加：

```java
group.put("usedFlag", first == null ? null : first.getUsedFlag());
```

同步更新 `InternalTestDataSourceTest`(内测数据源测试)中的仓储动态代理：四个方法新增参数后，`usedFlag`(占用状态)、`offset`(偏移量)和 `limit`(数量限制)的参数下标都向后移动一位。代理应按方法名读取新下标，确保资源组分页测试不是因为代理参数错位而通过。

前端类型增加：

```ts
export interface InternalTestResourceGroup {
  groupNo: string
  groupTitle?: string
  mediaType?: string
  usedFlag?: number
  resources: InternalTestResource[]
}
```

- [ ] **步骤 5：执行静态检查并提交**

```bash
git diff --check -- \
  backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java \
  backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java \
  backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java \
  frontend-admin/src/api/modules/internal-test.ts
git add backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java \
  backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java \
  backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java \
  frontend-admin/src/api/modules/internal-test.ts
git commit -m "feat: 支持作品组占用状态筛选"
```

开发者可手动运行：

```bash
cd backend && mvn -pl system-domain -Dtest=InternalTestDataSourceTest test
```

## 任务 3：实现作品单组和约拍多图片后端生成

**文件：**

- 修改：`backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java:109-139,427-460,701-747`
- 测试：`backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java`

- [ ] **步骤 1：增加失败契约测试**

```java
@Test
public void generationRequiresOneWorkGroupAndPersistsSelectedYpatImages() throws Exception {
    String service = read("backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java");

    assertTrue(service.contains("qo.getGroupNos().size() != 1"));
    assertTrue(service.contains("一次只能选择一个作品组"));
    assertTrue(service.contains("qo.getYpatResourceIds().size() > YPAT_RESOURCE_LIMIT"));
    assertTrue(service.contains("约拍图片最多选择9张"));
    assertTrue(service.contains("loadSelectedYpatResources(qo.getYpatResourceIds())"));
    assertTrue(service.contains("saveYpatImages(ypat, resources)"));
    assertTrue(service.contains("markResourcesUsed(resources"));
    assertTrue(service.contains("约拍资源包含视频"));
}
```

- [ ] **步骤 2：确认旧流程只保存一张图片**

```bash
rg -n "pick\(resources, 0\)|usedResources.add\(resource\)" \
  backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java
```

预期：找到只取首张资源并只占用一张的旧逻辑。

- [ ] **步骤 3：限制作品只能提交一个组**

增加常量：

```java
private static final int YPAT_RESOURCE_LIMIT = 9;
```

校验使用：

```java
if (qo == null || qo.getUserId() == null || CollectionUtils.isEmpty(qo.getGroupNos())) {
    throw new SysException(ResponseCode.FAIL_PARA, "请选择作品组");
}
if (qo.getGroupNos().size() != 1) {
    throw new SysException(ResponseCode.FAIL_PARA, "一次只能选择一个作品组");
}
```

`generateWorks`(生成作品)直接读取唯一组：

```java
String groupNo = qo.getGroupNos().get(0);
List<InternalTestResource> group = loadAvailableWorkGroup(groupNo);
Work work = createWorkFromGroup(user, group, qo, batchNo);
internalTestResourceService.markResourcesUsed(group, batchNo, "work", work.getId());
return buildBatch(batchNo, 0, 0, 1);
```

- [ ] **步骤 4：校验约拍资源编号**

在 `validateYpatGeneration`(校验约拍生成参数)增加：

```java
if (CollectionUtils.isEmpty(qo.getYpatResourceIds())) {
    throw new SysException(ResponseCode.FAIL_PARA, "约拍图片不能为空");
}
if (qo.getYpatResourceIds().size() > YPAT_RESOURCE_LIMIT) {
    throw new SysException(ResponseCode.FAIL_PARA, "约拍图片最多选择9张");
}
if (new HashSet<Long>(qo.getYpatResourceIds()).size() != qo.getYpatResourceIds().size()) {
    throw new SysException(ResponseCode.FAIL_PARA, "约拍图片不能重复");
}
```

- [ ] **步骤 5：按请求顺序加载并校验约拍资源**

新增 `java.util.HashMap` 导入和函数：

```java
private List<InternalTestResource> loadSelectedYpatResources(List<Long> resourceIds) {
    List<InternalTestResource> found = internalTestResourceRepository.findAll(resourceIds);
    Map<Long, InternalTestResource> byId = new HashMap<Long, InternalTestResource>();
    for (InternalTestResource resource : found) {
        byId.put(resource.getId(), resource);
    }

    List<InternalTestResource> ordered = new ArrayList<InternalTestResource>();
    for (Long resourceId : resourceIds) {
        InternalTestResource resource = byId.get(resourceId);
        if (resource == null) {
            throw new SysException(ResponseCode.FAIL_PARA, "约拍资源不存在，请重新选择");
        }
        if (!InternalTestResourceStatus.enabled.value.equals(resource.getStatus())) {
            throw new SysException(ResponseCode.FAIL_PARA, "约拍资源未启用，请重新选择");
        }
        if (Integer.valueOf(1).equals(resource.getUsedFlag())) {
            throw new SysException(ResponseCode.FAIL_PARA, "约拍资源已被占用，请重新选择");
        }
        if (!InternalTestResourceUsageType.ypat.value.equals(resource.getUsageType())) {
            throw new SysException(ResponseCode.FAIL_PARA, "约拍资源用途错误");
        }
        if (!InternalTestResourceMediaType.image.value.equals(resource.getMediaType())) {
            throw new SysException(ResponseCode.FAIL_PARA, "约拍资源包含视频");
        }
        ordered.add(resource);
    }
    return ordered;
}
```

不得使用主表单 `styleCode`(发布风格)过滤手动选择的资源。

- [ ] **步骤 6：拆分约拍主表和图片保存**

保留 `resource`(首张资源)参数用于兼容旧版自动生成约拍时的默认描述，只删除方法末尾创建单张图片的副作用：

```java
private YpatInfo createYpat(User user, InternalTestResource resource,
                            InternalTestGenerateQo qo, String batchNo) {
    Date now = new Date();
    YpatInfo ypat = new YpatInfo();
    ypat.setDescrib(defaultString(qo.getDescrib(),
            defaultString(resource.getDescription(), "内测约拍内容")));
    ypat.setTarget(defaultString(qo.getTarget(), resolveTarget(qo.getTemplateType())));
    ypat.setPatdate(resolvePatdate(qo.getPatdate(), now));
    ypat.setPatarea(defaultString(qo.getCity(), defaultString(user.getCity(), "杭州市")));
    ypat.setPatslice(defaultString(qo.getPatslice(), "全天"));
    ypat.setChargeway("0");
    ypat.setChargeamt(BigDecimal.ZERO);
    ypat.setProvince(defaultString(qo.getProvince(), user.getProvince()));
    ypat.setCity(defaultString(qo.getCity(), user.getCity()));
    ypat.setArea(defaultString(qo.getArea(), user.getArea()));
    ypat.setCreditflag(YesNo.yes.value);
    ypat.setRealnameflag(YesNo.yes.value);
    ypat.setPatstyle(resolveYpatStyle(qo));
    ypat.setStatus(resolveYpatStatus(qo.getPublishStatus()));
    ypat.setPubdate(now);
    ypat.setReadtimes(0);
    ypat.setPattimes(0);
    ypat.setColtimes(0);
    ypat.setUser(user);
    ypat.setRecomflag(YesNo.no.value);
    ypat.setIsNationwide(0);
    ypat.setDataFlag(InternalTestDataFlag.internalTest.value);
    ypat.setInternalBatchNo(batchNo);
    return ypatInfoRepository.save(ypat);
}
```

新增图片保存函数：

```java
private void saveYpatImages(YpatInfo ypat, List<InternalTestResource> resources) {
    for (InternalTestResource resource : resources) {
        YpatImg img = new YpatImg();
        img.setYpatid(ypat.getId());
        img.setType("0");
        img.setImgpath(resource.getUrl());
        ypatImgRepository.save(img);
    }
}
```

明确更新两个调用点：

```java
// generateYpats
List<InternalTestResource> resources = loadSelectedYpatResources(qo.getYpatResourceIds());
YpatInfo ypat = createYpat(user, resources.get(0), qo, batchNo);
saveYpatImages(ypat, resources);
internalTestResourceService.markResourcesUsed(resources, batchNo, "ypat", ypat.getId());

// 兼容旧 generate 流程
YpatInfo ypat = createYpat(users.get(i), resource, qo, batchNo);
saveYpatImages(ypat, Collections.singletonList(resource));
```

- [ ] **步骤 7：静态检查并提交**

```bash
git diff --check -- \
  backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java \
  backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java
git add backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java \
  backend/system-domain/src/test/java/com/ypat/service/InternalTestDataSourceTest.java
git commit -m "feat: 支持内测约拍多图片生成"
```

开发者可手动运行：

```bash
cd backend && mvn -pl system-domain -Dtest=InternalTestDataSourceTest test
```

## 任务 4：实现可测试的前端选择状态

**文件：**

- 新建：`frontend-admin/src/views/internal-test/generator/resource-picker-selection.ts`
- 新建：`frontend-admin/tests/unit/internal-test-resource-picker-selection.test.ts`

- [ ] **步骤 1：编写失败测试**

```ts
import { describe, expect, it } from 'vitest'
import type { InternalTestResource, InternalTestResourceGroup } from '@/api/modules/internal-test'
import {
  YPAT_RESOURCE_LIMIT,
  replaceWorkGroupSelection,
  toggleYpatResourceSelection,
} from '@/views/internal-test/generator/resource-picker-selection'

const resource = (id: number): InternalTestResource => ({ id, mediaType: 'image', usageType: 'ypat' })
const group = (groupNo: string): InternalTestResourceGroup => ({ groupNo, resources: [] })

describe('内测资源选择状态', () => {
  it('作品组始终只保留最后勾选的一组', () => {
    expect(replaceWorkGroupSelection(group('G1'), true)?.groupNo).toBe('G1')
    expect(replaceWorkGroupSelection(group('G2'), true)?.groupNo).toBe('G2')
    expect(replaceWorkGroupSelection(group('G2'), false)).toBeUndefined()
  })

  it('约拍图片跨页累积且最多保留9张', () => {
    let selected: InternalTestResource[] = []
    for (let id = 1; id <= YPAT_RESOURCE_LIMIT; id += 1) {
      selected = toggleYpatResourceSelection(selected, resource(id), true).selected
    }
    const overflow = toggleYpatResourceSelection(selected, resource(10), true)
    expect(overflow.selected).toHaveLength(9)
    expect(overflow.limitReached).toBe(true)
    expect(toggleYpatResourceSelection(selected, resource(3), false).selected.map(item => item.id)).not.toContain(3)
  })
})
```

- [ ] **步骤 2：运行测试并确认失败**

```bash
cd frontend-admin && pnpm vitest run tests/unit/internal-test-resource-picker-selection.test.ts
```

预期：失败，提示状态模块不存在。

- [ ] **步骤 3：实现最小状态逻辑**

```ts
import type { InternalTestResource, InternalTestResourceGroup } from '@/api/modules/internal-test'

export const YPAT_RESOURCE_LIMIT = 9

export function replaceWorkGroupSelection(
  group: InternalTestResourceGroup,
  checked: boolean,
): InternalTestResourceGroup | undefined {
  return checked ? group : undefined
}

export function toggleYpatResourceSelection(
  selected: InternalTestResource[],
  resource: InternalTestResource,
  checked: boolean,
): { selected: InternalTestResource[]; limitReached: boolean } {
  const withoutCurrent = selected.filter(item => item.id !== resource.id)
  if (!checked) return { selected: withoutCurrent, limitReached: false }
  if (withoutCurrent.length >= YPAT_RESOURCE_LIMIT) return { selected, limitReached: true }
  return { selected: [...withoutCurrent, resource], limitReached: false }
}
```

- [ ] **步骤 4：运行测试并确认通过**

```bash
cd frontend-admin && pnpm vitest run tests/unit/internal-test-resource-picker-selection.test.ts
```

预期：`2` 个测试通过。

- [ ] **步骤 5：提交**

```bash
git add frontend-admin/src/views/internal-test/generator/resource-picker-selection.ts \
  frontend-admin/tests/unit/internal-test-resource-picker-selection.test.ts
git commit -m "test: 覆盖内测资源选择状态"
```

## 任务 5：实现共享资源选择弹窗

**文件：**

- 新建：`frontend-admin/src/views/internal-test/generator/ResourcePickerDialog.vue`
- 修改：`frontend-admin/tests/unit/internal-test-workbench-source.test.ts`

- [ ] **步骤 1：增加失败结构契约**

```ts
it('资源弹窗支持独立筛选分页和两种选择模式', () => {
  const dialog = source('src/views/internal-test/generator/ResourcePickerDialog.vue')

  expect(dialog).toContain("type PickerMode = 'work' | 'ypat'")
  expect(dialog).toContain('getInternalResourceGroups')
  expect(dialog).toContain('getInternalResources')
  expect(dialog).toContain('<el-tabs')
  expect(dialog).toContain('type="selection"')
  expect(dialog).toContain('@select-all="handleSelectAll"')
  expect(dialog).toContain('toggleYpatResourceSelection')
  expect(dialog).toContain('当前约拍仅支持图片资源')
  expect(dialog).toContain('查看详情')
  expect(dialog).toContain('<el-pagination')
  expect(dialog).not.toContain('props.styleCodes')
})
```

- [ ] **步骤 2：运行测试并确认失败**

```bash
cd frontend-admin && pnpm vitest run tests/unit/internal-test-workbench-source.test.ts
```

预期：失败，提示弹窗组件不存在。

- [ ] **步骤 3：定义组件契约和状态**

```ts
type PickerMode = 'work' | 'ypat'

const props = defineProps<{
  visible: boolean
  mode: PickerMode
  selectedWorkGroup?: InternalTestResourceGroup
  selectedYpatResources: InternalTestResource[]
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  confirmWork: [group: InternalTestResourceGroup | undefined]
  confirmYpat: [resources: InternalTestResource[]]
}>()

const activeMediaType = ref(InternalTestMediaType.IMAGE.value)
const workRows = ref<InternalTestResourceGroup[]>([])
const ypatRows = ref<InternalTestResource[]>([])
const pendingWorkGroup = ref<InternalTestResourceGroup>()
const pendingYpatResources = ref<InternalTestResource[]>([])
const query = reactive({ keyword: '', styleCode: '', usedFlag: 0 as number | undefined, page: 0, size: 10 })
const ypatRegionPath = ref<RegionPath>([])
const total = ref(0)
const loading = ref(false)
```

打开弹窗时复制外部选择到临时状态；取消或关闭只发送 `update:visible`(更新显示状态)，不发送确认事件。

- [ ] **步骤 4：实现独立查询**

作品请求：

```ts
const res = await getInternalResourceGroups({
  mediaType: activeMediaType.value,
  usageType: InternalTestUsageType.WORK.value,
  styleCode: query.styleCode || undefined,
  usedFlag: query.usedFlag,
  keyword: query.keyword.trim() || undefined,
  page: query.page,
  size: query.size,
})
```

约拍请求：

```ts
const region = toRegionFields(ypatRegionPath.value)
const res = await getInternalResources({
  mediaType: activeMediaType.value,
  usageType: InternalTestUsageType.YPAT.value,
  styleCode: query.styleCode || undefined,
  province: region.province || undefined,
  city: region.city || undefined,
  area: region.area || undefined,
  usedFlag: query.usedFlag,
  keyword: query.keyword.trim() || undefined,
  page: query.page,
  size: query.size,
})
```

主表单风格不作为组件属性。查询、重置、翻页和标签切换只修改弹窗内部状态。

弹窗风格选项由模式决定，不共享主表单值：

```ts
const styleOptions = computed(() => props.mode === 'work'
  ? getWorkTagStyleOptions()
  : getYpatPatstyleOptions())
```

- [ ] **步骤 5：实现跨页选择与限制**

```ts
function canSelectYpat(row: InternalTestResource): boolean {
  if (activeMediaType.value === InternalTestMediaType.VIDEO.value) return false
  if (row.usedFlag === InternalTestResourceUsedFlag.USED.value) return false
  const selected = pendingYpatResources.value.some(item => item.id === row.id)
  return selected || pendingYpatResources.value.length < YPAT_RESOURCE_LIMIT
}

function canSelectWorkGroup(row: InternalTestResourceGroup): boolean {
  return row.usedFlag !== InternalTestResourceUsedFlag.USED.value
}

function handleWorkSelect(selection: InternalTestResourceGroup[], row: InternalTestResourceGroup): void {
  const checked = selection.some(item => item.groupNo === row.groupNo)
  pendingWorkGroup.value = replaceWorkGroupSelection(row, checked)
  syncCurrentPageSelection()
}

function handleYpatSelect(selection: InternalTestResource[], row: InternalTestResource): void {
  const checked = selection.some(item => item.id === row.id)
  const result = toggleYpatResourceSelection(pendingYpatResources.value, row, checked)
  pendingYpatResources.value = result.selected
  if (result.limitReached) ElMessage.warning('约拍图片最多选择9张')
  syncCurrentPageSelection()
}
```

`syncCurrentPageSelection`(同步当前页选择)通过表格引用先清空当前页勾选，再按 `groupNo`(作品组编号)或资源 `id`(编号)恢复。作品选择列自定义空表头，不提供全选；约拍图片使用标准表头复选框。`handleSelectAll`(处理全选)按表格顺序加入，达到 9 张后停止。视频页 `selectable`(是否可选)始终返回 `false`。

- [ ] **步骤 6：实现模板和详情**

模板必须包含以下骨架：

```vue
<el-dialog :model-value="visible" title="选择资源" width="960px" destroy-on-close @close="cancel">
  <el-tabs v-model="activeMediaType" @tab-change="mediaTypeChanged">
    <el-tab-pane label="图片" :name="InternalTestMediaType.IMAGE.value" />
    <el-tab-pane label="视频" :name="InternalTestMediaType.VIDEO.value" />
  </el-tabs>
  <el-alert
    v-if="mode === 'ypat' && activeMediaType === InternalTestMediaType.VIDEO.value"
    title="当前约拍仅支持图片资源"
    type="info"
    :closable="false"
  />
  <el-form :inline="true" :model="query" class="picker-filter" @submit.prevent>
    <el-form-item label="关键词">
      <el-input v-model="query.keyword" clearable placeholder="标题、URL、备注" />
    </el-form-item>
    <el-form-item label="风格">
      <el-select v-model="query.styleCode" clearable placeholder="全部">
        <el-option v-for="item in styleOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </el-form-item>
    <el-form-item v-if="mode === 'ypat'" label="地区">
      <el-cascader v-model="ypatRegionPath" :options="regionCascaderOptions" :props="regionCascaderProps" clearable />
    </el-form-item>
    <el-form-item label="占用状态">
      <el-select v-model="query.usedFlag" placeholder="请选择">
        <el-option label="未占用" :value="InternalTestResourceUsedFlag.UNUSED.value" />
        <el-option label="已占用" :value="InternalTestResourceUsedFlag.USED.value" />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </el-form-item>
  </el-form>

  <el-table
    v-if="mode === 'work'"
    ref="workTableRef"
    v-loading="loading"
    :data="workRows"
    row-key="groupNo"
    empty-text="暂无可用作品组"
    border
    @select="handleWorkSelect"
  >
    <el-table-column type="selection" width="48" :selectable="canSelectWorkGroup"><template #header><span /></template></el-table-column>
    <el-table-column prop="groupTitle" label="作品组" min-width="180" />
    <el-table-column label="资源数" width="90"><template #default="{ row }">{{ row.resources.length }}</template></el-table-column>
    <el-table-column label="占用状态" width="100"><template #default="{ row }">{{ row.usedFlag === 1 ? '已占用' : '未占用' }}</template></el-table-column>
    <el-table-column label="操作" width="100"><template #default="{ row }"><el-button link type="primary" @click="openGroupDetail(row)">查看详情</el-button></template></el-table-column>
  </el-table>

  <el-table
    v-else
    ref="ypatTableRef"
    v-loading="loading"
    :data="ypatRows"
    row-key="id"
    empty-text="暂无可用约拍资源"
    border
    @select="handleYpatSelect"
    @select-all="handleSelectAll"
  >
    <el-table-column type="selection" width="48" :selectable="canSelectYpat" />
    <el-table-column label="预览" width="96"><template #default="{ row }"><el-image v-if="row.mediaType === 'image'" :src="row.url" fit="cover" /></template></el-table-column>
    <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
    <el-table-column prop="styleCode" label="风格" width="100" />
    <el-table-column prop="city" label="城市" width="120" />
    <el-table-column label="占用状态" width="100"><template #default="{ row }">{{ row.usedFlag === 1 ? '已占用' : '未占用' }}</template></el-table-column>
  </el-table>
  <el-pagination
    :current-page="query.page + 1"
    :page-size="query.size"
    :total="total"
    layout="total, sizes, prev, pager, next, jumper"
    @current-change="pageChanged"
    @size-change="sizeChanged"
  />
  <template #footer>
    <span>已选 {{ selectedCount }} 项</span>
    <el-button @click="cancel">取消</el-button>
    <el-button type="primary" :disabled="selectedCount === 0" @click="confirm">确认选择</el-button>
  </template>
</el-dialog>
```

作品表格操作列调用 `openGroupDetail(row)`(打开作品组详情)。详情弹窗使用固定尺寸网格显示 `row.resources`(组内资源)，图片可预览，视频只显示类型、标题和地址，不创建播放器。

样式为选择列、预览图和底部操作设置稳定尺寸：表格预览图为 `72px × 72px`，生成页摘要缩略图为 `48px × 48px`，弹窗底部使用左右布局并允许窄屏换行，避免加载和选择状态改变布局。

- [ ] **步骤 7：运行测试和格式检查**

```bash
cd frontend-admin && pnpm vitest run \
  tests/unit/internal-test-resource-picker-selection.test.ts \
  tests/unit/internal-test-workbench-source.test.ts
pnpm eslint \
  src/views/internal-test/generator/ResourcePickerDialog.vue \
  src/views/internal-test/generator/resource-picker-selection.ts \
  tests/unit/internal-test-resource-picker-selection.test.ts \
  tests/unit/internal-test-workbench-source.test.ts
```

预期：测试通过，格式检查退出码为 `0`。

- [ ] **步骤 8：提交**

```bash
git add frontend-admin/src/views/internal-test/generator/ResourcePickerDialog.vue \
  frontend-admin/tests/unit/internal-test-workbench-source.test.ts
git commit -m "feat: 添加内测资源选择弹窗"
```

## 任务 6：将资源弹窗接入生成页面

**文件：**

- 修改：`frontend-admin/src/views/internal-test/generator/index.vue:1-438`
- 修改：`frontend-admin/tests/unit/internal-test-workbench-source.test.ts`
- 修改：`frontend-admin/tests/unit/internal-test-api.test.ts`

- [ ] **步骤 1：增加失败的生成页契约**

```ts
it('生成页明确提交一个作品组或多张约拍图片', () => {
  const page = source('src/views/internal-test/generator/index.vue')

  expect(page).toContain("import ResourcePickerDialog from './ResourcePickerDialog.vue'")
  expect(page).toContain('selectedWorkGroup')
  expect(page).toContain('selectedYpatResources')
  expect(page).toContain('groupNos: isCreateWorks.value && selectedWorkGroup.value')
  expect(page).toContain('ypatResourceIds: isCreateYpats.value')
  expect(page).toContain('请选择约拍图片')
  expect(page).toContain('选择作品组')
  expect(page).toContain('选择约拍图片')
  expect(page).not.toContain('<el-select v-model="form.groupNos"')
  expect(page).not.toContain('loadWorkGroups')
})
```

API(应用程序接口)测试补充：

```ts
await api.generateInternalYpats({
  actionType: 'create_ypats', userId: 1, wx: 'wx-test', mobile: '13800138000',
  ypatResourceIds: [101, 102],
})
expect(postMock).toHaveBeenCalledWith('/admin/internal-test/generate/ypats', {
  actionType: 'create_ypats', userId: 1, wx: 'wx-test', mobile: '13800138000',
  ypatResourceIds: [101, 102],
})
```

- [ ] **步骤 2：运行测试并确认失败**

```bash
cd frontend-admin && pnpm vitest run \
  tests/unit/internal-test-workbench-source.test.ts \
  tests/unit/internal-test-api.test.ts
```

预期：页面仍含作品组下拉框，且未提交约拍资源编号。

- [ ] **步骤 3：替换资源状态和事件**

移除 `workGroups`(作品组列表)、`activeWorkMediaType`(作品媒体标签)、`loadWorkGroups`(加载作品组)、`styleChanged`(风格变化加载资源)和 `workMediaChanged`(作品媒体变化)。新增：

```ts
import ResourcePickerDialog from './ResourcePickerDialog.vue'

const resourcePickerVisible = ref(false)
const resourcePickerMode = ref<'work' | 'ypat'>('work')
const selectedWorkGroup = ref<InternalTestResourceGroup>()
const selectedYpatResources = ref<InternalTestResource[]>([])

function openResourcePicker(mode: 'work' | 'ypat'): void {
  resourcePickerMode.value = mode
  resourcePickerVisible.value = true
}
function confirmWorkGroup(group: InternalTestResourceGroup | undefined): void {
  selectedWorkGroup.value = group
}
function confirmYpatResources(resources: InternalTestResource[]): void {
  selectedYpatResources.value = resources
}
```

- [ ] **步骤 4：更新校验和请求构建**

```ts
if (isCreateWorks.value && !selectedWorkGroup.value) return '请选择作品组'
if (isCreateYpats.value && selectedYpatResources.value.length === 0) return '请选择约拍图片'

groupNos: isCreateWorks.value && selectedWorkGroup.value
  ? [selectedWorkGroup.value.groupNo]
  : undefined,
ypatResourceIds: isCreateYpats.value
  ? selectedYpatResources.value.map(item => item.id!)
  : undefined,
```

保留 `styleCodes: form.styleCodes` 和 `styleCode: form.styleCodes[0]`，不得将它们传给弹窗。

- [ ] **步骤 5：替换页面资源控件**

```vue
<el-form-item v-if="isCreateWorks" label="作品组">
  <div class="resource-selection-summary">
    <el-button @click="openResourcePicker('work')">选择作品组</el-button>
    <span v-if="selectedWorkGroup">
      {{ workGroupLabel(selectedWorkGroup) }}，共 {{ selectedWorkGroup.resources.length }} 个资源
    </span>
    <el-button v-if="selectedWorkGroup" link type="danger" @click="selectedWorkGroup = undefined">清空</el-button>
  </div>
</el-form-item>

<el-form-item v-if="isCreateYpats" label="约拍图片">
  <div class="resource-selection-summary">
    <el-button @click="openResourcePicker('ypat')">选择约拍图片</el-button>
    <span v-if="selectedYpatResources.length">已选 {{ selectedYpatResources.length }} 张</span>
    <div v-if="selectedYpatResources.length" class="selected-resource-thumbnails">
      <el-image
        v-for="resource in selectedYpatResources.slice(0, 3)"
        :key="resource.id"
        :src="resource.url"
        fit="cover"
      />
      <span v-if="selectedYpatResources.length > 3">+{{ selectedYpatResources.length - 3 }}</span>
    </div>
    <el-button v-if="selectedYpatResources.length" link type="danger" @click="selectedYpatResources = []">清空</el-button>
  </div>
</el-form-item>

<ResourcePickerDialog
  v-model:visible="resourcePickerVisible"
  :mode="resourcePickerMode"
  :selected-work-group="selectedWorkGroup"
  :selected-ypat-resources="selectedYpatResources"
  @confirm-work="confirmWorkGroup"
  @confirm-ypat="confirmYpatResources"
/>
```

- [ ] **步骤 6：运行定向测试和格式检查**

```bash
cd frontend-admin && pnpm vitest run \
  tests/unit/internal-test-resource-picker-selection.test.ts \
  tests/unit/internal-test-workbench-source.test.ts \
  tests/unit/internal-test-api.test.ts
pnpm eslint \
  src/views/internal-test/generator/index.vue \
  src/views/internal-test/generator/ResourcePickerDialog.vue \
  src/views/internal-test/generator/resource-picker-selection.ts \
  src/api/modules/internal-test.ts \
  tests/unit/internal-test-resource-picker-selection.test.ts \
  tests/unit/internal-test-workbench-source.test.ts \
  tests/unit/internal-test-api.test.ts
```

预期：定向测试通过，格式检查退出码为 `0`。不运行 `pnpm build`(前端完整构建)。

- [ ] **步骤 7：提交**

```bash
git add frontend-admin/src/views/internal-test/generator/index.vue \
  frontend-admin/src/api/modules/internal-test.ts \
  frontend-admin/tests/unit/internal-test-workbench-source.test.ts \
  frontend-admin/tests/unit/internal-test-api.test.ts
git commit -m "feat: 接入内测资源弹窗选择"
```

## 任务 7：最终静态验证和手工验收

- [ ] **步骤 1：检查改动范围和空白**

```bash
git status --short
for subject in \
  "fix: 保留跨服务业务错误详情" \
  "feat: 支持作品组占用状态筛选" \
  "feat: 支持内测约拍多图片生成" \
  "test: 覆盖内测资源选择状态" \
  "feat: 添加内测资源选择弹窗" \
  "feat: 接入内测资源弹窗选择"
do
  commit=$(git log -1 --format=%H --grep="^${subject}$")
  test -n "$commit" && git show --check --oneline "$commit"
done
```

预期：六个功能提交均能找到且没有空白错误；本功能提交不包含 `.env`(环境变量文件)、部署编排或其他用户改动。

- [ ] **步骤 2：检查冲突标记**

```bash
rg -n "<<<<<<<|=======|>>>>>>>" \
  frontend-admin/src/views/internal-test/generator \
  frontend-admin/src/api/modules/internal-test.ts \
  backend/system-domain/src/main/java/com/ypat/service/InternalTestDataService.java \
  backend/system-domain/src/main/java/com/ypat/service/InternalTestResourceService.java \
  backend/system-domain/src/main/java/com/ypat/repository/InternalTestResourceRepository.java
```

预期：无输出。

- [ ] **步骤 3：运行前端定向测试**

```bash
cd frontend-admin && pnpm vitest run \
  tests/unit/internal-test-resource-picker-selection.test.ts \
  tests/unit/internal-test-workbench-source.test.ts \
  tests/unit/internal-test-api.test.ts
```

预期：相关测试全部通过。

- [ ] **步骤 4：记录未执行的后端命令**

交付说明必须明确以下命令未运行：

```bash
cd backend && mvn -pl system-object,system-domain \
  -Dtest=SysExceptionTest,InternalTestDataSourceTest test
```

- [ ] **步骤 5：按以下清单手工验收**

1. 作品弹窗图片、视频标签可切换，筛选和分页独立工作。
2. 作品组使用行复选框单选，选择新组替换旧组，详情展示完整组资源。
3. 约拍图片支持跨页选择，达到 9 张后禁止继续选择。
4. 约拍视频页可以筛选和浏览，但不能勾选。
5. 主表单风格改变不会刷新或过滤资源弹窗。
6. 取消弹窗不改变原选择，确认后才回填摘要。
7. 提交作品只生成一条作品和该组全部媒体。
8. 提交多张约拍图片只生成一条约拍和多条约拍图片记录。
9. 提交前占用任一资源后，整个请求失败且不产生部分数据。
10. 后端返回具体业务原因，不再只返回“参数错误”。
