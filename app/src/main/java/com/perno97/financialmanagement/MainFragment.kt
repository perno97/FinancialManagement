package com.perno97.financialmanagement

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.perno97.financialmanagement.database.AppViewModel
import com.perno97.financialmanagement.database.AppViewModelFactory
import com.perno97.financialmanagement.database.CategoryWithExpensesSum
import com.perno97.financialmanagement.databinding.FragmentMainBinding
import java.time.LocalDate
import kotlin.math.roundToInt

private const val LOG_TAG = "MainFragment"

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var editAssetsDialog: AlertDialog
    private val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }
    private lateinit var dateFrom :LocalDate
    private lateinit var dateTo :LocalDate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        initListeners()

        dateTo = LocalDate.now()
        dateFrom = LocalDate.of(dateTo.year, dateTo.month, 1)

        appViewModel.getCategoryBudgetsList(dateFrom, dateTo, this)
            .asLiveData()
            .observe(viewLifecycleOwner, Observer {
            categoriesLoaded(it)
        })


        //appViewModel.deleteAllCategories()
        //appViewModel.insert(Category(name = "Groceries", color = "#0000FF", budget = 100f))



        return binding.root
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

    fun categoriesLoaded(categories: List<CategoryWithExpensesSum>) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        if(categories.isEmpty()){
            entries.add(PieEntry(1f, "No data"))
            colors.add(Color.GRAY)
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
                catProg.findViewById<TextView>(R.id.categoryColorLabel).setBackgroundColor(Color.parseColor(c.color))
                colors.add(Color.parseColor(c.color))
                catProg.findViewById<TextView>(R.id.txtCategoryName).text = c.name
                val progressBar = catProg.findViewById<ProgressBar>(R.id.progressBarCategoryBudget)
                progressBar.progress = (c.current * 100 / c.budget).roundToInt()
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