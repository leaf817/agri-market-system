import request from './request'

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
  list: () => request.get('/origins'),
  save: (data) => request.post('/origins', data),
  remove: (id) => request.delete(`/origins/${id}`)
}

export const orderApi = {
  list: () => request.get('/orders'),
  get: (id) => request.get(`/orders/${id}`),
  create: (data) => request.post('/orders', data),
  changeStatus: (id, status) => request.patch(`/orders/${id}/status`, null, { params: { status } })
}

export const statsApi = {
  overview: () => request.get('/stats/overview'),
  byCategory: () => request.get('/stats/by-category'),
  topProducts: (limit = 5) => request.get('/stats/top-products', { params: { limit } })
}

export const cartApi = {
  list: () => request.get('/cart'),
  count: () => request.get('/cart/count'),
  add: (productId, quantity = 1) => request.post('/cart', null, { params: { productId, quantity } }),
  updateQuantity: (id, quantity) => request.put(`/cart/${id}/quantity`, null, { params: { quantity } }),
  remove: (id) => request.delete(`/cart/${id}`),
  clear: () => request.delete('/cart')
}

export const reviewApi = {
  list: (productId) => request.get('/reviews', { params: { productId } }),
  stats: (productId) => request.get(`/reviews/product/${productId}/stats`),
  create: (productId, rating, content) => request.post('/reviews', null, { params: { productId, rating, content } }),
  remove: (id) => request.delete(`/reviews/${id}`)
}

export const favoriteApi = {
  list: () => request.get('/favorites'),
  count: () => request.get('/favorites/count'),
  check: (productId) => request.get('/favorites/check', { params: { productId } }),
  toggle: (productId) => request.post('/favorites', null, { params: { productId } })
}

export const profileApi = {
  get: () => request.get('/profile'),
  update: (data) => request.put('/profile', data)
}

export const addressApi = {
  list: () => request.get('/addresses'),
  getDefault: () => request.get('/addresses/default'),
  get: (id) => request.get(`/addresses/${id}`),
  create: (data) => request.post('/addresses', null, { params: data }),
  update: (id, data) => request.put(`/addresses/${id}`, null, { params: data }),
  remove: (id) => request.delete(`/addresses/${id}`),
  setDefault: (id) => request.patch(`/addresses/${id}/default`)
}
