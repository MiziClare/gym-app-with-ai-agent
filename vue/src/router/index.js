import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

// Solve the problem of frequent clicking the menu in the navigation bar or bottom navigation tabBar in the 3.0 version above.
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push (location) {
  return originalPush.call(this, location).catch(err => err)
}

const routes = [
  {
    path: '/',
    name: 'Manager',
    component: () => import('../views/Manager.vue'),
    // redirect: '/home',  // will execute before router guard
    meta: { requiresAdmin: true },  // Add the mark of needing admin permission
    children: [
      { path: '403', name: 'NoAuth', meta: { name: 'Forbidden' }, component: () => import('../views/manager/403') },
      { path: 'home', name: 'Home', meta: { name: 'Dashboard', requiresAdmin: true }, component: () => import('../views/manager/Home') },
      { path: 'admin', name: 'Admin', meta: { name: 'Admin List', requiresAdmin: true }, component: () => import('../views/manager/Admin') },
      { path: 'adminPerson', name: 'AdminPerson', meta: { name: 'Profile', requiresAdmin: true }, component: () => import('../views/manager/AdminPerson') },
      { path: 'password', name: 'Password', meta: { name: 'Password', requiresAdmin: true }, component: () => import('../views/manager/Password') },
      { path: 'notice', name: 'Notice', meta: { name: 'Notices', requiresAdmin: true }, component: () => import('../views/manager/Notice') },
      { path: 'user', name: 'User', meta: { name: 'Member List', requiresAdmin: true }, component: () => import('../views/manager/User') },
      { path: 'user/:username', name: 'UserProfile', meta: { name: 'User Profile', requiresAdmin: true }, component: () => import('../views/manager/UserProfile') },
      { path: 'coach', name: 'Coach', meta: { name: 'Coach List', requiresAdmin: true }, component: () => import('../views/manager/Coach') },
      { path: 'reserve', name: 'Reserve', meta: { name: 'Coach Reservations', requiresAdmin: true }, component: () => import('../views/manager/Reserve') },
      { path: 'course', name: 'Course', meta: { name: 'Courses', requiresAdmin: true }, component: () => import('../views/manager/Course') },
      { path: 'orders', name: 'Orders', meta: { name: 'Course Orders', requiresAdmin: true }, component: () => import('../views/manager/Orders') },
      { path: 'equipment', name: 'Equipment', meta: { name: 'Equipments', requiresAdmin: true }, component: () => import('../views/manager/Equipment') },
      { path: 'eqReserve', name: 'EqReserve', meta: { name: 'Equipment Reservations', requiresAdmin: true }, component: () => import('../views/manager/EqReserve') },
      { path: 'experience', name: 'Experience', meta: { name: 'Posts', requiresAdmin: true }, component: () => import('../views/manager/Experience') },
    ]
  },
  {
    path: '/front',
    name: 'Front',
    component: () => import('../views/Front.vue'),
    meta: { requiresAuth: true },  // Only need to login, no specific role
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
      { path: 'myExperience', name: 'MyExperience', meta: { name: 'My Posts' }, component: () => import('../views/front/MyExperience') },
      { path: 'experience', name: 'Experience', meta: { name: 'Sharing' }, component: () => import('../views/front/Experience') },
      { path: 'experienceDetail', name: 'ExperienceDetail', meta: { name: 'Post Details' }, component: () => import('../views/front/ExperienceDetail') },
      { path: 'vr', name: 'Vr', meta: { name: 'VR' }, component: () => import('../components/Vr') },
      { path: 'card', name: 'Card', meta: { name: 'Member Card', requiresUser: true }, component: () => import('../views/front/Card') },
      { path: 'chat', name: 'Chat', meta: { name: 'Chat with Coach' }, component: () => import('../views/front/Chat') },
      { path: 'coachChat', name: 'CoachChat', meta: { name: 'Member Consultations' }, component: () => import('../views/front/CoachChat') },
    ]
  },
  { path: '/login', name: 'Login', meta: { name: 'Login' }, component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', meta: { name: 'Register' }, component: () => import('../views/Register.vue') },
  { path: '*', name: 'NotFound', meta: { name: 'Not Found' }, component: () => import('../views/404.vue') },
  {path: '/ai', name: 'AI', meta: {name: 'AI'}, component: () => import('../views/customer.vue')},
]

const router = new VueRouter({
  mode: 'hash',
  // base: process.env.BASE_URL,
  routes
})

// Router Guard
router.beforeEach((to, from, next) => {
  let user = JSON.parse(localStorage.getItem("xm-user") || '{}');
  
  // Handle root path redirection
  if (to.path === '/') {
    if (user.role) {
      if (user.role === 'USER' || user.role === 'COACH') {
        next('/front/home')
      } else {
        next('/home')
      }
    } else {
      next('/login')
    }
    return;
  }
  
  // Check if admin permission is required
  if (to.matched.some(record => record.meta.requiresAdmin)) {
    // Check if the user is logged in and is an admin
    if (!user.role || (user.role !== 'ADMIN')) {
      // If not an admin, redirect to the 403 page or the front page
      next('/front/home');
      return;
    }
  }
  
  // Check if login is required
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // Check if the user is logged in
    if (!user.role) {
      next('/login');
      return;
    }
  }
  
  // Check if user role is required
  if (to.matched.some(record => record.meta.requiresUser)) {
    // Check if the user is logged in and is a coach
    if (user.role === 'COACH') {
      next('/front/home');
      return;
    }
  }
  
  // Other cases are normally allowed to pass
  next();
})


export default router
