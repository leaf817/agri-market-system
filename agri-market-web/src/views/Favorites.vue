<template>
  <div v-loading="loading">
    <el-empty v-if="!loading && favorites.length === 0" description="暂无收藏，去发现心仪的农产品吧" />
    <el-row :gutter="16">
      <el-col v-for="fav in favorites" :key="fav.id" :xs="24" :sm="12" :md="8" :lg="6" class="card-col">
        <el-card shadow="hover" class="product-card" :body-style="{ padding: 0 }">
          <div class="cover-wrap">
            <el-image
              v-if="fav.product.cover"
              :src="fav.product.cover"
              :preview-src-list="[fav.product.cover]"
              fit="cover"
              lazy
              preview-teleported
              class="cover"
            />
            <div v-else class="cover-empty">🌾</div>
          </div>
          <div class="info">
            <div class="name" :title="fav.product.name">{{ fav.product.name }}</div>
            <div class="meta">
              <el-tag size="small" effect="plain" type="success">{{ fav.product.category?.name || '未分类' }}</el-tag>
            </div>
            <div class="origin" :title="fav.product.origin?.name">
              <el-icon><Location /></el-icon>{{ fav.product.origin?.name || '产地未公示' }}
            </div>
            <div class="price">
              ¥<span>{{ fav.product.price }}</span><small>/{{ fav.product.unit || '份' }}</small>
            </div>
            <div class="nums">库存 {{ fav.product.stock }}</div>
          </div>
          <div class="actions">
            <el-button type="primary" :icon="ShoppingCart" :disabled="fav.product.stock <= 0" @click="addToCart(fav.product)">
              {{ fav.product.stock > 0 ? '加入购物车' : '暂无库存' }}
            </el-button>
            <el-button text type="danger" :icon="Star" @click="removeFav(fav)">取消收藏</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Star, Location } from '@element-plus/icons-vue'
import { favoriteApi, cartApi } from '../api'

const favorites = ref([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try { favorites.value = await favoriteApi.list() } finally { loading.value = false }
}

const removeFav = async (fav) => {
  await favoriteApi.toggle(fav.product.id)
  ElMessage.success('已取消收藏')
  load()
}

const addToCart = async (product) => {
  await cartApi.add(product.id)
  ElMessage.success('已加入购物车')
}

onMounted(load)
</script>

<style scoped>
.card-col { margin-bottom: 16px; }
.product-card { border-radius: 12px; overflow: hidden; transition: transform 0.18s ease, box-shadow 0.18s ease; }
.product-card:hover { transform: translateY(-4px); }

.cover-wrap { position: relative; width: 100%; aspect-ratio: 4 / 3; overflow: hidden; background: linear-gradient(135deg, #eef5ee, #d7e9d8); }
.cover { width: 100%; height: 100%; display: block; }
.cover :deep(img) { width: 100%; height: 100%; object-fit: cover; }
.cover-empty { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 48px; }

.info { padding: 12px 14px 8px; }
.name { font-size: 15px; font-weight: 700; color: #1f2d3d; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.meta { margin: 8px 0 6px; }
.origin { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #8a97a0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.price { margin-top: 8px; font-size: 14px; color: #2e7d32; font-weight: 600; }
.price span { font-size: 22px; font-weight: 800; }
.price small { color: #8a97a0; font-weight: 400; }
.nums { margin-top: 6px; font-size: 12px; color: #aab4c0; }

.actions { display: flex; gap: 6px; padding: 10px 14px 14px; border-top: 1px solid #f3f5f3; }
.actions .el-button { flex: 1; }
</style>