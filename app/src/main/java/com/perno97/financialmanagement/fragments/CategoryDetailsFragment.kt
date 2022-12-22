package com.perno97.financialmanagement.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.AmountWithDate
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.databinding.FragmentCategoryDetailsBinding
import com.perno97.financialmanagement.utils.MonthValueFormatter
import com.perno97.financialmanagement.utils.PeriodState
import com.perno97.financialmanagement.utils.PeriodValueFormatter
import com.perno97.financialmanagement.utils.WeekValueFormatter
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import com.perno97.financialmanagement.viewmodels.PositiveNegativeSums
import kotlinx.coroutines.launch
import java.time.*
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class CategoryDetailsFragment(private val categoryName: String) :
    Fragment() {
    private val logTag = "CategoryDetailsFragment"
    private val numberOfColumnsInGraphs = 12

    /**
     * Connection to data
     */
    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

    /**
     * Binding to layout resource
     */
    private var _binding: FragmentCategoryDetailsBinding? = null

    private lateinit var dateFrom: LocalDate
    private lateinit var dateTo: LocalDate
    private var state = PeriodState.MONTH
    private var datePickerSelection: Pair<Long, Long>? = null
    private var categoryFilters: List<Category> = listOf()
    private lateinit var category: Category
    private var expense = 0f
    private var categoriesExpenses: Map<Category, PositiveNegativeSums>? = null
    private val weekStartOffset = ChronoUnit.DAYS.between(
        LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)),
        LocalDate.now().with(TemporalAdjusters.previous(firstDayOfWeek))
    ).toInt().absoluteValue

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.e(logTag, "Called onCreateView")
        _binding = FragmentCategoryDetailsBinding.inflate(inflater, container, false)

        appViewModel.getCategory(categoryName).observe(viewLifecycleOwner) {
            if (it != null) {
                category = it
                loadUiData()
            } else {
                Log.e(logTag, "No category found with name: $categoryName")
            }
        }

        val expChart = binding.expensesLineChart
        val gainsChart = binding.incomesLineChart
        expChart.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_30))
        expChart.setExtraOffsets(10f, 0f, 10f, 30f)
        expChart.description.isEnabled = false
        gainsChart.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_30))
        gainsChart.setExtraOffsets(10f, 0f, 10f, 30f)
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

        return binding.root
    }

    private fun loadGraphsData() {
        val catList = categoryFilters.map { c -> c.name } + listOf(category.name)
        when (state) {
            // ----------------- DAY -----------------
            PeriodState.DAY -> Log.e(logTag, "Period day not defined in this screen")
            // ----------------- WEEK ----------------
            PeriodState.WEEK -> appViewModel.getCategoriesExpensesWeek(
                catList,
                weekStartOffset
            ).observe(viewLifecycleOwner) { categoryWithExpense ->
                updateLineGraphsWeek(categoryWithExpense)
            }
            // ---------------- MONTH ----------------
            PeriodState.MONTH -> appViewModel.getCategoriesExpensesMonth(
                catList,
                LocalDate.now()
            ).observe(viewLifecycleOwner) { categoryWithExpense ->
                updateLineGraphsMonth(categoryWithExpense)
            }
            // ---------------- PERIOD ----------------
            PeriodState.PERIOD -> appViewModel.getCategoriesExpensesPeriod(
                catList,
                dateFrom,
                dateTo
            ).observe(viewLifecycleOwner) { categoryWithExpense ->
                updateLineGraphsPeriod(categoryWithExpense)
            }
            // ----------------------------------------
        }
        updateHorizontalGraphs()
    }

    private fun updateHorizontalGraphs() {
        binding.expensesProgressList.removeAllViews()
        binding.incomesProgressList.removeAllViews()
        val budgetMultiplier: Int = when (state) {
            PeriodState.DAY -> {
                Log.e(logTag, "Period day not defined in this screen")
                1
            }
            PeriodState.WEEK -> 7
            PeriodState.MONTH -> LocalDate.now().lengthOfMonth()
            PeriodState.PERIOD -> ChronoUnit.DAYS.between(dateFrom, dateTo)
                .toInt() + 1 // Add 1 because between is exclusive
        } // Budget is defined as daily budget
        if (categoriesExpenses == null || categoriesExpenses!!.isEmpty()) {
            //TODO usare snackbar o scrivere nel grafico o non fare niente
            Log.e(logTag, "No category movements to show in horizontal graphs")
        } else {
            //var currentSum = 0f
            //var budgetsSum = 0f
            var maxGain = 0f
            for (c in categoriesExpenses!!.keys) {
                if (categoriesExpenses!![c] != null && maxGain < categoriesExpenses!![c]!!.positive) {
                    maxGain = categoriesExpenses!![c]!!.positive
                }
            }
            for (c in categoriesExpenses!!.keys) {
                val multipliedBudget = c.budget * budgetMultiplier
                if (categoriesExpenses!![c] == null) {
                    Log.e(logTag, "No expense progress data found for category ${category.name}")
                    break
                }
                val currentCatExpenseAsPositive = categoriesExpenses!![c]!!.negative.absoluteValue
                val currentCatGain = categoriesExpenses!![c]!!.positive
                if (currentCatExpenseAsPositive != 0f || currentCatGain != 0f) {
                    if (currentCatExpenseAsPositive != 0f) {
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
                        viewCatProgressLayoutExp.findViewById<TextView>(R.id.txtProgMinimalCategoryName).text =
                            c.name
                        // Set progress bar progress and color
                        progressBarExp.progress =
                            if (multipliedBudget == 0f) 100 else // If multiplied budget is 0 then fill progress bar (progress 100/100)
                                (currentCatExpenseAsPositive * 100 / multipliedBudget).roundToInt()
                        progressBarExp.indicatorColor[0] = Color.parseColor(c.color)
                        // Set category budget of category line
                        viewCatProgressLayoutExp.findViewById<TextView>(R.id.txtCategoryBudget).text =
                            getString(
                                R.string.current_on_max_budget,
                                currentCatExpenseAsPositive,
                                multipliedBudget
                            )
                    }
                    if (currentCatGain != 0f) {
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
                        viewCatProgressLayoutGain.findViewById<TextView>(R.id.txtProgMinimalCategoryName).text =
                            c.name
                        // Set progress bar progress and color
                        progressBarGain.progress =
                            if (maxGain == 0f) currentCatGain.roundToInt() else (currentCatGain * 100 / maxGain).roundToInt()
                        progressBarGain.indicatorColor[0] = Color.parseColor(c.color)
                        // Set category budget of category line
                        viewCatProgressLayoutGain.findViewById<TextView>(R.id.txtCategoryBudget).text =
                            getString(
                                R.string.euro_value,
                                currentCatGain
                            )
                    }
                }
            }
        }
    }

    private fun updateLineGraphsMonth(
        data: Map<Category, List<AmountWithDate>>
    ) {
        if (data.isEmpty()) {
            //TODO usare snackbar o scrivere nel grafico o non fare niente
            Log.e(logTag, "No category movements to show in line graphs")
        } else {
            val lineChartExp = binding.expensesLineChart
            val lineChartGain = binding.incomesLineChart
            val lineChartExpData = LineData()
            val lineChartGainData = LineData()
            val labels = arrayListOf<LocalDate>()
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
                        /* Log.e(
                            logTag,
                            "Found multiple expenses for same column.\nCategory: ${category.name}, column: $columnCount"
                        ) */
                        throw Exception(
                            "Found multiple expenses for same column.\n" +
                                    "Category: ${category.name}, column: $columnCount"
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
                        labels.add(dateToCheckAfter)
                    columnCount++
                }
                val expensesDataSet = LineDataSet(expEntries, category.name)
                val incomesDataSet = LineDataSet(gainEntries, category.name)
                expensesDataSet.color = Color.parseColor(category.color)
                incomesDataSet.color = Color.parseColor(category.color)
                // TODO styling dataset
                lineChartExpData.addDataSet(expensesDataSet)
                lineChartGainData.addDataSet(incomesDataSet)
                labelsLoaded = true
            }
            val valueFormatter = MonthValueFormatter(labels)
            if (!expensesFound)
                binding.expensesSectionCatDetails.visibility = View.GONE
            else {
                binding.expensesSectionCatDetails.visibility = View.VISIBLE
                val xAxisExp = lineChartExp.xAxis
                xAxisExp.labelCount = numberOfColumnsInGraphs
                xAxisExp.granularity = 1f

                lineChartExp.xAxis.valueFormatter = valueFormatter
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

                lineChartGain.xAxis.valueFormatter = valueFormatter
                lineChartGain.data = lineChartGainData

                lineChartGain.invalidate()
            }
        }
    }

    private fun updateLineGraphsWeek(
        data: Map<Category, List<AmountWithDate>>
    ) {
        if (data.isEmpty()) {
            //TODO usare snackbar o scrivere nel grafico o non fare niente
            Log.e(logTag, "No category movements to show in line graphs")
        } else {
            val lineChartExp = binding.expensesLineChart
            val lineChartGain = binding.incomesLineChart
            val lineChartExpData = LineData()
            val lineChartGainData = LineData()
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
                        /*Log.e(
                            logTag,
                            "Found multiple expenses for same column.\nCategory: ${category.name}, column: $columnCount"
                        )*/
                        throw Exception("Found multiple expenses for same column.\nCategory: ${category.name}, column: $columnCount")
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
                    columnCount++
                }
                val expensesDataSet = LineDataSet(expEntries, category.name)
                val incomesDataSet = LineDataSet(gainEntries, category.name)
                expensesDataSet.color = Color.parseColor(category.color)
                incomesDataSet.color = Color.parseColor(category.color)
                // TODO styling dataset
                lineChartExpData.addDataSet(expensesDataSet)
                lineChartGainData.addDataSet(incomesDataSet)
            }

            val valueFormatter = WeekValueFormatter()
            if (!expensesFound)
                binding.expensesSectionCatDetails.visibility = View.GONE
            else {
                binding.expensesSectionCatDetails.visibility = View.VISIBLE
                val xAxisExp = lineChartExp.xAxis
                xAxisExp.labelCount = numberOfColumnsInGraphs
                xAxisExp.granularity = 1f

                lineChartExp.xAxis.valueFormatter = valueFormatter
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

                lineChartGain.xAxis.valueFormatter = valueFormatter
                lineChartGain.data = lineChartGainData

                lineChartGain.invalidate()
            }
        }
    }

    private fun updateLineGraphsPeriod(
        data: Map<Category, List<AmountWithDate>>
    ) {
        if (data.isEmpty()) {
            //TODO usare snackbar o scrivere nel grafico o non fare niente
            Log.e(logTag, "No category movements to show in line graphs")
        } else {
            val lineChartExp = binding.expensesLineChart
            val lineChartGain = binding.incomesLineChart
            val lineChartExpData = LineData()
            val lineChartGainData = LineData()
            val labels = arrayListOf<LocalDate>()
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
                        labels.add(currentColumnDate)
                    columnCount++
                    currentColumnDate = currentColumnDate.minusDays(1)
                }
                val expensesDataSet = LineDataSet(expEntries, category.name)
                val incomesDataSet = LineDataSet(gainEntries, category.name)
                expensesDataSet.color = Color.parseColor(category.color)
                incomesDataSet.color = Color.parseColor(category.color)
                // TODO styling dataset
                lineChartExpData.addDataSet(expensesDataSet)
                lineChartGainData.addDataSet(incomesDataSet)
                labelsLoaded = true
            }

            val valueFormatter = PeriodValueFormatter(labels)
            val numberOfDays = ChronoUnit.DAYS.between(dateFrom, dateTo).toInt() + 1
            if (!expensesFound)
                binding.expensesSectionCatDetails.visibility = View.GONE
            else {
                binding.expensesSectionCatDetails.visibility = View.VISIBLE
                val xAxisExp = lineChartExp.xAxis
                xAxisExp.labelCount = numberOfDays
                xAxisExp.granularity = 1f

                lineChartExp.xAxis.valueFormatter = valueFormatter
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

                lineChartGain.xAxis.valueFormatter = valueFormatter
                lineChartGain.data = lineChartGainData

                lineChartGain.invalidate()
            }
        }
    }

    private fun loadUiData() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appViewModel.uiState.collect {
                    Log.e(logTag, "Collected UI data")
                    dateTo = it.dateToCatDetails ?: LocalDate.now()
                    dateFrom = it.dateFromCatDetails ?: LocalDate.of(dateTo.year, dateTo.month, 1)
                    state = it.stateCatDetails ?: PeriodState.MONTH
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
        appViewModel.getCategoryProgresses(dateFrom, dateTo)
            .observe(viewLifecycleOwner) {
                if (it?.get(category) != null) {
                    expense = it[category]!!.negative // Non viene caricata la spesa corretta
                    categoriesExpenses =
                            // Assign list of categories expenses that are in filters and the main category
                        it.filter { (cat, _) ->
                            cat.name == category.name || categoryFilters.any { filter -> cat.name == filter.name }
                        }
                    updateCategoryProgress()
                    loadGraphsData()
                }
            }
    }

    private fun updateFiltersList() {
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
            if (multipliedBudget == 0f) 100 else
                (currentCatExpenseAsPositive * 100 / multipliedBudget).roundToInt()
        binding.progressBarCategoryBudget.indicatorColor[0] = Color.parseColor(category.color)
        binding.progressBarCategoryBudget.progress = progress
        binding.txtMaxCategoryBudget.text = getString(R.string.euro_value, multipliedBudget)
        binding.txtCurrentCategoryProgress.text =
            getString(R.string.euro_value, currentCatExpenseAsPositive)
    }

    override fun onResume() {
        super.onResume()
        initListeners()
        updateFiltersList()
    }

    override fun onStop() {
        super.onStop()
        appViewModel.setCatDetailsPeriod(
            dateFrom,
            dateTo,
            state,
            datePickerSelection
        ) //Saving UI state

        appViewModel.setCategoryFilters(listOf())
    }

    private fun setWeek() {//TODO provare periodi in questa schermata
        binding.btnWeek.isEnabled = false
        binding.btnMonth.isEnabled = true
        dateTo = LocalDate.now()
        dateFrom = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
        state = PeriodState.WEEK
        setTitle(
            "${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year} " +
                    "- ${dateTo.dayOfMonth}/${dateTo.monthValue}/${dateTo.year}"
        )
        loadCategoryExpenses()
        //updateCategoryProgress()
        //loadGraphsData()
    }

    private fun setMonth() {
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = false
        dateTo = LocalDate.now()
        dateFrom = LocalDate.of(dateTo.year, dateTo.month, 1)
        state = PeriodState.MONTH
        setTitle("${dateTo.month} ${dateTo.year}")
        loadCategoryExpenses()
        //updateCategoryProgress() // TODO riorganizzare ordine in cui vengono caricati i dati
        //TODO expense non viene mai aggiornato, va aggiornato quando vengono caricate le spese
        //loadGraphsData()
    }

    private fun setPeriod(from: LocalDate, to: LocalDate) {
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
        //updateCategoryProgress()
        //loadGraphsData()
    }

    private fun initListeners() {
        binding.fabBtnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.imgBtnEdit.setOnClickListener {
            EditCategoryDialog(category).show(
                childFragmentManager, EditCategoryDialog.TAG
            )
        }
        binding.fabAddFilterCat.setOnClickListener {
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
                    .setTitleText("Select period")
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
        binding.txtTitle.text = title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}