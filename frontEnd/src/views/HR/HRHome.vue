<template>
  <div class="hr-home">
    <!-- 顶部标题栏 -->
    <div class="top-bar">
      <div class="page-title">主页</div>
      <div class="welcome">欢迎回来，{{ hrName }}顾问！</div>
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
          <div class="task-item clickable" @click="goToGuidance">
            <el-icon><EditPen /></el-icon>
            <span>您有 <b>{{ kpi.newGuidance }}</b> 条新的求职指导请求待处理。</span>
          </div>
          <div class="task-item clickable" @click="goToChat">
            <el-icon><ChatDotRound /></el-icon>
            <span>您有 <b>{{ kpi.unreadMessages }}</b> 条来自求职者的未读消息。</span>
          </div>
        </div>
        <div class="task-footer">
          <el-link type="primary" @click="goToGuidance">查看全部</el-link>
        </div>
      </el-card>
      <el-card class="quick-card">
        <div class="task-title">快速入口</div>
        <el-divider />
        <div class="quick-entry-row">
          <div class="quick-entry clickable" @click="goToResumeList">
            <el-icon><Document /></el-icon>
            <span>浏览推荐简历</span>
          </div>
          <div class="quick-entry clickable" @click="goToResumeSearch">
            <el-icon><Search /></el-icon>
            <span>搜索特定人才</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { EditPen, ChatDotRound, Finished, Document, Search } from '@element-plus/icons-vue'
const router = useRouter()
// 假设HR姓名后续从后端获取
const hrName = ref('王小明')
const kpi = ref({
  newGuidance: 5,
  newContacts: 8,
  completedGuidance: 3,
  unreadMessages: 2
})
function goToGuidance() {
  router.push('/hr/guidance')
}
function goToChat() {
  router.push('/chat')
}
function goToResumeList() {
  router.push({ path: '/hr/resume-list', query: { recommend: 'true' } })
}
function goToResumeSearch() {
  router.push({ path: '/hr/resume-list', query: { searchFocus: 'true' } })
}
</script>

<style scoped>
.hr-home {
  box-sizing: border-box;
  padding: 0 24px 16px 16px;
  min-height: calc(100vh - 16px);
  background: #f5f7fa;
  max-width: 1200px;
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
  min-height: 100px;
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
  font-size: 36px;
  font-weight: 700;
  margin-top: 16px;
  margin-bottom: 8px;
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
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.task-title {
  font-size: 18px;
  font-weight: 600;
  color: #3a36db;
  margin-bottom: 8px;
}
.task-list {
  margin: 16px 0 8px 0;
}
.task-item {
  display: flex;
  align-items: center;
  font-size: 15px;
  color: #333;
  padding: 10px 0;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
}
.task-item .el-icon {
  margin-right: 10px;
  font-size: 20px;
  color: #3a36db;
}
.task-item.clickable:hover {
  background: #f5f5ff;
}
.task-footer {
  text-align: right;
  margin-top: 8px;
}
.quick-entry-row {
  display: flex;
  gap: 12px;
  margin: 12px 0 4px 0;
}
.quick-entry {
  flex: 1;
  background: linear-gradient(135deg, #f0f0ff 80%, #e6e6fa 100%);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 0;
  font-size: 15px;
  font-weight: 600;
  color: #3a36db;
  cursor: pointer;
  transition: box-shadow 0.2s, background 0.2s;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
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