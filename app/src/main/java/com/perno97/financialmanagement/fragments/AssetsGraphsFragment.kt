package com.perno97.financialmanagement.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.GroupInfo
import com.perno97.financialmanagement.database.Profile
import com.perno97.financialmanagement.databinding.FragmentAssetsGraphsBinding
import com.perno97.financialmanagement.utils.MonthValueFormatter
import com.perno97.financialmanagement.utils.PeriodState
import com.perno97.financialmanagement.utils.PeriodValueFormatter
import com.perno97.financialmanagement.utils.WeekValueFormatter
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt

class AssetsGraphsFragment : Fragment() {

    private val logTag = "AssetsGraphFragment"
    private val numberOfColumnsInGraphs = 12

    private var _binding: FragmentAssetsGraphsBinding? = null

    private val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

    /**
     * Connection to data
     */
    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var dateFrom: LocalDate
    private lateinit var dateTo: LocalDate
    private lateinit var defaultProfile: Profile
    private var state = PeriodState.MONTH
    private var datePickerSelection: Pair<Long, Long>? = null
    private var weekStartOffset = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssetsGraphsBinding.inflate(inflater, container, false)

        appViewModel.getDefaultProfile().observe(viewLifecycleOwner) { profile ->
            defaultProfile = profile
            loadUiData()
        }

        val f = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val t = f.with(TemporalAdjusters.previous(firstDayOfWeek))
        // Count days from the next sunday to the previous firstDayOfWeek
        // Workaround for having custom first day of week in SQLite
        weekStartOffset = ChronoUnit.DAYS.between(f, t).toInt().absoluteValue
        initGraphs()

        return binding.root
    }

    private fun loadUiData() {
        Log.i(logTag, "Called loadUiData()")
        // TODO caricare UI data dal main o creare variabili specifiche per questo fragment?
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appViewModel.uiState.collect {
                    Log.e(logTag, "Collected UI data")
                    dateTo = it.dateToCatDetails ?: LocalDate.now()
                    dateFrom = it.dateFromCatDetails ?: LocalDate.of(dateTo.year, dateTo.month, 1)
                    state = it.stateCatDetails ?: PeriodState.MONTH
                    datePickerSelection = it.datePickerSelectionCatDetails
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

    private fun initGraphs() {
        val lineChart = binding.assetsLineChart
        val barChart = binding.assetsBarChart
        lineChart.setExtraOffsets(10f, 0f, 10f, 30f)
        lineChart.description.isEnabled = false

        val xAxisExp = lineChart.xAxis
        xAxisExp.setDrawGridLines(false)
        xAxisExp.setDrawAxisLine(false)
        xAxisExp.position = XAxis.XAxisPosition.BOTTOM
        xAxisExp.labelRotationAngle = -90f

        val leftAxisExp = lineChart.axisLeft
        leftAxisExp.isEnabled = false

        val rightAxisExp = lineChart.axisRight
        rightAxisExp.isEnabled = false
    }

    private fun loadData() {
        Log.i(logTag, "Called loadLineGraphData()")
        when (state) {
            // ----------------- DAY -----------------
            PeriodState.DAY -> Log.e(logTag, "Period day not defined in this screen")
            // ----------------- WEEK ----------------
            PeriodState.WEEK -> {
                appViewModel.getMovementsSumGroupByWeek(
                    weekStartOffset,
                    LocalDate.now()
                ).observe(viewLifecycleOwner) { data ->
                    updateLineAndBarCharts(data)
                }
                appViewModel.getGainsAndExpensesInPeriod(
                    LocalDate.now(),
                    LocalDate.now().minusDays(7 * numberOfColumnsInGraphs.toLong())
                ).observe(viewLifecycleOwner) { data ->
                    updateHorizontalGraph(data)
                }
            }
            // ---------------- MONTH ----------------
            PeriodState.MONTH -> {
                appViewModel.getMovementsSumGroupByMonth(LocalDate.now())
                    .observe(viewLifecycleOwner) { data ->
                        updateLineAndBarCharts(data)
                    }
                appViewModel.getGainsAndExpensesInPeriod(
                    LocalDate.now(),
                    LocalDate.now().minusMonths(numberOfColumnsInGraphs.toLong())
                ).observe(viewLifecycleOwner) { data ->
                    updateHorizontalGraph(data)
                }
            }
            // ---------------- PERIOD ----------------
            PeriodState.PERIOD -> {
                appViewModel.getMovementsSumInPeriod(dateFrom, dateTo)
                    .observe(viewLifecycleOwner) { data ->
                        updateLineAndBarCharts(data)
                    }
                appViewModel.getGainsAndExpensesInPeriod(dateFrom, dateTo)
                    .observe(viewLifecycleOwner) { data ->
                        updateHorizontalGraph(data)
                    }
            }
            // ----------------------------------------
        }
    }

    private fun updateHorizontalGraph(data: GroupInfo?) { // TODO i dati sono sbagliati?
        val expProgressBar = binding.progressBarAssetsOutcomes
        val gainProgressBar = binding.progressBarAssetsIncomes
        val txtViewExp = binding.txtAssetsOutcomes
        val txtViewGain = binding.txtAssetsIcomes
        if (data != null) {
            val max = max(data.negative, data.positive)
            val exp = data.negative
            val gain = data.positive
            expProgressBar.progress = exp.roundToInt()
            expProgressBar.max = max.roundToInt()
            txtViewExp.text = getString(R.string.current_on_max_budget, exp, max)
            gainProgressBar.progress = gain.roundToInt()
            gainProgressBar.max = max.roundToInt()
            txtViewGain.text = getString(R.string.current_on_max_budget, gain, max)
        }
    }

    private fun updateLineAndBarCharts(data: List<GroupInfo>) {
        var columnCount = 0
        var columnsToShow = 0
        var columnDate = LocalDate.now()
        val lineChart = binding.assetsLineChart
        val barChart = binding.assetsBarChart
        val labels = arrayListOf<LocalDate>()
        val lineEntries = arrayListOf<Entry>()
        val barEntriesExp = arrayListOf<BarEntry>()
        val barEntriesGain = arrayListOf<BarEntry>()

        when (state) {
            // ----------------- DAY -----------------
            PeriodState.DAY -> {
                Log.e(logTag, "Period day not defined in this screen, updating line graph")
                return
            }
            // ----------------- WEEK ----------------
            PeriodState.WEEK -> {
                columnsToShow = numberOfColumnsInGraphs
                columnDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
            }
            // ---------------- MONTH ----------------
            PeriodState.MONTH -> {
                columnsToShow = numberOfColumnsInGraphs
                columnDate = LocalDate.now().withDayOfMonth(1)
            }
            // ---------------- PERIOD ----------------
            PeriodState.PERIOD -> {
                columnsToShow =
                    ChronoUnit.DAYS.between(dateFrom, dateTo).toInt() // TODO controllare
                columnDate = dateTo
            }
            // ----------------------------------------
        }
        while (columnCount < columnsToShow) {
            var columnValue = defaultProfile.assets
            when (state) {
                // ----------------- DAY -----------------
                PeriodState.DAY -> {
                    Log.e(
                        logTag,
                        "Period day not defined in this screen, iteration updating line graph"
                    )
                    return
                }
                // ----------------- WEEK ----------------
                PeriodState.WEEK -> {
                    if (columnCount == 0) {
                        columnValue = defaultProfile.assets
                    } else {
                        val item = data.find { item ->
                            LocalDate.parse(item.groupDate)
                                .with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                                .isEqual(columnDate)
                        }
                        // Check if there is item for this week
                        if (item != null) {
                            columnValue += item.positive + item.negative
                        }
                    }
                    lineEntries.add(
                        Entry(
                            columnCount.toFloat(),
                            columnValue
                        )
                    )
                    barEntriesExp.add(
                        BarEntry(
                            columnCount.toFloat(),
                            columnValue
                        )
                    )
                    barEntriesGain.add(
                        BarEntry(
                            columnCount.toFloat(),
                            columnValue
                        )
                    )

                    columnDate = columnDate.minusDays(7)
                }
                // ---------------- MONTH ----------------
                PeriodState.MONTH -> {
                    if (columnCount == 0) {
                        columnValue = defaultProfile.assets
                    } else {
                        val item = data.find { item ->
                            LocalDate.parse(item.groupDate)
                                .withDayOfMonth(1)
                                .isEqual(columnDate)
                        }
                        // Check if there is item for this week
                        if (item != null) {
                            columnValue += item.positive + item.negative
                        }
                    }
                    lineEntries.add(
                        Entry(
                            columnCount.toFloat(),
                            columnValue
                        )
                    )
                    barEntriesExp.add(
                        BarEntry(
                            columnCount.toFloat(),
                            columnValue
                        )
                    )
                    barEntriesGain.add(
                        BarEntry(
                            columnCount.toFloat(),
                            columnValue
                        )
                    )
                    labels.add(columnDate)

                    columnDate = columnDate.minusMonths(1)
                }
                // ---------------- PERIOD ----------------
                PeriodState.PERIOD -> {
                    if (columnCount == 0) {
                        columnValue = defaultProfile.assets
                    } else {
                        val item = data.find { item ->
                            LocalDate.parse(item.groupDate).isEqual(columnDate)
                        }
                        // Check if there is item for this week
                        if (item != null) {
                            columnValue += item.positive + item.negative
                        }
                    }
                    lineEntries.add(
                        Entry(
                            columnCount.toFloat(),
                            columnValue
                        )
                    )
                    barEntriesExp.add(
                        BarEntry(
                            columnCount.toFloat(),
                            columnValue
                        )
                    )
                    barEntriesGain.add(
                        BarEntry(
                            columnCount.toFloat(),
                            columnValue
                        )
                    )
                    labels.add(columnDate)

                    columnDate = columnDate.minusDays(1)
                }
                // ----------------------------------------
            }
            columnCount++
        }

        val valueFormatter = when (state) {
            PeriodState.DAY -> {
                Log.e(
                    logTag,
                    "Period day not defined in this screen, after columns loaded"
                )
                return
            }
            PeriodState.WEEK -> WeekValueFormatter()
            PeriodState.MONTH -> MonthValueFormatter(labels)
            PeriodState.PERIOD -> PeriodValueFormatter(labels)
        }

        val lineDataSet = LineDataSet(lineEntries, getString(R.string.assets))
        // TODO set linedataset color
        val lineChartData = LineData(lineDataSet)
        val xAxisLine = lineChart.xAxis
        xAxisLine.labelCount = columnsToShow
        xAxisLine.granularity = 1f
        xAxisLine.valueFormatter = valueFormatter
        lineChart.data = lineChartData
        lineChart.invalidate()

        val barDataSetExp = BarDataSet(barEntriesExp, getString(R.string.incomes))
        barDataSetExp.color = ContextCompat.getColor(
            requireContext(),
            R.color.warning
        )
        val barDataSetGain = BarDataSet(barEntriesGain, getString(R.string.outcomes))
        barDataSetGain.color = ContextCompat.getColor(
            requireContext(),
            R.color.success
        )
        val barChartData = BarData(barDataSetExp, barDataSetGain)
        val xAxisBar = barChart.xAxis
        xAxisBar.labelCount = columnsToShow
        xAxisBar.granularity = 1f
        xAxisBar.valueFormatter = valueFormatter
        barChart.data = barChartData
        barChart.invalidate()
    }

    private fun setWeek() {
        Log.i(logTag, "Called setWeek()")
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
        loadData()
    }

    private fun setMonth() {
        Log.i(logTag, "Called setMonth()")
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = false
        dateTo = LocalDate.now()
        dateFrom = LocalDate.of(dateTo.year, dateTo.month, 1)
        state = PeriodState.MONTH
        setTitle("${dateTo.month} ${dateTo.year}")
        loadData()
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
        loadData()
    }

    private fun setTitle(title: String) {
        binding.txtTitle.text = title
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    override fun onStop() {
        super.onStop()
        // TODO save UI data
    }

    private fun initListeners() {
        binding.fabBtnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
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
        binding.btnMonth.setOnClickListener {
            setMonth()
        }
        binding.btnWeek.setOnClickListener {
            setWeek()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}