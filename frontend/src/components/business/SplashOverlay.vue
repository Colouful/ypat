<template>
  <view v-if="visible" class="splash-overlay">
    <view class="splash-overlay__skip" @tap="close">
      <text>跳过 {{ countdown }}s</text>
    </view>
    <view class="splash-overlay__center">
      <view class="splash-overlay__mark">
        <text>拍</text>
      </view>
      <text class="splash-overlay__brand">爱去拍</text>
      <text class="splash-overlay__slogan">安全约拍 · 灵感成片 · 真实认证</text>
      <view class="splash-overlay__chips">
        <text>优质作品</text>
        <text>实名保障</text>
        <text>同城约拍</text>
      </view>
    </view>
    <view class="splash-overlay__bottom">
      <text>{{ countdown }} 秒后进入</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

const STORAGE_KEY = 'ypat_splash_last_show_date'
const visible = ref(false)
const countdown = ref(3)
let timer: ReturnType<typeof setInterval> | null = null

function todayKey(): string {
  const date = new Date()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

function close(): void {
  visible.value = false
  uni.setStorageSync(STORAGE_KEY, todayKey())
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function start(): void {
  countdown.value = 3
  visible.value = true
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) close()
  }, 1000)
}

onMounted(() => {
  if (uni.getStorageSync(STORAGE_KEY) === todayKey()) return
  start()
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped lang="scss">
.splash-overlay {
  position: fixed;
  z-index: 9999;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background:
    radial-gradient(circle at 22% 18%, rgba(255, 255, 255, 0.34), transparent 28%),
    linear-gradient(145deg, #1A1D1F 0%, #174734 46%, #23C268 100%);
}

.splash-overlay__skip {
  position: absolute;
  top: calc(54rpx + env(safe-area-inset-top));
  right: 34rpx;
  padding: 14rpx 24rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.28);
  border-radius: $radius-round;
  color: #FFFFFF;
  background: rgba(0, 0, 0, 0.22);
  font-size: 24rpx;
  font-weight: 800;
}

.splash-overlay__center {
  display: flex;
  align-items: center;
  flex-direction: column;
  padding: 0 54rpx;
  text-align: center;
}

.splash-overlay__mark {
  @include flex-center;
  width: 156rpx;
  height: 156rpx;
  border: 6rpx solid rgba(255, 255, 255, 0.7);
  border-radius: 48rpx;
  color: #1A1D1F;
  background: #FFFFFF;
  box-shadow: 0 30rpx 80rpx rgba(0, 0, 0, 0.22);
  font-size: 74rpx;
  font-weight: 900;
}

.splash-overlay__brand {
  margin-top: 34rpx;
  color: #FFFFFF;
  font-size: 58rpx;
  font-weight: 900;
}

.splash-overlay__slogan {
  margin-top: 18rpx;
  color: rgba(255, 255, 255, 0.84);
  font-size: 28rpx;
  font-weight: 700;
}

.splash-overlay__chips {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 14rpx;
  margin-top: 38rpx;
}

.splash-overlay__chips text {
  padding: 12rpx 20rpx;
  border-radius: $radius-round;
  color: #FFFFFF;
  background: rgba(255, 255, 255, 0.18);
  font-size: 23rpx;
  font-weight: 800;
}

.splash-overlay__bottom {
  position: absolute;
  right: 0;
  bottom: calc(58rpx + env(safe-area-inset-bottom));
  left: 0;
  color: rgba(255, 255, 255, 0.72);
  font-size: 24rpx;
  font-weight: 700;
  text-align: center;
}
</style>
