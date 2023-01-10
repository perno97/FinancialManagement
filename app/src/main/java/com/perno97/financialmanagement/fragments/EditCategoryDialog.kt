package com.perno97.financialmanagement.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.databinding.FragmentEditCategoryDialogBinding
import com.perno97.financialmanagement.utils.ColorsSpinnerAdapter
import com.perno97.financialmanagement.utils.DecimalDigitsInputFilter
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory

class EditCategoryDialog(private val category: Category) : DialogFragment() {

    private var _binding: FragmentEditCategoryDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCategoryDialogBinding.inflate(inflater, container, false)
        if (dialog != null)
            dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val colorsSpinnerAdapter = ColorsSpinnerAdapter(requireContext())
        binding.spinnerColor.adapter = colorsSpinnerAdapter
        binding.btnConfirmEditCategory.setOnClickListener {
            confirmAction()
        }
        binding.btnCancelEditCategory.setOnClickListener {
            cancelAction()
        }
        binding.editTextNewCatName.setText(category.name)
        binding.editTextNewCatBudget.setText(
            getString(
                R.string.number_decimal,
                category.budget
            ).replace(',', '.')
        )
        val index = colorsSpinnerAdapter.getIndexFromColor(category.color)
        binding.spinnerColor.setSelection(index)
        binding.editTextNewCatBudget.filters =
            arrayOf(DecimalDigitsInputFilter(binding.editTextNewCatBudget))
        return binding.root
    }

    private fun confirmAction() {
        val name = binding.editTextNewCatName.text.toString()
        val color = binding.spinnerColor.selectedItem.toString()
        val budget = binding.editTextNewCatBudget.text.toString().toFloat()
        appViewModel.update(
            Category(
                name = name.trim(),
                color = color,
                budget = budget
            )
        )
        Snackbar.make(
            binding.btnConfirmEditCategory,
            R.string.success_edit_category,
            BaseTransientBottomBar.LENGTH_LONG
        ).setBackgroundTint(
            ContextCompat.getColor(
                requireContext(),
                R.color.success
            )
        ).show()
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