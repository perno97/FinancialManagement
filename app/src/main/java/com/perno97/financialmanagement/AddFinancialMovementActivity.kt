package com.perno97.financialmanagement

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AddFinancialMovementActivity : AppCompatActivity() {

    private val logTag = "AddFinMovActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
            Not using binding because the root layout height is "wrap_content"
            therefore with binding it won't render properly
         */
        setContentView(R.layout.activity_add_financial_movement)

        findViewById<FloatingActionButton>(R.id.fabConfirmNew).setOnClickListener {
            Log.i(logTag, "Clicked on confirm")
            finish()
        }
        findViewById<FloatingActionButton>(R.id.fabAbortNew).setOnClickListener {
            Log.i(logTag, "Clicked on abort")
            finish()
        }
    }
}