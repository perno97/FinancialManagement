package com.perno97.financialmanagement.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import com.perno97.financialmanagement.database.Movement
import com.perno97.financialmanagement.databinding.FragmentAddFinancialMovementBinding
import com.perno97.financialmanagement.utils.DecimalDigitsInputFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class AddFinancialMovementFragment : Fragment() {

    private val logTag = "AddFinMovFragment"
    private var _binding: FragmentAddFinancialMovementBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    private lateinit var categoryList: List<Category>

    // Outcome is default
    private var income = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFinancialMovementBinding.inflate(inflater, container, false)

        appViewModel.allCategories.observe(viewLifecycleOwner) {
            val spinnerAdapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_row)
            appViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
                categoryList = categories
                spinnerAdapter.clear()
                for (c in categories) {
                    spinnerAdapter.add(c.name)
                }
            }
            binding.spinnerCategory.adapter = spinnerAdapter
        }

        binding.editTextMovAmount.filters =
            arrayOf(DecimalDigitsInputFilter(binding.editTextMovAmount))

        // Outcome is default
        binding.btnOutcome.isEnabled = false

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
        binding.editTextMovementDate.inputType = InputType.TYPE_NULL
        binding.editTextMovementDate.setText(LocalDate.now().toString())

        binding.spinnerCategory.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (parent != null) {
                    val name =
                        parent.getItemAtPosition(position)
                    val category = categoryList.find { cat -> name == cat.name }
                    if (category != null) {
                        binding.categoryEditMovColor.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor(category!!.color))
                    } else {
                        Log.e(
                            logTag,
                            "Error selected spinner category not found in category list, is null"
                        )
                    }
                } else {
                    Log.e(logTag, "Error category spinner selection has null parent")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                binding.categoryEditMovColor.backgroundTintList = null
            }

        }

        binding.editTextMovementDate.setOnTouchListener { _, event -> //TODO non si capisce che è cliccabile
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
        binding.btnOutcome.setOnClickListener {
            binding.btnIncome.isEnabled = true
            binding.btnOutcome.isEnabled = false
            income = false
        }
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