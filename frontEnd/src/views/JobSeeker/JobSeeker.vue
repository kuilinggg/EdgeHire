<template>
  <div class="jobseeker-layout">
    <div class="sidebar">
      <div class="logo-container">
        <span class="logo-text">求职者中心</span>
      </div>
      <nav class="menu">
        <el-menu
          :default-openeds="['resume']"
          :default-active="$route.path"
          class="el-menu-vertical-demo"
          router
          background-color="#fff"
          text-color="#333"
          active-text-color="#3a36db"
        >
          <el-sub-menu index="resume">
            <template #title>
              <span>简历功能</span>
            </template>
            <el-menu-item index="/jobseeker/resume-edit">填写简历</el-menu-item>
            <el-menu-item index="/jobseeker/resume-view">查看简历</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/jobseeker/match">求职匹配</el-menu-item>
          <el-menu-item index="/jobseeker/vip">会员功能</el-menu-item>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const avatarUrl = ref('https://api.dicebear.com/7.x/miniavs/svg?seed=jobseeker') // 可替换为用户真实头像
function logout() {
  router.push('/login')
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
  font-family: 'Segoe UI', Roboto, sans-serif;
}
.sidebar {
  width: 220px;
  background: #fff;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
  position: fixed;
  height: 100vh;
  z-index: 100;
}
.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}
.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #3a36db;
}
.menu {
  display: flex;
  flex-direction: column;
  margin-top: 40px;
}
.submenu {
  margin-bottom: 24px;
}
.submenu-title {
  font-size: 15px;
  font-weight: 600;
  color: #3a36db;
  padding: 12px 32px 4px 32px;
  letter-spacing: 1px;
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.arrow {
  margin-left: 8px;
  font-size: 12px;
  transition: transform 0.2s;
}
.arrow.open {
  transform: rotate(180deg);
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: #333;
  text-decoration: none;
  font-size: 14px;
  border-left: 4px solid transparent;
  transition: background 0.2s, border-color 0.2s;
}
.menu-item.active, .menu-item.router-link-exact-active {
  background: #f0f4ff;
  color: #3a36db;
  border-left: 4px solid #3a36db;
}
.main-content {
  margin-left: 220px;
  width: calc(100vw - 220px);
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
</style>