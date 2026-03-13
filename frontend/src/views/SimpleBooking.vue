<template>
  <div class="simple-booking">
    <section class="hero">
      <div class="hero-text">
        <p class="eyebrow">面向普通用户 · 三步完成预订</p>
        <h1>精选民宿/酒店，一键下单</h1>
        <p class="sub">挑一个喜欢的房源，填写入住信息即可提交，无需繁琐字段。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="scrollToForm">
            立即预订
          </el-button>
          <el-button size="large" @click="loadProperties">
            刷新房源
          </el-button>
        </div>
        <div class="highlights">
          <div class="pill">快速下单</div>
          <div class="pill">明文密码登录（演示环境）</div>
          <div class="pill">少字段 · 更稳定</div>
        </div>
      </div>
      <div class="hero-card">
        <el-card shadow="hover">
          <div class="mini-title">今日推荐</div>
          <div v-if="recoList.length" class="hero-carousel">
            <el-carousel height="220px" indicator-position="outside" :interval="4200">
              <el-carousel-item v-for="item in recoList" :key="item.id">
                <div class="reco-slide" :style="thumbStyle(item)" @click="viewDetail(item)">
                  <div class="reco-overlay">
                    <div class="reco-badge">热门 · 订单 {{ item.bookingCount || 0 }}</div>
                    <div class="reco-name">{{ item.propertyName }}</div>
                    <div class="reco-meta">{{ item.city }} · {{ item.address }}</div>
                    <el-button type="primary" size="small" class="reco-cta">查看详情</el-button>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>
          <div v-else class="hero-list">
            <div
              v-for="item in properties.slice(0, 2)"
              :key="item.id"
              class="hero-item clickable"
              @click="viewDetail(item)"
            >
              <div class="thumb" :style="thumbStyle(item)" />
              <div>
                <div class="name">{{ item.propertyName }}</div>
                <div class="meta">{{ item.city }} · {{ item.address }}</div>
              </div>
            </div>
            <div v-if="!properties.length" class="empty-text">暂无房源，请刷新或先录入数据</div>
          </div>
        </el-card>
      </div>
    </section>

    <section class="grid">
      <div class="left">
        <div class="section-title">
          <h2>可预订房源</h2>
          <el-input
            v-model="keyword"
            placeholder="按城市/名称过滤"
            clearable
            prefix-icon="Search"
            @input="filterProperties"
            size="large"
          />
        </div>
        <el-skeleton :loading="loading" animated>
          <template #template>
            <el-skeleton-item variant="rect" style="height: 120px" />
          </template>
          <template #default>
            <el-empty v-if="!filteredProperties.length" description="暂无房源" />
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12" :md="8" v-for="item in filteredProperties" :key="item.id">
                <el-card class="property-card" shadow="hover">
                  <div class="cover" :style="coverStyle(item)" />
                  <div class="title">{{ item.propertyName }}</div>
                  <div class="meta">{{ item.city }} · {{ item.address }}</div>
                  <div class="actions">
                    <el-button type="primary" plain size="small" @click="pickProperty(item)">选择</el-button>
                    <el-button type="primary" size="small" @click="viewDetail(item)">查看</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </template>
        </el-skeleton>
      </div>

      <div class="right" ref="formCard">
        <el-card shadow="hover">
          <div class="section-title">
            <h2>快速预订</h2>
            <small>只填核心字段，更易提交</small>
          </div>
          <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
            <el-form-item label="选择房源" prop="propertyId">
              <el-select v-model="form.propertyId" placeholder="请选择民宿/酒店" filterable @change="loadRoomTypes">
                <el-option v-for="item in properties" :key="item.id" :label="item.propertyName" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="房型" prop="roomTypeId">
              <el-select v-model="form.roomTypeId" placeholder="请选择房型" :disabled="!roomTypes.length">
                <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.typeName" :value="rt.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="入住/离店日期" required>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                start-placeholder="入住"
                end-placeholder="离店"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="form.contactName" placeholder="姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="用于联系和确认" />
            </el-form-item>
            <el-form-item label="入住人数" prop="numberOfGuests">
              <el-input-number v-model="form.numberOfGuests" :min="1" :max="6" />
            </el-form-item>
            <el-form-item label="备注需求" prop="specialRequests">
              <el-input v-model="form.specialRequests" type="textarea" :rows="3" placeholder="可留空" />
            </el-form-item>
            <el-button type="primary" :loading="submitting" size="large" style="width: 100%" @click="submit">
              提交预订
            </el-button>
            <el-alert
              v-if="!isLoggedIn"
              type="info"
              title="需要先登录，密码为明文（演示环境）"
              show-icon
              :closable="false"
              style="margin-top: 12px"
            />
          </el-form>
        </el-card>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { propertyApi, roomTypeApi, bookingApi } from '../api'

const properties = ref([])
const filteredProperties = ref([])
const recoList = ref([])
const roomTypes = ref([])
const keyword = ref('')
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const formCard = ref(null)
const dateRange = ref([])
const router = useRouter()
const route = useRoute()

const form = reactive({
  propertyId: null,
  roomTypeId: null,
  contactName: '',
  contactPhone: '',
  numberOfGuests: 2,
  specialRequests: ''
})

const rules = {
  propertyId: [{ required: true, message: '请选择房源', trigger: 'change' }],
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const isLoggedIn = computed(() => !!localStorage.getItem('user'))

const loadProperties = async () => {
  loading.value = true
  try {
    // 普通用户仅查询已审核通过的房源
    const { data } = await propertyApi.getList({ status: 'APPROVED' })
    properties.value = data || []
    filteredProperties.value = properties.value
  } catch (err) {
    ElMessage.error('加载房源失败')
  } finally {
    loading.value = false
  }
}

const loadRecommendations = async () => {
  try {
    const { data } = await propertyApi.getTop(5)
    const list = Array.isArray(data) ? data : []
    recoList.value = list
      .map((it) => {
        const p = it?.property || it
        if (!p) return null
        return { ...p, bookingCount: it?.bookingCount ?? p?.bookingCount ?? 0 }
      })
      .filter(Boolean)
  } catch (e) {
    recoList.value = []
  }
}

const filterProperties = () => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) {
    filteredProperties.value = properties.value
    return
  }
  filteredProperties.value = properties.value.filter(item => {
    const text = `${item.propertyName || ''}${item.city || ''}${item.address || ''}`.toLowerCase()
    return text.includes(kw)
  })
}

const pickProperty = (item) => {
  form.propertyId = item.id
  loadRoomTypes(item.id)
  scrollToForm()
}

const viewDetail = (item) => {
  router.push({ name: 'PropertyDetail', params: { id: item.id } })
}

const parseImages = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    if (typeof images === 'string' && images.includes('http')) {
      // tolerate malformed like 'http...webp"]'
      const maybe = images.replace(/^[^h]*?(https?:\/\/[^\\s\\]]+)/, '$1').split(/\\s*,\\s*/)
      return maybe.filter(u => u.startsWith('http'))
    }
    return []
  }
}

const coverStyle = (item) => {
  const imgs = parseImages(item.images)
  const url = imgs[0]
  return url
    ? { backgroundImage: `url(${url})`, backgroundSize: 'cover', backgroundPosition: 'center' }
    : { background: 'linear-gradient(135deg, #74ebd5 0%, #9face6 100%)' }
}

const thumbStyle = (item) => {
  const imgs = parseImages(item.images)
  const url = imgs[0]
  return url
    ? { backgroundImage: `url(${url})`, backgroundSize: 'cover', backgroundPosition: 'center' }
    : { background: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)' }
}

const loadRoomTypes = async (propertyId) => {
  roomTypes.value = []
  form.roomTypeId = null
  const pid = propertyId || form.propertyId
  if (!pid) return []
  try {
    const { data } = await roomTypeApi.getActiveByPropertyId(pid)
    roomTypes.value = data || []
  } catch (err) {
    ElMessage.error('加载房型失败')
  }
  return roomTypes.value
}

const scrollToForm = () => {
  if (formCard.value) {
    formCard.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const submit = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录，演示环境密码为明文')
    return
  }
  await formRef.value.validate()
  if (!dateRange.value?.length) {
    ElMessage.warning('请选择入住和离店日期')
    return
  }
  submitting.value = true
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    const payload = {
      user: { id: user.id },
      property: { id: form.propertyId },
      roomType: { id: form.roomTypeId },
      checkInDate: toDate(dateRange.value[0]),
      checkOutDate: toDate(dateRange.value[1]),
      numberOfGuests: form.numberOfGuests,
      contactName: form.contactName,
      contactPhone: form.contactPhone,
      specialRequests: form.specialRequests,
      status: 'PENDING',
      paymentStatus: 'UNPAID'
    }
    await bookingApi.create(payload)
    ElMessage.success('预订提交成功')
    formRef.value.resetFields()
    dateRange.value = []
  } catch (err) {
    ElMessage.error(err?.response?.data?.error || '提交失败')
  } finally {
    submitting.value = false
  }
}

const toDate = (val) => {
  if (!val) return null
  const d = new Date(val)
  return d.toISOString().split('T')[0]
}

onMounted(() => {
  loadRecommendations()
  loadProperties()
  // 若从详情页带参数进入，预填
  const pid = route.query.propertyId ? Number(route.query.propertyId) : null
  const rid = route.query.roomTypeId ? Number(route.query.roomTypeId) : null
  if (pid) {
    form.propertyId = pid
    loadRoomTypes(pid).then(() => {
      if (rid) form.roomTypeId = rid
    })
    scrollToForm()
  }
})
</script>

<style scoped>
.simple-booking {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  align-items: stretch;
}

.hero-text {
  background: linear-gradient(135deg, #f0f4ff, #e8f7ff);
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 10px 30px rgba(64, 158, 255, 0.12);
}

.eyebrow {
  color: #409eff;
  font-weight: 600;
  letter-spacing: 0.04em;
  margin-bottom: 6px;
}

.hero-text h1 {
  margin: 0 0 10px;
  font-size: 32px;
  color: #1f2d3d;
}

.sub {
  color: #606266;
  margin-bottom: 20px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.highlights {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pill {
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 999px;
  font-size: 12px;
  color: #409eff;
}

.hero-card .mini-title {
  color: #909399;
  margin-bottom: 8px;
  font-size: 13px;
}

.hero-item {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.hero-item.clickable {
  cursor: pointer;
  border-radius: 12px;
  padding: 8px 10px;
  transition: background 0.2s ease, transform 0.2s ease;
}

.hero-item.clickable:hover {
  background: #f5f8ff;
  transform: translateY(-1px);
}

.hero-item .thumb {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  border-radius: 8px;
}

.hero-item .name {
  font-weight: 600;
}

.hero-item .meta {
  color: #909399;
  font-size: 12px;
}

.hero-carousel {
  margin-top: 6px;
}

.reco-slide {
  height: 220px;
  border-radius: 14px;
  overflow: hidden;
  position: relative;
  background-size: cover;
  background-position: center;
  cursor: pointer;
}

.reco-slide::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(2, 6, 23, 0.06) 0%, rgba(2, 6, 23, 0.62) 100%);
}

.reco-overlay {
  position: absolute;
  inset: 0;
  padding: 14px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 6px;
  color: #fff;
}

.reco-badge {
  align-self: flex-start;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.18);
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 12px;
}

.reco-name {
  font-size: 16px;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.reco-meta {
  opacity: 0.92;
  font-size: 12px;
}

.reco-cta {
  align-self: flex-start;
  margin-top: 6px;
}

.grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.section-title h2 {
  margin: 0;
}

.property-card {
  margin-bottom: 16px;
  border: none;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.05);
}

.property-card .cover {
  height: 120px;
  background: linear-gradient(135deg, #74ebd5 0%, #9face6 100%);
  border-radius: 8px;
  margin-bottom: 10px;
}

.property-card .title {
  font-weight: 600;
  margin-bottom: 4px;
}

.property-card .meta {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}

.property-card .actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.right .el-card {
  border: none;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
}

@media (max-width: 992px) {
  .hero,
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
