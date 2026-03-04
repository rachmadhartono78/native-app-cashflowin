package com.example.cashflowin.api.model

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    val status: String,
    val data: DashboardData?,
    val message: String? = null
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
    @SerializedName("created_at")
    val createdAt: String? = null,
    val category: CategoryInfo?,
    val asset: AssetInfo?,
    val category_id: Int? = null,
    val asset_id: Int? = null
)
