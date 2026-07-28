import { createRouter, createWebHistory } from 'vue-router'
import { loadSession, session } from './state'
import AuthView from './views/AuthView.vue'
import FrontLayout from './layouts/FrontLayout.vue'
import AdminLayout from './layouts/AdminLayout.vue'
import FrontHomeView from './views/FrontHomeView.vue'
import FrontFeatureView from './views/FrontFeatureView.vue'
import AdminView from './views/AdminView.vue'
import GymLayoutView from './views/GymLayoutView.vue'
import GymMapView from './views/GymMapView.vue'
import EquipmentAvailabilityView from './views/EquipmentAvailabilityView.vue'
import EquipmentAdminView from './views/EquipmentAdminView.vue'
import ClassesView from './views/ClassesView.vue'
import BookingsView from './views/BookingsView.vue'
import AssistantView from './views/AssistantView.vue'
import ScanView from './views/ScanView.vue'

type Role = 'ADMIN' | 'COACH' | 'MEMBER'

const feature = (path: string, name: string, title: string, roles: Role[] = ['MEMBER', 'COACH']) => ({
  path,
  component: FrontFeatureView,
  meta: { feature: name, title, roles },
})
const admin = (path: string, name: string, title: string) => ({
  path,
  component: AdminView,
  meta: { module: name, title, roles: ['ADMIN'] as Role[] },
})

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/front/home' },
    { path: '/login', alias: '/sign-in', component: AuthView, meta: { guest: true } },
    { path: '/scan', component: ScanView, meta: { roles: ['ADMIN', 'COACH'] as Role[] } },
    {
      path: '/front',
      component: FrontLayout,
      meta: { front: true },
      children: [
        { path: '', redirect: '/front/home' },
        { path: 'home', component: FrontHomeView, meta: { title: 'Overview', roles: ['MEMBER', 'COACH'] as Role[] } },
        { path: 'course', alias: 'courseDetail', component: ClassesView, meta: { title: 'Classes', roles: ['MEMBER', 'COACH'] as Role[] } },
        { path: 'orders', component: BookingsView, meta: { title: 'My classes', roles: ['MEMBER'] as Role[] } },
        { path: 'gym-map', component: GymMapView, meta: { title: 'Gym map', roles: ['MEMBER', 'COACH'] as Role[] } },
        { path: 'ai', component: AssistantView, meta: { title: 'AI assistant', roles: ['MEMBER'] as Role[] } },
        feature('person', 'profile', 'My profile', ['MEMBER']),
        feature('coachPerson', 'profile', 'My profile', ['COACH']),
        feature('coach', 'coaches', 'Coaches', ['MEMBER']),
        feature('coachDetail', 'coaches', 'Coach details', ['MEMBER']),
        feature('reserve', 'appointments', 'Coach bookings'),
        { path: 'equipment', component: EquipmentAvailabilityView, meta: { title: 'Equipment availability', roles: ['MEMBER'] as Role[] } },
        { path: 'eqReserve', redirect: '/front/equipment' },
        feature('calendar', 'operationCalendar', 'Operations calendar'),
        feature('experience', 'community', 'Community'),
        feature('experienceDetail', 'community', 'Post details'),
        feature('myExperience', 'myPosts', 'My posts', ['MEMBER']),
        feature('card', 'card', 'Member card', ['MEMBER']),
        feature('vr', 'vr', 'Virtual gym', ['MEMBER']),
        feature('chat', 'chat', 'Coach chat', ['MEMBER']),
        feature('coachChat', 'chat', 'Member chat', ['COACH']),
      ],
    },
    {
      path: '/',
      component: AdminLayout,
      meta: { admin: true },
      children: [
        admin('home', 'overview', 'Dashboard'),
        admin('admin', 'admins', 'Administrators'),
        admin('adminPerson', 'profile', 'My profile'),
        admin('password', 'profile', 'Security'),
        admin('notice', 'notices', 'Notices'),
        admin('user', 'members', 'Members'),
        admin('user/:username', 'members', 'Member details'),
        admin('coach', 'coaches', 'Coaches'),
        admin('coachAssignments', 'coachAssignments', 'Coach assignments'),
        admin('reserve', 'appointments', 'Coach bookings'),
        admin('calendar', 'closedDays', 'Operations calendar'),
        { path: 'gym-layout', component: GymLayoutView, meta: { title: 'Gym layout', roles: ['ADMIN'] as Role[] } },
        admin('course', 'courses', 'Classes'),
        admin('sessions', 'sessions', 'Class schedule'),
        admin('orders', 'bookings', 'Class bookings'),
        admin('visits', 'visits', 'Gym visits'),
        { path: 'equipment', component: EquipmentAdminView, meta: { title: 'Equipment availability', roles: ['ADMIN'] as Role[] } },
        { path: 'eqReserve', redirect: '/equipment' },
        admin('experience', 'posts', 'Community posts'),
      ],
    },
    { path: '/classes', redirect: '/front/course' },
    { path: '/bookings', redirect: '/front/orders' },
    { path: '/assistant', redirect: '/front/ai' },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to) => {
  if (!session.ready) {
    await loadSession().catch(() => {
      session.ready = true
    })
  }
  if (to.meta.guest && session.user) {
    return session.user.role === 'ADMIN' ? '/home' : '/front/home'
  }
  if (to.meta.admin && session.user?.role !== 'ADMIN') {
    return { path: '/login', query: { next: to.fullPath } }
  }
  if (to.meta.front && !['MEMBER', 'COACH'].includes(session.user?.role ?? '')) {
    return { path: '/login', query: { next: to.fullPath } }
  }
  const roles = to.meta.roles as Role[] | undefined
  if (roles && !session.user) {
    return { path: '/login', query: { next: to.fullPath } }
  }
  if (roles && !roles.includes(session.user?.role as Role)) {
    return session.user?.role === 'ADMIN' ? '/home' : '/front/home'
  }
})

export default router
