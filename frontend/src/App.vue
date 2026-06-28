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
  const userStore = useUserStore()
  const appStore = useAppStore()
  setupNavigationInterceptors()
  userStore.restoreSession()
  appStore.initApp()
})

onShow(() => {
  const userStore = useUserStore()
  if (userStore.isLoggedIn) {
    userStore.refreshUnreadCount()
  }
})
</script>

<style lang="scss">
@import '@/styles/global.scss';
</style>
