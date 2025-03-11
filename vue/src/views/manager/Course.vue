<template>
  <div>
    <div class="search">
      <el-input placeholder="Please enter course name" style="width: 200px" v-model="name"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">Search</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">Reset</el-button>
    </div>

    <div class="operation">
      <el-button type="primary" plain @click="handleAdd">Add</el-button>
      <el-button type="danger" plain @click="delBatch">Batch Delete</el-button>
    </div>

    <div class="table" style="width: 100%; margin: 0 auto">
      <el-table :data="tableData" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="ID" width="80" align="center" sortable></el-table-column>
        <el-table-column prop="img" label="Poster" show-overflow-tooltip>
          <template v-slot="scope">
            <div style="display: flex; align-items: center">
              <el-image style="width: 60px; height: 40px; border-radius: 10px; object-fit: cover" v-if="scope.row.img"
                :src="scope.row.img" :preview-src-list="[scope.row.img]"></el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="Name" show-overflow-tooltip></el-table-column>
        <el-table-column prop="time" label="Duration" show-overflow-tooltip></el-table-column>
        <el-table-column prop="price" label="Price" show-overflow-tooltip></el-table-column>
        <el-table-column prop="totalSessions" label="Total Sessions" show-overflow-tooltip></el-table-column>
        <el-table-column prop="coachName" label="Coach" show-overflow-tooltip></el-table-column>
        <el-table-column prop="content" label="Description" show-overflow-tooltip>
          <template v-slot="scope">
            <el-button type="primary" @click="viewEditor(scope.row.content, scope.row.id)">View</el-button>
          </template>
        </el-table-column>

        <el-table-column label="Operations" width="180" align="center">
          <template v-slot="scope">
            <el-button plain type="primary" @click="handleEdit(scope.row)" size="mini">Edit</el-button>
            <el-button plain type="danger" size="mini" @click=del(scope.row.id)>Delete</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination background @current-change="handleCurrentChange" :current-page="pageNum"
          :page-sizes="[5, 10, 20]" :page-size="pageSize" layout="total, prev, pager, next" :total="total">
        </el-pagination>
      </div>
    </div>


    <el-dialog title="Add a Course" :visible.sync="fromVisible" width="50%" height="30%" :close-on-click-modal="false"
      custom-class="custom-dialog" destroy-on-close>
      <el-form label-width="100px" style="padding-right: 50px" :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="img" label="Poster">
          <el-upload class="avatar-uploader" :action="$baseUrl + '/files/upload'" :headers="{ token: user.token }"
            list-type="picture" :on-success="handleImgSuccess">
            <el-button type="primary">Upload Image</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="name" label="Name">
          <el-input v-model="form.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="time" label="Duration">
          <el-input v-model="form.time" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="price" label="Price">
          <el-input v-model="form.price" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="totalSessions" label="Sessions">
          <el-input-number v-model="form.totalSessions" :min="1" :max="100" autocomplete="off"></el-input-number>
        </el-form-item>
        <el-form-item prop="coachId" label="Coach">
          <el-select v-model="form.coachId" placeholder="Please select coach" style="width: 100%">
            <el-option v-for="item in coachData" :label="item.name" :value="item.id" :key="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Schedule" v-if="form.id">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
            <span>Course Schedule Management</span>
            <el-button type="primary" size="mini" @click="openScheduleDialog">Manage Schedule</el-button>
          </div>
          <div v-if="editScheduleData && editScheduleData.length > 0">
            <el-table :data="editScheduleData" border size="mini" style="width: 100%">
              <el-table-column prop="weekday" label="Weekday" width="100"></el-table-column>
              <el-table-column prop="startTime" label="Start Time" width="100">
                <template v-slot="scope">
                  {{ formatTime(scope.row.startTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="room" label="Room"></el-table-column>
            </el-table>
          </div>
          <div v-else style="color: #909399; text-align: center; padding: 10px 0; font-size: 13px;">
            No schedule available for this course
          </div>
        </el-form-item>
        <el-form-item prop="content" label="Description">
          <div id="editor"></div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button class="custom-button" @click="fromVisible = false">Cancel</el-button>
        <el-button class="custom-button" type="primary" @click="save">Confirm</el-button>
      </div>
    </el-dialog>
    <el-dialog title="Course Description" :visible.sync="viewVisible" width="55%" :close-on-click-modal="false"
      destroy-on-close>
      <div v-if="scheduleData && scheduleData.length > 0" style="margin-bottom: 20px;">
        <h3 style="margin-bottom: 15px;">Course Schedule</h3>
        <el-table :data="scheduleData" border style="width: 100%">
          <el-table-column prop="weekday" label="Weekday" width="120"></el-table-column>
          <el-table-column prop="startTime" label="Start Time" width="120">
            <template v-slot="scope">
              {{ formatTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="room" label="Room"></el-table-column>
        </el-table>
      </div>
      <div v-else style="margin-bottom: 20px;">
        <h3 style="margin-bottom: 15px;">Course Schedule</h3>
        <div style="color: #909399; text-align: center; padding: 20px 0;">No schedule available for this course</div>
      </div>
      <div v-html="viewData" class="w-e-text w-e-text-container"></div>
    </el-dialog>
    <el-dialog title="Course Schedule Management" :visible.sync="scheduleVisible" width="50%"
      :close-on-click-modal="false" destroy-on-close>
      <div style="margin-bottom: 20px; display: flex; justify-content: space-between;">
        <span>Course: <strong>{{ form.name }}</strong></span>
        <el-button type="primary" size="small" @click="addScheduleRow">Add Schedule</el-button>
      </div>

      <el-table :data="editScheduleData" border style="width: 100%">
        <el-table-column prop="weekday" label="Weekday" width="150">
          <template v-slot="scope">
            <el-select v-model="scope.row.weekday" placeholder="Select day" style="width: 100%">
              <el-option v-for="day in weekdays" :key="day" :label="day" :value="day"></el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="Start Time" width="150">
          <template v-slot="scope">
            <el-time-picker v-model="scope.row.timePickerValue" format="HH:mm" placeholder="Select time"
              style="width: 100%" @change="updateStartTime(scope.row)"></el-time-picker>
          </template>
        </el-table-column>
        <el-table-column prop="room" label="Room">
          <template v-slot="scope">
            <el-input v-model="scope.row.room" placeholder="Enter room"></el-input>
          </template>
        </el-table-column>
        <el-table-column label="Operations" width="120" align="center">
          <template v-slot="scope">
            <el-button type="danger" icon="el-icon-delete" circle size="mini"
              @click="removeScheduleRow(scope.$index)"></el-button>
          </template>
        </el-table-column>
      </el-table>

      <div slot="footer" class="dialog-footer">
        <el-button @click="scheduleVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveSchedule">Save</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import E from 'wangeditor'
export default {
  name: "Course",
  data() {
    return {
      tableData: [],  // All data
      pageNum: 1,   // Current page number
      pageSize: 10,  // Number of items per page
      total: 0,
      name: null,
      fromVisible: false,
      form: {},
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      rules: {},
      ids: [],
      coachData: [],
      editor: null,
      viewData: null,
      viewVisible: false,
      scheduleData: [], // store course schedule data
      viewCourseId: null, // current view course id
      scheduleVisible: false, // 课程安排对话框可见性
      editScheduleData: [], // 编辑中的课程安排数据
      weekdays: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'],
    }
  },
  created() {
    this.load(1)
    this.loadCoach()
  },
  methods: {
    viewEditor(content, courseId) {
      this.viewData = content
      this.viewCourseId = courseId
      this.loadCourseSchedule(courseId)
      this.viewVisible = true
    },
    initWangEditor(content) {
      this.$nextTick(() => {
        this.editor = new E('#editor')
        this.editor.config.placeholder = 'Please enter content'
        this.editor.config.uploadFileName = 'file'
        this.editor.config.uploadImgServer = 'http://localhost:9090/files/wang/upload'
        this.editor.create()
        setTimeout(() => {
          this.editor.txt.html(content)
        })
      })
    },
    loadCoach() {
      this.$request.get('/coach/selectAll').then(res => {
        if (res.code === '200') {
          this.coachData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleAdd() {   // Add data
      this.form = {}  // When adding data, clear the data
      this.initWangEditor('')
      this.fromVisible = true   // Open the popup
    },
    handleEdit(row) {   // Edit data
      this.form = JSON.parse(JSON.stringify(row))
      this.initWangEditor(this.form.content)
      this.loadEditSchedule(this.form.id)
      this.fromVisible = true
    },
    save() {   // The logic triggered by the save button will trigger the addition or update
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          if (this.form.price) {
            this.form.price = Number(this.form.price);
          }
          this.form.content = this.editor.txt.html()
          this.$request({
            url: this.form.id ? '/course/update' : '/course/add',
            method: this.form.id ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {  // Successfully saved
              this.$message.success('Save successfully')
              this.load(1)
              this.fromVisible = false
            } else {
              this.$message.error(res.msg)  // Show error message
            }
          })
        }
      })
    },
    del(id) {   // Single delete
      this.$confirm('Are you sure you want to delete?', 'Confirm Delete', { type: "warning" }).then(response => {
        this.$request.delete('/course/delete/' + id).then(res => {
          if (res.code === '200') {   // Successfully deleted
            this.$message.success('Operation successful')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // Show error message
          }
        })
      }).catch(() => {
      })
    },
    handleSelectionChange(rows) {   // Current selected all row data
      this.ids = rows.map(v => v.id)   //  [1,2]
    },
    delBatch() {   // Batch delete
      if (!this.ids.length) {
        this.$message.warning('Please select data')
        return
      }
      this.$confirm('Are you sure you want to delete these items?', 'Confirm Delete', { type: "warning" }).then(response => {
        this.$request.delete('/course/delete/batch', { data: this.ids }).then(res => {
          if (res.code === '200') {   // Successfully deleted
            this.$message.success('Operation successful')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // Show error message
          }
        })
      }).catch(() => {
      })
    },
    load(pageNum) {  // Pagination query
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/course/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    reset() {
      this.name = null
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
    handleImgSuccess(res) {
      this.form.img = res.data
    },
    // load course schedule
    loadCourseSchedule(courseId) {
      this.$request.get('/courseSchedule/selectByCourseId/' + courseId).then(res => {
        if (res.code === '200') {
          this.scheduleData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    // format time
    formatTime(timeString) {
      if (!timeString) return ''
      // handle LocalTime format, usually "HH:MM:SS" format
      return timeString.substring(0, 5) // only show hour and minute
    },
    // 加载编辑中的课程安排
    loadEditSchedule(courseId) {
      if (!courseId) return

      this.$request.get('/courseSchedule/selectByCourseId/' + courseId).then(res => {
        if (res.code === '200') {
          this.editScheduleData = res.data.map(item => {
            // 为时间选择器创建Date对象
            const timeParts = item.startTime ? item.startTime.split(':') : ['00', '00', '00']
            const date = new Date()
            date.setHours(parseInt(timeParts[0]))
            date.setMinutes(parseInt(timeParts[1]))
            date.setSeconds(parseInt(timeParts[2]))

            return {
              ...item,
              timePickerValue: date
            }
          })
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    // 打开课程安排管理对话框
    openScheduleDialog() {
      this.scheduleVisible = true
    },
    // 添加课程安排行
    addScheduleRow() {
      const now = new Date()
      this.editScheduleData.push({
        courseId: this.form.id,
        weekday: 'Monday',
        timePickerValue: now,
        room: 'Room 01'
      })
    },
    // 移除课程安排行
    removeScheduleRow(index) {
      this.editScheduleData.splice(index, 1)
    },
    // 更新开始时间
    updateStartTime(row) {
      if (row.timePickerValue) {
        const hours = row.timePickerValue.getHours().toString().padStart(2, '0')
        const minutes = row.timePickerValue.getMinutes().toString().padStart(2, '0')
        row.startTime = `${hours}:${minutes}:00`
      }
    },
    // 保存课程安排
    saveSchedule() {
      // 首先删除该课程的所有现有安排
      this.$request.delete('/courseSchedule/deleteByCourseId/' + this.form.id).then(res => {
        if (res.code === '200') {
          // 然后添加新的安排
          const promises = this.editScheduleData.map(item => {
            // 确保每行都有startTime
            if (item.timePickerValue && !item.startTime) {
              this.updateStartTime(item)
            }

            const scheduleData = {
              courseId: this.form.id,
              weekday: item.weekday,
              startTime: item.startTime,
              room: item.room
            }

            return this.$request.post('/courseSchedule/add', scheduleData)
          })

          Promise.all(promises).then(() => {
            this.$message.success('Schedule saved successfully')
            this.scheduleVisible = false
            // 重新加载课程安排
            this.loadEditSchedule(this.form.id)
          }).catch(err => {
            this.$message.error('Failed to save schedule: ' + err.message)
          })
        } else {
          this.$message.error(res.msg)
        }
      })
    },
  }
}
</script>

<style scoped>
.custom-dialog {
  border-radius: 15px;
  overflow: hidden;
}

/* Override the title style of el-dialog */
:deep(.custom-dialog .el-dialog__header) {
  padding: 20px;
  background-color: #f5f7fa;
}

.custom-button {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.custom-button:hover {
  transform: scale(1.05);
}

/* Ensure that the popup content has appropriate padding */
:deep(.custom-dialog .el-dialog__body) {
  padding: 30px 20px;
}

/* Ensure that the bottom button area has appropriate padding */
:deep(.custom-dialog .el-dialog__footer) {
  padding: 20px;
  border-top: 1px solid #eee;
}
</style>
