package com.perno97.financialmanagement.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.database.MovementAndCategory
import com.perno97.financialmanagement.databinding.FragmentFinancialMovementDetailsBinding
import com.perno97.financialmanagement.utils.DecimalDigitsInputFilter
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import java.time.Instant
import java.time.ZoneId
import kotlin.math.absoluteValue

class FinancialMovementDetailsFragment(private val movAndCategory: MovementAndCategory) : Fragment() {

    private var _binding: FragmentFinancialMovementDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    private lateinit var categoryList: List<Category>

    private var editingEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinancialMovementDetailsBinding.inflate(inflater, container, false)
        loadMovementData()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    private fun loadMovementData() {
        val movement = movAndCategory.movement
        val category = movAndCategory.category
        binding.editTextMovAmount.setText(movement.amount.absoluteValue.toString())
        if (movement.amount >= 0) {
            binding.btnIncome.isEnabled = false
            binding.btnOutcome.isEnabled = true
        } else {
            binding.btnIncome.isEnabled = true
            binding.btnOutcome.isEnabled = false
        }

        val spinnerAdapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_row)
        appViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            categoryList = categories
            spinnerAdapter.clear()
            for (c in categories) {
                spinnerAdapter.add(c.name)
            }
            binding.spinnerCategory.isEnabled = false
            binding.spinnerCategory.adapter = spinnerAdapter
        }
        binding.spinnerCategory.setSelection(spinnerAdapter.getPosition(movement.category))

        binding.categoryEditMovColor.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(category.color))
        binding.editTextMovementDate.setText(movement.date.toString())
        binding.btnAddNewCategory.visibility = View.GONE
        binding.editTextTitle.setText(movement.title)
        binding.editTextNotes.setText(movement.notes)
        binding.editTextMovementDate.inputType = InputType.TYPE_NULL
        binding.editTextMovAmount.filters =
            arrayOf(DecimalDigitsInputFilter(binding.editTextMovAmount))
    }

    override fun onPause() {
        super.onPause()
        initListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
        binding.editTextMovementDate.setOnTouchListener { _, event -> //TODO non si capisce che è cliccabile
            if (event.action == MotionEvent.ACTION_DOWN && editingEnabled) {
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
        binding.spinnerCategory.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val name = parent!!.getItemAtPosition(position) // TODO lanciare errore se non trovato
                val category = categoryList.find { cat -> name == cat.name }
                binding.categoryEditMovColor.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(category!!.color)) // TODO lanciare errore se non trovato
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                binding.categoryEditMovColor.backgroundTintList = null
            }

        }
        binding.btnIncome.setOnClickListener {
            if (editingEnabled) {
                binding.btnIncome.isEnabled = false
                binding.btnOutcome.isEnabled = true
            }
        }
        binding.btnOutcome.setOnClickListener {
            if (editingEnabled) {
                binding.btnIncome.isEnabled = true
                binding.btnOutcome.isEnabled = false
            }
        }
        binding.fabEditMovement.setOnClickListener {
            enableEditing()
        }
        binding.fabBtnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.fabConfirmEdit.setOnClickListener {
            confirmEdit()
        }
        binding.fabAbortEdit.setOnClickListener {
            cancelEdit()
        }
    }

    private fun disableEditing() {
        // Changing buttons
        binding.fabBtnBack.show()
        binding.fabEditMovement.show()
        binding.fabConfirmEdit.hide()
        binding.fabAbortEdit.hide()

        // TODO finire?
        // Disable inputs
        editingEnabled = false
    }

    private fun enableEditing() {
        // Changing buttons
        binding.fabBtnBack.hide()
        binding.fabEditMovement.hide()
        binding.fabAbortEdit.show()
        binding.fabConfirmEdit.show()

        // TODO finire?
        // Enable inputs
        editingEnabled = true
        binding.spinnerCategory.isEnabled = true
        binding.editTextMovAmount.isEnabled = true

    }

    private fun confirmEdit() {
        disableEditing()
        // TODO aggiornare movement, cambiare [movAndCategory] e aggiornare UI
    }

    private fun cancelEdit() {
        disableEditing()
        loadMovementData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}