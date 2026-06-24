<template>
  <view class="detail">
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="errorMessage" class="state error">
      <text>{{ errorMessage }}</text>
      <button @tap="load">重新加载</button>
    </view>
    <template v-else-if="detail">
      <swiper class="swiper" circular :indicator-dots="images.length > 1">
        <swiper-item v-for="(image, index) in images" :key="image">
          <image :src="image" mode="aspectFill" @tap="preview(index)" />
        </swiper-item>
      </swiper>
      <view class="card publisher">
        <image class="avatar" :src="detail.userQo?.imgpath || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="publisher-info">
          <view class="name-row">
            <text class="name">{{ detail.userQo?.nickname || '匿名用户' }}</text>
            <text v-if="detail.userQo?.realnameflag === '1'" class="badge">已认证</text>
          </view>
          <text class="secondary">{{ detail.userQo?.profess || '摄影爱好者' }}</text>
        </view>
      </view>
      <view class="card">
        <text class="heading">{{ detail.targetTxt || '约拍详情' }}</text>
        <text class="description">{{ detail.describ }}</text>
        <view class="row"><text>拍摄日期</text><text>{{ detail.patdate }}</text></view>
        <view class="row"><text>拍摄地点</text><text>{{ detail.city }} {{ detail.area || '' }}</text></view>
        <view class="row"><text>收费方式</text><text>{{ detail.chargewayTxt || detail.chargeway }}</text></view>
        <view v-if="Number(detail.chargeamt || 0) > 0" class="row"><text>参考金额</text><text>¥{{ detail.chargeamt }}</text></view>
        <view class="row"><text>浏览量</text><text>{{ detail.readtimes || 0 }}</text></view>
      </view>
      <view class="actions">
        <button class="favorite" :disabled="favorited || actionLoading" @tap="favorite">{{ favorited ? '已收藏' : '收藏' }}</button>
        <button class="apply" :disabled="actionLoading" :loading="actionLoading" @tap="apply">我要报名</button>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import * as ypatApi from '@/api/modules/ypat'
import { put } from '@/api/request'
import type { YpatInfo } from '@/api/types'

const props = defineProps<{ id: number }>()
const userStore = useUserStore()
const detail = ref<YpatInfo | null>(null)
const loading = ref(false)
const actionLoading = ref(false)
const errorMessage = ref('')
const favorited = ref(false)
const images = computed(() => detail.value?.pics?.filter(Boolean) || [])

async function load(): Promise<void> {
  if (!props.id) {
    errorMessage.value = '缺少约拍 ID'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await ypatApi.getDetail(props.id)
    detail.value = result.data
    favorited.value = result.data?.colflag === '1'
    put('/ypat/yd/add', { ypatid: props.id }, { showError: false }).catch(() => undefined)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '获取详情失败'
  } finally {
    loading.value = false
  }
}

function preview(index: number): void {
  uni.previewImage({ current: index, urls: images.value })
}

function requireLogin(): boolean {
  if (userStore.isLoggedIn) return true
  uni.navigateTo({ url: '/pages/login/index' })
  return false
}

async function favorite(): Promise<void> {
  if (!requireLogin() || !detail.value || favorited.value || actionLoading.value) return
  actionLoading.value = true
  try {
    await ypatApi.addFavorite(userStore.userInfo!.id, detail.value.id)
    favorited.value = true
    uni.showToast({ title: '收藏成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '收藏失败', icon: 'none' })
  } finally {
    actionLoading.value = false
  }
}

async function apply(): Promise<void> {
  if (!requireLogin() || !detail.value || actionLoading.value) return
  if (detail.value.userid === userStore.userInfo!.id) {
    uni.showToast({ title: '不能报名自己发布的约拍', icon: 'none' })
    return
  }
  actionLoading.value = true
  try {
    await ypatApi.applyYpat({
      sendperid: userStore.userInfo!.id,
      recperid: detail.value.userid,
      ypatid: detail.value.id,
      content: '我想报名参加您的约拍',
    })
    uni.showToast({ title: '报名成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '报名失败', icon: 'none' })
  } finally {
    actionLoading.value = false
  }
}

watch(() => props.id, load, { immediate: true })
</script>

<style scoped lang="scss">
.detail { min-height: 100vh; padding-bottom: 150rpx; background: #f7f8fa; }
.state { padding: 220rpx 40rpx; color: #7c8593; text-align: center; }
.state button { margin-top: 24rpx; }
.error { color: #b4232c; }
.swiper { height: 620rpx; background: #e9edf2; }
.swiper image { width: 100%; height: 100%; }
.card { margin: 24rpx; padding: 28rpx; border-radius: 24rpx; background: #fff; }
.publisher { display: flex; align-items: center; }
.avatar { width: 96rpx; height: 96rpx; border-radius: 50%; }
.publisher-info { margin-left: 20rpx; }
.name-row { display: flex; align-items: center; gap: 12rpx; }
.name, .heading { color: #1d2433; font-size: 32rpx; font-weight: 600; }
.badge { padding: 4rpx 12rpx; border-radius: 12rpx; color: #168849; background: #e8f8ef; font-size: 22rpx; }
.secondary { color: #7c8593; font-size: 25rpx; }
.description { display: block; margin: 22rpx 0; color: #414958; font-size: 29rpx; line-height: 1.75; }
.row { display: flex; justify-content: space-between; padding: 20rpx 0; border-top: 1rpx solid #edf0f4; color: #5f6876; font-size: 27rpx; }
.actions { position: fixed; left: 0; right: 0; bottom: 0; display: flex; gap: 18rpx; padding: 20rpx 28rpx calc(20rpx + env(safe-area-inset-bottom)); background: #fff; box-shadow: 0 -8rpx 24rpx rgba(30,40,50,.08); }
.actions button { flex: 1; border-radius: 44rpx; }
.actions button::after { border: 0; }
.favorite { color: #23c268; background: #eaf8f0; }
.apply { color: #fff; background: #23c268; }
</style>
