<template>
  <view class="target-select-page">
    <KeepPageNav title="选择约拍对象" />
    <AppointmentTargetSelector @select="onSelect" />
    <ModelWarningDialog v-model:visible="warningVisible" @confirm="onConfirm" @cancel="onCancel" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import AppointmentTargetSelector from '@/components/business/AppointmentTargetSelector.vue'
import ModelWarningDialog from '@/components/business/ModelWarningDialog.vue'
import { YpatTarget, type YpatTargetType, UserProfess } from '@/constants/enums'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const warningVisible = ref(false)
const pendingTarget = ref<YpatTargetType | null>(null)

function onSelect(target: YpatTargetType) {
  // 素人模特 + 约模特 → 弹窗
  if (target === YpatTarget.MODEL && userStore.userInfo?.profess === UserProfess.AMATEUR_MODEL) {
    pendingTarget.value = target
    warningVisible.value = true
    return
  }
  goAppointment(target)
}

function onConfirm() {
  if (pendingTarget.value) goAppointment(pendingTarget.value)
  pendingTarget.value = null
}
function onCancel() {
  pendingTarget.value = null
}

function goAppointment(target: YpatTargetType) {
  uni.navigateTo({ url: `/pages-sub/publish/appointment?target=${target}` })
}
</script>

<style lang="scss" scoped>
.target-select-page {
  min-height: 100vh;
  background: $color-bg-page;
  padding-top: 32rpx;
}
</style>
