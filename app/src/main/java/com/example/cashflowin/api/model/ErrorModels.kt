package com.example.cashflowin.api.model

data class ApiErrorResponse(
    val status: String,
    val message: String?,
    val errors: Map<String, List<String>>? = null
)
