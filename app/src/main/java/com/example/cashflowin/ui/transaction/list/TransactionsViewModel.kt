package com.example.cashflowin.ui.transaction.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.TransactionRepository
import com.example.cashflowin.api.model.TransactionListResponse
import kotlinx.coroutines.launch

sealed class TransactionsState {
    object Idle : TransactionsState()
    object Loading : TransactionsState()
    data class Success(val response: TransactionListResponse) : TransactionsState()
    data class Error(val message: String) : TransactionsState()
}

class TransactionsViewModel(private val repository: TransactionRepository) : ViewModel() {
    private val _transactionsState = MutableLiveData<TransactionsState>(TransactionsState.Idle)
    val transactionsState: LiveData<TransactionsState> = _transactionsState

    fun loadTransactions(type: String? = null) {
        _transactionsState.value = TransactionsState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getTransactions(type)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == "success") {
                        _transactionsState.value = TransactionsState.Success(body)
                    } else {
                        _transactionsState.value = TransactionsState.Error("Failed to fetch transactions")
                    }
                } else if (response.code() == 401) {
                    _transactionsState.value = TransactionsState.Error("UNAUTHORIZED")
                } else {
                    _transactionsState.value = TransactionsState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                _transactionsState.value = TransactionsState.Error(e.message ?: "Network error occurred")
            }
        }
    }
}
