<template>
  <div class="main-content">
    <div style="width: 65%; margin: 20px auto; position: relative;">
      <div class="back-button" @click="goBack">
        <i class="el-icon-arrow-left"></i> Back
      </div>
      <div
        style="text-align: center; margin-top: 30px; font-size: 17px; font-weight: bold; font-family: 'Montserrat', sans-serif;">
        {{ experienceData.name }}
      </div>
      <div style="text-align: center; margin-top: 10px; color: #666666">
        <span>Author: {{ experienceData.userName }}</span>
        <span style="margin-left: 20px">Time: {{ experienceData.time }}</span>
      </div>
      <div style="margin-top: 30px" v-html="experienceData.content" class="w-e-text w-e-text-container"></div>
    </div>
  </div>
</template>

<script>
import E from 'wangeditor'
export default {

  data() {
    return {
      experienceId: this.$route.query.id,
      experienceData: {}
    }
  },
  mounted() {
    this.loadExperience()
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    loadExperience() {
      this.$request.get('/experience/selectById/' + this.experienceId).then(res => {
        if (res.code === '200') {
          this.experienceData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    goBack() {
      this.$router.push('/front/experience');
    }
  }
}
</script>
<style>
.main-content {
  font-family: 'Dangrek', sans-serif;
}

p {
  color: #666666;
}

.back-button {
  position: absolute;
  top: -20px;
  left: 0;
  background-color: rgba(53, 84, 118, 0.1);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: inline-flex;
  align-items: center;
  margin-bottom: 40px;
  border: 1px solid rgba(53, 84, 118, 0.2);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.back-button:hover {
  transform: scale(1.1);
  background-color: white;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.back-button i {
  margin-right: 5px;
}
</style>