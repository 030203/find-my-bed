<template>
  <div class="home">
    <section class="hero-shell">
      <div class="hero-board">
        <div class="hero-copy">
          <p class="hero-kicker">Boutique Stay Operating Canvas</p>
          <h1>把预订、入住与运营，收进一张更有呼吸感的首页。</h1>
          <p class="hero-summary">
            FindMyBed 用更清晰的视觉层级组织订单、房态、支付与设置，让用户与商户都能在第一屏找到下一步。
          </p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="primaryAction">
              {{ isLoggedIn ? '立即开始预订' : '创建你的空间' }}
              <el-icon><ArrowRight /></el-icon>
            </el-button>
            <el-button plain size="large" @click="secondaryAction">
              {{ secondaryActionLabel }}
            </el-button>
          </div>
          <div class="metric-grid">
            <div v-for="metric in heroMetrics" :key="metric.label" class="metric-card">
              <div class="metric-value">{{ metric.value }}</div>
              <div class="metric-label">{{ metric.label }}</div>
              <p>{{ metric.desc }}</p>
            </div>
          </div>
        </div>

        <div class="hero-stage">
          <div class="stage-card stage-card--main">
            <div class="panel-head">
              <div>
                <span class="panel-kicker">Live Board</span>
                <h2>今日经营概览</h2>
              </div>
              <div class="status-pill">
                <span class="status-dot"></span>
                系统在线
              </div>
            </div>
            <div class="stats-grid">
              <div v-for="stat in stats" :key="stat.label" class="stat-card">
                <span class="stat-label">{{ stat.label }}</span>
                <strong class="stat-value">{{ stat.value }}</strong>
                <span class="stat-desc">{{ stat.desc }}</span>
              </div>
            </div>
            <div class="trend-strip">
              <div v-for="highlight in highlights" :key="highlight.label" class="trend-card">
                <span class="trend-label">{{ highlight.label }}</span>
                <strong>{{ highlight.value }}</strong>
                <p>{{ highlight.desc }}</p>
              </div>
            </div>
          </div>

          <div class="stage-card stage-card--side">
            <span class="panel-kicker">Flow Cue</span>
            <h3>首页即是工作台</h3>
            <div class="workflow-list">
              <div v-for="workflow in workflows" :key="workflow.index" class="workflow-item">
                <div class="workflow-index">{{ workflow.index }}</div>
                <div class="workflow-body">
                  <strong>{{ workflow.title }}</strong>
                  <p>{{ workflow.desc }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="signal-strip">
      <article v-for="signal in signals" :key="signal.title" class="signal-card">
        <div class="signal-icon" :style="{ background: signal.bg }">
          <el-icon :style="{ color: signal.color }">
            <component :is="signal.icon" />
          </el-icon>
        </div>
        <div class="signal-copy">
          <h3>{{ signal.title }}</h3>
          <p>{{ signal.desc }}</p>
        </div>
      </article>
    </section>

    <section class="feature-wrap">
      <div class="section-head">
        <div>
          <p class="eyebrow">Core Studio</p>
          <h2>为高频任务建立明确、直接的视觉入口</h2>
          <p class="section-sub">
            首页不再只是欢迎页，而是把预订、订单、统计与设置组织成可直接点击的工作面板。
          </p>
        </div>
      </div>

      <div class="feature-grid">
        <article v-for="feature in features" :key="feature.title" class="feature-card">
          <div class="feature-top">
            <div class="icon-wrap" :style="{ background: feature.bg }">
              <el-icon :style="{ color: feature.color }">
                <component :is="feature.icon" />
              </el-icon>
            </div>
            <span class="feature-tag">{{ feature.tag }}</span>
          </div>
          <div class="feature-text">
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.desc }}</p>
          </div>
          <div class="feature-footer">
            <span>{{ feature.tip }}</span>
            <el-button text type="primary" @click="feature.action">
              进入模块
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight,
  Calendar,
  DataAnalysis,
  Document,
  Setting,
  UserFilled,
  Van
} from '@element-plus/icons-vue'

const router = useRouter()
const userInfo = ref(null)

const syncUser = () => {
  const storedUser = localStorage.getItem('user')

  if (!storedUser) {
    userInfo.value = null
    return
  }

  try {
    userInfo.value = JSON.parse(storedUser)
  } catch (error) {
    userInfo.value = null
  }
}

onMounted(() => {
  syncUser()
  window.addEventListener('user-changed', syncUser)
  window.addEventListener('storage', syncUser)
})

onBeforeUnmount(() => {
  window.removeEventListener('user-changed', syncUser)
  window.removeEventListener('storage', syncUser)
})

const isLoggedIn = computed(() => !!userInfo.value)
const secondaryActionLabel = computed(() => (isLoggedIn.value ? '查看经营看板' : '注册账户'))

const goToRegister = () => {
  router.push('/register')
}

const navigateWithGuard = (path, requiresAuth = true) => {
  if (requiresAuth && !isLoggedIn.value) {
    goToRegister()
    return
  }

  router.push(path)
}

const primaryAction = () => {
  if (isLoggedIn.value) {
    router.push('/simple-booking')
    return
  }

  goToRegister()
}

const secondaryAction = () => {
  if (isLoggedIn.value) {
    router.push('/stats')
    return
  }

  goToRegister()
}

const heroMetrics = [
  {
    value: '4',
    label: '核心入口',
    desc: '首屏直达预订、订单、统计与设置'
  },
  {
    value: '2 min',
    label: '平均下单路径',
    desc: '更短的操作链路，适合快速决策'
  },
  {
    value: '24/7',
    label: '协同可见性',
    desc: '用户、商户与后台共享同一节奏'
  }
]

const stats = [
  { label: '待处理订单', value: '12', desc: '含待确认与待支付' },
  { label: '本月入住率', value: '82%', desc: '环比提升 5.4%' },
  { label: '近 30 日好评率', value: '97%', desc: '口碑稳定向上' }
]

const highlights = [
  { label: '入住波峰', value: '18:30', desc: '建议前台提前准备房卡与引导' },
  { label: '支付完成率', value: '91%', desc: '相比昨日再提升 4.8%' }
]

const workflows = [
  { index: '01', title: '访客快速进入', desc: '首个动作被放大，避免在首页做无效搜索。' },
  { index: '02', title: '经营指标同时可见', desc: '判断业务状态，不必再切换多个页面。' },
  { index: '03', title: '高频模块直接跳转', desc: '把配置、统计和订单收束到同一动线。' }
]

const signals = [
  {
    icon: Document,
    title: '信息层级更清晰',
    desc: '首屏通过标题、指标卡与工作流提示区分浏览信息与可执行动作。',
    color: '#0f78ff',
    bg: 'linear-gradient(135deg, #e8f1ff 0%, #d9e9ff 100%)'
  },
  {
    icon: Van,
    title: '关键动作更靠前',
    desc: '常见入口被推到第一屏，减少重复滚动和多余跳转。',
    color: '#0f9f6e',
    bg: 'linear-gradient(135deg, #e6fff4 0%, #cff7e8 100%)'
  },
  {
    icon: UserFilled,
    title: '移动端结构更稳定',
    desc: '卡片在窄屏下自动切换单列，依然保持阅读节奏与触达效率。',
    color: '#f08c2e',
    bg: 'linear-gradient(135deg, #fff4e6 0%, #ffe8c9 100%)'
  }
]

const features = [
  {
    icon: Calendar,
    title: '快速预订',
    desc: '把日期、房型与下单动作放进最短路径，适合访客和前台快速完成预约。',
    tip: '最快进入预订流程',
    tag: 'Booking',
    color: '#0f78ff',
    bg: 'linear-gradient(135deg, #e8f1ff 0%, #d7e8ff 100%)',
    action: () => navigateWithGuard('/simple-booking', false)
  },
  {
    icon: Document,
    title: '订单排期',
    desc: '集中处理确认、改期、取消与入住状态，避免在多个页面来回切换。',
    tip: '面向运营与商户',
    tag: 'Operations',
    color: '#0f9f6e',
    bg: 'linear-gradient(135deg, #e7fff4 0%, #d3f7e7 100%)',
    action: () => navigateWithGuard('/booking')
  },
  {
    icon: DataAnalysis,
    title: '数据看板',
    desc: '把入住率、支付完成率和波峰时段放到统一看板，帮助更快做经营判断。',
    tip: '适合每日巡检',
    tag: 'Insights',
    color: '#6d5efc',
    bg: 'linear-gradient(135deg, #f0edff 0%, #e6e0ff 100%)',
    action: () => navigateWithGuard('/stats')
  },
  {
    icon: Setting,
    title: '系统配置',
    desc: '佣金策略、偏好设置与运营规则在一个入口内收束，减少认知负担。',
    tip: '统一管理策略细节',
    tag: 'Setup',
    color: '#f08c2e',
    bg: 'linear-gradient(135deg, #fff5e8 0%, #ffe6c8 100%)',
    action: () => navigateWithGuard('/settings')
  }
]
</script>

<style scoped>
.home {
  --ink: #0f172a;
  --muted: #5f6f86;
  --line: rgba(15, 120, 255, 0.12);
  --panel: rgba(255, 255, 255, 0.78);
  min-height: calc(100vh - 124px);
  margin: -22px;
  padding: 30px;
  position: relative;
  overflow: hidden;
  border-radius: 18px;
  background:
    radial-gradient(circle at 12% 20%, rgba(94, 180, 255, 0.28), transparent 30%),
    radial-gradient(circle at 84% 8%, rgba(20, 132, 255, 0.16), transparent 28%),
    radial-gradient(circle at 82% 80%, rgba(255, 216, 167, 0.18), transparent 32%),
    linear-gradient(180deg, #eff6ff 0%, #f6fbff 46%, #fcfeff 100%);
}

.home::before,
.home::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  filter: blur(10px);
  opacity: 0.7;
}

.home::before {
  width: 280px;
  height: 280px;
  top: 56px;
  right: -100px;
  background: rgba(255, 255, 255, 0.55);
}

.home::after {
  width: 220px;
  height: 220px;
  bottom: 72px;
  left: -70px;
  background: rgba(125, 199, 255, 0.18);
}

.hero-shell,
.signal-strip,
.feature-wrap {
  position: relative;
  z-index: 1;
}

.hero-board {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(340px, 0.95fr);
  gap: 22px;
  align-items: stretch;
}

.hero-copy,
.stage-card,
.signal-card,
.feature-card {
  animation: rise-in 0.72s ease both;
}

.hero-copy {
  padding: 34px;
  border-radius: 30px;
  color: #ffffff;
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, #071a40 0%, #0f78ff 56%, #7fd4ff 100%);
  box-shadow: 0 30px 60px rgba(6, 31, 89, 0.24);
}

.hero-copy::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 24%, rgba(255, 255, 255, 0.22), transparent 24%),
    radial-gradient(circle at 82% 20%, rgba(255, 255, 255, 0.18), transparent 26%);
  pointer-events: none;
}

.hero-copy > * {
  position: relative;
  z-index: 1;
}

.hero-kicker,
.panel-kicker,
.eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.hero-kicker {
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.18);
  color: rgba(255, 255, 255, 0.82);
}

.hero-copy h1 {
  margin: 20px 0 16px;
  max-width: 640px;
  font-size: 50px;
  line-height: 1.08;
  letter-spacing: -0.04em;
}

.hero-summary {
  max-width: 620px;
  margin: 0;
  color: rgba(255, 255, 255, 0.88);
  font-size: 16px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 28px;
}

.hero-actions :deep(.el-button) {
  min-width: 156px;
  border-radius: 14px;
}

.hero-actions :deep(.el-button span) {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 34px;
}

.metric-card {
  padding: 16px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(8px);
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
}

.metric-label {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.76);
}

.metric-card p {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.74);
}

.hero-stage {
  display: grid;
  gap: 18px;
}

.stage-card {
  border-radius: 28px;
  padding: 24px;
  background: var(--panel);
  border: 1px solid rgba(255, 255, 255, 0.52);
  backdrop-filter: blur(18px);
  box-shadow: 0 20px 52px rgba(15, 55, 112, 0.12);
}

.stage-card--main {
  animation-delay: 0.08s;
}

.stage-card--side {
  animation-delay: 0.16s;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.panel-kicker {
  color: #0f78ff;
}

.panel-head h2,
.stage-card h3 {
  margin: 8px 0 0;
  color: var(--ink);
}

.panel-head h2 {
  font-size: 28px;
}

.stage-card h3 {
  font-size: 24px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 999px;
  color: #0f9f6e;
  background: rgba(15, 159, 110, 0.08);
  font-size: 13px;
  font-weight: 700;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #0f9f6e;
  box-shadow: 0 0 0 6px rgba(15, 159, 110, 0.12);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 22px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(15, 120, 255, 0.08);
}

.stat-label,
.trend-label,
.feature-footer span {
  color: var(--muted);
  font-size: 13px;
}

.stat-value {
  font-size: 30px;
  color: #0f78ff;
  line-height: 1;
}

.stat-desc,
.trend-card p,
.workflow-body p,
.signal-copy p,
.feature-text p,
.section-sub {
  margin: 0;
  color: var(--muted);
  line-height: 1.7;
}

.trend-strip {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.trend-card {
  padding: 16px 18px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(15, 120, 255, 0.06), rgba(255, 255, 255, 0.9));
  border: 1px solid rgba(15, 120, 255, 0.08);
}

.trend-card strong {
  display: block;
  margin: 6px 0 8px;
  color: var(--ink);
  font-size: 22px;
}

.workflow-list {
  display: grid;
  gap: 14px;
  margin-top: 18px;
}

.workflow-item {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.workflow-index {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: linear-gradient(135deg, #eaf3ff 0%, #d6e7ff 100%);
  color: #0f78ff;
  font-weight: 700;
  flex-shrink: 0;
}

.workflow-body strong,
.signal-copy h3,
.feature-text h3 {
  display: block;
  color: var(--ink);
}

.signal-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin-top: 22px;
}

.signal-card {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding: 18px 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(255, 255, 255, 0.55);
  box-shadow: 0 18px 42px rgba(15, 55, 112, 0.08);
}

.signal-card:nth-child(1) {
  animation-delay: 0.12s;
}

.signal-card:nth-child(2) {
  animation-delay: 0.18s;
}

.signal-card:nth-child(3) {
  animation-delay: 0.24s;
}

.signal-icon,
.icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.signal-icon {
  width: 52px;
  height: 52px;
  border-radius: 18px;
  font-size: 24px;
}

.signal-copy h3 {
  margin: 2px 0 8px;
  font-size: 18px;
}

.feature-wrap {
  margin-top: 34px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
  margin-bottom: 18px;
}

.eyebrow {
  color: #0f78ff;
}

.section-head h2 {
  margin: 10px 0 8px;
  max-width: 700px;
  color: var(--ink);
  font-size: 34px;
  line-height: 1.16;
}

.section-sub {
  max-width: 760px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.feature-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: 260px;
  padding: 20px;
  border-radius: 26px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.88), rgba(255, 255, 255, 0.7)),
    linear-gradient(135deg, rgba(15, 120, 255, 0.06), transparent 56%);
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 20px 46px rgba(15, 55, 112, 0.08);
  transition: transform 0.24s ease, box-shadow 0.24s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 26px 54px rgba(15, 55, 112, 0.12);
}

.feature-card:nth-child(1) {
  animation-delay: 0.18s;
}

.feature-card:nth-child(2) {
  animation-delay: 0.24s;
}

.feature-card:nth-child(3) {
  animation-delay: 0.3s;
}

.feature-card:nth-child(4) {
  animation-delay: 0.36s;
}

.feature-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.icon-wrap {
  width: 60px;
  height: 60px;
  border-radius: 20px;
  font-size: 26px;
}

.feature-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  border-radius: 999px;
  color: #4f5f78;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  background: rgba(15, 23, 42, 0.05);
}

.feature-text h3 {
  margin: 0 0 8px;
  font-size: 22px;
}

.feature-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-top: auto;
}

.feature-footer :deep(.el-button span) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

@keyframes rise-in {
  from {
    opacity: 0;
    transform: translateY(18px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1200px) {
  .hero-board {
    grid-template-columns: 1fr;
  }

  .hero-stage {
    grid-template-columns: 1.05fr 0.95fr;
  }

  .feature-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .home {
    padding: 22px;
  }

  .hero-copy {
    padding: 28px;
  }

  .hero-copy h1 {
    font-size: 42px;
  }

  .metric-grid,
  .signal-strip {
    grid-template-columns: 1fr;
  }

  .stats-grid,
  .trend-strip,
  .hero-stage {
    grid-template-columns: 1fr;
  }

  .section-head h2 {
    font-size: 30px;
  }
}

@media (max-width: 768px) {
  .home {
    margin: -22px;
    padding: 18px;
  }

  .hero-copy,
  .stage-card,
  .signal-card,
  .feature-card {
    border-radius: 22px;
  }

  .hero-copy h1 {
    font-size: 34px;
  }

  .hero-summary {
    font-size: 15px;
  }

  .hero-actions {
    flex-direction: column;
  }

  .hero-actions :deep(.el-button) {
    width: 100%;
  }

  .feature-grid,
  .metric-grid,
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .panel-head,
  .feature-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .section-head h2 {
    font-size: 26px;
  }
}
</style>
