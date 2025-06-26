<template>
  <div class="admin-container">
    <!-- 侧边导航栏 -->
    <div class="sidebar">
      <div class="logo-container">
        <span class="logo-text">欢迎你，管理员</span>
        <div class="logo-badge">v1.0</div>
      </div>
      <el-menu
        :default-active="$route.path"
        class="el-menu-vertical-demo"
        router
        background-color="#fff"
        text-color="#5a5a5a"
        active-text-color="#3a36db"
      >
        <el-menu-item index="/adminlayout/users">
          <el-icon><User /></el-icon>
          <span>用户信息管理</span>
        </el-menu-item>
        <el-menu-item index="/adminlayout/jobs">
          <el-icon><Briefcase /></el-icon>
          <span>求职信息审核</span>
        </el-menu-item>
        <el-menu-item index="/adminlayout/stats">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计分析</span>
        </el-menu-item>
        <el-menu-item index="/adminlayout/algo">
          <el-icon><Monitor /></el-icon>
          <span>算法监控</span>
        </el-menu-item>
      </el-menu>
    </div>
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <header class="header">
        <el-dropdown trigger="hover">
          <span class="user-info">
            <el-avatar :src="avatar" size="40" icon="el-icon-user" />
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="profile">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>
      <!-- 内容区域 -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import { authApi } from '../../api/auth'
import { ElMessage } from 'element-plus'
import { User, Briefcase, DataAnalysis, Monitor } from '@element-plus/icons-vue'

const avatarUrl = ref('https://api.dicebear.com/7.x/miniavs/svg?seed=jobseeker')
const router=useRouter()
const avatar = ref(null)
const profile = () => {
  ElMessage.info('个人中心功能待实现')
}
const logout = () => {
    authApi.logout();
  }
</script>

<style scoped>
.admin-container {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
  font-family: 'Segoe UI', Roboto, sans-serif;
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
.logo-badge {
  background: #3a36db;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}
.el-menu-vertical-demo {
  border-right: none;
  background: transparent;
  box-shadow: none;
}
.el-menu-vertical-demo .el-menu-item {
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
.el-menu-vertical-demo .el-menu-item:hover {
  background: #f5f5ff !important;
  color: #3a36db !important;
}
.el-menu-vertical-demo .el-menu-item.is-active {
  background: #f0f0ff !important;
  color: #3a36db !important;
  font-weight: 600;
  box-shadow: 0 2px 8px 0 rgba(58,54,219,0.04);
}
.el-menu-vertical-demo .el-menu-item .el-icon {
  margin-right: 12px;
  font-size: 20px;
}
.main-content {
  flex: 1;
  margin-left: 240px;
  min-height: 100vh;
  background: #f5f7fa;
}
.header {
  height: 64px;
  background: #fff;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 32px;
  border-bottom: 1px solid #f0f0f0;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}
.content {
  padding: 32px;
}
</style>