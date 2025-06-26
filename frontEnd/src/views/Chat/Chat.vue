<template>
  <div class="chat-container">
    <div class="chat-sidebar">
      <div class="chat-title">我的私聊</div>
      <el-input v-model="search" placeholder="搜索联系人" class="chat-search" clearable />
      <el-menu :default-active="activeUser" class="chat-user-list">
        <el-menu-item v-for="user in filteredUsers" :key="user.id" :index="user.id.toString()" @click="selectUser(user)">
          <el-avatar :src="user.avatar" size="small" />
          <span class="user-name">{{ user.name }}</span>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="chat-main">
      <div class="chat-header">
        <el-avatar :src="currentUser.avatar" />
        <span class="chat-user-name">{{ currentUser.name || '请选择联系人' }}</span>
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
          <div class="msg-content">{{ msg.content }}</div>
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
import { ref, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const search = ref('')
const inputMsg = ref('')
const activeUser = ref('1')
const users = ref([
  { id: 1, name: '张三', avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=zhangsan' },
  { id: 2, name: '李四', avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=lisi' },
  { id: 3, name: '王五', avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=wangwu' }
])
const myAvatar = ref('https://api.dicebear.com/7.x/miniavs/svg?seed=jobseeker') // 当前用户头像
const currentUser = computed(() => users.value.find(u => u.id.toString() === activeUser.value) || {})
const messages = ref([
  { fromMe: false, avatar: users.value[0].avatar, content: '你好，有什么可以帮您？' }
])
const messagesRef = ref(null)
const filteredUsers = computed(() => {
  if (!search.value) return users.value
  return users.value.filter(u => u.name.includes(search.value))
})
function selectUser(user) {
  activeUser.value = user.id.toString()
  // 切换联系人时可加载历史消息
  messages.value = [
    { fromMe: false, avatar: user.avatar, content: `你好，我是${user.name}` }
  ]
  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}
function sendMsg() {
  if (!inputMsg.value.trim()) return
  messages.value.push({ fromMe: true, avatar: myAvatar.value, content: inputMsg.value })
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
function logout() {
  router.push('/login')
}
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
  align-items: flex-end;
  margin-bottom: 16px;
}
.chat-message.from-me {
  flex-direction: row-reverse;
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
</style>
