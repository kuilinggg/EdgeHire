<template>
  <div class="resume-view-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="resume-title">查看简历</span>
        </div>
      </template>
      <el-row :gutter="32" class="resume-main-row">
        <template v-if="resumeList.length > 0">
          <el-col :span="5" class="resume-list-col">
            <el-menu :default-active="selectedIndex" @select="handleSelect" class="resume-list-menu"
              style="height: 100%">
              <el-menu-item v-for="(item, idx) in resumeList" :key="item.id" :index="String(idx)"
                class="resume-menu-item">
                <div class="menu-item-flex">
                  <el-icon style="margin-right: 4px;">
                    <Document />
                  </el-icon>
                  <span>简历{{ idx + 1 }}</span>
                </div>
              </el-menu-item>
            </el-menu>
          </el-col>
          <el-col :span="19" class="resume-a4-col">
            <div v-if="selectedResume" class="resume-a4-wrapper">
              <div class="resume-a4-paper">
                <!-- 个人信息顶部模块 -->
                <div class="resume-a4-header-info">
                  <div class="resume-a4-personal">
                    <div class="resume-a4-name">{{ parsedResumeContent.姓名 || '姓名' }}</div>
                    <div class="resume-a4-job">{{ parsedResumeContent.求职意向 || '求职意向' }}</div>
                    
                    <!-- 个人信息图标栏 -->
                    <div class="resume-a4-info-grid" v-if="parsedResumeContent['个人信息']">
                      <div v-for="(v, k) in parsedResumeContent['个人信息']" :key="k" class="resume-a4-info-item">
                        <div class="resume-a4-info-icon">
                          <el-icon v-if="k.includes('年龄')"><Calendar /></el-icon>
                          <el-icon v-else-if="k.includes('民族')"><User /></el-icon>
                          <el-icon v-else-if="k.includes('电话') || k.includes('联系')"><Phone /></el-icon>
                          <el-icon v-else-if="k.includes('邮箱') || k.includes('邮件') || k.includes('mail')"><Message /></el-icon>
                          <el-icon v-else-if="k.includes('籍贯') || k.includes('地区') || k.includes('地址')"><Location /></el-icon>
                          <el-icon v-else><InfoFilled /></el-icon>
                        </div>
                        <span class="resume-a4-info-label">{{ k }}：</span>
                        <span class="resume-a4-info-value">{{ v }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="resume-a4-avatar">
                    <el-avatar v-if="selectedResume.avatar" :src="selectedResume.avatar" size="large" shape="square" class="resume-avatar"/>
                    <span v-else>无</span>
                  </div>
                </div>
                
                <div class="resume-a4-section" v-if="parsedResumeContent['教育背景']">
                  <div class="resume-a4-section-title">
                    <div class="section-title-box">
                      <div class="section-icon">
                        <el-icon><Reading /></el-icon>
                      </div>
                      教育背景
                    </div>
                  </div>
                  <div class="resume-a4-section-content">
                    <span v-for="(v, k) in parsedResumeContent['教育背景']" :key="k" class="resume-a4-field">
                      <span class="field-label">{{ k }}：</span>
                      <span class="field-value">
                        <template v-if="Array.isArray(v)">{{ v.join('，') }}</template>
                        <template v-else>{{ v }}</template>
                      </span>
                    </span>
                  </div>
                </div>
                <div class="resume-a4-section" v-if="parsedResumeContent['任职情况'] && parsedResumeContent['任职情况'].length">
                  <div class="resume-a4-section-title">
                    <div class="section-title-box">
                      <div class="section-icon">
                        <el-icon><Briefcase /></el-icon>
                      </div>
                      任职情况
                    </div>
                  </div>
                  <div class="resume-a4-section-content">
                    <div v-for="(job, idx) in parsedResumeContent['任职情况']" :key="idx" class="resume-a4-job-block">
                      <span v-for="(v, k) in job" :key="k" class="resume-a4-job-field">
                        <span class="field-label">{{ k }}：</span>
                        <span class="field-value">{{ v }}</span>
                      </span>
                    </div>
                  </div>
                </div>
                <div class="resume-a4-section"
                  v-if="parsedResumeContent['实习_兼职'] && parsedResumeContent['实习_兼职'].length">
                  <div class="resume-a4-section-title">
                    <div class="section-title-box">
                      <div class="section-icon">
                        <el-icon><Suitcase /></el-icon>
                      </div>
                      实习/兼职
                    </div>
                  </div>
                  <div class="resume-a4-section-content">
                    <div v-for="(exp, idx) in parsedResumeContent['实习_兼职']" :key="idx" class="resume-a4-job-block">
                      <span v-for="(v, k) in exp" :key="k" class="resume-a4-job-field">
                        <span class="field-label">{{ k }}：</span>
                        <span class="field-value">{{ v }}</span>
                      </span>
                    </div>
                  </div>
                </div>
                <div class="resume-a4-section" v-if="parsedResumeContent['自我评价']">
                  <div class="resume-a4-section-title">
                    <div class="section-title-box">
                      <div class="section-icon">
                        <el-icon><Star /></el-icon>
                      </div>
                      自我评价
                    </div>
                  </div>
                  <div class="resume-a4-section-content">
                    <div class="resume-a4-self-eval">
                      <span class="field-label">自我评价：</span>
                      <span class="field-value">{{ parsedResumeContent['自我评价'] }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="resume-create-time-a4">
                创建时间：<span class="resume-label">{{ formatDate(selectedResume.createTime) }}</span>
              </div>
              <div style="margin-top: 32px; text-align: center; display: flex; justify-content: center; gap: 16px;">
                <el-button type="success" size="large" icon="Document" @click="exportPdf">
                  导出为PDF
                </el-button>
                <el-button type="primary" size="large" icon="Edit" class="edit-btn-main" @click="onEditSelected">
                  编辑
                </el-button>
                <el-popconfirm title="确定删除该简历？" @confirm="onDeleteSelected">
                  <template #reference>
                    <el-button type="danger" size="large" icon="Delete" class="delete-btn-main">
                      删除当前简历
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
            <el-empty v-else description="请选择左侧简历" />
          </el-col>
        </template>
        <template v-else>
          <el-col :span="24">
            <el-empty description="暂无简历数据" />
          </el-col>
        </template>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getResumesByUserId, deleteResume } from '../../api/resume'
import { useAuthStore } from '../../stores/authStore'
import { useRouter } from 'vue-router'
import { Document, Delete, Edit, Calendar, Phone, Message, Location, User, InfoFilled, Reading, Briefcase, Suitcase, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import html2pdf from 'html2pdf.js'

const resumeList = ref([])
const selectedIndex = ref('0')
const selectedResume = ref(null)
const authStore = useAuthStore()
const router = useRouter()

const handleSelect = (idx) => {
  selectedIndex.value = idx
  selectedResume.value = resumeList.value[Number(idx)]
}

const onDeleteSelected = async () => {
  if (!selectedResume.value) return
  const id = selectedResume.value.id
  try {
    await deleteResume(id)
    ElMessage.success('删除成功')
    // 删除成功后刷新简历列表
    await refreshResumeList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const onEditSelected = () => {
  if (!selectedResume.value) return
  router.push({
    path: '/jobseeker/resume-edit',
    query: { id: selectedResume.value.id }
  })
}

// 拉取简历列表并刷新选中项
const refreshResumeList = async () => {
  const userId = authStore.userId || authStore.user?.id
  try {
    const res = await getResumesByUserId(userId)
    if (res.data && res.data.length > 0) {
      resumeList.value = res.data
      selectedResume.value = res.data[0]
      selectedIndex.value = '0'
    } else {
      resumeList.value = []
      selectedResume.value = null
      selectedIndex.value = '0'
    }
  } catch (e) {
    resumeList.value = []
    selectedResume.value = null
    selectedIndex.value = '0'
  }
}

const parsedResumeContent = computed(() => {
  if (!selectedResume.value || !selectedResume.value.content) return {}
  try {
    const obj = JSON.parse(selectedResume.value.content)
    return obj && typeof obj === 'object' ? obj : {}
  } catch {
    return { 内容: selectedResume.value.content }
  }
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}年${m}月${day}日 ${h}:${min}`
}

//导出简历为PDF
function exportPdf() {
  const element = document.querySelector('.resume-a4-paper')
  if (!element) {
    ElMessage.error('没有可导出的简历内容')
    return
  }
  html2pdf()
    .from(element)
    .set({
      margin: 0,
      filename: (parsedResumeContent.value.姓名 || '简历') + '.pdf',
      html2canvas: { scale: 2, useCORS: true },
      jsPDF: {
        unit: 'mm', format: 'a4', orientation: 'portrait',
      },
      pagebreak: { mode: ['avoid-all'] },
      pagesplit: false
    })
    .toPdf()
    .get('pdf')
    .then(function(pdf){
      // 删除多余的页面
      while (pdf.internal.getNumberOfPages() > 1) {
        pdf.deletePage(2)
      }
      return pdf
    })
    .save()
    .then(() => {
      ElMessage.success('导出成功')
    })
    .catch((err) => {
      console.error('导出失败:', err)
      ElMessage.error('导出失败，请稍后重试')
    })
}

onMounted(refreshResumeList)
</script>

<style scoped>
.resume-view-container {
  padding: 32px 0;
  min-height: 100vh;
  background: #f4f6fa;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.el-card {
  box-shadow: 0 4px 32px rgba(64, 158, 255, 0.10), 0 1.5px 6px 0 rgba(0, 0, 0, 0.04);
  border-radius: 18px;
  width: 1100px;
  max-width: 98vw;
  margin: 0 auto;
  background: #f9fafb;
}

.resume-main-row {
  min-height: 900px;
  align-items: flex-start;
}

.resume-list-col {
  min-width: 180px;
  max-width: 260px;
  height: 900px;
  background: transparent;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.resume-list-menu {
  border-radius: 14px;
  background: #f7f8fa;
  box-shadow: 0 1px 4px rgba(99,102,241,0.03);
  padding: 0;
  height: 100%;
  border: none;
  transition: background 0.2s, box-shadow 0.2s;
}

.resume-menu-item {
  border-radius: 0 8px 8px 0;
  margin: 2px 0;
  font-size: 16px;
  transition: background 0.2s, color 0.2s;
  padding: 8px 12px 8px 0;
  color: #333;
  position: relative;
  overflow: visible;
}

.resume-menu-item.is-active,
.resume-menu-item:hover {
  background: linear-gradient(90deg, #e6eaff 0%, #f4f6fb 100%);
  color: #4f46e5;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(99,102,241,0.06);
}

.resume-menu-item.is-active::before,
.resume-menu-item:hover::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 4px;
  border-radius: 4px;
  background: #6366f1;
  z-index: 1;
}

.menu-item-flex {
  padding-left: 16px;
  display: flex;
  align-items: center;
}

.resume-a4-col {
  min-width: 740px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}

.resume-a4-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 auto;
}

.resume-a4-paper {
  width: 794px; /* 210mm * 3.78px/mm = 794px */
  min-height: 1123px; /* 297mm * 3.78px/mm = 1123px */
  background: #fff;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.13);
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  padding: 0;
  margin-bottom: 18px;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

/* 顶部Resume标题 */
.resume-a4-paper::before {
  content: "Resume";
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 40px;
  background: #3c4b64;
  color: white;
  font-size: 22px;
  font-weight: bold;
  letter-spacing: 1px;
  position: relative;
  border-radius: 10px 10px 0 0;
}

/* 顶部标题的V形切口 */
.resume-a4-paper::after {
  content: "";
  position: absolute;
  top: 40px;
  left: 50%;
  width: 35px;
  height: 18px;
  background: #fff;
  transform: translateX(-50%) rotate(45deg);
  z-index: 1;
}

.resume-a4-header-info {
  display: flex;
  align-items: flex-start;
  margin: 30px 40px 5px 40px;
  justify-content: space-between;
  position: relative;
  padding-bottom: 5px;
}

.resume-a4-personal {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  flex: 1;
}

.resume-a4-name {
  font-size: 28px;
  font-weight: bold;
  color: #111827;
  margin-bottom: 4px;
  letter-spacing: 1px;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.05);
  font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
}

.resume-a4-job {
  font-size: 16px;
  color: #4f46e5;
  font-weight: 500;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
  font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
}

.resume-a4-avatar {
  margin-left: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 160px;
  align-self: center;
}

/* 个人信息网格布局 */
.resume-a4-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
  margin-top: 6px;
}

/* 个人信息图标样式 */
.resume-a4-info-item {
  display: flex;
  align-items: center;
  margin-bottom: 2px;
  font-size: 14px;
  color: #333;
}

.resume-a4-info-icon {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #f0f2ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  color: #4f46e5;
}

.resume-a4-info-label {
  font-weight: 600;
  color: #2c3e50;
  margin-right: 4px;
  white-space: nowrap; /* 防止标签文字换行 */
  letter-spacing: 0.3px;
}

.resume-a4-info-value {
  color: #3a3a3a;
  letter-spacing: 0.2px;
}

.field-label {
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap; /* 防止标签文字换行 */
  letter-spacing: 0.3px;
}

.field-value {
  color: #3a3a3a;
  letter-spacing: 0.2px;
}

.resume-a4-job-field {
  display: flex;
  margin-right: 16px;
  align-items: baseline;
  min-width: 140px;
}

.resume-a4-section {
  margin: 5px 40px;
}

.resume-a4-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  position: relative;
  display: flex;
  align-items: flex-end;
  border-radius: 0;
  padding: 0;
  background: transparent;
  height: 28px;
  letter-spacing: 0.5px;
  text-shadow: 0 1px 1px rgba(255, 255, 255, 0.7);
}

.resume-a4-section-title::after {
  content: "";
  height: 8px;
  background: #3c4b64;
  flex: 1;
  position: relative;
  bottom: 0;
  clip-path: polygon(0 0, 100% 0, 100% 100%, 1% 100%);
}

.resume-a4-section-title .section-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 6px;
  color: white;
  width: 20px;
  height: 20px;
}

.section-title-box {
  background: #3c4b64;
  color: white;
  display: flex;
  align-items: center;
  padding: 5px 5px 5px 10px;
  position: relative;
  clip-path: polygon(0 0, calc(100% - 20px) 0, 100% 100%, 0 100%);
  min-width: 120px;
  height: 20px;
  font-weight: 500;
  letter-spacing: 1px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.resume-a4-section-title .section-icon .el-icon {
  font-size: 14px;
  width: 100%;
  height: 100%;
}

.resume-a4-section-content {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  padding: 3px 8px;
  font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
}

.resume-a4-field {
  min-width: 140px;
  margin-right: 16px;
  display: flex;
  align-items: baseline;
}



.resume-a4-job-block {
  margin-bottom: 8px;
  padding-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px 14px;
  border-bottom: 1px dashed #e5e7eb;
  width: 100%;
  transition: all 0.2s ease;
}

.resume-a4-job-block:last-child {
  border-bottom: none;
}

.resume-create-time-a4 {
  margin-top: 16px;
  font-size: 15px;
  color: #888;
  text-align: left;
  width: 794px;
  max-width: 100%;
  margin-left: auto;
  margin-right: auto;
}

.resume-avatar {
  width: 120px !important;
  height: 160px !important;
  object-fit: cover;
  border: 1px solid #e5e7eb;
  background: #fff;
}

.resume-a4-self-eval {
  width: 100%;
  display: flex;
  align-items: baseline;
}

.resume-a4-self-eval .field-label {
  margin-right: 6px;
  flex-shrink: 0;
}

.resume-a4-self-eval .field-value {
  line-height: 1.7;
  text-align: justify;
}

@media (max-width: 1200px) {
  .el-card {
    width: 100vw;
    min-width: unset;
    max-width: 100vw;
    border-radius: 0;
  }
  .resume-a4-paper, .resume-create-time-a4 {
    width: 100vw !important;
    min-width: unset;
    border-radius: 0;
    padding: 16px 2vw 16px 2vw;
  }
}

@media (max-width: 900px) {
  .resume-view-container {
    padding: 8px 0;
  }
  .resume-a4-col {
    min-width: unset;
  }
  .resume-a4-paper, .resume-create-time-a4 {
    width: 100vw !important;
    min-width: unset;
    border-radius: 0;
    padding: 8px 0 8px 0;
  }
}
</style>
