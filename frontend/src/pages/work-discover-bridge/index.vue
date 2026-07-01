<template>
  <view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
// 桥接旧 /pages/discover/index，自动跳到 /pages/work/index
onMounted(() => {
  uni.reLaunch({ url: '/pages/work/index' })
})
</script>
