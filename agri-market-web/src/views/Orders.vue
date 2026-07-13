<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>{{ isConsumer ? '我的订单' : '订单管理' }}</span>
        <el-button type="primary" :icon="Plus" @click="openCreate">{{ isConsumer ? '去下单' : '模拟下单' }}</el-button>
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
          <el-button v-if="isManager" size="small" type="primary" :icon="Edit" @click="openStatus(row)">变更状态</el-button>
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
          <el-table-column label="评价" width="120">
            <template #default="{ row }">
              <el-button v-if="current.status === 'COMPLETED' && !row.reviewed" size="small" type="primary" @click="openReview(row)">去评价</el-button>
              <span v-else-if="row.reviewed" class="text-success">已评价</span>
              <span v-else class="text-gray">未完成</span>
            </template>
          </el-table-column>
        </el-table>
        <p style="text-align:right;margin-top:10px">合计：<b>¥{{ current.totalAmount }}</b></p>
      </div>
    </el-dialog>

    <!-- 变更状态（管理端/农户） -->
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

    <!-- 评价弹窗 -->
    <el-dialog v-model="reviewVisible" title="评价商品" width="560px">
      <div class="review-form">
        <div class="form-row">
          <label>商品</label>
          <span>{{ reviewingItem?.productName }}</span>
        </div>
        <div class="form-row rating-row">
          <label>评分</label>
          <div class="rating-wrapper">
            <el-rate v-model="reviewForm.rating" :max="5" show-score text-color="#ffb300" />
          </div>
        </div>
        <div class="form-row">
          <label>评价内容</label>
          <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="请输入您的评价..." />
        </div>
      </div>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- 下单 -->
    <el-dialog v-model="createVisible" :title="isConsumer ? '去下单' : '模拟下单'" width="620px">
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
import { orderApi, productApi, reviewApi } from '../api'
import { role, hasRole } from '../stores/user'

const isManager = computed(() => hasRole('admin', 'farmer'))
const isConsumer = computed(() => role() === 'consumer')

const orders = ref([])
const availableProducts = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const statusVisible = ref(false)
const createVisible = ref(false)
const reviewVisible = ref(false)
const current = ref(null)
const currentId = ref(null)
const targetStatus = ref('')
const reviewingItem = ref(null)
const reviewForm = reactive({ rating: 5, content: '' })

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
  const order = await orderApi.get(row.id)
  order.items.forEach(item => {
    item.reviewed = false
  })
  current.value = order
  detailVisible.value = true
}

const openReview = (item) => {
  reviewingItem.value = item
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.content.trim()) return ElMessage.warning('请填写评价内容')
  const productId = reviewingItem.value?.product?.id
  if (!productId) return ElMessage.error('无法评价：商品信息缺失')
  await reviewApi.create(productId, reviewForm.rating, reviewForm.content)
  ElMessage.success('评价成功')
  reviewingItem.value.reviewed = true
  reviewVisible.value = false
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
.text-success { color: #2e7d32; }
.text-gray { color: #aab4c0; }
.font-medium { font-weight: 500; }

.review-form { padding: 10px 0; }
.form-row { display: flex; align-items: flex-start; margin-bottom: 16px; }
.form-row label { width: 80px; flex-shrink: 0; font-weight: 500; color: #5a6a5a; padding-top: 4px; }
.form-row > span { flex: 1; font-weight: 500; }
.form-row :deep(.el-input) { flex: 1; }
.rating-row { align-items: center; }
.rating-wrapper { flex: 1; }
.rating-wrapper :deep(.el-rate) {
  font-size: 28px;
  display: inline-flex;
  flex-wrap: nowrap;
}
.rating-wrapper :deep(.el-rate__icon) {
  margin-right: 6px;
}
.rating-wrapper :deep(.el-rate__text) {
  margin-left: 12px;
  font-size: 18px;
}
</style>
