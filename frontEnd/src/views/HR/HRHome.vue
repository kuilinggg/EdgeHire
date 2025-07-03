<template>
  <div class="hr-home">
    <!-- 顶部标题栏 -->
    <div class="top-bar">
      <div class="page-title">主页</div>
      <div class="welcome">欢迎回来，{{ hrName }}顾问！</div>
    </div>

    <!-- 个人信息完善提醒 -->
    <div v-if="!authStore.profileComplete" class="profile-reminder">
      <el-alert
        title="完善个人信息"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #default>
          <p>为了提升您的专业形象和服务质量，请完善您的个人信息。</p>
          <el-button type="primary" size="small" @click="goToProfile">
            立即完善
          </el-button>
        </template>
      </el-alert>
    </div>
    <!-- KPI卡片区 -->
    <div class="kpi-row">
      <el-card class="kpi-card kpi-blue">
        <div class="kpi-icon"><el-icon><EditPen /></el-icon></div>
        <div class="kpi-value">{{ kpi.newGuidance }}</div>
        <div class="kpi-label">新的指导请求</div>
      </el-card>
      <el-card class="kpi-card kpi-green">
        <div class="kpi-icon"><el-icon><ChatDotRound /></el-icon></div>
        <div class="kpi-value">{{ kpi.newContacts }}</div>
        <div class="kpi-label">本周沟通人数</div>
      </el-card>
      <el-card class="kpi-card kpi-purple">
        <div class="kpi-icon"><el-icon><Finished /></el-icon></div>
        <div class="kpi-value">{{ kpi.completedGuidance }}</div>
        <div class="kpi-label">本周完成指导</div>
      </el-card>
    </div>
    <!-- 核心任务区 -->
    <div class="task-row">
      <el-card class="task-card">
        <div class="task-title">待办事项</div>
        <el-divider />
        <div class="task-list">
          <div v-if="kpi.newGuidance > 0" class="task-item clickable" @click="goToGuidance">
            <div class="task-content">
              <el-icon><EditPen /></el-icon>
              <span>您有 <b>{{ kpi.newGuidance }}</b> 条新的求职指导请求待处理。</span>
            </div>
            <div class="task-action">
              <span class="action-text">点击处理</span>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
          </div>
          <div v-if="kpi.unreadMessages > 0" class="task-item clickable" @click="goToChat">
            <div class="task-content">
              <el-icon><ChatDotRound /></el-icon>
              <span>您有 <b>{{ kpi.unreadMessages }}</b> 条来自求职者的未读消息。</span>
            </div>
            <div class="task-action">
              <span class="action-text">点击查看</span>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
          </div>
          <div v-if="kpi.newGuidance === 0 && kpi.unreadMessages === 0" class="task-item no-tasks">
            <el-icon><Check /></el-icon>
            <span>暂无待办事项，您已处理完所有任务！</span>
          </div>
        </div>
      </el-card>
      <el-card class="quick-card">
        <div class="task-title">快速入口</div>
        <el-divider />
        <div class="quick-entry-row">
          <div class="quick-entry clickable" @click="goToResumeList">
            <el-icon><Document /></el-icon>
            <span>浏览简历</span>
          </div>
          <div class="quick-entry clickable" @click="goToResumeSearch">
            <el-icon><Search /></el-icon>
            <span>搜索人才</span>
          </div>
          <div class="quick-entry clickable" @click="goToGuidance">
            <el-icon><EditPen /></el-icon>
            <span>求职指导</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { EditPen, ChatDotRound, Finished, Document, Search, Check, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { hrApi } from '../../api/hr.js'
import { getInfoByUserId } from '../../api/info.js'
import { useAuthStore } from '../../stores/authStore.js'

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const hrName = ref('HR')
const loading = ref(false)
const kpi = ref({
  newGuidance: 0,
  newContacts: 0,
  completedGuidance: 0,
  unreadMessages: 0
})
// 数据加载方法
const loadKpiData = async () => {
  try {
    loading.value = true
    const userId = authStore.userId || '1'
    const response = await hrApi.getKpiData(userId)

    if (response.data) {
      kpi.value = response.data
    }
  } catch (error) {
    console.error('加载KPI数据失败:', error)
    ElMessage.error('加载数据失败，请稍后重试')
    // 使用默认数据作为fallback
    kpi.value = {
      newGuidance: 5,
      newContacts: 8,
      completedGuidance: 3,
      unreadMessages: 2
    }
  } finally {
    loading.value = false
  }
}

const loadHrInfo = async () => {
  try {
    const userId = authStore.userId || '1'
    
    // 从 Info 表中获取用户基本信息（包括姓名）
    try {
      const userResponse = await getInfoByUserId(userId)
      if (userResponse.data && userResponse.data.realname) {
        hrName.value = userResponse.data.realname
        return
      }
    } catch (error) {
      console.error('获取用户基本信息失败:', error)
    }
    
    // 如果无法获取姓名，则尝试从 HR 信息中获取公司名称作为备选
    try {
      const hrResponse = await hrApi.getHrInfo(userId)
      if (hrResponse.data && hrResponse.data.company) {
        hrName.value = hrResponse.data.company + ' HR'
        return
      }
    } catch (error) {
      console.error('获取HR信息失败:', error)
    }
    
    // 如果都获取不到，使用默认值
    hrName.value = 'HR'
  } catch (error) {
    console.error('加载HR信息失败:', error)
    hrName.value = 'HR'
  }
}

// 个人信息完整性检查
const checkProfileCompleteness = async () => {
  try {
    const userId = authStore.userId || '1'

    // 检查用户基本信息
    let userInfoComplete = false
    try {
      const userResponse = await getInfoByUserId(userId)
      const userInfo = userResponse.data
      userInfoComplete = userInfo &&
                        userInfo.realname &&
                        userInfo.age &&
                        userInfo.phone &&
                        userInfo.email
    } catch (error) {
      // 用户信息不存在
      userInfoComplete = false
    }

    // 检查HR专业信息
    let hrInfoComplete = false
    try {
      const hrResponse = await hrApi.getHrInfo(userId)
      const hrInfo = hrResponse.data
      hrInfoComplete = hrInfo &&
                      hrInfo.company &&
                      hrInfo.position &&
                      hrInfo.experience
    } catch (error) {
      // HR信息不存在
      hrInfoComplete = false
    }

    const isComplete = userInfoComplete && hrInfoComplete
    authStore.setProfileComplete(isComplete)

    return isComplete
  } catch (error) {
    console.error('检查个人信息完整性失败:', error)
    return false
  }
}

// 页面跳转方法
function goToProfile() {
  router.push('/hr/profile')
}

function goToGuidance() {
  router.push('/hr/guidance')
}
function goToChat() {
  router.push('/chat')
}
function goToResumeList() {
  router.push('/hr/resume-list')
}
function goToResumeSearch() {
  router.push({ path: '/hr/resume-list', query: { searchFocus: 'true' } })
}

// 页面初始化
onMounted(async () => {
  await Promise.all([
    loadKpiData(),
    loadHrInfo(),
    checkProfileCompleteness()
  ])
})
</script>

<style scoped>
.hr-home {
  box-sizing: border-box;
  padding: 0 24px 16px 16px;
  min-height: calc(100vh - 16px);
  background: #f5f7fa;
  max-width: 1400px;
}
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 20px;
  margin-bottom: 20px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #3a36db;
}
.welcome {
  font-size: 16px;
  color: #666;
}

.profile-reminder {
  margin-bottom: 20px;
}

.profile-reminder .el-alert {
  border-radius: 8px;
}

.profile-reminder .el-alert__content p {
  margin-bottom: 12px;
  color: #666;
}

.kpi-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}
.kpi-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  min-width: 150px;
  min-height: 130px;
  border-radius: 14px;
  color: #fff;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
  position: relative;
  overflow: hidden;
}
.kpi-blue { background: linear-gradient(135deg, #3a36db 80%, #5e8bff 100%); }
.kpi-green { background: linear-gradient(135deg, #1ecb8b 80%, #4be1c6 100%); }
.kpi-purple { background: linear-gradient(135deg, #7c3aed 80%, #a78bfa 100%); }
.kpi-icon {
  font-size: 32px;
  opacity: 0.18;
  position: absolute;
  right: 18px;
  top: 18px;
}
.kpi-value {
  font-size: 42px;
  font-weight: 700;
  margin-top: 20px;
  margin-bottom: 10px;
  z-index: 1;
}
.kpi-label {
  font-size: 15px;
  opacity: 0.95;
  z-index: 1;
}
.task-row {
  display: flex;
  gap: 16px;
}
.task-card, .quick-card {
  flex: 1;
  min-width: 260px;
  min-height: 280px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.task-title {
  font-size: 18px;
  font-weight: 600;
  color: #3a36db;
  margin-bottom: 8px;
}
.task-list {
  margin: 20px 0 16px 0;
  flex-grow: 1;
}
.task-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 15px;
  color: #333;
  padding: 16px 12px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  margin: 8px 0;
}
.task-item.clickable {
  background: #fafbff;
  border: 1px solid #e8ebf7;
  box-shadow: 0 1px 3px rgba(58, 54, 219, 0.05);
}
.task-content {
  display: flex;
  align-items: center;
  flex: 1;
}
.task-content .el-icon {
  margin-right: 10px;
  font-size: 20px;
  color: #3a36db;
}
.task-action {
  display: flex;
  align-items: center;
  gap: 6px;
  opacity: 0.7;
  transition: all 0.3s ease;
}
.action-text {
  font-size: 13px;
  color: #3a36db;
  font-weight: 500;
}
.arrow-icon {
  font-size: 14px;
  color: #3a36db;
  transition: transform 0.3s ease;
}
.task-item.clickable:hover {
  background: #f5f5ff;
  border-color: #3a36db;
  box-shadow: 0 2px 8px rgba(58, 54, 219, 0.1);
}
.task-item.clickable:hover .task-action {
  opacity: 1;
}
.task-item.clickable:hover .arrow-icon {
  transform: translateX(3px);
}
.task-item.no-tasks {
  color: #67c23a;
  font-style: italic;
  justify-content: flex-start;
  cursor: default;
}
.task-item.no-tasks .el-icon {
  color: #67c23a;
  margin-right: 10px;
}
.quick-entry-row {
  display: flex;
  gap: 12px;
  margin: 20px 0 16px 0;
  flex-grow: 1;
}
.quick-entry {
  flex: 1;
  background: linear-gradient(135deg, #f0f0ff 80%, #e6e6fa 100%);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 0;
  font-size: 15px;
  font-weight: 600;
  color: #3a36db;
  cursor: pointer;
  transition: box-shadow 0.2s, background 0.2s;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
  min-height: 120px;
}
.quick-entry .el-icon {
  font-size: 28px;
  margin-bottom: 8px;
}
.quick-entry.clickable:hover {
  background: linear-gradient(135deg, #e6e6fa 80%, #f0f0ff 100%);
  box-shadow: 0 4px 16px 0 rgba(58,54,219,0.08);
}
</style> 