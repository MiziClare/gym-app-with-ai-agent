<template>
  <div class="main-content">
    <div style="width: 80%; margin: 20px auto">
      <div style="margin: 20px 0; color: #666666; font-size: 17px">My Reservations ({{ total }})</div>
      <div class="table">
        <el-table :data="tableData" stripe>
          <el-table-column prop="id" label="No." width="80" align="center" sortable></el-table-column>
          <el-table-column prop="userName" label="Member" show-overflow-tooltip
            v-if="user.role === 'COACH'"></el-table-column>
          <el-table-column prop="coachName" label="Coach" show-overflow-tooltip></el-table-column>
          <el-table-column prop="content" label="Notes" show-overflow-tooltip></el-table-column>
          <el-table-column prop="time" label="Created Time"></el-table-column>

          <el-table-column label="Actions" width="180" align="center" v-if="user.role === 'USER'">
            <template v-slot="scope">
              <el-button plain type="danger" size="mini" @click=del(scope.row.id)>Cancel</el-button>
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
  </div>
</template>

<script>

export default {

  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,

    }
  },
  mounted() {
    this.load(1)
  },
  // methods: All click events and other function definitions for this page
  methods: {
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/reserve/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    del(id) {   // Delete single reservation
      this.$confirm('Are you sure you want to cancel this reservation?', 'Confirm Cancellation', { type: "warning" }).then(response => {
        this.$request.delete('/reserve/delete/' + id).then(res => {
          if (res.code === '200') {   // Operation successful
            this.$message.success('Cancelled successfully')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // Display error message
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