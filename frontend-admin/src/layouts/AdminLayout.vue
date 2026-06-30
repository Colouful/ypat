<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores/modules/app'
import Sidebar from './components/Sidebar.vue'
import HeaderBar from './components/HeaderBar.vue'
import Breadcrumb from './components/Breadcrumb.vue'
import TabsBar from './components/TabsBar.vue'

const appStore = useAppStore()

const sidebarWidth = computed(() =>
  appStore.sidebarCollapsed ? '64px' : '210px',
)
</script>

<template>
  <div class="admin-layout">
    <Sidebar :collapsed="appStore.sidebarCollapsed" />
    <div class="main-container" :style="{ marginLeft: sidebarWidth }">
      <HeaderBar />
      <Breadcrumb />
      <TabsBar />
      <div class="app-main">
        <RouterView v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </RouterView>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.admin-layout {
  display: flex;
  min-height: 100vh;
  background-color: $bg-page;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.3s ease;
  overflow: hidden;
}

.app-main {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-lg;
}
</style>
