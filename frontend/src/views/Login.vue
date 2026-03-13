<template>
  <div class="auth-page">
    <div class="auth-shell">
      <div class="auth-left">
        <div class="brand">
          <div class="brand-title">民宿预定管理系统</div>
          <div class="brand-sub">一处完成预订、支付、运营，全角色通行</div>
        </div>
        <div class="bullets">
          <div class="bullet"><el-icon><Check /></el-icon><span>便捷下单 · 智能分配房型</span></div>
          <div class="bullet"><el-icon><Check /></el-icon><span>支付/退款留痕，可追踪、可回滚</span></div>
          <div class="bullet"><el-icon><Check /></el-icon><span>多角色入口：用户 / 商户 / 管理员</span></div>
        </div>
      </div>

      <div class="auth-right">
        <el-card class="auth-card" shadow="hover">
          <template #header>
            <div class="card-title">欢迎登录</div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-form-item label="角色" prop="role">
              <el-select v-model="form.role" placeholder="请选择角色" size="large" style="width: 100%">
                <el-option label="普通用户" value="USER" />
                <el-option label="商户" value="MERCHANT" />
                <el-option label="管理员" value="ADMIN" />
              </el-select>
              <div class="helper">用户用于预订，商户用于管理房源，管理员拥有全局管理权限</div>
            </el-form-item>

            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" size="large" placeholder="请输入用户名" :prefix-icon="User" />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                size="large"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="onSubmit"
              />
            </el-form-item>

            <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="onSubmit">
              登录
            </el-button>

            <el-alert
              v-if="error"
              type="error"
              :title="error"
              show-icon
              :closable="false"
              style="margin-top: 12px"
            />
          </el-form>

          <div class="divider">
            <el-divider>示例账号</el-divider>
          </div>
          <div class="demo">
            <el-tag
              v-for="acc in demoAccounts"
              :key="acc.username + acc.role"
              class="demo-tag"
              effect="plain"
              @click="fillAccount(acc)"
            >
              {{ roleText(acc.role) }} · {{ acc.username }} / {{ acc.password }}
            </el-tag>
          </div>

          <div class="footer">
            <span>还没有账号？</span>
            <router-link class="link" to="/register">立即注册</router-link>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, User, Lock } from '@element-plus/icons-vue'
import { authApi } from '../api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const error = ref('')

const form = reactive({
  role: 'USER',
  username: '',
  password: ''
})

const rules = {
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const demoAccounts = [
  { role: 'USER', username: 'user1', password: 'password' },
  { role: 'MERCHANT', username: 'merchant1', password: 'password' },
  { role: 'ADMIN', username: 'admin', password: 'admin123' }
]

const roleText = (role) => {
  const map = { USER: '用户', MERCHANT: '商户', ADMIN: '管理员' }
  return map[role] || role
}

const fillAccount = (acc) => {
  form.role = acc.role
  form.username = acc.username
  form.password = acc.password
}

const onSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    error.value = ''
    const { data } = await authApi.login({
      username: form.username,
      password: form.password,
      role: form.role
    })
    localStorage.setItem('user', JSON.stringify(data))
    window.dispatchEvent(new Event('user-changed'))
    ElMessage.success('登录成功')
    if (data?.role === 'ADMIN') router.push('/admin')
    else if (data?.role === 'USER') router.push('/simple-booking')
    else router.push('/booking')
  } catch (err) {
    if (err !== false) {
      error.value = err?.response?.data?.error || '登录失败，请检查账号/密码/角色'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 22px;
}
.auth-shell {
  width: 100%;
  max-width: 1060px;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 14px 50px rgba(0, 0, 0, 0.10);
  overflow: hidden;
  display: grid;
  grid-template-columns: 1fr 1fr;
}
.auth-left {
  padding: 56px 44px;
  color: #fff;
  background: linear-gradient(135deg, #0f7bff 0%, #5ec7ff 65%, #8de6ff 100%);
}
.brand-title {
  font-size: 34px;
  font-weight: 800;
  letter-spacing: 0.02em;
}
.brand-sub {
  margin-top: 10px;
  opacity: 0.92;
  line-height: 1.6;
}
.bullets {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.bullet {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
}
.bullet .el-icon {
  font-size: 18px;
}
.auth-right {
  padding: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 20% 10%, #f0f7ff 0%, #fff 45%);
}
.auth-card {
  width: 100%;
  max-width: 420px;
  border: none;
}
.card-title {
  text-align: center;
  font-weight: 700;
  font-size: 22px;
  color: #111827;
}
.divider {
  margin-top: 18px;
}
.demo {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}
.demo-tag {
  cursor: pointer;
}
.helper {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}
.footer {
  margin-top: 18px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}
.link {
  color: #0f7bff;
  margin-left: 6px;
  text-decoration: none;
  font-weight: 600;
}
.link:hover {
  text-decoration: underline;
}
@media (max-width: 980px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }
  .auth-left {
    display: none;
  }
  .auth-right {
    padding: 24px;
  }
}
</style>
