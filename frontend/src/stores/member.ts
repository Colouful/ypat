import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import * as memberApi from '@/api/modules/member'
import type { MemberBenefitQuote, MemberStatus, PpdBenefitScene } from '@/api/types'

/**
 * 会员 store — 维护当前会员状态缓存，承载支付成功后的轮询。
 *
 * 轮询契约：
 *   - 支付成功调起 uni.requestPayment，success 后调 `pollUntilPaid` 每 1.5s 调
 *     /member/order/status，最多 20 次（约 30s）。
 *   - 后端 status=1 (已支付) 时返回 true，前端展示"开通成功"并刷新 status。
 *   - 轮询失败 / 取消 / 超时都不阻塞用户退出 UI；status 缓存通过 refreshStatus 更新。
 */
export const useMemberStore = defineStore('member', () => {
  const status = ref<MemberStatus | null>(null)
  const quotes = ref<Partial<Record<PpdBenefitScene, MemberBenefitQuote>>>({})
  const submitYpatQuote = computed(() => quotes.value.SUBMIT_YPAT ?? null)
  const polling = ref(false)

  async function refreshStatus(): Promise<MemberStatus | null> {
    try {
      const result = await memberApi.getMemberStatus()
      if (result.success && result.data) {
        status.value = result.data
        return result.data
      }
    } catch {
      // 网络/鉴权失败时保留旧值
    }
    return status.value
  }

  async function refreshBenefitQuote(scene: PpdBenefitScene): Promise<MemberBenefitQuote | null> {
    try {
      const result = await memberApi.getMemberBenefitQuote(scene)
      if (result.success && result.data) {
        quotes.value[scene] = result.data
        return result.data
      }
    } catch {
      // 调用方负责展示失败状态并决定是否允许继续操作。
    }
    delete quotes.value[scene]
    return null
  }

  function refreshSubmitYpatQuote(): Promise<MemberBenefitQuote | null> {
    return refreshBenefitQuote('SUBMIT_YPAT')
  }

  async function pollUntilPaid(outTradeNo: string, maxAttempts = 20, intervalMs = 1500): Promise<boolean> {
    polling.value = true
    try {
      for (let i = 0; i < maxAttempts; i++) {
        const result = await memberApi.getMemberOrderStatus(outTradeNo)
        const order = result.data
        if (order && order.status === '1') {
          await refreshStatus()
          return true
        }
        if (order && order.status !== '0') {
          // 已取消 / 已退款 / 已关闭，不继续轮询
          return false
        }
        await new Promise((resolve) => setTimeout(resolve, intervalMs))
      }
      return false
    } finally {
      polling.value = false
    }
  }

  return {
    status,
    quotes,
    submitYpatQuote,
    polling,
    refreshStatus,
    refreshBenefitQuote,
    refreshSubmitYpatQuote,
    pollUntilPaid,
  }
})
