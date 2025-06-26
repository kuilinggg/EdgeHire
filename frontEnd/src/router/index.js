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
        redirect: '/jobseeker/resume-edit'
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
      }
    ],
    meta: { requiresAuth: true }
  },
  {
    path: '/hr',
    name: 'HR',
    component: () => import('../views/HR/HR.vue'),
    meta: { requiresAuth: true }

  },
  {
    path: '/adminlayout',
    component: () => import('../views/Admin/AdminLayout.vue'),
    meta:{requiresAuth: true},
    children: [
      {
        path:'',
        redirect:'/users'
      },
      {
        path: '/users',
        name: 'UserManagement',
        component: () => import('../views/Admin/UserManagement.vue')
      },
      {
        path: '/jobs',
        name: 'JobReview',
        component: () => import('../views/Admin/JobReview.vue')
      },
      {
        path: '/stats',
        name: 'Statistics',
        component: () => import('../views/Admin/Statistics.vue')
      },
      {
        path:'/algo',
        name:"Algorithm",
        component: () => import('../views/Admin/Algorithm.vue')
      }
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

  // 如果用户已登录且尝试访问登录页面，重定向到主页
  if (to.path === '/login' && authStore.token) {
    if(authStore.role ==1) {
      next('/jobseeker/resume-edit')
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


  next()
})


export default router