<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api, messageOf } from '../api'
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
const result = ref<ScanResult | null>(null)
const error = ref('')

onMounted(async () => {
  const token = route.hash.slice(1)
  history.replaceState(history.state, '', route.path)
  if (!token) {
    error.value = 'No membership pass was provided.'
    return
  }
  try {
    result.value = (await api.post<ScanResult>('/staff/scans/resolve', { token })).data
  } catch (caught) {
    error.value = messageOf(caught)
  }
})
</script>

<template>
  <main class="scan-page">
    <section class="scan-card">
      <p class="eyebrow">Membership scan</p>
      <div v-if="result" :class="['scan-status', { active: result.active }]">
        {{ result.active ? 'Active membership' : 'Access requires review' }}
      </div>
      <p v-if="result?.accessScope !== 'ADMIN'" class="scan-scope">
        {{ result?.accessScope === 'ASSIGNED_STUDENT' ? 'Assigned student' : 'Current session attendee' }}
      </p>
      <h1 v-if="result">{{ result.displayName }}</h1>
      <dl v-if="result">
        <div><dt>Member number</dt><dd>{{ result.memberNumber }}</dd></div>
        <div><dt>Plan</dt><dd>{{ result.planName || 'No plan' }}</dd></div>
        <div><dt>Status</dt><dd>{{ result.status }}</dd></div>
        <div v-if="result.endsOn"><dt>Ends</dt><dd>{{ result.endsOn }}</dd></div>
      </dl>
      <p v-else-if="error" class="scan-error" role="alert">{{ error }}</p>
      <p v-else>Verifying membership pass…</p>
      <RouterLink class="button dark" :to="session.user?.role === 'ADMIN' ? '/user' : '/front/home'">
        {{ session.user?.role === 'ADMIN' ? 'Back to members' : 'Back to dashboard' }}
      </RouterLink>
    </section>
  </main>
</template>

<style scoped>
.scan-page { min-height: 100vh; display: grid; place-items: center; padding: 24px; background: #f3f5f1; }
.scan-card { width: min(520px, 100%); padding: 36px; background: white; border-radius: 20px; box-shadow: 0 22px 60px rgba(28, 42, 33, .12); }
.scan-card h1 { margin: 18px 0 24px; font-size: 36px; }
.scan-status { width: fit-content; padding: 7px 11px; color: #8b4b31; background: #fff0e8; border-radius: 999px; font-weight: 800; }
.scan-status.active { color: #245f3c; background: #e7f5e4; }
.scan-scope { color: #68788b; font-size: 13px; }
dl { margin: 0 0 28px; }
dl div { padding: 13px 0; display: flex; justify-content: space-between; gap: 20px; border-bottom: 1px solid #e7ebe5; }
dt { color: #718077; }
dd { margin: 0; font-weight: 750; text-align: right; }
.scan-error { color: #a53e35; }
</style>
