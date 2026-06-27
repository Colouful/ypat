<template>
  <view class="privacy-page">
    <view class="privacy-header">
      <text class="privacy-header__title">隐私政策</text>
      <text class="privacy-header__update">更新日期：2024年1月1日</text>
    </view>

    <view v-if="loading" class="loading-state">
      <view class="loading-spinner" />
      <text class="loading-text">加载中...</text>
    </view>

    <view v-else class="privacy-content">
      <rich-text v-if="content" :nodes="content" />
      <view v-else class="privacy-sections">
        <view class="section">
          <text class="section__title">一、引言</text>
          <text class="section__text">欢迎使用"爱去拍"（以下简称"本平台"）。我们深知个人信息对您的重要性，我们将按照法律法规要求，采取相应的安全保护措施，尽力保护您的个人信息安全可控。本隐私政策适用于您通过本平台提供的所有服务。</text>
        </view>

        <view class="section">
          <text class="section__title">二、我们收集的信息</text>
          <text class="section__text">为了向您提供服务，我们可能会收集以下信息：</text>
          <text class="section__text">1. 账号信息：当您注册账号时，我们会收集您的手机号码、微信授权信息（头像、昵称）。</text>
          <text class="section__text">2. 身份信息：当您进行实名认证时，我们会收集您的姓名、身份证号码、身份证照片，用于身份核实。</text>
          <text class="section__text">3. 位置信息：当您使用约拍服务时，我们可能会获取您的地理位置信息，以便为您推荐同城约拍信息。</text>
          <text class="section__text">4. 设备信息：我们可能会收集您的设备型号、操作系统版本、设备标识符等信息，用于保障服务安全。</text>
          <text class="section__text">5. 使用信息：我们会收集您的浏览记录、搜索记录、发布内容、报名记录等使用数据。</text>
          <text class="section__text">6. 交易信息：当您使用充值服务时，我们会收集您的交易记录和支付信息。</text>
        </view>

        <view class="section">
          <text class="section__title">三、信息的使用</text>
          <text class="section__text">我们收集的信息将用于以下目的：</text>
          <text class="section__text">1. 为您提供约拍信息展示、匹配和沟通服务；</text>
          <text class="section__text">2. 验证您的身份，保障平台交易安全；</text>
          <text class="section__text">3. 根据您的位置推荐同城约拍内容；</text>
          <text class="section__text">4. 改善和优化我们的产品和服务；</text>
          <text class="section__text">5. 预防、发现和调查欺诈、侵权及其他违法行为；</text>
          <text class="section__text">6. 向您发送服务通知和推送消息。</text>
        </view>

        <view class="section">
          <text class="section__title">四、信息的存储</text>
          <text class="section__text">1. 存储地点：您的个人信息存储于中华人民共和国境内的服务器中。</text>
          <text class="section__text">2. 存储期限：我们仅在为实现目的所必需的最短时间内保留您的个人信息。在您注销账号后，我们将在合理期限内删除或匿名化处理您的个人信息。</text>
          <text class="section__text">3. 安全措施：我们采用业界通行的安全技术和管理措施来保护您的信息安全，包括但不限于数据加密传输、服务器加密存储、严格的访问权限控制。</text>
        </view>

        <view class="section">
          <text class="section__title">五、信息的共享</text>
          <text class="section__text">我们不会向第三方出售您的个人信息。但在以下情况下，我们可能会共享您的信息：</text>
          <text class="section__text">1. 经您本人明确同意或授权的；</text>
          <text class="section__text">2. 根据法律法规或政府主管部门的要求；</text>
          <text class="section__text">3. 为完成约拍匹配，向对方用户展示您授权公开的信息；</text>
          <text class="section__text">4. 与我们的关联公司共享，用于提供服务支持；</text>
          <text class="section__text">5. 与授权合作伙伴共享，仅限于为您提供服务所必需的最小范围。</text>
        </view>

        <view class="section">
          <text class="section__title">六、您的权利</text>
          <text class="section__text">根据相关法律法规，您对您的个人信息享有以下权利：</text>
          <text class="section__text">1. 访问权：您有权访问您的个人信息，法律法规规定的例外情况除外；</text>
          <text class="section__text">2. 更正权：当您发现个人信息有误时，您有权要求我们更正；</text>
          <text class="section__text">3. 删除权：在特定情况下，您有权要求我们删除您的个人信息；</text>
          <text class="section__text">4. 撤回同意：您有权撤回之前给予的同意，但不影响撤回前基于同意的处理；</text>
          <text class="section__text">5. 注销账号：您有权注销账号，注销后我们将停止为您提供服务并删除您的个人信息。</text>
        </view>

        <view class="section">
          <text class="section__title">七、未成年人保护</text>
          <text class="section__text">本平台的服务面向年满18周岁的成年人。我们不会故意收集未成年人的个人信息。如您为未成年人的监护人，当您对所监护的未成年人的个人信息有疑问时，请通过本政策中的联系方式与我们联系。</text>
        </view>

        <view class="section">
          <text class="section__title">八、隐私政策的更新</text>
          <text class="section__text">我们可能会适时修改本隐私政策。当隐私政策发生重大变更时，我们会在平台内以通知、弹窗等方式告知您。若您在隐私政策更新后继续使用本平台服务，即表示您已充分阅读并同意更新后的政策。</text>
        </view>

        <view class="section">
          <text class="section__title">九、联系我们</text>
          <text class="section__text">如您对本隐私政策有任何疑问、建议或投诉，请通过以下方式联系我们：</text>
          <text class="section__text">平台名称：爱去拍</text>
          <text class="section__text">联系邮箱：privacy@ypat.com</text>
          <text class="section__text">我们将在收到您的反馈后15个工作日内予以回复。</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as contentApi from '@/api/modules/content'

const loading = ref(false)
const content = ref('')

async function loadContent() {
  loading.value = true
  try {
    const res = await contentApi.getArticleDetail(2)
    if (res.success && res.data?.content) {
      content.value = res.data.content
    }
  } catch {
    // Use static content as fallback
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadContent()
})
</script>

<style lang="scss">

.privacy-page {
  min-height: 100vh;
  background-color: $color-bg-card;
  padding: $spacing-xl;
  padding-bottom: calc($spacing-xxl + env(safe-area-inset-bottom));
}

.privacy-header {
  margin-bottom: $spacing-xl;

  &__title {
    display: block;
    font-size: $font-size-xxl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin-bottom: $spacing-sm;
  }

  &__update {
    display: block;
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 200rpx 0;
}

.loading-spinner {
  width: 64rpx;
  height: 64rpx;
  border: 4rpx solid $color-border;
  border-top-color: $color-primary;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: $spacing-md;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: $font-size-sm;
  color: $color-text-helper;
}

.privacy-content {
  line-height: 1.8;
}

.privacy-sections {
  display: flex;
  flex-direction: column;
  gap: $spacing-xl;
}

.section {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;

  &__title {
    display: block;
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin-bottom: $spacing-xs;
  }

  &__text {
    display: block;
    font-size: $font-size-base;
    color: $color-text-secondary;
    line-height: 1.8;
  }
}
</style>
