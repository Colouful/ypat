<script setup lang="ts">
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

onLaunch(() => {
  const userStore = useUserStore()
  const appStore = useAppStore()
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
