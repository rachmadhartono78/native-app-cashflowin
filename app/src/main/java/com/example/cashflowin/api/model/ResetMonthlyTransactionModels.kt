package com.example.cashflowin.api.model

import com.google.gson.annotations.SerializedName

data class ResetMonthlyTransactionRequest(
    @SerializedName("month") val month: Int,
    @SerializedName("year") val year: Int,
    @SerializedName("password") val password: String
)

data class ResetMonthlyTransactionResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ResetMonthlyTransactionData?
)

data class ResetMonthlyTransactionData(
    @SerializedName("deleted_count") val deletedCount: Int,
    @SerializedName("month") val month: Int,
    @SerializedName("year") val year: Int
)
