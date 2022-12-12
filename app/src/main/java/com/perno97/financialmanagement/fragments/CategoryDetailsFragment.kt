package com.perno97.financialmanagement.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.Pair
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.datepicker.MaterialDatePicker
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.database.Expense
import com.perno97.financialmanagement.databinding.FragmentCategoryDetailsBinding
import com.perno97.financialmanagement.utils.PeriodState
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import com.perno97.financialmanagement.viewmodels.CategoryFiltersUiState
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.roundToInt

class CategoryDetailsFragment(private val category: Category, private val expense: Expense) :
    Fragment() {
    private val logTag = "CategoryDetailsFragment"

    /**
     * Connection to data
     */
    private val appViewModel: AppViewModel by activityViewModels {
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
    private var categoryFilters: List<Category> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.e(logTag, "Called onCreateView")
        _binding = FragmentCategoryDetailsBinding.inflate(inflater, container, false)

        updateCategoryProgress()

        //updateFiltersList()

        // Load UI data
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appViewModel.uiState.collect {
                    Log.e(logTag, "Collected UI data")
                    dateFrom = it.dateFromCatDetails ?: LocalDate.now().minusDays(1)
                    dateTo = it.dateToCatDetails ?: LocalDate.now()
                    state = it.stateCatDetails ?: PeriodState.MONTH
                    datePickerSelection = it.datePickerSelectionCatDetails
                    categoryFilters = it.categoryFilters
                    updateFiltersList()
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

        /*appViewModel.categoryFiltersState.observe(viewLifecycleOwner) {
            categoryFilters = it.categoryFilters
            updateFiltersList()
        }*/

        return binding.root
    }

    private fun updateFiltersList() {
        binding.filtersContainer.removeAllViews()
        for (filter in categoryFilters) {
            val itemView = layoutInflater.inflate(
                R.layout.category_filter_item,
                binding.filtersContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.categoryFilterColor).backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(filter.color))
            itemView.findViewById<TextView>(R.id.txtCategoryFilterName).text = filter.name
            binding.filtersContainer.addView(itemView)
        }
    }

    private fun updateCategoryProgress() { // TODO unire a updateData()?
        binding.txtCategoryName.text = category.name
        val budgetMultiplier: Int = when (state) {
            PeriodState.DAY -> 1
            PeriodState.WEEK -> 7
            PeriodState.MONTH -> LocalDate.now().lengthOfMonth()
            PeriodState.PERIOD -> ChronoUnit.DAYS.between(dateFrom, dateTo)
                .toInt() + 1// Add 1 because between is exclusive
        } // Budget is defined as daily budget
        val multipliedBudget = category.budget * budgetMultiplier
        val currentCatExpenseAsPositive = -expense.expense
        val progress = (currentCatExpenseAsPositive * 100 / multipliedBudget).roundToInt()
        binding.progressBarCategoryBudget.indicatorColor[0] = Color.parseColor(category.color)
        binding.progressBarCategoryBudget.progress = progress
        binding.txtMaxCategoryBudget.text = String.format("%.2f", multipliedBudget)
        binding.txtCurrentCategoryProgress.text = String.format("%.2f", currentCatExpenseAsPositive)
    }

    override fun onResume() {
        super.onResume()
        initListeners()
        updateFiltersList()
    }

    override fun onStop() {
        super.onStop()
        appViewModel.setCatDetailsPeriod(
            dateFrom,
            dateTo,
            state,
            datePickerSelection
        ) //Saving UI state

        appViewModel.setCategoryFilters(listOf())
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
        updateCategoryProgress()
        updateData()
    }

    private fun setMonth() {
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = false
        dateTo = LocalDate.now()
        dateFrom = LocalDate.of(dateTo.year, dateTo.month, 1)
        state = PeriodState.MONTH
        setTitle("${dateTo.month} ${dateTo.year}")
        updateCategoryProgress()
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
        updateCategoryProgress()
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
                add(R.id.fragment_container_view, AddCategoryToFilterFragment(category))
                addToBackStack(null)
            }
        }
        binding.fabAddMovement.setOnClickListener {
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
    }

    private fun setTitle(title: String) {
        binding.txtTitle.text = title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}