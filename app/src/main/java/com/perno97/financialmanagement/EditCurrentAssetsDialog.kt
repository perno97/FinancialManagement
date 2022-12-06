package com.perno97.financialmanagement

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.perno97.financialmanagement.database.AppViewModel
import com.perno97.financialmanagement.database.AppViewModelFactory
import com.perno97.financialmanagement.databinding.FragmentEditCurrentAssetsDialogBinding
import com.perno97.financialmanagement.utils.DecimalDigitsInputFilter

class EditCurrentAssetsDialog : DialogFragment() {

    private var _binding: FragmentEditCurrentAssetsDialogBinding? = null

    /**
     * Connection to persistent data
     */
    private val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCurrentAssetsDialogBinding.inflate(inflater, container, false)
        binding.btnConfirmEditAssets.setOnClickListener {
            confirmAction()
        }
        binding.btnCancelEditAssets.setOnClickListener {
            cancelAction()
        }
        binding.editTextCurrentAssets.filters = arrayOf(DecimalDigitsInputFilter(10,2))
        appViewModel.getDefaultProfile().observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                binding.editTextCurrentAssets.setText(profile.assets.toString())
            } else {
                binding.editTextCurrentAssets.setText("0")
            }
        }
        return binding.root
    }

    private fun confirmAction() {
        appViewModel.insertDefaultProfile(
                binding.editTextCurrentAssets.text.toString().toFloat()
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
        const val TAG = "EditCurrentAssetsDialog"
    }
}