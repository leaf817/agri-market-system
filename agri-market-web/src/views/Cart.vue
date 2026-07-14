<template>
  <div>
    <el-card shadow="never" class="toolbar">
      <div class="toolbar-inner">
        <span class="title">我的购物车</span>
        <div class="ops">
          <el-button :disabled="!list.length" @click="clearAll">清空购物车</el-button>
          <el-button type="primary" :disabled="!selected.length" @click="openCheckout">
            结算（已选 {{ selected.length }}）
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" v-loading="loading">
      <el-empty v-if="!loading && list.length === 0" description="购物车还是空的，去选购农产品吧">
        <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
      </el-empty>

      <el-table
        v-else
        :data="list"
        border
        stripe
        @selection-change="onSelect"
        ref="tableRef"
      >
        <el-table-column type="selection" width="48" :selectable="rowSelectable" />
        <el-table-column label="商品" min-width="260">
          <template #default="{ row }">
            <div class="goods">
              <el-image
                v-if="row.product?.cover"
                :src="row.product.cover"
                fit="cover"
                class="thumb"
              />
              <div v-else class="thumb-empty">🌾</div>
              <div>
                <div class="name">{{ row.product?.name || '商品已失效' }}</div>
                <div class="sub">
                  <el-tag v-if="row.product?.status !== 1" size="small" type="info">已下架</el-tag>
                  <span v-else>产地 {{ row.product?.origin?.name || '-' }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">
            ¥{{ row.product?.price }}<small class="unit">/{{ row.product?.unit || '份' }}</small>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="160">
          <template #default="{ row }">
            <el-input-number
              :model-value="row.quantity"
              :min="1"
              :max="Math.max(row.product?.stock || 1, 1)"
              :disabled="!row.product || row.product.status !== 1"
              @change="(v) => changeQty(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="库存" width="80" align="center">
          <template #default="{ row }">{{ row.product?.stock ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            <b class="amount">¥{{ lineAmount(row) }}</b>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" text @click="removeItem(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="list.length" class="footer-bar">
        <span>已选 {{ selected.length }} 件，合计 <b class="amount">¥{{ selectedTotal }}</b></span>
        <el-button type="primary" :disabled="!selected.length" @click="openCheckout">去结算</el-button>
      </div>
    </el-card>

    <el-dialog v-model="checkoutVisible" title="确认结算" width="480px">
      <el-form label-width="90px" v-loading="addressLoading">
        <el-form-item label="收货地址">
          <el-select
            v-if="addresses.length"
            :model-value="selectedAddressId"
            placeholder="选择个人中心已保存的地址"
            style="width: 100%"
            @change="(id) => onSelectAddress(id, buyer)"
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
        <el-form-item label="收货人"><el-input v-model="buyer.name" placeholder="可修改" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="buyer.phone" placeholder="可修改" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="buyer.address" type="textarea" :rows="2" placeholder="可修改" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="buyer.remark" /></el-form-item>
        <el-form-item label="合计"><b class="amount">¥{{ selectedTotal }}</b></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCheckout">提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cartApi } from '../api'
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

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const selected = ref([])
const checkoutVisible = ref(false)
const buyer = reactive({ name: '', phone: '', address: '', remark: '' })

const lineAmount = (row) => {
  const price = Number(row.product?.price || 0)
  return (price * (row.quantity || 0)).toFixed(2)
}
const selectedTotal = computed(() =>
  selected.value.reduce((sum, row) => sum + Number(lineAmount(row)), 0).toFixed(2)
)
const rowSelectable = (row) => row.product && row.product.status === 1 && row.product.stock > 0

const load = async () => {
  loading.value = true
  try {
    list.value = await cartApi.list()
    selected.value = []
  } finally {
    loading.value = false
  }
}

const onSelect = (rows) => { selected.value = rows }

const changeQty = async (row, qty) => {
  if (!qty || qty < 1) return
  try {
    await cartApi.update(row.id, qty)
    row.quantity = qty
  } catch (e) {
    await load()
  }
}

const removeItem = async (row) => {
  await ElMessageBox.confirm(`从购物车移除「${row.product?.name || '该商品'}」？`, '提示', { type: 'warning' })
  await cartApi.remove(row.id)
  ElMessage.success('已移除')
  load()
}

const clearAll = async () => {
  await ElMessageBox.confirm('确定清空购物车吗？', '提示', { type: 'warning' })
  await cartApi.clear()
  ElMessage.success('购物车已清空')
  load()
}

const goProfileAddress = () => {
  checkoutVisible.value = false
  router.push({ path: '/profile', query: { tab: 'address' } })
}

const openCheckout = async () => {
  if (!selected.value.length) return ElMessage.warning('请先勾选要结算的商品')
  buyer.remark = ''
  buyer.name = ''
  buyer.phone = ''
  buyer.address = ''
  checkoutVisible.value = true
  await loadAndPrefill(buyer)
}

const submitCheckout = async () => {
  if (!buyer.name) return ElMessage.warning('请填写收货人')
  if (!buyer.phone) return ElMessage.warning('请填写联系电话')
  if (!buyer.address) return ElMessage.warning('请填写收货地址')
  submitting.value = true
  try {
    await cartApi.checkout({
      cartItemIds: selected.value.map((r) => r.id),
      buyerName: buyer.name,
      buyerPhone: buyer.phone,
      buyerAddress: buyer.address,
      remark: buyer.remark
    })
    ElMessage.success('下单成功，请到「我的订单」完成支付')
    checkoutVisible.value = false
    await load()
    router.push('/orders')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.toolbar-inner { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
.title { font-size: 16px; font-weight: 700; color: #1f2d3d; }
.ops { display: flex; gap: 8px; }

.goods { display: flex; align-items: center; gap: 12px; }
.thumb { width: 56px; height: 56px; border-radius: 8px; flex: none; }
.thumb-empty {
  width: 56px; height: 56px; border-radius: 8px; flex: none;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #eef5ee, #d7e9d8); font-size: 22px;
}
.name { font-weight: 650; color: #1f2d3d; }
.sub { margin-top: 4px; font-size: 12px; color: #8a97a0; }
.unit { color: #8a97a0; margin-left: 2px; }
.amount { color: #2e7d32; }

.footer-bar {
  margin-top: 16px;
  display: flex; justify-content: flex-end; align-items: center; gap: 16px;
  padding-top: 12px; border-top: 1px solid #eef2ee;
}
.addr-empty-tip { font-size: 13px; color: #8a97a0; line-height: 1.6; }
</style>
