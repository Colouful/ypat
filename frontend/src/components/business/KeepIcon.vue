<template>
  <view class="keep-icon" :style="iconStyle" aria-hidden="true">
    <!-- 统一用 SVG 兜底（之前 MP-WEIXIN 分支依赖 static/icons/*.png，但项目未提供，
         导致运行时 500 Internal Server Error。SVG 在所有平台都正常工作）-->
    <svg
      class="keep-icon__svg"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      :stroke-width="props.strokeWidth"
      stroke-linecap="round"
      stroke-linejoin="round"
      xmlns="http://www.w3.org/2000/svg"
    >
      <template v-for="(part, index) in iconParts" :key="`${part.tag}-${index}`">
        <path v-if="part.tag === 'path'" v-bind="part.attrs" />
        <circle v-else-if="part.tag === 'circle'" v-bind="part.attrs" />
        <rect v-else-if="part.tag === 'rect'" v-bind="part.attrs" />
        <line v-else-if="part.tag === 'line'" v-bind="part.attrs" />
        <polyline v-else-if="part.tag === 'polyline'" v-bind="part.attrs" />
      </template>
    </svg>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type IconPart = {
  tag: 'path' | 'circle' | 'rect' | 'line' | 'polyline'
  attrs: Record<string, string | number>
}

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

const iconMap: Record<string, IconPart[]> = {
  home: [
    { tag: 'path', attrs: { d: 'm3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z' } },
    { tag: 'polyline', attrs: { points: '9 22 9 12 15 12 15 22' } },
  ],
  compass: [
    { tag: 'circle', attrs: { cx: 12, cy: 12, r: 10 } },
    { tag: 'path', attrs: { d: 'm16.24 7.76-2.12 6.36-6.36 2.12 2.12-6.36z' } },
  ],
  'plus-circle': [
    { tag: 'circle', attrs: { cx: 12, cy: 12, r: 10 } },
    { tag: 'path', attrs: { d: 'M8 12h8' } },
    { tag: 'path', attrs: { d: 'M12 8v8' } },
  ],
  mail: [
    { tag: 'rect', attrs: { width: 20, height: 16, x: 2, y: 4, rx: 2 } },
    { tag: 'path', attrs: { d: 'm22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7' } },
  ],
  copy: [
    { tag: 'rect', attrs: { width: 14, height: 14, x: 8, y: 8, rx: 2, ry: 2 } },
    { tag: 'path', attrs: { d: 'M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2' } },
  ],
  user: [
    { tag: 'circle', attrs: { cx: 12, cy: 8, r: 5 } },
    { tag: 'path', attrs: { d: 'M20 21a8 8 0 0 0-16 0' } },
  ],
  users: [
    { tag: 'path', attrs: { d: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2' } },
    { tag: 'circle', attrs: { cx: 9, cy: 7, r: 4 } },
    { tag: 'path', attrs: { d: 'M22 21v-2a4 4 0 0 0-3-3.9' } },
    { tag: 'path', attrs: { d: 'M16 3.1a4 4 0 0 1 0 7.8' } },
  ],
  camera: [
    { tag: 'path', attrs: { d: 'M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3z' } },
    { tag: 'circle', attrs: { cx: 12, cy: 13, r: 3 } },
  ],
  'map-pin': [
    { tag: 'path', attrs: { d: 'M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 0 1 16 0' } },
    { tag: 'circle', attrs: { cx: 12, cy: 10, r: 3 } },
  ],
  search: [
    { tag: 'circle', attrs: { cx: 11, cy: 11, r: 8 } },
    { tag: 'path', attrs: { d: 'm21 21-4.3-4.3' } },
  ],
  sliders: [
    { tag: 'line', attrs: { x1: 21, x2: 14, y1: 4, y2: 4 } },
    { tag: 'line', attrs: { x1: 10, x2: 3, y1: 4, y2: 4 } },
    { tag: 'line', attrs: { x1: 21, x2: 12, y1: 12, y2: 12 } },
    { tag: 'line', attrs: { x1: 8, x2: 3, y1: 12, y2: 12 } },
    { tag: 'line', attrs: { x1: 21, x2: 16, y1: 20, y2: 20 } },
    { tag: 'line', attrs: { x1: 12, x2: 3, y1: 20, y2: 20 } },
    { tag: 'line', attrs: { x1: 14, x2: 14, y1: 2, y2: 6 } },
    { tag: 'line', attrs: { x1: 8, x2: 8, y1: 10, y2: 14 } },
    { tag: 'line', attrs: { x1: 16, x2: 16, y1: 18, y2: 22 } },
  ],
  star: [
    { tag: 'path', attrs: { d: 'M11.5 2.8a.6.6 0 0 1 1 0l2.6 5.4 5.9.8a.6.6 0 0 1 .3 1l-4.2 4.1 1 5.8a.6.6 0 0 1-.9.6L12 17.8l-5.2 2.7a.6.6 0 0 1-.9-.6l1-5.8L2.7 10a.6.6 0 0 1 .3-1l5.9-.8z' } },
  ],
  shield: [
    { tag: 'path', attrs: { d: 'M20 13c0 5-3.5 7.5-7.7 8.8a1 1 0 0 1-.6 0C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.2-2.6a1.3 1.3 0 0 1 1.6 0C14.5 3.8 17 5 19 5a1 1 0 0 1 1 1z' } },
    { tag: 'path', attrs: { d: 'm9 12 2 2 4-4' } },
  ],
  'help-circle': [
    { tag: 'circle', attrs: { cx: 12, cy: 12, r: 10 } },
    { tag: 'path', attrs: { d: 'M9.1 9a3 3 0 1 1 5.8 1c-.5 1.3-2 2-2.9 3' } },
    { tag: 'path', attrs: { d: 'M12 17h.01' } },
  ],
  wallet: [
    { tag: 'path', attrs: { d: 'M19 7V5a2 2 0 0 0-2-2H5a2 2 0 0 0 0 4h15a2 2 0 0 1 2 2v3' } },
    { tag: 'path', attrs: { d: 'M3 5v14a2 2 0 0 0 2 2h15a2 2 0 0 0 2-2v-4' } },
    { tag: 'path', attrs: { d: 'M18 12h4v6h-4a3 3 0 0 1 0-6' } },
    { tag: 'path', attrs: { d: 'M18 15h.01' } },
  ],
  check: [
    { tag: 'path', attrs: { d: 'M20 6 9 17l-5-5' } },
  ],
  'chevron-left': [
    { tag: 'path', attrs: { d: 'm15 18-6-6 6-6' } },
  ],
  'chevron-right': [
    { tag: 'path', attrs: { d: 'm9 18 6-6-6-6' } },
  ],
  grid: [
    { tag: 'rect', attrs: { width: 7, height: 7, x: 3, y: 3, rx: 1 } },
    { tag: 'rect', attrs: { width: 7, height: 7, x: 14, y: 3, rx: 1 } },
    { tag: 'rect', attrs: { width: 7, height: 7, x: 3, y: 14, rx: 1 } },
    { tag: 'path', attrs: { d: 'M17.5 14v7' } },
    { tag: 'path', attrs: { d: 'M14 17.5h7' } },
  ],
  menu: [
    { tag: 'path', attrs: { d: 'M4 6h16' } },
    { tag: 'path', attrs: { d: 'M4 12h16' } },
    { tag: 'path', attrs: { d: 'M4 18h16' } },
  ],
  image: [
    { tag: 'rect', attrs: { width: 18, height: 18, x: 3, y: 3, rx: 2, ry: 2 } },
    { tag: 'circle', attrs: { cx: 9, cy: 9, r: 2 } },
    { tag: 'path', attrs: { d: 'm21 15-3.1-3.1a2 2 0 0 0-2.8 0L6 21' } },
  ],
  edit: [
    { tag: 'path', attrs: { d: 'M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7' } },
    { tag: 'path', attrs: { d: 'M18.4 2.6a2.1 2.1 0 0 1 3 3L12 15l-4 1 1-4z' } },
  ],
  chart: [
    { tag: 'path', attrs: { d: 'M3 17 9 11l4 4L21 7' } },
    { tag: 'path', attrs: { d: 'M14 7h7v7' } },
  ],
  lock: [
    { tag: 'rect', attrs: { width: 18, height: 11, x: 3, y: 11, rx: 2, ry: 2 } },
    { tag: 'path', attrs: { d: 'M7 11V7a5 5 0 0 1 10 0v4' } },
  ],
  phone: [
    { tag: 'path', attrs: { d: 'M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.4 19.4 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7c.1 1 .4 1.9.7 2.8a2 2 0 0 1-.5 2.1L8.1 9.9a16 16 0 0 0 6 6l1.3-1.3a2 2 0 0 1 2.1-.5c.9.3 1.8.6 2.8.7a2 2 0 0 1 1.7 2.1' } },
  ],
  eye: [
    { tag: 'path', attrs: { d: 'M2.1 12.5a1 1 0 0 1 0-1c1.8-3.1 5.1-6 9.9-6s8.1 2.9 9.9 6a1 1 0 0 1 0 1c-1.8 3.1-5.1 6-9.9 6s-8.1-2.9-9.9-6' } },
    { tag: 'circle', attrs: { cx: 12, cy: 12, r: 3 } },
  ],
  handshake: [
    { tag: 'path', attrs: { d: 'm11 17 2 2a2.8 2.8 0 0 0 4-4' } },
    { tag: 'path', attrs: { d: 'm14 14 2.5 2.5a2.8 2.8 0 0 0 4-4L14 6h-4l-6.5 6.5a2.8 2.8 0 0 0 4 4L10 14' } },
    { tag: 'path', attrs: { d: 'M7 7 5.5 5.5' } },
    { tag: 'path', attrs: { d: 'm17 7 1.5-1.5' } },
  ],
  trash: [
    { tag: 'path', attrs: { d: 'M3 6h18' } },
    { tag: 'path', attrs: { d: 'M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2' } },
    { tag: 'path', attrs: { d: 'M19 6 18 20a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6' } },
    { tag: 'path', attrs: { d: 'M10 11v6' } },
    { tag: 'path', attrs: { d: 'M14 11v6' } },
  ],
  close: [
    { tag: 'path', attrs: { d: 'M18 6 6 18' } },
    { tag: 'path', attrs: { d: 'm6 6 12 12' } },
  ],
  sparkles: [
    { tag: 'path', attrs: { d: 'M9.9 4.2 8.7 7.5a2 2 0 0 1-1.2 1.2L4.2 9.9a.6.6 0 0 0 0 1.2l3.3 1.2a2 2 0 0 1 1.2 1.2l1.2 3.3a.6.6 0 0 0 1.2 0l1.2-3.3a2 2 0 0 1 1.2-1.2l3.3-1.2a.6.6 0 0 0 0-1.2l-3.3-1.2a2 2 0 0 1-1.2-1.2l-1.2-3.3a.6.6 0 0 0-1.2 0' } },
    { tag: 'path', attrs: { d: 'M18 15v4' } },
    { tag: 'path', attrs: { d: 'M20 17h-4' } },
  ],
  gem: [
    { tag: 'path', attrs: { d: 'M6 3h12l4 6-10 12L2 9z' } },
    { tag: 'path', attrs: { d: 'M12 21 8 9l4-6 4 6z' } },
    { tag: 'path', attrs: { d: 'M2 9h20' } },
  ],
  coins: [
    { tag: 'circle', attrs: { cx: 8, cy: 8, r: 6 } },
    { tag: 'path', attrs: { d: 'M18.1 10.4A6 6 0 1 1 10.4 18' } },
    { tag: 'path', attrs: { d: 'M7 6h1.5a1.5 1.5 0 0 1 0 3H7h2a1.5 1.5 0 0 1 0 3H7' } },
    { tag: 'path', attrs: { d: 'M7 5v8' } },
  ],
  'circle-x': [
    { tag: 'circle', attrs: { cx: 12, cy: 12, r: 10 } },
    { tag: 'path', attrs: { d: 'm15 9-6 6' } },
    { tag: 'path', attrs: { d: 'm9 9 6 6' } },
  ],
  video: [
    { tag: 'rect', attrs: { x: 2, y: 6, width: 14, height: 12, rx: 2 } },
    { tag: 'path', attrs: { d: 'm22 8-6 4 6 4z' } },
  ],
  plus: [
    { tag: 'path', attrs: { d: 'M12 5v14' } },
    { tag: 'path', attrs: { d: 'M5 12h14' } },
  ],
  warning: [
    { tag: 'path', attrs: { d: 'M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z' } },
    { tag: 'path', attrs: { d: 'M12 9v4' } },
    { tag: 'path', attrs: { d: 'M12 17h.01' } },
  ],
  'map-pin-fill': [
    { tag: 'path', attrs: { d: 'M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 0 1 16 0z' } },
    { tag: 'circle', attrs: { cx: 12, cy: 10, r: 3, fill: 'currentColor', stroke: 'none' } },
  ],
}

const iconParts = computed(() => iconMap[props.name] || iconMap.sparkles)

const iconName = computed(() => (iconMap[props.name] ? props.name : 'sparkles'))

const mpImageIconAlias: Record<string, string> = {
  users: 'user',
  'help-circle': 'sparkles',
}

const mpImageIconName = computed(() => mpImageIconAlias[iconName.value] || iconName.value)

const normalizedSize = computed(() => {
  if (typeof props.size === 'number') return `${props.size}rpx`
  return props.size
})

function normalizeHexColor(value: string): string {
  const color = value.trim().toUpperCase()
  const shorthandMatch = color.match(/^#([0-9A-F])([0-9A-F])([0-9A-F])$/)
  if (!shorthandMatch) return color
  return `#${shorthandMatch[1]}${shorthandMatch[1]}${shorthandMatch[2]}${shorthandMatch[2]}${shorthandMatch[3]}${shorthandMatch[3]}`
}

const colorToken = computed(() => {
  const color = normalizeHexColor(props.color)
  if (color === '#23C268') return 'brand'
  if (color === '#83888F') return 'secondary'
  if (color === '#B3B8BE') return 'helper'
  if (color === '#FFFFFF') return 'white'
  if (color === '#FF9F1C') return 'orange'
  if (color === '#5577A8') return 'blue'
  if (color === '#9C7836') return 'gold'
  return 'primary'
})

const imageSource = computed(() => `/static/icons/${mpImageIconName.value}-${colorToken.value}.png`)

const iconStyle = computed(() => ({
  width: normalizedSize.value,
  height: normalizedSize.value,
  color: props.color,
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

.keep-icon__svg,
.keep-icon__image {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
