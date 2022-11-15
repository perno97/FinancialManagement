package com.perno97.financialmanagement

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.perno97.financialmanagement.databinding.ActivityMainBinding

private const val LOG_TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}