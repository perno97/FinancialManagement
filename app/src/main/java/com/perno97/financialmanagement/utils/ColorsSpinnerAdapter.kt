package com.perno97.financialmanagement.utils

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.perno97.financialmanagement.R

class ColorsSpinnerAdapter(private val context: Context) : BaseAdapter() {

    private val colors = ArrayList<String>()

    init {
        val array = context.resources.getStringArray(R.array.category_colors_array)
        for(c in array){
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

        view.findViewById<TextView>(R.id.spinnerText)
            .setBackgroundColor(Color.parseColor(colors.get(p0)))
        return view
    }

}