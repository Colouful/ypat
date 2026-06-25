<template>
  <view class="keep-icon" :style="iconStyle" aria-hidden="true">
    <text class="keep-icon__glyph">{{ glyph }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  name: string
  size?: number | string
  color?: string
  strokeWidth?: number
}>(), {
  size: 24,
  color: 'currentColor',
  strokeWidth: 2,
})

const glyphMap: Record<string, string> = {
  home: '⌂',
  compass: '◈',
  'plus-circle': '+',
  mail: '✉',
  user: '♙',
  camera: '▣',
  'map-pin': '⌖',
  search: '⌕',
  sliders: '≡',
  star: '☆',
  shield: '◇',
  wallet: '▭',
  check: '✓',
  'chevron-left': '‹',
  'chevron-right': '›',
  grid: '▦',
  menu: '☰',
  image: '▧',
  edit: '✎',
  chart: '⌁',
  lock: '□',
  phone: '▯',
  trash: '×',
  close: '×',
}

const glyph = computed(() => glyphMap[props.name] || '•')

const normalizedSize = computed(() => {
  if (typeof props.size === 'number') return `${props.size}rpx`
  return props.size
})

const iconStyle = computed(() => ({
  width: normalizedSize.value,
  height: normalizedSize.value,
  color: props.color,
  fontSize: normalizedSize.value,
  fontWeight: props.strokeWidth >= 2.2 ? 800 : 700,
}))
</script>

<style scoped lang="scss">
.keep-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  flex: none;
}

.keep-icon__glyph {
  display: block;
  line-height: 1;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
}
</style>
