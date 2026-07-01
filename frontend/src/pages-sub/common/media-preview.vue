<template>
  <view class="media-preview-page">
    <swiper :indicator-dots="true" :duration="200" circular>
      <swiper-item v-for="(m, i) in items" :key="i">
        <view class="media-preview-page__slide">
          <image v-if="m.type !== 'VIDEO' && m.type !== '2'" class="media-preview-page__img" :src="m.url" mode="aspectFit" />
          <video v-else class="media-preview-page__video" :src="m.url" controls />
        </view>
      </swiper-item>
    </swiper>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface PreviewItem {
  url: string
  type: 'IMAGE' | 'VIDEO' | '1' | '2'
}

const items = ref<PreviewItem[]>([])

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  const opts = (page && page.options) || {}
  if (opts.items) {
    try {
      items.value = JSON.parse(decodeURIComponent(opts.items))
    } catch {
      items.value = []
    }
  }
  if (opts.url) {
    items.value = [{ url: decodeURIComponent(opts.url), type: 'IMAGE' }]
  }
})
</script>

<style lang="scss" scoped>
.media-preview-page {
  width: 100vw;
  height: 100vh;
  background: #000000;
  swiper { width: 100%; height: 100%; }
  &__slide {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  &__img { width: 100%; height: 100%; }
  &__video { width: 100%; height: 100%; }
}
</style>
