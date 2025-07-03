<template>
  <div class="resume-detail">
    <!-- 顶部：与ResumeList一致的信息区 -->
    <div class="detail-header">
      <div class="user-info">
        <el-avatar :src="info.avatar" :size="80" />
        <div class="user-basic">
          <h2 class="user-name">
            {{ info.realname || '姓名' }}
          </h2>
          <div class="favor-tags">
            <el-tag v-for="favor in parseFavor(seekerInfo.favor)" :key="favor" class="favor-tag" effect="plain">
              {{ favor }}
            </el-tag>
          </div>
          <div class="user-tags">
            <el-tag class="basic-tag" v-if="seekerInfo.education">学历：{{ getEducationText(seekerInfo.education) }}</el-tag>
            <el-tag class="basic-tag" v-if="seekerInfo.school">毕业院校：{{ seekerInfo.school }}</el-tag>
            <el-tag class="basic-tag" v-if="info.phone">手机号：{{ info.phone }}</el-tag>
            <el-tag class="basic-tag" v-if="info.email">邮箱：{{ info.email }}</el-tag>
          </div>
        </div>
      </div>
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="startChat">
          <el-icon><ChatDotRound /></el-icon>
          发起沟通
        </el-button>
      </div>
    </div>

    <!-- 简历A4纸风格内容区 -->
    <div class="resume-a4-wrapper">
      <template v-if="isImportedResume">
        <div class="pdf-a4-fixed-wrapper">
          <canvas ref="pdfCanvasRef" class="pdf-a4-canvas"></canvas>
          <div v-if="pdfLoading" class="pdf-loading-mask">PDF加载中...</div>
        </div>
        <div class="resume-create-time-a4">
          上传时间：<span class="resume-label">{{ formatDate(resume.createTime) }}</span>
        </div>
      </template>
      <template v-else>
        <component
          :is="resumeA4Component"
          :content="parsedResumeContent"
          :avatar="resume.avatar"
        />
        <div class="resume-create-time-a4">
          创建时间：<span class="resume-label">{{ formatDate(resume.createTime) }}</span>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { hrApi } from '../../../api/hr.js'
import { useAuthStore } from '../../../stores/authStore.js'
import ResumeA4Paper from '../../../components/ResumeA4Paper.vue'
import ResumeA4PaperBlueLeft from '../../../components/ResumeA4PaperBlueLeft.vue'
import ResumeA4PaperBlueTopBar from '../../../components/ResumeA4PaperBlueTopBar.vue'

const props = defineProps({
  resume: { type: Object, required: true },
  info: { type: Object, required: false, default: () => ({}) }, // t_info
  seekerInfo: { type: Object, required: false, default: () => ({}) } // t_seeker_info
})

const emit = defineEmits(['start-chat'])
const router = useRouter()
const authStore = useAuthStore()

// 解析简历内容（严格仿照JobSeeker/ResumeView.vue）
const parsedResumeContent = computed(() => {
  if (!props.resume || !props.resume.content) return {}
  try {
    const obj = JSON.parse(props.resume.content)
    return obj && typeof obj === 'object' ? obj : {}
  } catch {
    return { 内容: props.resume.content }
  }
})

const isImportedResume = computed(() => {
  return parsedResumeContent.value && parsedResumeContent.value.importType === 'pdf' && parsedResumeContent.value.pdfUrl
})
const importedPdfUrl = computed(() => {
  return isImportedResume.value ? parsedResumeContent.value.pdfUrl : ''
})

const resumeA4Component = computed(() => {
  const template = parsedResumeContent.value.template || '1'
  if (template === '2') return ResumeA4PaperBlueLeft
  else if (template === '3') return ResumeA4PaperBlueTopBar
  return ResumeA4Paper
})

// PDF.js渲染相关
const pdfCanvasRef = ref(null)
const pdfLoading = ref(false)
const pdfDocCache = new Map()
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
watch(importedPdfUrl, (url) => {
  if (isImportedResume.value && url) {
    setTimeout(() => renderPdfToCanvas(url), 0)
  }
})
onMounted(() => {
  if (isImportedResume.value && importedPdfUrl.value) {
    setTimeout(() => renderPdfToCanvas(importedPdfUrl.value), 0)
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

const startChat = async () => {
  try {
    // 获取当前HR用户ID
    const hrUserId = authStore.userId
    if (!hrUserId) {
      ElMessage.error('请先登录')
      return
    }

    // 获取目标求职者用户ID
    const targetUserId = props.resume.userId
    if (!targetUserId) {
      ElMessage.error('无法获取求职者信息')
      return
    }

    // 获取当前简历ID
    const resumeId = props.resume.id
    if (!resumeId) {
      ElMessage.error('无法获取简历信息')
      return
    }

    // 显示加载状态
    const loading = ElMessage({
      message: '正在发起沟通...',
      type: 'info',
      duration: 3000
    })

    // 构建自定义消息，包含简历ID
    const customMessage = `你好，我是腾讯的HR，对你的这份简历很感兴趣，希望能和你聊一聊。resume:${resumeId}`

    // 调用发起沟通API，传递自定义消息
    const response = await hrApi.initiateChat(hrUserId, targetUserId, customMessage)

    // 关闭加载状态
    loading.close()

    if (response.data && response.data.success) {
      ElMessage.success(`沟通已发起`)

      // 跳转到聊天窗口
      router.push(`/chat?userId=${targetUserId}`)

      // 发出事件通知父组件（可选，用于关闭对话框等）
      emit('start-chat', props.resume)
    } else {
      ElMessage.error(response.data?.error || '发起沟通失败')
    }
  } catch (error) {
    console.error('发起沟通失败:', error)
    ElMessage.error(error.response?.data?.error || '发起沟通失败，请稍后重试')
  }
}

// favor解析
function parseFavor(favor) {
  if (!favor) return []
  try {
    const arr = JSON.parse(favor)
    return Array.isArray(arr) ? arr : [favor]
  } catch {
    return favor ? [favor] : []
  }
}

function getEducationText(educationValue) {
  const educationMap = {
    1: '小学',
    2: '初中',
    3: '高中',
    4: '大专',
    5: '本科',
    6: '硕士',
    7: '博士'
  }
  return educationMap[educationValue] || '未填写'
}
</script>

<style scoped>
.resume-detail {
  max-height: 80vh;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f0ff 100%);
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-basic {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-name {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.favor-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 6px 0 4px 0;
}

.favor-tag {
  background: #eaf3ff;
  color: #3a36db;
  border: 1px solid #e0e7ef;
  font-size: 12px;
  font-weight: 500;
  border-radius: 12px;
  padding: 2px 12px;
  transition: background 0.2s, color 0.2s, border 0.2s;
  box-shadow: none;
  margin: 0;
}

.favor-tag:hover {
  background: #d2e6ff;
  color: #222;
  border-color: #b3bff7;
}

.user-tags {
  display: flex;
  gap: 8px;
}

.basic-tag {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.resume-a4-wrapper {
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
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
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #409eff;
  z-index: 10;
}

.resume-create-time-a4 {
  margin-top: 24px;
  text-align: right;
}

.resume-label {
  font-size: 14px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .user-info {
    flex-direction: column;
    text-align: center;
  }

  .action-buttons {
    width: 100%;
    justify-content: center;
  }
}
</style>
