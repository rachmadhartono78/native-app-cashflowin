package com.example.cashflowin.ui.transaction.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.TransactionRepository
import com.example.cashflowin.api.model.TransactionItem
import com.example.cashflowin.api.model.TransactionListResponse
import kotlinx.coroutines.launch

sealed class TransactionsState {
    object Idle : TransactionsState()
    object Loading : TransactionsState()
    data class Success(val transactions: List<TransactionItem>) : TransactionsState()
    data class Error(val message: String) : TransactionsState()
}

class TransactionsViewModel(private val repository: TransactionRepository) : ViewModel() {
    private val _transactionsState = MutableLiveData<TransactionsState>(TransactionsState.Idle)
    val transactionsState: LiveData<TransactionsState> = _transactionsState

    private var fullTransactionList: List<TransactionItem> = emptyList()

    fun loadTransactions(
        type: String? = null,
        categoryId: Int? = null,
        search: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        _transactionsState.value = TransactionsState.Loading
        viewModelScope.launch {
            try {
                // We still call the API with parameters in case the backend is updated later
                val response = repository.getTransactions(type, categoryId, search, startDate, endDate)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status.equals("success", ignoreCase = true)) {
                        fullTransactionList = body.data?.data ?: emptyList()
                        // Apply smart local filtering on top of API result
                        filterTransactionsLocally(type, search, startDate, endDate)
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

    /**
     * Smart Local Filtering: Filters the current list in memory.
     * This ensures the search works even if the backend hasn't implemented it yet.
     */
    fun filterTransactionsLocally(
        type: String? = null,
        search: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        var filteredList = fullTransactionList

        // Filter by Type (Income/Expense)
        if (type != null) {
            filteredList = filteredList.filter { it.type.equals(type, ignoreCase = true) }
        }

        // Filter by Search Query (Description or Category Name)
        if (!search.isNullOrBlank()) {
            val query = search.lowercase()
            filteredList = filteredList.filter { 
                it.description?.lowercase()?.contains(query) == true || 
                it.category?.name?.lowercase()?.contains(query) == true 
            }
        }

        // Filter by Date Range
        if (startDate != null && endDate != null) {
            filteredList = filteredList.filter { it.date in startDate..endDate }
        }

        _transactionsState.value = TransactionsState.Success(filteredList)
    }
}
