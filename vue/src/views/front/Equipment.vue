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
        <!-- to /front/eqreserve -->
        <el-tooltip content="View Equipment Reservation Status" placement="top">
          <el-button type="primary" plain style="margin-left: 10px" icon="el-icon-edit" round
            @click="goToEqReserve"></el-button>
        </el-tooltip>
      </div>

      <!-- prompt area -->
      <div class="glass-header">
        <h1>
          <img src="../../assets/imgs/icon-barbell.png" alt="info icon" class="title-icon">
          &#12288;Equipment Information
        </h1>
        <p>Normally no reservation required - equipment is open for walk-in use. This page provides availability
          information.</p>
      </div>

      <div style="margin-top: 30px">
        <el-row :gutter="20">
          <el-col :xs="12" :sm="8" :md="6" :lg="6" v-for="item in equipmentData" style="margin-bottom: 30px">
            <div class="equipment-image-container">
              <img :src="item.img" alt="" class="equipment-image">
              <img src="@/assets/imgs/icon-info.png" @click="viewDescri(item.descr)" class="info-icon" alt="info">
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
    <div style="font-family: 'Roboto', sans-serif;">
      <el-dialog title="Instructions" :visible.sync="fromVisible" width="40%" :close-on-click-modal="false"
        style="padding-top: 120px" center="boolean" destroy-on-close>
        <el-form label-width="100px" style="padding-right: 50px; padding-top: 20px; padding-bottom: 20px">
          {{ descr }}
        </el-form>
      </el-dialog>
    </div>
    <el-dialog title="Reservation Information" :visible.sync="reserveVisible" width="40%" :close-on-click-modal="false"
      center="boolean" style="padding-top: 120px;" destroy-on-close>
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
    window.scrollTo(0, 0)
  },
  // methods: The click event or other function definition area of this page
  methods: {
    loadEquipment() {
      this.$request.get('/equipment/selectAll', {
        params: {
          name: this.name
        }
      }).then(res => {
        if (res.code === '200') {
          this.equipmentData = res.data
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
    },
    goToEqReserve() {
      this.$router.push('/front/eqreserve');
    }
  }
}
</script>

<style scoped>
.main-content {
  font-family: 'Roboto', sans-serif;
}

/* add glass effect title area style */
.glass-header {
  background: rgba(245, 247, 250, 0.8);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-radius: 20px;
  padding: 30px;
  margin: 30px 0;
  border: 1px solid rgba(230, 235, 240, 0.8);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.05),
    inset 0 0 32px rgba(255, 255, 255, 0.8);
  text-align: center;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  transform: scale(1);
  font-family: 'Montserrat', sans-serif;
}

.glass-header:hover {
  transform: scale(1.02);
  box-shadow:
    0 15px 45px rgba(0, 0, 0, 0.1),
    inset 0 0 45px rgba(255, 255, 255, 0.9);
  background: rgba(245, 247, 250, 0.9);
}

.glass-header h1 {
  color: #355476;
  font-size: 28px;
  margin: 0;
  margin-bottom: 15px;
  font-weight: 600;
  letter-spacing: -0.02em;
  display: flex;
  align-items: center;
  justify-content: center;
}

.glass-header p {
  color: #667788;
  font-size: 16px;
  margin: 0;
  line-height: 1.6;
  font-weight: 400;
  letter-spacing: 0.01em;
}

.title-icon {
  width: 24px;
  height: 24px;
  vertical-align: middle;
  margin-right: 2px;
  transform: translateY(-1px);
  filter: brightness(0.5);
  opacity: 0.9;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.glass-header:hover .title-icon {
  transform: translateY(-2px) scale(1.1);
}

/* Add the following CSS styles */
.el-row {
  display: flex;
  flex-wrap: wrap;
  margin-right: -10px;
  margin-left: -10px;
}

.el-col {
  width: 25%;
  padding: 0 10px;
  box-sizing: border-box;
}

@media screen and (max-width: 1200px) {
  .el-col {
    width: 33.33%;
  }
}

@media screen and (max-width: 768px) {
  .el-col {
    width: 50%;
  }
}

@media screen and (max-width: 480px) {
  .el-col {
    width: 100%;
  }
}

/* Add equipment image container style */
.equipment-image-container {
  position: relative;
  width: 100%;
  height: 280px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  cursor: pointer;
}

.equipment-image-container:hover {
  transform: translateY(-10px) scale(1.03);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
}

.equipment-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
  transition: transform 0.5s ease;
}

.equipment-image-container:hover .equipment-image {
  transform: scale(1.1);
}

.info-icon {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 24px;
  height: 24px;
  cursor: pointer;
  z-index: 10;
  transition: transform 0.3s ease;
}

.info-icon:hover {
  transform: scale(1.2);
}
</style>
