<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { api, messageOf, type Booking, type Session } from '../api'
import { session as auth } from '../state'

const sessions = ref<Session[]>([])
const route = useRoute()
const query = ref('')
const loading = ref(true)
const bookingId = ref<number | null>(null)
const bookedSessionIds = ref(new Set<number>())

const filtered = computed(() => {
  const needle = query.value.trim().toLowerCase()
  const target = Number(route.query.sessionId)
  const candidates = Number.isFinite(target)
    ? sessions.value.filter(item => item.id === target)
    : sessions.value
  return needle
    ? candidates.filter(item => `${item.courseName} ${item.coachName}`.toLowerCase().includes(needle))
    : candidates
})

onMounted(load)

async function load() {
  loading.value = true
  try {
    const target = Number(route.query.sessionId)
    const params = Number.isFinite(target)
      ? { from: new Date(new Date().setHours(0, 0, 0, 0)).toISOString() }
      : undefined
    sessions.value = (await api.get<Session[]>('/sessions', { params })).data
    if (auth.user?.role === 'MEMBER') {
      const mine = (await api.get<Booking[]>('/bookings/me')).data
      bookedSessionIds.value = new Set(
        mine.filter(item => item.status === 'CONFIRMED').map(item => item.sessionId),
      )
    }
  } finally {
    loading.value = false
  }
}

async function book(item: Session) {
  if (!auth.user) {
    location.href = `/login?next=${encodeURIComponent('/front/course')}`
    return
  }
  bookingId.value = item.id
  try {
    await api.post('/bookings', { sessionId: item.id })
    ElMessage.success('Class booked')
    await load()
  } catch (error) {
    ElMessage.error(messageOf(error))
  } finally {
    bookingId.value = null
  }
}

function date(value: string) {
  return new Intl.DateTimeFormat('en-CA', {
    weekday: 'short', month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit',
  }).format(new Date(value))
}
</script>

<template>
  <section class="page">
    <div class="page-head">
      <div>
        <p class="eyebrow">Live schedule</p>
        <h1>Choose your<br>next session.</h1>
      </div>
      <label class="search">
        <span class="sr-only">Search classes</span>
        <input v-model="query" type="search" placeholder="Search class or coach">
        <span aria-hidden="true">⌕</span>
      </label>
    </div>

    <div v-if="loading" class="class-grid">
      <div v-for="n in 6" :key="n" class="skeleton"></div>
    </div>
    <div v-else-if="filtered.length" class="class-grid">
      <article v-for="item in filtered" :key="item.id" class="class-card card">
        <div class="class-top" :class="`tone-${item.courseId % 3}`">
          <span>{{ date(item.startsAt).split(',')[0] }}</span>
          <strong>{{ new Date(item.startsAt).getDate() }}</strong>
        </div>
        <div class="class-body">
          <div>
            <p>{{ date(item.startsAt) }}</p>
            <h3>{{ item.courseName }}</h3>
            <small>Coach {{ item.coachName }}</small>
            <RouterLink
              v-if="item.spaceId"
              class="class-location"
              :to="`/front/gym-map?spaceId=${item.spaceId}`"
            >{{ item.floorName }} · {{ item.spaceName }}</RouterLink>
          </div>
          <div class="capacity">
            <span>{{ item.bookedCount }} / {{ item.capacity }} booked</span>
            <div><i :style="{ width: `${item.bookedCount / item.capacity * 100}%` }"></i></div>
          </div>
          <button
            v-if="auth.user?.role === 'MEMBER'"
            class="button dark"
            type="button"
            :disabled="item.bookedCount >= item.capacity || bookedSessionIds.has(item.id) || bookingId === item.id"
            @click="book(item)"
          >
            {{ bookedSessionIds.has(item.id) ? 'Booked' : item.bookedCount >= item.capacity ? 'Class full' : bookingId === item.id ? 'Booking…' : 'Reserve spot' }}
          </button>
          <span v-else class="coach-note">Member booking opens from the member portal.</span>
        </div>
      </article>
    </div>
    <div v-else class="empty card">No sessions match your search.</div>
  </section>
</template>

<style scoped>
.search { width: min(330px, 100%); display: flex; align-items: center; gap: 10px; padding: 13px 16px; background: white; border: 1px solid var(--line); border-radius: 13px; }
.search input { width: 100%; border: 0; outline: 0; background: transparent; }
.sr-only { position: absolute; width: 1px; height: 1px; padding: 0; overflow: hidden; clip: rect(0,0,0,0); white-space: nowrap; border: 0; }
.class-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 19px; }
.class-card { overflow: hidden; }
.class-top { min-height: 135px; padding: 24px; display: flex; justify-content: space-between; align-items: start; color: #173d2a; background: #dcedcb; }
.class-top strong { font-size: 58px; line-height: .8; letter-spacing: -4px; }
.class-top span { text-transform: uppercase; font-size: 11px; font-weight: 850; letter-spacing: 1px; }
.tone-0 { background: #d9e4ff; color: #263e72; }
.tone-1 { background: #dff2c5; color: #244b32; }
.tone-2 { background: #fae3c0; color: #644321; }
.class-body { padding: 22px; }
.class-body p { margin: 0 0 10px; color: var(--muted); font-size: 12px; }
.class-body small { display: block; margin-top: 7px; color: var(--muted); }
.class-location { width: fit-content; margin-top: 8px; display: block; color: #49745a; font-size: 12px; font-weight: 800; }
.capacity { margin: 25px 0 17px; }
.capacity span { color: var(--muted); font-size: 11px; }
.capacity div { height: 5px; margin-top: 7px; overflow: hidden; background: #edf0ec; border-radius: 99px; }
.capacity i { display: block; height: 100%; background: #70a663; border-radius: 99px; }
.class-body .button { width: 100%; }
@media (max-width: 900px) { .class-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .class-grid { grid-template-columns: 1fr; } }
</style>
