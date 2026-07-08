<template>
  <view class="complain-page">
    <KeepPageNav title="作品投诉" />

    <view v-if="work" class="author-card">
      <image class="author-card__avatar" :src="authorAvatar" mode="aspectFill" />
      <view class="author-card__body">
        <text class="author-card__name">{{ work.user?.nickname || '匿名用户' }}</text>
        <text class="author-card__meta">{{ authorMeta }}</text>
      </view>
      <view class="author-card__block" :class="{ 'author-card__block--on': blockTarget }" @tap="confirmBlock">
        {{ blockTarget ? '已确认拉黑' : '拉黑TA' }}
      </view>
    </view>

    <view class="section">
      <text class="section__title">投诉原因</text>
      <view class="reason-grid">
        <view
          v-for="item in reasons"
          :key="item"
          class="reason-grid__item"
          :class="{ 'reason-grid__item--on': reason === item }"
          @tap="reason = item"
        >
          {{ item }}
        </view>
      </view>
    </view>

    <view class="section">
      <text class="section__title">投诉内容</text>
      <textarea
        class="textarea"
        maxlength="300"
        :value="content"
        placeholder="请补充问题细节，平台会结合证据尽快处理"
        placeholder-class="textarea__placeholder"
        @input="onContentInput"
      />
      <text class="counter">{{ content.length }}/300</text>
    </view>

    <view class="section">
      <view class="section__row">
        <text class="section__title">证据截图</text>
        <text class="section__hint">最多 3 张</text>
      </view>
      <view class="evidence-grid">
        <view v-for="(item, index) in evidence" :key="item.localPath" class="evidence-grid__item">
          <image class="evidence-grid__image" :src="item.url || item.localPath" mode="aspectFill" @tap="previewEvidence(index)" />
          <view v-if="item.uploading" class="evidence-grid__mask">上传中</view>
          <view class="evidence-grid__remove" @tap.stop="removeEvidence(index)">×</view>
        </view>
        <view v-if="evidence.length < 3" class="evidence-grid__add" @tap="chooseEvidence">
          <text class="evidence-grid__plus">+</text>
          <text>上传截图</text>
        </view>
      </view>
    </view>

    <view class="notice">
      <text>提交后平台会核查作品、账号和证据。恶意投诉会影响账号信用。</text>
    </view>

    <button class="submit" :disabled="submitDisabled" :loading="submitting" @tap="submitComplain">
      {{ submitting ? '提交中...' : '提交投诉' }}
    </button>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { complain, getDetail } from '@/api/modules/work'
import { normalizeImageUrl } from '@/api/adapters'
import { chooseImages, uploadImageWithRetry } from '@/utils/media-uploader'
import { getProfessLabel } from '@/constants/enums'
import type { WorkDetail } from '@/api/types/work'
import type { MediaItem } from '@/api/types/media'

interface EvidenceItem {
  localPath: string
  url?: string
  uploading: boolean
}

const workId = ref(0)
const work = ref<WorkDetail | null>(null)
const reason = ref('')
const content = ref('')
const evidence = ref<EvidenceItem[]>([])
const blockTarget = ref(false)
const submitting = ref(false)

const reasons = ['盗用图片', '欺诈收费', '色情低俗', '不实信息', '骚扰侵权', '其他问题']

const authorAvatar = computed(() => normalizeImageUrl(work.value?.user?.avatar) || '/static/default-avatar.png')
const authorMeta = computed(() => {
  const user = work.value?.user
  return [getProfessLabel(user?.profession || ''), user?.city].filter(Boolean).join(' · ') || '作品作者'
})
const submitDisabled = computed(() => (
  submitting.value
  || !reason.value
  || (!content.value.trim() && !evidence.value.some((item) => item.url))
  || evidence.value.some((item) => item.uploading)
))

async function loadWork(): Promise<void> {
  if (!workId.value) return
  try {
    work.value = (await getDetail(workId.value)).data || null
  } catch {
    work.value = null
  }
}

function confirmBlock(): void {
  uni.showModal({
    title: '拉黑确认',
    content: '加入黑名单后，对方将不能给你发送约拍请求和极速联系信息。',
    confirmText: '确认拉黑',
    success: ({ confirm }) => {
      if (confirm) blockTarget.value = true
    },
  })
}

function onContentInput(event: Event): void {
  const detail = (event as unknown as { detail?: { value?: string } }).detail
  const target = event.target as HTMLTextAreaElement | null
  content.value = String(detail?.value ?? target?.value ?? '')
}

async function chooseEvidence(): Promise<void> {
  const remain = 3 - evidence.value.length
  if (remain <= 0) return
  try {
    const files = await chooseImages({ count: remain })
    const pending = files.map((file) => ({ localPath: file.path, uploading: true }))
    evidence.value = evidence.value.concat(pending)
    await Promise.all(pending.map(uploadEvidence))
  } catch {
    uni.showToast({ title: '未选择图片', icon: 'none' })
  }
}

async function uploadEvidence(item: EvidenceItem): Promise<void> {
  const media: MediaItem = {
    localPath: item.localPath,
    type: 'IMAGE',
    size: 0,
    uploadStatus: 'pending',
    progress: 0,
  }
  const result = await uploadImageWithRetry(media)
  item.uploading = false
  if (result.uploadStatus === 'success' && result.url) {
    item.url = result.url
  } else {
    uni.showToast({ title: result.error || '截图上传失败', icon: 'none' })
  }
}

function removeEvidence(index: number): void {
  evidence.value.splice(index, 1)
}

function previewEvidence(index: number): void {
  const urls = evidence.value.map((item) => item.url || item.localPath).filter(Boolean)
  uni.previewImage({ urls, current: urls[index] })
}

async function submitComplain(): Promise<void> {
  if (submitDisabled.value || !workId.value) return
  submitting.value = true
  try {
    await complain({
      workId: workId.value,
      reason: reason.value,
      content: content.value.trim(),
      pics: evidence.value.map((item) => item.url).filter(Boolean).join(','),
      blockFlag: blockTarget.value ? '1' : '0',
    })
    uni.showToast({ title: '投诉已提交', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 700)
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

onLoad((options) => {
  workId.value = Number(options?.workId || options?.id || 0)
  void loadWork()
})
</script>

<style scoped lang="scss">
.complain-page {
  min-height: 100vh;
  padding: 28rpx 28rpx 48rpx;
  background: $color-bg-page;
}

.author-card,
.section,
.notice {
  border-radius: 30rpx;
  background: #FFFFFF;
  box-shadow: $shadow-keep-card;
}

.author-card {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 24rpx;
}

.author-card__avatar {
  width: 88rpx;
  height: 88rpx;
  flex: none;
  border-radius: 50%;
  background: $color-bg-chip;
}

.author-card__body {
  min-width: 0;
  flex: 1;
}

.author-card__name {
  display: block;
  overflow: hidden;
  color: $color-text-primary;
  font-size: 32rpx;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.author-card__meta {
  display: block;
  margin-top: 8rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 700;
}

.author-card__block {
  flex: none;
  padding: 14rpx 22rpx;
  border-radius: $radius-round;
  color: $color-accent-red;
  background: #FFF1F1;
  font-size: 24rpx;
  font-weight: 900;
}

.author-card__block--on {
  color: #FFFFFF;
  background: $color-accent-red;
}

.section {
  margin-top: 24rpx;
  padding: 28rpx;
}

.section__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section__title {
  color: $color-text-primary;
  font-size: 30rpx;
  font-weight: 900;
}

.section__hint,
.counter {
  color: $color-text-helper;
  font-size: 23rpx;
  font-weight: 700;
}

.reason-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 22rpx;
}

.reason-grid__item {
  padding: 22rpx 12rpx;
  border: 2rpx solid transparent;
  border-radius: 20rpx;
  color: $color-text-secondary;
  background: $color-bg-chip;
  font-size: 26rpx;
  font-weight: 800;
  text-align: center;
}

.reason-grid__item--on {
  border-color: $color-primary;
  color: $color-primary-dark;
  background: $color-primary-soft;
}

.textarea {
  box-sizing: border-box;
  width: 100%;
  height: 220rpx;
  margin-top: 20rpx;
  padding: 22rpx;
  border-radius: 22rpx;
  background: $color-bg-page;
  color: $color-text-primary;
  font-size: 27rpx;
  line-height: 1.6;
}

.counter {
  display: block;
  margin-top: 10rpx;
  text-align: right;
}

.evidence-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 22rpx;
}

.evidence-grid__item,
.evidence-grid__add {
  position: relative;
  width: 182rpx;
  height: 182rpx;
  overflow: hidden;
  border-radius: 22rpx;
  background: $color-bg-chip;
}

.evidence-grid__image {
  width: 100%;
  height: 100%;
}

.evidence-grid__add {
  @include flex-column;
  align-items: center;
  justify-content: center;
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 800;
}

.evidence-grid__plus {
  font-size: 54rpx;
  line-height: 1;
}

.evidence-grid__mask {
  position: absolute;
  inset: 0;
  @include flex-center;
  color: #FFFFFF;
  background: rgba(0, 0, 0, 0.46);
  font-size: 24rpx;
  font-weight: 800;
}

.evidence-grid__remove {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  @include flex-center;
  width: 38rpx;
  height: 38rpx;
  border-radius: 50%;
  color: #FFFFFF;
  background: rgba(0, 0, 0, 0.54);
  font-size: 30rpx;
}

.notice {
  margin-top: 24rpx;
  padding: 22rpx 26rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  line-height: 1.6;
}

.submit {
  height: 94rpx;
  margin-top: 34rpx;
  border-radius: $radius-round;
  color: #FFFFFF;
  background: $color-primary;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 94rpx;
  box-shadow: $shadow-keep-button;
}

.submit[disabled] {
  opacity: 0.45;
}

.submit::after {
  border: 0;
}
</style>
