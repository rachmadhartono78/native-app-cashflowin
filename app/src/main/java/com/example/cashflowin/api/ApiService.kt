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

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: com.example.cashflowin.api.model.GoogleLoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @GET("dashboard-summary")
    suspend fun getDashboardSummary(): Response<DashboardResponse>

    @GET("assets")
    suspend fun getAssets(): Response<AssetResponse>

    @GET("categories")
    suspend fun getCategories(@Query("type") type: String? = null): Response<CategoryResponse>

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
        @Query("page") page: Int? = null,
        @Query("type") type: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("asset_id") assetId: Int? = null,
        @Query("search") search: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
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

    @POST("transactions/reset-monthly")
    suspend fun resetMonthlyTransactions(@Body request: com.example.cashflowin.api.model.ResetMonthlyTransactionRequest): Response<com.example.cashflowin.api.model.ResetMonthlyTransactionResponse>


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

    @POST("assets/sync")
    suspend fun syncAssets(): Response<com.google.gson.JsonObject>

    @PUT("profile")
    suspend fun updateProfile(@Body request: com.example.cashflowin.api.model.UpdateProfileRequest): Response<com.example.cashflowin.api.model.UpdateProfileResponse>

    @PUT("password")
    suspend fun updatePassword(@Body request: com.example.cashflowin.api.model.UpdatePasswordRequest): Response<com.google.gson.JsonObject>

    // Budgets
    @GET("budgets")
    suspend fun getBudgets(@Query("month") month: Int? = null, @Query("year") year: Int? = null): Response<com.example.cashflowin.api.model.BudgetResponse>

    @POST("budgets")
    suspend fun addBudget(@Body request: com.example.cashflowin.api.model.BudgetRequest): Response<com.google.gson.JsonObject>

    @POST("budgets/clone")
    suspend fun cloneBudgets(@Body request: com.example.cashflowin.api.model.CloneBudgetRequest): Response<com.google.gson.JsonObject>

    @DELETE("budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: Int): Response<com.google.gson.JsonObject>

    // Goals
    @GET("goals")
    suspend fun getGoals(): Response<com.example.cashflowin.api.model.GoalListResponse>

    @GET("goals/{id}")
    suspend fun getGoalDetails(@Path("id") id: Int): Response<com.example.cashflowin.api.model.GoalDetailResponse>

    @POST("goals")
    suspend fun addGoal(@Body request: com.example.cashflowin.api.model.GoalRequest): Response<com.google.gson.JsonObject>

    @PUT("goals/{id}")
    suspend fun updateGoal(@Path("id") id: Int, @Body request: com.example.cashflowin.api.model.GoalRequest): Response<com.google.gson.JsonObject>

    @PUT("goals/{id}")
    suspend fun addGoalTransaction(@Path("id") id: Int, @Body request: com.example.cashflowin.api.model.GoalTransactionRequest): Response<com.google.gson.JsonObject>

    @DELETE("goals/{id}")
    suspend fun deleteGoal(@Path("id") id: Int): Response<com.google.gson.JsonObject>

    @POST("goals/{id}/withdraw")
    suspend fun withdrawGoal(@Path("id") id: Int, @Body request: com.example.cashflowin.api.model.GoalWithdrawRequest): Response<com.google.gson.JsonObject>

    // Debts
    @GET("debts")
    suspend fun getDebts(): Response<com.example.cashflowin.api.model.DebtListResponse>

    @POST("debts")
    suspend fun addDebt(@Body request: com.example.cashflowin.api.model.DebtRequest): Response<com.google.gson.JsonObject>

    @GET("debts/{id}")
    suspend fun getDebtDetails(@Path("id") id: Int): Response<com.example.cashflowin.api.model.DebtDetailResponse>

    @PUT("debts/{id}")
    suspend fun updateDebt(@Path("id") id: Int, @Body request: com.example.cashflowin.api.model.DebtRequest): Response<com.google.gson.JsonObject>

    @DELETE("debts/{id}")
    suspend fun deleteDebt(@Path("id") id: Int): Response<com.google.gson.JsonObject>

    @POST("debts/{id}/payments")
    suspend fun addDebtPayment(@Path("id") id: Int, @Body request: com.example.cashflowin.api.model.DebtPaymentRequest): Response<com.google.gson.JsonObject>

    @DELETE("debts/{id}/payments/{paymentId}")
    suspend fun deleteDebtPayment(@Path("id") id: Int, @Path("paymentId") paymentId: Int): Response<com.google.gson.JsonObject>

    // Recurring Transactions
    @GET("recurring-transactions")
    suspend fun getRecurringTransactions(@Query("status") status: String? = null, @Query("type") type: String? = null, @Query("search") search: String? = null): Response<com.example.cashflowin.api.model.RecurringTransactionListResponse>

    @GET("recurring-transactions/{recurring}")
    suspend fun getRecurringTransactionDetails(@Path("recurring") id: Int): Response<com.example.cashflowin.api.model.RecurringTransactionResponse>

    @POST("recurring-transactions")
    suspend fun addRecurringTransaction(@Body request: com.example.cashflowin.api.model.RecurringTransactionRequest): Response<com.google.gson.JsonObject>

    @PUT("recurring-transactions/{recurring}")
    suspend fun updateRecurringTransaction(@Path("recurring") id: Int, @Body request: com.example.cashflowin.api.model.RecurringTransactionRequest): Response<com.google.gson.JsonObject>

    @DELETE("recurring-transactions/{recurring}")
    suspend fun deleteRecurringTransaction(@Path("recurring") id: Int): Response<com.google.gson.JsonObject>

    @POST("recurring-transactions/{recurring}/pause")
    suspend fun pauseRecurringTransaction(@Path("recurring") id: Int): Response<com.google.gson.JsonObject>

    @POST("recurring-transactions/{recurring}/resume")
    suspend fun resumeRecurringTransaction(@Path("recurring") id: Int): Response<com.google.gson.JsonObject>
}
