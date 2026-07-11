import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/products' },
  { path: '/products', name: 'products', component: () => import('../views/Products.vue'), meta: { title: '农产品管理' } },
  { path: '/categories', name: 'categories', component: () => import('../views/Categories.vue'), meta: { title: '分类管理' } },
  { path: '/origins', name: 'origins', component: () => import('../views/Origins.vue'), meta: { title: '产地公示' } },
  { path: '/orders', name: 'orders', component: () => import('../views/Orders.vue'), meta: { title: '订单管理' } },
  { path: '/statistics', name: 'statistics', component: () => import('../views/Statistics.vue'), meta: { title: '销量统计' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
