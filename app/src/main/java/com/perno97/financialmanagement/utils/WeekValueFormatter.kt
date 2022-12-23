package com.perno97.financialmanagement.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter

class WeekValueFormatter : ValueFormatter() {
    override fun getPointLabel(entry: Entry?): String {
        return super.getPointLabel(entry)
    }

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return if (value != 0f) String.format("-%d weeks", value.toInt()) else "Current"
    }
}
