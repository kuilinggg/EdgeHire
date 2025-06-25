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
    name: 'JobSeeker',
    component: () => import('../views/JobSeeker/JobSeeker.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/hr',
    name: 'HR',
    component: () => import('../views/HR/HR.vue'),
    meta: { requiresAuth: true }

  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin/Admin.vue'),
    meta: { requiresAuth: true }

  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router