<script setup lang="ts">
import { useRoute } from 'vue-router'
import { menuConfig } from '@/constants/menu'

defineProps<{
  collapsed: boolean
}>()

const route = useRoute()

function isActive(path: string): boolean {
  return route.path === path
}
</script>

<template>
  <aside class="sidebar" :class="{ collapsed }">
    <div class="logo">
      <span v-if="!collapsed" class="logo-text">YPAT 管理后台</span>
      <span v-else class="logo-text-mini">Y</span>
    </div>
    <el-scrollbar class="menu-scroll">
      <el-menu
        :default-active="route.path"
        :collapse="collapsed"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#ffffff"
        router
      >
        <template v-for="group in menuConfig" :key="group.title">
          <el-sub-menu :index="group.title">
            <template #title>
              <el-icon>
                <component :is="group.icon" />
              </el-icon>
              <span>{{ group.title }}</span>
            </template>
            <el-menu-item
              v-for="item in group.children"
              :key="item.path"
              :index="item.path"
              :class="{ 'is-active': isActive(item.path) }"
            >
              <span>{{ item.title }}</span>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-scrollbar>
  </aside>
</template>

<style scoped lang="scss">
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: $layout-sidebar-width;
  background-color: $bg-sidebar;
  z-index: 1001;
  transition: width 0.3s ease;
  overflow: hidden;

  &.collapsed {
    width: $layout-sidebar-collapsed-width;
  }
}

.logo {
  height: $layout-header-height;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: darken($bg-sidebar, 5%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.logo-text {
  color: #fff;
  font-size: $font-size-lg;
  font-weight: 600;
  white-space: nowrap;
}

.logo-text-mini {
  color: #fff;
  font-size: $font-size-xl;
  font-weight: 700;
}

.menu-scroll {
  height: calc(100vh - #{$layout-header-height});

  :deep(.el-menu) {
    border-right: none;
  }
}
</style>
