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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.GroupInfo
import com.perno97.financialmanagement.database.Profile
import com.perno97.financialmanagement.databinding.FragmentAssetsGraphsBinding
import com.perno97.financialmanagement.utils.*
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt

class AssetsGraphsFragment : Fragment() {

    private val logTag = "AssetsGraphFragment"
    private val numberOfColumnsInGraphs = 8

    private var _binding: FragmentAssetsGraphsBinding? = null

    private val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

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
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appViewModel.uiState.collect {
                    Log.i(logTag, "Collected UI data")
                    dateTo = it.dateToAssets
                    dateFrom = it.dateFromAssets
                    state = it.stateAssets
                    datePickerSelection = it.datePickerSelectionAssets
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
        lineChart.setExtraOffsets(10f, 0f, 10f, 10f)
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        barChart.setExtraOffsets(10f, 0f, 10f, 10f)
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false

        val xAxisLine = lineChart.xAxis
        xAxisLine.setDrawGridLines(false)
        xAxisLine.setDrawAxisLine(false)
        xAxisLine.position = XAxis.XAxisPosition.BOTTOM
        xAxisLine.labelRotationAngle = -90f

        val xAxisBar = barChart.xAxis
        xAxisBar.yOffset = 10f
        xAxisBar.setCenterAxisLabels(true)
        xAxisBar.setDrawGridLines(false)
        xAxisBar.setDrawAxisLine(false)
        xAxisBar.position = XAxis.XAxisPosition.BOTTOM
        xAxisBar.labelRotationAngle = -90f
        xAxisBar.axisMinimum = 0f

        lineChart.axisLeft.isEnabled = false

        val leftAxisBar = barChart.axisLeft
        leftAxisBar.isEnabled = false
        leftAxisBar.axisMinimum = 0f

        lineChart.axisRight.isEnabled = false
        barChart.axisRight.isEnabled = false
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
                    LocalDate.now(),
                    numberOfColumnsInGraphs
                ).observe(viewLifecycleOwner) { data ->
                    updateLineAndBarCharts(data)
                }
                appViewModel.getGainsAndExpensesInPeriod(
                    LocalDate.now().minusDays(7 * numberOfColumnsInGraphs.toLong()),
                    LocalDate.now()
                ).observe(viewLifecycleOwner) { data ->
                    updateHorizontalGraph(data)
                }
            }
            // ---------------- MONTH ----------------
            PeriodState.MONTH -> {
                appViewModel.getMovementsSumGroupByMonth(LocalDate.now(), numberOfColumnsInGraphs)
                    .observe(viewLifecycleOwner) { data ->
                        updateLineAndBarCharts(data)
                    }
                appViewModel.getGainsAndExpensesInPeriod(
                    LocalDate.now().minusMonths(numberOfColumnsInGraphs.toLong()),
                    LocalDate.now()
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

    private fun updateHorizontalGraph(data: GroupInfo?) {
        val expProgressBar = binding.progressBarAssetsExpenses
        val gainProgressBar = binding.progressBarAssetsIncomes
        val txtViewExp = binding.txtAssetsExpenses
        val txtViewGain = binding.txtAssetsIncomes
        if (data != null) {
            val exp = data.negative.absoluteValue
            val gain = data.positive
            val max = max(exp, gain)
            expProgressBar.max = max.roundToInt()
            expProgressBar.progress = exp.roundToInt()
            txtViewExp.text = getString(R.string.current_on_max_budget, exp, max)
            gainProgressBar.max = max.roundToInt()
            gainProgressBar.progress = gain.roundToInt()
            txtViewGain.text = getString(R.string.current_on_max_budget, gain, max)
        }
    }

    private fun updateLineAndBarCharts(data: List<GroupInfo>) {
        var columnCount = 0
        var columnsToShow = 0
        var columnDate = LocalDate.now()
        val lineChart = binding.assetsLineChart
        val barChart = binding.assetsBarChart
        val lineLabels = arrayListOf<String>()
        val labels = arrayListOf<String>()
        val lineEntries = arrayListOf<Entry>()
        val barEntriesExp = arrayListOf<BarEntry>()
        val barEntriesGain = arrayListOf<BarEntry>()
        var columnValue = defaultProfile.assets

        // First column of line graph is current assets if not period
        if (state != PeriodState.PERIOD) {
            lineEntries.add(
                Entry(
                    0f,
                    columnValue
                )
            )
            lineLabels.add(getString(R.string.graph_label_now))
        }

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
                    ChronoUnit.DAYS.between(dateFrom, dateTo).toInt() + 1
                columnDate = dateTo
            }
            // ----------------------------------------
        }

        while (columnCount < columnsToShow) {
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
                    val item = data.find { item ->
                        LocalDate.parse(item.groupDate)
                            .with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                            .isEqual(columnDate)
                    }
                    val l = if (columnCount == 0)
                        getString(R.string.graph_label_current)
                    else
                        getString(R.string.graph_label_week, columnCount)

                    // Check if there is item for this week
                    if (item != null) {
                        columnValue -= item.positive + item.negative
                    }
                    if (columnCount + 1 < columnsToShow) { // Due to +1, need to check out of bound
                        lineEntries.add(
                            Entry(
                                columnCount.toFloat() + 1, // +1 because the first column is current assets
                                columnValue
                            )
                        )
                        lineLabels.add(l)
                    }
                    barEntriesGain.add(
                        BarEntry(
                            columnCount.toFloat(),
                            item?.positive ?: 0f
                        )
                    )
                    barEntriesExp.add(
                        BarEntry(
                            columnCount.toFloat(),
                            item?.negative?.absoluteValue ?: 0f
                        )
                    )

                    labels.add(l)


                    columnDate = columnDate.minusDays(7)
                }
                // ---------------- MONTH ----------------
                PeriodState.MONTH -> {
                    val item = data.find { item ->
                        LocalDate.parse(item.groupDate)
                            .withDayOfMonth(1)
                            .isEqual(columnDate)
                    }
                    val l = columnDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        .replaceFirstChar { c -> c.uppercase() }

                    // Check if there is item for this week
                    if (item != null) {
                        columnValue -= item.positive + item.negative
                    }
                    if (columnCount + 1 < columnsToShow) { // Due to +1, need to check out of bound
                        lineEntries.add(
                            Entry(
                                columnCount.toFloat() + 1, // +1 because the first column is current assets
                                columnValue
                            )
                        )
                        lineLabels.add(l)
                    }

                    barEntriesGain.add(
                        BarEntry(
                            columnCount.toFloat(),
                            item?.positive ?: 0f
                        )
                    )
                    barEntriesExp.add(
                        BarEntry(
                            columnCount.toFloat(),
                            item?.negative?.absoluteValue ?: 0f
                        )
                    )

                    labels.add(l)

                    columnDate = columnDate.minusMonths(1)
                }
                // ---------------- PERIOD ----------------
                PeriodState.PERIOD -> {
                    val item = data.find { item ->
                        LocalDate.parse(item.groupDate).isEqual(columnDate)
                    }
                    val l = "${columnDate.dayOfMonth}/${columnDate.monthValue}/${columnDate.year}"

                    // Check if there is item for this week
                    if (item != null) {
                        columnValue -= item.positive + item.negative
                    }

                    lineEntries.add(
                        Entry(
                            columnCount.toFloat(),
                            columnValue
                        )
                    )
                    lineLabels.add(l)

                    barEntriesGain.add(
                        BarEntry(
                            columnCount.toFloat(),
                            item?.positive ?: 0f
                        )
                    )
                    barEntriesExp.add(
                        BarEntry(
                            columnCount.toFloat(),
                            item?.negative?.absoluteValue ?: 0f
                        )
                    )

                    labels.add(l)

                    columnDate = columnDate.minusDays(1)
                }
                // ----------------------------------------
            }
            columnCount++
        }

        val lineDataSet = LineDataSet(lineEntries, getString(R.string.assets))
        lineDataSet.color = ContextCompat.getColor(requireContext(), R.color.dark)
        val lineChartData = LineData(lineDataSet)
        val xAxisLine = lineChart.xAxis
        xAxisLine.labelCount = columnsToShow
        xAxisLine.granularity = 1f
        xAxisLine.valueFormatter = IndexAxisValueFormatter(lineLabels)
        lineChart.data = lineChartData
        lineChart.invalidate()

        val barDataSetExp = BarDataSet(barEntriesExp, getString(R.string.expenses))
        barDataSetExp.color = ContextCompat.getColor(
            requireContext(),
            R.color.warning
        )
        val barDataSetGain = BarDataSet(barEntriesGain, getString(R.string.incomes))
        barDataSetGain.color = ContextCompat.getColor(
            requireContext(),
            R.color.success
        )
        val barChartData = BarData(barDataSetGain, barDataSetExp)
        barChartData.barWidth = 0.4f
        val xAxisBar = barChart.xAxis
        xAxisBar.labelCount = columnsToShow
        xAxisBar.granularity = 1f
        xAxisBar.axisMaximum = columnsToShow.toFloat()
        xAxisBar.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.data = barChartData
        barChart.groupBars(0f, 0.1f, 0.05f)
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
        setTitle(getString(R.string.group_by_week))
        loadData()
    }

    private fun setMonth() {
        Log.i(logTag, "Called setMonth()")
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = false
        dateTo = LocalDate.now()
        dateFrom = LocalDate.of(dateTo.year, dateTo.month, 1)
        state = PeriodState.MONTH
        setTitle(getString(R.string.group_by_month))
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
        binding.txtSubtitle.text = title
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    override fun onStop() {
        super.onStop()
        appViewModel.setAssetsPeriod(dateFrom, dateTo, state, datePickerSelection)
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

            val calendarConstraints =
                CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now())
            // Build
            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText(getString(R.string.select_period))
                    .setCalendarConstraints(calendarConstraints.build())
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