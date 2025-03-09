<template>
  <div>
    <div style="display: flex">
      <!-- First row: Monthly Revenue(3) and Equipment Usage(1) -->
      <div style="width: 75%; height: 330px" class="card" id="lineChart"></div>
      <div style="width: 25%; height: 330px" class="card" id="pie1"></div>
    </div>
    <!-- Second row: Coach Reservation(3) and Course Sales(1) -->
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
    text: '', // Main title
    subtext: '', // Subtitle
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
      name: '', // Content displayed when hovering over the mouse
      type: 'pie',
      radius: '50%',
      center: ['50%', '60%'],
      data: [
        { value: 1048, name: '1' }, // Example data: name represents the dimension, and value represents the corresponding value
        { value: 735, name: '2' },
        { value: 580, name: '3' },
        { value: 484, name: '4' },
        { value: 300, name: '5' }
      ]
    }
  ]
}

let barCoachOptions = {
  title: {
    text: '', // Main title
    subtext: '', // Subtitle
    left: 'center'
  },
  xAxis: {
    type: 'category',
    data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'], // Example data: Statistics dimension (horizontal coordinate)
    axisLabel: {
      rotate: 30  // Display at an angle
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
            '#5470C6',  // Elegant blue
            '#91CC75',  // Fresh green
            '#FAC858',  // Warm yellow
            '#EE6666',  // Gentle red
            '#73C0DE',  // Light blue
            '#3BA272',  // Turquoise green
            '#FC8452'   // Orange
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

// Modify the line chart configuration
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
      rotate: 30  // Display at an angle
    }
  },
  yAxis: {
    type: 'value',
    name: 'Amounts (￡)'
  },
  series: [{
    data: [],
    type: 'line',
    smooth: true,  // Smooth curve
    lineStyle: {
      color: '#5470C6',
      width: 3
    },
    symbol: 'circle',  // Data point style
    symbolSize: 8,
    itemStyle: {
      color: '#5470C6'
    },
    areaStyle: {  // Add area fill
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
    markPoint: {  // Add mark point, display maximum and minimum values
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

          // Format the x-axis label, convert the YYYY-MM format to a more friendly display
          lineChartOptions.xAxis.axisLabel = {
            formatter: function (value) {
              // Convert YYYY-MM to YYYY year MM month
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
