<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api, messageOf, type Booking } from '../api'

const bookings = ref<Booking[]>([])
const loading = ref(true)

onMounted(load)

async function load() {
  loading.value = true
  try {
    bookings.value = (await api.get<Booking[]>('/bookings/me')).data
  } finally {
    loading.value = false
  }
}

async function cancel(item: Booking) {
  try {
    await ElMessageBox.confirm(`Cancel ${item.courseName}?`, 'Cancel booking', {
      confirmButtonText: 'Cancel booking',
      cancelButtonText: 'Keep it',
      type: 'warning',
    })
    await api.delete(`/bookings/${item.id}`)
    ElMessage.success('Booking cancelled')
    await load()
  } catch (error: unknown) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error))
  }
}

function date(value: string) {
  return new Intl.DateTimeFormat('en-CA', {
    dateStyle: 'medium', timeStyle: 'short',
  }).format(new Date(value))
}
</script>

<template>
  <section class="page narrow">
    <div class="page-head">
      <div>
        <p class="eyebrow">Your movement plan</p>
        <h1>My bookings.</h1>
      </div>
      <RouterLink class="button dark" to="/front/course">Book another class</RouterLink>
    </div>

    <div v-if="loading" class="skeleton"></div>
    <div v-else-if="bookings.length" class="booking-list card">
      <article v-for="item in bookings" :key="item.id">
        <time :datetime="item.startsAt">
          <strong>{{ new Date(item.startsAt).getDate() }}</strong>
          <span>{{ new Date(item.startsAt).toLocaleString('en-CA', { month: 'short' }) }}</span>
        </time>
        <div class="booking-info">
          <h3>{{ item.courseName }}</h3>
          <p>{{ date(item.startsAt) }} · Coach {{ item.coachName }}</p>
        </div>
        <span class="status" :class="{ cancelled: item.status !== 'CONFIRMED' }">{{ item.status }}</span>
        <button v-if="item.status === 'CONFIRMED'" class="button ghost small" type="button" @click="cancel(item)">Cancel</button>
      </article>
    </div>
    <div v-else class="empty card">
      <h3>No bookings yet</h3>
      <p>Your upcoming sessions will appear here.</p>
      <RouterLink class="button lime" to="/front/course">Explore classes</RouterLink>
    </div>
  </section>
</template>

<style scoped>
.narrow { max-width: 980px; }
.booking-list { overflow: hidden; }
.booking-list article { display: grid; grid-template-columns: 72px 1fr auto auto; align-items: center; gap: 22px; padding: 23px 26px; border-bottom: 1px solid var(--line); }
.booking-list article:last-child { border-bottom: 0; }
time { width: 64px; height: 64px; display: grid; place-content: center; text-align: center; background: #eff6e5; border-radius: 16px; }
time strong { font-size: 22px; line-height: 1; }
time span { margin-top: 3px; color: var(--muted); font-size: 10px; text-transform: uppercase; }
.booking-info p { margin: 7px 0 0; color: var(--muted); font-size: 13px; }
.status { padding: 6px 9px; color: #2c6244; background: #eef8df; border-radius: 999px; font-size: 10px; font-weight: 800; }
.status.cancelled { color: #777; background: #eee; }
.empty h3 { margin-bottom: 8px; }
.empty p { margin-bottom: 24px; }
@media (max-width: 650px) {
  .booking-list article { grid-template-columns: 62px 1fr; }
  .status, .booking-list button { grid-column: 2; justify-self: start; }
}
</style>
