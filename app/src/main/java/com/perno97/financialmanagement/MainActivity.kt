package com.perno97.financialmanagement

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.perno97.financialmanagement.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dialog: AlertDialog
    private val logTag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Creating builder for edit current assets dialog
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.activity_edit_current_assets_dialog)
        dialog = builder.create()

        binding.txtCurrentValue.setOnClickListener {
            // TODO non si capisce che il testo è cliccabile
            Log.i(logTag, "Clicked edit current assets value")
            dialog.show()
        }

    }
}