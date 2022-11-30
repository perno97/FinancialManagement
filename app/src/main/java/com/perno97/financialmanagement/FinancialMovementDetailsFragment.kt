package com.perno97.financialmanagement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.perno97.financialmanagement.databinding.FragmentFinancialMovementDetailsBinding

class FinancialMovementDetailsFragment : Fragment() {

    private var _binding: FragmentFinancialMovementDetailsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinancialMovementDetailsBinding.inflate(inflater, container, false)
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
        return binding.root
    }

    private fun disableEditing() {
        binding.fabBtnBack.show()
        binding.fabEditMovement.show()
        binding.fabConfirmEdit.hide()
        binding.fabAbortEdit.hide()
    }

    private fun enableEditing() {
        binding.fabBtnBack.hide()
        binding.fabEditMovement.hide()
        binding.fabAbortEdit.show()
        binding.fabConfirmEdit.show()
    }

    private fun confirmEdit() {
        disableEditing()
        //TODO
    }

    private fun cancelEdit() {
        disableEditing()
        //TODO
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}