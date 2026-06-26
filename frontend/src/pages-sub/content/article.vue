<template>
  <view class="page">
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="errorMessage" class="state error">
      <text>{{ errorMessage }}</text>
      <button @tap="loadArticle">重新加载</button>
    </view>
    <article v-else-if="article" class="article">
      <image v-if="article.imgpath" class="cover" :src="article.imgpath" mode="aspectFill" />
      <text class="title">{{ article.title }}</text>
      <view class="meta">
        <text>{{ article.timeStr || formatDate(article.credate) }}</text>
        <text>·</text>
        <text>{{ article.readtimes || 0 }} 次阅读</text>
      </view>
      <rich-text class="content" :nodes="article.content" />
    </article>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import * as contentApi from '@/api/modules/content'
import type { Article } from '@/api/types'

const article = ref<Article | null>(null)
const loading = ref(true)
const errorMessage = ref('')
let articleId = 0

async function loadArticle(): Promise<void> {
  if (!articleId) return
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await contentApi.getArticleDetail(articleId)
    article.value = result.data
    if (result.data?.title) uni.setNavigationBarTitle({ title: result.data.title })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '文章加载失败'
  } finally {
    loading.value = false
  }
}

function formatDate(value?: string): string {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onLoad((options) => {
  articleId = Number(options?.id || 0)
  if (!articleId) {
    loading.value = false
    errorMessage.value = '文章不存在'
    return
  }
  loadArticle()
})
</script>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.page { min-height: 100vh; padding: 28rpx; background: $color-bg-page; }
.state { padding: 240rpx 40rpx; color: $color-text-secondary; text-align: center; }
.state button { margin-top: 24rpx; }
.error { color: #b4232c; }
.article { display: block; overflow: hidden; padding-bottom: 48rpx; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; }
.cover { width: 100%; height: 420rpx; }
.title { display: block; padding: 34rpx 32rpx 12rpx; color: $color-text-primary; font-size: 40rpx; font-weight: 700; }
.meta { display: flex; gap: 12rpx; padding: 0 32rpx 28rpx; color: $color-text-helper; font-size: 24rpx; }
.content { display: block; padding: 0 32rpx; color: $color-text-primary; font-size: 29rpx; line-height: 1.85; }
</style>
