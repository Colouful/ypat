<template>
  <view class="article-page">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner" />
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 错误状态 -->
    <view v-else-if="error" class="error-container">
      <image class="error-icon" src="/static/icons/error.png" mode="aspectFit" />
      <text class="error-text">{{ errorMsg }}</text>
      <view class="retry-btn" @tap="loadArticle">
        <text class="retry-btn-text">重新加载</text>
      </view>
    </view>

    <!-- 文章内容 -->
    <view v-else-if="article" class="article-content">
      <!-- 封面图 -->
      <image
        v-if="article.coverUrl"
        class="article-cover"
        :src="article.coverUrl"
        mode="aspectFill"
      />

      <!-- 标题 -->
      <text class="article-title">{{ article.title }}</text>

      <!-- 文章元信息 -->
      <view class="article-meta">
        <text class="meta-author">{{ article.author }}</text>
        <text class="meta-divider">·</text>
        <text class="meta-date">{{ formatDate(article.createTime) }}</text>
        <text class="meta-divider">·</text>
        <text class="meta-read">{{ article.readCount }}次阅读</text>
      </view>

      <!-- 分隔线 -->
      <view class="divider" />

      <!-- 正文内容 -->
      <view class="article-body">
        <rich-text :nodes="article.content" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import * as contentApi from '@/api/modules/content'

interface Article {
  id: number
  title: string
  content: string
  coverUrl: string
  summary: string
  category: number
  author: string
  readCount: number
  status: number
  createTime: string
  updateTime: string
}

const article = ref<Article | null>(null)
const loading = ref(true)
const error = ref(false)
const errorMsg = ref('加载失败，请稍后重试')

let articleId = 0

onLoad((options) => {
  if (options?.id) {
    articleId = Number(options.id)
    loadArticle()
  } else {
    error.value = true
    loading.value = false
    errorMsg.value = '文章不存在'
  }
})

async function loadArticle() {
  if (!articleId) return

  loading.value = true
  error.value = false

  try {
    const res = await contentApi.getArticleDetail(articleId)
    if (res.data) {
      article.value = res.data
      // 设置页面标题
      uni.setNavigationBarTitle({ title: res.data.title })
    } else {
      error.value = true
      errorMsg.value = '文章不存在'
    }
  } catch (e) {
    error.value = true
    errorMsg.value = '网络异常，请检查网络后重试'
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.article-page {
  min-height: 100vh;
  background-color: $color-bg-card;
  padding-bottom: $spacing-xxl;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 300rpx;

  .loading-spinner {
    width: 64rpx;
    height: 64rpx;
    border: 4rpx solid $color-border;
    border-top-color: $color-primary;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  .loading-text {
    margin-top: $spacing-lg;
    font-size: $font-size-base;
    color: $color-text-secondary;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 260rpx;

  .error-icon {
    width: 160rpx;
    height: 160rpx;
    margin-bottom: $spacing-lg;
  }

  .error-text {
    font-size: $font-size-base;
    color: $color-text-secondary;
    margin-bottom: $spacing-xl;
  }

  .retry-btn {
    padding: $spacing-md $spacing-xxl;
    background-color: $color-primary;
    border-radius: $radius-round;

    .retry-btn-text {
      font-size: $font-size-base;
      color: #ffffff;
    }
  }
}

.article-content {
  .article-cover {
    width: 100%;
    height: 400rpx;
  }

  .article-title {
    display: block;
    padding: $spacing-xl $spacing-xl 0;
    font-size: $font-size-title;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    line-height: 1.4;
  }

  .article-meta {
    display: flex;
    align-items: center;
    padding: $spacing-lg $spacing-xl;

    .meta-author,
    .meta-date,
    .meta-read {
      font-size: $font-size-sm;
      color: $color-text-helper;
    }

    .meta-divider {
      margin: 0 $spacing-sm;
      font-size: $font-size-sm;
      color: $color-text-helper;
    }
  }

  .divider {
    height: 1rpx;
    margin: 0 $spacing-xl;
    background-color: $color-divider;
  }

  .article-body {
    padding: $spacing-xl;

    :deep(img) {
      max-width: 100% !important;
      height: auto !important;
      border-radius: $radius-sm;
      margin: $spacing-md 0;
    }

    :deep(p) {
      font-size: $font-size-base;
      color: $color-text-primary;
      line-height: 1.8;
      margin-bottom: $spacing-md;
    }
  }
}
</style>
