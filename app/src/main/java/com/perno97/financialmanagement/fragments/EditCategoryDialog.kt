package com.perno97.financialmanagement.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.databinding.FragmentEditCategoryDialogBinding
import com.perno97.financialmanagement.utils.ColorsSpinnerAdapter
import com.perno97.financialmanagement.utils.DecimalDigitsInputFilter

class EditCategoryDialog(private val category: Category) : DialogFragment() {

    private var _binding: FragmentEditCategoryDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCategoryDialogBinding.inflate(inflater, container, false)
        val colorsSpinnerAdapter = ColorsSpinnerAdapter(requireContext())
        binding.spinnerColor.adapter = colorsSpinnerAdapter
        binding.btnConfirmEditCategory.setOnClickListener {
            confirmAction()
        }
        binding.btnCancelEditCategory.setOnClickListener {
            cancelAction()
        }
        binding.editTextNewCatName.setText(category.name)
        binding.editTextNewCatBudget.setText(String.format("%.2f", category.budget))
        val index = colorsSpinnerAdapter.getIndexFromColor(category.color)
        binding.spinnerColor.setSelection(index)
        binding.editTextNewCatBudget.filters =
            arrayOf(DecimalDigitsInputFilter(binding.editTextNewCatBudget))
        return binding.root
    }

    private fun confirmAction() {
        val name = binding.editTextNewCatName.text.toString()
        val color = binding.spinnerColor.selectedItem.toString()
        val budget = String.format("%.2f", binding.editTextNewCatBudget.text.toString()).toFloat()
        appViewModel.insert(
            Category(
                name = name,
                color = color,
                budget = budget
            )
        )
        dismiss()
    }

    private fun cancelAction() {
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditCategoryDialog"
    }
}