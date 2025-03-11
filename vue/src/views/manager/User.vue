<template>
  <div>
    <div class="search">
      <el-input placeholder="Enter your username to search" style="width: 200px" v-model="username"></el-input>
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
              <el-image style="width: 40px; height: 40px; border-radius: 50%; cursor: pointer;" v-if="scope.row.avatar"
                :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]" @click="viewUserProfile(scope.row)">
              </el-image>
              <div v-else
                style="width: 40px; height: 40px; border-radius: 50%; background-color: #f0f0f0; display: flex; justify-content: center; align-items: center; cursor: pointer;"
                @click="viewUserProfile(scope.row)">
                <i class="el-icon-user-solid" style="font-size: 24px; color: #909399;"></i>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="Username"></el-table-column>
        <el-table-column prop="name" label="Name"></el-table-column>
        <el-table-column prop="phone" label="Phone"></el-table-column>
        <el-table-column prop="email" label="Email"></el-table-column>
        <el-table-column prop="role" label="Role"></el-table-column>
        <el-table-column prop="account" label="Balance"></el-table-column>
        <el-table-column label="Operation" align="center" width="180">
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

    <!-- 添加固定的圆形扫描按钮 -->
    <div class="scan-button" @click="openScanDialog">
      <img src="@/assets/imgs/icon-scan.png" alt="Scan" />
    </div>

    <el-dialog title="Add an User" :visible.sync="fromVisible" width="40%" :close-on-click-modal="false"
      destroy-on-close>
      <el-form :model="form" label-width="100px" style="padding-right: 50px" :rules="rules" ref="formRef">
        <el-form-item label="Avatar">
          <el-upload class="avatar-uploader" :action="$baseUrl + '/files/upload'" :headers="{ token: user.token }"
            list-type="picture" :on-success="handleAvatarSuccess">
            <el-button type="primary">Upload Avatar</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="Username" prop="username">
          <el-input v-model="form.username" placeholder="Username"></el-input>
        </el-form-item>
        <el-form-item label="Name" prop="name">
          <el-input v-model="form.name" placeholder="Your Name"></el-input>
        </el-form-item>
        <el-form-item label="Phone" prop="phone">
          <el-input v-model="form.phone" placeholder="Your Phone Number"></el-input>
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="Your Email"></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save">Save</el-button>
      </div>
    </el-dialog>

    <!-- Scan QR Code -->
    <el-dialog title="Scan QR Code" :visible.sync="scanDialogVisible" width="500px" :close-on-click-modal="false">
      <div class="scanner-container">
        <qrcode-stream v-if="scanDialogVisible" @decode="onDecode" @init="onInit">
          <div v-if="scanError" class="error-message">
            {{ scanError }}
          </div>
        </qrcode-stream>
      </div>
      <div v-if="scanResult" class="scan-result">
        <p>Scan Result: {{ scanResult }}</p>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="scanDialogVisible = false">Close</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { QrcodeStream } from 'vue-qrcode-reader'

export default {
  name: "User",
  components: {
    QrcodeStream
  },
  data() {
    return {
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
          { required: true, message: 'Please enter your username', trigger: 'blur' },
        ]
      },
      ids: [],
      scanDialogVisible: false,
      scanResult: '',
      scanError: ''
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    handleAdd() {   // Add data
      this.form = {}  // When adding data, clear the data
      this.fromVisible = true   // Open the popup
    },
    handleEdit(row) {   // Edit data
      this.form = JSON.parse(JSON.stringify(row))  // Assign the value to the form object  注意要深拷贝数据
      this.fromVisible = true   // Open the popup
    },
    save() {   // The logic triggered by the save button will trigger the addition or update
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request({
            url: this.form.id ? '/user/update' : '/user/add',
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
      this.$confirm('Are you sure about the deletion?', 'Confirm', { type: "warning" }).then(response => {
        this.$request.delete('/user/delete/' + id).then(res => {
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
        this.$message.warning('Please select the data to be deleted')
        return
      }
      this.$confirm('Are you sure about the deletion?', 'Confirm', { type: "warning" }).then(response => {
        this.$request.delete('/user/delete/batch', { data: this.ids }).then(res => {
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
      this.$request.get('/user/selectPage', {
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
      // Replace the avatar property with the uploaded image link
      this.form.avatar = response.data
    },
    viewUserProfile(user) {
      this.$router.push(`/user/${user.username}`);
    },
    openScanDialog() {
      this.scanDialogVisible = true;
      this.scanResult = '';
      this.scanError = '';
    },
    onDecode(result) {
      this.scanResult = result;

      // Check if the result is a valid membership card URL
      if (result && result.includes('/user/')) {
        // Extract the username from the URL
        const urlParts = result.split('/');
        const username = urlParts[urlParts.length - 1];

        if (username) {
          // Delay closing the dialog and redirecting to the user profile page
          setTimeout(() => {
            this.scanDialogVisible = false;
            this.$router.push(`/user/${username}`);
          }, 1000);
        }
      }
    },
    onInit(promise) {
      promise
        .then(() => {
          // Camera initialization successful
          this.scanError = '';
        })
        .catch(error => {
          if (error.name === 'NotAllowedError') {
            this.scanError = 'Need camera permission to scan QR code';
          } else if (error.name === 'NotFoundError') {
            this.scanError = 'Camera device not found';
          } else if (error.name === 'NotSupportedError') {
            this.scanError = 'Your browser does not support camera functionality';
          } else if (error.name === 'NotReadableError') {
            this.scanError = 'Camera is occupied';
          } else if (error.name === 'OverconstrainedError') {
            this.scanError = 'Camera does not meet the requirements';
          } else if (error.name === 'StreamApiNotSupportedError') {
            this.scanError = 'Browser does not support stream API';
          } else {
            this.scanError = `Camera error: ${error.name}`;
          }
        });
    }
  }
}
</script>

<style scoped>
.scan-button {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #2c3e50;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  z-index: 999;
  transition: transform 0.3s, box-shadow 0.3s;
}

.scan-button:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.3);
}

.scan-button img {
  width: 30px;
  height: 30px;
  object-fit: contain;
}

.scanner-container {
  width: 100%;
  height: 300px;
  overflow: hidden;
  position: relative;
  background-color: #000;
  border-radius: 8px;
}

.error-message {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 10px 20px;
  border-radius: 4px;
  text-align: center;
  max-width: 80%;
}

.scan-result {
  margin-top: 15px;
  padding: 10px;
  background-color: #f0f9eb;
  border-radius: 4px;
  border-left: 4px solid #67c23a;
}
</style>