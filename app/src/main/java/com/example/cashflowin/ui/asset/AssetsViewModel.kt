package com.example.cashflowin.ui.asset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.AssetResponse
import kotlinx.coroutines.launch

sealed class AssetsState {
    object Idle : AssetsState()
    object Loading : AssetsState()
    data class Success(val response: AssetResponse) : AssetsState()
    data class Error(val message: String) : AssetsState()
}

class AssetsViewModel : ViewModel() {
    private val _assetsState = MutableLiveData<AssetsState>(AssetsState.Idle)
    val assetsState: LiveData<AssetsState> = _assetsState

    fun loadAssets(token: String) {
        _assetsState.value = AssetsState.Loading
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                // Reusing Dashboard ApiClient logic which contains Asset requests if defined, or dynamically via Retrofit
                val response = ApiClient.instance.getAssets(bearerToken)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == "success") {
                        _assetsState.value = AssetsState.Success(body)
                    } else {
                        _assetsState.value = AssetsState.Error("Failed to fetch assets")
                    }
                } else if (response.code() == 401) {
                    _assetsState.value = AssetsState.Error("UNAUTHORIZED")
                } else {
                    _assetsState.value = AssetsState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                _assetsState.value = AssetsState.Error(e.message ?: "Network error occurred")
            }
        }
    }
}
