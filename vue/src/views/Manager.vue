<template>
  <div class="manager-container">
    <!--  头部  -->
    <div class="manager-header">
      <div class="manager-header-left">
        <img src="@/assets/imgs/logo.png" />
        <div class="title">Gym Panel</div>
      </div>

      <div class="manager-header-center">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/' }">Home</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: $route.path }">{{ $route.meta.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="manager-header-right">
        <el-dropdown placement="bottom">
          <div class="avatar">
            <img :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="" />
            <div>{{ user.name || 'Admin' }}</div>
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="goToPerson">Profile</el-dropdown-item>
            <el-dropdown-item @click.native="$router.push('/password')">Change password</el-dropdown-item>
            <el-dropdown-item @click.native="logout">Log out</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

    <!--  主体  -->
    <div class="manager-main">
      <!--  侧边栏  -->
      <div class="manager-main-left">
        <el-menu :default-openeds="['info', 'booking', 'user']" router style="border: none"
          :default-active="$route.path">
          <el-menu-item index="/home">
            <i class="el-icon-data-line"></i>
            <span slot="title">Dashboard</span>
          </el-menu-item>
          <el-submenu index="info">
            <template slot="title">
              <el-icon class="el-icon-coin"></el-icon>
              <span>Info</span>
            </template>
            <el-menu-item index="/notice">Notices</el-menu-item>
            <el-menu-item index="/course">Courses</el-menu-item>
            <el-menu-item index="/equipment">Equipments</el-menu-item>
            <el-menu-item index="/orders">Course Orders</el-menu-item>
            <el-menu-item index="/experience">Posts</el-menu-item>
          </el-submenu>

          <el-submenu index="booking">
            <template slot="title">
              <el-icon class="el-icon-bell"></el-icon>
              <span>Bookings</span>
            </template>
            <el-menu-item index="/eqReserve">Equipment Reservations</el-menu-item>
            <el-menu-item index="/reserve">Coach Reservations</el-menu-item>
          </el-submenu>

          <el-submenu index="user">
            <template slot="title">
              <el-icon class="el-icon-user"></el-icon>
              <span>Users</span>
            </template>
            <el-menu-item index="/admin">Admin List</el-menu-item>
            <el-menu-item index="/coach">Coach List</el-menu-item>
            <el-menu-item index="/user">Member List</el-menu-item>
          </el-submenu>
        </el-menu>
      </div>

      <!--  数据表格  -->
      <div class="manager-main-right">
        <router-view @update:user="updateUser" />
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: "Manager",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
    }
  },
  created() {
    if (!this.user.id) {
      this.$router.push('/login')
    }
  },
  methods: {
    updateUser() {
      this.user = JSON.parse(localStorage.getItem('xm-user') || '{}')   // 重新获取下用户的最新信息
    },
    goToPerson() {
      if (this.user.role === 'ADMIN') {
        this.$router.push('/adminPerson')
      }
    },
    logout() {
      localStorage.removeItem('xm-user')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
@import "@/assets/css/manager.css";
</style>