<template>
  <div class="jobseeker-layout">
    <div class="sidebar">
      <div class="logo-container">
        <span class="logo-text">求职者中心</span>
      </div>
      <nav class="menu">
        <el-menu
          :default-openeds="[]"
          :default-active="defaultActiveMenu"
          class="el-menu-vertical-demo"
          router
          background-color="#fff"
          text-color="#333"
          active-text-color="#3a36db"
        >
          <el-menu-item index="/jobseeker/profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-sub-menu index="resume">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>简历功能</span>
            </template>
            <el-menu-item index="/jobseeker/resume-edit">
              <el-icon><EditPen /></el-icon>
              <span>填写简历</span>
            </el-menu-item>
            <el-menu-item index="/jobseeker/resume-view">
              <el-icon><View /></el-icon>
              <span>查看简历</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="match">
            <template #title>
              <el-icon><Search /></el-icon>
              <span>求职匹配</span>
            </template>
            <el-menu-item index="/jobseeker/match">
              <el-icon><User /></el-icon>
              <span>求职信息</span>
            </el-menu-item>
            <el-menu-item index="/jobseeker/match-new">
              <el-icon><EditPen /></el-icon>
              <span>新建匹配</span>
            </el-menu-item>
            <el-menu-item index="/jobseeker/match-history">
              <el-icon><View /></el-icon>
              <span>历史匹配</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/jobseeker/vip">
            <el-icon><Star /></el-icon>
            <span>会员功能</span>
          </el-menu-item>
        </el-menu>
      </nav>
    </div>
    <div class="main-content">
      <header class="header">
        <el-dropdown trigger="hover">
          <span class="user-info">
            <img class="avatar" :src="avatarUrl" alt="avatar" />
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goToChat">我的私聊</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>
      <main class="content">
        <router-view></router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Document, EditPen, View, Search, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { authApi } from '../../api/auth'
const router = useRouter()
const route = useRoute()
const avatarUrl = ref('https://api.dicebear.com/7.x/miniavs/svg?seed=jobseeker') // 可替换为用户真实头像
const defaultActiveMenu = computed(() => {
  // 如果当前路由是 /jobseeker 或 /jobseeker/，默认激活个人信息
  if (route.path === '/jobseeker' || route.path === '/jobseeker/') {
    return '/jobseeker/profile'
  }
  // 其他情况激活当前路由
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
  router.push('/chat') // 假设私聊页面路由为 /chat
}
const resumeMenuOpen = ref(true)
function toggleResumeMenu() {
  resumeMenuOpen.value = !resumeMenuOpen.value
}
</script>

<style scoped>
.jobseeker-layout {
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
/* 统一 Element Plus 子菜单悬停样式，防止被默认灰色覆盖 */
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
/* 保证普通菜单项 hover 依然为淡蓝色 */
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