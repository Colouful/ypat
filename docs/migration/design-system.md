# YPAT 设计系统

## 视觉定位

新版 YPAT 爱去拍采用 Keep 风格设计语言：
- 清新、现代、有品质感
- 绿色为主色调，传递活力和信任
- 大量白色背景，信息层级清晰
- 圆角卡片、轻阴影
- 摄影作品沉浸式展示

## 色彩体系

### 品牌主色
| Token | 色值 | 用途 |
|-------|------|------|
| $color-primary | #23C268 | 主按钮、导航高亮、选中状态 |
| $color-primary-dark | #17A857 | 按钮按下、深色强调 |
| $color-primary-light | #C9F4D9 | 标签背景、轻量提示 |

### 功能色
| Token | 色值 | 用途 |
|-------|------|------|
| $color-accent-orange | #FF9F1C | 拍拍豆、积分相关 |
| $color-accent-red | #FF4D4F | 错误、警告、未读角标 |
| $color-accent-purple | #9B59B6 | VIP、特殊标识 |
| $color-accent-blue | #3498DB | 信息提示 |

### 中性色
| Token | 色值 | 用途 |
|-------|------|------|
| $color-text-primary | #1A1D1F | 标题、正文 |
| $color-text-secondary | #83888F | 副标题、说明文字 |
| $color-text-helper | #B3B8BE | 辅助文字、占位符 |
| $color-text-disabled | #D1D5DB | 禁用状态 |

### 背景和边框
| Token | 色值 | 用途 |
|-------|------|------|
| $color-bg-page | #F5F6F8 | 页面背景 |
| $color-bg-card | #FFFFFF | 卡片背景 |
| $color-border | #EEF0F2 | 边框、分割线 |
| $color-divider | #F0F0F0 | 列表分割线 |

## 字号体系

| Token | 尺寸 | 用途 |
|-------|------|------|
| $font-size-xs | 20rpx | 角标、辅助信息 |
| $font-size-sm | 24rpx | 标签、时间 |
| $font-size-base | 28rpx | 正文 |
| $font-size-md | 30rpx | 列表标题 |
| $font-size-lg | 32rpx | 区块标题 |
| $font-size-xl | 36rpx | 页面标题 |
| $font-size-xxl | 40rpx | 大数字 |
| $font-size-title | 44rpx | 特殊标题 |

## 字重体系

| Token | 值 | 用途 |
|-------|------|------|
| $font-weight-regular | 400 | 正文 |
| $font-weight-medium | 500 | 小标题 |
| $font-weight-semibold | 600 | 标题 |
| $font-weight-bold | 700 | 大标题、数字 |

## 间距体系

| Token | 尺寸 |
|-------|------|
| $spacing-xs | 8rpx |
| $spacing-sm | 16rpx |
| $spacing-md | 24rpx |
| $spacing-lg | 32rpx |
| $spacing-xl | 40rpx |
| $spacing-xxl | 48rpx |

## 圆角体系

| Token | 尺寸 | 用途 |
|-------|------|------|
| $radius-sm | 8rpx | 标签、小按钮 |
| $radius-md | 16rpx | 卡片、输入框 |
| $radius-lg | 24rpx | 大卡片 |
| $radius-xl | 32rpx | 按钮 |
| $radius-round | 999rpx | 胶囊按钮、头像 |

## 阴影体系

| Token | 值 | 用途 |
|-------|------|------|
| $shadow-sm | 0 2rpx 8rpx rgba(0,0,0,0.04) | 卡片 |
| $shadow-md | 0 4rpx 16rpx rgba(0,0,0,0.08) | 弹出层 |
| $shadow-lg | 0 8rpx 32rpx rgba(0,0,0,0.12) | 模态框 |

## 动画时长

| Token | 值 | 用途 |
|-------|------|------|
| $duration-fast | 150ms | 按钮反馈 |
| $duration-normal | 300ms | 页面切换 |
| $duration-slow | 500ms | 复杂动画 |

## 层级规范

| Token | 值 | 用途 |
|-------|------|------|
| $z-index-navbar | 100 | 导航栏 |
| $z-index-popup | 200 | 弹出层 |
| $z-index-mask | 300 | 遮罩 |
| $z-index-toast | 400 | 提示 |

## 字体栈

```css
font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "HarmonyOS Sans SC", "Microsoft YaHei", sans-serif;
```

## 图片比例

| 场景 | 比例 |
|------|------|
| Banner | 16:9 (690×300rpx) |
| 约拍封面 | 4:3 (690×520rpx) |
| 约拍列表缩略图 | 1:1 (200×200rpx) |
| 头像 | 1:1 圆形 |
| 身份证 | 85.6:54 (标准ID卡) |

## 安全区规范

- 顶部：状态栏高度 + 导航栏高度 (44px)
- 底部：TabBar高度 + env(safe-area-inset-bottom)
- iPhone X 系列底部安全距离：34px

## 暗色模式

当前版本不实现暗色模式，但 Design Token 结构已为暗色模式预留扩展空间。所有颜色通过 SCSS 变量引用，未来可通过 CSS 变量覆盖实现主题切换。
