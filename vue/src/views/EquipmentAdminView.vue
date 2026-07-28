<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElDialog, ElMessage, ElMessageBox } from 'element-plus'
import 'element-plus/es/components/dialog/style/css'
import { api, messageOf } from '../api'

type Row = Record<string, any>
const resources = ref<Row[]>([])
const units = ref<Row[]>([])
const spaces = ref<Row[]>([])
const selectedResourceId = ref<number | null>(null)
const selectedUnitIds = ref<number[]>([])
const loading = ref(true)
const lastLoadedAt = ref<Date | null>(null)
const resourceSearch = ref('')
const resourceCategory = ref('ALL')
const unitSearch = ref('')
const unitStatus = ref('ALL')
const typeDialog = ref(false)
const unitDialog = ref(false)
const maintenanceDialog = ref(false)
const editingTypeId = ref<number | null>(null)
const editingUnitId = ref<number | null>(null)
const editingMaintenanceId = ref<number | null>(null)
const maintenanceHistory = ref<Row[]>([])
const batchStatus = ref('AVAILABLE')
const batchSpace = ref('KEEP')

const typeForm = reactive({
  name: '', category: '', description: '', unitLabel: 'units',
  initialUnits: 1, spaceId: '' as number | '',
})
const unitForm = reactive({
  assetCode: '', spaceId: '' as number | '', serialNumber: '',
  purchasedOn: '', status: 'AVAILABLE', notes: '',
})
const addUnitsForm = reactive({ count: 1, spaceId: '' as number | '' })
const maintenanceForm = reactive({
  startsAt: localDateTime(new Date()),
  endsAt: localDateTime(new Date(Date.now() + 2 * 60 * 60_000)),
  reason: '', notes: '',
})

const selectedResource = computed(() => resources.value.find(item => item.id === selectedResourceId.value))
const resourceCategories = computed(() => [...new Set(resources.value.map(item => item.category))].sort())
const filteredResources = computed(() => resources.value.filter(item =>
  (resourceCategory.value === 'ALL' || item.category === resourceCategory.value)
  && `${item.name} ${item.category} ${item.description}`.toLowerCase().includes(resourceSearch.value.toLowerCase()),
))
const totals = computed(() => resources.value.reduce((total, item) => ({
  units: total.units + Number(item.totalUnits || 0),
  ready: total.ready + Number(item.availableUnits || 0),
  attention: total.attention + Number(item.maintenanceUnits || 0) + Number(item.outOfServiceUnits || 0),
}), { units: 0, ready: 0, attention: 0 }))
const readyPercent = computed(() => totals.value.units
  ? Math.round(totals.value.ready / totals.value.units * 100)
  : 0)
const filteredUnits = computed(() => units.value.filter(unit =>
  (unitStatus.value === 'ALL' || unit.status === unitStatus.value)
  && `${unit.assetCode} ${unit.serialNumber ?? ''} ${unit.spaceName ?? ''}`.toLowerCase()
    .includes(unitSearch.value.toLowerCase()),
))

onMounted(load)

async function load() {
  loading.value = true
  try {
    const [resourceResponse, layoutResponse] = await Promise.all([
      api.get('/admin/resources'),
      api.get('/admin/gym-layout'),
    ])
    resources.value = resourceResponse.data
    spaces.value = layoutResponse.data.floors.flatMap((floor: Row) =>
      floor.spaces.map((space: Row) => ({ id: space.id, label: `${floor.name} · ${space.name}` })),
    )
    lastLoadedAt.value = new Date()
    if (selectedResourceId.value && resources.value.some(item => item.id === selectedResourceId.value)) {
      await loadUnits(selectedResourceId.value)
    } else if (resources.value.length) {
      await loadUnits(resources.value[0].id)
    }
  } catch (error) {
    ElMessage.error(messageOf(error))
  } finally {
    loading.value = false
  }
}

async function loadUnits(id: number) {
  selectedResourceId.value = id
  selectedUnitIds.value = []
  units.value = (await api.get(`/admin/equipment/${id}/units`)).data
}

function openCreateType() {
  editingTypeId.value = null
  Object.assign(typeForm, {
    name: '', category: '', description: '', unitLabel: 'units', initialUnits: 1, spaceId: '',
  })
  typeDialog.value = true
}

function openEditType(item: Row) {
  editingTypeId.value = item.id
  Object.assign(typeForm, {
    name: item.name, category: item.category, description: item.description,
    unitLabel: item.unitLabel, initialUnits: 1, spaceId: '',
  })
  typeDialog.value = true
}

async function saveType() {
  try {
    if (editingTypeId.value) {
      await api.patch(`/admin/equipment/${editingTypeId.value}`, {
        name: typeForm.name, category: typeForm.category,
        description: typeForm.description, unitLabel: typeForm.unitLabel,
      })
    } else {
      await api.post('/admin/equipment', {
        ...typeForm, spaceId: typeForm.spaceId || null,
      })
    }
    typeDialog.value = false
    ElMessage.success('Resource saved')
    await load()
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function archiveType(item: Row) {
  try {
    await ElMessageBox.confirm(`Archive ${item.name} and all its active units?`, 'Archive resource')
    await api.delete(`/admin/equipment/${item.id}`)
    if (selectedResourceId.value === item.id) {
      selectedResourceId.value = null
      units.value = []
    }
    await load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error))
  }
}

async function addUnits() {
  if (!selectedResourceId.value) return
  try {
    await api.post(`/admin/equipment/${selectedResourceId.value}/units`, {
      count: addUnitsForm.count, spaceId: addUnitsForm.spaceId || null,
    })
    ElMessage.success('Units added')
    await load()
  } catch (error) { ElMessage.error(messageOf(error)) }
}

function openUnit(item: Row) {
  editingUnitId.value = item.id
  Object.assign(unitForm, {
    assetCode: item.assetCode, spaceId: item.spaceId ?? '',
    serialNumber: item.serialNumber ?? '', purchasedOn: dateOnly(item.purchasedOn),
    status: item.baseStatus, notes: item.notes ?? '',
  })
  unitDialog.value = true
}

async function saveUnit() {
  try {
    await api.patch(`/admin/equipment-units/${editingUnitId.value}`, {
      ...unitForm, spaceId: unitForm.spaceId || null, purchasedOn: unitForm.purchasedOn || null,
    })
    unitDialog.value = false
    ElMessage.success('Unit saved')
    await load()
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function archiveUnit(item: Row) {
  try {
    await ElMessageBox.confirm(`Archive ${item.assetCode}?`, 'Archive unit')
    await api.delete(`/admin/equipment-units/${item.id}`)
    await load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error))
  }
}

function toggleUnit(id: number) {
  selectedUnitIds.value = selectedUnitIds.value.includes(id)
    ? selectedUnitIds.value.filter(item => item !== id)
    : [...selectedUnitIds.value, id]
}

async function applyBatch() {
  if (!selectedUnitIds.value.length) return
  try {
    await api.patch('/admin/equipment-units/batch', {
      ids: selectedUnitIds.value,
      status: batchStatus.value,
      updateSpace: batchSpace.value !== 'KEEP',
      spaceId: /^\d+$/.test(batchSpace.value) ? Number(batchSpace.value) : null,
    })
    ElMessage.success('Units updated')
    await load()
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function openMaintenance(item?: Row) {
  editingUnitId.value = item?.id ?? null
  editingMaintenanceId.value = null
  maintenanceHistory.value = item
    ? (await api.get(`/admin/equipment-units/${item.id}/maintenance`)).data
    : []
  resetMaintenance()
  maintenanceDialog.value = true
}

function editMaintenance(item: Row) {
  editingMaintenanceId.value = item.id
  Object.assign(maintenanceForm, {
    startsAt: localDateTime(new Date(item.startsAt)),
    endsAt: localDateTime(new Date(item.endsAt)),
    reason: item.reason, notes: item.notes ?? '',
  })
}

async function saveMaintenance() {
  const payload = {
    startsAt: new Date(maintenanceForm.startsAt).toISOString(),
    endsAt: new Date(maintenanceForm.endsAt).toISOString(),
    reason: maintenanceForm.reason,
    notes: maintenanceForm.notes,
  }
  try {
    if (!editingUnitId.value) {
      await api.post('/admin/equipment-units/batch-maintenance', {
        ids: selectedUnitIds.value, maintenance: payload,
      })
    } else if (editingMaintenanceId.value) {
      await api.put(`/admin/equipment-units/${editingUnitId.value}/maintenance/${editingMaintenanceId.value}`, payload)
    } else {
      await api.post(`/admin/equipment-units/${editingUnitId.value}/maintenance`, payload)
    }
    ElMessage.success('Maintenance saved')
    maintenanceDialog.value = false
    await load()
  } catch (error) { ElMessage.error(messageOf(error)) }
}

async function deleteMaintenance(item: Row) {
  try {
    await api.delete(`/admin/equipment-units/${editingUnitId.value}/maintenance/${item.id}`)
    maintenanceHistory.value = (await api.get(`/admin/equipment-units/${editingUnitId.value}/maintenance`)).data
    resetMaintenance()
  } catch (error) { ElMessage.error(messageOf(error)) }
}

function resetMaintenance() {
  editingMaintenanceId.value = null
  Object.assign(maintenanceForm, {
    startsAt: localDateTime(new Date()),
    endsAt: localDateTime(new Date(Date.now() + 2 * 60 * 60_000)),
    reason: '', notes: '',
  })
}

function localDateTime(value: Date) {
  const local = new Date(value.getTime() - value.getTimezoneOffset() * 60_000)
  return local.toISOString().slice(0, 16)
}
function dateOnly(value: unknown) {
  return String(value ?? '').match(/\d{4}-\d{2}-\d{2}/)?.[0] ?? ''
}
function dateTime(value: string) {
  return new Date(value).toLocaleString('en-CA')
}
function shortTime(value: Date | null) {
  return value?.toLocaleTimeString('en-CA', { hour: '2-digit', minute: '2-digit' }) ?? 'Not synced'
}
</script>

<template>
  <div class="admin-page equipment-admin">
    <header class="admin-title">
      <div>
        <p>INVENTORY CONTROL</p>
        <h1>Equipment availability</h1>
        <span class="page-summary">Manage resource types, individual assets and planned maintenance.</span>
      </div>
      <div class="equipment-head-actions">
        <span>Last synced {{ shortTime(lastLoadedAt) }}</span>
        <button class="secondary" type="button" :disabled="loading" @click="load">↻ Refresh</button>
        <button class="primary" type="button" @click="openCreateType">＋ Add resource type</button>
      </div>
    </header>

    <section class="inventory-summary" aria-label="Inventory summary">
      <article><span>Resource types</span><strong>{{ resources.length }}</strong><small>active resource types</small></article>
      <article><span>Total assets</span><strong>{{ totals.units }}</strong><small>individual units</small></article>
      <article><span>Ready now</span><strong>{{ readyPercent }}%</strong><small>{{ totals.ready }} assets available</small></article>
      <article :class="{ attention: totals.attention }"><span>Needs attention</span><strong>{{ totals.attention }}</strong><small>maintenance or out of service</small></article>
    </section>

    <div v-if="loading" class="admin-panel empty" role="status">Loading resources…</div>
    <template v-else>
      <div class="resource-console">
        <aside class="admin-panel resource-catalogue" aria-label="Resource catalogue">
          <header>
            <div><h2>Resource types</h2><span>{{ filteredResources.length }} of {{ resources.length }}</span></div>
            <input v-model.trim="resourceSearch" type="search" placeholder="Search resources" aria-label="Search resource types">
            <select v-model="resourceCategory" aria-label="Filter resource category">
              <option value="ALL">All categories</option>
              <option v-for="category in resourceCategories" :key="category">{{ category }}</option>
            </select>
          </header>

          <div class="resource-list">
            <article v-for="item in filteredResources" :key="item.id" :class="{ selected: item.id === selectedResourceId }">
              <button class="resource-select" type="button" :aria-pressed="item.id === selectedResourceId" @click="loadUnits(item.id)">
                <span class="resource-name"><small>{{ item.category }}</small><strong>{{ item.name }}</strong></span>
                <span class="resource-count"><strong>{{ item.availableUnits }}</strong><small>/ {{ item.totalUnits }} ready</small></span>
                <span class="availability-track" aria-hidden="true"><i :style="{ width: `${item.totalUnits ? item.availableUnits / item.totalUnits * 100 : 0}%` }"></i></span>
                <span class="resource-signals">
                  <small><i class="in-use"></i>{{ item.inUseUnits }} in use</small>
                  <small><i class="maintenance"></i>{{ item.maintenanceUnits }} maintenance</small>
                  <small><i class="out"></i>{{ item.outOfServiceUnits }} out</small>
                </span>
              </button>
              <div class="resource-actions">
                <button type="button" :aria-label="`Edit ${item.name}`" @click="openEditType(item)">Edit</button>
                <button class="danger" type="button" :aria-label="`Archive ${item.name}`" @click="archiveType(item)">Archive</button>
              </div>
            </article>
            <p v-if="!filteredResources.length" class="empty">No resource types match these filters.</p>
          </div>
        </aside>

        <section v-if="selectedResource" class="admin-panel unit-workspace" aria-label="Individual asset management">
          <header class="unit-head">
            <div>
              <small>{{ selectedResource.category }} · UNIT MANAGEMENT</small>
              <h2>{{ selectedResource.name }}</h2>
              <p>{{ selectedResource.description }}</p>
            </div>
            <div class="unit-head-actions">
              <div class="selected-health">
                <strong>{{ selectedResource.availableUnits }} / {{ selectedResource.totalUnits }}</strong>
                <span>{{ selectedResource.unitLabel }} ready now</span>
              </div>
              <div><button type="button" @click="openEditType(selectedResource)">Edit type</button><button class="danger" type="button" @click="archiveType(selectedResource)">Archive type</button></div>
            </div>
          </header>

          <div class="status-legend" aria-label="Status definitions">
            <span><i class="available"></i><strong>Available</strong> ready for walk-in use</span>
            <span><i class="in-use"></i><strong>In use</strong> currently occupied</span>
            <span><i class="maintenance"></i><strong>Maintenance</strong> inside a planned service window</span>
            <span><i class="out"></i><strong>Out of service</strong> unavailable until manually restored</span>
          </div>

          <form class="add-units" @submit.prevent="addUnits">
            <strong>Add assets</strong>
            <label>Quantity<input v-model.number="addUnitsForm.count" type="number" min="1" max="100" required></label>
            <label>Initial location<select v-model.number="addUnitsForm.spaceId">
              <option value="">Not assigned</option><option v-for="space in spaces" :key="space.id" :value="space.id">{{ space.label }}</option>
            </select></label>
            <button type="submit">Add to inventory</button>
          </form>

          <div class="unit-toolbar">
            <input v-model.trim="unitSearch" type="search" placeholder="Search asset code, serial or location" aria-label="Search individual assets">
            <select v-model="unitStatus" aria-label="Filter asset status"><option value="ALL">All statuses</option><option>AVAILABLE</option><option>IN_USE</option><option>MAINTENANCE</option><option>OUT_OF_SERVICE</option></select>
            <span class="result-count">{{ filteredUnits.length }} assets</span>
          </div>

          <div v-if="selectedUnitIds.length" class="batch-bar" role="region" aria-label="Bulk asset actions">
            <strong>{{ selectedUnitIds.length }} selected</strong>
            <label>Status<select v-model="batchStatus"><option>AVAILABLE</option><option>IN_USE</option><option>OUT_OF_SERVICE</option><option>RETIRED</option></select></label>
            <label>Location<select v-model="batchSpace"><option value="KEEP">Keep current</option><option value="">Not assigned</option><option v-for="space in spaces" :key="space.id" :value="String(space.id)">{{ space.label }}</option></select></label>
            <button type="button" @click="applyBatch">Apply changes</button>
            <button type="button" @click="openMaintenance()">Schedule maintenance</button>
          </div>

          <div class="unit-table-wrap">
            <table>
              <thead><tr><th scope="col">Select</th><th scope="col">Asset</th><th scope="col">Effective status</th><th scope="col">Location</th><th scope="col">Actions</th></tr></thead>
              <tbody>
                <tr v-for="item in filteredUnits" :key="item.id">
                  <td><input type="checkbox" :checked="selectedUnitIds.includes(item.id)" :aria-label="`Select ${item.assetCode}`" @change="toggleUnit(item.id)"></td>
                  <td><strong>{{ item.assetCode }}</strong><small class="cell-meta">Serial: {{ item.serialNumber || 'Not recorded' }}</small></td>
                  <td><span class="unit-status" :class="item.status.toLowerCase().replaceAll('_', '-')">{{ item.status.replaceAll('_', ' ') }}</span></td>
                  <td>{{ [item.floorName, item.spaceName].filter(Boolean).join(' · ') || 'Not assigned' }}<small class="cell-meta">Updated {{ dateTime(item.updatedAt) }}</small></td>
                  <td class="unit-actions"><button type="button" @click="openUnit(item)">Edit</button><button type="button" @click="openMaintenance(item)">Maintenance</button><button class="danger" type="button" @click="archiveUnit(item)">Archive</button></td>
                </tr>
              </tbody>
            </table>
            <p v-if="!filteredUnits.length" class="empty">No assets match this search and status filter.</p>
          </div>
        </section>
      </div>
    </template>

    <el-dialog v-model="typeDialog" :title="editingTypeId ? 'Edit resource' : 'Add resource'" width="min(560px, 92vw)">
      <form class="equipment-dialog-form" @submit.prevent="saveType">
        <p>Resource types are member-facing groups. Individual assets are managed after the type is created.</p>
        <label>Resource name<input v-model.trim="typeForm.name" maxlength="120" placeholder="e.g. Treadmill" required></label>
        <label>Category<input v-model.trim="typeForm.category" list="equipment-categories" maxlength="80" placeholder="e.g. Cardio" required></label>
        <datalist id="equipment-categories"><option v-for="item in ['Cardio','Strength','Free Weights','Functional','Studios','Aquatics & Recovery','Courts']" :key="item">{{ item }}</option></datalist>
        <label>Member-facing description<textarea v-model.trim="typeForm.description" maxlength="1000" rows="3" placeholder="A short description shown on the availability page" required></textarea></label>
        <label>Counting unit<input v-model.trim="typeForm.unitLabel" maxlength="32" placeholder="e.g. machines, lanes or rooms" required></label>
        <template v-if="!editingTypeId">
          <label>Initial asset count<input v-model.number="typeForm.initialUnits" type="number" min="1" max="100" required></label>
          <label>Initial location<select v-model.number="typeForm.spaceId"><option value="">Not assigned</option><option v-for="space in spaces" :key="space.id" :value="space.id">{{ space.label }}</option></select></label>
        </template>
        <button type="submit">Save resource</button>
      </form>
    </el-dialog>

    <el-dialog v-model="unitDialog" title="Edit equipment unit" width="min(560px, 92vw)">
      <form class="equipment-dialog-form" @submit.prevent="saveUnit">
        <p>These details are internal and are never shown on the member availability page.</p>
        <label>Asset ID<input v-model.trim="unitForm.assetCode" maxlength="64" placeholder="Unique inventory identifier" required></label>
        <label>Location<select v-model.number="unitForm.spaceId"><option value="">Not assigned</option><option v-for="space in spaces" :key="space.id" :value="space.id">{{ space.label }}</option></select></label>
        <label>Serial number<input v-model.trim="unitForm.serialNumber" maxlength="120" placeholder="Optional manufacturer serial number"></label>
        <label>Purchase date<input v-model="unitForm.purchasedOn" type="date"></label>
        <label>Base status<select v-model="unitForm.status"><option>AVAILABLE</option><option>IN_USE</option><option>OUT_OF_SERVICE</option></select></label>
        <label>Internal notes<textarea v-model.trim="unitForm.notes" maxlength="1000" rows="3" placeholder="Condition, warranty or service context"></textarea></label>
        <button type="submit">Save unit</button>
      </form>
    </el-dialog>

    <el-dialog v-model="maintenanceDialog" :title="editingUnitId ? 'Unit maintenance' : `Maintenance for ${selectedUnitIds.length} units`" width="min(700px, 94vw)">
      <form class="equipment-dialog-form" @submit.prevent="saveMaintenance">
        <p>An asset is automatically shown as Maintenance during this window. Past records remain read-only.</p>
        <div class="maintenance-times"><label>Starts<input v-model="maintenanceForm.startsAt" type="datetime-local" required></label><label>Ends<input v-model="maintenanceForm.endsAt" type="datetime-local" required></label></div>
        <label>Reason<input v-model.trim="maintenanceForm.reason" maxlength="160" placeholder="e.g. Preventive service" required></label>
        <label>Internal maintenance notes<textarea v-model.trim="maintenanceForm.notes" maxlength="1000" rows="3" placeholder="Vendor, work order or repair details"></textarea></label>
        <div class="dialog-actions"><button type="submit">{{ editingMaintenanceId ? 'Update maintenance' : 'Schedule maintenance' }}</button><button v-if="editingMaintenanceId" type="button" @click="resetMaintenance">Cancel edit</button></div>
      </form>
      <section v-if="editingUnitId && maintenanceHistory.length" class="maintenance-history">
        <h3>Maintenance history</h3>
        <article v-for="item in maintenanceHistory" :key="item.id">
          <div><strong>{{ item.reason }}</strong><span>{{ dateTime(item.startsAt) }} – {{ dateTime(item.endsAt) }}</span><p>{{ item.notes }}</p></div>
          <div v-if="new Date(item.endsAt).getTime() > Date.now()"><button type="button" @click="editMaintenance(item)">Edit</button><button type="button" @click="deleteMaintenance(item)">Cancel</button></div>
        </article>
      </section>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-summary { margin-top: 7px; display: block; color: #718091; font-size: 12px; }
.equipment-head-actions { display: flex; align-items: center; gap: 8px; }
.equipment-head-actions > span { margin-right: 5px; color: #8793a0; font-size: 10px; }
.equipment-admin button { padding: 8px 10px; border: 0; border-radius: 7px; color: #4f6377; background: #edf1f4; font-weight: 700; }
.equipment-admin button:hover { background: #e3e9ee; }
.equipment-admin button.primary, .add-units button, .batch-bar button:first-of-type, .equipment-dialog-form > button, .dialog-actions button:first-child { color: white; background: #3f5c74; }
.equipment-admin button.primary:hover, .add-units button:hover, .batch-bar button:first-of-type:hover { background: #314d65; }
.equipment-admin button.danger { color: #98534d; background: transparent; }

.inventory-summary { margin-bottom: 18px; display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.inventory-summary article { padding: 16px 18px; display: grid; grid-template-columns: 1fr auto; align-items: end; gap: 2px 12px; background: white; border: 1px solid #edf0f3; border-radius: 10px; }
.inventory-summary span { color: #758291; font-size: 10px; font-weight: 800; letter-spacing: .5px; text-transform: uppercase; }
.inventory-summary strong { grid-row: span 2; color: #29445c; font-size: 28px; line-height: 1; }
.inventory-summary small { color: #929ca7; font-size: 10px; }
.inventory-summary article.attention strong { color: #a05a50; }

.resource-console { display: grid; grid-template-columns: minmax(290px, 340px) minmax(0, 1fr); align-items: start; gap: 16px; }
.resource-catalogue { padding: 0; overflow: hidden; }
.resource-catalogue > header { padding: 18px; display: grid; grid-template-columns: 1fr 1fr; gap: 9px; border-bottom: 1px solid #edf0f3; }
.resource-catalogue > header div { grid-column: 1 / -1; display: flex; align-items: baseline; justify-content: space-between; }
.resource-catalogue h2 { color: #2c4359; font-size: 16px; }
.resource-catalogue header span, .result-count { color: #8794a0; font-size: 10px; font-weight: 700; }
.resource-catalogue input, .resource-catalogue select, .unit-workspace input, .unit-workspace select { min-width: 0; padding: 9px 10px; border: 1px solid #dce2e8; border-radius: 7px; color: #435a70; background: white; font-size: 11px; }
.resource-list { max-height: 690px; overflow-y: auto; }
.resource-list article { position: relative; border-bottom: 1px solid #edf0f3; }
.resource-list article.selected { background: #f4f8f0; box-shadow: inset 3px 0 #78965a; }
.resource-select { width: 100%; padding: 14px 14px 12px 17px !important; display: grid; grid-template-columns: 1fr auto; gap: 8px 12px; text-align: left; background: transparent !important; }
.resource-select:hover { background: #f8fafb !important; }
.resource-name, .resource-count { display: flex; flex-direction: column; gap: 3px; }
.resource-name small { color: #d17f63; font-size: 8px; font-weight: 900; letter-spacing: .7px; text-transform: uppercase; }
.resource-name strong { color: #304960; font-size: 13px; }
.resource-count { align-items: end; }
.resource-count strong { color: #304960; font-size: 18px; line-height: 1; }
.resource-count small, .resource-signals small { color: #7e8a96; font-size: 9px; }
.availability-track { grid-column: 1 / -1; height: 4px; overflow: hidden; background: #e8edf1; border-radius: 99px; }
.availability-track > i { height: 100%; display: block; background: #6ca979; border-radius: inherit; }
.resource-signals { grid-column: 1 / -1; display: flex; gap: 11px; }
.resource-signals i, .status-legend i { width: 7px; height: 7px; margin-right: 4px; display: inline-block; border-radius: 50%; }
i.available { background: #64a76f; } i.in-use { background: #e0a94d; } i.maintenance { background: #638db5; } i.out { background: #b86969; }
.resource-actions { position: absolute; right: 9px; bottom: 7px; opacity: 0; transition: opacity .15s; }
.resource-list article:hover .resource-actions, .resource-list article:focus-within .resource-actions { opacity: 1; }
.resource-actions button { padding: 4px 6px; font-size: 9px; }

.unit-workspace { padding: 0; overflow: hidden; }
.unit-head { padding: 20px 22px; display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; border-bottom: 1px solid #edf0f3; }
.unit-head small { color: #d17f63; font-size: 9px; font-weight: 900; letter-spacing: .8px; }
.unit-head h2 { margin: 5px 0; color: #29445c; font-size: 24px; }
.unit-head p { max-width: 620px; color: #7b8793; font-size: 11px; line-height: 1.5; }
.selected-health { min-width: 110px; padding: 10px 13px; display: flex; flex-direction: column; align-items: end; color: #47725a; background: #eef7f0; border-radius: 9px; }
.selected-health strong { font-size: 22px; }
.selected-health span { color: #6d8174; font-size: 9px; }
.unit-head-actions { display: grid; justify-items: end; gap: 7px; }
.unit-head-actions > div:last-child { display: flex; gap: 4px; }
.unit-head-actions button { padding: 5px 7px; font-size: 9px; }
.status-legend { padding: 11px 20px; display: flex; flex-wrap: wrap; gap: 8px 18px; background: #f8fafb; border-bottom: 1px solid #edf0f3; }
.status-legend span { color: #7d8995; font-size: 9px; }
.status-legend strong { margin-right: 3px; color: #536678; }
.add-units { padding: 13px 18px; display: flex; align-items: end; gap: 10px; border-bottom: 1px solid #edf0f3; }
.add-units > strong { margin: 0 auto 8px 0; color: #425b71; font-size: 12px; }
.add-units label, .batch-bar label { display: grid; gap: 4px; color: #7d8995; font-size: 9px; font-weight: 800; }
.add-units input { width: 72px; }
.unit-toolbar { padding: 12px 18px; display: flex; align-items: center; gap: 8px; background: #fbfcfd; }
.unit-toolbar > input { min-width: 220px; flex: 1; }
.result-count { white-space: nowrap; }
.batch-bar { padding: 10px 18px; display: flex; align-items: end; gap: 9px; color: #425b71; background: #fff8df; border-block: 1px solid #f3e4b3; }
.batch-bar > strong { margin: 0 auto 9px 0; font-size: 11px; }
.unit-table-wrap { overflow-x: auto; }
.unit-table-wrap table { width: 100%; border-collapse: collapse; }
.unit-table-wrap th { color: #788694; background: #f8fafb; font-size: 9px; letter-spacing: .4px; text-transform: uppercase; }
.unit-table-wrap th, .unit-table-wrap td { padding: 11px 12px; border-bottom: 1px solid #edf0f3; text-align: left; white-space: nowrap; }
.unit-table-wrap td { color: #5d6d7d; font-size: 11px; }
.unit-table-wrap tbody tr:hover { background: #fbfcfd; }
.cell-meta { margin-top: 4px; display: block; color: #929ca7; font-size: 8px; }
.unit-actions { display: flex; gap: 4px; }
.unit-actions button { padding: 5px 7px; font-size: 9px; }
.unit-status { padding: 5px 7px; border-radius: 999px; color: #2f7047; background: #e9f7ed; font-size: 8px; font-weight: 900; }
.unit-status.in-use { color: #8a6721; background: #fff3d5; }
.unit-status.maintenance { color: #3f678d; background: #e6f0f8; }
.unit-status.out-of-service { color: #8c4545; background: #fae9e9; }

.equipment-dialog-form { display: grid; gap: 12px; }
.equipment-dialog-form > p { margin: -3px 0 3px; color: #7b8793; font-size: 11px; line-height: 1.5; }
.equipment-dialog-form label { display: grid; gap: 5px; color: #53677a; font-size: 10px; font-weight: 800; }
.equipment-dialog-form input, .equipment-dialog-form select, .equipment-dialog-form textarea { width: 100%; padding: 10px 12px; border: 1px solid #dce2e8; border-radius: 8px; font-weight: 400; }
.maintenance-times, .dialog-actions { display: flex; gap: 10px; }
.maintenance-times label { flex: 1; }
.maintenance-history { margin-top: 22px; border-top: 1px solid #edf0f3; }
.maintenance-history h3 { margin: 18px 0 10px; }
.maintenance-history article { padding: 11px 0; display: flex; justify-content: space-between; gap: 16px; border-bottom: 1px solid #edf0f3; }
.maintenance-history span, .maintenance-history p { margin-top: 4px; display: block; color: #7b8793; font-size: 11px; }

@media (max-width: 1120px) {
  .inventory-summary { grid-template-columns: repeat(2, 1fr); }
  .resource-console { grid-template-columns: 280px minmax(0, 1fr); }
  .status-legend span { flex: 1 1 40%; }
}
@media (max-width: 820px) {
  .admin-title, .equipment-head-actions, .unit-head, .add-units { align-items: stretch; flex-direction: column; }
  .equipment-head-actions > span { margin: 0; }
  .resource-console { grid-template-columns: 1fr; }
  .resource-list { max-height: 430px; }
  .resource-actions { opacity: 1; }
  .unit-head-actions { justify-items: start; }
  .selected-health { align-items: start; }
}
@media (max-width: 560px) {
  .inventory-summary { grid-template-columns: 1fr; }
  .resource-catalogue > header { grid-template-columns: 1fr; }
  .resource-catalogue > header div { grid-column: auto; }
  .status-legend { display: grid; }
  .unit-toolbar, .batch-bar, .maintenance-times { align-items: stretch; flex-direction: column; }
  .batch-bar > strong { margin: 0; }
}
</style>
