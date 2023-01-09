package com.perno97.financialmanagement.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.database.Expense
import com.perno97.financialmanagement.database.PeriodicMovementsChecker
import com.perno97.financialmanagement.database.Profile
import com.perno97.financialmanagement.databinding.FragmentMainBinding
import com.perno97.financialmanagement.utils.PeriodState
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.roundToInt

class MainFragment : Fragment() {
    private val logTag = "MainFragment"

    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

    private var _binding: FragmentMainBinding? = null

    private lateinit var dateFrom: LocalDate
    private lateinit var dateTo: LocalDate
    private lateinit var defaultProfile: Profile
    private var datePickerSelection: Pair<Long, Long>? = null
    private var state = PeriodState.MONTH
    private var availableDailyBudget: Float? = null
    private var categoriesExpenses: Map<Category, Expense>? = null
    private var uiLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(logTag, "Called onCreateView()")
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            PeriodicMovementsChecker.check(
                requireContext(),
                appViewModel,
                appViewModel.viewModelScope,
                appViewModel.getLastAccess(),
                null
            )
        }

        // Setup chart styling
        val chart = binding.pieChartMain
        chart.setNoDataText(getString(R.string.loading_data))
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        chart.setDrawEntryLabels(false)
        chart.setDrawCenterText(true)
        chart.setTouchEnabled(false)
        return binding.root
    }

    private fun loadCountIncoming() {
        appViewModel.countIncoming(LocalDate.now()).observe(viewLifecycleOwner) { count ->
            Log.i(logTag, "Count incoming --> $count")
            if (count != null && count > 0) {
                binding.txtCountIncoming.visibility = View.VISIBLE
                binding.txtCountIncoming.text = count.toString()
            } else {
                binding.txtCountIncoming.visibility = View.GONE
            }
        }
    }

    private fun loadUiData() {
        viewLifecycleOwner.lifecycleScope.launch {
            Log.i(logTag, "Launched Coroutine")
            appViewModel.uiState.collect {
                Log.i(logTag, "Collecting UI data")
                uiLoaded = true
                dateFrom = it.dateFromMain
                dateTo = it.dateToMain
                state = it.stateMain
                datePickerSelection = it.datePickerSelectionMain
            }
        }
    }

    private fun updateExpectedAssets(prev: Float, new: Float) {
        binding.txtExpectedValue.text =
            getString(R.string.euro_value, new)
        if (new > prev)
            binding.txtExpectedValue.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.blue
                )
            )
        else if (new < prev)
            binding.txtExpectedValue.setTextColor(Color.RED)
        else
            binding.txtExpectedValue.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.dark
                )
            )
    }

    private fun computeExpectedAssets() {
        lifecycleScope.launch {
            val currentAssets = appViewModel.getCurrentAssetDefault()
            val tomorrow = LocalDate.now().plusDays(1)
            when (state) {
                // ----------------- DAY -----------------
                PeriodState.DAY -> updateExpectedAssets(currentAssets, currentAssets)
                // ----------------- WEEK ----------------
                PeriodState.WEEK -> appViewModel.getExpectedSum(
                    tomorrow,
                    LocalDate.now().with(TemporalAdjusters.nextOrSame(firstDayOfWeek.minus(1)))
                ).observe(viewLifecycleOwner) { expectedSum ->
                    var movementsSum = expectedSum ?: 0f
                    lifecycleScope.launch {
                        for (periodicMovement in appViewModel.getAllPeriodicMovements()) {
                            movementsSum += PeriodicMovementsChecker.getMovementsSumPeriodicMovement(
                                periodicMovement,
                                tomorrow,
                                LocalDate.now()
                                    .with(TemporalAdjusters.nextOrSame(firstDayOfWeek.minus(1)))
                            )
                        }
                        updateExpectedAssets(currentAssets, currentAssets + movementsSum)
                    }
                }
                // ---------------- MONTH ----------------
                PeriodState.MONTH -> appViewModel.getExpectedSum(
                    tomorrow,
                    LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())
                ).observe(viewLifecycleOwner) { expectedSum ->
                    var movementsSum = expectedSum ?: 0f
                    lifecycleScope.launch {
                        for (periodicMovement in appViewModel.getAllPeriodicMovements()) {
                            movementsSum += PeriodicMovementsChecker.getMovementsSumPeriodicMovement(
                                periodicMovement,
                                tomorrow,
                                LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())
                            )
                        }
                        updateExpectedAssets(currentAssets, currentAssets + movementsSum)
                    }
                }
                // ---------------- PERIOD ----------------
                PeriodState.PERIOD -> periodSelected()
                // ----------------------------------------
            }
        }
    }

    private fun periodSelected() {
        /*
        Compute the assets value at the start of the period and at the end of the period:
        - if the start/end is in the past then remove past movements from current assets
        - if the start/end is in the future then add incoming movements to current assets
         */

        setStartingAssets()
    }

    private fun setStartingAssets() {
        var startingAssets = defaultProfile.assets
        //Update starting assets
        if (dateFrom.isBefore(LocalDate.now())) {
            appViewModel.getGainsAndExpensesInPeriod(dateFrom, LocalDate.now())
                .observe(viewLifecycleOwner) { pastGroupInfo ->
                    val pastSum = pastGroupInfo.positive + pastGroupInfo.negative
                    binding.txtCurrent.text = getString(R.string.assets_at_start_capital)
                    startingAssets = defaultProfile.assets - pastSum
                    binding.txtCurrentValue.text =
                        getString(R.string.euro_value, startingAssets)
                    setEndingAssets(startingAssets) // Calling after startingAssets is updated, in each if-branch
                }
        } else if (dateFrom.isAfter(LocalDate.now())) {
            val from = LocalDate.now().plusDays(1)
            val to = dateFrom
            appViewModel.getExpectedSum(from, to)
                .observe(viewLifecycleOwner) { expectedSum ->
                    var movementsSum = expectedSum ?: 0f
                    lifecycleScope.launch {
                        for (periodicMovement in appViewModel.getAllPeriodicMovements()) {
                            movementsSum += PeriodicMovementsChecker.getMovementsSumPeriodicMovement(
                                periodicMovement,
                                from,
                                to
                            )
                        }
                        startingAssets = defaultProfile.assets + movementsSum
                        binding.txtCurrentValue.text =
                            getString(R.string.euro_value, startingAssets)
                        setEndingAssets(startingAssets)
                    }

                }
        } else {
            binding.txtCurrentValue.text = getString(R.string.euro_value, startingAssets)
            setEndingAssets(startingAssets)
        }
    }

    private fun setEndingAssets(startingAssets: Float) {
        //Update ending assets
        if (dateTo.isBefore(LocalDate.now())) {
            appViewModel.getGainsAndExpensesInPeriod(dateTo, LocalDate.now())
                .observe(viewLifecycleOwner) { pastGroupInfo ->
                    val pastSum = pastGroupInfo.positive + pastGroupInfo.negative
                    updateExpectedAssets(startingAssets, defaultProfile.assets - pastSum)
                }
        } else if (dateTo.isAfter(LocalDate.now())) {
            val from = LocalDate.now().plusDays(1)
            val to = dateTo
            appViewModel.getExpectedSum(from, to)
                .observe(viewLifecycleOwner) { expectedSum ->
                    var movementsSum = expectedSum ?: 0f
                    lifecycleScope.launch {
                        for (periodicMovement in appViewModel.getAllPeriodicMovements()) {
                            movementsSum += PeriodicMovementsChecker.getMovementsSumPeriodicMovement(
                                periodicMovement,
                                from,
                                to
                            )
                        }
                        updateExpectedAssets(startingAssets, defaultProfile.assets + movementsSum)
                    }

                }
        } else {
            updateExpectedAssets(startingAssets, defaultProfile.assets)
        }
    }

    private fun createNewDefaultProfile() {
        appViewModel.insertDefaultProfile(0f, LocalDate.now())
    }

    override fun onResume() {
        super.onResume()
        Log.i(logTag, "Called onResume()")
        // Load profile
        appViewModel.getDefaultProfile().observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                defaultProfile = profile
                binding.txtCurrentValue.text = getString(R.string.euro_value, defaultProfile.assets)
                initReady()
            } else {
                createNewDefaultProfile()
            }
        }

        loadUiData()
        loadCountIncoming()
        initListeners()
    }

    override fun onStop() {
        super.onStop()
        appViewModel.setMainPeriod(dateFrom, dateTo, state, datePickerSelection) // Saving UI state
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(logTag, "Called onDestroy()")
        _binding = null
    }

    private fun updateCategoriesExpenses() {
        Log.i(logTag, "Called updateCategoriesExpenses()")
        appViewModel.getCategoryExpensesProgresses(dateFrom, dateTo)
            .observe(viewLifecycleOwner) { list ->
                Log.i(logTag, "Observed getCategoryExpensesProgress")
                categoriesExpenses = list
                updateAvailableBudget()
            }

    }

    private fun updateAvailableBudget() {
        Log.i(logTag, "Called updateAvailableBudget()")
        appViewModel.availableDailyBudget.observe(viewLifecycleOwner) { budget ->
            Log.i(logTag, "Observed availableDailyBudget")
            availableDailyBudget = budget
            updateUI(
                categoriesExpenses!!,
                availableDailyBudget
            )
        }
    }

    private fun initReady() {
        Log.i(logTag, "Called initReady()")
        when (state) {
            PeriodState.DAY -> setDay()
            PeriodState.WEEK -> setWeek()
            PeriodState.MONTH -> setMonth()
            PeriodState.PERIOD -> setPeriod(dateFrom, dateTo)
        }
    }

    private fun setDay() {
        Log.i(logTag, "Called setDay()")
        binding.txtCurrent.text = getString(R.string.current_assets_label)
        binding.txtExpected.text = getString(R.string.expected_assets_label)
        binding.btnDay.isEnabled = false
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = true
        dateTo = LocalDate.now()
        dateFrom = dateTo
        state = PeriodState.DAY
        val month = dateTo.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }
        setTitle("${dateTo.dayOfMonth} $month ${dateTo.year}")
        updateCategoriesExpenses()
        computeExpectedAssets()
    }

    private fun setWeek() {
        Log.i(logTag, "Called setWeek()")
        binding.txtCurrent.text = getString(R.string.current_assets_label)
        binding.txtExpected.text = getString(R.string.expected_assets_label)
        binding.btnDay.isEnabled = true
        binding.btnWeek.isEnabled = false
        binding.btnMonth.isEnabled = true
        dateTo = LocalDate.now()
        dateFrom = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
        val to = dateFrom.with(TemporalAdjusters.next(firstDayOfWeek))
        state = PeriodState.WEEK
        setTitle(
            "${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year} " +
                    "- ${to.dayOfMonth}/${to.monthValue}/${to.year}"
        )
        updateCategoriesExpenses()
        computeExpectedAssets()
    }

    private fun setMonth() {
        binding.txtCurrent.text = getString(R.string.current_assets_label)
        binding.txtExpected.text = getString(R.string.expected_assets_label)
        Log.i(logTag, "Called setMonth()")
        binding.btnDay.isEnabled = true
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = false
        dateTo = LocalDate.now()
        dateFrom = LocalDate.of(dateTo.year, dateTo.month, 1)
        state = PeriodState.MONTH
        val month = dateTo.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }
        setTitle("$month ${dateTo.year}")
        updateCategoriesExpenses()
        computeExpectedAssets()
    }

    private fun setPeriod(from: LocalDate, to: LocalDate) {
        binding.txtCurrent.text = getString(R.string.assets_at_start_capital)
        binding.txtExpected.text = getString(R.string.assets_at_end_capital)
        Log.i(logTag, "Called setPeriod()")
        binding.btnDay.isEnabled = true
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = true
        dateTo = to
        dateFrom = from
        state = PeriodState.PERIOD
        setTitle(
            "${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year} " +
                    "- ${dateTo.dayOfMonth}/${dateTo.monthValue}/${dateTo.year}"
        )
        updateCategoriesExpenses()
        computeExpectedAssets()
    }

    private fun initListeners() {
        Log.i(logTag, "Called initListeners()")
        binding.fabAddMovement.setOnClickListener {
            Log.i(logTag, "Clicked add financial movement")
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
        binding.btnDay.setOnClickListener {
            setDay()
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
        binding.txtCurrentValue.setOnClickListener {
            Log.i(logTag, "Clicked edit current assets value")
            EditCurrentAssetsDialog().show(
                childFragmentManager, EditCurrentAssetsDialog.TAG
            )
        }
        binding.imgBtnGraphs.setOnClickListener {
            Log.i(logTag, "Clicked on graphs button")
            parentFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                replace(R.id.fragment_container_view, AssetsGraphsFragment())
                addToBackStack(null)
            }
        }
        binding.imgBtnCategories.setOnClickListener {
            Log.i(logTag, "Clicked on categories list button")
            parentFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                replace(R.id.fragment_container_view, CategoriesListFragment())
                addToBackStack(null)
            }
        }
        binding.fabRegisteredMovements.setOnClickListener {
            Log.i(logTag, "Clicked registered movements")
            parentFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_top,
                    R.anim.slide_in_top,
                    R.anim.slide_out_bottom
                )
                replace(R.id.fragment_container_view, RegisteredMovementsFragment())
                addToBackStack(null)
            }
        }
        binding.fabIncomingMovements.setOnClickListener {
            Log.i(logTag, "Clicked incoming movements")
            parentFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_top,
                    R.anim.slide_in_top,
                    R.anim.slide_out_bottom
                )
                replace(R.id.fragment_container_view, IncomingMovementsFragment())
                addToBackStack(null)
            }
        }
    }

    private fun setTitle(title: String) {
        binding.txtSubtitle.text = title
    }

    private fun updateUI(categories: Map<Category, Expense>, dailyBudget: Float?) {
        Log.i(logTag, "Called updateUI")
        binding.categoryList.removeAllViews()
        val pieChartEntries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        val budgetMultiplier: Int = when (state) {
            PeriodState.DAY -> 1
            PeriodState.WEEK -> 7
            PeriodState.MONTH -> LocalDate.now().lengthOfMonth()
            PeriodState.PERIOD -> ChronoUnit.DAYS.between(dateFrom, dateTo)
                .toInt() + 1 // Add 1 because between is exclusive
        } // Budget is defined as daily budget
        if (categories.isEmpty()) {
            pieChartEntries.add(PieEntry(1f, "No data"))
            colors.add(ContextCompat.getColor(requireContext(), R.color.dark))
        } else {
            var currentSum = 0f
            var budgetsSum = 0f
            for (c in categories.keys) {
                val multipliedBudget = c.budget * budgetMultiplier
                val currentCatExpenseAsPositive = categories[c]!!.expense.absoluteValue
                budgetsSum += multipliedBudget
                currentSum += currentCatExpenseAsPositive

                // Add category name and value to chart
                pieChartEntries.add(PieEntry(currentCatExpenseAsPositive, c.name))
                // Add category color to chart
                colors.add(Color.parseColor(c.color))

                // Load layout
                val viewCatProgressLayout =
                    layoutInflater.inflate(R.layout.category_progress, binding.categoryList, false)
                // Add progress layout to container
                binding.categoryList.addView(viewCatProgressLayout)
                // Set category color of category line
                viewCatProgressLayout.findViewById<TextView>(R.id.categoryColorLabel)
                    .backgroundTintList = ColorStateList.valueOf(Color.parseColor(c.color))
                // Set category name of category line
                viewCatProgressLayout.findViewById<TextView>(R.id.txtCategoryName).text = c.name
                // Load progress bar
                val progressBar =
                    viewCatProgressLayout.findViewById<LinearProgressIndicator>(R.id.progressBarCategoryBudget)
                // Set progress bar progress and color
                progressBar.progress =
                    if (multipliedBudget == 0f && currentCatExpenseAsPositive == 0f) 0
                    else if (multipliedBudget == 0f) 100 else
                        (currentCatExpenseAsPositive * 100 / multipliedBudget).roundToInt()
                progressBar.indicatorColor[0] = Color.parseColor(c.color)
                // Set category current expenses of category line
                viewCatProgressLayout.findViewById<TextView>(R.id.txtCurrentCategoryProgress).text =
                    getString(
                        R.string.current_on_max_budget,
                        currentCatExpenseAsPositive,
                        multipliedBudget
                    )
            }
            // If current total expenses are lower than budget then there's a slice showing
            // the remaining available budget
            val diff = budgetsSum - currentSum
            if (diff > 0) {
                pieChartEntries.add(PieEntry(diff, "Available"))
                colors.add(ContextCompat.getColor(requireContext(), R.color.dark))
            }
        }
        val dataSet = PieDataSet(pieChartEntries, "Budgets")
        dataSet.colors = colors
        val pieData = PieData(dataSet)
        pieData.setDrawValues(false)
        val chart = binding.pieChartMain
        chart.data = pieData
        val textSize1 = resources.getDimensionPixelSize(R.dimen.text_size_1)
        val textSize2 = resources.getDimensionPixelSize(R.dimen.text_size_2)
        val s1 = SpannableString("AVAILABLE \n BUDGET")
        val s2 = SpannableString(
            String.format(
                "%d€",
                ceil((dailyBudget ?: 0f) * budgetMultiplier).toInt() // If null show 0
            )
        )
        s1.setSpan(AbsoluteSizeSpan(textSize1), 0, s1.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        s2.setSpan(AbsoluteSizeSpan(textSize2), 0, s2.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        chart.centerText = TextUtils.concat(s1, "\n", s2)
        chart.setCenterTextColor(R.color.dark)
        chart.invalidate()
    }
}