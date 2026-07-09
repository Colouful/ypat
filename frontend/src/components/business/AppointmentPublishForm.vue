<template>
  <view class="appointment-publish-form">
    <!-- 红色风险条 -->
    <view v-if="config" class="appointment-publish-form__warning">
      <KeepIcon name="warning" :size="24" color="#FF9F1C" />
      <text class="appointment-publish-form__warning-text">{{ config.riskText }}</text>
    </view>

    <!-- 约拍要求 -->
    <view class="appointment-publish-form__card">
      <view class="appointment-publish-form__card-head">
        <text class="appointment-publish-form__label appointment-publish-form__label--required">约拍要求</text>
        <text class="appointment-publish-form__sub">（尽可能详细）</text>
      </view>
      <textarea v-model="form.describ" class="appointment-publish-form__textarea"
                placeholder="请描述一下你的约拍说明...（不能包含任何联系方式，照片中不能有漏点图片，不能发布私房约拍。不要用拼图。否则审核不通过，5个字以上）"
                :maxlength="500" />
    </view>

    <!-- 上传照片/视频 -->
    <view class="appointment-publish-form__card">
      <view class="appointment-publish-form__card-head">
        <text class="appointment-publish-form__label appointment-publish-form__label--required">上传照片/视频</text>
        <text class="appointment-publish-form__sub">（本人作品/本人照片/期待风格）</text>
      </view>
      <MediaUploader v-model="mediaItems" />
    </view>

    <!-- 时间 / 地点 / 成片（角色配置驱动） -->
    <view v-if="config && config.showTime" class="appointment-publish-form__field">
      <text class="appointment-publish-form__label">时间</text>
      <view class="appointment-publish-form__inline">
        <picker mode="date" :value="form.patdate" :start="minPatdate" @change="changePatdate" class="appointment-publish-form__picker">
          <view class="appointment-publish-form__picker-field" :class="{ 'appointment-publish-form__picker-field--placeholder': !form.patdate }">
            {{ form.patdate || '请选择时间' }}
          </view>
        </picker>
      </view>
    </view>

    <view v-if="config && config.showLocation" class="appointment-publish-form__field">
      <text class="appointment-publish-form__label">地点</text>
      <view class="appointment-publish-form__inline">
        <input v-model="form.patarea" class="appointment-publish-form__input" maxlength="100" placeholder="你期望的地点" />
        <text class="appointment-publish-form__hint">（选填）</text>
      </view>
    </view>

    <view v-if="config && config.showDeliverable" class="appointment-publish-form__field">
      <text class="appointment-publish-form__label">成片</text>
      <view class="appointment-publish-form__inline">
        <input v-model="form.patslice" class="appointment-publish-form__input" maxlength="100" placeholder="如：原片多少，精修多少等" />
        <text class="appointment-publish-form__hint">（选填）</text>
      </view>
    </view>

    <!-- 费用模式 -->
    <view class="appointment-publish-form__field appointment-publish-form__field--link" @tap="onPickChargeway">
      <text class="appointment-publish-form__label appointment-publish-form__label--required">费用模式</text>
      <view class="appointment-publish-form__inline">
        <text class="appointment-publish-form__value">{{ chargewayText || '请选择费用模式' }}</text>
        <text class="appointment-publish-form__arrow">›</text>
      </view>
    </view>

    <!-- 面向地区 -->
    <view class="appointment-publish-form__field appointment-publish-form__field--link">
      <text class="appointment-publish-form__label appointment-publish-form__label--required">面向地区</text>
      <view class="appointment-publish-form__inline">
        <picker v-if="!form.isNationwide" mode="region" :value="regionValue" @change="changeRegion" class="appointment-publish-form__region-picker">
          <view class="appointment-publish-form__picker-field" :class="{ 'appointment-publish-form__picker-field--placeholder': !regionText }">
            {{ regionText || '请选择面向地区' }}
          </view>
        </picker>
        <text v-else class="appointment-publish-form__value">全国</text>
        <view v-if="config && config.allowNationwide" class="appointment-publish-form__nationwide" @tap.stop>
          <text>全国</text>
          <switch :checked="form.isNationwide" @change="changeNationwide" color="#23C268" />
        </view>
        <text v-if="!form.isNationwide" class="appointment-publish-form__arrow">›</text>
      </view>
    </view>

    <!-- 信用担保 -->
    <view class="appointment-publish-form__field appointment-publish-form__field--link" @tap="onPickCredit">
      <text class="appointment-publish-form__label appointment-publish-form__label--required">信用担保</text>
      <view class="appointment-publish-form__inline">
        <text class="appointment-publish-form__value">{{ creditText || '请选择信用担保' }}</text>
        <text class="appointment-publish-form__arrow">›</text>
      </view>
    </view>

    <!-- 主题标签 -->
    <view class="appointment-publish-form__card">
      <view class="appointment-publish-form__card-head">
        <text class="appointment-publish-form__label appointment-publish-form__label--required">主题标签</text>
      </view>
      <TagSelector :selectedIds="form.selectedTagIds" :tags="tagOptions" :maxSelect="5"
                   @update:selectedIds="form.selectedTagIds = $event" />
    </view>

    <view v-if="memberQuote" class="appointment-publish-form__card appointment-publish-form__benefit">
      <view class="appointment-publish-form__benefit-row">
        <text>原价：</text>
        <text>{{ memberQuote.originalPpd }} 拍拍豆</text>
      </view>
      <view v-if="memberQuote.discountPpd > 0" class="appointment-publish-form__benefit-row appointment-publish-form__benefit-row--discount">
        <text>{{ memberQuote.levelCode || 'BASIC' }} 会员优惠：</text>
        <text>-{{ memberQuote.discountPpd }} 拍拍豆</text>
      </view>
      <view class="appointment-publish-form__benefit-row appointment-publish-form__benefit-row--actual">
        <text>本次实扣：</text>
        <text>{{ memberQuote.actualPpd }} 拍拍豆</text>
      </view>
    </view>

    <view class="appointment-publish-form__bottom-spacer" />
    <view class="appointment-publish-form__submit">
      <button class="appointment-publish-form__btn" :class="{ 'appointment-publish-form__btn--disabled': submitting }" :disabled="submitting" @tap="onSubmit">
        {{ submitting ? '提交中...' : '发布约拍' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, watch } from 'vue'
import MediaUploader from './MediaUploader.vue'
import TagSelector from './TagSelector.vue'
import { YPAT_ROLE_CONFIGS, type YpatTargetType, type YpatRoleConfig, YpatChargeWay, CHARGE_WAY_LABELS } from '@/constants/enums'
import { getWorkTags } from '@/api/modules/dict'
import { resolveWorkTagOptions, WORK_TAG_LIMIT } from '@/constants/work-tags'
import { submit as submitYpat } from '@/api/modules/ypat'
import { useMemberStore } from '@/stores/member'
import { filePathToDataUrl } from '@/utils/file-base64'
import { requestMessageSubscribe } from '@/utils/subscribe-message'
import type { WorkTag } from '@/api/types/work'
import type { MediaItem } from '@/api/types/media'

const props = defineProps<{
  target: YpatTargetType
  workId?: string
  authorId?: string
}>()

const emit = defineEmits<{
  (e: 'submitted'): void
  (e: 'error', msg: string): void
}>()

const config = ref<YpatRoleConfig | null>(YPAT_ROLE_CONFIGS[props.target] || null)
const memberStore = useMemberStore()
const tagOptions = ref<WorkTag[]>([])
const submitting = ref(false)
const mediaItems = ref<MediaItem[]>([])
const minPatdate = formatLocalDate()

const form = reactive({
  describ: '',
  chargeway: YpatChargeWay.FREE as typeof YpatChargeWay[keyof typeof YpatChargeWay],
  chargeamt: '',
  patdate: '',
  patarea: '',
  patslice: '',
  region: null as { province: string; city: string; area: string } | null,
  isNationwide: false,
  creditflag: '0' as '0' | '1',
  selectedTagIds: [] as number[],
  workId: props.workId,
})

watch(() => props.target, (v) => {
  config.value = YPAT_ROLE_CONFIGS[v] || null
}, { immediate: true })

onMounted(async () => {
  try {
    await memberStore.refreshSubmitYpatQuote()
  } catch {
    // 报价失败不阻塞发布，提交接口仍会按后端实扣规则校验。
  }
  try {
    const res = await getWorkTags()
    const data = (res && res.data) || []
    tagOptions.value = resolveWorkTagOptions(data)
  } catch (e) {
    tagOptions.value = resolveWorkTagOptions([])
  }
})

const chargewayText = computed(() => CHARGE_WAY_LABELS[form.chargeway] || '')
const memberQuote = computed(() => memberStore.submitYpatQuote)
const creditText = computed(() => form.creditflag === '1' ? '要求对方存入保证金' : '不要求对方存入保证金')
const regionValue = computed(() => (form.region ? [form.region.province, form.region.city, form.region.area] : []))
const regionText = computed(() => {
  if (form.isNationwide) return '全国'
  if (!form.region) return ''
  return [form.region.province, form.region.city, form.region.area].filter(Boolean).join(' / ')
})

function formatLocalDate(offsetDays = 0): string {
  const date = new Date()
  date.setDate(date.getDate() + offsetDays)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function onPickChargeway() {
  uni.showActionSheet({
    itemList: ['希望互勉', '我要收费', '可付费', '费用协商'],
    success: (res) => {
      const map: any = { 0: '0', 1: '1', 2: '2', 3: '3' }
      form.chargeway = map[res.tapIndex]
    },
  })
}

function changePatdate(event: { detail: { value: string } }): void {
  form.patdate = event.detail.value || ''
}

function changeRegion(event: { detail: { value: string[] } }): void {
  const [province = '', city = '', area = ''] = event.detail.value
  form.region = { province, city, area }
  form.isNationwide = false
}

function changeNationwide(event: any): void {
  form.isNationwide = event.detail.value
  if (form.isNationwide) form.region = null
}

function onPickCredit() {
  uni.showActionSheet({
    itemList: ['不要求对方存入保证金', '要求对方存入保证金'],
    success: (res) => {
      form.creditflag = res.tapIndex === 0 ? '0' : '1'
    },
  })
}

async function onSubmit() {
  if (submitting.value) return
  if (form.describ.length < 5) {
    uni.showToast({ title: '约拍要求至少 5 个字', icon: 'none' })
    return
  }
  if (mediaItems.value.length === 0) {
    uni.showToast({ title: '请上传媒体', icon: 'none' })
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
  if (!form.region && !form.isNationwide) {
    uni.showToast({ title: '请选择面向地区', icon: 'none' })
    return
  }
  await requestMessageSubscribe('publish')
  submitting.value = true
  try {
    const pics: string[] = []
    for (const m of mediaItems.value) {
      if (m.type === 'IMAGE') {
        const dataUrl = await filePathToDataUrl(m.localPath)
        pics.push(dataUrl)
      }
    }
    if (pics.length === 0) {
      uni.showToast({ title: '请上传图片', icon: 'none' })
      submitting.value = false
      return
    }
    const submitData: any = {
      target: props.target,
      describ: form.describ,
      chargeway: form.chargeway,
      chargeamt: form.chargeamt,
      patarea: form.patarea,
      patslice: form.patslice,
      patstyle: form.selectedTagIds.join(','),
      creditflag: '0',
      realnameflag: '0',
      patdate: form.patdate || formatLocalDate(),
      pics,
    }
    if (form.region) {
      submitData.province = form.region.province
      submitData.city = form.region.city
      submitData.area = form.region.area
    }
    if (form.isNationwide) {
      submitData.isNationwide = '1'
    }
    if (props.workId) {
      submitData.workId = props.workId
    }
    await submitYpat(submitData)
    uni.showToast({ title: '发布成功，等待审核', icon: 'success' })
    emit('submitted')
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
.appointment-publish-form {
  padding: 0 32rpx 32rpx;
  background: #FFFFFF;
  min-height: 100vh;
  &__warning {
    background: $color-primary-soft;
    padding: 16rpx 24rpx;
    border-radius: 12rpx;
    margin-bottom: 16rpx;
    display: flex;
    align-items: flex-start;
    gap: 8rpx;
  }
  &__warning-icon {
    font-size: 24rpx;
    color: $color-primary;
    flex-shrink: 0;
  }
  &__warning-text {
    font-size: 26rpx;
    color: $color-primary;
    line-height: 1.4;
  }
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
    background: $color-bg-card;
    border-radius: 16rpx;
    padding: 32rpx;
    margin-bottom: 16rpx;
    min-height: 96rpx;
    &--link {
      justify-content: space-between;
    }
  }
  &__label {
    font-size: 30rpx;
    color: $color-text-primary;
    flex-shrink: 0;
    width: 160rpx;
    &--required::before {
      content: '*';
      color: $color-primary;
      margin-right: 4rpx;
    }
  }
  &__sub {
    font-size: 24rpx;
    color: $color-text-helper;
    margin-left: 8rpx;
  }
  &__inline {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 12rpx;
    justify-content: flex-end;
  }
  &__textarea {
    width: 100%;
    height: 240rpx;
    font-size: 28rpx;
    color: $color-text-primary;
    line-height: 1.6;
  }
  &__input {
    flex: 1;
    font-size: 28rpx;
    color: $color-text-primary;
    text-align: right;
  }
  &__value {
    flex: 1;
    min-width: 0;
    text-align: right;
    font-size: 28rpx;
    color: $color-text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  &__arrow {
    color: $color-text-helper;
    font-size: 32rpx;
  }
  &__hint {
    font-size: 24rpx;
    color: $color-text-helper;
  }
  &__sep {
    color: $color-text-helper;
    font-size: 24rpx;
  }
  &__picker {
    flex: 1;
    min-width: 0;
    height: 56rpx;
    line-height: 56rpx;
  }
  &__region-picker {
    flex: 1;
    min-width: 0;
  }
  &__picker-field {
    width: 100%;
    font-size: 28rpx;
    color: $color-text-primary;
    text-align: right;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    &--placeholder {
      color: $color-text-helper;
    }
  }
  &__nationwide {
    display: flex;
    align-items: center;
    gap: 8rpx;
    flex-shrink: 0;
    color: $color-text-primary;
    font-size: 28rpx;
  }
  &__benefit {
    background: rgba(35, 194, 104, 0.08);
  }
  &__benefit-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: $color-text-secondary;
    font-size: 26rpx;
    line-height: 1.6;
    & + & {
      margin-top: 8rpx;
    }
    &--discount {
      color: $color-primary;
      font-weight: 700;
    }
    &--actual {
      color: $color-text-primary;
      font-size: 30rpx;
      font-weight: 800;
    }
  }
  &__bottom-spacer { height: 200rpx; }
  &__submit {
    position: fixed;
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
