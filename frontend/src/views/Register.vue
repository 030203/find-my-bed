<template>
  <div class="auth-page">
    <div class="auth-shell">
      <div class="auth-left">
        <div class="brand">
          <div class="brand-title">创建你的账号</div>
          <div class="brand-sub">快速预订、支付与运营，从这里开始</div>
        </div>
        <div class="tips">
          <div class="tip"><el-icon><Check /></el-icon><span>普通用户：预订与支付</span></div>
          <div class="tip"><el-icon><Check /></el-icon><span>商户：管理房源与订单</span></div>
          <div class="tip"><el-icon><Check /></el-icon><span>管理员：查看全局数据</span></div>
        </div>
      </div>

      <div class="auth-right">
        <el-card class="auth-card" shadow="hover">
          <template #header>
            <div class="card-title">用户注册</div>
          </template>

          <el-form ref="formRef" :model="form" :rules="mergedRules" label-position="top">
            <el-form-item label="角色" prop="role">
              <el-select v-model="form.role" placeholder="请选择角色" size="large" style="width: 100%">
                <el-option label="普通用户" value="USER" />
                <el-option label="商户" value="MERCHANT" />
                <el-option label="管理员" value="ADMIN" />
              </el-select>
              <div class="helper">普通用户用于预订，商户可管理房源，管理员用于全局管理</div>
            </el-form-item>

            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="form.username"
                size="large"
                placeholder="3-20位，字母/数字/下划线"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                size="large"
                type="password"
                placeholder="至少6位"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                size="large"
                type="password"
                placeholder="再次输入密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item label="邮箱（可选）" prop="email">
              <el-input v-model="form.email" size="large" placeholder="name@example.com" :prefix-icon="Message" />
            </el-form-item>

            <el-form-item label="手机（可选）" prop="phone">
              <el-input v-model="form.phone" size="large" placeholder="11位手机号" :prefix-icon="Phone" />
            </el-form-item>

            <template v-if="form.role === 'MERCHANT'">
              <el-divider>商户信息</el-divider>
              <el-form-item label="商户名称" prop="merchantName">
                <el-input v-model="form.merchantName" size="large" placeholder="公司/店铺名称" />
              </el-form-item>
              <el-form-item label="联系人姓名" prop="merchantContactName">
                <el-input v-model="form.merchantContactName" size="large" placeholder="联系人姓名" />
              </el-form-item>
              <el-form-item label="联系人电话" prop="merchantContactPhone">
                <el-input v-model="form.merchantContactPhone" size="large" placeholder="联系人电话" />
              </el-form-item>
              <el-form-item label="联系人邮箱" prop="merchantContactEmail">
                <el-input v-model="form.merchantContactEmail" size="large" placeholder="联系人邮箱（可选）" />
              </el-form-item>
              <el-form-item label="所在省市" prop="merchantProvince">
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-input v-model="form.merchantProvince" size="large" placeholder="省份" />
                  </el-col>
                  <el-col :span="12">
                    <el-input v-model="form.merchantCity" size="large" placeholder="城市" />
                  </el-col>
                </el-row>
              </el-form-item>
              <el-form-item label="区县" prop="merchantDistrict">
                <el-input v-model="form.merchantDistrict" size="large" placeholder="区县（可选）" />
              </el-form-item>
              <el-form-item label="地址" prop="merchantAddress">
                <el-input v-model="form.merchantAddress" size="large" placeholder="详细地址（可选）" />
              </el-form-item>
              <el-form-item label="营业执照号" prop="businessLicense">
                <el-input v-model="form.businessLicense" size="large" placeholder="营业执照号（可选）" />
              </el-form-item>
            </template>

            <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="onSubmit">
              立即注册
            </el-button>

            <el-alert v-if="error" type="error" :title="error" show-icon :closable="false" style="margin-top: 12px" />
          </el-form>

          <div class="footer">
            <span>已有账号？</span>
            <router-link class="link" to="/login">立即登录</router-link>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, User, Lock, Message, Phone } from '@element-plus/icons-vue'
import { authApi } from '../api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const error = ref('')

const form = reactive({
  role: 'USER',
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  merchantName: '',
  merchantContactName: '',
  merchantContactPhone: '',
  merchantContactEmail: '',
  merchantAddress: '',
  merchantProvince: '',
  merchantCity: '',
  merchantDistrict: '',
  businessLicense: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const validateEmail = (rule, value, callback) => {
  if (!value) return callback()
  const ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
  callback(ok ? undefined : new Error('邮箱格式不正确'))
}

const validatePhone = (rule, value, callback) => {
  if (!value) return callback()
  const ok = /^1[3-9]\d{9}$/.test(value)
  callback(ok ? undefined : new Error('手机号格式不正确'))
}

const baseRules = {
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度 3-20 位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '仅支持字母数字/下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '长度 6-50 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }]
}

const merchantRequired = [{ required: true, message: '商户信息为必填', trigger: 'blur' }]

const mergedRules = computed(() => {
  if (form.role === 'MERCHANT') {
    return {
      ...baseRules,
      merchantName: merchantRequired,
      merchantContactName: merchantRequired,
      merchantContactPhone: merchantRequired
    }
  }
  return baseRules
})

const onSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    error.value = ''
    await authApi.register({
      username: form.username,
      password: form.password,
      email: form.email || null,
      phone: form.phone || null,
      role: form.role,
      merchantName: form.merchantName,
      merchantContactName: form.merchantContactName,
      merchantContactPhone: form.merchantContactPhone,
      merchantContactEmail: form.merchantContactEmail || null,
      merchantAddress: form.merchantAddress || null,
      merchantProvince: form.merchantProvince || null,
      merchantCity: form.merchantCity || null,
      merchantDistrict: form.merchantDistrict || null,
      businessLicense: form.businessLicense || null
    })
    ElMessage.success('注册成功，正在跳转登录')
    setTimeout(() => router.push('/login'), 800)
  } catch (err) {
    if (err !== false) {
      error.value = err?.response?.data?.error || '注册失败，请检查输入'
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
  box-shadow: 0 14px 50px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: grid;
  grid-template-columns: 1fr 1fr;
}
.auth-left {
  padding: 56px 44px;
  color: #fff;
  background: linear-gradient(135deg, #16b364 0%, #3dd6a5 55%, #8de6ff 100%);
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
.tips {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.tip {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
}
.tip .el-icon {
  font-size: 18px;
}
.helper {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}
.auth-right {
  padding: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 20% 10%, #f0fff7 0%, #fff 50%);
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
.footer {
  margin-top: 18px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}
.link {
  color: #16b364;
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
