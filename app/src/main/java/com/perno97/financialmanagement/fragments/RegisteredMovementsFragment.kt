package com.perno97.financialmanagement.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.util.Pair
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.*
import com.perno97.financialmanagement.databinding.FragmentRegisteredMovementsBinding
import com.perno97.financialmanagement.utils.PeriodState
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.abs


class RegisteredMovementsFragment : Fragment() {

    private val logTag = "RegMovementsFragment"

    private var _binding: FragmentRegisteredMovementsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }
    private lateinit var dateFrom: LocalDate
    private lateinit var dateTo: LocalDate
    private var firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    private var datePickerSelection: Pair<Long, Long>? = null
    private var state = PeriodState.MONTH

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisteredMovementsBinding.inflate(inflater, container, false)

        // Load UI data
        viewLifecycleOwner.lifecycleScope.launch {
            Log.i(logTag, "Launched Coroutine")
            appViewModel.uiState.collect {
                Log.i(logTag, "Collecting UI data")
                dateFrom = it.dateFromMain ?: LocalDate.now().minusDays(1)
                dateTo = it.dateToMain ?: LocalDate.now()
                state = it.stateMain ?: PeriodState.MONTH
                datePickerSelection = it.datePickerSelectionMain
                when (state) {
                    PeriodState.DAY -> setDay()
                    PeriodState.WEEK -> setWeek()
                    PeriodState.MONTH -> setMonth()
                    PeriodState.PERIOD -> setPeriod(dateFrom, dateTo)
                }
            }
        }

        setMonth()
        binding.btnMonth.isEnabled = false
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    private fun loadData() {
        when (state) {
            PeriodState.DAY -> appViewModel.movementsGroupByDay.observe(viewLifecycleOwner) {
                movementsLoaded(it)
            }
            PeriodState.WEEK -> appViewModel.getMovementsGroupByWeek(
                abs(
                    ChronoUnit.DAYS.between(
                        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)),
                        LocalDate.now()
                    ).toInt()
                )
            ).observe(viewLifecycleOwner) {
                movementsLoaded(it)
            }
            PeriodState.MONTH -> appViewModel.movementsGroupByMonth.observe(viewLifecycleOwner) {
                movementsLoaded(it)
            }
            PeriodState.PERIOD -> appViewModel.getMovementsInPeriod(dateFrom, dateTo)
                .observe(viewLifecycleOwner) {
                    movementsLoaded(it)
                }
        }

    }

    private fun movementsLoaded(movements: Map<GroupInfo, List<MovementAndCategory>>) {
        binding.movementCardsContainer.removeAllViews()
        if (movements.isEmpty()) {
            return
        } else {
            for (group in movements.keys) {
                val card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.movement_card, binding.movementCardsContainer, false)
                var cardDate = ""
                val groupDate = group.groupDate
                when (state) {
                    PeriodState.DAY -> cardDate =
                        "${groupDate.dayOfMonth}/${groupDate.monthValue}/${groupDate.year}"
                    PeriodState.WEEK -> {
                        val weekFrom =
                            groupDate.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                        val weekTo = groupDate.plusDays(7)
                        cardDate =
                            "${weekFrom.dayOfMonth}/${weekFrom.monthValue}/${weekFrom.year}" +
                                    "-\n" +
                                    "${weekTo.dayOfMonth}/${weekTo.monthValue}/${weekTo.year}"
                    }
                    PeriodState.MONTH -> cardDate = "${groupDate.month} ${groupDate.year}"
                    PeriodState.PERIOD -> cardDate =
                        "${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year}" +
                                "-\n" +
                                "${dateTo.dayOfMonth}/${dateTo.monthValue}/${dateTo.year}"
                }
                card.findViewById<TextView>(R.id.txtHeaderDate).text = cardDate
                card.findViewById<TextView>(R.id.txtHeaderNegative).text =
                    String.format("%.2f€", group.negative)
                card.findViewById<TextView>(R.id.txtHeaderPositive).text =
                    String.format("%.2f€", group.positive)
                for (mov in movements[group]!!) {
                    val lineContainer = card.findViewById<LinearLayout>(R.id.movementLinesContainer)
                    val cardLine = LayoutInflater.from(requireContext())
                        .inflate(R.layout.movement_line, lineContainer, false)
                    cardLine.findViewById<TextView>(R.id.txtCatLineColor)
                        .backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor(mov.category.color))
                    cardLine.findViewById<TextView>(R.id.txtCatLineName).text = mov.category.name
                    cardLine.findViewById<TextView>(R.id.txtMovLineTitle).text = mov.movement.title
                    cardLine.findViewById<TextView>(R.id.txtMovLineAmount).text =
                        String.format("%.2f€", mov.movement.amount)
                    cardLine.setOnClickListener {
                        parentFragmentManager.commit {
                            replace(
                                R.id.fragment_container_view,
                                FinancialMovementDetailsFragment()
                            )
                            addToBackStack(null)
                        }
                    }
                    lineContainer.addView(cardLine)
                }
                binding.movementCardsContainer.addView(card)
            }
        }
    }

    private fun initListeners() {
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
        binding.fabBtnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.btnMonth.setOnClickListener {
            setMonth()
        }
        binding.btnDay.setOnClickListener {
            setDay()
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

    private fun setDay() {
        Log.i(logTag, "Called setDay()")
        binding.btnDay.isEnabled = false
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = true
        state = PeriodState.DAY
        setTitle("GROUP BY DAY")
        loadData()
    }

    private fun setWeek() {
        Log.i(logTag, "Called setWeek()")
        binding.btnDay.isEnabled = true
        binding.btnWeek.isEnabled = false
        binding.btnMonth.isEnabled = true
        state = PeriodState.WEEK
        setTitle("GROUP BY WEEK")
        loadData()
    }

    private fun setMonth() {
        Log.i(logTag, "Called setMonth()")
        binding.btnDay.isEnabled = true
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = false
        state = PeriodState.MONTH
        setTitle("${dateTo.month} ${dateTo.year}")
        loadData()
    }

    private fun setPeriod(from: LocalDate, to: LocalDate) {
        Log.i(logTag, "Called setPeriod()")
        binding.btnDay.isEnabled = true
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = true
        dateTo = to
        dateFrom = from
        state = PeriodState.PERIOD
        setTitle("SELECTED PERIOD")
        loadData()
    }

    private fun setTitle(title: String) {
        binding.txtTitle.text = title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}