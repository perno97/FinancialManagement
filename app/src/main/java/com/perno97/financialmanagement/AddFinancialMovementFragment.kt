package com.perno97.financialmanagement

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.perno97.financialmanagement.database.AppViewModel
import com.perno97.financialmanagement.database.AppViewModelFactory
import com.perno97.financialmanagement.database.Movement
import com.perno97.financialmanagement.databinding.FragmentAddFinancialMovementBinding
import java.time.LocalDate

private const val LOG_TAG = "AddFinMovFragment"

class AddFinancialMovementFragment : Fragment() {

    private var _binding: FragmentAddFinancialMovementBinding? = null
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
        _binding = FragmentAddFinancialMovementBinding.inflate(inflater, container, false)
        binding.editTextMovementDate.setText(LocalDate.now().toString())
        val spinnerAdapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_row)
        spinnerAdapter.add("Prova")
        binding.spinnerCategory.adapter = spinnerAdapter
        binding.btnAddNewCategory.setOnClickListener {
            AddNewCategoryDialog().show(
                childFragmentManager, AddNewCategoryDialog.TAG
            )
        }
        binding.fabConfirmNew.setOnClickListener {
            addMovementToDatabase()
        }
        binding.fabAbortNew.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        return binding.root
    }

    private fun addMovementToDatabase() {
        val date = LocalDate.parse(binding.editTextMovementDate.text)
        val amount :Float = binding.editTextMovAmount.text.toString().toFloat()
        val category = binding.spinnerCategory.selectedItem
        Log.e(LOG_TAG, "Category --> $category")
        val movement = Movement(
            date = date,
            amount = amount,
            category = 0
        )
        appViewModel.insert(movement)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}