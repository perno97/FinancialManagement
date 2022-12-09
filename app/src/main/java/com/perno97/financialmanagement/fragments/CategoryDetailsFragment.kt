package com.perno97.financialmanagement.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.AppViewModel
import com.perno97.financialmanagement.database.AppViewModelFactory
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.database.Expense
import com.perno97.financialmanagement.databinding.FragmentCategoryDetailsBinding
import com.perno97.financialmanagement.utils.PeriodState
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

class CategoryDetailsFragment(private val category: Category, private val expense: Expense) :
    Fragment() {
    /**
     * Connection to persistent data
     */
    private val appViewModel: AppViewModel by viewModels {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryDetailsBinding.inflate(inflater, container, false)

        updateCategoryProgress()

        // Load UI data
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            appViewModel.uiState.collect {
                dateFrom = it.dateFromCatDetails ?: LocalDate.now().minusDays(1)
                dateTo = it.dateToCatDetails ?: LocalDate.now()
                state = it.stateCatDetails ?: PeriodState.MONTH
                datePickerSelection = it.datePickerSelectionCatDetails
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
        return binding.root
    }

    private fun updateCategoryProgress() {
        binding.txtCategoryName.text = category.name
        val budgetMultiplier: Int = when (state) {
            PeriodState.DAY -> 1
            PeriodState.WEEK -> 7
            PeriodState.MONTH -> LocalDate.now().lengthOfMonth()
            PeriodState.PERIOD -> ChronoUnit.DAYS.between(dateFrom, dateTo)
                .toInt() + 1 // Add 1 because between is exclusive
        } // Budget is defined as daily budget
        val multipliedBudget = category.budget * budgetMultiplier
        val currentCatExpenseAsPositive = -expense.expense
        val progress = (currentCatExpenseAsPositive * 100 / multipliedBudget).roundToInt()
        binding.progressBarCategoryBudget.indicatorColor[0] = Color.parseColor(category.color)
        binding.progressBarCategoryBudget.progress = progress
        binding.txtMaxCategoryBudget.text = String.format("%.2f", multipliedBudget)
        binding.txtCurrentCategoryProgress.text = String.format("%.2f", expense.expense)
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    override fun onStop() {
        super.onStop()
        appViewModel.setCatDetailsPeriod(
            dateFrom,
            dateTo,
            state,
            datePickerSelection
        ) //Saving UI state
    }

    private fun updateData() {
        //TODO
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
        updateData()
    }

    private fun setMonth() {
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = false
        dateTo = LocalDate.now()
        dateFrom = LocalDate.of(dateTo.year, dateTo.month, 1)
        state = PeriodState.MONTH
        setTitle("${dateTo.month} ${dateTo.year}")
        updateData()
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
        updateData()
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
                add<AddCategoryToFilterFragment>(R.id.fragment_container_view)
                addToBackStack(null)
            }
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