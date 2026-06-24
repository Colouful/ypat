import { useUserStore } from '@/stores/user'

export function useAuth() {
  const userStore = useUserStore()

  function requireLogin(): boolean {
    if (userStore.isLoggedIn) return true
    const pages = getCurrentPages()
    const current = pages[pages.length - 1]
    const route = current?.route ? `/${current.route}` : ''
    uni.navigateTo({ url: `/pages/login/index?redirect=${encodeURIComponent(route)}` })
    return false
  }

  function requireRealName(): boolean {
    if (!requireLogin()) return false
    if (userStore.userInfo?.realnameflag === '1') return true

    uni.showModal({
      title: '需要实名认证',
      content: '完成实名认证后才能继续，是否前往认证？',
      confirmText: '去认证',
      success: ({ confirm }) => {
        if (confirm) uni.navigateTo({ url: '/pages-sub/user/realname' })
      },
    })
    return false
  }

  function requireCredit(): boolean {
    if (!requireLogin()) return false
    if (userStore.userInfo?.creditflag === '1') return true

    uni.showModal({
      title: '需要信用保证',
      content: '当前功能要求已缴纳保证金，请先完成信用保证。',
      showCancel: false,
    })
    return false
  }

  return { requireLogin, requireRealName, requireCredit }
}
