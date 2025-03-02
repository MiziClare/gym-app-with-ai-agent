<template>
  <div>
    <div class="search">
      <el-input placeholder="Please enter post title for enquiry..." style="width: 200px" v-model="name"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">Search</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">Reset</el-button>
    </div>

    <div class="operation">
      <el-button type="danger" plain @click="delBatch">Batch Delete</el-button>
    </div>

    <div class="table" style="width: 100%">
      <el-table :data="tableData" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="No." width="80" align="center" sortable></el-table-column>
        <el-table-column prop="name" label="Title" show-overflow-tooltip></el-table-column>
        <el-table-column prop="content" label="Details" show-overflow-tooltip>
          <template v-slot="scope">
            <el-button type="primary" @click="viewEditor(scope.row.content)">View</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="Poster" show-overflow-tooltip></el-table-column>
        <el-table-column prop="role" label="Role" show-overflow-tooltip>
          <template v-slot="scope">
            <el-tag type="primary" v-if="scope.row.role === 'COACH'">Coach</el-tag>
            <el-tag type="info" v-else>User</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="Post Time"></el-table-column>

        <el-table-column label="Action" width="180" align="center">
          <template v-slot="scope">
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


    <el-dialog title="Post Details" :visible.sync="viewVisible" width="55%" :close-on-click-modal="false"
      destroy-on-close>
      <div v-html="viewData" class="w-e-text w-e-text-container"></div>
    </el-dialog>

  </div>
</template>

<script>
import E from 'wangeditor'
export default {
  name: "Experience",
  data() {
    return {
      tableData: [],  // 所有的数据
      pageNum: 1,   // 当前的页码
      pageSize: 10,  // 每页显示的个数
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
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    viewEditor(content) {
      this.viewData = content
      this.viewVisible = true
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
    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.ids = rows.map(v => v.id)   //  [1,2]
    },
    delBatch() {   // 批量删除
      if (!this.ids.length) {
        this.$message.warning('Please select the data')
        return
      }
      this.$confirm('Are you sure you want to delete these data?', 'Confirm Delete', { type: "warning" }).then(response => {
        this.$request.delete('/experience/delete/batch', { data: this.ids }).then(res => {
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
    load(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/experience/selectPage', {
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
  }
}
</script>

<style scoped></style>
