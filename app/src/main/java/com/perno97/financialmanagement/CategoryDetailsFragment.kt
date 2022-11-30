package com.perno97.financialmanagement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.databinding.FragmentCategoryDetailsBinding

private const val LOG_TAG = "CategoryDetailsFragment"

class CategoryDetailsFragment(private val category: Category) : Fragment() {

    private var _binding: FragmentCategoryDetailsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryDetailsBinding.inflate(inflater, container, false)
        binding.fabBtnBack.setOnClickListener{
            parentFragmentManager.popBackStack()
        }
        binding.imgBtnEdit.setOnClickListener {
            EditCategoryDialog().show(
                childFragmentManager, EditCategoryDialog.TAG
            )
        }
        binding.fabAddFilterCat.setOnClickListener {
            parentFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom,
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom
                )
                add<AddCategoryToFilterFragment>(R.id.fragment_container_view)
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