<template>
  <div class="main-content">
    <div style="width: 70%; margin: 10px auto; position: relative;">
      <div class="back-button" @click="$router.push('/front/course')">
        <i class="el-icon-arrow-left"></i> Back
      </div>
      <div style="margin-top: 50px">
        <el-row :gutter="50">
          <el-col :span="6">
            <div class="img-container">
              <img :src="courseData.img" alt="" class="course-img">
            </div>
          </el-col>
          <el-col :span="18">
            <div style="margin-bottom: 20px; font-size: 18px; font-weight: bold">Course: {{
              courseData.name
            }}</div>
            <div style="margin-bottom: 20px; font-size: 18px">Coach: {{ courseData.coachName }}</div>
            <div style="margin-bottom: 20px; font-size: 18px; color: #333333">Duration: {{ courseData.time }}</div>
            <div style="margin-bottom: 20px; font-size: 18px; color: #222222">Price: <span style="color: red">￡{{
              courseData.price }}</span></div>
            <div style="margin-top: 30px"><el-button type="primary" round @click="buy">Buy Now</el-button></div>
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
    window.scrollTo(0, 0);
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
.main-content {
  font-family: 'Montserrat', sans-serif;
}

.back-button {
  position: absolute;
  top: -55px;
  left: 0;
  background-color: rgba(53, 84, 118, 0.1);
  color: #355476;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: inline-flex;
  align-items: center;
  margin-bottom: 20px;
  border: 1px solid rgba(53, 84, 118, 0.2);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.back-button:hover {
  transform: scale(1.1);
  background-color: rgba(53, 84, 118, 0.2);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.back-button i {
  margin-right: 5px;
}

p {
  color: #666666;
}

.img-container {
  width: 100%;
  height: 0;
  padding-top: 66.67%;
  position: relative;
  margin-bottom: 10px;
  margin-top: 10px;
  border-radius: 5px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.img-container:hover {
  transform: translateY(-10px) scale(1.03);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
}

.course-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.img-container:hover .course-img {
  transform: scale(1.1);
}
</style>