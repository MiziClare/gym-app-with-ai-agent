<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, messageOf } from '../api'
import { session } from '../state'

type Equipment = {
  id: number
  name: string
  category: string
  totalUnits: number
  availableUnits: number
  availabilityStatus: 'AVAILABLE' | 'LIMITED' | 'UNAVAILABLE'
}
type Activity = {
  id: number
  courseName: string
  coachName: string
  startsAt: string
  endsAt: string
  capacity: number
  bookedCount: number
}
type Space = {
  id: number
  name: string
  type: 'ROOM' | 'AREA'
  x: number
  y: number
  width: number
  height: number
  status: 'CLOSED' | 'IN_USE' | 'LIMITED_EQUIPMENT' | 'AVAILABLE'
  currentActivity: Activity | null
  equipment: Equipment[]
  timeline: Activity[]
}
type Floor = { id: number; name: string; sortOrder: number; spaces: Space[] }
type GymMap = {
  generatedAt: string
  currentGymOccupancy: number
  closedToday: boolean
  floors: Floor[]
}

const route = useRoute()
const router = useRouter()
const data = ref<GymMap | null>(null)
const activeFloorId = ref<number | null>(null)
const selectedSpaceId = ref<number | null>(null)
const loading = ref(true)
const refreshing = ref(false)
const error = ref('')
let poller: number | undefined

const activeFloor = computed(() =>
  data.value?.floors.find(floor => floor.id === activeFloorId.value) ?? data.value?.floors[0],
)
const selected = computed(() =>
  data.value?.floors.flatMap(floor => floor.spaces).find(space => space.id === selectedSpaceId.value) ?? null,
)
const orderedSpaces = computed(() =>
  [...(activeFloor.value?.spaces ?? [])].sort((a, b) => Number(b.type === 'AREA') - Number(a.type === 'AREA')),
)
const statusLabels = {
  CLOSED: 'Closed',
  IN_USE: 'In use',
  LIMITED_EQUIPMENT: 'Limited equipment',
  AVAILABLE: 'Available',
}

onMounted(async () => {
  await load()
  poller = window.setInterval(() => {
    if (document.visibilityState === 'visible') void load(true)
  }, 60_000)
  document.addEventListener('visibilitychange', refreshWhenVisible)
})
onBeforeUnmount(() => {
  if (poller) window.clearInterval(poller)
  document.removeEventListener('visibilitychange', refreshWhenVisible)
})
watch(() => route.query.spaceId, selectFromQuery)

async function load(quiet = false) {
  if (quiet) refreshing.value = true
  else loading.value = true
  error.value = ''
  const from = new Date()
  from.setHours(0, 0, 0, 0)
  const to = new Date(from.getTime() + 8 * 24 * 60 * 60 * 1000)
  try {
    data.value = (await api.get<GymMap>('/gym-map', {
      params: { from: from.toISOString(), to: to.toISOString() },
    })).data
    selectFromQuery()
  } catch (cause) {
    error.value = messageOf(cause)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function selectFromQuery() {
  if (!data.value?.floors.length) return
  const requested = Number(route.query.spaceId)
  const floor = data.value.floors.find(item => item.spaces.some(space => space.id === requested))
  if (floor) {
    activeFloorId.value = floor.id
    selectedSpaceId.value = requested
    return
  }
  if (!activeFloorId.value) activeFloorId.value = data.value.floors[0].id
  if (!selectedSpaceId.value) selectedSpaceId.value = activeFloor.value?.spaces[0]?.id ?? null
}

function selectFloor(id: number) {
  activeFloorId.value = id
  const first = activeFloor.value?.spaces[0]
  if (first) selectSpace(first.id)
}

function selectSpace(id: number) {
  selectedSpaceId.value = id
  void router.replace({ query: { ...route.query, spaceId: String(id) } })
}

function refreshWhenVisible() {
  if (document.visibilityState === 'visible') void load(true)
}

function dateTime(value: string) {
  return new Intl.DateTimeFormat('en-CA', {
    weekday: 'short', month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit',
  }).format(new Date(value))
}

function time(value: string) {
  return new Intl.DateTimeFormat('en-CA', { hour: 'numeric', minute: '2-digit' }).format(new Date(value))
}

function activityState(item: Activity) {
  const now = Date.now()
  if (new Date(item.endsAt).getTime() <= now) return 'Ended'
  if (new Date(item.startsAt).getTime() <= now) return 'Now'
  return 'Upcoming'
}
</script>

<template>
  <section class="page gym-map-page">
    <header class="page-head gym-map-head">
      <div>
        <p class="eyebrow">Live facility view</p>
        <h1>Find your space.</h1>
      </div>
      <div class="gym-map-summary">
        <div><strong>{{ data?.currentGymOccupancy ?? '—' }}</strong><span>checked in now</span></div>
        <div><strong>{{ data ? time(data.generatedAt) : '—' }}</strong><span>last updated</span></div>
        <button class="button ghost" type="button" :disabled="refreshing" @click="load(true)">
          {{ refreshing ? 'Refreshing…' : 'Refresh' }}
        </button>
      </div>
    </header>

    <div v-if="loading" class="card empty">Loading gym map…</div>
    <div v-else-if="error" class="card empty">
      <h3>Map unavailable</h3><p>{{ error }}</p><button class="button dark" type="button" @click="load()">Try again</button>
    </div>
    <template v-else-if="data">
      <nav class="floor-tabs gym-map-floors card" aria-label="Gym floors">
        <button
          v-for="floor in data.floors" :key="floor.id" type="button"
          :class="{ active: floor.id === activeFloor?.id }"
          @click="selectFloor(floor.id)"
        >{{ floor.name }}</button>
      </nav>

      <div class="gym-map-workspace">
        <section class="card gym-map-canvas-card">
          <div class="gym-map-legend" aria-label="Space status legend">
            <span v-for="(label, status) in statusLabels" :key="status" :class="`status-${status.toLowerCase()}`">
              <i></i>{{ label }}
            </span>
          </div>
          <div class="layout-canvas-scroll">
            <svg class="layout-canvas gym-map-canvas" viewBox="0 0 100 100" preserveAspectRatio="none" aria-label="Gym floor map">
              <defs>
                <pattern id="gym-map-grid" width="5" height="5" patternUnits="userSpaceOnUse">
                  <path d="M 5 0 L 0 0 0 5" fill="none" stroke="#dbe4ea" stroke-width=".15"/>
                </pattern>
              </defs>
              <rect width="100" height="100" fill="url(#gym-map-grid)"/>
              <g
                v-for="space in orderedSpaces" :key="space.id"
                class="layout-space gym-map-space"
                :class="[space.type.toLowerCase(), `status-${space.status.toLowerCase()}`, { selected: selectedSpaceId === space.id }]"
                tabindex="0" role="button" :aria-label="`${space.name}, ${statusLabels[space.status]}`"
                @click="selectSpace(space.id)" @keydown.enter.prevent="selectSpace(space.id)"
                @keydown.space.prevent="selectSpace(space.id)"
              >
                <rect :x="space.x" :y="space.y" :width="space.width" :height="space.height" rx="1"/>
                <text :x="space.x + 1.5" :y="space.y + 4">{{ space.name }}</text>
                <text class="space-kind" :x="space.x + 1.5" :y="space.y + 7">{{ statusLabels[space.status] }}</text>
              </g>
            </svg>
          </div>
          <p class="gym-map-note">Status reflects scheduled classes and equipment maintenance. Walk-in crowding by space is not tracked.</p>
        </section>

        <aside class="card gym-map-detail" aria-live="polite">
          <template v-if="selected">
            <div class="gym-map-detail-head">
              <div><small>{{ selected.type }}</small><h2>{{ selected.name }}</h2><p>{{ activeFloor?.name }}</p></div>
              <span class="gym-map-status" :class="`status-${selected.status.toLowerCase()}`">{{ statusLabels[selected.status] }}</span>
            </div>

            <section v-if="selected.currentActivity" class="current-activity">
              <small>HAPPENING NOW</small>
              <h3>{{ selected.currentActivity.courseName }}</h3>
              <p>Coach {{ selected.currentActivity.coachName }} · ends {{ time(selected.currentActivity.endsAt) }}</p>
              <span>{{ selected.currentActivity.bookedCount }} / {{ selected.currentActivity.capacity }} booked</span>
            </section>
            <p v-else class="detail-empty">No class is using this space now.</p>

            <section class="gym-map-section">
              <h3>Today + next 7 days</h3>
              <ol v-if="selected.timeline.length" class="space-timeline">
                <li v-for="item in selected.timeline" :key="item.id">
                  <div><strong>{{ item.courseName }}</strong><span>{{ dateTime(item.startsAt) }}–{{ time(item.endsAt) }}</span><small>Coach {{ item.coachName }} · {{ item.bookedCount }}/{{ item.capacity }}</small></div>
                  <div><em>{{ activityState(item) }}</em><RouterLink :to="`/front/course?sessionId=${item.id}`">View class</RouterLink></div>
                </li>
              </ol>
              <p v-else class="detail-empty">No classes scheduled in this range.</p>
            </section>

            <section class="gym-map-section">
              <div class="section-row"><h3>Equipment</h3><RouterLink v-if="session.user?.role === 'MEMBER'" to="/front/equipment">View all</RouterLink></div>
              <ul v-if="selected.equipment.length" class="space-equipment">
                <li v-for="item in selected.equipment" :key="item.id">
                  <span><strong>{{ item.name }}</strong><small>{{ item.category }}</small></span>
                  <em :class="{ maintenance: item.availabilityStatus !== 'AVAILABLE' }">{{ item.availableUnits }} / {{ item.totalUnits }} available</em>
                </li>
              </ul>
              <p v-else class="detail-empty">No equipment assigned here.</p>
            </section>
          </template>
          <p v-else class="detail-empty">Select a room or area to see its schedule.</p>
        </aside>
      </div>
    </template>
  </section>
</template>
