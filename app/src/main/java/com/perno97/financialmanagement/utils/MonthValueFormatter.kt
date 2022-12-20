package com.perno97.financialmanagement.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

class MonthValueFormatter(private val values: ArrayList<LocalDate>) : ValueFormatter() {
    override fun getPointLabel(entry: Entry?): String {
        return super.getPointLabel(entry)
    }

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val date = values[value.toInt()]
        return date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
}
