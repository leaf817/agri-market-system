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
          <el-form-item label="状态">
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
        <el-button type="primary" :icon="Plus" @click="openCreate">上架农产品</el-button>
      </div>
    </el-card>

    <!-- 产品卡片网格 -->
    <div v-loading="loading" class="grid-wrap">
      <el-empty v-if="!loading && products.length === 0" description="暂无农产品，点击右上角上架" />
      <el-row :gutter="16">
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
              <el-tag :type="p.status === 1 ? 'success' : 'info'" size="small" class="status-tag">
                {{ p.status === 1 ? '上架' : '下架' }}
              </el-tag>
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
              <div class="nums">库存 {{ p.stock }} · 销量 {{ p.sales }}</div>
            </div>
            <div class="actions">
              <el-button size="small" :icon="Edit" @click="openEdit(p)">编辑</el-button>
              <el-button size="small" :type="p.status === 1 ? 'warning' : 'success'" @click="toggleStatus(p)">
                {{ p.status === 1 ? '下架' : '上架' }}
              </el-button>
              <el-button size="small" type="danger" :icon="Delete" @click="remove(p)">删除</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑农产品' : '上架农产品'" width="560px">
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
              <el-upload action="/api/upload" accept="image/*" :show-file-list="false" :before-upload="beforeCoverUpload" :on-success="handleCoverSuccess">
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Upload, Location } from '@element-plus/icons-vue'
import { productApi, categoryApi, originApi } from '../api'

const products = ref([])
const categories = ref([])
const origins = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()

const filters = reactive({ categoryId: null, status: null, keyword: '' })
const form = reactive({ name: '', categoryId: null, originId: null, price: 0, stock: 0, unit: '', cover: '', description: '', status: 1 })
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const loadProducts = async () => {
  loading.value = true
  try { products.value = await productApi.list(filters) } finally { loading.value = false }
}
const loadOptions = async () => {
  categories.value = await categoryApi.list()
  origins.value = await originApi.list()
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
  if (res && res.code === 0) { form.cover = res.data.url; ElMessage.success('图片上传成功') }
  else { ElMessage.error((res && res.message) || '图片上传失败') }
}

onMounted(() => { loadOptions(); loadProducts() })
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.toolbar-inner { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 8px; }
.filter { margin-bottom: -18px; }

.grid-wrap { min-height: 200px; }
.card-col { margin-bottom: 16px; }
.product-card { border-radius: 12px; overflow: hidden; transition: transform 0.18s ease, box-shadow 0.18s ease; }
.product-card:hover { transform: translateY(-4px); }

.cover-wrap { position: relative; width: 100%; aspect-ratio: 4 / 3; overflow: hidden; background: linear-gradient(135deg, #eef5ee, #d7e9d8); }
.cover { width: 100%; height: 100%; display: block; }
.cover :deep(img) { width: 100%; height: 100%; object-fit: cover; }
.cover-empty { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 48px; }
.status-tag { position: absolute; top: 8px; right: 8px; }

.info { padding: 12px 14px 8px; }
.name { font-size: 15px; font-weight: 700; color: #1f2d3d; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.meta { margin: 8px 0 6px; }
.origin { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #8a97a0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.price { margin-top: 8px; font-size: 14px; color: #2e7d32; font-weight: 600; }
.price span { font-size: 22px; font-weight: 800; }
.price small { color: #8a97a0; font-weight: 400; }
.nums { margin-top: 6px; font-size: 12px; color: #aab4c0; }

.actions { display: flex; gap: 6px; padding: 10px 14px 14px; border-top: 1px solid #f3f5f3; }
.actions .el-button { flex: 1; }

.cover-uploader { display: flex; flex-direction: column; align-items: flex-start; gap: 8px; }
.cover-preview { width: 120px; height: 120px; border-radius: 6px; border: 1px solid #ebeef5; }
.cover-actions { display: flex; align-items: center; gap: 8px; }
</style>
