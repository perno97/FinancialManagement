package com.perno97.financialmanagement.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.databinding.FragmentConfirmExistingCategoryEditBinding
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import kotlinx.coroutines.launch

class ConfirmExistingCategoryEditDialog(
    private val oldCategoryData: Category,
    private val newCategoryData: Category
) : DialogFragment() {
    private val logTag = "ConfirmExistingCategoryEditDialog"
    private var _binding: FragmentConfirmExistingCategoryEditBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmExistingCategoryEditBinding.inflate(inflater, container, false)
        if (dialog != null)
            dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.txtConfirmationMessage.text =
            getString(R.string.confirm_existing_category_edit, newCategoryData.name)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    private fun initListeners() {
        binding.btnConfirmEdit.setOnClickListener {
            confirmEdit()
        }
        binding.btnAbortEdit.setOnClickListener {
            dismiss()
        }
    }

    private fun confirmEdit() {
        appViewModel.updateCategoryNameInMovements(oldCategoryData.name, newCategoryData.name)
        appViewModel.viewModelScope.launch {
            val cat = appViewModel.getCategoryByName(newCategoryData.name)
            if (cat != null) {
                appViewModel.deleteCategory(cat)
                appViewModel.update(newCategoryData)
            } else {
                Log.e(logTag, getString(R.string.error_category_conflict_not_found))
            }
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        parentFragmentManager.popBackStack()
    }

    companion object {
        const val TAG = "ConfirmExistingCategoryEditDialog"
    }
}