import { createRouter, createWebHistory } from 'vue-router'

const routes = [
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
    children: [
      {
        path:'/',
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
router.beforeEach((to, from, next) => {
  next()
})

export default router