import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import userStore, { clearSession, isLogin, setSession } from './stores/user'
import { authApi } from './api'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus, { locale: zhCn })
app.use(router)

const BOOT_KEY = 'agri_dev_boot_id'

/**
 * 开发环境：仅在重新 npm run dev（Vite 进程重启）时清登录态并进登录页；
 * 浏览器刷新保留登录。业务数据始终在 MySQL，不会因此清空。
 */
function clearSessionIfDevServerRestarted() {
  if (!import.meta.env.DEV) return
  const bootId = typeof __DEV_BOOT_ID__ !== 'undefined' ? __DEV_BOOT_ID__ : ''
  const saved = localStorage.getItem(BOOT_KEY)
  if (bootId && saved !== bootId) {
    clearSession()
    localStorage.setItem(BOOT_KEY, bootId)
  }
}

async function boot() {
  clearSessionIfDevServerRestarted()

  if (userStore.token) {
    try {
      const user = await authApi.me({ silentError: true })
      setSession(userStore.token, user)
    } catch (e) {
      clearSession()
    }
  }

  await router.isReady()
  if (!isLogin() && !router.currentRoute.value.meta?.public) {
    await router.replace('/login')
  }
  app.mount('#app')
}

boot()
