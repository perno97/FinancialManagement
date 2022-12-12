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
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.*
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
import kotlin.math.ceil
import kotlin.math.roundToInt

class MainFragment : Fragment() {

    private val logTag = "MainFragment"

    /**
     * Connection to data
     */
    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    //TODO controllare se possibile evitare di chiamare update perché i dati vengono osservati

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

    /**
     * Binding to layout resource
     */
    private var _binding: FragmentMainBinding? = null

    private lateinit var dateFrom: LocalDate
    private lateinit var dateTo: LocalDate
    private lateinit var defaultProfile: Profile
    private var datePickerSelection: Pair<Long, Long>? = null
    private var state = PeriodState.MONTH
    private var availableDailyBudget: Float? = null
    private var categoriesExpenses: Map<Category, Expense>? = null
    private var isAllDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(logTag, "Called onCreateView()")
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        // Load profile
        appViewModel.getDefaultProfile().observe(viewLifecycleOwner) { profile ->
            defaultProfile = profile ?: Profile(appViewModel.defaultProfileId, 0f)
            binding.txtCurrentValue.text = String.format("%.2f€", defaultProfile.assets)
        }

        // Load UI data
        viewLifecycleOwner.lifecycleScope.launch {
            Log.i(logTag, "Launched Coroutine")
            appViewModel.uiState.collect {
                Log.i(logTag, "Collecting UI data")
                dateFrom = it.dateFromMain ?: LocalDate.now().minusDays(1)
                dateTo = it.dateToMain ?: LocalDate.now()
                state = it.stateMain ?: PeriodState.MONTH
                datePickerSelection = it.datePickerSelectionMain
                isAllDataLoaded = false
                when (state) {
                    PeriodState.DAY -> setDay()
                    PeriodState.WEEK -> setWeek()
                    PeriodState.MONTH -> setMonth()
                    PeriodState.PERIOD -> setPeriod(dateFrom, dateTo)
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.i(logTag, "Called onResume()")
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

    private fun updateData() {
        Log.i(logTag, "Called updateData()")
        appViewModel.getCategoryExpensesProgresses(dateFrom, dateTo)
            .observe(viewLifecycleOwner) { list ->
                Log.i(logTag, "Observed getCategoryExpensesProgress")
                categoriesExpenses = list
                dataLoaded()
            }
        appViewModel.availableDailyBudget.observe(viewLifecycleOwner) { budget ->
            Log.i(logTag, "Observed availableDailyBudget")
            availableDailyBudget = budget
            dataLoaded()
        }
    }

    private fun dataLoaded() {
        Log.i(logTag, "Called dataLoaded with isAllDataLoaded = $isAllDataLoaded")
        if (isAllDataLoaded) updateUI( //TODO controllare perché non viene aggiornata UI
            categoriesExpenses!!,
            availableDailyBudget
        )
        else {
            isAllDataLoaded = true
        }
    }

    private fun setDay() {
        Log.i(logTag, "Called setDay()")
        binding.btnDay.isEnabled = false
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = true
        dateTo = LocalDate.now()
        dateFrom = dateTo
        state = PeriodState.DAY
        setTitle("${dateTo.dayOfMonth} ${dateTo.month} ${dateTo.year}")
        updateData()
    }

    private fun setWeek() {
        Log.i(logTag, "Called setWeek()")
        binding.btnDay.isEnabled = true
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
        updateData()
    }

    private fun setMonth() {
        Log.i(logTag, "Called setMonth()")
        binding.btnDay.isEnabled = true
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = false
        dateTo = LocalDate.now()
        dateFrom = LocalDate.of(dateTo.year, dateTo.month, 1)
        state = PeriodState.MONTH
        setTitle("${dateTo.month} ${dateTo.year}")
        updateData()
    }

    private fun setPeriod(from: LocalDate, to: LocalDate) {
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
        updateData()
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
        binding.txtCurrentValue.setOnClickListener {
            // TODO non si capisce che il testo è cliccabile
            Log.i(logTag, "Clicked edit current assets value")
            EditCurrentAssetsDialog().show(
                childFragmentManager, EditCurrentAssetsDialog.TAG
            )
        }
        binding.imgBtnGraphs.setOnClickListener {
            Log.i(logTag, "Clicked on graphs button")
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, AssetsGraphsFragment())
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
    }

    private fun setTitle(title: String) {
        binding.txtTitle.text = title
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
                val currentCatExpenseAsPositive = -categories[c]!!.expense
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
                    (currentCatExpenseAsPositive * 100 / multipliedBudget).roundToInt()
                progressBar.indicatorColor[0] = Color.parseColor(c.color)
                // Set category budget of category line
                viewCatProgressLayout.findViewById<TextView>(R.id.txtMaxCategoryBudget).text =
                    String.format("%.2f€", multipliedBudget)
                // Set category current expenses of category line
                viewCatProgressLayout.findViewById<TextView>(R.id.txtCurrentCategoryProgress).text =
                    String.format("%.2f€", currentCatExpenseAsPositive)
                // Set click listener for category line
                viewCatProgressLayout.setOnClickListener {
                    parentFragmentManager.commit {
                        replace(
                            R.id.fragment_container_view,
                            CategoryDetailsFragment(c.name)
                        )
                        addToBackStack(null)
                    }
                }
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
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        chart.setDrawEntryLabels(false)
        chart.setDrawCenterText(true)
        chart.setTouchEnabled(false)
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