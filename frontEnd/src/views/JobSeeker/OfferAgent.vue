<template>
  <div class="offer-agent-page">
    <section class="summary-panel">
      <div class="panel-heading">
        <span>OfferAgent</span>
        <el-button :icon="Refresh" circle :loading="contextLoading" @click="loadContext" />
      </div>

      <div class="profile-block">
        <div class="profile-name">{{ context.realName || context.username || '求职者' }}</div>
        <div class="profile-line">{{ context.school || '未填写学校' }} · {{ context.education || '未填写学历' }}</div>
        <div class="profile-line">{{ context.membership === 1 ? '高级会员' : '普通会员' }}</div>
      </div>

      <div class="tag-group">
        <el-tag v-for="item in context.favorPositions || []" :key="item" effect="plain">
          {{ item }}
        </el-tag>
        <el-tag v-if="!context.favorPositions || context.favorPositions.length === 0" type="info" effect="plain">
          未设置目标岗位
        </el-tag>
      </div>

      <div class="stat-grid">
        <div class="stat-item">
          <strong>{{ context.resumes?.length || 0 }}</strong>
          <span>简历</span>
        </div>
        <div class="stat-item">
          <strong>{{ context.posts?.length || 0 }}</strong>
          <span>投递</span>
        </div>
        <div class="stat-item">
          <strong>{{ context.guidanceRequests?.length || 0 }}</strong>
          <span>指导</span>
        </div>
      </div>

      <div class="tool-trace">
        <div class="trace-title">工具上下文</div>
        <div v-for="item in context.toolTrace || []" :key="item" class="trace-item">{{ item }}</div>
      </div>

      <div class="knowledge-sources">
        <div class="trace-title">检索来源</div>
        <div v-if="!context.retrievedKnowledge || context.retrievedKnowledge.length === 0" class="trace-item">
          暂无命中的知识片段
        </div>
        <div v-for="item in context.retrievedKnowledge || []" :key="item.chunkId" class="source-item">
          <div class="source-title">{{ item.title }}</div>
          <div class="source-meta">
            <span>{{ item.category || '知识库' }}</span>
            <span>score {{ item.score || 0 }}</span>
          </div>
          <p>{{ item.summary || item.contentPreview }}</p>
        </div>
      </div>
    </section>

    <section class="chat-panel">
      <div class="chat-header">
        <div>
          <h1>智能求职助手</h1>
          <p>结合简历、求职偏好、投递记录和指导历史生成行动建议</p>
        </div>
        <el-button @click="newConversation">新会话</el-button>
      </div>

      <div ref="messagesContainer" class="messages">
        <div v-for="(message, index) in messages" :key="index" :class="['message-row', message.role]">
          <div class="message-bubble">
            <div v-if="message.role === 'assistant'" v-html="renderMarkdown(message.content)" />
            <div v-else>{{ message.content }}</div>
          </div>
        </div>
        <div v-if="messages.length === 0" class="empty-chat">
          <el-empty description="选择一个问题，或直接输入你的求职目标" />
        </div>
      </div>

      <div class="quick-actions">
        <el-button v-for="item in quickPrompts" :key="item" size="small" @click="sendQuickPrompt(item)">
          {{ item }}
        </el-button>
      </div>

      <div class="composer">
        <el-input
          v-model="input"
          type="textarea"
          :rows="3"
          resize="none"
          placeholder="例如：我想投 AI Agent 实习岗，请帮我分析简历差距并安排 7 天准备计划"
          @keydown.ctrl.enter="sendMessage"
          @keydown.meta.enter="sendMessage"
        />
        <el-button v-if="isStreaming" type="danger" plain @click="stopStream">停止</el-button>
        <el-button v-else type="primary" :disabled="!input.trim()" @click="sendMessage">发送</el-button>
      </div>
    </section>

    <section class="plan-panel">
      <div class="panel-heading">行动看板</div>
      <div class="plan-item">
        <span>1</span>
        <p>完善目标岗位和求职偏好，让 Agent 能更准确判断岗位匹配。</p>
      </div>
      <div class="plan-item">
        <span>2</span>
        <p>保持至少一份结构化简历，便于分析项目经历和关键词覆盖。</p>
      </div>
      <div class="plan-item">
        <span>3</span>
        <p>每次投递后回到这里复盘，形成下一轮简历和面试准备建议。</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/authStore'
import { parseStreamResponse } from '../../api/ai'
import { getOfferAgentContext, offerAgentChatStream } from '../../api/offerAgent'

const authStore = useAuthStore()
const userId = computed(() => authStore.userId || localStorage.getItem('userId'))
const context = ref({})
const contextLoading = ref(false)
const messages = ref([])
const input = ref('')
const isStreaming = ref(false)
const abortController = ref(null)
const messagesContainer = ref(null)

const quickPrompts = [
  '分析我适合投哪些岗位',
  '帮我生成 7 天求职计划',
  '模拟 AI Agent 实习面试',
  '检查我的简历短板'
]

const conversationKey = computed(() => `offer_agent_conversation_${userId.value || 'guest'}`)

function getConversationId() {
  let id = localStorage.getItem(conversationKey.value)
  if (!id) {
    id = Math.random().toString(36).slice(2, 18)
    localStorage.setItem(conversationKey.value, id)
  }
  return id
}

async function loadContext() {
  if (!userId.value) return
  contextLoading.value = true
  try {
    context.value = await getOfferAgentContext(userId.value)
  } catch (error) {
    console.error('OfferAgent context failed:', error)
    ElMessage.error('加载 OfferAgent 上下文失败')
  } finally {
    contextLoading.value = false
  }
}

function newConversation() {
  messages.value = []
  localStorage.removeItem(conversationKey.value)
  getConversationId()
  ElMessage.success('已开启新会话')
}

function sendQuickPrompt(prompt) {
  input.value = prompt
  sendMessage()
}

async function sendMessage() {
  const text = input.value.trim()
  if (!text || isStreaming.value || !userId.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  isStreaming.value = true
  abortController.value = new AbortController()

  const assistantIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })
  await scrollToBottom()

  try {
    const stream = await offerAgentChatStream(
      Number(userId.value),
      getConversationId(),
      text,
      abortController.value.signal
    )
    let accumulated = ''

    await parseStreamResponse(
      stream,
      chunk => {
        if (!isStreaming.value) return
        accumulated += chunk
        messages.value[assistantIndex].content = cleanContent(accumulated)
        scrollToBottom()
      },
      () => {
        isStreaming.value = false
        abortController.value = null
        messages.value[assistantIndex].content = cleanContent(accumulated)
        scrollToBottom()
      },
      error => {
        if (error.name === 'AbortError') return
        throw error
      }
    )
  } catch (error) {
    console.error('OfferAgent request failed:', error)
    isStreaming.value = false
    abortController.value = null
    messages.value[assistantIndex].content = 'OfferAgent 暂时不可用，请稍后重试。'
    ElMessage.error('OfferAgent 请求失败')
  }
}

function stopStream() {
  if (abortController.value) {
    abortController.value.abort()
  }
  isStreaming.value = false
  abortController.value = null
}

function cleanContent(value) {
  return value.replace(/^data:\s*/gm, '').replace(/\n{4,}/g, '\n\n\n').trim()
}

function renderMarkdown(value) {
  return marked.parse(value || '')
}

async function scrollToBottom() {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(() => {
  loadContext()
  getConversationId()
})
</script>

<style scoped>
.offer-agent-page {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr) 260px;
  gap: 20px;
  min-height: calc(100vh - 128px);
}

.summary-panel,
.chat-panel,
.plan-panel {
  background: #fff;
  border: 1px solid #e8ecf3;
  border-radius: 8px;
}

.summary-panel,
.plan-panel {
  padding: 18px;
  height: fit-content;
}

.panel-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 18px;
}

.profile-block {
  padding-bottom: 16px;
  border-bottom: 1px solid #edf0f5;
}

.profile-name {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 8px;
}

.profile-line {
  font-size: 13px;
  color: #667085;
  line-height: 1.8;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 16px 0;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 18px;
}

.stat-item {
  background: #f7f9fc;
  border: 1px solid #edf0f5;
  border-radius: 8px;
  padding: 12px 8px;
  text-align: center;
}

.stat-item strong {
  display: block;
  font-size: 20px;
  color: #2f5bea;
}

.stat-item span {
  font-size: 12px;
  color: #667085;
}

.tool-trace {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.knowledge-sources {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 18px;
}

.trace-title {
  font-weight: 700;
  color: #374151;
}

.trace-item {
  font-size: 12px;
  color: #667085;
  background: #f8fafc;
  border-radius: 6px;
  padding: 8px;
  line-height: 1.5;
}

.source-item {
  border: 1px solid #e4eaf5;
  background: #fbfdff;
  border-radius: 8px;
  padding: 10px;
}

.source-title {
  font-size: 13px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.5;
}

.source-meta {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin: 5px 0;
  color: #667085;
  font-size: 12px;
}

.source-item p {
  margin: 0;
  color: #4b5563;
  font-size: 12px;
  line-height: 1.6;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  min-height: 720px;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 22px;
  border-bottom: 1px solid #edf0f5;
}

.chat-header h1 {
  font-size: 22px;
  margin: 0 0 6px 0;
  color: #111827;
}

.chat-header p {
  margin: 0;
  font-size: 13px;
  color: #667085;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 22px;
  background: #fbfcff;
}

.message-row {
  display: flex;
  margin-bottom: 14px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 78%;
  border-radius: 8px;
  padding: 12px 14px;
  line-height: 1.7;
  font-size: 14px;
  word-break: break-word;
}

.message-row.user .message-bubble {
  background: #2f5bea;
  color: #fff;
}

.message-row.assistant .message-bubble {
  background: #fff;
  border: 1px solid #e2e8f0;
  color: #1f2937;
}

.message-bubble :deep(h2),
.message-bubble :deep(h3) {
  margin: 10px 0 8px;
  font-size: 16px;
}

.message-bubble :deep(p) {
  margin: 6px 0;
}

.message-bubble :deep(ul),
.message-bubble :deep(ol) {
  padding-left: 20px;
}

.empty-chat {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding: 14px 18px 0;
  border-top: 1px solid #edf0f5;
}

.composer {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 88px;
  gap: 12px;
  padding: 14px 18px 18px;
  align-items: end;
}

.plan-item {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr);
  gap: 10px;
  margin-bottom: 14px;
  align-items: start;
}

.plan-item span {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e8efff;
  color: #2f5bea;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.plan-item p {
  margin: 2px 0 0;
  color: #4b5563;
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 1180px) {
  .offer-agent-page {
    grid-template-columns: 260px minmax(0, 1fr);
  }

  .plan-panel {
    display: none;
  }
}

@media (max-width: 860px) {
  .offer-agent-page {
    grid-template-columns: 1fr;
  }

  .chat-panel {
    min-height: 640px;
  }

  .message-bubble {
    max-width: 92%;
  }
}
</style>
