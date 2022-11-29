package com.perno97.financialmanagement

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.time.LocalDate
import java.util.*

class DatePickerFragment(private val fromDate: LocalDate?, private val toUpdate: ICustomPeriod) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(requireContext(), this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        if(fromDate == null){
            DatePickerFragment(LocalDate.of(year,month,day), toUpdate).show(
                parentFragmentManager, TAG
            )
        }
        else {
            toUpdate.setCustomPeriod(fromDate, LocalDate.of(year,month,day))
        }
    }

    companion object {
        const val TAG = "AddNewCategoryDialog"
    }
}