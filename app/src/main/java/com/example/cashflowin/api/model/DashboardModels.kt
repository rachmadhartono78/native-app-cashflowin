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
    val total_expense_month: Double,
    val total_savings: Double? = 0.0,
    val net_worth: Double? = 0.0
)

data class TransactionItem(
    val id: Int,
    val amount: Double,
    val type: String, // 'income' or 'expense'
    val description: String?,
    val date: String,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    val category: CategoryInfo?,
    val asset: AssetInfo?,
    val goal: Goal?,
    val is_transfer: Boolean? = false,
    val is_adjustment: Boolean? = false,
    val category_id: Int? = null,
    val asset_id: Int? = null,
    val goal_id: Int? = null
)
