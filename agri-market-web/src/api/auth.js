import request from './request'

export const authApi = {
  login: (data, config) => request.post('/auth/login', data, config),
  register: (data) => request.post('/auth/register', data),
  me: (config) => request.get('/auth/me', config),
  logout: () => request.post('/auth/logout')
}
