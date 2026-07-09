<template>
  <view class="detail-page">
    <KeepState
      v-if="loading"
      type="loading"
      title="加载中..."
    />
    <KeepState
      v-else-if="errorMessage"
      type="error"
      :title="errorMessage"
      button-text="重新加载"
      @action="load"
    />
    <template v-else-if="detail">
      <view class="detail-topbar">
        <view
          class="detail-icon-button"
          @tap="back"
        >
          <KeepIcon
            name="chevron-left"
            :size="42"
          />
        </view>
        <view
          class="detail-icon-button"
          @tap="copyShareLink"
        >
          <KeepIcon
            name="copy"
            :size="38"
          />
        </view>
      </view>

      <view class="hero-wrap">
        <swiper
          class="hero-swiper"
          circular
          :indicator-dots="images.length > 1"
        >
          <swiper-item
            v-for="(image, index) in images"
            :key="image"
          >
            <image
              :src="image"
              mode="aspectFill"
              @tap="preview(index)"
            />
          </swiper-item>
        </swiper>
        <view class="hero-count">
          1 / {{ images.length || 1 }}
        </view>
      </view>

      <view class="detail-sheet">
        <view class="detail-tags detail-tag-row">
          <text class="detail-tag detail-tag--green">
            <text class="detail-tag__text">
              {{ detail.targetTxt || targetLabel }}
            </text>
          </text>
          <text class="detail-tag detail-tag--green">
            <text class="detail-tag__text">
              {{ detail.chargewayTxt || detail.chargeway }}
            </text>
          </text>
          <text class="detail-tag detail-tag--icon">
            <KeepIcon
              name="map-pin"
              :size="24"
            />
            <text class="detail-tag__text">
              {{ cityText }}
            </text>
          </text>
        </view>
        <view class="detail-trust-row">
          <view
            v-for="item in trustItems"
            :key="item.key"
            :class="['detail-trust-chip', `detail-trust-chip--${item.tone}`]"
          >
            <KeepIcon
              :name="item.icon"
              :size="22"
            />
            <text class="detail-trust-chip__text">
              {{ item.label }}
            </text>
          </view>
        </view>

        <text class="detail-title">
          {{ detailTitle }}
        </text>
        <view class="detail-meta">
          <text class="detail-meta__item">
            <KeepIcon
              name="eye"
              :size="26"
            />
            {{ detail.readtimes || 0 }}
          </text>
          <text class="detail-meta__item">
            <KeepIcon
              name="handshake"
              :size="26"
            />
            约拍 {{ detail.pattimes || 0 }}
          </text>
          <text class="detail-meta__item">
            <KeepIcon
              name="star"
              :size="26"
            />
            收藏 {{ detail.coltimes || 0 }}
          </text>
        </view>
        <text class="detail-body">
          {{ detail.describ }}
        </text>

        <view
          v-if="styleTags.length"
          class="style-tags"
        >
          <text
            v-for="style in styleTags"
            :key="style"
            class="style-tag"
          >
            {{ style }}
          </text>
        </view>

        <view
          class="author-card"
          @tap="goProfile"
        >
          <image
            class="author-card__avatar"
            :src="authorAvatar"
            mode="aspectFill"
          />
          <view class="author-card__info">
            <view class="author-card__name-row">
              <text class="author-card__name">
                {{ authorName }}
              </text>
              <text
                v-if="isMemberActive"
                class="author-card__member"
              >
                {{ memberBadgeLabel }}
              </text>
            </view>
            <view class="author-card__trust">
              <text :class="['author-card__trust-tag', isRealname ? 'author-card__trust-tag--verified' : 'author-card__trust-tag--muted']">
                {{ realnameLabel }}
              </text>
              <text :class="['author-card__trust-tag', isCreditPaid ? 'author-card__trust-tag--secured' : 'author-card__trust-tag--muted']">
                {{ creditLabel }}
              </text>
            </view>
            <text class="author-card__desc">
              {{ authorSummary }}
            </text>
          </view>
          <view class="author-card__profile">
            查看主页
          </view>
        </view>

        <text class="section-title">
          TA 的作品
        </text>
        <view class="portfolio-grid">
          <image
            v-for="image in portfolioImages"
            :key="image"
            :src="image"
            mode="aspectFill"
          />
        </view>
      </view>

      <view class="detail-actions">
        <view
          class="detail-actions__mini"
          @tap="favorite"
        >
          <KeepIcon
            name="star"
            :size="42"
          />
          <text>{{ favorited ? '已收藏' : '收藏' }}</text>
        </view>
        <view
          class="detail-actions__mini"
          @tap="goProfile"
        >
          <KeepIcon
            name="mail"
            :size="42"
          />
          <text>主页</text>
        </view>
        <button
          class="detail-actions__primary"
          :disabled="actionLoading"
          :loading="actionLoading"
          @tap="apply"
        >
          立即约拍
        </button>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import * as ypatApi from '@/api/modules/ypat'
import { put } from '@/api/request'
import { normalizeImageUrl } from '@/api/adapters'
import { TARGET_LABELS } from '@/constants/enums'
import KeepIcon from './KeepIcon.vue'
import KeepState from './KeepState.vue'
import type { YpatInfo } from '@/api/types'
import { goRootTab } from '@/utils/tab-navigation'

const props = defineProps<{ id: number }>()
const emit = defineEmits<{
  'share-meta': [{ title: string; imageUrl?: string }]
}>()
const userStore = useUserStore()
const detail = ref<YpatInfo | null>(null)
const loading = ref(false)
const actionLoading = ref(false)
const errorMessage = ref('')
const favorited = ref(false)
const images = computed(() => detail.value?.pics?.map(normalizeImageUrl).filter(Boolean) || ['/static/default-cover.png'])
const portfolioImages = computed(() => images.value.slice(0, 6).concat(Array(Math.max(0, 6 - images.value.length)).fill('/static/default-cover.png')))
const authorAvatar = computed(() => normalizeImageUrl(detail.value?.userQo?.imgpath || detail.value?.userQo?.avatarurl) || '/static/default-avatar.png')
const detailTitle = computed(() => detail.value?.describ?.split('\n')[0] || detail.value?.targetTxt || '约拍详情')
const cityText = computed(() => [detail.value?.city, detail.value?.area].filter(Boolean).join('·') || '同城')
const styleTags = computed(() => (detail.value?.patstyleTxt || detail.value?.patstyle || '').split(/[,，\s]+/).filter(Boolean).slice(0, 6))
const targetLabel = computed(() => detail.value ? TARGET_LABELS[detail.value.target] || '约拍' : '约拍')
const authorName = computed(() => detail.value?.userQo?.nickname || '匿名用户')
const authorSummary = computed(() => detail.value?.userQo?.profess?.trim() || '摄影爱好者')
const isRealname = computed(() => resolveFlag(detail.value?.userQo?.realnameflag, detail.value?.realnameflag))
const isCreditPaid = computed(() => resolveFlag(detail.value?.creditflag, detail.value?.userQo?.creditflag))
const isMemberActive = computed(() => detail.value?.userQo?.memberActive === true)
const memberBadgeLabel = computed(() => resolveMemberBadge(detail.value?.userQo?.memberLevel))
const realnameLabel = computed(() => isRealname.value ? '已认证' : '未认证')
const creditLabel = computed(() => isCreditPaid.value ? '已缴担保金' : '未缴担保金')
const memberStatusLabel = computed(() => isMemberActive.value ? `${memberBadgeLabel.value}会员` : '非会员')
const trustItems = computed(() => [
  {
    key: 'realname',
    icon: 'shield',
    label: realnameLabel.value,
    tone: isRealname.value ? 'verified' : 'muted',
  },
  {
    key: 'credit',
    icon: 'wallet',
    label: creditLabel.value,
    tone: isCreditPaid.value ? 'secured' : 'muted',
  },
  {
    key: 'member',
    icon: 'gem',
    label: memberStatusLabel.value,
    tone: isMemberActive.value ? 'member' : 'muted',
  },
])

function resolveFlag(primary?: string | null, fallback?: string | null): boolean {
  if (primary === '1' || primary === '0') return primary === '1'
  if (fallback === '1' || fallback === '0') return fallback === '1'
  return false
}

function resolveMemberBadge(level?: string | null): string {
  switch ((level || '').trim().toUpperCase()) {
    case 'PLUS':
      return 'VIP+'
    case 'PRO':
      return 'PRO'
    default:
      return 'VIP'
  }
}

async function load(): Promise<void> {
  if (!props.id) {
    errorMessage.value = '缺少约拍 ID'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await ypatApi.getDetail(props.id)
    detail.value = result.data
    favorited.value = result.data?.colflag === '1'
    emit('share-meta', {
      title: detailTitle.value,
      imageUrl: images.value[0],
    })
    put('/ypat/yd/add', { ypatid: props.id }, { showError: false }).catch(() => undefined)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '获取详情失败'
  } finally {
    loading.value = false
  }
}

function preview(index: number): void {
  uni.previewImage({ current: index, urls: images.value })
}

function back(): void {
  if (getCurrentPages().length > 1) uni.navigateBack()
  else goRootTab('/pages/home/index')
}

function copyShareLink(): void {
  if (!detail.value) return
  const path = `/pages-sub/ypat/detail?id=${detail.value.id}`
  uni.setClipboardData({
    data: path,
    success: () => uni.showToast({ title: '链接已复制', icon: 'success' }),
    fail: () => uni.showToast({ title: '当前平台不支持复制链接', icon: 'none' }),
  })
}

function goProfile(): void {
  if (detail.value?.userid) uni.navigateTo({ url: `/pages-sub/user/profile?id=${detail.value.userid}` })
}

function requireLogin(): boolean {
  if (userStore.isLoggedIn) return true
  uni.navigateTo({ url: '/pages/login/index' })
  return false
}

async function favorite(): Promise<void> {
  if (!requireLogin() || !detail.value || favorited.value || actionLoading.value) return
  actionLoading.value = true
  try {
    await ypatApi.addFavorite(userStore.userInfo!.id, detail.value.id)
    favorited.value = true
    uni.showToast({ title: '收藏成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '收藏失败', icon: 'none' })
  } finally {
    actionLoading.value = false
  }
}

async function apply(): Promise<void> {
  if (!requireLogin() || !detail.value || actionLoading.value) return
  if (detail.value.userid === userStore.userInfo!.id) {
    uni.showToast({ title: '不能报名自己发布的约拍', icon: 'none' })
    return
  }
  // 约拍理由由用户填写并校验 ≥6 字(对齐旧版 orderShe content.length<6 拦截);
  // 实名/保证金/拍拍豆余额/重复报名均由后端强制并经 request.ts showBusinessGuide
  // 引导(1010 实名 / 1009 充值 / 1011 保证金 / 1006 已报名)。
  const modal = await new Promise<UniApp.ShowModalRes>((resolve) => {
    uni.showModal({
      title: '报名约拍',
      editable: true,
      placeholderText: '简单介绍自己或拍摄想法（至少 6 个字）',
      success: resolve,
      fail: () => resolve({ confirm: false, cancel: true, content: '' }),
    })
  })
  if (!modal.confirm) return
  const content = (modal.content || '').trim()
  if (content.length < 6) {
    uni.showToast({ title: '约拍理由至少 6 个字', icon: 'none' })
    return
  }
  actionLoading.value = true
  try {
    await ypatApi.applyYpat({
      sendperid: userStore.userInfo!.id,
      recperid: detail.value.userid,
      ypatid: detail.value.id,
      content,
    })
    uni.showToast({ title: '报名成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '报名失败', icon: 'none' })
  } finally {
    actionLoading.value = false
  }
}

watch(() => props.id, load, { immediate: true })
</script>

<style scoped lang="scss">

.detail-page {
  min-height: 100vh;
  padding-bottom: 168rpx;
  background: $color-bg-page;
}

.detail-topbar {
  position: fixed;
  top: calc(24rpx + env(safe-area-inset-top));
  right: 28rpx;
  left: 28rpx;
  z-index: 20;
  display: flex;
  justify-content: space-between;
}

.detail-icon-button {
  @include flex-center;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  color: #fff;
  background: rgba(0, 0, 0, 0.34);
}

.hero-wrap {
  position: relative;
}

.hero-swiper {
  height: 720rpx;
  background: $color-bg-chip;
}

.hero-swiper image {
  width: 100%;
  height: 100%;
}

.hero-count {
  position: absolute;
  right: 28rpx;
  bottom: 46rpx;
  padding: 8rpx 22rpx;
  border-radius: $radius-round;
  color: #fff;
  background: rgba(0, 0, 0, 0.42);
  font-size: 24rpx;
  font-weight: 700;
}

.detail-sheet {
  position: relative;
  z-index: 2;
  margin-top: -44rpx;
  padding: 40rpx 36rpx 36rpx;
  border-radius: 44rpx 44rpx 0 0;
  background: $color-bg-card;
}

.detail-tags,
.style-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.detail-tag-row,
.detail-trust-row,
.author-card__trust,
.author-card__name-row {
  min-width: 0;
}

.detail-tag,
.style-tag {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  min-width: 0;
  max-width: 100%;
  padding: 10rpx 22rpx;
  border-radius: $radius-round;
  color: $color-text-secondary;
  background: $color-bg-chip;
  font-size: 24rpx;
  font-weight: 800;
}

.detail-tag__text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-tag--green {
  color: $color-primary-dark;
  background: $color-primary-light;
}

.detail-trust-row {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 16rpx;
}

.detail-trust-chip {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  min-width: 0;
  max-width: 100%;
  padding: 12rpx 24rpx;
  border-radius: $radius-round;
  font-size: 24rpx;
  font-weight: 800;
}

.detail-trust-chip__text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-trust-chip--verified {
  color: #176c71;
  background: #e1f5f3;
}

.detail-trust-chip--secured {
  color: #8b6b1d;
  background: #fff4d5;
}

.detail-trust-chip--member {
  color: #85621f;
  background: #fff0c2;
}

.detail-trust-chip--muted {
  color: $color-text-secondary;
  background: #f2f4f7;
}

.detail-title {
  display: block;
  margin-top: 28rpx;
  color: $color-text-primary;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1.35;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 32rpx;
  margin-top: 28rpx;
  padding: 26rpx 0;
  border-top: 1rpx solid $color-border;
  border-bottom: 1rpx solid $color-border;
  color: $color-text-secondary;
  font-size: 26rpx;
  font-weight: 700;
}

.detail-meta__item {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
}

.detail-body {
  display: block;
  margin-top: 28rpx;
  color: #3A3D3F;
  font-size: 30rpx;
  line-height: 1.75;
}

.style-tags {
  margin-top: 28rpx;
}

.author-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-top: 36rpx;
  padding: 28rpx;
  border: 1rpx solid $color-border;
  border-radius: $radius-keep-card;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(247, 249, 251, 0.96) 100%);
}

.author-card__avatar {
  width: 100rpx;
  height: 100rpx;
  flex: none;
  border-radius: 50%;
  background: $color-bg-chip;
}

.author-card__info {
  min-width: 0;
  flex: 1;
}

.author-card__name,
.author-card__desc {
  display: block;
}

.author-card__name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.author-card__name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: $color-text-primary;
  font-size: 32rpx;
  font-weight: 800;
}

.author-card__member {
  flex: none;
  padding: 6rpx 14rpx;
  border-radius: $radius-round;
  color: #7a5b16;
  background: linear-gradient(135deg, #fff2bf 0%, #ffe39a 100%);
  font-size: 20rpx;
  font-weight: 900;
  line-height: 1.2;
}

.author-card__trust {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 12rpx;
}

.author-card__trust-tag {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  padding: 8rpx 18rpx;
  border-radius: $radius-round;
  font-size: 22rpx;
  font-weight: 700;
}

.author-card__trust-tag--verified {
  color: #176c71;
  background: #e1f5f3;
}

.author-card__trust-tag--secured {
  color: #8b6b1d;
  background: #fff4d5;
}

.author-card__trust-tag--muted {
  color: $color-text-secondary;
  background: #f2f4f7;
}

.author-card__desc {
  margin-top: 12rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  line-height: 1.55;
}

.author-card__profile {
  flex: none;
  padding: 16rpx 30rpx;
  border-radius: $radius-round;
  color: $color-primary-dark;
  background: $color-primary-light;
  font-size: 26rpx;
  font-weight: 800;
}

.section-title {
  display: block;
  margin-top: 40rpx;
  color: $color-text-primary;
  font-size: 34rpx;
  font-weight: 900;
}

.portfolio-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10rpx;
  margin-top: 24rpx;
}

.portfolio-grid image {
  width: 100%;
  height: 212rpx;
  border-radius: 20rpx;
  background: $color-bg-chip;
}

.detail-actions {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  gap: 28rpx;
  padding: 24rpx 36rpx calc(28rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid $color-border;
  background: $color-bg-card;
}

.detail-actions__mini {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: $color-text-secondary;
  font-size: 22rpx;
  font-weight: 700;
}

.detail-actions__primary {
  flex: 1;
  height: 100rpx;
  border-radius: $radius-round;
  color: #fff;
  background: $color-primary;
  box-shadow: $shadow-keep-button;
  font-size: 32rpx;
  font-weight: 900;
  line-height: 100rpx;
}
</style>
