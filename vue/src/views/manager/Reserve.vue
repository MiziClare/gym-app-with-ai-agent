<template>
  <div>
    <div class="search">
      <el-input placeholder="Enter coach name" style="width: 200px" v-model="coachName"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">Search</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">Reset</el-button>
    </div>

    <div class="table" style="width: 100%;">
      <el-table :data="tableData" stripe>
        <el-table-column prop="id" label="No." width="80" align="center" sortable></el-table-column>
        <el-table-column prop="userName" label="Customer" show-overflow-tooltip></el-table-column>
        <el-table-column prop="coachName" label="Coach" show-overflow-tooltip></el-table-column>
        <el-table-column prop="content" label="Description" show-overflow-tooltip></el-table-column>
        <el-table-column prop="time" label="Created Time"></el-table-column>
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
  name: "Reserve",
  data() {
    return {
      tableData: [],  // 所有的数据
      pageNum: 1,   // 当前的页码
      pageSize: 10,  // 每页显示的个数
      total: 0,
      coachName: null,
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      rules: {},
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    load(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/reserve/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          coachName: this.coachName,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    reset() {
      this.coachName = null
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
  }
}
</script>

<style scoped></style>
