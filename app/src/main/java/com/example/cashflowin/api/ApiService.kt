package com.example.cashflowin.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
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
    suspend fun getDashboardSummary(@Header("Authorization") token: String): Response<DashboardResponse>

    @GET("assets")
    suspend fun getAssets(@Header("Authorization") token: String): Response<AssetResponse>

    @GET("categories")
    suspend fun getCategories(@Header("Authorization") token: String): Response<CategoryResponse>

    @POST("categories")
    suspend fun addCategory(
        @Header("Authorization") token: String,
        @Body request: CategoryRequest
    ): Response<Any>

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CategoryRequest
    ): Response<Any>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Any>

    // Reports Export
    @Streaming
    @GET("reports/export/pdf")
    suspend fun exportPdf(
        @Header("Authorization") token: String,
        @Query("month") month: String,
        @Query("year") year: String
    ): Response<ResponseBody>

    @Streaming
    @GET("reports/export/csv")
    suspend fun exportCsv(
        @Header("Authorization") token: String,
        @Query("month") month: String,
        @Query("year") year: String
    ): Response<ResponseBody>

    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String): Response<com.google.gson.JsonObject>

    @GET("transactions")
    suspend fun getTransactions(
        @Header("Authorization") token: String,
        @retrofit2.http.Query("type") type: String? = null
    ): Response<com.example.cashflowin.api.model.TransactionListResponse>

    @POST("transactions")
    suspend fun addTransaction(
        @Header("Authorization") token: String,
        @Body request: TransactionRequest
    ): Response<com.google.gson.JsonObject>

    @retrofit2.http.PUT("transactions/{id}")
    suspend fun updateTransaction(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") id: Int,
        @Body request: TransactionRequest
    ): Response<com.google.gson.JsonObject>

    @retrofit2.http.DELETE("transactions/{id}")
    suspend fun deleteTransaction(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") id: Int
    ): Response<com.google.gson.JsonObject>

    @retrofit2.http.POST("assets/transfer")
    suspend fun transferAsset(
        @Header("Authorization") token: String,
        @Body request: com.example.cashflowin.api.model.TransferAssetRequest
    ): Response<com.google.gson.JsonObject>

    @POST("assets")
    suspend fun addAsset(
        @Header("Authorization") token: String,
        @Body request: com.example.cashflowin.api.model.AssetRequest
    ): Response<com.google.gson.JsonObject>

    @retrofit2.http.PUT("assets/{id}")
    suspend fun updateAsset(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") id: Int,
        @Body request: com.example.cashflowin.api.model.AssetRequest
    ): Response<com.google.gson.JsonObject>

    @retrofit2.http.DELETE("assets/{id}")
    suspend fun deleteAsset(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") id: Int
    ): Response<com.google.gson.JsonObject>
}
