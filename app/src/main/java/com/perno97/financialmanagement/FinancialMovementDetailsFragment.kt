package com.perno97.financialmanagement

import android.os.Bundle
import android.transition.Visibility
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
        binding.imgBtnBack.setOnClickListener {
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
        binding.imgBtnBack.visibility = View.VISIBLE
        binding.fabEdit.show()
        binding.fabConfirmEdit.hide()
        binding.fabAbortEdit.hide()
    }

    private fun confirmEdit() {
        disableEditing()
        //TODO
    }

    private fun cancelEdit() {
        disableEditing()
        //TODO
    }

    private fun enableEditing() {
        binding.imgBtnBack.visibility = View.INVISIBLE
        binding.fabEdit.hide()
        binding.fabAbortEdit.show()
        binding.fabConfirmEdit.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}