package com.example.cashflowin.api

import com.example.cashflowin.api.model.AssetResponse
import com.example.cashflowin.api.model.CategoryResponse
import com.example.cashflowin.api.model.TransactionListResponse
import com.example.cashflowin.api.model.TransactionRequest
import com.google.gson.JsonObject
import retrofit2.Response

class TransactionRepository(private val apiService: ApiService) {

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

    suspend fun getCategories(type: String? = null): Response<CategoryResponse> {
        return apiService.getCategories(type)
    }

    suspend fun getAssets(): Response<AssetResponse> {
        return apiService.getAssets()
    }

    suspend fun addTransaction(request: TransactionRequest): Response<JsonObject> {
        return apiService.addTransaction(request)
    }

    suspend fun updateTransaction(id: Int, request: TransactionRequest): Response<JsonObject> {
        return apiService.updateTransaction(id, request)
    }

    suspend fun deleteTransaction(id: Int): Response<JsonObject> {
        return apiService.deleteTransaction(id)
    }

    suspend fun resetMonthlyTransactions(request: com.example.cashflowin.api.model.ResetMonthlyTransactionRequest): Response<com.example.cashflowin.api.model.ResetMonthlyTransactionResponse> {
        return apiService.resetMonthlyTransactions(request)
    }
}
