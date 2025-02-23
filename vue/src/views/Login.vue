<template>
  <div class="container">
    <div style="width: 400px; padding: 30px; background-color: white; border-radius: 15px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);">
      <div style="text-align: center; font-size: 20px; margin-bottom: 20px; color: #333">Welcome</div>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input prefix-icon="el-icon-user" placeholder="Enter your username" v-model="form.username"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input prefix-icon="el-icon-lock" placeholder="Enter your password" show-password  v-model="form.password"></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-select v-model="form.role" placeholder="Your Role" style="width: 100%">
            <el-option label="Admin" value="ADMIN"></el-option>
            <el-option label="Coach" value="COACH"></el-option>
            <el-option label="Member" value="USER"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button style="width: 100%; background-color: #333; border-color: #333; color: white" @click="login">Login</el-button>
        </el-form-item>
        
        <div style="display: flex; align-items: center">
          <div style="flex: 1"></div>
          <div style="flex: 1; text-align: right">
            Don't have an account? <a href="/register">Register</a>
          </div>
        </div>
      </el-form>
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
              this.$router.push('/')  // 跳转主页
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
/* 移除默认的边距 - 注意这里不使用 scoped */
html, body, #app {
  margin: 0;
  padding: 0;
  width: 100vw;  /* 使用vw单位 */
  height: 100vh; /* 使用vh单位 */
  overflow: hidden;
  position: relative; /* 添加定位上下文 */
}
</style>

<style scoped>
.container {
  position: absolute; /* 改用absolute定位 */
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
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
  transform: scale(1.01);
  filter: blur(3px) brightness(0.9);  /* 添加模糊和亮度调整 */
  z-index: -1;  /* 确保背景在内容之后 */
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
</style>