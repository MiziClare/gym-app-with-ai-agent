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
            <div style="margin-bottom: 20px; font-size: 18px; color: #222222">Price Per Session: <span
                style="color: red">￡{{
                  courseData.price }}</span></div>
            <div style="margin-bottom: 20px; font-size: 18px; color: #333333">Total Sessions: {{
              courseData.totalSessions || 1 }}
            </div>
            <div style="margin-top: 30px"><el-button type="primary" round @click="buy">Get Single Class Pass</el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- Course Schedule Table -->
      <div style="margin-top: 40px; margin-bottom: 30px;">
        <div style="display: flex; align-items: center; margin-bottom: 20px;">
          <h3 style="font-size: 20px; color: #333; border-left: 4px solid #409EFF; padding-left: 10px; margin: 0;">
            Course Schedule
          </h3>
          <el-tooltip class="item" effect="light" placement="top-start">
            <div slot="content" style="max-width: 300px; line-height: 1.5;">
              With a Single Class Pass, you're free to join any session throughout the week by checking in with your ID.
              Our coach will provide guidance tailored to your personal progress.
            </div>
            <i class="el-icon-info info-icon"></i>
          </el-tooltip>
        </div>
        <div v-if="scheduleData && scheduleData.length > 0">
          <el-table :data="scheduleData" border style="width: 100%" :row-class-name="tableRowClassName">
            <el-table-column prop="weekday" label="Weekday" width="180" align="center"></el-table-column>
            <el-table-column prop="startTime" label="Start Time" width="180" align="center">
              <template v-slot="scope">
                {{ formatTime(scope.row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="room" label="Room" align="center"></el-table-column>
          </el-table>
        </div>
        <div v-else
          style="background-color: #f9f9f9; padding: 20px; text-align: center; border-radius: 5px; color: #909399;">
          No schedule available for this course
        </div>
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
      courseData: {},
      scheduleData: [] // course schedule data
    }
  },
  mounted() {
    window.scrollTo(0, 0);
    this.loadCourseData();
    this.loadCourseSchedule();
  },
  methods: {
    loadCourseData() {
      this.$request.get('/course/selectById/' + this.courseId).then(res => {
        if (res.code === '200') {
          this.courseData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    loadCourseSchedule() {
      this.$request.get('/courseSchedule/selectByCourseId/' + this.courseId).then(res => {
        if (res.code === '200') {
          this.scheduleData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    formatTime(timeString) {
      if (!timeString) return ''
      // Handle LocalTime format, usually "HH:MM:SS" format
      return timeString.substring(0, 5) // Only display hours and minutes
    },
    tableRowClassName({ row, rowIndex }) {
      // Add alternating colors to table rows
      return rowIndex % 2 === 0 ? 'even-row' : 'odd-row';
    },
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
<style scoped>
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

/* Add table style */
.el-table .even-row {
  background-color: #f9f9f9;
}

.el-table .odd-row {
  background-color: #ffffff;
}

.el-table--border {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.el-table th {
  background-color: #eef5fe !important;
  color: #355476;
  font-weight: 600;
}

/* Add info icon style */
.info-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  font-size: 12px;
  margin-left: 10px;
  cursor: help;
  transition: all 0.3s ease;
}

.info-icon:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
}

/* Custom tooltip style */
.el-tooltip__popper.is-light {
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 12px;
  font-size: 14px;
  font-family: 'Montserrat', sans-serif;
  color: #606266;
}
</style>