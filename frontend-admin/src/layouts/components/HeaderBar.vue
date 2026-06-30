<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAppStore } from '@/stores/modules/app'
import { useAuthStore } from '@/stores/modules/auth'
import { usePermissionStore } from '@/stores/modules/permission'
import { useTabsStore } from '@/stores/modules/tabs'

const appStore = useAppStore()
const authStore = useAuthStore()
const permissionStore = usePermissionStore()
const tabsStore = useTabsStore()
const router = useRouter()

async function handleLogout(): Promise<void> {
  try {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await authStore.logout()
    permissionStore.resetRoutes()
    tabsStore.reset()
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<template>
  <header class="header-bar">
    <div class="header-left">
      <el-icon class="collapse-btn" @click="appStore.toggleSidebar()">
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
    </div>
    <div class="header-right">
      <el-dropdown trigger="click">
        <div class="admin-info">
          <el-avatar :size="32" class="avatar">
            {{ authStore.adminInfo?.name?.charAt(0) || 'A' }}
          </el-avatar>
          <span class="admin-name">
            {{ authStore.adminInfo?.name || authStore.adminInfo?.mobile || '管理员' }}
          </span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped lang="scss">
.header-bar {
  height: $layout-header-height;
  background-color: $bg-card;
  border-bottom: 1px solid $border-lighter;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-lg;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: $text-regular;
  transition: color 0.2s;

  &:hover {
    color: $color-primary;
  }
}

.admin-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: $spacing-sm;
}

.avatar {
  background-color: $color-primary;
  color: #fff;
}

.admin-name {
  font-size: $font-size-base;
  color: $text-primary;
}
</style>
