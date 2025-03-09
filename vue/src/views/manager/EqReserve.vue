<template>
  <div>
    <div class="table" style="width: 100%; margin: 0 auto">
      <el-table :data="tableData" stripe>
        <el-table-column prop="id" label="No." width="80" align="center" sortable></el-table-column>
        <el-table-column prop="equipmentImg" label="Image" show-overflow-tooltip>
          <template v-slot="scope">
            <div style="display: flex; align-items: center">
              <el-image style="width: 40px; height: 40px; border-radius: 10px" v-if="scope.row.equipmentImg"
                :src="scope.row.equipmentImg" :preview-src-list="[scope.row.equipmentImg]"></el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="equipmentName" label="Name" show-overflow-tooltip></el-table-column>
        <el-table-column prop="userName" label="User" show-overflow-tooltip></el-table-column>
        <el-table-column prop="start" label="Start Time" show-overflow-tooltip></el-table-column>
        <el-table-column prop="end" label="End Time" show-overflow-tooltip></el-table-column>
        <el-table-column prop="status" label="Status"></el-table-column>

        <el-table-column label="Action" width="180" align="center">
          <template v-slot="scope">
            <el-button plain type="primary" @click="handleEdit(scope.row)" size="mini">Approve</el-button>
            <el-button plain type="danger" size="mini" @click=del(scope.row.id)>Reject</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination background @current-change="handleCurrentChange" :current-page="pageNum"
          :page-sizes="[5, 10, 20]" :page-size="pageSize" layout="total, prev, pager, next" :total="total">
        </el-pagination>
      </div>
    </div>


    <el-dialog title="Approval Status" :visible.sync="fromVisible" width="40%" :close-on-click-modal="false"
      destroy-on-close>
      <el-form label-width="100px" style="padding-right: 50px" :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="title" label="Title">
          <el-select v-model="form.status" placeholder="Select" style="width: 100%">
            <el-option label="Pending" value="Pending"></el-option>
            <el-option label="Approved" value="Approved"></el-option>
            <el-option label="Rejected" value="Rejected"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save">Confirm</el-button>
      </div>
    </el-dialog>


  </div>
</template>

<script>
export default {
  name: "EqReserve",
  data() {
    return {
      tableData: [],  // All data
      pageNum: 1,   // Current page number
      pageSize: 10,  // Number of items per page
      total: 0,
      fromVisible: false,
      form: {},
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      rules: {},
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    handleEdit(row) {   // Edit data
      this.form = JSON.parse(JSON.stringify(row))  // Assign the value to the form object  注意要深拷贝数据
      this.fromVisible = true   // Open the popup
    },
    save() {   // The logic triggered by the save button will trigger the addition or update
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request({
            url: this.form.id ? '/eqreserve/update' : '/eqreserve/add',
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
      this.$confirm('Are you sure you want to delete it?', 'Confirm delete', { type: "warning" }).then(response => {
        this.$request.delete('/eqreserve/delete/' + id).then(res => {
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
      this.$request.get('/eqreserve/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
  }
}
</script>

<style scoped></style>
