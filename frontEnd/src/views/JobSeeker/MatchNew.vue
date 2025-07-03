<template>
  <div class="match-new-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Document /></el-icon>
          智能简历匹配
        </h1>
        <p class="page-subtitle">选择您的优质简历，开启精准求职之旅</p>
      </div>
    </div>

    <!-- 顶部提示 -->
    <el-alert
      title="请在下方选择需要提交的简历"
      type="info"
      show-icon
      center
      class="top-tip-alert"
      :closable="false"
    />

    <!-- 求职信息卡片 -->
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h3>
            <el-icon><User /></el-icon>
            求职信息
          </h3>
          <p>您的基本求职信息概览</p>
        </div>
      </template>
      <div class="seeker-info-content">
        <el-card v-if="seekerInfo && seekerInfo.id" class="seeker-info-card">
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">
                <el-icon><Reading /></el-icon>
                学历
              </div>
              <div class="info-value">{{ educationOptions.find(e => e.value === seekerInfo.education)?.label || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">
                <el-icon><School /></el-icon>
                学校
              </div>
              <div class="info-value">{{ seekerInfo.school || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">
                <el-icon><Star /></el-icon>
                会员类型
              </div>
              <div class="info-value">
                <el-tag :type="seekerInfo.membership === 0 ? 'info' : 'success'">
                  {{ seekerInfo.membership === 0 ? '普通会员' : '高级会员' }}
                </el-tag>
              </div>
            </div>
            <div class="info-item full-width">
              <div class="info-label">
                <el-icon><TrendCharts /></el-icon>
                理想岗位
              </div>
              <div class="info-value">
                <template v-if="favorList.length > 0">
                  <el-tag v-for="(item, idx) in favorList" :key="idx" type="primary" effect="light" class="favor-tag">
                    {{ item }}
                  </el-tag>
                </template>
                <template v-else>
                  <span class="no-data">暂无设置</span>
                </template>
              </div>
            </div>
          </div>
        </el-card>
        <el-empty v-else description="暂无求职信息" />
      </div>
    </el-card>
    <!-- 简历选择卡片 -->
    <el-card class="resume-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h3>
            <el-icon><Document /></el-icon>
            简历选择
          </h3>
          <p>选择您要提交的简历进行匹配</p>
        </div>
      </template>
      <div class="resume-content-wrapper">
        <template v-if="resumeList.length > 0">
          <div class="resume-sidebar">
            <el-menu
              :default-active="selectedIndex"
              @select="handleSelect"
              class="resume-list-menu"
            >
              <el-menu-item
                v-for="(item, idx) in resumeList"
                :key="item.id"
                :index="String(idx)"
                class="resume-menu-item"
              >
                <div class="menu-item-flex">
                  <el-icon style="margin-right: 4px;"><Document /></el-icon>
                  <span>简历{{ idx + 1 }}</span>
                </div>
              </el-menu-item>
            </el-menu>
          </div>
          <div class="resume-preview">
            <div v-if="selectedResume" class="resume-wrapper">
              <div v-if="isImportedResume" class="pdf-a4-fixed-wrapper">
                <canvas ref="pdfCanvasRef" class="pdf-a4-canvas"></canvas>
                <div v-if="pdfLoading" class="pdf-loading-mask">PDF加载中...</div>
              </div>
              <div v-else class="resume-a4-paper">
                <component
                  :is="resumeA4Component"
                  :content="parsedResumeContent"
                  :avatar="selectedResume.avatar"
                />
              </div>
            </div>
            <el-empty v-else description="请选择左侧简历" />
          </div>
        </template>
        <template v-else>
          <div class="empty-state">
            <el-empty description="暂无简历数据" />
          </div>
        </template>
      </div>
      <div style="margin-top: 32px; text-align: center;">
        <el-button 
          type="primary" 
          size="large" 
          @click="onSubmitMatch" 
          :disabled="!selectedResume || !seekerInfo || !seekerInfo.id"
          class="submit-btn"
        >
          <el-icon><Check /></el-icon>
          提交匹配
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { Document, User, Reading, School, Star, TrendCharts, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createPost } from '../../api/post'
import { useAuthStore } from '../../stores/authStore'
import { getSeekerByUserId } from '../../api/seeker'
import { getResumesByUserId } from '../../api/resume'
import ResumeA4Paper from '../../components/ResumeA4Paper.vue'
import ResumeA4PaperBlueLeft from '../../components/ResumeA4PaperBlueLeft.vue'
import ResumeA4PaperBlueTopBar from '../../components/ResumeA4PaperBlueTopBar.vue'

const resumeList = ref([])
const selectedIndex = ref('0')
const selectedResume = ref(null)
const authStore = useAuthStore()
const seekerInfo = ref({ id: null, education: '', school: '', favor: '', membership: 0 })
const educationOptions = [
  { value: 1, label: '小学' },
  { value: 2, label: '初中' },
  { value: 3, label: '高中' },
  { value: 4, label: '大专' },
  { value: 5, label: '本科' },
  { value: 6, label: '硕士' },
  { value: 7, label: '博士' }
]

const educationText = computed(() => {
  const found = educationOptions.find(e => e.value === seekerInfo.value.education)
  return found ? found.label : '-'
})

const favorList = computed(() => {
  if (!seekerInfo.value.favor) return []
  try {
    const arr = JSON.parse(seekerInfo.value.favor)
    return Array.isArray(arr) ? arr : [seekerInfo.value.favor]
  } catch {
    return seekerInfo.value.favor ? [seekerInfo.value.favor] : []
  }
})

const handleSelect = (idx) => {
  selectedIndex.value = idx
  selectedResume.value = resumeList.value[Number(idx)]
}

const fetchSeekerInfo = async () => {
  const userId = authStore.userId || authStore.user?.id
  if (!userId) return
  try {
    const { data } = await getSeekerByUserId(userId)
    seekerInfo.value = data || { id: null, education: '', school: '', favor: '', membership: 0 }
  } catch {
    seekerInfo.value = { id: null, education: '', school: '', favor: '', membership: 0 }
  }
}
const fetchResumeList = async () => {
  const userId = authStore.userId || authStore.user?.id
  if (!userId) return
  try {
    const res = await getResumesByUserId(userId)
    resumeList.value = res.data || []
    if (resumeList.value.length > 0) {
      selectedResume.value = resumeList.value[0]
      selectedIndex.value = '0'
    } else {
      selectedResume.value = null
      selectedIndex.value = '0'
    }
  } catch {
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

const resumeA4Component = computed(() => {
  const template = parsedResumeContent.value.template || '1'
  if (template === '2') return ResumeA4PaperBlueLeft
  if (template === '3') return ResumeA4PaperBlueTopBar
  // 默认模板 1
  return ResumeA4Paper
})

const onSubmitMatch = async () => {
  const userId = authStore.userId || authStore.user?.id
  if (!userId || !seekerInfo.value?.id || !selectedResume.value?.id) {
    ElMessage.error('信息不完整，无法提交')
    return
  }
  const postData = {
    userId: userId,
    seekerInfoId: seekerInfo.value.id,
    resumeId: selectedResume.value.id
  }
  try {
    await createPost(postData)
    ElMessage.success('匹配提交成功')
  } catch {
    ElMessage.error('提交失败')
  }
}

// PDF.js渲染相关
const pdfCanvasRef = ref(null)
const pdfLoading = ref(false)
const pdfDocCache = new Map() // url => pdf对象

function loadPdfJsScript() {
  return new Promise((resolve, reject) => {
    if (window.pdfjsLib) return resolve(window.pdfjsLib)
    const script = document.createElement('script')
    script.src = 'https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/build/pdf.min.js'
    script.onload = () => {
      window.pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/build/pdf.worker.min.js'
      resolve(window.pdfjsLib)
    }
    script.onerror = reject
    document.head.appendChild(script)
  })
}
loadPdfJsScript()

async function renderPdfToCanvas(url) {
  pdfLoading.value = true
  await loadPdfJsScript()
  const pdfjsLib = window.pdfjsLib
  try {
    let pdf
    if (pdfDocCache.has(url)) {
      pdf = pdfDocCache.get(url)
    } else {
      const loadingTask = pdfjsLib.getDocument(url)
      pdf = await loadingTask.promise
      pdfDocCache.set(url, pdf)
    }
    const page = await pdf.getPage(1)
    const viewport = page.getViewport({ scale: 1 })
    const targetWidth = 794
    const targetHeight = 1123
    const scale = Math.min(targetWidth / viewport.width, targetHeight / viewport.height)
    const scaledViewport = page.getViewport({ scale })
    const canvas = pdfCanvasRef.value
    const context = canvas.getContext('2d')
    canvas.width = scaledViewport.width
    canvas.height = scaledViewport.height
    context.clearRect(0, 0, canvas.width, canvas.height)
    await page.render({ canvasContext: context, viewport: scaledViewport }).promise
  } catch (e) {
    console.error('PDF渲染失败', e)
  } finally {
    pdfLoading.value = false
  }
}

const isImportedResume = computed(() => {
  if (!selectedResume.value || !selectedResume.value.content) return false
  try {
    const obj = JSON.parse(selectedResume.value.content)
    return obj && obj.importType === 'pdf' && obj.pdfUrl
  } catch {
    return false
  }
})
const importedPdfUrl = computed(() => {
  if (!isImportedResume.value) return ''
  try {
    const obj = JSON.parse(selectedResume.value.content)
    return obj.pdfUrl || ''
  } catch {
    return ''
  }
})

watch(importedPdfUrl, (url) => {
  if (isImportedResume.value && url) {
    setTimeout(() => renderPdfToCanvas(url), 0)
  }
})

onMounted(() => {
  fetchSeekerInfo()
  fetchResumeList()
  if (isImportedResume.value && importedPdfUrl.value) {
    setTimeout(() => renderPdfToCanvas(importedPdfUrl.value), 0)
  }
})
</script>

<style scoped>
.match-new-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background: #f8fafe;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 页面头部 */
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

/* 顶部提示 */
.top-tip-alert {
  border-radius: 12px;
  border: 1px solid #e1f3ff;
  background: #f0f9ff;
  color: #1e40af;
  font-size: 16px;
  font-weight: 500;
}

.top-tip-alert .el-alert__icon {
  color: #3b82f6;
}

/* 卡片通用样式 */
.el-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  background: #fff;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 求职信息卡片 */
.info-card {
  background: #fffbf0;
  border: 1px solid #fef3c7;
}

.info-card .el-card__header {
  background: #fef3c7;
  border-bottom: 1px solid #fbbf24;
}

.info-card .card-header h3 {
  color: #92400e;
}

.info-card .card-header p {
  color: #a16207;
}

.seeker-info-content {
  padding: 20px 0;
}

.seeker-info-card {
  background: #fff;
  border: 1px solid #f3f4f6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  padding: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #667eea;
}

.info-value {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.favor-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

.no-data {
  color: #999;
  font-style: italic;
}

/* 简历选择卡片 */
.resume-card {
  flex: 1;
}

.resume-content-wrapper {
  display: flex;
  gap: 32px;
  min-height: 700px;
  align-items: flex-start;
}

.resume-sidebar {
  min-width: 240px;
  max-width: 280px;
  height: 700px;
  flex-shrink: 0;
}

.resume-list-menu {
  border-radius: 12px;
  background: #f8fafe;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.08);
  padding: 8px;
  height: 100%;
  border: 1px solid #e8f2ff;
  overflow: hidden;
}

.resume-menu-item {
  border-radius: 8px;
  margin: 4px 0;
  font-size: 16px;
  transition: all 0.3s ease;
  padding: 12px 16px;
  color: #333;
  position: relative;
  background: transparent;
  border: 1px solid transparent;
}

.resume-menu-item:hover {
  background: linear-gradient(90deg, #e6eaff 0%, #f4f6fb 100%);
  color: #667eea;
  border-color: #c7d2fe;
  transform: translateX(4px);
}

.resume-menu-item.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  border-color: #667eea;
}

.resume-menu-item.is-active::before {
  content: '';
  position: absolute;
  left: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  border-radius: 2px;
  background: #fff;
}

.menu-item-flex {
  display: flex;
  align-items: center;
  gap: 8px;
}

.resume-preview {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}

.resume-wrapper {
  display: flex;
  justify-content: center;
  padding: 8px;
  overflow: visible;
}
.resume-a4-paper {
  width: 794px;
  min-height: auto;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border-radius: 8px;
  max-width: 100%;
  overflow: visible;
}
.pdf-a4-fixed-wrapper {
  width: 100%;
  height: auto;
  min-height: 600px;
  max-width: 100%;
  background: #fff;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: visible;
  position: relative;
}
.pdf-a4-canvas {
  width: 100%;
  height: auto;
  min-height: 600px;
  background: #fff;
  display: block;
  border-radius: 8px;
}

.empty-state {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

/* PDF 渲染样式 */
.pdf-loading-mask {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #667eea;
  z-index: 10;
  backdrop-filter: blur(2px);
}

/* 提交按钮 */
.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 600;
  padding: 0 48px;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
  transition: all 0.3s ease;
}

.submit-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.submit-btn:disabled {
  background: #d1d5db;
  color: #9ca3af;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .match-new-container {
    max-width: 100%;
    padding: 16px;
  }
  
  .resume-content-wrapper {
    gap: 20px;
    min-height: auto;
  }
  
  .resume-sidebar {
    min-width: 200px;
    max-width: 220px;
    height: auto;
    min-height: 400px;
  }
  
  .resume-list-menu {
    min-height: 400px;
  }
}

@media (max-width: 768px) {
  .match-new-container {
    padding: 12px;
  }
  
  .page-header {
    padding: 32px 24px;
  }
  
  .header-content h1 {
    font-size: 24px;
    flex-direction: column;
    gap: 8px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 16px;
  }
  
  .info-item.full-width {
    grid-column: 1;
  }
  
  .resume-content-wrapper {
    flex-direction: column;
    gap: 16px;
  }
  
  .resume-sidebar {
    min-width: auto;
    max-width: 100%;
    width: 100%;
    height: auto;
    min-height: auto;
  }
  
  .resume-list-menu {
    display: flex;
    flex-direction: row;
    overflow-x: auto;
    overflow-y: hidden;
    padding: 8px;
    height: auto;
    min-height: auto;
  }
  
  .resume-menu-item {
    min-width: 120px;
    margin: 0 4px;
    text-align: center;
    flex-shrink: 0;
  }
  
  .resume-preview {
    width: 100%;
  }
  
  .pdf-a4-fixed-wrapper {
    width: 100%;
    height: auto;
    min-height: auto;
    max-height: none;
  }
  
  .pdf-a4-canvas {
    width: 100%;
    height: auto;
    min-height: auto;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 24px 16px;
  }
  
  .header-content h1 {
    font-size: 20px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .submit-btn {
    padding: 0 32px;
    height: 44px;
    font-size: 15px;
  }
}
</style>
