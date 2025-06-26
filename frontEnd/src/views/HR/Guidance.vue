<template>
  <div class="guidance-page">
    <!-- 顶部标题与标签页 -->
    <div class="top-section">
      <div class="page-title">求职指导请求</div>
      <el-tabs v-model="activeTab" class="status-tabs" @tab-change="handleTabChange">
        <el-tab-pane :label="`待处理 (${pendingCount})`" name="pending" />
        <el-tab-pane :label="`指导中 (${processingCount})`" name="processing" />
        <el-tab-pane label="已完成" name="completed" />
      </el-tabs>
    </div>

    <!-- 搜索栏 -->
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索求职者姓名或职位"
        class="search-input"
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 指导请求列表 -->
    <div class="guidance-list" v-loading="loading" element-loading-text="正在加载请求数据...">
      <div v-if="!loading && currentRequests.length === 0" class="empty-state">
        <el-empty :description="`暂无${getTabText(activeTab)}的请求`" />
      </div>

      <el-card
        v-for="request in currentRequests"
        :key="request.id"
        class="guidance-card"
        shadow="hover"
      >
        <div class="card-header">
          <div class="user-info">
            <el-avatar :src="request.avatar" :size="60" />
            <div class="user-details">
              <h3 class="user-name">{{ request.userName }}</h3>
              <div class="job-intention">
                <span class="label">求职意向：</span>
                <span class="value">{{ request.targetPosition }}</span>
              </div>
              <div class="guidance-need">
                <span class="label">指导需求：</span>
                <span class="value">{{ request.guidanceType }}</span>
              </div>
              <div class="apply-time">
                <span class="label">申请时间：</span>
                <span class="value">{{ formatDate(request.requestTime) }}</span>
              </div>
            </div>
          </div>

          <div class="action-section">
            <el-button
              type="primary"
              size="large"
              @click="viewAndHandle(request)"
              class="handle-btn"
            >
              查看并处理
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="currentRequests.length > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20]"
        :total="filteredRequests.length"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 请求详情处理抽屉 -->
    <el-drawer
      v-model="showDetailDrawer"
      title="处理求职指导请求"
      :size="'80%'"
      direction="rtl"
      :before-close="handleCloseDrawer"
    >
      <div class="detail-container" v-if="selectedRequest">
        <div class="detail-content">
          <!-- 左侧：求职者信息区 -->
          <div class="left-section">
            <div class="section-title">
              <el-icon><User /></el-icon>
              求职者信息
            </div>

            <!-- 基本信息 -->
            <div class="user-profile">
              <el-avatar :src="selectedRequest.avatar" :size="80" />
              <div class="profile-info">
                <h2>{{ selectedRequest.userName }}</h2>
                <p class="target-position">{{ selectedRequest.targetPosition }}</p>
                <div class="contact-info">
                  <p><el-icon><Phone /></el-icon> {{ selectedRequest.phone || '138****8888' }}</p>
                  <p><el-icon><Message /></el-icon> {{ selectedRequest.email || 'user@example.com' }}</p>
                </div>
              </div>
            </div>

            <!-- 指导需求详情 -->
            <div class="guidance-request-detail">
              <h3>指导需求详情</h3>
              <div class="request-info">
                <p><strong>指导类型：</strong>{{ selectedRequest.guidanceType }}</p>
                <p><strong>申请时间：</strong>{{ formatDate(selectedRequest.requestTime) }}</p>
                <p><strong>详细描述：</strong></p>
                <div class="description-content">
                  {{ selectedRequest.detailedDescription }}
                </div>
              </div>
            </div>

            <!-- 简历信息 -->
            <div class="resume-section">
              <h3>简历信息</h3>
              <div class="resume-summary">
                <p><strong>工作经验：</strong>{{ selectedRequest.experience || '3年' }}</p>
                <p><strong>教育背景：</strong>{{ selectedRequest.education || '本科' }}</p>
                <p><strong>核心技能：</strong></p>
                <div class="skills-tags">
                  <el-tag
                    v-for="skill in (selectedRequest.skills || ['Vue.js', 'JavaScript', 'Node.js'])"
                    :key="skill"
                    size="small"
                    type="info"
                  >
                    {{ skill }}
                  </el-tag>
                </div>
              </div>
              <el-button type="text" @click="viewFullResume">查看完整简历</el-button>
            </div>
          </div>

          <!-- 右侧：操作与决策区 -->
          <div class="right-section">
            <div class="section-title">
              <el-icon><Tools /></el-icon>
              处理请求
            </div>

            <div class="action-area">
              <div class="action-buttons">
                <el-button
                  type="primary"
                  size="large"
                  @click="acceptRequest"
                  :loading="processing"
                  class="accept-btn"
                >
                  <el-icon><Check /></el-icon>
                  接受指导
                </el-button>

                <el-button
                  size="large"
                  @click="showRejectDialog = true"
                  class="reject-btn"
                >
                  <el-icon><Close /></el-icon>
                  婉拒请求
                </el-button>
              </div>

              <div class="tips-info">
                <el-alert
                  title="提示信息"
                  type="info"
                  :closable="false"
                  show-icon
                >
                  <p>接受后将与该求职者建立联系，您可以开始提供指导服务。</p>
                  <p>服务完成后平台将为您结算收益。</p>
                </el-alert>
              </div>

              <!-- 如果是处理中状态，显示完成按钮 -->
              <div v-if="selectedRequest.status === 'processing'" class="complete-section">
                <el-divider />
                <h4>完成指导</h4>
                <el-input
                  v-model="completionNote"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入指导总结或备注（可选）"
                  class="completion-input"
                />
                <el-button
                  type="success"
                  size="large"
                  @click="completeGuidance"
                  class="complete-btn"
                >
                  <el-icon><CircleCheck /></el-icon>
                  完成指导
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 拒绝请求对话框 -->
    <el-dialog
      v-model="showRejectDialog"
      title="婉拒请求"
      width="500px"
      :before-close="handleCloseRejectDialog"
    >
      <div class="reject-content">
        <p>请选择拒绝原因或自定义输入：</p>
        <el-radio-group v-model="rejectReason" class="reject-reasons">
          <el-radio label="时间安排冲突">时间安排冲突</el-radio>
          <el-radio label="专业领域不匹配">专业领域不匹配</el-radio>
          <el-radio label="当前指导名额已满">当前指导名额已满</el-radio>
          <el-radio label="custom">自定义原因</el-radio>
        </el-radio-group>

        <el-input
          v-if="rejectReason === 'custom'"
          v-model="customRejectReason"
          type="textarea"
          :rows="3"
          placeholder="请输入拒绝原因"
          class="custom-reason-input"
        />
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRejectDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmReject">确认拒绝</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search,
  User,
  Phone,
  Message,
  Tools,
  Check,
  Close,
  CircleCheck
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 响应式数据
const activeTab = ref('pending')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const showDetailDrawer = ref(false)
const selectedRequest = ref(null)
const processing = ref(false)
const showRejectDialog = ref(false)
const rejectReason = ref('')
const customRejectReason = ref('')
const completionNote = ref('')

// 模拟数据
const guidanceRequests = ref([
  {
    id: 1,
    userName: '李小明',
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=li',
    targetPosition: '前端开发工程师',
    status: 'pending',
    requestTime: '2024-01-15T14:30:00',
    guidanceType: '简历优化',
    detailedDescription: '希望能够对我的简历进行优化，特别是项目经历部分，感觉描述不够突出。我是一名有3年工作经验的前端开发，主要使用Vue.js和React，但是投递简历后回复率不高，希望能得到专业的指导建议。',
    phone: '138****1234',
    email: 'lixiaoming@example.com',
    experience: '3年经验',
    education: '本科',
    skills: ['Vue.js', 'React', 'JavaScript', 'TypeScript', 'Node.js']
  },
  {
    id: 2,
    userName: '王小红',
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=wang',
    targetPosition: '产品经理',
    status: 'processing',
    requestTime: '2024-01-14T16:20:00',
    guidanceType: '转行指导',
    detailedDescription: '我目前是一名UI设计师，工作2年了，想要转行做产品经理。虽然在工作中有接触过产品相关的内容，但是没有正式的产品经理经验，希望能指导如何包装简历，突出我的产品思维和相关能力。',
    phone: '139****5678',
    email: 'wangxiaohong@example.com',
    experience: '2年经验',
    education: '本科',
    skills: ['UI设计', 'Figma', 'Axure', '用户研究', '数据分析']
  },
  {
    id: 3,
    userName: '张三',
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=zhang',
    targetPosition: 'Java开发工程师',
    status: 'completed',
    requestTime: '2024-01-13T10:15:00',
    guidanceType: '面试技巧',
    detailedDescription: '简历投递后能收到面试邀请，但是面试通过率很低。希望能得到面试技巧方面的指导，特别是技术面试和HR面试的注意事项。',
    phone: '137****9012',
    email: 'zhangsan@example.com',
    experience: '5年经验',
    education: '硕士',
    skills: ['Java', 'Spring Boot', 'MySQL', 'Redis', 'Docker']
  },
  {
    id: 4,
    userName: '刘小美',
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=liu',
    targetPosition: '数据分析师',
    status: 'pending',
    requestTime: '2024-01-16T09:45:00',
    guidanceType: '职业规划',
    detailedDescription: '刚毕业的应届生，学的是统计学专业，对数据分析很感兴趣，但是不知道如何规划职业发展路径，希望能得到专业的建议。',
    phone: '136****3456',
    email: 'liuxiaomei@example.com',
    experience: '应届毕业生',
    education: '本科',
    skills: ['Python', 'SQL', 'Excel', 'SPSS', 'Tableau']
  }
])

// 计算属性
const pendingCount = computed(() =>
  guidanceRequests.value.filter(req => req.status === 'pending').length
)

const processingCount = computed(() =>
  guidanceRequests.value.filter(req => req.status === 'processing').length
)

const filteredRequests = computed(() => {
  let result = guidanceRequests.value.filter(req => req.status === activeTab.value)

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(req =>
      req.userName.toLowerCase().includes(keyword) ||
      req.targetPosition.toLowerCase().includes(keyword) ||
      req.guidanceType.toLowerCase().includes(keyword)
    )
  }

  return result
})

const currentRequests = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredRequests.value.slice(start, end)
})

// 方法
const handleTabChange = (tabName) => {
  activeTab.value = tabName
  currentPage.value = 1
}

const handleSearch = () => {
  currentPage.value = 1
}

const getTabText = (tab) => {
  const textMap = {
    'pending': '待处理',
    'processing': '指导中',
    'completed': '已完成'
  }
  return textMap[tab] || ''
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const viewAndHandle = (request) => {
  selectedRequest.value = request
  showDetailDrawer.value = true
}

const handleCloseDrawer = () => {
  showDetailDrawer.value = false
  selectedRequest.value = null
  completionNote.value = ''
}

const acceptRequest = async () => {
  try {
    processing.value = true
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    selectedRequest.value.status = 'processing'
    ElMessage.success('已接受指导请求，可以开始与求职者沟通了')
    handleCloseDrawer()
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    processing.value = false
  }
}

const confirmReject = async () => {
  const reason = rejectReason.value === 'custom' ? customRejectReason.value : rejectReason.value
  if (!reason) {
    ElMessage.warning('请选择或输入拒绝原因')
    return
  }

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))

    // 从列表中移除该请求
    const index = guidanceRequests.value.findIndex(req => req.id === selectedRequest.value.id)
    if (index > -1) {
      guidanceRequests.value.splice(index, 1)
    }

    ElMessage.success('已婉拒该指导请求')
    handleCloseDrawer()
    handleCloseRejectDialog()
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试')
  }
}

const handleCloseRejectDialog = () => {
  showRejectDialog.value = false
  rejectReason.value = ''
  customRejectReason.value = ''
}

const completeGuidance = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))

    selectedRequest.value.status = 'completed'
    ElMessage.success('指导已完成')
    handleCloseDrawer()
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试')
  }
}

const viewFullResume = () => {
  ElMessage.info('跳转到完整简历页面...')
  // 这里可以跳转到简历详情页面
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}
</script>

<style scoped>
.guidance-page {
  box-sizing: border-box;
  padding: 0 24px 16px 16px;
  min-height: calc(100vh - 16px);
  background: #f5f7fa;
  max-width: 1400px;
}

.top-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #3a36db;
  margin-bottom: 16px;
}

.status-tabs {
  margin-top: 16px;
}

.status-tabs .el-tabs__header {
  margin: 0;
}

.status-tabs .el-tabs__item {
  font-size: 16px;
  font-weight: 500;
}

.status-tabs .el-tabs__item.is-active {
  color: #3a36db;
  font-weight: 600;
}

.search-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
}

.search-input {
  max-width: 400px;
}

.guidance-list {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
  min-height: 400px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.guidance-card {
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  margin-bottom: 16px;
}

.guidance-card:hover {
  border-color: #3a36db;
  box-shadow: 0 4px 16px 0 rgba(58,54,219,0.1);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.user-info {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.job-intention,
.guidance-need,
.apply-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.label {
  color: #666;
  font-weight: 500;
  min-width: 80px;
}

.value {
  color: #333;
}

.action-section {
  flex-shrink: 0;
}

.handle-btn {
  background: #3a36db;
  border-color: #3a36db;
  font-weight: 500;
  padding: 0 24px;
  height: 40px;
}

.handle-btn:hover {
  background: #2d29b8;
  border-color: #2d29b8;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 抽屉样式 */
.detail-container {
  height: 100%;
  overflow-y: auto;
}

.detail-content {
  display: flex;
  gap: 24px;
  height: 100%;
}

.left-section {
  flex: 2;
  padding-right: 24px;
  border-right: 1px solid #f0f0f0;
}

.right-section {
  flex: 1;
  padding-left: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #3a36db;
  margin-bottom: 20px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0ff;
}

.user-profile {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.profile-info h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.target-position {
  color: #3a36db;
  font-weight: 500;
  margin-bottom: 12px;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contact-info p {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
  margin: 0;
}

.guidance-request-detail,
.resume-section {
  margin-bottom: 24px;
}

.guidance-request-detail h3,
.resume-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.request-info p {
  margin-bottom: 8px;
  color: #666;
}

.description-content {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  line-height: 1.6;
  color: #333;
  margin-top: 8px;
}

.resume-summary p {
  margin-bottom: 8px;
  color: #666;
}

.skills-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.action-area {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.accept-btn {
  background: #3a36db;
  border-color: #3a36db;
  font-weight: 500;
}

.accept-btn:hover {
  background: #2d29b8;
  border-color: #2d29b8;
}

.reject-btn {
  color: #666;
  border-color: #ddd;
}

.reject-btn:hover {
  color: #333;
  border-color: #999;
}

.complete-section {
  margin-top: 20px;
}

.complete-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.completion-input {
  margin-bottom: 16px;
}

.complete-btn {
  width: 100%;
}

/* 拒绝对话框样式 */
.reject-content p {
  margin-bottom: 16px;
  color: #666;
}

.reject-reasons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.custom-reason-input {
  margin-top: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .guidance-page {
    padding: 0 12px 12px 12px;
  }

  .top-section {
    padding: 16px;
  }

  .search-section {
    padding: 12px 16px;
  }

  .card-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .user-info {
    width: 100%;
  }

  .action-section {
    width: 100%;
    display: flex;
    justify-content: center;
  }

  .detail-content {
    flex-direction: column;
  }

  .left-section,
  .right-section {
    padding: 0;
    border: none;
  }

  .user-profile {
    flex-direction: column;
    text-align: center;
  }
}
</style>
