<template>
  <view v-if="visibleBanners.length" class="home-banner">
    <swiper
      class="home-banner__swiper"
      :autoplay="visibleBanners.length > 1"
      :circular="visibleBanners.length > 1"
      :indicator-dots="visibleBanners.length > 1"
      indicator-color="rgba(255, 255, 255, 0.45)"
      indicator-active-color="#FFFFFF"
      :interval="4800"
      :duration="360"
    >
      <swiper-item v-for="item in visibleBanners" :key="item.id">
        <view class="home-banner__item" @tap="handleTap(item)">
          <image class="home-banner__image" :src="item.image" mode="aspectFill" />
          <view class="home-banner__shade">
            <view class="home-banner__copy">
              <text class="home-banner__label">精选活动</text>
              <text class="home-banner__title">{{ item.title || '发现更多约拍灵感' }}</text>
            </view>
          </view>
        </view>
      </swiper-item>
    </swiper>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import * as contentApi from '@/api/modules/content'
import { normalizeImageUrl } from '@/api/adapters'
import type { Banner } from '@/api/types'
import { openBannerAction, resolveBannerAction } from '@/utils/banner-link'

interface BannerView extends Banner {
  image: string
}

const banners = ref<BannerView[]>([])

const visibleBanners = computed(() => banners.value.filter((item) => item.image))

async function loadBanners(): Promise<void> {
  try {
    const res = await contentApi.getBannerList({ page: 0, size: 5 })
    const list = (res.data?.content || [])
      .map((item) => ({
        ...item,
        image: normalizeImageUrl(item.imgpath),
      }))
      .filter((item) => item.image)
    banners.value = list
  } catch {
    banners.value = []
  }
}

function handleTap(item: BannerView): void {
  const action = resolveBannerAction(item)
  openBannerAction(action, () => {
    uni.previewImage({
      urls: visibleBanners.value.map((banner) => banner.image),
      current: item.image,
    })
  })
}

onMounted(loadBanners)
</script>

<style scoped lang="scss">
.home-banner {
  margin-top: 28rpx;
  padding: 0 32rpx;
}

.home-banner__swiper {
  height: 244rpx;
  overflow: hidden;
  border-radius: 30rpx;
  box-shadow: 0 18rpx 42rpx rgba(20, 24, 31, 0.12);
}

.home-banner__item {
  position: relative;
  width: 100%;
  height: 244rpx;
  overflow: hidden;
  border-radius: 30rpx;
  background: $color-bg-chip;
}

.home-banner__image {
  width: 100%;
  height: 100%;
}

.home-banner__shade {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  align-items: flex-end;
  min-height: 132rpx;
  padding: 0 28rpx 28rpx;
  background: linear-gradient(180deg, rgba(26, 29, 31, 0), rgba(26, 29, 31, 0.72));
}

.home-banner__copy {
  min-width: 0;
}

.home-banner__label {
  display: inline-flex;
  width: fit-content;
  padding: 6rpx 14rpx;
  border-radius: $radius-round;
  color: #1A1D1F;
  background: rgba(255, 255, 255, 0.86);
  font-size: 20rpx;
  font-weight: 900;
}

.home-banner__title {
  display: block;
  max-width: 560rpx;
  margin-top: 12rpx;
  overflow: hidden;
  color: #FFFFFF;
  font-size: 34rpx;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
