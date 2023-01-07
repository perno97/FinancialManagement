package com.perno97.financialmanagement.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.Category
import com.perno97.financialmanagement.databinding.FragmentCategoriesListBinding
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory

class CategoriesListFragment : Fragment() {
    private var _binding: FragmentCategoriesListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    private lateinit var allCategories: List<Category>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesListBinding.inflate(inflater, container, false)

        appViewModel.allCategories.observe(viewLifecycleOwner) { categoriesList ->
            allCategories = categoriesList
            showCategoriesList()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    private fun initListeners() {
        binding.fabBtnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun showCategoriesList() {
        binding.categoryListContainer.removeAllViews()
        for (category in allCategories) {
            val itemView = layoutInflater.inflate(
                R.layout.category_line_edit_category,
                binding.categoryListContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.categoryFilterColor).backgroundTintList =
                ColorStateList.valueOf(
                    Color.parseColor(category.color)
                )
            itemView.findViewById<TextView>(R.id.txtCategoryFilterName).text = category.name
            itemView.findViewById<ImageButton>(R.id.imgBtnEditCat).setOnClickListener {
                parentFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    replace(
                        R.id.fragment_container_view,
                        CategoryDetailsFragment(category.name)
                    )
                    addToBackStack(null)
                }
            }
            binding.categoryListContainer.addView(itemView)
        }
        if (allCategories.isEmpty()) {
            val view = layoutInflater.inflate(
                R.layout.no_categories_to_select_textview,
                binding.categoryListContainer,
                false
            )
            view.findViewById<TextView>(R.id.txtNoCategories).text =
                getString(R.string.no_categories_to_show)
            binding.categoryListContainer.addView(view)
        }
    }
}