package com.example.cashflowin.api.model

import com.google.gson.annotations.SerializedName

data class AssetResponse(
    val status: String? = null,
    val data: List<AssetInfo>,
    val message: String? = null
)

data class AssetInfo(
    val id: Int,
    val name: String,
    val type: String,
    @SerializedName("amount")
    val balance: Double,
    val color: String? = null,
    val icon: String? = null
)

data class AssetRequest(
    val name: String,
    val type: String,
    val amount: String,
    val color: String? = null,
    val icon: String? = null
)

data class TransferAssetRequest(
    val source_asset_id: Int,
    val destination_asset_id: Int,
    val amount: String,
    val date: String,
    val description: String?
)
