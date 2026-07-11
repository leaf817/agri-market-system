<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>订单管理</span>
        <el-button type="primary" :icon="Plus" @click="openCreate">模拟下单</el-button>
      </div>
    </template>

    <el-table :data="orders" v-loading="loading" border stripe>
      <el-table-column prop="orderNo" label="订单号" min-width="200" />
      <el-table-column prop="buyerName" label="买家" width="100" />
      <el-table-column prop="buyerPhone" label="电话" width="130" />
      <el-table-column label="金额" width="110">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :icon="View" @click="openDetail(row)">明细</el-button>
          <el-button size="small" type="primary" :icon="Edit" @click="openStatus(row)">变更状态</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 订单明细 -->
    <el-dialog v-model="detailVisible" title="订单明细" width="620px">
      <div v-if="current">
        <p>订单号：{{ current.orderNo }}　买家：{{ current.buyerName }}（{{ current.buyerPhone }}）</p>
        <p>收货地址：{{ current.buyerAddress || '-' }}　备注：{{ current.remark || '-' }}</p>
        <el-table :data="current.items" border size="small">
          <el-table-column prop="productName" label="商品" />
          <el-table-column label="单价" width="100"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="110"><template #default="{ row }">¥{{ row.amount }}</template></el-table-column>
        </el-table>
        <p style="text-align:right;margin-top:10px">合计：<b>¥{{ current.totalAmount }}</b></p>
      </div>
    </el-dialog>

    <!-- 变更状态 -->
    <el-dialog v-model="statusVisible" title="变更订单状态" width="380px">
      <el-form label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="targetStatus" style="width: 100%">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStatus">确定</el-button>
      </template>
    </el-dialog>

    <!-- 模拟下单 -->
    <el-dialog v-model="createVisible" title="模拟下单" width="620px">
      <el-form label-width="80px">
        <el-form-item label="买家"><el-input v-model="buyer.name" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="buyer.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="buyer.address" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="buyer.remark" /></el-form-item>
        <el-form-item label="商品">
          <div v-for="(it, idx) in items" :key="idx" class="order-item">
            <el-select v-model="it.productId" placeholder="选择商品" style="width: 260px">
              <el-option v-for="p in availableProducts" :key="p.id" :label="`${p.name}（库存${p.stock}）`" :value="p.id" />
            </el-select>
            <el-input-number v-model="it.quantity" :min="1" style="width: 120px; margin-left: 8px" />
            <el-button text type="danger" :icon="Delete" @click="items.splice(idx, 1)" style="margin-left: 4px" />
          </div>
          <el-button text type="primary" :icon="Plus" @click="items.push({ productId: null, quantity: 1 })">添加商品</el-button>
        </el-form-item>
        <el-form-item label="合计"><b>¥{{ previewTotal }}</b></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder">提交订单</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, View } from '@element-plus/icons-vue'
import { orderApi, productApi } from '../api'

const orders = ref([])
const availableProducts = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const statusVisible = ref(false)
const createVisible = ref(false)
const current = ref(null)
const currentId = ref(null)
const targetStatus = ref('')

const statusMap = {
  PENDING: { label: '待付款', type: 'info' },
  PAID: { label: '待发货', type: 'warning' },
  SHIPPED: { label: '已发货', type: 'primary' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'danger' }
}
const statusOptions = Object.entries(statusMap).map(([value, v]) => ({ value, label: v.label }))
const statusLabel = (s) => statusMap[s]?.label || s
const statusType = (s) => statusMap[s]?.type || 'info'

const buyer = reactive({ name: '', phone: '', address: '', remark: '' })
const items = reactive([{ productId: null, quantity: 1 }])

const previewTotal = computed(() => {
  return items.reduce((sum, it) => {
    const p = availableProducts.value.find((x) => x.id === it.productId)
    return p ? sum + Number(p.price) * it.quantity : sum
  }, 0).toFixed(2)
})

const load = async () => {
  loading.value = true
  try { orders.value = await orderApi.list() } finally { loading.value = false }
}
const openDetail = async (row) => {
  current.value = await orderApi.get(row.id)
  detailVisible.value = true
}
const openStatus = (row) => {
  currentId.value = row.id
  targetStatus.value = row.status
  statusVisible.value = true
}
const submitStatus = async () => {
  await orderApi.changeStatus(currentId.value, targetStatus.value)
  ElMessage.success('状态已更新')
  statusVisible.value = false
  load()
}
const openCreate = async () => {
  Object.assign(buyer, { name: '', phone: '', address: '', remark: '' })
  items.splice(0, items.length, { productId: null, quantity: 1 })
  availableProducts.value = await productApi.list({ status: 1 })
  createVisible.value = true
}
const submitOrder = async () => {
  const validItems = items.filter((it) => it.productId && it.quantity > 0)
  if (!buyer.name) return ElMessage.warning('请填写买家姓名')
  if (validItems.length === 0) return ElMessage.warning('请至少选择一件商品')
  await orderApi.create({
    buyerName: buyer.name,
    buyerPhone: buyer.phone,
    buyerAddress: buyer.address,
    remark: buyer.remark,
    items: validItems.map((it) => ({ productId: it.productId, quantity: it.quantity }))
  })
  ElMessage.success('下单成功，已扣减库存')
  createVisible.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.order-item { display: flex; align-items: center; margin-bottom: 8px; }
</style>
