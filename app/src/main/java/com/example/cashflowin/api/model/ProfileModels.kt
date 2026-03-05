package com.example.cashflowin.api.model

data class UpdateProfileRequest(
    val name: String,
    val email: String
)

data class UpdateProfileResponse(
    val status: String,
    val message: String,
    val user: User?
)

data class UpdatePasswordRequest(
    val current_password: String,
    val password: String,
    val password_confirmation: String
)
