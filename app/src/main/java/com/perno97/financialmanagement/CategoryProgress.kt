package com.perno97.financialmanagement

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

// TODO delete this file

private const val CATEGORY_COLOR_DIMENSIONS = 15
private const val DEFAULT_COLOR = Color.RED
private const val DEFAULT_CURRENT_PROGRESS = 50
private const val DEFAULT_MAX_PROGRESS = 100

class CategoryProgress(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CategoryProgress,
            0, 0).apply {

            try {
                val constraintSet = ConstraintSet()
                val catName = TextView(context)
                val catColor = TextView(context)
                val maxProgress = TextView(context)
                val currentProgress = TextView(context)
                val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)

                // Category color
                catColor.layoutParams = LayoutParams(CATEGORY_COLOR_DIMENSIONS,CATEGORY_COLOR_DIMENSIONS)
                catColor.setBackgroundColor(getColor(R.styleable.CategoryProgress_categoryColor, DEFAULT_COLOR))
                constraintSet.connect(catColor.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                constraintSet.connect(catColor.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(catColor.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                // Category name
                catName.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                catName.text = getString(R.styleable.CategoryProgress_categoryName)
                constraintSet.connect(catName.id, ConstraintSet.BOTTOM, maxProgress.id, ConstraintSet.TOP)
                constraintSet.connect(catName.id, ConstraintSet.START, catColor.id, ConstraintSet.END)
                constraintSet.connect(catName.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                // Progress bar
                progressBar.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                progressBar.max = getInteger(R.styleable.CategoryProgress_maxProgress, DEFAULT_MAX_PROGRESS)
                progressBar.progress = getInteger(R.styleable.CategoryProgress_currentProgress, DEFAULT_CURRENT_PROGRESS)
                constraintSet.connect(progressBar.id, ConstraintSet.BOTTOM, currentProgress.id, ConstraintSet.TOP)
                constraintSet.connect(progressBar.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(progressBar.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                // Max progress text
                maxProgress.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                maxProgress.text = getString(R.styleable.CategoryProgress_maxProgress) //TODO mettere €
                constraintSet.connect(maxProgress.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                constraintSet.connect(maxProgress.id, ConstraintSet.START, catColor.id, ConstraintSet.END)
                constraintSet.connect(maxProgress.id, ConstraintSet.TOP, catName.id, ConstraintSet.BOTTOM)
                // Current progress text
                currentProgress.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                currentProgress.text = getString(R.styleable.CategoryProgress_currentProgress)
                constraintSet.connect(currentProgress.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                constraintSet.connect(currentProgress.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(currentProgress.id, ConstraintSet.TOP, progressBar.id, ConstraintSet.BOTTOM)
            } finally {
                recycle()
            }
        }
    }
}