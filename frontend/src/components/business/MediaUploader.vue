<template>
  <view class="media-uploader">
    <!-- 2 个独立按钮：上传照片 + 上传视频（图片和视频互斥） -->
    <view v-if="!hasAny" class="media-uploader__buttons">
      <view class="media-uploader__btn" @tap="onChooseImage">
        <KeepIcon name="image" :size="48" color="#83888F" />
        <text class="media-uploader__btn-text">上传照片</text>
      </view>
      <view class="media-uploader__btn" @tap="onChooseVideo">
        <KeepIcon name="video" :size="48" color="#83888F" />
        <text class="media-uploader__btn-text">上传视频</text>
      </view>
    </view>

    <!-- 已选图片网格 -->
    <view v-else-if="hasImage" class="media-uploader__grid">
      <view v-for="(item, idx) in modelValue" :key="idx" class="media-uploader__item">
        <image class="media-uploader__image" :src="item.localPath" mode="aspectFill" />
        <view v-if="item.uploadStatus === 'uploading'" class="media-uploader__mask">
          <text class="media-uploader__progress">{{ item.progress }}%</text>
        </view>
        <view v-else-if="item.uploadStatus === 'failed'" class="media-uploader__mask media-uploader__mask--failed" @tap="retryUpload(idx)">
          <text class="media-uploader__retry">重试</text>
        </view>
        <view v-else-if="item.uploadStatus === 'success'" class="media-uploader__success-icon">
          <text>✓</text>
        </view>
        <view class="media-uploader__remove" @tap="removeImage(idx)">
          <text class="media-uploader__remove-x">×</text>
        </view>
      </view>
      <view v-if="modelValue.length < maxImageCount" class="media-uploader__add" @tap="onChooseImage">
        <text class="media-uploader__add-plus">+</text>
      </view>
    </view>

    <!-- 已选视频 -->
    <view v-else-if="hasVideo" class="media-uploader__video">
      <video class="media-uploader__video-player" :src="modelValue[0].localPath" :poster="modelValue[0].thumb" controls />
      <view v-if="modelValue[0].uploadStatus === 'uploading'" class="media-uploader__mask">
        <text class="media-uploader__progress">{{ modelValue[0].progress }}%</text>
      </view>
      <view v-else-if="modelValue[0].uploadStatus === 'failed'" class="media-uploader__mask media-uploader__mask--failed" @tap="retryUploadVideo">
        <text class="media-uploader__retry">重试</text>
      </view>
      <view class="media-uploader__remove" @tap="removeVideo">
        <text class="media-uploader__remove-x">×</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import KeepIcon from './KeepIcon.vue'
import type { MediaItem } from '@/api/types/media'
import {
  chooseImages, chooseVideo,
  uploadImageWithRetry, uploadVideoWithRetry,
  MAX_IMAGE_COUNT, MAX_VIDEO_COUNT, MAX_IMAGE_TOTAL_SIZE, MAX_VIDEO_SIZE,
  checkImageTotalSize,
} from '@/utils/media-uploader'

const props = withDefaults(defineProps<{
  modelValue: MediaItem[]
  placeholder?: string
}>(), {
  placeholder: '',
})

const emit = defineEmits<{
  (e: 'update:modelValue', v: MediaItem[]): void
  (e: 'change', v: MediaItem[]): void
}>()

const hasAny = computed(() => props.modelValue.length > 0)
const hasImage = computed(() => props.modelValue.length > 0 && props.modelValue[0].type === 'IMAGE')
const hasVideo = computed(() => props.modelValue.length > 0 && props.modelValue[0].type === 'VIDEO')
const maxImageCount = MAX_IMAGE_COUNT

async function onChooseImage() {
  // 已有视频 → 禁止选图
  if (hasVideo.value) {
    uni.showToast({ title: '已选择视频，请先删除视频', icon: 'none' })
    return
  }
  const remain = MAX_IMAGE_COUNT - props.modelValue.length
  if (remain <= 0) {
    uni.showToast({ title: `最多只能上传 ${MAX_IMAGE_COUNT} 张图片`, icon: 'none' })
    return
  }
  try {
    const files = await chooseImages({ count: remain })
    const candidates: MediaItem[] = files.map((f: any) => ({
      localPath: (f.path || f.tempFilePath) as string,
      type: 'IMAGE' as const,
      size: typeof f.size === 'number' ? f.size : 0,
      uploadStatus: 'pending' as const,
      progress: 0,
    }))
    const merged = [...props.modelValue, ...candidates]
    const check = checkImageTotalSize(merged)
    if (!check.ok) {
      uni.showToast({ title: check.message || '图片总大小不能超过 100MB', icon: 'none' })
      return
    }
    emit('update:modelValue', merged)
    emit('change', merged)
    for (let i = 0; i < candidates.length; i++) {
      await uploadOne(candidates[i], false, props.modelValue.length + i)
    }
  } catch (e) {
    // 用户取消
  }
}

async function onChooseVideo() {
  // 已有图片 → 禁止选视频
  if (hasImage.value) {
    uni.showModal({
      title: '提示',
      content: '已选择图片，请先删除全部图片后再选择视频',
      showCancel: false,
    })
    return
  }
  try {
    const res = await chooseVideo()
    if (res.size > MAX_VIDEO_SIZE) {
      uni.showToast({ title: '视频大小不能超过 200MB', icon: 'none' })
      return
    }
    const item: MediaItem = {
      localPath: res.tempFilePath,
      type: 'VIDEO',
      size: res.size || 0,
      uploadStatus: 'uploading',
      progress: 0,
      thumb: res.tempFilePath,
      duration: res.duration,
    }
    emit('update:modelValue', [item])
    emit('change', [item])
    await uploadOne(item, true)
  } catch (e) {
    uni.showToast({ title: '选择视频失败', icon: 'none' })
  }
}

async function uploadOne(item: MediaItem, isVideo: boolean, idx?: number) {
  const updateItem = (next: MediaItem) => {
    if (isVideo) {
      emit('update:modelValue', [next])
      emit('change', [next])
    } else {
      const arr = [...props.modelValue]
      const target = idx !== undefined ? idx : arr.findIndex((x) => x.localPath === item.localPath)
      if (target >= 0) arr[target] = next
      emit('update:modelValue', arr)
      emit('change', arr)
    }
  }
  const updated = { ...item, uploadStatus: 'uploading' as const, progress: 0 }
  updateItem(updated)
  const result = isVideo
    ? await uploadVideoWithRetry(updated, (e) => {
        updateItem({ ...updated, progress: Math.round((e.progress || 0) * 100) })
      })
    : await uploadImageWithRetry(updated, (e) => {
        updateItem({ ...updated, progress: Math.round((e.progress || 0) * 100) })
      })
  updateItem(result)
}

async function retryUpload(idx: number) {
  const item = props.modelValue[idx]
  if (!item) return
  await uploadOne(item, false, idx)
}

async function retryUploadVideo() {
  if (props.modelValue[0]) {
    await uploadOne(props.modelValue[0], true)
  }
}

function removeImage(idx: number) {
  const arr = [...props.modelValue]
  arr.splice(idx, 1)
  emit('update:modelValue', arr)
  emit('change', arr)
}

function removeVideo() {
  emit('update:modelValue', [])
  emit('change', [])
}
</script>

<style lang="scss" scoped>
.media-uploader {
  width: 100%;
  &__buttons {
    display: flex;
    gap: 16rpx;
  }
  &__btn {
    flex: 1;
    height: 192rpx;
    background: $color-bg-page;
    border-radius: 16rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
  }
  &__btn-icon {
    font-size: 56rpx;
    color: $color-text-helper;
  }
  &__btn-text {
    font-size: 24rpx;
    color: $color-text-secondary;
  }
  &__grid {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 16rpx;
  }
  &__item, &__add {
    position: relative;
    width: 100%;
    padding-top: 100%;
    background: $color-bg-page;
    border-radius: 16rpx;
    overflow: hidden;
  }
  &__add {
    border: 2rpx dashed $color-border;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  &__add-plus {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 56rpx;
    color: $color-text-helper;
  }
  &__image {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
  }
  &__mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    &--failed { background: rgba(0, 0, 0, 0.6); }
  }
  &__progress, &__retry {
    color: #FFFFFF;
    font-size: 24rpx;
  }
  &__success-icon {
    position: absolute;
    top: 12rpx;
    left: 12rpx;
    width: 36rpx;
    height: 36rpx;
    border-radius: 18rpx;
    background: $color-primary;
    color: #FFFFFF;
    font-size: 22rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  &__remove {
    position: absolute;
    top: 8rpx;
    right: 8rpx;
    width: 36rpx;
    height: 36rpx;
    border-radius: 18rpx;
    background: rgba(0, 0, 0, 0.5);
    color: #FFFFFF;
    font-size: 28rpx;
    line-height: 36rpx;
    text-align: center;
  }
  &__remove-x { color: #FFFFFF; line-height: 1; }
  &__video {
    position: relative;
    width: 100%;
    padding-top: 56.25%;
    background: #000;
    border-radius: 16rpx;
    overflow: hidden;
  }
  &__video-player {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
  }
}
</style>
