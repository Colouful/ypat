<template>
  <view class="agreement-page">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner" />
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 协议内容 -->
    <view v-else class="agreement-content">
      <text class="agreement-title">用户协议</text>
      <text class="agreement-update">更新日期：2024年1月1日</text>

      <view class="content-body">
        <rich-text :nodes="contentHtml" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import * as contentApi from '@/api/modules/content'

const loading = ref(true)
const contentHtml = ref('')

// 固定协议文章ID
const AGREEMENT_ARTICLE_ID = 1

const staticContent = `
<h2>一、总则</h2>
<p>欢迎您使用YPAT约拍平台（以下简称"本平台"）。本用户协议（以下简称"本协议"）是您与本平台之间就注册、使用本平台服务所订立的协议。请您在注册或使用本平台服务前，仔细阅读本协议全部条款。一旦您注册、登录、使用或以任何方式访问本平台，即表示您已充分阅读、理解并同意接受本协议的全部内容。</p>
<p>本平台为用户提供摄影约拍信息发布、浏览、沟通等服务。本平台仅作为信息中介平台，不直接参与用户之间的约拍活动。</p>

<h2>二、用户注册与账户管理</h2>
<p>1. 您在注册时应提供真实、准确、完整的个人资料，并在资料变更时及时更新。因提供虚假信息所产生的一切后果由您自行承担。</p>
<p>2. 您的账户仅限您本人使用，不得以任何形式转让、借用或出售给他人。因账户保管不当造成的损失由您自行承担。</p>
<p>3. 本平台有权对您提交的注册信息进行审核，如发现信息不实或违反本协议规定，本平台有权拒绝注册或暂停、终止您的账户。</p>
<p>4. 您应妥善保管账户密码，因密码泄露导致的账户被盗用等损失由您自行承担。如发现账户异常，请立即联系平台客服。</p>

<h2>三、服务内容与使用规则</h2>
<p>1. 本平台提供的服务包括但不限于：约拍信息发布与浏览、摄影师/模特个人主页展示、站内消息通讯、PPD虚拟积分系统等。</p>
<p>2. 您在使用本平台服务时，应遵守中华人民共和国相关法律法规，不得利用本平台从事任何违法违规活动。</p>
<p>3. 您发布的约拍信息应真实、合法，不得包含虚假信息、色情低俗内容、暴力恐怖内容或其他违反法律法规及社会公德的内容。</p>
<p>4. 您不得通过本平台发布任何形式的广告、推销信息，除非获得本平台的书面许可。</p>
<p>5. 查看其他用户的联系方式需消耗PPD积分，PPD可通过平台充值获得。已消耗的PPD不予退还。</p>

<h2>四、用户行为规范</h2>
<p>1. 您应尊重其他用户的合法权益，不得骚扰、辱骂、威胁其他用户。</p>
<p>2. 您在约拍过程中应注意人身安全，建议选择公共场所进行初次见面，并告知亲友您的行程。</p>
<p>3. 您不得利用本平台进行任何非法交易，包括但不限于色情服务交易、毒品交易等。</p>
<p>4. 您在拍摄活动中应尊重被拍摄者的肖像权、隐私权，未经同意不得将拍摄作品用于商业用途或在公开场合传播。</p>
<p>5. 您不得使用任何技术手段干扰本平台的正常运行，包括但不限于恶意攻击服务器、篡改数据、使用爬虫等自动化工具。</p>

<h2>五、知识产权</h2>
<p>1. 本平台的商标、标识、界面设计、程序代码等知识产权归本平台所有，未经许可不得使用。</p>
<p>2. 您在本平台发布的原创内容（如作品集图片、个人简介等），其知识产权仍归您所有，但您授予本平台在平台服务范围内免费使用的许可。</p>
<p>3. 如您发现本平台存在侵犯您知识产权的内容，请及时联系本平台进行投诉处理。</p>

<h2>六、PPD积分与付费服务</h2>
<p>1. PPD为本平台虚拟积分，可用于查看联系方式等付费功能。PPD不可转让、不可提现、不可兑换为现金。</p>
<p>2. PPD的充值价格以平台页面显示为准。充值成功后不支持退款，法律规定的情形除外。</p>
<p>3. 本平台有权根据运营需要调整PPD的使用规则和价格，调整前将在平台进行公告。</p>

<h2>七、免责声明</h2>
<p>1. 本平台仅提供信息展示和沟通渠道，不对用户之间的约拍活动承担任何责任。用户之间因约拍产生的纠纷应自行协商解决。</p>
<p>2. 本平台不对用户发布信息的真实性、合法性作出保证。您在与其他用户交互时应自行判断信息的可靠性。</p>
<p>3. 因不可抗力、系统维护、网络故障等原因导致服务中断或数据丢失，本平台不承担赔偿责任。</p>
<p>4. 对于用户在约拍过程中发生的人身伤害、财产损失等，本平台不承担赔偿责任。</p>

<h2>八、协议的变更与终止</h2>
<p>1. 本平台有权根据需要修改本协议内容，修改后的协议将在平台公告，自公告之日起生效。</p>
<p>2. 您继续使用本平台服务即视为同意修改后的协议。如您不同意修改后的协议，应立即停止使用本平台服务。</p>
<p>3. 您有权随时注销账户。账户注销后，您的个人信息将按照隐私政策进行处理。</p>
<p>4. 如您严重违反本协议规定，本平台有权立即终止向您提供服务，并保留追究法律责任的权利。</p>

<h2>九、争议解决</h2>
<p>1. 本协议的签订、履行、解释均适用中华人民共和国法律。</p>
<p>2. 因本协议或使用本平台服务产生的争议，双方应友好协商解决；协商不成的，任何一方有权向本平台所在地有管辖权的人民法院提起诉讼。</p>

<h2>十、联系方式</h2>
<p>如您对本协议有任何疑问或建议，请通过本平台"意见反馈"功能或客服渠道与我们联系。</p>
`

onLoad(() => {
  uni.setNavigationBarTitle({ title: '用户协议' })
  loadContent()
})

async function loadContent() {
  loading.value = true
  try {
    const res = await contentApi.getArticleDetail(AGREEMENT_ARTICLE_ID)
    if (res.data && res.data.content) {
      contentHtml.value = res.data.content
    } else {
      contentHtml.value = staticContent
    }
  } catch {
    contentHtml.value = staticContent
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.agreement-page {
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

.agreement-content {
  padding: $spacing-xl;

  .agreement-title {
    display: block;
    font-size: $font-size-xxl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    text-align: center;
    margin-bottom: $spacing-md;
  }

  .agreement-update {
    display: block;
    font-size: $font-size-sm;
    color: $color-text-helper;
    text-align: center;
    margin-bottom: $spacing-xl;
  }

  .content-body {
    :deep(h2) {
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
      margin-top: $spacing-xl;
      margin-bottom: $spacing-md;
      line-height: 1.5;
    }

    :deep(p) {
      font-size: $font-size-base;
      color: $color-text-primary;
      line-height: 1.8;
      margin-bottom: $spacing-md;
      text-align: justify;
    }
  }
}
</style>
