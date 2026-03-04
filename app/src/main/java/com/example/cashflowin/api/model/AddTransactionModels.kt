package com.example.cashflowin.api.model

data class CategoryResponse(
    val status: String,
    val data: List<CategoryInfo>
)

data class AssetResponse(
    val status: String,
    val data: List<AssetInfo>
)

data class TransactionRequest(
    val amount: String,
    val type: String, // 'income' or 'expense'
    val category_id: Int,
    val asset_id: Int,
    val description: String?,
    val date: String // format "YYYY-MM-DD"
)
