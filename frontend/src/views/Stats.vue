<template>
  <div class="stats">
    <el-page-header content="数据统计" icon="ArrowLeft" @back="$router.push('/')" />
    <el-row :gutter="16" class="top-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="card-title">预定数量</div>
          <div class="card-value">{{ stats.bookings }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="card-title">民宿数量</div>
          <div class="card-value">{{ stats.properties }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="card-title">房型数量</div>
          <div class="card-value">{{ stats.roomTypes }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="card-title">取消单</div>
          <div class="card-value">{{ stats.cancelled }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="chart-card">
      <template #header>
        <div class="chart-header">
          <span>状态分布</span>
          <el-tag type="info">按当前数据计算</el-tag>
        </div>
      </template>
      <div class="status-grid">
        <div v-for="item in statusStats" :key="item.name" class="status-item">
          <div class="status-name">{{ item.name }}</div>
          <el-progress :percentage="item.percent" :text-inside="true" :stroke-width="16" />
          <div class="status-count">{{ item.count }} 单</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { bookingApi, propertyApi, roomTypeApi } from '../api/index'

const stats = reactive({
  bookings: 0,
  properties: 0,
  roomTypes: 0,
  cancelled: 0,
  statusMap: {}
})

const statusStats = computed(() => {
  const total = Object.values(stats.statusMap).reduce((a, b) => a + b, 0) || 1
  return Object.entries(stats.statusMap).map(([k, v]) => ({
    name: statusName(k),
    count: v,
    percent: Math.round((v / total) * 100)
  }))
})

const statusName = (status) => {
  const map = {
    PENDING: '待确认',
    CONFIRMED: '已确认',
    CHECKED_IN: '已入住',
    CHECKED_OUT: '已退房',
    CANCELLED: '已取消',
    REFUNDED: '已退款'
  }
  return map[status] || status
}

const normalizeList = (res) => {
  const data = res?.data
  if (Array.isArray(data)) return data
  if (data?.content && Array.isArray(data.content)) return data.content
  return []
}

const loadData = async () => {
  try {
    const [bookingsRes, propRes, roomTypeRes] = await Promise.all([
      bookingApi.getList({}).catch(() => ({ data: [] })),
      propertyApi.getList({}).catch(() => ({ data: [] })),
      roomTypeApi.getList({}).catch(() => ({ data: [] }))
    ])
    const bookings = normalizeList(bookingsRes)
    const properties = normalizeList(propRes)
    const roomTypes = normalizeList(roomTypeRes)

    stats.bookings = bookings.length
    stats.properties = properties.length
    stats.roomTypes = roomTypes.length
    stats.cancelled = bookings.filter(b => b.status === 'CANCELLED').length
    stats.statusMap = bookings.reduce((acc, cur) => {
      acc[cur.status] = (acc[cur.status] || 0) + 1
      return acc
    }, {})
  } catch (err) {
    ElMessage.error('加载统计数据失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.top-cards .card-title {
  color: #909399;
}
.card-value {
  font-size: 28px;
  font-weight: 700;
  margin-top: 4px;
}
.chart-card {
  margin-top: 8px;
}
.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.status-item {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 12px;
}
.status-name {
  font-weight: 600;
  margin-bottom: 6px;
}
.status-count {
  color: #909399;
  margin-top: 4px;
}
</style>
