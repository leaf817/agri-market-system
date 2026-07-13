import { createRouter, createWebHistory } from 'vue-router'
import { isLogin, hasRole } from '../stores/user'

const routes = [
  { path: '/', redirect: '/products' },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { public: true, title: '登录' } },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue'), meta: { public: true, title: '注册' } },
  { path: '/products', name: 'products', component: () => import('../views/Products.vue'), meta: { title: '农产品', roles: ['admin', 'farmer', 'consumer'] } },
  { path: '/profile', name: 'profile', component: () => import('../views/Profile.vue'), meta: { title: '个人中心', roles: ['admin', 'farmer', 'consumer'] } },
  { path: '/cart', name: 'cart', component: () => import('../views/Cart.vue'), meta: { title: '购物车', roles: ['consumer'] } },
  { path: '/favorites', name: 'favorites', component: () => import('../views/Favorites.vue'), meta: { title: '我的收藏', roles: ['consumer'] } },
  { path: '/categories', name: 'categories', component: () => import('../views/Categories.vue'), meta: { title: '分类管理', roles: ['admin'] } },
  { path: '/origins', name: 'origins', component: () => import('../views/Origins.vue'), meta: { title: '产地公示', roles: ['admin'] } },
  { path: '/orders', name: 'orders', component: () => import('../views/Orders.vue'), meta: { title: '订单', roles: ['admin', 'farmer', 'consumer'] } },
  { path: '/statistics', name: 'statistics', component: () => import('../views/Statistics.vue'), meta: { title: '销量统计', roles: ['admin', 'farmer'] } },
  { path: '/:pathMatch(.*)*', redirect: '/products' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.public) {
    // 已登录访问登录/注册页，直接进首页
    if (isLogin()) return { path: '/products' }
    return true
  }
  if (!isLogin()) return { path: '/login' }
  const roles = to.meta.roles
  if (roles && !hasRole(...roles)) {
    // 角色不符：回到首页（前端拦截，后端仍会二次校验）
    return { path: '/products' }
  }
  return true
})

export default router
