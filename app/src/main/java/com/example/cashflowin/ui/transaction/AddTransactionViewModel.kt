package com.example.cashflowin.ui.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.AssetInfo
import com.example.cashflowin.api.model.CategoryInfo
import com.example.cashflowin.api.model.TransactionRequest
import kotlinx.coroutines.launch

sealed class DropdownState<out T> {
    object Loading : DropdownState<Nothing>()
    data class Success<T>(val data: List<T>) : DropdownState<T>()
    data class Error(val message: String) : DropdownState<Nothing>()
}

sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    object Success : SubmitState()
    object UpdateSuccess : SubmitState()
    object DeleteSuccess : SubmitState()
    data class Error(val message: String) : SubmitState()
}

class AddTransactionViewModel : ViewModel() {

    private val _categoriesState = MutableLiveData<DropdownState<CategoryInfo>>()
    val categoriesState: LiveData<DropdownState<CategoryInfo>> = _categoriesState

    private val _assetsState = MutableLiveData<DropdownState<AssetInfo>>()
    val assetsState: LiveData<DropdownState<AssetInfo>> = _assetsState

    private val _submitState = MutableLiveData<SubmitState>(SubmitState.Idle)
    val submitState: LiveData<SubmitState> = _submitState

    fun loadDropdownData(token: String) {
        val bearerToken = "Bearer $token"
        
        // Load Categories
        _categoriesState.value = DropdownState.Loading
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getCategories(bearerToken)
                if (response.isSuccessful && response.body() != null) {
                    _categoriesState.value = DropdownState.Success(response.body()!!.data)
                } else {
                    _categoriesState.value = DropdownState.Error("Failed to load categories: ${response.code()}")
                }
            } catch (e: Exception) {
                _categoriesState.value = DropdownState.Error("Network error: ${e.message}")
            }
        }

        // Load Assets
        _assetsState.value = DropdownState.Loading
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getAssets(bearerToken)
                if (response.isSuccessful && response.body() != null) {
                    _assetsState.value = DropdownState.Success(response.body()!!.data)
                } else {
                    _assetsState.value = DropdownState.Error("Failed to load assets: ${response.code()}")
                }
            } catch (e: Exception) {
                _assetsState.value = DropdownState.Error("Network error: ${e.message}")
            }
        }
    }

    fun submitTransaction(token: String, request: TransactionRequest) {
        _submitState.value = SubmitState.Loading
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                val response = ApiClient.instance.addTransaction(bearerToken, request)
                if (response.isSuccessful) {
                    _submitState.value = SubmitState.Success
                } else {
                    _submitState.value = SubmitState.Error("Failed to save transaction: ${response.code()}")
                }
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e.message ?: "Network error")
            }
        }
    }

    fun updateTransaction(token: String, id: Int, request: TransactionRequest) {
        _submitState.value = SubmitState.Loading
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                val response = ApiClient.instance.updateTransaction(bearerToken, id, request)
                if (response.isSuccessful) {
                    _submitState.value = SubmitState.UpdateSuccess
                } else {
                    _submitState.value = SubmitState.Error("Failed to update transaction: ${response.code()}")
                }
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e.message ?: "Network error")
            }
        }
    }

    fun deleteTransaction(token: String, id: Int) {
        _submitState.value = SubmitState.Loading
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                val response = ApiClient.instance.deleteTransaction(bearerToken, id)
                if (response.isSuccessful) {
                    _submitState.value = SubmitState.DeleteSuccess
                } else {
                    _submitState.value = SubmitState.Error("Failed to delete transaction: ${response.code()}")
                }
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e.message ?: "Network error")
            }
        }
    }
}
