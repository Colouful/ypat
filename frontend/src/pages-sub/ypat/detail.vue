<template>
  <YpatDetailView :id="ypatId" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import YpatDetailView from '@/components/business/YpatDetailView.vue'

const ypatId = ref(0)

onLoad((query) => {
  ypatId.value = Number(query?.id || 0)
})
</script>
