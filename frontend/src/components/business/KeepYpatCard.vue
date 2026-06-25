<template>
  <view class="keep-ypat-card" @tap="$emit('tap', item)">
    <image class="keep-ypat-card__thumb" :src="item.image" mode="aspectFill" lazy-load />
    <view class="keep-ypat-card__body">
      <text class="keep-ypat-card__title">{{ item.title }}</text>
      <view class="keep-ypat-card__tags">
        <text class="keep-ypat-card__tag keep-ypat-card__tag--main">{{ item.targetLabel }}</text>
        <text class="keep-ypat-card__way">{{ item.chargeLabel }}</text>
        <text v-if="item.realname" class="keep-ypat-card__badge keep-ypat-card__badge--real">
          <KeepIcon name="shield" :size="20" />
          实名认证
        </text>
        <text v-if="item.credit" class="keep-ypat-card__badge keep-ypat-card__badge--credit">
          <KeepIcon name="star" :size="20" />
          信用担保
        </text>
      </view>
      <view class="keep-ypat-card__user">
        <image class="keep-ypat-card__avatar" :src="item.avatar" mode="aspectFill" />
        <text class="keep-ypat-card__name">{{ item.name }}</text>
        <text class="keep-ypat-card__city">📍{{ item.city }}</text>
      </view>
      <text class="keep-ypat-card__meta">已收到约拍 {{ item.applyCount }} · {{ item.time }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepIcon from './KeepIcon.vue'

export interface KeepYpatCardItem {
  id?: number
  title: string
  targetLabel: string
  chargeLabel: string
  city: string
  name: string
  image: string
  avatar: string
  time: string
  applyCount: number
  realname: boolean
  credit: boolean
}

defineProps<{
  item: KeepYpatCardItem
}>()

defineEmits<{
  (event: 'tap', item: KeepYpatCardItem): void
}>()
</script>

<style scoped lang="scss">
@import '@/styles/tokens.scss';
@import '@/styles/mixins.scss';

.keep-ypat-card {
  display: flex;
  gap: 24rpx;
  padding: 24rpx;
  margin-bottom: 22rpx;
  background: $color-bg-card;
  border-radius: $radius-keep-card;
  box-shadow: $shadow-keep-card;
}

.keep-ypat-card__thumb {
  width: 204rpx;
  height: 260rpx;
  flex: none;
  border-radius: 24rpx;
  background: $color-bg-chip;
}

.keep-ypat-card__body {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.keep-ypat-card__title {
  @include line-clamp(2);
  color: $color-text-primary;
  font-size: 32rpx;
  font-weight: 800;
  line-height: 1.35;
}

.keep-ypat-card__tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.keep-ypat-card__tag,
.keep-ypat-card__way,
.keep-ypat-card__badge {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  min-height: 42rpx;
  padding: 0 14rpx;
  border-radius: 10rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.keep-ypat-card__tag--main {
  color: $color-text-primary;
  background: $color-bg-chip;
}

.keep-ypat-card__way {
  padding: 0;
  color: $color-text-secondary;
  background: transparent;
}

.keep-ypat-card__badge--real {
  color: #5577A8;
  background: $color-blue-soft;
}

.keep-ypat-card__badge--credit {
  color: #9C7836;
  background: $color-gold-soft;
}

.keep-ypat-card__user {
  display: flex;
  align-items: center;
  min-width: 0;
  margin-top: 18rpx;
}

.keep-ypat-card__avatar {
  width: 36rpx;
  height: 36rpx;
  flex: none;
  border-radius: 50%;
  background: $color-bg-chip;
}

.keep-ypat-card__name,
.keep-ypat-card__city {
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 600;
}

.keep-ypat-card__name {
  max-width: 160rpx;
  margin-left: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.keep-ypat-card__city {
  margin-left: 16rpx;
}

.keep-ypat-card__meta {
  margin-top: auto;
  padding-top: 16rpx;
  color: $color-text-helper;
  font-size: 24rpx;
  font-weight: 700;
}
</style>
