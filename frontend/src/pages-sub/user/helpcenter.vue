<template>
  <view class="help-page">
    <KeepPageNav title="帮助中心" />
    <view class="help-head">
      <text class="help-head__title">常见问题</text>
      <text class="help-head__desc">围绕发布、审核、拍拍豆和消息通知整理的使用说明。</text>
    </view>

    <view class="faq-card">
      <view
        v-for="(item, index) in faqs"
        :key="item.title"
        class="faq-item"
        @tap="toggle(index)"
      >
        <view class="faq-item__title">
          <text>{{ item.title }}</text>
          <KeepIcon name="chevron-right" :size="34" color="#B3B8BE" />
        </view>
        <view v-if="current === index" class="faq-item__content">
          <text v-for="line in item.desc" :key="line">{{ line }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import KeepIcon from '@/components/business/KeepIcon.vue'

type FaqItem = {
  title: string
  desc: string[]
}

const current = ref(0)
const faqs: FaqItem[] = [
  { title: '爱去拍是什么？', desc: ['爱去拍是个人与摄影师的摄影约拍平台。'] },
  { title: '发布约拍信息时，图片为什么上传不上去？', desc: ['可能是图片过大导致，请尽量选择单张 3M 以内、总数 6 张以内的图片。'] },
  { title: '为什么我发布的信息会审核失败？', desc: ['图片过于模糊不清晰。', '约拍内容不宜传播，或存在虚假、错误信息。', '账号被实名举报后，平台可能限制发布。'] },
  { title: '平台拍拍豆是做什么的？', desc: ['拍拍豆可用于发布约拍、查看联系方式、向其他人发起约拍申请。'] },
  { title: '如何获取拍拍豆？', desc: ['可通过平台购买。', '好友邀请奖励能力仍在新版迁移中，具体以页面上线后的真实规则为准。'] },
  { title: '订阅消息是干什么的？', desc: ['订阅消息用于接收约拍申请、审核结果等通知。允许通知后，可以更及时处理约拍动态。'] },
  { title: '怎么向其他人发起约拍？', desc: ['在约拍详情页发起申请后，平台会按规则展示双方预留的联系方式。查看联系方式和发起申请可能消耗拍拍豆，实际扣费以服务端为准。'] },
  { title: '怎么提高约拍成功率？', desc: ['完善个人资料、实名认证和信用信息。', '发布时描述真实清晰，补充高质量样片和明确城市、日期、风格。', '保持活跃，及时处理收到的约拍消息。'] },
]

function toggle(index: number): void {
  current.value = current.value === index ? -1 : index
}
</script>

<style scoped lang="scss">

.help-page {
  min-height: 100vh;
  padding: 28rpx;
  background: $color-bg-page;
}

.help-head {
  margin-bottom: 24rpx;
  padding: 34rpx;
  border-radius: $radius-keep-card;
  color: #fff;
  background: linear-gradient(135deg, #1F242B, #30443A);
}

.help-head__title,
.help-head__desc {
  display: block;
}

.help-head__title {
  font-size: 42rpx;
  font-weight: 900;
}

.help-head__desc {
  margin-top: 12rpx;
  color: rgba(255, 255, 255, 0.76);
  font-size: 26rpx;
  font-weight: 700;
  line-height: 1.55;
}

.faq-card {
  overflow: hidden;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.faq-item {
  border-bottom: 1rpx solid $color-border;
}

.faq-item:last-child {
  border-bottom: 0;
}

.faq-item__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 30rpx;
}

.faq-item__title text {
  color: $color-text-primary;
  font-size: 29rpx;
  font-weight: 900;
  line-height: 1.45;
}

.faq-item__content {
  padding: 0 30rpx 30rpx;
}

.faq-item__content text {
  display: block;
  margin-top: 12rpx;
  color: $color-text-secondary;
  font-size: 26rpx;
  font-weight: 700;
  line-height: 1.65;
}
</style>
