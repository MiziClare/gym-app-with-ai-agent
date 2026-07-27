import axios from 'axios'

export type User = {
  id: number
  username: string
  displayName: string
  email: string
  role: 'ADMIN' | 'COACH' | 'MEMBER'
}

export type Session = {
  id: number
  courseId: number
  courseName: string
  coachId: number
  coachName: string
  startsAt: string
  endsAt: string
  capacity: number
  bookedCount: number
  status: string
}

export type Booking = {
  id: number
  sessionId: number
  courseName: string
  coachName: string
  startsAt: string
  endsAt: string
  status: string
  createdAt: string
}

export type ProposedAction = {
  id: string
  actionType: 'BOOK' | 'CANCEL'
  summary: string
  expiresAt: string
}

export const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
  withXSRFToken: false,
})

let csrfToken = ''

api.interceptors.request.use((config) => {
  if (csrfToken && config.method && !['get', 'head', 'options'].includes(config.method)) {
    config.headers['X-XSRF-TOKEN'] = csrfToken
  }
  return config
})

api.interceptors.response.use((response) => {
  if (response.data?.csrfToken) csrfToken = response.data.csrfToken
  return response
})

export function messageOf(error: unknown) {
  if (axios.isAxiosError(error)) {
    if (error.response?.status === 503) {
      return 'Gym Guide is disabled. Configure an AI provider to enable it.'
    }
    return error.response?.data?.detail ?? error.response?.data?.message ?? 'Request failed'
  }
  return 'Something went wrong'
}
