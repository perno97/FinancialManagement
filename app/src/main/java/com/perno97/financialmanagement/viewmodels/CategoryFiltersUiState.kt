package com.perno97.financialmanagement.viewmodels

import com.perno97.financialmanagement.database.Category

data class CategoryFiltersUiState(
    val categoryFilters: List<Category> = listOf()
)