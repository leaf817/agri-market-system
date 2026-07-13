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
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      // 上传的图片走同一后端静态资源，开发期一并代理
      '/uploads': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      // 演示封面（classpath:/static/covers）
      '/covers': {
        target: 'http://localhost:8082',
        changeOrigin: true
      }
    }
  }
})
