<template>
  <div class="merchant-orders">
    <el-page-header content="订单管理" icon="ArrowLeft" @back="$router.push('/')" />

    <el-card shadow="hover" class="mt-16">
      <template #header>
        <div class="card-header">
          <div>
            <div class="title">商户订单</div>
            <div class="subtitle">仅展示支付成功的订单，便于前台快速办理入住与退宿。</div>
          </div>
          <div class="actions">
            <el-select
              v-model="filters.bookingStatus"
              placeholder="按入住状态"
              clearable
              size="small"
              style="width: 150px"
            >
              <el-option label="全部" value="" />
              <el-option label="未入住" value="CONFIRMED" />
              <el-option label="已入住" value="CHECKED_IN" />
              <el-option label="已退宿" value="CHECKED_OUT" />
            </el-select>
            <el-input
              v-model="filters.keyword"
              size="small"
              clearable
              placeholder="联系人/电话/订单号"
              style="width: 200px"
            />
            <el-button type="primary" size="small" @click="loadOrders">筛选</el-button>
          </div>
        </div>
      </template>

      <el-table :data="orders" stripe v-loading="loading">
        <el-table-column label="支付/订单" min-width="200">
          <template #default="{ row }">
            <div class="strong">支付号：{{ row.paymentNumber }}</div>
            <div class="sub">预订号：{{ row.booking?.bookingNumber || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="客人信息" min-width="180">
          <template #default="{ row }">
            <div>{{ row.booking?.contactName || '-' }}</div>
            <div class="sub">{{ row.booking?.contactPhone || row.booking?.user?.phone || '-' }}</div>
            <div class="sub">用户：{{ row.booking?.user?.username || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="房源/床型" min-width="240">
          <template #default="{ row }">
            <div class="strong">{{ row.booking?.property?.propertyName || '-' }}</div>
            <div class="sub">
              {{ row.booking?.roomType?.typeName || '房型未填' }}
              <span v-if="row.booking?.roomType?.bedType"> · {{ row.booking.roomType.bedType }}</span>
            </div>
            <div class="sub">{{ row.booking?.property?.city || '' }} {{ row.booking?.property?.address || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="入住/退房" width="170">
          <template #default="{ row }">
            <div>{{ row.booking?.checkInDate || '-' }} → {{ row.booking?.checkOutDate || '-' }}</div>
            <div class="sub">晚数：{{ row.booking?.nights || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            ¥{{ Number(row.amount || 0).toFixed(2) }}
            <div class="sub">{{ methodText(row.paymentMethod) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="支付时间" width="170">
          <template #default="{ row }">
            <div>{{ row.paidAt || '-' }}</div>
            <el-tag size="small" :type="paymentStatusType(row.status)" class="mt-4">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="入住状态" width="140">
          <template #default="{ row }">
            <el-tag :type="bookingStatusType(row.booking?.status)">{{ bookingStatusText(row.booking?.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="success"
              plain
              :disabled="!canCheckIn(row)"
              @click="markCheckIn(row)"
            >
              入住
            </el-button>
            <el-button
              size="small"
              type="warning"
              plain
              :disabled="!canCheckOut(row)"
              @click="markCheckOut(row)"
            >
              退宿
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookingApi, paymentApi } from '../api'

const currentUser = ref(null)
const orders = ref([])
const loading = ref(false)
const filters = reactive({
  bookingStatus: '',
  keyword: ''
})

const methodText = (val) => {
  const map = {
    ALIPAY: '支付宝',
    WECHAT: '微信',
    BANK_CARD: '银行卡',
    CASH: '现金',
    OTHER: '其他'
  }
  return map[val] || val || '-'
}

const statusText = (val) => {
  const map = { PENDING: '待支付', SUCCESS: '已支付', FAILED: '失败', CANCELLED: '已取消' }
  return map[val] || val || '-'
}
const paymentStatusType = (val) => {
  const map = { PENDING: 'warning', SUCCESS: 'success', FAILED: 'danger', CANCELLED: 'info' }
  return map[val] || 'info'
}

const bookingStatusText = (val) => {
  const map = {
    PENDING: '待确认',
    CONFIRMED: '未入住',
    CHECKED_IN: '已入住',
    CHECKED_OUT: '已退宿',
    CANCELLED: '已取消',
    REFUNDED: '已退款'
  }
  return map[val] || '未同步'
}
const bookingStatusType = (val) => {
  const map = {
    PENDING: 'warning',
    CONFIRMED: 'primary',
    CHECKED_IN: 'success',
    CHECKED_OUT: 'info',
    CANCELLED: 'info',
    REFUNDED: 'info'
  }
  return map[val] || 'info'
}

const canCheckIn = (row) => {
  const status = row?.booking?.status
  return row?.status === 'SUCCESS' && (status === 'CONFIRMED' || status === 'PENDING')
}
const canCheckOut = (row) => row?.status === 'SUCCESS' && row?.booking?.status === 'CHECKED_IN'

const applyFilters = (list) => {
  let result = [...(list || [])]
  if (filters.bookingStatus) {
    result = result.filter((item) => item.booking?.status === filters.bookingStatus)
  }
  if (filters.keyword && filters.keyword.trim()) {
    const kw = filters.keyword.trim().toLowerCase()
    result = result.filter((item) => {
      const booking = item.booking || {}
      const propertyName = booking.property?.propertyName || ''
      const roomType = booking.roomType?.typeName || ''
      return (
        (booking.bookingNumber || '').toLowerCase().includes(kw) ||
        (item.paymentNumber || '').toLowerCase().includes(kw) ||
        (booking.contactName || '').toLowerCase().includes(kw) ||
        (booking.contactPhone || '').toLowerCase().includes(kw) ||
        propertyName.toLowerCase().includes(kw) ||
        roomType.toLowerCase().includes(kw)
      )
    })
  }
  return result
}

const loadOrders = async () => {
  if (!currentUser.value?.merchantId) {
    ElMessage.warning('仅商户可查看订单列表')
    return
  }
  loading.value = true
  try {
    const params = { merchantId: currentUser.value.merchantId, status: 'SUCCESS' }
    const { data } = await paymentApi.getList(params)
    orders.value = applyFilters(data || [])
  } catch (err) {
    ElMessage.error('加载订单失败')
    console.error('loadOrders failed', err)
  } finally {
    loading.value = false
  }
}

const markCheckIn = async (row) => {
  if (!row?.booking?.id) return
  try {
    await bookingApi.checkIn(row.booking.id)
    ElMessage.success('已标记为入住')
    loadOrders()
  } catch (err) {
    const msg = err?.response?.data?.error || err?.message || '操作失败'
    ElMessage.error(msg)
  }
}

const markCheckOut = async (row) => {
  if (!row?.booking?.id) return
  try {
    await ElMessageBox.confirm('确认将该订单标记为退宿吗？', '退宿确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await bookingApi.checkOut(row.booking.id)
    ElMessage.success('已标记为退宿')
    loadOrders()
  } catch (err) {
    if (err !== 'cancel') {
      const msg = err?.response?.data?.error || err?.message || '操作失败'
      ElMessage.error(msg)
    }
  }
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
  loadOrders()
})
</script>

<style scoped>
.merchant-orders {
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
  gap: 12px;
}
.title {
  font-size: 18px;
  font-weight: 600;
}
.subtitle {
  color: #909399;
  font-size: 13px;
}
.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.sub {
  color: #909399;
  font-size: 12px;
}
.strong {
  font-weight: 600;
}
.mt-4 {
  margin-top: 4px;
}
</style>
