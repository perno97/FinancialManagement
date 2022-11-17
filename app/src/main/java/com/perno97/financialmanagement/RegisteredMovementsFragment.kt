package com.perno97.financialmanagement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.perno97.financialmanagement.databinding.FragmentRegisteredMovementsBinding

class RegisteredMovementsFragment : Fragment() {

    private var _binding: FragmentRegisteredMovementsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisteredMovementsBinding.inflate(inflater, container, false)
        binding.imgBtnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.singleRegisteredMov.setOnClickListener {
            parentFragmentManager.commit {
                add<FinancialMovementDetailsFragment>(R.id.fragment_container_view)
                addToBackStack(null)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}