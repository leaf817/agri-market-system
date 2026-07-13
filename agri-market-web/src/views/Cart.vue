<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>购物车</span>
        <el-button v-if="items.length > 0" text type="danger" :icon="Delete" @click="clearCart">清空购物车</el-button>
      </div>
    </template>

    <div v-loading="loading">
      <el-empty v-if="!loading && items.length === 0" description="购物车是空的，去选购农产品吧" />
      <div v-else class="cart-list">
        <div v-for="item in items" :key="item.id" class="cart-item">
          <div class="item-cover">
            <el-image v-if="item.product.cover" :src="item.product.cover" fit="cover" class="cover" />
            <div v-else class="cover-empty">🌾</div>
          </div>
          <div class="item-info">
            <div class="item-name">{{ item.product.name }}</div>
            <div class="item-meta">
              <el-tag size="small" effect="plain" type="success">{{ item.product.category?.name || '未分类' }}</el-tag>
              <span class="origin">{{ item.product.origin?.name || '产地未公示' }}</span>
            </div>
            <div class="item-price">¥{{ item.product.price }} <small>/{{ item.product.unit || '份' }}</small></div>
            <div class="item-stock">库存 {{ item.product.stock }}</div>
          </div>
          <div class="item-right">
            <el-input-number v-model="item.quantity" :min="1" :max="item.product.stock" @change="updateQty(item)" class="quantity" />
            <div class="item-total">¥{{ (item.product.price * item.quantity).toFixed(2) }}</div>
            <el-button text type="danger" :icon="Delete" @click="removeItem(item)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="items.length > 0" class="cart-footer">
      <div class="total-info">
        <span>共 {{ totalCount }} 件商品</span>
        <span class="total-price">合计：<b>¥{{ totalAmount }}</b></span>
      </div>
      <el-button type="primary" size="large" :icon="ShoppingCart" @click="checkout">去结算</el-button>
    </div>

    <el-dialog v-model="checkoutVisible" title="确认订单" width="560px">
      <el-form label-width="90px">
        <el-form-item label="收货地址">
          <div v-if="addresses.length > 0">
            <div v-for="addr in addresses" :key="addr.id" 
                 class="address-option" :class="{ 'is-selected': buy.addressId === addr.id }"
                 @click="selectAddress(addr)">
              <div class="addr-main">
                <span>{{ addr.name }} {{ addr.phone }}</span>
                <el-tag v-if="addr.isDefault" size="small" type="success">默认</el-tag>
              </div>
              <div class="addr-detail">{{ addr.address }}</div>
            </div>
          </div>
          <div v-else class="no-address">暂无收货地址，请在个人中心添加</div>
        </el-form-item>
        <el-form-item label="收货人"><el-input v-model="buy.name" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="buy.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="buy.address" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="buy.remark" /></el-form-item>
        <el-form-item label="商品清单">
          <el-table :data="items" border size="small">
            <el-table-column prop="product.name" label="商品" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column label="小计" width="100"><template #default="{ row }">¥{{ (row.product.price * row.quantity).toFixed(2) }}</template></el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="合计"><b style="color:#2e7d32">¥{{ totalAmount }}</b></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder">提交订单</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, ShoppingCart } from '@element-plus/icons-vue'
import { cartApi, orderApi, addressApi } from '../api'
import userStore from '../stores/user'

const items = ref([])
const loading = ref(false)
const checkoutVisible = ref(false)
const addresses = ref([])

const buy = reactive({ addressId: null, name: '', phone: '', address: '', remark: '' })

const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
const totalAmount = computed(() => items.value.reduce((sum, item) => sum + Number(item.product.price) * item.quantity, 0).toFixed(2))

const load = async () => {
  loading.value = true
  try { items.value = await cartApi.list() } finally { loading.value = false }
}

const updateQty = async (item) => {
  await cartApi.updateQuantity(item.id, item.quantity)
}

const removeItem = async (item) => {
  await ElMessageBox.confirm(`确定删除「${item.product.name}」吗？`, '提示', { type: 'warning' })
  await cartApi.remove(item.id)
  ElMessage.success('已删除')
  load()
}

const clearCart = async () => {
  await ElMessageBox.confirm('确定清空购物车吗？', '提示', { type: 'warning' })
  await cartApi.clear()
  ElMessage.success('已清空')
  load()
}

const loadAddresses = async () => {
  addresses.value = await addressApi.list()
}

const selectAddress = (addr) => {
  buy.addressId = addr.id
  buy.name = addr.name
  buy.phone = addr.phone
  buy.address = addr.address
}

const checkout = async () => {
  await loadAddresses()
  buy.addressId = null
  buy.name = userStore.user?.nickname || ''
  buy.phone = userStore.user?.phone || ''
  buy.address = userStore.user?.address || ''
  buy.remark = ''
  
  const defaultAddr = addresses.value.find(a => a.isDefault)
  if (defaultAddr) {
    selectAddress(defaultAddr)
  }
  checkoutVisible.value = true
}

const submitOrder = async () => {
  if (!buy.name) return ElMessage.warning('请填写收货人')
  if (!buy.address) return ElMessage.warning('请填写收货地址')
  
  await orderApi.create({
    buyerName: buy.name,
    buyerPhone: buy.phone,
    buyerAddress: buy.address,
    remark: buy.remark,
    items: items.value.map(item => ({ productId: item.product.id, quantity: item.quantity }))
  })
  
  ElMessage.success('下单成功')
  checkoutVisible.value = false
  await cartApi.clear()
  load()
}

onMounted(load)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }

.cart-list { padding: 12px 0; }
.cart-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #fafcf9;
  border-radius: 12px;
  margin-bottom: 12px;
  align-items: center;
}

.item-cover { flex: none; width: 100px; height: 80px; border-radius: 8px; overflow: hidden; background: #eef5ee; }
.cover { width: 100%; height: 100%; }
.cover-empty { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 32px; }

.item-info { flex: 1; min-width: 0; }
.item-name { font-size: 15px; font-weight: 600; color: #1f2d3d; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-meta { display: flex; align-items: center; gap: 8px; margin-top: 6px; }
.origin { font-size: 12px; color: #8a97a0; }
.item-price { margin-top: 8px; font-size: 16px; font-weight: 700; color: #2e7d32; }
.item-price small { color: #8a97a0; font-weight: 400; font-size: 13px; }
.item-stock { margin-top: 4px; font-size: 12px; color: #aab4c0; }

.item-right { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; }
.quantity { width: 100px; }
.item-total { font-size: 15px; font-weight: 700; color: #2e7d32; }

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-top: 1px solid #eef0ee;
  margin-top: 12px;
}
.total-info { display: flex; align-items: center; gap: 16px; font-size: 14px; color: #5a6a5a; }
.total-price b { font-size: 20px; color: #2e7d32; }

.address-option {
  padding: 12px;
  border: 1px solid #eef0ee;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.address-option:hover { border-color: #2e7d32; }
.address-option.is-selected { border-color: #2e7d32; background: #f1f8f1; }
.addr-main { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 500; }
.addr-detail { font-size: 13px; color: #5a6a5a; margin-top: 4px; }
.no-address { text-align: center; color: #8a97a0; padding: 20px; }
</style>