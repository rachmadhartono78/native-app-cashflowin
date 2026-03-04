package com.example.cashflowin.api.model

data class User(
    val id: Int,
    val name: String,
    val email: String
)

data class LoginResponse(
    val status: String,
    val token: String?,
    val user: User?,
    val message: String?
)

data class LoginRequest(
    val email: String,
    val password: String,
    val device_name: String = "android_device"
)
