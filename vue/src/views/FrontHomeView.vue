<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api, type Session } from '../api'
import { session } from '../state'

type Notice = { id: number; title: string; content: string }
const sessions = ref<Session[]>([])
const notices = ref<Notice[]>([])

onMounted(async () => {
  const [sessionResponse, noticeResponse] = await Promise.all([
    api.get<Session[]>('/sessions'),
    api.get<Notice[]>('/notices'),
  ])
  sessions.value = sessionResponse.data.slice(0, 3)
  notices.value = noticeResponse.data
})

const courseImages = [
  new URL('../assets/imgs/course-core.jpg', import.meta.url).href,
  new URL('../assets/imgs/course-yoga.jpg', import.meta.url).href,
  new URL('../assets/imgs/course-treadmill.jpg', import.meta.url).href,
]
</script>

<template>
  <div class="legacy-page home-page">
    <section class="welcome">
      <div>
        <p class="section-kicker">WELCOME BACK</p>
        <h1>{{ session.user?.displayName }}</h1>
        <p>Find a class, book a coach, or continue your training plan.</p>
        <RouterLink class="legacy-button" to="/front/course">Explore courses</RouterLink>
      </div>
      <img src="../assets/imgs/bg-coach.png" alt="Gym training">
    </section>

    <section>
      <div class="legacy-title"><h2>Popular Courses</h2><RouterLink to="/front/course">View all →</RouterLink></div>
      <div class="legacy-card-grid">
        <article v-for="(item, index) in sessions" :key="item.id" class="legacy-card course-preview">
          <img :src="courseImages[index % courseImages.length]" :alt="item.courseName">
          <div>
            <small>{{ new Date(item.startsAt).toLocaleDateString('en-CA', { weekday: 'short', month: 'short', day: 'numeric' }) }}</small>
            <h3>{{ item.courseName }}</h3>
            <p>Coach {{ item.coachName }} · {{ item.capacity - item.bookedCount }} spots left</p>
          </div>
        </article>
      </div>
    </section>

    <section class="notice-panel legacy-card">
      <div class="legacy-title"><h2>Latest Notices</h2></div>
      <article v-for="notice in notices" :key="notice.id">
        <strong>{{ notice.title }}</strong><span>{{ notice.content }}</span>
      </article>
    </section>
  </div>
</template>
