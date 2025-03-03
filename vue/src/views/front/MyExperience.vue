<template>
  <div class="main-content">
    <div style="width: 80%; margin: 20px auto">
      <div><el-button type="primary" circle @click="handleAdd">Post</el-button></div>
      <div style="margin: 20px 0; color: #666666; font-size: 17px">My Post（{{ total }}）</div>
      <div class="table" style="width: 100%">
        <el-table :data="tableData" stripe>
          <el-table-column prop="id" label="No." width="80" align="center" sortable></el-table-column>
          <el-table-column prop="name" label="Post Title" show-overflow-tooltip></el-table-column>
          <el-table-column prop="content" label="Post Details" show-overflow-tooltip>
            <template v-slot="scope">
              <el-button type="primary" @click="viewEditor(scope.row.content)">View</el-button>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="Time"></el-table-column>

          <el-table-column label="Action" width="180" align="center">
            <template v-slot="scope">
              <el-button plain type="primary" size="mini" @click=handleEdit(scope.row)>Edit</el-button>
              <el-button plain type="danger" size="mini" @click=del(scope.row.id)>Delete</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination" style="margin-top: 20px">
          <el-pagination background @current-change="handleCurrentChange" :current-page="pageNum"
            :page-sizes="[5, 10, 20]" :page-size="pageSize" layout="total, prev, pager, next" :total="total">
          </el-pagination>
        </div>
      </div>
    </div>
    <el-dialog title="Post Information" :visible.sync="fromVisible" width="60%" :close-on-click-modal="false"
      destroy-on-close>
      <el-form label-width="100px" style="padding-right: 50px" :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="name" label="Post Title">
          <el-input v-model="form.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="content" label="Post Details">
          <div id="editor"></div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save">Confirm</el-button>
      </div>
    </el-dialog>
    <el-dialog title="Post Introduction" :visible.sync="viewVisible" width="55%" :close-on-click-modal="false"
      destroy-on-close>
      <div v-html="viewData" class="w-e-text w-e-text-container"></div>
    </el-dialog>
  </div>
</template>

<script>
import E from 'wangeditor'
export default {

  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      form: {},
      fromVisible: false,
      editor: null,
      viewData: null,
      viewVisible: false,
    }
  },
  mounted() {
    this.load(1)
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    viewEditor(content) {
      this.viewData = content
      this.viewVisible = true
    },
    initWangEditor(content) {
      this.$nextTick(() => {
        this.editor = new E('#editor')
        this.editor.config.placeholder = 'Please enter the content...'
        this.editor.config.uploadFileName = 'file'
        this.editor.config.uploadImgServer = 'http://localhost:9090/files/wang/upload'
        this.editor.create()
        setTimeout(() => {
          this.editor.txt.html(content)
        })
      })
    },
    handleAdd() {
      this.form = {
        userId: this.user.id,
        role: this.user.role
      }
      this.initWangEditor('')
      this.fromVisible = true
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.initWangEditor(this.form.content)
      this.fromVisible = true
    },
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/experience/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          userId: this.user.id,
          role: this.user.role
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    save() {
      this.form.content = this.editor.txt.html()
      this.$request({
        url: this.form.id ? '/experience/update' : '/experience/add',
        method: this.form.id ? 'PUT' : 'POST',
        data: this.form
      }).then(res => {
        if (res.code === '200') {  // 表示成功保存
          this.$message.success('Save successfully')
          this.load(1)
          this.fromVisible = false
        } else {
          this.$message.error(res.msg)  // 弹出错误的信息
        }
      })
    },
    del(id) {   // 单个删除
      this.$confirm('Are you sure you want to delete it?', 'Confirm Delete', { type: "warning" }).then(response => {
        this.$request.delete('/experience/delete/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('Operation successful')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    }
  }
}
</script>

<style scoped>
.main-content {
  font-family: 'Dangrek', sans-serif;
}
</style>