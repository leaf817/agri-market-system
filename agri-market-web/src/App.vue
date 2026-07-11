<template>
  <el-container class="layout">
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
        <el-menu-item index="/products"><el-icon><Goods /></el-icon><span>农产品管理</span></el-menu-item>
        <el-menu-item index="/categories"><el-icon><Menu /></el-icon><span>分类管理</span></el-menu-item>
        <el-menu-item index="/origins"><el-icon><Location /></el-icon><span>产地公示</span></el-menu-item>
        <el-menu-item index="/orders"><el-icon><List /></el-icon><span>订单管理</span></el-menu-item>
        <el-menu-item index="/statistics"><el-icon><DataAnalysis /></el-icon><span>销量统计</span></el-menu-item>
      </el-menu>
      <div class="aside-footer">© {{ year }} 乡村助农</div>
    </el-aside>

    <!-- 右侧：顶栏 + 主内容 -->
    <el-container>
      <el-header class="header">
        <div class="page-title">{{ pageTitle }}</div>
        <div class="header-right">
          <span class="date">{{ today }}</span>
          <el-avatar :size="32" class="avatar">陈</el-avatar>
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
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const pageTitle = computed(() => route.meta?.title || '概览')
const year = new Date().getFullYear()
const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
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
.aside-footer { padding: 14px; text-align: center; font-size: 12px; color: #aab4c0; border-top: 1px solid #f0f3f0; }

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
.header-right { display: flex; align-items: center; gap: 14px; }
.date { font-size: 13px; color: #8a97a0; }
.avatar { background: var(--brand); color: #fff; font-weight: 600; }

/* 主内容 */
.main { background: var(--bg-page); padding: 20px; }

/* 路由切换过渡 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.18s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
