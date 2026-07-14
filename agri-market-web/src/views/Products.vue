<template>
  <div>
    <!-- 筛选工具条 -->
    <el-card shadow="never" class="toolbar">
      <div class="toolbar-inner">
        <el-form :inline="true" class="filter">
          <el-form-item label="分类">
            <el-select v-model="filters.categoryId" placeholder="全部分类" clearable style="width: 150px" @change="loadProducts">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="isManager" label="状态">
            <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 130px" @change="loadProducts">
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="filters.keyword" placeholder="商品名称" clearable :prefix-icon="Search" @keyup.enter="loadProducts" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadProducts">查询</el-button>
            <el-button :icon="Refresh" @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
        <el-button v-if="isManager" type="primary" :icon="Plus" @click="openCreate">上架农产品</el-button>
      </div>
    </el-card>

    <!-- 管理端：表格 + 表单弹窗 -->
    <el-card v-if="isManager" shadow="never">
      <el-table :data="products" v-loading="loading" border stripe>
        <el-table-column label="封面" width="84">
          <template #default="{ row }">
            <el-image v-if="row.cover" :src="row.cover" :preview-src-list="[row.cover]" fit="cover" preview-teleported class="thumb" />
            <div v-else class="thumb-empty">🌾</div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="分类" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" type="success">{{ row.category?.name || '未分类' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="产地" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.origin?.name || '-' }}</template>
        </el-table-column>
        <el-table-column label="价格" width="120">
          <template #default="{ row }">¥{{ row.price }}<small class="unit">/{{ row.unit || '份' }}</small></template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" align="center" />
        <el-table-column prop="sales" label="销量" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && products.length === 0" description="暂无农产品，点击右上角上架" />
    </el-card>

    <!-- 消费者：只读展示卡片 + 购买 -->
    <div v-else v-loading="loading" class="grid-wrap">
      <el-empty v-if="!loading && products.length === 0" description="暂无在售农产品" />
      <el-row v-else :gutter="16">
        <el-col v-for="p in products" :key="p.id" :xs="24" :sm="12" :md="8" :lg="6" class="card-col">
          <el-card shadow="hover" class="product-card" :body-style="{ padding: 0 }">
            <div class="cover-wrap">
              <el-image
                v-if="p.cover"
                :src="p.cover"
                :preview-src-list="[p.cover]"
                fit="cover"
                lazy
                preview-teleported
                class="cover"
              />
              <div v-else class="cover-empty">🌾</div>
            </div>
            <div class="info">
              <div class="name" :title="p.name">{{ p.name }}</div>
              <div class="meta">
                <el-tag size="small" effect="plain" type="success">{{ p.category?.name || '未分类' }}</el-tag>
              </div>
              <div class="origin" :title="p.origin?.name">
                <el-icon><Location /></el-icon>{{ p.origin?.name || '产地未公示' }}
              </div>
              <div class="price">
                ¥<span>{{ p.price }}</span><small>/{{ p.unit || '份' }}</small>
              </div>
              <div class="nums">库存 {{ p.stock }}</div>
            </div>
            <div class="card-qty" @click.stop>
              <span class="qty-label">购买数量</span>
              <el-input-number
                :model-value="getCardQty(p)"
                :min="1"
                :max="Math.max(1, Number(p.stock) || 1)"
                size="small"
                controls-position="right"
                :disabled="p.stock <= 0"
                @update:model-value="(v) => setCardQty(p.id, v)"
              />
            </div>
            <div class="actions">
              <el-button
                circle
                :type="isFav(p.id) ? 'danger' : 'default'"
                :icon="isFav(p.id) ? StarFilled : Star"
                :title="isFav(p.id) ? '取消收藏' : '收藏'"
                @click="toggleFav(p)"
              />
              <el-button :icon="ShoppingCart" :disabled="p.stock <= 0" @click="addToCart(p)">加购</el-button>
              <el-button type="primary" :disabled="p.stock <= 0" @click="openBuy(p)">
                {{ p.stock > 0 ? '立即购买' : '暂无库存' }}
              </el-button>
            </div>
            <div class="review-info">
              <div class="review-left" @click="openReviews(p)">
                <span class="rating">
                  <el-icon v-for="i in 5" :key="i" :color="i <= Math.round(getRating(p.id)) ? '#ffb300' : '#e0e0e0'"><Star /></el-icon>
                </span>
                <span class="review-count">{{ getReviewCount(p.id) }} 条评价</span>
              </div>
              <span class="sales-count">已售 {{ p.sales ?? 0 }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 新增/编辑弹窗（管理端） -->
    <el-dialog v-if="isManager" v-model="dialogVisible" :title="editingId ? '编辑农产品' : '上架农产品'" width="560px">
      <el-form :model="form" label-width="90px" :rules="rules" ref="formRef">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="产地">
          <el-select v-model="form.originId" placeholder="请选择产地" style="width: 100%">
            <el-option v-for="o in origins" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" :step="1" /></el-form-item>
        <el-form-item label="库存" prop="stock"><el-input-number v-model="form.stock" :min="0" :step="1" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="form.unit" placeholder="如：斤/公斤/份" style="width: 160px" /></el-form-item>
        <el-form-item label="封面图">
          <div class="cover-uploader">
            <el-image v-if="form.cover" :src="form.cover" fit="cover" class="cover-preview" />
            <div class="cover-actions">
              <el-upload
                action="/api/upload"
                name="file"
                accept="image/*"
                :show-file-list="false"
                :headers="uploadHeaders"
                :before-upload="beforeCoverUpload"
                :on-success="handleCoverSuccess"
                :on-error="handleCoverError"
              >
                <el-button :icon="Upload">{{ form.cover ? '更换图片' : '上传图片' }}</el-button>
              </el-upload>
              <el-button v-if="form.cover" text type="danger" @click="form.cover = ''">移除图片</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 购买弹窗（消费者） -->
    <el-dialog v-model="buyVisible" title="立即购买" width="480px">
      <div v-if="buy.product" class="buy-summary">
        <div class="buy-name">{{ buy.product.name }}</div>
        <div class="buy-meta">单价 ¥{{ buy.product.price }} / {{ buy.product.unit || '份' }} · 库存 {{ buy.product.stock }}</div>
      </div>
      <el-form label-width="90px" style="margin-top: 12px" v-loading="addressLoading">
        <el-form-item label="购买数量">
          <el-input-number v-model="buy.quantity" :min="1" :max="buy.product?.stock || 1" />
        </el-form-item>
        <el-form-item label="收货地址">
          <el-select
            v-if="addresses.length"
            :model-value="selectedAddressId"
            placeholder="选择个人中心已保存的地址"
            style="width: 100%"
            @change="(id) => onSelectAddress(id, buy)"
          >
            <el-option
              v-for="a in addresses"
              :key="a.id"
              :label="formatLabel(a)"
              :value="a.id"
            />
          </el-select>
          <div v-else class="addr-empty-tip">
            暂无收货地址，请先到
            <el-button type="primary" link @click="goProfileAddress">个人中心</el-button>
            添加
          </div>
        </el-form-item>
        <el-form-item label="收货人"><el-input v-model="buy.name" placeholder="可修改" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="buy.phone" placeholder="可修改" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="buy.address" type="textarea" :rows="2" placeholder="可修改" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="buy.remark" /></el-form-item>
        <el-form-item label="合计"><b style="color:#2e7d32">¥{{ buyTotal }}</b></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBuy">提交订单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewVisible" :title="`${currentProduct?.name || ''} - 商品评价`" width="620px">
      <div v-if="currentProduct" class="review-header">
        <div class="review-rating">
          <el-icon v-for="i in 5" :key="i" :color="i <= Math.round(getRating(currentProduct.id)) ? '#ffb300' : '#e0e0e0'" size="24"><Star /></el-icon>
          <span>{{ getRating(currentProduct.id).toFixed(1) }} 分</span>
        </div>
        <span class="review-count">{{ getReviewCount(currentProduct.id) }} 条评价</span>
      </div>
      <div v-if="currentReviews.length > 0" class="review-list">
        <div v-for="r in currentReviews" :key="r.id" class="review-item">
          <div class="review-item-header">
            <span class="reviewer">用户 #{{ r.userId }}</span>
            <div class="review-item-rating">
              <el-icon v-for="i in 5" :key="i" :color="i <= r.rating ? '#ffb300' : '#e0e0e0'"><Star /></el-icon>
            </div>
            <span class="review-time">{{ r.createTime }}</span>
          </div>
          <div class="review-content">{{ r.content || '（无文字评价）' }}</div>
        </div>
      </div>
      <el-empty v-else description="暂无评价" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Upload, Location, ShoppingCart, Star, StarFilled } from '@element-plus/icons-vue'
import { productApi, categoryApi, originApi, orderApi, cartApi, favoriteApi, reviewApi } from '../api'
import userStore, { role, hasRole } from '../stores/user'
import { useBuyerAddress } from '../composables/useBuyerAddress'

const router = useRouter()
const {
  addresses,
  selectedAddressId,
  addressLoading,
  formatLabel,
  loadAndPrefill,
  onSelectAddress
} = useBuyerAddress()

const isManager = computed(() => hasRole('admin', 'farmer'))
const isConsumer = computed(() => role() === 'consumer')
const uploadHeaders = computed(() => ({
  Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
}))

const products = ref([])
const categories = ref([])
const origins = ref([])
const favoriteIds = ref(new Set())
const cardQty = reactive({})

const getCardQty = (p) => {
  const id = p?.id
  if (id == null) return 1
  const max = Math.max(1, Number(p.stock) || 1)
  const cur = Number(cardQty[id]) || 1
  return Math.min(Math.max(1, cur), max)
}
const setCardQty = (id, v) => {
  const n = Math.max(1, Number(v) || 1)
  cardQty[id] = n
}
const reviewStats = ref({})
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()

const reviewVisible = ref(false)
const currentProduct = ref(null)
const currentReviews = ref([])

const filters = reactive({ categoryId: null, status: null, keyword: '' })
const form = reactive({ name: '', categoryId: null, originId: null, price: 0, stock: 0, unit: '', cover: '', description: '', status: 1 })
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

// 购买
const buyVisible = ref(false)
const buy = reactive({ product: null, quantity: 1, name: '', phone: '', address: '', remark: '' })
const buyTotal = computed(() => (buy.product ? (Number(buy.product.price) * buy.quantity).toFixed(2) : '0.00'))

const loadProducts = async () => {
  loading.value = true
  try {
    products.value = await productApi.list(filters)
    loadReviewStats(products.value).catch(() => {})
  } catch (e) {
    products.value = []
  } finally {
    loading.value = false
  }
}
const loadReviewStats = async (list) => {
  const next = { ...reviewStats.value }
  await Promise.all((list || []).map(async (p) => {
    try {
      const stats = await reviewApi.stats(p.id)
      next[p.id] = {
        count: Number(stats?.count) || 0,
        avgRating: Number(stats?.avgRating) || 0
      }
    } catch (e) {
      next[p.id] = { count: 0, avgRating: 0 }
    }
  }))
  reviewStats.value = next
}
const getRating = (productId) => Number(reviewStats.value[productId]?.avgRating) || 0
const getReviewCount = (productId) => Number(reviewStats.value[productId]?.count) || 0
const openReviews = async (p) => {
  currentProduct.value = p
  currentReviews.value = []
  reviewVisible.value = true
  try {
    currentReviews.value = await reviewApi.list({ productId: p.id })
    const stats = await reviewApi.stats(p.id)
    reviewStats.value = {
      ...reviewStats.value,
      [p.id]: {
        count: Number(stats?.count) || 0,
        avgRating: Number(stats?.avgRating) || 0
      }
    }
  } catch (e) {
    currentReviews.value = []
  }
}
const loadFavorites = async () => {
  if (!isConsumer.value) return
  try {
    const ids = await favoriteApi.ids()
    favoriteIds.value = new Set(ids || [])
  } catch (e) {
    favoriteIds.value = new Set()
  }
}
const loadOptions = async () => {
  categories.value = await categoryApi.list()
  origins.value = await originApi.list()
}
const isFav = (productId) => favoriteIds.value.has(productId)
const toggleFav = async (p) => {
  const res = await favoriteApi.toggle(p.id)
  const next = new Set(favoriteIds.value)
  if (res?.favorited) {
    next.add(p.id)
    ElMessage.success('已收藏')
  } else {
    next.delete(p.id)
    ElMessage.success('已取消收藏')
  }
  favoriteIds.value = next
}
const addToCart = async (p) => {
  const quantity = getCardQty(p)
  if (quantity > p.stock) return ElMessage.warning('购买数量不能超过库存')
  await cartApi.add({ productId: p.id, quantity })
  ElMessage.success(`已加入购物车 ×${quantity}`)
}
const resetFilter = () => {
  Object.assign(filters, { categoryId: null, status: null, keyword: '' })
  loadProducts()
}
const resetForm = () => {
  Object.assign(form, { name: '', categoryId: null, originId: null, price: 0, stock: 0, unit: '', cover: '', description: '', status: 1 })
}
const openCreate = () => { editingId.value = null; resetForm(); dialogVisible.value = true }
const openEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name, categoryId: row.category?.id || null, originId: row.origin?.id || null,
    price: row.price, stock: row.stock, unit: row.unit, cover: row.cover,
    description: row.description, status: row.status
  })
  dialogVisible.value = true
}
const submit = async () => {
  await formRef.value.validate()
  if (editingId.value) { await productApi.update(editingId.value, form); ElMessage.success('已更新') }
  else { await productApi.create(form); ElMessage.success('上架成功') }
  dialogVisible.value = false
  loadProducts()
}
const toggleStatus = async (row) => {
  const target = row.status === 1 ? 0 : 1
  await productApi.changeStatus(row.id, target)
  ElMessage.success(target === 1 ? '已上架' : '已下架')
  loadProducts()
}
const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'warning' })
  await productApi.remove(row.id)
  ElMessage.success('已删除')
  loadProducts()
}
const beforeCoverUpload = (file) => {
  const isImage = file.type && file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt5M) ElMessage.error('图片大小不能超过 5MB')
  return isImage && isLt5M
}
const handleCoverSuccess = (res) => {
  if (res && res.code === 0 && res.data?.url) {
    form.cover = res.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error((res && res.message) || '图片上传失败')
  }
}
const handleCoverError = () => {
  ElMessage.error('图片上传失败，请确认已登录')
}

const goProfileAddress = () => {
  buyVisible.value = false
  router.push({ path: '/profile', query: { tab: 'address' } })
}

const openBuy = async (p) => {
  buy.product = p
  buy.quantity = getCardQty(p)
  buy.remark = ''
  buy.name = ''
  buy.phone = ''
  buy.address = ''
  buyVisible.value = true
  await loadAndPrefill(buy)
}
const submitBuy = async () => {
  if (!buy.name) return ElMessage.warning('请填写收货人')
  if (!buy.phone) return ElMessage.warning('请填写联系电话')
  if (!buy.address) return ElMessage.warning('请填写收货地址')
  if (!buy.quantity || buy.quantity < 1) return ElMessage.warning('购买数量至少为 1')
  await orderApi.create({
    buyerName: buy.name,
    buyerPhone: buy.phone,
    buyerAddress: buy.address,
    remark: buy.remark,
    items: [{ productId: buy.product.id, quantity: buy.quantity }]
  })
  ElMessage.success('下单成功，请到「我的订单」完成支付')
  buyVisible.value = false
  loadProducts()
}

onMounted(() => { loadOptions(); loadProducts(); loadFavorites() })
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.toolbar-inner { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 8px; }
.filter { margin-bottom: -18px; }

/* 管理端表格缩略图 */
.thumb { width: 60px; height: 45px; border-radius: 6px; display: block; }
.thumb-empty { width: 60px; height: 45px; border-radius: 6px; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #eef5ee, #d7e9d8); font-size: 22px; }
.unit { color: #8a97a0; }

/* 消费者卡片 */
.grid-wrap { min-height: 200px; }
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

.card-qty {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 0 14px 10px;
}
.qty-label { font-size: 12px; color: #5a6a5a; flex: none; }
.card-qty :deep(.el-input-number) { width: 120px; }

.actions { display: flex; gap: 6px; padding: 10px 14px 14px; border-top: 1px solid #f3f5f3; align-items: center; }
.actions .el-button:not(.is-circle) { flex: 1; }

.cover-uploader { display: flex; flex-direction: column; align-items: flex-start; gap: 8px; }
.cover-preview { width: 120px; height: 120px; border-radius: 6px; border: 1px solid #ebeef5; }
.cover-actions { display: flex; align-items: center; gap: 8px; }

.buy-summary { padding: 12px 14px; background: #f6faf6; border-radius: 8px; }
.addr-empty-tip { font-size: 13px; color: #8a97a0; line-height: 1.6; }
.buy-name { font-size: 16px; font-weight: 700; color: #1f2d3d; }
.buy-meta { margin-top: 4px; font-size: 12px; color: #8a97a0; }

.review-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 0 14px 10px;
  font-size: 12px;
  color: #8a97a0;
}
.review-left {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  min-width: 0;
}
.review-info .rating { display: flex; }
.review-count { color: #8a97a0; }
.sales-count { flex: none; color: #2e7d32; font-weight: 600; }

.review-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #fafcf9;
  border-radius: 8px;
}
.review-rating { display: flex; align-items: center; gap: 8px; font-size: 18px; font-weight: 600; color: #ffb300; }
.review-list h4 { margin: 16px 0 12px; font-size: 14px; font-weight: 600; }
.review-item { padding: 12px; border-bottom: 1px solid #f3f5f3; }
.review-item:last-child { border-bottom: none; }
.review-item-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.reviewer { font-size: 13px; font-weight: 600; color: #1f2d3d; }
.review-item-rating { display: flex; }
.review-time { font-size: 12px; color: #aab4c0; margin-left: auto; }
.review-content { font-size: 13px; color: #5a6a5a; line-height: 1.6; }
</style>
