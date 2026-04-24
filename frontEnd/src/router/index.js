import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import { authApi } from '../api/auth'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/', 
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login/Login.vue'),
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Login/Register.vue'),
  },
  {
    path: '/jobseeker',
    component: () => import('../views/JobSeeker/JobSeeker.vue'),
    children: [
      {
        path: '',
        redirect: '/jobseeker/profile'
      },
      {
        path: 'resume-edit',
        name: 'ResumeEdit',
        component: () => import('../views/JobSeeker/ResumeEdit.vue')
      },
      {
        path: 'resume-view',
        name: 'ResumeView',
        component: () => import('../views/JobSeeker/ResumeView.vue')
      },
      {
        path: 'vip',
        name: 'Vip',
        component: () => import('../views/JobSeeker/Vip.vue')
      },
      {
        path: 'match',
        name: 'Match',
        component: () => import('../views/JobSeeker/Match.vue')
      },
      {
        path: 'match-new',
        name: 'MatchNew',
        component: () => import('../views/JobSeeker/MatchNew.vue')
      },
      {
        path: 'match-history',
        name: 'MatchHistory',
        component: () => import('../views/JobSeeker/MatchHistory.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/JobSeeker/Profile.vue')
      },
      {
        path: 'teach-vip',
        name: 'TeachVip',
        component: () => import('../views/JobSeeker/TeachVip.vue')
      },
      {
        path: 'offer-agent',
        name: 'OfferAgent',
        component: () => import('../views/JobSeeker/OfferAgent.vue')
      }
    ],
    meta: { requiresAuth: true }
  },
  {
    path: '/hr',
    name: 'HR',
    component: () => import('../views/HR/HR.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/hr/home'
      },
      {
        path: 'home',
        name: 'HRHome',
        component: () => import('../views/HR/HRHome.vue')
      },
      {
        path: 'profile',
        name: 'HRProfile',
        component: () => import('../views/HR/HRProfile.vue')
      },
      {
        path: 'resume-list',
        name: 'HRResumeList',
        component: () => import('../views/HR/ResumeList.vue')
      },
      {
        path: 'guidance',
        name: 'HRGuidance',
        component: () => import('../views/HR/Guidance.vue')
      },
      // 这里可继续添加其他HR子页面
    ]
  },
  {
    path: '/adminlayout',
    component: () => import('../views/Admin/AdminLayout.vue'),
    meta:{requiresAuth: true},
    children: [
      {
        path:'',
        redirect:'/adminlayout/users'
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('../views/Admin/UserManagement.vue')
      },
      {
        path: 'jobs',
        name: 'JobReview',
        component: () => import('../views/Admin/JobReview.vue')
      },
      {
        path: 'stats',
        name: 'Statistics',
        component: () => import('../views/Admin/Statistics.vue')
      },
    ]  
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('../views/Chat/Chat.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 如果用户已登录且尝试访问登录或注册页面，重定向到主页
  if ((to.path === '/login'||to.path === '/register') && authStore.token) {
    if(authStore.role ==1) {
      next('/jobseeker')
    }
    else if(authStore.role ==2) {
      next('/hr')
    }
    else {
      next('/adminlayout')
    }
    return
  }

  // 如果路由需要认证且用户未登录，重定向到登录页面
  if (to.meta.requiresAuth && !authStore.token) {
    ElMessage('请登录')
    next('/login')
    return
  }

  // 如果有 token，进行验证
  if (authStore.token) {
    try {
      await authApi.getUserInfo(authStore.token) 
    } catch (error) {
      // 如果验证失败，清除 token 并重定向到登录页面
      console.error('Token 验证失败:', error)
      authStore.clearToken()
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }

  // 定义各角色允许访问的路径前缀
    const allowedPaths = {
      0: ['/adminlayout'],    // 管理员
      1: ['/jobseeker', '/chat'],  // 求职者
      2: ['/hr', '/chat']      // HR
    }
   
    // 检查当前路径是否允许访问
    const role = authStore.role
    const isAllowed = allowedPaths[role]?.some(prefix => to.path.startsWith(prefix))
    
    // 如果访问不被允许的路径，重定向到角色首页
    if (!isAllowed) {
      if (role == 0) {
        next('/adminlayout')
      } else if (role == 1) {
        next('/jobseeker')
      } else if (role == 2) {
        next('/hr')
      }
    }
  next()
})


export default router
