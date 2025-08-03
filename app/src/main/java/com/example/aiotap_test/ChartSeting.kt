package com.example.aiotap_test

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

object ChartSetup {

    fun setupLineChart(
        chart: LineChart,
        entries: List<Entry>,
        label: String,
        lineColor: Int = Color.BLUE,
        circleColor: Int = Color.RED
    ) {
        // 데이터셋 생성
        val lineDataSet = LineDataSet(entries, label).apply {
            color = lineColor
            circleRadius = 4f
            setCircleColor(circleColor)
            lineWidth = 2f
            valueTextSize = 10f

            // ✅ 소수점 셋째자리까지 표시
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.3f", value)
                }
            }
        }

        val lineData = LineData(lineDataSet)
        chart.data = lineData

        // 차트 설명 제거
        chart.description = Description().apply { text = "" }

        // X축 설정
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
        }

        // Y축 설정
        chart.axisLeft.apply {
            setDrawGridLines(true)
            axisMinimum = 0f
        }
        chart.axisRight.isEnabled = false

        // 기타 설정
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
        chart.invalidate() // 차트 갱신
    }
}
