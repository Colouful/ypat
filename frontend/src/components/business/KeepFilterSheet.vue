<template>
  <view v-if="visible" class="keep-filter">
    <view class="keep-filter__mask" @tap="close" />
    <view class="keep-filter__sheet">
      <text class="keep-filter__title">筛选</text>
      <view v-for="group in groups" :key="group.key" class="keep-filter__group">
        <text class="keep-filter__group-title">{{ group.title }}</text>
        <view class="keep-filter__options">
          <view
            v-for="option in group.options"
            :key="option.value"
            class="keep-filter__option"
            :class="{ 'keep-filter__option--active': isSelected(group.key, option.value) }"
            @tap="toggleOption(group, option.value)"
          >
            {{ option.label }}
          </view>
        </view>
      </view>
      <view class="keep-filter__actions">
        <button class="keep-filter__reset" @tap="reset">重置</button>
        <button class="keep-filter__confirm" @tap="confirm">确定 · 看 {{ count }} 条</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'

export interface KeepFilterGroup {
  key: string
  title: string
  multiple: boolean
  options: Array<{ label: string; value: string }>
}

type FilterValue = Record<string, string[]>

const props = withDefaults(defineProps<{
  visible: boolean
  groups: KeepFilterGroup[]
  modelValue?: FilterValue
  count?: number
}>(), {
  modelValue: () => ({}),
  count: 86,
})

const emit = defineEmits<{
  (event: 'update:visible', value: boolean): void
  (event: 'update:modelValue', value: FilterValue): void
  (event: 'reset'): void
  (event: 'confirm', value: FilterValue): void
}>()

const localValue = reactive<FilterValue>({})

watch(
  () => props.modelValue,
  (value) => {
    Object.keys(localValue).forEach((key) => delete localValue[key])
    Object.entries(value || {}).forEach(([key, selected]) => {
      localValue[key] = [...selected]
    })
  },
  { immediate: true, deep: true },
)

function cloneValue(): FilterValue {
  return Object.fromEntries(Object.entries(localValue).map(([key, value]) => [key, [...value]]))
}

function isSelected(groupKey: string, optionValue: string): boolean {
  return localValue[groupKey]?.includes(optionValue) || false
}

function toggleOption(group: KeepFilterGroup, optionValue: string): void {
  const selected = localValue[group.key] || []
  if (group.multiple) {
    localValue[group.key] = selected.includes(optionValue)
      ? selected.filter((value) => value !== optionValue)
      : selected.concat(optionValue)
  } else {
    localValue[group.key] = [optionValue]
  }
  emit('update:modelValue', cloneValue())
}

function close(): void {
  emit('update:visible', false)
}

function reset(): void {
  Object.keys(localValue).forEach((key) => {
    localValue[key] = []
  })
  emit('update:modelValue', cloneValue())
  emit('reset')
}

function confirm(): void {
  const value = cloneValue()
  emit('confirm', value)
  close()
}
</script>

<style scoped lang="scss">
@import '@/styles/tokens.scss';
@import '@/styles/mixins.scss';

.keep-filter {
  position: fixed;
  inset: 0;
  z-index: $z-index-mask;
}

.keep-filter__mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
}

.keep-filter__sheet {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 44rpx 40rpx calc(44rpx + env(safe-area-inset-bottom));
  background: $color-bg-card;
  border-radius: $radius-keep-sheet $radius-keep-sheet 0 0;
}

.keep-filter__title {
  color: $color-text-primary;
  font-size: 40rpx;
  font-weight: 800;
}

.keep-filter__group {
  margin-top: 38rpx;
}

.keep-filter__group-title {
  display: block;
  margin-bottom: 22rpx;
  color: $color-text-helper;
  font-size: 28rpx;
  font-weight: 800;
}

.keep-filter__options {
  display: flex;
  flex-wrap: wrap;
  gap: 24rpx;
}

.keep-filter__option {
  @include keep-chip;
}

.keep-filter__option--active {
  @include keep-chip(true);
}

.keep-filter__actions {
  display: flex;
  gap: 24rpx;
  margin-top: 48rpx;
}

.keep-filter__reset,
.keep-filter__confirm {
  height: 96rpx;
  border-radius: $radius-round;
  font-size: 32rpx;
  font-weight: 800;
  line-height: 96rpx;
}

.keep-filter__reset {
  flex: 1;
  color: $color-text-secondary;
  background: $color-bg-chip;
}

.keep-filter__confirm {
  flex: 1.45;
  color: #fff;
  background: $color-primary;
}
</style>
