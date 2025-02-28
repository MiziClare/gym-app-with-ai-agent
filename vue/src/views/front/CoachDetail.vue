<template>
  <div class="main-content">
    <div style="width: 80%; margin: 20px auto">
      <div style="display: flex">
        <div style="flex: 1">
          <el-image style="width: 300px; height: 300px; border-radius: 10px" :src="coachData.img"></el-image>
        </div>
        <div style="flex: 2; padding: 20px">
          <div style="font-size: 25px; font-weight: bold; margin: 10px 0">{{ coachData.name }}</div>
          <div style="margin: 10px 0">
            <span style="color: #666">Gender: </span>
            {{ coachData.sex }}
          </div>
          <div style="margin: 10px 0">
            <span style="color: #666">Age: </span>
            {{ coachData.age }}
          </div>
          <div style="margin: 10px 0">
            <span style="color: #666">Phone: </span>
            {{ coachData.phone }}
          </div>
          <div style="margin: 10px 0">
            <span style="color: #666">Email: </span>
            {{ coachData.email }}
          </div>
          <div style="margin: 10px 0">
            <span style="color: #666">Speciality: </span>
            {{ coachData.speciality }}
          </div>
          <div style="margin: 10px 0">
            <span style="color: #666">Description: </span>
            {{ coachData.description }}
          </div>
          <div style="margin: 10px 0">
            <el-button type="primary" @click="reserve" v-if="user.role === 'USER'">Make Reservation</el-button>
          </div>
        </div>
      </div>

      <div style="margin: 20px 0">
        <div style="border-bottom: 1px solid #eee; padding: 10px">Comments ({{ commentData.length }})</div>
        <div v-if="user.role === 'USER'" style="padding: 10px; display: flex; align-items: center">
          <el-input v-model="comment" placeholder="Write your comment here..."></el-input>
          <el-button type="primary" style="margin-left: 10px" @click="addComment">Submit</el-button>
        </div>
        <div v-for="item in commentData" :key="item.id" style="padding: 10px; border-bottom: 1px solid #eee">
          <div>
            <span style="color: #666; margin-right: 10px">{{ item.userName }}</span>
            <span style="color: #999; font-size: 12px">{{ item.time }}</span>
          </div>
          <div style="margin: 5px 0">{{ item.content }}</div>
        </div>
      </div>
    </div>

    <el-dialog title="Make Reservation" :visible.sync="dialogFormVisible" width="30%">
      <el-form :model="form">
        <el-form-item label="Notes">
          <el-input v-model="form.content" type="textarea" :rows="4"
            placeholder="Please enter reservation notes"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveReserve">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import E from 'wangeditor'
export default {

  data() {
    return {
      coachId: this.$route.query.id,
      coachData: {},
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      comment: '',
      commentData: [],
      dialogFormVisible: false,
      form: {}
    }
  },
  mounted() {
    this.loadCoach()
    this.loadComment()
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
    },
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
    reserve() {
      this.dialogFormVisible = true
    },
    saveReserve() {
      if (!this.form.content) {
        this.$message.warning('Please enter reservation notes')
        return
      }
      this.$request.post('/reserve/add', {
        coachId: this.coachId,
        content: this.form.content
      }).then(res => {
        if (res.code === '200') {
          this.$message.success('Reservation successful')
          this.dialogFormVisible = false
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    addComment() {
      if (!this.comment) {
        this.$message.warning('Please enter your comment')
        return
      }
      this.$request.post('/comment/add', {
        coachId: this.coachId,
        content: this.comment
      }).then(res => {
        if (res.code === '200') {
          this.$message.success('Comment submitted successfully')
          this.comment = ''
          this.loadComment()
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