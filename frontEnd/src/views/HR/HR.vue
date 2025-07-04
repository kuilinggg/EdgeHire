<template>
  <div class="hr-layout">
    <div class="sidebar">
      <div class="logo-container">
        <span class="logo-text">HR中心</span>
      </div>
      <nav class="menu">
        <el-menu
          :default-active="defaultActiveMenu"
          class="el-menu-vertical-demo"
          router
          background-color="#fff"
          text-color="#333"
          active-text-color="#3a36db"
        >
          <el-menu-item index="/hr/home">
            <el-icon><User /></el-icon>
            <span>主页</span>
          </el-menu-item>
          <el-menu-item index="/hr/profile">
            <el-icon><Setting /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/hr/resume-list">
            <el-icon><Document /></el-icon>
            <span>浏览简历</span>
          </el-menu-item>
          <el-menu-item index="/hr/guidance">
            <el-icon><EditPen /></el-icon>
            <span>求职指导请求</span>
          </el-menu-item>
        </el-menu>
      </nav>
    </div>
    <div class="main-content">
      <header class="header">
        <div class="header-right">
          <div class="message-container" @click="goToChat">
            <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" class="message-badge">
              <el-icon class="message-icon">
                <Bell />
              </el-icon>
            </el-badge>
            <el-icon v-else class="message-icon">
              <Bell />
            </el-icon>
          </div>
          <el-dropdown trigger="hover">
            <span class="user-info">
              <img class="avatar" :src="avatarUrl" alt="avatar" />
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="showChange = true">修改密码</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <ChangePasswordDialog v-model:visible="showChange" />
      </header>
      <main class="content">
        <router-view></router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Document, EditPen, Setting, Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { authApi } from '../../api/auth'
import { getAvatarUrl } from '../../api/info'
import { getUnreadCount } from '../../api/chat'
import { useAuthStore } from '../../stores/authStore'
import ChangePasswordDialog from '../../components/ChangePasswordDialog.vue'

const router = useRouter()
const route = useRoute()

const showChange = ref(false)
const unreadCount = ref(0) // 未读消息数量
let unreadCountTimer = null // 定时器用于定期获取未读消息数量

const authStore = useAuthStore()
const userId = authStore.userId || localStorage.getItem('userId')
const avatarUrl = ref('https://api.dicebear.com/7.x/miniavs/svg?seed=hr') // 可替换为HR真实头像

const defaultActiveMenu = computed(() => {
  if (route.path === '/hr' || route.path === '/hr/') {
    return '/hr/home'
  }
  return route.path
})
function logout() {
  authApi.logout()
  try {
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (e) {
    ElMessage.error('退出失败')
  }
}
function goToChat() {
  // 点击进入聊天时清除未读消息数量
  unreadCount.value = 0
  localStorage.setItem('activeUser', null) // 清除当前活跃用户
  router.push('/chat')
}

// 获取未读消息数量
async function fetchUnreadCount() {
  if (!userId) return
  try {
    const response = await getUnreadCount(userId)
    if (response && response.data !== undefined) {
      unreadCount.value = response.data
    }
  } catch (e) {
    console.log('获取未读消息数量失败:', e)
  }
}

// 启动定时器定期获取未读消息数量
function startUnreadCountTimer() {
  if (unreadCountTimer) return
  // 立即获取一次
  fetchUnreadCount()
  // 每30秒获取一次未读消息数量
  unreadCountTimer = setInterval(fetchUnreadCount, 30000)
}

// 停止定时器
function stopUnreadCountTimer() {
  if (unreadCountTimer) {
    clearInterval(unreadCountTimer)
    unreadCountTimer = null
  }
}

onMounted(async () => {
  if (userId) {
    // 启动未读消息数量定时器
    startUnreadCountTimer()
    
    // 获取用户头像
    try {
      const url = await getAvatarUrl(userId)
      if (url) avatarUrl.value = url
    } catch (e) {
      // 保持默认头像
    }
  }
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopUnreadCountTimer()
})
</script>

<style scoped>
.hr-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
  font-family: 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
}
.sidebar {
  width: 240px;
  background: #ffffff;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
  position: fixed;
  height: 100vh;
  z-index: 100;
  display: flex;
  flex-direction: column;
  padding: 0;
}
.logo-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
}
.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #3a36db;
}
.menu {
  padding: 15px 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-top: 0;
  background: transparent;
}
.el-menu-vertical-demo {
  border-right: none;
  background: transparent;
  box-shadow: none;
}
.el-menu-vertical-demo .el-menu-item, .el-menu-vertical-demo .el-sub-menu__title {
  display: flex;
  align-items: center;
  padding: 12px 20px !important;
  margin: 5px 10px;
  border-radius: 8px;
  color: #5a5a5a;
  text-decoration: none;
  font-size: 15px;
  font-family: 'Segoe UI', Roboto, sans-serif;
  transition: all 0.3s cubic-bezier(.4,0,.2,1);
  background: transparent !important;
  font-weight: 500;
  box-sizing: border-box;
}
.el-menu-vertical-demo .el-menu-item:hover,
.el-menu-vertical-demo .el-sub-menu__title:hover {
  background: #f5f5ff !important;
  color: #3a36db !important;
}
.el-menu-vertical-demo .el-menu-item:focus,
.el-menu-vertical-demo .el-sub-menu__title:focus {
  outline: none;
  background: #f0f0ff !important;
  color: #3a36db !important;
}
.el-menu-vertical-demo .el-menu-item:active,
.el-menu-vertical-demo .el-sub-menu__title:active {
  background: #e6e6fa !important;
  color: #3a36db !important;
}
.el-menu-vertical-demo .el-menu-item.is-active,
.el-menu-vertical-demo .el-sub-menu__title.is-active,
.el-menu-vertical-demo .el-sub-menu.is-opened > .el-sub-menu__title {
  background: #f0f0ff !important;
  color: #3a36db !important;
  font-weight: 600;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
}
.el-menu-vertical-demo .el-sub-menu__icon-arrow {
  right: 16px !important;
}
.main-content {
  margin-left: 240px;
  width: calc(100vw - 240px);
  min-height: 100vh;
  background: #f5f7fa;
}
.header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 32px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.message-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.2s;
  position: relative;
}

.message-container:hover {
  background-color: #f5f5ff;
}

.message-icon {
  font-size: 22px;
  color: #333;
  transition: color 0.2s;
}

.message-container:hover .message-icon {
  color: #3a36db;
}

.message-badge {
  display: inline-block;
  position: relative;
}

.message-badge :deep(.el-badge__content) {
  background-color: #ff4757 !important;
  border: 2px solid #fff !important;
  font-size: 10px !important;
  min-width: 14px !important;
  width: 14px !important;
  height: 14px !important;
  padding: 0 !important;
  border-radius: 50% !important;
  box-shadow: 0 1px 3px rgba(255, 71, 87, 0.3) !important;
  font-weight: 600 !important;
  position: absolute !important;
  top: -7px !important;
  right: -7px !important;
  transform: none !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  z-index: 10 !important;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  background: #eee;
}
.content {
  padding: 32px;
}
.fade-enter-active, .fade-leave-active {
  transition: all 0.2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  height: 0;
}
.fade-enter-to, .fade-leave-from {
  opacity: 1;
  height: auto;
}
::v-deep(.el-menu-vertical-demo .el-sub-menu__title:hover),
::v-deep(.el-menu-vertical-demo .el-menu--vertical .el-sub-menu__title:hover),
::v-deep(.el-menu-vertical-demo .el-sub-menu.is-active > .el-sub-menu__title),
::v-deep(.el-menu-vertical-demo .el-sub-menu__title.is-active),
::v-deep(.el-menu-vertical-demo .el-sub-menu.is-opened > .el-sub-menu__title) {
  background: #f0f0ff !important;
  color: #3a36db !important;
  border-radius: 8px;
  font-weight: 600;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
}
::v-deep(.el-menu-vertical-demo .el-sub-menu__title:active) {
  background: #e6e6fa !important;
  color: #3a36db !important;
}
::v-deep(.el-menu-vertical-demo .el-sub-menu__title:focus) {
  outline: none;
  background: #f0f0ff !important;
  color: #3a36db !important;
}
::v-deep(.el-menu-vertical-demo .el-menu-item:hover) {
  background: #f5f5ff !important;
  color: #3a36db !important;
}
.el-menu-vertical-demo .el-menu-item .el-icon,
.el-menu-vertical-demo .el-sub-menu__title .el-icon {
  margin-right: 12px;
  font-size: 20px;
  display: flex;
  align-items: center;
}
</style>