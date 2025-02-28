<template>
  <div class="main-content">
    <div style="width: 80%; margin: 30px auto">
      <div style="text-align: center">
        <el-input placeholder="请输入器材名称查询" style="width: 300px" v-model="name"></el-input>
        <el-button type="primary" plain style="margin-left: 10px" @click="loadEquipment">查询</el-button>
        <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
      </div>
      <div style="margin-top: 30px">
        <el-row :gutter="30">
          <el-col :span="6" v-for="item in equipmentData" style="margin-bottom: 30px">
            <img :src="item.img" alt="" style="width: 100%; height: 280px; border-radius: 10px; cursor: pointer">
            <div style="font-size: 17px; margin-top: 10px; font-weight: bold; display: flex">
              <div style="flex: 1; color: #666666">{{ item.name }}</div>
              <div style="text-align: right">
                <el-tag type="primary" v-if="item.status === '空闲中'">{{ item.status }}</el-tag>
                <el-tag type="danger" v-else>{{ item.status }}</el-tag>
              </div>
            </div>
            <div style="margin-top: 10px; color: #666666; display: flex">
              <div style="flex: 1">器材区域：{{ item.location }}</div>
              <div style="flex: 1">器材编号：{{ item.code }}</div>
            </div>
            <div style="margin-top: 15px">
              <el-button type="primary" style="padding: 10px 30px" @click="viewDescri(item.descr)">使用说明</el-button>
              <el-button type="info" style="padding: 10px 30px" :disabled="item.status === '使用中'"
                @click="reserveInit(item.id)">我要预约</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
    <el-dialog title="使用说明" :visible.sync="fromVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form label-width="100px" style="padding-right: 50px">
        {{ descr }}
      </el-form>
    </el-dialog>
    <el-dialog title="预约信息" :visible.sync="reserveVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form label-width="100px" style="padding-right: 50px">
        <el-form-item prop="start" label="开始时间">
          <el-date-picker style="width: 100%" v-model="start" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="选择日期时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item prop="end" label="结束时间">
          <el-date-picker style="width: 100%" v-model="end" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="选择日期时间">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="reserveVisible = false">取 消</el-button>
        <el-button type="primary" @click="submit">确 定</el-button>
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
          this.equipmentData = res.data
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
        this.$message.warning('您的角色暂不支持该操作')
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
        status: '待审核'
      }
      this.$request.post('/eqreserve/add', data).then(res => {
        if (res.code === '200') {
          this.$message.success('预约成功，等待管理员审核，您可以在器材预约里查看审核结果')
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
