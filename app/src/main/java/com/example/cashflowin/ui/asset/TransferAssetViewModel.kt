package com.example.cashflowin.ui.asset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.AssetRepository
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

class TransferAssetViewModel(private val repository: AssetRepository) : ViewModel() {
    private val _transferState = MutableLiveData<TransferAssetState>(TransferAssetState.Idle)
    val transferState: LiveData<TransferAssetState> = _transferState
    
    fun loadAssets() {
        _transferState.value = TransferAssetState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getAssets()
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status.equals("success", ignoreCase = true)) {
                        _transferState.value = TransferAssetState.AssetsLoaded(body.data)
                    } else {
                        _transferState.value = TransferAssetState.Error(body.message ?: "Failed to fetch assets")
                    }
                } else {
                    _transferState.value = TransferAssetState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                _transferState.value = TransferAssetState.Error(e.message ?: "Network error occurred")
            }
        }
    }

    fun submitTransfer(sourceId: Int, destId: Int, amount: String, date: String, description: String) {
        _transferState.value = TransferAssetState.Loading
        viewModelScope.launch {
            try {
                val request = TransferAssetRequest(sourceId, destId, amount, date, description)
                val response = repository.transferAsset(request)
                
                if (response.isSuccessful) {
                    _transferState.value = TransferAssetState.Success
                } else {
                    _transferState.value = TransferAssetState.Error("Failed to transfer. Please ensure balance is adequate.")
                }
            } catch (e: Exception) {
                _transferState.value = TransferAssetState.Error(e.message ?: "Network error")
            }
        }
    }
}