package com.perno97.financialmanagement.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.AmountWithDate
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.databinding.FragmentCategoryDetailsBinding
import com.perno97.financialmanagement.utils.PeriodState
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import com.perno97.financialmanagement.viewmodels.PositiveNegativeSums
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class CategoryDetailsFragment(private val categoryId: Long) :
    Fragment() {
    private val logTag = "CategoryDetailsFragment"
    private val numberOfColumnsInGraphs = 8

    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    private var _binding: FragmentCategoryDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

    private lateinit var dateFrom: LocalDate
    private lateinit var dateTo: LocalDate
    private var state = PeriodState.MONTH
    private var datePickerSelection: Pair<Long, Long>? = null
    private var categoryFilters: List<Category> = listOf()
    private lateinit var category: Category
    private var expense = 0f
    private var categoriesExpenses: Map<Category, PositiveNegativeSums>? = null
    private var weekStartOffset = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(logTag, "Called onCreateView")
        _binding = FragmentCategoryDetailsBinding.inflate(inflater, container, false)

        val f = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val t = f.with(TemporalAdjusters.previous(firstDayOfWeek))
        // Count days from the next sunday to the previous firstDayOfWeek
        // Workaround for having custom first day of week in SQLite
        weekStartOffset = ChronoUnit.DAYS.between(f, t).toInt().absoluteValue

        appViewModel.getCategory(categoryId).observe(viewLifecycleOwner) {
            if (it != null) {
                category = it
                loadUiData()
            } else {
                Log.e(logTag, "No category found with id: $categoryId")
            }
        }

        initGraphs()

        return binding.root
    }

    private fun initGraphs() {
        val expChart = binding.expensesLineChart
        val gainsChart = binding.incomesLineChart
        expChart.setExtraOffsets(10f, 0f, 10f, 10f)
        expChart.description.isEnabled = false
        gainsChart.setExtraOffsets(10f, 0f, 10f, 10f)
        gainsChart.description.isEnabled = false

        val xAxisExp = expChart.xAxis
        xAxisExp.setDrawGridLines(false)
        xAxisExp.setDrawAxisLine(false)
        xAxisExp.position = XAxis.XAxisPosition.BOTTOM
        xAxisExp.labelRotationAngle = -90f

        val xAxisGain = gainsChart.xAxis
        xAxisGain.setDrawGridLines(false)
        xAxisGain.setDrawAxisLine(false)
        xAxisGain.position = XAxis.XAxisPosition.BOTTOM
        xAxisGain.labelRotationAngle = -90f

        val leftAxisExp = expChart.axisLeft
        leftAxisExp.isEnabled = false
        val leftAxisGain = gainsChart.axisLeft
        leftAxisGain.isEnabled = false

        val rightAxisExp = expChart.axisRight
        rightAxisExp.isEnabled = false
        val rightAxisGain = gainsChart.axisRight
        rightAxisGain.isEnabled = false
    }

    private fun loadGraphsData() {
        Log.i(logTag, "Called loadGraphsData()")
        val catList = categoryFilters.map { c -> c.name } + listOf(category.name)
        binding.noDataCategoryDetails.visibility = View.GONE
        when (state) {
            // ----------------- DAY -----------------
            PeriodState.DAY -> Log.e(logTag, "Period day not defined in this screen")
            // ----------------- WEEK ----------------
            PeriodState.WEEK -> appViewModel.getCategoriesMovementsWeek(
                catList,
                weekStartOffset,
                numberOfColumnsInGraphs
            ).observe(viewLifecycleOwner) { categoryWithMovements ->
                updateLineGraphsWeek(categoryWithMovements)
            }
            // ---------------- MONTH ----------------
            PeriodState.MONTH -> appViewModel.getCategoriesMovementsMonth(
                catList,
                LocalDate.now(),
                numberOfColumnsInGraphs
            ).observe(viewLifecycleOwner) { categoryWithMovements ->
                updateLineGraphsMonth(categoryWithMovements)
            }
            // ---------------- PERIOD ----------------
            PeriodState.PERIOD -> appViewModel.getCategoriesMovementsPeriod(
                catList,
                dateFrom,
                dateTo
            ).observe(viewLifecycleOwner) { categoryWithMovements ->
                updateLineGraphsPeriod(categoryWithMovements)
            }
            // ----------------------------------------
        }
        updateHorizontalGraphs()
    }

    private fun updateHorizontalGraphs() {
        Log.i(logTag, "Called updateHorizontalGraphs()")
        binding.expensesProgressList.removeAllViews()
        binding.incomesProgressList.removeAllViews()
        if (categoriesExpenses == null || categoriesExpenses!!.isEmpty()) {
            Log.e(logTag, "No category movements to show in horizontal graphs")
        } else {
            var maxGain = 0f
            var maxExpense = 0f
            for (c in categoriesExpenses!!.keys) {
                if (categoriesExpenses!![c] != null && maxGain < categoriesExpenses!![c]!!.positive) {
                    maxGain = categoriesExpenses!![c]!!.positive
                }
                if (categoriesExpenses!![c] != null && categoriesExpenses!![c]!!.negative < maxExpense)
                    maxExpense = categoriesExpenses!![c]!!.negative
            }
            maxExpense = maxExpense.absoluteValue
            if (maxGain != 0f) {
                for (c in categoriesExpenses!!.keys) {
                    if (categoriesExpenses!![c] == null) {
                        Log.e(
                            logTag,
                            "No expense progress data found for category ${category.name}"
                        )
                        break
                    }
                    val currentCatGain = categoriesExpenses!![c]!!.positive
                    // Load layout
                    val viewCatProgressLayoutGain =
                        layoutInflater.inflate(
                            R.layout.category_progress_minimal,
                            binding.incomesProgressList,
                            false
                        )
                    // Add progress layout to container
                    binding.incomesProgressList.addView(viewCatProgressLayoutGain)
                    // Load progress bar
                    val progressBarGain =
                        viewCatProgressLayoutGain.findViewById<LinearProgressIndicator>(R.id.progressBarCategoryBudget)

                    // Set category name for progress bar
                    viewCatProgressLayoutGain.findViewById<TextView>(R.id.txtProgressMinimalCategoryName).text =
                        c.name
                    // Set progress bar progress and color
                    progressBarGain.progress = (currentCatGain * 100 / maxGain).roundToInt()
                    progressBarGain.indicatorColor[0] = Color.parseColor(c.color)
                    // Set category budget of category line
                    viewCatProgressLayoutGain.findViewById<TextView>(R.id.txtCategoryBudget).text =
                        getString(
                            R.string.euro_value,
                            currentCatGain
                        )
                }
            }
            if (maxExpense != 0f) {
                for (c in categoriesExpenses!!.keys) {
                    if (categoriesExpenses!![c] == null) {
                        Log.e(
                            logTag,
                            "No expense progress data found for category ${category.name}"
                        )
                        break
                    }
                    val currentCatExpenseAsPositive =
                        categoriesExpenses!![c]!!.negative.absoluteValue

                    // Load layout
                    val viewCatProgressLayoutExp =
                        layoutInflater.inflate(
                            R.layout.category_progress_minimal,
                            binding.expensesProgressList,
                            false
                        )
                    // Add progress layout to container
                    binding.expensesProgressList.addView(viewCatProgressLayoutExp)
                    // Load progress bar
                    val progressBarExp =
                        viewCatProgressLayoutExp.findViewById<LinearProgressIndicator>(R.id.progressBarCategoryBudget)
                    // Set category name for progress bar
                    viewCatProgressLayoutExp.findViewById<TextView>(R.id.txtProgressMinimalCategoryName).text =
                        c.name
                    // Set progress bar progress and color
                    //If multiplied budget is 0 and there's no expense then empty the progress bar
                    //If multiplied budget is 0 and there's some expense then fill the progress bar
                    progressBarExp.progress =
                        (currentCatExpenseAsPositive * 100 / maxExpense).roundToInt()
                    progressBarExp.indicatorColor[0] = Color.parseColor(c.color)
                    // Set category budget of category line
                    viewCatProgressLayoutExp.findViewById<TextView>(R.id.txtCategoryBudget).text =
                        getString(
                            R.string.euro_value,
                            currentCatExpenseAsPositive
                        )
                }
            }
        }
    }

    private fun updateLineGraphsMonth(
        data: Map<Category, List<AmountWithDate>>
    ) {
        Log.i(logTag, "Called updateLineGraphsMonth()")
        if (data.isEmpty()) {
            binding.expensesSectionCatDetails.visibility = View.GONE
            binding.incomesSectionCatDetails.visibility = View.GONE
            binding.noDataCategoryDetails.visibility = View.VISIBLE
        } else {
            val categories = data.keys.plusElement(category)
            val lineChartExp = binding.expensesLineChart
            val lineChartGain = binding.incomesLineChart
            val lineChartExpData = LineData()
            val lineChartGainData = LineData()
            val labels = arrayListOf<String>()
            var labelsLoaded = false
            var expensesFound = false
            var gainsFound = false
            for (category in categories) {
                val expEntries = arrayListOf<Entry>()
                val gainEntries = arrayListOf<Entry>()
                if (data[category] == null) {
                    Log.e(logTag, "No line chart data found for category ${category.name}")
                    break
                }
                var columnCount = 0
                var dateToCheckBefore: LocalDate
                var dateToCheckAfter: LocalDate
                while (columnCount < numberOfColumnsInGraphs) {
                    // x value must be between these dates to be in this column
                    dateToCheckAfter =
                        YearMonth.now().minusMonths(columnCount.toLong()).atDay(1)
                    dateToCheckBefore =
                        if (columnCount == 0) LocalDate.now()
                        else YearMonth.now().minusMonths(columnCount.toLong())
                            .atEndOfMonth()
                    val amountWithDateFound = data[category]!!.filter { amountWithDate ->
                        !amountWithDate.amountDate.isBefore(
                            dateToCheckAfter
                        ) && !amountWithDate.amountDate.isAfter(dateToCheckBefore)
                    }
                    if (amountWithDateFound.size > 1) {
                        Log.e(
                            logTag,
                            "Found multiple expenses for same column.\nCategory: ${category.name}, column: $columnCount"
                        )
                    }
                    val exp: Float
                    if (amountWithDateFound.isEmpty()) {
                        // Couldn't find a movement with date to put in this column, hence no date satisfying before and after boundaries
                        exp = 0f
                    } else {
                        // Found a value for this column for this category
                        exp = amountWithDateFound[0].expense.absoluteValue
                        if (exp != 0f) expensesFound = true
                    }

                    val gain: Float
                    if (amountWithDateFound.isEmpty()) {
                        // Couldn't find a movement with date to put in this column, hence no date satisfying before and after boundaries
                        gain = 0f
                    } else {
                        // Found a value for this column for this category
                        gain = amountWithDateFound[0].gain
                        if (gain != 0f) gainsFound = true
                    }
                    expEntries.add(
                        Entry(
                            columnCount.toFloat(),
                            exp
                        )
                    )
                    gainEntries.add(
                        Entry(
                            columnCount.toFloat(),
                            gain
                        )
                    )
                    // Adding date for formatter (to have x labels) only at first cycle
                    if (!labelsLoaded)
                        labels.add(
                            dateToCheckAfter.month.getDisplayName(
                                TextStyle.SHORT,
                                Locale.getDefault()
                            )
                        )
                    columnCount++
                }
                val expensesDataSet = LineDataSet(expEntries, category.name)
                val incomesDataSet = LineDataSet(gainEntries, category.name)
                expensesDataSet.color = Color.parseColor(category.color)
                incomesDataSet.color = Color.parseColor(category.color)
                lineChartExpData.addDataSet(expensesDataSet)
                lineChartGainData.addDataSet(incomesDataSet)
                labelsLoaded = true
            }
            val valueFormatter = IndexAxisValueFormatter(labels)
            if (!expensesFound)
                binding.expensesSectionCatDetails.visibility = View.GONE
            else {
                binding.expensesSectionCatDetails.visibility = View.VISIBLE
                val xAxisExp = lineChartExp.xAxis
                xAxisExp.labelCount = numberOfColumnsInGraphs
                xAxisExp.granularity = 1f

                xAxisExp.valueFormatter = valueFormatter
                lineChartExp.data = lineChartExpData

                lineChartExp.invalidate()
            }
            if (!gainsFound)
                binding.incomesSectionCatDetails.visibility = View.GONE
            else {
                binding.incomesSectionCatDetails.visibility = View.VISIBLE
                val xAxisGain = lineChartGain.xAxis
                xAxisGain.labelCount = numberOfColumnsInGraphs
                xAxisGain.granularity = 1f

                xAxisGain.valueFormatter = valueFormatter
                lineChartGain.data = lineChartGainData

                lineChartGain.invalidate()
            }
            if (!expensesFound && !gainsFound) {
                binding.noDataCategoryDetails.visibility = View.VISIBLE
            }
        }
    }

    private fun updateLineGraphsWeek(
        data: Map<Category, List<AmountWithDate>>
    ) {
        Log.i(logTag, "Called updateLineGraphsWeek()")
        if (data.isEmpty()) {
            binding.expensesSectionCatDetails.visibility = View.GONE
            binding.incomesSectionCatDetails.visibility = View.GONE
            binding.noDataCategoryDetails.visibility = View.VISIBLE
        } else {
            val lineChartExp = binding.expensesLineChart
            val lineChartGain = binding.incomesLineChart
            val lineChartExpData = LineData()
            val lineChartGainData = LineData()
            val labels = arrayListOf<String>()
            var labelsLoaded = false
            var expensesFound = false
            var gainsFound = false
            for (category in data.keys) {
                val expEntries = arrayListOf<Entry>()
                val gainEntries = arrayListOf<Entry>()
                if (data[category] == null) {
                    Log.e(logTag, "No line chart data found for category ${category.name}")
                    break
                }
                var columnCount = 0
                var dateToCheckBefore: LocalDate
                var dateToCheckAfter: LocalDate
                while (columnCount < numberOfColumnsInGraphs) {
                    // x value must be between these dates to be in this column
                    dateToCheckAfter =
                        LocalDate.now()
                            .with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                    dateToCheckBefore =
                        if (columnCount == 0) LocalDate.now()
                        else LocalDate.now()
                            .with(TemporalAdjusters.nextOrSame(firstDayOfWeek.minus(1)))
                    val amountWithDateFound = data[category]!!.filter { amountWithDate ->
                        !amountWithDate.amountDate.isBefore(
                            dateToCheckAfter
                        ) && !amountWithDate.amountDate.isAfter(dateToCheckBefore)
                    }
                    if (amountWithDateFound.size > 1) {
                        Log.e(
                            logTag,
                            "Found multiple expenses for same column.\nCategory: ${category.name}, column: $columnCount"
                        )
                    }
                    val exp: Float
                    if (amountWithDateFound.isEmpty()) {
                        // Couldn't find a movement with date to put in this column, hence no date satisfying before and after boundaries
                        exp = 0f
                    } else {
                        // Found a value for this column for this category
                        exp = amountWithDateFound[0].expense.absoluteValue
                        expensesFound = exp != 0f
                    }

                    val gain: Float
                    if (amountWithDateFound.isEmpty()) {
                        // Couldn't find a movement with date to put in this column, hence no date satisfying before and after boundaries
                        gain = 0f
                    } else {
                        // Found a value for this column for this category
                        gain = amountWithDateFound[0].gain
                        gainsFound = gain != 0f
                    }
                    expEntries.add(
                        Entry(
                            columnCount.toFloat(),
                            exp
                        )
                    )
                    gainEntries.add(
                        Entry(
                            columnCount.toFloat(),
                            gain
                        )
                    )

                    if (!labelsLoaded) {
                        val l = if (columnCount == 0)
                            getString(R.string.graph_label_current)
                        else
                            getString(R.string.graph_label_week, columnCount)
                        labels.add(l)
                    }
                    columnCount++
                }
                val expensesDataSet = LineDataSet(expEntries, category.name)
                val incomesDataSet = LineDataSet(gainEntries, category.name)
                expensesDataSet.color = Color.parseColor(category.color)
                incomesDataSet.color = Color.parseColor(category.color)
                lineChartExpData.addDataSet(expensesDataSet)
                lineChartGainData.addDataSet(incomesDataSet)
                labelsLoaded = true
            }

            val valueFormatter = IndexAxisValueFormatter(labels)
            if (!expensesFound)
                binding.expensesSectionCatDetails.visibility = View.GONE
            else {
                binding.expensesSectionCatDetails.visibility = View.VISIBLE
                val xAxisExp = lineChartExp.xAxis
                xAxisExp.labelCount = numberOfColumnsInGraphs
                xAxisExp.granularity = 1f

                xAxisExp.valueFormatter = valueFormatter
                lineChartExp.data = lineChartExpData

                lineChartExp.invalidate()
            }
            if (!gainsFound)
                binding.incomesSectionCatDetails.visibility = View.GONE
            else {
                binding.incomesSectionCatDetails.visibility = View.VISIBLE
                val xAxisGain = lineChartGain.xAxis
                xAxisGain.labelCount = numberOfColumnsInGraphs
                xAxisGain.granularity = 1f

                xAxisGain.valueFormatter = valueFormatter
                lineChartGain.data = lineChartGainData

                lineChartGain.invalidate()
            }
            if (!expensesFound && !gainsFound) {
                binding.noDataCategoryDetails.visibility = View.VISIBLE
            }
        }
    }

    private fun updateLineGraphsPeriod(
        data: Map<Category, List<AmountWithDate>>
    ) {
        Log.i(logTag, "Called updateLineGraphsPeriod()")
        if (data.isEmpty()) {
            binding.expensesSectionCatDetails.visibility = View.GONE
            binding.incomesSectionCatDetails.visibility = View.GONE
            binding.noDataCategoryDetails.visibility = View.VISIBLE
        } else {
            val lineChartExp = binding.expensesLineChart
            val lineChartGain = binding.incomesLineChart
            val lineChartExpData = LineData()
            val lineChartGainData = LineData()
            val labels = arrayListOf<String>()
            var labelsLoaded = false
            var expensesFound = false
            var gainsFound = false
            for (category in data.keys) {
                val expEntries = arrayListOf<Entry>()
                val gainEntries = arrayListOf<Entry>()
                if (data[category] == null) {
                    Log.e(logTag, "No line chart data found for category ${category.name}")
                    break
                }
                var currentColumnDate = dateTo
                var columnCount = 0
                while (!currentColumnDate.isBefore(dateFrom)) {
                    val amountWithDateFound = data[category]!!.filter { amountWithDate ->
                        amountWithDate.amountDate.isEqual(currentColumnDate)
                    }
                    if (amountWithDateFound.size > 1) {
                        Log.e(
                            logTag,
                            "Found multiple expenses for same column.\nCategory: ${category.name}, date: $currentColumnDate"
                        )
                    }
                    val exp: Float
                    if (amountWithDateFound.isEmpty()) {
                        // Couldn't find a movement with date to put in this column, hence no date satisfying before and after boundaries
                        exp = 0f
                    } else {
                        // Found a value for this column for this category
                        exp = amountWithDateFound[0].expense.absoluteValue
                        expensesFound = exp != 0f
                    }

                    val gain: Float
                    if (amountWithDateFound.isEmpty()) {
                        // Couldn't find a movement with date to put in this column, hence no date satisfying before and after boundaries
                        gain = 0f
                    } else {
                        // Found a value for this column for this category
                        gain = amountWithDateFound[0].gain
                        gainsFound = gain != 0f
                    }
                    expEntries.add(
                        Entry(
                            columnCount.toFloat(),
                            exp
                        )
                    )
                    gainEntries.add(
                        Entry(
                            columnCount.toFloat(),
                            gain
                        )
                    )
                    // Adding date for formatter (to have x labels) only at first cycle
                    if (!labelsLoaded)
                        labels.add("${currentColumnDate.dayOfMonth}/${currentColumnDate.monthValue}/${currentColumnDate.year}")
                    columnCount++
                    currentColumnDate = currentColumnDate.minusDays(1)
                }
                val expensesDataSet = LineDataSet(expEntries, category.name)
                val incomesDataSet = LineDataSet(gainEntries, category.name)
                expensesDataSet.color = Color.parseColor(category.color)
                incomesDataSet.color = Color.parseColor(category.color)
                lineChartExpData.addDataSet(expensesDataSet)
                lineChartGainData.addDataSet(incomesDataSet)
                labelsLoaded = true
            }

            val valueFormatter = IndexAxisValueFormatter(labels)
            val numberOfDays = ChronoUnit.DAYS.between(dateFrom, dateTo).toInt() + 1
            if (!expensesFound)
                binding.expensesSectionCatDetails.visibility = View.GONE
            else {
                binding.expensesSectionCatDetails.visibility = View.VISIBLE
                val xAxisExp = lineChartExp.xAxis
                xAxisExp.labelCount = numberOfDays
                xAxisExp.granularity = 1f

                xAxisExp.valueFormatter = valueFormatter
                lineChartExp.data = lineChartExpData

                lineChartExp.invalidate()
            }
            if (!gainsFound)
                binding.incomesSectionCatDetails.visibility = View.GONE
            else {
                binding.incomesSectionCatDetails.visibility = View.VISIBLE
                val xAxisGain = lineChartGain.xAxis
                xAxisGain.labelCount = numberOfDays
                xAxisGain.granularity = 1f

                xAxisGain.valueFormatter = valueFormatter
                lineChartGain.data = lineChartGainData

                lineChartGain.invalidate()
            }
            if (!expensesFound && !gainsFound) {
                binding.noDataCategoryDetails.visibility = View.VISIBLE
            }
        }
    }

    private fun loadUiData() {
        Log.i(logTag, "Called loadUiData()")
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appViewModel.uiState.collect {
                    Log.i(logTag, "Collected UI data")
                    dateTo = it.dateToCatDetails
                    dateFrom = it.dateFromCatDetails
                    state = it.stateCatDetails
                    datePickerSelection = it.datePickerSelectionCatDetails
                    categoryFilters = it.categoryFilters
                    updateFiltersList()
                    //loadCategoryExpenses()
                    when (state) {
                        PeriodState.WEEK -> setWeek()
                        PeriodState.MONTH -> setMonth()
                        PeriodState.PERIOD -> setPeriod(dateFrom, dateTo)
                        else -> {
                            setMonth()
                        }
                    }
                }
            }

        }
    }

    private fun loadCategoryExpenses() {
        Log.i(logTag, "Called loadCategoryExpenses()")
        appViewModel.getCategoryProgresses(dateFrom, dateTo)
            .observe(viewLifecycleOwner) {
                if (it != null) {
                    expense = if (it[category] != null) {
                        it[category]!!.negative
                    } else {
                        0f
                    }
                    categoriesExpenses =
                            // Assign list of categories expenses that are in filters and the main category
                        it.filter { (cat, _) ->
                            cat.name == category.name || categoryFilters.any { filter -> cat.name == filter.name }
                        }
                    updateCategoryProgress()
                    loadGraphsData()
                } else {
                    Log.e(logTag, "No category progresses found. No categories in database?")
                }
            }
    }

    private fun updateFiltersList() {
        Log.i(logTag, "Called updateFiltersList()")
        binding.filtersContainer.removeAllViews()
        for (filter in categoryFilters) {
            val itemView = layoutInflater.inflate(
                R.layout.category_filter_item,
                binding.filtersContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.categoryFilterColor).backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(filter.color))
            itemView.findViewById<TextView>(R.id.txtCategoryFilterName).text = filter.name
            binding.filtersContainer.addView(itemView)
        }
    }

    private fun updateCategoryProgress() {
        Log.i(logTag, "Called updateCategoryProgress()")
        binding.txtCategoryName.text = category.name
        val budgetMultiplier: Int = when (state) {
            PeriodState.DAY -> 1
            PeriodState.WEEK -> 7
            PeriodState.MONTH -> LocalDate.now().lengthOfMonth()
            PeriodState.PERIOD -> ChronoUnit.DAYS.between(dateFrom, dateTo)
                .toInt() + 1// Add 1 because between is exclusive
        } // Budget is defined as daily budget
        val multipliedBudget = category.budget * budgetMultiplier
        val currentCatExpenseAsPositive = expense.absoluteValue
        val progress =
            if (multipliedBudget == 0f && currentCatExpenseAsPositive == 0f) 0
            else if (multipliedBudget == 0f) 100 else
                (currentCatExpenseAsPositive * 100 / multipliedBudget).roundToInt()
        binding.progressBarCategoryBudget.indicatorColor[0] = Color.parseColor(category.color)
        binding.progressBarCategoryBudget.progress = progress
        binding.txtMaxCategoryBudget.text = getString(R.string.euro_value, multipliedBudget)
        binding.txtCurrentCategoryProgress.text =
            getString(R.string.euro_value, currentCatExpenseAsPositive)
    }

    override fun onResume() {
        super.onResume()
        Log.i(logTag, "Called onResume()")
        initListeners()
        updateFiltersList()
    }

    override fun onStop() {
        super.onStop()
        Log.i(logTag, "Called onStop()")
        appViewModel.setCatDetailsPeriod(
            dateFrom,
            dateTo,
            state,
            datePickerSelection
        ) //Saving UI state

        appViewModel.setCategoryFilters(listOf())
    }

    private fun setWeek() {
        Log.i(logTag, "Called setWeek()")
        binding.btnWeek.isEnabled = false
        binding.btnMonth.isEnabled = true
        dateTo = LocalDate.now()
        dateFrom = LocalDate.now().minusDays(7 * numberOfColumnsInGraphs.toLong())
        state = PeriodState.WEEK
        setTitle(
            "${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year} " +
                    "- ${dateTo.dayOfMonth}/${dateTo.monthValue}/${dateTo.year}"
        )
        loadCategoryExpenses()
    }

    private fun setMonth() {
        Log.i(logTag, "Called setMonth()")
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = false
        dateTo = LocalDate.now()
        dateFrom = LocalDate.now().minusMonths(numberOfColumnsInGraphs.toLong())
        state = PeriodState.MONTH
        val month = dateTo.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }
        setTitle("$month ${dateTo.year}")
        loadCategoryExpenses()
    }

    private fun setPeriod(from: LocalDate, to: LocalDate) {
        Log.i(logTag, "Called setPeriod()")
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = true
        dateTo = to
        dateFrom = from
        state = PeriodState.PERIOD
        setTitle(
            "${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year} " +
                    "- ${dateTo.dayOfMonth}/${dateTo.monthValue}/${dateTo.year}"
        )
        loadCategoryExpenses()
    }

    private fun initListeners() {
        Log.i(logTag, "Called initListeners()")
        binding.fabBtnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.imgBtnEdit.setOnClickListener {
            EditCategoryDialog(category, binding.txtCategoryName).show(
                childFragmentManager, EditCategoryDialog.TAG
            )
        }
        binding.fabAddFilterCat.setOnClickListener {
            appViewModel.setCatDetailsPeriod(dateFrom, dateTo, state, datePickerSelection)
            appViewModel.setCategoryFilters(categoryFilters)
            parentFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom,
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom
                )
                add(R.id.fragment_container_view, AddCategoryToFilterFragment(category))
                addToBackStack(null)
            }
        }
        binding.fabAddMovement.setOnClickListener {
            parentFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom,
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom
                )
                add<AddFinancialMovementFragment>(R.id.fragment_container_view)
                addToBackStack(null)
            }
        }
        binding.btnWeek.setOnClickListener {
            setWeek()
        }
        binding.btnMonth.setOnClickListener {
            setMonth()
        }
        binding.btnPeriod.setOnClickListener {
            binding.btnPeriod.isEnabled = false

            // Build
            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText(getString(R.string.select_period))
                    .setSelection(
                        datePickerSelection ?: Pair(
                            MaterialDatePicker.thisMonthInUtcMilliseconds(),
                            MaterialDatePicker.todayInUtcMilliseconds()
                        )
                    )
                    .build()
            // Add confirm listener
            dateRangePicker.addOnPositiveButtonClickListener { pair ->
                val from = Instant.ofEpochMilli(pair.first)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                val to = Instant.ofEpochMilli(pair.second)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                datePickerSelection = dateRangePicker.selection
                setPeriod(from, to)
            }
            dateRangePicker.addOnDismissListener { binding.btnPeriod.isEnabled = true }

            // Show
            dateRangePicker.show(parentFragmentManager, "rangeDatePickerDialog")
        }
    }

    private fun setTitle(title: String) {
        binding.txtSubtitle.text = title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(logTag, "Called onDestroyView()")
        _binding = null
    }
}