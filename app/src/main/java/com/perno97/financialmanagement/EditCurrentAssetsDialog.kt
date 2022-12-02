package com.perno97.financialmanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.perno97.financialmanagement.databinding.FragmentEditCurrentAssetsDialogBinding

class EditCurrentAssetsDialog : DialogFragment() {

    private var _binding: FragmentEditCurrentAssetsDialogBinding? = null

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
        return binding.root
    }

    private fun confirmAction() {
        dismiss()
        //TODO
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