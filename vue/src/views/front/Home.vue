<template>
  <div class="main-content">
    <!-- 轮播图 -->
    <div class="carousel-container">
      <div class="shell">
        <div class="content">
          <div class="item"></div>
          <div class="item"></div>
          <div class="item"></div>
        </div>
      </div>
    </div>
    <div style="width: 80%; margin: 30px auto; padding-top: 41px">
      <div style="display: flex">
        <div style="flex: 1; text-align: center; font-size: 25px; color: #737578"></div>
        <div style="width: 80px; text-align: right; color: #666666; cursor:pointer;"
          @click="$router.push('/front/course')">More></div>
      </div>
      <div style="margin-top: 30px">
        <el-row :gutter="30">
          <el-col :span="6" v-for="item in courseData">
            <div class="course-image-container">
              <img :src="item.img" alt="" class="course-image"
                @click="$router.push('/front/courseDetail?id=' + item.id)">
            </div>
            <div style="font-size: 17px; margin-top: 10px; color: #668293; font-weight: bold">{{ item.name }}</div>
            <div style="margin-top: 10px; color: #666666; display: flex">
              <div style="flex: 1; display: flex; align-items: center;">
                <img src="@/assets/imgs/icon-coach.png" alt="coach icon" class="coach-icon">
                <span>{{ item.coachName }}</span>
              </div>
              <div style="flex: 1">{{ item.time }}</div>
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
      courseData: []
    }
  },
  mounted() {
    this.loadCourse()
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    loadCourse() {
      this.$request.get('/course/selectFour').then(res => {
        if (res.code === '200') {
          this.courseData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
  }
}
</script>

<style scoped>
body {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  height: 100vh;
  background-image: linear-gradient(to top, #9795f0 0%, #fbc8d4 100%);
  overflow: hidden;
}

/* 新增轮播图容器样式，用于居中显示 */
.carousel-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  padding: 40px 0;
}

/* 定义外层容器样式 */
.shell {
  position: relative;
  /* 相对定位 */
  width: 60vw;
  /* 宽度占视口宽度的60% */
  height: 40vw;
  /* 高度占视口高度的40% */
  max-width: 380px;
  /* 最大宽度为380像素 */
  max-height: 250px;
  /* 最大高度为250像素 */
  margin: 0;
  /* 外边距为0 */
  color: white;
  /* 字体颜色为白色 */
  perspective: 1000px;
  /* 透视效果，观察者与z=0平面的距离 */
  transform-origin: center;
  /* 变形原点为中心 */
}

/* 定义内容容器样式 */
.content {
  display: flex;
  /* 弹性盒子布局 */
  justify-content: center;
  /* 主轴居中 */
  align-items: center;
  /* 交叉轴居中 */
  position: absolute;
  /* 绝对定位 */
  width: 100%;
  /* 宽度100% */
  height: 100%;
  /* 高度100% */
  transform-origin: center;
  /* 变形原点为中心 */
  transform-style: preserve-3d;
  /* 保持3D变换 */
  transform: translateZ(-30vw) rotateY(0);
  /* 初始变换：沿z轴平移-30vw，绕y轴旋转0度 */
  animation: carousel 9s infinite cubic-bezier(0.77, 0, 0.175, 1) forwards;
  /* 动画：名称为carousel，持续9秒，无限循环 */
}

/* 定义项目样式 */
.item {
  position: absolute;
  /* 绝对定位 */
  width: 60vw;
  /* 宽度占视口宽度的60% */
  height: 40vw;
  /* 高度占视口高度的40% */
  max-width: 380px;
  /* 最大宽度为380像素 */
  max-height: 250px;
  /* 最大高度为250像素 */
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.3);
  /* 设置阴影 */
  border-radius: 6px;
  /* 边框圆角 */
  background-size: cover;
  /* 背景图片覆盖整个容器 */
  -webkit-box-reflect: below 25px -webkit-linear-gradient(transparent 50%, rgba(255, 255, 255, 0.3));
  /* 创建倒影效果 */
}

/* 第一个项目样式 */
.item:nth-child(1) {
  background-image: url(@/assets/imgs/01.jpg);
  /* 背景图片为01.jpg */
  transform: rotateY(0) translateZ(35vw);
  /* 绕y轴旋转0度，沿z轴平移35vw */
}

/* 第二个项目样式 */
.item:nth-child(2) {
  background-image: url(@/assets/imgs/01.jpg);
  /* 背景图片为02.jpg */
  transform: rotateY(120deg) translateZ(35vw);
  /* 绕y轴旋转120度，沿z轴平移35vw */
}

/* 第三个项目样式 */
.item:nth-child(3) {
  background-image: url(@/assets/imgs/01.jpg);
  /* 背景图片为03.jpg */
  transform: rotateY(240deg) translateZ(35vw);
  /* 绕y轴旋转240度，沿z轴平移35vw */
}

/* 定义动画 */
@keyframes carousel {

  0%,
  17.5% {
    transform: translateZ(-35vw) rotateY(0);
  }

  27.5%,
  45% {
    transform: translateZ(-35vw) rotateY(-120deg);
  }

  55%,
  72.5% {
    transform: translateZ(-35vw) rotateY(-240deg);
  }

  82.5%,
  100% {
    transform: translateZ(-35vw) rotateY(-360deg);
  }
}

/* 如果main-content没有设置样式，也可以添加以下样式确保整个页面内容居中 */
.main-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  min-height: 100vh;
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

.coach-icon {
  width: 16px;
  height: 16px;
  margin-right: 5px;
  vertical-align: middle;
}
</style>
