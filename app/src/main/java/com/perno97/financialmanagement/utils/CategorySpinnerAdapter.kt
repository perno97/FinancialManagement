package com.perno97.financialmanagement.utils

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.Category

class CategorySpinnerAdapter(private val c: Context) : BaseAdapter() {

    private val items = ArrayList<Category>()

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(p0: Int): Any {
        return items[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view = LayoutInflater.from(c).inflate(R.layout.spinner_row, p2, false)

        val v = view.findViewById<TextView>(R.id.spinnerCategoryText)
        val cat = items[p0]
        v.setBackgroundColor(Color.parseColor(cat.color))
        v.text = cat.name
        return view
    }

    fun clear() {
        items.clear()
    }

    fun add(cat: Category) {
        items.add(cat)
    }
}