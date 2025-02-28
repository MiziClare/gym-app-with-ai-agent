<template>
  <div class="main-content">
    <div style="width: 80%; margin: 30px auto">
      <div style="text-align: center">
        <el-input placeholder="请输入课程名称查询" style="width: 300px" v-model="name"></el-input>
        <el-button type="primary" plain style="margin-left: 10px" @click="loadCourse">查询</el-button>
        <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
      </div>
      <div style="margin-top: 30px">
        <el-row :gutter="30">
         <el-col :span="6" v-for="item in courseData" style="margin-bottom: 30px">
           <img :src="item.img" alt="" style="width: 100%; height: 280px; border-radius: 10px; cursor: pointer" @click="$router.push('/front/courseDetail?id=' + item.id)">
           <div style="font-size: 17px; margin-top: 10px; font-weight: bold; display: flex">
             <div style="flex: 1; color: #666666">{{item.name}}</div>
             <div style="width: 70px; color: red; text-align: right">￥{{ item.price }}</div>
           </div>

           <div style="margin-top: 10px; color: #666666; display: flex">
             <div style="flex: 1">教练：{{ item.coachName }}</div>
             <div style="flex: 1">课程周期：{{ item.time }}</div>
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
