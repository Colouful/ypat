import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPersistedstate from 'pinia-plugin-persistedstate'
import App from './App.vue'
import KeepPageNav from './components/business/KeepPageNav.vue'

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  pinia.use(piniaPersistedstate)
  app.component('KeepPageNav', KeepPageNav)
  app.use(pinia)
  return { app }
}
