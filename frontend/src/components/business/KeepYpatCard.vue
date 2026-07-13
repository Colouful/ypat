<template>
  <view
    class="keep-ypat-card"
    @tap="$emit('tap', item)"
  >
    <image
      class="keep-ypat-card__thumb"
      :src="item.image"
      mode="aspectFill"
      lazy-load
    />
    <view class="keep-ypat-card__content">
      <view class="keep-ypat-card__body">
        <view class="keep-ypat-card__head">
          <text class="keep-ypat-card__title">
            {{ item.title }}
          </text>
        </view>

        <view class="keep-ypat-card__base-tags">
          <text class="keep-ypat-card__tag keep-ypat-card__tag--main">
            <text class="keep-ypat-card__label-text">
              {{ item.targetLabel }}
            </text>
          </text>
          <text class="keep-ypat-card__tag keep-ypat-card__tag--way">
            <text class="keep-ypat-card__label-text">
              {{ item.chargeLabel }}
            </text>
          </text>
        </view>

        <view
          v-if="item.tags.length"
          class="keep-ypat-card__topic-tags"
        >
          <text
            v-for="tag in item.tags.slice(0, 3)"
            :key="tag"
            class="keep-ypat-card__topic-tag"
          >
            # {{ tag }}
          </text>
          <text
            v-if="item.tags.length > 3"
            class="keep-ypat-card__topic-more"
          >
            +{{ item.tags.length - 3 }}
          </text>
        </view>

        <view class="keep-ypat-card__user">
          <image
            class="keep-ypat-card__avatar"
            :src="item.avatar"
            mode="aspectFill"
          />
          <view class="keep-ypat-card__identity">
            <view class="keep-ypat-card__name-row">
              <text class="keep-ypat-card__name">
                {{ item.name }}
              </text>
              <text
                v-if="item.memberActive"
                class="keep-ypat-card__member"
              >
                <KeepIcon
                  name="gem"
                  :size="18"
                />
                {{ getMemberBadgeLabel(item.memberLevel) }}
              </text>
            </view>
            <text class="keep-ypat-card__city">
              <KeepIcon
                name="map-pin"
                :size="22"
              />
              {{ item.city }}
            </text>
          </view>
        </view>

        <view class="keep-ypat-card__meta">
          <text>已收到约拍 {{ item.applyCount }}</text>
          <text>{{ item.time }}</text>
        </view>
      </view>

      <view
        v-if="item.realname || item.credit"
        class="keep-ypat-card__tags"
      >
        <text
          v-if="item.realname"
          class="keep-ypat-card__badge keep-ypat-card__badge--real"
        >
          <KeepIcon
            name="shield"
            :size="20"
          />
          <text class="keep-ypat-card__label-text">
            已认证
          </text>
        </text>
        <text
          v-if="item.credit"
          class="keep-ypat-card__badge keep-ypat-card__badge--credit"
        >
          <KeepIcon
            name="star"
            :size="20"
          />
          <text class="keep-ypat-card__label-text">
            已缴担保金
          </text>
        </text>
      </view>
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
  memberActive?: boolean
  memberLevel?: string
  tags: string[]
}

defineProps<{
  item: KeepYpatCardItem
}>()

defineEmits<{
  (event: 'tap', item: KeepYpatCardItem): void
}>()

const getMemberBadgeLabel = (memberLevel?: string) => {
  const normalizedLevel = memberLevel?.trim().toUpperCase()

  if (normalizedLevel === 'PLUS') {
    return 'VIP+'
  }

  if (normalizedLevel === 'PRO') {
    return 'PRO'
  }

  return 'VIP'
}
</script>

<style scoped lang="scss">
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

.keep-ypat-card__content {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.keep-ypat-card__body {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.keep-ypat-card__head {
  min-width: 0;
}

.keep-ypat-card__title {
  @include line-clamp(2);
  color: $color-text-primary;
  font-size: 32rpx;
  font-weight: 800;
  line-height: 1.35;
}

.keep-ypat-card__base-tags,
.keep-ypat-card__tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 16rpx;
}

.keep-ypat-card__tags {
  flex-wrap: nowrap;
}

.keep-ypat-card__topic-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 14rpx;
}

.keep-ypat-card__topic-tag,
.keep-ypat-card__topic-more {
  display: inline-flex;
  align-items: center;
  min-width: 0;
  max-width: 100%;
  min-height: 36rpx;
  padding: 0 12rpx;
  border: 1rpx solid rgba(35, 194, 104, 0.22);
  border-radius: 10rpx;
  color: $color-primary-dark;
  background: $color-primary-soft;
  font-size: 20rpx;
  font-weight: 700;
  line-height: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.keep-ypat-card__topic-more {
  flex: none;
  color: $color-primary;
  background: transparent;
}

.keep-ypat-card__tag,
.keep-ypat-card__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  min-width: 0;
  min-height: 42rpx;
  max-width: 100%;
  padding: 0 12rpx;
  border-radius: 10rpx;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1;
}

.keep-ypat-card__label-text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.keep-ypat-card__tag--main {
  color: $color-text-primary;
  background: $color-bg-chip;
}

.keep-ypat-card__tag--way {
  color: $color-text-secondary;
  background: rgba(255, 255, 255, 0.72);
  border: 1rpx solid rgba(127, 137, 160, 0.18);
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
  align-items: flex-start;
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

.keep-ypat-card__identity {
  min-width: 0;
  flex: 1;
  margin-left: 10rpx;
}

.keep-ypat-card__name-row {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 10rpx;
}

.keep-ypat-card__name {
  min-width: 0;
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.keep-ypat-card__member {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  min-height: 32rpx;
  flex: none;
  padding: 0 10rpx;
  border-radius: 999rpx;
  color: #9C7836;
  background: $color-gold-soft;
  border: 1rpx solid rgba(156, 120, 54, 0.2);
  font-size: 20rpx;
  font-weight: 800;
  line-height: 1;
}

.keep-ypat-card__city {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  max-width: 100%;
  margin-top: 8rpx;
  color: $color-text-helper;
  font-size: 22rpx;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.keep-ypat-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: auto;
  padding-top: 16rpx;
  color: $color-text-helper;
  font-size: 24rpx;
  font-weight: 700;
}

.keep-ypat-card__meta text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
