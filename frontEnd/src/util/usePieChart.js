import * as echarts from 'echarts'

export function usePieChart({
  el,           // DOM 元素或 ref
  dataRef ,
  title = '',
  width = '100%',
  height = '400px',
  legendPosition = 'top',
  showLabel = true,
  isDonut = false,
  colors = ['#5470C6', '#91CC75', '#FAC858', '#EE6666', '#73C0DE', '#3BA272', '#FC8452', '#9A60B4']
}) {
  let chartInstance = null

  const initChart = () => {
    if (!el.value) return
    chartInstance = echarts.init(el.value)
    updateChart()
  }

  const updateChart = () => {
    if (!chartInstance) return

    const option = {
      title: {
        text: title,
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: ['left', 'right'].includes(legendPosition) ? 'vertical' : 'horizontal',
        [legendPosition]: legendPosition,
        left: ['top', 'bottom'].includes(legendPosition) ? 'center' : legendPosition,
        data:dataRef.value.map(item => item.name)
      },
      series: [
        {
          name: title || '数据占比',
          type: 'pie',
          radius: isDonut ? ['40%', '70%'] : '70%',
          center: ['50%', '50%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: showLabel,
            formatter: '{b}: {d}%'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '16',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: showLabel
          },
          data: dataRef.value.map((item, index) => ({
            ...item,
            itemStyle: {
              color: colors[index % colors.length]
            }
          }))
        }
      ]
    }

    chartInstance.setOption(option)
  }

  const resizeChart = () => {
    chartInstance?.resize()
  }

  const disposeChart = () => {
    chartInstance?.dispose()
  }

  return {
    initChart,
    updateChart,
    resizeChart,
    disposeChart
  }
}