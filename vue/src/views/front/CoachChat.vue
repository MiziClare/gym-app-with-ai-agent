<template>
  <div style="width: 60%; margin: 10px auto">

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
            <!-- Modify the judgment logic to ensure correct display of user messages -->
            <div v-if="item.userId !== user.id" style="display: flex; grid-gap: 10px">
              <img style="width: 50px; height: 50px; border-radius: 5px" :src="item.chatUserAvatar || item.userAvatar"
                alt="">
              <div style="max-width: 50%; width: fit-content">
                <div style="color: #666; margin-bottom: 5px">{{ item.chatUserName || ' ' }}</div>
                <div style="background-color: #d1e7ff; padding: 10px; border-radius: 5px;" v-text="item.text"></div>
              </div>
            </div>
            <!-- End of Left Chat Message -->

            <!-- Start of Right Chat Message -->
            <!-- Modify the judgment logic to ensure correct display of coach messages -->
            <div v-if="item.userId === user.id" style="display: flex; justify-content: right; grid-gap: 10px">
              <div style="max-width: 50%; width: fit-content">
                <div style="color: #666; margin-bottom: 5px; text-align: right">{{ user.name || ' ' }}</div>
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
      chatList: []
    }
  },
  mounted() {
    window.scrollTo(0, 0)
  },
  created() {
    this.init()
    this.loadChatGroup()
  },
  methods: {
    send() {
      if (!this.activeChatUserId) {
        this.$message.warning('Please select a chat user')
        return
      }
      if (!this.form.text) {
        this.$message.warning('Please enter a message content')
        return
      }

      // create a new object to avoid modifying the existing object
      const messageData = {
        chatUserId: this.activeChatUserId,
        userId: this.user.id,
        text: this.form.text,
        role: 'COACH'
      }

      console.log('Send message data:', messageData)

      this.$request.post('/chatInfo/add', messageData).then(res => {
        console.log('Send message response:', res)
        this.form = {}
        this.loadChatList(this.activeChatUserId)

        // send socket message
        socket.send('Chat message')
      }).catch(err => {
        console.error('Send message failed:', err)
      })
    },
    loadChatList(chatUserId) {
      this.form = {}
      this.activeChatUserId = chatUserId

      console.log("Load chat list, chatUserId:", chatUserId, "Current user:", this.user)
      // update message read status
      this.$request.put('/chatInfo/updateRead/' + chatUserId)
        .then(res => {
          console.log("Message marked as read:", res)
          this.loadChatGroup()
        })
        .catch(err => {
          console.error("Update message status failed:", err)
        })

      this.$request.get('/chatInfo/selectUserChat/' + chatUserId)
        .then(res => {
          console.log("Get chat history:", res)
          this.chatList = res.data || []
          this.$nextTick(() => {
            const chatTextElement = document.querySelector('.chat-text')
            if (chatTextElement) {
              chatTextElement.scrollTop = chatTextElement.scrollHeight
            }
          })
        })
        .catch(err => {
          console.error("Get chat history failed:", err)
        })
    },
    loadChatGroup() {
      this.$request.get('/chatGroup/selectUserGroup').then(res => {
        this.chatGroupList = res.data || []
        console.log('Chat group data:', this.chatGroupList)
      })
    },
    init() {
      if (typeof (WebSocket) == "undefined") {
        console.log("Your browser does not support WebSocket")
      } else {
        console.log("Your browser supports WebSocket")
        let socketUrl = "ws://localhost:9090/chatServer/" + this.user.id
        console.log("Attempting to connect to WebSocket:", socketUrl)

        if (socket != null) {
          socket.close()
          socket = null
        }

        try {
          // open a websocket service
          socket = new WebSocket(socketUrl)

          // open event
          socket.onopen = function () {
            console.log("WebSocket is open")
          }

          // browser side receive message
          socket.onmessage = (msg) => {
            console.log("Received WebSocket message:", msg)
            this.loadChatGroup()
            if (this.activeChatUserId) {
              this.loadChatList(this.activeChatUserId)
            }
          }

          //关闭事件
          socket.onclose = function () {
            console.log("WebSocket is closed")
          }

          //发生了错误事件
          socket.onerror = function (err) {
            console.error("WebSocket error:", err)
          }
        } catch (e) {
          console.error("WebSocket connection failed:", e)
        }
      }
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

/deep/ .el-badge__content.is-fixed {
  right: 2px;
}
</style>