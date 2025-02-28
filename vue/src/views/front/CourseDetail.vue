<template>
  <div class="main-content">
    <div style="width: 70%; margin: 30px auto">
      <div>
        <el-row :gutter="50">
          <el-col :span="6">
            <img :src="courseData.img" alt=""
              style="width: 100%; height: 220px; border-radius: 5px; object-fit: cover;">
          </el-col>
          <el-col :span="18">
            <div style="margin-bottom: 20px; font-size: 18px; font-weight: bold">Course: {{
              courseData.name
              }}</div>
            <div style="margin-bottom: 20px; font-size: 18px">Coach: {{ courseData.coachName }}</div>
            <div style="margin-bottom: 20px; font-size: 18px; color: #333333">Duration: {{ courseData.time }}</div>
            <div style="margin-bottom: 20px; font-size: 18px; color: #222222">Price: <span style="color: red">￡{{
              courseData.price }}</span></div>
            <div style="margin-top: 30px"><el-button type="primary" @click="buy">Buy Now</el-button></div>
          </el-col>
        </el-row>
      </div>
      <div style="margin-top: 20px" v-html="courseData.content" class="w-e-text w-e-text-container"></div>
    </div>
  </div>
</template>

<script>
import E from 'wangeditor'
export default {

  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      courseId: this.$route.query.id,
      courseData: {}
    }
  },
  mounted() {
    this.$request.get('/course/selectById/' + this.courseId).then(res => {
      if (res.code === '200') {
        this.courseData = res.data
      } else {
        this.$message.error(res.msg)
      }
    })
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    buy() {
      let data = {
        courseId: this.courseId,
        userId: this.user.id,
        price: this.courseData.price
      }
      this.$request.post('/orders/add', data).then(res => {
        if (res.code === '200') {
          this.$message.success('Purchase Successful!')
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