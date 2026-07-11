<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo">🌾 乡村助农 · 农产品信息展示与售卖管理系统</div>
      <div class="subtitle">课程实训项目</div>
    </el-header>
    <el-container>
      <el-aside width="220px" class="aside">
        <el-menu :default-active="$route.path" router class="menu">
          <el-menu-item index="/products">
            <el-icon><Goods /></el-icon><span>农产品管理</span>
          </el-menu-item>
          <el-menu-item index="/categories">
            <el-icon><Menu /></el-icon><span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/origins">
            <el-icon><Location /></el-icon><span>产地公示</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><List /></el-icon><span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/statistics">
            <el-icon><DataAnalysis /></el-icon><span>销量统计</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
// 图标已在 main.js 全局注册，此处直接使用
</script>

<style>
html, body, #app { height: 100%; margin: 0; }
.layout { height: 100%; }
.header {
  display: flex;
  align-items: baseline;
  gap: 16px;
  background: #2e7d32;
  color: #fff;
}
.header .logo { font-size: 18px; font-weight: 600; }
.header .subtitle { font-size: 12px; opacity: 0.85; }
.aside { background: #fff; border-right: 1px solid #e6e6e6; }
.menu { border-right: none; }
.main { background: #f5f7fa; }
</style>
