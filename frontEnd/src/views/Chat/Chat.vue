<template>
  <div class="chat-container">
    <div class="chat-sidebar">
      <div class="chat-title">消息中心</div>
      <el-input v-model="search" placeholder="搜索联系人" class="chat-search" clearable />
      <el-menu :default-active="activeUser" class="chat-user-list">
        <el-menu-item v-for="user in filteredUsers" :key="user.id" :index="user.id.toString()" @click="selectUser(user)">
          <el-avatar :src="user.avatar" size="small" />
          <div class="user-info">
            <div class="user-texts">
              <div class="user-name">{{ user.username }}</div>
              <div class="user-latest-message">{{ formatLatestMessage(user.latestMessage) }}</div>
            </div>
            <span v-if="user.unReadCount && user.unReadCount > 0" class="user-unread-count">{{ user.unReadCount }}</span>
          </div>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="chat-main">
      <div class="chat-header">
        <el-avatar v-if="currentUser.avatar" :src="currentUser.avatar" />
        <el-avatar v-else src="" class="default-avatar">
          <el-icon><User /></el-icon>
        </el-avatar>
        <div class="chat-user-info">
          <span class="chat-user-name">{{ currentUser.username || '请选择联系人' }}</span>
          <span v-if="currentUser.company" class="chat-user-company">{{ currentUser.company }}</span>
        </div>
        <div class="chat-header-avatar-menu">
          <el-dropdown trigger="hover">
            <span>
              <el-avatar :src="myAvatar" class="my-avatar" />
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToProfile">个人主页</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <div class="chat-messages" ref="messagesRef">
        <!-- 空状态显示 -->
        <div v-if="!activeUser || activeUser === ''" class="chat-empty-state">
          <div class="empty-icon">💬</div>
          <div class="empty-text">{{ getEmptyStateText() }}</div>
        </div>
        <!-- 消息列表 -->
        <div v-else v-for="(msg, idx) in messages" :key="idx" :class="['chat-message', msg.fromMe ? 'from-me' : 'from-other']">
          <el-avatar :src="msg.avatar" size="small" />
          <div class="msg-content-wrapper">
            <div class="msg-time">{{ formatTime(msg.time) }}</div>
            <!-- 图片消息 -->
            <div v-if="msg.content.startsWith('image:')" class="msg-image">
              <img 
                :src="msg.content.substring(6)" 
                @click="previewImage(msg.content.substring(6))"
                @error="handleImageError"
                loading="lazy"
                alt="聊天图片"
              />
            </div>
            <!-- 文字消息 -->
            <div v-else class="msg-content">
              <template v-if="hasResumeLink(msg.content)">
                {{ getMessageText(msg.content) }}
                <span class="resume-link" @click="handleResumeClick(getResumeId(msg.content))">[简历]</span>
              </template>
              <template v-else>
                {{ msg.content }}
              </template>
            </div>
          </div>
        </div>
      </div>
      <div class="chat-input">
        <div class="input-wrapper" :class="{ 'disabled': !activeUser || activeUser === '' }">
          <el-input
            v-model="inputMsg"
            type="textarea"
            :rows="3"
            :placeholder="!activeUser || activeUser === '' ? '请先选择联系人' : '请输入聊天内容'"
            @keydown.enter.exact.prevent="sendMsg"
            @keydown.shift.enter="handleShiftEnter"
            clearable
            resize="none"
            class="message-input"
            :disabled="!activeUser || activeUser === ''"
          />
          <div class="input-actions">
            <!-- 图片上传按钮 -->
            <el-upload
              ref="uploadRef"
              :show-file-list="false"
              :before-upload="beforeImageUpload"
              :http-request="customUpload"
              accept="image/*"
              class="image-upload"
              :disabled="!activeUser || activeUser === ''"
            >
              <el-button type="info" circle size="large" class="upload-button" :loading="uploading" :disabled="!activeUser || activeUser === ''">
                <el-icon><Picture /></el-icon>
              </el-button>
            </el-upload>
            
            <el-button 
              type="primary" 
              @click="sendMsg" 
              class="send-button"
              :disabled="!inputMsg.trim() || !activeUser || activeUser === ''"
              circle
              size="large"
            >
              ➤
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 简历预览弹窗 -->
    <el-dialog 
      v-model="resumeDialogVisible" 
      title="简历预览" 
      width="1200px" 
      :close-on-click-modal="false"
      class="resume-dialog"
    >
      <div v-if="resumeDialogLoading" class="dialog-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <div v-else-if="currentResumeData" class="resume-preview-container">
        <template v-if="isDialogImportedResume">
          <!-- PDF简历显示 -->
          <div class="pdf-preview-wrapper">
            <canvas ref="dialogPdfCanvasRef" class="pdf-preview-canvas"></canvas>
            <div v-if="pdfDialogLoading" class="pdf-loading-mask">PDF加载中...</div>
          </div>
        </template>
        <template v-else>
          <!-- 普通简历显示 -->
          <component
            :is="dialogResumeComponent"
            :content="parsedDialogResumeContent"
            :avatar="currentResumeData.avatar"
            class="resume-preview-paper"
          />
        </template>
      </div>
      <div v-else class="dialog-error">
        <el-icon><Warning /></el-icon>
        <span>简历加载失败</span>
      </div>
      <template #footer>
        <el-button @click="resumeDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>


<script setup>
import { ca } from 'element-plus/es/locales.mjs'
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Picture, Loading, Warning, User } from '@element-plus/icons-vue'
import { getAvatarUrl } from '../../api/info'
import { authApi } from "../../api/auth"
import { getResumeById } from "../../api/resume"
import ResumeA4Paper from '../../components/ResumeA4Paper.vue'
import ResumeA4PaperBlueLeft from '../../components/ResumeA4PaperBlueLeft.vue'
import ResumeA4PaperBlueTopBar from '../../components/ResumeA4PaperBlueTopBar.vue'


const router = useRouter()
const search = ref('')
const inputMsg = ref('')
const activeUser = ref('')
const uploading = ref(false) 

// 简历弹窗相关
const resumeDialogVisible = ref(false)
const resumeDialogLoading = ref(false)
const currentResumeData = ref(null)
const pdfDialogLoading = ref(false)
const dialogPdfCanvasRef = ref(null)
const pdfDocCache = new Map() // PDF缓存 

const speaker = localStorage.getItem('userId')
const users = ref([
  { id: 0, username: '系统消息', latestMessage: '', avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=admin', unReadCount: 0 }
])

var messageMap = ref(null) // 用于接收服务器推送的消息

const myAvatar = ref('') // 当前用户头像
const currentUser = computed(() => users.value.find(u => u.id.toString() === activeUser.value) || {})

// 弹窗简历相关计算属性
const parsedDialogResumeContent = computed(() => {
  if (!currentResumeData.value || !currentResumeData.value.content) return {}
  try {
    const obj = JSON.parse(currentResumeData.value.content)
    return obj && typeof obj === 'object' ? obj : {}
  } catch {
    return { 内容: currentResumeData.value.content }
  }
})

const isDialogImportedResume = computed(() => {
  return parsedDialogResumeContent.value && parsedDialogResumeContent.value.importType === 'pdf' && parsedDialogResumeContent.value.pdfUrl
})

const dialogResumeComponent = computed(() => {
  const template = parsedDialogResumeContent.value.template || '1'
  if (template === '2') return ResumeA4PaperBlueLeft
  else if (template === '3') return ResumeA4PaperBlueTopBar
  return ResumeA4Paper
})

const dialogImportedPdfUrl = computed(() => {
  return isDialogImportedResume.value ? parsedDialogResumeContent.value.pdfUrl : ''
})

onMounted(async () => {
  try {
    const userId = localStorage.getItem('userId')
    if (userId) {
      myAvatar.value = await getAvatarUrl(userId)
    }
    if (!myAvatar.value) {
      myAvatar.value = `https://api.dicebear.com/7.x/miniavs/svg?seed=jobseeker`
    }
  } catch (e) {
    myAvatar.value = `https://api.dicebear.com/7.x/miniavs/svg?seed=jobseeker`
  }
  
  // 预加载PDF.js脚本
  loadPdfJsScript().catch(e => console.warn('PDF.js加载失败:', e))
})

const usersLoaded = ref(false)
const messageMapLoaded = ref(false)

function trySelectActiveUser() {
  if (usersLoaded.value && messageMapLoaded.value) {
    const savedActiveUser = localStorage.getItem('activeUser')
    if (savedActiveUser) {
      const user = users.value.find(u => u.id.toString() === savedActiveUser)
      if (user) {
        activeUser.value = savedActiveUser
        nextTick(() => {
          selectUser(user)
        })
      } else {
        // 如果保存的用户不存在，清除activeUser
        activeUser.value = ''
        localStorage.removeItem('activeUser')
      }
    }
  }
}

onMounted(async () => {
  try {
    const userId = localStorage.getItem('userId')
    if (!userId) return

    const res = await axios.get(`/chat/users/${userId}`)
    console.log('获取聊天对象列表:', res.data)

    // 异步获取所有用户头像
    const chatUsers = await Promise.all(res.data.map(async u => {
      let avatar = ''
      try {
        avatar = await getAvatarUrl(u.id)
      } catch (e) {
        avatar = `https://api.dicebear.com/7.x/miniavs/svg?seed=${u.username || u.id}`
      }
      return {
        ...u,
        avatar
      }
    }))

    users.value = [
      ...chatUsers
    ]
    usersLoaded.value = true
    trySelectActiveUser()
  } catch (error) {
    console.error('获取聊天列表对象失败:', error)
  }
})

const messages = ref([])

onMounted(async () => {
  try {
    const userId = localStorage.getItem('userId')
    if (!userId) return

    messageMap = await axios.get(`/chat/messages/${userId}`)
    console.log('获取历史消息:', messageMap.data)
    messageMapLoaded.value = true
    trySelectActiveUser()
  } catch (error) {
    console.error('获取历史消息失败:', error)
  }
})

const messagesRef = ref(null)
const filteredUsers = computed(() => {
  if (!search.value) return users.value
  return users.value.filter(u => u.username.includes(search.value))
})

const socket = ref(window._socket || null)
onMounted(() => {
  const userId = localStorage.getItem('userId')
  if (!userId) return

  if (!socket.value) {
    socket.value = new WebSocket(`ws://localhost:8080/webSocket?userId=${userId}`)

    socket.value.onopen = () => {
    console.log('WebSocket连接已建立')
    }
  }
})


function selectUser(user) {
  activeUser.value = user.id.toString()
  localStorage.setItem('activeUser', activeUser.value)
  // 清除选中用户的未读消息数量
  const selectedUser = users.value.find(u => u.id === user.id)
  if (selectedUser && selectedUser.unReadCount) {
    if (selectedUser.unReadCount > 0) {
      var res = axios.post(`/chat/messages/read/${user.id}/${speaker}`)
      console.log('已标记消息为已读:', res.data)
    } 
    selectedUser.unReadCount = 0
  }
  
  // 切换联系人时可加载历史消息
  if (!messageMap.data) {
    return
  }
  var tempMessages = messageMap.data[user.id] || []
  messages.value = tempMessages.map(msg => ({
    fromMe: msg.senderId === parseInt(localStorage.getItem('userId')),
    avatar: '', // 先占位，后面异步获取
    content: msg.content,
    time: msg.time
  }))
  // 异步为每条消息获取头像
  messages.value.forEach(async (msg, idx) => {
    if (msg.fromMe) {
      msg.avatar = myAvatar.value
    } else {
      try {
        msg.avatar = await getAvatarUrl(user.id)
      } catch (e) {
        msg.avatar = `https://api.dicebear.com/7.x/miniavs/svg?seed=${user.username || user.id}`
      }
    }
  })
  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}
function sendMsg() {
  if (!activeUser.value || activeUser.value === '') {
    ElMessage.warning('请先选择联系人')
    return
  }
  if (!inputMsg.value.trim()) {
    ElMessage.warning('消息不能为空')
    return
  }
  sendTextMessage()
}

// 图片上传前验证
function beforeImageUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 自定义上传处理
async function customUpload(options) {
  if (!activeUser.value || activeUser.value === '') {
    ElMessage.warning('请先选择联系人')
    return
  }
  if (activeUser.value === '0') {
    ElMessage.warning('不能向系统发送消息')
    return
  }

  uploading.value = true
  
  try {
    const formData = new FormData()
    formData.append('file', options.file)

    const response = await axios.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    console.log(response.data)

    if (response.data) {
      await sendImageMessage(response.data)
      ElMessage.success('图片发送成功')
    } else {
      ElMessage.error('图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
  }
}

// 发送图片消息
async function sendImageMessage(imageUrl) {
  const userId = parseInt(localStorage.getItem('userId'))
  
  // 构建图片消息对象（添加 image: 前缀）
  const imageContent = `image:${imageUrl}`
  const imageMsg = {
    fromMe: true,
    avatar: myAvatar.value,
    content: imageContent,
    time: Date.now()
  }
  
  // 添加到本地消息列表
  messages.value.push(imageMsg)

  // 添加到消息映射
  messageMap.data[activeUser.value].push({
    senderId: userId,
    receiverId: activeUser.value,
    content: imageContent,
    time: Date.now()
  })

  // 更新最新消息显示
  users.value.forEach(u => {
    if (u.id == parseInt(activeUser.value)) {
      u.latestMessage = imageContent
    }
  })

  // 发送文字消息（如果有输入文字）
  if (inputMsg.value.trim()) {
    await sendTextMessage()
  }

  // 通过WebSocket发送图片消息
  socket.value.send(JSON.stringify({
    from: userId,
    to: activeUser.value,
    content: imageContent,
    type: 0
  }))

  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}

// 发送文字消息（分离出来的函数）
async function sendTextMessage() {
  if (!activeUser.value || activeUser.value === '') {
    ElMessage.warning('请先选择联系人')
    return
  }
  if (activeUser.value === '0') {
    ElMessage.warning('不能向系统发送消息')
    return
  }

  const userId = parseInt(localStorage.getItem('userId'))
  if (userId != speaker) {
    ElMessage.warning("请先登录！")  //防止用户在同一浏览器中登录两个账号
    return
  }
  
  const textMsg = {
    fromMe: true,
    avatar: myAvatar.value,
    content: inputMsg.value,
    time: Date.now()
  }
  
  messages.value.push(textMsg)

  messageMap.data[activeUser.value].push({
    senderId: userId,
    receiverId: activeUser.value,
    content: inputMsg.value,
    time: Date.now()
  })

  users.value.forEach(u => {
    if (u.id == parseInt(activeUser.value)) {
      u.latestMessage = inputMsg.value
    }
  })

  // 通过WebSocket发送文字消息
  socket.value.send(JSON.stringify({
    from: userId,
    to: activeUser.value,
    content: inputMsg.value,
    type: 0
  }))

  inputMsg.value = ''

  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}

// 图片预览 - 在新标签页中打开
function previewImage(imageUrl) {
  window.open(imageUrl, '_blank')
}

// 图片加载错误处理
function handleImageError(event) {
  event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2Y1ZjVmNSIvPgogIDx0ZXh0IHg9IjEwMCIgeT0iMTAwIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMTQiIGZpbGw9IiM5OTkiIHRleHQtYW5jaG9yPSJtaWRkbGUiPuWbvueJh+WKoOi9veWksei0pTwvdGV4dD4KPC9zdmc+'
}

// 处理Shift+Enter换行
function handleShiftEnter(event) {
  // Shift+Enter时允许换行，不阻止默认行为
  // 这样用户可以正常换行
}

// 检测消息是否包含简历链接
function hasResumeLink(content) {
  return /resume:\d+$/.test(content)
}

// 获取消息文本（去除简历链接部分）
function getMessageText(content) {
  return content.replace(/resume:\d+$/, '').trim()
}

// 获取简历ID
function getResumeId(content) {
  const match = content.match(/resume:(\d+)$/)
  return match ? parseInt(match[1]) : null
}

// 处理简历点击事件
async function handleResumeClick(resumeId) {
  console.log('点击了简历，ID:', resumeId)
  
  resumeDialogLoading.value = true
  resumeDialogVisible.value = true
  currentResumeData.value = null
  
  try {
    const res = await getResumeById(resumeId)
    if (res && res.data) {
      currentResumeData.value = res.data
      
      // 如果是PDF简历，需要渲染PDF
      setTimeout(() => {
        if (isDialogImportedResume.value && dialogImportedPdfUrl.value) {
          renderDialogPdfToCanvas(dialogImportedPdfUrl.value)
        }
      }, 100)
    } else {
      ElMessage.error('简历不存在或已被删除')
      resumeDialogVisible.value = false
    }
  } catch (error) {
    console.error('获取简历失败:', error)
    ElMessage.error('获取简历失败')
    resumeDialogVisible.value = false
  } finally {
    resumeDialogLoading.value = false
  }
}

// 动态加载PDF.js脚本
function loadPdfJsScript() {
  return new Promise((resolve, reject) => {
    if (window.pdfjsLib) return resolve(window.pdfjsLib)
    const script = document.createElement('script')
    script.src = '/libs/pdf.min.js'
    script.onload = () => {
      window.pdfjsLib.GlobalWorkerOptions.workerSrc = '/libs/pdf.worker.min.js'
      resolve(window.pdfjsLib)
    }
    script.onerror = reject
    document.head.appendChild(script)
  })
}

// 在弹窗中渲染PDF
async function renderDialogPdfToCanvas(url) {
  pdfDialogLoading.value = true
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
    
    // 使用更大的固定缩放比例，而不是根据容器大小计算
    const scale = 1.33  // 从2.0缩小到1.33（缩小1/3）
    const scaledViewport = page.getViewport({ scale })
    
    const canvas = dialogPdfCanvasRef.value
    if (!canvas) return
    
    const context = canvas.getContext('2d')
    canvas.width = scaledViewport.width
    canvas.height = scaledViewport.height
    
    // 清空画布
    context.clearRect(0, 0, canvas.width, canvas.height)
    
    // 渲染PDF页面
    await page.render({ canvasContext: context, viewport: scaledViewport }).promise
  } catch (e) {
    console.error('PDF渲染失败', e)
    ElMessage.error('PDF加载失败')
  } finally {
    pdfDialogLoading.value = false
  }
}
function goToProfile() {
  // 角色数字：0-管理员 1-求职者 2-HR
  // 假设登录后已将 t_user.role 存入 localStorage 的 userRole
  const role = localStorage.getItem('userRole')
  if (role === '0') {
    router.push('/adminlayout')
  } else if (role === '1') {
    router.push('/jobseeker')
  } else if (role === '2') {
    router.push('/hr')
  } else {
    router.push('/') // 默认主页
  }
}

function formatTime(time) {
  const date = new Date(time)
  // 格式：2025-06-27 14:30
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}`
}

// 格式化最新消息显示
function formatLatestMessage(message) {
  if (!message) return ''
  if (message.startsWith('image:')) {
    return '[图片]'
  }
  return message
}

// 获取空状态显示文字
function getEmptyStateText() {
  const role = localStorage.getItem('role')
  if (role === '1') {
    return '快找HR进行沟通吧'
  } else if (role === '2') {
    return '快找求职者进行沟通吧'
  } else {
    return '快开始聊天吧'
  }
}

function logout() {
  authApi.logout()
}

onMounted(() => {
  if (socket && typeof socket.value !== 'string') {
    socket.value.onmessage = async function(event) {
      try {
        const msg = JSON.parse(event.data)
        console.log("接收到消息:", msg)
        const myId = parseInt(localStorage.getItem('userId'))
        if (msg.senderId == activeUser.value || msg.senderId == myId) {
          let avatar = ''
          if (msg.senderId === myId) {
            avatar = myAvatar.value
          } else {
            try {
              avatar = await getAvatarUrl(msg.senderId)
            } catch (e) {
              const u = users.value.find(u => u.id == msg.senderId)
              avatar = `https://api.dicebear.com/7.x/miniavs/svg?seed=${u?.username || msg.senderId}`
            }
          }
          messages.value.push({
            fromMe: msg.senderId === myId,
            avatar,
            content: msg.content,
            time: msg.time
          })
          nextTick(() => {
            if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
          })
        } else {
          // 如果消息不来自当前聊天对象，增加未读消息数量
          let senderUser = users.value.find(u => u.id == msg.senderId)
          if (senderUser) {
            if (!senderUser.unReadCount) {
              senderUser.unReadCount = 0
            }
            senderUser.unReadCount++
            if (messageMap.data[senderUser.id]) {
              messageMap.data[senderUser.id].push(msg)
            }
            if (msg.senderId !== 0) {
              const idx = users.value.findIndex(u => u.id == msg.senderId)
              if (idx > 1) {
                const [userObj] = users.value.splice(idx, 1)
                users.value.splice(1, 0, userObj)
              }
            }
          } else {
            //TODO: 不存在该用户的消息列表，即边栏不存在该用户，需要请求该用户信息
            const fetchUser = async () => {
              const res = await axios.get(`/chat/newChatUser/${msg.senderId}`)
              let avatar = ''
              try {
                avatar = await getAvatarUrl(msg.senderId)
              } catch (e) {
                avatar = `https://api.dicebear.com/7.x/miniavs/svg?seed=${res.data.username || res.data.id}`
              }
              if (res.data) {
                users.value.push({
                  id: msg.senderId,
                  username: res.data.username,
                  avatar,
                  unReadCount: res.data.unReadCount || 1
                })
                messageMap.data[msg.senderId] = [msg]
                if (msg.senderId !== 0) {
                  const idx = users.value.findIndex(u => u.id == msg.senderId)
                  if (idx > 1) {
                    const [userObj] = users.value.splice(idx, 1)
                    users.value.splice(1, 0, userObj)
                  }
                }
              } else {
                console.warn('未找到用户信息:', msg.senderId)
              }
            }
            fetchUser()
          }
          users.value.forEach(u => {
            if (u.id === msg.senderId) {
              u.latestMessage = msg.content.startsWith('image:') ? '[图片]' : msg.content
            }
          })
        }
      } catch (e) {
        console.error('出现错误', e)
      }
    }
  }
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f7 100%);
  position: relative;
  overflow: hidden;
}

.chat-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.8) 10%, transparent 10.5%);
  background-size: 20px 20px;
  opacity: 0.4;
  z-index: 0;
  pointer-events: none;
}
.chat-sidebar {
  width: 280px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  border-radius: 0 20px 20px 0;
  margin: 16px 0 16px 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  z-index: 1;
  overflow: hidden;
}
.chat-title {
  font-size: 20px;
  font-weight: 600;
  color: #3a36db;
  padding: 28px 0 16px 32px;
  position: relative;
  background: linear-gradient(90deg, #3a36db, #5b57e8);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 4px rgba(58, 54, 219, 0.1);
}
.chat-search {
  margin: 0 12px 12px 12px;
  width: calc(100% - 24px);
  box-sizing: border-box;
}
.chat-user-list {
  flex: 1;
  border: none;
  background: transparent;
  padding: 0 4px;
}

.chat-user-list .el-menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin: 4px 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
}

.chat-user-list .el-menu-item:hover {
  background-color: #f5f7fa;
}

.chat-user-list .el-menu-item.is-active {
  background-color: #e8ecf0;
  border-left: 3px solid #3a36db;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.chat-user-list .el-menu-item.is-active .user-name {
  color: #3a36db;
  font-weight: 500;
}
.user-info {
  flex: 1;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-left: 12px;
  min-width: 0;
  position: relative;
  gap: 0px;
}
.user-texts {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}
.user-name {
  flex: none;
  font-size: 14px;
  color: #333;
  font-weight: 400;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: all 0.3s ease;
  margin: 0;
  line-height: 1.1;
}
.user-latest-message {
  font-size: 12px;
  color: #a8abb2;
  max-width: 140px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
  line-height: 1.1;
}
.user-unread-count {
  background: #ff4757;
  color: white;
  border-radius: 10px;
  min-width: 18px;
  height: 18px;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  padding: 0 6px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  box-sizing: border-box;
  transition: all 0.3s ease;
  margin-left: 8px;
}

.chat-user-list .el-menu-item.is-active .user-unread-count {
  background: #3a36db;
}
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 20px 0 0 20px;
  margin: 16px 16px 16px 0;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  z-index: 1;
  overflow: hidden;
}
.chat-header {
  height: 70px;
  display: flex;
  align-items: center;
  padding: 0 32px;
  background: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid rgba(235, 238, 245, 0.5);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
  font-size: 16px;
  font-weight: 500;
  color: #333;
  position: relative;
  z-index: 2;
}

.chat-user-info {
  display: flex;
  flex-direction: column;
  margin-left: 16px;
}

.chat-user-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  line-height: 1.2;
}

.chat-user-company {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
  line-height: 1;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px 32px;
  background: transparent;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.4) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.4) 1px, transparent 1px);
  background-size: 20px 20px;
  background-position: center;
  position: relative;
}
.chat-message {
  display: flex;
  align-items: flex-start; /* 由 flex-end 改为 flex-start */
  margin-bottom: 16px;
}
.chat-message.from-me {
  flex-direction: row-reverse;
}
.msg-content-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center; /* 新增，垂直居中内容 */
}
.chat-message.from-me .msg-content-wrapper {
  align-items: flex-end;
}
.chat-message .el-avatar {
  align-self: flex-start;
  margin-top: 18px; /* 向下调整头像位置 */
}
.chat-message .msg-content {
  max-width: 320px;
  background: #fff;
  border-radius: 18px 18px 18px 4px;
  padding: 12px 18px;
  margin: 0 12px;
  font-size: 15px;
  color: #333;
  box-shadow: 0 2px 12px rgba(60,60,60,0.06);
  transition: all 0.3s ease;
  border-left: 3px solid #ebeef5;
}



.chat-message.from-me .msg-content {
  background: linear-gradient(135deg, #e6f0ff 0%, #f0f7ff 100%);
  color: #3a36db;
  border-radius: 18px 18px 4px 18px;
  border-left: none;
  border-right: 3px solid #d0e1ff;
}
.chat-input {
  padding: 20px 32px 24px;
  background: rgba(255, 255, 255, 0.9);
  border-top: 1px solid rgba(235, 238, 245, 0.5);
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.03);
  position: relative;
  z-index: 2;
}

.chat-input::before {
  content: '';
  position: absolute;
  top: 0;
  left: 32px;
  right: 32px;
  height: 1px;
  background: linear-gradient(90deg, transparent, #e4e7ed 20%, #e4e7ed 80%, transparent);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  position: relative;
  background: rgba(250, 251, 252, 0.8);
  border-radius: 20px;
  padding: 14px;
  border: 1px solid rgba(240, 242, 245, 0.8);
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.03);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  background: #fff;
  border-color: #3a36db;
  box-shadow: 0 0 0 3px rgba(58, 54, 219, 0.08);
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-textarea__inner) {
  border: none !important;
  background: transparent !important;
  padding: 8px 12px;
  font-size: 14px;
  line-height: 1.5;
  transition: all 0.3s ease;
  resize: none !important;
  box-shadow: none !important;
  min-height: 60px;
}

.message-input :deep(.el-textarea__inner):focus {
  outline: none !important;
  box-shadow: none !important;
}

.message-input :deep(.el-textarea__inner)::placeholder {
  color: #a8abb2;
  font-size: 14px;
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.send-button {
  width: 40px !important;
  height: 40px !important;
  border-radius: 50% !important;
  background: linear-gradient(135deg, #3a36db 0%, #5b57e8 100%) !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(58, 54, 219, 0.3) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  position: relative;
  overflow: hidden;
  font-size: 16px !important;
  font-weight: bold !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 20px rgba(58, 54, 219, 0.4) !important;
  background: linear-gradient(135deg, #4a46e5 0%, #6b67eb 100%) !important;
}

.send-button:active:not(:disabled) {
  transform: translateY(0) !important;
  box-shadow: 0 2px 8px rgba(58, 54, 219, 0.3) !important;
}

.send-button:disabled {
  background: #c0c4cc !important;
  box-shadow: none !important;
  cursor: not-allowed;
  opacity: 0.6;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px) scale(1.05) !important;
}
.chat-header-avatar-menu {
  position: absolute;
  right: 32px;
  top: 0;
  height: 64px;
  display: flex;
  align-items: center;
}
.my-avatar {
  margin-left: 24px;
  cursor: pointer;
}
.msg-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
  margin-left: 10px; /* 向右移动时间显示 */
}

/* 图片上传按钮样式 */
.input-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.image-upload {
  display: flex;
  align-items: center;
}

.upload-button {
  width: 40px !important;
  height: 40px !important;
  border-radius: 50% !important;
  background: #409eff !important;
  border: none !important;
  color: white !important;
  font-size: 16px !important;
  transition: all 0.3s ease !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.upload-button:hover:not(.is-loading) {
  background: #337ecc !important;
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3) !important;
}

.upload-button .el-icon {
  font-size: 18px !important;
}

/* 图片消息样式 */
.msg-image {
  margin: 0 12px;
  max-width: 280px;
}

.msg-image img {
  width: 100%;
  max-width: 280px;
  height: auto;
  max-height: 200px;
  object-fit: cover;
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.3s ease;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}



/* 我发送的图片消息样式 */
.chat-message.from-me .msg-image {
  margin: 0 12px;
}

/* 简历链接样式 */
.resume-link {
  color: #3a36db;
  cursor: pointer;
  font-weight: 500;
  text-decoration: underline;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(58, 54, 219, 0.1);
  transition: all 0.3s ease;
  margin-left: 8px;
  display: inline-block;
}

.resume-link:hover {
  background: rgba(58, 54, 219, 0.2);
  transform: scale(1.05);
  text-decoration: underline;
}

.resume-link:active {
  transform: scale(0.95);
}

/* 简历弹窗样式 */
.resume-dialog :deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

.resume-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 24px;
  margin: 0;
}

.resume-dialog :deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
}

.resume-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 18px;
}

.resume-dialog :deep(.el-dialog__body) {
  padding: 0;
  max-height: 80vh;  /* 从70vh增加到80vh */
  overflow-y: auto;
}

.dialog-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #409eff;
  font-size: 16px;
}

.dialog-loading .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.dialog-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #f56c6c;
  font-size: 16px;
}

.dialog-error .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.resume-preview-container {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #f8fafe;
}

.resume-preview-paper {
  transform: scale(0.8);
  transform-origin: top center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.pdf-preview-wrapper {
  display: flex;
  justify-content: center;
  align-items: flex-start;  /* 改为flex-start，避免垂直居中导致的尺寸问题 */
  position: relative;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: auto;  /* 改为auto，允许滚动查看大尺寸PDF */
  max-height: 75vh;  /* 添加最大高度限制 */
}

.pdf-preview-canvas {
  display: block;
  /* 移除尺寸限制，让PDF以实际渲染尺寸显示 */
}

.pdf-loading-mask {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #409eff;
  z-index: 10;
}

/* 聊天空状态样式 */
.chat-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  font-size: 16px;
  text-align: center;
  padding: 60px 20px;
  margin-top: -80px; /* 向上移动文字 */
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  font-size: 16px;
  color: #909399;
  font-weight: 400;
}

/* 输入框禁用状态样式 */
.input-wrapper.disabled {
  opacity: 0.6;
  background: #f5f7fa !important;
  border-color: #e4e7ed !important;
  cursor: not-allowed;
}

.input-wrapper.disabled .message-input :deep(.el-textarea__inner) {
  background: #f5f7fa !important;
  color: #c0c4cc !important;
  cursor: not-allowed;
}

/* 默认头像样式 */
.default-avatar {
  background: #f0f2f5 !important;
  color: #c0c4cc !important;
}
</style>
