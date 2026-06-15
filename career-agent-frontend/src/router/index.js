import { createRouter, createWebHistory } from 'vue-router'
import { authStore } from '../stores/auth.js'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/home', name: 'Home', component: () => import('../views/Home.vue'), meta: { title: '首页' } },
  { path: '/agent', name: 'AgentChat', component: () => import('../views/AgentChat.vue'), meta: { title: '智能助手' } },
  { path: '/profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { title: '简历录入' } },
  { path: '/jobs', name: 'JobList', component: () => import('../views/JobList.vue'), meta: { title: '岗位探索' } },
  { path: '/job/:id', name: 'JobDetail', component: () => import('../views/JobDetail.vue'), meta: { title: '岗位详情' } },
  ,
  { path: '/report/:id', name: 'ReportDetail', component: () => import('../views/ReportDetail.vue'), meta: { title: '报告详情' } },
  { path: '/reports', redirect: '/my' },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { title: '登录' } },  { path: '/my', name: 'My', component: () => import('../views/My.vue'), meta: { title: '我的' } },

]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  document.title = (to.meta.title || 'Career Agent') + ' - Career Agent'
  next()
})

// 全局路由守卫：未登录跳转登录页
router.beforeEach((to, from, next) => {
  if (to.path === '/login') { next(); return }
  if (!authStore.isLoggedIn) { next('/login'); return }
  next()
})

export default router
