<template>
  <div ref="chartRef" class="radar-chart"></div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Object,
    required: true,
  },
  width: {
    type: String,
    default: '100%',
  },
  height: {
    type: String,
    default: '400px',
  },
})

const chartRef = ref(null)
let chartInstance = null

const dimensionLabels = {
  basic: '基础条件',
  hard_skill: '硬技能',
  soft_skill: '软技能',
  potential: '发展潜力',
}

function initChart() {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

function updateChart() {
  if (!chartInstance || !props.data) return

  const indicators = Object.keys(dimensionLabels).map((key) => ({
    name: dimensionLabels[key],
    max: 1,
  }))

  const values = Object.keys(dimensionLabels).map((key) => props.data[key] ?? 0)

  const option = {
    color: ['#3B82D9'],
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#DBEAFE',
      borderWidth: 1,
      textStyle: { color: '#1e3a5f', fontSize: 13 },
      formatter: (params) => {
        return params.value
          .map((v, i) => `${indicators[i].name}: ${(v * 100).toFixed(0)}%`)
          .join('<br/>')
      },
    },
    radar: {
      indicator: indicators,
      shape: 'polygon',
      splitNumber: 5,
      center: ['50%', '50%'],
      radius: '70%',
      axisName: {
        color: '#334155',
        fontSize: 14,
        fontWeight: 600,
      },
      splitArea: {
        areaStyle: {
          color: [
            'rgba(59, 130, 217, 0.02)',
            'rgba(59, 130, 217, 0.05)',
            'rgba(59, 130, 217, 0.08)',
            'rgba(59, 130, 217, 0.11)',
            'rgba(59, 130, 217, 0.14)',
          ],
        },
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(59, 130, 217, 0.15)',
        },
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(59, 130, 217, 0.2)',
        },
      },
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: values,
            name: '匹配度',
            areaStyle: {
              color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
                { offset: 0, color: 'rgba(59, 130, 217, 0.35)' },
                { offset: 1, color: 'rgba(59, 130, 217, 0.08)' },
              ]),
            },
            lineStyle: {
              width: 2.5,
              color: '#3B82D9',
            },
            itemStyle: {
              color: '#3B82D9',
              borderColor: '#fff',
              borderWidth: 2,
            },
            symbol: 'circle',
            symbolSize: 8,
          },
        ],
      },
    ],
  }

  chartInstance.setOption(option)
}

watch(() => props.data, updateChart, { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})

function handleResize() {
  chartInstance?.resize()
}
</script>

<style scoped>
.radar-chart {
  width: v-bind(width);
  height: v-bind(height);
}
</style>
