<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api'
import { logout, session } from '../state'

type Notice = { id: number; title: string; content: string }
const route = useRoute()
const router = useRouter()
const notices = ref<Notice[]>([])
const open = ref(false)
const iconFiles = import.meta.glob('../assets/imgs/*.png', { eager: true, query: '?url', import: 'default' }) as Record<string, string>
const iconUrl = (name: string) => iconFiles[`../assets/imgs/${name}`]
const currentTitle = computed(() => String(route.meta.title ?? 'Overview'))
const isCoach = computed(() => session.user?.role === 'COACH')
const workspaceName = computed(() => isCoach.value ? 'Coach workspace' : 'Member workspace')
const profilePath = computed(() => isCoach.value ? '/front/coachPerson' : '/front/person')

const memberGroups = [
  { label: 'Today', links: [
    ['Dashboard', '/front/home', 'icon-home.png'],
    ['Classes', '/front/course', 'icon-courses.png'],
    ['My classes', '/front/orders', 'icon-orders.png'],
    ['AI assistant', '/front/ai', 'icon-ai.png'],
  ] },
  { label: 'Coaching', links: [
    ['Coaches', '/front/coach', 'icon-coach.png'],
    ['Coach bookings', '/front/reserve', 'icon-reservations.png'],
    ['Coach chat', '/front/chat', 'icon-chat.png'],
  ] },
  { label: 'Gym', links: [
    ['Operations calendar', '/front/calendar', 'icon-info.png'],
    ['Equipment', '/front/equipment', 'icon-treadmill.png'],
    ['Virtual gym', '/front/vr', 'icon-vr.png'],
  ] },
  { label: 'Community', links: [
    ['Explore', '/front/experience', 'icon-post.png'],
  ] },
] as const
const coachGroups = [
  { label: 'Today', links: [
    ['Dashboard', '/front/home', 'icon-home.png'],
    ['Scan member', '/scan', 'icon-card.png'],
    ['Appointments', '/front/reserve', 'icon-reservations.png'],
    ['Operations calendar', '/front/calendar', 'icon-info.png'],
    ['Classes', '/front/course', 'icon-courses.png'],
    ['Member chat', '/front/coachChat', 'icon-chat.png'],
  ] },
  { label: 'Community', links: [['Explore', '/front/experience', 'icon-post.png']] },
] as const
const navGroups = computed(() => isCoach.value ? coachGroups : memberGroups)

onMounted(async () => {
  notices.value = (await api.get<Notice[]>('/notices')).data
})

async function signOut() {
  await logout()
  await router.push('/login')
}
</script>

<template>
  <div class="app-shell front-shell" :class="isCoach ? 'coach-shell' : 'member-shell'">
    <aside class="app-sidebar" :class="{ open }" :aria-label="`${workspaceName} navigation`">
      <RouterLink class="sidebar-brand" to="/front/home" @click="open = false">
        <img src="../assets/imgs/logo.png" alt="">
        <span><strong>Gym Panel</strong><small>{{ workspaceName }}</small></span>
      </RouterLink>
      <nav class="sidebar-nav">
        <section v-for="group in navGroups" :key="group.label">
          <p>{{ group.label }}</p>
          <RouterLink
            v-for="[label, path, icon] in group.links"
            :key="path"
            :to="path"
            @click="open = false"
          >
            <img :src="iconUrl(icon)" alt="">
            <span>{{ label }}</span>
          </RouterLink>
        </section>
      </nav>
      <footer class="sidebar-account">
        <RouterLink class="sidebar-user" :to="profilePath" @click="open = false">
          <img src="../assets/imgs/avatar-default.jpg" alt="">
          <span><strong>{{ session.user?.displayName }}</strong><small>{{ session.user?.role?.toLowerCase() }}</small></span>
        </RouterLink>
        <RouterLink v-if="session.user?.role === 'MEMBER'" class="sidebar-utility" to="/front/card" @click="open = false">
          <img :src="iconUrl('icon-card.png')" alt="">
          <span>Member card</span>
        </RouterLink>
        <RouterLink v-if="session.user?.role === 'MEMBER'" class="sidebar-utility" to="/front/myExperience" @click="open = false">
          <img :src="iconUrl('icon-mypost.png')" alt="">
          <span>My posts</span>
        </RouterLink>
        <button class="sidebar-utility danger" type="button" @click="signOut">
          <img :src="iconUrl('icon-logout.png')" alt="">
          <span>Sign out</span>
        </button>
      </footer>
    </aside>
    <button v-if="open" class="sidebar-backdrop" type="button" aria-label="Close menu" @click="open = false"></button>

    <div class="app-workspace">
      <header class="app-topbar">
        <button class="mobile-menu" type="button" aria-label="Open menu" @click="open = !open">
          <span></span><span></span><span></span>
        </button>
        <div class="page-context">
          <small>{{ workspaceName }}</small>
          <strong>{{ currentTitle }}</strong>
        </div>
        <div v-if="notices[0]" class="topbar-notice" :title="notices[0].content">
          <span>Notice</span>
          <strong>{{ notices[0].title }}</strong>
        </div>
      </header>
      <main class="app-main front-main"><RouterView /></main>
    </div>
  </div>
</template>
