package com.example.cashflowin.ui.asset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.AssetRepository
import com.example.cashflowin.api.model.AssetRequest
import kotlinx.coroutines.launch

sealed class AssetSubmitState {
    object Idle : AssetSubmitState()
    object Loading : AssetSubmitState()
    object Success : AssetSubmitState()
    data class Error(val message: String) : AssetSubmitState()
}

class AddEditAssetViewModel(private val repository: AssetRepository) : ViewModel() {

    private val _submitState = MutableLiveData<AssetSubmitState>(AssetSubmitState.Idle)
    val submitState: LiveData<AssetSubmitState> = _submitState

    fun addAsset(name: String, balance: Double, type: String = "Cash") {
        _submitState.value = AssetSubmitState.Loading
        viewModelScope.launch {
            try {
                val request = AssetRequest(name, type, balance.toString())
                val response = repository.addAsset(request)
                if (response.isSuccessful) {
                    _submitState.value = AssetSubmitState.Success
                } else {
                    _submitState.value = AssetSubmitState.Error("Failed to add asset: ${response.code()}")
                }
            } catch (e: Exception) {
                _submitState.value = AssetSubmitState.Error(e.message ?: "Network error")
            }
        }
    }

    fun updateAsset(id: Int, name: String, balance: Double, type: String = "Cash") {
        _submitState.value = AssetSubmitState.Loading
        viewModelScope.launch {
            try {
                val request = AssetRequest(name, type, balance.toString())
                val response = repository.updateAsset(id, request)
                if (response.isSuccessful) {
                    _submitState.value = AssetSubmitState.Success
                } else {
                    _submitState.value = AssetSubmitState.Error("Failed to update asset: ${response.code()}")
                }
            } catch (e: Exception) {
                _submitState.value = AssetSubmitState.Error(e.message ?: "Network error")
            }
        }
    }

    fun deleteAsset(id: Int) {
        _submitState.value = AssetSubmitState.Loading
        viewModelScope.launch {
            try {
                val response = repository.deleteAsset(id)
                if (response.isSuccessful) {
                    _submitState.value = AssetSubmitState.Success
                } else {
                    _submitState.value = AssetSubmitState.Error("Failed to delete asset: ${response.code()}")
                }
            } catch (e: Exception) {
                _submitState.value = AssetSubmitState.Error(e.message ?: "Network error")
            }
        }
    }
}
