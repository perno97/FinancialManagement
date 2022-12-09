package com.perno97.financialmanagement.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.perno97.financialmanagement.databinding.FragmentAddCategoryToFilterBinding

class AddCategoryToFilterFragment : Fragment() {

    private var _binding: FragmentAddCategoryToFilterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCategoryToFilterBinding.inflate(inflater, container, false)
        binding.fabConfirmAdd.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.fabAbortAdd.setOnClickListener {
            parentFragmentManager.popBackStack() //TODO reset filters
        }
        return binding.root
    }
}