<template>
  <div class="admin-shell">
    <aside class="sider">
      <div class="logo">后台控制台</div>
      <el-menu
        class="menu"
        :default-active="active"
        background-color="#0f172a"
        text-color="#cbd5e1"
        active-text-color="#38bdf8"
        @select="active = $event"
      >
        <el-menu-item index="users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item index="merchantAudit"><el-icon><OfficeBuilding /></el-icon><span>商户审核</span></el-menu-item>
        <el-menu-item index="propertyAudit"><el-icon><HomeFilled /></el-icon><span>民宿审核</span></el-menu-item>
        <el-menu-item index="propertyManage"><el-icon><Grid /></el-icon><span>民宿管理</span></el-menu-item>
        <el-menu-item index="profile"><el-icon><Setting /></el-icon><span>个人信息</span></el-menu-item>
      </el-menu>
    </aside>

    <main class="content">
      <div v-if="active === 'users'" class="panel">
        <div class="panel-head">
          <div>
            <div class="title">用户管理</div>
            <div class="sub">支持增删改查，快速维护账号</div>
          </div>
          <el-button type="primary" @click="openUserDialog()"><el-icon><Plus /></el-icon>新增用户</el-button>
        </div>

        <el-form :inline="true" :model="userSearch" class="search-form" @submit.prevent>
          <el-form-item label="用户名">
            <el-input v-model="userSearch.username" placeholder="模糊查询用户名" clearable />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="userSearch.role" placeholder="全部" clearable>
              <el-option label="用户" value="USER" />
              <el-option label="商户" value="MERCHANT" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="userSearch.status" placeholder="全部" clearable>
              <el-option label="激活" value="ACTIVE" />
              <el-option label="停用" value="INACTIVE" />
              <el-option label="封禁" value="BANNED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="filterUsers">查询</el-button>
            <el-button @click="resetUserSearch">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="pagedUsers" stripe v-loading="loading.users">
          <el-table-column prop="username" label="用户名" width="160" />
          <el-table-column prop="role" label="角色" width="120" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="phone" label="手机" width="140" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openUserDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeUser(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="userPage.current"
          v-model:page-size="userPage.size"
          :page-sizes="[5, 10, 20, 50]"
          :total="filteredUsers.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleUserPage"
          @current-change="handleUserPage"
          style="margin-top: 12px; text-align: right"
        />
      </div>

      <div v-if="active === 'merchantAudit'" class="panel">
        <div class="panel-head">
          <div>
            <div class="title">商户审核</div>
            <div class="sub">审核商户入驻申请</div>
          </div>
        </div>
        <el-table :data="merchants" stripe v-loading="loading.merchants">
          <el-table-column prop="merchantName" label="商户名称" />
          <el-table-column prop="contactName" label="联系人" width="120" />
          <el-table-column prop="contactPhone" label="电话" width="140" />
          <el-table-column prop="city" label="城市" width="120" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.status === 'PENDING' ? 'warning' : 'info'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="approveMerchant(row)">通过</el-button>
              <el-button size="small" type="danger" @click="rejectMerchant(row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="active === 'propertyAudit'" class="panel">
        <div class="panel-head">
          <div class="title">民宿申请管理</div>
          <div class="sub">查看并审核商户提交的民宿</div>
        </div>
        <el-table :data="pendingProperties" stripe v-loading="loading.properties">
          <el-table-column prop="propertyName" label="民宿名称" />
          <el-table-column prop="city" label="城市" width="120" />
          <el-table-column prop="address" label="地址" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag type="warning">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="viewProperty(row)">查看</el-button>
              <el-button size="small" type="primary" @click="approveProperty(row)">通过</el-button>
              <el-button size="small" type="danger" @click="rejectProperty(row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="active === 'propertyManage'" class="panel">
        <div class="panel-head">
          <div class="title">民宿管理</div>
          <div class="sub">查看平台全部民宿</div>
        </div>
        <el-table :data="allProperties" stripe v-loading="loading.propertiesAll">
          <el-table-column prop="propertyName" label="民宿名称" />
          <el-table-column prop="city" label="城市" width="120" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="viewProperty(row)">查看</el-button>
              <el-button size="small" type="danger" @click="rejectProperty(row)">停用/拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="active === 'profile'" class="panel">
        <div class="panel-head">
          <div class="title">个人信息</div>
          <div class="sub">查看当前管理员账号信息</div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ currentUser?.username }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ currentUser?.role }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser?.email || '—' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ currentUser?.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag type="success">{{ currentUser?.status || 'ACTIVE' }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </main>

    <el-dialog v-model="userDialog.visible" :title="userDialog.title" width="520px">
      <el-form :model="userForm" ref="userFormRef" :rules="userRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" style="width: 100%">
            <el-option label="用户" value="USER" />
            <el-option label="商户" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="userForm.status" style="width: 100%">
            <el-option label="激活" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
            <el-option label="封禁" value="BANNED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveUser" :loading="userDialog.loading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, OfficeBuilding, HomeFilled, Grid, Setting, Plus } from '@element-plus/icons-vue'
import { userApi, merchantApi, propertyApi } from '../api/index'

const router = useRouter()
const active = ref('users')
const currentUser = ref(null)

const users = ref([])
const userSearch = reactive({
  username: '',
  role: '',
  status: ''
})
const userPage = reactive({
  current: 1,
  size: 10
})

const filteredUsers = computed(() => {
  return users.value.filter((u) => {
    const byName = userSearch.username ? u.username?.toLowerCase().includes(userSearch.username.toLowerCase()) : true
    const byRole = userSearch.role ? u.role === userSearch.role : true
    const byStatus = userSearch.status ? u.status === userSearch.status : true
    return byName && byRole && byStatus
  })
})

const pagedUsers = computed(() => {
  const start = (userPage.current - 1) * userPage.size
  return filteredUsers.value.slice(start, start + userPage.size)
})
const merchants = ref([])
const pendingProperties = ref([])
const allProperties = ref([])

const loading = reactive({
  users: false,
  merchants: false,
  properties: false,
  propertiesAll: false
})

const userDialog = reactive({
  visible: false,
  title: '新增用户',
  loading: false
})
const userFormRef = ref(null)
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  role: 'USER',
  email: '',
  phone: '',
  status: 'ACTIVE'
})

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    SUSPENDED: 'info',
    DRAFT: 'info'
  }
  return map[status] || 'info'
}

const openUserDialog = (row) => {
  userDialog.visible = true
  userDialog.title = row ? '编辑用户' : '新增用户'
  Object.assign(userForm, {
    id: row?.id || null,
    username: row?.username || '',
    password: '',
    role: row?.role || 'USER',
    email: row?.email || '',
    phone: row?.phone || '',
    status: row?.status || 'ACTIVE'
  })
}

const filterUsers = () => {
  userPage.current = 1
}

const resetUserSearch = () => {
  userSearch.username = ''
  userSearch.role = ''
  userSearch.status = ''
  userPage.current = 1
}

const handleUserPage = () => {
  // 触发计算属性重算
}

const saveUser = async () => {
  try {
    await userFormRef.value.validate()
    userDialog.loading = true
    if (userForm.id) {
      await userApi.update(userForm.id, userForm)
      ElMessage.success('更新成功')
    } else {
      await userApi.create(userForm)
      ElMessage.success('创建成功')
    }
    userDialog.visible = false
    loadUsers()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error?.response?.data?.error || '保存失败')
    }
  } finally {
    userDialog.loading = false
  }
}

const removeUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 ${row.username} 吗？`, '提示', { type: 'warning' })
    await userApi.remove(row.id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.error || '删除失败')
    }
  }
}

const approveMerchant = async (row) => {
  try {
    await merchantApi.approve(row.id, currentUser.value?.id || 0, '管理员审核')
    ElMessage.success('已通过')
    loadMerchants()
  } catch (error) {
    ElMessage.error(error?.response?.data?.error || '操作失败')
  }
}

const rejectMerchant = async (row) => {
  try {
    await ElMessageBox.confirm('确认拒绝该商户？', '提示', { type: 'warning' })
    await merchantApi.reject(row.id, currentUser.value?.id || 0, '管理员拒绝')
    ElMessage.success('已拒绝')
    loadMerchants()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.error || '操作失败')
    }
  }
}

const approveProperty = async (row) => {
  try {
    await propertyApi.approve(row.id)
    ElMessage.success('已通过')
    loadPendingProperties()
  } catch (error) {
    ElMessage.error(error?.response?.data?.error || '操作失败')
  }
}

const rejectProperty = async (row) => {
  try {
    await ElMessageBox.confirm('确认拒绝/停用该民宿？', '提示', { type: 'warning' })
    await propertyApi.reject(row.id)
    ElMessage.success('已处理')
    loadPendingProperties()
    loadAllProperties()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.error || '操作失败')
    }
  }
}

const viewProperty = (row) => {
  router.push({ name: 'PropertyDetail', params: { id: row.id } })
}

const loadUsers = async () => {
  loading.users = true
  try {
    const { data } = await userApi.list()
    users.value = data || []
  } catch (error) {
    ElMessage.error('加载用户失败')
  } finally {
    loading.users = false
  }
}

const loadMerchants = async () => {
  loading.merchants = true
  try {
    const { data } = await merchantApi.getList({ status: 'PENDING' })
    merchants.value = data || []
  } catch (error) {
    ElMessage.error('加载商户失败')
  } finally {
    loading.merchants = false
  }
}

const loadPendingProperties = async () => {
  loading.properties = true
  try {
    const { data } = await propertyApi.getList({ status: 'PENDING' })
    pendingProperties.value = data || []
  } catch (error) {
    ElMessage.error('加载民宿申请失败')
  } finally {
    loading.properties = false
  }
}

const loadAllProperties = async () => {
  loading.propertiesAll = true
  try {
    const { data } = await propertyApi.getList()
    allProperties.value = data || []
  } catch (error) {
    ElMessage.error('加载民宿失败')
  } finally {
    loading.propertiesAll = false
  }
}

onMounted(() => {
  const user = localStorage.getItem('user')
  if (user) {
    try {
      currentUser.value = JSON.parse(user)
    } catch (e) {
      currentUser.value = null
    }
  }
  loadUsers()
  loadMerchants()
  loadPendingProperties()
  loadAllProperties()
})
</script>

<style scoped>
.admin-shell {
  min-height: calc(100vh - 64px);
  display: grid;
  grid-template-columns: 240px 1fr;
  background: #f8fafc;
}
.sider {
  background: #0f172a;
  color: #cbd5e1;
  display: flex;
  flex-direction: column;
  padding: 16px 0;
}
.logo {
  font-weight: 800;
  font-size: 20px;
  padding: 0 20px 12px;
  color: #e2e8f0;
}
.menu {
  border-right: none;
}
.content {
  padding: 22px 24px;
}
.panel {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}
.sub {
  color: #64748b;
  margin-top: 2px;
}
</style>
