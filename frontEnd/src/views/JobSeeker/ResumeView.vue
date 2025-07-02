<template>
  <div class="resume-view-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="resume-title">查看简历</span>
          <div class="header-bar-spacer"></div>
          <el-button type="primary" class="import-btn" size="medium" @click="handleImportClick">
            <el-icon style="margin-right:4px;"><Upload /></el-icon>
            导入简历（PDF）
          </el-button>
        </div>
      </template>
      <el-dialog v-model="importDialogVisible" title="导入PDF简历" width="400px" :close-on-click-modal="false">
        <input type="file" accept="application/pdf" @change="handleFileChange" />
        <template #footer>
          <el-button @click="importDialogVisible=false">取消</el-button>
          <el-button type="primary" :loading="importLoading" :disabled="!importFile" @click="handleImportConfirm">上传</el-button>
        </template>
      </el-dialog>
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
              <template v-if="isImportedResume">
                <div class="pdf-a4-fixed-wrapper">
                  <canvas ref="pdfCanvasRef" class="pdf-a4-canvas"></canvas>
                  <div v-if="pdfLoading" class="pdf-loading-mask">PDF加载中...</div>
                </div>
                <div class="resume-a4-ops-bar">
                  <div class="resume-create-time-a4">
                    上传时间：<span class="resume-label">{{ formatDate(selectedResume.createTime) }}</span>
                  </div>
                  <div style="margin-top: 32px; text-align: center; display: flex; justify-content: center; gap: 16px;">
                    <el-button type="success" size="large" icon="Document" :href="importedPdfUrl" target="_blank">下载PDF</el-button>
                    <el-popconfirm title="确定删除该简历？" @confirm="onDeleteSelected">
                      <template #reference>
                        <el-button type="danger" size="large" icon="Delete" class="delete-btn-main">删除当前简历</el-button>
                      </template>
                    </el-popconfirm>
                  </div>
                </div>
              </template>
              <template v-else>
                <component
                  :is="resumeA4Component"
                  :content="parsedResumeContent"
                  :avatar="selectedResume.avatar"
                  class="resume-a4-paper"
                />
                <div class="resume-a4-ops-bar">
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
              </template>
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
import { onMounted as vueOnMounted, ref, computed, watch } from 'vue'
import { getResumesByUserId, deleteResume, createResume } from '../../api/resume'
import { useAuthStore } from '../../stores/authStore'
import { useRouter } from 'vue-router'
import { Document, Delete, Edit, Calendar, Phone, Message, Location, User, InfoFilled, Reading, Briefcase, Suitcase, Star, Upload } from '@element-plus/icons-vue'
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

function handleFileChange(e) {
  const file = e.target.files[0]
  if (file && file.type === 'application/pdf') {
    importFile.value = file
  } else {
    ElMessage.error('请上传PDF文件')
  }
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
  padding: 32px 0;
  min-height: 100vh;
  background: #f4f6fa;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.el-card {
  box-shadow: 0 4px 32px rgba(64,158,255,0.10), 0 1.5px 6px 0 rgba(0, 0, 0, 0.04);
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

.resume-a4-ops-bar {
  width: 794px;
  max-width: 100%;
  margin: 0 auto 18px auto;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.pdf-viewer-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 0;
  box-sizing: border-box;
}

.pdf-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

.pdf-link {
  font-size: 18px;
  color: #409eff;
  text-decoration: underline;
}

.pdf-a4-fixed-wrapper {
  width: 794px;
  height: 1123px;
  max-width: 100%;
  max-height: 100%;
  background: #fff;
  box-shadow: 0 2px 12px #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}
.pdf-a4-canvas {
  width: 794px;
  height: 1123px;
  background: #fff;
  display: block;
}
.pdf-loading-mask {
  position: absolute;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(255,255,255,0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #409eff;
  z-index: 10;
}

.header-bar {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  position: relative;
}
.header-bar-spacer {
  flex: 1 1 auto;
}
.import-btn {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  letter-spacing: 1px;
  padding: 0 12px;
  height: 30px;
  font-size: 14px;
  min-width: 0;
  box-shadow: none;
  transition: background 0.2s;
}
.import-btn:hover {
  background: #337ecc;
  box-shadow: none;
}

.resume-title {
  font-size: 20px;
}

@media (max-width: 1200px) {
  .el-card {
    width: 100vw;
    min-width: unset;
    max-width: 100vw;
    border-radius: 0;
  }
  .resume-a4-ops-bar, .resume-create-time-a4 {
    width: 100vw !important;
    min-width: unset;
    border-radius: 0;
    padding: 0 2vw;
  }
}

@media (max-width: 900px) {
  .resume-view-container {
    padding: 8px 0;
  }
  .resume-a4-col {
    min-width: unset;
  }
  .resume-a4-ops-bar, .resume-create-time-a4 {
    width: 100vw !important;
    min-width: unset;
    border-radius: 0;
    padding: 0;
  }
}
</style>
