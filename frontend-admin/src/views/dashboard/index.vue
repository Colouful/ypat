<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/modules/auth'
import { menuConfig } from '@/constants/menu'

const router = useRouter()
const authStore = useAuthStore()

function goTo(path: string): void {
  router.push(path)
}
</script>

<template>
  <div class="dashboard">
    <div class="welcome">
      <h2>欢迎回来，{{ authStore.adminInfo?.name || '管理员' }}</h2>
      <p>请选择要进入的管理系统</p>
    </div>

    <div class="system-cards">
      <div
        v-for="group in menuConfig"
        :key="group.title"
        class="system-card"
        @click="goTo(group.children[0].path)"
      >
        <div class="card-icon">
          <el-icon :size="40">
            <component :is="group.icon" />
          </el-icon>
        </div>
        <h3 class="card-title">{{ group.title }}</h3>
        <p class="card-desc">{{ group.children.length }} 个功能模块</p>
        <div class="card-modules">
          <el-tag
            v-for="item in group.children.slice(0, 3)"
            :key="item.path"
            size="small"
            type="info"
            effect="plain"
          >
            {{ item.title }}
          </el-tag>
          <el-tag v-if="group.children.length > 3" size="small" type="info" effect="plain">
            +{{ group.children.length - 3 }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome {
  margin-bottom: $spacing-xl;

  h2 {
    font-size: $font-size-xl;
    color: $text-primary;
    margin-bottom: $spacing-xs;
  }

  p {
    color: $text-secondary;
  }
}

.system-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: $spacing-lg;
}

.system-card {
  background-color: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-xl;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid $border-lighter;
  box-shadow: $shadow-light;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-dark;
    border-color: $color-primary;
  }
}

.card-icon {
  width: 64px;
  height: 64px;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, $color-primary, $color-primary-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: $spacing-lg;
}

.card-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.card-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: $spacing-base;
}

.card-modules {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-xs;
}
</style>
