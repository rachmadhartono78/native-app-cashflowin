package com.example.cashflowin.api

import com.example.cashflowin.api.model.AssetRequest
import com.example.cashflowin.api.model.AssetResponse
import com.example.cashflowin.api.model.TransactionListResponse
import com.example.cashflowin.api.model.TransferAssetRequest
import com.google.gson.JsonObject
import retrofit2.Response

class AssetRepository(private val apiService: ApiService) {

    suspend fun getAssets(): Response<AssetResponse> {
        return apiService.getAssets()
    }

    suspend fun addAsset(request: AssetRequest): Response<JsonObject> {
        return apiService.addAsset(request)
    }

    suspend fun updateAsset(id: Int, request: AssetRequest): Response<JsonObject> {
        return apiService.updateAsset(id, request)
    }

    suspend fun deleteAsset(id: Int): Response<JsonObject> {
        return apiService.deleteAsset(id)
    }

    suspend fun transferAsset(request: TransferAssetRequest): Response<JsonObject> {
        return apiService.transferAsset(request)
    }

    suspend fun getTransactions(
        page: Int? = null,
        type: String? = null,
        categoryId: Int? = null,
        assetId: Int? = null,
        search: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Response<TransactionListResponse> {
        return apiService.getTransactions(
            page = page,
            type = type,
            categoryId = categoryId,
            assetId = assetId,
            search = search,
            startDate = startDate,
            endDate = endDate
        )
    }
}
