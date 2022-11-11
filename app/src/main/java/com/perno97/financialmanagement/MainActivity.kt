package com.perno97.financialmanagement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.perno97.financialmanagement.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var editAssetsDialog: AlertDialog
    private lateinit var addFinMovDialog: AlertDialog
    private val logTag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Creating builder for edit current assets dialog
        val editAssetsDialogBuilder = AlertDialog.Builder(this)
        val addFinMovDialogBuilder = AlertDialog.Builder(this)
        editAssetsDialogBuilder.setView(R.layout.activity_edit_current_assets_dialog)
        addFinMovDialogBuilder.setView(R.layout.activity_add_financial_movement)
        editAssetsDialog = editAssetsDialogBuilder.create()
        addFinMovDialog = addFinMovDialogBuilder.create()

        binding.txtCurrentValue.setOnClickListener {
            // TODO non si capisce che il testo è cliccabile
            Log.i(logTag, "Clicked edit current assets value")
            editAssetsDialog.show()
        }
        binding.fabAddMovement.setOnClickListener {
            Log.i(logTag, "Clicked add financial movement")
            startActivity(Intent(this, AddFinancialMovementActivity::class.java))
        }

    }
}