package com.example.cashflowin.api.model

data class BudgetResponse(
    val status: String,
    val data: BudgetData
)

data class BudgetData(
    val budgets: List<Budget>,
    val total_budget: Double,
    val total_spent: Double,
    val month: Int,
    val year: Int
)

data class Budget(
    val id: Int,
    val user_id: Int,
    val category_id: Int,
    val amount: Double,
    val month: Int,
    val year: Int,
    val spent: Double?,
    val percentage: Double?,
    val category: CategoryInfo?
)

data class BudgetRequest(
    val category_id: Int,
    val amount: Double,
    val month: Int?,
    val year: Int?
)

data class CloneBudgetRequest(
    val month: Int,
    val year: Int
)