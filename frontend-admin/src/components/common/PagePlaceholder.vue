<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <div class="page-placeholder">
    <el-empty description="该模块待迁移">
      <template #image>
        <el-icon :size="64" color="#909399">
          <Tools />
        </el-icon>
      </template>
      <p class="placeholder-info">
        页面：<strong>{{ route.meta?.title || route.path }}</strong>
      </p>
      <p class="placeholder-hint">此模块尚未迁移，请参考迁移矩阵中的计划</p>
    </el-empty>
  </div>
</template>

<style scoped lang="scss">
.page-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.placeholder-info {
  margin-top: $spacing-base;
  color: $text-regular;
}

.placeholder-hint {
  margin-top: $spacing-sm;
  color: $text-secondary;
  font-size: $font-size-sm;
}
</style>
