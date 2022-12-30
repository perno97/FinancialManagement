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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.*
import com.perno97.financialmanagement.databinding.FragmentFinancialMovementDetailsBinding
import com.perno97.financialmanagement.utils.DecimalDigitsInputFilter
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.absoluteValue

class FinancialMovementDetailsFragment(private val movementDetailsData: MovementDetailsData) :
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
    private lateinit var selectedCategory: String

    private var editingEnabled = false

    // Outcome is default
    private var income = false
    private var dateOpen: Boolean = false
    private var periodic = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinancialMovementDetailsBinding.inflate(inflater, container, false)

        //UnusedCategoriesChecker.check(appViewModel, lifecycleScope)

        viewLifecycleOwner.lifecycleScope.launch {
            appViewModel.uiState.collect {
                selectedCategory = it.selectedCategory
            }
        }

        loadMovementData()

        binding.editTextMovAmount.filters =
            arrayOf(DecimalDigitsInputFilter(binding.editTextMovAmount))

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    private fun loadMovementData() {
        val amount = movementDetailsData.amount
        binding.editTextMovAmount.setText(amount.absoluteValue.toString())
        if (amount >= 0) {
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
            val selected = selectedCategory.ifEmpty { movementDetailsData.category }
            binding.spinnerCategory.setSelection(spinnerAdapter.getPosition(selected))
        }

        binding.editTextMovementDate.isEnabled = false
        binding.categoryEditMovColor.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(movementDetailsData.color))
        binding.editTextMovementDate.setText(movementDetailsData.date.toString())
        binding.btnAddNewCategory.visibility = View.GONE
        binding.editTextTitle.setText(movementDetailsData.title)
        binding.editTextNotes.setText(movementDetailsData.notes)
        binding.editTextMovementDate.inputType = InputType.TYPE_NULL
        binding.editTextMovAmount.filters =
            arrayOf(DecimalDigitsInputFilter(binding.editTextMovAmount))
        if (movementDetailsData.periodicMovementId != null) {
            binding.checkPeriodic.isChecked = true
            periodic = true
            binding.layoutPeriodic.visibility = View.VISIBLE
            for (day in movementDetailsData.weekDays!!) {
                when (day) {
                    DayOfWeek.MONDAY -> binding.checkMonday.isChecked = true
                    DayOfWeek.TUESDAY -> binding.checkTuesday.isChecked = true
                    DayOfWeek.WEDNESDAY -> binding.checkWednesday.isChecked = true
                    DayOfWeek.THURSDAY -> binding.checkThursday.isChecked = true
                    DayOfWeek.FRIDAY -> binding.checkFriday.isChecked = true
                    DayOfWeek.SATURDAY -> binding.checkSaturday.isChecked = true
                    DayOfWeek.SUNDAY -> binding.checkSunday.isChecked = true
                }
            }
            binding.editTextDaysRepeat.setText(movementDetailsData.days)
            binding.editTextMonthsRepeat.setText(movementDetailsData.months)
        } else {
            binding.checkPeriodic.isChecked = false
            binding.checkPeriodic.isEnabled = true
            periodic = false
            binding.layoutPeriodic.visibility = View.GONE
        }
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
            dateOpen = true
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
                datePicker.addOnDismissListener {
                    dateOpen = false
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
        binding.editTextMonthsRepeat.isEnabled = false
        binding.editTextDaysRepeat.isEnabled = false
        binding.spinnerCategory.isEnabled = false
        binding.editTextMovAmount.isEnabled = false
        binding.editTextTitle.isEnabled = false
        binding.editTextNotes.isEnabled = false
        binding.checkNotify.isEnabled = false

        binding.checkMonday.isEnabled = false
        binding.checkTuesday.isEnabled = false
        binding.checkWednesday.isEnabled = false
        binding.checkThursday.isEnabled = false
        binding.checkFriday.isEnabled = false
        binding.checkSaturday.isEnabled = false
        binding.checkSunday.isEnabled = false

        if (periodic)
            binding.checkPeriodic.isEnabled = false
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
        binding.editTextMonthsRepeat.isEnabled = true
        binding.editTextDaysRepeat.isEnabled = true
        binding.spinnerCategory.isEnabled = true
        binding.editTextMovAmount.isEnabled = true
        binding.editTextTitle.isEnabled = true
        binding.editTextNotes.isEnabled = true
        binding.checkNotify.isEnabled = true

        binding.checkMonday.isEnabled = true
        binding.checkTuesday.isEnabled = true
        binding.checkWednesday.isEnabled = true
        binding.checkThursday.isEnabled = true
        binding.checkFriday.isEnabled = true
        binding.checkSaturday.isEnabled = true
        binding.checkSunday.isEnabled = true

        if (periodic)
            binding.checkPeriodic.isEnabled = true

    }

    private fun confirmEdit() {
        disableEditing()
        val d = binding.editTextMovementDate.text
        if (d.isEmpty()) {
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
        val date = LocalDate.parse(d)
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
        val previousAmount = movementDetailsData.amount
        val newAmount = if (income) amount else -amount

        if (periodic && binding.checkPeriodic.isChecked) {
            var days = binding.editTextDaysRepeat.text.toString().toIntOrNull() ?: 0
            val months = binding.editTextMonthsRepeat.text.toString().toIntOrNull() ?: 0
            var monday = binding.checkMonday.isChecked
            var tuesday = binding.checkTuesday.isChecked
            var wednesday = binding.checkWednesday.isChecked
            var thursday = binding.checkThursday.isChecked
            var friday = binding.checkFriday.isChecked
            var saturday = binding.checkSaturday.isChecked
            var sunday = binding.checkSunday.isChecked
            if (monday && tuesday && wednesday && thursday && friday && saturday && sunday) {
                days = 1
                monday = false
                tuesday = false
                wednesday = false
                thursday = false
                friday = false
                saturday = false
                sunday = false
            }
            val periodicMovement = PeriodicMovement(
                days = days,
                months = months,
                monday = monday,
                tuesday = tuesday,
                wednesday = wednesday,
                thursday = thursday,
                friday = friday,
                saturday = saturday,
                sunday = sunday,
                date = date,
                amount = newAmount,
                category = category,
                title = title,
                notes = notes,
                notify = notify
            )
            appViewModel.update(periodicMovement)
            Snackbar.make(
                binding.editTextMovementDate,
                R.string.success_update_periodic,
                BaseTransientBottomBar.LENGTH_LONG
            ).setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.success
                )
            ).show()
        } else {
            if (date.isAfter(LocalDate.now())) {
                val incumbentMovement = IncumbentMovement(
                    date = date,
                    amount = amount,
                    category = category,
                    title = title,
                    notes = notes,
                    notify = notify
                )
                if (movementDetailsData.movementId != null) { // If it was a movement and now it's an incumbent movement
                    appViewModel.insert(incumbentMovement)
                    appViewModel.deleteMovement(movementDetailsData.movementId)
                    Snackbar.make(
                        binding.editTextMovementDate,
                        R.string.success_movement_to_incumbent,
                        BaseTransientBottomBar.LENGTH_LONG
                    ).setBackgroundTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.success
                        )
                    ).show()
                } else { // If it was an incumbent movement
                    appViewModel.update(incumbentMovement)
                    Snackbar.make(
                        binding.editTextMovementDate,
                        R.string.success_movement_update,
                        BaseTransientBottomBar.LENGTH_LONG
                    ).setBackgroundTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.success
                        )
                    ).show()
                }
            } else {
                val movement = Movement(
                    date = date,
                    amount = newAmount,
                    category = category,
                    title = title,
                    notes = notes,
                    periodicMovementId = null
                )

                if (movementDetailsData.incumbentMovementId != null) { // If it was an incumbent movement and now it's a movement
                    appViewModel.insert(movement)
                    appViewModel.deleteMovement(movementDetailsData.incumbentMovementId)
                    Snackbar.make(
                        binding.editTextMovementDate,
                        R.string.success_incumbent_to_movement,
                        BaseTransientBottomBar.LENGTH_LONG
                    ).setBackgroundTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.success
                        )
                    ).show()
                } else { // If it was a movement
                    appViewModel.update(movement)
                    Snackbar.make(
                        binding.editTextMovementDate,
                        R.string.success_movement_update,
                        BaseTransientBottomBar.LENGTH_LONG
                    ).setBackgroundTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.success
                        )
                    ).show()
                }
                updateAssets(previousAmount, newAmount)
            }
        }
        PeriodicMovementsChecker.check(appViewModel, appViewModel.viewModelScope, null)
        UnusedCategoriesChecker.check(appViewModel, appViewModel.viewModelScope)
        appViewModel.setSelectedCategory("") // Reset selected category
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