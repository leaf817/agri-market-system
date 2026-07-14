import { reactive } from 'vue'

const KEY = 'agri_market_session'

function normalizeUser(user) {
  if (!user) return null
  return {
    ...user,
    role: user.role ? String(user.role).toLowerCase() : ''
  }
}

function loadRaw() {
  try {
    return JSON.parse(localStorage.getItem(KEY))
  } catch (e) {
    return null
  }
}

const raw = loadRaw()

/** 全局会话状态：token + 当前用户信息（role 统一小写：admin/farmer/consumer） */
const state = reactive({
  token: raw?.token || '',
  user: normalizeUser(raw?.user)
})

function persist() {
  localStorage.setItem(KEY, JSON.stringify({ token: state.token, user: state.user }))
}

export function setSession(token, user) {
  state.token = token
  state.user = normalizeUser(user)
  persist()
}

export function clearSession() {
  state.token = ''
  state.user = null
  localStorage.removeItem(KEY)
}

export function isLogin() {
  return !!(state.token && state.user?.role)
}

export function role() {
  return (state.user?.role || '').toLowerCase()
}

/** 当前角色是否在传入角色列表中（大小写不敏感） */
export function hasRole(...roles) {
  const current = role()
  return roles.some((r) => String(r).toLowerCase() === current)
}

export function useUserStore() {
  return state
}

export default state
