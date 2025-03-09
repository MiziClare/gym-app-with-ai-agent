<template>
  <div>
    <div class="search">
      <el-input placeholder="Search by username" style="width: 200px" v-model="username"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">Search</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">Reset</el-button>
    </div>

    <div class="operation">
      <el-button type="primary" plain @click="handleAdd">Add New</el-button>
      <el-button type="danger" plain @click="delBatch">Batch Delete</el-button>
    </div>

    <div class="table" style="width: 100%; margin: 0 auto">
      <el-table :data="tableData" strip @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="ID" width="70" align="center" sortable></el-table-column>
        <el-table-column label="Avatar">
          <template v-slot="scope">
            <div style="display: flex; align-items: center">
              <el-image style="width: 40px; height: 40px; border-radius: 50%" v-if="scope.row.avatar"
                :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]"></el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="Username"></el-table-column>
        <el-table-column prop="name" label="Name"></el-table-column>
        <el-table-column prop="content" label="Bio">

          <template v-slot="scope">
            <el-button type="primary" @click="viewEditor(scope.row.content)">View</el-button>
          </template>

        </el-table-column>
        <el-table-column prop="phone" label="Phone"></el-table-column>
        <el-table-column prop="email" label="Email"></el-table-column>
        <el-table-column prop="role" label="Role"></el-table-column>
        <el-table-column label="Actions" align="center" width="180">
          <template v-slot="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">Edit</el-button>
            <el-button size="mini" type="danger" plain @click="del(scope.row.id)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination background @current-change="handleCurrentChange" :current-page="pageNum"
          :page-sizes="[5, 10, 20]" :page-size="pageSize" layout="total, prev, pager, next" :total="total">
        </el-pagination>
      </div>
    </div>

    <el-dialog title="Add a Coach" :visible.sync="fromVisible" :close-on-click-modal="false" destroy-on-close
      custom-class="custom-dialog" width="50%" top="5vh">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef" class="coach-form">
        <div class="form-left">
          <el-form-item label="Avatar">
            <el-upload class="avatar-uploader" :action="$baseUrl + '/files/upload'" :headers="{ token: user.token }"
              list-type="picture" :on-success="handleAvatarSuccess">
              <el-button type="primary">Upload Avatar</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="Username" prop="username">
            <el-input v-model="form.username" placeholder="Enter username"></el-input>
          </el-form-item>
          <el-form-item label="Name" prop="name">
            <el-input v-model="form.name" placeholder="Enter name"></el-input>
          </el-form-item>
          <el-form-item label="Phone" prop="phone">
            <el-input v-model="form.phone" placeholder="Enter phone number"></el-input>
          </el-form-item>
          <el-form-item label="Email" prop="email">
            <el-input v-model="form.email" placeholder="Enter email"></el-input>
          </el-form-item>
        </div>

        <el-form-item label="Bio" prop="content" class="bio-editor">
          <div id="editor"></div>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false" class="dialog-btn">Cancel</el-button>
        <el-button type="primary" @click="save" class="dialog-btn">Save</el-button>
      </div>
    </el-dialog>

    <!-- Preview rich text editor -->
    <el-dialog title="Details" :visible.sync="viewVisible" width="55%" :close-on-click-modal="false" destroy-on-close>
      <div v-html="viewData" class="w-e-text w-e-text-container preview-content"></div>
    </el-dialog>
  </div>
</template>

<script>
import E from 'wangeditor' // Import rich text editor
export default {
  name: "Coach",
  data() {
    return {
      editor: null,
      viewData: null,
      viewVisible: false,
      tableData: [],  // All data
      pageNum: 1,   // Current page number
      pageSize: 10,  // Number of items per page
      total: 0,
      username: null,
      fromVisible: false,
      form: {},
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      rules: {
        username: [
          { required: true, message: 'Please enter username', trigger: 'blur' },
        ]
      },
      ids: []
    }
  },
  created() {
    this.load(1)
  },
  methods: {

    // Initialize rich text editor
    initWangEditor(content) {
      this.$nextTick(() => {
        this.editor = new E('#editor')
        this.editor.config.placeholder = 'Enter the Bio...'
        this.editor.config.uploadFileName = 'file'
        this.editor.config.uploadImgServer = 'http://localhost:9090/files/wang/upload'
        this.editor.create()
        setTimeout(() => {
          this.editor.txt.html(content)
        })
      })
    },

    // Click View button to preview rich text editor
    viewEditor(content) {
      this.viewData = content
      this.viewVisible = true
    },

    handleAdd() {   // Add data
      this.form = {}  // When adding data, clear the data
      this.initWangEditor('') // Initialize rich text editor
      this.fromVisible = true   // Open the popup
    },
    handleEdit(row) {   // Edit data
      this.form = JSON.parse(JSON.stringify(row))  // Assign the value to the form object  注意要深拷贝数据
      this.initWangEditor(this.form.content) // When editing, initialize the rich text editor
      this.fromVisible = true   // Open the popup
    },
    save() {   // The logic triggered by the save button will trigger the addition or update
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.form.content = this.editor.txt.html()
          this.$request({
            url: this.form.id ? '/coach/update' : '/coach/add',
            method: this.form.id ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {  // Successfully saved
              this.$message.success('Saved successfully')
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
        this.$request.delete('/coach/delete/' + id).then(res => {
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
      this.ids = rows.map(v => v.id)
    },
    delBatch() {   // Batch delete
      if (!this.ids.length) {
        this.$message.warning('Please select data first')
        return
      }
      this.$confirm('Are you sure you want to delete these items?', 'Confirm Delete', { type: "warning" }).then(response => {
        this.$request.delete('/coach/delete/batch', { data: this.ids }).then(res => {
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
      this.$request.get('/coach/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          username: this.username,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    reset() {
      this.username = null
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
    handleAvatarSuccess(response, file, fileList) {
      // Replace the avatar attribute with the link of the uploaded image
      this.form.avatar = response.data
    },
  }
}
</script>

<style scoped>
/* Custom dialog style */
.custom-dialog {
  border-radius: 15px !important;
  overflow: hidden;
}

.coach-form {
  padding: 0 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.form-left {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.form-left .el-form-item {
  margin-bottom: 0;
}

/* Let the Bio editor occupy a separate line */
.bio-editor {
  grid-column: 1 / -1;
}

/* Set the outer frame and style of the editor */
:deep(#editor) {
  border-radius: 12px;
  overflow: hidden;
  height: 360px;
  border: 2px solid #e4e7ed;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

:deep(#editor):hover {
  border-color: #c0c4cc;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(#editor .w-e-toolbar) {
  border-bottom: 1px solid #e4e7ed !important;
  background-color: #f5f7fa !important;
  padding: 5px 10px !important;
}

:deep(#editor .w-e-text-container) {
  border: none !important;
  height: calc(100% - 45px) !important;
}

:deep(#editor .w-e-text) {
  padding: 10px 15px !important;
}

/* Button animation effect */
.dialog-btn {
  transition: all 0.3s ease;
  border-radius: 20px;
  padding: 10px 25px;
}

.dialog-btn:hover {
  transform: scale(1.05);
}

/* Ensure that the input box and other elements in the popup also have rounded corners */
:deep(.el-input__inner) {
  border-radius: 8px !important;
}

:deep(.el-upload) {
  border-radius: 8px !important;
}

:deep(.avatar-uploader .el-upload) {
  border-radius: 8px;
  overflow: hidden;
}

/* Custom scrollbar style */
.coach-form::-webkit-scrollbar {
  width: 6px;
}

.coach-form::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.coach-form::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

/* Add preview window style */
.preview-content {
  min-height: 500px;
  padding: 20px;
  background-color: #fff;
  border-radius: 15px;
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.05);
}

/* Ensure that the content is empty and the background can also be displayed */
.preview-content:empty {
  background-color: #f8f9fa;
}

/* Custom preview dialog style */
:deep(.el-dialog) {
  border-radius: 15px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  padding: 20px;
  border-bottom: 1px solid #eee;
}

:deep(.el-dialog__body) {
  padding: 20px;
}
</style>