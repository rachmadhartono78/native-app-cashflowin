package com.example.cashflowin.api.model

data class TransactionListResponse(
    val status: String,
    val data: PaginatedTransactionData?
)

data class PaginatedTransactionData(
    val current_page: Int,
    val data: List<TransactionItem>,
    val first_page_url: String?,
    val from: Int?,
    val last_page: Int,
    val last_page_url: String?,
    val next_page_url: String?,
    val path: String,
    val per_page: Int,
    val prev_page_url: String?,
    val to: Int?,
    val total: Int
)

// Reusing TransactionItem for individual transactions
