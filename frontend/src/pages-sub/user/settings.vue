<template>
  <view class="settings-page">
    <view class="settings-section">
      <view class="section-title">账号</view>
      <view class="settings-card">
        <view class="settings-item" @tap="handlePhone">
          <text class="settings-item__label">手机号</text>
          <view class="settings-item__right"><text class="settings-item__value">{{ maskedPhone }}</text><KeepIcon name="chevron-right" :size="32" color="#B3B8BE" /></view>
        </view>
        <view class="settings-item" @tap="goRealname">
          <text class="settings-item__label">实名认证</text>
          <view class="settings-item__right"><text class="settings-item__value" :class="{ 'settings-item__value--verified': isVerified }">{{ verifyStatus }}</text><KeepIcon name="chevron-right" :size="32" color="#B3B8BE" /></view>
        </view>
      </view>
    </view>

    <view class="settings-section">
      <view class="section-title">通用</view>
      <view class="settings-card">
        <view class="settings-item" @tap="handleClearCache"><text class="settings-item__label">清除缓存</text><view class="settings-item__right"><text class="settings-item__value">{{ cacheSize }}</text><KeepIcon name="chevron-right" :size="32" color="#B3B8BE" /></view></view>
        <view class="settings-item" @tap="goAbout"><text class="settings-item__label">关于我们</text><KeepIcon name="chevron-right" :size="32" color="#B3B8BE" /></view>
        <view class="settings-item" @tap="goFeedback"><text class="settings-item__label">意见反馈</text><KeepIcon name="chevron-right" :size="32" color="#B3B8BE" /></view>
      </view>
    </view>

    <view class="logout-section"><view class="logout-btn" @tap="handleLogout"><text class="logout-btn__text">退出登录</text></view></view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import KeepIcon from '@/components/business/KeepIcon.vue'

const userStore = useUserStore()
const cacheSize = ref('0 KB')
const maskedPhone = computed(() => {
  const mobile = userStore.userInfo?.mobile || ''
  return mobile.length >= 11 ? `${mobile.slice(0, 3)}****${mobile.slice(7)}` : mobile || '未绑定'
})
const isVerified = computed(() => userStore.userInfo?.realnameflag === '1')
const verifyStatus = computed(() => isVerified.value ? '已认证' : '未认证')

function getStorageSize(): void {
  uni.getStorageInfo({
    success: ({ currentSize }) => {
      cacheSize.value = currentSize >= 1024 ? `${(currentSize / 1024).toFixed(2)} MB` : `${currentSize} KB`
    },
  })
}

function handlePhone(): void { uni.showToast({ title: '暂不支持修改手机号', icon: 'none' }) }
function goRealname(): void { uni.navigateTo({ url: '/pages-sub/user/realname' }) }
function goAbout(): void { uni.navigateTo({ url: '/pages-sub/user/about' }) }
function goFeedback(): void { uni.navigateTo({ url: '/pages-sub/user/feedback' }) }

function handleClearCache(): void {
  uni.showModal({
    title: '提示',
    content: '确定要清除缓存吗？',
    success: ({ confirm }) => {
      if (!confirm) return
      const token = uni.getStorageSync('ypat_token')
      const userInfo = uni.getStorageSync('ypat_user_info')
      uni.clearStorageSync()
      if (token) uni.setStorageSync('ypat_token', token)
      if (userInfo) uni.setStorageSync('ypat_user_info', userInfo)
      getStorageSize()
      uni.showToast({ title: '缓存已清除', icon: 'success' })
    },
  })
}

function handleLogout(): void {
  uni.showModal({ title: '提示', content: '确定要退出登录吗？', success: ({ confirm }) => confirm && userStore.logout() })
}

onShow(getStorageSize)
</script>

<style scoped lang="scss">

.settings-page { min-height: 100vh; padding: 28rpx; background: $color-bg-page; }
.settings-section { margin-bottom: 28rpx; }
.section-title { margin-bottom: 14rpx; color: $color-text-secondary; font-size: 25rpx; }
.settings-card { overflow: hidden; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; }
.settings-item { display: flex; align-items: center; justify-content: space-between; padding: 28rpx; border-bottom: 1rpx solid $color-border; }
.settings-item__right { display: flex; align-items: center; gap: 12rpx; }
.settings-item__value, .settings-item__arrow { color: $color-text-secondary; }
.settings-item__value--verified { color: $color-primary; }
.logout-section { margin-top: 48rpx; }
.logout-btn { padding: 26rpx; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; text-align: center; }
.logout-btn__text { color: #e5484d; }
</style>
