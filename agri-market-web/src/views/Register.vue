<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <div class="brand">
        <div class="brand-icon">{{ form.role === 'farmer' ? '🌾' : '🛒' }}</div>
        <div class="brand-title">{{ form.role === 'farmer' ? '农户注册' : '消费者注册' }}</div>
        <div class="brand-sub">{{ form.role === 'farmer' ? '注册后可维护产地并上架自己的农产品' : '注册后即可浏览与下单' }}</div>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="注册身份" prop="role">
          <el-segmented v-model="form.role" :options="roleOptions" class="role-switch" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="3-50 个字符" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="选填，展示用" :prefix-icon="Avatar" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少 6 位" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm">
          <el-input v-model="form.confirm" type="password" placeholder="再次输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" class="submit" @click="submit">注 册</el-button>
      </el-form>
      <div v-if="form.role === 'farmer'" class="register-tip">
        农户账号注册后，请先进入“我的产地”创建产地，再到“我的产品”上架商品。
      </div>
      <div class="foot">
        <span>已有账号？</span>
        <el-link type="primary" @click="$router.push('/login')">去登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Avatar } from '@element-plus/icons-vue'
import { authApi } from '../api'
import { setSession } from '../stores/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', nickname: '', password: '', confirm: '', role: 'consumer' })
const roleOptions = [
  { label: '消费者', value: 'consumer' },
  { label: '农户', value: 'farmer' }
]
const rules = {
  role: [{ required: true, message: '请选择注册身份', trigger: 'change' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度 3-50', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, cb) => (value === form.password ? cb() : cb(new Error('两次输入的密码不一致'))),
      trigger: 'blur'
    }
  ]
}

const submit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await authApi.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      role: form.role
    })
    setSession(data.token, data.user)
    ElMessage.success('注册成功，已自动登录')
    router.push(form.role === 'farmer' ? '/my-origins' : '/products')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f5e9, #f1f8e9);
}
.login-card { width: 380px; border-radius: 16px; padding: 8px 8px 16px; }
.brand { text-align: center; margin: 12px 0 24px; }
.brand-icon {
  width: 56px; height: 56px; margin: 0 auto 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 30px; color: #fff;
  background: linear-gradient(135deg, #43a047, #2e7d32); border-radius: 14px;
}
.brand-title { font-size: 18px; font-weight: 800; color: #1f2d3d; }
.brand-sub { font-size: 12px; color: #8a97a0; margin-top: 4px; }
.submit { width: 100%; height: 42px; margin-top: 4px; }
.role-switch { width: 100%; }
.register-tip {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #f4faf4;
  color: #4f6f4f;
  font-size: 12px;
  line-height: 1.6;
}
.foot { display: flex; justify-content: center; gap: 6px; margin-top: 14px; font-size: 13px; color: #8a97a0; }
</style>
