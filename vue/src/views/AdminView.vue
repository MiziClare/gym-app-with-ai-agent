<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api, messageOf } from '../api'
import { session } from '../state'

type Row = Record<string, any>
const route = useRoute()
const module = computed(() => String(route.meta.module || 'overview'))
const rows = ref<Row[]>([])
const overview = ref<Row>({})
const dashboardClosedDays = ref<Row[]>([])
const operationHours = reactive({ opensAt: '06:00', closesAt: '22:00' })
const loading = ref(false)
const calendarSaving = ref(false)
const adminCalendarMonthDate = ref(new Date(new Date().getFullYear(), new Date().getMonth(), 1))
const form = reactive({ title: '', content: '', name: '', category: '', description: '', coverKey: 'course', spaceId: '' as number | '', durationMinutes: 45, defaultCapacity: 12 })
const assignment = reactive({ coachId: '' as number | '', memberId: '' as number | '', startsOn: new Date().toLocaleDateString('en-CA'), endsOn: '' })
const closedDay = reactive({
  closedOn: new Date().toLocaleDateString('en-CA'),
  type: 'FULL' as 'FULL' | 'PARTIAL',
  startsAt: '12:00',
  endsAt: '14:00',
  reason: '',
})
const assignmentOptions = reactive<{ coaches: Row[]; members: Row[] }>({ coaches: [], members: [] })
const sessionForm = reactive({
  courseId: '' as number | '',
  coachId: '' as number | '',
  startsAt: localDateTime(new Date(Date.now() + 60 * 60_000)),
  endsAt: localDateTime(new Date(Date.now() + 2 * 60 * 60_000)),
  capacity: 12,
  spaceId: '' as number | '',
})
const sessionResourceUnits = reactive<Record<number, number>>({})
const sessionOptions = reactive<{ courses: Row[]; coaches: Row[]; resources: Row[] }>({ courses: [], coaches: [], resources: [] })
const spaceOptions = ref<Row[]>([])
const names: Record<string, string> = {
  overview: 'Dashboard', admins: 'Administrators', members: 'Members', coaches: 'Coaches',
  notices: 'Notices', courses: 'Courses', bookings: 'Course Bookings', appointments: 'Coach Bookings',
  coachAssignments: 'Coach Assignments', sessions: 'Class Schedule', visits: 'Gym Visits',
  closedDays: 'Operations Calendar', posts: 'Community Posts', profile: 'My Account',
}
const columns = computed(() => Object.keys(rows.value[0] || {}).filter(key =>
  (module.value !== 'coachAssignments' || !['coachId', 'memberId'].includes(key))
))
const todayKey = computed(() => localDate(new Date()))
const selectedClosures = computed(() => dashboardClosedDays.value.filter(item => closedOn(item) === closedDay.closedOn))
const selectedPastDay = computed(() => closedDay.closedOn < todayKey.value)
const adminCalendarMonth = computed(() => new Intl.DateTimeFormat('en-CA', { month: 'long', year: 'numeric' }).format(adminCalendarMonthDate.value))
const weekdayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
const adminCalendarDays = computed(() => {
  const month = adminCalendarMonthDate.value
  const firstDay = new Date(month.getFullYear(), month.getMonth(), 1)
  const start = new Date(firstDay)
  start.setDate(firstDay.getDate() - ((firstDay.getDay() + 6) % 7))
  return Array.from({ length: 42 }, (_, offset) => {
    const value = new Date(start)
    value.setDate(value.getDate() + offset)
    const key = localDate(value)
    const closures = dashboardClosedDays.value.filter(item => closedOn(item) === key)
    return {
      value: key,
      day: value.getDate(),
      muted: value.getMonth() !== month.getMonth(),
      past: key < todayKey.value,
      selected: closedDay.closedOn === key,
      closures,
      closed: closures.some(item => !item.startsAt),
      partial: closures.some(item => item.startsAt),
    }
  })
})
const visibleClosedCount = computed(() => adminCalendarDays.value.filter(day => day.closures.length && !day.muted).length)

watch(module, load, { immediate: true })

async function load() {
  loading.value = true
  rows.value = []
  try {
    if (module.value === 'overview') {
      const [stats, closed, hours] = await Promise.all([
        api.get('/admin/overview'),
        api.get('/admin/closed-days'),
        api.get('/admin/operation-hours'),
      ])
      overview.value = stats.data
      dashboardClosedDays.value = closed.data
      setOperationHours(hours.data)
    }
    else if (module.value === 'closedDays') {
      const [closed, hours] = await Promise.all([
        api.get('/admin/closed-days'),
        api.get('/admin/operation-hours'),
      ])
      dashboardClosedDays.value = closed.data
      setOperationHours(hours.data)
    }
    else if (module.value === 'sessions') {
      const [sessions, courses, coaches, resources, layout] = await Promise.all([
        api.get('/admin/course-sessions'),
        api.get('/courses'),
        api.get('/admin/users', { params: { role: 'COACH' } }),
        api.get('/admin/resources'),
        api.get('/admin/gym-layout'),
      ])
      rows.value = sessions.data
      sessionOptions.courses = courses.data
      sessionOptions.coaches = coaches.data.filter((item: Row) => item.active)
      sessionOptions.resources = resources.data.filter((item: Row) => item.totalUnits > 0)
      Object.keys(sessionResourceUnits).forEach(key => delete sessionResourceUnits[Number(key)])
      spaceOptions.value = flattenSpaces(layout.data.floors)
    } else if (module.value === 'coachAssignments') {
      const [assignments, coaches, members] = await Promise.all([
        api.get('/admin/coach-assignments'),
        api.get('/admin/users', { params: { role: 'COACH' } }),
        api.get('/admin/users', { params: { role: 'MEMBER' } }),
      ])
      rows.value = assignments.data
      assignmentOptions.coaches = coaches.data.filter((item: Row) => item.active)
      assignmentOptions.members = members.data.filter((item: Row) => item.active)
    } else if (module.value === 'profile' && session.user) rows.value = [session.user]
    else if (['admins', 'members', 'coaches'].includes(module.value)) {
      const role = { admins: 'ADMIN', members: 'MEMBER', coaches: 'COACH' }[module.value]
      rows.value = (await api.get('/admin/users', { params: { role } })).data
    } else {
      const paths: Record<string, string> = {
        notices: '/admin/notices', courses: '/courses', bookings: '/admin/bookings',
        appointments: '/admin/coach-appointments',
        visits: '/admin/member-visits', posts: '/admin/posts',
      }
      rows.value = (await api.get(paths[module.value])).data
    }
  } catch (error) { ElMessage.error(messageOf(error)) }
  finally { loading.value = false }
}

async function create() {
  try {
    if (module.value === 'notices') await api.post('/admin/notices', { title: form.title, content: form.content })
    if (module.value === 'courses') await api.post('/admin/courses', form)
    if (module.value === 'sessions') {
      await api.post('/admin/course-sessions', {
        ...sessionForm,
        coachId: sessionForm.coachId || null,
        spaceId: sessionForm.spaceId || null,
        startsAt: new Date(sessionForm.startsAt).toISOString(),
        endsAt: new Date(sessionForm.endsAt).toISOString(),
        resources: sessionOptions.resources
          .filter(item => (sessionResourceUnits[item.id] ?? 0) > 0)
          .map(item => ({ equipmentId: item.id, requiredUnits: sessionResourceUnits[item.id] })),
      })
      Object.keys(sessionResourceUnits).forEach(key => delete sessionResourceUnits[Number(key)])
    }
    if (module.value === 'coachAssignments') {
      await api.post('/admin/coach-assignments', { ...assignment, endsOn: assignment.endsOn || null })
      assignment.endsOn = ''
    }
    Object.assign(form, { title: '', content: '', name: '', category: '', description: '', coverKey: 'course', spaceId: '', durationMinutes: 45, defaultCapacity: 12 })
    ElMessage.success('Saved')
    await load()
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function saveClosedDay() {
  await api.post('/admin/closed-days', {
    closedOn: closedDay.closedOn,
    startsAt: closedDay.type === 'PARTIAL' ? closedDay.startsAt : null,
    endsAt: closedDay.type === 'PARTIAL' ? closedDay.endsAt : null,
    reason: closedDay.reason,
  })
}

async function saveDashboardClosedDay() {
  if (calendarSaving.value) return
  try {
    if (selectedPastDay.value) {
      calendarMessage('warning', 'Past dates are read-only')
      return
    }
    calendarSaving.value = true
    await saveClosedDay()
    await load()
    calendarMessage('success', 'Closure saved')
  } catch (error) { calendarMessage('error', messageOf(error)) }
  finally { calendarSaving.value = false }
}

function selectCalendarDay(day: Row) {
  closedDay.closedOn = day.value
  closedDay.reason = ''
}

function moveAdminCalendar(months: number) {
  const value = new Date(adminCalendarMonthDate.value)
  value.setMonth(value.getMonth() + months)
  adminCalendarMonthDate.value = value
}

function resetAdminCalendar() {
  const today = new Date()
  adminCalendarMonthDate.value = new Date(today.getFullYear(), today.getMonth(), 1)
  closedDay.closedOn = localDate(today)
  closedDay.reason = ''
}

async function reopenSelectedDay(item: Row) {
  if (selectedPastDay.value || calendarSaving.value) return
  try {
    calendarSaving.value = true
    await deleteClosedDay(item)
    await load()
    calendarMessage('success', 'Closure removed')
  } catch (error) { calendarMessage('error', messageOf(error)) }
  finally { calendarSaving.value = false }
}

async function saveOperationHours() {
  if (calendarSaving.value) return
  try {
    calendarSaving.value = true
    await api.put('/admin/operation-hours', operationHours)
    calendarMessage('success', 'Default hours saved')
  } catch (error) { calendarMessage('error', messageOf(error)) }
  finally { calendarSaving.value = false }
}

function calendarMessage(type: 'success' | 'warning' | 'error', message: string) {
  ElMessage.closeAll()
  ElMessage({ type, message })
}

async function deleteClosedDay(item: Row) {
  await api.delete(`/admin/closed-days/${item.id}`)
}

async function remove(item: Row) {
  try {
    const endingAssignment = module.value === 'coachAssignments'
    const cancellingSession = module.value === 'sessions'
    await ElMessageBox.confirm(
      endingAssignment ? 'End this coach assignment?' : cancellingSession ? 'Cancel this class session?' : 'Delete this record?',
      'Please confirm'
    )
    await api.delete(`/admin/${endingAssignment ? 'coach-assignments' : cancellingSession ? 'course-sessions' : module.value}/${item.id}`)
    ElMessage.success(endingAssignment ? 'Assignment ended' : cancellingSession ? 'Session cancelled' : 'Deleted')
    await load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error))
  }
}

async function setActive(kind: 'users' | 'courses', item: Row) {
  try {
    await api.patch(`/admin/${kind}/${item.id}/active`, { active: !item.active })
    await load()
  } catch (error) { ElMessage.error(messageOf(error)) }
}

function label(key: string) {
  return key.replace(/([A-Z])/g, ' $1').replace(/^./, letter => letter.toUpperCase())
}
function show(value: unknown) {
  if (typeof value === 'string' && value.includes('T')) {
    const date = new Date(value)
    if (!Number.isNaN(date.getTime())) return date.toLocaleString('en-CA')
  }
  return String(value ?? '—')
}

function flattenSpaces(floors: Row[]) {
  return floors.flatMap(floor => floor.spaces.map((space: Row) => ({
    id: space.id,
    label: `${floor.name} · ${space.name}`,
  })))
}

function localDate(value: Date) {
  const local = new Date(value.getTime() - value.getTimezoneOffset() * 60_000)
  return local.toISOString().slice(0, 10)
}

function localDateTime(value: Date) {
  const local = new Date(value.getTime() - value.getTimezoneOffset() * 60_000)
  return local.toISOString().slice(0, 16)
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

function setOperationHours(value: Row) {
  operationHours.opensAt = timeText(value.opensAt)
  operationHours.closesAt = timeText(value.closesAt)
}

function timeText(value: unknown) {
  return String(value ?? '').slice(0, 5)
}
</script>

<template>
  <div class="admin-page">
    <div class="admin-title"><div><p>MANAGEMENT</p><h1>{{ names[module] }}</h1></div><button type="button" @click="load">Refresh</button></div>
    <div v-if="loading" class="admin-panel empty">Loading…</div>

    <template v-else-if="['overview', 'closedDays'].includes(module)">
      <div v-if="module === 'overview'" class="metric-grid dashboard-metrics">
        <article v-for="[key, title] in [['currentOccupancy','In gym now'],['memberCount','Members'],['coachCount','Coaches'],['courseCount','Courses'],['bookingCount','Bookings'],['equipmentCount','Equipment'],['postCount','Posts']]" :key="key">
          <span>{{ title }}</span><strong>{{ overview[key] ?? 0 }}</strong>
        </article>
      </div>
      <section class="admin-panel ops-calendar dashboard-calendar">
          <div class="panel-title"><div><h2>Operations calendar</h2><p>Manage default hours, full-day closures, and partial closures.</p></div><RouterLink v-if="module === 'overview'" to="/calendar">Manage all</RouterLink></div>
          <div class="calendar-workspace dashboard-calendar-workspace">
            <div class="calendar-main">
              <div class="calendar-toolbar">
                <div><strong>{{ adminCalendarMonth }}</strong><span>{{ visibleClosedCount }} affected</span></div>
                <div class="calendar-nav">
                  <button type="button" aria-label="Previous month" @click="moveAdminCalendar(-1)">Prev</button>
                  <button type="button" @click="resetAdminCalendar">Today</button>
                  <button type="button" aria-label="Next month" @click="moveAdminCalendar(1)">Next</button>
                </div>
              </div>
              <div class="calendar-weekdays"><span v-for="day in weekdayLabels" :key="day">{{ day }}</span></div>
              <div class="admin-calendar">
                <button v-for="day in adminCalendarDays" :key="day.value" type="button" :title="day.closed ? 'Closed all day' : day.partial ? 'Partial closure' : `Open ${operationHours.opensAt}–${operationHours.closesAt}`" :class="{ closed: day.closed, partial: day.partial, selected: day.selected, muted: day.muted, past: day.past }" @click="selectCalendarDay(day)">
                  <strong>{{ day.day }}</strong><span class="day-status">{{ day.closed ? 'Closed' : day.partial ? 'Limited' : 'Open' }}</span>
                </button>
              </div>
            </div>
            <aside class="calendar-editor">
              <form class="hours-form" @submit.prevent="saveOperationHours">
                <small>DEFAULT HOURS</small>
                <div class="time-range"><input v-model="operationHours.opensAt" type="time" aria-label="Default opening time" required><span>to</span><input v-model="operationHours.closesAt" type="time" aria-label="Default closing time" required></div>
                <button type="submit" :disabled="calendarSaving">Save hours</button>
              </form>
              <form class="closure-form" @submit.prevent="saveDashboardClosedDay">
                <small>SELECTED DAY</small>
                <strong>{{ closedDay.closedOn }}</strong>
                <span class="selected-status" :class="{ closed: selectedClosures.some(item => !item.startsAt), partial: selectedClosures.some(item => item.startsAt) }">{{ selectedClosures.some(item => !item.startsAt) ? 'Closed' : selectedClosures.length ? 'Limited hours' : `Open ${operationHours.opensAt}–${operationHours.closesAt}` }}</span>
                <p v-if="selectedPastDay" class="calendar-note">Past date is read-only.</p>
                <ul v-if="selectedClosures.length" class="closure-list">
                  <li v-for="item in selectedClosures" :key="item.id"><span>{{ closureLabel(item) }}</span><button class="ghost-action" type="button" :disabled="selectedPastDay || calendarSaving" aria-label="Remove closure" @click="reopenSelectedDay(item)">×</button></li>
                </ul>
                <select v-model="closedDay.type" aria-label="Closure type" :disabled="selectedPastDay">
                  <option value="FULL">Close all day</option>
                  <option value="PARTIAL">Close part of day</option>
                </select>
                <div v-if="closedDay.type === 'PARTIAL'" class="time-range"><input v-model="closedDay.startsAt" type="time" aria-label="Closure start time" :disabled="selectedPastDay" required><span>to</span><input v-model="closedDay.endsAt" type="time" aria-label="Closure end time" :disabled="selectedPastDay" required></div>
                <input v-model.trim="closedDay.reason" maxlength="160" placeholder="Reason for closure" :disabled="selectedPastDay" required>
                <button type="submit" :disabled="selectedPastDay || calendarSaving">{{ calendarSaving ? 'Saving…' : 'Add closure' }}</button>
              </form>
            </aside>
          </div>
      </section>
    </template>

    <form v-else-if="module === 'notices'" class="admin-panel admin-form" @submit.prevent="create">
      <input v-model.trim="form.title" maxlength="120" placeholder="Notice title" required><input v-model.trim="form.content" maxlength="1000" placeholder="Notice content" required><button type="submit">Publish</button>
    </form>
    <form v-else-if="module === 'courses'" class="admin-panel admin-form course-form" @submit.prevent="create">
      <input v-model.trim="form.name" maxlength="120" placeholder="Course name" required><input v-model.trim="form.description" maxlength="1000" placeholder="Description" required><input v-model.number="form.durationMinutes" type="number" min="10" max="240" aria-label="Duration in minutes" required><input v-model.number="form.defaultCapacity" type="number" min="1" max="200" aria-label="Capacity" required><button type="submit">Add course</button>
    </form>
    <form v-else-if="module === 'coachAssignments'" class="admin-panel admin-form assignment-form" @submit.prevent="create">
      <select v-model.number="assignment.coachId" aria-label="Coach" required><option value="" disabled>Select coach</option><option v-for="coach in assignmentOptions.coaches" :key="coach.id" :value="coach.id">{{ coach.displayName }}</option></select>
      <select v-model.number="assignment.memberId" aria-label="Member" required><option value="" disabled>Select member</option><option v-for="member in assignmentOptions.members" :key="member.id" :value="member.id">{{ member.displayName }}</option></select>
      <input v-model="assignment.startsOn" type="date" aria-label="Start date" required>
      <input v-model="assignment.endsOn" type="date" :min="assignment.startsOn" aria-label="Optional end date">
      <button type="submit">Assign coach</button>
    </form>
    <form v-else-if="module === 'sessions'" class="admin-panel admin-form session-form" @submit.prevent="create">
      <select v-model.number="sessionForm.courseId" aria-label="Course" required><option value="" disabled>Select course</option><option v-for="course in sessionOptions.courses" :key="course.id" :value="course.id">{{ course.name }}</option></select>
      <select v-model.number="sessionForm.coachId" aria-label="Optional coach"><option value="">No coach assigned</option><option v-for="coach in sessionOptions.coaches" :key="coach.id" :value="coach.id">{{ coach.displayName }}</option></select>
      <input v-model="sessionForm.startsAt" type="datetime-local" aria-label="Starts at" required>
      <input v-model="sessionForm.endsAt" type="datetime-local" :min="sessionForm.startsAt" aria-label="Ends at" required>
      <input v-model.number="sessionForm.capacity" type="number" min="1" max="200" aria-label="Capacity" required>
      <select v-model.number="sessionForm.spaceId" aria-label="Class location"><option value="">No location</option><option v-for="space in spaceOptions" :key="space.id" :value="space.id">{{ space.label }}</option></select>
      <div class="session-resource-quantities" aria-label="Required equipment">
        <label v-for="resource in sessionOptions.resources" :key="resource.id">
          <span>{{ resource.name }}</span>
          <input v-model.number="sessionResourceUnits[resource.id]" type="number" min="0" :max="resource.totalUnits" :aria-label="`${resource.name} required units`">
        </label>
      </div>
      <button type="submit">Schedule class</button>
    </form>

    <section v-if="!['overview', 'closedDays'].includes(module) && !loading" class="admin-panel table-wrap">
      <table v-if="rows.length">
        <thead><tr><th v-for="key in columns" :key="key">{{ label(key) }}</th><th v-if="['notices','posts','courses','members','coaches','coachAssignments','closedDays','sessions'].includes(module)">Actions</th></tr></thead>
        <tbody><tr v-for="item in rows" :key="item.id || item.username"><td v-for="key in columns" :key="key">{{ show(item[key]) }}</td><td v-if="['notices','posts','courses','members','coaches','coachAssignments','closedDays','sessions'].includes(module)" class="table-actions"><button v-if="module === 'courses'" type="button" @click="setActive('courses', item)">{{ item.active ? 'Disable' : 'Enable' }}</button><button v-else-if="['members','coaches'].includes(module)" type="button" @click="setActive('users', item)">{{ item.active ? 'Disable' : 'Enable' }}</button><template v-else-if="module === 'sessions'"><button v-if="item.status === 'OPEN'" type="button" @click="remove(item)">Cancel</button></template><button v-else-if="module !== 'coachAssignments' || item.status === 'ACTIVE'" type="button" @click="remove(item)">{{ module === 'coachAssignments' ? 'End' : 'Delete' }}</button></td></tr></tbody>
      </table>
      <p v-else class="empty">No records found.</p>
    </section>
  </div>
</template>
