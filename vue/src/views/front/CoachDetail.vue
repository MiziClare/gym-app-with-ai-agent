<template>
    <div class="main-content">
      <div style="width: 65%; margin: 20px auto">
        <div style="margin-top: 20px; font-size: 17px; font-weight: bold; color: #666666">
          About Coach {{ coachData.name }}
        </div>
        <div style="margin-top: 20px" v-html="coachData.content" class="w-e-text w-e-text-container"></div>
      </div>
    </div>
  </template>
  
  <script>
  import E from 'wangeditor'
  export default {
  
    data() {
      return {
        coachId: this.$route.query.id,
        coachData: {}
      }
    },
    mounted() {
      this.loadCoach()
    },
    // methods：本页面所有的点击事件或者其他函数定义区
    methods: {
      loadCoach() {
        this.$request.get('/coach/selectById/' + this.coachId).then(res => {
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
  <style>
    p {
      color: #333333;
    }
  </style>