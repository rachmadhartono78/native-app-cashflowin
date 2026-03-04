package com.example.cashflowin.api.model

data class DashboardResponse(
    val status: String,
    val data: DashboardData?
)

data class DashboardData(
    val summary: Summary,
    val recent_transactions: List<TransactionItem>
)

data class Summary(
    val balance: Double,
    val total_income_month: Double,
    val total_expense_month: Double
)

data class TransactionItem(
    val id: Int,
    val amount: String,
    val type: String, // 'income' or 'expense'
    val description: String?,
    val date: String,
    val category: CategoryInfo?,
    val asset: AssetInfo?
)

data class CategoryInfo(
    val id: Int,
    val name: String,
    val color: String,
    val icon: String
)

data class AssetInfo(
    val id: Int,
    val name: String,
    val type: String? = null,
    val amount: String? = null
)
