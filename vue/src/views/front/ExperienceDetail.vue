<template>
  <div class="main-content">
    <div style="width: 65%; margin: 20px auto; position: relative;">
      <div class="back-button">
        <el-button icon="el-icon-arrow-left" size="small" @click="goBack">
          Back</el-button>
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
  top: 0;
  left: 0;
}
</style>