<template>
  <el-card shadow="never" class="profile-card">
    <div class="profile-header">
      <el-avatar :size="80" class="avatar">
        <el-image v-if="profile.avatar" :src="profile.avatar" fit="cover" />
        <span v-else>{{ (profile.nickname || profile.username || '?').slice(0, 1) }}</span>
      </el-avatar>
      <div class="profile-info">
        <h2>{{ profile.nickname || profile.username }}</h2>
        <el-tag :type="roleTagType" size="small">{{ roleLabel }}</el-tag>
        <p class="username">用户名：{{ profile.username }}</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="profile-tabs">
      <el-tab-pane label="个人资料" name="basic">
        <el-form :model="form" label-width="100px" class="profile-form">
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="收货地址">
            <el-input v-model="form.address" type="textarea" :rows="3" placeholder="请输入收货地址" />
          </el-form-item>
          <el-form-item label="头像">
            <div class="avatar-upload">
              <el-image v-if="form.avatar" :src="form.avatar" class="avatar-preview" fit="cover" />
              <div v-else class="avatar-empty">上传头像</div>
              <el-upload
                action="/api/upload"
                name="file"
                accept="image/*"
                :show-file-list="false"
                :headers="uploadHeaders"
                :before-upload="beforeAvatarUpload"
                :on-success="handleAvatarSuccess"
                :on-error="() => ElMessage.error('头像上传失败，请确认已登录')"
              >
                <el-button :icon="Upload" size="small">{{ form.avatar ? '更换头像' : '上传头像' }}</el-button>
              </el-upload>
              <el-button v-if="form.avatar" text type="danger" size="small" @click="form.avatar = ''">移除</el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Disk" @click="saveProfile">保存修改</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="我的收藏" name="fav">
        <div v-loading="favLoading">
          <el-empty v-if="!favLoading && favorites.length === 0" description="暂无收藏" />
          <el-table v-else :data="favorites" border stripe>
            <el-table-column label="封面" width="84">
              <template #default="{ row }">
                <el-image v-if="row.product?.cover" :src="row.product.cover" fit="cover" class="thumb" />
                <div v-else class="thumb-empty">🌾</div>
              </template>
            </el-table-column>
            <el-table-column label="商品名称" min-width="150">
              <template #default="{ row }">{{ row.product?.name || '商品已失效' }}</template>
            </el-table-column>
            <el-table-column label="分类" width="100">
              <template #default="{ row }">{{ row.product?.category?.name || '-' }}</template>
            </el-table-column>
            <el-table-column label="价格" width="100">
              <template #default="{ row }">{{ row.product ? `¥${row.product.price}` : '-' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="收藏时间" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button text type="danger" @click="removeFav(row)">取消收藏</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="收货地址" name="address">
        <div class="address-list">
          <div v-for="addr in addresses" :key="addr.id" class="address-card" :class="{ 'is-default': addr.isDefault }">
            <div class="address-header">
              <span class="address-name">{{ addr.name }}</span>
              <span class="address-phone">{{ addr.phone }}</span>
              <el-tag v-if="addr.isDefault" type="success" size="small">默认地址</el-tag>
            </div>
            <div class="address-detail">{{ addr.address }}</div>
            <div class="address-actions">
              <el-button v-if="!addr.isDefault" text size="small" @click="setDefaultAddr(addr)">设为默认</el-button>
              <el-button text size="small" @click="editAddr(addr)">编辑</el-button>
              <el-button text type="danger" size="small" @click="removeAddr(addr)">删除</el-button>
            </div>
          </div>
          <el-empty v-if="addresses.length === 0" description="暂无收货地址" />
        </div>
        <el-button type="primary" :icon="Plus" style="margin-top: 16px" @click="openAddrForm">添加收货地址</el-button>
      </el-tab-pane>

      <el-dialog v-model="addrDialogVisible" :title="editingAddrId ? '编辑收货地址' : '添加收货地址'" width="480px">
        <el-form label-width="80px">
          <el-form-item label="收货人"><el-input v-model="addrForm.name" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="addrForm.phone" /></el-form-item>
          <el-form-item label="地址"><el-input v-model="addrForm.address" type="textarea" :rows="3" /></el-form-item>
          <el-form-item>
            <el-checkbox v-model="addrForm.isDefault">设为默认地址</el-checkbox>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="addrDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAddr">保存</el-button>
        </template>
      </el-dialog>

      <el-tab-pane label="我的评价" name="review">
        <div v-loading="reviewLoading">
          <el-empty v-if="!reviewLoading && reviews.length === 0" description="暂无评价" />
          <el-table v-else :data="reviews" border stripe>
            <el-table-column label="商品" min-width="150">
              <template #default="{ row }">{{ row.product?.name || '商品已失效' }}</template>
            </el-table-column>
            <el-table-column label="评分" width="100">
              <template #default="{ row }">
                <div class="rating">
                  <el-icon v-for="i in 5" :key="i" :color="i <= row.rating ? '#ffb300' : '#e0e0e0'"><Star /></el-icon>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="评价内容" min-width="200" />
            <el-table-column prop="createTime" label="评价时间" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button text type="danger" @click="removeReview(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { profileApi, favoriteApi, reviewApi, addressApi } from '../api'
import userStore, { role, hasRole } from '../stores/user'

const activeTab = ref('basic')
const profile = reactive({ id: null, username: '', nickname: '', phone: '', avatar: '', address: '', role: '' })
const form = reactive({ nickname: '', phone: '', avatar: '', address: '' })
const uploadHeaders = computed(() => ({
  Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
}))

const favorites = ref([])
const reviews = ref([])
const addresses = ref([])
const favLoading = ref(false)
const reviewLoading = ref(false)

const addrDialogVisible = ref(false)
const editingAddrId = ref(null)
const addrForm = reactive({ name: '', phone: '', address: '', isDefault: false })

const roleLabelMap = { admin: '管理员', farmer: '农户', consumer: '消费者' }
const roleTypeMap = { admin: 'danger', farmer: 'warning', consumer: 'success' }
const roleLabel = roleLabelMap[role()] || '游客'
const roleTagType = roleTypeMap[role()] || 'info'

const loadProfile = async () => {
  const data = await profileApi.get()
  Object.assign(profile, data)
  Object.assign(form, { nickname: data.nickname || '', phone: data.phone || '', avatar: data.avatar || '', address: data.address || '' })
}

const loadFavorites = async () => {
  if (!hasRole('consumer')) {
    favorites.value = []
    return
  }
  favLoading.value = true
  try { favorites.value = await favoriteApi.list() } finally { favLoading.value = false }
}

const loadReviews = async () => {
  reviewLoading.value = true
  try { reviews.value = await reviewApi.list() } finally { reviewLoading.value = false }
}

const loadAddresses = async () => {
  addresses.value = await addressApi.list()
}

const openAddrForm = () => {
  editingAddrId.value = null
  Object.assign(addrForm, { name: '', phone: '', address: '', isDefault: false })
  addrDialogVisible.value = true
}

const editAddr = (addr) => {
  editingAddrId.value = addr.id
  Object.assign(addrForm, { name: addr.name, phone: addr.phone, address: addr.address, isDefault: addr.isDefault })
  addrDialogVisible.value = true
}

const saveAddr = async () => {
  if (!addrForm.name) return ElMessage.warning('请填写收货人')
  if (!addrForm.phone) return ElMessage.warning('请填写手机号')
  if (!addrForm.address) return ElMessage.warning('请填写地址')
  
  if (editingAddrId.value) {
    await addressApi.update(editingAddrId.value, addrForm)
    ElMessage.success('地址已更新')
  } else {
    await addressApi.create(addrForm)
    ElMessage.success('地址已添加')
  }
  addrDialogVisible.value = false
  loadAddresses()
}

const setDefaultAddr = async (addr) => {
  await addressApi.setDefault(addr.id)
  ElMessage.success('已设为默认地址')
  loadAddresses()
}

const removeAddr = async (addr) => {
  await ElMessageBox.confirm('确定删除该地址吗？', '提示', { type: 'warning' })
  await addressApi.remove(addr.id)
  ElMessage.success('已删除')
  loadAddresses()
}

const saveProfile = async () => {
  await profileApi.update(form)
  ElMessage.success('保存成功')
  await loadProfile()
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type && file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt5M) ElMessage.error('图片大小不能超过 5MB')
  return isImage && isLt5M
}

const handleAvatarSuccess = (res) => {
  if (res && res.code === 0) { form.avatar = res.data.url; ElMessage.success('头像上传成功') }
  else { ElMessage.error((res && res.message) || '头像上传失败') }
}

const removeFav = async (row) => {
  const productId = row.product?.id
  if (!productId) return ElMessage.warning('商品已失效，无法操作')
  await favoriteApi.remove(productId)
  ElMessage.success('已取消收藏')
  loadFavorites()
}

const removeReview = async (row) => {
  await reviewApi.remove(row.id)
  ElMessage.success('已删除评价')
  loadReviews()
}

onMounted(() => {
  loadProfile()
  loadFavorites()
  loadReviews()
  loadAddresses()
})
</script>

<style scoped>
.profile-card { max-width: 720px; margin: 0 auto; }

.profile-header {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f6faf6, #eef5ee);
  border-radius: 12px;
  margin-bottom: 20px;
}

.avatar { background: var(--brand); color: #fff; font-weight: 600; }
.avatar :deep(img) { width: 100%; height: 100%; object-fit: cover; }

.profile-info { display: flex; flex-direction: column; gap: 8px; justify-content: center; }
.profile-info h2 { font-size: 20px; font-weight: 700; color: #1f2d3d; margin: 0; }
.username { font-size: 13px; color: #8a97a0; margin: 0; }

.profile-tabs { margin-top: 16px; }
.profile-form { margin-top: 16px; }

.avatar-upload { display: flex; flex-direction: column; gap: 8px; }
.avatar-preview { width: 100px; height: 100px; border-radius: 8px; border: 1px solid #ebeef5; }
.avatar-empty { width: 100px; height: 100px; border-radius: 8px; border: 1px dashed #ccc; display: flex; align-items: center; justify-content: center; font-size: 12px; color: #999; }

.thumb { width: 60px; height: 45px; border-radius: 6px; display: block; }
.thumb-empty { width: 60px; height: 45px; border-radius: 6px; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #eef5ee, #d7e9d8); font-size: 22px; }

.rating { display: flex; gap: 2px; }

.address-list { display: flex; flex-direction: column; gap: 12px; }
.address-card {
  padding: 16px;
  border: 1px solid #eef0ee;
  border-radius: 10px;
  background: #fafcf9;
  position: relative;
}
.address-card.is-default { border-color: #43a047; background: #f1f8f1; }
.address-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.address-name { font-size: 15px; font-weight: 600; color: #1f2d3d; }
.address-phone { font-size: 14px; color: #5a6a5a; }
.address-detail { font-size: 14px; color: #5a6a5a; line-height: 1.5; margin-bottom: 12px; }
.address-actions { display: flex; gap: 8px; }
</style>