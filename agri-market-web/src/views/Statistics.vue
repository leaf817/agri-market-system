<template>
  <div>
    <!-- 总览卡片 -->
    <el-row :gutter="16" class="overview">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in cards" :key="card.title" class="overview-col">
        <el-card shadow="hover" class="stat-card" :body-style="{ padding: '18px 20px' }">
          <div class="stat-inner">
            <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="24"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-title">{{ card.title }}</div>
              <div class="stat-value"><span v-if="card.prefix">{{ card.prefix }}</span>{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>各品类销量 / 销售额</template>
          <el-table :data="byCategory" stripe>
            <el-table-column prop="categoryName" label="品类" />
            <el-table-column prop="quantity" label="销量" />
            <el-table-column label="销售额"><template #default="{ row }">¥{{ row.amount }}</template></el-table-column>
          </el-table>
          <el-empty v-if="byCategory.length === 0" description="暂无销售数据" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>销量 TOP 商品</template>
          <el-table :data="topProducts" stripe>
            <el-table-column label="#" width="60">
              <template #default="{ $index }">
                <span class="rank" :class="`rank-${$index + 1}`">{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="商品" />
            <el-table-column prop="sales" label="销量" width="90" />
            <el-table-column label="销售额" width="120"><template #default="{ row }">¥{{ row.amount }}</template></el-table-column>
          </el-table>
          <el-empty v-if="topProducts.length === 0" description="暂无销售数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, markRaw } from 'vue'
import { Goods, Tickets, Histogram, Money } from '@element-plus/icons-vue'
import { statsApi } from '../api'

const overview = ref({ productCount: 0, orderCount: 0, totalQuantity: 0, totalAmount: 0 })
const byCategory = ref([])
const topProducts = ref([])

const cards = computed(() => [
  { title: '在售商品', value: overview.value.productCount, icon: markRaw(Goods), bg: '#e8f5e9', color: '#2e7d32' },
  { title: '订单总数', value: overview.value.orderCount, icon: markRaw(Tickets), bg: '#e3f2fd', color: '#1565c0' },
  { title: '累计销量', value: overview.value.totalQuantity, icon: markRaw(Histogram), bg: '#fff3e0', color: '#ef6c00' },
  { title: '累计销售额', value: Number(overview.value.totalAmount), prefix: '¥', icon: markRaw(Money), bg: '#f3e5f5', color: '#7b1fa2' }
])

onMounted(async () => {
  const [o, c, t] = await Promise.all([statsApi.overview(), statsApi.byCategory(), statsApi.topProducts(5)])
  overview.value = o
  byCategory.value = c
  topProducts.value = t
})
</script>

<style scoped>
.overview-col { margin-bottom: 16px; }
.stat-card { border-radius: 12px; transition: transform 0.18s ease, box-shadow 0.18s ease; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 52px; height: 52px; flex: none; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.stat-title { font-size: 13px; color: #8a97a0; }
.stat-value { font-size: 26px; font-weight: 800; color: #1f2d3d; margin-top: 4px; }

.rank { display: inline-flex; align-items: center; justify-content: center; width: 22px; height: 22px; border-radius: 50%; font-size: 12px; font-weight: 700; background: #eef0ee; color: #8a97a0; }
.rank-1 { background: #ffd54f; color: #8a5a00; }
.rank-2 { background: #cfd8dc; color: #546e7a; }
.rank-3 { background: #ffccbc; color: #bf360c; }
</style>
