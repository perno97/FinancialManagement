package com.perno97.financialmanagement.database

import android.util.Log
import com.perno97.financialmanagement.viewmodels.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object UnusedCategoriesChecker {
    private const val logTag = "UnusedCategoriesChecker"

    fun check(appViewModel: AppViewModel, scope: CoroutineScope) {
        Log.e(logTag, "Called check")
        scope.launch {
            Log.e(logTag, "Started checking")
            val list = appViewModel.getCategoryWithMovements()
            Log.e(logTag, "Category list of ${list.size} items")
            for (item in list) {
                Log.e(
                    logTag,
                    "Checking category ${item.category.name} with ${item.movements.size} movements"
                )
                if (item.movements.isEmpty()) {
                    Log.e(logTag, "Category ${item.category.name} has no related movement")
                    appViewModel.deleteCategory(item.category)
                }
            }
            Log.e(logTag, "Finished checking")
        }
    }
}