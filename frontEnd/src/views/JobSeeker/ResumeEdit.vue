<template>
  <div class="resume-edit-container" :class="{ 'with-ai-sidebar': showAiSidebar }">
    <!-- AI侧边栏 -->
    <transition name="ai-sidebar">
      <div v-if="showAiSidebar" class="ai-sidebar">
        <div class="ai-sidebar-header">
          <span class="ai-sidebar-title">AI 助手</span>
          <el-button type="text" @click="closeAiSidebar" class="close-btn">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      <div class="ai-chat-container">
        <div class="ai-messages" ref="messagesContainer">
          <div v-for="(message, index) in aiMessages" :key="index" 
               :class="['message', message.type === 'user' ? 'user-message' : 'ai-message', 
                       { 'streaming': message.type === 'ai' && isAiTyping && index === aiMessages.length - 1 }]">
            <div class="message-content">
              <div v-if="message.type === 'ai'" class="ai-content" v-html="renderMarkdown(message.content)"></div>
              <div v-else class="user-content">{{ message.content }}</div>
            </div>
            <div class="message-time">{{ message.time }}</div>
            <!-- 如果是AI消息且包含错误信息，显示重试按钮 -->
            <div v-if="message.type === 'ai' && message.content.includes('抱歉') && !isAiTyping" class="message-actions">
              <el-button size="small" type="text" @click="retryLastMessage" class="retry-btn">
                <el-icon><Refresh /></el-icon>
                重试
              </el-button>
            </div>
          </div>
          <div v-if="isAiTyping && aiMessages.length === 0" class="typing-indicator">
            <div class="typing-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
        <div class="ai-input-area">
          <div class="input-controls">
            <el-button size="small" @click="newConversation" class="new-chat-btn">
              <el-icon><Plus /></el-icon>
              新建对话
            </el-button>
          </div>
          <div class="input-wrapper">
            <div class="input-container">
              <el-input
                v-model="userInput"
                type="textarea"
                :rows="3"
                placeholder="输入您的问题，比如：优化我的简历内容、改进自我评价等..."
                @keydown.ctrl.enter="sendMessage"
                @keydown.meta.enter="sendMessage"
                class="user-input"
              />
              <el-button 
                type="primary" 
                @click="sendMessage" 
                :disabled="!userInput.trim() || isAiTyping"
                class="send-btn"
              >
                <el-icon><Promotion /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    </transition>
    
    <el-card :class="{ 'with-ai-sidebar': showAiSidebar }" class="main-content">
      <template #header>
        <div class="header-bar">
          <span class="resume-title">填写简历</span>
          <el-button type="success" size="small" class="ai-btn" @click="onAiOptimize">
            <el-icon style="margin-right:4px;">
              <User />
            </el-icon>AI优化
          </el-button>
        </div>
      </template>
      <el-form :model="form.Resume" :rules="rules" ref="formRef" label-width="100px" class="resume-form"
        @change="autoSave">
        <el-row :gutter="24">
          <el-col :span="24">
            <!-- 模板选择下拉框 -->
            <el-form-item label="模板选择" prop="template">
              <el-select v-model="form.Resume.template" placeholder="请选择简历模板" style="width: 220px;">
                <el-option label="模板一" value="1" />
                <el-option label="模板二" value="2" />
                <el-option label="模板三" value="3" />
              </el-select>
              <el-button size="small" style="margin-left:12px;" @click="previewDialogVisible = true">预览</el-button>
            </el-form-item>
            <el-dialog v-model="previewDialogVisible" title="简历预览" width="850px" top="40px" :close-on-click-modal="false">
              <component :is="currentPreviewComponent" :content="form.Resume" :avatar="form.avatar" />
            </el-dialog>
            <el-row>
              <el-col :span="18">
                <el-divider>基本信息</el-divider>
                <el-form-item label="姓名" prop="姓名">
                  <el-input v-model="form.Resume.姓名" placeholder="请输入姓名" />
                </el-form-item>
                <el-form-item label="求职意向" prop="求职意向">
                  <el-input v-model="form.Resume.求职意向" placeholder="如：前端开发实习生" />
                </el-form-item>
              </el-col>
              <el-col :span="6" style="display:flex;align-items:center;justify-content:center;">
                <div class="avatar-box">
                  <ImgCutter v-on:cutDown="onAvatarCrop" rate="3:4" size-change="false">
                    <template #open>
                      <el-avatar v-if="form.avatar" :src="form.avatar" size="large" shape="square" class="resume-avatar"/>
                      <div v-if="!form.avatar" class="el-upload__text">点击上传头像</div>
                    </template>
                  </ImgCutter>
                </div>
              </el-col>
            </el-row>
            <el-divider>个人信息</el-divider>
            <el-row :gutter="12">
              <el-col :span="8"><el-form-item label="年龄" prop="个人信息.年龄"><el-input v-model="form.Resume.个人信息.年龄"
                    placeholder="如：22" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="电话" prop="个人信息.电话"><el-input v-model="form.Resume.个人信息.电话"
                    placeholder="如：138****8888" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="民族" prop="个人信息.民族"><el-input v-model="form.Resume.个人信息.民族"
                    placeholder="如：汉族" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="8"><el-form-item label="邮箱" prop="个人信息.邮箱"><el-input v-model="form.Resume.个人信息.邮箱"
                    placeholder="如：xxx@email.com" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="政治面貌" prop="个人信息.政治面貌"><el-input v-model="form.Resume.个人信息.政治面貌"
                    placeholder="如：共青团员" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="籍贯" prop="个人信息.籍贯"><el-input v-model="form.Resume.个人信息.籍贯"
                    placeholder="如：江苏南京" /></el-form-item></el-col>
            </el-row>
            <el-divider>教育背景</el-divider>
            <el-form-item label="学校" prop="教育背景.学校">
              <el-input v-model="form.Resume.教育背景.学校" placeholder="如：武汉大学" />
            </el-form-item>
            <el-form-item label="学历" prop="教育背景.学历">
              <el-input v-model="form.Resume.教育背景.学历" placeholder="如：本科" />
            </el-form-item>
            <el-form-item label="专业" prop="教育背景.专业">
              <el-input v-model="form.Resume.教育背景.专业" placeholder="如：软件工程" />
            </el-form-item>
            <el-form-item label="时间" prop="教育背景.时间">
              <el-input v-model="form.Resume.教育背景.时间" placeholder="如：2021-09 - 2025-06" />
            </el-form-item>
            <el-form-item label="GPA" prop="教育背景.GPA"><el-input v-model="form.Resume.教育背景.GPA"
                placeholder="如：3.8/4.0" /></el-form-item>
            <el-form-item label="主修课程" prop="教育背景.主修课程">
              <div style="width:100%">
                <el-input v-for="(course, idx) in form.Resume.教育背景.主修课程" :key="idx" v-model="form.Resume.教育背景.主修课程[idx]"
                  :placeholder="'主修课程' + (idx + 1) + '（如：数据结构）'"
                  style="margin-bottom:4px;width:90%;display:inline-block;" />
                <el-button type="primary" size="small" @click="addCourse" style="margin-left:8px;">添加</el-button>
                <el-button v-if="form.Resume.教育背景.主修课程.length > 1" type="danger" size="small" @click="removeCourse"
                  style="margin-left:4px;">删除</el-button>
              </div>
            </el-form-item>
            <el-form-item label="个人荣誉" prop="教育背景.个人荣誉">
              <el-input v-model="form.Resume.教育背景.个人荣誉" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }"
                placeholder="如：国家奖学金、三好学生等" />
            </el-form-item>
            <el-divider>任职情况</el-divider>
            <div v-for="(job, idx) in form.Resume.任职情况" :key="idx" style="margin-bottom:12px;">
              <el-row :gutter="12">
                <el-col :span="8"><el-form-item :label="'单位' + (idx + 1)" :prop="'任职情况.' + idx + '.单位'">
                    <el-input v-model="job.单位" placeholder="如：字节跳动" />
                  </el-form-item></el-col>
                <el-col :span="8"><el-form-item label="职位" :prop="'任职情况.' + idx + '.职位'">
                    <el-input v-model="job.职位" placeholder="如：前端开发实习生" />
                  </el-form-item></el-col>
                <el-col :span="8">
                  <el-form-item label="时间" :prop="'任职情况.' + idx + '.时间'">
                    <el-input v-model="job.时间" placeholder="如：2022-07 - 2022-09" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="职责" :prop="'任职情况.' + idx + '.职责'">
                <el-input v-model="job.职责" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }"
                  placeholder="如：参与企业级管理后台开发" />
              </el-form-item>
              <el-form-item v-if="job.活动描述 !== undefined" label="活动描述" :prop="'任职情况.' + idx + '.活动描述'">
                <el-input v-model="job.活动描述" placeholder="如：组织技术分享活动" />
              </el-form-item>
              <el-form-item v-if="job.结果 !== undefined" label="结果" :prop="'任职情况.' + idx + '.结果'">
                <el-input v-model="job.结果" placeholder="如：提升团队协作效率" />
              </el-form-item>
              <el-button v-if="form.Resume.任职情况.length > 1" type="danger" size="small" @click="removeJob(idx)"
                style="margin-bottom:8px;">删除</el-button>
            </div>
            <el-button type="primary" size="small" @click="addJob" style="margin-bottom:12px;">添加单位</el-button>
            <el-divider>实习/兼职</el-divider>
            <div v-for="(exp, idx) in form.Resume.实习_兼职" :key="idx" style="margin-bottom:12px;">
              <el-row :gutter="12">
                <el-col :span="8"><el-form-item :label="'单位' + (idx + 1)" :prop="'实习_兼职.' + idx + '.单位'">
                    <el-input v-model="exp.单位" placeholder="如：腾讯" />
                  </el-form-item></el-col>
                <el-col :span="8"><el-form-item label="职位" :prop="'实习_兼职.' + idx + '.职位'">
                    <el-input v-model="exp.职位" placeholder="如：兼职助教" />
                  </el-form-item></el-col>
                <el-col :span="8">
                  <el-form-item label="时间" :prop="'实习_兼职.' + idx + '.时间'">
                    <el-input v-model="exp.时间" placeholder="如：2023-01 - 2023-03" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="职责" :prop="'实习_兼职.' + idx + '.职责'">
                <el-input v-model="exp.职责" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }"
                  placeholder="如：协助课程答疑" />
              </el-form-item>
              <el-button v-if="form.Resume.实习_兼职.length > 1" type="danger" size="small" @click="removeIntern(idx)"
                style="margin-bottom:8px;">删除</el-button>
            </div>
            <el-button type="primary" size="small" @click="addIntern" style="margin-bottom:12px;">添加单位</el-button>
            <el-divider>自我评价</el-divider>
            <el-form-item label="自我评价" prop="自我评价">
              <el-input v-model="form.Resume.自我评价" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }"
                placeholder="如：学习能力强、沟通能力好等" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="onSubmit">提交</el-button>
              <el-button type="danger" @click="onClearAll">新建</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
</div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'
import { createResume, updateResume, getResumeByUserId, getResumeById } from '../../api/resume'
import { resumeOptimizeStream, parseStreamResponse } from '../../api/ai'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Close, Plus, Promotion, Refresh } from '@element-plus/icons-vue'
import { uploadFile } from '../../util/upload'
import ImgCutter from 'vue-img-cutter'
import ResumeA4PaperBlueTopBar from '../../components/ResumeA4PaperBlueTopBar.vue'
import ResumeA4Paper from '../../components/ResumeA4Paper.vue'
import ResumeA4PaperBlueLeft from '../../components/ResumeA4PaperBlueLeft.vue'

// AI侧边栏相关状态
const showAiSidebar = ref(false)
const aiMessages = ref([])
const userInput = ref('')
const isAiTyping = ref(false)
const messagesContainer = ref(null)
const previewDialogVisible = ref(false)

const defaultForm = {
  Resume: {
    template: '1', // 默认值改为字符串
    姓名: '',
    求职意向: '',
    个人信息: {
      年龄: '', 电话: '', 民族: '', 邮箱: '', 政治面貌: '', 籍贯: ''
    },
    教育背景: {
      学校: '', 学历: '', 专业: '', 时间: '', GPA: '', 主修课程: [''], 个人荣誉: ''
    },
    任职情况: [
      { 单位: '', 职位: '', 时间: '', 职责: '' }
    ],
    实习_兼职: [
      { 单位: '', 职位: '', 时间: '', 职责: '' }
    ],
    自我评价: ''
  },
  avatar: ''
}
const form = ref(JSON.parse(JSON.stringify(defaultForm)))
const resumeId = ref(null)
const router = useRouter()
const authStore = useAuthStore()

const avatarFile = ref(null)

// 新增：根据id获取简历详情
async function loadResumeById(id) {
  try {
    const res = await getResumeById(id)
    if (res && res.data) {
      form.value.Resume = JSON.parse(res.data.content)
      form.value.avatar = res.data.avatar || ''
      resumeId.value = res.data.id
    }
  } catch (e) {
    ElMessage.error('获取简历失败')
  }
}

// 页面加载时优先加载草稿/后端或根据id加载
async function loadResumeWithDraftOrId() {
  const id = router.currentRoute.value.query.id
  if (id) {
    await loadResumeById(id)
    return
  }
  if (loadDraft()) return
  await loadResume()
}

// 页面加载时获取当前用户简历，优先本地草稿
async function loadResume() {
  const userId = authStore.userId || authStore.user?.id
  if (!userId) return
  // 优先读取本地草稿
  const draftKey = `resume_draft_${userId}`
  const draft = localStorage.getItem(draftKey)
  if (draft) {
    try {
      const parsed = JSON.parse(draft)
      form.value = parsed
      return
    } catch (e) {
      // 草稿损坏则忽略
    }
  }
}

function getDynamicRules(listKey, fields, labelMap) {
  const rules = {}
  const list = form.value.Resume[listKey]
  for (let i = 0; i < list.length; i++) {
    fields.forEach(field => {
      const prop = `${listKey}.${i}.${field}`
      rules[prop] = [{ required: true, message: `${labelMap[field]}未填写`, trigger: 'blur' }]
    })
  }
  return rules
}

function getCourseRules() {
  const rules = {}
  const courses = form.value.Resume.教育背景.主修课程
  for (let i = 0; i < courses.length; i++) {
    rules[`教育背景.主修课程.${i}`] = [{ required: true, message: `主修课程${i + 1}未填写`, trigger: 'blur' }]
  }
  return rules
}

function computeRules() {
  return {
    ...staticRules,
    ...getDynamicRules('任职情况', ['单位', '职位', '时间', '职责'], { 单位: '单位', 职位: '职位', 时间: '时间', 职责: '职责' }),
    ...getDynamicRules('实习_兼职', ['单位', '职位', '时间', '职责'], { 单位: '单位', 职位: '职位', 时间: '时间', 职责: '职责' }),
    ...getCourseRules()
  }
}

const staticRules = {
  姓名: [{ required: true, message: '姓名未填写', trigger: 'blur' }],
  求职意向: [{ required: true, message: '求职意向未填写', trigger: 'blur' }],
  '个人信息.年龄': [{ required: true, message: '年龄未填写', trigger: 'blur' }],
  '个人信息.电话': [{ required: true, message: '电话未填写', trigger: 'blur' }],
  '个人信息.民族': [{ required: true, message: '民族未填写', trigger: 'blur' }],
  '个人信息.邮箱': [
    { required: true, message: '邮箱未填写', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  '个人信息.政治面貌': [{ required: true, message: '政治面貌未填写', trigger: 'blur' }],
  '个人信息.籍贯': [{ required: true, message: '籍贯未填写', trigger: 'blur' }],
  '教育背景.学校': [{ required: true, message: '学校未填写', trigger: 'blur' }],
  '教育背景.学历': [{ required: true, message: '学历未填写', trigger: 'blur' }],
  '教育背景.专业': [{ required: true, message: '专业未填写', trigger: 'blur' }],
  '教育背景.时间': [{ required: true, message: '时间未填写', trigger: 'blur' }],
  '教育背景.GPA': [{ required: true, message: 'GPA未填写', trigger: 'blur' }],
  '教育背景.主修课程': [{ required: true, message: '主修课程未填写', trigger: 'blur' }],
  '教育背景.个人荣誉': [{ required: true, message: '个人荣誉未填写', trigger: 'blur' }],
  自我评价: [{ required: true, message: '自我评价未填写', trigger: 'blur' }],
}

const rules = computed(computeRules)
const formRef = ref()

const onAvatarCrop = (result) => {
  // result.dataURL 是 base64 图片
  form.value.avatar = result.dataURL
  // 兼容之前的本地File对象逻辑
  if (result.file) {
    avatarFile.value = result.file
    const reader = new FileReader()
    reader.onload = (e) => {
      form.value.avatar = e.target.result
    }
    reader.readAsDataURL(result.file)
  }
  // 你也可以使用 result.blob 进行上传等操作
}

const LOCAL_DRAFT_KEY = computed(() => {
  const userId = authStore.userId || authStore.user?.id || 'guest'
  return `resume_draft_${userId}`
})

// AI对话缓存相关
const AI_MESSAGES_KEY = computed(() => {
  const userId = authStore.userId || authStore.user?.id || 'guest'
  return `ai_messages_${userId}`
})

// 保存AI对话到本地缓存
function saveAiMessages() {
  try {
    const data = {
      messages: aiMessages.value,
      conversationId: localStorage.getItem('conversationId'),
      timestamp: Date.now()
    }
    localStorage.setItem(AI_MESSAGES_KEY.value, JSON.stringify(data))
  } catch (e) {
    console.error('保存AI对话失败:', e)
  }
}

// 从本地缓存加载AI对话
function loadAiMessages() {
  try {
    const str = localStorage.getItem(AI_MESSAGES_KEY.value)
    if (str) {
      const data = JSON.parse(str)
      // 检查缓存是否过期（24小时）
      const isExpired = Date.now() - data.timestamp > 24 * 60 * 60 * 1000
      if (!isExpired && data.messages && Array.isArray(data.messages)) {
        aiMessages.value = data.messages
        if (data.conversationId) {
          localStorage.setItem('conversationId', data.conversationId)
        }
        return true
      }
    }
  } catch (e) {
    console.error('加载AI对话失败:', e)
  }
  return false
}

// 清除AI对话缓存
function clearAiMessages() {
  aiMessages.value = []
  localStorage.removeItem(AI_MESSAGES_KEY.value)
  localStorage.removeItem('conversationId')
}

// 保存草稿到 localStorage
function saveDraft() {
  try {
    const data = JSON.parse(JSON.stringify(form.value))
    localStorage.setItem(LOCAL_DRAFT_KEY.value, JSON.stringify(data))
  } catch (e) { }
}
// 读取草稿
function loadDraft() {
  try {
    const str = localStorage.getItem(LOCAL_DRAFT_KEY.value)
    if (str) {
      const draft = JSON.parse(str)
      form.value = draft
      return true
    }
  } catch (e) { }
  return false
}
// 清除草稿
function clearDraft() {
  form.value = JSON.parse(JSON.stringify(defaultForm))
  localStorage.removeItem(LOCAL_DRAFT_KEY.value)
  resumeId.value = null
}

let autoSaveTimer = null
function autoSave() {
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
  autoSaveTimer = setTimeout(() => {
    saveDraft()
  }, 1000)
}

// 页面加载时优先加载草稿/后端或根据id加载
loadResumeWithDraftOrId()

const onSubmit = async () => {
  formRef.value.validate(async (valid, fields) => {
    if (!valid) {
      // 自动滚动到第一个校验失败的栏目（最上面的）
      if (fields) {
        // 获取所有有错误的prop，找到最上面的el-form-item
        const keys = Object.keys(fields)
        await nextTick()
        let minTop = Infinity
        let target = null
        keys.forEach(key => {
          const item = document.querySelector(`[prop='${key}'], [data-prop='${key}']`)
          if (item) {
            const rect = item.getBoundingClientRect()
            if (rect.top < minTop) {
              minTop = rect.top
              target = item
            }
          }
        })
        if (target) {
          target.scrollIntoView({ behavior: 'smooth', block: 'center' })
          target.classList.add('form-item-error-highlight')
          setTimeout(() => target.classList.remove('form-item-error-highlight'), 1500)
        }
        ElMessage.warning('请完整填写所有必填栏目')
      }
      return
    }
    const userId = authStore.userId || authStore.user?.id
    if (!userId) {
      ElMessage.error('请先登录')
      return
    }
    //照片上传
      if (form.value.avatar && form.value.avatar.startsWith('data:image')) {
      //解析base64字符串
      const arr = form.value.avatar.split(',')
      const mime = arr[0].match(/:(.*?);/)[1]
      const bstr = atob(arr[1])
      //转成二进制数据
      let n = bstr.length
      const u8arr = new Uint8Array(n)
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n)
      }
      //创建File对象并上传
      const file = new File([u8arr], 'avatar.png', { type: mime })
      const formData = new FormData()
      formData.append('file', file)
      const url = await uploadFile(formData)
      if (url) {
        form.value.avatar = url.data
      } else {
        ElMessage.error('头像上传失败')
        return
      }
    } else if (avatarFile.value) {
      const formData = new FormData()
      formData.append('file', avatarFile.value)
      const url = await uploadFile(formData)
      if (url) {
        form.value.avatar = url.data
      } else {
        ElMessage.error('头像上传失败')
        return
      }
    }
    // 处理时间字段拼接（如有需要可在此处理）
    const data = {
      userId,
      content: JSON.stringify(form.value.Resume),
      avatar: form.value.avatar || ''
    }
    try {
      let res
      if (resumeId.value) {
        // 编辑模式，更新简历
        res = await updateResume(resumeId.value, data)
      } else {
        // 新建简历
        res = await createResume(data)
      }
      if (res && res.data) {
        const id = router.currentRoute.value.query.id
        if (id) {
          localStorage.removeItem(LOCAL_DRAFT_KEY.value)
        } else {
          clearDraft()
        }
        ElMessage.success('简历保存成功')
        // 可选：跳转或刷新
        router.push({ path: '/jobseeker/resume-view', query: { id: res.data.id } })
      }
    } catch (e) {
      ElMessage.error('保存简历失败')
    }
  })
}

const onAiOptimize = () => {
  showAiSidebar.value = true
  
  // 尝试加载缓存的对话
  const hasLoadedMessages = loadAiMessages()
  
  // 如果没有缓存的对话或加载失败，添加欢迎消息
  if (!hasLoadedMessages && aiMessages.value.length === 0) {
    aiMessages.value.push({
      type: 'ai',
      content: '您好！我是AI简历助手。我可以帮助您：\n\n- 优化简历内容\n- 改进自我评价\n- 完善工作经历描述\n- 提供求职建议\n请告诉我您需要什么帮助？',
      time: new Date().toLocaleTimeString()
    })
    saveAiMessages() // 保存欢迎消息
  }
  
  nextTick(() => {
    scrollToBottom()
  })
}

// AI相关功能函数
const closeAiSidebar = () => {
  showAiSidebar.value = false
}

// 清理累积内容中的冗余data:前缀
const cleanAccumulatedContent = (content) => {
  if (!content) return ''
  
  return content
    .replace(/data:\s*\n/g, '\n') // 移除单独行的data:
    .replace(/\ndata:\s*\n/g, '\n\n') // 秘除换行间的data:
    .replace(/^data:\s*/gm, '') // 移除行首的data:前缀
    .replace(/data:\s*$/gm, '') // 移除行尾的data:
    .replace(/data:\s+/g, ' ') // 移除中间的data:并保留空格
    .replace(/\n{4,}/g, '\n\n\n') // 保留适当的换行，但限制过多的连续换行
    .trim()
}

const newConversation = () => {
  // 清空对话记录和缓存
  clearAiMessages()
  
  // 生成新的对话ID
  var conversationId = generateConversationId()
  localStorage.setItem('conversationId', conversationId)
  
  // 添加新的欢迎消息
  
  
  // 保存新的对话状态
  saveAiMessages()
  
  nextTick(() => {
    scrollToBottom()
  })
}

const sendMessage = async () => {
  if (!userInput.value.trim() || isAiTyping.value) return

  var conversationId = localStorage.getItem('conversationId')
  if (!conversationId) {
    conversationId = generateConversationId()
    localStorage.setItem('conversationId', conversationId)
  }
  
  // 添加用户消息
  const userMessage = {
    type: 'user',
    content: userInput.value.trim(),
    time: new Date().toLocaleTimeString()
  }
  aiMessages.value.push(userMessage)
  
  // 保存用户消息到缓存
  saveAiMessages()
  
  const currentInput = userInput.value.trim()
  userInput.value = ''
  isAiTyping.value = true
  
  // 创建AI消息占位符
  const aiMessageIndex = aiMessages.value.length
  const aiMessage = {
    type: 'ai',
    content: '',
    time: new Date().toLocaleTimeString()
  }
  aiMessages.value.push(aiMessage)
  
  nextTick(() => {
    scrollToBottom()
  })
  
  try {
    // 调用流式AI接口
    const stream = await resumeOptimizeStream(
      conversationId,
      form.value.Resume,
      currentInput
    )
    
    let accumulatedContent = ''
    let lastUpdateTime = Date.now()
    
    await parseStreamResponse(
      stream,
      // onChunk: 接收到数据块时
      (chunk) => {
        // 清理chunk中可能残留的data:前缀，但保留内容和换行
        let cleanChunk = chunk
        if (typeof cleanChunk === 'string') {
          // 移除可能的data:前缀，但保留内容和换行
          cleanChunk = cleanChunk.replace(/^data:\s*/g, '')
          // 移除空行或只包含data:的行，但保留有意义的换行
          if (!cleanChunk || cleanChunk.trim() === 'data:') {
            return
          }
          // 如果chunk只是空白，保留为换行
          if (cleanChunk.trim() === '' && cleanChunk.includes('\n')) {
            cleanChunk = '\n'
          }
        }
        
        accumulatedContent += cleanChunk
        
        // 实时清理累积内容中的冗余data:，但保留换行结构
        const cleanedContent = cleanAccumulatedContent(accumulatedContent)
        
        // 节流更新，避免过于频繁的DOM操作
        const now = Date.now()
        if (now - lastUpdateTime > 50) { // 最多每50ms更新一次
          aiMessages.value[aiMessageIndex].content = cleanedContent
          lastUpdateTime = now
          nextTick(() => {
            scrollToBottom()
          })
        }
      },
      // onComplete: 完成时
      () => {
        isAiTyping.value = false
        // 最终清理并确保内容正确显示
        const finalContent = cleanAccumulatedContent(accumulatedContent)
        
        aiMessages.value[aiMessageIndex].content = finalContent
        
        // 保存完整的对话记录到缓存
        saveAiMessages()
        
        nextTick(() => {
          scrollToBottom()
        })
      },
      // onError: 错误时
      (error) => {
        console.error('AI流式响应错误:', error)
        isAiTyping.value = false
        
        let errorMessage = '抱歉，AI服务暂时不可用，请稍后再试。'
        
        // 根据错误类型提供更具体的错误信息
        if (error.message.includes('404')) {
          errorMessage = '抱歉，AI服务接口不存在，请联系管理员。'
        } else if (error.message.includes('401') || error.message.includes('403')) {
          errorMessage = '抱歉，您的登录状态已过期，请重新登录后再试。'
        } else if (error.message.includes('500')) {
          errorMessage = '抱歉，服务器内部错误，请稍后再试。'
        } else if (error.message.includes('timeout')) {
          errorMessage = '请求超时，请检查网络连接后重试。'
        }
        
        aiMessages.value[aiMessageIndex].content = errorMessage
        
        // 保存错误消息到缓存
        saveAiMessages()
        
        ElMessage.error('AI服务连接失败')
        nextTick(() => {
          scrollToBottom()
        })
      }
    )
    
  } catch (error) {
    console.error('发送消息失败:', error)
    isAiTyping.value = false
    
    let errorMessage = '抱歉，发送消息失败，请检查网络连接后重试。'
    
    if (error.message.includes('Failed to fetch')) {
      errorMessage = '网络连接失败，请检查您的网络连接。'
    }
    
    aiMessages.value[aiMessageIndex].content = errorMessage
    
    // 保存错误消息到缓存
    saveAiMessages()
    
    ElMessage.error('发送失败')
    nextTick(() => {
      scrollToBottom()
    })
  }
}

const renderMarkdown = (content) => {
  if (!content) return ''
  
  // 首先清理内容中可能残留的data:前缀
  let cleanContent = content
    .replace(/^data:\s*/gm, '') // 移除行首的data:前缀
    .replace(/\n\s*data:\s*\n/g, '\n') // 移除单独一行的data:
    .replace(/data:\s*$/gm, '') // 移除行尾的data:
    .replace(/\n{3,}/g, '\n\n') // 合并多个连续换行符
    .trim()
  
  // 简单的Markdown渲染，使用较小的字体和黑色文字，优化换行显示
  return cleanContent
    .replace(/\{\{(.*?)\}\}/g, '<span class="highlight-tag">$1</span>') // 处理{{优化}}等双大括号内容
    .replace(/### (.*?)(\n|$)/g, '<h3 style="color: #333; margin: 14px 0 6px 0; font-size: 14px; font-weight: bold;">$1</h3>')
    .replace(/## (.*?)(\n|$)/g, '<h2 style="color: #333; margin: 16px 0 8px 0; font-size: 15px; font-weight: bold;">$1</h2>')
    .replace(/\*\*(.*?)\*\*/g, '<strong style="color: #333; font-weight: bold;">$1</strong>')
    .replace(/```markdown\s*/g, '') // 移除markdown代码块标记
    .replace(/```\s*/g, '') // 移除代码块标记
    .replace(/📌/g, '<span style="color: #67c23a;">📌</span>')
    .replace(/🔧/g, '<span style="color: #666;">🔧</span>')
    .replace(/💡/g, '<span style="color: #e6a23c;">💡</span>')
    .replace(/🔍/g, '<span style="color: #666;">🔍</span>')
    .replace(/❌/g, '<span style="color: #f56c6c;">❌</span>')
    .replace(/✅/g, '<span style="color: #67c23a;">✅</span>')
    .replace(/• (.*?)(\n|$)/g, '<div style="margin: 3px 0; padding-left: 16px; font-size: 13px; color: #333; line-height: 1.4;">• $1</div>')
    .replace(/- (.*?)(\n|$)/g, '<div style="margin: 3px 0; padding-left: 16px; font-size: 13px; color: #333; line-height: 1.4;">• $1</div>')
    .replace(/\n\n/g, '<div style="margin: 6px 0;"></div>')
    .replace(/\n/g, '<br style="display: block; margin: 3px 0;">')
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    nextTick(() => {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    })
  }
}

const retryLastMessage = () => {
  if (isAiTyping.value) return
  
  // 找到最后一条用户消息
  const lastUserMessage = aiMessages.value
    .slice()
    .reverse()
    .find(msg => msg.type === 'user')
  
  if (lastUserMessage) {
    // 移除最后一条AI错误消息
    if (aiMessages.value.length > 0 && aiMessages.value[aiMessages.value.length - 1].type === 'ai') {
      aiMessages.value.pop()
      // 保存移除错误消息后的状态
      saveAiMessages()
    }
    
    // 重新发送最后一条用户消息
    userInput.value = lastUserMessage.content
    sendMessage()
  }
}

function generateConversationId() {
  return Math.random().toString(36).substr(2, 16);
}

function addCourse() {
  form.value.Resume.教育背景.主修课程.push('')
}
function removeCourse() {
  if (form.value.Resume.教育背景.主修课程.length > 1) {
    form.value.Resume.教育背景.主修课程.pop()
  }
}
function addJob() {
  form.value.Resume.任职情况.push({ 单位: '', 职位: '', 时间: '', 职责: '' })
}
function removeJob(idx) {
  if (form.value.Resume.任职情况.length > 1) {
    form.value.Resume.任职情况.splice(idx, 1)
  }
}
function addIntern() {
  form.value.Resume.实习_兼职.push({ 单位: '', 职位: '', 时间: '', 职责: '' })
}
function removeIntern(idx) {
  if (form.value.Resume.实习_兼职.length > 1) {
    form.value.Resume.实习_兼职.splice(idx, 1)
  }
}

const onClearAll = () => {
  ElMessageBox.confirm('确定要清空所有填写内容吗？此操作不可撤销。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    clearDraft()
    router.replace({ path: router.currentRoute.value.path, query: {} })
    ElMessage.success('内容已清空')
  })
}

// 动态预览组件选择，参考ResumeView.vue
const currentPreviewComponent = computed(() => {
  const template = form.value.Resume.template || '1'
  if (template === '2') return ResumeA4PaperBlueLeft
  else if (template === '3') return ResumeA4PaperBlueTopBar
  return ResumeA4Paper
})
</script>

<style scoped>
.resume-edit-container {
  padding: 32px;
  max-width: 900px;
  margin: 0 auto;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  background: #f8f9fa;
  transition: all 0.3s ease;
}

.resume-edit-container.with-ai-sidebar {
  margin-right: 420px;
  max-width: calc(100vw - 452px);
  padding-right: 16px;
}

.main-content {
  transition: all 0.3s ease;
}

.main-content.with-ai-sidebar {
  margin-right: 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .resume-edit-container.with-ai-sidebar {
    margin-right: 400px;
    max-width: calc(100vw - 432px);
    padding: 16px;
  }
  
  .ai-sidebar {
    width: 380px;
  }
}

@media (max-width: 1024px) {
  .resume-edit-container.with-ai-sidebar {
    margin-right: 350px;
    max-width: calc(100vw - 382px);
    padding: 12px;
  }
  
  .ai-sidebar {
    width: 350px;
  }
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.resume-title {
  font-size: 22px;
  font-weight: bold;
  color: #222;
  letter-spacing: 1px;
}

.ai-btn {
  margin-left: 16px;
}

/* AI侧边栏样式 */
.ai-sidebar {
  position: fixed;
  top: 0;
  right: 0;
  width: 400px;
  height: 100vh;
  background: #ffffff;
  border-left: 1px solid #e4e7ed;
  box-shadow: -2px 0 12px rgba(0, 0, 0, 0.12);
  z-index: 2000;
  display: flex;
  flex-direction: column;
  transform: translateX(0);
  transition: transform 0.3s ease;
}

/* 侧边栏进入动画 */
.ai-sidebar-enter-active,
.ai-sidebar-leave-active {
  transition: transform 0.3s ease;
}

.ai-sidebar-enter-from,
.ai-sidebar-leave-to {
  transform: translateX(100%);
}

.ai-sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #f8f9fa;
}

.ai-sidebar-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.close-btn {
  padding: 4px;
  color: #909399;
}

.close-btn:hover {
  color: #409eff;
}

.ai-chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px);
}

.ai-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.message {
  margin-bottom: 16px;
}

.user-message {
  text-align: right;
}

.ai-message {
  text-align: left;
}

.message-content {
  display: inline-block;
  max-width: 85%;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.5;
  word-wrap: break-word;
  white-space: pre-wrap; /* 保留换行和空格 */
}

.user-message .message-content {
  background: #409eff;
  color: white;
  border-bottom-right-radius: 4px;
}

.ai-message .message-content {
  background: #f0f2f5;
  color: #333;
  border-bottom-left-radius: 4px;
  border: 1px solid #e4e7ed;
}

.ai-content {
  line-height: 1.6;
  animation: fadeInContent 0.3s ease-in-out;
  font-size: 13px;
  color: #333;
  white-space: pre-wrap; /* 保留换行和空格 */
  word-wrap: break-word; /* 自动换行 */
}

@keyframes fadeInContent {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-content h2, .ai-content h3 {
  margin: 12px 0 8px 0;
}

.ai-content p {
  color: #333;
  font-size: 13px;
}

.ai-content div {
  color: #333;
}

/* 确保所有AI内容文字使用较小字体和黑色 */
.ai-content * {
  color: #333 !important;
  font-size: 13px;
}

.ai-content h2 {
  font-size: 15px !important;
}

.ai-content h3 {
  font-size: 14px !important;
}

/* 高亮标签样式 - 用于{{优化}}等双大括号内容 */
.highlight-tag {
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: white !important;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px !important;
  display: inline-block;
  margin: 0 2px;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.3);
  animation: highlightPulse 0.6s ease-in-out;
}

@keyframes highlightPulse {
  0% {
    transform: scale(0.8);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 流式输出时的光标效果 */
.ai-message.streaming .message-content::after {
  content: '▋';
  color: #409eff;
  animation: blink 1s infinite;
  margin-left: 2px;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: inherit;
}

.typing-indicator {
  text-align: left;
  margin-bottom: 16px;
}

.typing-dots {
  display: inline-flex;
  align-items: center;
  padding: 12px 16px;
  background: #f0f2f5;
  border-radius: 8px;
  border-bottom-left-radius: 4px;
  border: 1px solid #e4e7ed;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #909399;
  margin: 0 2px;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.ai-input-area {
  border-top: 1px solid #e4e7ed;
  background: #fff;
  padding: 16px;
}

.input-controls {
  margin-bottom: 12px;
}

.new-chat-btn {
  font-size: 12px;
  padding: 6px 12px;
  height: auto;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  width: 100%;
}

.input-container {
  position: relative;
  width: 100%;
}

.user-input {
  flex: 1;
  width: 100%;
}

.user-input .el-textarea__inner {
  resize: none;
  border-radius: 18px;
  padding-right: 50px !important; /* 为按钮留出空间 */
  font-size: 14px;
  transition: all 0.3s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.user-input .el-textarea__inner:focus {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.send-btn {
  position: absolute;
  right: 8px;
  bottom: 8px;
  height: 36px;
  width: 36px;
  min-width: 36px !important;
  padding: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  background-color: #337ecc;
}

.send-btn:active:not(:disabled) {
  transform: scale(0.95);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.resume-form {
  margin-top: 24px;
}

.avatar-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 12px;
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.form-item-error-highlight {
  box-shadow: 0 0 0 2px #f56c6c;
  border-radius: 4px;
  transition: box-shadow 0.3s;
}

.message-actions {
  margin-top: 8px;
  text-align: left;
}

.retry-btn {
  color: #409eff;
  font-size: 12px;
  padding: 2px 8px;
}

.retry-btn:hover {
  background-color: #ecf5ff;
}

.resume-avatar {
  width: 120px !important;
  height: 160px !important;
  object-fit: cover;
  border: 1px solid #e4e7ed;
  background: #fff;
}
</style>
