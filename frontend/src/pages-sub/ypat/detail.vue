<template>
  <YpatDetailView :id="ypatId" @share-meta="updateShareMeta" />
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onLoad, onShareAppMessage } from '@dcloudio/uni-app'
import YpatDetailView from '@/components/business/YpatDetailView.vue'

const ypatId = ref(0)
const shareMeta = reactive<{ title: string; imageUrl?: string }>({
  title: '爱去拍约拍详情',
})

onLoad((query) => {
  ypatId.value = Number(query?.id || 0)
})

function updateShareMeta(value: { title: string; imageUrl?: string }): void {
  shareMeta.title = value.title
  shareMeta.imageUrl = value.imageUrl
}

onShareAppMessage(() => ({
  title: shareMeta.title || '爱去拍约拍详情',
  path: `/pages-sub/ypat/detail?id=${ypatId.value}`,
  imageUrl: shareMeta.imageUrl,
}))
</script>
