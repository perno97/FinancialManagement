package com.perno97.financialmanagement.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import kotlin.math.roundToInt

class BarChartMonthValueFormatter(private val labels: ArrayList<LocalDate>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val index = (value / 2).roundToInt()
        val date = labels[index]
        return date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
}
