package com.perno97.financialmanagement.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.databinding.FragmentAddCategoryToFilterBinding
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import kotlinx.coroutines.launch

class AddCategoryToFilterFragment(private val parentCategory: Category) : Fragment() {
    private var _binding: FragmentAddCategoryToFilterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    private lateinit var allCategories: List<Category>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCategoryToFilterBinding.inflate(inflater, container, false)

        appViewModel.allCategories.observe(viewLifecycleOwner) { categoriesList ->
            allCategories = categoriesList
            viewLifecycleOwner.lifecycleScope.launch {
                appViewModel.uiState.collect {
                    showCategoriesList(it.categoryFilters)
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    private fun initListeners() {
        binding.fabConfirmAdd.setOnClickListener {
            binding.fabConfirmAdd.isEnabled = false
            confirmSelection()
        }
        binding.fabAbortAdd.setOnClickListener {
            binding.fabAbortAdd.isEnabled = false
            parentFragmentManager.popBackStack()
        }
    }

    private fun confirmSelection() {
        val newCategoryFilters = mutableListOf<Category>()
        for (view in binding.categoryListContainer.children) {
            val checkBox = view.findViewById<CheckBox>(R.id.checkSelectedFilter)
            if (checkBox.isChecked) {
                val catName = view.findViewById<TextView>(R.id.txtCategoryFilterName).text
                val catSelected = allCategories.filter { it.name == catName }
                newCategoryFilters.add(catSelected[0])
            }
        }
        appViewModel.setCategoryFilters(newCategoryFilters)
        parentFragmentManager.popBackStack()
    }

    private fun showCategoriesList(selectedCategories: List<Category>) {
        binding.categoryListContainer.removeAllViews()
        for (category in allCategories) {
            if (category.name != parentCategory.name) {
                val itemView = layoutInflater.inflate(
                    R.layout.category_line_add_to_filter,
                    binding.categoryListContainer,
                    false
                )
                itemView.findViewById<TextView>(R.id.categoryFilterColor).backgroundTintList =
                    ColorStateList.valueOf(
                        Color.parseColor(category.color)
                    )
                itemView.findViewById<TextView>(R.id.txtCategoryFilterName).text = category.name
                if (selectedCategories.contains(category))
                    itemView.findViewById<CheckBox>(R.id.checkSelectedFilter).isChecked = true
                binding.categoryListContainer.addView(itemView)
            }
        }
        if (binding.categoryListContainer.childCount == 0) {
            val view = layoutInflater.inflate(
                R.layout.no_categories_to_select_textview,
                binding.categoryListContainer,
                false
            )
            view.findViewById<TextView>(R.id.txtNoCategories).text =
                getString(R.string.no_categories_to_select)
            binding.categoryListContainer.addView(view)
        }
    }
}