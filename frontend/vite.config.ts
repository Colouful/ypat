import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  resolve: {
    alias: [
      {
        find: '@/api/request',
        replacement: fileURLToPath(new URL('./src/api/request-adapter.ts', import.meta.url)),
      },
      {
        find: '@',
        replacement: fileURLToPath(new URL('./src', import.meta.url)),
      },
    ],
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: '@import "@/styles/tokens.scss";@import "@/styles/mixins.scss";',
      },
    },
  },
})
