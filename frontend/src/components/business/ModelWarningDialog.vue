<template>
  <view v-if="visible" class="model-warning-mask" @tap="onMaskTap">
    <view class="model-warning-dialog" @tap.stop>
      <text class="model-warning-dialog__title">温馨提示</text>
      <text class="model-warning-dialog__content">
        您的当前身份是"素人模特"，您确定要发布找"模特"的约拍需求吗？
      </text>
      <view class="model-warning-dialog__actions">
        <button class="model-warning-dialog__btn model-warning-dialog__btn--cancel" @tap="onCancel">取消</button>
        <button class="model-warning-dialog__btn model-warning-dialog__btn--ok" @tap="onConfirm">确定</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean
}>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

function onMaskTap() {
  emit('update:visible', false)
  emit('cancel')
}
function onCancel() {
  emit('update:visible', false)
  emit('cancel')
}
function onConfirm() {
  emit('update:visible', false)
  emit('confirm')
}
</script>

<style lang="scss" scoped>
.model-warning-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 400;
}
.model-warning-dialog {
  width: 560rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 48rpx 48rpx 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
  &__title {
    font-size: 32rpx;
    font-weight: 600;
    color: $color-text-primary;
    margin-bottom: 24rpx;
    text-align: center;
  }
  &__content {
    font-size: 28rpx;
    color: $color-text-primary;
    line-height: 1.6;
    text-align: center;
    margin-bottom: 48rpx;
  }
  &__actions {
    width: 100%;
    display: flex;
    border-top: 2rpx solid $color-border;
    margin: 0 -48rpx;
  }
  &__btn {
    flex: 1;
    height: 96rpx;
    line-height: 96rpx;
    text-align: center;
    font-size: 30rpx;
    border: none;
    background: transparent;
    border-radius: 0;
    &--cancel {
      color: $color-text-primary;
      border-right: 2rpx solid $color-border;
    }
    &--ok {
      color: $color-primary;
      font-weight: 500;
    }
    &::after { border: none; }
  }
}
</style>
