package com.example.cashflowin.api

import com.example.cashflowin.api.model.LoginRequest
import com.example.cashflowin.api.model.LoginResponse
import com.example.cashflowin.api.model.RegisterRequest
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {

    suspend fun login(request: LoginRequest): Response<LoginResponse> {
        return apiService.login(request)
    }

    suspend fun register(request: RegisterRequest): Response<LoginResponse> {
        return apiService.register(request)
    }
}