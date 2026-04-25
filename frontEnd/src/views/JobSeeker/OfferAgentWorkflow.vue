<template>
  <div class="workflow-page">
    <section class="workflow-header">
      <div>
        <div class="eyebrow">OfferAgent 工作流</div>
        <h1>{{ config.title }}</h1>
        <p>{{ config.description }}</p>
      </div>
      <el-button type="primary" size="large" :loading="loading" @click="runWorkflow">
        {{ loading ? '执行中' : config.actionText }}
      </el-button>
    </section>

    <section class="control-panel">
      <el-form label-position="top">
        <el-form-item label="目标岗位">
          <el-input v-model="targetPosition" placeholder="例如：AI Agent 实习生" />
        </el-form-item>
      </el-form>
      <div class="control-meta">
        <span>用户 ID：{{ userId || '-' }}</span>
        <span>会话：{{ conversationId }}</span>
      </div>
    </section>

    <section v-if="workflow.steps?.length" class="workflow-grid">
      <div class="steps-panel">
        <div class="panel-heading">Agent 执行步骤</div>
        <div v-for="(step, index) in workflow.steps" :key="step.agentName" class="step-item">
          <div class="step-index">{{ index + 1 }}</div>
          <div class="step-body">
            <div class="step-title">
              <span>{{ step.agentName }}</span>
              <el-tag :type="step.status === 'success' ? 'success' : 'warning'" effect="plain">
                {{ statusText(step.status) }}
              </el-tag>
            </div>
            <p>{{ step.summary }}</p>
            <div class="tool-row">
              <el-tag v-for="tool in step.toolCalls || []" :key="tool.toolName" size="small" effect="plain">
                {{ tool.toolName }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="report-panel">
        <div class="panel-heading">最终报告</div>
        <div class="report-content" v-html="renderMarkdown(workflow.finalReport || '')" />
      </div>
    </section>

    <section v-else class="empty-panel">
      <el-empty :description="config.emptyText" />
    </section>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/authStore'
import {
  runAiAgentSprintWorkflow,
  runMockInterviewWorkflow,
  runResumeOptimizationWorkflow
} from '../../api/offerAgent'

const route = useRoute()
const authStore = useAuthStore()
const userId = computed(() => authStore.userId || localStorage.getItem('userId'))
const targetPosition = ref('AI Agent 实习生')
const workflow = ref({})
const loading = ref(false)

const configs = {
  sprint: {
    title: 'AI Agent 实习冲刺规划',
    description: '读取用户画像、简历和知识库内容，生成岗位匹配、技能差距和 7 天冲刺计划。',
    actionText: '开始冲刺规划',
    emptyText: '点击开始后，系统会依次执行 ProfileAgent、ResumeAgent、KnowledgeAgent、MatchAgent 和 PlannerAgent。',
    run: runAiAgentSprintWorkflow
  },
  resume: {
    title: '简历优化工作流',
    description: '结合目标岗位、简历内容和 RAG 知识库，诊断简历问题并生成项目经历改写建议。',
    actionText: '开始简历优化',
    emptyText: '点击开始后，系统会依次执行 ResumeParserAgent、JDAnalysisAgent、ResumeCoachAgent、RAGAgent 和 RewriteAgent。',
    run: runResumeOptimizationWorkflow
  },
  interview: {
    title: '模拟面试工作流',
    description: '围绕目标岗位生成面试题、评估准备度、检索答题框架并输出追问训练建议。',
    actionText: '开始模拟面试',
    emptyText: '点击开始后，系统会依次执行 ProfileAgent、InterviewQuestionAgent、EvaluatorAgent、KnowledgeAgent 和 CoachAgent。',
    run: runMockInterviewWorkflow
  }
}

const workflowType = computed(() => route.meta.workflowType || 'sprint')
const config = computed(() => configs[workflowType.value] || configs.sprint)
const conversationId = computed(() => `workflow-${workflowType.value}-${userId.value || 'guest'}`)

watch(workflowType, () => {
  workflow.value = {}
})

async function runWorkflow() {
  if (!userId.value || loading.value) return
  loading.value = true
  try {
    workflow.value = await config.value.run(
      Number(userId.value),
      conversationId.value,
      targetPosition.value
    )
    ElMessage.success('工作流执行完成')
  } catch (error) {
    console.error('OfferAgent workflow failed:', error)
    ElMessage.error('工作流执行失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function statusText(status) {
  if (status === 'success') return '成功'
  if (status === 'partial') return '部分成功'
  return '未完成'
}

function renderMarkdown(value) {
  return marked.parse(value || '')
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
}

.steps-panel,
.report-panel,
.empty-panel {
  padding: 20px;
}

.panel-heading {
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

.report-content {
  color: #1f2937;
  line-height: 1.8;
  font-size: 14px;
}

.report-content :deep(h2),
.report-content :deep(h3) {
  margin: 14px 0 10px;
  color: #111827;
}

.report-content :deep(ul),
.report-content :deep(ol) {
  padding-left: 22px;
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
