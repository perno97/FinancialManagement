package com.perno97.financialmanagement

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.util.Pair
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.datepicker.MaterialDatePicker
import com.perno97.financialmanagement.database.AppViewModel
import com.perno97.financialmanagement.database.AppViewModelFactory
import com.perno97.financialmanagement.database.Movement
import com.perno97.financialmanagement.databinding.FragmentAddFinancialMovementBinding
import com.perno97.financialmanagement.utils.CategorySpinnerAdapter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val LOG_TAG = "AddFinMovFragment"

class AddFinancialMovementFragment : Fragment() {

    private var _binding: FragmentAddFinancialMovementBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }
    private var income = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFinancialMovementBinding.inflate(inflater, container, false)
        binding.editTextMovementDate.inputType = InputType.TYPE_NULL
        binding.editTextMovementDate.setText(LocalDate.now().toString())

        binding.editTextMovementDate.setOnTouchListener(object :View.OnTouchListener {
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_DOWN){
                    val datePicker =
                        MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select period")
                            .setSelection(
                                MaterialDatePicker.todayInUtcMilliseconds()
                            )
                            .build()
                    datePicker.addOnPositiveButtonClickListener {
                            value ->
                        val date = Instant.ofEpochMilli(value)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        binding.editTextMovementDate.setText(date.toString())
                    }
                    datePicker.show(parentFragmentManager, DatePickerFragment.TAG)
                }
                return false
            }
        })
        val spinnerAdapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_row)
        appViewModel.allCategories.observe(viewLifecycleOwner, Observer {
            categories ->
            spinnerAdapter.clear()
            for (c in categories) {
                spinnerAdapter.add(c.name)
            }
        })
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
        binding.btnIncome.isEnabled = false
        binding.btnIncome.setOnClickListener {
            binding.btnIncome.isEnabled = false
            binding.btnOutcome.isEnabled = true
            income = true
        }
        binding.btnOutcome.setOnClickListener {
            binding.btnIncome.isEnabled = true
            binding.btnOutcome.isEnabled = false
            income = false
        }
        return binding.root
    }

    private fun addMovementToDatabase() {
        val date = binding.editTextMovementDate.text
        if(date.isEmpty()){
            //TODO toast inserire data
            return
        }
        val amount :Float = binding.editTextMovAmount.text.toString().toFloat()
        if(amount <= 0){
            //TODO toast amount > 0
            return
        }
        val category = binding.spinnerCategory.selectedItem.toString()
        if(category.isEmpty()){
            //TODO inserire categoria
            return
        }
        val title = binding.editTextTitle.text.toString()
        if(title.isEmpty()){
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