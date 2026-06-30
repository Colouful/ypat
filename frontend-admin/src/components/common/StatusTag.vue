<script setup lang="ts">
import { computed } from 'vue'
import { getUserStatusInfo } from '@/constants/enums'

const props = defineProps<{
  status: string
}>()

const statusInfo = computed(() => getUserStatusInfo(props.status))
</script>

<template>
  <el-tag :type="statusInfo.type" size="small">
    {{ statusInfo.name }}
  </el-tag>
</template>
