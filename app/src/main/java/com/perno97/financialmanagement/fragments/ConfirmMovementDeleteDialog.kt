package com.perno97.financialmanagement.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
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
import com.perno97.financialmanagement.databinding.FragmentConfirmMovementDeleteDialogBinding
import com.perno97.financialmanagement.notifications.NotifyManager
import com.perno97.financialmanagement.utils.MovementDeletionData
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import kotlinx.coroutines.launch

class ConfirmMovementDeleteDialog(private val movementDeletionData: MovementDeletionData) :
    DialogFragment() {

    private val logTag = "ConfirmMovementDeleteDialog"
    private var _binding: FragmentConfirmMovementDeleteDialogBinding? = null

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
        _binding = FragmentConfirmMovementDeleteDialogBinding.inflate(inflater, container, false)
        if (dialog != null)
            dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val periodic =
            if (movementDeletionData.periodicMovementId != null && movementDeletionData.movementId == null && movementDeletionData.incomingMovementId == null)
                getString(
                    R.string.periodic
                )
            else ""

        binding.txtConfirmationMessage.text = getString(
            R.string.delete_movement_confirmation_message,
            periodic,
            movementDeletionData.date.toString(),
            movementDeletionData.title
        )
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initListeners()
    }

    private fun initListeners() {
        binding.btnConfirmDeletion.setOnClickListener {
            confirmDeletion()
        }
        binding.btnAbortDeletion.setOnClickListener {
            dismiss()
        }
    }

    private fun confirmDeletion() {
        if (movementDeletionData.movementId != null) {
            appViewModel.deleteMovement(movementDeletionData.movementId)
            appViewModel.viewModelScope.launch {
                appViewModel.updateAssets(appViewModel.getCurrentAssetDefault() - movementDeletionData.amount)
            }
            Snackbar.make(
                binding.btnConfirmDeletion,
                R.string.success_delete_movement,
                BaseTransientBottomBar.LENGTH_LONG
            ).setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.success
                )
            ).show()
        } else if (movementDeletionData.incomingMovementId != null) {
            deleteIncomingMovement(
                movementDeletionData.incomingMovementId,
                movementDeletionData.notify
            )
            Snackbar.make(
                binding.btnConfirmDeletion,
                R.string.success_delete_movement,
                BaseTransientBottomBar.LENGTH_LONG
            ).setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.success
                )
            ).show()
        } else if (movementDeletionData.periodicMovementId != null) {
            appViewModel.deletePeriodicMovement(movementDeletionData.periodicMovementId)
            appViewModel.viewModelScope.launch {
                val incomingMovements =
                    appViewModel.getAllIncomingFromPeriodic(movementDeletionData.periodicMovementId)
                for (mov in incomingMovements) {
                    deleteIncomingMovement(mov.incomingMovementId, mov.notify)
                }
            }
            Snackbar.make(
                binding.btnConfirmDeletion,
                R.string.success_delete_periodic,
                BaseTransientBottomBar.LENGTH_LONG
            ).setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.success
                )
            ).show()
        } else {
            Log.e(logTag, "Error deleting movement $movementDeletionData")
        }
        dismiss()
    }

    private fun deleteIncomingMovement(incomingMovementId: Long, notify: Boolean) {
        appViewModel.deleteIncomingMovement(incomingMovementId)
        if (notify) {
            NotifyManager.removeAlarm(
                requireContext(),
                incomingMovementId,
                movementDeletionData.title,
                movementDeletionData.category,
                movementDeletionData.amount
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        parentFragmentManager.popBackStack()
    }

    companion object {
        const val TAG = "ConfirmMovementDeleteDialog"
    }
}