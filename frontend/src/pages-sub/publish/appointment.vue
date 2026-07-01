<template>
  <view class="appointment-page">
    <KeepPageNav :title="pageTitle" />
    <AppointmentPublishForm :target="target" :workId="workId" :authorId="authorId" @submitted="onSubmitted" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import AppointmentPublishForm from '@/components/business/AppointmentPublishForm.vue'
import { YPAT_ROLE_CONFIGS, YpatTarget, type YpatTargetType } from '@/constants/enums'

const target = ref<YpatTargetType>(YpatTarget.PHOTOGRAPHER)
const workId = ref<string>('')
const authorId = ref<string>('')
const pageTitle = computed(() => {
  const c = YPAT_ROLE_CONFIGS[target.value]
  return c ? c.label : '发布约拍'
})

const pages = getCurrentPages()
const page = pages[pages.length - 1] as any
const opts = (page && page.options) || {}
if (opts.target) target.value = opts.target as YpatTargetType
if (opts.workId) workId.value = String(opts.workId)
if (opts.authorId) authorId.value = String(opts.authorId)

function onSubmitted() {
  setTimeout(() => uni.navigateBack(), 1500)
}
</script>

<style lang="scss" scoped>
.appointment-page {
  min-height: 100vh;
  background: $color-bg-page;
}
</style>
