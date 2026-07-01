<script setup lang="ts">
import { watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTabsStore } from '@/stores/modules/tabs'

const route = useRoute()
const router = useRouter()
const tabsStore = useTabsStore()

// 监听路由变化添加标签
watch(
  () => route.path,
  () => {
    tabsStore.addTab(route)
  },
  { immediate: true },
)

function handleClick(path: string): void {
  router.push(path)
}

function handleClose(path: string): void {
  const next = tabsStore.closeTab(path)
  if (next) {
    router.push(next.path)
  } else if (tabsStore.tabs.length === 0) {
    router.push('/dashboard')
  }
}
</script>

<template>
  <div class="tabs-bar">
    <el-scrollbar>
      <div class="tabs-wrapper">
        <div
          v-for="tab in tabsStore.tabs"
          :key="tab.path"
          class="tab-item"
          :class="{ active: tabsStore.activeTab === tab.path }"
          @click="handleClick(tab.path)"
        >
          <span class="tab-title">{{ tab.title }}</span>
          <el-icon
            v-if="!tab.affix"
            class="tab-close"
            @click.stop="handleClose(tab.path)"
          >
            <Close />
          </el-icon>
        </div>
      </div>
    </el-scrollbar>
  </div>
</template>

<style scoped lang="scss">
.tabs-bar {
  height: $layout-tabs-height;
  background-color: $bg-card;
  border-bottom: 1px solid $border-lighter;
  display: flex;
  align-items: center;
}

.tabs-wrapper {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  padding: 0 $spacing-lg;
  white-space: nowrap;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  gap: $spacing-xs;
  height: 26px;
  padding: 0 $spacing-sm;
  border-radius: $radius-sm;
  font-size: $font-size-sm;
  color: $text-regular;
  background-color: $bg-page;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    color: $color-primary;
  }

  &.active {
    background-color: $color-primary;
    color: #fff;
  }
}

.tab-close {
  font-size: 12px;
  border-radius: 50%;

  &:hover {
    background-color: rgb(0 0 0 / 15%);
  }
}
</style>
