<template>
  <div id="app">
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="logo">
            <el-icon><House /></el-icon>
            <span class="logo-text">FindMyBed</span>
          </div>
          <nav class="nav-menu">
            <router-link to="/" class="nav-link">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </router-link>
            <router-link to="/simple-booking" class="nav-link">
              <el-icon><Calendar /></el-icon>
              <span>快速预订</span>
            </router-link>
            <router-link to="/booking" class="nav-link" v-if="isLoggedIn">
              <el-icon><Calendar /></el-icon>
              <span>{{ bookingNavText }}</span>
            </router-link>
            <router-link to="/payments" class="nav-link" v-if="isLoggedIn">
              <el-icon><Collection /></el-icon>
              <span>支付管理</span>
            </router-link>

            <router-link
              to="/merchant-orders"
              class="nav-link"
              v-if="isLoggedIn && (userInfo?.role === 'MERCHANT' || userInfo?.role === 'ADMIN')"
            >
              <el-icon><Tickets /></el-icon>
              <span>订单</span>
            </router-link>
            <router-link
              to="/property"
              class="nav-link"
              v-if="isLoggedIn && (userInfo?.role === 'MERCHANT' || userInfo?.role === 'ADMIN')"
            >
              <el-icon><House /></el-icon>
              <span>民宿管理</span>
            </router-link>
            <router-link to="/profile" class="nav-link" v-if="isLoggedIn">
              <el-icon><Avatar /></el-icon>
              <span>个人中心</span>
            </router-link>
            <router-link
              to="/admin"
              class="nav-link"
              v-if="isLoggedIn && userInfo?.role === 'ADMIN'"
            >
              <el-icon><Tickets /></el-icon>
              <span>后台管理</span>
            </router-link>
            <router-link to="/login" class="nav-link" v-if="!isLoggedIn">
              <el-icon><User /></el-icon>
              <span>登录</span>
            </router-link>
            <div class="user-info" v-if="isLoggedIn">
              <el-dropdown @command="handleCommand">
                <span class="user-dropdown">
                  <el-avatar :size="28" :src="userInfo?.avatar" icon="Avatar" />
                  <span>{{ userInfo?.username }}</span>
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </nav>
        </div>
      </el-header>
      <el-main class="main-area">
        <div class="main-panel">
          <router-view></router-view>
        </div>
      </el-main>
    </el-container>
    <AiAssistant />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { House, HomeFilled, Calendar, User, Avatar, ArrowDown, Tickets, Collection } from '@element-plus/icons-vue'
import AiAssistant from './components/AiAssistant.vue'

const router = useRouter()
const route = useRoute()
const userInfo = ref(null)
const isLoggedIn = computed(() => !!userInfo.value)
const bookingNavText = computed(() => (userInfo.value?.role === 'USER' ? '购物车 / 订单' : '预订管理'))

const syncUser = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      userInfo.value = JSON.parse(userStr)
    } catch (e) {
      userInfo.value = null
    }
  } else {
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

// 切换路由时也同步一次，避免登录后需要刷新
watch(
  () => route.fullPath,
  () => syncUser(),
  { immediate: true }
)

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('user')
    window.dispatchEvent(new Event('user-changed'))
    userInfo.value = null
    router.push('/login')
  }
}
</script>

<style scoped>
.root-vars {
  --primary: #2563eb;
  --primary-soft: #eff6ff;
  --ink: #0f172a;
  --muted: #94a3b8;
  --card-border: rgba(15, 120, 255, 0.08);
}

#app {
  min-height: 100vh;
  background:
    radial-gradient(620px 620px at 12% 22%, rgba(37, 99, 235, 0.10), transparent 58%),
    radial-gradient(520px 520px at 82% 8%, rgba(100, 181, 255, 0.10), transparent 55%),
    radial-gradient(720px 720px at 60% 72%, rgba(58, 160, 255, 0.10), transparent 62%),
    linear-gradient(135deg, #f6f9ff 0%, #fbfdff 45%, #ffffff 100%);
}

.el-header {
  background: linear-gradient(110deg, #0f78ff 0%, #3aa0ff 60%, #64b5ff 100%);
  box-shadow: 0 8px 24px rgba(5, 83, 168, 0.25);
  padding: 0;
  height: 64px !important;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 30px;
  max-width: 1400px;
  margin: 0 auto;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #ffffff;
}

.logo .el-icon {
  font-size: 28px;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  color: #e5efff;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.25s ease;
  font-size: 14px;
  backdrop-filter: blur(2px);
  border: 1px solid transparent;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
  border-color: rgba(255, 255, 255, 0.12);
}

.nav-link.router-link-active {
  background: #ffffff;
  color: #0f78ff;
  box-shadow: 0 10px 22px rgba(15, 120, 255, 0.18);
  border-color: rgba(15, 120, 255, 0.18);
}

.user-info {
  margin-left: 8px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 999px;
  transition: all 0.25s ease;
  color: #e5efff;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.user-dropdown:hover {
  background: rgba(255, 255, 255, 0.24);
  color: #ffffff;
}

.main-area {
  padding: 30px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.main-panel {
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 20px 50px rgba(8, 49, 124, 0.14);
  padding: 22px;
  border: 1px solid var(--card-border);
}

:global(.el-card) {
  border-radius: 14px;
  border: 1px solid rgba(15, 120, 255, 0.08) !important;
  box-shadow: 0 12px 32px rgba(15, 120, 255, 0.08);
}

:global(.el-button--primary:not(.is-plain):not(.is-text):not(.is-link)) {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 55%, #2563eb 100%);
  border: none;
  color: #ffffff;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.22);
}

:global(.el-button--primary:not(.is-plain):not(.is-text):not(.is-link):hover) {
  filter: brightness(1.02);
}

:global(.el-button--success:not(.is-plain):not(.is-text):not(.is-link)) {
  background: linear-gradient(135deg, #34d399 0%, #22c55e 55%, #16a34a 100%);
  border: none;
  color: #ffffff;
  box-shadow: 0 10px 24px rgba(34, 197, 94, 0.20);
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 15px;
  }

  .logo-text {
    display: none;
  }

  .nav-link span {
    display: none;
  }
}
</style>


