package com.example.cashflowin.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

import com.example.cashflowin.api.model.LoginRequest
import com.example.cashflowin.api.model.LoginResponse
import com.example.cashflowin.api.model.RegisterRequest
import com.example.cashflowin.api.model.DashboardResponse
import com.example.cashflowin.api.model.CategoryResponse
import com.example.cashflowin.api.model.AssetResponse
import com.example.cashflowin.api.model.TransactionRequest
import com.example.cashflowin.api.model.TransactionListResponse
import com.example.cashflowin.api.model.TransferAssetRequest
import com.example.cashflowin.api.model.AssetRequest
import com.example.cashflowin.api.model.CategoryRequest

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @GET("dashboard-summary")
    suspend fun getDashboardSummary(): Response<DashboardResponse>

    @GET("assets")
    suspend fun getAssets(): Response<AssetResponse>

    @GET("categories")
    suspend fun getCategories(): Response<CategoryResponse>

    @POST("categories")
    suspend fun addCategory(@Body request: CategoryRequest): Response<Any>

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body request: CategoryRequest
    ): Response<Any>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): Response<Any>

    // Reports Export
    @Streaming
    @GET("reports/export/pdf")
    suspend fun exportPdf(
        @Query("month") month: String,
        @Query("year") year: String
    ): Response<ResponseBody>

    @Streaming
    @GET("reports/export/csv")
    suspend fun exportCsv(
        @Query("month") month: String,
        @Query("year") year: String
    ): Response<ResponseBody>

    @POST("logout")
    suspend fun logout(): Response<com.google.gson.JsonObject>

    @GET("transactions")
    suspend fun getTransactions(
        @Query("type") type: String? = null
    ): Response<TransactionListResponse>

    @POST("transactions")
    suspend fun addTransaction(@Body request: TransactionRequest): Response<com.google.gson.JsonObject>

    @PUT("transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: Int,
        @Body request: TransactionRequest
    ): Response<com.google.gson.JsonObject>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Int): Response<com.google.gson.JsonObject>

    @POST("assets/transfer")
    suspend fun transferAsset(@Body request: TransferAssetRequest): Response<com.google.gson.JsonObject>

    @POST("assets")
    suspend fun addAsset(@Body request: AssetRequest): Response<com.google.gson.JsonObject>

    @PUT("assets/{id}")
    suspend fun updateAsset(
        @Path("id") id: Int,
        @Body request: AssetRequest
    ): Response<com.google.gson.JsonObject>

    @DELETE("assets/{id}")
    suspend fun deleteAsset(@Path("id") id: Int): Response<com.google.gson.JsonObject>
}
