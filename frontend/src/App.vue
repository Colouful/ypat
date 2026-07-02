<script setup lang="ts">
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { hideNavigationLoading, showNavigationLoading } from '@/utils/tab-navigation'

let navigationInterceptorsReady = false

function setupNavigationInterceptors(): void {
  if (navigationInterceptorsReady) return
  navigationInterceptorsReady = true
  const interceptor = {
    invoke() {
      showNavigationLoading()
    },
    complete() {
      hideNavigationLoading()
    },
  }
  uni.addInterceptor('navigateTo', interceptor)
  uni.addInterceptor('redirectTo', interceptor)
  uni.addInterceptor('reLaunch', interceptor)
  uni.addInterceptor('navigateBack', interceptor)
}

onLaunch(() => {
  // 同步设置拦截器（轻量，必须在第一次跳转前完成）
  setupNavigationInterceptors()

  // 异步初始化 store，不阻塞首屏渲染
  setTimeout(() => {
    try {
      const appStore = useAppStore()
      appStore.initApp()
    } catch (e) {
      console.error('[App] initApp failed:', e)
    }
  }, 0)

  // 用户会话恢复（包含 token 解析），用 try-catch 隔离
  // 失败也不阻塞主流程
  setTimeout(() => {
    try {
      const userStore = useUserStore()
      userStore.restoreSession()
    } catch (e) {
      console.error('[App] restoreSession failed:', e)
    }
  }, 0)
})

onShow(() => {
  // 用 setTimeout 异步化，避免阻塞主线程
  setTimeout(() => {
    try {
      const userStore = useUserStore()
      if (userStore.isLoggedIn) {
        userStore.refreshUnreadCount()
      }
    } catch (e) {
      console.error('[App] refreshUnreadCount failed:', e)
    }
  }, 0)
})
</script>

<style lang="scss">
@import '@/styles/global.scss';
</style>
