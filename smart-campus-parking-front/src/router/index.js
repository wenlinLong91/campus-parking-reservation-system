import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Manage from '../views/Manage.vue' // 🌟 引入刚刚写好的控制中心
import UserDashboard from '../views/UserDashboard.vue' //职工、学生、访客页面
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login
    },
    {
      path: '/manage',
      name: 'manage',
      component: Manage // 🌟 注册控制中心路由路径
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: UserDashboard
    },
    {
      path: '/',
      redirect: '/login'
    }
  ]
})

export default router