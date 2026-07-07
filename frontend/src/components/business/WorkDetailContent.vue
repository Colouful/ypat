<template>
  <view class="work-detail">
    <view class="work-detail__user">
      <image v-if="work.user && work.user.avatar" class="work-detail__avatar" :src="work.user.avatar" mode="aspectFill" />
      <view v-else class="work-detail__avatar-placeholder" />
      <view class="work-detail__user-meta">
        <view class="work-detail__name-row">
          <text class="work-detail__nickname">{{ work.user && work.user.nickname || '匿名' }}</text>
          <text v-if="work.user && work.user.gender" class="work-detail__gender">
            {{ work.user.gender === '1' ? '♂' : work.user.gender === '2' ? '♀' : '' }}
          </text>
          <text class="work-detail__active-time">今日活跃</text>
        </view>
        <text class="work-detail__user-sub">
          <text v-if="work.user && work.user.profession">{{ professLabel(work.user.profession) }}</text>
          <text v-if="work.user && work.user.city"> | {{ work.user.city }}</text>
        </text>
      </view>
      <view v-if="!work.isOwner" class="work-detail__complain" @tap="emit('complain')">
        <text class="work-detail__complain-icon"></text>
        <text>投诉</text>
      </view>
    </view>

    <view v-if="work.description" class="work-detail__title-block">
      <text class="work-detail__title">{{ work.description }}</text>
    </view>

    <view v-if="work.medias && work.medias.length" class="work-detail__medias">
      <view v-for="(m, i) in work.medias" :key="i" class="work-detail__media-item">
        <image v-if="m.type !== 'VIDEO' && m.type !== '2'" class="work-detail__media" :src="m.url" mode="widthFix" @tap="previewImage(i)" />
        <video v-else class="work-detail__media-video" :src="m.url" controls />
      </view>
    </view>

    <view v-if="work.tags && work.tags.length" class="work-detail__tags">
      <view v-for="t in work.tags" :key="t.id" class="work-detail__tag">
        <KeepIcon name="circle-x" :size="20" color="#23C268" />
        <text>{{ t.name }}</text>
      </view>
    </view>

    <view class="work-detail__stats">
      <text class="work-detail__time">{{ formatTime(work.publishTime) }}</text>
      <view class="work-detail__stat-group">
        <text class="work-detail__stat">阅读 {{ formatCount(work.readCount) }}</text>
        <text class="work-detail__stat">赞 {{ formatCount(work.likeCount) }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepIcon from './KeepIcon.vue'
import { getProfessLabel } from '@/constants/enums'
import type { WorkDetail } from '@/api/types/work'

const props = defineProps<{
  work: WorkDetail
}>()

const emit = defineEmits<{
  (e: 'complain'): void
}>()

function professLabel(code: string): string {
  return getProfessLabel(code)
}

function previewImage(idx: number) {
  const urls = (props.work.medias || [])
    .filter((m) => m.type !== 'VIDEO' && m.type !== '2')
    .map((m) => m.url)
  uni.previewImage({ urls, current: urls[idx] })
}

function formatCount(n: number | undefined): string {
  if (!n) return '0'
  if (n >= 10000) return `${(n / 10000).toFixed(1)}w`
  if (n >= 1000) return `${(n / 1000).toFixed(1)}k`
  return String(n)
}

function formatTime(t: string | undefined): string {
  if (!t) return ''
  const date = new Date(t)
  const now = Date.now()
  const diff = (now - date.getTime()) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  return `${Math.floor(diff / 86400)}天前`
}
</script>

<style lang="scss" scoped>
.work-detail {
  padding: 0 32rpx 200rpx;
  background: #FFFFFF;
  &__user {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    gap: 16rpx;
  }
  &__avatar, &__avatar-placeholder {
    width: 80rpx;
    height: 80rpx;
    border-radius: 40rpx;
    flex-shrink: 0;
  }
  &__avatar-placeholder { background: $color-bg-page; }
  &__user-meta { flex: 1; min-width: 0; }
  &__name-row {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }
  &__nickname {
    font-size: 30rpx;
    font-weight: 600;
    color: $color-text-primary;
  }
  &__gender {
    font-size: 24rpx;
    color: $color-primary;
  }
  &__active-time {
    font-size: 22rpx;
    color: $color-text-helper;
    margin-left: 8rpx;
  }
  &__user-sub {
    font-size: 24rpx;
    color: $color-text-secondary;
    margin-top: 4rpx;
  }
  &__complain {
    display: flex;
    align-items: center;
    gap: 4rpx;
    font-size: 24rpx;
    color: $color-text-helper;
    padding: 8rpx 0;
  }
  &__complain-icon {
    font-size: 24rpx;
    color: $color-text-helper;
  }
  &__title-block {
    padding: 16rpx 0;
    border-left: 8rpx solid $color-primary;
    padding-left: 16rpx;
    margin: 16rpx 0;
  }
  &__title {
    font-size: 32rpx;
    font-weight: 600;
    color: $color-text-primary;
  }
  &__medias {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
  }
  &__media-item { width: 100%; }
  &__media { width: 100%; display: block; }
  &__media-video { width: 100%; aspect-ratio: 16/9; }
  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    padding: 24rpx 0;
  }
  &__tag {
    display: flex;
    align-items: center;
    gap: 4rpx;
    font-size: 24rpx;
    color: $color-text-primary;
  }
  &__tag-icon {
    color: $color-primary;
    font-size: 18rpx;
  }
  &__stats {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 0;
  }
  &__time {
    font-size: 24rpx;
    color: $color-text-helper;
  }
  &__stat-group {
    display: flex;
    gap: 32rpx;
  }
  &__stat {
    font-size: 24rpx;
    color: $color-text-helper;
  }
}
</style>
