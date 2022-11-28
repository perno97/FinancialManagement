package com.perno97.financialmanagement

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.perno97.financialmanagement.database.AppViewModel
import com.perno97.financialmanagement.database.AppViewModelFactory
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.databinding.FragmentAddNewCategoryDialogBinding
import com.perno97.financialmanagement.databinding.FragmentEditCurrentAssetsDialogBinding
import com.perno97.financialmanagement.utils.SpinnerAdapter

class AddNewCategoryDialog : DialogFragment() {

    private var _binding: FragmentAddNewCategoryDialogBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewCategoryDialogBinding.inflate(inflater, container, false)
        val spinnerAdapter = SpinnerAdapter(requireContext())
        binding.spinnerColor.adapter = spinnerAdapter
        binding.btnConfirmNewCategory.setOnClickListener {
            confirmAction()
        }
        binding.btnCancelNewCategory.setOnClickListener {
            cancelAction()
        }
        return binding.root
    }

    private fun confirmAction() {
        val name = binding.editTextNewCatName.text.toString()
        val color = binding.spinnerColor.selectedItem
        val budget = binding.editTextNewCatBudget.text.toString().toFloat()
        appViewModel.insert(
            Category(
                name = name,
                color = color.toString(),
                budget = budget
            )
        )
        dismiss()
    }

    private fun cancelAction() {
        dismiss()
        //TODO
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddNewCategoryDialog"
    }
}