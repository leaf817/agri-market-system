import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 每次执行 npm run dev 会重新生成；浏览器刷新不改变（同一 Vite 进程）
const DEV_BOOT_ID = `${Date.now()}`

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  define: {
    __DEV_BOOT_ID__: JSON.stringify(DEV_BOOT_ID)
  },
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
