package com.perno97.financialmanagement

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.perno97.financialmanagement.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.txtCurrentValue.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setView(R.layout.activity_edit_current_assets_dialog)
            val dialog = builder.create()
        }

    }
}