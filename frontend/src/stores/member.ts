import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as memberApi from '@/api/modules/member'
import type { MemberBenefitQuote, MemberStatus } from '@/api/types'

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
  const submitYpatQuote = ref<MemberBenefitQuote | null>(null)
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

  async function refreshSubmitYpatQuote(): Promise<MemberBenefitQuote | null> {
    try {
      const result = await memberApi.getMemberBenefitQuote('SUBMIT_YPAT')
      if (result.success && result.data) {
        submitYpatQuote.value = result.data
        return result.data
      }
    } catch {
      // 报价失败不影响发布表单，调用方按原价兜底。
    }
    submitYpatQuote.value = null
    return null
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

  return { status, submitYpatQuote, polling, refreshStatus, refreshSubmitYpatQuote, pollUntilPaid }
})
