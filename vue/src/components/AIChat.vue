<template>
    <div class="ai-chat-container">
        <!-- Chat icon button -->
        <div class="chat-icon" @click="toggleChat" v-if="!chatVisible">
            <img src="../assets/imgs/icon-ai.png" alt="AI Gym Service" />
        </div>

        <!-- Chat window -->
        <div class="chat-window" v-if="chatVisible">
            <div class="chat-header">
                <span>AI Gym Service</span>
                <span class="close-btn" @click="toggleChat">&times;</span>
            </div>

            <div class="messages" ref="messagesContainer">
                <div v-for="(msg, index) in messages" :key="index" class="message"
                    :class="msg.sender === 'Customer Service' ? 'ai-message' : 'user-message'">
                    <div class="message-content">
                        <span class="sender">{{ msg.sender }}</span>
                        <span class="content">{{ msg.content }}</span>
                    </div>
                </div>
            </div>

            <div class="input-area">
                <input type="text" v-model="prompt" @keyup.enter="sendMessage"
                    placeholder="Please enter your question..." />
                <button @click="sendMessage">Send</button>
            </div>
        </div>
    </div>
</template>

<script>
import axios from 'axios';

export default {
    name: 'AiChat',
    data() {
        return {
            chatVisible: false,
            prompt: '',
            messages: [
                { sender: 'Customer Service', content: 'Hi! I’m the AI assistant for Gym Panel. I’m here to help with any questions about our platform and fitness advice. How can I assist you today?' }
            ]
        };
    },
    methods: {
        toggleChat() {
            this.chatVisible = !this.chatVisible;
            if (this.chatVisible) {
                // When the chat window is opened, focus on the input box
                this.$nextTick(() => {
                    const inputElement = this.$el.querySelector('input[type="text"]');
                    if (inputElement) inputElement.focus();
                });
            }
        },
        sendMessage() {
            if (!this.prompt) return;

            // Record the user's message
            this.messages.push({ sender: 'User', content: this.prompt });

            // Add a temporary "typing" message
            const typingIndex = this.messages.length;
            this.messages.push({ sender: 'Customer Service', content: 'Thinking...' });

            // Use the same method as customer.vue to send the request
            axios
                .get(`http://localhost:9090/chat/${encodeURIComponent(this.prompt)}`, {
                    timeout: 60000  // Increase the timeout to 60 seconds
                })
                .then(response => {
                    // Replace the "typing" message with the actual reply
                    this.messages.splice(typingIndex, 1, { sender: 'Customer Service', content: response.data });
                    // Scroll to the bottom
                    this.scrollToBottom();
                })
                .catch(error => {
                    console.error('Request error:', error);
                    // Replace the "typing" message with the error information
                    this.messages.splice(typingIndex, 1, {
                        sender: 'Customer Service',
                        content: 'System error, please try again later!' + (error.code === 'ECONNABORTED' ? '（Request timeout）' : '')
                    });
                    // Scroll to the bottom
                    this.scrollToBottom();
                });

            // Clear the input box
            this.prompt = '';
        },
        scrollToBottom() {
            this.$nextTick(() => {
                if (this.$refs.messagesContainer) {
                    this.$refs.messagesContainer.scrollTop = this.$refs.messagesContainer.scrollHeight;
                }
            });
        }
    }
};
</script>

<style scoped>
.ai-chat-container {
    position: fixed;
    right: 20px;
    bottom: 20px;
    z-index: 1000;
}

.chat-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background-color: #ffeaa7;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;
}

.chat-icon:hover {
    transform: scale(1.05);
}

.chat-icon img {
    width: 35px;
    height: 35px;
}

.chat-window {
    position: absolute;
    bottom: 70px;
    right: 0;
    width: 350px;
    height: 450px;
    border-radius: 10px;
    background-color: #fff;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.chat-header {
    background-color: #ffeaa7;
    color: black;
    padding: 10px 15px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.close-btn {
    font-size: 24px;
    cursor: pointer;
}

.messages {
    flex: 1;
    padding: 15px;
    overflow-y: auto;
    background-color: #f5f5f5;
}

.message {
    margin-bottom: 15px;
    display: flex;
    flex-direction: column;
}

.message-content {
    max-width: 80%;
    padding: 10px;
    border-radius: 10px;
    position: relative;
}

.user-message {
    align-items: flex-end;
}

.ai-message {
    align-items: flex-start;
}

.user-message .message-content {
    background-color: #dcf8c6;
}

.ai-message .message-content {
    background-color: white;
}

.sender {
    font-weight: bold;
    font-size: 12px;
    margin-bottom: 5px;
    display: block;
}

.content {
    word-break: break-word;
}

.input-area {
    display: flex;
    padding: 10px;
    border-top: 1px solid #eee;
}

input[type="text"] {
    flex: 1;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 20px;
    outline: none;
}

button {
    background-color: #ffeaa7;
    color: black;
    border: none;
    padding: 10px 15px;
    margin-left: 10px;
    border-radius: 20px;
    cursor: pointer;
    transition: background-color 0.3s;
}

button:hover {
    background-color: #fdcb6e;
}
</style>
