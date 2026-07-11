import { reactive } from 'vue'

const KEY = 'agri_market_session'

function load() {
  try {
    return JSON.parse(localStorage.getItem(KEY))
  } catch (e) {
    return null
  }
}

const saved = load()

/** 全局会话状态：token + 当前用户信息（含 role: admin/farmer/consumer） */
const state = reactive({
  token: saved?.token || '',
  user: saved?.user || null
})

function persist() {
  localStorage.setItem(KEY, JSON.stringify({ token: state.token, user: state.user }))
}

export function setSession(token, user) {
  state.token = token
  state.user = user
  persist()
}

export function clearSession() {
  state.token = ''
  state.user = null
  localStorage.removeItem(KEY)
}

export function isLogin() {
  return !!state.token
}

export function role() {
  return state.user?.role || ''
}

/** 当前角色是否在传入角色列表中（角色名为小写） */
export function hasRole(...roles) {
  return roles.includes(role())
}

export function useUserStore() {
  return state
}

export default state
