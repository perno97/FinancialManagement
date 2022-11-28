package com.perno97.financialmanagement.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.perno97.financialmanagement.R

class SpinnerAdapter(private val context: Context) : BaseAdapter() {

    private val colors = ArrayList<Int>()

    init {
        for(c in context.resources.getIntArray(R.array.category_colors_array)){
            colors.add(c)
        }
    }

    override fun getCount(): Int {
        return colors.size
    }

    override fun getItem(p0: Int): Any {
        return colors[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.color_spinner_row, p2, false)

        view.findViewById<TextView>(R.id.spinnerText).setBackgroundColor(colors.get(p0))
        return view
    }

}