<template>
  <div class="shell">
    <ul class="nav">
      <li class="active" id="logo">
        <span @click="$router.push('/front/home')">
          <div class="icon">
            <div class="imageBox">
              <img :src="user.avatar" alt="" />
            </div>
          </div>
          <div class="text">{{ user.name || 'User' }}</div>
        </span>
      </li>
      <li>
        <span @click="person">
          <div class="icon">
            <img src="@/assets/imgs/icon-account.png" alt="" />
          </div>
          <div class="text">Account</div>
        </span>
      </li>
      <li>
        <span @click="$router.push('/front/orders')">
          <div class="icon">
            <img src="@/assets/imgs/icon-orders.png" alt="" />
          </div>
          <div class="text">Orders</div>
        </span>
      </li>
      <li>
        <span @click="$router.push('/front/reserve')">
          <div class="icon">
            <img src="@/assets/imgs/icon-reservations.png" alt="" />
          </div>
          <div class="text">Reservations</div>
        </span>
      </li>
      <!-- my post -->
      <li>
        <span @click="$router.push('/front/myExperience')">
          <div class="icon">
            <img src="@/assets/imgs/icon-mypost.png" alt="" />
          </div>
          <div class="text">My Post</div>
        </span>
      </li>
      <li>
        <span @click="logout">
          <div class="icon">
            <img src="@/assets/imgs/icon-logout.png" alt="" />
          </div>
          <div class="text">Logout</div>
        </span>
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
  font-family: 'Inter', sans-serif;
}

.shell {
  position: fixed;
  width: 80px;
  height: calc(100% - 150px);
  background: #ffeaa7;
  z-index: 9999;
  transition: width 0.5s;
  padding-left: 10px;
  overflow: hidden;
  margin-top: 50px;
  margin-left: 25px;
  border-radius: 30px;
}

.shell:hover {
  width: 250px;
  border-radius: 30px;
}

.imageBox {
  position: relative;
  width: 35px;
  height: 35px;
  border-radius: 50%;
  overflow: hidden;
}

.imageBox img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Set the style for the icon image, but exclude the avatar */
.icon img:not(.imageBox img) {
  width: 25px;
  height: 25px;
  object-fit: contain;
  z-index: 999;
}

.shell ul {
  position: relative;
  height: calc(100% - 20px);
  padding-bottom: 20px;
}

.shell ul li {
  position: relative;
  padding: 5px;
  margin-bottom: -10px;
}

.active {
  background: white;
  /* The color of the selected item */
  border-top-left-radius: 30px;
  border-bottom-left-radius: 30px;
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
  /* The shadow around the selected item */
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
  /* The shadow */
  background: transparent;
}

#logo {
  margin: 40px 0 60px 0;
}

.shell ul li span {
  position: relative;
  display: flex;
  white-space: nowrap;
  cursor: pointer;
}

.icon {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-width: 50px;
  padding-left: 10px;
  height: 60px;
  color: #333;
  /* The text color */
  transition: 0.5s;
  color: white;
  /* The icon color */
  margin-right: 10px;
}

.text {
  position: relative;
  height: 60px;
  display: flex;
  align-items: center;
  font-size: 15px;
  color: #333;
  padding-left: 15px;
  text-transform: uppercase;
  letter-spacing: 2px;
  transition: 0.5s;
}

.shell ul li:hover span .icon,
.shell ul li:hover span .text {
  color: #ffa117;
}

.active span .icon::before {
  content: "";
  position: absolute;
  inset: 5px;
  width: 50px;
  height: 50px;
  background: #fff;
  border-radius: 50%;
  transition: 0.5s;
  border: 5px solid #ffeaa7;
  box-sizing: border-box;
}
</style>
