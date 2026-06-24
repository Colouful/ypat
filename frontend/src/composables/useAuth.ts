import { useUserStore } from '@/stores/user'

export function useAuth() {
  const userStore = useUserStore()

  /**
   * Check if user is logged in. If not, redirect to login page.
   * Returns true if logged in, false otherwise.
   */
  function requireLogin(): boolean {
    if (!userStore.isLoggedIn) {
      const currentPages = getCurrentPages()
      const currentPage = currentPages[currentPages.length - 1]
      const currentRoute = currentPage ? `/${currentPage.route}` : ''

      uni.navigateTo({
        url: `/pages/login/index?redirect=${encodeURIComponent(currentRoute)}`,
      })
      return false
    }
    return true
  }

  /**
   * Check if user has completed real-name verification.
   * Requires login first. Returns true if verified.
   */
  function requireRealName(): boolean {
    if (!requireLogin()) return false

    if (!userStore.userInfo || userStore.userInfo.realnameflag !== 1) {
      uni.showModal({
        title: '实名认证',
        content: '该功能需要实名认证后才能使用，是否前往认证？',
        confirmText: '去认证',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({
              url: '/pages/user/realname',
            })
          }
        },
      })
      return false
    }
    return true
  }

  /**
   * Check if user has completed credit verification.
   * Requires login first. Returns true if verified.
   */
  function requireCredit(): boolean {
    if (!requireLogin()) return false

    if (!userStore.userInfo || userStore.userInfo.creditflag !== 1) {
      uni.showModal({
        title: '信用认证',
        content: '该功能需要信用认证后才能使用，是否前往认证？',
        confirmText: '去认证',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({
              url: '/pages/user/credit',
            })
          }
        },
      })
      return false
    }
    return true
  }

  return {
    requireLogin,
    requireRealName,
    requireCredit,
  }
}
