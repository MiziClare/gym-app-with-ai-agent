<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api, messageOf } from '../api'
import { moveBox, normalizeBox, resizeBox, type LayoutBox } from '../layoutGeometry'

type SpaceType = 'ROOM' | 'AREA'
type Space = LayoutBox & {
  id: number | null
  clientKey: string
  name: string
  type: SpaceType
  equipmentCount: number
  sessionCount: number
}
type Floor = { id: number | null; name: string; sortOrder: number; spaces: Space[] }
type DragState = {
  key: string
  mode: 'move' | 'resize'
  startX: number
  startY: number
  origin: LayoutBox
} | null

const floors = ref<Floor[]>([])
const activeIndex = ref(0)
const selectedKey = ref('')
const loading = ref(true)
const saving = ref(false)
const savedPayload = ref('')
const history = ref<string[]>([])
const svg = ref<SVGSVGElement>()
let dragState: DragState = null

const activeFloor = computed(() => floors.value[activeIndex.value])
const selected = computed(() => activeFloor.value?.spaces.find(space => space.clientKey === selectedKey.value))
const orderedSpaces = computed(() => [...(activeFloor.value?.spaces ?? [])]
  .sort((left, right) => Number(left.type === 'ROOM') - Number(right.type === 'ROOM')))
const dirty = computed(() => payload() !== savedPayload.value)

onMounted(load)
onMounted(() => window.addEventListener('beforeunload', warnUnsaved))
onBeforeUnmount(() => window.removeEventListener('beforeunload', warnUnsaved))

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/gym-layout')
    floors.value = data.floors.map((floor: Floor) => ({
      ...floor,
      spaces: floor.spaces.map(space => ({ ...space, clientKey: keyFor(space.id) })),
    }))
    activeIndex.value = 0
    selectedKey.value = ''
    history.value = []
    savedPayload.value = payload()
  } catch (error) {
    ElMessage.error(messageOf(error))
  } finally {
    loading.value = false
  }
}

function payload() {
  return JSON.stringify({
    floors: floors.value.map(floor => ({
      id: floor.id,
      name: floor.name.trim(),
      spaces: floor.spaces.map(({ id, name, type, x, y, width, height }) =>
        ({ id, name: name.trim(), type, x, y, width, height })),
    })),
  })
}

function checkpoint() {
  const snapshot = JSON.stringify(floors.value)
  if (history.value.at(-1) !== snapshot) history.value.push(snapshot)
  if (history.value.length > 40) history.value.shift()
}

function undo() {
  const snapshot = history.value.pop()
  if (!snapshot) return
  floors.value = JSON.parse(snapshot)
  activeIndex.value = Math.min(activeIndex.value, floors.value.length - 1)
  selectedKey.value = ''
}

function selectFloor(index: number) {
  activeIndex.value = index
  selectedKey.value = ''
}

function addFloor() {
  if (floors.value.length >= 20) return ElMessage.warning('A layout can contain at most 20 floors')
  checkpoint()
  const number = floors.value.length + 1
  floors.value.push({ id: null, name: `Floor ${number}`, sortOrder: floors.value.length, spaces: [] })
  selectFloor(floors.value.length - 1)
}

async function removeFloor() {
  if (floors.value.length === 1) return ElMessage.warning('A layout must keep at least one floor')
  const floor = activeFloor.value
  const links = floorLinks(floor)
  if (links) return ElMessage.warning('Move linked equipment and classes before deleting this floor')
  try {
    await ElMessageBox.confirm(`Delete ${floor.name}?`, 'Delete floor')
    checkpoint()
    floors.value.splice(activeIndex.value, 1)
    activeIndex.value = Math.max(0, activeIndex.value - 1)
    selectedKey.value = ''
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error))
  }
}

function moveFloor(offset: number) {
  const target = activeIndex.value + offset
  if (target < 0 || target >= floors.value.length) return
  checkpoint()
  const [floor] = floors.value.splice(activeIndex.value, 1)
  floors.value.splice(target, 0, floor)
  activeIndex.value = target
}

function addSpace(type: SpaceType) {
  const floor = activeFloor.value
  if (floor.spaces.length >= 100) return ElMessage.warning('A floor can contain at most 100 spaces')
  checkpoint()
  const name = uniqueName(type === 'ROOM' ? 'Room' : 'Area')
  const offset = (floor.spaces.length * 4) % 40
  const space: Space = {
    id: null, clientKey: keyFor(null), name, type,
    x: 8 + offset, y: 8 + offset, width: 24, height: 16,
    equipmentCount: 0, sessionCount: 0,
  }
  floor.spaces.push(space)
  selectedKey.value = space.clientKey
}

async function removeSpace() {
  const space = selected.value
  if (!space) return
  const links = space.equipmentCount + space.sessionCount
  if (links) return ElMessage.warning('Move linked equipment and classes before deleting this space')
  try {
    await ElMessageBox.confirm(`Delete ${space.name}?`, 'Delete space')
    checkpoint()
    activeFloor.value.spaces = activeFloor.value.spaces.filter(item => item.clientKey !== space.clientKey)
    selectedKey.value = ''
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error))
  }
}

function startDrag(event: PointerEvent, space: Space, mode: 'move' | 'resize') {
  if (event.button !== 0) return
  event.preventDefault()
  checkpoint()
  selectedKey.value = space.clientKey
  dragState = {
    key: space.clientKey, mode, startX: event.clientX, startY: event.clientY,
    origin: { x: space.x, y: space.y, width: space.width, height: space.height },
  }
  svg.value?.setPointerCapture(event.pointerId)
}

function drag(event: PointerEvent) {
  if (!dragState || !svg.value) return
  const rect = svg.value.getBoundingClientRect()
  const dx = (event.clientX - dragState.startX) / rect.width * 100
  const dy = (event.clientY - dragState.startY) / rect.height * 100
  const space = activeFloor.value.spaces.find(item => item.clientKey === dragState?.key)
  if (!space) return
  Object.assign(space, dragState.mode === 'move'
    ? moveBox(dragState.origin, dx, dy)
    : resizeBox(dragState.origin, dx, dy))
}

function stopDrag(event: PointerEvent) {
  if (!dragState) return
  svg.value?.releasePointerCapture(event.pointerId)
  dragState = null
}

function moveWithKeyboard(event: KeyboardEvent, space: Space) {
  const direction: Record<string, [number, number]> = {
    ArrowLeft: [-1, 0], ArrowRight: [1, 0], ArrowUp: [0, -1], ArrowDown: [0, 1],
  }
  const delta = direction[event.key]
  if (!delta) return
  event.preventDefault()
  checkpoint()
  const step = event.shiftKey ? 5 : 1
  Object.assign(space, moveBox(space, delta[0] * step, delta[1] * step))
}

function normalizeSelected() {
  if (selected.value) Object.assign(selected.value, normalizeBox(selected.value))
}

async function save() {
  if (floors.value.some(floor => !floor.name.trim()
      || new Set(floor.spaces.map(space => space.name.trim().toLowerCase())).size !== floor.spaces.length
      || floor.spaces.some(space => !space.name.trim()))) {
    return ElMessage.warning('Floor and space names are required and space names must be unique per floor')
  }
  saving.value = true
  try {
    const { data } = await api.put('/admin/gym-layout', JSON.parse(payload()))
    floors.value = data.floors.map((floor: Floor) => ({
      ...floor,
      spaces: floor.spaces.map(space => ({ ...space, clientKey: keyFor(space.id) })),
    }))
    activeIndex.value = Math.min(activeIndex.value, floors.value.length - 1)
    selectedKey.value = ''
    history.value = []
    savedPayload.value = payload()
    ElMessage.success('Layout saved')
  } catch (error) {
    ElMessage.error(messageOf(error))
  } finally {
    saving.value = false
  }
}

function uniqueName(prefix: string) {
  const names = new Set(activeFloor.value.spaces.map(space => space.name))
  let index = 1
  while (names.has(`${prefix} ${index}`)) index++
  return `${prefix} ${index}`
}

function floorLinks(floor: Floor) {
  return floor.spaces.reduce((total, space) => total + space.equipmentCount + space.sessionCount, 0)
}

function keyFor(id: number | null) {
  return id == null ? crypto.randomUUID() : `space-${id}`
}

function warnUnsaved(event: BeforeUnloadEvent) {
  if (!dirty.value) return
  event.preventDefault()
}
</script>

<template>
  <div class="admin-page layout-page">
    <header class="admin-title layout-title">
      <div><p>FACILITY</p><h1>Gym layout</h1><span>Build floors, rooms and activity areas.</span></div>
      <div class="layout-actions">
        <button type="button" :disabled="!history.length" @click="undo">Undo</button>
        <button type="button" :disabled="saving || !dirty" @click="save">{{ saving ? 'Saving…' : 'Save layout' }}</button>
      </div>
    </header>

    <div v-if="loading" class="admin-panel empty">Loading…</div>
    <template v-else>
      <section class="floor-toolbar admin-panel">
        <div class="floor-tabs" role="tablist" aria-label="Gym floors">
          <button
            v-for="(floor, index) in floors" :key="floor.id ?? index"
            type="button" role="tab" :aria-selected="index === activeIndex"
            :class="{ active: index === activeIndex }" @click="selectFloor(index)"
          >{{ floor.name }}</button>
          <button class="add-floor" type="button" @click="addFloor">+ Floor</button>
        </div>
        <div class="floor-controls">
          <button type="button" :disabled="activeIndex === 0" aria-label="Move floor left" @click="moveFloor(-1)">←</button>
          <button type="button" :disabled="activeIndex === floors.length - 1" aria-label="Move floor right" @click="moveFloor(1)">→</button>
          <button
            class="danger" type="button"
            :disabled="floors.length === 1 || floorLinks(activeFloor) > 0"
            :title="floorLinks(activeFloor) ? 'Move linked equipment and classes first' : 'Delete floor'"
            @click="removeFloor"
          >Delete floor</button>
        </div>
      </section>

      <div class="layout-workspace">
        <section class="layout-canvas-card admin-panel">
          <div class="layout-tools">
            <div>
              <button type="button" @click="addSpace('ROOM')">+ Room</button>
              <button type="button" @click="addSpace('AREA')">+ Area</button>
            </div>
            <span>{{ activeFloor.spaces.length }} spaces · drag to move · corner to resize</span>
          </div>
          <div class="layout-canvas-scroll">
            <svg
              ref="svg" class="layout-canvas" viewBox="0 0 100 100"
              preserveAspectRatio="none"
              aria-label="Editable gym floor layout"
              @pointermove="drag" @pointerup="stopDrag" @pointercancel="stopDrag"
            >
              <defs>
                <pattern id="layout-grid" width="5" height="5" patternUnits="userSpaceOnUse">
                  <path d="M 5 0 L 0 0 0 5" fill="none" stroke="#dbe4ea" stroke-width=".15"/>
                </pattern>
              </defs>
              <rect width="100" height="100" fill="url(#layout-grid)"/>
              <g
                v-for="space in orderedSpaces" :key="space.clientKey"
                class="layout-space" :class="[space.type.toLowerCase(), { selected: selectedKey === space.clientKey }]"
                tabindex="0" role="button" :aria-label="`${space.name}, ${space.type.toLowerCase()}`"
                @keydown="moveWithKeyboard($event, space)"
                @pointerdown="startDrag($event, space, 'move')"
              >
                <rect :x="space.x" :y="space.y" :width="space.width" :height="space.height" rx="1"/>
                <text :x="space.x + 1.5" :y="space.y + 4">{{ space.name }}</text>
                <text class="space-kind" :x="space.x + 1.5" :y="space.y + 7">{{ space.type }}</text>
                <rect
                  v-if="selectedKey === space.clientKey" class="resize-handle"
                  :x="space.x + space.width - 2.5" :y="space.y + space.height - 2.5"
                  width="3" height="3" rx=".5"
                  @pointerdown.stop="startDrag($event, space, 'resize')"
                />
              </g>
            </svg>
          </div>
        </section>

        <aside class="layout-inspector admin-panel">
          <template v-if="selected">
            <small>SELECTED SPACE</small>
            <label>Name<input v-model.trim="selected.name" maxlength="80" @focus="checkpoint"></label>
            <label>Type<select v-model="selected.type" @focus="checkpoint"><option value="ROOM">Room</option><option value="AREA">Area</option></select></label>
            <div class="geometry-fields">
              <label>X<input v-model.number="selected.x" type="number" min="0" max="97" @focus="checkpoint" @change="normalizeSelected"></label>
              <label>Y<input v-model.number="selected.y" type="number" min="0" max="97" @focus="checkpoint" @change="normalizeSelected"></label>
              <label>Width<input v-model.number="selected.width" type="number" min="3" max="100" @focus="checkpoint" @change="normalizeSelected"></label>
              <label>Height<input v-model.number="selected.height" type="number" min="3" max="100" @focus="checkpoint" @change="normalizeSelected"></label>
            </div>
            <p>{{ selected.equipmentCount }} equipment · {{ selected.sessionCount }} classes</p>
            <button
              class="danger" type="button"
              :disabled="selected.equipmentCount + selected.sessionCount > 0"
              :title="selected.equipmentCount + selected.sessionCount ? 'Move linked equipment and classes first' : 'Delete space'"
              @click="removeSpace"
            >Delete space</button>
          </template>
          <template v-else>
            <small>FLOOR</small>
            <label>Name<input v-model.trim="activeFloor.name" maxlength="80" @focus="checkpoint"></label>
            <p>Select a room or area to edit its details. Use arrow keys to move a selected space.</p>
          </template>
        </aside>
      </div>
    </template>
  </div>
</template>
