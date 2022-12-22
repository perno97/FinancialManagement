package com.perno97.financialmanagement.utils

import android.text.InputFilter
import android.text.Spanned
import android.widget.EditText
import java.util.regex.Pattern

class DecimalDigitsInputFilter(private val view: EditText) : InputFilter {
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
        if (source != null && dest != null && (source.isNotEmpty() || dest.isNotEmpty())) {
            val string = dest.toString().replaceRange(dstart, dend, source.subSequence(start, end))
            if (string.isNotEmpty()) {
                val matcher = mPattern.matcher(string)
                if (!matcher.matches()) {
                    /*var count = 0
                    for (char in string) {
                        if (char.isDigit() && char != '0') break
                        count++
                    }*/
                    view.setText(String.format("%.2f", string.toFloat()))
                }
            }
        }
        return null
    }
}