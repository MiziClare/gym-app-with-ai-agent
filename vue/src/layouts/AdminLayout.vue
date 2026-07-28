<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { logout, session } from '../state'

const route = useRoute()
const router = useRouter()
const open = ref(false)
const currentTitle = computed(() => String(route.meta.title ?? 'Dashboard'))
const iconFiles = import.meta.glob('../assets/imgs/*.png', { eager: true, query: '?url', import: 'default' }) as Record<string, string>
const iconUrl = (name: string) => iconFiles[`../assets/imgs/${name}`]
const groups = [
  { label: 'Command', links: [
    ['Dashboard', '/home', 'icon-home.png'],
    ['Operations calendar', '/calendar', 'icon-info.png'],
    ['Scan member', '/scan', 'icon-card.png'],
  ] },
  { label: 'People', links: [
    ['Members', '/user', 'icon-account.png'],
    ['Coaches', '/coach', 'icon-coach.png'],
    ['Coach assignments', '/coachAssignments', 'icon-reservations.png'],
  ] },
  { label: 'Bookings', links: [
    ['Class schedule', '/sessions', 'icon-courses.png'],
    ['Class bookings', '/orders', 'icon-orders.png'],
    ['Coach bookings', '/reserve', 'icon-reservations.png'],
    ['Equipment bookings', '/eqReserve', 'icon-barbell.png'],
  ] },
  { label: 'Inventory', links: [
    ['Classes', '/course', 'icon-courses.png'],
    ['Resources', '/equipment', 'icon-treadmill.png'],
    ['Gym visits', '/visits', 'icon-card.png'],
  ] },
  { label: 'Content', links: [
    ['Notices', '/notice', 'icon-info.png'],
    ['Community posts', '/experience', 'icon-post.png'],
  ] },
] as const

async function signOut() {
  await logout()
  await router.push('/login')
}
</script>

<template>
  <div class="app-shell admin-shell">
    <aside class="app-sidebar admin-sidebar" :class="{ open }" aria-label="Administration navigation">
      <RouterLink class="sidebar-brand" to="/home" @click="open = false">
        <img src="../assets/imgs/logo.png" alt="">
        <span><strong>Gym Panel</strong><small>Administration</small></span>
      </RouterLink>
      <nav class="sidebar-nav">
        <section v-for="group in groups" :key="group.label">
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
        <RouterLink class="sidebar-user" to="/adminPerson" @click="open = false">
          <img src="../assets/imgs/avatar-default.jpg" alt="">
          <span><strong>{{ session.user?.displayName }}</strong><small>administrator</small></span>
        </RouterLink>
        <RouterLink class="sidebar-utility" to="/password" @click="open = false">
          <img :src="iconUrl('icon-info.png')" alt="">
          <span>Security</span>
        </RouterLink>
        <RouterLink class="sidebar-utility" to="/admin" @click="open = false">
          <img :src="iconUrl('icon-account.png')" alt="">
          <span>Administrators</span>
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
          <small>Administration</small>
          <strong>{{ currentTitle }}</strong>
        </div>
      </header>
      <main class="app-main admin-main"><RouterView /></main>
    </div>
  </div>
</template>
