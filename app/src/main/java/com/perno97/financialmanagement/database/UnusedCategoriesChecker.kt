package com.perno97.financialmanagement.database

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.perno97.financialmanagement.viewmodels.AppViewModel

object UnusedCategoriesChecker {
    private const val logTag = "UnusedCategoriesChecker"

    fun check(appViewModel: AppViewModel, viewLifecycleOwner: LifecycleOwner) {
        Log.e(logTag, "Called check")
        appViewModel.categoryWithMovements.observe(viewLifecycleOwner) { list ->
            Log.e(logTag, "Category list of ${list.size} items")
            for (item in list) {
                Log.e(logTag, "Checking category ${item.category.name} with ${item.movements.size} movements")
                if (item.movements.isEmpty()) {
                    Log.e(logTag, "Category ${item.category.name} has no related movement")
                    appViewModel.deleteCategory(item.category)
                }
            }
        }
    }
}