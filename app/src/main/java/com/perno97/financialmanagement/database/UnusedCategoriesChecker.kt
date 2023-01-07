package com.perno97.financialmanagement.database

import android.util.Log
import com.perno97.financialmanagement.viewmodels.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object UnusedCategoriesChecker {
    private const val logTag = "UnusedCategoriesChecker"

    fun check(appViewModel: AppViewModel, scope: CoroutineScope) {
        Log.i(logTag, "Called check")
        scope.launch {
            Log.i(logTag, "Started checking")
            val list = appViewModel.getCategoryWithMovements()
            Log.i(logTag, "Category list of ${list.size} items")
            for (item in list) {
                Log.i(
                    logTag,
                    "Checking category ${item.category.name} with ${item.movements.size} movements"
                )
                if (item.movements.isEmpty()) {
                    if (appViewModel.getPeriodicMovements(item.category.name)
                            .isEmpty() && appViewModel.getIncomingMovements(item.category.name)
                            .isEmpty()
                    ) {
                        Log.i(logTag, "Category ${item.category.name} has no related movement")
                        appViewModel.deleteCategory(item.category)
                    }
                }
            }
            Log.i(logTag, "Finished checking")
        }
    }
}