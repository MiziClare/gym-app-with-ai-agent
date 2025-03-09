<template>
  <div class="main-content">
    <div class="home-container">
      <!-- Carousel -->
      <div class="carousel-container">
        <div class="shell">
          <div class="content">
            <div class="item"></div>
            <div class="item"></div>
            <div class="item"></div>
          </div>
        </div>
      </div>
      <!-- Course section -->
      <div class="course-section">
        <div class="course-header">
          <div class="course-title"></div>
          <div class="course-more" @click="$router.push('/front/course')">More ></div>
        </div>
        <div class="course-list">
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
    window.scrollTo(0, 0);
    this.loadCourse()
  },
  // methods: The click event or other function definition area of this page
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
html {
  overflow-x: hidden;
  height: 100%;
  margin: 0;
  padding: 0;
}

.home-container {
  width: 90%;
  margin: 0 auto;
  margin-left: 0%;
  overflow-x: hidden;
  overflow-y: hidden;
  font-family: 'Roboto', sans-serif;
}

.home-container::-webkit-scrollbar {
  display: none;
}

.main-content::-webkit-scrollbar {
  display: none;
}

body {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  height: 100%;
  background-image: linear-gradient(to top, #9795f0 0%, #fbc8d4 100%);
  overflow: hidden;
  max-width: 80%;
}

/* Add new carousel container style for centering display */
.carousel-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  padding: 40px 0;
  max-width: 100%;
  overflow: visable;
}

/* Define the outer container style */
.shell {
  position: relative;
  width: 60vw;
  height: 40vw;
  max-width: 380px;
  max-height: 250px;
  margin: 0 auto;
  /* Center display */
  color: white;
  perspective: 1000px;
  transform-origin: center;
}

/* Define the content container style */
.content {
  display: flex;
  /* Elastic box layout */
  justify-content: center;
  /* Main axis center */
  align-items: center;
  /* Cross-axis center */
  position: absolute;
  /* Absolute positioning */
  width: 100%;
  /* Width 100% */
  height: 100%;
  /* Height 100% */
  transform-origin: center;
  /* Transform origin center */
  transform-style: preserve-3d;
  /* Keep 3D transformation */
  transform: translateZ(-30vw) rotateY(0);
  /* Initial transformation: translate along z-axis by -30vw, rotate around y-axis by 0 degrees */
  animation: carousel 10s infinite cubic-bezier(0.77, 0, 0.175, 1) forwards;
  /* Animation: Name is carousel, lasts 10 seconds, and loops infinitely */
}

/* Define the item style */
.item {
  position: absolute;
  /* Absolute positioning */
  width: 60vw;
  /* Width */
  height: 40vw;
  /* Height */
  max-width: 380px;
  /* Maximum width is 380 pixels */
  max-height: 250px;
  /* Maximum height is 250 pixels */
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.3);
  /* Set shadow */
  border-radius: 6px;
  /* Border radius */
  background-size: cover;
  /* Background image covers the container */
  -webkit-box-reflect: below 25px -webkit-linear-gradient(transparent 50%, rgba(255, 255, 255, 0.3));
  /* Create a reflection effect */
}

/* The first item style */
.item:nth-child(1) {
  background-image: url(@/assets/imgs/course-yoga.jpg);
  /* Background image is 01.jpg */
  transform: rotateY(0) translateZ(35vw);
  /* Rotate around y-axis by 0 degrees, translate along z-axis by 35vw */
}

/* The second item style */
.item:nth-child(2) {
  background-image: url(@/assets/imgs/course-treadmill.jpg);
  /* Background image is 02.jpg */
  transform: rotateY(120deg) translateZ(35vw);
  /* Rotate around y-axis by 120 degrees, translate along z-axis by 35vw */
}

/* The third item style */
.item:nth-child(3) {
  background-image: url(@/assets/imgs/course-core.jpg);
  /* Background image is 03.jpg */
  transform: rotateY(240deg) translateZ(35vw);
  /* Rotate around y-axis by 240 degrees, translate along z-axis by 35vw */
}

/* Define the animation */
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

/* If main-content is not set, you can also add the following styles to ensure the entire page content is centered */
.main-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  min-height: 80vh;
  overflow-x: hidden;
  /* Add this line to prevent horizontal overflow */
  max-width: 100vw;
  /* Ensure it does not exceed the viewport width */
}

/* Course image container style */
.course-image-container {
  width: 100%;
  padding-top: 66.67%;
  /* Keep the 3:2 aspect ratio (2/3 = 66.67%) */
  position: relative;
  overflow: hidden;
  border-radius: 10px;
  margin-bottom: 20px;
}

.course-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  /* Keep the image ratio and fill the container */
  cursor: pointer;
}

.coach-icon {
  width: 16px;
  height: 16px;
  margin-right: 5px;
  vertical-align: middle;
}

/* Course area style */
.course-section {
  width: 80%;
  margin: 30px auto;
  margin-top: 90px;
  max-width: 1200px;
}

.course-header {
  display: flex;
}

.course-title {
  flex: 1;
  text-align: center;
  font-size: 25px;
  color: #737578;
}

.course-more {
  width: 80px;
  text-align: right;
  color: #666666;
  cursor: pointer;
}

.course-list {
  margin-top: 30px;
}

/* Responsive adjustment */
@media screen and (max-width: 768px) {
  .course-section {
    width: 80%;
  }

  .shell,
  .item {
    width: 80vw;
    height: 53vw;
  }
}
</style>
