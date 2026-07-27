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
const loading = ref(false)
const form = reactive({ title: '', content: '', name: '', category: '', description: '', coverKey: 'course', durationMinutes: 45, defaultCapacity: 12 })
const assignment = reactive({ coachId: '' as number | '', memberId: '' as number | '', startsOn: new Date().toLocaleDateString('en-CA'), endsOn: '' })
const assignmentOptions = reactive<{ coaches: Row[]; members: Row[] }>({ coaches: [], members: [] })
const names: Record<string, string> = {
  overview: 'Dashboard', admins: 'Administrators', members: 'Members', coaches: 'Coaches',
  notices: 'Notices', courses: 'Courses', bookings: 'Course Bookings', appointments: 'Coach Bookings',
  coachAssignments: 'Coach Assignments', visits: 'Gym Visits', equipment: 'Equipment',
  equipmentReservations: 'Equipment Bookings', posts: 'Community Posts', profile: 'My Account',
}
const columns = computed(() => Object.keys(rows.value[0] || {}).filter(key =>
  module.value !== 'coachAssignments' || !['coachId', 'memberId'].includes(key)
))

watch(module, load, { immediate: true })

async function load() {
  loading.value = true
  rows.value = []
  try {
    if (module.value === 'overview') overview.value = (await api.get('/admin/overview')).data
    else if (module.value === 'coachAssignments') {
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
        appointments: '/admin/coach-appointments', equipment: '/equipment',
        equipmentReservations: '/admin/equipment-reservations',
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
    if (module.value === 'equipment') await api.post('/admin/equipment', form)
    if (module.value === 'courses') await api.post('/admin/courses', form)
    if (module.value === 'coachAssignments') {
      await api.post('/admin/coach-assignments', { ...assignment, endsOn: assignment.endsOn || null })
      assignment.endsOn = ''
    }
    Object.assign(form, { title: '', content: '', name: '', category: '', description: '', coverKey: 'course', durationMinutes: 45, defaultCapacity: 12 })
    ElMessage.success('Saved')
    await load()
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function remove(item: Row) {
  try {
    const endingAssignment = module.value === 'coachAssignments'
    await ElMessageBox.confirm(endingAssignment ? 'End this coach assignment?' : 'Delete this record?', 'Please confirm')
    await api.delete(`/admin/${endingAssignment ? 'coach-assignments' : module.value}/${item.id}`)
    ElMessage.success(endingAssignment ? 'Assignment ended' : 'Deleted')
    await load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error))
  }
}

async function equipmentStatus(item: Row, status: string) {
  try {
    await api.patch(`/admin/equipment/${item.id}/status`, { status })
    await load()
  } catch (error) { ElMessage.error(messageOf(error)) }
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
</script>

<template>
  <div class="admin-page">
    <div class="admin-title"><div><p>MANAGEMENT</p><h1>{{ names[module] }}</h1></div><button type="button" @click="load">Refresh</button></div>
    <div v-if="loading" class="admin-panel empty">Loading…</div>

    <template v-else-if="module === 'overview'">
      <div class="metric-grid">
        <article v-for="[key, title] in [['currentOccupancy','In gym now'],['memberCount','Members'],['coachCount','Coaches'],['courseCount','Courses'],['bookingCount','Bookings'],['equipmentCount','Equipment'],['postCount','Posts']]" :key="key">
          <span>{{ title }}</span><strong>{{ overview[key] ?? 0 }}</strong>
        </article>
      </div>
      <div class="dashboard-grid">
        <section class="admin-panel"><h2>Bookings by course</h2><div v-for="item in overview.bookingByCourse" :key="item.name" class="bar-row"><span>{{ item.name }}</span><i :style="{ width: `${Math.max(4, Number(item.total) * 12)}%` }"></i><strong>{{ item.total }}</strong></div></section>
        <section class="admin-panel"><h2>Equipment status</h2><div v-for="item in overview.equipmentByStatus" :key="item.status" class="status-row"><span>{{ item.status }}</span><strong>{{ item.total }}</strong></div></section>
      </div>
    </template>

    <form v-else-if="module === 'notices'" class="admin-panel admin-form" @submit.prevent="create">
      <input v-model.trim="form.title" maxlength="120" placeholder="Notice title" required><input v-model.trim="form.content" maxlength="1000" placeholder="Notice content" required><button type="submit">Publish</button>
    </form>
    <form v-else-if="module === 'equipment'" class="admin-panel admin-form equipment-form" @submit.prevent="create">
      <input v-model.trim="form.name" maxlength="120" placeholder="Equipment name" required><input v-model.trim="form.category" maxlength="80" placeholder="Category" required><input v-model.trim="form.description" maxlength="1000" placeholder="Description" required><button type="submit">Add equipment</button>
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

    <section v-if="module !== 'overview' && !loading" class="admin-panel table-wrap">
      <table v-if="rows.length">
        <thead><tr><th v-for="key in columns" :key="key">{{ label(key) }}</th><th v-if="['notices','equipment','posts','courses','members','coaches','coachAssignments'].includes(module)">Actions</th></tr></thead>
        <tbody><tr v-for="item in rows" :key="item.id || item.username"><td v-for="key in columns" :key="key">{{ show(item[key]) }}</td><td v-if="['notices','equipment','posts','courses','members','coaches','coachAssignments'].includes(module)" class="table-actions"><template v-if="module === 'equipment'"><button type="button" @click="equipmentStatus(item, 'AVAILABLE')">Available</button><button type="button" @click="equipmentStatus(item, 'MAINTENANCE')">Maintenance</button></template><button v-else-if="module === 'courses'" type="button" @click="setActive('courses', item)">{{ item.active ? 'Disable' : 'Enable' }}</button><button v-else-if="['members','coaches'].includes(module)" type="button" @click="setActive('users', item)">{{ item.active ? 'Disable' : 'Enable' }}</button><button v-else-if="module !== 'coachAssignments' || item.status === 'ACTIVE'" type="button" @click="remove(item)">{{ module === 'coachAssignments' ? 'End' : 'Delete' }}</button></td></tr></tbody>
      </table>
      <p v-else class="empty">No records found.</p>
    </section>
  </div>
</template>
