<template>
  <view class="center-page">
    <KeepPageNav title="我的中心" />

    <view class="center-scroll">
      <view v-if="isLoggedIn" class="center-head">
        <image class="center-head__avatar" :src="avatar" mode="aspectFill" />
        <view class="center-head__body">
          <text class="center-head__name">{{ displayName }}</text>
          <text class="center-head__phone">{{ maskedMobile || '未绑定手机号' }}</text>
        </view>
      </view>

      <view v-else class="login-panel">
        <KeepState
          type="login"
          title="登录后管理我的信息"
          description="登录后可管理资料、设置和反馈。"
          button-text="去登录"
          @action="goLogin"
        />
      </view>

      <view v-for="group in visibleGroups" :key="group.title" class="service-group">
        <text class="service-group__title">{{ group.title }}</text>
        <view class="service-card">
          <view
            v-for="item in group.items"
            :key="item.title"
            class="service-row"
            :class="{ 'service-row--disabled': item.disabled }"
            @tap="handleService(item)"
          >
            <view class="service-row__icon">
              <KeepIcon :name="item.icon" :size="38" />
            </view>
            <view class="service-row__body">
              <text class="service-row__title">{{ item.title }}</text>
              <text v-if="item.desc" class="service-row__desc">{{ item.desc }}</text>
            </view>
            <view class="service-row__right">
              <text v-if="item.badge" class="service-row__badge">{{ item.badge }}</text>
              <KeepIcon name="chevron-right" :size="34" color="#B3B8BE" />
            </view>
          </view>
        </view>
      </view>

      <button v-if="isLoggedIn" class="logout" @tap="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import KeepState from '@/components/business/KeepState.vue'
import { isAdminOpenid } from '@/constants/admin'

interface ServiceItem {
  title: string
  icon: string
  url?: string
  desc?: string
  badge?: string
  auth?: boolean
  disabled?: boolean
  action?: () => void
}

interface ServiceGroup {
  title: string
  items: ServiceItem[]
  visible?: boolean
}

const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)
const avatar = computed(() => userInfo.value?.imgpath || userInfo.value?.avatarurl || '/static/default-avatar.png')
const displayName = computed(() => userInfo.value?.nickname || maskMobile(userInfo.value?.mobile) || '未设置昵称')
const maskedMobile = computed(() => maskMobile(userInfo.value?.mobile))
const isAdmin = computed(() => isAdminOpenid(userInfo.value?.openid))

const groups = computed<ServiceGroup[]>(() => [
  {
    title: '系统',
    items: [
      { title: '帮助中心', icon: 'help-circle', url: '/pages-sub/user/helpcenter' },
      { title: '意见反馈', icon: 'edit', url: '/pages-sub/user/feedback', auth: true },
      { title: '关于我们', icon: 'camera', url: '/pages-sub/user/about' },
      { title: '设置', icon: 'menu', url: '/pages-sub/user/settings', auth: true },
    ],
  },
  {
    title: '管理员',
    visible: isAdmin.value,
    items: [
      { title: '信息审核', icon: 'shield', desc: '管理员入口', url: '/pages-sub/user/admin-audit-soon', auth: true },
      { title: '消息授权', icon: 'mail', desc: '订阅消息模板授权（待迁移）', auth: true, disabled: true, action: () => uni.showToast({ title: '订阅消息授权将在切片 2 接入', icon: 'none' }) },
    ],
  },
])

const visibleGroups = computed(() => groups.value.filter(g => g.visible !== false))

function maskMobile(value?: string): string {
  if (!value) return ''
  return value.length >= 11 ? `${value.slice(0, 3)}****${value.slice(7)}` : value
}

function requireLogin(): boolean {
  if (isLoggedIn.value) return true
  uni.navigateTo({ url: '/pages/login/index' })
  return false
}

function handleService(item: ServiceItem): void {
  if (item.disabled) {
    if (item.action) item.action()
    return
  }
  if (item.auth && !requireLogin()) return
  if (item.action) {
    item.action()
    return
  }
  if (item.url) {
    uni.navigateTo({ url: item.url })
  }
}

function goLogin(): void { uni.navigateTo({ url: '/pages/login/index' }) }

function handleLogout(): void {
  uni.showModal({
    title: '退出登录',
    content: '退出后需重新登录才能查看消息、钱包等信息。',
    success: ({ confirm }) => {
      if (confirm) userStore.logout()
    },
  })
}

onShow(() => {
  if (isLoggedIn.value) void userStore.refreshUnreadCount()
})
</script>

<style lang="scss">
.center-page {
  min-height: 100vh;
  background: $color-bg-page;
}

.center-scroll {
  padding: 20rpx 32rpx calc(64rpx + env(safe-area-inset-bottom));
}

.center-head {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 24rpx 30rpx;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.center-head__avatar {
  width: 104rpx;
  height: 104rpx;
  flex: none;
  border-radius: 50%;
  background: $color-bg-chip;
}

.center-head__body {
  min-width: 0;
  flex: 1;
}

.center-head__name {
  display: block;
  color: $color-text-primary;
  font-size: 34rpx;
  font-weight: 900;
}

.center-head__phone {
  display: block;
  margin-top: 8rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 700;
}

.login-panel {
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.service-group {
  margin-top: 36rpx;
}

.service-group__title {
  display: block;
  margin: 0 8rpx 14rpx;
  color: $color-text-helper;
  font-size: 24rpx;
  font-weight: 800;
}

.service-card {
  overflow: hidden;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.service-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx 30rpx;
  border-bottom: 1rpx solid $color-border;
}

.service-row:last-child {
  border-bottom: 0;
}

.service-row--disabled {
  opacity: 0.68;
}

.service-row__icon {
  @include flex-center;
  width: 72rpx;
  height: 72rpx;
  flex: none;
  border-radius: 24rpx;
  color: $color-text-primary;
  background: $color-bg-chip;
}

.service-row__body {
  min-width: 0;
  flex: 1;
}

.service-row__title {
  display: block;
  color: $color-text-primary;
  font-size: 29rpx;
  font-weight: 900;
}

.service-row__desc {
  display: block;
  margin-top: 6rpx;
  color: $color-text-helper;
  font-size: 23rpx;
  font-weight: 700;
}

.service-row__right {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.service-row__badge {
  max-width: 150rpx;
  overflow: hidden;
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout {
  margin-top: 40rpx;
  height: 92rpx;
  border-radius: 999rpx;
  color: $color-accent-red;
  background: #fff;
  box-shadow: $shadow-keep-card;
  font-size: 28rpx;
  font-weight: 800;
  line-height: 92rpx;
}

.logout::after {
  border: 0;
}
</style>
