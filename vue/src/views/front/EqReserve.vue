<template>
  <div class="main-content">
    <div style="width: 80%; margin: 20px auto">
      <div style="margin: 20px 0; color: #666666; font-size: 17px">我的器材预约记录（{{total}}）</div>
      <div class="table">
        <el-table :data="tableData" stripe>
          <el-table-column prop="id" label="序号" width="80" align="center" sortable></el-table-column>
          <el-table-column prop="equipmentImg" label="器材图片" show-overflow-tooltip>
            <template v-slot="scope">
              <div style="display: flex; align-items: center">
                <el-image style="width: 40px; height: 40px; border-radius: 10px" v-if="scope.row.equipmentImg"
                          :src="scope.row.equipmentImg" :preview-src-list="[scope.row.equipmentImg]"></el-image>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="equipmentName" label="器材名称" show-overflow-tooltip></el-table-column>
          <el-table-column prop="start" label="开始时间" show-overflow-tooltip></el-table-column>
          <el-table-column prop="end" label="结束时间" show-overflow-tooltip></el-table-column>
          <el-table-column prop="status" label="审核状态"></el-table-column>

          <el-table-column label="操作" width="180" align="center" v-if="user.role === 'USER'">
            <template v-slot="scope">
              <el-button plain type="danger" size="mini" @click=del(scope.row.id) :disabled="scope.row.status !== '待审核'">取消预约</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination" style="margin-top: 20px">
          <el-pagination
              background
              @current-change="handleCurrentChange"
              :current-page="pageNum"
              :page-sizes="[5, 10, 20]"
              :page-size="pageSize"
              layout="total, prev, pager, next"
              :total="total">
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
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/eqreserve/selectPage', {
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
      this.$confirm('您确定取消预约吗？', '确认取消', {type: "warning"}).then(response => {
        this.$request.delete('/eqreserve/delete/' + id).then(res => {
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
      this.load(pageNum)
    }
  }
}
</script>
