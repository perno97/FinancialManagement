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
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.data.*
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
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt

class CategoryDetailsFragment(private val categoryName: String) :
    Fragment() {
    private val logTag = "CategoryDetailsFragment"

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
        return binding.root
    }

    private fun loadGraphsData() {
        when (state) {
            PeriodState.DAY -> Log.e(logTag, "Period day not defined in this screen")
            PeriodState.WEEK -> TODO()
            PeriodState.MONTH -> appViewModel.getCategoriesExpensesMonth(
                categoryFilters.map { c -> c.name } + listOf(category.name)
            ).observe(viewLifecycleOwner) { categoryWithExpense ->
                updateLineGraphs(categoryWithExpense, "Month")
            }
            PeriodState.PERIOD -> TODO()
        }
        updateHorizontalGraphs()
    }

    /*private fun updateHorizontalGraphs() {
        val horizontalBarExpenses = binding.expensesHorizontalBar
        val horizontalBarIncomes = binding.incomesHorizontalBar
        val horizontalExpensesData = BarData()
        val horizontalIncomesData = BarData()
        val budgetMultiplier: Int = when (state) {
            PeriodState.DAY -> 1
            PeriodState.WEEK -> 7
            PeriodState.MONTH -> LocalDate.now().lengthOfMonth()
            PeriodState.PERIOD -> ChronoUnit.DAYS.between(dateFrom, dateTo)
                .toInt() + 1 // Add 1 because between is exclusive
        } // Budget is defined as daily budget
        if (categoriesExpenses == null || categoriesExpenses!!.isEmpty()) {
            // TODO no data
        } else {
            var rowCount = 0f
            for (category in categoriesExpenses!!.keys) {
                val expEntries = arrayListOf<BarEntry>()
                val gainEntries = arrayListOf<BarEntry>()
                if (categoriesExpenses!![category] == null) {
                    Log.e(logTag, "No horizontal chart data found for category ${category.name}")
                    break
                }
                expEntries.add(
                    BarEntry(
                        rowCount * 1,
                        categoriesExpenses!![category]!!.expense.absoluteValue
                    )
                )
                rowCount++
                val horizontalBarDataSetExpenses = BarDataSet(expEntries, category.name)
                horizontalBarDataSetExpenses.color = Color.parseColor(category.color)
                horizontalExpensesData.addDataSet(horizontalBarDataSetExpenses)
            }
            horizontalBarExpenses.data = horizontalExpensesData
            horizontalBarExpenses.description.isEnabled = false

            val xAxis = horizontalBarExpenses.xAxis
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum



            /*val axisLeft = horizontalBarExpenses.axisLeft
            axisLeft.axisMinimum = 0f

            val axisRight = horizontalBarExpenses.axisRight
            axisRight.axisMinimum = 0f*/

            horizontalBarExpenses.invalidate()
        }
    }*/

    private fun updateHorizontalGraphs() {
        binding.expensesProgressList.removeAllViews()
        binding.incomesProgressList.removeAllViews()
        val budgetMultiplier: Int = when (state) {
            PeriodState.DAY -> 1
            PeriodState.WEEK -> 7
            PeriodState.MONTH -> LocalDate.now().lengthOfMonth()
            PeriodState.PERIOD -> ChronoUnit.DAYS.between(dateFrom, dateTo)
                .toInt() + 1 // Add 1 because between is exclusive
        } // Budget is defined as daily budget
        if (categoriesExpenses == null || categoriesExpenses!!.isEmpty()) {
            //TODO no data
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
                //budgetsSum += multipliedBudget
                //currentSum += currentCatExpenseAsPositive

                // Load layout
                val viewCatProgressLayoutExp =
                    layoutInflater.inflate(
                        R.layout.category_progress_minimal,
                        binding.expensesProgressList,
                        false
                    )
                val viewCatProgressLayoutGain =
                    layoutInflater.inflate(
                        R.layout.category_progress_minimal,
                        binding.incomesProgressList,
                        false
                    )
                // Add progress layout to container
                binding.expensesProgressList.addView(viewCatProgressLayoutExp)
                binding.incomesProgressList.addView(viewCatProgressLayoutGain)
                // Load progress bar
                val progressBarExp =
                    viewCatProgressLayoutExp.findViewById<LinearProgressIndicator>(R.id.progressBarCategoryBudget)
                val progressBarGain =
                    viewCatProgressLayoutGain.findViewById<LinearProgressIndicator>(R.id.progressBarCategoryBudget)
                // Set progress bar progress and color
                progressBarExp.progress =
                    if (multipliedBudget != 0f) 100 else // If multiplied budget is 0 then fill progress bar (progress 100/100)
                        (currentCatExpenseAsPositive * 100 / multipliedBudget).roundToInt()
                progressBarExp.indicatorColor[0] = Color.parseColor(c.color)
                progressBarGain.progress =
                    if (maxGain == 0f) currentCatGain.roundToInt() else (currentCatGain * 100 / maxGain).roundToInt()
                progressBarGain.indicatorColor[0] = Color.parseColor(c.color)
                // TODO come visualizzo progresso del guadagno?
                // TODO prendo il massimo guadagno e lo fisso come maxprogress? Fatto
                // Set category budget of category line
                viewCatProgressLayoutExp.findViewById<TextView>(R.id.txtCategoryBudget).text =
                    getString(
                        R.string.current_on_max_budget,
                        currentCatExpenseAsPositive,
                        multipliedBudget
                    )
                viewCatProgressLayoutGain.findViewById<TextView>(R.id.txtCategoryBudget).text =
                    getString(
                        R.string.euro_value,
                        currentCatGain
                    )
            }
        }
    }

    private fun updateLineGraphs(
        data: Map<Category, List<AmountWithDate>>,
        columnName: String
    ) {
        if (data.isEmpty()) {
            // TODO no data
        } else {
            val lineChartExp = binding.expensesLineChart
            val lineChartGain = binding.incomesLineChart
            val lineChartExpData = LineData()
            val lineChartGainData = LineData()
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
                while (columnCount < 12) {
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
                    val exp: Float = if (amountWithDateFound.isEmpty()) {
                        0f
                    } else {
                        amountWithDateFound[0].expense.absoluteValue
                    }

                    val gain: Float = if (amountWithDateFound.isEmpty()) {
                        0f
                    } else {
                        amountWithDateFound[0].gain
                    }
                    expEntries.add(
                        Entry(
                            columnCount.toFloat(),
                            exp,
                            "$columnName + $columnCount" //TODO non funziona
                        )
                    )
                    gainEntries.add(
                        Entry(
                            columnCount.toFloat(),
                            gain,
                            "$columnName + $columnCount" //TODO non funziona
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
            lineChartExp.data = lineChartExpData
            lineChartGain.data = lineChartGainData
            lineChartExp.invalidate()
            lineChartGain.invalidate()
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
                    loadCategoryExpenses()
                }
            }

        }
    }

    private fun loadCategoryExpenses() {
        appViewModel.getCategoryProgresses(dateFrom, dateTo)
            .observe(viewLifecycleOwner) {
                if (it?.get(category) != null) {
                    expense = it[category]!!.negative
                    categoriesExpenses = it
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

    private fun updateCategoryProgress() { // TODO unire a updateData()?
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
        val progress = (currentCatExpenseAsPositive * 100 / multipliedBudget).roundToInt()
        binding.progressBarCategoryBudget.indicatorColor[0] = Color.parseColor(category.color)
        binding.progressBarCategoryBudget.progress = progress
        binding.txtMaxCategoryBudget.text = String.format("%.2f", multipliedBudget)
        binding.txtCurrentCategoryProgress.text = String.format("%.2f", currentCatExpenseAsPositive)
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

    private fun setWeek() {
        binding.btnWeek.isEnabled = false
        binding.btnMonth.isEnabled = true
        dateTo = LocalDate.now()
        dateFrom = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(firstDayOfWeek)) //TODO controllare
        state = PeriodState.WEEK
        setTitle(
            "${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year} " +
                    "- ${dateTo.dayOfMonth}/${dateTo.monthValue}/${dateTo.year}"
        )
        updateCategoryProgress()
        loadGraphsData()
    }

    private fun setMonth() {
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = false
        dateTo = LocalDate.now()
        dateFrom = LocalDate.of(dateTo.year, dateTo.month, 1)
        state = PeriodState.MONTH
        setTitle("${dateTo.month} ${dateTo.year}")
        updateCategoryProgress()
        loadGraphsData()
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
        updateCategoryProgress()
        loadGraphsData()
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