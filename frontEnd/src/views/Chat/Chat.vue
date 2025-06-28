<template>
  <div class="chat-container">
    <div class="chat-sidebar">
      <div class="chat-title">我的消息</div>
      <el-input v-model="search" placeholder="搜索联系人" class="chat-search" clearable />
      <el-menu :default-active="activeUser" class="chat-user-list">
        <el-menu-item v-for="user in filteredUsers" :key="user.id" :index="user.id.toString()" @click="selectUser(user)">
          <el-avatar :src="user.avatar" size="small" />
          <div class="user-info">
            <div class="user-texts">
              <div class="user-name">{{ user.username }}</div>
              <div class="user-latest-message">{{ user.latestMessage }}</div>
            </div>
            <span v-if="user.unReadCount && user.unReadCount > 0" class="user-unread-count">{{ user.unReadCount }}</span>
          </div>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="chat-main">
      <div class="chat-header">
        <el-avatar :src="currentUser.avatar" />
        <span class="chat-user-name">{{ currentUser.username || '请选择联系人' }}</span>
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
        <div v-for="(msg, idx) in messages" :key="idx" :class="['chat-message', msg.fromMe ? 'from-me' : 'from-other']">
          <el-avatar :src="msg.avatar" size="small" />
          <div class="msg-content-wrapper">
            <div class="msg-time">{{ formatTime(msg.time) }}</div>
            <div class="msg-content">{{ msg.content }}</div>
          </div>
        </div>
      </div>
      <div class="chat-input">
        <div class="input-wrapper">
          <el-input
            v-model="inputMsg"
            type="textarea"
            :rows="3"
            placeholder="请输入聊天内容"
            @keydown.enter.exact.prevent="sendMsg"
            @keydown.shift.enter="handleShiftEnter"
            clearable
            resize="none"
            class="message-input"
          />
          <div class="input-actions">
            <el-button 
              type="primary" 
              @click="sendMsg" 
              class="send-button"
              :disabled="!inputMsg.trim()"
              circle
              size="large"
            >
              ➤
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
import { ca } from 'element-plus/es/locales.mjs'
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'


const router = useRouter()
const search = ref('')
const inputMsg = ref('')
const activeUser = ref('0') 

const speaker = localStorage.getItem('userId')
const users = ref([
  { id: 0, username: '系统消息', latestMessage: '', avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=admin', unReadCount: 0 }
])

var messageMap = ref(null) // 用于接收服务器推送的消息

const myAvatar = ref('https://api.dicebear.com/7.x/miniavs/svg?seed=jobseeker') // 当前用户头像
const currentUser = computed(() => users.value.find(u => u.id.toString() === activeUser.value) || {})

onMounted(async () => {
  try {
    const userId = localStorage.getItem('userId')
    if (!userId) return

    const res = await axios.get(`/chat/users/${userId}`)

    console.log('获取聊天对象列表:', res.data)

    const chatUsers = res.data.map(u => ({
      ...u,
      avatar: u.avatar ||
        `https://api.dicebear.com/7.x/miniavs/svg?seed=${u.username
      || u.username || u.id}`
    }))

    users.value = [
      ...chatUsers
    ]

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

    const savedActiveUser = localStorage.getItem('activeUser')

    if (savedActiveUser) {
    activeUser.value = savedActiveUser
    nextTick(() => {
      selectUser(users.value.find(u => u.id.toString() === savedActiveUser) || users.value[0])
    })
  }

  } catch (error) {
    console.error('获取历史消息失败:', error)
  }
})

const messagesRef = ref(null)
const filteredUsers = computed(() => {
  if (!search.value) return users.value
  return users.value.filter(u => u.username.includes(search.value))
})

const socket = ref(null)
onMounted(() => {
  const userId = localStorage.getItem('userId')
  if (!userId) return

  socket.value = new WebSocket(`ws://localhost:8080/webSocket?userId=${userId}`)

  socket.value.onopen = () => {
    console.log('WebSocket连接已建立')
  }
})


function selectUser(user) {
  activeUser.value = user.id.toString()
  localStorage.setItem('activeUser', activeUser.value)
  
  // 清除选中用户的未读消息数量
  const selectedUser = users.value.find(u => u.id === user.id)
  if (selectedUser && selectedUser.unReadCount) {
    selectedUser.unReadCount = 0
  }

  var res = axios.post(`/chat/messages/read/${user.id}/${speaker}`)
  console.log('已标记消息为已读:', res.data)

  // 切换联系人时可加载历史消息
  if (!messageMap.data) {
  //  messages.value[0].avatar = user.avatar
    return
  }
  var tempMessages = messageMap.data[user.id] || []

  messages.value = tempMessages.map(msg => ({
    fromMe: msg.senderId === parseInt(localStorage.getItem('userId')),
    avatar: msg.senderId === parseInt(localStorage.getItem('userId')) ? myAvatar.value : user.avatar,
    content: msg.content,
    time: msg.time
  }))

  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}
function sendMsg() {
  if (!inputMsg.value.trim()) {
    ElMessage.warning('消息不能为空')
    return
  }
  if (activeUser.value === '0') {
    ElMessage.warning('不能向管理员发送消息')
    return
  }

  var userId = parseInt(localStorage.getItem('userId'))
  if (userId != speaker) {
    ElMessage.warning("请先登录！")  //防止用户在同一浏览器中登录两个账号
    return
  }

  messages.value.push({ fromMe: true, avatar: myAvatar.value, content: inputMsg.value, time: Date.now() })

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

  // 发送消息到服务器
  socket.value.send(JSON.stringify({
    from: userId,
    to: activeUser.value,
    content: inputMsg.value,
    type: 0 // 0表示私聊
  }))

  inputMsg.value = ''
  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}

// 处理Shift+Enter换行
function handleShiftEnter(event) {
  // Shift+Enter时允许换行，不阻止默认行为
  // 这样用户可以正常换行
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

function logout() {
  router.push('/login')
}

onMounted(() => {
  if (socket && typeof socket.value !== 'string') {
    socket.value.onmessage = function(event) {
      try {
        const msg = JSON.parse(event.data)

        console.log("接收到消息:", msg)
        const myId = parseInt(localStorage.getItem('userId'))
        // 如果是当前聊天对象的消息，直接显示
        if (msg.senderId == activeUser.value || msg.senderId == myId) {
          messages.value.push({
            fromMe: msg.senderId === myId,
            avatar: msg.senderId === myId ? myAvatar.value : (users.value.find(u => u.id == msg.senderId)?.avatar || ''),
            content: msg.content,
            time: msg.time
          })

          console.log("时间：" + msg.time)

          nextTick(() => {
            if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
          })
        } else {
          // 如果消息不来自当前聊天对象，增加未读消息数量
          const senderUser = users.value.find(u => u.id == msg.senderId)
          if (senderUser) {
            if (!senderUser.unReadCount) {
              senderUser.unReadCount = 0
            }
            senderUser.unReadCount++

            if (messageMap.data[senderUser.id]) {
              messageMap.data[senderUser.id].push(msg)
            } 
            // --- 新增逻辑：将用户移到管理员下方 ---
            if (msg.senderId !== 0) {
              const idx = users.value.findIndex(u => u.id == msg.senderId)
              if (idx > 1) {
                const [userObj] = users.value.splice(idx, 1)
                users.value.splice(1, 0, userObj)
              }
            }
          }
          else {
              //TODO: 不存在该用户的消息列表，即边栏不存在该用户，需要请求该用户信息

            const fetchUser = async () => {
              const res = await axios.get(`/chat/newChatUser/${msg.senderId}`)
              console.log("获得新的用户信息:", res.data)
              if (res.data) {
                users.value.push({
                  id: msg.senderId,
                  username: res.data.username,
                  avatar: res.data.avatar || `https://api.dicebear.com/7.x/miniavs/svg?seed=${res.data.username || res.data.id}`,
                  unReadCount: res.data.unReadCount || 1
                })
                messageMap.data[msg.senderId] = [msg]
                // --- 新增逻辑：新用户直接插入管理员下方 ---
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
              u.latestMessage = msg.content
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
  background: #f5f7fa;
}
.chat-sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}
.chat-title {
  font-size: 18px;
  font-weight: 600;
  color: #3a36db;
  padding: 24px 0 12px 32px;
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
  background: #f5f7fa;
}
.chat-header {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 32px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  font-size: 16px;
  font-weight: 500;
  color: #333;
  position: relative;
}
.chat-user-name {
  margin-left: 16px;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px 32px;
  background: #f5f7fa;
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
  border-radius: 8px;
  padding: 10px 16px;
  margin: 0 12px;
  font-size: 15px;
  color: #333;
  box-shadow: 0 2px 8px rgba(60,60,60,0.04);
}
.chat-message.from-me .msg-content {
  background: #e6f0ff;
  color: #3a36db;
}
.chat-input {
  padding: 20px 32px 24px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  position: relative;
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
  background: #fafbfc;
  border-radius: 16px;
  padding: 12px;
  border: 1px solid #f0f2f5;
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
  margin-bottom: 2px;
}

.send-button {
  width: 48px !important;
  height: 48px !important;
  border-radius: 50% !important;
  background: linear-gradient(135deg, #3a36db 0%, #5b57e8 100%) !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(58, 54, 219, 0.3) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  position: relative;
  overflow: hidden;
  font-size: 18px !important;
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
</style>
