<template>
  <div class="main-content">
    <div style="width: 80%; margin: 30px auto">
      <div style="text-align: center">
        <el-input placeholder="Please enter the course name for enquiry..." style="width: 350px"
          v-model="name"></el-input>
        <el-button type="primary" plain style="margin-left: 10px" icon="el-icon-search" round
          @click="loadCourse"></el-button>
        <el-button type="warning" plain style="margin-left: 10px" icon="el-icon-refresh" round
          @click="reset"></el-button>
      </div>
      <div style="margin-top: 30px">
        <el-row :gutter="20" class="course-row">
          <el-col :xs="12" :sm="8" :md="6" :lg="6" v-for="item in courseData" :key="item.id" class="course-col">
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
    window.scrollTo(0, 0)
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
.main-content {
  font-family: 'Poppins', sans-serif;
}

.coach-icon {
  width: 16px;
  height: 16px;
  margin-right: 5px;
  vertical-align: middle;
}

/* 添加响应式布局样式 */
.course-row {
  display: flex;
  flex-wrap: wrap;
  margin-right: -10px;
  margin-left: -10px;
}

.course-col {
  padding: 0 10px;
  box-sizing: border-box;
  margin-bottom: 30px;
}

/* 课程图片容器样式 */
.course-image-container {
  width: 100%;
  padding-top: 66.67%;
  /* 保持3:2的宽高比 (2/3 = 66.67%) */
  position: relative;
  overflow: hidden;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.course-image-container:hover {
  transform: translateY(-10px) scale(1.03);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
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
  transition: transform 0.5s ease;
}

.course-image-container:hover .course-image {
  transform: scale(1.1);
}
</style>