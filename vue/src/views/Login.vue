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
                  I agree to the <a href="/privacy-policy.html" target="_blank" class="policy-link">Privacy Policy</a>
                  and
                  <a href="/terms-of-service.html" target="_blank" class="policy-link">Terms of Service</a>
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
      form: {
        role: '',
        gdprConsent: false  // Add GDPR consent status
      },
      rules: {
        username: [
          { required: true, message: 'Please enter your username', trigger: 'blur' },
        ],
        password: [
          { required: true, message: 'Please enter your password', trigger: 'blur' },
        ],
        gdprConsent: [
          {
            validator: (rule, value, callback) => {
              if (!value) {
                callback(new Error('Please agree to the privacy policy and terms of service'));
              } else {
                callback();
              }
            }, trigger: 'change'
          }
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
          // Validation passed
          if (!this.form.gdprConsent) {
            this.$message.error('Please agree to the privacy policy and terms of service');
            return;
          }

          this.$request.post('/login', this.form).then(res => {
            if (res.code === '200') {
              localStorage.setItem("xm-user", JSON.stringify(res.data))  // Store user data

              if (res.data.role === 'ADMIN') {
                this.$router.push('/home') // Redirect to the backend home page
              } else {
                location.href = '/front/home'  // Redirect to the front page
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
/* Remove global styles and use more specific selectors */
.login-page {
  margin: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
  /* Add positioning context */
}
</style>

<style scoped>
.main-content {
  font-family: 'Inter', sans-serif;
}

/* Modify container styles */
.container {
  position: fixed;
  /* Change to fixed positioning */
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

/* Add a pseudo-element as a background */
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
  /* Add blur and brightness adjustment */
  z-index: -1;
  /* Ensure the background is behind the content */
  padding: 0 !important;
}

/* Add these global styles */
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

/* Add rounded styles to input fields */
:deep(.el-input__inner) {
  border-radius: 8px !important;
}

:deep(.el-button) {
  border-radius: 8px !important;
}

:deep(.el-select .el-input__inner) {
  border-radius: 8px !important;
}

/* The following are styles added for testing */
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
  /* Add a softer shadow */
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
  /* Remove the shadow */
  border-radius: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  padding: 0 !important;
}

/* Modify image container styles */
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
  /* Add transition effect */
}

#submit-button:hover {
  transform: scale(1.2);
  /* When the mouse is hovered, increase by 5% */
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

/* Newly added GDPR-related styles */
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
  /* Add transition effect */
}

.gdpr-consent input[type="checkbox"]:hover {
  transform: scale(1.5);
  /* When the mouse is hovered, increase */
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
