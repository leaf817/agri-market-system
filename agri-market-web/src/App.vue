<template>
  <!-- 登录/注册等公开页：裸渲染，不显示管理布局 -->
  <router-view v-if="isPublic" v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>

  <!-- 管理布局：侧边栏 + 顶栏 + 主内容 -->
  <el-container v-else class="layout">
    <!-- 侧边栏：Logo + 菜单 -->
    <el-aside width="240px" class="aside">
      <div class="brand">
        <div class="brand-icon">🌾</div>
        <div class="brand-text">
          <div class="brand-title">农产品信息展示与售卖系统</div>
          <div class="brand-sub">乡村助农 · 产地直供</div>
        </div>
      </div>
      <el-menu :default-active="$route.path" router class="menu">
        <el-menu-item v-if="hasRole('admin', 'farmer', 'consumer')" index="/profile">
          <el-icon><User /></el-icon><span>个人中心</span>
        </el-menu-item>
        <el-menu-item v-if="hasRole('admin', 'farmer', 'consumer')" index="/products">
          <el-icon><Goods /></el-icon><span>{{ role() === 'consumer' ? '农产品' : '农产品管理' }}</span>
        </el-menu-item>
        <el-menu-item v-if="hasRole('consumer')" index="/cart">
          <el-icon><ShoppingCart /></el-icon><span>购物车</span>
        </el-menu-item>
        <el-menu-item v-if="hasRole('consumer')" index="/favorites">
          <el-icon><Star /></el-icon><span>我的收藏</span>
        </el-menu-item>
        <el-menu-item v-if="hasRole('admin')" index="/categories">
          <el-icon><Menu /></el-icon><span>分类管理</span>
        </el-menu-item>
        <el-menu-item v-if="hasRole('admin')" index="/origins">
          <el-icon><Location /></el-icon><span>产地公示</span>
        </el-menu-item>
        <el-menu-item v-if="hasRole('admin', 'farmer', 'consumer')" index="/orders">
          <el-icon><List /></el-icon><span>{{ role() === 'consumer' ? '我的订单' : '订单管理' }}</span>
        </el-menu-item>
        <el-menu-item v-if="hasRole('admin', 'farmer')" index="/statistics">
          <el-icon><DataAnalysis /></el-icon><span>销量统计</span>
        </el-menu-item>
      </el-menu>
      <div class="aside-footer">© {{ year }} 乡村助农</div>
    </el-aside>

    <!-- 右侧：顶栏 + 主内容 -->
    <el-container>
      <el-header class="header">
        <div class="page-title">{{ pageTitle }}</div>
        <div class="header-right">
          <span class="date">{{ today }}</span>
          <el-tag size="small" :type="roleTagType" effect="light">{{ roleLabel }}</el-tag>
          <el-dropdown @command="onCommand">
            <span class="user-trigger">
              <el-avatar :size="32" class="avatar">{{ avatarText }}</el-avatar>
              <span class="uname">{{ user?.nickname || user?.username || '未登录' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { SwitchButton, ShoppingCart, Star, User, Goods, Location, List, DataAnalysis, Menu } from '@element-plus/icons-vue'
import userStore, { role, hasRole, clearSession, isLogin } from './stores/user'
import { authApi, cartApi } from './api'

const route = useRoute()
const router = useRouter()

const user = computed(() => userStore.user)
const isPublic = computed(() => !!route.meta?.public)
const pageTitle = computed(() => route.meta?.title || '概览')
const year = new Date().getFullYear()
const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })

const roleLabelMap = { admin: '管理员', farmer: '农户', consumer: '消费者' }
const roleTypeMap = { admin: 'danger', farmer: 'warning', consumer: 'success' }
const roleLabel = computed(() => roleLabelMap[role()] || '游客')
const roleTagType = computed(() => roleTypeMap[role()] || 'info')
const avatarText = computed(() => (user.value?.nickname || user.value?.username || '?').slice(0, 1))

const cartCount = ref(0)

const loadCartCount = async () => {
  if (isLogin() && hasRole('consumer')) {
    try { cartCount.value = await cartApi.count() } catch (e) { cartCount.value = 0 }
  } else {
    cartCount.value = 0
  }
}

const onCommand = async (cmd) => {
  if (cmd === 'logout') {
    try { await authApi.logout() } catch (e) { /* 忽略网络错误，本地仍清理 */ }
    clearSession()
    cartCount.value = 0
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/profile')
  }
}

onMounted(loadCartCount)
</script>

<style>
.layout { height: 100%; }

/* 侧边栏 */
.aside {
  background: #fff;
  border-right: 1px solid #eef0ee;
  display: flex;
  flex-direction: column;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 16px;
  border-bottom: 1px solid #f0f3f0;
}
.brand-icon {
  width: 40px; height: 40px; flex: none;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px;
  background: linear-gradient(135deg, #43a047, #2e7d32);
  border-radius: 10px;
}
.brand-title { font-size: 14px; font-weight: 700; color: #1f2d3d; line-height: 1.3; }
.brand-sub { font-size: 12px; color: #8a97a0; margin-top: 2px; }

.menu {
  flex: 1;
  border-right: none;
  padding: 8px;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #5a6a5a;
  --el-menu-active-bg-color: var(--el-color-primary-light-9);
}
.menu .el-menu-item {
  border-radius: 8px;
  margin-bottom: 4px;
  height: 46px;
}
.menu .el-menu-item.is-active {
  color: var(--brand);
  font-weight: 600;
  border-right: 3px solid var(--brand);
}
.aside-footer { padding: 14px; text-align: center; font-size: 12px; color: #aab4c0; border-top: 1px solid #f3f5f3; }
.cart-badge { position: absolute; top: 8px; right: 12px; }

/* 顶栏 */
.header {
  height: 58px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #eef0ee;
  box-shadow: 0 1px 6px rgba(46, 125, 50, 0.05);
}
.header-right { display: flex; align-items: center; gap: 12px; }
.date { font-size: 13px; color: #8a97a0; }
.user-trigger { display: flex; align-items: center; gap: 8px; cursor: pointer; outline: none; }
.uname { font-size: 13px; color: #1f2d3d; font-weight: 600; }
.avatar { background: var(--brand); color: #fff; font-weight: 600; }

/* 主内容 */
.main { background: var(--bg-page); padding: 20px; }

/* 路由切换过渡 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.18s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
