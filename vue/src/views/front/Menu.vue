<template>
  <div class="main-content">
    <div style="width: 80%; margin: 30px auto">
      <div style="text-align: center">
        <el-input placeholder="请输入菜谱名称查询" style="width: 300px" v-model="name"></el-input>
        <el-button type="primary" plain style="margin-left: 10px" @click="loadMenu">查询</el-button>
        <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
      </div>
      <div style="margin-top: 30px">
        <el-row :gutter="30">
          <el-col :span="6" v-for="item in menuData" style="margin-bottom: 30px">
            <img :src="item.img" alt="" style="width: 100%; height: 200px; border-radius: 10px; cursor: pointer">
            <div style="font-size: 17px; margin-top: 10px; font-weight: bold; display: flex">
              <div style="flex: 1; color: #666666">{{ item.name }}</div>
            </div>
            <div style="margin-top: 10px; color: #666666; display: flex">
              <div style="flex: 1">发布时间：{{ item.time }}</div>
            </div>
            <div style="margin-top: 15px">
              <el-button type="primary" style="padding: 10px 30px" @click="viewDescri(item.content)">查看菜谱</el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
    <el-dialog title="菜谱详情" :visible.sync="fromVisible" width="50%" :close-on-click-modal="false" destroy-on-close>
      <div v-html="viewData" class="w-e-text w-e-text-container"></div>
    </el-dialog>
  </div>
</template>

<script>
import E from 'wangeditor'
export default {

  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      menuData: [],
      fromVisible: false,
      name: null,
      viewData: null,
    }
  },
  mounted() {
    this.loadMenu()
  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    loadMenu() {
      this.$request.get('/menu/selectAll', {
        params: {
          name: this.name
        }
      }).then(res => {
        if (res.code === '200') {
          this.menuData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    viewDescri(content) {
      this.viewData = content
      this.fromVisible = true
    },
    reset() {
      this.name = null
      this.loadMenu()
    },
  }
}
</script>
