<template>
  <div class="main-content">
    <el-card style="width: 50%; margin: 30px auto">
      <div style="text-align: right; margin-bottom: 20px">
        <el-button type="info" @click="rechargeInit">Top Up</el-button>
        <el-button type="primary" @click="updatePassword">Change Password</el-button>
      </div>
      <el-form :model="user" label-width="80px" style="padding-right: 20px">
        <div style="margin: 15px; text-align: center">
          <el-upload class="avatar-uploader" :action="$baseUrl + '/files/upload'" :show-file-list="false"
            :on-success="handleAvatarSuccess">
            <img v-if="user.avatar" :src="user.avatar" class="avatar" />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </div>
        <el-form-item label="Username" prop="username">
          <el-input v-model="user.username" placeholder="Username" disabled></el-input>
        </el-form-item>
        <el-form-item label="Name" prop="name">
          <el-input v-model="user.name" placeholder="Name"></el-input>
        </el-form-item>
        <el-form-item label="Phone" prop="phone">
          <el-input v-model="user.phone" placeholder="Phone"></el-input>
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="user.email" placeholder="Email"></el-input>
        </el-form-item>
        <el-form-item label="Balance" prop="account">
          {{ user.account }}
        </el-form-item>
        <div style="text-align: center; margin-bottom: 20px">
          <el-button type="primary" @click="update">Save</el-button>
        </div>
      </el-form>

      <!-- Add data export and delete user data button -->
      <div style="text-align: right; margin-top: 20px; border-top: 1px solid #eee; padding-top: 15px">
        <el-button size="small" type="success" @click="exportUserData">Export My Data</el-button>
        <el-button size="small" type="danger" @click="deleteUserData">Delete My Data</el-button>
      </div>
    </el-card>
    <el-dialog title="Change Password" :visible.sync="dialogVisible" width="30%" :close-on-click-modal="false"
      destroy-on-close>
      <el-form :model="user" label-width="80px" style="padding-right: 20px" :rules="rules" ref="formRef">
        <el-form-item label="Current Password" prop="password">
          <el-input show-password v-model="user.password" placeholder="Current Password"></el-input>
        </el-form-item>
        <el-form-item label="New Password" prop="newPassword">
          <el-input show-password v-model="user.newPassword" placeholder="New Password"></el-input>
        </el-form-item>
        <el-form-item label="Confirm Password" prop="confirmPassword">
          <el-input show-password v-model="user.confirmPassword" placeholder="Confirm Password"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save">Confirm</el-button>
      </div>
    </el-dialog>

    <!-- Top Up Dialog -->
    <el-dialog title="Top Up" :visible.sync="rechargeVisible" width="30%" :close-on-click-modal="false"
      destroy-on-close>
      <el-form label-width="80px" style="padding-right: 20px">
        <el-form-item label="Top-Up " prop="account">
          <el-input v-model="account" placeholder="Amount"></el-input>
        </el-form-item>
        <el-form-item label="Payment Methods" prop="account">
          <el-radio v-model="type" label="cardpay">Card</el-radio>
          <el-radio v-model="type" label="ewalletpay">E-Wallets</el-radio>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rechargeVisible = false">Cancel</el-button>
        <el-button type="primary" @click="recharge">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('Please confirm your password'))
      } else if (value !== this.user.newPassword) {
        callback(new Error('Passwords do not match'))
      } else {
        callback()
      }
    }
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      dialogVisible: false,
      rechargeVisible: false,
      account: null,
      type: 'cardpay',

      rules: {
        password: [
          { required: true, message: 'Please enter your current password', trigger: 'blur' },
        ],
        newPassword: [
          { required: true, message: 'Please enter your new password', trigger: 'blur' },
        ],
        confirmPassword: [
          { validator: validatePassword, required: true, trigger: 'blur' },
        ],
      }
    }
  },
  created() {
    this.loadUser()
  },
  mounted() {
    // When the page is opened, the view is positioned at the top
    window.scrollTo(0, 0)
  },
  methods: {
    loadUser() {
      this.$request.get('/user/selectById/' + this.user.id).then(res => {
        if (res.code === '200') {
          // Now the user data is the latest
          this.user = res.data
          // Update the browser cache's user data
          localStorage.setItem('xm-user', JSON.stringify(this.user)) // WATCH OUT: Token will be missing after updata the cache if selectById doesnot regenerate a new token again
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    rechargeInit() {
      this.account = 50
      this.rechargeVisible = true
    },
    recharge() {
      if (!this.account) {
        this.$message.warning('Please enter the amount to top up')
        return
      }
      this.$request.get('/user/recharge/' + this.account).then(res => {
        if (res.code === '200') {
          this.$message.success('Top up successfully')
          this.loadUser() // Refresh the user data after top up
          this.rechargeVisible = false
        }
      })
    },
    update() {
      // Save the current user information to the database
      this.$request.put('/user/update', this.user).then(res => {
        if (res.code === '200') {
          // Successfully updated
          this.$message.success('Saved successfully')
          // Update the user information in the browser cache
          localStorage.setItem('xm-user', JSON.stringify(this.user))

          // Trigger the data update of the parent
          this.$emit('update:user')
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleAvatarSuccess(response, file, fileList) {
      // Replace the user's avatar attribute with the link of the uploaded image
      this.$set(this.user, 'avatar', response.data)
    },
    // Change password
    updatePassword() {
      this.dialogVisible = true
    },
    save() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request.put('/updatePassword', this.user).then(res => {
            if (res.code === '200') {
              this.$message.success('Password changed successfully')
              this.$router.push('/login')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    },
    // Export user data
    exportUserData() {
      // Get the current user's ID
      const userId = this.user.id;

      // Check if the user ID exists
      if (!userId) {
        this.$message.error('User not logged in or information is incomplete, please log in again');
        return;
      }

      // Show loading prompt
      this.$message.info('Preparing to download data...');

      // Use the browser's fetch API to directly download
      fetch(this.$baseUrl + '/user/exportUserInfo?userId=' + userId, {
        method: 'GET',
        headers: {
          'token': this.user.token
        }
      })
        .then(response => response.blob())
        .then(blob => {
          // Create a Blob URL
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', 'user_info.csv');
          document.body.appendChild(link);
          link.click();
          // Clean up
          window.URL.revokeObjectURL(url);
          document.body.removeChild(link);
          this.$message.success('Data export successfully');
        })
        .catch(error => {
          console.error('Export failed:', error);
          this.$message.error('Data export failed, please try again later');
        });
    },

    // Delete user data
    deleteUserData() {
      this.$confirm('Are you sure you want to delete your personal data? This will clear all personal information.', 'Warning', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        // Create an object that only contains the fields to be updated
        const updateData = {
          id: this.user.id,
          name: '',
          phone: '',
          email: ''
        };

        this.$request.put('/user/deletePersonalData', updateData).then(res => {
          if (res.code === '200') {
            this.$message.success('Personal data deleted successfully');
            // Update local user data
            this.user.name = '';
            this.user.phone = '';
            this.user.email = '';
            // Update the user information in the browser cache
            localStorage.setItem('xm-user', JSON.stringify(this.user));
            // Trigger the data update of the parent
            this.$emit('update:user');
          } else {
            this.$message.error(res.msg);
          }
        }).catch(error => {
          console.error('Failed to delete personal data:', error);
          this.$message.error('Failed to delete personal data, please try again later');
        });
      }).catch(() => {
        this.$message.info('Operation cancelled');
      });
    }
  }
}
</script>

<style scoped>
.main-content {
  font-family: 'Roboto', sans-serif;
  font-size: 36px;
}

/deep/.el-form-item__label {
  font-weight: bold;
}

/deep/.el-upload {
  border-radius: 50%;
}

/deep/.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border-radius: 50%;
}

/deep/.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
  border-radius: 50%;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  border-radius: 50%;
}
</style>