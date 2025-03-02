<template>
  <div>
    <div style="display: flex">
      <!-- 第一行：Monthly Revenue(3) 和 Equipment Usage(1) -->
      <div style="width: 75%; height: 330px" class="card" id="lineChart"></div>
      <div style="width: 25%; height: 330px" class="card" id="pie1"></div>
    </div>
    <!-- 第二行：Coach Reservation(3) 和 Course Sales(1) -->
    <div style="display: flex">
      <div style="width: 40%; height: 430px" class="card" id="bar"></div>
      <div style="width: 60%; height: 430px" class="card" id="pieCourse"></div>
    </div>
  </div>
</template>

<script>
import * as echarts from "echarts";

let pieEquipmentOptions = {
  title: {
    text: '', // 主标题
    subtext: '', // 副标题
    left: 'right'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b} : {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '', // 鼠标移上去显示内容
      type: 'pie',
      radius: '50%',
      center: ['50%', '60%'],
      data: [
        { value: 1048, name: '瑞幸咖啡' }, // 示例数据：name表示维度，value表示对应的值
        { value: 735, name: '雀巢咖啡' },
        { value: 580, name: '星巴克咖啡' },
        { value: 484, name: '栖巢咖啡' },
        { value: 300, name: '小武哥咖啡' }
      ]
    }
  ]
}

let barCoachOptions = {
  title: {
    text: '', // 主标题
    subtext: '', // 副标题
    left: 'center'
  },
  xAxis: {
    type: 'category',
    data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'], // 示例数据：统计的维度（横坐标）
    axisLabel: {
      rotate: 30  // 斜着显示
    }
  },
  yAxis: {
    type: 'value'
  },
  tooltip: {
    trigger: 'item'
  },
  series: [
    {
      data: [120, 200, 150, 80, 70, 110, 130],
      type: 'bar',
      itemStyle: {
        color: function (params) {
          const colors = [
            '#5470C6',  // 优雅的蓝色
            '#91CC75',  // 清新的绿色
            '#FAC858',  // 温暖的黄色
            '#EE6666',  // 柔和的红色
            '#73C0DE',  // 浅蓝色
            '#3BA272',  // 翠绿色
            '#FC8452'   // 橙色
          ];
          return colors[params.dataIndex];
        }
      }
    }
  ]
}

let pieCourseOptions = {
  title: {
    text: '',
    subtext: '',
    left: 'left'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b} : {c} ({d}%)',
    show: true
  },
  legend: {
    orient: 'vertical',
    left: 'right'
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: '50%',
      center: ['50%', '60%'],
      data: []
    }
  ]
}

// 修改折线图配置
let lineChartOptions = {
  title: {
    text: '',
    subtext: '',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: [],
    axisLabel: {
      rotate: 30  // 斜着显示月份
    }
  },
  yAxis: {
    type: 'value',
    name: 'Amounts (￡)'
  },
  series: [{
    data: [],
    type: 'line',
    smooth: true,  // 平滑曲线
    lineStyle: {
      color: '#5470C6',
      width: 3
    },
    symbol: 'circle',  // 数据点样式
    symbolSize: 8,
    itemStyle: {
      color: '#5470C6'
    },
    areaStyle: {  // 添加区域填充
      color: {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 0,
        y2: 1,
        colorStops: [{
          offset: 0,
          color: 'rgba(84,112,198,0.3)'
        }, {
          offset: 1,
          color: 'rgba(84,112,198,0.1)'
        }]
      }
    },
    markPoint: {  // 添加标记点，显示最大值和最小值
      data: [
        { type: 'max', name: 'max' },
        { type: 'min', name: 'min' }
      ]
    }
  }]
}

export default {
  name: 'Home',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}')
    }
  },
  created() {
    this.loadEquipment()
    this.loadCourse()
    this.loadCoach()
    this.loadWeeklyOrders()
  },
  methods: {
    loadEquipment() {
      this.$request.get('/echarts/equipmentData').then(res => {
        if (res.code === '200') {
          let chartDom = document.getElementById('pie1');
          let myChart = echarts.init(chartDom);
          pieEquipmentOptions.title.text = res.data.text
          pieEquipmentOptions.title.subtext = res.data.subtext
          pieEquipmentOptions.series[0].name = res.data.name
          pieEquipmentOptions.series[0].data = res.data.data
          myChart.setOption(pieEquipmentOptions);
        }
      })
    },
    loadCourse() {
      this.$request.get('/echarts/courserData').then(res => {
        if (res.code === '200') {
          let chartDom = document.getElementById('pieCourse');
          let myChart = echarts.init(chartDom);
          pieCourseOptions.title.text = res.data.text
          pieCourseOptions.title.subtext = res.data.subtext
          pieCourseOptions.series[0].name = res.data.name
          pieCourseOptions.series[0].data = res.data.data
          myChart.setOption(pieCourseOptions);
        }
      })
    },
    loadCoach() {
      this.$request.get('/echarts/coachData').then(res => {
        if (res.code === '200') {
          let chartDom = document.getElementById('bar');
          let myChart = echarts.init(chartDom);
          barCoachOptions.title.text = res.data.text
          barCoachOptions.title.subtext = res.data.subtext
          barCoachOptions.xAxis.data = res.data.xAxis
          barCoachOptions.series[0].data = res.data.yAxis
          myChart.setOption(barCoachOptions)
        }
      })
    },
    loadWeeklyOrders() {
      this.$request.get('/echarts/weeklyOrderData').then(res => {
        if (res.code === '200') {
          let chartDom = document.getElementById('lineChart');
          let myChart = echarts.init(chartDom);
          lineChartOptions.title.text = res.data.text
          lineChartOptions.title.subtext = res.data.subtext
          lineChartOptions.xAxis.data = res.data.xAxis
          lineChartOptions.series[0].data = res.data.yAxis

          // 格式化x轴标签，将YYYY-MM格式转换为更友好的显示
          lineChartOptions.xAxis.axisLabel = {
            formatter: function (value) {
              // 将YYYY-MM转换为YYYY年MM月
              const parts = value.split('-');
              return parts[0] + '/' + parts[1];
            },
            rotate: 30
          }

          myChart.setOption(lineChartOptions);
        }
      })
    }
  }
}
</script>

<style scoped>
.card {
  padding: 20px;
  margin: 5px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>
