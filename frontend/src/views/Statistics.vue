<template>
  <div>
    <el-row :gutter="16" class="overview">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card shadow="hover">
          <el-statistic :title="card.title" :value="card.value" :prefix="card.prefix" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>各品类销量 / 销售额</template>
          <el-table :data="byCategory" border stripe>
            <el-table-column prop="categoryName" label="品类" />
            <el-table-column prop="quantity" label="销量" />
            <el-table-column label="销售额"><template #default="{ row }">¥{{ row.amount }}</template></el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>销量 TOP 商品</template>
          <el-table :data="topProducts" border stripe>
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="name" label="商品" />
            <el-table-column prop="sales" label="销量" width="90" />
            <el-table-column label="销售额" width="120"><template #default="{ row }">¥{{ row.amount }}</template></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { statsApi } from '../api'

const overview = ref({ productCount: 0, orderCount: 0, totalQuantity: 0, totalAmount: 0 })
const byCategory = ref([])
const topProducts = ref([])

const cards = computed(() => [
  { title: '在售商品', value: overview.value.productCount },
  { title: '订单总数', value: overview.value.orderCount },
  { title: '累计销量', value: overview.value.totalQuantity },
  { title: '累计销售额', value: Number(overview.value.totalAmount), prefix: '¥' }
])

onMounted(async () => {
  const [o, c, t] = await Promise.all([statsApi.overview(), statsApi.byCategory(), statsApi.topProducts(5)])
  overview.value = o
  byCategory.value = c
  topProducts.value = t
})
</script>

<style scoped>
.overview .el-card { text-align: center; }
</style>
