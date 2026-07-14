import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import userStore, { clearSession } from '../stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  if (userStore.token) {
    config.headers.Authorization = 'Bearer ' + userStore.token
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 0) {
        return res.data
      }
      if (!response.config?.silentError) {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || 'Business Error'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.message
    if (status === 401) {
      clearSession()
      const path = router.currentRoute.value.path
      const isPublic = !!router.currentRoute.value.meta?.public
      if (path !== '/login' && !isPublic) {
        // silentError 只抑制 toast，登录失效仍需离开鉴权页
        if (!error.config?.silentError) {
          ElMessage.error(msg || '登录已过期，请重新登录')
        }
        router.replace('/login')
      }
    } else if (!error.config?.silentError) {
      ElMessage.error(msg || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request
