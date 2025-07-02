<template>
  <div class="teachvip-container">
    <div v-if="loading" class="loading">加载中...</div>
    <template v-else>
      <div v-if="seekerInfo.membership === 0" class="not-vip">
        <el-alert title="请先升级为高级会员后使用本功能" type="warning" show-icon class="vip-alert" />
        <el-button type="warning" size="large" class="go-vip-btn" @click="goToVip">去升级会员</el-button>
      </div>
      <div v-else class="vip-feature">
        <!-- 页面头部 -->
        <div class="page-header">
          <div class="header-content">
            <h1 class="page-title">
              <el-icon><Star /></el-icon>
              高级会员求职指导服务
            </h1>
            <p class="page-subtitle">专业HR一对一指导，助力您的职业发展</p>
          </div>
        </div>

        <!-- 服务介绍 -->
        <el-card class="service-intro" shadow="never">
          <div class="intro-content">
            <h3>
              <el-icon><Compass /></el-icon>
              服务特色
            </h3>
            <div class="service-features">
              <div class="feature-item">
                <el-icon class="feature-icon"><User /></el-icon>
                <div class="feature-text">
                  <h4>专业HR指导</h4>
                  <p>资深HR一对一个性化指导</p>
                </div>
              </div>
              <div class="feature-item">
                <el-icon class="feature-icon"><Document /></el-icon>
                <div class="feature-text">
                  <h4>简历优化</h4>
                  <p>专业简历优化建议</p>
                </div>
              </div>
              <div class="feature-item">
                <el-icon class="feature-icon"><ChatDotRound /></el-icon>
                <div class="feature-text">
                  <h4>面试技巧</h4>
                  <p>面试技巧与经验分享</p>
                </div>
              </div>
              <div class="feature-item">
                <el-icon class="feature-icon"><TrendCharts /></el-icon>
                <div class="feature-text">
                  <h4>职业规划</h4>
                  <p>个人职业发展路径规划</p>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 指导请求表单 -->
        <el-card class="request-form-card" shadow="never">
          <template #header>
            <div class="card-header">
              <h3>
                <el-icon><Edit /></el-icon>
                申请求职指导
              </h3>
              <p>请填写以下信息，我们会为您匹配合适的HR顾问</p>
            </div>
          </template>

          <el-form
            ref="requestFormRef"
            :model="requestForm"
            :rules="formRules"
            label-width="120px"
            class="request-form"
          >
            <el-form-item label="目标职位" prop="targetPosition">
              <el-input
                v-model="requestForm.targetPosition"
                placeholder="请输入您的目标职位，如：前端开发工程师"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="指导类型" prop="guidanceType">
              <el-select
                v-model="requestForm.guidanceType"
                placeholder="请选择您需要的指导类型"
                style="width: 100%"
              >
                <el-option label="简历优化" value="简历优化" />
                <el-option label="面试技巧" value="面试技巧" />
                <el-option label="职业规划" value="职业规划" />
                <el-option label="转行指导" value="转行指导" />
                <el-option label="薪资谈判" value="薪资谈判" />
                <el-option label="行业分析" value="行业分析" />
              </el-select>
            </el-form-item>

            <el-form-item label="联系电话" prop="phone">
              <el-input
                v-model="requestForm.phone"
                placeholder="请输入您的联系电话"
                maxlength="11"
              />
            </el-form-item>

            <el-form-item label="邮箱地址" prop="email">
              <el-input
                v-model="requestForm.email"
                placeholder="请输入您的邮箱地址"
                maxlength="50"
              />
            </el-form-item>

            <el-form-item label="详细需求" prop="detailedDescription">
              <el-input
                v-model="requestForm.detailedDescription"
                type="textarea"
                :rows="6"
                placeholder="请详细描述您的指导需求，包括目前遇到的问题、期望得到的帮助等。详细的描述有助于我们为您匹配更合适的HR顾问。"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="工作经验" prop="experience">
              <el-select
                v-model="requestForm.experience"
                placeholder="请选择您的工作经验"
                style="width: 100%"
              >
                <el-option label="应届毕业生" value="应届毕业生" />
                <el-option label="1年以下" value="1年以下" />
                <el-option label="1-3年" value="1-3年" />
                <el-option label="3-5年" value="3-5年" />
                <el-option label="5-10年" value="5-10年" />
                <el-option label="10年以上" value="10年以上" />
              </el-select>
            </el-form-item>

            <el-form-item label="教育背景" prop="education">
              <el-select
                v-model="requestForm.education"
                placeholder="请选择您的教育背景"
                style="width: 100%"
              >
                <el-option label="高中及以下" value="高中及以下" />
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>

            <el-form-item label="核心技能">
              <el-input
                v-model="skillInput"
                placeholder="输入技能后按回车添加，如：Vue.js, Python, 数据分析等"
                @keyup.enter="addSkill"
              />
              <div class="skills-display" v-if="requestForm.skills.length > 0">
                <el-tag
                  v-for="(skill, index) in requestForm.skills"
                  :key="index"
                  closable
                  @close="removeSkill(index)"
                  type="info"
                  class="skill-tag"
                >
                  {{ skill }}
                </el-tag>
              </div>
            </el-form-item>

            <el-form-item>
              <div class="form-actions">
                <el-button
                  type="primary"
                  size="large"
                  @click="submitRequest"
                  :loading="submitting"
                  class="submit-btn"
                >
                  <el-icon><Check /></el-icon>
                  提交申请
                </el-button>
                <el-button
                  size="large"
                  @click="resetForm"
                  class="reset-btn"
                >
                  重置表单
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 申请须知 -->
        <el-card class="notice-card" shadow="never">
          <template #header>
            <h3>
              <el-icon><InfoFilled /></el-icon>
              申请须知
            </h3>
          </template>
          <div class="notice-content">
            <ul>
              <li>提交申请后，我们会在1-2个工作日内为您匹配合适的HR顾问</li>
              <li>请确保联系方式准确，以便顾问能够及时与您联系</li>
              <li>详细的需求描述有助于我们提供更精准的指导服务</li>
              <li>每位高级会员每月可申请3次免费指导服务</li>
              <li>指导服务通过平台内聊天系统进行，保护您的隐私安全</li>
            </ul>
          </div>
        </el-card>

        <!-- 历史指导记录 -->
        <el-card class="history-card" shadow="never">
          <template #header>
            <div class="history-header">
              <h3>
                <el-icon><Clock /></el-icon>
                历史指导记录
              </h3>
              <el-button 
                type="link" 
                @click="refreshHistory"
                :loading="historyLoading"
                class="refresh-btn"
              >
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>

          <div v-loading="historyLoading" element-loading-text="正在加载历史记录...">
            <div v-if="!historyLoading && historyList.length === 0" class="empty-history">
              <el-empty description="暂无历史指导记录">
                <el-button type="primary" @click="scrollToForm">立即申请指导</el-button>
              </el-empty>
            </div>

            <div v-else class="history-list">
              <div 
                v-for="(record, index) in historyList" 
                :key="record.id"
                class="history-item"
                :class="{ 'history-item-last': index === historyList.length - 1 }"
              >
                <div class="history-timeline">
                  <div class="timeline-dot" :class="getStatusClass(record.status)"></div>
                  <div v-if="index !== historyList.length - 1" class="timeline-line"></div>
                </div>

                <div class="history-content">
                  <div class="history-main">
                    <div class="history-title">
                      <h4>{{ record.guidanceType }} - {{ record.targetPosition }}</h4>
                      <el-tag 
                        :type="getStatusTagType(record.status)" 
                        size="small"
                        class="status-tag"
                      >
                        {{ getStatusText(record.status) }}
                      </el-tag>
                    </div>

                    <div class="history-meta">
                      <span class="meta-item">
                        <el-icon><Calendar /></el-icon>
                        申请时间：{{ formatDate(record.requestTime) }}
                      </span>
                      <span v-if="record.hrName" class="meta-item">
                        <el-icon><User /></el-icon>
                        指导顾问：{{ record.hrName }}
                      </span>
                    </div>

                    <div class="history-description">
                      {{ record.detailedDescription }}
                    </div>

                    <div class="history-actions">
                      <el-button 
                        type="link" 
                        size="small"
                        @click="viewDetails(record)"
                      >
                        查看详情
                      </el-button>
                      <el-button 
                        v-if="record.status === 'processing'"
                        type="link" 
                        size="small"
                        @click="contactHR(record)"
                      >
                        联系顾问
                      </el-button>
                      <el-button 
                        v-if="record.status === 'completed' && record.feedback"
                        type="link" 
                        size="small"
                        @click="viewFeedback(record)"
                      >
                        查看反馈
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div v-if="historyList.length > 0" class="history-pagination">
              <el-pagination
                v-model:current-page="historyPage"
                v-model:page-size="historyPageSize"
                :total="historyTotal"
                :page-sizes="[5, 10, 20]"
                layout="total, sizes, prev, pager, next"
                size="small"
                @size-change="handleHistorySizeChange"
                @current-change="handleHistoryPageChange"
              />
            </div>
          </div>
        </el-card>
      </div>
    </template>

    <!-- 详情查看对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="指导记录详情"
      width="600px"
      :before-close="closeDetailDialog"
    >
      <div v-if="selectedRecord" class="detail-dialog-content">
        <div class="detail-section">
          <h4>申请信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="目标职位">{{ selectedRecord.targetPosition }}</el-descriptions-item>
            <el-descriptions-item label="指导类型">{{ selectedRecord.guidanceType }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ formatDate(selectedRecord.requestTime) }}</el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getStatusTagType(selectedRecord.status)">
                {{ getStatusText(selectedRecord.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="工作经验" span="2">{{ selectedRecord.experience }}</el-descriptions-item>
            <el-descriptions-item label="教育背景" span="2">{{ selectedRecord.education }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h4>需求描述</h4>
          <div class="description-box">
            {{ selectedRecord.detailedDescription }}
          </div>
        </div>

        <div v-if="selectedRecord.skills && selectedRecord.skills.length > 0" class="detail-section">
          <h4>核心技能</h4>
          <div class="skills-display">
            <el-tag
              v-for="skill in selectedRecord.skills"
              :key="skill"
              type="info"
              size="small"
              class="skill-tag"
            >
              {{ skill }}
            </el-tag>
          </div>
        </div>

        <div v-if="selectedRecord.hrName" class="detail-section">
          <h4>指导顾问</h4>
          <div class="hr-info">
            <el-avatar :src="selectedRecord.hrAvatar" size="small" />
            <span>{{ selectedRecord.hrName }}</span>
          </div>
        </div>

        <div v-if="selectedRecord.feedback" class="detail-section">
          <h4>指导反馈</h4>
          <div class="feedback-box">
            {{ selectedRecord.feedback }}
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeDetailDialog">关闭</el-button>
          <el-button 
            v-if="selectedRecord && selectedRecord.status === 'processing'"
            type="primary"
            @click="contactHR(selectedRecord)"
          >
            联系顾问
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSeekerByUserId } from '../../api/seeker'
import { useAuthStore } from '../../stores/authStore'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Star,
  Compass,
  User,
  Document,
  ChatDotRound,
  TrendCharts,
  Edit,
  Check,
  InfoFilled,
  Clock,
  Refresh,
  Calendar
} from '@element-plus/icons-vue'

const seekerInfo = ref({ membership: 0 })
const loading = ref(true)
const submitting = ref(false)
const authStore = useAuthStore()
const router = useRouter()
const requestFormRef = ref(null)
const skillInput = ref('')

// 历史记录相关数据
const historyList = ref([])
const historyLoading = ref(false)
const historyPage = ref(1)
const historyPageSize = ref(10)
const historyTotal = ref(0)
const showDetailDialog = ref(false)
const selectedRecord = ref(null)
const historyLoaded = ref(false) // 标记历史记录是否已加载

// 表单数据
const requestForm = ref({
  targetPosition: '',
  guidanceType: '',
  phone: '',
  email: '',
  detailedDescription: '',
  experience: '',
  education: '',
  skills: []
})

// 表单验证规则
const formRules = {
  targetPosition: [
    { required: true, message: '请输入目标职位', trigger: 'blur' },
    { min: 2, max: 50, message: '职位名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  guidanceType: [
    { required: true, message: '请选择指导类型', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  detailedDescription: [
    { required: true, message: '请详细描述您的指导需求', trigger: 'blur' },
    { min: 20, max: 500, message: '需求描述长度在 20 到 500 个字符', trigger: 'blur' }
  ],
  experience: [
    { required: true, message: '请选择工作经验', trigger: 'change' }
  ],
  education: [
    { required: true, message: '请选择教育背景', trigger: 'change' }
  ]
}

onMounted(async () => {
  const userId = authStore.userId || authStore.user?.id
  if (!userId) {
    loading.value = false
    return
  }
  try {
    const { data } = await getSeekerByUserId(userId)
    seekerInfo.value = data || { membership: 0 }
    
    // 如果是高级会员，并行加载历史记录（不阻塞主界面）
    if (seekerInfo.value.membership > 0) {
      // 先显示界面，历史记录异步加载
      loading.value = false
      loadHistoryRecords()
    } else {
      loading.value = false
    }
  } catch {
    seekerInfo.value = { membership: 0 }
    loading.value = false
  }
})

function goToVip() {
  router.push('/jobseeker/vip')
}

// 添加技能
function addSkill() {
  const skill = skillInput.value.trim()
  if (skill && !requestForm.value.skills.includes(skill)) {
    requestForm.value.skills.push(skill)
    skillInput.value = ''
  }
}

// 移除技能
function removeSkill(index) {
  requestForm.value.skills.splice(index, 1)
}

// 重置表单
function resetForm() {
  requestFormRef.value?.resetFields()
  requestForm.value.skills = []
  skillInput.value = ''
}

// 提交申请
async function submitRequest() {
  try {
    const valid = await requestFormRef.value?.validate()
    if (!valid) return

    submitting.value = true
    
    // 构造请求数据
    const requestData = {
      userId: authStore.userId || authStore.user?.id,
      userName: authStore.user?.username || '用户',
      avatar: authStore.user?.avatar || '',
      targetPosition: requestForm.value.targetPosition,
      guidanceType: requestForm.value.guidanceType,
      phone: requestForm.value.phone,
      email: requestForm.value.email,
      detailedDescription: requestForm.value.detailedDescription,
      experience: requestForm.value.experience,
      education: requestForm.value.education,
      skills: requestForm.value.skills,
      requestTime: new Date().toISOString(),
      status: 'pending'
    }

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('申请提交成功！我们会在1-2个工作日内为您匹配合适的HR顾问')
    resetForm()
    
    // 提交成功后刷新历史记录
    await loadHistoryRecords(true) // 强制重新加载以显示新提交的记录
    
  } catch (error) {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 加载历史指导记录
async function loadHistoryRecords(forceReload = false) {
  // 如果已经加载过且不是强制重新加载，则跳过
  if (historyLoaded.value && !forceReload) {
    return
  }
  
  historyLoading.value = true
  try {
    const userId = authStore.userId || authStore.user?.id
    
    // 模拟API调用获取历史记录（减少延迟）
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 模拟历史记录数据
    const mockHistoryData = [
      {
        id: 1,
        targetPosition: '前端开发工程师',
        guidanceType: '简历优化',
        detailedDescription: '希望能够对我的前端开发简历进行优化，突出技术栈和项目经验，提高面试通过率。',
        phone: '138****8888',
        email: 'user@example.com',
        experience: '3-5年',
        education: '本科',
        skills: ['Vue.js', 'JavaScript', 'CSS3', 'Node.js'],
        requestTime: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
        status: 'completed',
        hrName: '李顾问',
        hrAvatar: '',
        feedback: '您的简历整体结构不错，建议在项目经验部分更详细地描述技术难点和解决方案，同时添加一些量化的成果数据。'
      },
      {
        id: 2,
        targetPosition: 'React开发工程师',
        guidanceType: '面试技巧',
        detailedDescription: '想了解React开发岗位的面试重点和常见技术问题，提升面试表现。',
        phone: '138****8888',
        email: 'user@example.com',
        experience: '3-5年',
        education: '本科',
        skills: ['React', 'TypeScript', 'Redux', 'Webpack'],
        requestTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(),
        status: 'processing',
        hrName: '王顾问',
        hrAvatar: '',
        feedback: null
      },
      {
        id: 3,
        targetPosition: '全栈开发工程师',
        guidanceType: '职业规划',
        detailedDescription: '目前在做前端开发，想转向全栈方向发展，希望得到职业规划建议。',
        phone: '138****8888',
        email: 'user@example.com',
        experience: '3-5年',
        education: '本科',
        skills: ['Vue.js', 'Python', 'MySQL', 'Docker'],
        requestTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
        status: 'pending',
        hrName: null,
        hrAvatar: null,
        feedback: null
      }
    ]
    
    historyList.value = mockHistoryData
    historyTotal.value = mockHistoryData.length
    historyLoaded.value = true // 标记已加载
    
  } catch (error) {
    ElMessage.error('加载历史记录失败')
    historyList.value = []
    historyTotal.value = 0
  } finally {
    historyLoading.value = false
  }
}

// 刷新历史记录
async function refreshHistory() {
  await loadHistoryRecords(true) // 强制重新加载
  ElMessage.success('历史记录已刷新')
}

// 获取状态样式类
function getStatusClass(status) {
  const classMap = {
    'pending': 'status-pending',
    'processing': 'status-processing', 
    'completed': 'status-completed',
    'cancelled': 'status-cancelled'
  }
  return classMap[status] || 'status-pending'
}

// 获取状态标签类型
function getStatusTagType(status) {
  const typeMap = {
    'pending': 'warning',
    'processing': 'primary',
    'completed': 'success',
    'cancelled': 'info'
  }
  return typeMap[status] || 'warning'
}

// 获取状态文本
function getStatusText(status) {
  const textMap = {
    'pending': '等待分配',
    'processing': '指导中', 
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return textMap[status] || '未知状态'
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 查看详情
function viewDetails(record) {
  selectedRecord.value = record
  showDetailDialog.value = true
}

// 关闭详情对话框
function closeDetailDialog() {
  showDetailDialog.value = false
  selectedRecord.value = null
}

// 联系HR顾问
function contactHR(record) {
  if (!record.hrName) {
    ElMessage.warning('顾问尚未分配，请耐心等待')
    return
  }
  
  // 跳转到聊天页面
  router.push({
    path: '/chat',
    query: { 
      type: 'guidance',
      recordId: record.id,
      hrName: record.hrName
    }
  })
}

// 查看反馈
function viewFeedback(record) {
  ElMessage({
    message: record.feedback,
    type: 'info',
    duration: 5000,
    showClose: true
  })
}

// 处理历史记录分页大小变化
function handleHistorySizeChange(size) {
  historyPageSize.value = size
  historyPage.value = 1
  loadHistoryRecords(true) // 重新加载数据
}

// 处理历史记录页码变化
function handleHistoryPageChange(page) {
  historyPage.value = page
  loadHistoryRecords(true) // 重新加载数据
}

// 滚动到表单
function scrollToForm() {
  const formElement = document.querySelector('.request-form-card')
  if (formElement) {
    formElement.scrollIntoView({ behavior: 'smooth' })
  }
}
</script>

<style scoped>
.teachvip-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  background: #f8fafe;
  min-height: 100vh;
}

.loading {
  text-align: center;
  font-size: 18px;
  color: #888;
  margin: 80px 0;
}

.not-vip {
  max-width: 600px;
  margin: 80px auto;
  padding: 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  text-align: center;
}

.vip-alert {
  margin-bottom: 24px;
}

.go-vip-btn {
  margin-top: 16px;
  font-size: 16px;
  font-weight: 600;
  padding: 12px 32px;
}

/* VIP功能样式 */
.vip-feature {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px 32px;
  color: #fff;
  text-align: center;
}

.header-content h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.page-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.service-intro {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.intro-content h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 24px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.service-features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8fafe;
  border-radius: 12px;
  border: 1px solid #e8f2ff;
  transition: all 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  border-color: #667eea;
}

.feature-icon {
  font-size: 32px;
  color: #667eea;
  flex-shrink: 0;
}

.feature-text h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
}

.feature-text p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.request-form-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.card-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.request-form {
  margin-top: 24px;
}

.request-form .el-form-item {
  margin-bottom: 24px;
}

.request-form .el-form-item__label {
  font-weight: 500;
  color: #333;
}

.request-form .el-input__inner,
.request-form .el-textarea__inner,
.request-form .el-select {
  border-radius: 8px;
}

.skills-display {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  margin: 0;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 32px;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 600;
  padding: 0 32px;
  height: 44px;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

.reset-btn {
  color: #666;
  border-color: #ddd;
  font-weight: 500;
  padding: 0 24px;
  height: 44px;
}

.reset-btn:hover {
  color: #333;
  border-color: #999;
}

.notice-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  background: #fffbf0;
  border: 1px solid #ffeaa7;
}

.notice-card .el-card__header {
  background: #fff3cd;
  border-bottom: 1px solid #ffeaa7;
}

.notice-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #856404;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notice-content ul {
  margin: 0;
  padding-left: 20px;
  color: #856404;
}

.notice-content li {
  margin-bottom: 8px;
  line-height: 1.6;
}

/* 历史记录样式 */
.history-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-btn {
  color: #667eea;
  font-size: 14px;
}

.refresh-btn:hover {
  color: #5a6fd8;
}

.empty-history {
  text-align: center;
  padding: 40px 20px;
}

.history-list {
  padding: 20px 0;
}

.history-item {
  display: flex;
  margin-bottom: 24px;
  position: relative;
}

.history-item-last {
  margin-bottom: 0;
}

.history-timeline {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 20px;
  position: relative;
}

.timeline-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #ddd;
  background: #fff;
  z-index: 2;
}

.timeline-dot.status-pending {
  border-color: #e6a23c;
  background: #fdf6ec;
}

.timeline-dot.status-processing {
  border-color: #409eff;
  background: #ecf5ff;
}

.timeline-dot.status-completed {
  border-color: #67c23a;
  background: #f0f9ff;
}

.timeline-dot.status-cancelled {
  border-color: #909399;
  background: #f4f4f5;
}

.timeline-line {
  width: 2px;
  flex: 1;
  background: #eee;
  margin-top: 8px;
  min-height: 40px;
}

.history-content {
  flex: 1;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
}

.history-content:hover {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.1);
}

.history-main {
  width: 100%;
}

.history-title {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.history-title h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex: 1;
}

.status-tag {
  margin-left: 12px;
  flex-shrink: 0;
}

.history-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
}

.meta-item .el-icon {
  font-size: 14px;
}

.history-description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.history-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.history-pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

/* 详情对话框样式 */
.detail-dialog-content {
  max-height: 500px;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.description-box,
.feedback-box {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  line-height: 1.6;
  color: #333;
}

.hr-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.hr-info span {
  font-weight: 500;
  color: #333;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .teachvip-container {
    padding: 16px;
  }

  .page-header {
    padding: 32px 24px;
  }

  .header-content h1 {
    font-size: 24px;
    flex-direction: column;
    gap: 8px;
  }

  .service-features {
    grid-template-columns: 1fr;
  }

  .feature-item {
    padding: 16px;
  }

  .form-actions {
    flex-direction: column;
    align-items: center;
  }

  .submit-btn,
  .reset-btn {
    width: 100%;
    max-width: 300px;
  }

  /* 历史记录移动端适配 */
  .history-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .history-item {
    margin-bottom: 20px;
  }

  .history-timeline {
    margin-right: 16px;
  }

  .timeline-dot {
    width: 10px;
    height: 10px;
  }

  .history-content {
    padding: 16px;
  }

  .history-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .history-title h4 {
    font-size: 15px;
  }

  .history-meta {
    flex-direction: column;
    gap: 8px;
  }

  .meta-item {
    font-size: 13px;
  }

  .history-actions {
    gap: 8px;
  }

  .history-actions .el-button {
    font-size: 13px;
    padding: 4px 8px;
  }

  .detail-dialog-content {
    padding: 0 8px;
  }

  .el-descriptions {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .feature-item {
    flex-direction: column;
    text-align: center;
  }

  .feature-icon {
    font-size: 28px;
  }
}
</style>
