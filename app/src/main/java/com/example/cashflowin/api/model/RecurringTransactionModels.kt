package com.example.cashflowin.api.model

data class RecurringTransactionListResponse(
    val status: String,
    val data: List<RecurringTransaction>
)

data class RecurringTransactionResponse(
    val status: String,
    val data: RecurringTransaction
)

data class RecurringTransaction(
    val id: Int,
    val name: String,
    val description: String?,
    val amount: Double,
    val category_id: Int,
    val category_name: String?,
    val asset_id: Int,
    val asset_name: String?,
    val type: String, // "income" or "expense"
    val frequency: String, // "daily", "weekly", "monthly", "yearly"
    val start_date: String,
    val end_date: String?,
    val next_execution_date: String?,
    val is_active: Boolean,
    val auto_execute: Boolean,
    val created_at: String,
    val updated_at: String
)

data class RecurringTransactionRequest(
    val name: String,
    val description: String? = null,
    val amount: Double,
    val category_id: Int,
    val asset_id: Int,
    val type: String,
    val frequency: String,
    val start_date: String,
    val end_date: String? = null,
    val auto_execute: Boolean = true
)
