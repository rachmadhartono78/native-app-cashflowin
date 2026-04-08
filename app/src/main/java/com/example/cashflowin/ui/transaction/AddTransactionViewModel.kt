package com.example.cashflowin.ui.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.TransactionRepository
import com.example.cashflowin.api.model.ApiErrorResponse
import com.example.cashflowin.api.model.AssetInfo
import com.example.cashflowin.api.model.CategoryInfo
import com.example.cashflowin.api.model.Debt
import com.example.cashflowin.api.model.Goal
import com.example.cashflowin.api.model.TransactionRequest
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Response

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

class AddTransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _categoriesState = MutableLiveData<DropdownState<CategoryInfo>>()
    val categoriesState: LiveData<DropdownState<CategoryInfo>> = _categoriesState

    private val _assetsState = MutableLiveData<DropdownState<AssetInfo>>()
    val assetsState: LiveData<DropdownState<AssetInfo>> = _assetsState

    private val _debtsState = MutableLiveData<DropdownState<Debt>>()
    val debtsState: LiveData<DropdownState<Debt>> = _debtsState

    private val _goalsState = MutableLiveData<DropdownState<Goal>>()
    val goalsState: LiveData<DropdownState<Goal>> = _goalsState

    private val _submitState = MutableLiveData<SubmitState>(SubmitState.Idle)
    val submitState: LiveData<SubmitState> = _submitState

    fun loadDropdownData() {
        // Load Categories
        _categoriesState.value = DropdownState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getCategories()
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
                val response = repository.getAssets()
                if (response.isSuccessful && response.body() != null) {
                    _assetsState.value = DropdownState.Success(response.body()!!.data)
                } else {
                    _assetsState.value = DropdownState.Error("Failed to load assets: ${response.code()}")
                }
            } catch (e: Exception) {
                _assetsState.value = DropdownState.Error("Network error: ${e.message}")
            }
        }

        // Load Debts
        _debtsState.value = DropdownState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getDebts()
                if (response.isSuccessful && response.body() != null) {
                    _debtsState.value = DropdownState.Success(response.body()!!.data.debts)
                } else {
                    _debtsState.value = DropdownState.Error("Failed to load debts")
                }
            } catch (e: Exception) {
                _debtsState.value = DropdownState.Error("Network error: ${e.message}")
            }
        }

        // Load Goals
        _goalsState.value = DropdownState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getGoals()
                if (response.isSuccessful && response.body() != null) {
                    _goalsState.value = DropdownState.Success(response.body()!!.data)
                } else {
                    _goalsState.value = DropdownState.Error("Failed to load goals")
                }
            } catch (e: Exception) {
                _goalsState.value = DropdownState.Error("Network error: ${e.message}")
            }
        }
    }

    fun submitTransaction(request: TransactionRequest) {
        _submitState.value = SubmitState.Loading
        viewModelScope.launch {
            try {
                val response = repository.addTransaction(request)
                if (response.isSuccessful) {
                    _submitState.value = SubmitState.Success
                } else {
                    val errorMsg = parseError(response)
                    _submitState.value = SubmitState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e.message ?: "Network error")
            }
        }
    }

    fun updateTransaction(id: Int, request: TransactionRequest) {
        _submitState.value = SubmitState.Loading
        viewModelScope.launch {
            try {
                val response = repository.updateTransaction(id, request)
                if (response.isSuccessful) {
                    _submitState.value = SubmitState.UpdateSuccess
                } else {
                    val errorMsg = parseError(response)
                    _submitState.value = SubmitState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e.message ?: "Network error")
            }
        }
    }

    private fun parseError(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val errorResponse = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                errorResponse.message ?: "Error: ${response.code()}"
            } else {
                "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            "An error occurred: ${response.code()}"
        }
    }

    fun deleteTransaction(id: Int) {
        _submitState.value = SubmitState.Loading
        viewModelScope.launch {
            try {
                val response = repository.deleteTransaction(id)
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
