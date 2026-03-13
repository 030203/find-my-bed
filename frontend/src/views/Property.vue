<template>
  <div class="property">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>民宿管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增民宿
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="城市">
          <el-input v-model="searchForm.city" placeholder="请输入城市" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已暂停" value="SUSPENDED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadProperties">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="propertyList" stripe v-loading="loading" style="width: 100%">
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <div class="table-cover" :style="coverStyle(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="propertyName" label="民宿名称" width="200" />
        <el-table-column prop="propertyType" label="类型" width="100">
          <template #default="{ row }">
            {{ getPropertyTypeText(row.propertyType) }}
          </template>
        </el-table-column>
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column prop="address" label="地址" width="200" />
        <el-table-column prop="totalRooms" label="总房间数" width="100" />
        <el-table-column prop="availableRooms" label="可用房间" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="success" size="small" @click="handleManageRoomType(row)">房型</el-button>
            <el-button type="warning" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            <template v-if="isAdmin && row.status === 'PENDING'">
              <el-button type="primary" size="small" @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadProperties"
        @current-change="loadProperties"
        style="margin-top: 20px; justify-content: flex-end"
      />

      <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
        <el-form :model="form" label-width="120px" ref="formRef" :rules="rules">
          <el-form-item label="商户ID" prop="merchantId">
            <el-input v-model="form.merchantId" :disabled="isMerchant" placeholder="请输入商户ID" type="number" />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="民宿名称" prop="propertyName">
                <el-input v-model="form.propertyName" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="类型" prop="propertyType">
                <el-select v-model="form.propertyType" style="width: 100%">
                  <el-option label="民宿" value="HOMESTAY" />
                  <el-option label="酒店" value="HOTEL" />
                  <el-option label="公寓" value="APARTMENT" />
                  <el-option label="别墅" value="VILLA" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="封面图片 URL" prop="imageUrl">
                <el-input v-model="form.imageUrl" placeholder="https://example.com/cover.jpg" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="是否推荐">
                <el-switch v-model="form.isFeatured" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="描述" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="4" />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="省份" prop="province">
                <el-input v-model="form.province" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="城市" prop="city">
                <el-input v-model="form.city" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="区县" prop="district">
                <el-input v-model="form.district" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="详细地址" prop="address">
            <el-input v-model="form.address" />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="房间数量" prop="totalRooms">
                <el-input-number v-model="form.totalRooms" :min="1" :step="1" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="经度" prop="longitude">
                <el-input-number v-model="form.longitude" :precision="7" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="纬度" prop="latitude">
                <el-input-number v-model="form.latitude" :precision="7" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="入住时间" prop="checkInTime">
                <el-time-picker v-model="form.checkInTime" format="HH:mm" value-format="HH:mm" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="退房时间" prop="checkOutTime">
                <el-time-picker v-model="form.checkOutTime" format="HH:mm" value-format="HH:mm" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { propertyApi } from '../api/index'

const propertyList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增民宿')
const submitting = ref(false)
const formRef = ref(null)
const router = useRouter()
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = ref({
  city: '',
  status: ''
})

const currentUser = ref(null)
const isMerchant = computed(() => currentUser.value?.role === 'MERCHANT')
const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')

const form = ref({
  id: null,
  merchantId: null,
  propertyName: '',
  propertyType: 'HOMESTAY',
  description: '',
  imageUrl: '',
  isFeatured: false,
  province: '',
  city: '',
  district: '',
  address: '',
  totalRooms: 1,
  longitude: null,
  latitude: null,
  checkInTime: '14:00',
  checkOutTime: '12:00'
})

const parseImages = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    if (typeof images === 'string') {
      // 兜底：字符串里提取第一个 http 开头的 URL，避免因格式问题丢图
      const match = images.match(/https?:[^\\s\\]]+/)
      return match ? [match[0]] : []
    }
    return []
  }
}

const rules = {
  merchantId: [{ required: true, message: '请输入商户ID', trigger: 'blur' }],
  propertyName: [{ required: true, message: '请输入民宿名称', trigger: 'blur' }],
  propertyType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  totalRooms: [{ required: true, type: 'number', min: 1, message: '请输入房间数量', trigger: 'change' }],
  imageUrl: [{ type: 'url', message: '请输入有效的封面图片 URL', trigger: 'blur' }]
}

const getPropertyTypeText = (type) => {
  const map = {
    HOMESTAY: '民宿',
    HOTEL: '酒店',
    APARTMENT: '公寓',
    VILLA: '别墅',
    OTHER: '其他'
  }
  return map[type] || type
}

const getStatusType = (status) => {
  const map = {
    DRAFT: 'info',
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    SUSPENDED: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    DRAFT: '草稿',
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    SUSPENDED: '已暂停'
  }
  return map[status] || status
}

const coverStyle = (row) => {
  const imgs = parseImages(row?.images)
  const url = imgs[0]
  return url
    ? { backgroundImage: `url(${url})`, backgroundSize: 'cover', backgroundPosition: 'center' }
    : { background: 'linear-gradient(135deg, #cfe5ff 0%, #eef5ff 100%)' }
}

const loadProperties = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm.value
    }
    if (isMerchant.value && currentUser.value?.merchantId) {
      params.merchantId = currentUser.value.merchantId
    }
    const response = await propertyApi.getList(params)
    propertyList.value = response.data || []
    total.value = response.data?.length || 0
  } catch (error) {
    ElMessage.error('加载民宿数据失败: ' + (error.response?.data?.error || error.message))
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增民宿'
  form.value = {
    id: null,
    merchantId: null,
    propertyName: '',
    propertyType: 'HOMESTAY',
    description: '',
    imageUrl: '',
    isFeatured: false,
    province: '',
    city: '',
    district: '',
    address: '',
    totalRooms: 1,
    longitude: null,
    latitude: null,
    checkInTime: '14:00',
    checkOutTime: '12:00'
  }
  if (isMerchant.value && currentUser.value?.merchantId) {
    form.value.merchantId = currentUser.value.merchantId
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑民宿'
  const imgs = parseImages(row.images)
  form.value = { ...row, imageUrl: imgs[0] || '' }
  dialogVisible.value = true
}

const handleView = (row) => {
  router.push({ name: 'PropertyDetail', params: { id: row.id } })
}

const handleManageRoomType = (row) => {
  router.push({ name: 'RoomType', query: { propertyId: row.id } })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除民宿 "${row.propertyName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await propertyApi.delete(row.id)
    ElMessage.success('删除成功')
    loadProperties()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.response?.data?.error || error.message))
    }
  }
}

const handleApprove = async (row) => {
  try {
    await propertyApi.approve(row.id)
    ElMessage.success('已通过审核')
    loadProperties()
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.response?.data?.error || error.message))
  }
}

const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm('确认拒绝该民宿吗？', '提示', { type: 'warning' })
    await propertyApi.reject(row.id)
    ElMessage.success('已拒绝')
    loadProperties()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + (error.response?.data?.error || error.message))
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    const { imageUrl, ...rest } = form.value
    const trimmedImage = imageUrl && imageUrl.trim()
    const imagesValue = trimmedImage ? JSON.stringify([trimmedImage]) : (rest.images ?? null)
    const payload = {
      ...rest,
      images: imagesValue
    }
    if (!form.value.id && payload.totalRooms != null && payload.availableRooms == null) {
      payload.availableRooms = payload.totalRooms
    }

    if (form.value.id) {
      await propertyApi.update(form.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await propertyApi.create(payload)
      ElMessage.success('提交成功，等待审核')
    }

    dialogVisible.value = false
    loadProperties()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败: ' + (error.response?.data?.error || error.message))
    }
  } finally {
    submitting.value = false
  }
}

const resetSearch = () => {
  searchForm.value = { city: '', status: '' }
  loadProperties()
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
  loadProperties()
})
</script>

<style scoped>
.property {
  min-height: calc(100vh - 100px);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
.search-form {
  margin-bottom: 20px;
}
.table-cover {
  width: 88px;
  height: 56px;
  border-radius: 10px;
  background: #f2f6fc;
  border: 1px solid #e5e9f2;
}
</style>
