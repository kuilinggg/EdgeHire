<template>
  <div class="jobseeker-layout">
    <div class="sidebar">
      <div class="logo-container">
        <span class="logo-text">求职者中心</span>
      </div>
      <nav class="menu">
        <router-link to="/jobseeker/resume-edit" class="menu-item" active-class="active">填写简历</router-link>
        <router-link to="/jobseeker/resume-view" class="menu-item" active-class="active">查看简历</router-link>
        <router-link to="/jobseeker/match" class="menu-item" active-class="active">求职匹配</router-link>
        <router-link to="/jobseeker/vip" class="menu-item" active-class="active">会员功能</router-link>
      </nav>
    </div>
    <div class="main-content">
      <header class="header">
        <div class="user-info">
          <img class="avatar" :src="avatarUrl" alt="avatar" />
          <el-dropdown>
            <span class="el-dropdown-link">
              <el-button type="text">退出登录</el-button>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
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
.menu-item {
  display: flex;
  align-items: center;
  padding: 16px 32px;
  color: #333;
  text-decoration: none;
  font-size: 16px;
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
</style>