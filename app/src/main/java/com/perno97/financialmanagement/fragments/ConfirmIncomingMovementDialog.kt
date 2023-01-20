package com.perno97.financialmanagement.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.database.IncomingMovement
import com.perno97.financialmanagement.database.Movement
import com.perno97.financialmanagement.databinding.FragmentConfirmIncomingMovementDialogBinding
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import kotlinx.coroutines.launch

class ConfirmIncomingMovementDialog(
    private val incomingMovement: IncomingMovement,
    private val viewForSnack: View
) :
    DialogFragment() {

    private var _binding: FragmentConfirmIncomingMovementDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val appViewModel: AppViewModel by activityViewModels {
        AppViewModelFactory((activity?.application as FinancialManagementApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmIncomingMovementDialogBinding.inflate(inflater, container, false)
        if (dialog != null)
            dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.txtConfirmationMessage.text = getString(
            R.string.confirm_incoming_confirmation_message,
            incomingMovement.date.toString(),
            incomingMovement.title
        )
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    private fun initListeners() {
        binding.btnConfirmIncoming.setOnClickListener {
            confirmIncoming()
        }
        binding.btnAbortIncoming.setOnClickListener {
            dismiss()
        }
    }

    private fun confirmIncoming() {
        appViewModel.insert(
            Movement(
                date = incomingMovement.date,
                amount = incomingMovement.amount,
                title = incomingMovement.title,
                notes = incomingMovement.notes,
                category = incomingMovement.category,
                periodicMovementId = incomingMovement.periodicMovementId
            )
        )
        appViewModel.deleteIncomingMovement(incomingMovementId = incomingMovement.incomingMovementId)
        appViewModel.viewModelScope.launch {
            appViewModel.updateAssets(appViewModel.getCurrentAssetDefault() + incomingMovement.amount)
        }
        Snackbar.make(
            viewForSnack,
            R.string.success_confirmed_incoming,
            BaseTransientBottomBar.LENGTH_LONG
        ).setBackgroundTint(
            ContextCompat.getColor(
                requireContext(),
                R.color.success
            )
        ).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ConfirmIncomingMovementDialog"
    }
}