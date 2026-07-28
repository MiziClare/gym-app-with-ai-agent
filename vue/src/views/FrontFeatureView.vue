<script setup lang="ts">
import { computed, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import QRCode from 'qrcode'
import { api, messageOf } from '../api'
import { session } from '../state'

type Row = Record<string, any>
const route = useRoute()
const feature = computed(() => String(route.meta.feature || 'profile'))
const rows = ref<Row[]>([])
const secondary = ref<Row[]>([])
const selected = ref<Row | null>(null)
const loading = ref(false)
const form = reactive({ coachId: 0, equipmentId: 0, startsAt: '', note: '', title: '', content: '' })
const availabilityForm = reactive({ dayOfWeek: 1, startsAt: '09:00', endsAt: '17:00' })
const appointmentDate = ref(localDate(new Date()))
const appointmentStart = ref('')
const reservationDate = ref(localDate(new Date()))
const reservationDuration = ref(30)
const selectedStart = ref('')
const busyPeriods = ref<Row[]>([])
const closedDays = ref<Row[]>([])
const operationHours = reactive({ opensAt: '06:00', closesAt: '22:00' })
const calendarMonthDate = ref(new Date(new Date().getFullYear(), new Date().getMonth(), 1))
const selectedCalendarDate = ref(localDate(new Date()))
const coachAvailability = ref<Row[]>([])
const availabilityLoading = ref(false)
const reserving = ref(false)
const qrCodeUrl = ref('')
let availabilityRequest = 0
let passRefreshTimer: ReturnType<typeof setTimeout> | undefined
const dayNames = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
const weekdayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']

const closedDateSet = computed(() => new Set(closedDays.value.filter(item => !item.startsAt).map(closedOn).filter(Boolean)))

const reservationDays = computed(() => Array.from({ length: 7 }, (_, offset) => {
  const value = new Date()
  value.setDate(value.getDate() + offset)
  return {
    value: localDate(value),
    day: new Intl.DateTimeFormat('en-CA', { weekday: 'short' }).format(value),
    date: new Intl.DateTimeFormat('en-CA', { month: 'short', day: 'numeric' }).format(value),
  }
}))

const rollingCalendarDays = computed(() => Array.from({ length: 14 }, (_, offset) => {
  const value = new Date()
  value.setDate(value.getDate() + offset)
  const key = localDate(value)
  const closures = closedDays.value.filter(item => closedOn(item) === key)
  return {
    value: key,
    day: new Intl.DateTimeFormat('en-CA', { weekday: 'short' }).format(value),
    date: new Intl.DateTimeFormat('en-CA', { month: 'short', day: 'numeric' }).format(value),
    closed: closures.some(item => !item.startsAt),
    partial: closures.some(item => item.startsAt),
  }
}))

const calendarMonth = computed(() => new Intl.DateTimeFormat('en-CA', { month: 'long', year: 'numeric' }).format(calendarMonthDate.value))
const calendarDays = computed(() => {
  const month = calendarMonthDate.value
  const first = new Date(month.getFullYear(), month.getMonth(), 1)
  const start = new Date(first)
  start.setDate(first.getDate() - ((first.getDay() + 6) % 7))
  return Array.from({ length: 42 }, (_, offset) => {
    const value = new Date(start)
    value.setDate(start.getDate() + offset)
    const key = localDate(value)
    const closures = closedDays.value.filter(item => closedOn(item) === key)
    return {
      value: key,
      day: value.getDate(),
      muted: value.getMonth() !== month.getMonth(),
      selected: selectedCalendarDate.value === key,
      closures,
      closed: closures.some(item => !item.startsAt),
      partial: closures.some(item => item.startsAt),
    }
  })
})
const visibleClosedCount = computed(() => calendarDays.value.filter(day => day.closures.length && !day.muted).length)
const selectedCalendarClosures = computed(() => closedDays.value.filter(item => closedOn(item) === selectedCalendarDate.value))

const timeSlots = computed(() => {
  const slots = []
  const day = new Date(`${reservationDate.value}T00:00:00`)
  const opensAt = timeMinutes(operationHours.opensAt)
  const closesAt = timeMinutes(operationHours.closesAt)
  for (let minutes = opensAt; minutes + reservationDuration.value <= closesAt; minutes += 30) {
    const startsAt = new Date(day)
    startsAt.setMinutes(minutes)
    const endsAt = new Date(startsAt.getTime() + reservationDuration.value * 60_000)
    const available = startsAt.getTime() > Date.now()
      && !isClosedDuring(reservationDate.value, minutes, minutes + reservationDuration.value)
      && !busyPeriods.value.some(item =>
        startsAt < new Date(item.endsAt) && endsAt > new Date(item.startsAt))
    slots.push({
      value: startsAt.toISOString(),
      label: new Intl.DateTimeFormat('en-CA', { hour: 'numeric', minute: '2-digit' }).format(startsAt),
      available,
    })
  }
  return slots
})

const appointmentSlots = computed(() => {
  const slots = []
  const day = new Date(`${appointmentDate.value}T00:00:00`)
  const opensAt = Math.ceil(timeMinutes(operationHours.opensAt) / 60) * 60
  const closesAt = timeMinutes(operationHours.closesAt)
  for (let minutes = opensAt; minutes + 60 <= closesAt; minutes += 60) {
    const startsAt = new Date(day)
    startsAt.setMinutes(minutes)
    const dayOfWeek = ((startsAt.getDay() + 6) % 7) + 1
    const startTime = startsAt.toTimeString().slice(0, 5)
    const endTime = new Date(startsAt.getTime() + 60 * 60_000).toTimeString().slice(0, 5)
    const open = coachAvailability.value.some(item =>
      Number(item.dayOfWeek) === dayOfWeek
      && timeText(item.startsAt) <= startTime
      && timeText(item.endsAt) >= endTime
    )
    slots.push({
      value: startsAt.toISOString(),
      label: new Intl.DateTimeFormat('en-CA', { hour: 'numeric', minute: '2-digit' }).format(startsAt),
      available: startsAt.getTime() > Date.now() && !isClosedDuring(appointmentDate.value, minutes, minutes + 60) && open,
    })
  }
  return slots
})

const titles: Record<string, [string, string]> = {
  profile: ['My Profile', 'Your account information'],
  coaches: ['Our Coaches', 'Find the right support for your goals'],
  appointments: ['Coach Booking', session.user?.role === 'COACH' ? 'Review member appointment requests' : 'Book time with a coach'],
  equipment: ['Gym Equipment', 'Browse available training equipment'],
  equipmentReservations: ['Equipment Booking', 'Reserve a time slot and review your bookings'],
  operationCalendar: ['Operations Calendar', 'View regular hours and scheduled closures'],
  community: ['Fitness Community', 'Share experience and learn from other members'],
  myPosts: ['My Posts', 'Manage the experience you have shared'],
  card: ['Membership E-card', 'Your digital gym identity'],
  chat: [session.user?.role === 'COACH' ? 'Member Chat' : 'Coach Chat', 'Keep training conversations in one place'],
  vr: ['VR Gym Tour', 'Explore the training space before your visit'],
}

watch(feature, load, { immediate: true })
onUnmounted(stopPassRefresh)

async function load() {
  stopPassRefresh()
  qrCodeUrl.value = ''
  loading.value = true
  rows.value = []
  secondary.value = []
  selected.value = null
  try {
    if (feature.value === 'coaches') rows.value = (await api.get('/coaches')).data
    if (feature.value === 'appointments') {
      await loadOperationsCalendar()
      if (session.user?.role === 'COACH') {
        const [appointments, availability] = await Promise.all([
          api.get('/coach/appointments'),
          api.get('/coach/availability'),
        ])
        rows.value = appointments.data
        coachAvailability.value = availability.data
      }
      else {
        const [mine, coaches] = await Promise.all([api.get('/coach-appointments/me'), api.get('/coaches')])
        rows.value = mine.data
        secondary.value = coaches.data
        form.coachId ||= secondary.value[0]?.id || 0
        await loadCoachAvailability()
      }
    }
    if (feature.value === 'equipment') rows.value = (await api.get('/equipment')).data
    if (feature.value === 'equipmentReservations') {
      await loadOperationsCalendar()
      const [mine, equipment] = await Promise.all([api.get('/equipment-reservations/me'), api.get('/equipment')])
      rows.value = mine.data
      secondary.value = equipment.data
      if (!secondary.value.some(item => item.id === form.equipmentId && item.status === 'AVAILABLE')) {
        form.equipmentId = secondary.value.find(item => item.status === 'AVAILABLE')?.id || 0
      }
    }
    if (['community', 'myPosts'].includes(feature.value)) {
      rows.value = (await api.get(feature.value === 'myPosts' ? '/posts/me' : '/posts')).data
    }
    if (feature.value === 'card') {
      rows.value = [(await api.get('/membership/me')).data]
      await loadMembershipPass()
    }
    if (feature.value === 'chat') {
      rows.value = (await api.get('/messages/peers')).data
      if (rows.value[0]) await choosePeer(rows.value[0])
    }
    if (feature.value === 'operationCalendar') await loadOperationsCalendar()
  } catch (error) {
    ElMessage.error(messageOf(error))
  } finally {
    loading.value = false
  }
}

async function loadMembershipPass() {
  const { data } = await api.get<{ token: string; refreshAt: string }>('/membership/me/pass')
  const value = `${location.origin}/scan#${data.token}`
  const image = await QRCode.toDataURL(value, {
    width: 220,
    margin: 1,
    color: { dark: '#31475e', light: '#fff8dd' },
  })
  if (feature.value !== 'card') return
  qrCodeUrl.value = image
  const delay = Math.max(1000, new Date(data.refreshAt).getTime() - Date.now())
  passRefreshTimer = setTimeout(() => {
    loadMembershipPass().catch(error => ElMessage.error(messageOf(error)))
  }, delay)
}

function stopPassRefresh() {
  if (passRefreshTimer) clearTimeout(passRefreshTimer)
  passRefreshTimer = undefined
}

async function createAppointment() {
  if (!appointmentStart.value) return
  reserving.value = true
  try {
    await api.post('/coach-appointments', {
      coachId: form.coachId,
      startsAt: new Date(appointmentStart.value).toISOString(),
      note: form.note,
    })
    Object.assign(form, { startsAt: '', note: '' })
    appointmentStart.value = ''
    ElMessage.success('Appointment requested')
    await load()
  } catch (error) {
    ElMessage.error(messageOf(error))
  } finally {
    reserving.value = false
  }
}

async function updateAppointment(id: number, status: string) {
  try {
    await api.patch(`/coach/appointments/${id}`, { status })
    ElMessage.success('Appointment updated')
    await load()
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function createAvailability() {
  try {
    await api.post('/coach/availability', availabilityForm)
    ElMessage.success('Availability saved')
    coachAvailability.value = (await api.get('/coach/availability')).data
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function removeAvailability(id: number) {
  try {
    await api.delete(`/coach/availability/${id}`)
    coachAvailability.value = (await api.get('/coach/availability')).data
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function reserveEquipment() {
  if (!selectedStart.value) return
  reserving.value = true
  try {
    const startsAt = new Date(selectedStart.value)
    await api.post('/equipment-reservations', {
      equipmentId: form.equipmentId,
      startsAt: startsAt.toISOString(),
      endsAt: new Date(startsAt.getTime() + reservationDuration.value * 60_000).toISOString(),
    })
    selectedStart.value = ''
    ElMessage.success('Equipment reserved')
    await load()
  } catch (error) {
    ElMessage.error(messageOf(error))
    await loadEquipmentAvailability()
  } finally {
    reserving.value = false
  }
}

async function createPost() {
  try {
    await api.post('/posts', { title: form.title, content: form.content })
    Object.assign(form, { title: '', content: '' })
    ElMessage.success('Post published')
    await load()
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function remove(path: string, label: string) {
  try {
    await ElMessageBox.confirm(`Cancel/delete this ${label}?`, 'Please confirm')
    await api.delete(path)
    ElMessage.success(`${label} updated`)
    await load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error))
  }
}

async function choosePeer(peer: Row) {
  selected.value = peer
  secondary.value = (await api.get(`/messages/${peer.id}`)).data
}

async function sendMessage() {
  if (!selected.value || !form.content.trim()) return
  try {
    await api.post('/messages', { recipientId: selected.value.id, content: form.content })
    form.content = ''
    await choosePeer(selected.value)
  } catch (error) { ElMessage.error(messageOf(error)) }
}

function date(value: string) {
  return new Intl.DateTimeFormat('en-CA', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

function localDate(value: Date) {
  const local = new Date(value.getTime() - value.getTimezoneOffset() * 60_000)
  return local.toISOString().slice(0, 10)
}

function dateOnly(value: unknown) {
  if (Array.isArray(value)) {
    const [year, month, day] = value
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  }
  const text = String(value ?? '')
  const match = text.match(/\d{4}-\d{2}-\d{2}/)
  if (match) return match[0]
  const date = new Date(text)
  return Number.isNaN(date.getTime()) ? text : localDate(date)
}

function closedOn(item: Row) {
  return dateOnly(item.closedOn ?? item.closed_on ?? item.closedon)
}

function closedReason(item: Row) {
  return item.reason || 'Closed'
}

function closureLabel(item: Row) {
  return item.startsAt
    ? `Closed ${timeText(item.startsAt)}–${timeText(item.endsAt)} · ${closedReason(item)}`
    : `Closed all day · ${closedReason(item)}`
}

function timeText(value: unknown) {
  return String(value).slice(0, 5)
}

function timeMinutes(value: unknown) {
  const [hours, minutes] = timeText(value).split(':').map(Number)
  return hours * 60 + minutes
}

function isClosedDuring(date: string, startsAt: number, endsAt: number) {
  return closedDays.value.some(item => closedOn(item) === date
    && (!item.startsAt || (timeMinutes(item.startsAt) < endsAt && timeMinutes(item.endsAt) > startsAt)))
}

async function loadOperationsCalendar() {
  let fromDate = new Date()
  let toDate = new Date()
  if (feature.value === 'operationCalendar') {
    const first = new Date(calendarMonthDate.value)
    fromDate = new Date(first)
    fromDate.setDate(first.getDate() - ((first.getDay() + 6) % 7))
    toDate = new Date(fromDate)
    toDate.setDate(toDate.getDate() + 41)
  } else {
    toDate.setDate(toDate.getDate() + 34)
  }
  const [closures, hours] = await Promise.all([
    api.get('/operations/calendar', { params: { from: localDate(fromDate), to: localDate(toDate) } }),
    api.get('/operations/hours'),
  ])
  closedDays.value = closures.data
  operationHours.opensAt = timeText(hours.data.opensAt)
  operationHours.closesAt = timeText(hours.data.closesAt)
}

async function moveOperationsCalendar(months: number) {
  const value = new Date(calendarMonthDate.value)
  value.setMonth(value.getMonth() + months)
  calendarMonthDate.value = value
  selectedCalendarDate.value = localDate(value)
  await loadOperationsCalendar()
}

async function resetOperationsCalendar() {
  const today = new Date()
  calendarMonthDate.value = new Date(today.getFullYear(), today.getMonth(), 1)
  selectedCalendarDate.value = localDate(today)
  await loadOperationsCalendar()
}

async function loadCoachAvailability() {
  coachAvailability.value = form.coachId
    ? (await api.get('/coach-availability', { params: { coachId: form.coachId } })).data
    : []
  appointmentStart.value = ''
}

async function loadEquipmentAvailability() {
  const request = ++availabilityRequest
  selectedStart.value = ''
  if (!form.equipmentId) {
    busyPeriods.value = []
    return
  }
  availabilityLoading.value = true
  const from = new Date(`${reservationDate.value}T00:00:00`)
  const to = new Date(from)
  to.setDate(to.getDate() + 1)
  try {
    const response = await api.get('/equipment-reservations/availability', {
      params: { equipmentId: form.equipmentId, from: from.toISOString(), to: to.toISOString() },
    })
    if (request === availabilityRequest) busyPeriods.value = response.data
  } catch (error) {
    if (request === availabilityRequest) ElMessage.error(messageOf(error))
  } finally {
    if (request === availabilityRequest) availabilityLoading.value = false
  }
}

watch(
  () => [feature.value, form.equipmentId, reservationDate.value],
  () => {
    if (feature.value === 'equipmentReservations') void loadEquipmentAvailability()
  },
)

watch(reservationDuration, () => { selectedStart.value = '' })
watch(() => form.coachId, () => {
  if (feature.value === 'appointments' && session.user?.role === 'MEMBER') void loadCoachAvailability()
})
</script>

<template>
  <div class="legacy-page">
    <div class="legacy-heading">
      <div><p class="section-kicker">GYM PANEL</p><h1>{{ titles[feature]?.[0] }}</h1><p>{{ titles[feature]?.[1] }}</p></div>
    </div>

    <div v-if="loading" class="legacy-card empty">Loading…</div>

    <section v-else-if="feature === 'profile'" class="profile-card legacy-card">
      <img src="../assets/imgs/avatar-default.jpg" alt="">
      <div><small>DISPLAY NAME</small><h2>{{ session.user?.displayName }}</h2><p>@{{ session.user?.username }}</p></div>
      <dl><div><dt>Email</dt><dd>{{ session.user?.email }}</dd></div><div><dt>Account role</dt><dd>{{ session.user?.role }}</dd></div></dl>
    </section>

    <section v-else-if="feature === 'coaches'" class="legacy-card-grid">
      <article v-for="coach in rows" :key="coach.id" class="legacy-card coach-card">
        <img src="../assets/imgs/avatar-coach.png" alt="">
        <h3>{{ coach.displayName }}</h3><strong>{{ coach.specialties }}</strong><p>{{ coach.bio }}</p>
        <RouterLink class="legacy-button" to="/front/reserve">Book coach</RouterLink>
      </article>
    </section>

    <section v-else-if="feature === 'operationCalendar'" class="legacy-card dashboard-calendar front-calendar">
      <div class="panel-title"><div><h2>Operations calendar</h2><p>Select a day to see its full operating details.</p></div></div>
      <div class="calendar-workspace dashboard-calendar-workspace">
        <div class="calendar-main">
          <div class="calendar-toolbar">
            <div><strong>{{ calendarMonth }}</strong><span>{{ visibleClosedCount }} affected</span></div>
            <div class="calendar-nav">
              <button type="button" aria-label="Previous month" @click="moveOperationsCalendar(-1)">Prev</button>
              <button type="button" @click="resetOperationsCalendar">Today</button>
              <button type="button" aria-label="Next month" @click="moveOperationsCalendar(1)">Next</button>
            </div>
          </div>
          <div class="calendar-weekdays"><span v-for="day in weekdayLabels" :key="day">{{ day }}</span></div>
          <div class="admin-calendar">
            <button v-for="day in calendarDays" :key="day.value" type="button" :title="day.closed ? 'Closed all day' : day.partial ? 'Partial closure' : `Open ${operationHours.opensAt}–${operationHours.closesAt}`" :class="{ closed: day.closed, partial: day.partial, selected: day.selected, muted: day.muted }" @click="selectedCalendarDate = day.value">
              <strong>{{ day.day }}</strong><span class="day-status">{{ day.closed ? 'Closed' : day.partial ? 'Limited' : 'Open' }}</span>
            </button>
          </div>
        </div>
        <aside class="calendar-editor calendar-details">
          <small>SELECTED DAY</small>
          <strong>{{ selectedCalendarDate }}</strong>
          <span class="selected-status" :class="{ closed: selectedCalendarClosures.some(item => !item.startsAt), partial: selectedCalendarClosures.some(item => item.startsAt) }">{{ selectedCalendarClosures.some(item => !item.startsAt) ? 'Closed' : selectedCalendarClosures.length ? 'Limited hours' : 'Open' }}</span>
          <p v-if="!selectedCalendarClosures.some(item => !item.startsAt)" class="calendar-hours"><b>{{ operationHours.opensAt }}–{{ operationHours.closesAt }}</b><span>Default operating hours</span></p>
          <ul v-if="selectedCalendarClosures.length" class="closure-list">
            <li v-for="item in selectedCalendarClosures" :key="item.id"><span>{{ closureLabel(item) }}</span></li>
          </ul>
          <p v-else class="calendar-note">No exceptions scheduled for this day.</p>
        </aside>
      </div>
    </section>

    <template v-else-if="feature === 'appointments'">
      <form v-if="session.user?.role === 'MEMBER'" class="reservation-card legacy-card" @submit.prevent="createAppointment">
        <section>
          <header class="reservation-step"><span>1</span><div><h2>Choose coach</h2><p>Pick the coach that matches today's training goal.</p></div></header>
          <div class="equipment-options coach-options" role="radiogroup" aria-label="Coach">
            <button
              v-for="coach in secondary"
              :key="coach.id"
              type="button"
              :class="{ selected: form.coachId === coach.id }"
              :aria-pressed="form.coachId === coach.id"
              @click="form.coachId = coach.id; appointmentStart = ''"
            >
              <img src="../assets/imgs/avatar-coach.png" alt="">
              <span>{{ coach.specialties || 'Coach' }}</span>
              <strong>{{ coach.displayName }}</strong>
            </button>
          </div>
        </section>

        <section>
          <header class="reservation-step"><span>2</span><div><h2>Pick a day</h2><p>Book a one-hour session in the next 7 days.</p></div></header>
          <div class="date-options" role="radiogroup" aria-label="Appointment date">
            <button
              v-for="day in reservationDays"
              :key="day.value"
              type="button"
              :class="{ selected: appointmentDate === day.value }"
              :disabled="closedDateSet.has(day.value)"
              :aria-pressed="appointmentDate === day.value"
              @click="appointmentDate = day.value; appointmentStart = ''"
            ><small>{{ day.day }}</small><strong>{{ day.date }}</strong><span v-if="closedDateSet.has(day.value)">Closed</span></button>
          </div>
        </section>

        <section class="reservation-time">
          <header class="reservation-step"><span>3</span><div><h2>Choose a time</h2><p>One click reserves a fixed 60-minute appointment request.</p></div></header>
          <div class="time-options" role="radiogroup" aria-label="Appointment start times">
            <button
              v-for="slot in appointmentSlots"
              :key="slot.value"
              type="button"
              :class="{ selected: appointmentStart === slot.value }"
              :disabled="!slot.available"
              :aria-pressed="appointmentStart === slot.value"
              @click="appointmentStart = slot.value"
            >{{ slot.label }}</button>
          </div>
        </section>

        <section>
          <header class="reservation-step"><span>4</span><div><h2>Training goal</h2><p>Keep it short so the coach can prepare.</p></div></header>
          <input v-model.trim="form.note" class="goal-input" maxlength="500" placeholder="Strength, mobility, form check..." required>
        </section>

        <footer class="reservation-summary">
          <div>
            <small>YOUR APPOINTMENT</small>
            <strong v-if="appointmentStart">{{ date(appointmentStart) }} · 60 minutes</strong>
            <strong v-else>Choose an available time</strong>
          </div>
          <button class="legacy-button" type="submit" :disabled="!form.coachId || !appointmentStart || !form.note || reserving">{{ reserving ? 'Requesting...' : 'Request booking' }}</button>
        </footer>
      </form>
      <form v-else class="reservation-card legacy-card" @submit.prevent="createAvailability">
        <section>
          <header class="reservation-step"><span>1</span><div><h2>Set bookable time</h2><p>Members can request one-hour appointments inside these blocks.</p></div></header>
          <div class="admin-form availability-form">
            <select v-model.number="availabilityForm.dayOfWeek" aria-label="Day of week">
              <option v-for="(day, index) in dayNames" :key="day" :value="index + 1">{{ day }}</option>
            </select>
            <input v-model="availabilityForm.startsAt" type="time" aria-label="Start time" required>
            <input v-model="availabilityForm.endsAt" type="time" aria-label="End time" required>
            <button type="submit">Add time</button>
          </div>
        </section>
        <section>
          <header class="reservation-step"><span>2</span><div><h2>Gym calendar</h2><p>Closed days cannot be booked.</p></div></header>
          <div class="mini-calendar">
            <article v-for="day in rollingCalendarDays" :key="day.value" :class="{ closed: day.closed, partial: day.partial }">
              <small>{{ day.day }}</small><strong>{{ day.date }}</strong><span>{{ day.closed ? 'Closed' : day.partial ? 'Limited' : 'Open' }}</span>
            </article>
          </div>
        </section>
        <section>
          <header class="reservation-step"><span>3</span><div><h2>Your availability</h2><p>Remove a block when you no longer take bookings then.</p></div></header>
          <div class="data-list compact-list">
            <article v-for="item in coachAvailability" :key="item.id">
              <div><h3>{{ dayNames[Number(item.dayOfWeek) - 1] }}</h3><p>{{ timeText(item.startsAt) }} - {{ timeText(item.endsAt) }}</p></div>
              <button type="button" @click="removeAvailability(item.id)">Remove</button>
            </article>
            <p v-if="!coachAvailability.length" class="empty">No bookable time yet.</p>
          </div>
        </section>
      </form>
      <div class="data-list legacy-card">
        <article v-for="item in rows" :key="item.id">
          <div><h3>{{ item.coachName || item.memberName }}</h3><p>{{ date(item.startsAt) }} · {{ item.note }}</p></div><span class="pill">{{ item.status }}</span>
          <button v-if="session.user?.role === 'MEMBER' && ['PENDING','CONFIRMED'].includes(item.status)" type="button" @click="remove(`/coach-appointments/${item.id}`, 'appointment')">Cancel</button>
          <div v-else-if="session.user?.role === 'COACH' && ['PENDING','CONFIRMED'].includes(item.status)" class="row-actions"><button type="button" @click="updateAppointment(item.id, 'CONFIRMED')">Confirm</button><button type="button" @click="updateAppointment(item.id, 'COMPLETED')">Complete</button></div>
        </article>
        <p v-if="!rows.length" class="empty">No appointments yet.</p>
      </div>
    </template>

    <section v-else-if="feature === 'equipment'" class="legacy-card-grid">
      <article v-for="item in rows" :key="item.id" class="legacy-card equipment-card">
        <div class="equipment-art"><img src="../assets/imgs/icon-treadmill.png" alt=""></div>
        <small>{{ item.category }}</small><h3>{{ item.name }}</h3><p>{{ item.description }}</p><span class="pill">{{ item.status }}</span>
        <RouterLink
          v-if="item.spaceId"
          class="legacy-button"
          :to="`/front/gym-map?spaceId=${item.spaceId}`"
        >{{ item.floorName }} · {{ item.spaceName }}</RouterLink>
        <span v-else class="equipment-unavailable">Location not assigned</span>
      </article>
    </section>

    <template v-else-if="feature === 'equipmentReservations'">
      <form class="reservation-card legacy-card" @submit.prevent="reserveEquipment">
        <section>
          <header class="reservation-step"><span>1</span><div><h2>Choose equipment</h2><p>Only ready-to-use equipment can be booked.</p></div></header>
          <div class="equipment-options" role="radiogroup" aria-label="Equipment">
            <button
              v-for="item in secondary"
              :key="item.id"
              type="button"
              :class="{ selected: form.equipmentId === item.id }"
              :disabled="item.status !== 'AVAILABLE'"
              :aria-pressed="form.equipmentId === item.id"
              @click="form.equipmentId = item.id"
            >
              <small>{{ item.category }}</small><strong>{{ item.name }}</strong>
              <span>{{ item.status === 'AVAILABLE' ? 'Ready' : 'Unavailable' }}</span>
            </button>
          </div>
        </section>

        <section>
          <header class="reservation-step"><span>2</span><div><h2>Pick a day</h2><p>Choose from the next 7 days.</p></div></header>
          <div class="date-options" role="radiogroup" aria-label="Reservation date">
            <button
              v-for="day in reservationDays"
              :key="day.value"
              type="button"
              :class="{ selected: reservationDate === day.value }"
              :disabled="closedDateSet.has(day.value)"
              :aria-pressed="reservationDate === day.value"
              @click="reservationDate = day.value"
            ><small>{{ day.day }}</small><strong>{{ day.date }}</strong><span v-if="closedDateSet.has(day.value)">Closed</span></button>
          </div>
        </section>

        <section class="reservation-time">
          <header class="reservation-step"><span>3</span><div><h2>Choose a start time</h2><p>Select once; the end time is calculated for you.</p></div></header>
          <div class="duration-options" role="radiogroup" aria-label="Reservation duration">
            <span>Duration</span>
            <button v-for="minutes in [30, 60]" :key="minutes" type="button" :class="{ selected: reservationDuration === minutes }" :aria-pressed="reservationDuration === minutes" @click="reservationDuration = minutes">{{ minutes }} min</button>
          </div>
          <p v-if="availabilityLoading" class="slot-message">Checking availability…</p>
          <div v-else class="time-options" role="radiogroup" aria-label="Available start times">
            <button
              v-for="slot in timeSlots"
              :key="slot.value"
              type="button"
              :class="{ selected: selectedStart === slot.value }"
              :disabled="!slot.available"
              :aria-pressed="selectedStart === slot.value"
              @click="selectedStart = slot.value"
            >{{ slot.label }}</button>
          </div>
        </section>

        <footer class="reservation-summary">
          <div>
            <small>YOUR RESERVATION</small>
            <strong v-if="selectedStart">{{ date(selectedStart) }} · {{ reservationDuration }} minutes</strong>
            <strong v-else>Choose an available start time</strong>
          </div>
          <button class="legacy-button" type="submit" :disabled="!selectedStart || reserving">{{ reserving ? 'Reserving…' : 'Confirm reservation' }}</button>
        </footer>
      </form>
      <div class="data-list legacy-card"><article v-for="item in rows" :key="item.id"><div><h3>{{ item.equipmentName }}</h3><p>{{ date(item.startsAt) }} — {{ date(item.endsAt) }}</p></div><span class="pill">{{ item.status }}</span><button v-if="item.status === 'CONFIRMED'" type="button" @click="remove(`/equipment-reservations/${item.id}`, 'reservation')">Cancel</button></article><p v-if="!rows.length" class="empty">No equipment reservations yet.</p></div>
    </template>

    <template v-else-if="['community', 'myPosts'].includes(feature)">
      <form class="post-form legacy-card" @submit.prevent="createPost"><input v-model.trim="form.title" maxlength="160" placeholder="Post title" required><textarea v-model.trim="form.content" maxlength="5000" rows="3" placeholder="Share your fitness experience…" required></textarea><button class="legacy-button" type="submit">Publish post</button></form>
      <div class="post-grid"><article v-for="item in rows" :key="item.id" class="legacy-card post-card"><small>{{ item.authorName || session.user?.displayName }} · {{ date(item.createdAt) }}</small><h3>{{ item.title }}</h3><p>{{ item.content }}</p><button v-if="feature === 'myPosts'" type="button" @click="remove(`/posts/${item.id}`, 'post')">Delete</button></article></div>
    </template>

    <section v-else-if="feature === 'card'" class="member-card">
      <img class="member-logo" src="../assets/imgs/logo.png" alt="Gym Panel">
      <p>MEMBERSHIP CARD</p>
      <h2>{{ rows[0]?.displayName }}</h2>
      <strong>{{ rows[0]?.memberNumber }}</strong>
      <div class="membership-pass">
        <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="Short-lived membership QR code">
        <span v-else>Preparing secure pass…</span>
      </div>
      <small>{{ rows[0]?.planName || 'No membership plan' }} · {{ rows[0]?.status }}</small>
      <em>Secure code refreshes every 30 seconds</em>
    </section>

    <section v-else-if="feature === 'chat'" class="chat-panel legacy-card">
      <aside><button v-for="peer in rows" :key="peer.id" :class="{ active: selected?.id === peer.id }" type="button" @click="choosePeer(peer)"><img src="../assets/imgs/avatar-default.jpg" alt=""><span>{{ peer.displayName }}<small>{{ peer.role }}</small></span></button></aside>
      <div class="conversation"><header>{{ selected?.displayName || 'Choose a conversation' }}</header><div class="messages"><p v-for="message in secondary" :key="message.id" :class="{ mine: message.senderId === session.user?.id }">{{ message.content }}<small>{{ date(message.createdAt) }}</small></p></div><form @submit.prevent="sendMessage"><input v-model.trim="form.content" maxlength="1000" placeholder="Type a message…" :disabled="!selected"><button class="legacy-button" type="submit">Send</button></form></div>
    </section>

    <section v-else-if="feature === 'vr'" class="vr-tour legacy-card">
      <div><p class="section-kicker">360° PREVIEW</p><h2>Explore your gym</h2><p>A lightweight panoramic preview preserves the original VR tour page without requiring a headset.</p><RouterLink class="legacy-button" to="/front/equipment">Browse equipment</RouterLink></div>
    </section>
  </div>
</template>
