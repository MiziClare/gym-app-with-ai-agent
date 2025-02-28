<template>
  <div class="main-content">
    <div style="width: 80%; margin: 30px auto">
      <div style="text-align: center">
        <el-input placeholder="Enter the course name for enquiry..." style="width: 300px" v-model="name"></el-input>
        <el-button type="primary" plain style="margin-left: 10px" @click="loadCourse">Search</el-button>
        <el-button type="warning" plain style="margin-left: 10px" @click="reset">Reset</el-button>
      </div>
      <div style="margin-top: 30px">
        <el-row :gutter="30">
          <el-col :span="6" v-for="item in courseData" style="margin-bottom: 30px">
            <div class="course-image-container">
              <img :src="item.img" alt="" class="course-image"
                @click="$router.push('/front/courseDetail?id=' + item.id)">
            </div>
            <div style="font-size: 17px; margin-top: 10px; font-weight: bold; display: flex">
              <div style="flex: 1; color: #666666">{{ item.name }}</div>
              <div style="width: 70px; color: red; text-align: right">￡{{ item.price }}</div>
            </div>

            <div style="margin-top: 10px; color: #666666; display: flex">
              <div style="flex: 1; display: flex; align-items: center;">
                <img src="@/assets/imgs/icon-coach.png" alt="coach icon" class="coach-icon">
                <span>{{ item.coachName }}</span>
              </div>
              <div style="flex: 1;text-align: right">{{ item.time }}</div>
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
      courseData: [],
      name: null,
    }
  },
  mounted() {
    this.loadCourse()
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    loadCourse() {
      this.$request.get('/course/selectAll', {
        params: {
          name: this.name
        }
      }).then(res => {
        if (res.code === '200') {
          this.courseData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    reset() {
      this.name = null
      this.loadCourse()
    }
  }
}
</script>

<style scoped>
.coach-icon {
  width: 16px;
  height: 16px;
  margin-right: 5px;
  vertical-align: middle;
}

/* 课程图片容器样式 */
.course-image-container {
  width: 100%;
  padding-top: 66.67%;
  /* 保持3:2的宽高比 (2/3 = 66.67%) */
  position: relative;
  overflow: hidden;
  border-radius: 10px;
}

.course-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  /* 保持图片比例并填充容器 */
  cursor: pointer;
}
</style>