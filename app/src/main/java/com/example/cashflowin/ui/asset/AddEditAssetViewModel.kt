package com.example.cashflowin.ui.asset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.AssetRequest
import kotlinx.coroutines.launch

sealed class AssetSubmitState {
    object Idle : AssetSubmitState()
    object Loading : AssetSubmitState()
    object Success : AssetSubmitState()
    object UpdateSuccess : AssetSubmitState()
    object DeleteSuccess : AssetSubmitState()
    data class Error(val message: String) : AssetSubmitState()
}

class AddEditAssetViewModel : ViewModel() {
    private val _submitState = MutableLiveData<AssetSubmitState>(AssetSubmitState.Idle)
    val submitState: LiveData<AssetSubmitState> = _submitState

    fun saveAsset(token: String, name: String, type: String, amount: String) {
        _submitState.value = AssetSubmitState.Loading
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                val request = AssetRequest(name, type, amount.ifBlank { "0" })
                val response = ApiClient.instance.addAsset(bearerToken, request)
                
                if (response.isSuccessful) {
                    _submitState.value = AssetSubmitState.Success
                } else {
                    _submitState.value = AssetSubmitState.Error("Failed to save asset. Code: ${response.code()}")
                }
            } catch (e: Exception) {
                _submitState.value = AssetSubmitState.Error(e.message ?: "Network error")
            }
        }
    }

    fun updateAsset(token: String, id: Int, name: String, type: String) {
        _submitState.value = AssetSubmitState.Loading
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                // API expects amount field due to validation rule even if it isn't updated, let's pass a dummy or ensure it matches API req.
                val request = AssetRequest(name, type, "0") 
                val response = ApiClient.instance.updateAsset(bearerToken, id, request)
                
                if (response.isSuccessful) {
                    _submitState.value = AssetSubmitState.UpdateSuccess
                } else {
                    _submitState.value = AssetSubmitState.Error("Failed to update asset. Code: ${response.code()}")
                }
            } catch (e: Exception) {
                _submitState.value = AssetSubmitState.Error(e.message ?: "Network error")
            }
        }
    }

    fun deleteAsset(token: String, id: Int) {
        _submitState.value = AssetSubmitState.Loading
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                val response = ApiClient.instance.deleteAsset(bearerToken, id)
                
                if (response.isSuccessful) {
                    _submitState.value = AssetSubmitState.DeleteSuccess
                } else {
                    _submitState.value = AssetSubmitState.Error("Failed to delete asset. Code: ${response.code()}")
                }
            } catch (e: Exception) {
                _submitState.value = AssetSubmitState.Error(e.message ?: "Network error")
            }
        }
    }
}
