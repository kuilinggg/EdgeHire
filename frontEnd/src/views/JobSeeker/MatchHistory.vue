<template>
  <div class="resume-view-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="resume-title">历史匹配</span>
        </div>
      </template>
      <el-row :gutter="32" class="resume-main-row">
        <template v-if="matchList.length > 0">
          <el-col :span="5" class="resume-list-col">
            <el-menu
              :default-active="selectedIndex"
              @select="handleSelect"
              class="resume-list-menu"
              style="height: 100%"
            >
              <el-menu-item
                v-for="(item, idx) in matchList"
                :key="item.id"
                :index="String(idx)"
                class="resume-menu-item"
              >
                <div class="menu-item-flex">
                  <el-icon style="margin-right: 4px;"><Document /></el-icon>
                  <span>匹配{{ idx + 1 }}</span>
                </div>
              </el-menu-item>
            </el-menu>
          </el-col>
          <el-col :span="19" class="resume-a4-col">
            <div v-if="selectedResume" class="resume-a4-wrapper">
              <!-- 求职者信息卡片，始终显示 -->
              <el-card class="seekerinfo-card" style="margin-bottom: 18px;">
                <div class="profile-view">
                  <p><strong>学历：</strong>{{ educationText }}</p>
                  <p><strong>学校：</strong>{{ seekerInfo.school || '-' }}</p>
                  <p><strong>理想岗位：</strong>
                    <template v-if="favorList.length > 0">
                      <el-tag v-for="(item, idx) in favorList" :key="idx" type="info" style="margin-right: 8px;">{{ item }}</el-tag>
                    </template>
                    <template v-else>-</template>
                  </p>
                  <p><strong>会员类型：</strong>{{ seekerInfo.membership == 0 ? '普通会员': '高级会员' }}</p>
                </div>
              </el-card>
              <!-- 简历内容区分类型显示 -->
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
            <el-empty v-else description="请选择左侧历史匹配" />
          </el-col>
        </template>
        <template v-else>
          <el-col :span="24">
            <el-empty description="暂无历史匹配数据" />
          </el-col>
        </template>
      </el-row>
      <div class="button-area" v-if="matchList.length > 0">
        <el-button type="danger" size="large" @click="onDeleteMatch" :disabled="!selectedMatch">删除匹配</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { Document } from '@element-plus/icons-vue'
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

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  height: 64px;
}
.resume-title {
  font-size: 24px;
  font-weight: bold;
  color: #222;
  letter-spacing: 1px;
  line-height: 1.2;
}
.seekerinfo-card {
  margin-bottom: 24px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 4px 24px 0 rgba(64,158,255,0.08), 0 1.5px 6px 0 rgba(0,0,0,0.04);
  width: 794px;
  max-width: 100%;
  margin-left: auto;
  margin-right: auto;
}
.profile-view {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 16px 0 8px 0;
  width: 100%;
}
.profile-view p {
  margin: 10px 0 4px 0;
  font-size: 16px;
  color: #333;
  letter-spacing: 0.5px;
  text-align: left;
  width: 100%;
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
  width: 794px;
  min-height: 1123px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  box-shadow: 0 2px 12px #eee;
}
.button-area {
  text-align: right;
  padding: 16px 0;
}

.pdf-viewer {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 16px;
}

.pdf-canvas {
  width: 100%;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
}

.pdf-loading {
  margin: 16px 0;
  font-size: 16px;
  color: #666;
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

@media (max-width: 1200px) {
  .el-card {
    width: 100vw;
    min-width: unset;
    max-width: 100vw;
    border-radius: 0;
  }
}

@media (max-width: 900px) {
  .resume-view-container {
    padding: 8px 0;
  }
  .resume-a4-col {
    min-width: unset;
  }
}
</style>
