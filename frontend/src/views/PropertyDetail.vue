<template>
  <div class="property-detail">
    <el-page-header :content="property.propertyName || '民宿详情'" icon="ArrowLeft" @back="$router.back()" />

    <el-skeleton :loading="loading" animated>
      <template #template>
        <el-skeleton-item variant="rect" style="height: 240px; margin-bottom: 16px" />
        <el-skeleton-item variant="rect" style="height: 200px" />
      </template>
      <template #default>
        <div class="hero">
          <div class="cover" :style="coverStyle" />
          <div class="info">
            <div class="eyebrow">民宿详情</div>
            <h1>{{ property.propertyName }}</h1>
            <div class="meta">
              <span>{{ property.city }} · {{ property.address }}</span>
              <span>入住 {{ property.checkInTime || '14:00' }} | 退房 {{ property.checkOutTime || '12:00' }}</span>
            </div>
            <div class="chips">
              <el-tag type="primary">{{ typeText(property.propertyType) }}</el-tag>
              <el-tag type="success">评分 {{ property.rating ?? '暂无' }}</el-tag>
              <el-tag type="info">可售 {{ property.availableRooms ?? 0 }}</el-tag>
            </div>
            <p class="desc">{{ property.description || '暂无描述' }}</p>
            <div class="actions">
              <el-button type="primary" size="large" @click="openBooking()">快速预订</el-button>
              <el-button size="large" @click="refresh">刷新</el-button>
            </div>
          </div>
        </div>

        <el-card shadow="hover" class="card">
          <template #header>
            <div class="card-title">房型</div>
          </template>
          <el-empty v-if="!roomTypes.length" description="暂无房型数据" />
          <el-row v-else :gutter="16">
            <el-col :xs="24" :sm="12" :md="8" v-for="(rt, idx) in roomTypes" :key="rt.id || idx">
              <div class="room-card">
                <div class="room-cover">
                  <img :src="roomImage(rt, idx)" alt="房型图片" loading="lazy" @error="onRoomImgError" />
                </div>
                <div class="room-name">{{ rt.typeName }}</div>
                <div class="room-meta">
                  <span>可住 {{ rt.maxOccupancy }} 人</span>
                  <span>{{ rt.bedType || '床型未录入' }}</span>
                </div>
                <div class="room-desc">{{ rt.description || '暂无描述' }}</div>
                <div class="room-footer">
                  <div class="price">¥{{ Number(rt.basePrice || 0).toFixed(2) }}</div>
                  <el-button type="primary" size="small" @click="openBooking(rt, idx)">预订</el-button>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <el-card shadow="hover" class="card">
          <template #header>
            <div class="card-title">设施与政策</div>
          </template>
          <div class="section">
            <div class="section-title">设施</div>
            <div v-if="facilities.length" class="chips">
              <el-tag v-for="item in facilities" :key="item" style="margin-bottom: 6px">{{ item }}</el-tag>
            </div>
            <div v-else class="muted">暂无设施信息</div>
          </div>
          <div class="section">
            <div class="section-title">政策</div>
            <div v-if="policies.length">
              <ul class="policy-list">
                <li v-for="p in policies" :key="p">{{ p }}</li>
              </ul>
            </div>
            <div v-else class="muted">暂无政策信息</div>
          </div>
        </el-card>

        <el-dialog v-model="bookingDialog" title="快速预订" width="520px">
          <el-form :model="bookingForm" label-width="120px" ref="bookingFormRef" :rules="bookingRules">
            <el-form-item label="房型" prop="roomTypeId">
              <el-select v-model="bookingForm.roomTypeId" placeholder="请选择房型" style="width: 100%">
                <el-option
                  v-for="(rt, idx) in roomTypes"
                  :key="rt.id || idx"
                  :label="`${rt.typeName} · ¥${Number(rt.basePrice || 0).toFixed(2)}`"
                  :value="rt.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="入住/离店" prop="dates">
              <el-date-picker
                v-model="bookingForm.dates"
                type="daterange"
                start-placeholder="入住日期"
                end-placeholder="离店日期"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="入住人数" prop="guests">
              <el-input-number v-model="bookingForm.guests" :min="1" :max="6" />
            </el-form-item>
            <el-form-item label="价格预览">
              <div class="price-preview">
                <div>单晚价格：¥{{ currentPrice.toFixed(2) }}</div>
                <div>入住天数：{{ nights }} 晚</div>
                <div class="total">预计总价：¥{{ estimatedTotal }}</div>
              </div>
            </el-form-item>
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="bookingForm.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="bookingForm.contactPhone" placeholder="请输入手机号" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="bookingDialog = false">取消</el-button>
            <el-button type="primary" @click="submitBooking" :loading="submitting">提交预订</el-button>
          </template>
        </el-dialog>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { propertyApi, bookingApi } from '../api'

const route = useRoute()
const router = useRouter()

const property = ref({})
const roomTypes = ref([])
const loading = ref(false)
const bookingDialog = ref(false)
const submitting = ref(false)
const bookingFormRef = ref(null)
const bookingForm = ref({
  roomTypeId: null,
  dates: [],
  guests: 2,
  contactName: '',
  contactPhone: ''
})

const STATIC_IMAGES = [
  'https://pic.nximg.cn/20120414/128199_105320827000_2.jpg',
  'https://pic.nximg.cn/file/20200603/128199_102610317000_2.jpg'
]
const STATIC_FACILITIES = [
  '免费高速 Wi-Fi 与全屋覆盖',
  '24 小时前台与自助入住',
  '空调 / 地暖，全屋智能门锁',
  '早餐可选（需提前预约）',
  '免费行李寄存，叫车协助',
  '洗衣机 / 烘干机（部分房型）',
  '提供一次性洗漱用品与吹风机'
]
const STATIC_POLICIES = [
  '入住时间 14:00 之后，退房时间 12:00 之前；如需提前入住/延迟退房请与前台确认，可能产生额外费用。',
  '取消政策：入住前 24 小时内取消将收取首晚房费，24 小时之前可免费取消（以平台规则为准）。',
  '押金政策：部分房型需线下收取押金，退房验房后原路退还。',
  '吸烟政策：室内全禁烟，违者将收取 500 元深度清洁费。',
  '宠物政策：暂不接待宠物入住。',
  '儿童政策：12 岁以下儿童可免费与父母同住（不含加床和早餐）。',
  '发票政策：支持开具住宿发票，请在退房前向前台或房东提供抬头信息。'
]

const facilities = computed(() => STATIC_FACILITIES)
const policies = computed(() => STATIC_POLICIES)

const parseImages = (val) => {
  if (!val) return []
  if (Array.isArray(val)) return val.filter((x) => typeof x === 'string')
  try {
    const parsed = JSON.parse(val)
    return Array.isArray(parsed) ? parsed.filter((x) => typeof x === 'string') : []
  } catch (e) {
    // tolerate plain url string
    if (typeof val === 'string' && val.startsWith('http')) return [val]
    return []
  }
}

const coverStyle = computed(() => {
  const dbImages = parseImages(property.value.images)
  const url = dbImages[0] || STATIC_IMAGES[0]
  return { backgroundImage: `url(${url})`, backgroundSize: 'cover', backgroundPosition: 'center' }
})

const FALLBACK_IMG =
  'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHgAAAB4CAYAAABV7bNHAAAACXBIWXMAAAsSAAALEgHS3X78AAAA/0lEQVR4nO3UsQkAIAwD0fb/6Z0lKpA12Nh4Jt9BxMyVAAAAAAAAAAAAAAAAAADgG7dwPZWf9g1tb29vHOU0A9nWZX1uHkCOVHrKeq8B6rD/XZTy/BFGMd0kRQz6kmoElSgzRFInknKQxSjbRFIvlkMojFKOtEUieSQxSNtEUi+WQyiMUo60RSJ5JDFI20RSL5ZD6KZJBWiySRWi6SVShtZCahd5L6pm2Q1kl2tqm4QmoXaS+qZtkNZJdrarpCaheJK9UeaFtJXaarpCahe5JaZ92NNVNdaKpCahfpJbJ83kDu6gZ0hW1d8kAAAAP//oAk/lKk3z8gAAAAASUVORK5CYII='

const roomImage = (rt, idx) => {
  const imgs = parseImages(rt?.images)
  if (imgs.length) return imgs[0]
  const pick = (rt?.id != null ? Number(rt.id) : idx || 0) % STATIC_IMAGES.length
  return STATIC_IMAGES[pick]
}

const onRoomImgError = (evt) => {
  const target = evt?.target
  if (target && target.src !== FALLBACK_IMG) {
    target.src = FALLBACK_IMG
  }
}

const typeText = (val) => {
  const map = {
    HOMESTAY: '民宿',
    HOTEL: '酒店',
    APARTMENT: '公寓',
    VILLA: '别墅',
    OTHER: '其他'
  }
  return map[val] || val || '-'
}

const loadDetail = async () => {
  loading.value = true
  try {
    const { data } = await propertyApi.getDetail(route.params.id)
    property.value = data.property || {}
    roomTypes.value = data.roomTypes || []
  } catch (err) {
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

const refresh = () => loadDetail()

const goBook = (roomType) => {
  router.push({
    name: 'SimpleBooking',
    query: {
      propertyId: property.value.id,
      roomTypeId: roomType?.id
    }
  })
}

const openBooking = (rt, idx) => {
  bookingDialog.value = true
  bookingForm.value = {
    roomTypeId: rt?.id || roomTypes.value[0]?.id || null,
    dates: [],
    guests: 2,
    contactName: '',
    contactPhone: ''
  }
}

const bookingRules = {
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  dates: [{ type: 'array', required: true, message: '请选择入住和离店日期', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const nights = computed(() => {
  if (!bookingForm.value.dates?.length) return 1
  const [start, end] = bookingForm.value.dates
  if (!start || !end) return 1
  const s = new Date(start)
  const e = new Date(end)
  const diff = Math.round((e - s) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 1
})

const currentPrice = computed(() => {
  const rt = roomTypes.value.find((r) => r.id === bookingForm.value.roomTypeId)
  return Number(rt?.basePrice || 0)
})

const estimatedTotal = computed(() => {
  return (currentPrice.value * nights.value || 0).toFixed(2)
})

const submitBooking = async () => {
  try {
    await bookingFormRef.value.validate()
    submitting.value = true
    const [checkIn, checkOut] = bookingForm.value.dates || []
    const payload = {
      user: { id: JSON.parse(localStorage.getItem('user') || '{}').id },
      property: { id: property.value.id },
      roomType: { id: bookingForm.value.roomTypeId },
      checkInDate: toDate(checkIn),
      checkOutDate: toDate(checkOut),
      numberOfGuests: bookingForm.value.guests,
      contactName: bookingForm.value.contactName,
      contactPhone: bookingForm.value.contactPhone,
      status: 'PENDING',
      paymentStatus: 'UNPAID'
    }
    const { data } = await bookingApi.create(payload)
    ElMessage.success('预订已创建，请尽快完成支付')
    bookingDialog.value = false
    router.push({
      name: 'Booking',
      query: { tab: 'cart', bookingId: data?.id }
    })
  } catch (err) {
    if (err !== false) {
      ElMessage.error(err?.response?.data?.error || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

const toDate = (val) => {
  if (!val) return null
  const d = new Date(val)
  return d.toISOString().split('T')[0]
}

onMounted(loadDetail)
</script>

<style scoped>
.property-detail {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.hero {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
  align-items: stretch;
}
.cover {
  min-height: 260px;
  border-radius: 16px;
  background: linear-gradient(135deg, #74ebd5 0%, #acb6e5 100%);
}
.info {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
}
.eyebrow {
  color: #409eff;
  font-weight: 600;
  letter-spacing: 0.04em;
}
.info h1 {
  margin: 6px 0 8px;
}
.meta {
  color: #606266;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}
.chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin: 8px 0;
}
.desc {
  color: #606266;
  line-height: 1.6;
}
.actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
}
.card {
  border: none;
}
.card-title {
  font-weight: 600;
}
.room-card {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  height: 100%;
}
.room-cover {
  height: 120px;
  border-radius: 10px;
  overflow: hidden;
  background: #f2f6fc;
  display: flex;
  align-items: center;
  justify-content: center;
}
.room-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.room-name {
  font-weight: 600;
}
.room-meta {
  color: #909399;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.room-desc {
  color: #606266;
  font-size: 13px;
  min-height: 40px;
}
.room-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}
.price {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}
.section {
  margin-bottom: 12px;
}
.section-title {
  font-weight: 600;
  margin-bottom: 6px;
}
.policy-list {
  padding-left: 16px;
  color: #606266;
}
.muted {
  color: #909399;
}
@media (max-width: 992px) {
  .hero {
    grid-template-columns: 1fr;
  }
}
</style>
