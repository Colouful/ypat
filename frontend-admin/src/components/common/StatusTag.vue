<script setup lang="ts">
import { computed } from 'vue'
import { getUserStatusInfo, YpatStatus, ProductStatus, ArticleStatus, OrderStatus } from '@/constants/enums'

const props = defineProps<{
  status: string
  type?: 'user' | 'ypat' | 'product' | 'article' | 'order'
}>()

const statusInfo = computed(() => {
  const value = props.status
  switch (props.type) {
    case 'ypat':
      return Object.values(YpatStatus).find((s) => s.value === value) || { value, name: '未知', type: 'info' as const }
    case 'product':
      return Object.values(ProductStatus).find((s) => s.value === value) || { value, name: '未知', type: 'info' as const }
    case 'article':
      return Object.values(ArticleStatus).find((s) => s.value === value) || { value, name: '未知', type: 'info' as const }
    case 'order':
      return Object.values(OrderStatus).find((s) => s.value === value) || { value, name: '未知', type: 'info' as const }
    default:
      return getUserStatusInfo(value)
  }
})
</script>

<template>
  <el-tag :type="statusInfo.type" size="small">
    {{ statusInfo.name }}
  </el-tag>
</template>
