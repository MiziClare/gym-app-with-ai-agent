<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, messageOf } from '../api'
import { loadSession, session } from '../state'

const route = useRoute()
const router = useRouter()
const mode = ref<'login' | 'register'>('login')
const busy = ref(false)
const error = ref('')
const form = reactive({ username: 'member', password: 'GymDemo123!', displayName: '', email: '' })
const heading = computed(() => mode.value === 'login' ? 'Welcome Back!' : 'Create Account')

async function submit() {
  error.value = ''
  busy.value = true
  try {
    if (mode.value === 'register') await api.post('/auth/register', form)
    await api.post('/auth/session', { username: form.username, password: form.password })
    await loadSession()
    const fallback = session.user?.role === 'ADMIN' ? '/home' : '/front/home'
    await router.push(String(route.query.next || fallback))
  } catch (caught) {
    error.value = messageOf(caught)
  } finally {
    busy.value = false
  }
}

function useDemo(username: string) {
  form.username = username
  form.password = 'GymDemo123!'
}

function switchMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login'
  error.value = ''
  if (mode.value === 'register') Object.assign(form, { username: '', password: '', displayName: '', email: '' })
  else useDemo('member')
}
</script>

<template>
  <main class="login-page">
    <section class="login-card">
      <div class="login-art">
        <img src="../assets/imgs/gym-login.png" alt="Gym training">
      </div>
      <form @submit.prevent="submit">
        <img class="login-logo" src="../assets/imgs/logo.png" alt="Gym Panel">
        <h1>{{ heading }}</h1>
        <p>{{ mode === 'login' ? 'Sign in to manage your fitness journey.' : 'Register a new member account.' }}</p>
        <label>Username<input v-model.trim="form.username" autocomplete="username" minlength="3" maxlength="64" required></label>
        <label v-if="mode === 'register'">Display name<input v-model.trim="form.displayName" autocomplete="name" maxlength="100" required></label>
        <label v-if="mode === 'register'">Email<input v-model.trim="form.email" type="email" autocomplete="email" maxlength="255" required></label>
        <label>Password<input v-model="form.password" type="password" :autocomplete="mode === 'login' ? 'current-password' : 'new-password'" minlength="10" maxlength="100" required></label>
        <p v-if="error" class="form-error" role="alert">{{ error }}</p>
        <button class="login-submit" type="submit" :disabled="busy">{{ busy ? 'Please wait…' : mode === 'login' ? 'Login' : 'Register' }}</button>
        <div v-if="mode === 'login'" class="demo-logins">
          <span>Demo accounts</span>
          <button type="button" @click="useDemo('member')">Member</button>
          <button type="button" @click="useDemo('coach')">Coach</button>
          <button type="button" @click="useDemo('admin')">Admin</button>
        </div>
        <button class="login-switch" type="button" @click="switchMode">{{ mode === 'login' ? 'No account? Register' : 'Already registered? Login' }}</button>
      </form>
    </section>
  </main>
</template>
