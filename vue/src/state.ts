import { reactive } from 'vue'
import { api, type User } from './api'

export const session = reactive<{ user: User | null; ready: boolean }>({
  user: null,
  ready: false,
})

export const unread = reactive({ forum: 0, chat: 0, connections: 0 })

export async function loadUnread() {
  if (!session.user || session.user.role === 'ADMIN') return
  const [forum, chat, connections] = await Promise.all([
    api.get<{ count: number }>('/forum-notifications/unread-count'),
    api.get<{ count: number }>('/messages/unread-count'),
    api.get<{ count: number }>('/coach-connections/pending-count'),
  ])
  unread.forum = forum.data.count
  unread.chat = chat.data.count
  unread.connections = connections.data.count
}

export async function loadSession() {
  const { data } = await api.get('/auth/session')
  session.user = data.user
  session.ready = true
}

export async function logout() {
  await api.delete('/auth/session')
  session.user = null
  Object.assign(unread, { forum: 0, chat: 0, connections: 0 })
}
