package com.perno97.financialmanagement.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.util.Pair
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.*
import com.perno97.financialmanagement.databinding.FragmentRegisteredMovementsBinding
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*


class RegisteredMovementsFragment : Fragment() {

    private val logTag = "RegMovementsFragment"

    private var _binding: FragmentRegisteredMovementsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }
    private lateinit var dateFrom: LocalDate
    private lateinit var dateTo: LocalDate
    private var firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisteredMovementsBinding.inflate(inflater, container, false)
        initListeners()
        setPeriodMonth()
        binding.btnMonth.isEnabled = false
        loadData()
        return binding.root
    }

    private fun loadData() {
        appViewModel.movementsGroupByMonth.observe(viewLifecycleOwner) {
            movementsLoaded(it)
        }
    }

    private fun movementsLoaded(movements: Map<GroupInfo, List<MovementAndCategory>>) {
        if (movements.isEmpty()) {
            return
        } else {
            for (group in movements.keys) {
                val card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.movement_card, binding.movementCardsContainer, false)
                card.findViewById<TextView>(R.id.txtHeaderDate).text = group.newDate
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
                    lineContainer.addView(cardLine)
                }
                binding.movementCardsContainer.addView(card)
            }
        }
    }

    private fun initListeners() {
        /*binding.singleRegisteredMov.setOnClickListener {
            parentFragmentManager.commit {
                add<FinancialMovementDetailsFragment>(R.id.fragment_container_view)
                addToBackStack(null)
            }
        }*/
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
            binding.btnDay.isEnabled = true
            binding.btnWeek.isEnabled = true
            binding.btnMonth.isEnabled = false
            setPeriodMonth()
            loadData()
        }
        binding.btnDay.setOnClickListener {
            binding.btnDay.isEnabled = false
            binding.btnWeek.isEnabled = true
            binding.btnMonth.isEnabled = true
            setPeriodDay()
            loadData()
        }
        binding.btnWeek.setOnClickListener {
            binding.btnDay.isEnabled = true
            binding.btnWeek.isEnabled = false
            binding.btnMonth.isEnabled = true
            setPeriodWeek()
            loadData()
        }
        binding.btnPeriod.setOnClickListener {
            binding.btnPeriod.isEnabled = false
            binding.btnDay.isEnabled = true
            binding.btnWeek.isEnabled = true
            binding.btnMonth.isEnabled = true
            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select period")
                    .setSelection(
                        Pair(
                            MaterialDatePicker.thisMonthInUtcMilliseconds(),
                            MaterialDatePicker.todayInUtcMilliseconds()
                        )
                    )
                    .build()
            dateRangePicker.addOnPositiveButtonClickListener { pair ->
                val from = Instant.ofEpochMilli(pair.first)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                val to = Instant.ofEpochMilli(pair.second)
                    .atZone(ZoneId.systemDefault()).toLocalDate()

                setCustomPeriod(from, to)
                binding.btnPeriod.isEnabled = true
            }
            dateRangePicker.show(parentFragmentManager, "rangeDatePickerDialog")
        }
    }

    /**
     *  Sets period of visualized data
     */
    private fun setPeriodMonth() {
        dateTo = LocalDate.now()
        dateFrom = LocalDate.of(dateTo.year, dateTo.month, 1)
        setTitle("${dateTo.month} ${dateTo.year}")
    }

    private fun setPeriodDay() {
        dateTo = LocalDate.now()
        dateFrom = dateTo
        setTitle("${dateTo.dayOfMonth} ${dateTo.month} ${dateTo.year}")
    }

    private fun setPeriodWeek() {
        dateTo = LocalDate.now()
        dateFrom = LocalDate.now().with(TemporalAdjusters.previousOrSame(firstDayOfWeek));
        setTitle(
            "${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year} " +
                    "- ${dateTo.dayOfMonth}/${dateTo.monthValue}/${dateTo.year}"
        )
    }

    fun setCustomPeriod(from: LocalDate, to: LocalDate) {
        dateTo = to
        dateFrom = from
        setTitle(
            "${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year} " +
                    "- ${dateTo.dayOfMonth}/${dateTo.monthValue}/${dateTo.year}"
        )
    }

    private fun setTitle(title: String) {
        binding.txtTitle.text = title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}