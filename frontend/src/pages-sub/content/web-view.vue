<template>
  <view class="web-view-page">
    <KeepPageNav title="外部链接" />

    <web-view
      v-if="targetUrl"
      class="web-view-page__frame"
      :src="targetUrl"
      @load="handleLoad"
      @error="handleError"
    />

    <view v-if="showFallback" class="web-view-page__fallback">
      <view class="web-view-page__panel">
        <text class="web-view-page__title">{{ fallbackTitle }}</text>
        <text class="web-view-page__message">{{ fallbackMessage }}</text>
        <text v-if="fallbackUrl" class="web-view-page__url">{{ fallbackUrl }}</text>
        <button v-if="fallbackUrl" class="web-view-page__button" @tap="copyFallbackUrl">复制链接</button>
      </view>
    </view>

    <view v-else-if="loading" class="web-view-page__loading">
      <view class="web-view-page__spinner" />
      <text class="web-view-page__loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { copyUrl } from '@/utils/banner-link'

const targetUrl = ref('')
const fallbackUrl = ref('')
const loading = ref(true)
const errorMessage = ref('')

const showFallback = computed(() => !loading.value && (!targetUrl.value || Boolean(errorMessage.value)))
const fallbackTitle = computed(() => (targetUrl.value ? '页面加载失败' : '无法打开链接'))
const fallbackMessage = computed(() => errorMessage.value || '链接缺失或格式不受支持')

function isAllowedUrl(url: string): boolean {
  return /^https?:\/\//i.test(url)
}

function decodeUrl(value: string): string {
  try {
    return decodeURIComponent(value)
  } catch {
    return value
  }
}

function handleLoad(): void {
  loading.value = false
  errorMessage.value = ''
}

function handleError(): void {
  loading.value = false
  errorMessage.value = '当前页面暂时无法打开，请复制链接后在浏览器中访问'
}

function copyFallbackUrl(): void {
  if (!fallbackUrl.value) return
  copyUrl(fallbackUrl.value)
}

onLoad((query) => {
  const rawUrl = typeof query?.url === 'string' ? query.url : ''
  const decodedUrl = rawUrl ? decodeUrl(rawUrl).trim() : ''
  fallbackUrl.value = decodedUrl

  if (!decodedUrl) {
    loading.value = false
    errorMessage.value = '未提供可打开的链接'
    return
  }

  if (!isAllowedUrl(decodedUrl)) {
    loading.value = false
    errorMessage.value = '仅支持 http:// 或 https:// 开头的链接'
    return
  }

  targetUrl.value = decodedUrl
})
</script>

<style scoped lang="scss">
.web-view-page {
  position: relative;
  min-height: 100vh;
  background: $color-bg-page;
}

.web-view-page__frame {
  width: 100%;
  height: 100vh;
}

.web-view-page__loading,
.web-view-page__fallback {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 160rpx 40rpx 48rpx;
  background: $color-bg-page;
}

.web-view-page__loading {
  z-index: 2;
  flex-direction: column;
}

.web-view-page__fallback {
  z-index: 3;
}

.web-view-page__spinner {
  width: 56rpx;
  height: 56rpx;
  border: 4rpx solid $color-border;
  border-top-color: $color-primary;
  border-radius: 50%;
  animation: web-view-spin 0.8s linear infinite;
}

.web-view-page__loading-text {
  margin-top: 20rpx;
  color: $color-text-secondary;
  font-size: 26rpx;
}

.web-view-page__panel {
  width: 100%;
  padding: 44rpx 36rpx;
  border-radius: $radius-keep-card;
  background: $color-bg-card;
  box-shadow: $shadow-keep-card;
}

.web-view-page__title {
  display: block;
  color: $color-text-primary;
  font-size: 36rpx;
  font-weight: 800;
  text-align: center;
}

.web-view-page__message {
  display: block;
  margin-top: 18rpx;
  color: $color-text-secondary;
  font-size: 27rpx;
  line-height: 1.6;
  text-align: center;
}

.web-view-page__url {
  display: block;
  margin-top: 28rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  color: $color-text-secondary;
  background: $color-bg-chip;
  font-size: 24rpx;
  line-height: 1.5;
  overflow-wrap: break-word;
  word-break: break-all;
}

.web-view-page__button {
  margin-top: 32rpx;
  border-radius: $radius-round;
  color: #FFFFFF;
  background: $color-primary;
  font-size: 28rpx;
  font-weight: 700;
}

@keyframes web-view-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
