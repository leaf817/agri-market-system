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
