package com.perno97.financialmanagement

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.Pair
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.perno97.financialmanagement.database.AppViewModel
import com.perno97.financialmanagement.database.AppViewModelFactory
import com.perno97.financialmanagement.database.GroupedMovements
import com.perno97.financialmanagement.databinding.FragmentRegisteredMovementsBinding
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*

private const val LOG_TAG = "RegMovementsFragment"

class RegisteredMovementsFragment : Fragment() {

    private var _binding: FragmentRegisteredMovementsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }
    private lateinit var dateFrom :LocalDate
    private lateinit var dateTo :LocalDate
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

    private fun movementsLoaded(movements: List<GroupedMovements>) {
        if(movements.isEmpty()){
            return
        }
        else {
            for(m in movements){
                val card = LayoutInflater.from(requireContext()).inflate(R.layout.movement_card, binding.movementCardsContainer, false)
                card.findViewById<TextView>(R.id.txtHeaderDate).text = m.newDate
                card.findViewById<TextView>(R.id.txtHeaderNegative).text = m.negative.toString()
                card.findViewById<TextView>(R.id.txtHeaderPositive).text = m.positive.toString()
                binding.movementCardsContainer.addView(card)
            }
        }
    }

    private fun initListeners(){
        /*binding.singleRegisteredMov.setOnClickListener {
            parentFragmentManager.commit {
                add<FinancialMovementDetailsFragment>(R.id.fragment_container_view)
                addToBackStack(null)
            }
        }*/
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
            dateRangePicker.addOnPositiveButtonClickListener {
                    pair ->
                val from = Instant.ofEpochMilli(pair.first)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                val to = Instant.ofEpochMilli(pair.second)
                    .atZone(ZoneId.systemDefault()).toLocalDate()

                setCustomPeriod(from,to)
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
        setTitle("${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year} " +
                "- ${dateTo.dayOfMonth}/${dateTo.monthValue}/${dateTo.year}")
    }
    fun setCustomPeriod(from: LocalDate, to: LocalDate) {
        dateTo = to
        dateFrom = from
        setTitle("${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year} " +
                "- ${dateTo.dayOfMonth}/${dateTo.monthValue}/${dateTo.year}")
    }

    private fun setTitle(title: String) {
        binding.txtTitle.text = title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}