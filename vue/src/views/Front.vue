<template>
  <div>
    <div class="front-notice"><i class="el-icon-bell" style="margin-right: 2px"></i>Notice: {{ top }}</div>
    <!--Header-->
    <div class="front-header">
      <div class="front-header-left">
        <img src="@/assets/imgs/logo.png" alt="">
        <div class="title" @click="$router.push('/front/home')">Gym Panel</div>
      </div>
      <div class="front-header-center">
        <div class="front-header-nav" style="background-color: #ffeaa7;">
          <el-menu :default-active="$route.path" mode="horizontal" router>
            <el-menu-item index="/front/home"><img src="@/assets/imgs/icon-home.png" alt=""
                style="width: 20px; margin-right: 5px; display: inline-block; vertical-align: middle;">Home</el-menu-item>
            <el-menu-item index="/front/card" v-if="user.role !== 'COACH'"><img src="@/assets/imgs/icon-card.png" alt=""
                style="width: 20px; margin-right: 5px; display: inline-block; vertical-align: middle;">E-Card</el-menu-item>
            <el-menu-item index="/front/course"><img src="@/assets/imgs/icon-courses.png" alt=""
                style="width: 20px; margin-right: 5px; display: inline-block; vertical-align: middle;">Courses</el-menu-item>
            <el-menu-item index="/front/equipment">
              <img src="@/assets/imgs/icon-treadmill.png" alt=""
                style="width: 20px; margin-right: 5px; display: inline-block; vertical-align: middle;">Equipments
            </el-menu-item>
            <el-menu-item index="/front/coach"><img src="@/assets/imgs/icon-coach.png" alt=""
                style="width: 20px; margin-right: 5px; display: inline-block; vertical-align: middle;">Coaches</el-menu-item>
            <el-menu-item index="/front/experience"><img src="@/assets/imgs/icon-post.png" alt=""
                style="width: 20px; margin-right: 5px; display: inline-block; vertical-align: middle;">Post</el-menu-item>
          </el-menu>
        </div>
      </div>
      <div class="front-header-right">
        <div v-if="!user.username">
          <el-button @click="$router.push('/login')">Login</el-button>
          <el-button @click="$router.push('/register')">Register</el-button>
        </div>
        <div v-else class="dropdown-menu">
          <el-dropdown>
            <div class="front-header-dropdown">
              <img :src="user.avatar" alt="">
              <div style="margin-left: 10px">
                <span>{{ user.name }}</span><i class="el-icon-arrow-down" style="margin-left: 5px"></i>
              </div>
            </div>
            <el-dropdown-menu slot="dropdown" class="dropdown-menu">
              <el-dropdown-item>
                <div style="text-decoration: none" @click="person">Account</div>
              </el-dropdown-item>
              <el-dropdown-item>
                <div style="text-decoration: none" @click="$router.push('/front/orders')">Orders</div>
              </el-dropdown-item>
              <el-dropdown-item>
                <div style="text-decoration: none" @click="$router.push('/front/reserve')">Reservations</div>
              </el-dropdown-item>
              <el-dropdown-item>
                <div style="text-decoration: none" @click="$router.push('/front/myExperience')">My Post</div>
              </el-dropdown-item>
              <el-dropdown-item>
                <div style="text-decoration: none" @click="logout">Logout</div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </div>
    <!--Main Content-->
    <div class="main-body">
      <!-- Sidebar -->
      <div class="sidebar-container">
        <Sidebar />
      </div>
      <!-- Main content area -->
      <div class="main-content">
        <router-view ref="child" @update:user="updateUser" />
      </div>
    </div>
    <!-- Add AI chat component -->
    <AIChat />
    <!-- Add VR component -->
    <Vr />
  </div>

</template>

<script>
import Sidebar from '@/components/Sidebar.vue'
import AIChat from '@/components/AIChat.vue'
import Vr from '@/components/Vr.vue'

export default {
  name: "FrontLayout",
  components: {
    Sidebar,
    AIChat,
    Vr
  },
  data() {
    return {
      top: '',
      notice: [],
      user: JSON.parse(localStorage.getItem("xm-user") || '{}'),
    }
  },

  mounted() {
    this.loadNotice()
    // Listen for login status changes
    window.addEventListener('storage', this.handleStorageChange)
    // Ensure initial load gets the latest user information
    this.updateUser()
  },
  beforeDestroy() {
    // Remove event listener when component is destroyed
    window.removeEventListener('storage', this.handleStorageChange)
  },
  methods: {
    handleStorageChange(e) {
      // When the user information in localStorage changes, update it
      if (e.key === 'xm-user') {
        this.updateUser()
      }
    },
    loadNotice() {
      this.$request.get('/notice/selectAll').then(res => {
        this.notice = res.data
        let i = 0
        if (this.notice && this.notice.length) {
          this.top = this.notice[0].content
          setInterval(() => {
            this.top = this.notice[i].content
            i++
            if (i === this.notice.length) {
              i = 0
            }
          }, 3500)
        }
      })
    },
    updateUser() {
      this.user = JSON.parse(localStorage.getItem('xm-user') || '{}')   // Re-get the latest user information
    },
    // Logout
    logout() {
      localStorage.removeItem("xm-user");
      this.$router.push("/login");
    },
    person() {
      if (this.user.role === 'COACH') {
        this.$router.push('/front/coachPerson')
      } else {
        this.$router.push('/front/person')
      }
    }
  }

}
</script>

<style scoped>
@import "@/assets/css/front.css";
</style>