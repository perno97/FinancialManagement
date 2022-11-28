package com.perno97.financialmanagement

import android.app.Application
import com.perno97.financialmanagement.database.AppDatabase
import com.perno97.financialmanagement.database.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class FinancialManagementApplication :Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getInstance(this, applicationScope) }
    val repository by lazy { AppRepository(database.applicationDao())}
}