package com.perno97.financialmanagement.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.AppViewModel
import com.perno97.financialmanagement.database.AppViewModelFactory
import com.perno97.financialmanagement.database.Movement
import com.perno97.financialmanagement.databinding.FragmentAddFinancialMovementBinding
import com.perno97.financialmanagement.utils.DecimalDigitsInputFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class AddFinancialMovementFragment : Fragment() {

    private var _binding: FragmentAddFinancialMovementBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    // Outcome is default
    private var income = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFinancialMovementBinding.inflate(inflater, container, false)
        binding.editTextMovementDate.inputType = InputType.TYPE_NULL
        binding.editTextMovementDate.setText(LocalDate.now().toString())

        binding.editTextMovementDate.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val datePicker =
                    MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select period")
                        .setSelection(
                            MaterialDatePicker.todayInUtcMilliseconds()
                        )
                        .build()
                datePicker.addOnPositiveButtonClickListener { value ->
                    val date = Instant.ofEpochMilli(value)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                    binding.editTextMovementDate.setText(date.toString())
                }
                datePicker.show(parentFragmentManager, "rangeDatePickerDialog")
            }
            false
        }
        val spinnerAdapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_row)
        appViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            spinnerAdapter.clear()
            for (c in categories) {
                spinnerAdapter.add(c.name)
            }
        }
        binding.spinnerCategory.adapter = spinnerAdapter
        binding.editTextMovAmount.filters =
            arrayOf(DecimalDigitsInputFilter(binding.editTextMovAmount))
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
        binding.btnIncome.setOnClickListener {
            binding.btnIncome.isEnabled = false
            binding.btnOutcome.isEnabled = true
            income = true
        }
        // Outcome is default
        binding.btnOutcome.isEnabled = false
        binding.btnOutcome.setOnClickListener {
            binding.btnIncome.isEnabled = true
            binding.btnOutcome.isEnabled = false
            income = false
        }
        return binding.root
    }

    private fun addMovementToDatabase() {
        val date = binding.editTextMovementDate.text
        if (date.isEmpty()) {
            //TODO toast inserire data
            return
        }
        val amount: Float = binding.editTextMovAmount.text.toString().toFloat()
        if (amount <= 0) {
            //TODO toast amount > 0
            return
        }
        val category = binding.spinnerCategory.selectedItem.toString()
        if (category.isEmpty()) {
            //TODO inserire categoria
            return
        }
        val title = binding.editTextTitle.text.toString()
        if (title.isEmpty()) {
            //TODO inserire titolo
            return
        }
        val notes = binding.editTextNotes.text.toString()
        val notify = binding.checkNotify.isChecked
        val movement = Movement(
            date = LocalDate.parse(date),
            amount = if (income) amount else -amount,
            category = category,
            title = title,
            notes = notes,
            notify = notify
        )
        appViewModel.insert(movement)
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}