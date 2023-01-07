package com.perno97.financialmanagement.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.GroupInfo
import com.perno97.financialmanagement.database.IncomingMovement
import com.perno97.financialmanagement.database.IncomingMovementAndCategory
import com.perno97.financialmanagement.databinding.FragmentIncomingMovementsBinding
import com.perno97.financialmanagement.utils.MovementDeletionData
import com.perno97.financialmanagement.utils.MovementDetailsData
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
import kotlin.math.absoluteValue


class IncomingMovementsFragment : Fragment() {

    private val logTag = "IncomingMovementsFragment"

    private var _binding: FragmentIncomingMovementsBinding? = null

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
    private var weekStartOffset = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomingMovementsBinding.inflate(inflater, container, false)

        val f = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val t = f.with(TemporalAdjusters.previous(firstDayOfWeek))
        // Count days from the next sunday to the previous firstDayOfWeek
        // Workaround for having custom first day of week in SQLite
        weekStartOffset = ChronoUnit.DAYS.between(f, t).toInt().absoluteValue

        // Load UI data
        viewLifecycleOwner.lifecycleScope.launch {
            Log.i(logTag, "Launched Coroutine")
            appViewModel.uiState.collect {
                Log.i(logTag, "Collecting UI data")
                dateFrom = it.dateFromIncoming
                dateTo = it.dateToIncoming
                state = it.stateIncoming
                datePickerSelection = it.datePickerSelectionIncoming
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
    }

    override fun onStop() {
        super.onStop()
        appViewModel.setIncomingPeriod(
            dateFrom,
            dateTo,
            state,
            datePickerSelection
        ) // Saving UI state
    }

    private fun loadData() {
        when (state) {
            // ----------------- DAY -----------------
            PeriodState.DAY -> appViewModel.getIncomingMovementsGroupByDay()
                .observe(viewLifecycleOwner) {
                    movementsLoaded(it)
                }
            // ----------------- WEEK ----------------
            PeriodState.WEEK -> appViewModel.getIncomingMovementsGroupByWeek(
                weekStartOffset
            ).observe(viewLifecycleOwner) {
                movementsLoaded(it)
            }
            // ---------------- MONTH ----------------
            PeriodState.MONTH -> appViewModel.getIncomingMovementsGroupByMonth()
                .observe(viewLifecycleOwner) {
                    movementsLoaded(it)
                }
            // ---------------- PERIOD ----------------
            PeriodState.PERIOD -> appViewModel.getIncomingMovementsInPeriod(dateFrom, dateTo)
                .observe(viewLifecycleOwner) {
                    movementsLoaded(it)
                }
            // ----------------------------------------
        }

    }

    private fun movementsLoaded(movements: Map<GroupInfo, List<IncomingMovementAndCategory>>) {
        binding.movementCardsContainer.removeAllViews()
        if (movements.isEmpty()) {
            return
        } else {
            for (group in movements.keys) {
                val card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.movement_card, binding.movementCardsContainer, false)
                var cardDate = ""
                //val groupDate = LocalDate.parse(group.groupDate)
                when (state) {
                    PeriodState.DAY -> {
                        val groupDate = LocalDate.parse(group.groupDate)
                        cardDate =
                            "${groupDate.dayOfMonth}/${groupDate.monthValue}/${groupDate.year}"
                    }
                    PeriodState.WEEK -> {
                        val groupDate = LocalDate.parse(group.groupDate)
                        val weekFrom =
                            groupDate.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                        val weekTo = groupDate.plusDays(6)
                        cardDate =
                            "${weekFrom.dayOfMonth}/${weekFrom.monthValue}/${weekFrom.year}" +
                                    "-\n" +
                                    "${weekTo.dayOfMonth}/${weekTo.monthValue}/${weekTo.year}"
                    }
                    PeriodState.MONTH -> {
                        val groupDate = LocalDate.parse(group.groupDate)
                        cardDate = "${groupDate.month} ${groupDate.year}"
                    }
                    PeriodState.PERIOD -> {
                        cardDate =
                            "${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year}" +
                                    "-\n" +
                                    "${dateTo.dayOfMonth}/${dateTo.monthValue}/${dateTo.year}"
                    }
                }
                card.findViewById<TextView>(R.id.txtHeaderDate).text = cardDate
                card.findViewById<TextView>(R.id.txtHeaderNegative).text =
                    getString(R.string.euro_value, group.negative)
                card.findViewById<TextView>(R.id.txtHeaderPositive).text =
                    getString(R.string.euro_value, group.positive)
                for (mov in movements[group]!!) {
                    val lineContainer = card.findViewById<LinearLayout>(R.id.movementLinesContainer)
                    val cardLine = LayoutInflater.from(requireContext())
                        .inflate(R.layout.movement_line_incoming, lineContainer, false)
                    if (!mov.incomingMovement.date.isAfter(LocalDate.now())) { // TODO usare colore più sul grigio D9D9D9
                        cardLine.findViewById<LinearLayout>(R.id.singleRegisteredMov)
                            .setBackgroundColor(
                                ContextCompat.getColor(requireContext(), R.color.grey)
                            )
                    }
                    cardLine.findViewById<TextView>(R.id.txtCatLineColor)
                        .backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor(mov.category.color))
                    cardLine.findViewById<TextView>(R.id.txtCatLineName).text = mov.category.name
                    cardLine.findViewById<TextView>(R.id.txtMovLineTitle).text =
                        mov.incomingMovement.title
                    cardLine.findViewById<ImageView>(R.id.imgPeriodic).visibility =
                        if (mov.incomingMovement.periodicMovementId != null) View.VISIBLE else View.GONE
                    cardLine.findViewById<TextView>(R.id.txtMovLineAmount).text =
                        getString(R.string.euro_value, mov.incomingMovement.amount)
                    cardLine.findViewById<Button>(R.id.btnDelete).setOnClickListener {
                        deleteIncomingMovement(mov.incomingMovement)
                    }
                    cardLine.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
                        confirmIncomingMovement(mov.incomingMovement)
                    }
                    cardLine.findViewById<LinearLayout>(R.id.singleRegisteredMov)
                        .setOnClickListener {
                            parentFragmentManager.commit {
                                replace(
                                    R.id.fragment_container_view,//mov
                                    FinancialMovementDetailsFragment(
                                        MovementDetailsData(
                                            movementId = null,
                                            date = mov.incomingMovement.date,
                                            amount = mov.incomingMovement.amount,
                                            category = mov.incomingMovement.category,
                                            color = mov.category.color,
                                            title = mov.incomingMovement.title,
                                            notes = mov.incomingMovement.notes,
                                            periodicMovementId = null,
                                            weekDays = null,
                                            days = 0,
                                            months = 0,
                                            notify = mov.incomingMovement.notify,
                                            incomingMovementId = mov.incomingMovement.incomingMovementId
                                        )
                                    )
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

    private fun confirmIncomingMovement(incomingMovement: IncomingMovement) {
        ConfirmIncomingMovementDialog(incomingMovement).show(
            childFragmentManager, ConfirmIncomingMovementDialog.TAG
        )
    }

    private fun deleteIncomingMovement(incomingMovement: IncomingMovement) {
        ConfirmMovementDeleteDialog(
            MovementDeletionData(
                date = incomingMovement.date,
                title = incomingMovement.title,
                incomingMovementId = incomingMovement.incomingMovementId,
                movementId = null,
                periodicMovementId = null,
                amount = incomingMovement.amount,
                notify = incomingMovement.notify,
                category = incomingMovement.category
            )
        ).show(
            childFragmentManager, ConfirmMovementDeleteDialog.TAG
        )
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
        setTitle(getString(R.string.group_by_day))
        loadData()
    }

    private fun setWeek() {
        Log.i(logTag, "Called setWeek()")
        binding.btnDay.isEnabled = true
        binding.btnWeek.isEnabled = false
        binding.btnMonth.isEnabled = true
        state = PeriodState.WEEK
        setTitle(getString(R.string.group_by_week))
        loadData()
    }

    private fun setMonth() {
        Log.i(logTag, "Called setMonth()")
        binding.btnDay.isEnabled = true
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = false
        state = PeriodState.MONTH
        val month = dateTo.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }
        setTitle(getString(R.string.month_year, month, dateTo.year.toString()))
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
        setTitle(getString(R.string.selected_period))
        loadData()
    }

    private fun setTitle(title: String) {
        binding.txtSubtitle.text = title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}