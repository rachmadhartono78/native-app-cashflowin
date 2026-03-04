package com.example.cashflowin.api.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val device_name: String
)
