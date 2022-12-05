package com.perno97.financialmanagement

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
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.perno97.financialmanagement.database.*
import com.perno97.financialmanagement.databinding.FragmentMainBinding
import com.perno97.financialmanagement.utils.PeriodState
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


private const val LOG_TAG = "MainFragment"
private const val DATE_FROM_KEY = "dateFrom"
private const val DATE_TO_KEY = "dateTo"
private const val DEFAULT_PROFILE_ID = 0

class MainFragment : Fragment() {
    /**
     * Connection to persistent data
     */
    private val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    /**
     * Binding to layout resource
     */
    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var dateFrom: LocalDate
    private lateinit var dateTo: LocalDate
    private lateinit var defaultProfile: Profile
    private var datePickerSelection: Pair<Long, Long>? = null
    private val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    private var state = PeriodState.MONTH
    private var availableDailyBudget: Float? = null
    private var categoriesExpenses: List<CategoryWithExpensesSum>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        // Load profile
        appViewModel.getProfile(DEFAULT_PROFILE_ID).observe(viewLifecycleOwner) { profile ->
            defaultProfile = profile ?: Profile(DEFAULT_PROFILE_ID, 0f)
            binding.txtCurrentValue.text = String.format("%.2f€", defaultProfile.assets)
        }

        // Load UI data
        lifecycleScope.launch {
            appViewModel.uiState.collect {
                dateFrom = it.dateFrom ?: LocalDate.now().minusDays(1)
                dateTo = it.dateTo ?: LocalDate.now()
                state = it.state ?: PeriodState.MONTH
                datePickerSelection = it.datePickerSelection
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
        initListeners()
        //TODO disattivare bottoni in onPause()?
    }

    override fun onStop() {
        super.onStop()
        appViewModel.setPeriod(dateFrom, dateTo, state, datePickerSelection) // Saving UI state
    }

    private fun updateData() {
        binding.categoryList.removeAllViews()
        appViewModel.getCategoryExpensesProgresses(dateFrom, dateTo)
            .observe(viewLifecycleOwner, Observer { list ->
                categoriesExpenses = list
                dataLoaded()
            })
        appViewModel.availableDailyBudget.observe(viewLifecycleOwner) { budget ->
            availableDailyBudget = budget
            dataLoaded()
        }
    }

    private fun dataLoaded() {
        if (categoriesExpenses != null && availableDailyBudget != null) updateUI( //TODO controllare perché non viene aggiornata UI
            categoriesExpenses!!,
            availableDailyBudget!!
        )
    }

    private fun setDay() {
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
        binding.fabAddMovement.setOnClickListener {
            Log.i(LOG_TAG, "Clicked add financial movement")
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
                binding.btnPeriod.isEnabled = true
                setPeriod(from, to)
            }

            // Show
            dateRangePicker.show(parentFragmentManager, "rangeDatePickerDialog")
        }
        binding.txtCurrentValue.setOnClickListener {
            // TODO non si capisce che il testo è cliccabile
            Log.i(LOG_TAG, "Clicked edit current assets value")
            EditCurrentAssetsDialog().show(
                childFragmentManager, EditCurrentAssetsDialog.TAG
            )
        }
        binding.imgBtnGraphs.setOnClickListener {
            Log.i(LOG_TAG, "Clicked on graphs button")
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, AssetsGraphsFragment())
                addToBackStack(null)
            }
        }
        binding.fabRegisteredMovements.setOnClickListener {
            Log.i(LOG_TAG, "Clicked registered movements")
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

    private fun updateUI(categories: List<CategoryWithExpensesSum>, dailyBudget: Float) {
        val pieChartEntries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        val budgetMultiplier: Int = when (state) {
            PeriodState.DAY -> 1
            PeriodState.WEEK -> 7
            PeriodState.MONTH -> 30
            PeriodState.PERIOD -> ChronoUnit.DAYS.between(dateFrom, dateTo)
                .toInt() + 1 // Add 1 because between is exclusive
        } // Budget is defined as daily budget
        if (categories.isEmpty()) {
            pieChartEntries.add(PieEntry(1f, "No data"))
            colors.add(ContextCompat.getColor(requireContext(), R.color.dark))
        } else {
            var currentSum = 0f
            var budgetsSum = 0f
            for (c in categories) {
                val multipliedBudget = c.budget * budgetMultiplier
                val currentModule = -c.current
                budgetsSum += multipliedBudget
                currentSum += currentModule

                // Add category name and value to chart
                pieChartEntries.add(PieEntry(currentModule, c.name))
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
                progressBar.progress = (currentModule * 100 / multipliedBudget).roundToInt()
                progressBar.indicatorColor[0] = Color.parseColor(c.color)
                // Set category budget of category line
                viewCatProgressLayout.findViewById<TextView>(R.id.txtMaxCategoryBudget).text =
                    String.format("%.2f€", multipliedBudget)
                // Set category current expenses of category line
                viewCatProgressLayout.findViewById<TextView>(R.id.txtCurrentCategoryProgress).text =
                    String.format("%.2f€", currentModule)
                // Set click listener for category line
                viewCatProgressLayout.setOnClickListener {
                    parentFragmentManager.commit {
                        binding.categoryList.removeView(viewCatProgressLayout)
                        replace(
                            R.id.fragment_container_view,
                            CategoryDetailsFragment(Category(c.name, c.color, c.budget))
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
        val s2 = SpannableString(String.format("%d€", ceil(dailyBudget * budgetMultiplier).toInt()))
        s1.setSpan(AbsoluteSizeSpan(textSize1), 0, s1.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        s2.setSpan(AbsoluteSizeSpan(textSize2), 0, s2.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        chart.centerText = TextUtils.concat(s1, "\n", s2)
        chart.setCenterTextColor(R.color.dark)
        chart.invalidate()
    }
}