<template>
  <div>
    <div class="search">
      <el-input placeholder="Please enter order number" style="width: 200px" v-model="orderNo"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">Search</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">Reset</el-button>
    </div>

    <div class="operation">
      <el-button type="danger" plain @click="delBatch">Batch Delete</el-button>
    </div>

    <div class="table" style="width: 100%; margin: 0 auto">
      <el-table :data="tableData" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="No." width="80" align="center" sortable></el-table-column>
        <el-table-column prop="courseName" label="Course Name" show-overflow-tooltip></el-table-column>
        <el-table-column prop="coachName" label="Coach Name" show-overflow-tooltip></el-table-column>
        <el-table-column prop="userName" label="Customer" show-overflow-tooltip></el-table-column>
        <el-table-column prop="orderNo" label="Order No." show-overflow-tooltip></el-table-column>
        <el-table-column prop="price" label="Course Price" show-overflow-tooltip></el-table-column>
        <el-table-column prop="time" label="Order Time"></el-table-column>

        <el-table-column label="Actions" width="180" align="center">
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
  </div>
</template>

<script>
export default {
  name: "Orders",
  data() {
    return {
      tableData: [],  // 所有的数据
      pageNum: 1,   // 当前的页码
      pageSize: 10,  // 每页显示的个数
      total: 0,
      orderNo: null,
      fromVisible: false,
      form: {},
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      rules: {},
      ids: []
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    del(id) {   // 单个删除
      this.$confirm('Are you sure you want to delete?', 'Confirm Delete', { type: "warning" }).then(response => {
        this.$request.delete('/orders/delete/' + id).then(res => {
          if (res.code === '200') {
            this.$message.success('Operation successful')
            this.load(1)
          } else {
            this.$message.error(res.msg)
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
        this.$message.warning('Please select data first')
        return
      }
      this.$confirm('Are you sure you want to delete these items?', 'Confirm Batch Delete', { type: "warning" }).then(response => {
        this.$request.delete('/orders/delete/batch', { data: this.ids }).then(res => {
          if (res.code === '200') {
            this.$message.success('Operation successful')
            this.load(1)
          } else {
            this.$message.error(res.msg)
          }
        })
      }).catch(() => {
      })
    },
    load(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/orders/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          orderNo: this.orderNo,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    reset() {
      this.orderNo = null
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
  }
}
</script>

<style scoped></style>
