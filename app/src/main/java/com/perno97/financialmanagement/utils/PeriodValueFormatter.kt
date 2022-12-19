package com.perno97.financialmanagement.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate

class PeriodValueFormatter(private val values: ArrayList<LocalDate>) : ValueFormatter() {
    override fun getPointLabel(entry: Entry?): String {
        return super.getPointLabel(entry)
    }

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val date = values[value.toInt()]
        return "${date.dayOfMonth}/${date.monthValue}/${date.year}"
    }
}
