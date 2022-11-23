package com.perno97.financialmanagement

import android.app.Application
import com.perno97.financialmanagement.database.AppDatabase

class FinancialManagementApplication :Application() {
    val database by lazy { AppDatabase.getInstance(this) }
}