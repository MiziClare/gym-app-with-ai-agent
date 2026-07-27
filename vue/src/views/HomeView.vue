<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api, type Session } from '../api'

const upcoming = ref<Session[]>([])

onMounted(async () => {
  const { data } = await api.get<Session[]>('/sessions')
  upcoming.value = data.slice(0, 3)
})

function shortDate(value: string) {
  return new Intl.DateTimeFormat('en-CA', {
    weekday: 'short', month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit',
  }).format(new Date(value))
}
</script>

<template>
  <section class="hero">
    <div class="hero-copy">
      <p class="eyebrow">Training, minus the friction</p>
      <h1>Move with<br><em>purpose.</em></h1>
      <p class="lead">Explore classes, see live availability and reserve your next session in a few calm clicks.</p>
      <div class="hero-actions">
        <RouterLink class="button lime" to="/classes">Explore classes <span aria-hidden="true">→</span></RouterLink>
        <RouterLink class="text-link" to="/assistant">Ask Gym Guide</RouterLink>
      </div>
      <div class="trust-row">
        <span><strong>Live</strong> capacity</span>
        <span><strong>Safe</strong> confirmation</span>
        <span><strong>Simple</strong> cancellation</span>
      </div>
    </div>
    <div class="hero-art" aria-label="Abstract training illustration">
      <div class="disc disc-one"></div>
      <div class="disc disc-two"></div>
      <div class="art-card">
        <span>UP NEXT</span>
        <strong>{{ upcoming[0]?.courseName ?? 'Find your next class' }}</strong>
        <small>{{ upcoming[0] ? shortDate(upcoming[0].startsAt) : 'Fresh sessions added weekly' }}</small>
      </div>
    </div>
  </section>

  <section class="page next-section">
    <div class="section-title">
      <div>
        <p class="eyebrow">This week</p>
        <h2>Find your rhythm.</h2>
      </div>
      <RouterLink class="text-link" to="/classes">View full schedule →</RouterLink>
    </div>
    <div class="session-strip">
      <article v-for="item in upcoming" :key="item.id" class="mini-session card">
        <div class="session-number">{{ String(item.id).padStart(2, '0') }}</div>
        <div>
          <p>{{ shortDate(item.startsAt) }}</p>
          <h3>{{ item.courseName }}</h3>
          <small>with {{ item.coachName }}</small>
        </div>
        <span class="availability">{{ item.capacity - item.bookedCount }} spots</span>
      </article>
      <div v-if="!upcoming.length" class="skeleton"></div>
    </div>
  </section>

  <section class="guide-band">
    <div>
      <p class="eyebrow">Built-in assistant</p>
      <h2>Plan naturally.<br>Confirm deliberately.</h2>
    </div>
    <div>
      <p>Gym Guide can search the real schedule and prepare a booking or cancellation. Nothing changes until you approve the action.</p>
      <RouterLink class="button dark" to="/assistant">Open Gym Guide</RouterLink>
    </div>
  </section>
</template>

<style scoped>
.hero { min-height: 650px; display: grid; grid-template-columns: 1.02fr .98fr; background: var(--dark); color: white; }
.hero-copy { padding: 100px max(40px, calc((100vw - 1180px) / 2)); padding-right: 64px; }
.hero .eyebrow { color: #b9d8c5; }
.hero h1 { font-size: clamp(60px, 8vw, 112px); line-height: .83; letter-spacing: -7px; }
.hero h1 em { color: var(--lime); font-family: Georgia, serif; font-weight: 400; }
.hero .lead { color: #c2d0c7; }
.hero-actions { display: flex; align-items: center; gap: 25px; margin-top: 34px; }
.text-link { font-size: 14px; font-weight: 750; border-bottom: 1px solid currentColor; padding-bottom: 3px; }
.trust-row { display: flex; gap: 30px; margin-top: 72px; color: #9db1a4; font-size: 12px; }
.trust-row span { display: flex; flex-direction: column; gap: 3px; }
.trust-row strong { color: white; font-size: 14px; }
.hero-art { overflow: hidden; position: relative; min-height: 590px; background: #dfe9d5; }
.disc { position: absolute; border-radius: 50%; border: 60px solid; }
.disc-one { width: 510px; height: 510px; top: 55px; right: -80px; border-color: #a8d94c; }
.disc-two { width: 280px; height: 280px; bottom: -90px; left: -50px; border-color: #173d2a; }
.hero-art::after { content: ""; position: absolute; inset: 0; background: repeating-linear-gradient(120deg, transparent 0 30px, rgba(255,255,255,.14) 31px); }
.art-card { position: absolute; z-index: 2; left: 11%; bottom: 12%; width: min(340px, 78%); padding: 28px; display: flex; flex-direction: column; background: rgba(255,255,255,.92); color: var(--ink); border-radius: 20px; box-shadow: 0 24px 70px rgba(23,61,42,.2); }
.art-card span { color: #698071; font-size: 10px; font-weight: 800; letter-spacing: 1.6px; }
.art-card strong { margin-top: 12px; font-size: 24px; letter-spacing: -.6px; }
.art-card small { margin-top: 6px; color: var(--muted); }
.next-section { padding-top: 90px; }
.section-title { display: flex; justify-content: space-between; align-items: end; margin-bottom: 34px; }
.session-strip { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.mini-session { min-height: 180px; display: grid; grid-template-columns: auto 1fr; gap: 19px; padding: 24px; position: relative; }
.mini-session p { margin: 3px 0 12px; color: var(--muted); font-size: 12px; }
.mini-session small { display: block; margin-top: 7px; color: var(--muted); }
.session-number { color: #9bac9f; font-weight: 800; font-size: 12px; }
.availability { position: absolute; right: 20px; bottom: 18px; color: #2c6244; background: #eef8df; border-radius: 999px; padding: 6px 9px; font-size: 11px; font-weight: 750; }
.guide-band { padding: 85px max(24px, calc((100vw - 1180px) / 2)); display: grid; grid-template-columns: 1fr 1fr; gap: 10%; align-items: end; color: var(--dark); background: var(--lime); }
.guide-band > div:last-child { max-width: 470px; }
.guide-band p:not(.eyebrow) { color: #3d5a47; line-height: 1.7; margin: 0 0 25px; }
@media (max-width: 800px) {
  .hero { grid-template-columns: 1fr; }
  .hero-copy { padding: 70px 28px; }
  .hero-art { min-height: 370px; }
  .trust-row { margin-top: 50px; }
  .session-strip { grid-template-columns: 1fr; }
  .section-title, .guide-band { grid-template-columns: 1fr; align-items: start; gap: 30px; }
}
</style>
