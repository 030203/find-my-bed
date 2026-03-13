<template>
  <div class="payment">
    <el-page-header content="支付管理" icon="ArrowLeft" @back="$router.push('/')" />

    <el-card shadow="hover" class="mt-16">
      <template #header>
        <div class="card-header">
          <div>支付记录</div>
          <div class="actions">
            <el-select v-model="filters.status" placeholder="按状态" clearable size="small" style="width: 140px">
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
            <el-input v-model="filters.bookingId" size="small" placeholder="按预订ID过滤" style="width: 180px" clearable />
            <el-button type="primary" size="small" @click="loadPayments">筛选</el-button>
          </div>
        </div>
      </template>

      <el-table :data="sortedPayments" stripe v-loading="loading" :row-class-name="rowClassName">
        <el-table-column prop="paymentNumber" label="支付单号" width="190" />
        <el-table-column label="预订/酒店" min-width="220">
          <template #default="{ row }">
            <div><span class="sub">预订ID：</span>{{ row.booking?.id || '-' }}</div>
            <div>
              <el-link
                v-if="row.booking?.property?.id"
                type="primary"
                :underline="false"
                @click="goToProperty(row.booking.property.id)"
              >
                {{ row.booking?.property?.propertyName || '查看酒店' }}
              </el-link>
              <span v-else>-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="110">
          <template #default="{ row }">¥{{ Number(row.amount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="方式" width="110">
          <template #default="{ row }">{{ methodText(row.paymentMethod) }}</template>
        </el-table-column>
        <el-table-column prop="paymentType" label="类型" width="110">
          <template #default="{ row }">{{ typeText(row.paymentType) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="入住状态" width="110">
          <template #default="{ row }">
            <el-tag :type="bookingStatusType(row.booking?.status)">{{ bookingStatusText(row.booking?.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paidAt" label="支付时间" width="180">
          <template #default="{ row }">{{ row.paidAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="goToProperty(row.booking?.property?.id)">查看酒店</el-button>

            <template v-if="!isWaste(row)">
              <el-button size="small" type="info" plain @click="openDialog(row)">支付信息</el-button>
              <el-button size="small" type="warning" plain @click="refund(row)" :disabled="!canRefund(row)">退款</el-button>
            </template>

            <template v-else>
              <el-popconfirm title="确认删除？" @confirm="remove(row)">
                <template #reference>
                  <el-button size="small" type="danger" plain>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="支付信息" width="520px">
      <el-form :model="detail" label-width="120px">
        <el-form-item label="预订ID"><el-input v-model="detail.bookingId" disabled /></el-form-item>
        <el-form-item label="金额"><el-input v-model="detail.amount" disabled /></el-form-item>
        <el-form-item label="支付方式"><el-input v-model="detail.paymentMethod" disabled /></el-form-item>
        <el-form-item label="支付类型"><el-input v-model="detail.paymentType" disabled /></el-form-item>
        <el-form-item label="状态"><el-input v-model="detail.status" disabled /></el-form-item>
        <el-form-item label="支付时间"><el-input v-model="detail.paidAt" disabled /></el-form-item>
        <el-form-item label="备注"><el-input v-model="detail.remark" type="textarea" :rows="3" disabled /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { paymentApi } from '../api'

const router = useRouter()
const payments = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentUser = ref(null)

const filters = reactive({
  status: '',
  bookingId: ''
})

const detail = reactive({
  bookingId: '',
  amount: '',
  paymentMethod: '',
  paymentType: '',
  status: '',
  paidAt: '',
  remark: ''
})

const statusOptions = [
  { label: '待支付', value: 'PENDING' },
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAILED' },
  { label: '已取消', value: 'CANCELLED' }
]

const methodText = (val) => {
  const map = { ALIPAY: '支付宝', WECHAT: '微信', BANK_CARD: '银行卡', CASH: '现金', OTHER: '其他' }
  return map[val] || val || '-'
}
const typeText = (val) => {
  const map = { DEPOSIT: '定金', FULL: '全款', REFUND: '退款' }
  return map[val] || val || '-'
}
const statusText = (val) => {
  const map = { PENDING: '待支付', SUCCESS: '成功', FAILED: '失败', CANCELLED: '已取消' }
  return map[val] || val || '-'
}
const statusType = (val) => {
  const map = { PENDING: 'warning', SUCCESS: 'success', FAILED: 'danger', CANCELLED: 'info' }
  return map[val] || 'info'
}

const bookingStatusText = (val) => {
  const map = {
    PENDING: '待确认',
    CONFIRMED: '待入住',
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

const isWaste = (row) => row?.status === 'CANCELLED' || row?.status === 'FAILED' || row?.paymentType === 'REFUND'
const canRefund = (row) => row?.status === 'SUCCESS' && row?.booking?.status === 'CONFIRMED'

const sortedPayments = computed(() => {
  const activeWeights = { SUCCESS: 0, PENDING: 1 }
  const list = [...(payments.value || [])]
  return list.sort((a, b) => {
    const aWeight = isWaste(a) ? 2 : activeWeights[a?.status] ?? 1
    const bWeight = isWaste(b) ? 2 : activeWeights[b?.status] ?? 1
    if (aWeight !== bWeight) return aWeight - bWeight
    const toTime = (row) => {
      const ts = row?.paidAt || row?.updatedAt || row?.createdAt
      return ts ? new Date(ts).getTime() : 0
    }
    return toTime(b) - toTime(a)
  })
})

const firstWasteIndex = computed(() => sortedPayments.value.findIndex((item) => isWaste(item)))
const rowClassName = ({ rowIndex }) => {
  const row = sortedPayments.value[rowIndex]
  if (!row) return ''
  const classes = []
  if (isWaste(row)) {
    classes.push('waste-row')
    if (rowIndex === firstWasteIndex.value && rowIndex !== 0) {
      classes.push('waste-divider')
    }
  }
  return classes.join(' ')
}

const goToProperty = (propertyId) => {
  if (!propertyId) {
    ElMessage.info('暂无酒店信息')
    return
  }
  router.push({ name: 'PropertyDetail', params: { id: propertyId } })
}

const openDialog = (row) => {
  Object.assign(detail, {
    bookingId: row?.booking?.id || '',
    amount: row?.amount ?? '',
    paymentMethod: methodText(row?.paymentMethod),
    paymentType: typeText(row?.paymentType),
    status: statusText(row?.status),
    paidAt: row?.paidAt || '',
    remark: row?.remark || ''
  })
  dialogVisible.value = true
}

const loadPayments = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.status) params.status = filters.status
    if (filters.bookingId) params.bookingId = filters.bookingId
    if (currentUser.value?.role === 'USER') {
      params.userId = currentUser.value.id
    } else if (currentUser.value?.role === 'MERCHANT' && currentUser.value.merchantId) {
      params.merchantId = currentUser.value.merchantId
    }
    const { data } = await paymentApi.getList(params)
    payments.value = data || []
  } catch (err) {
    ElMessage.error('加载支付记录失败')
  } finally {
    loading.value = false
  }
}

const refund = async (row) => {
  try {
    await paymentApi.update(row.id, { ...row, status: 'CANCELLED', paymentType: 'REFUND', remark: '用户退款' })
    ElMessage.success('退款成功')
    loadPayments()
  } catch (err) {
    ElMessage.error(err?.response?.data?.error || `退款失败（${err?.response?.status || err?.message || 'unknown'}）`)
  }
}

const remove = async (row) => {
  const id = row?.id
  if (!id) {
    ElMessage.error('无有效支付ID')
    return
  }
  try {
    await paymentApi.delete(id)
    ElMessage.success('已删除')
    payments.value = payments.value.filter((p) => p.id !== id)
  } catch (err) {
    const status = err?.response?.status
    const msg = err?.response?.data?.error || err?.response?.data?.message || err?.message
    console.error('delete payment failed', { id, status, err })
    ElMessage.error(`删除失败（${status || 'unknown'}）：${msg || 'unknown'}`)
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
  loadPayments()
})
</script>

<style scoped>
.payment {
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
.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.sub {
  color: #909399;
  font-size: 12px;
}

:deep(.waste-row td) {
  background: #fafafa;
}
:deep(.waste-divider td) {
  border-top: 1px solid #ebeef5 !important;
}
</style>
