<template>
  <div class="main-content">
    <div style="width: 80%; margin: 30px auto">
      <div style="text-align: center">
        <el-input placeholder="Please enter the equipment name for enquiry..." style="width: 350px"
          v-model="name"></el-input>
        <el-button type="primary" plain style="margin-left: 10px" icon="el-icon-search" round
          @click="loadEquipment"></el-button>
        <el-button type="warning" plain style="margin-left: 10px" icon="el-icon-refresh" round
          @click="reset"></el-button>
      </div>
      <div style="margin-top: 30px">
        <el-row :gutter="30">
          <el-col :span="6" v-for="item in equipmentData" style="margin-bottom: 30px">
            <div style="position: relative">
              <img :src="item.img" alt="" style="width: 100%; height: 280px; border-radius: 10px; cursor: pointer">
              <img src="@/assets/imgs/icon-info.png" @click="viewDescri(item.descr)"
                style="position: absolute; top: 10px; right: 10px; width: 24px; height: 24px; cursor: pointer"
                alt="info">
            </div>
            <div style="font-size: 17px; margin-top: 10px; font-weight: bold; display: flex">
              <div style="flex: 1; color: #666666">{{ item.name }}</div>
              <div style="text-align: right">
                <el-tag effect="plain" type="success" v-if="item.status === '✅ Available'">{{ item.status }}</el-tag>
                <el-tag effect="plain" type="danger" v-else>{{ item.status }}</el-tag>
              </div>
            </div>
            <div style="margin-top: 10px; color: #666666; display: flex">
              <div style="flex: 1">Area: {{ item.location }}</div>
              <div style="flex: 1">Code: {{ item.code }}</div>
            </div>
            <div style="margin-top: 15px">
              <el-button type="primary" style="padding: 10px 30px; border-radius: 20px;"
                :disabled="item.status === '🔴 In Use'" @click="reserveInit(item.id)">Reserve</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
    <el-dialog title="Instructions" :visible.sync="fromVisible" width="40%" :close-on-click-modal="false"
      style="padding-top: 120px" center="boolean" destroy-on-close>
      <el-form label-width="100px" style="padding-right: 50px; padding-top: 20px; padding-bottom: 20px">
        {{ descr }}
      </el-form>
    </el-dialog>
    <el-dialog title="Reservation Information" :visible.sync="reserveVisible" width="40%" :close-on-click-modal="false"
      center="boolean" style="padding-top: 120px" destroy-on-close>
      <el-form label-width="100px" style="padding-right: 50px">
        <el-form-item prop="start" label="Start Time">
          <el-date-picker style="width: 100%" v-model="start" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="Select Date and Time">
          </el-date-picker>
        </el-form-item>
        <el-form-item prop="end" label="End Time">
          <el-date-picker style="width: 100%" v-model="end" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="Select Date and Time">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="reserveVisible = false">Cancel</el-button>
        <el-button type="primary" @click="submit">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>

export default {

  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      equipmentData: [],
      descr: null,
      fromVisible: false,
      reserveVisible: false,
      name: null,
      start: null,
      end: null,
      equipmentId: null
    }
  },
  mounted() {
    this.loadEquipment()
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    loadEquipment() {
      this.$request.get('/equipment/selectAll', {
        params: {
          name: this.name
        }
      }).then(res => {
        if (res.code === '200') {
          this.equipmentData = res.data.reverse()
          this.equipmentData
            .sort((a, b) => {
              if (a.status === '🔴 In Use' && b.status === '✅ Available') return 1;
              if (a.status === '✅ Available' && b.status === '🔴 In Use') return -1;
              return 0;
            });
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    viewDescri(descr) {
      this.descr = descr
      this.fromVisible = true
    },
    reset() {
      this.name = null
      this.loadEquipment()
    },
    reserveInit(equipmentId) {
      if (this.user.role !== 'USER') {
        this.$message.warning('Your role does not support this operation')
        return
      }
      this.equipmentId = equipmentId
      this.start = null
      this.end = null
      this.reserveVisible = true
    },
    submit() {
      let data = {
        userId: this.user.id,
        equipmentId: this.equipmentId,
        start: this.start,
        end: this.end,
        status: 'Pending'
      }
      this.$request.post('/eqreserve/add', data).then(res => {
        if (res.code === '200') {
          this.$message.success('Reservation successful, waiting for admin approval, you can view the approval results in the \'equipment reservation\' page')
          this.reserveVisible = false
          this.equipmentId = null
        } else {
          this.$message.error(res.msg)
        }
      })
    }
  }
}
</script>
