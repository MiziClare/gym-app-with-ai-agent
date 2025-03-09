<template>
  <div class="chat-window">
    <div class="messages">
      <div v-for="(msg, index) in messages" :key="index" class="message">
        <span class="sender">{{ msg.sender }}: </span>
        <span class="content">{{ msg.content }}</span>
      </div>
    </div>
    <div class="input-area">
      <input type="text" v-model="prompt" @keyup.enter="sendMessage" placeholder="请输入您的问题..." />
      <button @click="sendMessage">发送</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      prompt: '',
      messages: []  // store the chat messages
    };
  },
  methods: {
    sendMessage() {
      if (!this.prompt) return;

      // Record the user's message
      this.messages.push({ sender: '用户', content: this.prompt });

      // Modify the request method, use the complete backend URL
      axios
        .get(`http://localhost:9090/chat/${encodeURIComponent(this.prompt)}`)
        .then(response => {
          // Record the message returned by the backend
          this.messages.push({ sender: '客服', content: response.data });
        })
        .catch(error => {
          console.error('请求错误:', error);
          this.messages.push({ sender: '客服', content: '系统错误，请稍后重试！' });
        });

      // Clear the input box
      this.prompt = '';
    }
  }
};
</script>

<style scoped>
.chat-window {
  width: 400px;
  margin: 20px auto;
  border: 1px solid #ddd;
  padding: 10px;
  border-radius: 4px;
}

.messages {
  height: 300px;
  overflow-y: auto;
  margin-bottom: 10px;
}

.message {
  margin: 5px 0;
}

.input-area {
  display: flex;
}

input[type="text"] {
  flex: 1;
  padding: 5px;
  font-size: 14px;
}

button {
  padding: 5px 10px;
  font-size: 14px;
  margin-left: 5px;
}
</style>
