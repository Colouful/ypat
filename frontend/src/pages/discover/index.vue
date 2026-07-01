<template>
  <view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
/**
 * 兼容旧 /pages/discover/index
 * 自动重定向到 /pages/work/index
 * 保留此文件以防旧版小程序缓存仍指向此路径
 */
onMounted(() => {
  uni.reLaunch({ url: '/pages/work/index' })
})
</script>
