<template>
  <div class="profile-page">
    <el-page-header content="个人信息" icon="ArrowLeft" @back="$router.push('/')" />

    <el-card shadow="hover" class="mt-16">
      <template #header>
        <div class="card-header">
          <div>
            <div class="title">我的资料</div>
            <div class="sub">更新头像、邮箱、手机等信息</div>
          </div>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="6">
          <div class="avatar-box">
            <el-avatar :size="120" :src="form.avatar" icon="UserFilled" />
            <el-upload
              class="mt-12"
              :auto-upload="false"
              :show-file-list="false"
              accept="image/*"
              :on-change="handleUpload"
            >
              <el-button type="primary" plain size="small">上传头像</el-button>
            </el-upload>
            <div class="avatar-url">
              <el-input v-model="form.avatar" placeholder="或粘贴头像URL" size="small" />
            </div>
          </div>
        </el-col>
        <el-col :span="18">
          <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag>{{ form.role }}</el-tag>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="name@example.com" />
            </el-form-item>
            <el-form-item label="手机" prop="phone">
              <el-input v-model="form.phone" placeholder="11位手机号" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="form.password" type="password" show-password placeholder="留空则不修改" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api'

const formRef = ref(null)
const saving = ref(false)
const form = reactive({
  id: null,
  username: '',
  role: '',
  email: '',
  phone: '',
  avatar: '',
  password: ''
})

const rules = {
  email: [
    {
      validator: (_r, v, cb) => {
        if (!v) return cb()
        const ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)
        cb(ok ? undefined : new Error('邮箱格式不正确'))
      },
      trigger: 'blur'
    }
  ],
  phone: [
    {
      validator: (_r, v, cb) => {
        if (!v) return cb()
        const ok = /^1[3-9]\d{9}$/.test(v)
        cb(ok ? undefined : new Error('手机号格式不正确'))
      },
      trigger: 'blur'
    }
  ]
}

const loadProfile = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      Object.assign(form, {
        id: user.id,
        username: user.username,
        role: user.role,
        email: user.email || '',
        phone: user.phone || '',
        avatar: user.avatar || '',
        password: ''
      })
    } catch (e) {
      // ignore
    }
  }
}

const handleUpload = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.avatar = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const saveProfile = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  if (!form.id) {
    ElMessage.error('未获取到用户ID')
    return
  }
  saving.value = true
  try {
    const payload = {
      username: form.username,
      email: form.email || null,
      phone: form.phone || null,
      avatar: form.avatar || null,
      role: form.role
    }
    if (form.password && form.password.trim()) {
      payload.password = form.password.trim()
    }
    const { data } = await userApi.update(form.id, payload)
    // 优先使用表单中的头像以确保前端立刻展示
    const merged = { ...JSON.parse(localStorage.getItem('user') || '{}'), ...data, avatar: form.avatar }
    localStorage.setItem('user', JSON.stringify(merged))
    window.dispatchEvent(new Event('user-changed'))
    ElMessage.success('已保存个人信息')
  } catch (err) {
    ElMessage.error(err?.response?.data?.error || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.mt-16 {
  margin-top: 12px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-size: 18px;
  font-weight: 700;
}
.sub {
  color: #8690a3;
  font-size: 13px;
}
.avatar-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.avatar-url {
  width: 100%;
}
</style>
