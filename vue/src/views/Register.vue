<template>
  <div class="container">
    <div class="shell">
      <form @submit.prevent>
        <div id="form-body">
          <div id="welcome-lines">
            <div id="w-line-1">Gym Panel</div>
            <div id="w-line-2">Create Account</div>
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
                <input type="password" placeholder="Confirm Password" v-model="form.confirmPass" />
              </div>
              <div class="f-inp">
                <select id="role-select" v-model="form.role">
                  <option value="" disabled selected>Your Role</option>
                  <option value="COACH">Coach</option>
                  <option value="USER">Member</option>
                </select>
              </div>
            </div>
            <div id="submit-button-cvr">
              <button type="button" id="submit-button" @click="register">REGISTER</button>
              <div id="register-link">
                <span>Already have an account? </span>
                <router-link to="/login" class="login">Login</router-link>
              </div>
            </div>
          </el-form>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: "Register",
  data() {
    // 验证码校验
    const validatePassword = (rule, confirmPass, callback) => {
      if (confirmPass === '') {
        callback(new Error('Please enter the password again'))
      } else if (confirmPass !== this.form.password) {
        callback(new Error('The two passwords do not match'))
      } else {
        callback()
      }
    }
    return {
      form: {
        username: '',
        password: '',
        confirmPass: '',
        role: ''  // 确保初始值为空字符串
        },
      rules: {
        username: [
          { required: true, message: 'Please enter the username', trigger: 'blur' },
        ],
        password: [
          { required: true, message: 'Please enter the password', trigger: 'blur' },
        ],
        confirmPass: [
          { validator: validatePassword, trigger: 'blur' }
        ]
      }
    }
  },
  created() {

  },
  methods: {
    register() {
      this.$refs['formRef'].validate((valid) => {
        if (valid) {
          // 验证通过
          this.$request.post('/register', this.form).then(res => {
            if (res.code === '200') {
              this.$router.push('/login')  // 跳转登录页面
              this.$message.success('Register successfully')
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

<style scoped>
/* 保持原有的container和背景样式 */
.container {
  position: fixed;
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
}

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
  z-index: -1;
}

/* 新增现代UI样式 */
.shell {
  display: flex;
  justify-content: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-radius: 15px;
  overflow: hidden;
  background-color: white;
}

form {
  width: 562px;
  padding: 40px;
  background-color: #fff;
  border-radius: 15px;
}

#form-body {
  width: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  margin: 0 auto;
}

#welcome-lines {
  width: 100%;
  text-align: center;
  line-height: 1;
  margin-bottom: 40px;
}

#w-line-1 {
  color: #7f7f7f;
  font-size: 40px;
}

#w-line-2 {
  color: #9c9c9c;
  font-size: 20px;
  margin-top: 20px;
}

#input-area {
  width: 100%;
  margin-bottom: 20px;
}

.f-inp {
  padding: 13px 25px;
  border: 2px solid #6e6d6d;
  line-height: 1;
  border-radius: 20px;
  margin-bottom: 15px;
}

.f-inp input, .f-inp select {
  width: 100%;
  font-size: 14px;
  padding: 0;
  margin: 0;
  border: 0;
  background: transparent;
  outline: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

.f-inp input::placeholder {
  color: #b9b9b9;
}

#role-select {
  width: 100%;
  font-size: 14px;
  color: #b9b9b9;
  cursor: pointer;
  background: transparent;
  border: none;
  outline: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

#role-select option {
  color: #333;
}

#role-select option:first-child {
  color: #b9b9b9;
}

#role-select:focus {
  color: #333;
}

#submit-button {
  display: block;
  width: 100%;
  color: #fff;
  font-size: 14px;
  padding: 14px 40px;
  border: 0;
  background-color: #f5506e;
  border-radius: 25px;
  cursor: pointer;
  margin: 20px 0;
  transition: transform 0.2s ease-in-out;
}

#submit-button:hover {
  transform: scale(1.05);
}

#register-link {
  text-align: center;
  font-size: 14px;
  color: #7f7f7f;
}

.login {
  color: #f5506e;
  text-decoration: none;
  margin-left: 5px;
  font-weight: 500;
}

.login:hover {
  text-decoration: underline;
}

/* 添加以下样式来处理自动填充的背景色 */
.f-inp input:-webkit-autofill,
.f-inp input:-webkit-autofill:hover,
.f-inp input:-webkit-autofill:focus {
  -webkit-box-shadow: 0 0 0 30px white inset !important;
  -webkit-text-fill-color: inherit !important;
}
</style>