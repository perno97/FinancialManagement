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
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.database.Movement
import com.perno97.financialmanagement.database.MovementAndCategory
import com.perno97.financialmanagement.database.UnusedCategoriesChecker
import com.perno97.financialmanagement.databinding.FragmentFinancialMovementDetailsBinding
import com.perno97.financialmanagement.utils.DecimalDigitsInputFilter
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.absoluteValue

class FinancialMovementDetailsFragment(private val movAndCategory: MovementAndCategory) :
    Fragment() {
    private val logTag = "FinancialMovementDetailsFragment"
    private var _binding: FragmentFinancialMovementDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    private lateinit var categoryList: List<Category>

    private var editingEnabled = false
    private var income = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinancialMovementDetailsBinding.inflate(inflater, container, false)

        //UnusedCategoriesChecker.check(appViewModel, lifecycleScope)

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
            incomeSelected()
        } else {
            outcomeSelected()
        }


        binding.spinnerCategory.isEnabled = false
        appViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            val spinnerAdapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_row)
            categoryList = categories
            spinnerAdapter.clear()
            for (c in categories) {
                spinnerAdapter.add(c.name)
            }
            binding.spinnerCategory.adapter = spinnerAdapter
            binding.spinnerCategory.setSelection(spinnerAdapter.getPosition(movement.category))
        }

        binding.editTextMovementDate.isEnabled = false
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

    private fun outcomeSelected() {
        income = false
        binding.btnIncome.isEnabled = true
        binding.btnOutcome.isEnabled = false
    }

    private fun incomeSelected() {
        income = true
        binding.btnIncome.isEnabled = false
        binding.btnOutcome.isEnabled = true
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
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
        binding.spinnerCategory.onItemSelectedListener = object : OnItemSelectedListener {
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
                            ColorStateList.valueOf(Color.parseColor(category.color))
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
        binding.btnIncome.setOnClickListener {
            if (editingEnabled) {
                incomeSelected()
            }
        }
        binding.btnOutcome.setOnClickListener {
            if (editingEnabled) {
                outcomeSelected()
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
        binding.btnAddNewCategory.setOnClickListener {
            AddNewCategoryDialog().show(
                childFragmentManager, AddNewCategoryDialog.TAG
            )
        }
    }

    private fun disableEditing() {
        // Changing buttons
        binding.fabBtnBack.show()
        binding.fabEditMovement.show()
        binding.fabConfirmEdit.hide()
        binding.fabAbortEdit.hide()
        binding.btnAddNewCategory.visibility = View.GONE

        // Disable inputs
        editingEnabled = false
        binding.editTextMovementDate.isEnabled = false
        binding.spinnerCategory.isEnabled = false
        binding.editTextMovAmount.isEnabled = false
        binding.editTextTitle.isEnabled = false
        binding.editTextNotes.isEnabled = false
        binding.checkNotify.isEnabled = false
    }

    private fun enableEditing() {
        // Changing buttons
        binding.fabBtnBack.hide()
        binding.fabEditMovement.hide()
        binding.fabAbortEdit.show()
        binding.fabConfirmEdit.show()
        binding.btnAddNewCategory.visibility = View.VISIBLE

        // Enable inputs
        editingEnabled = true
        binding.editTextMovementDate.isEnabled = true
        binding.spinnerCategory.isEnabled = true
        binding.editTextMovAmount.isEnabled = true
        binding.editTextTitle.isEnabled = true
        binding.editTextNotes.isEnabled = true
        binding.checkNotify.isEnabled = true

    }

    private fun confirmEdit() {
        val date = binding.editTextMovementDate.text
        if (date.isEmpty()) {
            Snackbar.make(
                binding.editTextMovementDate,
                R.string.error_no_date,
                BaseTransientBottomBar.LENGTH_LONG
            ).setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.warning
                )
            ).show()
            return
        }
        val amount: Float = binding.editTextMovAmount.text.toString().toFloat()
        if (amount <= 0) {
            Snackbar.make(
                binding.editTextMovementDate,
                R.string.error_null_or_negative_amount,
                BaseTransientBottomBar.LENGTH_LONG
            ).setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.warning
                )
            ).show()
            return
        }
        val category = binding.spinnerCategory.selectedItem.toString()
        if (category.isEmpty()) {
            Snackbar.make(
                binding.editTextMovementDate,
                R.string.error_no_category,
                BaseTransientBottomBar.LENGTH_LONG
            ).setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.warning
                )
            ).show()
            return
        }
        val title = binding.editTextTitle.text.toString()
        if (title.isEmpty()) {
            Snackbar.make(
                binding.editTextMovementDate,
                R.string.error_no_title,
                BaseTransientBottomBar.LENGTH_LONG
            ).setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.warning
                )
            ).show()
            return
        }
        val notes = binding.editTextNotes.text.toString()
        val notify = binding.checkNotify.isChecked

        // TODO copiare da addfinancialmovement
        val previousAmount = movAndCategory.movement.amount
        val newAmount = if (income) amount else -amount

        val movement = Movement(
            movementId = movAndCategory.movement.movementId,
            date = LocalDate.parse(date),
            amount = newAmount,
            category = category,
            title = title,
            notes = notes,
            periodicMovementId = null
        )

        appViewModel.update(movement)
        Snackbar.make(
            binding.fabConfirmEdit,
            R.string.success_edit_movement,
            BaseTransientBottomBar.LENGTH_LONG
        ).setBackgroundTint(
            ContextCompat.getColor(
                requireContext(),
                R.color.success
            )
        ).show()
        updateAssets(previousAmount, newAmount)
        UnusedCategoriesChecker.check(appViewModel, appViewModel.viewModelScope)
        disableEditing()
        parentFragmentManager.popBackStack()
    }

    private fun updateAssets(previousAmount: Float, newAmount: Float) {
        appViewModel.viewModelScope.launch {
            val current = appViewModel.getCurrentAssetDefault()
            val newAssets = current - previousAmount + newAmount
            appViewModel.updateAssets(newAssets)
        }
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