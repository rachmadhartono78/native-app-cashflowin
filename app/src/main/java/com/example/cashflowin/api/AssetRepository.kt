package com.example.cashflowin.api

import com.example.cashflowin.api.model.AssetRequest
import com.example.cashflowin.api.model.AssetResponse
import com.example.cashflowin.api.model.TransferAssetRequest
import com.google.gson.JsonObject
import retrofit2.Response

class AssetRepository(private val apiService: ApiService) {

    suspend fun getAssets(): Response<AssetResponse> {
        return apiService.getAssets()
    }

    // This is used for both adding and updating (if you follow that pattern)
    // But based on ApiService, they are separate.
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
}
