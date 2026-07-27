import { reactive } from 'vue'
import { api, type User } from './api'

export const session = reactive<{ user: User | null; ready: boolean }>({
  user: null,
  ready: false,
})

export async function loadSession() {
  const { data } = await api.get('/auth/session')
  session.user = data.user
  session.ready = true
}

export async function logout() {
  await api.delete('/auth/session')
  session.user = null
}
