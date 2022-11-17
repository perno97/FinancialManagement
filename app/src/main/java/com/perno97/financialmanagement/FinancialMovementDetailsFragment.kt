package com.perno97.financialmanagement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        binding.fabEdit.setOnClickListener {
            enableEditing()
        }
        binding.fabBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        return binding.root
    }

    private fun enableEditing() {
        //TODO da implementare
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}