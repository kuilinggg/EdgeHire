<template>
  <div class="admin-container">
    <!-- 侧边导航栏 -->
    <div class="sidebar">
      <div class="logo-container">
        <span class="logo-text">欢迎你，管理员</span>
        <div class="logo-badge">v1.0</div>
      </div>
      
      <nav class="menu">
        <router-link 
          to="/users"
          class="menu-item"
          active-class="active"
        >
          <i class="icon material-icons"></i>
          <span>用户信息管理</span>
        </router-link>
        
        <router-link 
          to="/jobs"
          class="menu-item"
          active-class="active"
        >
          <i class="icon material-icons"></i>
          <span>求职信息审核</span>
        </router-link>
        
        <router-link 
          to="/stats"
          class="menu-item"
          active-class="active"
        >
          <i class="icon material-icons"></i>
          <span>数据统计分析</span>
        </router-link>

        <router-link 
          to="/algo"
          class="menu-item"
          active-class="active"
        >
          <i class="icon material-icons"></i>
          <span>算法监控</span>
        </router-link>
      </nav>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <header class="header">
        <div class="user-dropdown" @mouseenter="showMenu = true" @mouseleave="showMenu = false">
        <div class="avatar">
         <img v-if="avatar" :src="avatar" alt="User">
          <div v-else class="avatar-placeholder">
          <i class="material-icons">a</i>
          </div>
        </div>
          <transition name="slide-down">
            <div v-if="showMenu" class="dropdown-menu">
              <div class="menu-item" @click="profile">
                <i class="material-icons"></i>
                <span>个人中心</span>
              </div>
              <div class="menu-item" @click="logout">
                <i class="material-icons"></i>
                <span>退出登录</span>
              </div>
            </div>
          </transition>
        </div>
      </header>

      <!-- 内容区域 -->
      <main class="content">
        <router-view>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref } from 'vue'

const router=useRouter()
const avatar = ref(null)
const showMenu = ref(false)
const profile=()=>{

}
const logout = () => {
    router.push('/login')
  }
</script>

<style scoped>
/* 现代化布局样式 */
.admin-container {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
  font-family: 'Segoe UI', Roboto, sans-serif;
}

/* 侧边栏样式 */
.sidebar {
  width: 240px;
  background: #ffffff;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
  position: fixed;
  height: 100vh;
  z-index: 100;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
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

.menu {
  padding: 15px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  margin: 5px 10px;
  border-radius: 8px;
  color: #5a5a5a;
  text-decoration: none;
  transition: all 0.3s ease;
}

.menu-item:hover {
  background: #f5f5ff;
  color: #3a36db;
}

.menu-item.active {
  background: #f0f0ff;
  color: #3a36db;
  font-weight: 500;
}

.menu-item .icon {
  margin-right: 12px;
  font-size: 20px;
}

/* 主内容区样式 */
.main-content {
  flex: 1;
  margin-left: 240px;
}

.header {
  height: 64px;
  background: white;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 10;
}

.content {
  padding: 25px;
  min-height: calc(100vh - 64px);
}

/* 用户下拉菜单样式 */
.user-dropdown {
  position: relative;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder .material-icons {
  font-size: 24px;
  color: #1890ff; /* 管理员图标使用主题色 */
}

.dropdown-menu {
  position: absolute;
  top: 50px;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  width: 180px;
  overflow: hidden;
  z-index: 20;
}

.dropdown-menu .menu-item {
  padding: 10px 15px;
  margin: 0;
  border-radius: 0;
}

.dropdown-menu .menu-item:hover {
  background: #f5f5ff;
}

/* 动画效果 */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
  transform-origin: top center;
}

.slide-down-enter,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.material-icons {
  font-family: 'Material Icons';
  font-weight: normal;
  font-style: normal;
  font-size: 24px;
  line-height: 1;
  letter-spacing: normal;
  text-transform: none;
  display: inline-block;
  white-space: nowrap;
  word-wrap: normal;
  direction: ltr;
  -webkit-font-feature-settings: 'liga';
  font-feature-settings: 'liga';
  -webkit-font-smoothing: antialiased;
}
</style>