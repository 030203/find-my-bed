<template>
  <div class="roomtype">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>房型管理</span>
          <div class="actions">
            <el-input v-model="propertyId" placeholder="绑定民宿ID" style="width: 180px; margin-right: 10px" />
            <el-button type="primary" @click="loadRoomTypes">刷新</el-button>
            <el-button type="success" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增房型
            </el-button>
          </div>
        </div>
      </template>

      <el-alert
        v-if="!propertyId"
        title="请先输入或从民宿列表进入选择的民宿ID"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 12px"
      />

      <el-table :data="roomTypeList" stripe v-loading="loading">
        <el-table-column label="图片" width="120">
          <template #default="{ row }">
            <div class="table-cover" :style="roomCoverStyle(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="typeName" label="房型名称" width="180" />
        <el-table-column prop="basePrice" label="基础价" width="100">
          <template #default="{ row }">¥ {{ row.basePrice }}</template>
        </el-table-column>
        <el-table-column prop="maxOccupancy" label="可住人数" width="100" />
        <el-table-column prop="bedType" label="床型" width="120" />
        <el-table-column prop="roomSize" label="面积(㎡)" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px">
        <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
          <el-form-item label="民宿ID" prop="propertyId">
            <el-input v-model="form.propertyId" placeholder="请输入民宿ID" type="number" />
          </el-form-item>
          <el-form-item label="房型图片 URL">
            <el-input v-model="form.imageUrl" placeholder="https://example.com/room.jpg" />
          </el-form-item>
          <el-form-item label="房型名称" prop="typeName">
            <el-input v-model="form.typeName" placeholder="如：标准间、豪华大床" />
          </el-form-item>
          <el-form-item label="基础价格" prop="basePrice">
            <el-input v-model="form.basePrice" placeholder="价格" type="number" />
          </el-form-item>
          <el-form-item label="最大入住人数" prop="maxOccupancy">
            <el-input v-model="form.maxOccupancy" placeholder="人数" type="number" />
          </el-form-item>
          <el-form-item label="床型" prop="bedType">
            <el-input v-model="form.bedType" placeholder="大床/双床/榻榻米" />
          </el-form-item>
          <el-form-item label="房间面积(㎡)" prop="roomSize">
            <el-input v-model="form.roomSize" placeholder="如 28" type="number" />
          </el-form-item>
          <el-form-item label="房型描述" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="启用" value="ACTIVE" />
              <el-option label="停用" value="INACTIVE" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { roomTypeApi } from '../api/index'

const route = useRoute()
const propertyId = ref(route.query.propertyId || '')
const roomTypeList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增房型')
const submitting = ref(false)
const formRef = ref(null)

const form = ref({
  id: null,
  propertyId: '',
  typeName: '',
  description: '',
  imageUrl: '',
  maxOccupancy: 2,
  bedType: '',
  roomSize: '',
  basePrice: '',
  status: 'ACTIVE'
})

const rules = {
  propertyId: [{ required: true, message: '请选择民宿ID', trigger: 'blur' }],
  typeName: [{ required: true, message: '请输入房型名称', trigger: 'blur' }],
  basePrice: [{ required: true, message: '请输入基础价', trigger: 'blur' }]
}

const parseImages = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    if (typeof images === 'string') {
      const match = images.match(/https?:[^\\s\\]]+/)
      return match ? [match[0]] : []
    }
    return []
  }
}

const roomCoverStyle = (row) => {
  const imgs = parseImages(row?.images)
  const url = imgs[0]
  return url
    ? { backgroundImage: `url(${url})`, backgroundSize: 'cover', backgroundPosition: 'center' }
    : { background: 'linear-gradient(135deg, #d1f7e5 0%, #ecfdf5 100%)' }
}

const loadRoomTypes = async () => {
  if (!propertyId.value) {
    roomTypeList.value = []
    return
  }
  loading.value = true
  try {
    const { data } = await roomTypeApi.getList({ propertyId: propertyId.value })
    roomTypeList.value = data || []
  } catch (error) {
    ElMessage.error('加载房型失败: ' + (error.response?.data?.error || error.message))
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增房型'
  form.value = {
    id: null,
    propertyId: propertyId.value || '',
    typeName: '',
    description: '',
    imageUrl: '',
    maxOccupancy: 2,
    bedType: '',
    roomSize: '',
    basePrice: '',
    status: 'ACTIVE'
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  const imgs = parseImages(row.images)
  dialogTitle.value = '编辑房型'
  form.value = { ...row, imageUrl: imgs[0] || '', propertyId: row.property?.id || row.propertyId || propertyId.value }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除房型 "${row.typeName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await roomTypeApi.delete(row.id)
    ElMessage.success('删除成功')
    loadRoomTypes()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.response?.data?.error || error.message))
    }
  }
}

const handleSubmit = async () => {
  if (!form.value.propertyId) {
    ElMessage.error('请先填写民宿ID')
    return
  }
  try {
    await formRef.value.validate()
    submitting.value = true
    const { imageUrl, ...rest } = form.value
    const trimmed = imageUrl && imageUrl.trim()
    const imagesValue = trimmed ? JSON.stringify([trimmed]) : (rest.images ?? null)
    const payload = {
      ...rest,
      images: imagesValue
    }
    if (form.value.id) {
      await roomTypeApi.update(form.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await roomTypeApi.create(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRoomTypes()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败: ' + (error.response?.data?.error || error.message))
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadRoomTypes()
})
</script>

<style scoped>
.roomtype {
  min-height: calc(100vh - 100px);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
.actions {
  display: flex;
  align-items: center;
}
.table-cover {
  width: 88px;
  height: 56px;
  border-radius: 10px;
  background: #f3f6fb;
  border: 1px solid #e5e9f2;
}
</style>
