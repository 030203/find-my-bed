<template>
  <div class="booking">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>预定管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增预定
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已入住" value="CHECKED_IN" />
            <el-option label="已退房" value="CHECKED_OUT" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadBookings">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table 
        :data="bookingList" 
        stripe 
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="bookingNumber" label="预定单号" width="180" />
        <el-table-column label="酒店" min-width="160">
          <template #default="{ row }">
            <el-link type="primary" @click="goToProperty(row.property?.id)" :underline="false">
              {{ row.property?.propertyName || '-' }}
            </el-link>
            <div class="subtext" v-if="row.roomType?.typeName">{{ row.roomType.typeName }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column prop="checkOutDate" label="退房日期" width="120" />
        <el-table-column prop="nights" label="天数" width="80" />
        <el-table-column prop="numberOfGuests" label="人数" width="80" />
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="handlePay(row)"
              v-if="row.paymentStatus !== 'PAID' && row.paymentStatus !== 'REFUNDED'"
            >
              去支付
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadBookings"
        @current-change="loadBookings"
        style="margin-top: 20px; justify-content: flex-end"
      />

      <el-dialog 
        v-model="dialogVisible" 
        :title="dialogTitle"
        width="600px"
      >
        <el-form :model="form" label-width="120px" ref="formRef" :rules="rules">
          <el-form-item label="民宿" prop="propertyId">
            <el-select v-model="form.propertyId" placeholder="请选择民宿" style="width: 100%" @change="loadRoomTypes">
              <el-option 
                v-for="property in propertyList" 
                :key="property.id" 
                :label="property.propertyName" 
                :value="property.id" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="房型" prop="roomTypeId">
            <el-select v-model="form.roomTypeId" placeholder="请选择房型" style="width: 100%">
              <el-option 
                v-for="roomType in roomTypeList" 
                :key="roomType.id" 
                :label="roomType.typeName" 
                :value="roomType.id" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="入住日期" prop="checkInDate">
            <el-date-picker
              v-model="form.checkInDate"
              type="date"
              placeholder="选择入住日期"
              style="width: 100%"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="退房日期" prop="checkOutDate">
            <el-date-picker
              v-model="form.checkOutDate"
              type="date"
              placeholder="选择退房日期"
              style="width: 100%"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="入住人数" prop="numberOfGuests">
            <el-input-number v-model="form.numberOfGuests" :min="1" :max="10" />
          </el-form-item>
          <el-form-item label="联系人姓名" prop="contactName">
            <el-input v-model="form.contactName" />
          </el-form-item>
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="form.contactPhone" />
          </el-form-item>
          <el-form-item label="联系邮箱" prop="contactEmail">
            <el-input v-model="form.contactEmail" />
          </el-form-item>
          <el-form-item label="特殊要求">
            <el-input v-model="form.specialRequests" type="textarea" :rows="3" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="detailDialogVisible" title="预订详情" width="520px" :show-close="true">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="预定单号">{{ detailData.bookingNumber }}</el-descriptions-item>
          <el-descriptions-item label="民宿">{{ detailData.property?.propertyName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="房型">{{ detailData.roomType?.typeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="入住 / 退房">
            {{ detailData.checkInDate }} ~ {{ detailData.checkOutDate }}
          </el-descriptions-item>
          <el-descriptions-item label="天数">{{ detailData.nights }}</el-descriptions-item>
          <el-descriptions-item label="人数">{{ detailData.numberOfGuests }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ detailData.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detailData.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">{{ detailData.contactEmail || '-' }}</el-descriptions-item>
          <el-descriptions-item label="总金额">
            ¥{{ detailData.totalAmount ? Number(detailData.totalAmount).toFixed(2) : '0.00' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusText(detailData.status) }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">{{ getPaymentStatusText(detailData.paymentStatus) }}</el-descriptions-item>
          <el-descriptions-item label="特殊要求">
            <span>{{ detailData.specialRequests || '无' }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <template #footer>
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="payDialogVisible" title="支付订单" width="420px">
        <el-form label-width="100px">
          <el-form-item label="订单号">
            <span>{{ payForm.bookingNumber }}</span>
          </el-form-item>
          <el-form-item label="支付方式">
            <el-select v-model="payForm.paymentMethod" style="width: 100%">
              <el-option label="支付宝" value="ALIPAY" />
              <el-option label="微信" value="WECHAT" />
              <el-option label="银行卡" value="BANK_CARD" />
            </el-select>
          </el-form-item>
          <el-form-item label="支付金额">
            <el-input-number v-model="payForm.amount" :min="0" :step="1" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="用户自助支付，提交后生成支付记录并标记成功"
          />
        </el-form>
        <template #footer>
          <el-button @click="payDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="paySubmitting" @click="submitPay">确认支付</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { bookingApi, propertyApi, roomTypeApi, paymentApi } from '../api/index'
import { useRouter } from 'vue-router'

const bookingList = ref([])
const propertyList = ref([])
const roomTypeList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增预定')
const submitting = ref(false)
const formRef = ref(null)
const detailDialogVisible = ref(false)
const detailData = ref({})
const payDialogVisible = ref(false)
const paySubmitting = ref(false)
const payForm = ref({
  bookingId: null,
  bookingNumber: '',
  amount: 0,
  paymentMethod: 'ALIPAY'
})
const router = useRouter()
const payBookingRow = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = ref({
  status: ''
})

const currentUser = ref(null)

const form = ref({
  id: null,
  propertyId: null,
  roomTypeId: null,
  checkInDate: '',
  checkOutDate: '',
  numberOfGuests: 1,
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  specialRequests: ''
})

const rules = {
  propertyId: [{ required: true, message: '请选择民宿', trigger: 'change' }],
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择退房日期', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'CHECKED_IN': '',
    'CHECKED_OUT': 'info',
    'CANCELLED': 'danger',
    'REFUNDED': 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认/可入住',
    'CHECKED_IN': '已入住',
    'CHECKED_OUT': '已退房',
    'CANCELLED': '已取消',
    'REFUNDED': '已退款'
  }
  return map[status] || status
}

const loadBookings = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm.value
    }
    if (!params.status) {
      delete params.status
    }
    if (currentUser.value?.role === 'USER') {
      params.userId = currentUser.value.id
    } else if (currentUser.value?.role === 'MERCHANT' && currentUser.value.merchantId) {
      params.merchantId = currentUser.value.merchantId
    }
    const response = await bookingApi.getList(params)
    bookingList.value = response.data || []
    total.value = response.data?.length || 0
  } catch (error) {
    ElMessage.error('加载预定数据失败: ' + (error.response?.data?.error || error.message))
  } finally {
    loading.value = false
  }
}

const loadProperties = async () => {
  try {
    const params = {}
    if (currentUser.value?.role === 'MERCHANT' && currentUser.value.merchantId) {
      params.merchantId = currentUser.value.merchantId
    } else {
      params.status = 'APPROVED'
    }
    const response = await propertyApi.getList(params)
    propertyList.value = response.data || []
  } catch (error) {
    console.error('加载民宿列表失败:', error)
  }
}

const loadRoomTypes = async () => {
  if (!form.value.propertyId) {
    roomTypeList.value = []
    return
  }
  try {
    const response = await roomTypeApi.getActiveByPropertyId(form.value.propertyId)
    roomTypeList.value = response.data || []
    form.value.roomTypeId = null
  } catch (error) {
    console.error('加载房型列表失败:', error)
    ElMessage.error('加载房型列表失败')
    roomTypeList.value = []
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增预定'
  form.value = {
    id: null,
    propertyId: null,
    roomTypeId: null,
    checkInDate: '',
    checkOutDate: '',
    numberOfGuests: 1,
    contactName: '',
    contactPhone: '',
    contactEmail: '',
    specialRequests: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑预定'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleView = (row) => {
  detailData.value = { ...row }
  detailDialogVisible.value = true
}

const calcOutstanding = (row) => {
  const total = Number(row.totalAmount || 0)
  const paid = Number(row.paidAmount || 0)
  const remain = total - paid
  return remain > 0 ? remain : 0
}

const handlePay = (row) => {
  router.push({
    name: 'PendingPayment',
    query: { bookingId: row.id }
  })
}

const submitPay = async () => {
  paySubmitting.value = true
  try {
    const payload = {
      booking: { id: payForm.value.bookingId },
      amount: payForm.value.amount,
      paymentMethod: payForm.value.paymentMethod,
      paymentType: 'FULL',
      status: 'SUCCESS',
      remark: '用户自助支付',
      paidAt: new Date().toISOString()
    }
    await paymentApi.create(payload)
    if (payBookingRow.value) {
      const bookingPayload = {
        ...payBookingRow.value,
        paymentStatus: 'PAID',
        status: payBookingRow.value.status === 'PENDING' ? 'CONFIRMED' : payBookingRow.value.status
      }
      await bookingApi.update(payBookingRow.value.id, bookingPayload)
    }
    ElMessage.success('支付成功，订单已记录')
    payDialogVisible.value = false
    loadBookings()
  } catch (error) {
    ElMessage.error('支付失败: ' + (error.response?.data?.error || error.message))
  } finally {
    paySubmitting.value = false
  }
}

const goToProperty = (propertyId) => {
  if (!propertyId) {
    ElMessage.info('暂无酒店信息')
    return
  }
  router.push({ name: 'PropertyDetail', params: { id: propertyId } })
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.prompt('请输入取消原因', '取消预定', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea'
    })
    await bookingApi.cancel(row.id, '')
    ElMessage.success('取消成功')
    loadBookings()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败: ' + (error.response?.data?.error || error.message))
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (form.value.id) {
      await bookingApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      const response = await bookingApi.create(form.value)
      ElMessage.success('创建成功')
      if (currentUser.value?.role === 'USER') {
        dialogVisible.value = false
        router.push({
          name: 'PendingPayment',
          query: { bookingId: response?.data?.id }
        })
        return
      }
    }
    
    dialogVisible.value = false
    loadBookings()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败: ' + (error.response?.data?.error || error.message))
    }
  } finally {
    submitting.value = false
  }
}

const resetSearch = () => {
  searchForm.value = { status: '' }
  loadBookings()
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      currentUser.value = JSON.parse(userStr)
    } catch (e) {
      currentUser.value = null
    }
  }
  loadBookings()
  loadProperties()
})
</script>

<style scoped>
.booking {
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
.subtext {
  color: #909399;
  font-size: 12px;
}
</style>
