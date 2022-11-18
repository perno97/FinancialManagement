package com.perno97.financialmanagement

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.perno97.financialmanagement.databinding.FragmentMainBinding
import org.w3c.dom.Text

private const val LOG_TAG = "MainFragment"

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var editAssetsDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.fabAddMovement.setOnClickListener {
            Log.i(LOG_TAG, "Clicked add financial movement")
            parentFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom,
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom
                )
                add<AddFinancialMovementFragment>(R.id.fragment_container_view)
                addToBackStack(null)
            }
        }
        binding.txtCurrentValue.setOnClickListener {
            // TODO non si capisce che il testo è cliccabile
            Log.i(LOG_TAG, "Clicked edit current assets value")
            EditCurrentAssetsDialog().show(
                childFragmentManager, EditCurrentAssetsDialog.TAG
            )
        }
        binding.fabRegisteredMovements.setOnClickListener {
            Log.i(LOG_TAG, "Clicked registered movements")
            parentFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_top,
                    R.anim.slide_in_top,
                    R.anim.slide_out_bottom
                )
                replace(R.id.fragment_container_view, RegisteredMovementsFragment())
                addToBackStack(null)
            }
        }

        for (i in 1..10) {
            val catProg = inflater.inflate(R.layout.category_progress, null)
            binding.categoryList.addView(catProg)
            catProg.findViewById<TextView>(R.id.categoryColorLabel).setBackgroundColor(Color.BLUE)
            catProg.findViewById<TextView>(R.id.txtCategoryName).text = "Food"
            val progressBar = catProg.findViewById<ProgressBar>(R.id.progressBarCategoryBudget)
            progressBar.max = 200
            progressBar.progress = 160
            catProg.findViewById<TextView>(R.id.txtMaxCategoryBudget).text = "200€"
            catProg.findViewById<TextView>(R.id.txtCurrentCategoryProgress).text = "160€"
            catProg.setOnClickListener {
                parentFragmentManager.commit {
                    replace(R.id.fragment_container_view, CategoryDetailsFragment(catProg))
                    addToBackStack(null)
                }
            }
        }
        return binding.root
    }
}