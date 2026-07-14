import request from './request'
import userStore from '../stores/user'

export { authApi } from './auth'

export const productApi = {
  list: (params) => request.get('/products', { params }),
  get: (id) => request.get(`/products/${id}`),
  create: (data) => request.post('/products', data),
  update: (id, data) => request.put(`/products/${id}`, data),
  changeStatus: (id, status) => request.patch(`/products/${id}/status`, null, { params: { status } }),
  remove: (id) => request.delete(`/products/${id}`)
}

export const categoryApi = {
  list: () => request.get('/categories'),
  save: (data) => request.post('/categories', data),
  remove: (id) => request.delete(`/categories/${id}`)
}

export const originApi = {
  list: (params) => request.get('/origins', { params }),
  save: (data) => request.post('/origins', data),
  remove: (id) => request.delete(`/origins/${id}`)
}

export const orderApi = {
  list: () => request.get('/orders'),
  get: (id) => request.get(`/orders/${id}`),
  create: (data) => request.post('/orders', data),
  update: (id, data) => request.put(`/orders/${id}`, data),
  remove: (id) => request.delete(`/orders/${id}`),
  changeStatus: (id, status) => request.patch(`/orders/${id}/status`, null, { params: { status } }),
  pay: (id) => request.post(`/orders/${id}/pay`),
  cancel: (id, reason) => request.post(`/orders/${id}/cancel`, { reason }),
  ship: (id, data) => request.post(`/orders/${id}/ship`, data),
  confirm: (id) => request.post(`/orders/${id}/confirm`),
  close: (id, reason) => request.post(`/orders/${id}/close`, { reason })
}

export const statsApi = {
  overview: (config) => request.get('/stats/overview', config),
  byCategory: (config) => request.get('/stats/by-category', config),
  topProducts: (limit = 5, config) => request.get('/stats/top-products', { params: { limit }, ...config })
}

export const cartApi = {
  list: () => request.get('/cart'),
  add: (data) => request.post('/cart', data),
  update: (id, quantity) => request.put(`/cart/${id}`, { quantity }),
  remove: (id) => request.delete(`/cart/${id}`),
  clear: () => request.delete('/cart'),
  checkout: (data) => request.post('/cart/checkout', data)
}

export const favoriteApi = {
  list: () => request.get('/favorites'),
  ids: () => request.get('/favorites/ids'),
  add: (productId) => request.post(`/favorites/${productId}`),
  remove: (productId) => request.delete(`/favorites/${productId}`),
  toggle: (productId) => request.post(`/favorites/toggle/${productId}`)
}

export const aiApi = {
  chat: (message, history = []) => request.post('/ai/chat', { message, history }),
  chatStream: async (message, history = [], onChunk) => {
    const response = await fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(userStore.token ? { Authorization: 'Bearer ' + userStore.token } : {})
      },
      body: JSON.stringify({ message, history })
    })
    if (!response.ok || !response.body) {
      throw new Error('AI stream request failed')
    }
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let fullText = ''
    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      fullText += chunk
      if (onChunk) onChunk(chunk, fullText)
    }
    const tail = decoder.decode()
    if (tail) {
      fullText += tail
      if (onChunk) onChunk(tail, fullText)
    }
    return fullText
  }
}

export const profileApi = {
  get: () => request.get('/profile'),
  update: (data) => request.put('/profile', data),
  changePassword: (data) => request.put('/profile/password', data)
}

export const userApi = {
  list: () => request.get('/users'),
  get: (id) => request.get(`/users/${id}`),
  create: (data) => request.post('/users', data),
  update: (id, data) => request.put(`/users/${id}`, data),
  resetPassword: (id, newPassword) => request.put(`/users/${id}/password`, { newPassword })
}

export const reviewApi = {
  list: (params) => request.get('/reviews', { params }),
  stats: (productId) => request.get(`/reviews/product/${productId}/stats`),
  create: (productId, rating, content) =>
    request.post('/reviews', null, { params: { productId, rating, content } }),
  remove: (id) => request.delete(`/reviews/${id}`)
}

/** 地址接口后端为 @RequestParam，用 query 传参 */
export const addressApi = {
  list: () => request.get('/addresses'),
  getDefault: () => request.get('/addresses/default'),
  create: (data) => request.post('/addresses', null, { params: data }),
  update: (id, data) => request.put(`/addresses/${id}`, null, { params: data }),
  remove: (id) => request.delete(`/addresses/${id}`),
  setDefault: (id) => request.patch(`/addresses/${id}/default`)
}
