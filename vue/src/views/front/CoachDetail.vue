<template>
  <div class="main-content">
    <div style="width: 65%; margin: 20px auto; position: relative;">
      <!-- Add back button -->
      <div class="back-button" @click="$router.push('/front/coach')">
        <i class="el-icon-arrow-left"></i> Back
      </div>

      <div style="margin-top: 20px; font-size: 17px; font-weight: bold; color: #666666; padding-top: 40px">{{
        coachData.name }}'s Bio</div>
      <div style="margin-top: 20px" v-html="coachData.content" class="w-e-text w-e-text-container"></div>
      <!-- Comment Section -->
      <div style="font-size: 17px; font-weight: bold; margin: 20px 0; color: #9a6d2a; padding-top: 30px">
        Comments（{{ commentData.length }}）</div>
      <div>
        <el-input type="textarea" :rows="5" v-model="comment" placeholder="Enter your comment..."
          class="glassmorphism-input"></el-input>
        <div style="margin-top: 10px; text-align: right">
          <el-button type="primary" @click="submit" class="glassmorphism-button">Submit</el-button>
        </div>
      </div>

      <div style="margin: 40px 0 100px 0">
        <el-row :gutter="10" v-for="item in commentData" style="margin-bottom: 30px">
          <el-col :span="4">
            <div style="display: flex; align-items: center">
              <img :src="item.userAvatar" alt="" style="width: 40px; height: 40px; border-radius: 50%">
              <div style="margin-left: 5px">{{ item.userName }}</div>
            </div>
          </el-col>
          <el-col :span="15">
            <div style="line-height: 40px">{{ item.content }}</div>
          </el-col>
          <el-col :span="5">
            <div style="line-height: 40px; text-align: right; color: #666666">{{ item.time }}</div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script>
import E from 'wangeditor'
export default {

  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      coachId: this.$route.query.id,
      coachData: {},
      comment: null,
      commentData: []
    }
  },
  mounted() {
    // Ensure the page scrolls to the top when it loads
    window.scrollTo(0, 0);

    this.loadCoach()
    this.loadComment()
  },
  // Add the hook function when the route enters
  beforeRouteEnter(to, from, next) {
    next(vm => {
      // After the component instance is created, ensure the page scrolls to the top
      window.scrollTo(0, 0);
    });
  },
  // methods: The click event or other function definition area of this page
  methods: {
    loadComment() {
      this.$request.get('/comment/selectAll', {
        params: {
          coachId: this.coachId
        }
      }).then(res => {
        if (res.code === '200') {
          this.commentData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    loadCoach() {
      this.$request.get('/coach/selectById/' + this.coachId).then(res => {
        if (res.code === '200') {
          this.coachData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    submit() {
      if (this.user.role !== 'USER') {
        this.$message.warning('Role not supported')
        return
      }
      if (!this.comment) {
        this.$message.warning('Enter your comment')
        return
      }
      let data = {
        userId: this.user.id,
        coachId: this.coachId,
        content: this.comment
      }
      this.$request.post('/comment/add', data).then(res => {
        if (res.code === '200') {
          this.$message.success('Comment added successfully')
          this.comment = null
          this.loadComment()
        } else {
          this.$message.error(res.msg)
        }
      })
    }
  }
}
</script>
<style scoped>
/* Add back button style */
.back-button {
  position: absolute;
  top: -20px;
  left: 0;
  background-color: rgba(53, 84, 118, 0.1);
  color: #34495e;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: inline-flex;
  align-items: center;
  margin-bottom: 40px;
  border: 1px solid rgba(53, 84, 118, 0.2);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.back-button:hover {
  transform: scale(1.1);
  background-color: white;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.back-button i {
  margin-right: 5px;
}

p {
  color: #333333;
}

/* Glass style */
.glassmorphism-input .el-textarea__inner {
  background: rgba(255, 255, 255, 0.25) !important;
  backdrop-filter: blur(10px) !important;
  border: 1px solid rgba(255, 255, 255, 0.18) !important;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.1) !important;
  border-radius: 10px !important;
  padding: 15px !important;
  color: #333 !important;
}

.glassmorphism-button {
  background: rgba(64, 158, 255, 0.6) !important;
  backdrop-filter: blur(10px) !important;
  border: 1px solid rgba(255, 255, 255, 0.18) !important;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.2) !important;
  border-radius: 15px !important;
  transition: all 0.3s ease !important;
  /* Zoom effect */
  transform: scale(1.2);
}

.glassmorphism-button:hover {
  background: rgba(64, 158, 255, 0.8) !important;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.4) !important;
  transform: scale(1.3);
  transform: translateY(-2px) !important;
}

/* Add glass effect to the comment section */
.el-row {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(5px);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 12px 0 rgba(31, 38, 135, 0.05);
  padding: 15px;
  transition: all 0.3s ease;
}

.el-row:hover {
  box-shadow: 0 8px 20px 0 rgba(31, 38, 135, 0.1);
  transform: translateY(-2px);
}

/* Add white background to the entire page */
.main-content {
  background-color: #ffffff;
  min-height: 100vh;
  padding-bottom: 50px;
  font-family: 'Dangrek', sans-serif;
}
</style>