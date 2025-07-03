<template>
  <div class="match-history-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Clock /></el-icon>
          历史匹配记录
        </h1>
        <p class="page-subtitle">查看您的历史简历匹配记录与详情</p>
      </div>
    </div>

    <!-- 主内容卡片 -->
    <el-card class="main-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h3>
            <el-icon><Document /></el-icon>
            匹配记录
          </h3>
          <p>选择左侧记录查看详细信息</p>
        </div>
      </template>
      <div class="history-content-wrapper">
        <template v-if="matchList.length > 0">
          <div class="history-sidebar">
            <el-menu
              :default-active="selectedIndex"
              @select="handleSelect"
              class="history-list-menu"
            >
              <el-menu-item
                v-for="(item, idx) in matchList"
                :key="item.id"
                :index="String(idx)"
                class="history-menu-item"
              >
                <div class="menu-item-flex">
                  <el-icon style="margin-right: 8px;"><Document /></el-icon>
                  <div class="menu-item-content">
                    <span class="match-title">匹配记录 {{ idx + 1 }}</span>
                    <span class="match-date">{{ formatDate(item.createdAt) }}</span>
                  </div>
                </div>
              </el-menu-item>
            </el-menu>
          </div>
          <div class="history-preview">
            <div v-if="selectedResume" class="resume-content">
              <!-- 求职者信息卡片 -->
              <el-card class="seeker-info-card" shadow="never">
                <template #header>
                  <div class="info-card-header">
                    <h4>
                      <el-icon><User /></el-icon>
                      求职者信息
                    </h4>
                  </div>
                </template>
                <div class="info-grid">
                  <div class="info-item">
                    <div class="info-label">
                      <el-icon><Reading /></el-icon>
                      学历
                    </div>
                    <div class="info-value">{{ educationText }}</div>
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
                      <el-tag :type="seekerInfo.membership == 0 ? 'info' : 'success'">
                        {{ seekerInfo.membership == 0 ? '普通会员' : '高级会员' }}
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
              
              <!-- 简历预览区域 -->
              <el-card class="resume-preview-card" shadow="never">
                <template #header>
                  <div class="info-card-header">
                    <h4>
                      <el-icon><Document /></el-icon>
                      简历详情
                    </h4>
                  </div>
                </template>
                <div class="resume-wrapper">
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
              </el-card>
            </div>
            <el-empty v-else description="请选择左侧历史匹配记录" />
          </div>
        </template>
        <template v-else>
          <div class="empty-state">
            <el-empty description="暂无历史匹配数据" />
          </div>
        </template>
      </div>
      <div class="action-area" v-if="matchList.length > 0">
        <el-button 
          type="danger" 
          size="large" 
          @click="onDeleteMatch" 
          :disabled="!selectedMatch"
          class="delete-btn"
        >
          <el-icon><Delete /></el-icon>
          删除匹配
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { Document, Clock, User, Reading, School, Star, TrendCharts, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getPostsByUserIdWithDetails, deletePost } from '../../api/post'
import { useAuthStore } from '../../stores/authStore'
import ResumeA4Paper from '../../components/ResumeA4Paper.vue'
import ResumeA4PaperBlueLeft from '../../components/ResumeA4PaperBlueLeft.vue'
import ResumeA4PaperBlueTopBar from '../../components/ResumeA4PaperBlueTopBar.vue'

const matchList = ref([]) // 历史匹配列表
const selectedIndex = ref('0')
const selectedMatch = ref(null)
const selectedResume = ref(null)
const authStore = useAuthStore()

const seekerInfo = ref({ education: '', school: '', favor: '', membership: 0 })
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

const refreshMatchList = async () => {
  const userId = authStore.userId || authStore.user?.id
  try {
    const res = await getPostsByUserIdWithDetails(userId)
    if (res.data && res.data.length > 0) {
      matchList.value = res.data
      // 默认选中第一个
      await handleSelect('0')
    } else {
      matchList.value = []
      selectedMatch.value = null
      selectedResume.value = null
      selectedIndex.value = '0'
    }
  } catch (e) {
    matchList.value = []
    selectedMatch.value = null
    selectedResume.value = null
    selectedIndex.value = '0'
  }
}

const handleSelect = async (idx) => {
  selectedIndex.value = idx
  const match = matchList.value[Number(idx)]
  selectedMatch.value = match
  if (match) {
    // 直接使用从后端返回的关联数据
    if (match.seekerInfo) {
      seekerInfo.value = match.seekerInfo
    } else {
      seekerInfo.value = { education: '', school: '', favor: '', membership: 0 }
    }
    
    if (match.resume) {
      selectedResume.value = match.resume
    } else {
      selectedResume.value = null
    }
  } else {
    seekerInfo.value = { education: '', school: '', favor: '', membership: 0 }
    selectedResume.value = null
  }
}

// 拉取历史匹配列表并刷新选中项
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

const onDeleteMatch = async () => {
  if (!selectedMatch.value || !selectedMatch.value.id) return
  try {
    await deletePost(selectedMatch.value.id)
    ElMessage.success('删除成功')
    await refreshMatchList()
  } catch {
    ElMessage.error('删除失败')
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

onMounted(async () => {
  await refreshMatchList()
  if (isImportedResume.value && importedPdfUrl.value) {
    setTimeout(() => renderPdfToCanvas(importedPdfUrl.value), 0)
  }
})
</script>

<style scoped>
.match-history-container {
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

/* 主卡片 */
.main-card {
  flex: 1;
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

/* 历史记录内容区域 */
.history-content-wrapper {
  display: flex;
  gap: 32px;
  min-height: 700px;
  align-items: flex-start;
}

.history-sidebar {
  min-width: 280px;
  max-width: 320px;
  height: 700px;
  flex-shrink: 0;
}

.history-list-menu {
  border-radius: 12px;
  background: #f8fafe;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.08);
  padding: 8px;
  height: 100%;
  border: 1px solid #e8f2ff;
  overflow: hidden;
}

.history-menu-item {
  border-radius: 8px;
  margin: 4px 0;
  font-size: 14px;
  transition: all 0.3s ease;
  padding: 16px;
  color: #333;
  position: relative;
  background: transparent;
  border: 1px solid transparent;
  min-height: auto;
}

.history-menu-item:hover {
  background: linear-gradient(90deg, #e6eaff 0%, #f4f6fb 100%);
  color: #667eea;
  border-color: #c7d2fe;
  transform: translateX(4px);
}

.history-menu-item.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  border-color: #667eea;
}

.history-menu-item.is-active::before {
  content: '';
  position: absolute;
  left: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 30px;
  border-radius: 2px;
  background: #fff;
}

.menu-item-flex {
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-item-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.match-title {
  font-weight: 500;
  font-size: 15px;
}

.match-date {
  font-size: 12px;
  opacity: 0.8;
}

.history-preview {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.resume-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 求职者信息卡片 */
.seeker-info-card {
  background: #fffbf0;
  border: 1px solid #fef3c7;
  border-radius: 12px;
}

.seeker-info-card .el-card__header {
  background: #fef3c7;
  border-bottom: 1px solid #fbbf24;
}

.info-card-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #92400e;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  padding: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #667eea;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.favor-tag {
  margin-right: 6px;
  margin-bottom: 4px;
}

.no-data {
  color: #999;
  font-style: italic;
}

/* 简历预览卡片 */
.resume-preview-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.resume-preview-card .el-card__header {
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.resume-preview-card .info-card-header h4 {
  color: #333;
}

.resume-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.resume-a4-paper {
  width: 794px;
  min-height: 900px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border-radius: 8px;
  max-width: 100%;
}

/* PDF 渲染样式 */
.pdf-a4-fixed-wrapper {
  width: 794px;
  height: 900px;
  max-width: 100%;
  max-height: 100%;
  background: #fff;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.pdf-a4-canvas {
  width: 794px;
  height: 900px;
  background: #fff;
  display: block;
  border-radius: 8px;
}

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

.empty-state {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

/* 操作区域 */
.action-area {
  text-align: center;
  padding: 24px 0;
  border-top: 1px solid #e5e7eb;
  margin-top: 24px;
}

.delete-btn {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border: none;
  font-weight: 600;
  padding: 0 32px;
  height: 44px;
  font-size: 15px;
  border-radius: 22px;
  transition: all 0.3s ease;
}

.delete-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(239, 68, 68, 0.3);
}

.delete-btn:disabled {
  background: #d1d5db;
  color: #9ca3af;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .match-history-container {
    max-width: 100%;
    padding: 16px;
  }
  
  .history-content-wrapper {
    gap: 20px;
    min-height: auto;
  }
  
  .history-sidebar {
    min-width: 240px;
    max-width: 260px;
    height: auto;
    min-height: 400px;
  }
  
  .history-list-menu {
    min-height: 400px;
  }
}

@media (max-width: 768px) {
  .match-history-container {
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
  
  .history-content-wrapper {
    flex-direction: column;
    gap: 16px;
  }
  
  .history-sidebar {
    min-width: auto;
    max-width: 100%;
    width: 100%;
    height: auto;
    min-height: auto;
  }
  
  .history-list-menu {
    display: flex;
    flex-direction: row;
    overflow-x: auto;
    overflow-y: hidden;
    padding: 8px;
    height: auto;
    min-height: auto;
  }
  
  .history-menu-item {
    min-width: 180px;
    margin: 0 4px;
    text-align: center;
    flex-shrink: 0;
  }
  
  .history-preview {
    width: 100%;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 12px;
  }
  
  .info-item.full-width {
    grid-column: 1;
  }
  
  .resume-wrapper {
    padding: 12px;
  }
  
  .pdf-a4-fixed-wrapper {
    width: 100%;
    height: auto;
    max-height: 600px;
  }
  
  .pdf-a4-canvas {
    width: 100%;
    height: auto;
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
  
  .delete-btn {
    padding: 0 24px;
    height: 40px;
    font-size: 14px;
  }
  
  .history-menu-item {
    min-width: 160px;
    padding: 12px;
  }
  
  .match-title {
    font-size: 14px;
  }
  
  .match-date {
    font-size: 11px;
  }
}
</style>
