<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2>用户管理</h2>
        <p>维护管理员、农户和消费者账号</p>
      </div>
      <el-button type="primary" @click="openCreate">新增用户</el-button>
    </div>

    <el-table v-loading="loading" :data="users" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="130" />
      <el-table-column prop="nickname" label="昵称" min-width="140" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="roleType(row.role)">{{ roleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">
            {{ row.enabled ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" @click="openPassword(row)">重置密码</el-button>
          <el-button
            size="small"
            :type="row.enabled ? 'warning' : 'success'"
            :disabled="row.id === currentUser?.id"
            @click="toggleEnabled(row)"
          >
            {{ row.enabled ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="formVisible" :title="editingId ? '编辑用户' : '新增用户'" width="460px">
      <el-form :model="form" label-width="86px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item v-if="!editingId" label="密码" required>
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="农户" value="farmer" />
            <el-option label="消费者" value="consumer" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordVisible" title="重置密码" width="420px">
      <el-form label-width="86px">
        <el-form-item label="用户">
          <el-input :model-value="passwordTarget?.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input v-model="newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="resetPassword">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api'
import userStore from '../stores/user'

const users = ref([])
const loading = ref(false)
const saving = ref(false)
const formVisible = ref(false)
const passwordVisible = ref(false)
const editingId = ref(null)
const passwordTarget = ref(null)
const newPassword = ref('')
const currentUser = computed(() => userStore.user)

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  role: 'consumer',
  enabled: true
})

const loadUsers = async () => {
  loading.value = true
  try {
    users.value = await userApi.list()
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  editingId.value = null
  Object.assign(form, {
    username: '',
    password: '',
    nickname: '',
    phone: '',
    role: 'consumer',
    enabled: true
  })
}

const openCreate = () => {
  resetForm()
  formVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, {
    username: row.username,
    password: '',
    nickname: row.nickname || '',
    phone: row.phone || '',
    role: row.role,
    enabled: row.enabled
  })
  formVisible.value = true
}

const saveUser = async () => {
  if (!form.username || (!editingId.value && !form.password) || !form.role) {
    ElMessage.warning('请填写必填项')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await userApi.update(editingId.value, {
        nickname: form.nickname,
        phone: form.phone,
        role: form.role,
        enabled: form.enabled
      })
    } else {
      await userApi.create({ ...form })
    }
    ElMessage.success('保存成功')
    formVisible.value = false
    await loadUsers()
  } finally {
    saving.value = false
  }
}

const openPassword = (row) => {
  passwordTarget.value = row
  newPassword.value = ''
  passwordVisible.value = true
}

const resetPassword = async () => {
  if (!newPassword.value || newPassword.value.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  saving.value = true
  try {
    await userApi.resetPassword(passwordTarget.value.id, newPassword.value)
    ElMessage.success('密码已重置')
    passwordVisible.value = false
  } finally {
    saving.value = false
  }
}

const toggleEnabled = async (row) => {
  saving.value = true
  try {
    await userApi.update(row.id, { enabled: !row.enabled })
    ElMessage.success(row.enabled ? '已禁用' : '已启用')
    await loadUsers()
  } finally {
    saving.value = false
  }
}

const roleLabel = (role) => ({ admin: '管理员', farmer: '农户', consumer: '消费者' }[role] || role)
const roleType = (role) => ({ admin: 'danger', farmer: 'warning', consumer: 'success' }[role] || 'info')

onMounted(loadUsers)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.toolbar h2 { margin: 0; font-size: 20px; color: #1f2d3d; }
.toolbar p { margin: 4px 0 0; color: #7a8a7a; font-size: 13px; }
</style>
