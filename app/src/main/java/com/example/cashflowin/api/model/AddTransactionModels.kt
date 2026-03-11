package com.example.cashflowin.api.model

// Removed duplicate AssetInfo and AssetResponse. 
// They are now centrally managed in AssetModels.kt

data class TransactionRequest(
    val amount: String,
    val type: String, // 'income' or 'expense'
    val category_id: Int,
    val asset_id: Int,
    val description: String?,
    val date: String // format "YYYY-MM-DD"
)