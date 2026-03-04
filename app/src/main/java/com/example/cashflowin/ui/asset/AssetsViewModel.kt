package com.example.cashflowin.ui.asset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.AssetRepository
import com.example.cashflowin.api.model.AssetResponse
import kotlinx.coroutines.launch

sealed class AssetsState {
    object Idle : AssetsState()
    object Loading : AssetsState()
    data class Success(val response: AssetResponse) : AssetsState()
    data class Error(val message: String) : AssetsState()
}

class AssetsViewModel(private val repository: AssetRepository) : ViewModel() {

    private val _assetsState = MutableLiveData<AssetsState>(AssetsState.Idle)
    val assetsState: LiveData<AssetsState> = _assetsState

    fun loadAssets() {
        _assetsState.value = AssetsState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getAssets()
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == "success") {
                        _assetsState.value = AssetsState.Success(body)
                    } else {
                        // Fixed unresolved reference 'message'
                        _assetsState.value = AssetsState.Error(body.message ?: "Failed to fetch assets")
                    }
                } else {
                    _assetsState.value = AssetsState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _assetsState.value = AssetsState.Error(e.message ?: "Network error occurred")
            }
        }
    }
}
