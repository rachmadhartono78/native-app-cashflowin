package com.example.cashflowin.api.model

data class GoalListResponse(
    val status: String,
    val data: List<Goal>
)

data class GoalDetailResponse(
    val status: String,
    val data: GoalDetailData
)

data class GoalDetailData(
    val goal: Goal,
    val transactions: List<TransactionItem>
)

data class Goal(
    val id: Int,
    val name: String,
    val target_amount: Double,
    val current_amount: Double,
    val deadline: String?,
    val icon: String?,
    val color: String?
)

data class GoalRequest(
    val name: String,
    val target_amount: Double,
    val current_amount: Double? = 0.0,
    val deadline: String? = null,
    val icon: String? = null,
    val color: String? = null
)

data class GoalTransactionRequest(
    val amount_change: Double,
    val asset_id: Int,
    val category_id: Int
)