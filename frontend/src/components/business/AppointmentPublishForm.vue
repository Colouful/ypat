<template>
  <view class="appointment-publish-form">
    <!-- 红色风险条 -->
    <view v-if="config" class="appointment-publish-form__warning">
      <text class="appointment-publish-form__warning-icon">📢</text>
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
        <picker mode="datetime" :value="form.startTime" @change="(e: any) => form.startTime = e.detail.value" class="appointment-publish-form__picker">
          <text class="appointment-publish-form__picker-text">{{ form.startTime || '你期望的时间' }}</text>
        </picker>
        <text class="appointment-publish-form__sep">至</text>
        <picker mode="datetime" :value="form.endTime" @change="(e: any) => form.endTime = e.detail.value" class="appointment-publish-form__picker">
          <text class="appointment-publish-form__picker-text">{{ form.endTime || '' }}</text>
        </picker>
        <text class="appointment-publish-form__hint">（选填）</text>
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
    <view class="appointment-publish-form__field appointment-publish-form__field--link" @tap="onPickRegion">
      <text class="appointment-publish-form__label appointment-publish-form__label--required">面向地区</text>
      <view class="appointment-publish-form__inline">
        <text class="appointment-publish-form__value">{{ regionText || '请选择面向地区' }}</text>
        <text v-if="config && config.allowNationwide" class="appointment-publish-form__nationwide">
          <text>全国</text>
          <switch :checked="form.isNationwide" @change="(e: any) => { form.isNationwide = e.detail.value; if (e.detail.value) form.region = null }" color="#FF4D5E" />
        </text>
        <text class="appointment-publish-form__arrow">›</text>
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

    <view class="appointment-publish-form__bottom-spacer" />
    <view class="appointment-publish-form__submit">
      <button class="appointment-publish-form__btn" :disabled="submitting" @tap="onSubmit">
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
import { WORK_TAGS_FALLBACK } from '@/constants/work-tags'
import { submit as submitYpat } from '@/api/modules/ypat'
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
const tagOptions = ref<WorkTag[]>([])
const submitting = ref(false)
const mediaItems = ref<MediaItem[]>([])

const form = reactive({
  describ: '',
  chargeway: YpatChargeWay.FREE as typeof YpatChargeWay[keyof typeof YpatChargeWay],
  chargeamt: '',
  startTime: '',
  endTime: '',
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
    const res = await getWorkTags()
    const data = (res && res.data) || []
    tagOptions.value = data || []
  } catch (e) {
    tagOptions.value = WORK_TAGS_FALLBACK.map((name, idx) => ({ id: idx, code: `fb_${idx}`, name }))
  }
})

const chargewayText = computed(() => CHARGE_WAY_LABELS[form.chargeway] || '')
const creditText = computed(() => form.creditflag === '1' ? '要求对方存入保证金' : '不要求对方存入保证金')
const regionText = computed(() => {
  if (form.isNationwide) return '全国'
  if (!form.region) return ''
  return [form.region.province, form.region.city, form.region.area].filter(Boolean).join(' / ')
})

function onPickChargeway() {
  uni.showActionSheet({
    itemList: ['免费互拍', '收费拍摄', '可付费', '费用面议'],
    success: (res) => {
      const map: any = { 0: '0', 1: '1', 2: '2', 3: '3' }
      form.chargeway = map[res.tapIndex]
    },
  })
}

function onPickRegion() {
  if (form.isNationwide) return
  uni.showActionSheet({
    itemList: ['北京市 / 北京市 / 东城区', '上海市 / 上海市 / 黄浦区', '广州市 / 广东省 / 天河区', '深圳市 / 广东省 / 南山区', '杭州市 / 浙江省 / 西湖区'],
    success: (res) => {
      const map: any = {
        0: { province: '北京市', city: '北京市', area: '东城区' },
        1: { province: '上海市', city: '上海市', area: '黄浦区' },
        2: { province: '广东省', city: '广州市', area: '天河区' },
        3: { province: '广东省', city: '深圳市', area: '南山区' },
        4: { province: '浙江省', city: '杭州市', area: '西湖区' },
      }
      form.region = map[res.tapIndex]
    },
  })
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
  if (!form.region && !form.isNationwide) {
    uni.showToast({ title: '请选择面向地区', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    const { filePathToDataUrl } = await import('@/utils/file-base64')
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
      patdate: form.startTime || new Date().toISOString().slice(0, 10),
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
    display: flex;
    align-items: center;
    gap: 12rpx;
    flex-wrap: wrap;
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
    text-align: right;
    font-size: 28rpx;
    color: $color-text-primary;
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
    height: 56rpx;
    line-height: 56rpx;
  }
  &__picker-text {
    font-size: 28rpx;
    color: $color-text-primary;
  }
  &__nationwide {
    display: flex;
    align-items: center;
    gap: 8rpx;
    color: $color-text-primary;
    font-size: 28rpx;
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
    background: linear-gradient(90deg, #FF8FA0 0%, #FF6B7E 100%);
    color: #FFFFFF;
    font-size: 30rpx;
    font-weight: 600;
    border-radius: 999rpx;
    border: none;
    box-shadow: 0 12rpx 24rpx rgba(255, 77, 94, 0.28);
    &::after { border: none; }
    &[disabled] {
      background: $color-text-helper;
      color: #FFFFFF;
      box-shadow: none;
    }
  }
}
</style>
