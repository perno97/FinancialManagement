package com.perno97.financialmanagement.utils

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

class DecimalDigitsInputFilter(
    private val digitsBeforeZero: Int,
    private val digitsAfterZero: Int
) : InputFilter {
    private val mPattern: Pattern =
        Pattern.compile("^[0-9]+([.][0-9]{0,2})?$")

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        if (source != null) {
            val string = dest.toString().replaceRange(dstart, dend, source.subSequence(start, end))
            val matcher = mPattern.matcher(string)
            if (!matcher.matches())
                return String.format("%.2f", string.toFloat())
        }
        return null
    }
}