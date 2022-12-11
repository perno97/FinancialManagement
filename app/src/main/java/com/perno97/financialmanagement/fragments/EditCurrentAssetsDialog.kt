package com.perno97.financialmanagement.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import com.perno97.financialmanagement.databinding.FragmentEditCurrentAssetsDialogBinding
import com.perno97.financialmanagement.utils.DecimalDigitsInputFilter

class EditCurrentAssetsDialog : DialogFragment() {

    private var _binding: FragmentEditCurrentAssetsDialogBinding? = null

    /**
     * Connection to data
     */
    private val appViewModel: AppViewModel by activityViewModels {
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
        binding.editTextCurrentAssets.filters =
            arrayOf(DecimalDigitsInputFilter(binding.editTextCurrentAssets))
        appViewModel.getDefaultProfile().observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                binding.editTextCurrentAssets.setText(String.format("%.2f", profile.assets))
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