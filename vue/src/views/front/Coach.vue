<template>
  <div class="main-content">
    <div class="container">
      <div class="glass-header">
        <h1>
          <img src="../../assets/imgs/icon-bag.png" alt="bag icon" class="title-icon">
          Book a Coach
        </h1>
        <p>Professional and responsible personal trainers, one-on-one customized training.</p>
      </div>
      
      <div class="coach-grid">
        <el-row :gutter="30">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in coachData" :key="item.id">
            <div class="glass-card">
              <div class="card-image">
                <img :src="item.avatar" :alt="item.name">
              </div>
              <div class="card-content">
                <h3>{{item.name}}</h3>
                <div class="card-footer">
                  <span>{{ item.phone }}</span>
                  <el-button class="glass-button">Book Now</el-button>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script>

export default {

  data() {
    return {
      coachData: []
    }
  },
  mounted() {
    this.loadCoach()
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    loadCoach() {
      this.$request.get('/coach/selectAll').then(res => {
        if (res.code === '200') {
          this.coachData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    }
  }
}
</script>

<style scoped>
/* 添加全局字体变量 */
:root {
  --font-primary: 'Poppins', -apple-system, BlinkMacSystemFont, sans-serif;
  --font-secondary: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.main-content {
  min-height: 100vh;
  padding: 60px 0;
  position: relative;
  overflow: hidden;
  font-family: var(--font-secondary);
  background: url('../../assets/imgs/bg-coach.png') center center/cover no-repeat fixed;
  position: relative;
}

.main-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(10px);
  z-index: 0;
}

.container {
  width: 90%;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
}

.glass-header {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-radius: 20px;
  padding: 40px;
  margin-bottom: 60px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 0 32px rgba(255, 255, 255, 0.05);
  text-align: center;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  transform: scale(1.05);
  cursor: pointer;
}

.glass-header:hover {
  transform: scale(1.05);
  box-shadow: 
    0 15px 45px rgba(0, 0, 0, 0.2),
    inset 0 0 45px rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.15);
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
  color: #e2e8f0;
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
  color: #a0aec0;
  font-size: 18px;
  margin: 0;
  line-height: 1.6;
  font-family: var(--font-secondary);
  font-weight: 400;
  letter-spacing: 0.01em;
}

.glass-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-radius: 30px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  margin-bottom: 30px;
  box-shadow: 
    0 10px 40px rgba(0, 0, 0, 0.1),
    inset 0 0 40px rgba(255, 255, 255, 0.05);
}

.glass-card:hover {
  transform: translateY(-10px);
  box-shadow: 
    0 15px 50px rgba(0, 0, 0, 0.2),
    0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-image {
  width: 100%;
  height: 300px;
  overflow: hidden;
  position: relative;
  border-radius: 25px 25px 0 0;
}

.card-image::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0) 0%, rgba(0,0,0,0.6) 100%);
  
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
  border-radius: 25px 25px 0 0;
}

.glass-card:hover .card-image img {
  transform: scale(1.1);
}

.card-content {
  padding: 25px;
  background: rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  border-radius: 0 0 30px 30px;
}

.card-content h3 {
  color: #e2e8f0;
  margin: 0;
  margin-bottom: 15px;
  font-size: 24px;
  font-weight: 600;
  font-family: var(--font-primary);
  font-weight: 600;
  letter-spacing: -0.01em;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #a0aec0;
  font-size: 13px;
  padding: 0 5px;
}

.glass-button {
  background: rgba(255, 255, 255, 0.15) !important;
  backdrop-filter: blur(15px) !important;
  -webkit-backdrop-filter: blur(15px) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: white !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275) !important;
  padding: 8px 16px !important;
  font-weight: 500;
  letter-spacing: 0.02em;
  text-transform: uppercase;
  font-size: 0.75rem !important;
  border-radius: 20px !important;
  transform-origin: center;
  transform: scale(1.05);
  position: relative;
  overflow: hidden;
}

.glass-button:hover {
  background: rgba(255, 255, 255, 0.25) !important;
  transform: scale(1.2);
  box-shadow: 
    0 4px 15px rgba(0, 0, 0, 0.2),
    inset 0 0 20px rgba(255, 255, 255, 0.1);
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
  background: linear-gradient(
    45deg,
    transparent,
    rgba(255, 255, 255, 0.1),
    transparent
  );
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

/* 优化文字渲染 */
* {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

.title-icon {
  width: 32px;  /* 调整图标大小 */
  height: 32px;
  vertical-align: middle;
  margin-right: 12px;  /* 调整图标和文字的间距 */
  transform: translateY(-2px);  /* 微调图标位置 */
  filter: brightness(0) invert(1);  /* 将图标改为白色 */
  opacity: 0.9;  /* 稍微调整透明度 */
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.glass-header:hover .title-icon {
  transform: translateY(-2px) scale(1.1);  /* 悬停时图标稍微放大 */
}
</style>
