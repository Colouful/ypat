<template>
  <view class="recharge-page">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">充值PPD</text>
      <text class="balance-label">当前余额: <text class="balance-value">{{ userStore.userInfo?.ppd || '0' }} PPD</text></text>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-wrapper">
      <view class="loading-spinner" />
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 商品列表 -->
    <view v-else class="product-grid">
      <view
        v-for="product in productList"
        :key="product.id"
        class="product-card"
        :class="{ 'product-card--selected': selectedProductId === product.id }"
        @tap="selectProduct(product)"
      >
        <view v-if="selectedProductId === product.id" class="check-icon">
          <text class="check-mark">&#10003;</text>
        </view>
        <text class="product-ppd">{{ product.credit }} PPD</text>
        <text class="product-price">&#165;{{ product.price.toFixed(2) }}</text>
        <text
          v-if="product.originalPrice > product.price"
          class="product-original-price"
        >&#165;{{ product.originalPrice.toFixed(2) }}</text>
      </view>
    </view>

    <!-- 底部支付按钮 -->
    <view class="pay-footer">
      <view class="pay-footer-safe">
        <button
          class="pay-btn"
          :disabled="!selectedProduct || paying"
          :loading="paying"
          @tap="handlePay"
        >
          {{ paying ? '支付中...' : `立即支付 ¥${selectedProduct ? selectedProduct.price.toFixed(2) : '0.00'}` }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as paymentApi from '@/api/modules/payment'

interface Product {
  id: number
  name: string
  description: string
  price: number
  originalPrice: number
  type: number
  credit: number
  status: number
  sort: number
  createTime: string
}

const userStore = useUserStore()

const loading = ref(true)
const paying = ref(false)
const productList = ref<Product[]>([])
const selectedProductId = ref<number | null>(null)

const selectedProduct = computed(() => {
  return productList.value.find((p) => p.id === selectedProductId.value) || null
})

/** 选择商品 */
function selectProduct(product: Product) {
  selectedProductId.value = product.id
}

/** 获取商品列表 */
async function fetchProducts() {
  loading.value = true
  try {
    const res = await paymentApi.getProductList({ page: 1, size: 20, status: 1 })
    if (res.data?.content) {
      productList.value = res.data.content
      // 默认选中第一个商品
      if (productList.value.length > 0) {
        selectedProductId.value = productList.value[0].id
      }
    }
  } catch (e) {
    uni.showToast({ title: '获取商品列表失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

/** 支付 */
async function handlePay() {
  if (!selectedProduct.value || paying.value) return

  if (!userStore.userInfo?.id) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  paying.value = true
  try {
    const res = await paymentApi.createOrder({
      userId: Number(userStore.userInfo.id),
      productId: selectedProduct.value.id,
      payType: 0,
    })

    if (res.data?.payParams) {
      // 调用微信支付
      await uni.requestPayment({
        provider: 'wxpay',
        timeStamp: res.data.payParams.timeStamp,
        nonceStr: res.data.payParams.nonceStr,
        package: res.data.payParams.package,
        signType: res.data.payParams.signType || 'MD5',
        paySign: res.data.payParams.paySign,
      })

      // 支付成功
      uni.showToast({ title: '支付成功', icon: 'success' })

      // 刷新用户余额
      const ppdNum = Number(userStore.userInfo.ppd || 0) + selectedProduct.value.credit
      userStore.updateUserInfo({ ppd: String(ppdNum) })

      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      uni.showToast({ title: '创建订单失败', icon: 'none' })
    }
  } catch (e: any) {
    if (e?.errMsg?.includes('cancel')) {
      uni.showToast({ title: '已取消支付', icon: 'none' })
    } else {
      uni.showToast({ title: '支付失败，请重试', icon: 'none' })
    }
  } finally {
    paying.value = false
  }
}

onLoad(() => {
  fetchProducts()
})
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.recharge-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-xl;
  padding-bottom: 180rpx;
}

.page-header {
  margin-bottom: $spacing-xl;

  .page-title {
    display: block;
    font-size: $font-size-xxl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin-bottom: $spacing-sm;
  }

  .balance-label {
    font-size: $font-size-base;
    color: $color-text-secondary;
  }

  .balance-value {
    color: $color-accent-orange;
    font-weight: $font-weight-semibold;
  }
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;

  .loading-spinner {
    width: 48rpx;
    height: 48rpx;
    border: 4rpx solid $color-border;
    border-top-color: $color-primary;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  .loading-text {
    margin-top: $spacing-md;
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-lg;
}

.product-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-xl $spacing-md;
  box-shadow: $shadow-sm;
  border: 4rpx solid transparent;
  transition: all $duration-fast ease;

  &--selected {
    border-color: $color-primary;
    background-color: rgba(35, 194, 104, 0.04);
  }

  .check-icon {
    position: absolute;
    top: $spacing-sm;
    right: $spacing-sm;
    width: 40rpx;
    height: 40rpx;
    background-color: $color-primary;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

    .check-mark {
      font-size: $font-size-xs;
      color: #ffffff;
      font-weight: $font-weight-bold;
    }
  }

  .product-ppd {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $color-accent-orange;
    margin-bottom: $spacing-sm;
  }

  .product-price {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin-bottom: $spacing-xs;
  }

  .product-original-price {
    font-size: $font-size-sm;
    color: $color-text-helper;
    text-decoration: line-through;
  }
}

.pay-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: $color-bg-card;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.06);
  z-index: $z-index-navbar;

  .pay-footer-safe {
    padding: $spacing-lg $spacing-xl;
    padding-bottom: calc(#{$spacing-lg} + #{$safe-area-inset-bottom});
  }
}

.pay-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background-color: $color-primary;
  color: #ffffff;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  border-radius: $radius-round;
  text-align: center;
  border: none;

  &[disabled] {
    background-color: $color-primary-light;
    color: rgba(255, 255, 255, 0.7);
  }

  &::after {
    border: none;
  }
}
</style>
