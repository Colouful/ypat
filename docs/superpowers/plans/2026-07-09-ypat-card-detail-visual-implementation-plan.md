# YPAT（爱去拍）约拍卡片与详情页视觉优化 Implementation Plan（实现计划）

> **给智能执行代理:** REQUIRED SUB-SKILL（必需子技能）: 使用 `superpowers:subagent-driven-development`（子代理驱动开发，推荐）或 `superpowers:executing-plans`（执行计划）逐任务实现本计划。步骤使用 checkbox（复选框，`- [ ]`）语法跟踪。

**Goal（目标）:** 构建真实会员状态回传，并优化新版小程序首页约拍卡片、筛选标签顺序和约拍详情页可信度展示。

**Architecture（架构）:** 后端在 `UserQo`（用户传输对象）上新增会员字段，并由 `YpatInfoService`（约拍信息服务）在约拍列表和详情组装作者信息时填充。新版前端 `frontend/src` 消费这些字段，集中在 `KeepYpatCard`（约拍卡片组件）和 `YpatDetailView`（约拍详情视图组件）完成视觉优化，页面文件只负责隐藏消息入口和映射数据。

**Tech Stack（技术栈）:** Java 8、Spring Boot（Java 应用框架）、Spring Data JPA（数据访问框架）、JUnit 4（Java 测试框架）、Vue 3（前端框架）、uni-app（跨端小程序框架）、TypeScript（类型化 JavaScript）、SCSS（样式预处理器）、Vitest（前端测试框架）、Vue Test Utils（Vue 测试工具）。

---

## File Structure（文件结构）

- Modify（修改）: `backend/system-object/src/main/java/com/ypat/UserQo.java`
  - 新增 `memberActive`（会员是否生效）和 `memberLevel`（会员等级）字段以及 getter/setter（读取/设置方法）。
- Modify（修改）: `backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java`
  - 新增 `UserMemberRepository`（用户会员仓库）依赖。
  - 新增会员状态填充 helper method（辅助方法）。
  - 在 `findById`（按 ID 查详情）和 `findPage`（分页查询）组装作者信息时调用辅助方法。
- Modify（修改）: `backend/system-domain/src/test/java/com/ypat/service/YpatInfoAdminFilterSourceTest.java`
  - 增加会员状态填充的源代码级断言，延续当前模块已有的轻量测试风格。
- Modify（修改）: `frontend/src/api/types/index.ts`
  - 在 `UserInfo`（用户信息类型）中新增可选的 `memberActive`（会员是否生效）和 `memberLevel`（会员等级）。
- Modify（修改）: `frontend/src/pages/home/index.vue`
  - 隐藏 `home-message`（消息入口）的渲染。
  - 按确认顺序调整 `quickChips`（快速筛选标签）。
  - 将会员字段映射进 `KeepYpatCardItem`（约拍卡片数据类型）。
- Modify（修改）: `frontend/src/components/business/KeepYpatCard.vue`
  - 在 props interface（属性接口）中新增会员字段。
  - 重构卡片 template（模板）和 SCSS（样式），保留点击事件与现有数据契约。
- Modify（修改）: `frontend/src/components/business/YpatDetailView.vue`
  - 新增 computed（计算属性）形式的可信度/会员辅助数据。
  - 重构信任标签和作者卡片布局，保留原有交互。
- Modify（修改）: `frontend/src/components/business/__tests__/keep-components.test.ts`
  - 更新卡片渲染测试，覆盖正向/反向信任状态和会员标识。

---

### Task 1（任务 1）: 后端会员字段与状态填充

**Files:**
- Modify（修改）: `backend/system-object/src/main/java/com/ypat/UserQo.java`
- Modify（修改）: `backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java`
- Test（测试）: `backend/system-domain/src/test/java/com/ypat/service/YpatInfoAdminFilterSourceTest.java`

- [ ] **Step 1（步骤 1）: 编写先失败的后端源代码测试**

在 `backend/system-domain/src/test/java/com/ypat/service/YpatInfoAdminFilterSourceTest.java` 的 `methodBody` 前追加这个测试方法：

```java
    @Test
    public void ypatAuthorPayloadIncludesMemberState() throws Exception {
        String userQoSource = read("../system-object/src/main/java/com/ypat/UserQo.java");
        String serviceSource = read("src/main/java/com/ypat/service/YpatInfoService.java");

        assertTrue(userQoSource.contains("private Boolean memberActive"));
        assertTrue(userQoSource.contains("private String memberLevel"));
        assertTrue(userQoSource.contains("getMemberActive()"));
        assertTrue(userQoSource.contains("setMemberActive(Boolean memberActive)"));
        assertTrue(userQoSource.contains("getMemberLevel()"));
        assertTrue(userQoSource.contains("setMemberLevel(String memberLevel)"));

        assertTrue(serviceSource.contains("private UserMemberRepository userMemberRepository"));
        assertTrue(serviceSource.contains("enrichMemberState(userQo, user.getId())"));
        assertTrue(serviceSource.contains("boolean isActiveMember(UserMember member)"));
        assertTrue(serviceSource.contains("member.getExpireAt().after(new Date())"));
        assertTrue(serviceSource.contains("!\"NONE\".equals(member.getLevel())"));
        assertTrue(serviceSource.contains("userQo.setMemberActive(true)"));
        assertTrue(serviceSource.contains("userQo.setMemberLevel(member.getLevel())"));
        assertTrue(serviceSource.contains("userQo.setMemberActive(false)"));
    }
```

- [ ] **Step 2（步骤 2）: 运行后端测试，确认它先失败**

从仓库根目录运行：

```bash
mvn -pl backend/system-domain -Dtest=YpatInfoAdminFilterSourceTest#ypatAuthorPayloadIncludesMemberState test
```

预期：FAIL（失败），因为 `UserQo`（用户传输对象）还没有 `memberActive/memberLevel`（会员字段），`YpatInfoService`（约拍信息服务）也还没有填充作者会员状态。

- [ ] **Step 3（步骤 3）: 给 `UserQo`（用户传输对象）增加会员字段**

在 `backend/system-object/src/main/java/com/ypat/UserQo.java` 中，在 `private String creditflag;` 后添加字段：

```java
    private Boolean memberActive;
    private String memberLevel;
```

在 `setCreditflag` 后添加 getter/setter（读取/设置方法）：

```java
    public Boolean getMemberActive() {
        return memberActive;
    }

    public void setMemberActive(Boolean memberActive) {
        this.memberActive = memberActive;
    }

    public String getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(String memberLevel) {
        this.memberLevel = memberLevel;
    }
```

建议在 `toString()`（字符串输出方法）中紧跟 `creditflag` 添加这两个字段：

```java
                ", memberActive=" + memberActive +
                ", memberLevel='" + memberLevel + '\'' +
```

- [ ] **Step 4（步骤 4）: 在 `YpatInfoService`（约拍信息服务）中填充作者会员状态**

在 `backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java` 中，当前 wildcard import（通配导入）已经覆盖仓库类型。在其他 repository（仓库）字段附近添加依赖：

```java
    @Autowired
    private UserMemberRepository userMemberRepository;
```

在 `findById`（按 ID 查详情）中，头像图片循环之后、`ypatInfoQo.setUserQo(userQo);` 之前添加：

```java
            enrichMemberState(userQo, user.getId());
```

在 `findPage`（分页查询）中，头像图片循环之后、`qo.setUserQo(userQo);` 之前添加：

```java
                    enrichMemberState(userQo, user.getId());
```

在 `public YpatInfo get(Long id)` 附近添加这些 helper method（辅助方法）：

```java
    void enrichMemberState(UserQo userQo, Long userId) {
        userQo.setMemberActive(false);
        if(userId == null){
            return;
        }
        UserMember member = userMemberRepository == null ? null : userMemberRepository.findOne(userId);
        if(isActiveMember(member)){
            userQo.setMemberActive(true);
            userQo.setMemberLevel(member.getLevel());
        }
    }

    boolean isActiveMember(UserMember member) {
        return member != null
                && member.getLevel() != null
                && !"NONE".equals(member.getLevel())
                && member.getExpireAt() != null
                && member.getExpireAt().after(new Date());
    }
```

- [ ] **Step 5（步骤 5）: 运行后端聚焦测试**

运行：

```bash
mvn -pl backend/system-domain -Dtest=YpatInfoAdminFilterSourceTest test
```

预期：PASS（通过）。如果 Maven（Java 构建工具）同时构建依赖模块，命令可能会先编译 `system-object`（对象模块）。

- [ ] **Step 6（步骤 6）: 提交后端会员状态支持**

运行：

```bash
git add backend/system-object/src/main/java/com/ypat/UserQo.java backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java backend/system-domain/src/test/java/com/ypat/service/YpatInfoAdminFilterSourceTest.java
git commit -m "feat: expose ypat author membership state"
```

---

### Task 2（任务 2）: 前端类型、首页入口和筛选标签顺序

**Files（文件）:**
- Modify（修改）: `frontend/src/api/types/index.ts`
- Modify（修改）: `frontend/src/pages/home/index.vue`
- Test（测试）: 手动源代码检查，外加 Task 5（任务 5）中的 TypeScript（类型脚本）检查。

- [ ] **Step 1（步骤 1）: 给前端 `UserInfo`（用户信息类型）增加会员字段**

在 `frontend/src/api/types/index.ts` 中，在 `creditflag?: string` 后添加字段：

```ts
  memberActive?: boolean
  memberLevel?: string
```

- [ ] **Step 2（步骤 2）: 隐藏 `home-message`（消息入口）渲染，不改变消息逻辑**

在 `frontend/src/pages/home/index.vue` 中，将当前消息入口块：

```vue
        <view class="home-message" @tap="goMessage">
          <KeepIcon name="mail" :size="38" color="#1B1E23" />
          <text v-if="unreadCount > 0" class="home-message__badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</text>
        </view>
```

替换为：

```vue
        <!-- 新版首页暂不展示消息入口，未读刷新逻辑保留给后续入口复用。 -->
```

- [ ] **Step 3（步骤 3）: 调整 `quickChips`（快速筛选标签）顺序**

在 `frontend/src/pages/home/index.vue` 中，将 `quickChips` 数组替换为：

```ts
const quickChips = [
  { label: '全部', value: 'all' },
  { label: '约模特', value: 'model' },
  { label: '约摄影师', value: 'photographer' },
  { label: '希望互勉', value: 'free' },
  { label: '可付费', value: 'pay' },
  { label: 'INS', value: 'INS' },
  { label: '胶片', value: '胶片' },
  { label: '情绪', value: '情绪' },
  { label: '灵感发现', value: 'discover' },
]
```

- [ ] **Step 4（步骤 4）: 将会员字段映射进卡片数据**

在 `frontend/src/pages/home/index.vue` 的 `cardItems` computed（计算属性）映射中添加：

```ts
  memberActive: item.userQo?.memberActive === true,
  memberLevel: item.userQo?.memberLevel,
```

最终对象仍应保留 `realname`（实名状态）和 `credit`（担保状态）：

```ts
  realname: item.realnameflag === '1' || item.userQo?.realnameflag === '1',
  credit: item.creditflag === '1' || item.userQo?.creditflag === '1',
  memberActive: item.userQo?.memberActive === true,
  memberLevel: item.userQo?.memberLevel,
```

- [ ] **Step 5（步骤 5）: 运行源代码检查**

运行：

```bash
rg -n "home-message|const quickChips|memberActive|memberLevel" frontend/src/pages/home/index.vue frontend/src/api/types/index.ts
```

预期：
- `home-message`（消息入口）只出现在样式或保留方法中，不再作为 `<view class="home-message">` 渲染。
- `quickChips`（快速筛选标签）顺序以 `全部`、`约模特`、`约摄影师` 开头。
- `memberActive/memberLevel`（会员字段）同时出现在前端类型和卡片映射中。

- [ ] **Step 6（步骤 6）: 提交前端映射变更**

运行：

```bash
git add frontend/src/api/types/index.ts frontend/src/pages/home/index.vue
git commit -m "feat: map ypat author trust fields"
```

---

### Task 3（任务 3）: 重构 `KeepYpatCard`（约拍卡片组件）

**Files（文件）:**
- Modify（修改）: `frontend/src/components/business/KeepYpatCard.vue`
- Test（测试）: `frontend/src/components/business/__tests__/keep-components.test.ts`

- [ ] **Step 1（步骤 1）: 更新会先失败的卡片测试**

在 `frontend/src/components/business/__tests__/keep-components.test.ts` 中，将第一个测试替换为这两个测试：

```ts
  it('renders ypat card trust states and member badge', () => {
    const wrapper = mount(KeepYpatCard, {
      props: {
        item: {
          id: 1,
          title: '寻找气质女模拍一组复古港风样片',
          targetLabel: '约模特',
          chargeLabel: '希望互勉',
          city: '上海·徐汇',
          name: '陈默 Mo',
          image: '/static/default-cover.png',
          avatar: '/static/default-avatar.png',
          time: '12分钟前',
          applyCount: 36,
          realname: true,
          credit: true,
          memberActive: true,
          memberLevel: 'BASIC',
        },
      },
    })

    expect(wrapper.text()).toContain('寻找气质女模')
    expect(wrapper.text()).toContain('约模特')
    expect(wrapper.text()).toContain('已认证')
    expect(wrapper.text()).toContain('已缴担保金')
    expect(wrapper.text()).toContain('VIP')
  })

  it('renders negative ypat card trust states without member badge', () => {
    const wrapper = mount(KeepYpatCard, {
      props: {
        item: {
          id: 2,
          title: '周末约一组城市街拍',
          targetLabel: '约摄影师',
          chargeLabel: '费用协商',
          city: '杭州',
          name: '匿名用户',
          image: '/static/default-cover.png',
          avatar: '/static/default-avatar.png',
          time: '刚刚',
          applyCount: 0,
          realname: false,
          credit: false,
          memberActive: false,
        },
      },
    })

    expect(wrapper.text()).toContain('未认证')
    expect(wrapper.text()).toContain('未缴担保金')
    expect(wrapper.text()).not.toContain('VIP')
  })
```

- [ ] **Step 2（步骤 2）: 运行前端聚焦测试，确认它先失败**

运行：

```bash
cd frontend && pnpm vitest run src/components/business/__tests__/keep-components.test.ts
```

预期：FAIL（失败），因为 `KeepYpatCardItem`（约拍卡片数据类型）还没有会员字段，组件也仍然只渲染旧的正向标签。

- [ ] **Step 3（步骤 3）: 更新 `KeepYpatCard`（约拍卡片组件）的 template（模板）和 interface（接口）**

在 `frontend/src/components/business/KeepYpatCard.vue` 中，将整个 `<template>` 替换为：

```vue
<template>
  <view class="keep-ypat-card" @tap="$emit('tap', item)">
    <image class="keep-ypat-card__thumb" :src="item.image" mode="aspectFill" lazy-load />
    <view class="keep-ypat-card__body">
      <view class="keep-ypat-card__head">
        <text class="keep-ypat-card__title">{{ item.title }}</text>
      </view>

      <view class="keep-ypat-card__tags">
        <text class="keep-ypat-card__tag keep-ypat-card__tag--main">{{ item.targetLabel }}</text>
        <text class="keep-ypat-card__tag keep-ypat-card__tag--way">{{ item.chargeLabel }}</text>
        <text class="keep-ypat-card__badge" :class="item.realname ? 'keep-ypat-card__badge--real' : 'keep-ypat-card__badge--muted'">
          <KeepIcon name="shield" :size="20" />
          {{ item.realname ? '已认证' : '未认证' }}
        </text>
        <text class="keep-ypat-card__badge" :class="item.credit ? 'keep-ypat-card__badge--credit' : 'keep-ypat-card__badge--muted'">
          <KeepIcon name="star" :size="20" />
          {{ item.credit ? '已缴担保金' : '未缴担保金' }}
        </text>
      </view>

      <view class="keep-ypat-card__user">
        <image class="keep-ypat-card__avatar" :src="item.avatar" mode="aspectFill" />
        <view class="keep-ypat-card__identity">
          <view class="keep-ypat-card__name-row">
            <text class="keep-ypat-card__name">{{ item.name }}</text>
            <text v-if="item.memberActive" class="keep-ypat-card__member">
              <KeepIcon name="gem" :size="18" />
              VIP
            </text>
          </view>
          <text class="keep-ypat-card__city">
            <KeepIcon name="map-pin" :size="22" />
            {{ item.city }}
          </text>
        </view>
      </view>

      <view class="keep-ypat-card__meta">
        <text>已收到约拍 {{ item.applyCount }}</text>
        <text>{{ item.time }}</text>
      </view>
    </view>
  </view>
</template>
```

在 `KeepYpatCardItem`（约拍卡片数据类型）接口中添加：

```ts
  memberActive?: boolean
  memberLevel?: string
```

- [ ] **Step 4（步骤 4）: 替换 `KeepYpatCard`（约拍卡片组件）的 SCSS（样式）**

在同一个文件中，将整个 `<style scoped lang="scss">` 内容替换为：

```scss

.keep-ypat-card {
  display: flex;
  gap: 24rpx;
  padding: 24rpx;
  margin-bottom: 22rpx;
  background: $color-bg-card;
  border: 1rpx solid rgba(27, 30, 35, 0.04);
  border-radius: $radius-keep-card;
  box-shadow: $shadow-keep-card;
}

.keep-ypat-card__thumb {
  width: 204rpx;
  height: 268rpx;
  flex: none;
  border-radius: 24rpx;
  background: $color-bg-chip;
}

.keep-ypat-card__body {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.keep-ypat-card__head {
  min-width: 0;
}

.keep-ypat-card__title {
  @include line-clamp(2);
  color: $color-text-primary;
  font-size: 32rpx;
  font-weight: 900;
  line-height: 1.35;
}

.keep-ypat-card__tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 16rpx;
}

.keep-ypat-card__tag,
.keep-ypat-card__badge,
.keep-ypat-card__member {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  min-height: 40rpx;
  padding: 0 12rpx;
  border-radius: 10rpx;
  font-size: 21rpx;
  font-weight: 800;
  line-height: 40rpx;
}

.keep-ypat-card__tag--main {
  color: #0F7E46;
  background: $color-primary-light;
}

.keep-ypat-card__tag--way {
  color: $color-text-secondary;
  background: $color-bg-chip;
}

.keep-ypat-card__badge--real {
  color: #5577A8;
  background: $color-blue-soft;
}

.keep-ypat-card__badge--credit {
  color: #9C7836;
  background: $color-gold-soft;
}

.keep-ypat-card__badge--muted {
  color: $color-text-helper;
  background: #F2F3F5;
}

.keep-ypat-card__user {
  display: flex;
  align-items: center;
  min-width: 0;
  margin-top: 18rpx;
}

.keep-ypat-card__avatar {
  width: 42rpx;
  height: 42rpx;
  flex: none;
  border-radius: 50%;
  background: $color-bg-chip;
}

.keep-ypat-card__identity {
  min-width: 0;
  flex: 1;
  margin-left: 12rpx;
}

.keep-ypat-card__name-row {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 8rpx;
}

.keep-ypat-card__name {
  max-width: 190rpx;
  overflow: hidden;
  color: $color-text-primary;
  font-size: 25rpx;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.keep-ypat-card__member {
  min-height: 32rpx;
  padding: 0 9rpx;
  border-radius: $radius-round;
  color: #6A4300;
  background: linear-gradient(135deg, #FFE9A6, #F5B642);
  font-size: 18rpx;
  line-height: 32rpx;
}

.keep-ypat-card__city {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  margin-top: 4rpx;
  color: $color-text-helper;
  font-size: 22rpx;
  font-weight: 700;
}

.keep-ypat-card__meta {
  display: flex;
  justify-content: space-between;
  gap: 12rpx;
  margin-top: auto;
  padding-top: 16rpx;
  color: $color-text-helper;
  font-size: 23rpx;
  font-weight: 700;
}
```

- [ ] **Step 5（步骤 5）: 运行前端聚焦测试**

运行：

```bash
cd frontend && pnpm vitest run src/components/business/__tests__/keep-components.test.ts
```

预期：PASS（通过）。

- [ ] **Step 6（步骤 6）: 提交卡片重构**

运行：

```bash
git add frontend/src/components/business/KeepYpatCard.vue frontend/src/components/business/__tests__/keep-components.test.ts
git commit -m "feat: refresh ypat card trust badges"
```

---

### Task 4（任务 4）: 重构 `YpatDetailView`（约拍详情视图组件）

**Files（文件）:**
- Modify（修改）: `frontend/src/components/business/YpatDetailView.vue`
- Test（测试）: Task 5（任务 5）中的 TypeScript（类型脚本）检查和手动源代码检查。

- [ ] **Step 1（步骤 1）: 新增 computed（计算属性）形式的信任状态辅助数据**

在 `frontend/src/components/business/YpatDetailView.vue` 中，在 `const targetLabel = computed(...)` 后添加：

```ts
const authorName = computed(() => detail.value?.userQo?.nickname || '匿名用户')
const isRealname = computed(() => detail.value?.realnameflag === '1' || detail.value?.userQo?.realnameflag === '1')
const isCredit = computed(() => detail.value?.creditflag === '1' || detail.value?.userQo?.creditflag === '1')
const isMember = computed(() => detail.value?.userQo?.memberActive === true)
const memberLevelText = computed(() => detail.value?.userQo?.memberLevel || 'VIP')
```

将现有 `authorDesc` computed（作者描述计算属性）替换为：

```ts
const authorDesc = computed(() => {
  const city = cityText.value === '同城' ? '同城创作者' : cityText.value
  return `摄影约拍用户 · ${city}`
})
```

- [ ] **Step 2（步骤 2）: 在 template（模板）中加入详情信任状态区**

在 template（模板）中，在现有 `detail-tags` 块之后、`detail-title` 之前插入：

```vue
        <view class="trust-row">
          <text class="trust-pill" :class="isRealname ? 'trust-pill--real' : 'trust-pill--muted'">
            <KeepIcon name="shield" :size="24" />
            {{ isRealname ? '已认证' : '未认证' }}
          </text>
          <text class="trust-pill" :class="isCredit ? 'trust-pill--credit' : 'trust-pill--muted'">
            <KeepIcon name="star" :size="24" />
            {{ isCredit ? '已缴担保金' : '未缴担保金' }}
          </text>
          <text v-if="isMember" class="trust-pill trust-pill--member">
            <KeepIcon name="gem" :size="24" />
            {{ memberLevelText }}
          </text>
        </view>
```

- [ ] **Step 3（步骤 3）: 替换作者卡片 template（模板）**

将当前 `author-card` 块：

```vue
        <view class="author-card" @tap="goProfile">
          <image class="author-card__avatar" :src="authorAvatar" mode="aspectFill" />
          <view class="author-card__info">
            <text class="author-card__name">{{ detail.userQo?.nickname || '匿名用户' }}</text>
            <text class="author-card__desc">{{ authorDesc }}</text>
          </view>
          <view class="author-card__profile">查看主页</view>
        </view>
```

替换为：

```vue
        <view class="author-card" @tap="goProfile">
          <image class="author-card__avatar" :src="authorAvatar" mode="aspectFill" />
          <view class="author-card__info">
            <view class="author-card__name-row">
              <text class="author-card__name">{{ authorName }}</text>
              <text v-if="isMember" class="author-card__member">
                <KeepIcon name="gem" :size="20" />
                VIP
              </text>
            </view>
            <text class="author-card__desc">{{ authorDesc }}</text>
            <view class="author-card__badges">
              <text class="author-card__badge" :class="isRealname ? 'author-card__badge--real' : 'author-card__badge--muted'">
                {{ isRealname ? '已认证' : '未认证' }}
              </text>
              <text class="author-card__badge" :class="isCredit ? 'author-card__badge--credit' : 'author-card__badge--muted'">
                {{ isCredit ? '已缴担保金' : '未缴担保金' }}
              </text>
            </view>
          </view>
          <view class="author-card__profile">主页</view>
        </view>
```

- [ ] **Step 4（步骤 4）: 增加详情信任区和作者卡片样式**

在 `<style scoped lang="scss">` 中，在 `.detail-tags, .style-tags { ... }` 后添加：

```scss
.trust-row {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 22rpx;
}

.trust-pill {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  min-height: 48rpx;
  padding: 0 18rpx;
  border-radius: $radius-round;
  font-size: 23rpx;
  font-weight: 900;
  line-height: 48rpx;
}

.trust-pill--real {
  color: #5577A8;
  background: $color-blue-soft;
}

.trust-pill--credit {
  color: #9C7836;
  background: $color-gold-soft;
}

.trust-pill--member {
  color: #6A4300;
  background: linear-gradient(135deg, #FFE9A6, #F5B642);
}

.trust-pill--muted {
  color: $color-text-helper;
  background: #F2F3F5;
}
```

将 `.author-card {` 到 `.author-card__profile { ... }` 的作者相关样式块替换为：

```scss
.author-card {
  display: flex;
  align-items: center;
  gap: 22rpx;
  margin-top: 36rpx;
  padding: 28rpx;
  border: 1rpx solid rgba(27, 30, 35, 0.08);
  border-radius: $radius-keep-card;
  background: #fff;
}

.author-card__avatar {
  width: 104rpx;
  height: 104rpx;
  flex: none;
  border-radius: 50%;
  background: $color-bg-chip;
}

.author-card__info {
  min-width: 0;
  flex: 1;
}

.author-card__name-row {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 10rpx;
}

.author-card__name,
.author-card__desc {
  display: block;
}

.author-card__name {
  max-width: 260rpx;
  overflow: hidden;
  color: $color-text-primary;
  font-size: 32rpx;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.author-card__member {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  min-height: 34rpx;
  padding: 0 10rpx;
  border-radius: $radius-round;
  color: #6A4300;
  background: linear-gradient(135deg, #FFE9A6, #F5B642);
  font-size: 18rpx;
  font-weight: 900;
  line-height: 34rpx;
}

.author-card__desc {
  margin-top: 8rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
}

.author-card__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 12rpx;
}

.author-card__badge {
  min-height: 36rpx;
  padding: 0 12rpx;
  border-radius: 10rpx;
  font-size: 20rpx;
  font-weight: 800;
  line-height: 36rpx;
}

.author-card__badge--real {
  color: #5577A8;
  background: $color-blue-soft;
}

.author-card__badge--credit {
  color: #9C7836;
  background: $color-gold-soft;
}

.author-card__badge--muted {
  color: $color-text-helper;
  background: #F2F3F5;
}

.author-card__profile {
  flex: none;
  padding: 16rpx 24rpx;
  border-radius: $radius-round;
  color: $color-primary-dark;
  background: $color-primary-light;
  font-size: 24rpx;
  font-weight: 900;
}
```

- [ ] **Step 5（步骤 5）: 运行源代码检查**

运行：

```bash
rg -n "trust-row|trust-pill|authorName|memberLevelText|已缴担保金|未缴担保金|author-card__member" frontend/src/components/business/YpatDetailView.vue
```

预期：所有关键词都出现在 `YpatDetailView.vue` 中。

- [ ] **Step 6（步骤 6）: 提交详情页重构**

运行：

```bash
git add frontend/src/components/business/YpatDetailView.vue
git commit -m "feat: refresh ypat detail trust layout"
```

---

### Task 5（任务 5）: 验证与最终清理

**Files（文件）:**
- 预期不新增文件。
- 验证 Task 1-4（任务 1-4）中修改过的所有文件。

- [ ] **Step 1（步骤 1）: 运行后端聚焦测试**

运行：

```bash
mvn -pl backend/system-domain -Dtest=YpatInfoAdminFilterSourceTest test
```

预期：PASS（通过）。

- [ ] **Step 2（步骤 2）: 运行前端聚焦组件测试**

运行：

```bash
cd frontend && pnpm vitest run src/components/business/__tests__/keep-components.test.ts
```

预期：PASS（通过）。

- [ ] **Step 3（步骤 3）: 运行前端类型检查**

运行：

```bash
cd frontend && pnpm run type-check
```

预期：PASS（通过）。

- [ ] **Step 4（步骤 4）: 检查最终 diff（差异）**

运行：

```bash
git diff --stat
git diff -- backend/system-object/src/main/java/com/ypat/UserQo.java backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java frontend/src/pages/home/index.vue frontend/src/components/business/KeepYpatCard.vue frontend/src/components/business/YpatDetailView.vue
```

预期：
- 没有不相关文件变更。
- `home-message`（消息入口）渲染块已移除。
- `quickChips`（快速筛选标签）顺序以 `全部`、`约模特`、`约摄影师` 开头。
- 后端返回 `memberActive/memberLevel`（会员字段）。
- 卡片和详情页都展示正向与反向信任状态。

- [ ] **Step 5（步骤 5）: 提交验证过程中产生的小修正**

如果 Step 1-4（步骤 1-4）需要小修正，提交它们：

```bash
git add backend/system-object/src/main/java/com/ypat/UserQo.java backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java backend/system-domain/src/test/java/com/ypat/service/YpatInfoAdminFilterSourceTest.java frontend/src/api/types/index.ts frontend/src/pages/home/index.vue frontend/src/components/business/KeepYpatCard.vue frontend/src/components/business/YpatDetailView.vue frontend/src/components/business/__tests__/keep-components.test.ts
git commit -m "chore: verify ypat trust refresh"
```

如果前面任务提交后没有额外文件变更，跳过这个提交。

---

## Self-Review（自检）

- Spec coverage（规格覆盖）: 后端真实会员字段、首页消息入口隐藏、筛选标签顺序、卡片认证/担保/会员展示、详情页认证/担保/会员展示、测试验证均有任务覆盖。
- Placeholder scan（占位检查）: 没有待定占位、空泛实现描述或含糊测试步骤。
- Type consistency（类型一致性）: 后端使用 `memberActive/memberLevel`；前端 `UserInfo`（用户信息类型）和 `KeepYpatCardItem`（约拍卡片数据类型）使用同名属性；详情页读取 `detail.userQo.memberActive/memberLevel`。
