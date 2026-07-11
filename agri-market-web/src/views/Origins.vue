<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>产地信息公示</span>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增产地</el-button>
      </div>
    </template>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="产地/基地" min-width="140" />
      <el-table-column prop="location" label="所在地" min-width="160" />
      <el-table-column prop="farmer" label="农户/合作社" width="140" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="certificate" label="资质认证" width="140" />
      <el-table-column prop="description" label="介绍" show-overflow-tooltip />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" :icon="Delete" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑产地' : '新增产地'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="产地名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="所在地"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="农户/合作社"><el-input v-model="form.farmer" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="资质认证"><el-input v-model="form.certificate" /></el-form-item>
        <el-form-item label="产地介绍"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { originApi } from '../api'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', location: '', farmer: '', phone: '', certificate: '', description: '' })

const load = async () => {
  loading.value = true
  try { list.value = await originApi.list() } finally { loading.value = false }
}
const reset = () => Object.assign(form, { id: null, name: '', location: '', farmer: '', phone: '', certificate: '', description: '' })
const openCreate = () => { reset(); dialogVisible.value = true }
const openEdit = (row) => { Object.assign(form, row); dialogVisible.value = true }
const submit = async () => {
  await originApi.save(form)
  ElMessage.success('已保存')
  dialogVisible.value = false
  load()
}
const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除产地「${row.name}」吗？`, '提示', { type: 'warning' })
  await originApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
