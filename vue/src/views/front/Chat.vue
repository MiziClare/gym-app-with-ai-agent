<template>
  <div style="width: 60%; margin: 10px auto">
    <!-- Back button -->
    <div class="back-button" @click="$router.push('/front/coach')">
      <i class="el-icon-arrow-left"></i> Back
    </div>

    <div style="display: flex; grid-gap: 20px">

      <!-- Start of Chat Group -->
      <div class="chat-card" style="width: 200px; min-height: 600px">
        <div v-if="chatGroupList.length > 0" v-for="item in chatGroupList" @click="loadChatList(item.chatUserId)"
          :key="item.id">
          <div style="position: relative" v-if="item.userId === user.id" class="chat-group"
            :class="{ 'chat-group-active': item.chatUserId === activeChatUserId }">
            <img :src="item.chatUserAvatar" style="width: 50px; height: 50px; border-radius: 50%" />
            <el-badge :value="item.chatNum" :max="99" v-if="item.chatNum > 0">
              <span style="color: #333">{{ item.chatUserName }}</span>
            </el-badge>
            <span v-else style="color: #333">{{ item.chatUserName }}</span>
          </div>
        </div>
        <div v-if="chatGroupList.length === 0" style="color: #666; padding: 20px 0; text-align: center">No chat object..
        </div>
      </div>
      <!-- End of Chat Group -->

      <!-- Start of Chat Messages -->
      <div class="chat-card" style="flex: 1; height: 600px">
        <div class="chat-text" style="height: 400px; border-bottom: 1px solid #ddd; overflow-y: auto;">
          <div v-for="item in chatList" :key="item.id" style="margin-bottom: 20px">

            <!-- Start of Left Chat Message -->
            <div v-if="item.userId !== user.id" style="display: flex; grid-gap: 10px">
              <img style="width: 50px; height: 50px; border-radius: 5px" :src="item.userAvatar || item.chatUserAvatar"
                alt="">
              <div style="max-width: 50%; width: fit-content">
                <div style="color: #666; margin-bottom: 5px">{{ item.userName || item.chatUserName || 'Coach' }}</div>
                <div style="background-color: #d1e7ff; padding: 10px; border-radius: 5px;" v-text="item.text"></div>
              </div>
            </div>
            <!-- End of Left Chat Message -->

            <!-- Start of Right Chat Message -->
            <div v-if="item.userId === user.id" style="display: flex; justify-content: right; grid-gap: 10px">
              <div style="max-width: 50%; width: fit-content">
                <div style="color: #666; margin-bottom: 5px; text-align: right">{{ user.name || 'Me' }}</div>
                <div style="background-color: #a3eecb; padding: 10px; border-radius: 5px;" v-text="item.text"></div>
              </div>
              <img style="width: 50px; height: 50px; border-radius: 5px" :src="user.avatar" alt="">
            </div>
            <!-- End of Right Chat Message -->

          </div>
          <div style="height: 0"><a id="chat-text-end" style="outline: none">&nbsp</a></div>
        </div>
        <!-- End of Chat Messages -->

        <!-- Start of Chat Input Box -->
        <div style="height: 200px">
          <textarea v-model="form.text" style="height: 160px; width: 100%; padding: 20px; border: none;
             border-bottom: 1px solid #ddd; outline: none"></textarea>
          <div style="text-align: right; padding-right: 10px">
            <el-button type="primary" size="mini" @click="send">Send</el-button>
          </div>
        </div>
        <!-- End of Chat Input Box -->

      </div>

    </div>
  </div>
</template>

<script>
let socket

export default {
  name: "Chat",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      chatGroupList: [],
      activeChatUserId: 0,
      form: {},
      chatList: [],
      socketConnected: false
    }
  },
  mounted() {
    window.scrollTo(0, 0)
    window.addEventListener('beforeunload', this.handleBeforeUnload)
  },
  created() {
    if (this.user && this.user.id) {
      this.init()
      this.loadChatGroup()

      const lastChatState = JSON.parse(localStorage.getItem('lastChatState') || '{}')
      if (lastChatState.activeChatUserId) {
        this.loadChatList(lastChatState.activeChatUserId)
      }
    } else {
      this.$message.warning('Please login first')
      this.$router.push('/login')
    }
  },
  beforeDestroy() {
    window.removeEventListener('beforeunload', this.handleBeforeUnload)
    this.closeSocket()
  },
  methods: {
    send() {
      if (!this.activeChatUserId) {
        this.$message.warning('Please select a chat object')
        return
      }
      if (!this.form.text) {
        this.$message.warning('Please enter a message content')
        return
      }

      if (!this.socketConnected) {
        console.log("WebSocket not connected, attempting to reconnect...")
        this.init()
      }

      this.form.chatUserId = this.activeChatUserId
      this.form.userId = this.user.id
      this.form.role = 'USER'
      this.$request.post('/chatInfo/add', this.form).then(res => {
        this.form = {}
        this.loadChatList(this.activeChatUserId)

        if (socket && this.socketConnected) {
          try {
            socket.send('Chat message')
          } catch (e) {
            console.error("Error sending WebSocket message", e)
            this.$message.error("Failed to send message through WebSocket")
            this.init()
          }
        } else {
          console.warn("WebSocket not connected, message not sent")
        }
      })
    },
    loadChatList(chatUserId) {
      this.form = {}
      this.activeChatUserId = chatUserId

      console.log(this.activeChatUserId)
      this.$request.put('/chatInfo/updateRead/' + chatUserId).then(res => {

        this.loadChatGroup()
      })
      this.$request.get('/chatInfo/selectUserChat/' + chatUserId).then(res => {
        this.chatList = res.data || []
        this.$nextTick(() => {
          const chatTextElement = document.querySelector('.chat-text')
          if (chatTextElement) {
            chatTextElement.scrollTop = chatTextElement.scrollHeight
          }
        })
      })
    },
    loadChatGroup() {
      this.$request.get('/chatGroup/selectUserGroup').then(res => {
        this.chatGroupList = res.data || []
      })
    },
    init() {
      if (typeof (WebSocket) == "undefined") {
        console.log("Your browser does not support WebSocket")
        this.$message.warning("Your browser does not support WebSocket, chat function may be limited")
        return
      }

      this.closeSocket()

      try {
        if (!this.user || !this.user.id) {
          console.error("User ID is missing, cannot establish WebSocket connection")
          return
        }

        let socketUrl = "ws://localhost:9090/chatServer/" + this.user.id

        socket = new WebSocket(socketUrl)

        socket.onopen = () => {
          console.log("WebSocket is open")
          this.socketConnected = true
        }

        socket.onmessage = (msg) => {
          console.log("Received message:", msg.data)
          this.loadChatGroup()
          if (this.activeChatUserId) {
            this.loadChatList(this.activeChatUserId)
          }
        }

        socket.onclose = () => {
          console.log("WebSocket is closed")
          this.socketConnected = false
        }

        socket.onerror = (error) => {
          console.log("WebSocket error", error)
          this.socketConnected = false
          this.$message.error("Chat connection error, please try again later")
        }
      } catch (e) {
        console.error("WebSocket initialization error", e)
        this.$message.error("Chat initialization failed, please try again later")
      }
    },
    closeSocket() {
      if (socket) {
        try {
          socket.close()
        } catch (e) {
          console.error("Error closing WebSocket", e)
        }
        socket = null
      }
      this.socketConnected = false
    },
    handleBeforeUnload() {
      this.closeSocket()
      localStorage.setItem('lastChatState', JSON.stringify({
        activeChatUserId: this.activeChatUserId,
        path: '/front/chat'
      }))
    }
  }
}
</script>

<style scoped>
.chat-card {
  background-color: #fff;
  border-radius: 5px;
  box-shadow: 0 2px 5px 0 rgba(0, 0, 0, 0.1);
}

.chat-group {
  padding: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  grid-gap: 10px;
  border-bottom: 1px solid #ddd
}

.chat-group-active {
  background-color: #d1e7ff;
}

.chat-text {
  padding: 20px;
}

.chat-text::-webkit-scrollbar {
  width: 10px;
}

.chat-text::-webkit-scrollbar-thumb {
  border-radius: 8px;
  background-color: #d1e7ff;
}

:deep(.el-badge__content.is-fixed) {
  right: 2px;
}

.back-button {
  display: inline-flex;
  align-items: center;
  padding: 8px 15px;
  background-color: #f5f7fa;
  border-radius: 5px;
  color: #355476;
  font-size: 14px;
  cursor: pointer;
  margin-bottom: 15px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  border: 1px solid #e6ebf0;
}

.back-button:hover {
  background-color: #e6ebf0;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.back-button i {
  margin-right: 5px;
}
</style>