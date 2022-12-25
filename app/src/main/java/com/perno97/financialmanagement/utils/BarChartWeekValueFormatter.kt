package com.perno97.financialmanagement.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.math.roundToInt

class BarChartWeekValueFormatter : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val index = (value / 2).roundToInt()
        return if (index == 0) "Current" else String.format("-%d weeks", value.toInt())
    }
}
