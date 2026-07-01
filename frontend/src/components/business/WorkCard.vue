<template>
  <view class="work-card" @tap="onTap">
    <view class="work-card__cover-wrap">
      <image v-if="work.coverUrl" class="work-card__cover" :src="work.coverUrl" mode="widthFix" lazy-load @load="onCoverLoad" />
      <view v-else class="work-card__cover-placeholder" />
      <view v-if="work.isVideo === '1'" class="work-card__video-badge">
        <text class="work-card__video-text">视频</text>
      </view>
    </view>
    <text v-if="displayTitle" class="work-card__title">{{ displayTitle }}</text>
    <view v-if="work.tags && work.tags.length" class="work-card__tags">
      <text v-for="t in work.tags.slice(0, 3)" :key="t" class="work-card__tag">{{ t }}</text>
    </view>
    <view class="work-card__footer">
      <image v-if="work.avatar" class="work-card__avatar" :src="work.avatar" mode="aspectFill" />
      <view v-else class="work-card__avatar-placeholder" />
      <view class="work-card__meta">
        <view class="work-card__name-row">
          <text class="work-card__name">{{ work.nickname || '匿名用户' }}</text>
          <text class="work-card__gender">{{ work.gender === '1' ? '♂' : work.gender === '2' ? '♀' : '' }}</text>
        </view>
        <text v-if="work.profession || work.city" class="work-card__sub">
          <text v-if="work.profession">{{ professionLabel(work.profession) }}</text>
          <text v-if="work.profession && work.city"> | </text>
          <text v-if="work.city">{{ work.city }}</text>
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepIcon from './KeepIcon.vue'
import type { WorkListItem } from '@/api/types/work'

const props = defineProps<{
  work: WorkListItem
  coverHeight?: number
}>()

import { computed } from 'vue'
import { PROFESS_LABELS } from '@/constants/enums'

const displayTitle = computed(() => {
  if (!props.work) return ''
  // 优先取第一个 tag 作为标题
  if (props.work.tags && props.work.tags.length > 0) return props.work.tags[0]
  // 否则取描述前 12 字
  if (props.work.description) {
    return props.work.description.length > 12 ? props.work.description.slice(0, 12) : props.work.description
  }
  return ''
})

function professionLabel(code: string): string {
  return PROFESS_LABELS[code] || ''
}

const emit = defineEmits<{
  (e: 'tap', work: WorkListItem): void
  (e: 'cover-load', work: WorkListItem, height: number): void
}>()

function onTap() {
  emit('tap', props.work)
}

function onCoverLoad(e: any) {
  const w = e?.detail?.width || 0
  const h = e?.detail?.height || 0
  if (w > 0 && h > 0) {
    // 容器宽度按卡片宽度（CSS 100%），由父组件计算高度
    const containerWidth = 360 // 默认
    const ratio = h / w
    emit('cover-load', props.work, containerWidth * ratio)
  }
}

function formatCount(n: number): string {
  if (n >= 10000) return `${(n / 10000).toFixed(1)}w`
  if (n >= 1000) return `${(n / 1000).toFixed(1)}k`
  return String(n)
}
</script>

<style lang="scss" scoped>
.work-card {
  background: $color-bg-card;
  border-radius: 16rpx;
  overflow: hidden;
  &__cover-wrap {
    position: relative;
    width: 100%;
    background: $color-bg-page;
  }
  &__cover {
    width: 100%;
    display: block;
  }
  &__cover-placeholder {
    width: 100%;
    height: 360rpx;
  }
  &__video-badge {
    position: absolute;
    top: 12rpx;
    right: 12rpx;
    background: rgba(0, 0, 0, 0.5);
    border-radius: 12rpx;
    padding: 4rpx 12rpx;
  }
  &__video-text { color: #FFFFFF; font-size: 20rpx; }
  &__title {
    display: block;
    padding: 16rpx 16rpx 4rpx;
    font-size: 30rpx;
    color: $color-text-primary;
    line-height: 1.4;
    font-weight: 500;
    @include line-clamp(2);
  }
  &__tags {
    padding: 4rpx 16rpx 8rpx;
    display: flex;
    gap: 8rpx;
    flex-wrap: wrap;
  }
  &__tag {
    font-size: 20rpx;
    color: $color-primary;
    background: $color-primary-soft;
    padding: 2rpx 12rpx;
    border-radius: 8rpx;
  }
  &__footer {
    display: flex;
    align-items: center;
    padding: 12rpx 16rpx 16rpx;
    gap: 8rpx;
  }
  &__avatar, &__avatar-placeholder {
    width: 40rpx;
    height: 40rpx;
    border-radius: 20rpx;
    flex-shrink: 0;
  }
  &__avatar-placeholder { background: $color-bg-page; }
  &__meta { flex: 1; min-width: 0; }
  &__name-row {
    display: flex;
    align-items: center;
    gap: 4rpx;
  }
  &__name {
    font-size: 24rpx;
    color: $color-text-primary;
    @include ellipsis;
  }
  &__gender {
    font-size: 22rpx;
    color: $color-primary;
  }
  &__sub {
    font-size: 20rpx;
    color: $color-text-helper;
    margin-top: 2rpx;
  }
  &__stat {
    font-size: 22rpx;
    color: $color-text-secondary;
    flex-shrink: 0;
  }
}
</style>
