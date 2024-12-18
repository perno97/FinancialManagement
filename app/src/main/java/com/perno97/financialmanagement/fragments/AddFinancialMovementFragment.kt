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
import com.perno97.financialmanagement.databinding.FragmentAddFinancialMovementBinding
import com.perno97.financialmanagement.utils.DecimalDigitsInputFilter
import com.perno97.financialmanagement.notifications.NotifyManager
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import kotlinx.coroutines.launch
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
    private lateinit var selectedCategory: String

    // Expense is default
    private var income = false
    private var dateOpen: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFinancialMovementBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            appViewModel.uiState.collect {
                selectedCategory = it.selectedCategory
            }
        }

        loadCategories()

        binding.editTextMovementDate.inputType = InputType.TYPE_NULL
        binding.editTextMovementDate.setText(LocalDate.now().toString())
        updateVisibilityOfCheckNotify()

        binding.editTextMovAmount.filters =
            arrayOf(DecimalDigitsInputFilter(binding.editTextMovAmount))

        // Expense is default
        binding.btnExpense.isEnabled = false

        return binding.root
    }

    private fun loadCategories() {
        appViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            val spinnerAdapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_row)
            categoryList = categories
            spinnerAdapter.clear()
            for (c in categories) {
                spinnerAdapter.add(c.name)
            }
            binding.spinnerCategory.adapter = spinnerAdapter
            binding.spinnerCategory.setSelection(spinnerAdapter.getPosition(selectedCategory))
        }
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
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

        binding.editTextMovementDate.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && !dateOpen) {
                dateOpen = true
                val datePicker =
                    MaterialDatePicker.Builder.datePicker()
                        .setTitleText(getString(R.string.select_date))
                        .setSelection(
                            MaterialDatePicker.todayInUtcMilliseconds() // TODO mettere data presente nell'edittext
                        )
                        .build()
                datePicker.addOnPositiveButtonClickListener { value ->
                    val date = Instant.ofEpochMilli(value)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                    binding.editTextMovementDate.setText(date.toString())
                    updateVisibilityOfCheckNotify()
                }
                datePicker.addOnDismissListener {
                    dateOpen = false
                }
                datePicker.show(parentFragmentManager, "rangeDatePickerDialog")
            }
            false
        }
        binding.btnAddNewCategory.setOnClickListener {
            AddNewCategoryDialog(binding.editTextMovAmount).show(
                childFragmentManager, AddNewCategoryDialog.TAG
            )
        }
        binding.fabConfirmNew.setOnClickListener {
            addMovementToDatabase()
        }
        binding.fabAbortNew.setOnClickListener {
            UnusedCategoriesChecker.check(appViewModel, appViewModel.viewModelScope)
            parentFragmentManager.popBackStack()
        }
        binding.btnIncome.setOnClickListener {
            binding.btnIncome.isEnabled = false
            binding.btnExpense.isEnabled = true
            income = true
        }
        binding.btnExpense.setOnClickListener {
            binding.btnIncome.isEnabled = true
            binding.btnExpense.isEnabled = false
            income = false
        }
        binding.checkPeriodic.setOnClickListener {
            updateVisibilityOfCheckNotify()
            if (binding.checkPeriodic.isChecked) {
                binding.layoutPeriodic.visibility = View.VISIBLE
                binding.checkNotify.visibility = View.VISIBLE
            } else
                binding.layoutPeriodic.visibility = View.GONE
        }
    }

    private fun updateVisibilityOfCheckNotify() {
        if (binding.checkPeriodic.isChecked) {
            binding.checkNotify.isEnabled = true
            binding.inputNotifyRow.visibility = View.VISIBLE
        } else {
            val date = LocalDate.parse(binding.editTextMovementDate.text)
            if (date.isAfter(LocalDate.now())) {
                // Can't notify if date is not after today
                binding.checkNotify.isEnabled = true
                binding.inputNotifyRow.visibility = View.VISIBLE
            } else {
                binding.checkNotify.isEnabled = false
                binding.inputNotifyRow.visibility = View.GONE
            }
        }
    }

    private fun addMovementToDatabase() {
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
        val amount: Float = binding.editTextMovAmount.text.toString().toFloatOrNull() ?: 0f
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

        if (binding.spinnerCategory.selectedItem == null || (binding.spinnerCategory.selectedItem != null && binding.spinnerCategory.selectedItem.toString()
                .isEmpty())
        ) {
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
        val category = binding.spinnerCategory.selectedItem.toString()

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

        val notes = binding.editTextNotes.text.toString()
        val notify = binding.checkNotify.isChecked
        val periodic = binding.checkPeriodic.isChecked
        val newAmount = if (income) amount else -amount

        if (periodic && days == 0 && months == 0 && !monday && !tuesday && !wednesday && !thursday && !friday && !saturday && !sunday) {
            Snackbar.make(
                binding.editTextMovementDate,
                R.string.error_no_periodicity,
                BaseTransientBottomBar.LENGTH_LONG
            ).setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.warning
                )
            ).show()
            return
        }

        if (periodic) {
            createPeriodic(
                date,
                newAmount,
                category,
                title,
                notes,
                notify,
                days,
                months,
                monday,
                tuesday,
                wednesday,
                thursday,
                friday,
                saturday,
                sunday
            )
        } else {
            if (date.isAfter(LocalDate.now())) {
                createIncoming(date, newAmount, category, title, notes, notify)
            } else {
                createMovement(date, newAmount, category, title, notes)
            }
        }

        UnusedCategoriesChecker.check(appViewModel, appViewModel.viewModelScope)
        appViewModel.setSelectedCategory("") // Reset selected category
        parentFragmentManager.popBackStack()
    }

    private fun createMovement(
        date: LocalDate,
        newAmount: Float,
        category: String,
        title: String,
        notes: String
    ) {
        val movement = Movement(
            date = date,
            amount = newAmount,
            category = category,
            title = title.trim(),
            notes = notes.trim(),
            periodicMovementId = null
        )

        appViewModel.insert(movement)
        Snackbar.make(
            binding.editTextMovementDate,
            R.string.success_create_movement,
            BaseTransientBottomBar.LENGTH_LONG
        ).setBackgroundTint(
            ContextCompat.getColor(
                requireContext(),
                R.color.success
            )
        ).show()
        updateAssets(newAmount)
    }

    private fun createIncoming(
        date: LocalDate,
        newAmount: Float,
        category: String,
        title: String,
        notes: String,
        notify: Boolean
    ) {
        val incomingMovement = IncomingMovement(
            date = date,
            amount = newAmount,
            category = category,
            title = title.trim(),
            notes = notes.trim(),
            notify = notify,
            periodicMovementId = null
        )

        appViewModel.viewModelScope.launch {
            val movementId = appViewModel.insert(incomingMovement)

            if (notify) {
                NotifyManager.setAlarm(
                    requireContext(),
                    movementId,
                    incomingMovement.title,
                    incomingMovement.category,
                    incomingMovement.amount,
                    incomingMovement.date
                )
            }
            Snackbar.make(
                binding.editTextMovementDate,
                R.string.success_create_incoming,
                BaseTransientBottomBar.LENGTH_LONG
            ).setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.success
                )
            ).show()
        }
    }

    private fun createPeriodic(
        date: LocalDate,
        newAmount: Float,
        category: String,
        title: String,
        notes: String,
        notify: Boolean,
        days: Int,
        months: Int,
        monday: Boolean,
        tuesday: Boolean,
        wednesday: Boolean,
        thursday: Boolean,
        friday: Boolean,
        saturday: Boolean,
        sunday: Boolean
    ) {
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
            title = title.trim(),
            notes = notes.trim(),
            notify = notify
        )

        appViewModel.viewModelScope.launch {
            val periodicMovementId = appViewModel.insert(periodicMovement)
            PeriodicMovementsChecker.check(
                requireContext(),
                appViewModel,
                appViewModel.viewModelScope,
                null, // Generate all movements starting from the periodic movement date
                appViewModel.getPeriodicMovement(periodicMovementId)
            )
            Snackbar.make(
                binding.editTextMovementDate,
                R.string.success_create_periodic,
                BaseTransientBottomBar.LENGTH_LONG
            ).setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.success
                )
            ).show()
        }
    }

    private fun updateAssets(newAmount: Float) {
        appViewModel.viewModelScope.launch {
            appViewModel.updateAssets(appViewModel.getCurrentAssetDefault() + newAmount)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}