<template>
  <div class="user-booking">
    <el-page-header content="购物车 / 订单" icon="ArrowLeft" @back="goBack" />

    <div class="summary-grid">
      <el-card shadow="hover" class="summary-card">
        <div class="summary-label">购物车待支付</div>
        <div class="summary-value">{{ cartBookings.length }}</div>
        <div class="summary-sub">这里只保留未支付订单，支付成功后会自动移出购物车。</div>
      </el-card>
      <el-card shadow="hover" class="summary-card">
        <div class="summary-label">待支付总额</div>
        <div class="summary-value">¥{{ totalOutstanding }}</div>
        <div class="summary-sub">按当前购物车中未支付订单的剩余应付金额汇总。</div>
      </el-card>
      <el-card shadow="hover" class="summary-card">
        <div class="summary-label">最近支付时限</div>
        <div class="summary-value">{{ nearestDeadlineText }}</div>
        <div class="summary-sub">超时未支付的订单会被系统自动取消。</div>
      </el-card>
    </div>

    <el-card shadow="hover" class="list-card">
      <template #header>
        <div class="card-header">
          <div>
            <div class="header-title">我的购物车 / 订单</div>
            <div class="header-sub">未支付订单放在购物车，已支付订单单独展示，已取消订单不再出现在这里。</div>
          </div>
          <el-button @click="loadBookings" :loading="loading">刷新</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane :label="`购物车 (${cartBookings.length})`" name="cart">
          <el-skeleton v-if="loading" :rows="4" animated />

          <el-empty
            v-else-if="!cartBookings.length"
            description="购物车里还没有待支付订单"
          >
            <el-button type="primary" @click="router.push('/simple-booking')">去预订</el-button>
          </el-empty>

          <div v-else class="booking-list">
            <el-card
              v-for="booking in cartBookings"
              :key="booking.id"
              shadow="never"
              class="booking-card"
              :class="{ active: booking.id === focusBookingId }"
            >
              <div class="booking-head">
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

              <el-descriptions :column="2" border class="booking-desc">
                <el-descriptions-item label="入住日期">{{ booking.checkInDate }}</el-descriptions-item>
                <el-descriptions-item label="离店日期">{{ booking.checkOutDate }}</el-descriptions-item>
                <el-descriptions-item label="联系人">{{ booking.contactName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ booking.contactPhone || '-' }}</el-descriptions-item>
                <el-descriptions-item label="订单总额">¥{{ money(booking.totalAmount) }}</el-descriptions-item>
                <el-descriptions-item label="待支付">¥{{ money(outstandingAmount(booking)) }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ dateTimeText(booking.createdAt) }}</el-descriptions-item>
                <el-descriptions-item label="订单状态">{{ bookingStatusText(booking.status) }}</el-descriptions-item>
              </el-descriptions>

              <div class="card-actions">
                <el-button type="primary" @click="openPayDialog(booking)">立即支付</el-button>
                <el-button plain @click="viewBooking(booking)">查看详情</el-button>
                <el-button plain @click="goToProperty(booking.property?.id)">查看房源</el-button>
                <el-button type="danger" plain @click="cancelBooking(booking)">取消购物车</el-button>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="`已支付订单 (${paidBookings.length})`" name="paid">
          <el-skeleton v-if="loading" :rows="4" animated />

          <el-empty
            v-else-if="!paidBookings.length"
            description="暂无已支付订单"
          />

          <div v-else class="booking-list">
            <el-card
              v-for="booking in paidBookings"
              :key="booking.id"
              shadow="never"
              class="booking-card"
            >
              <div class="booking-head">
                <div>
                  <div class="booking-no">{{ booking.bookingNumber }}</div>
                  <div class="booking-meta">
                    {{ booking.property?.propertyName || '未命名房源' }}
                    <span v-if="booking.roomType?.typeName">· {{ booking.roomType.typeName }}</span>
                  </div>
                </div>
                <div class="tag-wrap">
                  <el-tag type="success">已支付</el-tag>
                  <el-tag :type="bookingStatusType(booking.status)">{{ bookingStatusText(booking.status) }}</el-tag>
                </div>
              </div>

              <el-descriptions :column="2" border class="booking-desc">
                <el-descriptions-item label="入住日期">{{ booking.checkInDate }}</el-descriptions-item>
                <el-descriptions-item label="离店日期">{{ booking.checkOutDate }}</el-descriptions-item>
                <el-descriptions-item label="入住人数">{{ booking.numberOfGuests || '-' }}</el-descriptions-item>
                <el-descriptions-item label="支付状态">{{ paymentStatusText(booking.paymentStatus) }}</el-descriptions-item>
                <el-descriptions-item label="订单总额">¥{{ money(booking.totalAmount) }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ dateTimeText(booking.createdAt) }}</el-descriptions-item>
              </el-descriptions>

              <div class="card-actions">
                <el-button plain @click="viewBooking(booking)">查看详情</el-button>
                <el-button plain @click="goToProperty(booking.property?.id)">查看房源</el-button>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="订单详情" width="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单号">{{ detailData.bookingNumber }}</el-descriptions-item>
        <el-descriptions-item label="房源">{{ detailData.property?.propertyName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ detailData.roomType?.typeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入住 / 离店">
          {{ detailData.checkInDate }} ~ {{ detailData.checkOutDate }}
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detailData.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入住人数">{{ detailData.numberOfGuests || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          ¥{{ detailData.totalAmount ? Number(detailData.totalAmount).toFixed(2) : '0.00' }}
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">{{ bookingStatusText(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">{{ paymentStatusText(detailData.paymentStatus) }}</el-descriptions-item>
        <el-descriptions-item label="特殊要求">{{ detailData.specialRequests || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

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
          title="确认支付后会直接调用后端支付处理接口；如果你放弃支付，可以直接把该订单从购物车移除。"
        />
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">继续考虑</el-button>
        <el-button type="danger" plain :loading="cancelSubmitting" @click="cancelPayAndCart">
          取消支付并移出购物车
        </el-button>
        <el-button type="primary" :loading="paySubmitting" @click="submitPayment">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookingApi, paymentApi } from '../api'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const allBookings = ref([])
const currentUser = ref(null)
const activeTab = ref('cart')
const payDialogVisible = ref(false)
const paySubmitting = ref(false)
const cancelSubmitting = ref(false)
const payTarget = ref(null)
const payMethod = ref('ALIPAY')
const detailDialogVisible = ref(false)
const detailData = ref({})
const now = ref(Date.now())
let timer = null

const focusBookingId = computed(() => {
  const raw = route.query.bookingId
  return raw ? Number(raw) : null
})

const cartBookings = computed(() =>
  allBookings.value
    .filter((item) => item.paymentStatus === 'UNPAID')
    .filter((item) => !['CANCELLED', 'REFUNDED', 'CHECKED_OUT'].includes(item.status))
)

const paidBookings = computed(() =>
  allBookings.value
    .filter((item) => item.paymentStatus === 'PAID')
    .filter((item) => !['CANCELLED', 'REFUNDED'].includes(item.status))
)

const totalOutstanding = computed(() => {
  const total = cartBookings.value.reduce((sum, item) => sum + outstandingAmount(item), 0)
  return money(total)
})

const nearestDeadlineText = computed(() => {
  if (!cartBookings.value.length) {
    return '暂无'
  }
  const minRemain = Math.min(...cartBookings.value.map((item) => remainingMs(item)))
  return formatRemain(minRemain)
})

const syncUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    currentUser.value = null
    return
  }
  try {
    currentUser.value = JSON.parse(userStr)
  } catch (error) {
    currentUser.value = null
  }
}

const sortBookings = (list) =>
  [...list].sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/')
}

const outstandingAmount = (booking) => {
  if (!booking) return 0
  const total = Number(booking.totalAmount || 0)
  const paid = Number(booking.paidAmount || 0)
  const amount = total - paid
  return amount > 0 ? amount : 0
}

const money = (value) => Number(value || 0).toFixed(2)

const bookingStatusText = (status) => {
  const map = {
    PENDING: '待确认',
    CONFIRMED: '已确认',
    CHECKED_IN: '已入住',
    CHECKED_OUT: '已离店',
    CANCELLED: '已取消',
    REFUNDED: '已退款'
  }
  return map[status] || status || '-'
}

const bookingStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    CONFIRMED: 'success',
    CHECKED_IN: '',
    CHECKED_OUT: 'info',
    CANCELLED: 'danger',
    REFUNDED: 'info'
  }
  return map[status] || 'info'
}

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
    return `${hours}小时${String(minutes % 60).padStart(2, '0')}分钟`
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

const syncActiveTab = () => {
  activeTab.value = route.query.tab === 'paid' ? 'paid' : 'cart'
}

const loadBookings = async () => {
  syncUser()
  if (!currentUser.value?.id || currentUser.value?.role !== 'USER') {
    router.push('/login')
    return
  }
  loading.value = true
  try {
    const { data } = await bookingApi.getList({ userId: currentUser.value.id })
    const list = Array.isArray(data) ? data : []
    allBookings.value = sortBookings(list)
  } catch (error) {
    ElMessage.error(error?.response?.data?.error || '加载订单失败')
  } finally {
    loading.value = false
  }
}

const viewBooking = (booking) => {
  detailData.value = { ...booking }
  detailDialogVisible.value = true
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
      remark: '购物车订单支付中'
    })
    return { alreadyPaid: false, payment: updated }
  }

  const { data: created } = await paymentApi.create({
    booking: { id: booking.id },
    amount: outstandingAmount(booking),
    paymentMethod: payMethod.value,
    paymentType: 'FULL',
    status: 'PENDING',
    remark: '购物车订单待支付'
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
    ElMessage.success('支付成功，订单已从购物车移出')
    payDialogVisible.value = false
    activeTab.value = 'paid'
    await loadBookings()
  } catch (error) {
    ElMessage.error(error?.response?.data?.error || '支付失败')
  } finally {
    paySubmitting.value = false
  }
}

const cancelBooking = async (booking, options = {}) => {
  try {
    await ElMessageBox.confirm(
      '取消后，该订单会从购物车移除；如需重新预订，需要重新下单。',
      '取消购物车',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '我再想想',
        type: 'warning'
      }
    )
    await bookingApi.cancel(booking.id, '用户主动取消购物车订单')
    if (options.closeDialog) {
      payDialogVisible.value = false
    }
    ElMessage.success('该订单已从购物车移除')
    await loadBookings()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.response?.data?.error || '取消购物车失败')
    }
  }
}

const cancelPayAndCart = async () => {
  if (!payTarget.value) return
  cancelSubmitting.value = true
  try {
    await cancelBooking(payTarget.value, { closeDialog: true })
  } finally {
    cancelSubmitting.value = false
  }
}

const goToProperty = (propertyId) => {
  if (!propertyId) {
    ElMessage.info('暂无房源信息')
    return
  }
  router.push({ name: 'PropertyDetail', params: { id: propertyId } })
}

watch(
  () => route.query.tab,
  () => syncActiveTab(),
  { immediate: true }
)

onMounted(() => {
  syncUser()
  timer = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)
  loadBookings()
})

onBeforeUnmount(() => {
  if (timer) {
    window.clearInterval(timer)
  }
})
</script>

<style scoped>
.user-booking {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.summary-card,
.list-card {
  border: none;
}

.summary-label,
.summary-sub,
.header-sub,
.booking-meta {
  color: #606266;
}

.summary-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}

.summary-sub,
.header-sub {
  margin-top: 8px;
  font-size: 13px;
}

.card-header,
.booking-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
}

.booking-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.booking-card {
  border: 1px solid #e5e7eb;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.booking-card.active {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.12);
}

.booking-no {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.booking-meta {
  margin-top: 6px;
}

.booking-desc {
  margin: 14px 0;
}

.tag-wrap,
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

  .card-header,
  .booking-head {
    flex-direction: column;
  }
}
</style>
