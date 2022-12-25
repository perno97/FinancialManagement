package com.perno97.financialmanagement.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter

class WeekValueFormatter(private val firstColumn: String?) : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val index = if (firstColumn != null) value - 1 else value
        return if (index < 0f)
            firstColumn!!
        else if (index == 0f)
            "Current"
        else
            String.format("-%d weeks", value.toInt())
    }
}
