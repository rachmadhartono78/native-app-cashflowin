package com.example.cashflowin.api.model

// Removed duplicate AssetInfo and AssetResponse. 
// They are now centrally managed in AssetModels.kt

data class TransactionRequest(
    val amount: String,
    val type: String, // 'income' or 'expense'
    val category_id: Int? = null,
    val asset_id: Int? = null,
    val description: String?,
    val date: String, // format "YYYY-MM-DD"
    val is_transfer: Boolean? = false,
    val source_asset_id: Int? = null,
    val destination_asset_id: Int? = null,
    val debt_id: Int? = null,
    val goal_id: Int? = null,
    val is_adjustment: Boolean? = false
)
