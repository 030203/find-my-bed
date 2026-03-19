<template>
  <div class="assistant-shell">
    <transition name="assistant-panel">
      <section v-if="expanded" class="assistant-panel">
        <header class="assistant-header">
          <div class="assistant-header-copy">
            <p class="assistant-kicker">DeepSeek Data Copilot</p>
            <h3>业务数据助手</h3>
            <p class="assistant-subtitle">面向订单、支付、房源和评分的智能问答入口。</p>
          </div>
          <button class="assistant-close" type="button" @click="expanded = false">
            <el-icon><Close /></el-icon>
          </button>
        </header>

        <div class="assistant-body">
          <div ref="messagesRef" class="assistant-messages">
            <article
              v-for="item in messages"
              :key="item.id"
              class="assistant-message"
              :class="item.role"
            >
              <div class="avatar">{{ item.role === 'user' ? 'U' : 'AI' }}</div>
              <div class="bubble">
                <p>{{ item.content }}</p>
                <span v-if="item.meta" class="meta">{{ item.meta }}</span>
              </div>
            </article>

            <article v-if="loading" class="assistant-message assistant">
              <div class="avatar">AI</div>
              <div class="bubble typing">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </article>
          </div>
        </div>

        <footer class="assistant-footer">
          <div class="input-wrap">
            <textarea
              v-model="draft"
              rows="3"
              placeholder="例如：支付成功率怎么样？预订量最高的民宿有哪些？"
              @keydown.enter.exact.prevent="submit"
            />
          </div>
          <div class="footer-row">
            <span class="status" :class="{ error: !!errorText }">
              {{ errorText || '后端由 Flask 微服务提供，当前默认模型为 DeepSeek。' }}
            </span>
            <el-button type="primary" :loading="loading" @click="submit">发送</el-button>
          </div>
        </footer>
      </section>
    </transition>

    <button class="assistant-trigger" type="button" @click="togglePanel">
      <div class="trigger-badge">AI</div>
      <div class="trigger-text">
        <strong>业务助手</strong>
        <span>直接提问订单、支付和房源数据</span>
      </div>
      <el-icon class="trigger-icon"><ChatLineRound /></el-icon>
    </button>
  </div>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue'
import { ChatLineRound, Close } from '@element-plus/icons-vue'
import { assistantApi } from '../api'

const expanded = ref(false)
const loading = ref(false)
const errorText = ref('')
const draft = ref('')
const conversationId = ref('')
const messagesRef = ref(null)
const messages = ref([
  {
    id: 'welcome',
    role: 'assistant',
    content: '我是你的 AI 业务助手。当前版本已经接入独立 Flask 服务，适合后续演进成完整微服务和 RAG。',
    meta: 'Flask + DeepSeek'
  }
])

const scrollToBottom = async () => {
  if (!expanded.value) {
    return
  }

  await nextTick()
  const container = messagesRef.value
  if (!container) {
    return
  }

  container.scrollTop = container.scrollHeight
}

const togglePanel = () => {
  expanded.value = !expanded.value
}

watch(
  () => messages.value.length,
  () => {
    scrollToBottom()
  }
)

watch(loading, () => {
  scrollToBottom()
})

watch(expanded, (isExpanded) => {
  if (isExpanded) {
    scrollToBottom()
  }
})

const submit = async () => {
  const message = draft.value.trim()
  if (!message || loading.value) {
    return
  }

  errorText.value = ''
  messages.value.push({
    id: `user-${Date.now()}`,
    role: 'user',
    content: message
  })
  draft.value = ''
  loading.value = true

  try {
    const rawUser = localStorage.getItem('user')
    const currentUser = rawUser ? JSON.parse(rawUser) : null

    const { data } = await assistantApi.chat({
      message,
      conversationId: conversationId.value,
      userContext: currentUser
        ? {
            id: currentUser.id,
            username: currentUser.username,
            role: currentUser.role
          }
        : null
    })

    conversationId.value = data.conversationId || conversationId.value
    messages.value.push({
      id: `assistant-${Date.now()}`,
      role: 'assistant',
      content: data.answer || '服务已返回，但没有生成回答。',
      meta: data.source || 'deepseek'
    })
  } catch (error) {
    errorText.value =
      error.response?.data?.error ||
      'AI 服务暂时不可用，请检查 Flask 服务、DeepSeek 配置和数据库连接。'
    messages.value.push({
      id: `assistant-error-${Date.now()}`,
      role: 'assistant',
      content: '这次没有成功拿到结果。请先确认 Flask AI 服务已经启动。',
      meta: 'error'
    })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.assistant-shell {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 40;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 14px;
}

.assistant-trigger {
  display: flex;
  align-items: center;
  gap: 14px;
  width: min(320px, calc(100vw - 32px));
  border: none;
  cursor: pointer;
  padding: 14px 16px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(9, 65, 140, 0.97) 0%, rgba(20, 115, 230, 0.95) 48%, rgba(45, 183, 201, 0.94) 100%);
  color: #fff;
  box-shadow: 0 24px 48px rgba(15, 78, 162, 0.32);
}

.trigger-badge {
  width: 42px;
  height: 42px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
}

.trigger-text {
  flex: 1;
  text-align: left;
}

.trigger-text strong {
  display: block;
  font-size: 15px;
}

.trigger-text span {
  display: block;
  margin-top: 3px;
  font-size: 12px;
  color: rgba(235, 246, 255, 0.88);
}

.trigger-icon {
  font-size: 20px;
}

.assistant-panel {
  width: min(420px, calc(100vw - 24px));
  height: min(68vh, 680px);
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  border-radius: 28px;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(66, 153, 225, 0.16), transparent 28%),
    linear-gradient(180deg, rgba(247, 251, 255, 0.98), rgba(236, 246, 255, 0.98));
  border: 1px solid rgba(92, 153, 255, 0.18);
  box-shadow: 0 28px 70px rgba(14, 62, 121, 0.22);
  backdrop-filter: blur(14px);
}

.assistant-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px 14px;
  background: linear-gradient(135deg, rgba(10, 76, 160, 0.95), rgba(30, 149, 221, 0.92));
  color: #fff;
}

.assistant-header-copy {
  min-width: 0;
}

.assistant-kicker {
  margin: 0 0 6px;
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(220, 241, 255, 0.78);
}

.assistant-header h3 {
  margin: 0;
  font-size: 22px;
}

.assistant-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.5;
  color: rgba(227, 244, 255, 0.88);
}

.assistant-close {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  cursor: pointer;
  flex-shrink: 0;
}

.assistant-body {
  min-height: 0;
  padding: 16px 18px 10px;
}

.assistant-messages {
  height: 100%;
  min-height: 0;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-right: 4px;
}

.assistant-message {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.assistant-message.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #1274ea, #24b0db);
  flex-shrink: 0;
}

.user .avatar {
  background: linear-gradient(135deg, #0f172a, #334155);
}

.bubble {
  max-width: 78%;
  padding: 14px 15px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid rgba(56, 135, 255, 0.12);
  box-shadow: 0 12px 24px rgba(16, 66, 133, 0.08);
}

.user .bubble {
  background: linear-gradient(135deg, #1466d9, #1da6de);
  color: #fff;
}

.bubble p {
  margin: 0;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.meta {
  display: inline-block;
  margin-top: 8px;
  font-size: 11px;
  color: rgba(78, 101, 132, 0.75);
}

.user .meta {
  color: rgba(255, 255, 255, 0.74);
}

.typing {
  display: flex;
  align-items: center;
  gap: 6px;
}

.typing span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #5fa8ff;
  animation: pulse 1s infinite ease-in-out;
}

.typing span:nth-child(2) {
  animation-delay: 0.18s;
}

.typing span:nth-child(3) {
  animation-delay: 0.36s;
}

.assistant-footer {
  padding: 14px 18px 18px;
  background: rgba(255, 255, 255, 0.82);
  border-top: 1px solid rgba(84, 145, 246, 0.12);
}

.input-wrap {
  border-radius: 18px;
  background: #fff;
  border: 1px solid rgba(69, 132, 240, 0.14);
}

.input-wrap textarea {
  width: 100%;
  resize: none;
  border: none;
  background: transparent;
  padding: 14px 14px 10px;
  outline: none;
  color: #0f172a;
  font: inherit;
  min-height: 86px;
}

.footer-row {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.status {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  line-height: 1.5;
  color: #4b698b;
}

.status.error {
  color: #c2410c;
}

.assistant-panel-enter-active,
.assistant-panel-leave-active {
  transition: all 0.24s ease;
}

.assistant-panel-enter-from,
.assistant-panel-leave-to {
  opacity: 0;
  transform: translateY(14px) scale(0.98);
}

@keyframes pulse {
  0%,
  80%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }

  40% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .assistant-shell {
    right: 12px;
    left: 12px;
    bottom: 12px;
  }

  .assistant-trigger,
  .assistant-panel {
    width: 100%;
  }

  .assistant-panel {
    height: min(72vh, 620px);
  }

  .bubble {
    max-width: 84%;
  }

  .footer-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>

