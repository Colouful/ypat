# 会员中心紧凑布局实施计划

> **执行要求：** 使用 executing-plans(执行计划) 技能在当前任务内逐项实施，不使用 subagent(子代理)。步骤使用复选框跟踪。

**目标：** 将新版会员中心改为“紧凑会员状态卡 → 双列套餐 → 2×2 完整权益”的 A2 布局，同时保持套餐选择和支付行为不变。

**架构：** 只修改单个 Vue(Vue 前端框架)页面的模板与局部 SCSS(Sass 样式)，并新增源代码约束测试。套餐仍由现有接口加载，推荐与选中状态仍由现有计算属性驱动，不新增组件或状态。

**技术栈：** Vue 3(Vue 前端框架)、TypeScript(TypeScript 类型语言)、uni-app(跨端小程序框架)、SCSS(Sass 样式)、Vitest(单元测试工具)。

---

### 任务一：锁定 A2 页面结构

**文件：**

- 新增：`frontend/src/pages-sub/user/member/index.test.ts`
- 修改：`frontend/src/pages-sub/user/member/index.vue`

- [ ] **步骤一：新增失败的源代码测试**

测试读取 `index.vue`(会员中心页面)，要求套餐区位于权益区之前、套餐使用双列网格和奇数末项整行规则，并禁止旧套餐权益列表与大号选择条：

```ts
import fs from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'

describe('会员中心紧凑布局', () => {
  const source = fs.readFileSync(path.resolve(__dirname, 'index.vue'), 'utf8')

  it('在会员状态卡与完整权益之间展示双列套餐', () => {
    expect(source.indexOf('section--plans')).toBeLessThan(source.indexOf('section--benefits'))
    expect(source).toContain('grid-template-columns: repeat(2, minmax(0, 1fr))')
    expect(source).toContain('.plan-card:last-child:nth-child(odd)')
    expect(source).toContain('benefit-item__desc')
    expect(source).not.toContain('plan-card__benefits')
    expect(source).not.toContain('plan-card__select')
  })
})
```

- [ ] **步骤二：运行测试确认红灯**

执行：

```bash
cd frontend
./node_modules/.bin/vitest run src/pages-sub/user/member/index.test.ts
```

预期：测试因缺少 `section--plans`(套餐区样式类)或仍存在旧套餐权益结构而失败。

- [ ] **步骤三：调整模板顺序与套餐内容**

把套餐区移动到权益区之前并增加 `section--plans`(套餐区样式类)。套餐卡只保留套餐名、推荐标签、有效期、赠送信息、等级、现价、原价和选中勾选标识：

```vue
<view class="section section--plans">
  <view class="section__head">
    <text class="section__title">选择套餐</text>
    <text class="section__sub">权益随套餐一起开通</text>
  </view>
  <view class="plans">
    <view
      v-for="plan in plans"
      :key="plan.id"
      class="plan-card"
      :class="{ 'plan-card--recommended': isRecommended(plan), 'plan-card--selected': selectedPlan?.id === plan.id }"
      @tap="selectPlan(plan)"
    >
      <!-- 现有套餐字段的紧凑展示 -->
      <view class="plan-card__check">{{ selectedPlan?.id === plan.id ? '✓' : '' }}</view>
    </view>
  </view>
</view>
```

删除模板中的 `plan-card__benefits`(套餐权益列表)和 `plan-card__select`(套餐选择条)，并删除不再使用的 `getPlanBenefits`(套餐权益解析函数)。会员权益区继续完整渲染 `benefit-item__desc`(权益描述)。

### 任务二：实现紧凑双列视觉

**文件：**

- 修改：`frontend/src/pages-sub/user/member/index.vue`

- [ ] **步骤一：压缩会员状态卡**

将会员状态卡垂直内边距、标题上间距、标签间距和装饰尺寸减少约四分之一；保留当前深色背景、金色会员标识和激活状态背景。

- [ ] **步骤二：实现套餐双列与奇数末项整行**

```scss
.plans { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14rpx; }
.plan-card:last-child:nth-child(odd) { grid-column: 1 / -1; }
.plan-card { min-width: 0; min-height: 196rpx; padding: 22rpx; }
.plan-card:last-child:nth-child(odd) .plan-card__main {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
}
```

半宽套餐价格左对齐，奇数末项整行套餐价格右对齐。推荐标签和勾选标识使用绝对定位，卡片整体继续响应点击。

- [ ] **步骤三：保留完整 2×2 权益并降低占高**

保持 `.benefit-grid`(权益网格)双列，缩小间距、内边距和图标尺寸。`benefit-item__desc`(权益描述)允许自然换行，不使用省略号或行数截断。

- [ ] **步骤四：运行测试确认绿灯**

执行：

```bash
cd frontend
./node_modules/.bin/vitest run src/pages-sub/user/member/index.test.ts
```

预期：1 个测试文件全部通过。

### 任务三：定向检查与视觉验证

**文件：**

- 复核：`frontend/src/pages-sub/user/member/index.vue`
- 复核：`frontend/src/pages-sub/user/member/index.test.ts`

- [ ] **步骤一：执行定向代码规范检查**

```bash
cd frontend
./node_modules/.bin/eslint src/pages-sub/user/member/index.vue src/pages-sub/user/member/index.test.ts --quiet
```

预期：退出码为 0。

- [ ] **步骤二：确认行为脚本边界**

通过 `git diff`(Git 差异)确认 `loadPlans`(加载套餐)、`selectPlan`(选择套餐)、`submitSelected`(提交套餐)、`createOrderAndPay`(创建订单并支付)和支付回调逻辑未改变；只允许删除不再使用的展示函数 `getPlanBenefits`(套餐权益解析函数)。

- [ ] **步骤三：执行差异格式检查**

```bash
git diff --check -- frontend/src/pages-sub/user/member/index.vue frontend/src/pages-sub/user/member/index.test.ts
```

预期：退出码为 0。

- [ ] **步骤四：启动本地开发服务并做移动端视觉验证**

使用已有依赖启动 H5(H5 网页端)开发服务，在常见移动端视口检查：套餐位于状态卡与权益之间；包月和包季并列；包年横跨整行；四项权益描述完整换行；没有文字重叠或横向溢出；底部操作栏不遮挡内容。

