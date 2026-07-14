import { createRouter, createWebHistory } from 'vue-router'
import { isLogin, hasRole, role, clearSession } from '../stores/user'
// 同步引入，避免 Vite 热更新后异步 chunk 失效导致主内容区空白
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Products from '../views/Products.vue'
import Profile from '../views/Profile.vue'
import Cart from '../views/Cart.vue'
import Favorites from '../views/Favorites.vue'
import Categories from '../views/Categories.vue'
import Origins from '../views/Origins.vue'
import Orders from '../views/Orders.vue'
import Statistics from '../views/Statistics.vue'
import Users from '../views/Users.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'login', component: Login, meta: { public: true, title: '登录' } },
  { path: '/register', name: 'register', component: Register, meta: { public: true, title: '注册' } },
  { path: '/products', name: 'products', component: Products, meta: { title: '全部产品', roles: ['admin', 'farmer', 'consumer'], productMode: 'public' } },
  { path: '/admin/products', name: 'adminProducts', component: Products, meta: { title: '产品管理', roles: ['admin'], productMode: 'admin' } },
  { path: '/my-products', name: 'myProducts', component: Products, meta: { title: '我的产品管理', roles: ['farmer'], productMode: 'mine' } },
  { path: '/profile', name: 'profile', component: Profile, meta: { title: '个人中心', roles: ['admin', 'farmer', 'consumer'] } },
  { path: '/cart', name: 'cart', component: Cart, meta: { title: '购物车', roles: ['consumer'] } },
  { path: '/favorites', name: 'favorites', component: Favorites, meta: { title: '我的收藏', roles: ['consumer'] } },
  { path: '/categories', name: 'categories', component: Categories, meta: { title: '分类管理', roles: ['admin'] } },
  { path: '/origins', name: 'origins', component: Origins, meta: { title: '产地管理', roles: ['admin'], originMode: 'admin' } },
  { path: '/my-origins', name: 'myOrigins', component: Origins, meta: { title: '我的产地', roles: ['farmer'], originMode: 'mine' } },
  { path: '/users', name: 'users', component: Users, meta: { title: '用户管理', roles: ['admin'] } },
  { path: '/orders', name: 'orders', component: Orders, meta: { title: '订单', roles: ['admin', 'farmer', 'consumer'] } },
  { path: '/statistics', name: 'statistics', component: Statistics, meta: { title: '销量统计', roles: ['admin', 'farmer'] } },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
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
    // 有 token 但无有效角色：清会话，避免死循环
    if (!role()) {
      clearSession()
      return { path: '/login' }
    }
    // 角色不符：回到首页（勿对 /products 自身再 redirect）
    if (to.path === '/products') return { path: '/profile' }
    return { path: '/products' }
  }
  return true
})

export default router
