# Admin Enum Consistency Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 统一后端、新版管理后台 `frontend-admin(新版管理后台)`、新版小程序 `frontend(新版小程序)` 的职业与风格/主题标签口径。

**Architecture:** 后端 `WorkDictController(作品标签字典控制器)` 继续作为 29 个作品主题标签的来源；前端各自维护同一份 fallback(兜底) 常量，并通过解析函数兼容旧数字风格、中文风格和新 code(编码)。职业值以后端 `UserProfess(用户职业枚举)` 为准，新增 `9 摄像师`，公开选项只暴露 6 类，历史值只用于展示兼容。

**Tech Stack:** Java 8 + Spring Boot 1.5 + Maven(后端构建工具)，Vue 3 + TypeScript + Vite + Vitest(前端测试工具)，Element Plus(新版后台组件库)，uni-app(新版小程序框架)。

---

## Constraints

- 不修改 `backend/system-web(旧版后台)`。
- 不修改 `91pai-master(旧小程序)`。
- 不迁移历史数据库数据。
- 不改变内测资源表结构；`styleCode` 新写入主题标签 code(编码)。
- 不暂存或提交 `.omx/state/session.json`。

## File Structure

- Modify: `backend/system-object/src/main/java/com/ypat/enums/UserProfess.java`，新增 `9 摄像师`、把 `2` 展示为“化妆师”、提供公开职业选项和历史展示兼容。
- Modify: `backend/system-object/src/main/java/com/ypat/UserQo.java`，放宽 `profess(职业)` 长度校验以接受 `9`，保留一位字符串。
- Modify: `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`，把职业 `9` 映射到 `YpatTarget.xsxj(约摄像师)` 对应值。
- Create: `backend/system-object/src/test/java/com/ypat/enums/UserProfessTest.java`，覆盖职业值、公开选项和历史展示。
- Modify: `backend/system-domain/src/test/java/com/ypat/service/WorkServiceAdminSourceTest.java`，用 source test(源码测试) 覆盖 `9 摄像师 -> 约摄像师`。
- Modify: `backend/system-wap/src/test/java/com/ypat/controller/WorkDictControllerSourceTest.java`，断言 29 个默认标签完整、顺序稳定。
- Modify: `frontend-admin/src/constants/enums.ts`，新增 29 个 `WorkTagStyle(作品主题标签)`、风格解析函数、职业公开选项与历史展示函数。
- Create: `frontend-admin/src/constants/__tests__/enums.test.ts`，覆盖新版后台主题标签与职业常量。
- Modify: `frontend-admin/src/views/internal-test/resource/index.vue`，资源风格下拉改为主题标签，显示层兼容旧风格。
- Modify: `frontend-admin/src/views/internal-test/generator/index.vue`，内测生成风格下拉改为主题标签，职业下拉改为 6 类。
- Modify: `frontend/src/constants/enums.ts`，新增 `VIDEOGRAPHER: '9'`、公开职业选项、职业展示函数，把 `PHOTO_STYLES(摄影风格列表)` 对齐 29 个主题标签名称。
- Modify: `frontend/src/constants/work-tags.ts`，fallback(兜底) 改为 29 个带 code/name/sortNo(编码/名称/排序) 的常量，并保留 `WORK_TAGS_FALLBACK` 名称数组。
- Create or Modify: `frontend/src/constants/__tests__/enums.test.ts`，覆盖新版小程序职业和 `PHOTO_STYLES(摄影风格列表)`。
- Modify: `frontend/src/constants/__tests__/work-tags.test.ts`，覆盖 29 个主题标签 fallback(兜底)。
- Modify as needed: `frontend/src/pages-sub/user/profile.vue`、`frontend/src/pages-sub/work/filter.vue`、`frontend/src/pages/work/index.vue`、`frontend/src/pages/mine/index.vue` 中直接使用旧职业选项的位置，改为 `PUBLIC_PROFESS_OPTIONS(公开职业选项)`。

### Task 1: 后端职业枚举与映射

**Files:**
- Modify: `backend/system-object/src/main/java/com/ypat/enums/UserProfess.java`
- Modify: `backend/system-object/src/main/java/com/ypat/UserQo.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`
- Create: `backend/system-object/src/test/java/com/ypat/enums/UserProfessTest.java`
- Modify: `backend/system-domain/src/test/java/com/ypat/service/WorkServiceAdminSourceTest.java`

- [ ] **Step 1: 写后端职业枚举失败测试**

Create `backend/system-object/src/test/java/com/ypat/enums/UserProfessTest.java`:

```java
package com.ypat.enums;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserProfessTest {
    @Test
    public void publicProfessOptionsOnlyExposeSixCurrentRoles() {
        List<String> values = UserProfess.getPublicValues();
        assertEquals(Arrays.asList("6", "0", "2", "9", "3", "1"), values);
    }

    @Test
    public void displayNameKeepsHistoricalValuesAndRenamesMakeup() {
        assertEquals("化妆师", UserProfess.getNameByCode("2"));
        assertEquals("摄像师", UserProfess.getNameByCode("9"));
        assertEquals("个人", UserProfess.getNameByCode("4"));
        assertEquals("演员", UserProfess.getNameByCode("5"));
        assertEquals("其他", UserProfess.getNameByCode("7"));
        assertEquals("素人模特", UserProfess.getNameByCode("8"));
    }

    @Test
    public void validityAllowsNewVideographerAndHistoricalValues() {
        assertTrue(UserProfess.isValid("9"));
        assertTrue(UserProfess.isValid("8"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/backend
mvn -pl system-object -Dtest=UserProfessTest test
```

Expected: FAIL，因为 `UserProfess.getPublicValues()` 和 `9 摄像师` 尚未实现。

- [ ] **Step 3: 实现后端职业枚举**

Edit `backend/system-object/src/main/java/com/ypat/enums/UserProfess.java`:

```java
package com.ypat.enums;

import java.util.Arrays;
import java.util.List;

public enum UserProfess {
    sys("0","摄影师", true),
    mt("1","模特", true),
    zzs("2","化妆师", true),
    xts("3","修图师", true),
    gr("4","个人", false),
    yy("5","演员", false),
    sj("6","商家", true),
    qt("7","其他", false),
    sr("8","素人模特", false),
    sxj("9","摄像师", true);

    public String value;
    public String name;
    public boolean publicOption;

    UserProfess(String value, String name, boolean publicOption) {
        this.value = value;
        this.name = name;
        this.publicOption = publicOption;
    }

    public static String getNameByCode(String code){
        for(UserProfess target : UserProfess.values()){
            if(target.value.equals(code)){
                return target.name;
            }
        }
        return "";
    }

    public static List<String> getPublicValues(){
        return Arrays.asList(sj.value, sys.value, zzs.value, sxj.value, xts.value, mt.value);
    }

    public static boolean isValid(String code){
        if(code == null) return false;
        for(UserProfess p : UserProfess.values()){
            if(p.value.equals(code)) return true;
        }
        return false;
    }
}
```

- [ ] **Step 4: 放宽 `UserQo(用户请求对象)` 职业长度错误文案**

Edit `backend/system-object/src/main/java/com/ypat/UserQo.java`:

```java
@Size(max = 2, message = "profess最大两位")
private String profess;
```

- [ ] **Step 5: 为作品作者职业映射写 source test(源码测试)**

Modify `backend/system-domain/src/test/java/com/ypat/service/WorkServiceAdminSourceTest.java`，增加断言：

```java
@Test
public void mapProfessionToTargetSupportsVideographer() throws Exception {
    String source = readSource();
    assertTrue(source.contains("case \"9\": return YpatTarget.xsxj.value;"));
}
```

If the file uses a helper with a different name than `readSource()`，reuse that helper and keep only the assertion content.

- [ ] **Step 6: 实现职业到约拍目标映射**

Edit `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`:

```java
case "9": return YpatTarget.xsxj.value;
```

Put it in `mapProfessionToTarget(String profess)` and update the method comment to include `9摄像师→2约摄像师`。

- [ ] **Step 7: 运行后端相关测试**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/backend
mvn -pl system-object -Dtest=UserProfessTest test
mvn -pl system-domain -Dtest=WorkServiceAdminSourceTest test
```

Expected: PASS。

- [ ] **Step 8: 提交 Task 1**

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git add backend/system-object/src/main/java/com/ypat/enums/UserProfess.java backend/system-object/src/main/java/com/ypat/UserQo.java backend/system-object/src/test/java/com/ypat/enums/UserProfessTest.java backend/system-domain/src/main/java/com/ypat/service/WorkService.java backend/system-domain/src/test/java/com/ypat/service/WorkServiceAdminSourceTest.java
git commit -m "feat: align backend profession enum" -m "Constraint: Keep legacy profession values display-compatible; do not touch backend/system-web." -m "Tested: mvn -pl system-object -Dtest=UserProfessTest test; mvn -pl system-domain -Dtest=WorkServiceAdminSourceTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 2: 主题标签字典完整性测试

**Files:**
- Modify: `backend/system-wap/src/test/java/com/ypat/controller/WorkDictControllerSourceTest.java`
- No production change expected unless the test exposes drift in `backend/system-restapi/src/main/java/com/ypat/controller/WorkDictController.java`

- [ ] **Step 1: 写 29 个默认标签 source test(源码测试)**

Modify `backend/system-wap/src/test/java/com/ypat/controller/WorkDictControllerSourceTest.java`，断言这些代码和顺序存在：

```java
@Test
public void defaultWorkTagsContainTwentyNineStableCodes() throws Exception {
    String source = readSource();
    String[] codes = new String[] {
            "qinglv", "shangwu", "minguo", "hanfu", "yunzhao",
            "ertong", "anhei", "qingxu", "yejing", "xiaoyuan",
            "zhuangrong", "gufeng", "taobao", "shishang", "hefu",
            "qipao", "hanxi", "oumei", "senxi", "shaonv",
            "baolilai", "qingxin", "hunli", "cosplay", "jiaopian",
            "heibai", "jishi", "rixi", "fugu"
    };
    int lastIndex = -1;
    for (String code : codes) {
        int index = source.indexOf("new DefaultWorkTag(\"" + code + "\"");
        assertTrue("missing code " + code, index >= 0);
        assertTrue("code order changed for " + code, index > lastIndex);
        lastIndex = index;
    }
}
```

If the existing helper is not named `readSource()`，reuse the existing source-reading helper.

- [ ] **Step 2: 运行测试确认状态**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/backend
mvn -pl system-wap -Dtest=WorkDictControllerSourceTest test
```

Expected: PASS。如果 FAIL，only edit `backend/system-restapi/src/main/java/com/ypat/controller/WorkDictController.java` so `DEFAULT_WORK_TAGS` exactly matches the 29-code order in the design doc.

- [ ] **Step 3: 提交 Task 2**

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git add backend/system-wap/src/test/java/com/ypat/controller/WorkDictControllerSourceTest.java backend/system-restapi/src/main/java/com/ypat/controller/WorkDictController.java
git commit -m "test: lock work tag dictionary defaults" -m "Constraint: Backend dictionary remains the source for 29 work tags." -m "Tested: mvn -pl system-wap -Dtest=WorkDictControllerSourceTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 3: 新版后台职业与内测资源风格

**Files:**
- Modify: `frontend-admin/src/constants/enums.ts`
- Create: `frontend-admin/src/constants/__tests__/enums.test.ts`
- Modify: `frontend-admin/src/views/internal-test/resource/index.vue`
- Modify: `frontend-admin/src/views/internal-test/generator/index.vue`

- [ ] **Step 1: 写新版后台常量测试**

Create `frontend-admin/src/constants/__tests__/enums.test.ts`:

```ts
import {
  WORK_TAG_STYLE_CODES,
  getProfessDisplayName,
  getProfessOptions,
  getWorkTagStyleOptions,
  resolveWorkTagStyleName,
} from '../enums'

describe('admin enum consistency', () => {
  it('exposes 29 work tag style codes in backend order', () => {
    expect(WORK_TAG_STYLE_CODES).toEqual([
      'qinglv', 'shangwu', 'minguo', 'hanfu', 'yunzhao',
      'ertong', 'anhei', 'qingxu', 'yejing', 'xiaoyuan',
      'zhuangrong', 'gufeng', 'taobao', 'shishang', 'hefu',
      'qipao', 'hanxi', 'oumei', 'senxi', 'shaonv',
      'baolilai', 'qingxin', 'hunli', 'cosplay', 'jiaopian',
      'heibai', 'jishi', 'rixi', 'fugu',
    ])
    expect(getWorkTagStyleOptions()).toHaveLength(29)
  })

  it('resolves new codes, Chinese names, old numeric styles, and unknown values', () => {
    expect(resolveWorkTagStyleName('qinglv')).toBe('情侣')
    expect(resolveWorkTagStyleName('情侣')).toBe('情侣')
    expect(resolveWorkTagStyleName('10')).toBe('日系')
    expect(resolveWorkTagStyleName('custom')).toBe('custom')
  })

  it('only returns six public profession options and keeps legacy display names', () => {
    expect(getProfessOptions()).toEqual([
      { label: '商家', value: '6' },
      { label: '摄影师', value: '0' },
      { label: '化妆师', value: '2' },
      { label: '摄像师', value: '9' },
      { label: '修图师', value: '3' },
      { label: '模特', value: '1' },
    ])
    expect(getProfessDisplayName('8')).toBe('素人模特')
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend-admin
pnpm vitest run src/constants/__tests__/enums.test.ts
```

Expected: FAIL，因为常量与解析函数还不存在。

- [ ] **Step 3: 实现新版后台常量与解析函数**

Edit `frontend-admin/src/constants/enums.ts`，保留旧 `YpatPatstyle(约拍风格枚举)`，新增：

```ts
export const WorkTagStyle = [
  { code: 'qinglv', name: '情侣' },
  { code: 'shangwu', name: '商务' },
  { code: 'minguo', name: '民国' },
  { code: 'hanfu', name: '汉服' },
  { code: 'yunzhao', name: '孕照' },
  { code: 'ertong', name: '儿童摄影' },
  { code: 'anhei', name: '暗黑' },
  { code: 'qingxu', name: '情绪' },
  { code: 'yejing', name: '夜景' },
  { code: 'xiaoyuan', name: '校园' },
  { code: 'zhuangrong', name: '妆容' },
  { code: 'gufeng', name: '古风' },
  { code: 'taobao', name: '淘宝' },
  { code: 'shishang', name: '时尚' },
  { code: 'hefu', name: '和服' },
  { code: 'qipao', name: '旗袍' },
  { code: 'hanxi', name: '韩系' },
  { code: 'oumei', name: '欧美' },
  { code: 'senxi', name: '森系' },
  { code: 'shaonv', name: '少女' },
  { code: 'baolilai', name: '宝丽来' },
  { code: 'qingxin', name: '清新' },
  { code: 'hunli', name: '婚礼' },
  { code: 'cosplay', name: 'cosplay' },
  { code: 'jiaopian', name: '胶片' },
  { code: 'heibai', name: '黑白' },
  { code: 'jishi', name: '纪实' },
  { code: 'rixi', name: '日系' },
  { code: 'fugu', name: '复古' },
] as const

export const WORK_TAG_STYLE_CODES = WorkTagStyle.map((item) => item.code)
export const getWorkTagStyleOptions = () => WorkTagStyle.map((item) => ({ label: item.name, value: item.code }))

export function resolveWorkTagStyleName(value?: string): string {
  if (!value) return '-'
  const fromCode = WorkTagStyle.find((item) => item.code === value)
  if (fromCode) return fromCode.name
  const fromName = WorkTagStyle.find((item) => item.name === value)
  if (fromName) return fromName.name
  return Object.values(YpatPatstyle).find((item) => item.value === value)?.name || value
}
```

Replace `UserProfess(用户职业)` and helpers:

```ts
export const UserProfess = {
  BUSINESS: { value: '6', name: '商家' },
  PHOTOGRAPHER: { value: '0', name: '摄影师' },
  MAKEUP: { value: '2', name: '化妆师' },
  VIDEOGRAPHER: { value: '9', name: '摄像师' },
  RETOUCHER: { value: '3', name: '修图师' },
  MODEL: { value: '1', name: '模特' },
} as const

const LegacyUserProfess: Record<string, string> = {
  '4': '个人',
  '5': '演员',
  '7': '其他',
  '8': '素人模特',
}

export const getProfessOptions = () => Object.values(UserProfess).map((o) => ({ label: o.name, value: o.value }))

export function getProfessDisplayName(value?: string): string {
  if (!value) return '-'
  return Object.values(UserProfess).find((item) => item.value === value)?.name || LegacyUserProfess[value] || value
}
```

- [ ] **Step 4: 改造内测资源管理页面**

Edit `frontend-admin/src/views/internal-test/resource/index.vue`:

```ts
import {
  InternalTestMediaType,
  InternalTestResourceStatus,
  getInternalTestResourceStatusOptions,
  getInternalTestUsageTypeOptions,
  getProfessDisplayName,
  getProfessOptions,
  getWorkTagStyleOptions,
  resolveWorkTagStyleName,
} from '@/constants/enums'
```

Replace every `getYpatPatstyleOptions()` in this file with `getWorkTagStyleOptions()`。

Replace:

```ts
function styleText(value?: string): string {
  return getOptionLabel(getYpatPatstyleOptions(), value)
}

function professText(value?: string): string {
  return getOptionLabel(getProfessOptions(), value)
}
```

with:

```ts
function styleText(value?: string): string {
  return resolveWorkTagStyleName(value)
}

function professText(value?: string): string {
  return getProfessDisplayName(value)
}
```

- [ ] **Step 5: 改造内测数据生成页面**

Edit `frontend-admin/src/views/internal-test/generator/index.vue`:

```ts
import {
  Gender,
  InternalTestGenerateMode,
  InternalTestMediaType,
  InternalTestResourceStatus,
  InternalTestUsageType,
  getGenderOptions,
  getInternalTestGenerateModeOptions,
  getProfessOptions,
  getWorkTagStyleOptions,
} from '@/constants/enums'
```

Replace every `getYpatPatstyleOptions()` with `getWorkTagStyleOptions()`。

- [ ] **Step 6: 运行新版后台测试与构建检查**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend-admin
pnpm vitest run src/constants/__tests__/enums.test.ts
pnpm type-check
```

Expected: PASS。

- [ ] **Step 7: 提交 Task 3**

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git add frontend-admin/src/constants/enums.ts frontend-admin/src/constants/__tests__/enums.test.ts frontend-admin/src/views/internal-test/resource/index.vue frontend-admin/src/views/internal-test/generator/index.vue
git commit -m "feat: align admin internal test enums" -m "Constraint: Keep legacy ypat patstyle helpers for old ypat pages; do not modify backend/system-web." -m "Tested: pnpm vitest run src/constants/__tests__/enums.test.ts; pnpm type-check" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 4: 新版小程序职业和主题标签 fallback(兜底)

**Files:**
- Modify: `frontend/src/constants/enums.ts`
- Modify: `frontend/src/constants/work-tags.ts`
- Create or Modify: `frontend/src/constants/__tests__/enums.test.ts`
- Modify: `frontend/src/constants/__tests__/work-tags.test.ts`
- Modify as needed: `frontend/src/pages-sub/user/profile.vue`
- Modify as needed: `frontend/src/pages-sub/work/filter.vue`
- Modify as needed: `frontend/src/pages/work/index.vue`
- Modify as needed: `frontend/src/pages/mine/index.vue`

- [ ] **Step 1: 写新版小程序枚举测试**

Create `frontend/src/constants/__tests__/enums.test.ts` if missing:

```ts
import {
  PHOTO_STYLES,
  PROFESS_LABELS,
  PUBLIC_PROFESS_OPTIONS,
  UserProfess,
  getProfessLabel,
} from '../enums'

describe('miniapp enum consistency', () => {
  it('exposes six public professions including videographer', () => {
    expect(UserProfess.VIDEOGRAPHER).toBe('9')
    expect(PUBLIC_PROFESS_OPTIONS).toEqual([
      { label: '商家', value: '6' },
      { label: '摄影师', value: '0' },
      { label: '化妆师', value: '2' },
      { label: '摄像师', value: '9' },
      { label: '修图师', value: '3' },
      { label: '模特', value: '1' },
    ])
    expect(PROFESS_LABELS['8']).toBe('素人模特')
    expect(getProfessLabel('9')).toBe('摄像师')
  })

  it('uses 29 work tag names for photo style keywords', () => {
    expect(PHOTO_STYLES).toHaveLength(29)
    expect(PHOTO_STYLES).toContain('cosplay')
    expect(PHOTO_STYLES).toContain('黑白')
    expect(PHOTO_STYLES).toContain('纪实')
    expect(PHOTO_STYLES).toContain('日系')
    expect(PHOTO_STYLES).toContain('复古')
  })
})
```

- [ ] **Step 2: 扩展主题标签 fallback(兜底) 测试**

Modify `frontend/src/constants/__tests__/work-tags.test.ts` to assert:

```ts
import { WORK_TAGS_FALLBACK, WORK_TAG_FALLBACK_OPTIONS } from '../work-tags'

it('keeps backend default work tag fallback list aligned', () => {
  expect(WORK_TAGS_FALLBACK).toEqual([
    '情侣', '商务', '民国', '汉服', '孕照',
    '儿童摄影', '暗黑', '情绪', '夜景', '校园',
    '妆容', '古风', '淘宝', '时尚', '和服',
    '旗袍', '韩系', '欧美', '森系', '少女',
    '宝丽来', '清新', '婚礼', 'cosplay', '胶片',
    '黑白', '纪实', '日系', '复古',
  ])
  expect(WORK_TAG_FALLBACK_OPTIONS.map((item) => item.code)).toEqual([
    'qinglv', 'shangwu', 'minguo', 'hanfu', 'yunzhao',
    'ertong', 'anhei', 'qingxu', 'yejing', 'xiaoyuan',
    'zhuangrong', 'gufeng', 'taobao', 'shishang', 'hefu',
    'qipao', 'hanxi', 'oumei', 'senxi', 'shaonv',
    'baolilai', 'qingxin', 'hunli', 'cosplay', 'jiaopian',
    'heibai', 'jishi', 'rixi', 'fugu',
  ])
})
```

- [ ] **Step 3: 运行测试确认失败**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend
pnpm vitest run src/constants/__tests__/enums.test.ts src/constants/__tests__/work-tags.test.ts
```

Expected: FAIL，因为新增常量还未实现。

- [ ] **Step 4: 实现新版小程序职业常量**

Edit `frontend/src/constants/enums.ts`:

```ts
export const UserProfess = {
  PHOTOGRAPHER: '0',
  MODEL: '1',
  MAKEUP: '2',
  RETOUCHER: '3',
  PERSONAL: '4',
  ACTOR: '5',
  BUSINESS: '6',
  OTHER: '7',
  AMATEUR_MODEL: '8',
  VIDEOGRAPHER: '9',
} as const

export const PROFESS_LABELS: Record<string, string> = {
  [UserProfess.PHOTOGRAPHER]: '摄影师',
  [UserProfess.MODEL]: '模特',
  [UserProfess.MAKEUP]: '化妆师',
  [UserProfess.RETOUCHER]: '修图师',
  [UserProfess.PERSONAL]: '个人',
  [UserProfess.ACTOR]: '艺人',
  [UserProfess.BUSINESS]: '商家',
  [UserProfess.OTHER]: '其他',
  [UserProfess.AMATEUR_MODEL]: '素人模特',
  [UserProfess.VIDEOGRAPHER]: '摄像师',
}

export const PUBLIC_PROFESS_OPTIONS = [
  { label: '商家', value: UserProfess.BUSINESS },
  { label: '摄影师', value: UserProfess.PHOTOGRAPHER },
  { label: '化妆师', value: UserProfess.MAKEUP },
  { label: '摄像师', value: UserProfess.VIDEOGRAPHER },
  { label: '修图师', value: UserProfess.RETOUCHER },
  { label: '模特', value: UserProfess.MODEL },
] as const

export function getProfessLabel(value?: string): string {
  if (!value) return ''
  return PROFESS_LABELS[value] || value
}
```

Replace `PHOTO_STYLES(摄影风格列表)` with the 29 names from the test.

- [ ] **Step 5: 实现新版小程序主题标签 fallback(兜底)**

Edit `frontend/src/constants/work-tags.ts`:

```ts
export const WORK_TAG_FALLBACK_OPTIONS = [
  { code: 'qinglv', name: '情侣', sortNo: 1 },
  { code: 'shangwu', name: '商务', sortNo: 2 },
  { code: 'minguo', name: '民国', sortNo: 3 },
  { code: 'hanfu', name: '汉服', sortNo: 4 },
  { code: 'yunzhao', name: '孕照', sortNo: 5 },
  { code: 'ertong', name: '儿童摄影', sortNo: 6 },
  { code: 'anhei', name: '暗黑', sortNo: 7 },
  { code: 'qingxu', name: '情绪', sortNo: 8 },
  { code: 'yejing', name: '夜景', sortNo: 9 },
  { code: 'xiaoyuan', name: '校园', sortNo: 10 },
  { code: 'zhuangrong', name: '妆容', sortNo: 11 },
  { code: 'gufeng', name: '古风', sortNo: 12 },
  { code: 'taobao', name: '淘宝', sortNo: 13 },
  { code: 'shishang', name: '时尚', sortNo: 14 },
  { code: 'hefu', name: '和服', sortNo: 15 },
  { code: 'qipao', name: '旗袍', sortNo: 16 },
  { code: 'hanxi', name: '韩系', sortNo: 17 },
  { code: 'oumei', name: '欧美', sortNo: 18 },
  { code: 'senxi', name: '森系', sortNo: 19 },
  { code: 'shaonv', name: '少女', sortNo: 20 },
  { code: 'baolilai', name: '宝丽来', sortNo: 21 },
  { code: 'qingxin', name: '清新', sortNo: 22 },
  { code: 'hunli', name: '婚礼', sortNo: 23 },
  { code: 'cosplay', name: 'cosplay', sortNo: 24 },
  { code: 'jiaopian', name: '胶片', sortNo: 25 },
  { code: 'heibai', name: '黑白', sortNo: 26 },
  { code: 'jishi', name: '纪实', sortNo: 27 },
  { code: 'rixi', name: '日系', sortNo: 28 },
  { code: 'fugu', name: '复古', sortNo: 29 },
] as const

export const WORK_TAGS_FALLBACK = WORK_TAG_FALLBACK_OPTIONS.map((item) => item.name)
```

Keep `resolveWorkTagOptions(tags)` returning real API tags when present:

```ts
export function resolveWorkTagOptions(tags: WorkTag[] | null | undefined): WorkTag[] {
  return tags && tags.length > 0 ? tags : []
}
```

- [ ] **Step 6: 替换小程序公开职业选项使用点**

Use:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
rg -n "PROFESS_LABELS|UserProfess|profession|profess" frontend/src/pages frontend/src/pages-sub frontend/src/components frontend/src/utils
```

For select/radio/option lists in profile, complete-profile, work filter, mine/work display pages，use `PUBLIC_PROFESS_OPTIONS` for selectable public roles and `getProfessLabel(value)` for display. Do not remove `PROFESS_LABELS(职业文案)` because historical display still needs it.

- [ ] **Step 7: 运行新版小程序测试与类型检查**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend
pnpm vitest run src/constants/__tests__/enums.test.ts src/constants/__tests__/work-tags.test.ts
pnpm type-check
```

Expected: PASS。

- [ ] **Step 8: 提交 Task 4**

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git add frontend/src/constants/enums.ts frontend/src/constants/work-tags.ts frontend/src/constants/__tests__/enums.test.ts frontend/src/constants/__tests__/work-tags.test.ts frontend/src/pages-sub/user/profile.vue frontend/src/pages-sub/work/filter.vue frontend/src/pages/work/index.vue frontend/src/pages/mine/index.vue
git commit -m "feat: align miniapp profession and work tags" -m "Constraint: Do not modify 91pai-master; keep historical profession display values." -m "Tested: pnpm vitest run src/constants/__tests__/enums.test.ts src/constants/__tests__/work-tags.test.ts; pnpm type-check" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 5: 集成验证与服务重启

**Files:**
- No required source changes.

- [ ] **Step 1: 全局确认未改旧版后台和旧小程序**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git diff --name-only dev6...HEAD | rg '^(backend/system-web|91pai-master)/' || true
```

Expected: no output。

- [ ] **Step 2: 运行聚合验证**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/backend
mvn -pl system-object,system-domain,system-wap test

cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend-admin
pnpm test
pnpm type-check

cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend
pnpm test
pnpm type-check
```

Expected: all PASS。

- [ ] **Step 3: 重启 Docker(容器) 后端服务和新版后台入口服务**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
docker compose ps
docker compose restart restapi wap
```

If compose service names differ，use the exact names shown by `docker compose ps` and restart the services that expose backend REST API and `localhost:18899`。

- [ ] **Step 4: 手动页面验收**

Open:

```text
http://localhost:18899/internal-test/resource
http://localhost:18899/internal-test/generator
```

Verify:
- 风格下拉包含 29 个主题标签，包括 `cosplay`、`黑白`、`纪实`、`日系`、`复古`。
- 职业下拉只包含 `商家`、`摄影师`、`化妆师`、`摄像师`、`修图师`、`模特`。
- 新增内测资源保存时 `styleCode` 为主题标签 code(编码)，例如 `qinglv`。
- 内测生成页面能按选择的主题标签 code(编码) 加载资源。

- [ ] **Step 5: 最终提交或确认工作树**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git status --short
```

Expected: only allowed uncommitted file is `.omx/state/session.json`。

## Self-Review

- Spec coverage: 后端职业、29 个主题标签、新版后台内测资源与生成页、新版小程序职业与主题标签 fallback(兜底)、旧数据兼容、旧版后台/旧小程序排除均有任务覆盖。
- Placeholder scan: 本计划没有留下未定义步骤、空泛实现语句或缺失代码示例。
- Type consistency: 后端职业 code(编码) 统一为字符串；新版后台 `styleCode` 使用主题标签 code(编码)；新版小程序 `PHOTO_STYLES(摄影风格列表)` 使用主题标签名称用于展示和搜索关键词。
