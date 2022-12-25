package com.perno97.financialmanagement.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import kotlin.math.roundToInt

class BarChartPeriodValueFormatter(private val labels: ArrayList<LocalDate>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val index = (value / 2).roundToInt()
        if (index >= labels.size) return ""
        val date = labels[index]
        return "${date.dayOfMonth}/${date.monthValue}/${date.year}"
    }
}
