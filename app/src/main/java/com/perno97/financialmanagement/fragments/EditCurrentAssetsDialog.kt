package com.perno97.financialmanagement.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.databinding.FragmentEditCurrentAssetsDialogBinding
import com.perno97.financialmanagement.utils.DecimalDigitsInputFilter
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory

class EditCurrentAssetsDialog : DialogFragment() {

    private var _binding: FragmentEditCurrentAssetsDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCurrentAssetsDialogBinding.inflate(inflater, container, false)

        appViewModel.getDefaultProfile().observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                binding.editTextCurrentAssets.setText(String.format("%.2f", profile.assets))
            } else {
                binding.editTextCurrentAssets.setText(String.format("%.2f", 0f))
            }
        }

        if (dialog != null)
            dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    private fun confirmAction() {
        appViewModel.updateAssets(
            binding.editTextCurrentAssets.text.toString().toFloat()
        )
        dismiss()
    }

    private fun cancelAction() {
        dismiss()
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    private fun initListeners() {
        binding.btnConfirmEditAssets.setOnClickListener {
            confirmAction()
        }
        binding.btnCancelEditAssets.setOnClickListener {
            cancelAction()
        }
        binding.editTextCurrentAssets.filters =
            arrayOf(DecimalDigitsInputFilter(binding.editTextCurrentAssets))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditCurrentAssetsDialog"
    }
}