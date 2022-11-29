package com.perno97.financialmanagement

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.perno97.financialmanagement.database.AppViewModel
import com.perno97.financialmanagement.database.AppViewModelFactory
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

class MainFragment : Fragment(), ICustomPeriod{

    private var _binding: FragmentMainBinding? = null
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
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        initListeners()

        setPeriodMonth()
        binding.btnMonth.isEnabled = false
        loadData()

        return binding.root
    }

    private fun loadData() {
        appViewModel.getCategoryBudgetsList(dateFrom, dateTo)
            .observe(viewLifecycleOwner, Observer {
                categoriesLoaded(it)
            })
    }

    private fun initListeners(){
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
        binding.btnPeriod.setOnClickListener { //TODO disattivare appena premuto sennò se ne aprono molti
            binding.btnDay.isEnabled = true
            binding.btnWeek.isEnabled = true
            binding.btnMonth.isEnabled = true
            /*DatePickerFragment(null, this).show(
                parentFragmentManager, DatePickerFragment.TAG
            )*/
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
            }
            dateRangePicker.show(parentFragmentManager, DatePickerFragment.TAG)
        }
        binding.txtCurrentValue.setOnClickListener {
            // TODO non si capisce che il testo è cliccabile
            Log.i(LOG_TAG, "Clicked edit current assets value")
            EditCurrentAssetsDialog().show(
                childFragmentManager, EditCurrentAssetsDialog.TAG
            )
        }
        binding.imgBtnGraphs.setOnClickListener{
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
    override fun setCustomPeriod(from: LocalDate, to: LocalDate) {
        dateTo = to
        dateFrom = from
        setTitle("${dateFrom.dayOfMonth}/${dateFrom.monthValue}/${dateFrom.year} " +
                "- ${dateTo.dayOfMonth}/${dateTo.monthValue}/${dateTo.year}")
    }

    private fun setTitle(title: String) {
        binding.txtTitle.text = title
    }

    fun categoriesLoaded(categories: List<CategoryWithExpensesSum>) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        if(categories.isEmpty()){
            entries.add(PieEntry(1f, "No data"))
            colors.add(ContextCompat.getColor(requireContext(), R.color.dark))
        }
        else {
            var budgetsSum = 0f
            var currentSum = 0f
            for (c in categories) {
                budgetsSum += c.budget
                currentSum += c.current
                val catProg = layoutInflater.inflate(R.layout.category_progress, binding.categoryList, false)
                entries.add(PieEntry(c.current, c.name))
                binding.categoryList.addView(catProg)
                catProg.findViewById<TextView>(R.id.categoryColorLabel)
                    .backgroundTintList = ColorStateList.valueOf(Color.parseColor(c.color))
                colors.add(Color.parseColor(c.color))
                catProg.findViewById<TextView>(R.id.txtCategoryName).text = c.name
                val progressBar = catProg.findViewById<LinearProgressIndicator>(R.id.progressBarCategoryBudget)
                progressBar.progress = (c.current * 100 / c.budget).roundToInt()
                /*val progressDrawable = progressBar.progressDrawable.mutate()
                progressDrawable.setColorFilter(Color.parseColor(c.color), PorterDuff.Mode.SRC_IN)
                progressBar.progressDrawable = progressDrawable*/
                progressBar.indicatorColor[0] = Color.parseColor(c.color)

                catProg.findViewById<TextView>(R.id.txtMaxCategoryBudget).text = getString(R.string.euro_value, c.budget)
                catProg.findViewById<TextView>(R.id.txtCurrentCategoryProgress).text =
                    getString(R.string.euro_value, c.current)
                catProg.setOnClickListener {
                    parentFragmentManager.commit {
                        binding.categoryList.removeView(catProg)
                        replace(R.id.fragment_container_view, CategoryDetailsFragment(catProg))
                        addToBackStack(null)
                    }
                }
            }
            val diff = budgetsSum-currentSum
            if (diff > 0) {
                entries.add(PieEntry(diff, "Available"))
                colors.add(Color.GRAY)
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