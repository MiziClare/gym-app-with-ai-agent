<template>
  <div class="main-content">
    <div class="container">
      <div class="glass-header">
        <h1>
          <img src="../../assets/imgs/icon-bag.png" alt="bag icon" class="title-icon">
          &#12288;Book a Coach
        </h1>
        <p>Professional and responsible personal trainers, one-on-one customized training.</p>
      </div>

      <div class="coach-grid">
        <div class="coach-row">
          <div class="coach-card" v-for="item in coachData" :key="item.id">
            <div class="glass-card">
              <div class="card-image">
                <img @click="$router.push('/front/coachDetail?id=' + item.id)" :src="item.avatar" :alt="item.name">
              </div>
              <div class="card-content">
                <h3>{{ item.name }}</h3>
                <div class="card-footer">
                  <span class="phone-wrapper">
                    <img src="../../assets/imgs/icon-phone.png" alt="phone icon" class="phone-icon">
                    <span>{{ item.phone }}</span>
                  </span>
                  <el-button class="glass-button" @click="reserveInit(item.id)">Book</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 预约信息窗口 -->
    <el-dialog title="Reservations" :visible.sync="fromVisible" width="40%" top="30vh" :close-on-click-modal="false"
      destroy-on-close custom-class="glass-dialog" append-to-body>
      <el-form label-width="100px" style="padding-right: 50px">
        <el-form-item prop="content" label="Notes">
          <el-input type="textarea" :rows="5" v-model="content" autocomplete="off" class="glass-input"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false" class="glass-button cancel-button">Cancel</el-button>
        <el-button type="primary" @click="submit" class="glass-button submit-button">Submit</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>

export default {

  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'), // Get user info from local storage
      coachData: [],
      content: null,
      coachId: null,
      fromVisible: false,
    }
  },
  mounted() {
    this.loadCoach()
    window.scrollTo(0, 0)  // Add this line to ensure the page scrolls to the top
  },
  // methods: The click event or other function definition area of this page
  methods: {
    loadCoach() {
      this.$request.get('/coach/selectAll').then(res => {
        if (res.code === '200') {
          this.coachData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    reserveInit(coachId) {
      if (this.user.role !== 'USER') {
        this.$message.warning('You are not a member and cannot make an appointment.')
        return
      }
      this.coachId = coachId
      this.fromVisible = true
    },
    submit() {
      let data = {
        userId: this.user.id,
        coachId: this.coachId,
        content: this.content,
      }
      this.$request.post('/reserve/add', data).then(res => {
        if (res.code === '200') {
          this.$message.success('The booking success! Please wait to be contacted by a coach.')
          this.coachId = null
          this.content = null
          this.fromVisible = false
        }
      })
    }
  }
}
</script>

<style scoped>
.main-content {
  min-height: 100vh;
  padding: 60px 0;
  position: relative;
  overflow: visible;
  font-family: 'Poppins', sans-serif;
  background: transparent;
  position: relative;
  z-index: 1;
}

/* Completely remove the blur layer */
.main-content::before {
  display: none;
  /* Remove the blur layer */
}

.container {
  width: 90%;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
  padding-left: 180px;
  /* Add left inner padding */
  margin-left: 0;
}

/* Add a white background to the glass card */
.glass-header,
.glass-card {
  background: rgba(255, 255, 255, 0.95);
  /* Change to a more opaque white background */
  /* Other styles remain unchanged */
}

.glass-header {
  background: rgba(245, 247, 250, 0.8);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-radius: 20px;
  padding: 40px;
  margin-bottom: 60px;
  border: 1px solid rgba(230, 235, 240, 0.8);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.05),
    inset 0 0 32px rgba(255, 255, 255, 0.8);
  text-align: center;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  transform: scale(1.05);
  cursor: pointer;
  font-family: 'Montserrat', sans-serif;
}

.glass-header:hover {
  transform: scale(1.05);
  box-shadow:
    0 15px 45px rgba(0, 0, 0, 0.1),
    inset 0 0 45px rgba(255, 255, 255, 0.9);
  background: rgba(245, 247, 250, 0.9);
}

.glass-header h1,
.glass-header p {
  transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.glass-header:hover h1 {
  transform: translateY(-2px);
}

.glass-header:hover p {
  transform: translateY(2px);
}

.glass-header h1 {
  color: #355476;
  font-size: 42px;
  margin: 0;
  margin-bottom: 20px;
  font-weight: 700;
  letter-spacing: 1px;
  font-family: var(--font-primary);
  font-weight: 600;
  letter-spacing: -0.02em;
  display: flex;
  align-items: center;
  justify-content: center;
}

.glass-header p {
  color: #667788;
  font-size: 18px;
  margin: 0;
  line-height: 1.6;
  font-family: var(--font-secondary);
  font-weight: 400;
  letter-spacing: 0.01em;
}

.glass-card {
  background: rgba(245, 247, 250, 0.8);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-radius: 30px;
  overflow: hidden;
  border: 1px solid rgba(230, 235, 240, 0.8);
  transition: all 0.3s ease;
  margin-bottom: 30px;
  box-shadow:
    0 10px 40px rgba(0, 0, 0, 0.05),
    inset 0 0 40px rgba(255, 255, 255, 0.8);
}

.glass-card:hover {
  transform: translateY(-10px);
  box-shadow:
    0 15px 50px rgba(0, 0, 0, 0.1),
    0 10px 20px rgba(0, 0, 0, 0.05);
}

.card-image {
  width: 100%;
  height: 300px;
  overflow: hidden;
  position: relative;
  border-radius: 25px 25px 0 0;
  cursor: pointer;
}

.card-image::after {
  pointer-events: none;
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.4) 100%);
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
  border-radius: 25px 25px 0 0;
  cursor: pointer;
}

.glass-card:hover .card-image img {
  transform: scale(1.1);
}

.card-content {
  padding: 25px;
  background: rgba(245, 247, 250, 0.9);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  border-radius: 0 0 30px 30px;
}

.card-content h3 {
  color: #355476;
  margin: 0;
  margin-bottom: 15px;
  font-size: 20px;
  font-weight: 600;
  font-family: var(--font-primary);
  font-weight: 600;
  letter-spacing: -0.01em;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #667788;
  font-size: 13px;
  padding: 0 5px;
}

.phone-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.phone-icon {
  width: 16px;
  height: 16px;
  filter: brightness(0.5);
  opacity: 0.8;
}

.glass-button {
  background: rgba(53, 84, 118, 0.1) !important;
  backdrop-filter: blur(15px) !important;
  -webkit-backdrop-filter: blur(15px) !important;
  border: 1px solid rgba(53, 84, 118, 0.2) !important;
  color: #355476 !important;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.5);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275) !important;
  padding: 8px 16px !important;
  font-weight: 500;
  letter-spacing: 0.02em;
  text-transform: uppercase;
  font-size: 0.5rem !important;
  border-radius: 20px !important;
  transform-origin: center;
  transform: scale(1.05);
  position: relative;
  overflow: hidden;
  margin-left: 10px;
}

.glass-button:hover {
  background: rgba(53, 84, 118, 0.2) !important;
  transform: scale(1.2);
  box-shadow:
    0 4px 15px rgba(0, 0, 0, 0.1),
    inset 0 0 20px rgba(255, 255, 255, 0.5);
}

.glass-button:active {
  transform: scale(0.95);
  transition: all 0.1s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.glass-button::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg,
      transparent,
      rgba(255, 255, 255, 0.5),
      transparent);
  transform: rotate(45deg);
  transition: all 0.3s ease;
  opacity: 0;
}

.glass-button:hover::after {
  opacity: 1;
  transform: rotate(45deg) translate(50%, 50%);
}

@media (max-width: 768px) {
  .glass-header {
    padding: 30px;
  }

  .glass-header h1 {
    font-size: 28px;
    letter-spacing: -0.01em;
  }

  .glass-header p {
    font-size: 15px;
    line-height: 1.5;
  }

  .card-content h3 {
    font-size: 20px;
  }
}

/* Optimize text rendering */
* {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

.title-icon {
  width: 32px;
  height: 32px;
  vertical-align: middle;
  margin-right: 2px;
  transform: translateY(-1px);
  filter: brightness(0.5);
  opacity: 0.9;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.glass-header:hover .title-icon {
  transform: translateY(-2px) scale(1.1);
}

/* Appointment window style */
:deep(.glass-dialog) {
  background: rgba(245, 247, 250, 0.9) !important;
  backdrop-filter: blur(30px) !important;
  -webkit-backdrop-filter: blur(30px) !important;
  border-radius: 20px !important;
  border: 1px solid rgba(230, 235, 240, 0.8) !important;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.05),
    inset 0 0 32px rgba(255, 255, 255, 0.8) !important;
  overflow: hidden;
  z-index: 10000 !important;
  /* Ensure the dialog is on top */
}

:deep(.glass-dialog .el-dialog__header) {
  background: rgba(245, 247, 250, 0.8);
  padding: 20px 30px;
  border-bottom: 1px solid rgba(230, 235, 240, 0.8);
}

:deep(.glass-dialog .el-dialog__title) {
  color: #355476;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: -0.01em;
  font-family: var(--font-primary);
}

:deep(.glass-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: #355476;
  font-size: 20px;
}

:deep(.glass-dialog .el-dialog__body) {
  padding: 30px;
  color: #355476;
}

:deep(.glass-dialog .el-form-item__label) {
  color: #355476;
  font-weight: 500;
}

:deep(.glass-input .el-textarea__inner) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(230, 235, 240, 0.8);
  border-radius: 10px;
  color: #355476;
  transition: all 0.3s ease;
}

:deep(.glass-input .el-textarea__inner:focus) {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(53, 84, 118, 0.3);
  box-shadow: 0 0 15px rgba(53, 84, 118, 0.1);
}

:deep(.glass-dialog .el-dialog__footer) {
  padding: 20px 30px;
  border-top: 1px solid rgba(230, 235, 240, 0.8);
  background: rgba(245, 247, 250, 0.8);
}

.cancel-button {
  background: rgba(230, 235, 240, 0.8) !important;
  color: #667788 !important;
}

.submit-button {
  background: rgba(53, 84, 118, 0.8) !important;
  border-color: rgba(53, 84, 118, 0.3) !important;
  color: white !important;
}

.submit-button:hover {
  background: rgba(53, 84, 118, 0.9) !important;
  border-color: rgba(53, 84, 118, 0.5) !important;
}

/* Add new responsive layout styles */
.coach-grid {
  width: 100%;
}

.coach-row {
  display: flex;
  flex-wrap: wrap;
  margin: 0 -15px;
}

.coach-card {
  width: 25%;
  padding: 0 15px;
  margin-bottom: 30px;
  box-sizing: border-box;
}

/* Responsive breakpoints */
@media (max-width: 1200px) {
  .coach-card {
    width: 33.333%;
  }
}

@media (max-width: 992px) {
  .coach-card {
    width: 50%;
  }
}

@media (max-width: 576px) {
  .coach-card {
    width: 100%;
  }
}
</style>
