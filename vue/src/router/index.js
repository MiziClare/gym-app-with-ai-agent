import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

// 解决导航栏或者底部导航tabBar中的vue-router在3.0版本以上频繁点击菜单报错的问题。
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push (location) {
  return originalPush.call(this, location).catch(err => err)
}

const routes = [
  {
    path: '/',
    name: 'Manager',
    component: () => import('../views/Manager.vue'),
    redirect: '/home',  // 重定向到主页
    children: [
      { path: '403', name: 'NoAuth', meta: { name: 'Forbidden' }, component: () => import('../views/manager/403') },
      { path: 'home', name: 'Home', meta: { name: 'Home' }, component: () => import('../views/manager/Home') },
      { path: 'admin', name: 'Admin', meta: { name: 'Admin info' }, component: () => import('../views/manager/Admin') },
      { path: 'adminPerson', name: 'AdminPerson', meta: { name: 'Profile' }, component: () => import('../views/manager/AdminPerson') },
      { path: 'password', name: 'Password', meta: { name: 'Password' }, component: () => import('../views/manager/Password') },
      { path: 'notice', name: 'Notice', meta: { name: 'Message board' }, component: () => import('../views/manager/Notice') },
      { path: 'user', name: 'User', meta: { name: 'Member info' }, component: () => import('../views/manager/User') },
      { path: 'coach', name: 'Coach', meta: { name: 'Coach info' }, component: () => import('../views/manager/Coach') },
      { path: 'reserve', name: 'Reserve', meta: { name: 'Coach Reservations' }, component: () => import('../views/manager/Reserve') },
      { path: 'course', name: 'Course', meta: { name: 'Courses' }, component: () => import('../views/manager/Course') },
      { path: 'orders', name: 'Orders', meta: { name: 'Course Orders' }, component: () => import('../views/manager/Orders') },
      { path: 'equipment', name: 'Equipment', meta: { name: 'Equipments' }, component: () => import('../views/manager/Equipment') },
      { path: 'eqReserve', name: 'EqReserve', meta: { name: 'Equipment Reservations' }, component: () => import('../views/manager/EqReserve') },
      { path: 'menu', name: 'Menu', meta: { name: 'Fitness Recipes' }, component: () => import('../views/manager/Menu') },
      { path: 'experience', name: 'Experience', meta: { name: 'Posts' }, component: () => import('../views/manager/Experience') },
    ]
  },
  {
    path: '/front',
    name: 'Front',
    component: () => import('../views/Front.vue'),
    children: [
      { path: 'home', name: 'Home', meta: { name: 'Home' }, component: () => import('../views/front/Home') },
      { path: 'person', name: 'Person', meta: { name: 'Profile' }, component: () => import('../views/front/Person') },
      { path: 'coachPerson', name: 'CoachPerson', meta: { name: 'Profile' }, component: () => import('../views/front/CoachPerson') },
      { path: 'coach', name: 'Coach', meta: { name: 'Coaches' }, component: () => import('../views/front/Coach') },
      { path: 'coachDetail', name: 'CoachDetail', meta: { name: 'Coach Detail' }, component: () => import('../views/front/CoachDetail') },
      { path: 'reserve', name: 'Reserve', meta: { name: 'Reservations' }, component: () => import('../views/front/Reserve') },
      { path: 'course', name: 'Course', meta: { name: 'Courses' }, component: () => import('../views/front/Course') },
      { path: 'courseDetail', name: 'CourseDetail', meta: { name: 'Course Details' }, component: () => import('../views/front/CourseDetail') },
      { path: 'orders', name: 'Orders', meta: { name: 'Orders' }, component: () => import('../views/front/Orders') },
      { path: 'equipment', name: 'Equipment', meta: { name: 'Equipment' }, component: () => import('../views/front/Equipment') },
      { path: 'eqReserve', name: 'EqReserve', meta: { name: 'Equipment Reservations' }, component: () => import('../views/front/EqReserve') },
      { path: 'menu', name: 'Menu', meta: { name: 'Fitness Recipes' }, component: () => import('../views/front/Menu') },
      { path: 'myExperience', name: 'MyExperience', meta: { name: 'My Posts' }, component: () => import('../views/front/MyExperience') },
      { path: 'experience', name: 'Experience', meta: { name: 'Sharing' }, component: () => import('../views/front/Experience') },
      { path: 'experienceDetail', name: 'ExperienceDetail', meta: { name: 'Post Details' }, component: () => import('../views/front/ExperienceDetail') },
    ]
  },
  { path: '/login', name: 'Login', meta: { name: 'Login' }, component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', meta: { name: 'Register' }, component: () => import('../views/Register.vue') },
  { path: '*', name: 'NotFound', meta: { name: 'Not Found' }, component: () => import('../views/404.vue') },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 注：不需要前台的项目，可以注释掉该路由守卫
// 路由守卫
// router.beforeEach((to ,from, next) => {
//   let user = JSON.parse(localStorage.getItem("xm-user") || '{}');
//   if (to.path === '/') {
//     if (user.role) {
//       if (user.role === 'USER') {
//         next('/front/home')
//       } else {
//         next('/home')
//       }
//     } else {
//       next('/login')
//     }
//   } else {
//     next()
//   }
// })

export default router
