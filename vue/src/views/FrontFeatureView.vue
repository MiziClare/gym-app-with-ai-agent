<script setup lang="ts">
import { computed, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import QRCode from 'qrcode'
import { api, messageOf } from '../api'
import { loadUnread, session, unread } from '../state'

type Row = Record<string, any>
const route = useRoute()
const feature = computed(() => String(route.meta.feature || 'profile'))
const postDetail = computed(() => route.path.endsWith('/experienceDetail'))
const rows = ref<Row[]>([])
const secondary = ref<Row[]>([])
const selected = ref<Row | null>(null)
const forumText = ref('')
const forumSort = ref<'default' | 'latestReply' | 'latest'>('default')
const forumNotifications = ref<Row[]>([])
const forumFeedbackRows = ref<Row[]>([])
const replyTo = ref<Row | null>(null)
const loading = ref(false)
const form = reactive({ coachId: 0, startsAt: '', note: '', title: '', content: '' })
const availabilityForm = reactive({ dayOfWeek: 1, startsAt: '09:00', endsAt: '17:00' })
const appointmentDate = ref(localDate(new Date()))
const appointmentStart = ref('')
const closedDays = ref<Row[]>([])
const operationHours = reactive({ opensAt: '06:00', closesAt: '22:00' })
const calendarMonthDate = ref(new Date(new Date().getFullYear(), new Date().getMonth(), 1))
const selectedCalendarDate = ref(localDate(new Date()))
const coachAvailability = ref<Row[]>([])
const reserving = ref(false)
const qrCodeUrl = ref('')
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
const reservationGroups = computed(() => [
  {
    title: 'Upcoming',
    subtitle: 'Bookings you can still manage',
    rows: rows.value.filter(item => !isHistory(item)),
    history: false,
  },
  {
    title: 'History',
    subtitle: 'Completed and cancelled bookings',
    rows: rows.value.filter(isHistory),
    history: true,
  },
].filter(group => group.rows.length))

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
  connections: ['Coach Connections', session.user?.role === 'COACH' ? 'Review member requests to work with you' : 'Track your coach contact requests'],
  appointments: ['Coach Booking', session.user?.role === 'COACH' ? 'Review member appointment requests' : 'Book time with a coach'],
  operationCalendar: ['Operations Calendar', 'View regular hours and scheduled closures'],
  community: ['Fitness Community', 'Share experience and learn from other members'],
  myPosts: ['My Posts', 'Manage the experience you have shared'],
  savedPosts: ['Saved Posts', 'Posts you want to revisit'],
  forumMessages: ['Forum Messages', 'Review community activity and staff feedback'],
  card: ['Membership E-card', 'Your digital gym identity'],
  chat: [session.user?.role === 'COACH' ? 'Member Chat' : 'Coach Chat', 'Keep training conversations in one place'],
  vr: ['VR Gym Tour', 'Explore the training space before your visit'],
}

watch(() => route.fullPath, load, { immediate: true })
onUnmounted(stopPassRefresh)

async function load() {
  stopPassRefresh()
  qrCodeUrl.value = ''
  loading.value = true
  rows.value = []
  secondary.value = []
  selected.value = null
  replyTo.value = null
  try {
    if (feature.value === 'coaches') {
      const [coaches, connections] = await Promise.all([
        api.get('/coaches'),
        api.get('/coach-connections'),
      ])
      rows.value = coaches.data
      secondary.value = connections.data
    }
    if (feature.value === 'connections') {
      rows.value = (await api.get('/coach-connections')).data
      if (session.user?.role === 'MEMBER') {
        await api.patch('/coach-connections/read')
        unread.connections = 0
      }
    }
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
    if (['community', 'myPosts', 'savedPosts'].includes(feature.value)) {
      if (postDetail.value) {
        const postId = Number(route.query.id)
        rows.value = [(await api.get(`/posts/${postId}`)).data]
        secondary.value = (await api.get(`/posts/${postId}/comments`)).data
      } else {
        const path = feature.value === 'myPosts' ? '/posts/me'
          : feature.value === 'savedPosts' ? '/posts/favorites' : '/posts'
        rows.value = (await api.get(path, {
          params: path === '/posts' ? { sort: forumSort.value } : undefined,
        })).data
      }
    }
    if (feature.value === 'forumMessages') {
      const [notifications, feedback] = await Promise.all([
        api.get('/forum-notifications'),
        api.get('/forum-feedback/me'),
      ])
      forumNotifications.value = notifications.data
      forumFeedbackRows.value = feedback.data
      await api.patch('/forum-notifications/read')
      unread.forum = 0
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
  peer.unreadCount = 0
  await loadUnread()
}

function connectionFor(coachId: number) {
  return secondary.value.find(item =>
    item.coachId === coachId && (item.status === 'PENDING' || item.connected))
}

async function requestConnection(coach: Row) {
  try {
    const { value } = await ElMessageBox.prompt(
      'Briefly describe your goal and what support you are looking for.',
      `Contact ${coach.displayName}`,
      {
        inputType: 'textarea',
        inputPlaceholder: 'For example: I want help building a safe strength routine.',
        inputValidator: value => value.trim() ? true : 'Please describe your goal',
      }
    )
    await api.post('/coach-connections', { coachId: coach.id, message: value })
    ElMessage.success('Request sent to coach')
    await load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error))
  }
}

async function respondToConnection(id: number, status: 'ACCEPTED' | 'DECLINED') {
  try {
    await api.patch(`/coach-connections/${id}`, { status })
    ElMessage.success(status === 'ACCEPTED' ? 'Member connected' : 'Request declined')
    await Promise.all([load(), loadUnread()])
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function cancelConnection(id: number) {
  try {
    await api.delete(`/coach-connections/${id}`)
    ElMessage.success('Request cancelled')
    await load()
  } catch (error) { ElMessage.error(messageOf(error)) }
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

async function togglePost(item: Row, kind: 'like' | 'favorite') {
  try {
    const active = kind === 'like' ? item.liked : item.favorited
    await api[active ? 'delete' : 'put'](`/posts/${item.id}/${kind}`)
    if (kind === 'like') {
      item.liked = !active
      item.likeCount += active ? -1 : 1
    } else {
      item.favorited = !active
      if (feature.value === 'savedPosts' && active) rows.value = rows.value.filter(post => post.id !== item.id)
    }
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function addComment(post: Row) {
  if (!forumText.value.trim()) return
  try {
    await api.post(`/posts/${post.id}/comments`, {
      parentId: replyTo.value?.id || null,
      content: forumText.value,
    })
    forumText.value = ''
    replyTo.value = null
    secondary.value = (await api.get(`/posts/${post.id}/comments`)).data
    post.commentCount = secondary.value.length
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function toggleCommentLike(post: Row, comment: Row) {
  try {
    await api[comment.liked ? 'delete' : 'put'](
      `/posts/${post.id}/comments/${comment.id}/like`
    )
    comment.liked = !comment.liked
    comment.likeCount += comment.liked ? 1 : -1
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function sendFeedback(post: Row | null = null) {
  try {
    const { value } = await ElMessageBox.prompt(
      post ? `Tell staff what is wrong with “${post.title}”.` : 'Share forum feedback with gym staff.',
      post ? 'Report post' : 'Forum feedback',
      { inputType: 'textarea', inputValidator: value => value.trim() ? true : 'Please enter a message' }
    )
    await api.post('/forum-feedback', { postId: post?.id || null, content: value })
    ElMessage.success('Sent to gym staff')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error))
  }
}

function notificationText(item: Row) {
  const messages: Record<string, string> = {
    COMMENT: `${item.actorName} commented on “${item.postTitle}”`,
    REPLY: `${item.actorName} replied to your comment`,
    LIKE: `${item.actorName} liked “${item.postTitle}”`,
    COMMENT_LIKE: `${item.actorName} liked your comment`,
    FEEDBACK_REPLY: 'Gym staff replied to your feedback',
  }
  return messages[item.type] || 'Forum update'
}

async function sortPosts(sort: 'default' | 'latestReply' | 'latest') {
  forumSort.value = sort
  try {
    rows.value = (await api.get('/posts', { params: { sort } })).data
  } catch (error) { ElMessage.error(messageOf(error)) }
}

function isHistory(item: Row) {
  return ['COMPLETED', 'CANCELLED'].includes(item.status)
}

function statusLabel(status: string) {
  return {
    PENDING: 'Pending',
    CONFIRMED: 'Confirmed',
    COMPLETED: 'Completed',
    CANCELLED: 'Cancelled',
  }[status] || status
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

watch(() => form.coachId, () => {
  if (feature.value === 'appointments' && session.user?.role === 'MEMBER') void loadCoachAvailability()
})
</script>

<template>
  <div class="legacy-page">
    <div class="legacy-heading">
      <div><p class="section-kicker">GYM PANEL</p><h1>{{ titles[feature]?.[0] }}</h1><p>{{ titles[feature]?.[1] }}</p></div>
      <nav v-if="['community', 'myPosts', 'savedPosts', 'forumMessages'].includes(feature)" class="forum-nav" aria-label="Forum views">
        <RouterLink to="/front/experience">All posts</RouterLink>
        <RouterLink to="/front/myExperience">My posts</RouterLink>
        <RouterLink to="/front/savedPosts">Saved</RouterLink>
        <RouterLink to="/front/forumMessages">Messages <em v-if="unread.forum" class="inline-badge">{{ Math.min(unread.forum, 99) }}{{ unread.forum > 99 ? '+' : '' }}</em></RouterLink>
        <button type="button" @click="sendFeedback()">Feedback</button>
      </nav>
    </div>

    <div v-if="loading" class="legacy-card empty">Loading…</div>

    <section v-else-if="feature === 'forumMessages'" class="forum-messages">
      <div class="legacy-card">
        <h2>Activity</h2>
        <article v-for="item in forumNotifications" :key="item.id" :class="{ unread: !item.isRead }">
          <strong>{{ notificationText(item) }}</strong><p v-if="item.content">{{ item.content }}</p><small>{{ date(item.createdAt) }}</small>
        </article>
        <p v-if="!forumNotifications.length" class="empty">No forum activity yet.</p>
      </div>
      <div class="legacy-card">
        <h2>Reports and feedback</h2>
        <article v-for="item in forumFeedbackRows" :key="item.id">
          <strong>{{ item.postTitle ? `Report: ${item.postTitle}` : 'General feedback' }}</strong>
          <p>{{ item.content }}</p><blockquote v-if="item.adminReply">{{ item.adminReply }}</blockquote>
          <small>{{ item.status }} · {{ date(item.createdAt) }}</small>
        </article>
        <p v-if="!forumFeedbackRows.length" class="empty">No feedback sent.</p>
      </div>
    </section>

    <section v-else-if="feature === 'profile'" class="profile-card legacy-card">
      <img src="../assets/imgs/avatar-default.jpg" alt="">
      <div><small>DISPLAY NAME</small><h2>{{ session.user?.displayName }}</h2><p>@{{ session.user?.username }}</p></div>
      <dl><div><dt>Email</dt><dd>{{ session.user?.email }}</dd></div><div><dt>Account role</dt><dd>{{ session.user?.role }}</dd></div></dl>
    </section>

    <section v-else-if="feature === 'coaches'" class="legacy-card-grid">
      <article v-for="coach in rows" :key="coach.id" class="legacy-card coach-card">
        <img src="../assets/imgs/avatar-coach.png" alt="">
        <h3>{{ coach.displayName }}</h3><strong>{{ coach.specialties }}</strong><p>{{ coach.bio }}</p>
        <div class="coach-actions">
          <RouterLink class="legacy-button" to="/front/reserve">Book coach</RouterLink>
          <button v-if="connectionFor(coach.id)?.status === 'PENDING'" class="legacy-button secondary" type="button" disabled>Request pending</button>
          <RouterLink v-else-if="connectionFor(coach.id)?.connected" class="legacy-button secondary" to="/front/chat">Chat</RouterLink>
          <button v-else class="legacy-button secondary" type="button" @click="requestConnection(coach)">Contact coach</button>
        </div>
      </article>
    </section>

    <section v-else-if="feature === 'connections'" class="data-list legacy-card">
      <article v-for="item in rows" :key="item.id">
        <div>
          <h3>{{ item.coachName || item.memberName }}</h3>
          <p>{{ item.message }}</p>
          <small>{{ date(item.createdAt) }}</small>
        </div>
        <span class="pill" :class="`status-${String(item.status).toLowerCase()}`">{{ item.status }}</span>
        <template v-if="session.user?.role === 'COACH' && item.status === 'PENDING'">
          <button type="button" @click="respondToConnection(item.id, 'ACCEPTED')">Accept</button>
          <button type="button" @click="respondToConnection(item.id, 'DECLINED')">Decline</button>
        </template>
        <button v-else-if="session.user?.role === 'MEMBER' && item.status === 'PENDING'" type="button" @click="cancelConnection(item.id)">Cancel</button>
        <RouterLink v-else-if="item.status === 'ACCEPTED' && item.connected" class="legacy-button" :to="session.user?.role === 'COACH' ? '/front/coachChat' : '/front/chat'">Chat</RouterLink>
      </article>
      <p v-if="!rows.length" class="empty">No connection requests yet.</p>
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
      <div v-if="rows.length" class="booking-groups">
        <section v-for="group in reservationGroups" :key="group.title" class="booking-group">
          <header class="booking-list-head">
            <div><h2>{{ group.title }}</h2><p>{{ group.subtitle }}</p></div>
            <span>{{ group.rows.length }}</span>
          </header>
          <div class="data-list legacy-card">
            <article v-for="item in group.rows" :key="item.id" :class="{ historical: group.history }">
              <div><h3>{{ item.coachName || item.memberName }}</h3><p>{{ date(item.startsAt) }} · {{ item.note }}</p></div>
              <span class="pill" :class="`status-${item.status.toLowerCase()}`">{{ statusLabel(item.status) }}</span>
              <button v-if="session.user?.role === 'MEMBER' && ['PENDING','CONFIRMED'].includes(item.status)" type="button" @click="remove(`/coach-appointments/${item.id}`, 'appointment')">Cancel</button>
              <div v-else-if="session.user?.role === 'COACH' && ['PENDING','CONFIRMED'].includes(item.status)" class="row-actions"><button type="button" @click="updateAppointment(item.id, 'CONFIRMED')">Confirm</button><button type="button" @click="updateAppointment(item.id, 'COMPLETED')">Complete</button></div>
            </article>
          </div>
        </section>
      </div>
      <p v-else class="empty legacy-card">No appointments yet.</p>
    </template>

    <template v-else-if="['community', 'myPosts', 'savedPosts'].includes(feature)">
      <RouterLink v-if="postDetail" class="forum-back" to="/front/experience">← Back to all posts</RouterLink>
      <form v-if="feature === 'community' && !postDetail" class="post-form legacy-card" @submit.prevent="createPost"><input v-model.trim="form.title" maxlength="160" placeholder="Post title" required><textarea v-model.trim="form.content" maxlength="5000" rows="3" placeholder="Share your fitness experience…" required></textarea><button class="legacy-button" type="submit">Publish post</button></form>
      <nav v-if="feature === 'community' && !postDetail" class="forum-sort" aria-label="Sort posts">
        <button type="button" :class="{ active: forumSort === 'default' }" @click="sortPosts('default')">Default</button>
        <button type="button" :class="{ active: forumSort === 'latestReply' }" @click="sortPosts('latestReply')">Latest replies</button>
        <button type="button" :class="{ active: forumSort === 'latest' }" @click="sortPosts('latest')">Newest posts</button>
      </nav>
      <div class="post-grid">
        <article v-for="item in rows" :key="item.id" class="legacy-card post-card">
          <small>{{ item.authorName }} · {{ date(item.createdAt) }}</small>
          <RouterLink v-if="!postDetail" class="post-title-link" :to="{ path: '/front/experienceDetail', query: { id: item.id } }"><h3>{{ item.title }}</h3></RouterLink>
          <h3 v-else>{{ item.title }}</h3>
          <p>{{ item.content }}</p>
          <div class="post-actions">
            <button type="button" :class="{ active: item.liked }" :aria-pressed="Boolean(item.liked)" @click="togglePost(item, 'like')">♥ {{ item.likeCount }}</button>
            <RouterLink v-if="!postDetail" :to="{ path: '/front/experienceDetail', query: { id: item.id } }">Comments {{ item.commentCount }}</RouterLink>
            <span v-else>Comments {{ item.commentCount }}</span>
            <button type="button" :class="{ active: item.favorited }" :aria-pressed="Boolean(item.favorited)" @click="togglePost(item, 'favorite')">{{ item.favorited ? 'Saved' : 'Save' }}</button>
            <button type="button" @click="sendFeedback(item)">Report</button>
            <button v-if="feature === 'myPosts'" type="button" @click="remove(`/posts/${item.id}`, 'post')">Delete</button>
          </div>
          <p v-if="!postDetail && item.topComment" class="top-comment"><strong>Top comment</strong> {{ item.topComment }}</p>
          <section v-if="postDetail" class="post-comments">
            <article v-for="comment in secondary" :key="comment.id" :class="{ reply: comment.parentId }">
              <small>{{ comment.authorName }}<template v-if="comment.parentAuthorName"> replied to {{ comment.parentAuthorName }}</template> · {{ date(comment.createdAt) }}</small>
              <p>{{ comment.content }}</p>
              <button type="button" :class="{ active: comment.liked }" :aria-pressed="Boolean(comment.liked)" @click="toggleCommentLike(item, comment)">♥ {{ comment.likeCount }}</button>
              <button type="button" @click="replyTo = comment">Reply</button>
            </article>
            <p v-if="!secondary.length" class="empty">No comments yet.</p>
            <form @submit.prevent="addComment(item)">
              <span v-if="replyTo">Replying to {{ replyTo.authorName }} <button type="button" @click="replyTo = null">×</button></span>
              <textarea v-model.trim="forumText" maxlength="2000" rows="2" :placeholder="replyTo ? 'Write a reply…' : 'Write a comment…'" required></textarea>
              <button class="legacy-button" type="submit">Post</button>
            </form>
          </section>
        </article>
      </div>
      <p v-if="!rows.length" class="empty legacy-card">No posts found.</p>
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
      <aside><button v-for="peer in rows" :key="peer.id" :class="{ active: selected?.id === peer.id }" type="button" @click="choosePeer(peer)"><img src="../assets/imgs/avatar-default.jpg" alt=""><span>{{ peer.displayName }}<small>{{ peer.role }}</small></span><em v-if="peer.unreadCount" class="inline-badge">{{ Math.min(peer.unreadCount, 99) }}{{ peer.unreadCount > 99 ? '+' : '' }}</em></button></aside>
      <div class="conversation"><header>{{ selected?.displayName || 'Choose a conversation' }}</header><div class="messages"><p v-for="message in secondary" :key="message.id" :class="{ mine: message.senderId === session.user?.id }">{{ message.content }}<small>{{ date(message.createdAt) }}</small></p></div><form @submit.prevent="sendMessage"><input v-model.trim="form.content" maxlength="1000" placeholder="Type a message…" :disabled="!selected"><button class="legacy-button" type="submit">Send</button></form></div>
    </section>

    <section v-else-if="feature === 'vr'" class="vr-tour legacy-card">
      <div><p class="section-kicker">360° PREVIEW</p><h2>Explore your gym</h2><p>A lightweight panoramic preview preserves the original VR tour page without requiring a headset.</p><RouterLink class="legacy-button" to="/front/equipment">Browse equipment</RouterLink></div>
    </section>
  </div>
</template>
