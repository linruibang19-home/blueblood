import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import Components from 'unplugin-vue-components/vite'
import AutoImport from 'unplugin-auto-import/vite'
import { VantResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    Components({
      resolvers: [VantResolver()],
    }),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    host: true,
    proxy: {
      // 前端所有请求均以 /api 开头（与 axios baseURL 一致）。
      // 后端 context-path 已为 /api，因此无需 rewrite 去前缀：
      // 前端 /api/auth/login -> http://localhost:8090/api/auth/login
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
      },
    },
  },
})