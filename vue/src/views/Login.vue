<template>
  <div class="login-page">
    <div class="container">
      <div class="shell">
        <div id="img-box">
          <img src="@/assets/imgs/gym-login.png" alt="" />
        </div>
        <form @submit.prevent>
          <div id="form-body">
            <div id="welcome-lines">
              <div id="w-line-1">Gym Panel</div>
              <div id="w-line-2">Welcome Back</div>
            </div>
            <el-form :model="form" :rules="rules" ref="formRef">
              <div id="input-area">
                <div class="f-inp">
                  <input type="text" placeholder="Username" v-model="form.username" />
                </div>
                <div class="f-inp">
                  <input type="password" placeholder="Password" v-model="form.password" />
                </div>
                <div class="f-inp">
                  <select id="role-select" v-model="form.role">
                    <option value="" disabled selected>Your Role</option>
                    <option value="ADMIN">Admin</option>
                    <option value="COACH">Coach</option>
                    <option value="USER">Member</option>
                  </select>
                </div>
              </div>
              <div class="gdpr-consent">
                <input type="checkbox" id="gdpr-checkbox" required v-model="form.gdprConsent" />
                <label for="gdpr-checkbox">
                  I agree to the <a href="#" class="policy-link">Privacy Policy</a> and
                  <a href="#" class="policy-link">Terms of Service</a>
                </label>
              </div>
              <div id="submit-button-cvr">
                <button type="button" id="submit-button" @click="login">LOGIN</button>
                <div id="register-link">
                  <span>Don't have an account? </span>
                  <router-link to="/register" class="register">Register</router-link>
                </div>
              </div>
            </el-form>
          </div>
        </form>
      </div>
    </div>
  </div>

</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      dialogVisible: true,
      form: { role: '' },
      rules: {
        username: [
          { required: true, message: 'Please enter your username', trigger: 'blur' },
        ],
        password: [
          { required: true, message: 'Please enter your password', trigger: 'blur' },
        ]
      }
    }
  },
  created() {

  },
  methods: {
    login() {
      this.$refs['formRef'].validate((valid) => {
        if (valid) {
          // 验证通过
          this.$request.post('/login', this.form).then(res => {
            if (res.code === '200') {
              localStorage.setItem("xm-user", JSON.stringify(res.data))  // 存储用户数据

              if (res.data.role === 'ADMIN') {
                this.$router.push('/home') // 跳转后台主页
              } else {
                location.href = '/front/home'  // 跳转前台主页
              }
              this.$message.success('Login Success')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    }
  }
}
</script>

<style>
/* 移除全局样式，改为更具体的选择器 */
.login-page {
  margin: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
  /* 添加定位上下文 */
}
</style>

<style scoped>
.main-content {
  font-family: 'Inter', sans-serif;
}

/* 修改容器样式 */
.container {
  position: fixed;
  /* 改为fixed定位 */
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  transform: scale(1.07);
  padding: 0 !important;
}

/* 添加一个伪元素作为背景 */
.container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("@/assets/imgs/bg.jpg");
  background-size: cover;
  background-position: center;
  transform: scale(1.07);
  filter: blur(3px) brightness(0.9);
  /* 添加模糊和亮度调整 */
  z-index: -1;
  /* 确保背景在内容之后 */
  padding: 0 !important;
}

/* 添加这些全局样式 */
:deep(html),
:deep(body),
:deep(#app) {
  margin: 0;
  padding: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  padding: 0 !important;
}

a {
  color: #2a60c9;
}

/* 为输入框添加圆角样式 */
:deep(.el-input__inner) {
  border-radius: 8px !important;
}

:deep(.el-button) {
  border-radius: 8px !important;
}

:deep(.el-select .el-input__inner) {
  border-radius: 8px !important;
}

/* 以下为尝试添加的样式 */
* {
  padding: 0;
  margin: 0;
  outline: none;
}

body {
  margin: 0px !important;
  background: linear-gradient(45deg, #fbda61, #ff5acd);
  display: flex;
  justify-content: center;
  text-align: center;
  align-items: center;
  height: 100vh;
  padding: 0 !important;
}

.shell,
form {
  position: relative;
  padding: 0 !important;
  margin: 0 !important;
}

.shell {
  display: flex;
  justify-content: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  /* 添加更柔和的阴影 */
  border-radius: 15px;
  overflow: hidden;
  padding: 0 !important;
  margin: 0 !important;
}

form {
  width: 562px;
  height: 475px;
  background-color: #fff;
  box-shadow: none;
  /* 移除阴影 */
  border-radius: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  padding: 0 !important;
}

/* 修改图片容器样式 */
#img-box {
  width: 330px;
  height: 520px;
  border-radius: 15px 15px 0 15px;
}

#img-box img {
  height: 100%;
  border-radius: 15px 15px 0 15px;
}

#form-body {
  width: 300px;
  height: 10%;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
}

#welcome-lines {
  width: 100%;
  text-align: center;
  line-height: 1;
}

#w-line-1 {
  color: #7f7f7f;
  font-size: 40px;
  margin-top: 200px;
  font-family: 'Dangrek', sans-serif;
}

#w-line-2 {
  color: #9c9c9c;
  font-size: 20px;
  margin-top: 20px;
  margin-bottom: 20px;
  font-family: 'Dangrek', sans-serif;
}

#input-area {
  width: 60%;
  margin-top: 40px;
  margin-left: 35px;
}

.f-inp {
  padding: 13px 25px;
  border: 2px solid #6e6d6d;
  line-height: 1;
  border-radius: 20px;
  margin-bottom: 15px;
}

.f-inp input {
  width: 100%;
  font-size: 14px;
  padding: 0;
  margin: 0;
  border: 0;
}

.f-inp input::placeholder {
  color: #b9b9b9;
}

#submit-button-cvr {
  margin-top: 5px;
  margin-bottom: 15px;
}

#submit-button {
  display: block;
  width: 100%;
  color: #fff;
  font-size: 14px;
  margin: 0;
  padding: 14px 40px;
  border: 0;
  background-color: #f5506e;
  border-radius: 25px;
  line-height: 1;
  cursor: pointer;
  margin-top: 20px;
  margin-bottom: 15px;
  transition: transform 0.2s ease-in-out;
  /* 添加过渡效果 */
}

#submit-button:hover {
  transform: scale(1.2);
  /* 鼠标悬停时放大5% */
}

#role-select {
  width: 100%;
  font-size: 14px;
  border: none;
  background: transparent;
  color: #b9b9b9;
  cursor: pointer;
  font-family: inherit;
}

#role-select option:first-child {
  color: #b9b9b9;
}

#role-select option {
  color: #333;
  padding: 10px;
  background-color: #fff;
}

#role-select:focus {
  outline: none;
}

#role-select::-ms-expand {
  display: none;
}

#role-select option[disabled] {
  color: #b9b9b9;
}

#role-select:not([value=""]) {
  color: #333;
}

#register-link {
  text-align: center;
  font-size: 14px;
  color: #7f7f7f;
  margin-bottom: 150px;
}

.register {
  color: #f5506e;
  text-decoration: none;
  margin-left: 5px;
  font-weight: 500;
}

.register:hover {
  text-decoration: underline;
}

/* 新添加的GDPR相关样式 */
.gdpr-consent {
  width: 100%;
  margin: 10px 0;
  font-size: 13px;
  color: #7f7f7f;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-left: 260px;
}

.gdpr-consent input[type="checkbox"] {
  margin-top: 3px;
  cursor: pointer;
  transition: transform 0.2s ease-in-out;
  /* 添加过渡效果 */
}

.gdpr-consent input[type="checkbox"]:hover {
  transform: scale(1.5);
  /* 鼠标悬停时放大 */
}

.gdpr-consent label {
  line-height: 1.4;
  cursor: pointer;
}

.policy-link {
  color: #f5506e;
  text-decoration: none;
}

.policy-link:hover {
  text-decoration: underline;
}
</style>
