<template>
  <div class="orders-page">
    <el-card shadow="never" class="toolbar">
      <div class="toolbar-inner">
        <div class="title">{{ isConsumer ? '我的订单' : '订单管理' }}</div>
        <div class="toolbar-ops">
          <el-input
            v-model="keyword"
            clearable
            :placeholder="isConsumer ? '搜索订单号 / 商品' : '搜索订单号 / 买家 / 电话'"
            style="width: 240px"
            :prefix-icon="Search"
          />
          <el-button type="primary" :icon="Plus" @click="openCreate">
            {{ isConsumer ? '去下单' : '模拟订单' }}
          </el-button>
        </div>
      </div>
    </el-card>

    <div class="filter-bar">
      <button
        v-for="tab in statusTabs"
        :key="tab.value"
        type="button"
        class="filter-chip"
        :class="{ active: statusFilter === tab.value }"
        @click="statusFilter = tab.value"
      >
        {{ tab.label }}
        <span v-if="tab.count != null" class="chip-count">{{ tab.count }}</span>
      </button>
    </div>

    <div class="page-body" :class="{ 'with-side': isConsumer }" v-loading="loading">
      <div class="main-col">
        <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无相关订单">
          <el-button v-if="isConsumer" type="primary" @click="$router.push('/products')">去选购</el-button>
        </el-empty>

        <div v-else-if="isConsumer" class="order-list">
          <article
            v-for="row in filteredOrders"
            :key="row.id"
            class="order-card"
            :class="{ pending: isPending(row) }"
            @click="openDetail(row)"
            @contextmenu.prevent="onOrderContextMenu($event, row)"
          >
            <header class="order-card-head">
              <div class="order-meta">
                <span class="order-no">{{ row.orderNo }}</span>
                <span class="order-time">{{ formatTime(row.createTime) }}</span>
              </div>
              <span class="status-pill" :class="statusClass(row.status)">{{ statusLabel(row.status) }}</span>
            </header>

            <div class="order-body">
              <div class="goods-line">
                <span class="goods-text">{{ itemSummary(row) }}</span>
                <span class="goods-count">共 {{ itemCount(row) }} 件</span>
              </div>
              <div class="amount-line">
                实付 <b class="amount">¥{{ formatMoney(row.totalAmount) }}</b>
              </div>
            </div>

            <footer v-if="isPending(row)" class="order-card-foot" @click.stop>
              <div class="foot-actions">
                <el-button size="small" round @click="cancelOrder(row)">取消订单</el-button>
                <el-button size="small" type="primary" round @click="payOrder(row)">确认支付</el-button>
              </div>
            </footer>
          </article>
        </div>

        <el-card v-else shadow="never" class="table-card">
          <el-table
            :data="filteredOrders"
            border
            stripe
            class="clickable-table"
            @row-click="openDetail"
            @row-contextmenu="onTableContextMenu"
          >
            <el-table-column prop="orderNo" label="订单号" min-width="180" />
            <el-table-column label="买家" min-width="140">
              <template #default="{ row }">
                <div class="buyer-cell">
                  <div class="buyer-name">{{ row.buyerName }}</div>
                  <div class="buyer-phone">{{ row.buyerPhone || '-' }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="金额" width="110">
              <template #default="{ row }">
                <b class="amount">¥{{ formatMoney(row.totalAmount) }}</b>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <span class="status-pill compact" :class="statusClass(row.status)">{{ statusLabel(row.status) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="下单时间" width="160">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="110" fixed="right">
              <template #default="{ row }">
                <div class="op-cell" @click.stop>
                  <el-button size="small" text type="primary" @click="openStatus(row)">变更状态</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- 右侧概览，填满宽屏空白 -->
      <aside v-if="isConsumer" class="side-col">
        <div class="side-card summary-card">
          <div class="side-title">订单概览</div>
          <div class="stat-grid">
            <div class="stat-item" @click="statusFilter = 'ALL'">
              <div class="stat-num">{{ orders.length }}</div>
              <div class="stat-label">全部</div>
            </div>
            <div class="stat-item warn" @click="statusFilter = 'PENDING'">
              <div class="stat-num">{{ countByStatus('PENDING') }}</div>
              <div class="stat-label">待付款</div>
            </div>
            <div class="stat-item" @click="statusFilter = 'PAID'">
              <div class="stat-num">{{ countByStatus('PAID') }}</div>
              <div class="stat-label">待发货</div>
            </div>
            <div class="stat-item ok" @click="statusFilter = 'COMPLETED'">
              <div class="stat-num">{{ countByStatus('COMPLETED') }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
          <div class="spent-row">
            <span>累计消费</span>
            <b class="amount">¥{{ totalSpent }}</b>
          </div>
        </div>

        <div v-if="pendingOrders.length" class="side-card tip-card">
          <div class="side-title">待付款提醒</div>
          <div v-for="o in pendingOrders.slice(0, 3)" :key="o.id" class="tip-row">
            <div class="tip-info">
              <div class="tip-name">{{ itemSummary(o) }}</div>
              <div class="tip-amt">¥{{ formatMoney(o.totalAmount) }}</div>
            </div>
            <el-button size="small" type="primary" round @click="payOrder(o)">支付</el-button>
          </div>
        </div>

        <div class="side-card link-card">
          <div class="side-title">快捷入口</div>
          <button type="button" class="link-row" @click="$router.push('/products')">继续选购农产品</button>
          <button type="button" class="link-row" @click="$router.push('/cart')">查看购物车</button>
          <button type="button" class="link-row" @click="$router.push({ path: '/profile', query: { tab: 'address' } })">管理收货地址</button>
        </div>
      </aside>
    </div>

    <el-dialog v-model="detailVisible" title="订单明细" width="640px">
      <div v-if="current" class="detail">
        <div class="detail-banner">
          <div>
            <div class="detail-no">{{ current.orderNo }}</div>
            <div class="detail-time">下单于 {{ formatTime(current.createTime) }}</div>
          </div>
          <span class="status-pill" :class="statusClass(current.status)">{{ statusLabel(current.status) }}</span>
        </div>

        <div class="detail-grid">
          <div><label>收货人</label><span>{{ current.buyerName }}</span></div>
          <div><label>电话</label><span>{{ current.buyerPhone || '-' }}</span></div>
          <div class="full"><label>地址</label><span>{{ current.buyerAddress || '-' }}</span></div>
          <div v-if="current.remark" class="full"><label>备注</label><span>{{ current.remark }}</span></div>
        </div>

        <el-table :data="current.items" border size="small">
          <el-table-column prop="productName" label="商品" min-width="140" />
          <el-table-column label="单价" width="100">
            <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="70" align="center" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="评价" min-width="140" align="center">
            <template #default="{ row }">
              <!-- 消费者：可去评价 -->
              <template v-if="isConsumer">
                <el-button
                  v-if="normalizeStatus(current.status) === 'COMPLETED' && !row.reviewed"
                  size="small"
                  type="primary"
                  link
                  @click.stop="openReview(row)"
                >去评价</el-button>
                <el-button
                  v-else-if="row.reviewed"
                  size="small"
                  type="success"
                  link
                  @click.stop="viewReview(row)"
                >已评价</el-button>
                <span v-else class="text-gray">—</span>
              </template>
              <!-- 农户/管理员：只看消费者评价 -->
              <template v-else>
                <el-button
                  v-if="row.review"
                  size="small"
                  type="primary"
                  link
                  @click.stop="viewReview(row)"
                >查看评价</el-button>
                <span v-else-if="normalizeStatus(current.status) === 'COMPLETED'" class="text-gray">暂无评价</span>
                <span v-else class="text-gray">—</span>
              </template>
            </template>
          </el-table-column>
        </el-table>
        <div class="detail-total">合计 <b class="amount">¥{{ formatMoney(current.totalAmount) }}</b></div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="isConsumer && current && isPending(current)" @click="cancelOrder(current)">取消订单</el-button>
        <el-button v-if="isConsumer && current && isPending(current)" type="primary" @click="payOrder(current)">确认支付</el-button>
        <el-button
          v-if="isManager && current"
          type="primary"
          @click="openStatus(current); detailVisible = false"
        >变更状态</el-button>
      </template>
    </el-dialog>

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

    <el-dialog v-model="reviewViewVisible" title="消费者评价" width="480px">
      <div v-if="viewingReview" class="review-view">
        <div class="review-view-name">{{ viewingReview.productName }}</div>
        <div class="review-view-rate">
          <el-rate :model-value="viewingReview.rating" disabled show-score text-color="#ffb300" />
        </div>
        <div class="review-view-content">{{ viewingReview.content || '（无文字评价）' }}</div>
        <div v-if="viewingReview.createTime" class="review-view-time">{{ formatTime(viewingReview.createTime) }}</div>
      </div>
      <template #footer>
        <el-button type="primary" @click="reviewViewVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="createVisible" :title="isConsumer ? '去下单' : '模拟订单'" width="620px">
      <el-form label-width="90px" v-loading="isConsumer && addressLoading">
        <template v-if="isConsumer">
          <el-form-item label="收货地址">
            <el-select
              v-if="addresses.length"
              :model-value="selectedAddressId"
              placeholder="选择个人中心已保存的地址"
              style="width: 100%"
              @change="(id) => onSelectAddress(id, buyer)"
            >
              <el-option v-for="a in addresses" :key="a.id" :label="formatLabel(a)" :value="a.id" />
            </el-select>
            <div v-else class="addr-empty-tip">
              暂无收货地址，请先到
              <el-button type="primary" link @click="goProfileAddress">个人中心</el-button>
              添加
            </div>
          </el-form-item>
        </template>
        <el-form-item :label="isConsumer ? '收货人' : '买家'"><el-input v-model="buyer.name" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="buyer.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="buyer.address" type="textarea" :rows="2" /></el-form-item>
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
        <el-form-item label="合计"><b class="amount">¥{{ previewTotal }}</b></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder">提交订单</el-button>
      </template>
    </el-dialog>

    <!-- 消费者右键菜单（放在单根内，避免 Transition 无法动画导致切页空白） -->
    <teleport to="body">
      <div
        v-if="ctxMenu.visible && ctxMenu.row"
        class="order-ctx-menu"
        :style="{ left: ctxMenu.x + 'px', top: ctxMenu.y + 'px' }"
        @click.stop
      >
        <button type="button" class="ctx-item danger" @click="removeOrder(ctxMenu.row)">删除订单记录</button>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Search } from '@element-plus/icons-vue'
import { orderApi, productApi, reviewApi } from '../api'
import { role, hasRole } from '../stores/user'
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

const orders = ref([])
const availableProducts = ref([])
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref('ALL')
const detailVisible = ref(false)
const statusVisible = ref(false)
const createVisible = ref(false)
const reviewVisible = ref(false)
const reviewViewVisible = ref(false)
const current = ref(null)
const currentId = ref(null)
const targetStatus = ref('')
const reviewingItem = ref(null)
const viewingReview = ref(null)
const reviewForm = reactive({ rating: 5, content: '' })
const ctxMenu = reactive({ visible: false, x: 0, y: 0, row: null })

const statusMap = {
  PENDING: { label: '待付款', type: 'info' },
  PAID: { label: '待发货', type: 'warning' },
  SHIPPED: { label: '已发货', type: 'primary' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'danger' }
}
const statusFrom = ref('')
/** 全部状态可随意前进/回退（含已取消） */
const statusOptions = computed(() =>
  Object.entries(statusMap).map(([value, v]) => ({ value, label: v.label }))
)
const normalizeStatus = (s) => String(s || '').toUpperCase()
const statusLabel = (s) => statusMap[normalizeStatus(s)]?.label || s
const statusClass = (s) => `is-${normalizeStatus(s).toLowerCase()}`
const isPending = (row) => normalizeStatus(row?.status) === 'PENDING'
const isCancelled = (row) => normalizeStatus(row?.status) === 'CANCELLED'

const countByStatus = (status) =>
  orders.value.filter((o) => normalizeStatus(o.status) === status).length

const statusTabs = computed(() => [
  { value: 'ALL', label: '全部', count: orders.value.length },
  { value: 'PENDING', label: '待付款', count: countByStatus('PENDING') },
  { value: 'PAID', label: '待发货', count: countByStatus('PAID') },
  { value: 'SHIPPED', label: '已发货', count: countByStatus('SHIPPED') },
  { value: 'COMPLETED', label: '已完成', count: countByStatus('COMPLETED') },
  { value: 'CANCELLED', label: '已取消', count: countByStatus('CANCELLED') }
])

const filteredOrders = computed(() => {
  let list = orders.value
  if (statusFilter.value !== 'ALL') {
    list = list.filter((o) => normalizeStatus(o.status) === statusFilter.value)
  }
  const q = keyword.value.trim().toLowerCase()
  if (q) {
    list = list.filter((o) => {
      const no = String(o.orderNo || '').toLowerCase()
      const name = String(o.buyerName || '').toLowerCase()
      const phone = String(o.buyerPhone || '').toLowerCase()
      const goods = (o.items || []).map((it) => it.productName || '').join(' ').toLowerCase()
      return no.includes(q) || name.includes(q) || phone.includes(q) || goods.includes(q)
    })
  }
  return list
})

const pendingOrders = computed(() =>
  orders.value.filter((o) => normalizeStatus(o.status) === 'PENDING')
)

const totalSpent = computed(() => {
  const sum = orders.value
    .filter((o) => !['CANCELLED', 'PENDING'].includes(normalizeStatus(o.status)))
    .reduce((acc, o) => acc + Number(o.totalAmount || 0), 0)
  return sum.toFixed(2)
})

const formatTime = (t) => {
  if (!t) return '-'
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t).replace('T', ' ').slice(0, 19)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const formatMoney = (n) => Number(n || 0).toFixed(2)

const itemSummary = (row) => {
  const list = row.items || []
  if (!list.length) return '订单商品'
  const names = list.map((it) => it.productName).filter(Boolean)
  if (names.length <= 2) return names.join('、')
  return `${names.slice(0, 2).join('、')} 等`
}

const itemCount = (row) =>
  (row.items || []).reduce((sum, it) => sum + (it.quantity || 0), 0) || (row.items?.length || 0)

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

const openDetail = async (row, column) => {
  // 点操作列时不打开详情（避免抢走「变更状态」点击）
  if (column && (column.label === '操作' || column.fixed === 'right')) return

  try {
    const order = await orderApi.get(row.id)
    const items = order.items || []
    const productIds = [...new Set(
      items.map((it) => it.product?.id).filter(Boolean)
    )]

    const reviewByProduct = new Map()
    try {
      if (isConsumer.value) {
        const mine = (await reviewApi.list()) || []
        mine.forEach((r) => {
          const pid = r.product?.id
          if (pid && productIds.includes(pid) && !reviewByProduct.has(pid)) {
            reviewByProduct.set(pid, r)
          }
        })
      } else if (order.customerId) {
        await Promise.all(productIds.map(async (pid) => {
          const list = (await reviewApi.list({ productId: pid })) || []
          const match = list.find((r) => r.userId === order.customerId)
          if (match) reviewByProduct.set(pid, match)
        }))
      }
    } catch (e) {
      /* 评价加载失败不影响看订单 */
    }

    items.forEach((item) => {
      const pid = item.product?.id
      const rev = pid ? reviewByProduct.get(pid) : null
      item.reviewed = !!rev
      item.review = rev || null
    })
    order.items = items
    current.value = order
    detailVisible.value = true
  } catch (e) {
    ElMessage.error(e?.message || '订单详情加载失败')
  }
}

const openReview = (item) => {
  if (!isConsumer.value) return
  reviewingItem.value = item
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewVisible.value = true
}

const viewReview = (item) => {
  const r = item.review
  if (!r) return ElMessage.info('暂无评价内容')
  viewingReview.value = {
    productName: item.productName,
    rating: r.rating,
    content: r.content,
    createTime: r.createTime
  }
  reviewViewVisible.value = true
}

const submitReview = async () => {
  if (!isConsumer.value) return ElMessage.warning('仅消费者可评价')
  if (!reviewForm.content.trim()) return ElMessage.warning('请填写评价内容')
  const productId = reviewingItem.value?.product?.id
  if (!productId) return ElMessage.error('无法评价：商品信息缺失')
  const saved = await reviewApi.create(productId, reviewForm.rating, reviewForm.content)
  ElMessage.success('评价成功')
  reviewingItem.value.reviewed = true
  reviewingItem.value.review = saved
  reviewVisible.value = false
}

const openStatus = (row) => {
  currentId.value = row.id
  statusFrom.value = normalizeStatus(row.status)
  targetStatus.value = statusFrom.value
  statusVisible.value = true
}

const submitStatus = async () => {
  await orderApi.changeStatus(currentId.value, targetStatus.value)
  ElMessage.success('状态已更新')
  statusVisible.value = false
  load()
}

const onOrderContextMenu = (e, row) => {
  ctxMenu.visible = true
  ctxMenu.x = e.clientX
  ctxMenu.y = e.clientY
  ctxMenu.row = row
}

const onTableContextMenu = (row, _column, event) => {
  event.preventDefault()
  onOrderContextMenu(event, row)
}

const hideCtxMenu = () => {
  ctxMenu.visible = false
  ctxMenu.row = null
}

const removeOrder = async (row) => {
  hideCtxMenu()
  const st = normalizeStatus(row.status)
  if (st !== 'PENDING' && st !== 'CANCELLED') {
    return ElMessage.warning('仅待付款或已取消的订单可以删除')
  }
  const tip = st === 'PENDING'
    ? `确定删除订单 ${row.orderNo}？将回滚已扣库存。`
    : `确定删除订单 ${row.orderNo}？删除后不可恢复。`
  await ElMessageBox.confirm(tip, '删除订单', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await orderApi.remove(row.id)
  ElMessage.success('订单已删除')
  detailVisible.value = false
  load()
}

const payOrder = async (row) => {
  await ElMessageBox.confirm(
    `确认支付订单 ${row.orderNo}？金额 ¥${formatMoney(row.totalAmount)}（教学模拟，无真实扣款）`,
    '确认支付',
    { type: 'info', confirmButtonText: '确认支付', cancelButtonText: '再想想' }
  )
  await orderApi.pay(row.id)
  ElMessage.success('支付成功，等待发货')
  detailVisible.value = false
  load()
}

const cancelOrder = async (row) => {
  await ElMessageBox.confirm(
    `确定取消订单 ${row.orderNo}？取消后将恢复库存。`,
    '取消订单',
    { type: 'warning', confirmButtonText: '确定取消', cancelButtonText: '返回' }
  )
  await orderApi.cancel(row.id)
  ElMessage.success('订单已取消')
  detailVisible.value = false
  load()
}

const goProfileAddress = () => {
  createVisible.value = false
  router.push({ path: '/profile', query: { tab: 'address' } })
}

const openCreate = async () => {
  Object.assign(buyer, { name: '', phone: '', address: '', remark: '' })
  items.splice(0, items.length, { productId: null, quantity: 1 })
  availableProducts.value = await productApi.list({ status: 1 })
  createVisible.value = true
  if (isConsumer.value) await loadAndPrefill(buyer)
}

const submitOrder = async () => {
  const validItems = items.filter((it) => it.productId && it.quantity > 0)
  if (!buyer.name) return ElMessage.warning('请填写买家姓名')
  if (isConsumer.value) {
    if (!buyer.phone) return ElMessage.warning('请填写联系电话')
    if (!buyer.address) return ElMessage.warning('请填写收货地址')
  }
  if (validItems.length === 0) return ElMessage.warning('请至少选择一件商品')
  await orderApi.create({
    buyerName: buyer.name,
    buyerPhone: buyer.phone,
    buyerAddress: buyer.address,
    remark: buyer.remark,
    items: validItems.map((it) => ({ productId: it.productId, quantity: it.quantity }))
  })
  ElMessage.success(isConsumer.value ? '下单成功，请确认支付' : '下单成功，已扣减库存')
  createVisible.value = false
  statusFilter.value = 'PENDING'
  load()
}

onMounted(() => {
  load()
  document.addEventListener('click', hideCtxMenu)
})
onUnmounted(() => {
  document.removeEventListener('click', hideCtxMenu)
})
</script>

<style scoped>
.orders-page { width: 100%; }

.toolbar {
  margin-bottom: 14px;
  border: none;
  background: linear-gradient(135deg, #f7fbf7 0%, #eef6ef 100%);
}
.toolbar-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.toolbar-ops { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.title { font-size: 18px; font-weight: 700; color: #1f2d3d; }

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
.filter-chip {
  border: 1px solid #dce6dc;
  background: #fff;
  color: #5a6a5a;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.filter-chip:hover { border-color: #8fbf8f; color: #2e7d32; }
.filter-chip.active {
  background: #2e7d32;
  border-color: #2e7d32;
  color: #fff;
}
.chip-count {
  margin-left: 6px;
  opacity: 0.75;
  font-variant-numeric: tabular-nums;
}

.page-body {
  min-height: 320px;
}
.page-body.with-side {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  align-items: start;
}

.main-col { min-width: 0; }

.order-list { display: flex; flex-direction: column; gap: 12px; }
.order-card {
  background: #fff;
  border: 1px solid #e6eee6;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.18s ease, border-color 0.18s ease;
}
.order-card:hover {
  border-color: #c5dcc5;
  box-shadow: 0 8px 24px rgba(46, 125, 50, 0.08);
}
.order-card.pending {
  border-color: #c8e0c8;
  background: linear-gradient(180deg, #fbfefb 0%, #fff 40%);
}

.order-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f7faf7;
  border-bottom: 1px solid #eef3ee;
}
.order-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 10px 14px;
}
.order-no { font-weight: 650; color: #1f2d3d; font-size: 13px; }
.order-time { font-size: 12px; color: #8a97a0; }

.order-body { padding: 14px 16px; }
.goods-line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}
.goods-text { color: #304038; font-size: 14px; line-height: 1.5; }
.goods-count { flex: none; font-size: 12px; color: #8a97a0; }
.amount-line { margin-top: 10px; text-align: right; font-size: 13px; color: #5a6a5a; }
.amount { color: #2e7d32; font-size: 18px; font-weight: 800; }

.order-card-foot {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 10px 16px 14px;
  border-top: 1px dashed #e8efe8;
}
.foot-actions { display: flex; gap: 8px; }

.table-card { border: none; }
/* 管理端表格行悬停：对齐消费者订单卡片 */
.clickable-table {
  --el-table-row-hover-bg-color: #f7faf7;
}
.clickable-table :deep(.el-table__body tr) {
  cursor: pointer;
  transition: background-color 0.18s ease, box-shadow 0.18s ease;
}
.clickable-table :deep(.el-table__body tr:hover > td.el-table__cell) {
  background-color: #f7faf7 !important;
}
.clickable-table :deep(.el-table__body tr:hover) {
  box-shadow: 0 8px 24px rgba(46, 125, 50, 0.08);
}
.op-cell { display: inline-flex; align-items: center; }

.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.2;
}
.status-pill.compact { padding: 3px 8px; }
.status-pill.is-pending { background: #eef1f4; color: #5b6770; }
.status-pill.is-paid { background: #fff3e0; color: #ef6c00; }
.status-pill.is-shipped { background: #e3f2fd; color: #1565c0; }
.status-pill.is-completed { background: #e8f5e9; color: #2e7d32; }
.status-pill.is-cancelled { background: #fdecea; color: #c62828; }

.side-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: sticky;
  top: 12px;
}
.side-card {
  background: #fff;
  border: 1px solid #e6eee6;
  border-radius: 14px;
  padding: 16px;
}
.side-title {
  font-size: 14px;
  font-weight: 700;
  color: #1f2d3d;
  margin-bottom: 12px;
}
.summary-card {
  background: linear-gradient(160deg, #f4faf4 0%, #fff 55%);
}
.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.stat-item {
  border: 1px solid #e8efe8;
  border-radius: 10px;
  padding: 10px 8px;
  text-align: center;
  cursor: pointer;
  background: #fff;
  transition: border-color 0.15s ease;
}
.stat-item:hover { border-color: #9ccc9c; }
.stat-item.warn .stat-num { color: #ef6c00; }
.stat-item.ok .stat-num { color: #2e7d32; }
.stat-num { font-size: 22px; font-weight: 800; color: #1f2d3d; line-height: 1.1; }
.stat-label { margin-top: 4px; font-size: 12px; color: #8a97a0; }
.spent-row {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e0ebe0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #5a6a5a;
}

.tip-card { border-color: #f0d9b5; background: #fffaf3; }
.tip-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 0;
}
.tip-row + .tip-row { border-top: 1px dashed #f0e0c8; }
.tip-name {
  font-size: 13px;
  color: #304038;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
}
.tip-amt { font-size: 12px; color: #ef6c00; font-weight: 650; margin-top: 2px; }

.link-row {
  display: block;
  width: 100%;
  text-align: left;
  border: none;
  background: #f7faf7;
  color: #2e7d32;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 13px;
  cursor: pointer;
  margin-bottom: 8px;
}
.link-row:last-child { margin-bottom: 0; }
.link-row:hover { background: #eaf5ea; }

.buyer-cell .buyer-name { font-weight: 600; color: #1f2d3d; }
.buyer-cell .buyer-phone { margin-top: 2px; font-size: 12px; color: #8a97a0; }

.detail-banner {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 14px;
  border-radius: 12px;
  background: #f6faf6;
}
.detail-no { font-weight: 700; color: #1f2d3d; }
.detail-time { margin-top: 4px; font-size: 12px; color: #8a97a0; }
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 16px;
  margin-bottom: 14px;
  font-size: 13px;
}
.detail-grid .full { grid-column: 1 / -1; }
.detail-grid label {
  display: block;
  margin-bottom: 2px;
  color: #8a97a0;
  font-size: 12px;
}
.detail-grid span { color: #304038; }
.detail-total { margin-top: 12px; text-align: right; font-size: 14px; color: #5a6a5a; }

.order-item { display: flex; align-items: center; margin-bottom: 8px; }
.addr-empty-tip { font-size: 13px; color: #8a97a0; line-height: 1.6; }
.text-success { color: #2e7d32; }
.text-gray { color: #aab4c0; }

.review-form { padding: 10px 0; }
.review-view { padding: 4px 0 8px; }
.review-view-name { font-weight: 700; color: #1f2d3d; margin-bottom: 10px; }
.review-view-rate { margin-bottom: 12px; }
.review-view-content {
  padding: 12px 14px;
  background: #f6faf6;
  border-radius: 10px;
  color: #304038;
  line-height: 1.6;
  white-space: pre-wrap;
}
.review-view-time { margin-top: 10px; font-size: 12px; color: #8a97a0; }
.form-row { display: flex; align-items: flex-start; margin-bottom: 16px; }
.form-row label { width: 80px; flex-shrink: 0; font-weight: 500; color: #5a6a5a; padding-top: 4px; }
.form-row > span { flex: 1; font-weight: 500; }
.form-row :deep(.el-input) { flex: 1; }
.rating-row { align-items: center; }
.rating-wrapper { flex: 1; }
.rating-wrapper :deep(.el-rate) { font-size: 28px; display: inline-flex; flex-wrap: nowrap; }
.rating-wrapper :deep(.el-rate__icon) { margin-right: 6px; }
.rating-wrapper :deep(.el-rate__text) { margin-left: 12px; font-size: 18px; }

@media (max-width: 1100px) {
  .page-body.with-side { grid-template-columns: 1fr; }
  .side-col { position: static; display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
}
@media (max-width: 800px) {
  .side-col { grid-template-columns: 1fr; }
  .detail-grid { grid-template-columns: 1fr; }
  .goods-line { flex-direction: column; }
}
</style>

<style>
.order-ctx-menu {
  position: fixed;
  z-index: 4000;
  min-width: 160px;
  padding: 6px;
  background: #fff;
  border: 1px solid #e6eee6;
  border-radius: 10px;
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.12);
}
.order-ctx-menu .ctx-item {
  display: block;
  width: 100%;
  border: none;
  background: transparent;
  text-align: left;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  color: #304038;
  cursor: pointer;
}
.order-ctx-menu .ctx-item:hover { background: #f3f8f3; }
.order-ctx-menu .ctx-item.danger { color: #c62828; }
.order-ctx-menu .ctx-item.danger:hover { background: #fdecea; }
</style>
