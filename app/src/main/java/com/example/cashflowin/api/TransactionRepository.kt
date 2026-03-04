package com.example.cashflowin.api

import com.example.cashflowin.api.model.AssetResponse
import com.example.cashflowin.api.model.CategoryResponse
import com.example.cashflowin.api.model.TransactionListResponse
import com.example.cashflowin.api.model.TransactionRequest
import com.google.gson.JsonObject
import retrofit2.Response

class TransactionRepository(private val apiService: ApiService) {

    suspend fun getTransactions(type: String? = null): Response<TransactionListResponse> {
        return apiService.getTransactions(type)
    }

    suspend fun getCategories(): Response<CategoryResponse> {
        return apiService.getCategories()
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
}
