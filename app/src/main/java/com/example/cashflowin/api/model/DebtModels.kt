package com.example.cashflowin.api.model

data class DebtListResponse(
    val status: String,
    val data: DebtData
)

data class DebtData(
    val debts: List<Debt>,
    val total_debt_balance: Double,
    val total_receivable_balance: Double
)

data class DebtDetailResponse(
    val status: String,
    val data: Debt
)

data class Debt(
    val id: Int,
    val person_name: String,
    val type: String, // 'debt' | 'receivable'
    val amount: Double,
    val remaining_amount: Double?,
    val due_date: String?,
    val status: String, // 'pending' | 'paid'
    val description: String?,
    val payments: List<DebtPayment>?
)

data class DebtPayment(
    val id: Int,
    val amount: Double,
    val payment_date: String,
    val notes: String?,
    val asset: AssetInfo?
)

data class DebtRequest(
    val person_name: String,
    val type: String,
    val amount: Double,
    val due_date: String?,
    val description: String?
)

data class DebtPaymentRequest(
    val amount: Double,
    val payment_date: String,
    val asset_id: Int,
    val category_id: Int,
    val notes: String?
)
