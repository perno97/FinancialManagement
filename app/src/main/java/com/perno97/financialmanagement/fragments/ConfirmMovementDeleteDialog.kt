package com.perno97.financialmanagement.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.perno97.financialmanagement.FinancialManagementApplication
import com.perno97.financialmanagement.R
import com.perno97.financialmanagement.databinding.FragmentConfirmMovementDeleteDialogBinding
import com.perno97.financialmanagement.notifications.AlarmReceiver
import com.perno97.financialmanagement.viewmodels.AppViewModel
import com.perno97.financialmanagement.viewmodels.AppViewModelFactory
import java.time.ZoneId

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
            appViewModel.deleteIncomingMovement(movementDeletionData.incomingMovementId)
            if (movementDeletionData.notify) {
                val alarmManager =
                    requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
                    action = "ACTION_INCOMING_MOVEMENT_ALARM"
                    putExtra("incomingMovTitle", movementDeletionData.title)
                    putExtra("incomingMovCategory", movementDeletionData.category)
                    putExtra("incomingMovAmount", movementDeletionData.amount)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    requireContext(),
                    movementDeletionData.incomingMovementId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.cancel(pendingIntent)
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
        } else if (movementDeletionData.periodicMovementId != null) {
            appViewModel.deletePeriodicMovement(movementDeletionData.periodicMovementId)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ConfirmMovementDeleteDialog"
    }
}