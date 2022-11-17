package com.perno97.financialmanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

class EditCurrentAssetsDialog : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_current_assets_dialog, container, false)
    }

    companion object {
        const val TAG = "EditCurrentAssetsDialog"
    }
}