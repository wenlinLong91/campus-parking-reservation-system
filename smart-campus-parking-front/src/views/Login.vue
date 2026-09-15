<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-header">
        <el-icon size="40" color="#52c41a"><Compass /></el-icon>
        <h2>Campus Parking</h2>
        <p>Management & Reservation System</p>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" size="large">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="Username / Staff ID"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="Password" 
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">
            Sign In
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const loginFormRef = ref(null)
const router = useRouter()
const loading = ref(false)

// 🌟 完美规范：统一使用 username，传给后端作为匹配条件
const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: 'Please enter your username/staff ID', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter your password', trigger: 'blur' }]
}

const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      // 🚀 向后端传去绝对规范、干净的 username 和 password
      const res = await axios.post('http://localhost:8080/api/login', {
        username: loginForm.username,
        password: loginForm.password
      });

      if (res.data.code === 200) {
        ElMessage.success('Welcome back!')
        // 持久化存储
        localStorage.setItem('role', res.data.role);
        localStorage.setItem('username', res.data.username);
        sessionStorage.setItem('username', res.data.username);

        // 🌟 强力强类型分流（防止大小写不一致或混淆导致进错页面）
        const userRole = String(res.data.role).trim().toUpperCase();

        if (res.data.role === 'ADMIN') {
          router.push('/manage');
        } else {
          router.push('/dashboard');
        }
      } else {
        ElMessage.error(res.data.message || 'Login Failed');
      }
    } catch (err) {
      console.error(err);
      ElMessage.error(err.response?.data?.message || '无法连接到后端服务器，请检查后端是否运行在 8080 端口');
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
/* 🍏 Fresh Fruit Green Gradient Background */
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #a8e6cf 0%, #dcedc1 100%);
}
.login-card {
  width: 420px;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(82, 196, 26, 0.15);
  border: none;
}
.login-header {
  text-align: center;
  margin-bottom: 35px;
}
.login-header h2 {
  margin: 10px 0 5px 0;
  color: #2c3e50;
  font-size: 24px;
}
.login-header p {
  color: #7f8c8d;
  margin: 0;
  font-size: 14px;
}
.login-btn {
  width: 100%;
  background-color: #52c41a;
  border-color: #52c41a;
  font-weight: bold;
}
.login-btn:hover {
  background-color: #73d13d;
  border-color: #73d13d;
}
</style>