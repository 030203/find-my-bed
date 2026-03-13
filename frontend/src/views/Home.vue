<template>
  <div class="home">
    <div class="hero">
      <div class="hero-left">
        <div class="pill">安心入住 · 极速管理</div>
        <h1>民宿预定管理系统</h1>
        <p class="sub">为普通用户与商户提供轻量、直观的预定与运营后台，一处完成订单、房源、支付与设置。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="primaryAction">
            <el-icon><Calendar /></el-icon>
            {{ isLoggedIn ? '立即预订' : '快速开始' }}
          </el-button>
          <el-button size="large" plain @click="viewStats">
            <el-icon><DataAnalysis /></el-icon>
            查看数据
          </el-button>
        </div>
        <div class="badges">
          <span class="badge">极速下单</span>
          <span class="badge">可视化统计</span>
          <span class="badge">多角色协作</span>
        </div>
      </div>
      <div class="hero-right">
        <div class="glass">
          <div class="glass-header">今日概览</div>
          <div class="stats-grid">
            <div v-for="stat in stats" :key="stat.label" class="stat-card">
              <div class="stat-label">{{ stat.label }}</div>
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-desc">{{ stat.desc }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="feature-wrap">
      <div class="section-head">
        <div>
          <p class="eyebrow">核心功能</p>
          <h2>一站式管理民宿业务</h2>
          <p class="section-sub">围绕预订、数据和系统配置，提供开箱即用的操作体验。</p>
        </div>
        <div class="section-actions">
          <el-button type="primary" @click="primaryAction">开始管理</el-button>
          <el-button plain @click="goToRegister" v-if="!isLoggedIn">注册账号</el-button>
        </div>
      </div>

      <el-row :gutter="18">
        <el-col :xs="24" :sm="12" :md="8" v-for="feature in features" :key="feature.title">
          <div class="feature-card">
            <div class="icon-wrap" :style="{ background: feature.bg }">
              <el-icon :style="{ color: feature.color }">
                <component :is="feature.icon" />
              </el-icon>
            </div>
            <div class="feature-text">
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.desc }}</p>
            </div>
            <div class="feature-footer">
              <span>{{ feature.tip }}</span>
              <el-button text type="primary" size="small" @click="feature.action && feature.action()">
                立即前往
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Calendar,
  User,
  UserFilled,
  Document,
  Setting,
  DataAnalysis
} from '@element-plus/icons-vue'

const router = useRouter()

const isLoggedIn = computed(() => {
  return !!localStorage.getItem('user')
})

const features = [
  {
    icon: 'Calendar',
    title: '预订管理',
    desc: '批量管理订单、入住离店、取消与改期，一键完成。',
    tip: '面向管理员/商户',
    color: '#1f6feb',
    bg: 'linear-gradient(135deg,#e9f1ff 0%,#d7e7ff 100%)',
    action: () => router.push('/booking')
  },
  {
    icon: 'Document',
    title: '数据统计',
    desc: '实时看板与趋势，快速掌握入住率、取消率等关键指标。',
    tip: '实时刷新',
    color: '#16b364',
    bg: 'linear-gradient(135deg,#e9fff4 0%,#d5f7e7 100%)',
    action: () => router.push('/stats')
  },
  {
    icon: 'Setting',
    title: '系统配置',
    desc: '佣金、政策、偏好一站配置，降低运营门槛。',
    tip: '可视化调整',
    color: '#f59e0b',
    bg: 'linear-gradient(135deg,#fff6e5 0%,#ffe9c7 100%)',
    action: () => router.push('/settings')
  }
]

const stats = [
  { label: '待处理订单', value: '12', desc: '含待确认与待支付' },
  { label: '本月入住率', value: '82%', desc: '环比 +5.4%' },
  { label: '好评率', value: '97%', desc: '近30日评价' }
]

const goToRegister = () => {
  router.push('/register')
}

const primaryAction = () => {
  if (isLoggedIn.value) {
    router.push('/simple-booking') // simple-booking 内部已过滤 APPROVED
  } else {
    router.push('/register')
  }
}

const viewStats = () => {
  router.push('/stats')
}
</script>

<style scoped>
.home {
  min-height: calc(100vh - 80px);
  background: radial-gradient(circle at 15% 20%, #f0f7ff 0%, #fff 40%), radial-gradient(circle at 85% 10%, #f7f3ff 0%, #fff 35%), #f9fafb;
  padding: 12px 0 48px;
}

.hero {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 24px;
  align-items: stretch;
  padding: 24px;
}

.hero-left {
  background: linear-gradient(135deg, #0f7bff 0%, #5ec7ff 60%, #8de6ff 100%);
  color: #fff;
  border-radius: 20px;
  padding: 28px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 18px 40px rgba(15, 123, 255, 0.18);
}
.hero-left::after {
  content: '';
  position: absolute;
  right: -60px;
  top: -60px;
  width: 220px;
  height: 220px;
  background: rgba(255, 255, 255, 0.15);
  filter: blur(8px);
  border-radius: 50%;
}
.pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.16);
  border-radius: 999px;
  font-weight: 600;
}
.hero h1 {
  margin: 14px 0 6px;
  font-size: 32px;
  letter-spacing: 0.02em;
}
.sub {
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 16px;
  line-height: 1.6;
}
.hero-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
}
.badges {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.badge {
  background: rgba(255, 255, 255, 0.14);
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 13px;
}

.hero-right {
  display: flex;
  align-items: center;
}
.glass {
  width: 100%;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(6px);
  border-radius: 18px;
  padding: 18px;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(15, 123, 255, 0.06);
}
.glass-header {
  font-weight: 700;
  margin-bottom: 12px;
  color: #1f2933;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
}
.stat-card {
  background: #f8fbff;
  border-radius: 12px;
  padding: 12px;
  border: 1px solid #e8f0ff;
}
.stat-label {
  color: #6b7280;
  font-size: 13px;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin: 4px 0;
  color: #0f7bff;
}
.stat-desc {
  color: #9ca3af;
  font-size: 12px;
}

.feature-wrap {
  margin: 10px 24px 0;
  padding: 18px 0 8px;
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}
.eyebrow {
  color: #2563eb;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 12px;
}
.section-head h2 {
  margin: 4px 0;
  font-size: 26px;
  color: #111827;
}
.section-sub {
  color: #6b7280;
  margin: 0;
}
.section-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.feature-card {
  background: #fff;
  border-radius: 14px;
  padding: 14px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
  border: 1px solid #eef0f4;
  box-shadow: 0 12px 26px rgba(0, 0, 0, 0.04);
  height: 100%;
}
.icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.feature-text h3 {
  margin: 0 0 6px;
  font-size: 18px;
  color: #111827;
}
.feature-text p {
  margin: 0;
  color: #6b7280;
  line-height: 1.5;
}
.feature-footer {
  margin-left: auto;
  text-align: right;
  color: #9ca3af;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 992px) {
  .hero {
    grid-template-columns: 1fr;
    padding: 16px;
  }
  .home {
    padding: 8px 0 32px;
  }
  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }
  .feature-wrap {
    margin: 8px 16px 0;
  }
}
</style>
