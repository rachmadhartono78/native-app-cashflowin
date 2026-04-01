package com.example.cashflowin.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.AuthRepository
import com.example.cashflowin.api.model.LoginRequest
import com.example.cashflowin.api.model.LoginResponse
import com.example.cashflowin.api.model.RegisterRequest
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: LoginResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = repository.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    _authState.value = AuthState.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                        json.get("message").asString
                    } catch (e: Exception) {
                        "Login failed: ${response.message()}"
                    }
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun loginWithGoogle(token: String, deviceName: String = "android_device") {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = repository.loginWithGoogle(token, deviceName)
                if (response.isSuccessful) {
                    _authState.value = AuthState.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                        json.get("message").asString
                    } catch (e: Exception) {
                        "Google login failed: ${response.message()}"
                    }
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun register(name: String, email: String, password: String, deviceName: String = "Android") {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    name = name,
                    email = email,
                    password = password,
                    device_name = deviceName
                )
                val response = repository.register(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == "success") {
                        _authState.value = AuthState.Success(body)
                    } else {
                        _authState.value = AuthState.Error(body.message ?: "Registration failed")
                    }
                } else {
                    _authState.value = AuthState.Error("Registration failed. Please check your data.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error occurred")
            }
        }
    }
}
