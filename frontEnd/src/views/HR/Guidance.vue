<template>
  <div class="guidance-page">
    <!-- 顶部标题与标签页 -->
    <div class="top-section">
      <div class="page-title">求职指导请求</div>
      <el-tabs v-model="activeTab" class="status-tabs" @tab-change="handleTabChange">
        <el-tab-pane :label="`待分配 (${pendingCount})`" name="pending" />
        <el-tab-pane :label="`指导中 (${processingCount})`" name="processing" />
        <el-tab-pane :label="`已完成 (${completedCount})`" name="completed" />
      </el-tabs>
    </div>

    <!-- 搜索栏 -->
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索求职意向或指导需求"
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
            <el-avatar :src="userInfoMap[request.userId]?.avatar || request.userAvatar || 'https://api.dicebear.com/7.x/miniavs/svg?seed=' + (userInfoMap[request.userId]?.realname || request.userName)" :size="60" />
            <div class="user-details">
              <h3 class="user-name">{{ userInfoMap[request.userId]?.realname || request.userName }}</h3>
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
              <el-avatar :src="userInfoMap[selectedRequest.userId]?.avatar || selectedRequest.userAvatar || 'https://api.dicebear.com/7.x/miniavs/svg?seed=' + (userInfoMap[selectedRequest.userId]?.realname || selectedRequest.userName)" :size="80" />
              <div class="profile-info">
                <h2>{{ userInfoMap[selectedRequest.userId]?.realname || selectedRequest.userName }}</h2>
                <p class="target-position">{{ selectedRequest.targetPosition }}</p>
                <div class="contact-info">
                  <p><el-icon><Phone /></el-icon> {{ userInfoMap[selectedRequest.userId]?.phone || selectedRequest.phone }}</p>
                  <p><el-icon><Message /></el-icon> {{ userInfoMap[selectedRequest.userId]?.email || selectedRequest.email }}</p>
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
                <p><strong>工作经验：</strong>{{ selectedRequest.experience }}</p>
                <p><strong>教育背景：</strong>{{ selectedRequest.education }}</p>
                <p><strong>核心技能：</strong></p>
                <div class="skills-tags">
                  <el-tag
                    v-for="skill in getSkillsArray(selectedRequest.skills)"
                    :key="skill"
                    size="small"
                    type="info"
                  >
                    {{ skill }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：操作与决策区 -->
          <div class="right-section">
            <!-- 待分配状态 -->
            <div v-if="selectedRequest.status === 'pending'">
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
                    开始指导
                  </el-button>

                  <el-button
                    size="large"
                    @click="handleCloseDrawer"
                    class="reject-btn"
                  >
                    <el-icon><Close /></el-icon>
                    取消
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
              </div>
            </div>

            <!-- 指导中状态 -->
            <div v-else-if="selectedRequest.status === 'processing'">
              <div class="section-title">
                <el-icon><Tools /></el-icon>
                联系求职者
              </div>

              <div class="action-area">
                <div class="action-buttons">
                  <el-button
                    type="primary"
                    size="large"
                    @click="contactJobSeeker"
                    class="contact-btn"
                  >
                    <el-icon><Message /></el-icon>
                    联系求职者
                  </el-button>
                </div>

                <div class="complete-section">
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

            <!-- 已完成状态 -->
            <div v-else-if="selectedRequest.status === 'completed'">
              <div class="section-title">
                <el-icon><Tools /></el-icon>
                求职者评价
              </div>

              <div class="action-area">
                <div class="rating-section">
                  <div v-if="selectedRequest.rating" class="user-rating">
                    <h4>用户评分</h4>
                    <el-rate
                      v-model="selectedRequest.rating"
                      disabled
                      show-score
                      text-color="#ff9900"
                      score-template="{value} 分"
                    />
                  </div>

                  <div v-if="selectedRequest.userComment" class="user-comment">
                    <h4>用户评价</h4>
                    <div class="comment-content">
                      {{ selectedRequest.userComment }}
                    </div>
                  </div>

                  <div v-if="!selectedRequest.rating && !selectedRequest.userComment" class="no-rating">
                    <el-empty description="用户暂未评价" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>


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
import {
  getPendingGuidanceRequests,
  getHRGuidanceList,
  assignHR,
  submitGuidanceFeedback,
  getGuidanceRequestDetail
} from '@/api/guidance'
import { getInfoByUserId } from '@/api/info'
import { hrApi } from '@/api/hr'

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
const completionNote = ref('')

// 数据存储
const pendingRequests = ref([])
const processingRequests = ref([])
const completedRequests = ref([])

// 用户信息缓存（userId -> t_info）
const userInfoMap = ref({})

// 获取当前HR用户ID（从localStorage获取）
const getCurrentHRId = () => {
  const userId = localStorage.getItem('userId')
  return userId ? parseInt(userId) : 1 // 默认值，实际应该从登录信息获取
}

// 批量拉取所有请求涉及的userId的t_info
const fetchUserInfos = async (requests) => {
  const userIds = Array.from(new Set(requests.map(r => r.userId)))
  const promises = userIds.map(async (uid) => {
    if (!userInfoMap.value[uid]) {
      try {
        const res = await getInfoByUserId(uid)
        if (res.data) userInfoMap.value[uid] = res.data
      } catch {}
    }
  })
  await Promise.all(promises)
}

// 计算属性
const pendingCount = computed(() => pendingRequests.value.length)
const processingCount = computed(() => processingRequests.value.length)
const completedCount = computed(() => completedRequests.value.length)

const currentRequestList = computed(() => {
  switch (activeTab.value) {
    case 'pending':
      return pendingRequests.value
    case 'processing':
      return processingRequests.value
    case 'completed':
      return completedRequests.value
    default:
      return []
  }
})

const filteredRequests = computed(() => {
  let result = currentRequestList.value

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

// 数据获取方法
const loadPendingRequests = async () => {
  try {
    loading.value = true
    const response = await getPendingGuidanceRequests()
    if (response.data.success) {
      pendingRequests.value = response.data.data || []
      // 获取用户信息
      await fetchUserInfos(pendingRequests.value)
    } else {
      ElMessage.error(response.data.message || '获取待分配请求失败')
    }
  } catch (error) {
    console.error('获取待分配请求失败:', error)
    ElMessage.error('获取待分配请求失败')
  } finally {
    loading.value = false
  }
}

const loadHRGuidanceList = async () => {
  try {
    loading.value = true
    const hrId = getCurrentHRId()
    const response = await getHRGuidanceList(hrId)
    if (response.data.success) {
      const allRequests = response.data.data || []
      processingRequests.value = allRequests.filter(req => req.status === 'processing')
      completedRequests.value = allRequests.filter(req => req.status === 'completed')
      // 获取用户信息
      await fetchUserInfos(allRequests)
    } else {
      ElMessage.error(response.data.message || '获取指导列表失败')
    }
  } catch (error) {
    console.error('获取指导列表失败:', error)
    ElMessage.error('获取指导列表失败')
  } finally {
    loading.value = false
  }
}

const loadAllData = async () => {
  await Promise.all([
    loadPendingRequests(),
    loadHRGuidanceList()
  ])
}

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
    'pending': '待分配',
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

const getSkillsArray = (skills) => {
  if (!skills) return []
  if (Array.isArray(skills)) return skills
  try {
    return JSON.parse(skills)
  } catch {
    return skills.split(',').map(s => s.trim())
  }
}

const viewAndHandle = async (request) => {
  try {
    loading.value = true
    // 获取详细信息
    const response = await getGuidanceRequestDetail(request.id)
    if (response.data.success) {
      selectedRequest.value = response.data.data
      showDetailDrawer.value = true
    } else {
      ElMessage.error(response.data.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

const handleCloseDrawer = () => {
  showDetailDrawer.value = false
  selectedRequest.value = null
  completionNote.value = ''
}

const acceptRequest = async () => {
  try {
    processing.value = true
    const hrId = getCurrentHRId()
    const response = await assignHR(selectedRequest.value.id, hrId)

    if (response.data.success) {
      ElMessage.success('已接受指导请求，可以开始与求职者沟通了')
      // 更新本地数据
      const requestIndex = pendingRequests.value.findIndex(req => req.id === selectedRequest.value.id)
      if (requestIndex > -1) {
        const updatedRequest = { ...selectedRequest.value, status: 'processing', hrUserId: hrId }
        pendingRequests.value.splice(requestIndex, 1)
        processingRequests.value.push(updatedRequest)
      }
      handleCloseDrawer()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    console.error('接受请求失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    processing.value = false
  }
}



const completeGuidance = async () => {
  try {
    const feedback = completionNote.value || '指导已完成'
    const response = await submitGuidanceFeedback(selectedRequest.value.id, feedback)

    if (response.data.success) {
      ElMessage.success('指导已完成')
      // 更新本地数据
      const requestIndex = processingRequests.value.findIndex(req => req.id === selectedRequest.value.id)
      if (requestIndex > -1) {
        const updatedRequest = { ...selectedRequest.value, status: 'completed', feedback }
        processingRequests.value.splice(requestIndex, 1)
        completedRequests.value.push(updatedRequest)
      }
      handleCloseDrawer()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    console.error('完成指导失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const contactJobSeeker = async () => {
  try {
    const hrUserId = getCurrentHRId()
    const targetUserId = selectedRequest.value.userId
    const customMessage = "你好，我已通过你的求职指导请求，现在我们可以开始交流了。"

    // 发送自定义消息
    await hrApi.initiateChat(hrUserId, targetUserId, customMessage)

    ElMessage.success('消息已发送，正在跳转到聊天页面...')

    // 跳转到聊天页面
    router.push('/chat')
  } catch (error) {
    console.error('发起聊天失败:', error)
    ElMessage.error('发起聊天失败，请稍后重试')
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 生命周期
onMounted(() => {
  loadAllData()
})
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

.contact-btn {
  width: 100%;
  background: #409eff;
  border-color: #409eff;
}

.contact-btn:hover {
  background: #337ecc;
  border-color: #337ecc;
}

/* 评价相关样式 */
.rating-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-rating h4,
.user-comment h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.comment-content {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  line-height: 1.6;
  color: #333;
}

.no-rating {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
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
