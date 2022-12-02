package com.perno97.financialmanagement

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.perno97.financialmanagement.database.AppViewModel
import com.perno97.financialmanagement.database.AppViewModelFactory
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.database.CategoryWithExpensesSum
import com.perno97.financialmanagement.databinding.FragmentMainBinding
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.roundToInt


private const val LOG_TAG = "MainFragment"

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
    private lateinit var dateRangePicker: MaterialDatePicker<Pair<Long, Long>>
    private val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        setMonth()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initListeners()
        //TODO disattivare bottoni in onPause()?
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //TODO salvare configurazione UI
    }

    private fun updateData() {
        binding.categoryList.removeAllViews()
        appViewModel.getCategoryBudgetsList(dateFrom, dateTo)
            .observe(viewLifecycleOwner, Observer {
                categoriesLoaded(it)
            })
    }

    private fun setDay() {
        binding.btnDay.isEnabled = false
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = true
        dateTo = LocalDate.now()
        dateFrom = dateTo
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
        setTitle("${dateTo.month} ${dateTo.year}")
        updateData()
    }

    private fun setPeriod() {
        binding.btnPeriod.isEnabled = false
        binding.btnDay.isEnabled = true
        binding.btnWeek.isEnabled = true
        binding.btnMonth.isEnabled = true
        /**
         * Update of [dateTo], [dateFrom], layout title and visualized data
         * is done inside [initDateRangePicker]
         */
        dateRangePicker.show(parentFragmentManager, "rangeDatePickerDialog")
    }

    private fun initDateRangePicker() {
        dateRangePicker =
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
            dateTo = to
            dateFrom = from
            setTitle(
                "${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year} " +
                        "- ${dateTo.dayOfMonth}/${dateTo.monthValue}/${dateTo.year}"
            )
            updateData()
            binding.btnPeriod.isEnabled = true
        }
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
            setPeriod()
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

    fun categoriesLoaded(categories: List<CategoryWithExpensesSum>) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        if (categories.isEmpty()) {
            entries.add(PieEntry(1f, "No data"))
            colors.add(ContextCompat.getColor(requireContext(), R.color.dark))
        } else {
            var budgetsSum = 0f
            var currentSum = 0f
            for (c in categories) {
                budgetsSum += c.budget
                currentSum += c.current
                val catProg =
                    layoutInflater.inflate(R.layout.category_progress, binding.categoryList, false)
                entries.add(PieEntry(c.current, c.name))
                binding.categoryList.addView(catProg)
                catProg.findViewById<TextView>(R.id.categoryColorLabel)
                    .backgroundTintList = ColorStateList.valueOf(Color.parseColor(c.color))
                colors.add(Color.parseColor(c.color))
                catProg.findViewById<TextView>(R.id.txtCategoryName).text = c.name
                val progressBar =
                    catProg.findViewById<LinearProgressIndicator>(R.id.progressBarCategoryBudget)
                progressBar.progress = (c.current * 100 / c.budget).roundToInt()
                progressBar.indicatorColor[0] = Color.parseColor(c.color)

                catProg.findViewById<TextView>(R.id.txtMaxCategoryBudget).text =
                    getString(R.string.euro_value, c.budget)
                catProg.findViewById<TextView>(R.id.txtCurrentCategoryProgress).text =
                    String.format("%.2f€", c.current)
                catProg.setOnClickListener {
                    parentFragmentManager.commit {
                        binding.categoryList.removeView(catProg)
                        replace(
                            R.id.fragment_container_view,
                            CategoryDetailsFragment(Category(c.name, c.color, c.budget))
                        )
                        addToBackStack(null)
                    }
                }
            }
            val diff = budgetsSum - currentSum
            if (diff > 0) {
                entries.add(PieEntry(diff, "Available"))
                colors.add(ContextCompat.getColor(requireContext(), R.color.dark))
            }
        }
        val dataSet = PieDataSet(entries, "Budgets")
        dataSet.colors = colors
        val pieData = PieData(dataSet)
        val chart = binding.pieChartMain
        chart.data = pieData
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        chart.setDrawEntryLabels(false)
        chart.invalidate()
    }
}