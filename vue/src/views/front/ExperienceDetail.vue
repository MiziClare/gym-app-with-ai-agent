<template>
  <div class="main-content">
    <div style="width: 65%; margin: 20px auto">
      <div style="text-align: center; margin-top: 30px; font-size: 17px; font-weight: bold">{{ experienceData.name }}
      </div>
      <div style="text-align: center; margin-top: 10px; color: #666666">
        <span>发布人：{{ experienceData.userName }}</span>
        <span style="margin-left: 20px">发布时间：{{ experienceData.time }}</span>
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
    }
  }
}
</script>
<style>
p {
  color: #666666;
}
</style>