<template>
  <div class="workflow-page">
    <section class="workflow-header">
      <div>
        <div class="eyebrow">OfferAgent 工作流</div>
        <h1>{{ config.title }}</h1>
        <p>{{ config.description }}</p>
      </div>
      <div class="header-actions">
        <el-button v-if="loading" type="danger" plain size="large" @click="stopWorkflow">停止</el-button>
        <el-button v-else type="primary" size="large" @click="runWorkflow">{{ config.actionText }}</el-button>
      </div>
    </section>

    <section class="control-panel">
      <div class="target-display">
        <span>目标岗位</span>
        <strong>{{ targetPosition || '读取中...' }}</strong>
        <small>来自个人求职信息中的理想岗位</small>
      </div>
      <div class="control-meta">
        <span>用户 ID：{{ userId || '-' }}</span>
        <span>会话：{{ conversationId }}</span>
        <span v-if="stageText">{{ stageText }}</span>
      </div>
    </section>

    <section v-if="workflow.steps.length || workflow.finalReport || loading" class="workflow-grid">
      <div class="steps-panel">
        <div class="panel-heading">
          <span>Agent 执行步骤</span>
          <el-tag v-if="loading" type="primary" effect="plain">运行中</el-tag>
        </div>
        <div v-for="(step, index) in workflow.steps" :key="step.agentName" class="step-item">
          <div :class="['step-index', step.status]">{{ index + 1 }}</div>
          <div class="step-body">
            <div class="step-title">
              <span>{{ step.agentName }}</span>
              <el-tag :type="statusType(step.status)" effect="plain">
                {{ statusText(step.status) }}
              </el-tag>
            </div>
            <p>{{ step.summary || '正在读取上下文并调用工具...' }}</p>
            <div v-if="step.toolCalls?.length" class="tool-row">
              <el-tag v-for="tool in step.toolCalls" :key="tool.toolName" size="small" effect="plain">
                {{ tool.toolName }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="report-panel">
        <div class="panel-heading">
          <span>最终报告</span>
          <el-tag v-if="reportStreaming" type="success" effect="plain">生成中</el-tag>
        </div>
        <div v-if="workflow.finalReport" class="report-content" v-html="renderMarkdown(workflow.finalReport)" />
        <div v-else class="report-placeholder">
          {{ loading ? 'Agent 正在收集证据，报告即将开始生成。' : '运行后将在这里展示报告。' }}
        </div>
      </div>
    </section>

    <section v-else class="empty-panel">
      <el-empty :description="config.emptyText" />
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/authStore'
import { getOfferAgentContext, runOfferAgentWorkflowStream } from '../../api/offerAgent'

const route = useRoute()
const authStore = useAuthStore()
const userId = computed(() => authStore.userId || localStorage.getItem('userId'))
const targetPosition = ref('')
const workflow = ref({
  workflowName: '',
  targetPosition: '',
  conversationId: '',
  steps: [],
  finalReport: ''
})
const loading = ref(false)
const reportStreaming = ref(false)
const stageText = ref('')
const abortController = ref(null)

const configs = {
  sprint: {
    title: '实习冲刺规划',
    description: '按目标岗位读取用户画像、简历、RAG 知识和工具结果，流式生成可执行的求职冲刺计划。',
    actionText: '开始冲刺规划',
    emptyText: '点击开始后，系统会依次执行 ProfileAgent、ResumeAgent、KnowledgeAgent、MatchAgent 和 PlannerAgent。'
  },
  resume: {
    title: '简历优化工作流',
    description: '结合目标岗位、简历内容和 RAG 检索结果，实时输出岗位匹配分析和简历改写建议。',
    actionText: '开始简历优化',
    emptyText: '点击开始后，系统会依次执行 ResumeParserAgent、JDAnalysisAgent、ResumeCoachAgent、RAGAgent 和 RewriteAgent。'
  },
  interview: {
    title: '模拟面试工作流',
    description: '围绕目标岗位生成面试题、准备度评估、追问训练和答题表达建议。',
    actionText: '开始模拟面试',
    emptyText: '点击开始后，系统会依次执行 ProfileAgent、InterviewQuestionAgent、EvaluatorAgent、KnowledgeAgent 和 CoachAgent。'
  }
}

const workflowType = computed(() => route.meta.workflowType || 'sprint')
const config = computed(() => configs[workflowType.value] || configs.sprint)
const conversationId = computed(() => `workflow-${workflowType.value}-${userId.value || 'guest'}`)

watch(workflowType, () => {
  resetWorkflow()
})

onMounted(() => {
  loadTargetPosition()
})

async function loadTargetPosition() {
  if (!userId.value) return
  try {
    const context = await getOfferAgentContext(userId.value)
    targetPosition.value = context.favorPositions?.[0] || 'AI Agent 实习生'
    resetWorkflow()
  } catch (error) {
    console.warn('Load workflow target position failed:', error)
    targetPosition.value = 'AI Agent 实习生'
  }
}

function resetWorkflow() {
  workflow.value = {
    workflowName: '',
    targetPosition: targetPosition.value,
    conversationId: conversationId.value,
    steps: [],
    finalReport: ''
  }
  stageText.value = ''
  reportStreaming.value = false
}

async function runWorkflow() {
  if (!userId.value || loading.value) return
  resetWorkflow()
  loading.value = true
  abortController.value = new AbortController()
  stageText.value = '正在启动工作流...'

  try {
    const stream = await runOfferAgentWorkflowStream(
      workflowType.value,
      Number(userId.value),
      conversationId.value,
      abortController.value.signal
    )
    await readNdjsonStream(stream, handleStreamEvent)
    ElMessage.success('工作流执行完成')
  } catch (error) {
    if (error.name !== 'AbortError') {
      console.error('OfferAgent workflow failed:', error)
      ElMessage.error('工作流执行失败，请稍后重试')
    }
  } finally {
    loading.value = false
    reportStreaming.value = false
    abortController.value = null
    if (!stageText.value.includes('失败')) {
      stageText.value = '已完成'
    }
  }
}

function stopWorkflow() {
  abortController.value?.abort()
  loading.value = false
  reportStreaming.value = false
  stageText.value = '已停止'
}

function handleStreamEvent(event) {
  if (!event?.type) return
  if (event.workflowName) workflow.value.workflowName = event.workflowName
  if (event.targetPosition) {
    workflow.value.targetPosition = event.targetPosition
    targetPosition.value = event.targetPosition
  }
  if (event.conversationId) workflow.value.conversationId = event.conversationId

  if (event.type === 'workflow_start') {
    stageText.value = event.message || '工作流已启动'
    return
  }

  if (event.type === 'step_start') {
    upsertStep({
      agentName: event.agentName,
      status: 'running',
      summary: event.message,
      toolCalls: []
    })
    stageText.value = event.message || `${event.agentName} 正在执行`
    return
  }

  if (event.type === 'step_complete' && event.step) {
    upsertStep(event.step)
    stageText.value = event.message || `${event.agentName} 已完成`
    return
  }

  if (event.type === 'report_start') {
    reportStreaming.value = true
    stageText.value = event.message || '开始生成最终报告'
    return
  }

  if (event.type === 'report_delta') {
    workflow.value.finalReport += event.content || ''
    stageText.value = '最终报告生成中...'
    nextTick()
    return
  }

  if (event.type === 'workflow_done') {
    reportStreaming.value = false
    stageText.value = event.message || '工作流执行完成'
    return
  }

  if (event.type === 'error') {
    loading.value = false
    reportStreaming.value = false
    stageText.value = event.message || '工作流执行失败'
    ElMessage.error(stageText.value)
  }
}

function upsertStep(step) {
  const index = workflow.value.steps.findIndex(item => item.agentName === step.agentName)
  if (index >= 0) {
    workflow.value.steps[index] = step
  } else {
    workflow.value.steps.push(step)
  }
}

async function readNdjsonStream(stream, onEvent) {
  const reader = stream.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { value, done } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''
    for (const line of lines) {
      parseEventLine(line, onEvent)
    }
  }

  if (buffer.trim()) {
    parseEventLine(buffer, onEvent)
  }
}

function parseEventLine(line, onEvent) {
  const text = line.trim()
  if (!text) return
  try {
    onEvent(JSON.parse(text))
  } catch (error) {
    console.warn('Invalid workflow stream event:', text, error)
  }
}

function statusType(status) {
  if (status === 'success') return 'success'
  if (status === 'running') return 'primary'
  if (status === 'partial') return 'warning'
  return 'info'
}

function statusText(status) {
  if (status === 'success') return '成功'
  if (status === 'running') return '执行中'
  if (status === 'partial') return '部分成功'
  return '未开始'
}

function normalizeMarkdown(value) {
  return (value || '')
    .replace(/^data:\s*/gm, '')
    .replace(/\r\n/g, '\n')
    .replace(/([^\n])\s*(#{2,6}\s*)/g, '$1\n\n$2')
    .replace(/(^|\n)(#{2,6})([^\s#])/g, '$1$2 $3')
    .replace(/([。！？；:：])\s*[-*]\s+/g, '$1\n\n- ')
    .replace(/([。！？；:：])\s*(\d+\.\s+)/g, '$1\n\n$2')
    .replace(/([^\n])\n(#{2,6}\s+)/g, '$1\n\n$2')
    .replace(/(#{2,6}\s+[^\n]+)\n(?!\n)/g, '$1\n\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

function renderMarkdown(value) {
  return marked.parse(normalizeMarkdown(value))
}
</script>

<style scoped>
.workflow-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.workflow-header,
.control-panel,
.steps-panel,
.report-panel,
.empty-panel {
  background: #fff;
  border: 1px solid #e8ecf3;
  border-radius: 8px;
}

.workflow-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 28px;
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.eyebrow {
  color: #3a36db;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 8px;
}

.workflow-header h1 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #111827;
}

.workflow-header p {
  margin: 0;
  color: #667085;
  font-size: 14px;
  line-height: 1.7;
}

.control-panel {
  padding: 18px 22px;
}

.target-display {
  display: grid;
  gap: 6px;
  margin-bottom: 12px;
}

.target-display span,
.target-display small {
  color: #667085;
  font-size: 12px;
}

.target-display strong {
  color: #111827;
  font-size: 18px;
}

.control-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  color: #667085;
  font-size: 12px;
}

.workflow-grid {
  display: grid;
  grid-template-columns: minmax(320px, 420px) minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.steps-panel,
.report-panel,
.empty-panel {
  padding: 20px;
}

.panel-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 16px;
}

.step-item {
  display: grid;
  grid-template-columns: 32px minmax(0, 1fr);
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #edf0f5;
}

.step-item:last-child {
  border-bottom: none;
}

.step-index {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e8efff;
  color: #2f5bea;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.step-index.success {
  background: #e7f7ed;
  color: #159947;
}

.step-index.running {
  background: #eef2ff;
  color: #3a36db;
}

.step-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-weight: 700;
  color: #111827;
}

.step-body p {
  margin: 8px 0;
  color: #4b5563;
  font-size: 13px;
  line-height: 1.7;
}

.tool-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.report-panel {
  min-height: 360px;
}

.report-placeholder {
  color: #667085;
  background: #f8fafc;
  border: 1px dashed #d8deea;
  border-radius: 8px;
  padding: 18px;
  line-height: 1.7;
}

.report-content {
  color: #1f2937;
  line-height: 1.85;
  font-size: 14px;
  overflow-wrap: anywhere;
}

.report-content :deep(h2) {
  margin: 18px 0 10px;
  color: #111827;
  font-size: 20px;
}

.report-content :deep(h3) {
  margin: 16px 0 8px;
  color: #111827;
  font-size: 16px;
}

.report-content :deep(p) {
  margin: 8px 0;
}

.report-content :deep(ul),
.report-content :deep(ol) {
  padding-left: 22px;
  margin: 8px 0 14px;
}

.report-content :deep(li) {
  margin: 6px 0;
}

@media (max-width: 980px) {
  .workflow-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .workflow-grid {
    grid-template-columns: 1fr;
  }
}
</style>
