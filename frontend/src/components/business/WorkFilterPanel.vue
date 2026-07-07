<template>
  <view v-if="visible" class="work-filter" @tap="onMaskTap">
    <view class="work-filter__panel" @tap.stop>
      <picker mode="region" :value="regionPickerValue" @change="changeRegion">
        <view class="work-filter__row">
          <text class="work-filter__row-label">选择地区</text>
          <view class="work-filter__row-value">
            <text>{{ regionLabel || '全部' }}</text>
            <text class="work-filter__row-arrow">›</text>
          </view>
        </view>
      </picker>

      <view class="work-filter__section">
        <text class="work-filter__section-title">发布人性别</text>
        <view class="work-filter__chips">
          <view v-for="opt in genderOpts" :key="opt.value"
                :class="['work-filter__chip', { 'work-filter__chip--active': local.gender === opt.value }]"
                @tap="local.gender = opt.value">
            <text :class="{ 'work-filter__chip-text--active': local.gender === opt.value }">{{ opt.label }}</text>
          </view>
        </view>
      </view>

      <view class="work-filter__section">
        <text class="work-filter__section-title">发布人身份</text>
        <view class="work-filter__chips work-filter__chips--grid">
          <view v-for="opt in professionOpts" :key="opt.value"
                :class="['work-filter__chip', { 'work-filter__chip--active': local.profession === opt.value }]"
                @tap="local.profession = opt.value">
            <text :class="{ 'work-filter__chip-text--active': local.profession === opt.value }">{{ opt.label }}</text>
          </view>
        </view>
      </view>

      <view class="work-filter__actions">
        <button class="work-filter__btn work-filter__btn--reset" @tap="onReset">重置</button>
        <button class="work-filter__btn work-filter__btn--ok" @tap="onConfirm">确认</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, watch, computed } from 'vue'
import { PUBLIC_PROFESS_OPTIONS } from '@/constants/enums'

interface FilterValue {
  region: string
  regionLabel: string
  gender: string
  profession: string
}

const props = defineProps<{
  visible: boolean
  modelValue: FilterValue
}>()

const emit = defineEmits<{
  (e: 'update:visible', v: boolean): void
  (e: 'update:modelValue', v: FilterValue): void
  (e: 'reset'): void
  (e: 'confirm', v: FilterValue): void
}>()

const local = reactive<FilterValue>({ ...props.modelValue })

watch(() => props.modelValue, (v) => {
  Object.assign(local, v)
}, { deep: true })

watch(() => props.visible, (v) => {
  if (v) Object.assign(local, props.modelValue)
})

const genderOpts = [
  { value: '', label: '全部' },
  { value: '1', label: '男' },
  { value: '2', label: '女' },
]

const professionOpts = [
  { value: '', label: '全部' },
  ...PUBLIC_PROFESS_OPTIONS,
]

const regionLabel = computed(() => {
  return local.regionLabel || local.region
})
const regionPickerValue = computed(() => {
  if (!local.regionLabel) return []
  return local.regionLabel.split(' / ')
})

function changeRegion(event: { detail: { value: string[] } }) {
  const [province = '', city = '', area = ''] = event.detail.value || []
  local.region = city
  local.regionLabel = [province, city, area].filter(Boolean).join(' / ')
}

function onMaskTap() {
  emit('update:visible', false)
}
function onReset() {
  const empty: FilterValue = { region: '', regionLabel: '', gender: '', profession: '' }
  Object.assign(local, empty)
  emit('update:modelValue', empty)
  emit('reset')
}
function onConfirm() {
  emit('update:modelValue', { ...local })
  emit('confirm', { ...local })
  emit('update:visible', false)
}
</script>

<style lang="scss" scoped>
.work-filter {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 250;
  display: flex;
  align-items: flex-end;
  &__panel {
    width: 100%;
    background: $color-bg-card;
    border-radius: 32rpx 32rpx 0 0;
    padding: 32rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
    box-sizing: border-box;
  }
  &__row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 0 32rpx;
    border-bottom: 2rpx solid $color-border;
  }
  &__row-label {
    font-size: 30rpx;
    color: $color-text-primary;
  }
  &__row-value {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 28rpx;
    color: $color-text-primary;
  }
  &__row-arrow {
    color: $color-text-helper;
    font-size: 28rpx;
  }
  &__section {
    padding: 24rpx 0;
  }
  &__section-title {
    display: block;
    font-size: 28rpx;
    color: $color-text-primary;
    margin-bottom: 20rpx;
  }
  &__chips {
    display: flex;
    gap: 16rpx;
    &--grid {
      display: grid;
      grid-template-columns: 1fr 1fr 1fr 1fr;
      gap: 16rpx;
    }
  }
  &__chip {
    padding: 16rpx 24rpx;
    background: $color-bg-page;
    border-radius: 8rpx;
    text-align: center;
    border: 2rpx solid transparent;
    &--active {
      border-color: $color-primary;
      background: $color-primary-soft;
    }
  }
  &__chip-text--active {
    color: $color-primary;
  }
  &__actions {
    display: flex;
    gap: 16rpx;
    margin-top: 32rpx;
  }
  &__btn {
    flex: 1;
    height: 88rpx;
    line-height: 88rpx;
    text-align: center;
    border-radius: 8rpx;
    font-size: 30rpx;
    border: none;
    &::after { border: none; }
    &--reset { background: $color-bg-page; color: $color-text-primary; }
    &--ok { background: $color-primary; color: #FFFFFF; }
  }
}
</style>
