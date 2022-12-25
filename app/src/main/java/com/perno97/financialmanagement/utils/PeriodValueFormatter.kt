package com.perno97.financialmanagement.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate

class PeriodValueFormatter(private val labels: ArrayList<LocalDate>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        if (value >= labels.size) return ""
        val date = labels[value.toInt()]
        return "${date.dayOfMonth}/${date.monthValue}/${date.year}"
    }
}
