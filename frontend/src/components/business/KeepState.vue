<template>
  <view class="keep-state" :class="`keep-state--${type}`">
    <view class="keep-state__icon">
      <KeepIcon :name="iconName" :size="56" />
    </view>
    <text class="keep-state__title">{{ resolvedTitle }}</text>
    <text v-if="description" class="keep-state__description">{{ description }}</text>
    <button v-if="buttonText" class="keep-state__button" @tap="$emit('action')">
      {{ buttonText }}
    </button>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import KeepIcon from './KeepIcon.vue'

type StateType = 'loading' | 'empty' | 'error' | 'login'

const props = withDefaults(defineProps<{
  type?: StateType
  title?: string
  description?: string
  buttonText?: string
}>(), {
  type: 'empty',
  title: '',
  description: '',
  buttonText: '',
})

defineEmits<{
  (event: 'action'): void
}>()

const fallbackTitle: Record<StateType, string> = {
  loading: '加载中...',
  empty: '暂无内容',
  error: '加载失败',
  login: '登录后查看',
}

const iconByType: Record<StateType, string> = {
  loading: 'compass',
  empty: 'image',
  error: 'sliders',
  login: 'user',
}

const resolvedTitle = computed(() => props.title || fallbackTitle[props.type])
const iconName = computed(() => iconByType[props.type])
</script>

<style scoped lang="scss">

.keep-state {
  @include flex-column;
  align-items: center;
  justify-content: center;
  min-height: 320rpx;
  padding: 56rpx 32rpx;
  color: $color-text-secondary;
  text-align: center;
}

.keep-state__icon {
  @include flex-center;
  width: 104rpx;
  height: 104rpx;
  margin-bottom: 24rpx;
  border-radius: 50%;
  color: $color-primary-dark;
  background: $color-primary-light;
}

.keep-state--error .keep-state__icon {
  color: $color-accent-orange;
  background: $color-orange-soft;
}

.keep-state__title {
  color: $color-text-primary;
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
}

.keep-state__description {
  max-width: 560rpx;
  margin-top: 12rpx;
  color: $color-text-secondary;
  font-size: $font-size-sm;
  line-height: 1.6;
}

.keep-state__button {
  min-width: 220rpx;
  height: 76rpx;
  margin-top: 28rpx;
  border-radius: $radius-round;
  color: #fff;
  background: $color-primary;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  line-height: 76rpx;
}
</style>
