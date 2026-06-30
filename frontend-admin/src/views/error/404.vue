<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome(): void {
  router.push('/dashboard')
}
</script>

<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">404</h1>
      <p class="error-desc">抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
}

.error-content {
  text-align: center;
}

.error-code {
  font-size: 96px;
  font-weight: 700;
  color: $text-placeholder;
  margin-bottom: $spacing-base;
}

.error-desc {
  font-size: $font-size-lg;
  color: $text-secondary;
  margin-bottom: $spacing-xl;
}
</style>
