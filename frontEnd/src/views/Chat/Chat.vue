<template>
  <div class="chat-container">
    <div class="chat-sidebar">
      <div class="chat-title">我的私聊</div>
      <el-input v-model="search" placeholder="搜索联系人" class="chat-search" clearable />
      <el-menu :default-active="activeUser" class="chat-user-list">
        <el-menu-item v-for="user in filteredUsers" :key="user.id" :index="user.id.toString()" @click="selectUser(user)">
          <el-avatar :src="user.avatar" size="small" />
          <span class="user-name">{{ user.username }}</span>
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
        <el-input
          v-model="inputMsg"
          placeholder="输入消息..."
          @keyup.enter="sendMsg"
          clearable
        />
        <el-button type="primary" @click="sendMsg">发送</el-button>
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
  { id: 0, username: '管理员', avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=admin' }
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
      avatar: u.avatar || `https://api.dicebear.com/7.x/miniavs/svg?seed=${u.username || u.username || u.id}`
    }))

    users.value = [
      { id: 0, username: '管理员', avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=admin' },
      ...chatUsers
    ]

  } catch (error) {
    console.error('获取聊天列表失败:', error)
  }
})

const messages = ref([
  { fromMe: false, avatar: users.value[0].avatar, content: '你好，有什么可以帮您？', time: Date.now() }
])

onMounted(async () => {
  try {
    const userId = localStorage.getItem('userId')
    if (!userId) return

    messageMap = await axios.get(`/chat/messages/${userId}`)
    console.log('获取历史消息:', messageMap.data)

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
  // 切换联系人时可加载历史消息

  if (!messageMap.data) {
    messages.value[0].avatar = user.avatar
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
        // 判断是否为当前聊天对象的消息
        if (msg.senderId == activeUser.value || msg.senderId == parseInt(localStorage.getItem('userId'))) {
          messages.value.push({
            fromMe: msg.senderId === parseInt(localStorage.getItem('userId')),
            avatar: msg.senderId === parseInt(localStorage.getItem('userId')) ? myAvatar.value : (users.value.find(u => u.id == msg.senderId)?.avatar || ''),
            content: msg.content,
            time: msg.time
          })

          console.log("时间：" + msg.time)

          nextTick(() => {
            if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
          })
        }
      } catch (e) {
        console.error('解析socket消息失败', e)
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
}
.user-name {
  margin-left: 12px;
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
  display: flex;
  align-items: center;
  padding: 16px 32px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}
.chat-input .el-input {
  flex: 1;
  margin-right: 16px;
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
