<template>
  <div class="pending-payment">
    <el-page-header content="待付款订单" icon="ArrowLeft" @back="goBack" />

    <div class="summary-grid">
      <el-card shadow="hover" class="summary-card">
        <div class="summary-label">待支付订单</div>
        <div class="summary-value">{{ pendingBookings.length }}</div>
        <div class="summary-sub">仅展示当前账号仍未完成支付的订单</div>
      </el-card>
      <el-card shadow="hover" class="summary-card">
        <div class="summary-label">待支付总额</div>
        <div class="summary-value">¥{{ totalOutstanding }}</div>
        <div class="summary-sub">按订单剩余待支付金额汇总</div>
      </el-card>
      <el-card shadow="hover" class="summary-card">
        <div class="summary-label">最早截止</div>
        <div class="summary-value">{{ nearestDeadlineText }}</div>
        <div class="summary-sub">超时未支付会由系统自动取消</div>
      </el-card>
    </div>

    <el-card shadow="hover" class="list-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">待付款列表</div>
          <el-button @click="loadPendingBookings" :loading="loading">刷新</el-button>
        </div>
      </template>

      <el-empty
        v-if="!loading && !pendingBookings.length"
        description="当前没有待付款订单"
      >
        <el-button type="primary" @click="router.push('/simple-booking')">去下单</el-button>
      </el-empty>

      <el-skeleton v-else-if="loading" :rows="4" animated />

      <div v-else class="pending-list">
        <el-card
          v-for="booking in pendingBookings"
          :key="booking.id"
          shadow="never"
          class="pending-card"
          :class="{ active: booking.id === focusBookingId }"
        >
          <div class="pending-head">
            <div>
              <div class="booking-no">{{ booking.bookingNumber }}</div>
              <div class="booking-meta">
                {{ booking.property?.propertyName || '未命名房源' }}
                <span v-if="booking.roomType?.typeName">· {{ booking.roomType.typeName }}</span>
              </div>
            </div>
            <div class="tag-wrap">
              <el-tag type="warning">待支付</el-tag>
              <el-tag :type="deadlineTagType(booking)">{{ deadlineText(booking) }}</el-tag>
            </div>
          </div>

          <el-descriptions :column="2" border class="desc">
            <el-descriptions-item label="入住日期">{{ booking.checkInDate }}</el-descriptions-item>
            <el-descriptions-item label="离店日期">{{ booking.checkOutDate }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ booking.contactName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ booking.contactPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="订单总额">¥{{ money(booking.totalAmount) }}</el-descriptions-item>
            <el-descriptions-item label="待支付">¥{{ money(outstandingAmount(booking)) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ dateTimeText(booking.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="支付状态">{{ paymentStatusText(booking.paymentStatus) }}</el-descriptions-item>
          </el-descriptions>

          <div class="card-actions">
            <el-button type="primary" @click="openPayDialog(booking)">立即支付</el-button>
            <el-button plain @click="goToProperty(booking.property?.id)">查看房源</el-button>
            <el-button plain @click="router.push('/booking')">订单列表</el-button>
            <el-button type="danger" plain @click="cancelBooking(booking)">取消订单</el-button>
          </div>
        </el-card>
      </div>
    </el-card>

    <el-dialog v-model="payDialogVisible" title="确认支付" width="460px">
      <el-form label-width="110px">
        <el-form-item label="订单号">
          <span>{{ payTarget?.bookingNumber }}</span>
        </el-form-item>
        <el-form-item label="房源">
          <span>{{ payTarget?.property?.propertyName || '-' }}</span>
        </el-form-item>
        <el-form-item label="待支付金额">
          <span class="pay-amount">¥{{ money(outstandingAmount(payTarget)) }}</span>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="payMethod">
            <el-radio label="ALIPAY">支付宝</el-radio>
            <el-radio label="WECHAT">微信</el-radio>
            <el-radio label="BANK_CARD">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-alert
          type="info"
          show-icon
          :closable="false"
          title="演示环境：点击确认支付后会直接调用后端支付处理接口，并同步把订单更新为已支付。"
        />
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="paySubmitting" @click="submitPayment">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookingApi, paymentApi } from '../api'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const pendingBookings = ref([])
const currentUser = ref(null)
const payDialogVisible = ref(false)
const paySubmitting = ref(false)
const payTarget = ref(null)
const payMethod = ref('ALIPAY')
const now = ref(Date.now())
let timer = null

const focusBookingId = computed(() => {
  const raw = route.query.bookingId
  return raw ? Number(raw) : null
})

const totalOutstanding = computed(() => {
  const total = pendingBookings.value.reduce((sum, item) => sum + outstandingAmount(item), 0)
  return money(total)
})

const nearestDeadlineText = computed(() => {
  if (!pendingBookings.value.length) {
    return '暂无'
  }
  const minRemain = Math.min(...pendingBookings.value.map((item) => remainingMs(item)))
  return formatRemain(minRemain)
})

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/booking')
}

const outstandingAmount = (booking) => {
  if (!booking) return 0
  const total = Number(booking.totalAmount || 0)
  const paid = Number(booking.paidAmount || 0)
  const amount = total - paid
  return amount > 0 ? amount : 0
}

const money = (value) => Number(value || 0).toFixed(2)

const paymentStatusText = (status) => {
  const map = {
    UNPAID: '未支付',
    PARTIAL: '部分支付',
    PAID: '已支付',
    REFUNDED: '已退款'
  }
  return map[status] || status || '-'
}

const dateTimeText = (value) => {
  if (!value) return '-'
  const time = new Date(value)
  if (Number.isNaN(time.getTime())) return value
  return time.toLocaleString()
}

const deadlineAt = (booking) => {
  if (!booking?.createdAt) return null
  const createdAt = new Date(booking.createdAt).getTime()
  if (Number.isNaN(createdAt)) return null
  return createdAt + 15 * 60 * 1000
}

const remainingMs = (booking) => {
  const deadline = deadlineAt(booking)
  if (!deadline) return 0
  return deadline - now.value
}

const formatRemain = (remain) => {
  if (remain == null) return '未知'
  if (remain <= 0) return '已超时'
  const totalSeconds = Math.floor(remain / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  const hours = Math.floor(minutes / 60)
  if (hours > 0) {
    return `${hours}小时${String(minutes % 60).padStart(2, '0')}分`
  }
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

const deadlineText = (booking) => {
  const remain = remainingMs(booking)
  if (remain <= 0) {
    return '等待自动取消'
  }
  return `剩余 ${formatRemain(remain)}`
}

const deadlineTagType = (booking) => {
  const remain = remainingMs(booking)
  if (remain <= 0) return 'danger'
  if (remain <= 5 * 60 * 1000) return 'warning'
  return 'success'
}

const loadPendingBookings = async () => {
  if (!currentUser.value?.id) {
    router.push('/login')
    return
  }
  loading.value = true
  try {
    const { data } = await bookingApi.getList({ userId: currentUser.value.id })
    const list = Array.isArray(data) ? data : []
    pendingBookings.value = list
      .filter((item) => item.paymentStatus === 'UNPAID')
      .filter((item) => !['CANCELLED', 'REFUNDED', 'CHECKED_OUT'].includes(item.status))
      .sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
  } catch (error) {
    ElMessage.error(error?.response?.data?.error || '加载待付款订单失败')
  } finally {
    loading.value = false
  }
}

const openPayDialog = (booking) => {
  payTarget.value = booking
  payMethod.value = 'ALIPAY'
  payDialogVisible.value = true
}

const ensurePendingPayment = async (booking) => {
  const { data } = await paymentApi.getByBookingId(booking.id)
  const payments = Array.isArray(data) ? data : []
  const successPayment = payments.find((item) => item.status === 'SUCCESS')
  if (successPayment) {
    return { alreadyPaid: true, payment: successPayment }
  }

  const pendingPayments = payments
    .filter((item) => item.status === 'PENDING')
    .sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())

  const latestPending = pendingPayments[0]
  if (latestPending) {
    const { data: updated } = await paymentApi.update(latestPending.id, {
      ...latestPending,
      booking: { id: booking.id },
      amount: outstandingAmount(booking),
      paymentMethod: payMethod.value,
      paymentType: 'FULL',
      remark: '待付款订单支付中'
    })
    return { alreadyPaid: false, payment: updated }
  }

  const { data: created } = await paymentApi.create({
    booking: { id: booking.id },
    amount: outstandingAmount(booking),
    paymentMethod: payMethod.value,
    paymentType: 'FULL',
    status: 'PENDING',
    remark: '待付款订单待支付'
  })
  return { alreadyPaid: false, payment: created }
}

const submitPayment = async () => {
  if (!payTarget.value) return
  paySubmitting.value = true
  try {
    const { alreadyPaid, payment } = await ensurePendingPayment(payTarget.value)
    if (!alreadyPaid) {
      const transactionId = `SIM-${Date.now()}-${Math.random().toString(36).slice(2, 8).toUpperCase()}`
      await paymentApi.process(payment.id, transactionId)
    }
    ElMessage.success('支付成功')
    payDialogVisible.value = false
    await loadPendingBookings()
  } catch (error) {
    ElMessage.error(error?.response?.data?.error || '支付失败')
  } finally {
    paySubmitting.value = false
  }
}

const cancelBooking = async (booking) => {
  try {
    await ElMessageBox.confirm('确认取消这笔待付款订单吗？', '取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '我再想想',
      type: 'warning'
    })
    await bookingApi.cancel(booking.id, '用户在待付款页面主动取消')
    ElMessage.success('订单已取消')
    await loadPendingBookings()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.response?.data?.error || '取消订单失败')
    }
  }
}

const goToProperty = (propertyId) => {
  if (!propertyId) {
    ElMessage.info('暂无房源信息')
    return
  }
  router.push({ name: 'PropertyDetail', params: { id: propertyId } })
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      currentUser.value = JSON.parse(userStr)
    } catch (error) {
      currentUser.value = null
    }
  }
  timer = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)
  loadPendingBookings()
})

onBeforeUnmount(() => {
  if (timer) {
    window.clearInterval(timer)
  }
})
</script>

<style scoped>
.pending-payment {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.summary-card {
  border: none;
}

.summary-label {
  color: #909399;
  font-size: 13px;
}

.summary-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}

.summary-sub {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.list-card {
  border: none;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
}

.pending-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.pending-card {
  border: 1px solid #e5e7eb;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.pending-card.active {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.12);
}

.pending-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.booking-no {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.booking-meta {
  margin-top: 6px;
  color: #606266;
}

.tag-wrap {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.desc {
  margin-bottom: 14px;
}

.card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pay-amount {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

@media (max-width: 960px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .pending-head {
    flex-direction: column;
  }

  .tag-wrap {
    justify-content: flex-start;
  }
}
</style>
