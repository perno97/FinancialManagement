package com.perno97.financialmanagement.utils

import android.util.Log
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

class MonthValueFormatter(
    private val firstColumn: String?,
    private val labels: ArrayList<LocalDate>
) : ValueFormatter() {
    private val logTag = "MonthValueFormatter"

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        if (value < 0 || value >= labels.size) {
            Log.e(logTag, "Value --> $value")
            return ""
        }
        val index = if (firstColumn != null) value - 1 else value
        return if (index < 0f)
            firstColumn!!
        else {
            val date = labels[index.toInt()]
            date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }
    }
}
