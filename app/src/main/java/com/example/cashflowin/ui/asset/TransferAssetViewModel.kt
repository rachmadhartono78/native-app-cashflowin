package com.example.cashflowin.ui.asset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.AssetInfo
import com.example.cashflowin.api.model.TransferAssetRequest
import kotlinx.coroutines.launch

sealed class TransferAssetState {
    object Idle : TransferAssetState()
    object Loading : TransferAssetState()
    object Success : TransferAssetState()
    data class AssetsLoaded(val assets: List<AssetInfo>) : TransferAssetState()
    data class Error(val message: String) : TransferAssetState()
}

class TransferAssetViewModel : ViewModel() {
    private val _transferState = MutableLiveData<TransferAssetState>(TransferAssetState.Idle)
    val transferState: LiveData<TransferAssetState> = _transferState
    
    // We need to fetch the assets to populate the spinners
    fun loadAssets(token: String) {
        _transferState.value = TransferAssetState.Loading
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                val response = ApiClient.instance.getAssets(bearerToken)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == "success") {
                        _transferState.value = TransferAssetState.AssetsLoaded(body.data)
                    } else {
                        _transferState.value = TransferAssetState.Error("Failed to fetch assets")
                    }
                } else {
                    _transferState.value = TransferAssetState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                _transferState.value = TransferAssetState.Error(e.message ?: "Network error occurred")
            }
        }
    }

    fun submitTransfer(token: String, sourceId: Int, destId: Int, amount: String, date: String, description: String) {
        _transferState.value = TransferAssetState.Loading
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                val request = TransferAssetRequest(sourceId, destId, amount, date, description)
                val response = ApiClient.instance.transferAsset(bearerToken, request)
                
                if (response.isSuccessful) {
                    _transferState.value = TransferAssetState.Success
                } else {
                    // Try to extract error string if it exists from body
                    _transferState.value = TransferAssetState.Error("Failed to transfer. Please ensure balance is adequate.")
                }
            } catch (e: Exception) {
                _transferState.value = TransferAssetState.Error(e.message ?: "Network error")
            }
        }
    }
}
