<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { api, messageOf } from '../api'
import {
  filterEquipment,
  type AvailabilityStatus,
  type EquipmentAvailability as Equipment,
} from '../equipmentAvailability'

type AvailabilityResponse = { generatedAt: string; items: Equipment[] }

const data = ref<AvailabilityResponse | null>(null)
const loading = ref(true)
const refreshing = ref(false)
const error = ref('')
const search = ref('')
const category = ref('All')
const status = ref<'ALL' | AvailabilityStatus>('ALL')
let poller: number | undefined

const categories = computed(() => ['All', ...new Set((data.value?.items ?? []).map(item => item.category))])
const filtered = computed(() =>
  filterEquipment(data.value?.items ?? [], search.value, category.value, status.value),
)
const totals = computed(() => filtered.value.reduce((result, item) => ({
  available: result.available + item.availableUnits,
  total: result.total + item.totalUnits,
}), { available: 0, total: 0 }))

onMounted(async () => {
  await load()
  poller = window.setInterval(() => {
    if (document.visibilityState === 'visible') void load(true)
  }, 30_000)
  document.addEventListener('visibilitychange', refreshWhenVisible)
})
onBeforeUnmount(() => {
  if (poller) window.clearInterval(poller)
  document.removeEventListener('visibilitychange', refreshWhenVisible)
})

async function load(quiet = false) {
  quiet ? refreshing.value = true : loading.value = true
  error.value = ''
  try {
    data.value = (await api.get<AvailabilityResponse>('/equipment')).data
  } catch (cause) {
    error.value = messageOf(cause)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function refreshWhenVisible() {
  if (document.visibilityState === 'visible') void load(true)
}

function updated(value: string) {
  return new Intl.DateTimeFormat('en-CA', { hour: 'numeric', minute: '2-digit' }).format(new Date(value))
}
</script>

<template>
  <section class="page equipment-availability">
    <header class="availability-head">
      <div>
        <p class="eyebrow">Walk-in availability</p>
        <h1>Plan your workout.</h1>
        <p>Approximate availability maintained by gym staff. No reservation is required.</p>
      </div>
      <div class="availability-summary">
        <strong>{{ totals.available }} <small>/ {{ totals.total }}</small></strong>
        <span>available now</span>
        <small v-if="data">Updated {{ updated(data.generatedAt) }}</small>
        <button class="button ghost" type="button" :disabled="refreshing" @click="load(true)">
          {{ refreshing ? 'Refreshing…' : 'Refresh' }}
        </button>
      </div>
    </header>

    <div class="availability-toolbar card">
      <label class="availability-search">
        <span class="sr-only">Search equipment</span>
        <input v-model.trim="search" type="search" placeholder="Search equipment or location">
      </label>
      <div class="filter-chips" aria-label="Availability status">
        <button
          v-for="option in ['ALL', 'AVAILABLE', 'LIMITED', 'UNAVAILABLE'] as const"
          :key="option" type="button" :class="{ active: status === option }"
          @click="status = option"
        >{{ option === 'ALL' ? 'All status' : option.toLowerCase() }}</button>
      </div>
      <select v-model="category" aria-label="Equipment category">
        <option v-for="item in categories" :key="item">{{ item }}</option>
      </select>
    </div>

    <div v-if="loading" class="card empty">Loading availability…</div>
    <div v-else-if="error" class="card empty">
      <h3>Availability is temporarily unavailable</h3>
      <p>{{ error }}</p>
      <button class="button dark" type="button" @click="load()">Try again</button>
    </div>
    <div v-else-if="filtered.length" class="availability-grid">
      <article v-for="item in filtered" :key="item.id" class="availability-card card">
        <header>
          <span class="category-mark">{{ item.category.slice(0, 2).toUpperCase() }}</span>
          <span class="availability-pill" :class="item.availabilityStatus.toLowerCase()">
            {{ item.availabilityStatus.toLowerCase() }}
          </span>
        </header>
        <div>
          <small>{{ item.category }}</small>
          <h2>{{ item.name }}</h2>
          <p>{{ item.description }}</p>
        </div>
        <div class="availability-count">
          <strong>{{ item.availableUnits }}</strong>
          <span>of {{ item.totalUnits }} {{ item.unitLabel }} available</span>
        </div>
        <div
          class="availability-meter"
          role="progressbar"
          :aria-label="`${item.name} availability`"
          :aria-valuenow="item.availableUnits"
          :aria-valuemax="item.totalUnits"
        ><i :style="{ width: `${item.totalUnits ? item.availableUnits / item.totalUnits * 100 : 0}%` }"></i></div>
        <footer>
          <span>{{ item.locations.join(' · ') || 'Location not assigned' }}</span>
          <RouterLink v-if="item.locations.length" :to="`/front/gym-map`">Map</RouterLink>
        </footer>
      </article>
    </div>
    <div v-else class="card empty">
      <h3>No matching resources</h3>
      <p>Try another category, status, or search term.</p>
    </div>
  </section>
</template>

<style scoped>
.equipment-availability { max-width: 1240px; }
.availability-head { margin-bottom: 24px; display: flex; align-items: end; justify-content: space-between; gap: 24px; }
.availability-head h1 { margin: 4px 0 8px; font-size: clamp(30px, 4vw, 48px); }
.availability-head > div > p:last-child { max-width: 620px; color: var(--muted); }
.availability-summary { min-width: 210px; padding: 20px; display: grid; grid-template-columns: 1fr auto; align-items: center; background: #eff6e5; border-radius: 18px; }
.availability-summary > strong { font-size: 28px; }
.availability-summary > strong small { color: var(--muted); font-size: 16px; }
.availability-summary > span { color: #49654e; font-size: 12px; font-weight: 800; }
.availability-summary > small { margin-top: 5px; color: var(--muted); }
.availability-summary button { grid-row: 1 / 3; grid-column: 2; }
.availability-toolbar { margin-bottom: 22px; padding: 14px; display: grid; grid-template-columns: minmax(220px, 1fr) auto 190px; gap: 12px; }
.availability-toolbar input, .availability-toolbar select { width: 100%; padding: 11px 13px; border: 1px solid var(--line); border-radius: 10px; background: white; }
.filter-chips { display: flex; gap: 7px; }
.filter-chips button { padding: 9px 11px; border: 0; border-radius: 999px; color: var(--muted); background: #f3f5f6; text-transform: capitalize; }
.filter-chips button.active { color: white; background: #344b63; }
.availability-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 18px; }
.availability-card { padding: 22px; display: grid; gap: 18px; }
.availability-card header, .availability-card footer { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.category-mark { width: 40px; height: 40px; display: grid; place-items: center; color: #49654e; background: #eff6e5; border-radius: 12px; font-weight: 900; }
.availability-pill { padding: 6px 9px; border-radius: 999px; font-size: 10px; font-weight: 900; text-transform: uppercase; }
.availability-pill.available { color: #2e7148; background: #e9f7ed; }
.availability-pill.limited { color: #8a6721; background: #fff3d5; }
.availability-pill.unavailable { color: #8c4545; background: #fae9e9; }
.availability-card h2 { margin: 5px 0 7px; font-size: 20px; }
.availability-card p { min-height: 42px; color: var(--muted); font-size: 13px; line-height: 1.55; }
.availability-card small { color: #d48166; font-weight: 800; }
.availability-count { display: flex; align-items: baseline; gap: 9px; }
.availability-count strong { font-size: 32px; }
.availability-count span, .availability-card footer { color: var(--muted); font-size: 12px; }
.availability-meter { height: 8px; overflow: hidden; background: #edf0f2; border-radius: 999px; }
.availability-meter i { height: 100%; display: block; background: #86a95d; border-radius: inherit; transition: width .25s ease; }
.availability-card footer a { color: #49745a; font-weight: 800; }
.sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0, 0, 0, 0); }
@media (max-width: 980px) {
  .availability-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .availability-toolbar { grid-template-columns: 1fr; }
  .filter-chips { overflow-x: auto; }
}
@media (max-width: 650px) {
  .availability-head { align-items: stretch; flex-direction: column; }
  .availability-grid { grid-template-columns: 1fr; }
}
</style>
