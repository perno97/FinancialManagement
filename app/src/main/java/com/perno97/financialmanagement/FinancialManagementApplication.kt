package com.perno97.financialmanagement

import android.app.Application
import com.perno97.financialmanagement.database.AppDatabase
import com.perno97.financialmanagement.database.AppRepository

class FinancialManagementApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { AppRepository(database.applicationDao()) }
}