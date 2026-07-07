<template>
  <view class="work-publish-form">
    <!-- 作品名称/描述 -->
    <view class="work-publish-form__card">
      <view class="work-publish-form__card-head">
        <text class="work-publish-form__label work-publish-form__label--required">作品名称/描述</text>
      </view>
      <textarea v-model="form.description" class="work-publish-form__textarea"
                placeholder="请输入作品名称/描述...（不能包含任何联系方式，照片中不能有漏点图片，否则审核不通过）"
                :maxlength="500" />
      <text class="work-publish-form__count">{{ form.description.length }}/500</text>
    </view>

    <!-- 上传照片/视频 -->
    <view class="work-publish-form__card">
      <view class="work-publish-form__card-head">
        <text class="work-publish-form__label work-publish-form__label--required">上传照片/视频</text>
        <text class="work-publish-form__sub-label">（必须本人拍摄/本人照片）</text>
      </view>
      <MediaUploader v-model="mediaItems" />
    </view>

    <!-- 使用设备 -->
    <view class="work-publish-form__field">
      <text class="work-publish-form__label work-publish-form__label--inline">使用设备</text>
      <input v-model="form.device" class="work-publish-form__input" maxlength="100" placeholder="请输入使用设备（选填）" />
    </view>

    <!-- 拍摄地点 -->
    <view class="work-publish-form__field">
      <text class="work-publish-form__label work-publish-form__label--inline">拍摄地点</text>
      <input v-model="form.shootLocation" class="work-publish-form__input" maxlength="100" placeholder="请输入拍摄地点（选填）" />
    </view>

    <!-- 约拍返片 -->
    <view class="work-publish-form__field work-publish-form__field--row">
      <text class="work-publish-form__label work-publish-form__label--inline">约拍返片</text>
      <view class="work-publish-form__switch">
        <text class="work-publish-form__switch-text">是否约拍返片</text>
        <switch :checked="form.returnPhotoFlagBool" @change="(e: any) => form.returnPhotoFlagBool = e.detail.value" color="#23C268" />
      </view>
    </view>

    <!-- 主题标签 -->
    <view class="work-publish-form__card">
      <view class="work-publish-form__card-head">
        <text class="work-publish-form__label work-publish-form__label--required">主题标签</text>
      </view>
      <TagSelector :selectedIds="form.selectedTagIds" :tags="tagOptions" :maxSelect="5"
                   @update:selectedIds="form.selectedTagIds = $event" />
    </view>

    <view class="work-publish-form__bottom-spacer" />
    <view class="work-publish-form__submit">
      <button class="work-publish-form__btn" :class="{ 'work-publish-form__btn--disabled': submitting }" :disabled="submitting" @tap="onSubmit">
        {{ submitting ? '提交中...' : '确认发布' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import MediaUploader from './MediaUploader.vue'
import TagSelector from './TagSelector.vue'
import { getWorkTags } from '@/api/modules/dict'
import { submit as submitWork } from '@/api/modules/work'
import { resolveWorkTagOptions, WORK_TAG_LIMIT } from '@/constants/work-tags'
import type { WorkTag } from '@/api/types/work'
import type { MediaItem } from '@/api/types/media'

const emit = defineEmits<{
  (e: 'submitted', id: number): void
  (e: 'error', msg: string): void
}>()

const form = reactive({
  description: '',
  device: '',
  shootLocation: '',
  returnPhotoFlagBool: false,
  selectedTagIds: [] as number[],
})
const mediaItems = ref<MediaItem[]>([])
const tagOptions = ref<WorkTag[]>([])
const submitting = ref(false)

onMounted(async () => {
  try {
    const res = await getWorkTags()
    const data = (res && res.data) || []
    tagOptions.value = resolveWorkTagOptions(data)
  } catch (e) {
    tagOptions.value = resolveWorkTagOptions([])
  }
})

async function onSubmit() {
  if (submitting.value) return
  if (form.description.length < 5) {
    uni.showToast({ title: '描述至少 5 个字', icon: 'none' })
    return
  }
  if (form.selectedTagIds.length === 0) {
    uni.showToast({ title: '请选择主题标签', icon: 'none' })
    return
  }
  if (form.selectedTagIds.length > WORK_TAG_LIMIT) {
    uni.showToast({ title: `标签最多选择 ${WORK_TAG_LIMIT} 个`, icon: 'none' })
    return
  }
  if (mediaItems.value.length === 0) {
    uni.showToast({ title: '请上传媒体', icon: 'none' })
    return
  }
  const failed = mediaItems.value.find((m) => m.uploadStatus === 'failed')
  if (failed) {
    uni.showToast({ title: '部分媒体上传失败，请重试或删除', icon: 'none' })
    return
  }
  const pending = mediaItems.value.find((m) => m.uploadStatus !== 'success')
  if (pending) {
    uni.showToast({ title: '媒体正在上传中', icon: 'none' })
    return
  }
  const mediaIds = mediaItems.value.map((m) => m.mediaId).filter((id): id is number => typeof id === 'number')
  if (mediaIds.length === 0) {
    uni.showToast({ title: '媒体上传结果异常', icon: 'none' })
    return
  }
  const mediaType = mediaItems.value[0].type === 'VIDEO' ? '2' : '1'

  submitting.value = true
  try {
    const res = await submitWork({
      description: form.description,
      device: form.device || undefined,
      shootLocation: form.shootLocation || undefined,
      returnPhotoFlag: form.returnPhotoFlagBool ? '1' : '0',
      mediaType,
      mediaIds: mediaIds.join(','),
      tagIds: form.selectedTagIds.join(','),
    })
    const data = (res && res.data) || { id: 0 }
    uni.showToast({ title: '发布成功，等待审核', icon: 'success' })
    emit('submitted', data.id)
  } catch (e: any) {
    const msg = e?.message || '发布失败'
    uni.showToast({ title: msg, icon: 'none' })
    emit('error', msg)
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.work-publish-form {
  padding: 0 32rpx 32rpx;
  background: #FFFFFF;
  min-height: 100vh;
  &__card {
    background: $color-bg-card;
    border-radius: 16rpx;
    padding: 24rpx 32rpx;
    margin-bottom: 16rpx;
  }
  &__card-head {
    margin-bottom: 16rpx;
  }
  &__field {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: $color-bg-card;
    border-radius: 16rpx;
    padding: 32rpx;
    margin-bottom: 16rpx;
    min-height: 96rpx;
    &--row {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
  }
  &__label {
    font-size: 30rpx;
    color: $color-text-primary;
    &--required::before {
      content: '*';
      color: $color-primary;
      margin-right: 4rpx;
    }
    &--inline {
      flex-shrink: 0;
    }
  }
  &__sub-label {
    font-size: 24rpx;
    color: $color-text-helper;
    margin-left: 8rpx;
  }
  &__textarea {
    width: 100%;
    height: 200rpx;
    font-size: 28rpx;
    color: $color-text-primary;
    line-height: 1.6;
  }
  &__count {
    display: block;
    text-align: right;
    font-size: 22rpx;
    color: $color-text-helper;
    margin-top: 8rpx;
  }
  &__input {
    flex: 1;
    text-align: right;
    font-size: 28rpx;
    color: $color-text-primary;
    padding-left: 16rpx;
  }
  &__switch {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }
  &__switch-text {
    font-size: 26rpx;
    color: $color-text-secondary;
  }
  &__bottom-spacer { height: 200rpx; }
  &__submit {
    position: fixed;
    z-index: 300;
    left: 0;
    right: 0;
    bottom: 0;
    padding: 24rpx 32rpx calc(24rpx + env(safe-area-inset-bottom));
    background: $color-bg-card;
  }
  &__btn {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    background: $color-primary;
    color: #FFFFFF;
    font-size: 30rpx;
    font-weight: 600;
    border-radius: 999rpx;
    border: none;
    box-shadow: 0 12rpx 24rpx rgba(35, 194, 104, 0.28);
    &::after { border: none; }
    // 组件 WXSS 禁止 [disabled] 属性选择器，改用 class 绑定
    &--disabled {
      background: $color-text-helper;
      color: #FFFFFF;
      box-shadow: none;
    }
  }
}
</style>
