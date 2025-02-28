<template>
  <div class="main-content">
    <div style="width: 80%; margin: 20px auto">
      <div style="font-size: 17px; color: #666666">我的购买记录（{{ total }}）</div>
      <div style="margin-top: 20px">
        <el-table :data="tableData" stripe>
          <el-table-column prop="id" label="序号" width="80" align="center" sortable></el-table-column>
          <el-table-column prop="courseName" label="课程名称" show-overflow-tooltip>
            <template v-slot="scope">
              <a :href="'/front/courseDetail?id=' + scope.row.courseId">{{ scope.row.courseName }}</a>
            </template>
          </el-table-column>
          <el-table-column prop="coachName" label="教练姓名" show-overflow-tooltip>
            <template v-slot="scope">
              <a :href="'/front/coachDetail?id=' + scope.row.coachId">{{ scope.row.coachName }}</a>
            </template>
          </el-table-column>
          <el-table-column prop="orderNo" label="订单编号" show-overflow-tooltip></el-table-column>
          <el-table-column prop="price" label="课程价格" show-overflow-tooltip></el-table-column>
          <el-table-column prop="time" label="下单时间"></el-table-column>

          <el-table-column label="操作" width="180" align="center">
            <template v-slot="scope">
              <el-button plain type="danger" size="mini" @click=del(scope.row.id)>删除</el-button>
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
      user: JSON.parse(localStorage.getItem("xm-user") || '{}'),
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,

    }
  },
  mounted() {
    this.load(1)
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    load(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/orders/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          userId: this.user.id
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    del(id) {   // 单个删除
      this.$confirm('您确定删除吗？', '确认删除', { type: "warning" }).then(response => {
        this.$request.delete('/orders/delete/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('操作成功')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load(this.pageNum)
    }
  }
}
</script>
