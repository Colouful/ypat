<template>
  <view class="mine-page">
    <view class="mine-scroll" :style="{ paddingTop: statusBarHeight + 20 + 'px' }">
      <view class="mine-top">
        <view class="mine-top__icon" @tap="goSettings">
          <KeepIcon name="menu" :size="46" />
        </view>
        <view class="mine-top__mail" @tap="goMessage">
          <KeepIcon name="mail" :size="48" />
          <text v-if="unreadCount > 0" class="mine-top__badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</text>
        </view>
      </view>

      <view v-if="isLoggedIn" class="profile-card">
        <view class="profile-card__main" @tap="goProfile">
          <image class="profile-card__avatar" :src="avatar" mode="aspectFill" />
          <view class="profile-card__body">
            <view class="profile-card__name-row">
              <text class="profile-card__name">{{ displayName }}</text>
              <text v-if="genderLabel" class="profile-card__gender">{{ genderLabel }}</text>
            </view>
            <text class="profile-card__meta">{{ professLabel || '未设置身份' }} · {{ userInfo?.city || '未设置城市' }}</text>
            <view class="profile-card__tags">
              <text class="profile-tag" :class="{ 'profile-tag--ok': realnameState.done }">{{ realnameState.text }}</text>
              <text class="profile-tag" :class="{ 'profile-tag--ok': creditState.done }">{{ creditState.text }}</text>
            </view>
          </view>
          <KeepIcon name="chevron-right" :size="42" color="#B3B8BE" />
        </view>
        <view class="profile-card__actions">
          <view class="profile-action" @tap="goProfile">个人资料</view>
          <view class="profile-action profile-action--primary" @tap="goHomepage">个人主页</view>
        </view>
      </view>

      <view v-else class="login-panel">
        <KeepState
          type="login"
          title="登录后查看我的约拍"
          description="登录后可查看发布、收藏、消息、钱包和认证状态。"
          button-text="去登录"
          @action="goLogin"
        />
      </view>

      <view class="core-grid">
        <view v-if="showWallet" class="core-item" @tap="goWallet">
          <KeepIcon name="wallet" :size="50" />
          <text class="core-item__value">{{ userInfo?.ppd || 0 }}</text>
          <text class="core-item__label">我的钱包</text>
        </view>
        <view class="core-item" @tap="goPublish">
          <KeepIcon name="camera" :size="50" />
          <text class="core-item__value">{{ userInfo?.pubtimes || 0 }}</text>
          <text class="core-item__label">我的发布</text>
        </view>
        <view class="core-item" @tap="goApply">
          <KeepIcon name="check" :size="50" />
          <text class="core-item__value">{{ receivedCount }}</text>
          <text class="core-item__label">我的约拍</text>
        </view>
        <view class="core-item" @tap="goFavorite">
          <KeepIcon name="star" :size="50" />
          <text class="core-item__value">{{ userInfo?.coltimes || 0 }}</text>
          <text class="core-item__label">我的收藏</text>
        </view>
      </view>

      <view class="service-card">
        <view
          v-for="item in serviceItems"
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

      <button v-if="isLoggedIn" class="logout" @tap="handleLogout">退出登录</button>
    </view>

    <KeepTabBar active="mine" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import * as contentApi from '@/api/modules/content'
import * as ypatApi from '@/api/modules/ypat'
import { GENDER_LABELS, PROFESS_LABELS } from '@/constants/enums'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepState from '@/components/business/KeepState.vue'
import KeepTabBar from '@/components/business/KeepTabBar.vue'
import { openMessage } from '@/utils/tab-navigation'
import { isAdminOpenid } from '@/constants/admin'
import type { ParamInfo } from '@/api/types/area-types'

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

const userStore = useUserStore()
const appStore = useAppStore()
const params = ref<ParamInfo | null>(null)
const receivedCount = ref(0)

const statusBarHeight = computed(() => appStore.statusBarHeight)
const unreadCount = computed(() => userStore.unreadCount)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)
const avatar = computed(() => userInfo.value?.imgpath || userInfo.value?.avatarurl || '/static/default-avatar.png')
const showWallet = computed(() => params.value?.realname !== '0')
const displayName = computed(() => userInfo.value?.nickname || maskMobile(userInfo.value?.mobile) || '未设置昵称')
const genderLabel = computed(() => {
  const gender = userInfo.value?.gender
  return gender ? GENDER_LABELS[gender] || '' : ''
})
const professLabel = computed(() => {
  const code = userInfo.value?.profess
  return code ? PROFESS_LABELS[code] || '' : ''
})
const realnameState = computed(() => {
  const info = userInfo.value
  if (info?.realnameflag === '1' || info?.status === '2') return { text: '已认证', done: true }
  if (info?.status === '1') return { text: '审核中', done: false }
  if (info?.status === '3') return { text: '认证失败', done: false }
  return { text: '未认证', done: false }
})
const creditState = computed(() => (
  userInfo.value?.creditflag === '1'
    ? { text: '已信用担保', done: true }
    : { text: '未信用担保', done: false }
))
// 后端无 role/isAdmin 字段，通过 openid 比对兜底；常量与 backend Const.SYS_ADMIN 同步。
const isAdmin = computed(() => isAdminOpenid(userInfo.value?.openid))
const realnameAvailable = computed(() => params.value?.realname !== '0')
const messageBadge = computed(() => (unreadCount.value > 0 ? String(unreadCount.value) : ''))

// spec §6.2.4 顺序的连续功能列表，无分类标题；管理员入口在末尾追加。
const serviceItems = computed<ServiceItem[]>(() => {
  const items: ServiceItem[] = [
    { title: '我的消息', icon: 'mail', url: '/pages/message/index', badge: messageBadge.value, auth: true },
    { title: '我的主页', icon: 'user', action: goHomepage, auth: true },
    { title: '好友邀请', icon: 'users', desc: '邀请好友，享拍拍豆奖励', url: '/pages-sub/user/invite', auth: true },
    { title: '会员中心', icon: 'star', desc: '开通会员享专属权益', url: '/pages-sub/user/member/index', auth: true },
  ]
  if (realnameAvailable.value) {
    items.push({ title: '实名认证', icon: 'shield', url: '/pages-sub/user/realname', badge: realnameState.value.text, auth: true })
    items.push({ title: '信用担保', icon: 'lock', badge: creditState.value.text, auth: true, disabled: true, action: handleCredit })
  }
  if (showWallet.value) {
    items.push({ title: '我的钱包', icon: 'wallet', url: '/pages-sub/user/wallet', auth: true })
  }
  items.push({ title: '收支记录', icon: 'chart', url: '/pages-sub/user/records', auth: true })
  items.push({ title: '帮助中心', icon: 'help-circle', url: '/pages-sub/user/helpcenter' })
  items.push({ title: '意见反馈', icon: 'edit', url: '/pages-sub/user/feedback', auth: true })
  items.push({ title: '关于我们', icon: 'camera', url: '/pages-sub/user/about' })
  items.push({ title: '设置', icon: 'menu', url: '/pages-sub/user/settings', auth: true })
  if (isAdmin.value) {
    items.push({ title: '信息审核', icon: 'shield', desc: '管理员入口', url: '/pages-sub/user/admin-audit-soon', auth: true })
    items.push({ title: '消息授权', icon: 'mail', desc: '订阅消息模板授权（待迁移）', auth: true, disabled: true, action: () => uni.showToast({ title: '订阅消息授权将在切片 2 接入', icon: 'none' }) })
  }
  return items
})

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
  if (item.auth && !requireLogin()) return
  if (item.action) {
    item.action()
    return
  }
  if (item.url) {
    if (item.url === '/pages/message/index') {
      openMessage()
      return
    }
    uni.navigateTo({ url: item.url })
  }
}

function handleLogout(): void {
  uni.showModal({
    title: '退出登录',
    content: '退出后需重新登录才能查看消息、钱包等信息。',
    success: ({ confirm }) => {
      if (confirm) userStore.logout()
    },
  })
}

function handleCredit(): void {
  if (!requireLogin()) return
  if (creditState.value.done) {
    uni.showToast({ title: '已信用担保', icon: 'none' })
    return
  }
  uni.showToast({ title: '信用担保功能待迁移', icon: 'none' })
}

async function loadPlatformParams(): Promise<void> {
  try {
    params.value = (await contentApi.getParams()).data || null
  } catch {
    params.value = null
  }
}

async function loadMineData(): Promise<void> {
  await loadPlatformParams()
  if (!isLoggedIn.value || !userInfo.value?.id) return
  await Promise.all([
    userStore.updateUserInfo(),
    userStore.refreshUnreadCount(),
    loadReceivedCount(),
  ])
}

async function loadReceivedCount(): Promise<void> {
  const userid = userInfo.value?.id
  if (!userid) {
    receivedCount.value = 0
    return
  }
  try {
    const result = await ypatApi.getMyReceivedList({ userid, page: 0, size: 1 })
    receivedCount.value = Number(result.data?.totalElements || 0)
  } catch {
    receivedCount.value = Number(userInfo.value?.rectimes || 0)
  }
}

function goLogin(): void { uni.navigateTo({ url: '/pages/login/index' }) }
function goMessage(): void { openMessage() }
function goProfile(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/user/profile' }) }
function goSettings(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/user/settings' }) }
function goHomepage(): void {
  if (!requireLogin()) return
  const id = userInfo.value?.id
  uni.navigateTo({ url: id ? `/pages-sub/user/profile?id=${id}` : '/pages-sub/user/profile' })
}
function goPublish(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/ypat/my-publish' }) }
function goApply(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/ypat/my-apply' }) }
function goFavorite(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/ypat/my-favorite' }) }
function goWallet(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/user/wallet' }) }

onShow(() => {
  void loadMineData()
})

onPullDownRefresh(async () => {
  await loadMineData()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss">

.mine-page {
  min-height: 100vh;
  background: $color-bg-page;
}

.mine-scroll {
  padding: 0 32rpx calc(176rpx + env(safe-area-inset-bottom));
}

.mine-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mine-top__icon,
.mine-top__mail {
  position: relative;
  @include flex-center;
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  color: $color-text-primary;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.mine-top__badge {
  position: absolute;
  top: -10rpx;
  right: -16rpx;
  min-width: 34rpx;
  height: 34rpx;
  padding: 0 8rpx;
  border: 4rpx solid #fff;
  border-radius: $radius-round;
  color: #fff;
  background: $color-accent-red;
  font-size: 20rpx;
  font-weight: 800;
  line-height: 34rpx;
  text-align: center;
}

.profile-card,
.login-panel,
.core-grid,
.service-card {
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.profile-card {
  margin-top: 34rpx;
  padding: 30rpx;
}

.profile-card__main {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.profile-card__avatar {
  width: 124rpx;
  height: 124rpx;
  flex: none;
  border-radius: 50%;
  background: $color-bg-chip;
}

.profile-card__body {
  min-width: 0;
  flex: 1;
}

.profile-card__name-row,
.profile-card__tags,
.profile-card__actions {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.profile-card__name {
  max-width: 330rpx;
  overflow: hidden;
  color: $color-text-primary;
  font-size: 40rpx;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-card__gender,
.profile-tag {
  flex: none;
  padding: 5rpx 14rpx;
  border-radius: $radius-round;
  color: $color-text-secondary;
  background: $color-bg-chip;
  font-size: 22rpx;
  font-weight: 800;
}

.profile-card__meta {
  display: block;
  margin-top: 10rpx;
  color: $color-text-secondary;
  font-size: 26rpx;
  font-weight: 700;
}

.profile-card__tags {
  flex-wrap: wrap;
  margin-top: 16rpx;
}

.profile-tag--ok {
  color: $color-primary-dark;
  background: $color-primary-light;
}

.profile-card__actions {
  margin-top: 28rpx;
}

.profile-action {
  flex: 1;
  height: 76rpx;
  border-radius: $radius-round;
  color: $color-text-primary;
  background: $color-bg-chip;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 76rpx;
  text-align: center;
}

.profile-action--primary {
  color: #fff;
  background: $color-text-primary;
}

.login-panel {
  margin-top: 34rpx;
}

.core-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10rpx;
  margin-top: 28rpx;
  padding: 28rpx 16rpx;
}

.core-item {
  @include flex-column;
  align-items: center;
  gap: 10rpx;
  min-width: 0;
  color: $color-text-primary;
}

.core-item__value {
  max-width: 100%;
  overflow: hidden;
  font-size: 32rpx;
  font-weight: 900;
  line-height: 1.1;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.core-item__label {
  color: $color-text-secondary;
  font-size: 23rpx;
  font-weight: 800;
}

.service-card {
  margin-top: 34rpx;
  overflow: hidden;
}

.logout {
  margin: 36rpx 0 0;
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

.service-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 30rpx;
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

.service-row__title,
.service-row__desc {
  display: block;
}

.service-row__title {
  color: $color-text-primary;
  font-size: 29rpx;
  font-weight: 900;
}

.service-row__desc {
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
</style>
