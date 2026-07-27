<script setup lang="ts">
import QrScanner from 'qr-scanner'
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api, messageOf } from '../api'
import { tokenFromScan } from '../scanPass'
import { session } from '../state'

type ScanResult = {
  memberId: number
  memberNumber: string
  displayName: string
  planName?: string
  status: string
  endsOn?: string
  active: boolean
  accessScope: 'ADMIN' | 'ASSIGNED_STUDENT' | 'CURRENT_SESSION'
}

const route = useRoute()
const video = ref<HTMLVideoElement>()
const result = ref<ScanResult | null>(null)
const error = ref('')
const mode = ref<'idle' | 'camera' | 'loading'>('idle')
let scanner: QrScanner | null = null

async function resolveToken(token: string) {
  mode.value = 'loading'
  error.value = ''
  try {
    result.value = (await api.post<ScanResult>('/staff/scans/resolve', { token })).data
  } catch (caught) {
    error.value = messageOf(caught)
  } finally {
    mode.value = 'idle'
  }
}

async function startCamera() {
  result.value = null
  error.value = ''
  mode.value = 'camera'
  await nextTick()
  if (!video.value) return

  scanner?.destroy()
  scanner = new QrScanner(video.value, async ({ data }) => {
    const token = tokenFromScan(data, location.origin)
    stopCamera()
    if (!token) {
      error.value = 'This is not a valid membership pass for this gym.'
      return
    }
    await resolveToken(token)
  }, {
    preferredCamera: 'environment',
    highlightScanRegion: true,
    highlightCodeOutline: true,
    returnDetailedScanResult: true,
  })

  try {
    await scanner.start()
  } catch (caught) {
    scanner.destroy()
    scanner = null
    mode.value = 'idle'
    error.value = caught instanceof Error && caught.name === 'NotAllowedError'
      ? 'Camera access was denied. Allow camera access and try again.'
      : 'Unable to start a camera on this device.'
  }
}

function stopCamera() {
  scanner?.destroy()
  scanner = null
  mode.value = 'idle'
}

onMounted(async () => {
  const token = route.hash.slice(1)
  history.replaceState(history.state, '', route.path)
  if (token) await resolveToken(token)
})

onBeforeUnmount(stopCamera)
</script>

<template>
  <main class="scan-page">
    <section class="scan-card">
      <p class="eyebrow">Membership scan</p>

      <template v-if="result">
        <div :class="['scan-status', { active: result.active }]">
          {{ result.active ? 'Active membership' : 'Access requires review' }}
        </div>
        <p v-if="result.accessScope !== 'ADMIN'" class="scan-scope">
          {{ result.accessScope === 'ASSIGNED_STUDENT' ? 'Assigned student' : 'Current session attendee' }}
        </p>
        <h1>{{ result.displayName }}</h1>
        <dl>
          <div><dt>Member number</dt><dd>{{ result.memberNumber }}</dd></div>
          <div><dt>Plan</dt><dd>{{ result.planName || 'No plan' }}</dd></div>
          <div><dt>Status</dt><dd>{{ result.status }}</dd></div>
          <div v-if="result.endsOn"><dt>Ends</dt><dd>{{ result.endsOn }}</dd></div>
        </dl>
      </template>

      <div v-else-if="mode === 'camera'" class="camera">
        <video ref="video" muted playsinline></video>
        <p>Center the member QR code in the frame.</p>
      </div>
      <p v-else-if="mode === 'loading'">Verifying membership pass…</p>
      <p v-else>Use this device’s camera to scan a member card.</p>

      <p v-if="error" class="scan-error" role="alert">{{ error }}</p>

      <div class="scan-actions">
        <button v-if="mode === 'camera'" class="button" type="button" @click="stopCamera">Cancel</button>
        <button v-else-if="mode !== 'loading'" class="button dark" type="button" @click="startCamera">
          {{ result || error ? 'Scan another member' : 'Start camera' }}
        </button>
        <RouterLink class="button" :to="session.user?.role === 'ADMIN' ? '/user' : '/front/home'">
          {{ session.user?.role === 'ADMIN' ? 'Back to members' : 'Back to dashboard' }}
        </RouterLink>
      </div>
    </section>
  </main>
</template>

<style scoped>
.scan-page { min-height: 100vh; display: grid; place-items: center; padding: 24px; background: #f3f5f1; }
.scan-card { width: min(560px, 100%); padding: 36px; background: white; border-radius: 20px; box-shadow: 0 22px 60px rgba(28, 42, 33, .12); }
.scan-card h1 { margin: 18px 0 24px; font-size: 36px; }
.scan-status { width: fit-content; padding: 7px 11px; color: #8b4b31; background: #fff0e8; border-radius: 999px; font-weight: 800; }
.scan-status.active { color: #245f3c; background: #e7f5e4; }
.scan-scope { color: #68788b; font-size: 13px; }
.camera { margin: 18px 0; }
.camera video { width: 100%; aspect-ratio: 4 / 3; object-fit: cover; background: #17201b; border-radius: 14px; }
.camera p { margin-bottom: 0; color: #68788b; text-align: center; }
.scan-actions { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 24px; }
dl { margin: 0 0 28px; }
dl div { padding: 13px 0; display: flex; justify-content: space-between; gap: 20px; border-bottom: 1px solid #e7ebe5; }
dt { color: #718077; }
dd { margin: 0; font-weight: 750; text-align: right; }
.scan-error { color: #a53e35; }
</style>
