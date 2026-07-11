import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 前端请求 /api 会被代理到后端，解决开发期跨域
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})
