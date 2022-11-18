package com.perno97.financialmanagement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.perno97.financialmanagement.databinding.FragmentCategoryDetailsBinding

class CategoryDetailsFragment(private val catProg: View) : Fragment() {

    private var _binding: FragmentCategoryDetailsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryDetailsBinding.inflate(inflater, container, false)
        binding.imgBtnBack.setOnClickListener{
            parentFragmentManager.popBackStack()
        }
        binding.catProgContainer.addView(catProg)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}