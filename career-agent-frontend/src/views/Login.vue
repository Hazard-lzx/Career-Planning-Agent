<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <el-icon :size="40" color="#3B82D9"><Compass /></el-icon>
        <h2>AI职业规划智能体</h2>
        <p>{{ isRegister ? '创建新账号' : '登录你的账号' }}</p>
      </div>

      <el-form :model="form" label-position="top" class="login-form">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="isRegister" label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱(选填)" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"
            @keydown.enter="submit" show-password />
        </el-form-item>

        <el-button type="primary" :loading="loading" class="login-btn" @click="submit">
          {{ isRegister ? '注册' : '登录' }}
        </el-button>

        <div class="switch-link">
          <span v-if="!isRegister">还没有账号？<a href="#" @click.prevent="isRegister=true">立即注册</a></span>
          <span v-else>已有账号？<a href="#" @click.prevent="isRegister=false">去登录</a></span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authStore } from '../stores/auth.js'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const isRegister = ref(false)
const loading = ref(false)
const form = ref({ username: '', password: '', email: '' })

async function submit() {
  if (!form.value.username || !form.value.password) {
    return ElMessage.warning('请输入用户名和密码')
  }
  loading.value = true
  try {
    const url = isRegister.value ? '/api/auth/register' : '/api/auth/login'
    const body = isRegister.value
      ? { username: form.value.username, password: form.value.password, email: form.value.email }
      : { username: form.value.username, password: form.value.password }
    const res = await axios.post(url, body)
    const token = res.data?.data?.token
    if (token) {
      authStore.login(token, form.value.username)
      router.push('/home')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-card {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 24px rgba(100,149,237,0.08);
  padding: 40px;
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.login-header h2 { margin: 12px 0 4px; color: #1e3a5f; font-size: 22px; }
.login-header p { color: #94a3b8; font-size: 14px; }
.login-btn { width: 100%; margin-top: 8px; height: 44px; font-size: 16px; }
.switch-link { text-align: center; margin-top: 16px; font-size: 14px; color: #64748b; }
.switch-link a { color: #3B82D9; text-decoration: none; }
</style>
