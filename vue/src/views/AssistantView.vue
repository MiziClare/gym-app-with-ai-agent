<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api, messageOf, type ProposedAction } from '../api'

type ChatMessage = {
  from: 'assistant' | 'member'
  text: string
  action?: ProposedAction
  confirmed?: boolean
}

const messages = ref<ChatMessage[]>([{
  from: 'assistant',
  text: 'Hi! I can search the live schedule, show your bookings, and prepare a booking or cancellation for you.',
}])
const draft = ref('')
const sending = ref(false)
const feed = ref<HTMLElement>()

async function send() {
  const text = draft.value.trim()
  if (!text || sending.value) return
  messages.value.push({ from: 'member', text })
  draft.value = ''
  sending.value = true
  await scroll()
  try {
    const { data } = await api.post('/assistant/messages', { message: text })
    messages.value.push({
      from: 'assistant',
      text: data.reply || 'I found the information.',
      action: data.proposedAction,
    })
  } catch (error) {
    messages.value.push({ from: 'assistant', text: messageOf(error) })
  } finally {
    sending.value = false
    await scroll()
  }
}

async function confirm(message: ChatMessage) {
  if (!message.action) return
  try {
    await api.post(`/assistant/actions/${message.action.id}/confirm`)
    message.confirmed = true
    ElMessage.success('Action completed')
  } catch (error) {
    ElMessage.error(messageOf(error))
  }
}

async function scroll() {
  await nextTick()
  feed.value?.scrollTo({ top: feed.value.scrollHeight, behavior: 'smooth' })
}
</script>

<template>
  <section class="assistant-page">
    <aside>
      <p class="eyebrow">Gym Guide</p>
      <h1>Plan it.<br><em>Own it.</em></h1>
      <p>Ask in natural language. Gym Guide uses the live class and booking data—not invented availability.</p>
      <div class="safety-note">
        <strong>You stay in control</strong>
        <span>Bookings and cancellations require your explicit confirmation.</span>
      </div>
      <div class="examples">
        <small>TRY ASKING</small>
        <button type="button" @click="draft = 'What classes are available this week?'">Classes this week</button>
        <button type="button" @click="draft = 'Show my current bookings'">My bookings</button>
      </div>
    </aside>

    <div class="chat card">
      <header>
        <div class="guide-avatar">G</div>
        <div><strong>Gym Guide</strong><span>Grounded in your live schedule</span></div>
        <i></i>
      </header>
      <div ref="feed" class="feed" aria-live="polite">
        <div v-for="(message, index) in messages" :key="index" class="message" :class="message.from">
          <p>{{ message.text }}</p>
          <div v-if="message.action" class="action-card">
            <span>REVIEW ACTION</span>
            <strong>{{ message.action.summary }}</strong>
            <button
              class="button lime small"
              type="button"
              :disabled="message.confirmed"
              @click="confirm(message)"
            >
              {{ message.confirmed ? 'Completed' : 'Confirm action' }}
            </button>
          </div>
        </div>
        <div v-if="sending" class="typing" aria-label="Gym Guide is typing"><i></i><i></i><i></i></div>
      </div>
      <form @submit.prevent="send">
        <textarea v-model="draft" maxlength="1000" rows="1" placeholder="Ask about classes or your bookings…" @keydown.enter.exact.prevent="send"></textarea>
        <button class="button dark" type="submit" :disabled="!draft.trim() || sending" aria-label="Send message">↑</button>
      </form>
    </div>
  </section>
</template>

<style scoped>
.assistant-page { width: min(1180px, calc(100% - 48px)); min-height: calc(100vh - 74px); margin: 0 auto; padding: 54px 0 70px; display: grid; grid-template-columns: .7fr 1.3fr; gap: 70px; align-items: center; }
aside h1 { font-size: clamp(48px, 6vw, 78px); }
aside h1 em { color: #5f8444; font-family: Georgia, serif; font-weight: 400; }
aside > p:not(.eyebrow) { color: var(--muted); line-height: 1.7; }
.safety-note { margin-top: 30px; padding: 18px; display: flex; flex-direction: column; gap: 6px; color: #294d38; background: #eaf4dc; border-radius: 14px; }
.safety-note span { font-size: 12px; line-height: 1.5; }
.examples { display: flex; flex-direction: column; align-items: start; gap: 9px; margin-top: 30px; }
.examples small { color: var(--muted); font-size: 9px; font-weight: 800; letter-spacing: 1px; }
.examples button { padding: 0; border: 0; border-bottom: 1px solid #a4ada6; color: #58615b; background: transparent; cursor: pointer; }
.chat { height: 660px; display: grid; grid-template-rows: auto 1fr auto; overflow: hidden; }
.chat header { padding: 18px 21px; display: flex; align-items: center; gap: 12px; border-bottom: 1px solid var(--line); }
.guide-avatar { width: 38px; height: 38px; display: grid; place-items: center; color: var(--dark); background: var(--lime); border-radius: 12px; font-weight: 900; }
.chat header div:nth-child(2) { display: flex; flex-direction: column; }
.chat header span { color: var(--muted); font-size: 10px; margin-top: 3px; }
.chat header i { width: 8px; height: 8px; margin-left: auto; background: #5ab176; border-radius: 50%; }
.feed { padding: 25px; overflow-y: auto; display: flex; flex-direction: column; gap: 15px; background: #fafbf8; }
.message { max-width: 78%; }
.message p { margin: 0; padding: 13px 15px; line-height: 1.55; white-space: pre-wrap; background: white; border: 1px solid var(--line); border-radius: 4px 16px 16px; }
.message.member { align-self: flex-end; }
.message.member p { color: white; background: var(--dark); border: 0; border-radius: 16px 4px 16px 16px; }
.action-card { margin-top: 8px; padding: 15px; display: flex; flex-direction: column; align-items: start; gap: 9px; background: #edf7df; border: 1px solid #d7e9bf; border-radius: 13px; }
.action-card span { color: #6d805e; font-size: 9px; font-weight: 850; letter-spacing: 1px; }
.action-card strong { font-size: 13px; line-height: 1.4; }
.chat form { padding: 14px; display: grid; grid-template-columns: 1fr auto; gap: 10px; border-top: 1px solid var(--line); }
.chat textarea { resize: none; border: 1px solid var(--line); border-radius: 12px; padding: 12px 14px; outline: 0; }
.chat textarea:focus { border-color: #77937f; }
.typing { display: flex; gap: 4px; padding: 14px; }
.typing i { width: 5px; height: 5px; background: #8b968e; border-radius: 50%; animation: bounce .8s infinite alternate; }
.typing i:nth-child(2) { animation-delay: .15s; }
.typing i:nth-child(3) { animation-delay: .3s; }
@keyframes bounce { to { transform: translateY(-4px); } }
@media (max-width: 850px) {
  .assistant-page { grid-template-columns: 1fr; gap: 35px; }
  .chat { height: 620px; }
}
</style>
