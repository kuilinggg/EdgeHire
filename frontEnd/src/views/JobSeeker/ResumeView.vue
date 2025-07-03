<template>
  <div class="resume-view-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Document /></el-icon>
          简历管理
        </h1>
        <p class="page-subtitle">查看和管理您的简历，打造完美求职形象</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleImportClick" :icon="Upload">
          导入简历（PDF）
        </el-button>
      </div>
    </div>

    <!-- 主要内容 -->
    <el-card class="main-card" shadow="never" v-if="resumeList.length > 0">
      <template #header>
        <div class="card-header">
          <h3>
            <el-icon><Folder /></el-icon>
            简历列表
          </h3>
          <span class="resume-count">共 {{ resumeList.length }} 份简历</span>
        </div>
      </template>
      
      <div class="resume-content">
        <!-- 左侧简历列表 -->
        <div class="resume-list">
          <div 
            v-for="(item, idx) in resumeList" 
            :key="item.id"
            :class="['resume-item', { 'active': selectedIndex === String(idx) }]"
            @click="handleSelect(String(idx))"
          >
            <div class="resume-item-header">
              <div class="resume-title">
                <el-icon><Document /></el-icon>
                <span>简历 {{ idx + 1 }}</span>
              </div>
              <el-tag v-if="item.isPdf" type="success" size="small">PDF</el-tag>
              <el-tag v-else type="primary" size="small">在线</el-tag>
            </div>
            <div class="resume-meta">
              <div class="meta-item">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(item.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧简历预览 -->
        <div class="resume-preview">
          <div v-if="selectedResume" class="preview-content">
            <div class="preview-header">
              <h4>简历预览</h4>
              <div class="preview-actions">
                <el-button v-if="isImportedResume" type="success" :icon="Document" :href="importedPdfUrl" target="_blank">
                  下载PDF
                </el-button>
                <el-button v-else type="success" :icon="Document" @click="exportPdf">
                  导出PDF
                </el-button>
                <el-button v-if="!isImportedResume" type="primary" :icon="Edit" @click="onEditSelected">
                  编辑
                </el-button>
                <el-button type="danger" :icon="Delete" @click="confirmDelete">
                  删除
                </el-button>
              </div>
            </div>
            
            <div class="preview-body">
              <template v-if="isImportedResume">
                <div class="pdf-preview-wrapper">
                  <canvas ref="pdfCanvasRef" class="pdf-canvas"></canvas>
                  <div v-if="pdfLoading" class="pdf-loading">PDF加载中...</div>
                </div>
              </template>
              <template v-else>
                <component
                  :is="resumeA4Component"
                  :content="parsedResumeContent"
                  :avatar="selectedResume.avatar"
                  class="resume-component"
                />
              </template>
            </div>
          </div>
          
          <div v-else class="empty-preview">
            <el-empty description="请选择左侧简历进行预览" />
          </div>
        </div>
      </div>
    </el-card>

    <!-- 空状态 -->
    <el-card v-else class="empty-card" shadow="never">
      <el-empty description="暂无简历">
        <template #image>
          <el-icon class="empty-icon"><Document /></el-icon>
        </template>
        <template #description>
          <div class="empty-description">
            <p>您还没有创建任何简历</p>
            <p>开始创建您的第一份简历吧</p>
          </div>
        </template>
        <el-button type="primary" @click="handleImportClick" :icon="Upload">
          导入PDF简历
        </el-button>
      </el-empty>
    </el-card>

    <!-- 导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入PDF简历" width="500px" :close-on-click-modal="false">
      <div class="import-dialog-content">
        <el-upload
          ref="uploadRef"
          class="upload-demo"
          :auto-upload="false"
          :on-change="handleFileChange"
          :file-list="fileList"
          accept="application/pdf"
          drag
        >
          <el-icon class="el-icon--upload"><Upload /></el-icon>
          <div class="el-upload__text">
            将PDF文件拖拽到此处，或<em>点击选择文件</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传PDF文件，且不超过10MB
            </div>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="importLoading" :disabled="!importFile" @click="handleImportConfirm">
            确认上传
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="400px">
      <div class="delete-dialog-content">
        <el-icon class="warning-icon"><Warning /></el-icon>
        <div class="warning-text">
          <p>确定要删除这份简历吗？</p>
          <p>删除后将无法恢复</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" :loading="deleteLoading" @click="onDeleteSelected">
            确认删除
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted as vueOnMounted, ref, computed, watch } from 'vue'
import { getResumesByUserId, deleteResume, createResume } from '../../api/resume'
import { useAuthStore } from '../../stores/authStore'
import { useRouter } from 'vue-router'
import { Document, Delete, Edit, Calendar, Upload, Folder, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import html2pdf from 'html2pdf.js'
import domtoimage from 'dom-to-image'
import ResumeA4Paper from '../../components/ResumeA4Paper.vue'
import ResumeA4PaperBlueLeft from '../../components/ResumeA4PaperBlueLeft.vue'
import ResumeA4PaperBlueTopBar from '../../components/ResumeA4PaperBlueTopBar.vue'
import { uploadFile } from '../../util/upload'

const resumeList = ref([])
const selectedIndex = ref('0')
const selectedResume = ref(null)
const authStore = useAuthStore()
const router = useRouter()
const importDialogVisible = ref(false)
const importFile = ref(null)
const importLoading = ref(false)

// 新增响应式变量
const deleteDialogVisible = ref(false)
const deleteLoading = ref(false)
const fileList = ref([])

const handleSelect = (idx) => {
  selectedIndex.value = idx
  selectedResume.value = resumeList.value[Number(idx)]
}

const onDeleteSelected = async () => {
  if (!selectedResume.value) return
  const id = selectedResume.value.id
  deleteLoading.value = true
  try {
    await deleteResume(id)
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    // 删除成功后刷新简历列表
    await refreshResumeList()
  } catch (e) {
    ElMessage.error('删除失败')
  } finally {
    deleteLoading.value = false
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
      // 新增：根据路由 query.id 精准定位
      const routeId = router.currentRoute.value.query.id
      let idx = 0
      if (routeId) {
        idx = res.data.findIndex(item => String(item.id) === String(routeId))
        if (idx === -1) idx = 0
      }
      selectedResume.value = res.data[idx]
      selectedIndex.value = String(idx)
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

async function coverSectionTitlesWithImages() {
  const nodes = Array.from(document.querySelectorAll('.resume-a4-section-title'));
  for (const node of nodes) {
    if (node.querySelector('.section-title-img-cover')) continue;
    const dataUrl = await domtoimage.toPng(node, {bgcolor: null});
    const img = document.createElement('img');
    img.src = dataUrl;
    img.className = 'section-title-img-cover';
    img.style.position = 'absolute';
    img.style.left = '0';
    img.style.top = '0';
    img.style.width = node.offsetWidth + 'px';
    img.style.height = node.offsetHeight + 'px';
    img.style.zIndex = '10';
    img.style.pointerEvents = 'none';
    node.style.position = 'relative';
    node.appendChild(img);
  }
}
function removeSectionTitleImageCovers() {
  document.querySelectorAll('.section-title-img-cover').forEach(img => img.remove());
}

//导出简历为PDF
async function exportPdf() {
  await coverSectionTitlesWithImages();
  setTimeout(() => {
    const element = document.querySelector('.resume-a4-paper')
    if (!element) {
      ElMessage.error('没有可导出的简历内容')
      removeSectionTitleImageCovers();
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
        removeSectionTitleImageCovers();
      })
      .catch((err) => {
        console.error('导出失败:', err)
        ElMessage.error('导出失败，请稍后重试')
        removeSectionTitleImageCovers();
      })
  }, 100)
}

const resumeA4Component = computed(() => {
  const template = parsedResumeContent.value.template || '1'
  if (template === '2') return ResumeA4PaperBlueLeft
  else if (template === '3') return ResumeA4PaperBlueTopBar
  // 未来可扩展更多模板
  return ResumeA4Paper
})

const isImportedResume = computed(() => {
  // 判断content字段是否为导入PDF类型
  return parsedResumeContent.value && parsedResumeContent.value.importType === 'pdf' && parsedResumeContent.value.pdfUrl
})

const importedPdfUrl = computed(() => {
  return isImportedResume.value ? parsedResumeContent.value.pdfUrl : ''
})

function handleImportClick() {
  importDialogVisible.value = true
  importFile.value = null
}

async function handleImportConfirm() {
  if (!importFile.value) return
  importLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    const res = await uploadFile(formData)
    if (res && res.data) {
      // 创建一条content为JSON字符串、avatar为空的简历，兼容ResumeEdit.vue
      const userId = authStore.userId || authStore.user?.id
      const pdfUrl = res.data
      const data = {
        userId,
        content: JSON.stringify({ importType: 'pdf', pdfUrl }),
        avatar: ''
      }
      await createResume(data)
      ElMessage.success('导入成功')
      importDialogVisible.value = false
      await refreshResumeList()
    } else {
      ElMessage.error('上传失败')
    }
  } catch (e) {
    console.log(e)
    ElMessage.error('导入失败')
  } finally {
    importLoading.value = false
  }
}

function handleFileChange(file) {
  importFile.value = file.raw
  fileList.value = [file]
}

// PDF.js渲染相关
const pdfCanvasRef = ref(null)
const pdfLoading = ref(false)
const pdfDocCache = new Map() // url => pdf对象

// 动态加载PDF.js脚本（页面初始化时就加载）
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

// 页面初始化时预加载PDF.js脚本
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
    // 计算缩放比例以适配A4区域
    const targetWidth = 794
    const targetHeight = 1123
    const scale = Math.min(targetWidth / viewport.width, targetHeight / viewport.height)
    const scaledViewport = page.getViewport({ scale })
    const canvas = pdfCanvasRef.value
    const context = canvas.getContext('2d')
    canvas.width = scaledViewport.width
    canvas.height = scaledViewport.height
    // 清空
    context.clearRect(0, 0, canvas.width, canvas.height)
    await page.render({ canvasContext: context, viewport: scaledViewport }).promise
  } catch (e) {
    console.error('PDF渲染失败', e)
  } finally {
    pdfLoading.value = false
  }
}

// 监听importedPdfUrl变化，渲染PDF
watch(importedPdfUrl, (url) => {
  if (isImportedResume.value && url) {
    setTimeout(() => renderPdfToCanvas(url), 0)
  }
})

vueOnMounted(() => {
  refreshResumeList()
  // 首次渲染
  if (isImportedResume.value && importedPdfUrl.value) {
    setTimeout(() => renderPdfToCanvas(importedPdfUrl.value), 0)
  }
})
</script>

<style scoped>
.resume-view-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
}

/* 页面头部样式 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px 0;
  margin: -24px -24px 24px -24px;
  border-radius: 0 0 24px 24px;
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 100" fill="white" fill-opacity="0.1"><polygon points="0,0 1000,0 1000,100 0,20"/></svg>');
  background-size: cover;
  background-position: bottom;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title .el-icon {
  font-size: 36px;
}

.page-subtitle {
  font-size: 16px;
  margin: 8px 0 0 0;
  opacity: 0.9;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 主卡片样式 */
.main-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  border: none;
  margin-bottom: 24px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
}

.card-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 8px;
}

.resume-count {
  color: #718096;
  font-size: 14px;
}

/* 简历内容区域 */
.resume-content {
  display: flex;
  gap: 24px;
  min-height: 600px;
}

.resume-list {
  flex: 0 0 300px;
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  max-height: 600px;
  overflow-y: auto;
}

.resume-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.resume-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.resume-item.active {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102,126,234,0.2);
}

.resume-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.resume-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #2d3748;
}

.resume-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #718096;
  font-size: 14px;
}

/* 简历预览区域 */
.resume-preview {
  flex: 1;
  background: #f8fafc;
  border-radius: 12px;
  padding: 24px;
  overflow: hidden;
}

.preview-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.preview-header h4 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.preview-actions {
  display: flex;
  gap: 12px;
}

.preview-body {
  flex: 1;
  overflow: auto;
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.pdf-preview-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  min-height: 400px;
}

.pdf-canvas {
  max-width: 100%;
  max-height: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.pdf-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #667eea;
  font-weight: 500;
}

.resume-component {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.empty-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #718096;
}

/* 空状态卡片 */
.empty-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  border: none;
  padding: 60px 24px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  color: #cbd5e0;
  margin-bottom: 16px;
}

.empty-description {
  margin-bottom: 24px;
}

.empty-description p {
  margin: 8px 0;
  color: #718096;
  font-size: 16px;
}

/* 对话框样式 */
.import-dialog-content {
  padding: 20px 0;
}

.upload-demo {
  width: 100%;
}

.delete-dialog-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 0;
}

.warning-icon {
  font-size: 48px;
  color: #f56565;
}

.warning-text p {
  margin: 4px 0;
  color: #2d3748;
  font-size: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 按钮样式 */
.el-button {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.el-button--primary:hover {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(102,126,234,0.4);
}

.el-button--success {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  border: none;
  color: white;
}

.el-button--success:hover {
  background: linear-gradient(135deg, #38a169 0%, #2f855a 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(72,187,120,0.4);
}

.el-button--danger {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
  border: none;
  color: white;
}

.el-button--danger:hover {
  background: linear-gradient(135deg, #e53e3e 0%, #c53030 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(245,101,101,0.4);
}

/* 标签样式 */
.el-tag {
  border-radius: 6px;
  font-weight: 500;
  border: none;
}

.el-tag--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.el-tag--success {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .page-header {
    padding: 32px 0;
  }
  
  .resume-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .resume-list {
    flex: none;
    max-height: 300px;
  }
  
  .resume-preview {
    min-height: 500px;
  }
}

@media (max-width: 768px) {
  .resume-view-container {
    padding: 16px;
  }
  
  .page-header {
    margin: -16px -16px 16px -16px;
    padding: 24px 0;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .resume-content {
    gap: 12px;
  }
  
  .resume-list {
    max-height: 250px;
  }
  
  .preview-actions {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .preview-body {
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .resume-item {
    padding: 12px;
  }
  
  .preview-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .preview-actions {
    width: 100%;
    justify-content: center;
  }
}
</style>
