<template>
  <div class="shell">
    <ul class="nav">
      <li class="active" id="logo">
        <a href="#">
          <div class="icon">
            <div class="imageBox">
              <img :src="user.avatar" alt="" />
            </div>
          </div>
          <div class="text">{{ user.name || 'User' }}</div>
        </a>
      </li>
      <li>
        <a href="#home">
          <div class="icon">
            <i class="iconfont icon-cangku"></i>
          </div>
          <div class="text" @click="person">Account</div>
        </a>
      </li>
      <li>
        <a href="#theme">
          <div class="icon">
            <i class="iconfont icon-zhuti_tiaosepan"></i>
          </div>
          <div class="text" @click="$router.push('/front/orders')">Orders</div>
        </a>
      </li>
      <li>
        <a href="#wallet">
          <div class="icon">
            <i class="iconfont icon-qianbao"></i>
          </div>
          <div class="text" @click="$router.push('/front/reserve')">Reservations</div>
        </a>
      </li>
      <li>
        <a href="#picture">
          <div class="icon">
            <i class="iconfont icon-tupian"></i>
          </div>
          <div class="text" @click="logout">Logout</div>
        </a>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'NavBar',
  data() {
    return {
      user: JSON.parse(localStorage.getItem("xm-user") || '{}'),
    }
  },
  mounted() {
    let nav = document.querySelectorAll(".nav li");
    nav.forEach((item) => item.addEventListener("click", this.activeLink));
  },
  methods: {
    activeLink(event) {
      let nav = document.querySelectorAll(".nav li");
      nav.forEach((item) => item.classList.remove("active"));
      event.currentTarget.classList.add("active");
    },
    // 其他接口
    updateUser() {
      this.user = JSON.parse(localStorage.getItem('xm-user') || '{}')
    },
    // 退出登录
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
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  list-style: none;
  text-decoration: none;
}

.shell {
  position: fixed;
  width: 100px;
  height: 100%;
  background: #ffeaa7;
  z-index: 9999;
  transition: width 0.5s;
  padding-left: 10px;
  overflow: hidden;
  margin-top: calc(80px + 2vh);
}

.shell:hover {
  width: 300px;
}

.imageBox {
  position: relative;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
}

.imageBox img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.shell ul {
  position: relative;
  height: 100vh;
}

.shell ul li {
  position: relative;
  padding: 5px;
}

.active {
  background: white;
  /* 选中项周围颜色 */
  border-top-left-radius: 50px;
  border-bottom-left-radius: 50px;
}

.active::before {
  content: "";
  position: absolute;
  top: -30px;
  right: 0;
  width: 30px;
  height: 30px;
  border-bottom-right-radius: 25px;
  box-shadow: 5px 5px 0 5px white;
  /* 选中项周围阴影 */
  background: transparent;
}

.active::after {
  content: "";
  position: absolute;
  bottom: -30px;
  right: 0;
  width: 30px;
  height: 30px;
  border-top-right-radius: 25px;
  box-shadow: 5px -5px 0 5px white;
  /* 阴影 */
  background: transparent;
}

#logo {
  margin: 40px 0 100px 0;
}

.shell ul li a {
  position: relative;
  display: flex;
  white-space: nowrap;
}

.icon {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-width: 60px;
  padding-left: 10px;
  height: 70px;
  color: #333;
  /* 文字颜色 */
  transition: 0.5s;
  color: white;
  /* 图标颜色 */
  margin-right: 20px;
}

.icon i {
  font-size: 30px;
  z-index: 999;
}

.text {
  position: relative;
  height: 70px;
  display: flex;
  align-items: center;
  font-size: 20px;
  color: #333;
  padding-left: 15px;
  text-transform: uppercase;
  letter-spacing: 2px;
  transition: 0.5s;
}

.shell ul li:hover a .icon,
.shell ul li:hover a .text {
  color: #ffa117;
}

.active a .icon::before {
  content: "";
  position: absolute;
  inset: 5px;
  width: 60px;
  background: #fff;
  border-radius: 50%;
  transition: 0.5s;
  border: 5px solid #fdcb6e;
  box-sizing: border-box;
}
</style>
