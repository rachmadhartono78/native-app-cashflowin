package com.example.cashflowin.api.model

data class AssetRequest(
    val name: String,
    val type: String,
    val amount: String
)

data class TransferAssetRequest(
    val source_asset_id: Int,
    val destination_asset_id: Int,
    val amount: String,
    val date: String,
    val description: String?
)
