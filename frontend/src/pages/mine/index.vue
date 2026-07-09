<template>
  <view class="mine-page">
    <view class="mine-scroll" :style="{ paddingTop: statusBarHeight + 20 + 'px' }">
      <view class="mine-top">
        <view class="mine-top__left">
          <view class="mine-top__icon" @tap="goCenter">
            <KeepIcon name="menu" :size="42" />
          </view>
          <view
            v-if="showCheckinEntry"
            class="mine-top__checkin"
            :class="{ 'mine-top__checkin--done': checkinToday?.checkedIn }"
            @tap="openCheckinConfirm"
          >
            <KeepIcon name="calendar-check" :size="34" :color="checkinToday?.checkedIn ? '#9CA3AF' : '#17A857'" />
            <text v-if="checkinToday?.checkedIn" class="mine-top__checkin-text">已签到</text>
          </view>
        </view>
        <!-- <view class="mine-top__mail" @tap="goMessage">
          <KeepIcon name="mail" :size="42" />
          <text v-if="unreadCount > 0" class="mine-top__badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</text>
        </view> -->
      </view>

      <template v-if="isLoggedIn">
        <view class="mine-head" @tap="goCenter">
          <image class="mine-head__avatar" :src="avatar" mode="aspectFill" />
          <view class="mine-head__body">
            <view class="mine-head__name-row">
              <text class="mine-head__name">{{ displayName }}</text>
              <view v-if="realnameState.done" class="mine-head__verified">
                <KeepIcon name="check" :size="24" color="#fff" />
              </view>
              <text v-if="genderLabel" class="mine-head__gender">{{ genderLabel }}</text>
            </view>
            <view class="mine-head__stats">
              <!-- <text>发布 <text class="mine-head__stat-num">{{ userInfo?.pubtimes || 0 }}</text></text>
              <text>收藏 <text class="mine-head__stat-num">{{ userInfo?.coltimes || 0 }}</text></text>
              <text>约拍 <text class="mine-head__stat-num">{{ receivedCount || userInfo?.rectimes || 0 }}</text></text> -->
              <view class="pill pill--identity" @tap.stop="goEditInfo">
                <text>{{ professLabel || '未设置身份' }}</text>
              </view>
              <view class="pill" :class="{ 'pill--ok': realnameState.done }" @tap.stop="goRealname">
                <text>{{ realnameState.done ? '已实名 ✓' : realnameState.text }}</text>
              </view>
              <view class="pill" :class="{ 'pill--ok': creditState.done }" @tap.stop="handleCredit">
                <text>{{ creditState.text }}</text>
              </view>
            </view>
          </view>
          <KeepIcon name="chevron-right" :size="36" color="#B3B8BE" />
        </view>

        <!-- <view class="mine-pills">
          标签已移动至 mine-head__stats
        </view> -->

        <view class="mine-member" @tap="goMember">
          <view class="mine-member__left">
            <text class="mine-member__title">◆ 爱去拍 · 会员</text>
            <text class="mine-member__desc">{{ memberCardText }}</text>
          </view>
          <view class="mine-member__cta">{{ memberStatus?.active ? '去续费 ›' : '立即开通 ›' }}</view>
        </view>
      </template>

      <view v-else class="login-panel">
        <KeepState
          type="login"
          title="登录后查看我的约拍"
          description="登录后可查看发布、收藏、消息、钱包和认证状态。"
          button-text="去登录"
          @action="goLogin"
        />
      </view>

      <view class="mine-qgrid">
        <view class="qgrid__item" @tap="goPublish">
          <view class="qgrid__icon">
            <KeepIcon name="camera" :size="40" />
          </view>
          <text class="qgrid__label">我的发布</text>
        </view>
        <view class="qgrid__item" @tap="goApply">
          <view class="qgrid__icon">
            <KeepIcon name="check" :size="40" />
          </view>
          <text class="qgrid__label">我的约拍</text>
        </view>
        <view class="qgrid__item" @tap="goFavorite">
          <view class="qgrid__icon">
            <KeepIcon name="star" :size="40" />
          </view>
          <text class="qgrid__label">我的收藏</text>
        </view>
        <view class="qgrid__item" @tap="goWallet">
          <view class="qgrid__icon">
            <KeepIcon name="wallet" :size="40" />
          </view>
          <text class="qgrid__label">我的拍豆</text>
        </view>
        <view class="qgrid__item" @tap="goHomepage">
          <view class="qgrid__icon">
            <KeepIcon name="user" :size="40" />
          </view>
          <text class="qgrid__label">我的主页</text>
        </view>
        <view class="qgrid__item" @tap="goInvite">
          <view class="qgrid__icon">
            <KeepIcon name="users" :size="40" />
          </view>
          <text class="qgrid__label">好友邀请</text>
        </view>
        <view class="qgrid__item" @tap="goCredit">
          <view class="qgrid__icon">
            <KeepIcon name="shield" :size="40" />
          </view>
          <text class="qgrid__label">信用担保</text>
        </view>
      </view>

      <view class="mine-tabs">
        <text
          v-for="tab in tabs"
          :key="tab.key"
          class="mine-tabs__item"
          :class="{ 'mine-tabs__item--on': activeTab === tab.key }"
          @tap="activeTab = tab.key"
        >{{ tab.label }}</text>
      </view>

      <view v-if="activeTab === 'overview'" class="data-card">
        <view class="data-card__toprow">
          <view class="data-card__col">
            <view class="data-card__big">
              <KeepIcon name="chart" :size="26" color="#17A857" />
              <text>累计约拍</text>
            </view>
            <view class="data-card__num">
              <text>{{ receivedCount || userInfo?.rectimes || 0 }}</text>
              <text class="data-card__unit">次</text>
            </view>
          </view>
          <view class="data-card__coin" @tap="goWallet">
            <text>◎ 拍拍豆 {{ userInfo?.ppd || 0 }} ›</text>
          </view>
        </view>
        <view class="data-card__mini">
          <view class="mini-cell">
            <view class="mini-cell__t">
              <KeepIcon name="star" :size="24" color="#17A857" />
              <text>被收藏</text>
            </view>
            <view class="mini-cell__v">
              <text>{{ userInfo?.coltimes || 0 }}</text>
              <text class="mini-cell__unit">次</text>
            </view>
          </view>
          <view class="mini-cell">
            <view class="mini-cell__t">
              <KeepIcon name="image" :size="24" color="#17A857" />
              <text>作品</text>
            </view>
            <view class="mini-cell__v">
              <text>{{ userInfo?.pubtimes || 0 }}</text>
              <text class="mini-cell__unit">组</text>
            </view>
          </view>
        </view>
      </view>

      <view v-else-if="activeTab === 'apply'" class="data-card link-card" @tap="goApply">
        <view class="link-card__body">
          <text class="link-card__title">查看全部约拍记录</text>
          <text class="link-card__desc">收到的申请、进行中和已完成的约拍</text>
        </view>
        <KeepIcon name="chevron-right" :size="36" color="#B3B8BE" />
      </view>

      <view v-else class="data-card link-card" @tap="goPublish">
        <view class="link-card__body">
          <text class="link-card__title">查看全部作品</text>
          <text class="link-card__desc">你已发布的约拍与作品动态</text>
        </view>
        <KeepIcon name="chevron-right" :size="36" color="#B3B8BE" />
      </view>
    </view>

    <KeepTabBar active="mine" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { useMemberStore } from '@/stores/member'
import * as contentApi from '@/api/modules/content'
import * as ypatApi from '@/api/modules/ypat'
import * as checkinApi from '@/api/modules/checkin'
import { normalizeImageUrl } from '@/api/adapters'
import { GENDER_LABELS, getProfessLabel } from '@/constants/enums'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepState from '@/components/business/KeepState.vue'
import KeepTabBar from '@/components/business/KeepTabBar.vue'
import type { CheckinToday } from '@/api/types'
import type { ParamInfo } from '@/api/types/area-types'

type TabKey = 'overview' | 'apply' | 'works'

const userStore = useUserStore()
const appStore = useAppStore()
const memberStore = useMemberStore()
const params = ref<ParamInfo | null>(null)
const receivedCount = ref(0)
const activeTab = ref<TabKey>('overview')
const checkinToday = ref<CheckinToday | null>(null)
const checkinSubmitting = ref(false)

const tabs: { key: TabKey; label: string }[] = [
  { key: 'overview', label: '数据概览' },
  { key: 'apply', label: '约拍记录' },
  { key: 'works', label: '我的作品' },
]

const statusBarHeight = computed(() => appStore.statusBarHeight)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const showCheckinEntry = computed(() => isLoggedIn.value && checkinToday.value?.enabled !== false)
const userInfo = computed(() => userStore.userInfo)
const memberStatus = computed(() => memberStore.status)
const avatar = computed(() => normalizeImageUrl(userInfo.value?.imgpath || userInfo.value?.avatarurl) || '/static/default-avatar.png')
const displayName = computed(() => userInfo.value?.nickname || maskMobile(userInfo.value?.mobile) || '未设置昵称')
const genderLabel = computed(() => {
  const gender = userInfo.value?.gender
  return gender ? GENDER_LABELS[gender] || '' : ''
})
const professLabel = computed(() => {
  const code = userInfo.value?.profess
  return getProfessLabel(code)
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
    ? { text: '信用担保 ✓', done: true }
    : { text: '未信用担保', done: false }
))
const memberCardText = computed(() => (
  memberStatus.value?.active
    ? `有效期至 ${formatDate(memberStatus.value.expireAt)}`
    : '提交约拍可省拍拍豆'
))

function maskMobile(value?: string): string {
  if (!value) return ''
  return value.length >= 11 ? `${value.slice(0, 3)}****${value.slice(7)}` : value
}

function formatDate(value?: string): string {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 10)
}

function requireLogin(): boolean {
  if (isLoggedIn.value) return true
  uni.navigateTo({ url: '/pages/login/index' })
  return false
}

function handleCredit(): void {
  if (!requireLogin()) return
  if (creditState.value.done) {
    uni.showToast({ title: '已信用担保', icon: 'none' })
    return
  }
  goCredit()
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
  if (!isLoggedIn.value || !userInfo.value?.id) {
    await loadCheckinToday()
    return
  }
  await Promise.all([
    userStore.updateUserInfo(),
    userStore.refreshUnreadCount(),
    memberStore.refreshStatus(),
    loadReceivedCount(),
    loadCheckinToday(),
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

async function loadCheckinToday(): Promise<void> {
  if (!isLoggedIn.value) {
    checkinToday.value = null
    return
  }
  try {
    const result = await checkinApi.getCheckinToday()
    checkinToday.value = result.data || null
  } catch {
    checkinToday.value = null
  }
}

function openCheckinConfirm(): void {
  if (!requireLogin()) return
  if (checkinSubmitting.value) return
  if (!checkinToday.value?.enabled) return
  if (checkinToday.value.checkedIn) {
    uni.showToast({ title: '今日已签到', icon: 'none' })
    return
  }
  uni.showModal({
    title: checkinToday.value.confirmTitle || '每日签到',
    content: checkinToday.value.confirmContent || `签到成功可获得 ${checkinToday.value.rewardPpd || 1} 拍豆`,
    confirmText: '签到',
    success: (res) => {
      if (res.confirm) void submitCheckin()
    },
  })
}

async function submitCheckin(): Promise<void> {
  if (checkinSubmitting.value) return
  checkinSubmitting.value = true
  try {
    const result = await checkinApi.doCheckin()
    if (result.data?.checkedIn) {
      checkinToday.value = {
        ...(checkinToday.value || {
          enabled: true,
          rewardPpd: result.data.rewardPpd || 1,
          confirmTitle: '每日签到',
          confirmContent: '签到成功可获得 1 拍豆',
          checkinDate: '',
        }),
        checkedIn: true,
      }
      await userStore.updateUserInfo()
      const reward = Number(result.data.rewardPpd || 0)
      uni.showToast({ title: reward > 0 ? `签到成功，获得 ${reward} 拍豆` : '今日已签到', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '签到失败，请稍后重试', icon: 'none' })
  } finally {
    checkinSubmitting.value = false
  }
}

function goLogin(): void { uni.navigateTo({ url: '/pages/login/index' }) }
function goCenter(): void {
  if (!requireLogin()) return
  uni.navigateTo({ url: '/pages-sub/user/center' })
}
function goEditInfo(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/user/edit-info' }) }
function goRealname(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/user/realname-intro' }) }
function goMember(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/user/member/index' }) }
function goHomepage(): void {
  if (!requireLogin()) return
  const id = userInfo.value?.id
  uni.navigateTo({ url: id ? `/pages-sub/user/profile?id=${id}` : '/pages-sub/user/profile' })
}
function goPublish(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/ypat/my-publish' }) }
function goApply(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/ypat/my-apply' }) }
function goFavorite(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/ypat/my-favorite' }) }
function goWallet(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/user/wallet' }) }
function goInvite(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/user/invite' }) }
function goCredit(): void { if (requireLogin()) uni.navigateTo({ url: '/pages-sub/user/credit' }) }

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
  padding: 0 32rpx calc(172rpx + env(safe-area-inset-bottom));
}

.mine-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mine-top__left {
  display: flex;
  align-items: center;
  gap: 18rpx;
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

.mine-top__checkin {
  @include flex-center;
  gap: 8rpx;
  min-width: 76rpx;
  height: 76rpx;
  padding: 0 20rpx;
  border-radius: $radius-round;
  color: $color-primary;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.mine-top__checkin--done {
  color: $color-text-tertiary;
  background: #f4f5f6;
}

.mine-top__checkin-text {
  color: $color-text-tertiary;
  font-size: 24rpx;
  font-weight: 800;
  white-space: nowrap;
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

.mine-head {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-top: 28rpx;
  padding: 20rpx 8rpx;
}

.mine-head__avatar {
  width: 128rpx;
  height: 128rpx;
  flex: none;
  border-radius: 50%;
  background: $color-bg-chip;
}

.mine-head__body {
  min-width: 0;
  flex: 1;
}

.mine-head__name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.mine-head__name {
  max-width: 320rpx;
  overflow: hidden;
  color: $color-text-primary;
  font-size: 42rpx;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mine-head__verified {
  @include flex-center;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: $color-primary;
}

.mine-head__gender {
  flex: none;
  padding: 4rpx 14rpx;
  border-radius: $radius-round;
  color: $color-text-secondary;
  background: $color-bg-chip;
  font-size: 22rpx;
  font-weight: 800;
}

.mine-head__stats {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 14rpx;
  color: $color-text-secondary;
  font-size: 26rpx;
  font-weight: 700;
}

.mine-head__stat-num {
  margin: 0 4rpx;
  color: $color-text-primary;
  font-weight: 900;
}

.mine-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 8rpx;
  padding: 0 8rpx;
}

.pill {
  padding: 10rpx 22rpx;
  border-radius: $radius-round;
  color: $color-text-primary;
  background: #fff;
  box-shadow: $shadow-keep-card;
  font-size: 24rpx;
  font-weight: 800;
}

.pill--identity {
  padding: 6rpx 18rpx;
  color: $color-primary-dark;
  background: $color-primary-light;
  box-shadow: none;
  font-size: 22rpx;
}

.pill--ok {
  color: $color-primary-dark;
  background: $color-primary-light;
  box-shadow: none;
}

.mine-member {
  display: flex;
  align-items: center;
  margin-top: 28rpx;
  padding: 32rpx 30rpx;
  border-radius: $radius-keep-card;
  background: linear-gradient(110deg, #2A2D3A, #3C3550);
  color: #fff;
  overflow: hidden;
}

.mine-member__left {
  flex: 1;
  min-width: 0;
}

.mine-member__title {
  display: block;
  font-size: 30rpx;
  font-weight: 900;
}

.mine-member__desc {
  display: block;
  margin-top: 10rpx;
  opacity: 0.75;
  font-size: 24rpx;
  font-weight: 700;
}

.mine-member__cta {
  flex: none;
  padding: 16rpx 28rpx;
  border-radius: $radius-round;
  color: #5A3A00;
  background: linear-gradient(135deg, #FFE08A, #F5B642);
  font-size: 26rpx;
  font-weight: 900;
}

.login-panel {
  margin-top: 34rpx;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.mine-qgrid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  row-gap: 28rpx;
  margin-top: 28rpx;
  padding: 32rpx 0 30rpx;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.qgrid__item {
  @include flex-column;
  align-items: center;
  gap: 12rpx;
  min-width: 0;
  color: $color-text-primary;
}

.qgrid__icon {
  @include flex-center;
  width: 76rpx;
  height: 76rpx;
  border-radius: 24rpx;
  background: $color-bg-chip;
}

.qgrid__label {
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 800;
}

.mine-tabs {
  display: flex;
  align-items: baseline;
  gap: 44rpx;
  margin-top: 40rpx;
  padding: 0 12rpx;
}

.mine-tabs__item {
  position: relative;
  color: $color-text-helper;
  font-size: 28rpx;
  font-weight: 800;
}

.mine-tabs__item--on {
  color: $color-text-primary;
  font-size: 34rpx;
  font-weight: 900;
}

.mine-tabs__item--on::after {
  content: '';
  position: absolute;
  right: 0;
  bottom: -14rpx;
  left: 0;
  height: 6rpx;
  border-radius: 4rpx;
  background: $color-text-primary;
}

.data-card {
  margin-top: 28rpx;
  padding: 32rpx;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.data-card__toprow {
  display: flex;
  align-items: flex-start;
}

.data-card__col {
  flex: 1;
  min-width: 0;
}

.data-card__big {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: $color-primary-dark;
  font-size: 26rpx;
  font-weight: 800;
}

.data-card__num {
  display: flex;
  align-items: baseline;
  margin-top: 10rpx;
  color: $color-text-primary;
  font-size: 60rpx;
  font-weight: 900;
  line-height: 1.1;
}

.data-card__unit {
  margin-left: 6rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 700;
}

.data-card__coin {
  padding: 14rpx 24rpx;
  border-radius: $radius-round;
  color: #B26B00;
  background: #FFF3E0;
  font-size: 24rpx;
  font-weight: 900;
}

.data-card__mini {
  display: flex;
  gap: 18rpx;
  margin-top: 26rpx;
}

.mini-cell {
  flex: 1;
  padding: 22rpx 24rpx;
  border-radius: 24rpx;
  background: $color-bg-chip;
}

.mini-cell__t {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: $color-primary-dark;
  font-size: 24rpx;
  font-weight: 800;
}

.mini-cell__v {
  display: flex;
  align-items: baseline;
  margin-top: 10rpx;
  color: $color-text-primary;
  font-size: 36rpx;
  font-weight: 900;
}

.mini-cell__unit {
  margin-left: 6rpx;
  color: $color-text-secondary;
  font-size: 22rpx;
  font-weight: 700;
}

.link-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.link-card__body {
  flex: 1;
  min-width: 0;
}

.link-card__title {
  display: block;
  color: $color-text-primary;
  font-size: 30rpx;
  font-weight: 900;
}

.link-card__desc {
  display: block;
  margin-top: 8rpx;
  color: $color-text-helper;
  font-size: 24rpx;
  font-weight: 700;
}
</style>
