package com.example.cashflowin.api

import com.example.cashflowin.api.model.AssetResponse
import com.example.cashflowin.api.model.DashboardResponse
import okhttp3.ResponseBody
import retrofit2.Response

class DashboardRepository(private val apiService: ApiService) {

    suspend fun getDashboardSummary(): Response<DashboardResponse> {
        return apiService.getDashboardSummary()
    }

    suspend fun getAssets(): Response<AssetResponse> {
        return apiService.getAssets()
    }

    suspend fun logout(): Response<com.google.gson.JsonObject> {
        return apiService.logout()
    }

    suspend fun exportPdf(month: String, year: String): Response<ResponseBody> {
        return apiService.exportPdf(month, year)
    }

    suspend fun exportCsv(month: String, year: String): Response<ResponseBody> {
        return apiService.exportCsv(month, year)
    }
}