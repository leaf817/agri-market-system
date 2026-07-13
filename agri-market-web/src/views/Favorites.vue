<template>
  <div>
    <el-card shadow="never" class="toolbar">
      <div class="toolbar-inner">
        <span class="title">我的收藏</span>
        <el-button @click="$router.push('/products')">继续逛逛</el-button>
      </div>
    </el-card>

    <div v-loading="loading" class="grid-wrap">
      <el-empty v-if="!loading && list.length === 0" description="还没有收藏商品">
        <el-button type="primary" @click="$router.push('/products')">去选购</el-button>
      </el-empty>

      <el-row :gutter="16">
        <el-col v-for="item in list" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6" class="card-col">
          <el-card shadow="hover" class="product-card" :body-style="{ padding: 0 }">
            <div class="cover-wrap">
              <el-image
                v-if="item.product?.cover"
                :src="item.product.cover"
                fit="cover"
                class="cover"
              />
              <div v-else class="cover-empty">🌾</div>
              <el-tag v-if="item.product?.status !== 1" class="off-tag" size="small" type="info">已下架</el-tag>
            </div>
            <div class="info">
              <div class="name" :title="item.product?.name">{{ item.product?.name || '商品已失效' }}</div>
              <div class="meta">
                <el-tag size="small" effect="plain" type="success">{{ item.product?.category?.name || '未分类' }}</el-tag>
              </div>
              <div class="price">
                ¥<span>{{ item.product?.price ?? '-' }}</span>
                <small v-if="item.product">/{{ item.product.unit || '份' }}</small>
              </div>
              <div class="nums">库存 {{ item.product?.stock ?? 0 }}</div>
            </div>
            <div class="actions">
              <el-button
                type="primary"
                :icon="ShoppingCart"
                :disabled="!item.product || item.product.status !== 1 || item.product.stock <= 0"
                @click="addCart(item)"
              >
                加购
              </el-button>
              <el-button type="danger" plain :icon="StarFilled" @click="unfavorite(item)">取消收藏</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, StarFilled } from '@element-plus/icons-vue'
import { favoriteApi, cartApi } from '../api'

const loading = ref(false)
const list = ref([])

const load = async () => {
  loading.value = true
  try {
    list.value = await favoriteApi.list()
  } finally {
    loading.value = false
  }
}

const addCart = async (item) => {
  await cartApi.add({ productId: item.product.id, quantity: 1 })
  ElMessage.success('已加入购物车')
}

const unfavorite = async (item) => {
  const productId = item.product?.id
  if (!productId) return ElMessage.warning('商品已失效')
  await ElMessageBox.confirm(`取消收藏「${item.product?.name || '该商品'}」？`, '提示', { type: 'warning' })
  await favoriteApi.remove(productId)
  ElMessage.success('已取消收藏')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.toolbar-inner { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 700; color: #1f2d3d; }

.grid-wrap { min-height: 200px; }
.card-col { margin-bottom: 16px; }
.product-card { border-radius: 12px; overflow: hidden; transition: transform 0.18s ease; }
.product-card:hover { transform: translateY(-4px); }

.cover-wrap {
  position: relative; width: 100%; aspect-ratio: 4 / 3; overflow: hidden;
  background: linear-gradient(135deg, #eef5ee, #d7e9d8);
}
.cover { width: 100%; height: 100%; display: block; }
.cover :deep(img) { width: 100%; height: 100%; object-fit: cover; }
.cover-empty { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 48px; }
.off-tag { position: absolute; top: 10px; left: 10px; }

.info { padding: 12px 14px 8px; }
.name { font-size: 15px; font-weight: 700; color: #1f2d3d; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.meta { margin: 8px 0 6px; }
.price { margin-top: 8px; font-size: 14px; color: #2e7d32; font-weight: 600; }
.price span { font-size: 22px; font-weight: 800; }
.price small { color: #8a97a0; font-weight: 400; }
.nums { margin-top: 6px; font-size: 12px; color: #aab4c0; }

.actions { display: flex; gap: 6px; padding: 10px 14px 14px; border-top: 1px solid #f3f5f3; }
.actions .el-button { flex: 1; }
</style>
