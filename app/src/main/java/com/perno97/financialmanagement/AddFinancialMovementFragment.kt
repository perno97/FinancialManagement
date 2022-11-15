package com.perno97.financialmanagement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.perno97.financialmanagement.databinding.FragmentAddFinancialMovementBinding

class AddFinancialMovementFragment : Fragment() {

    private var _binding: FragmentAddFinancialMovementBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFinancialMovementBinding.inflate(inflater, container, false)
        binding.fabConfirmNew.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.fabAbortNew.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}