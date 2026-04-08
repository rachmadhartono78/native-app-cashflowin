package com.example.cashflowin.ui.transaction.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.TransactionRepository
import com.example.cashflowin.api.model.TransactionItem
import kotlinx.coroutines.launch

sealed class TransactionsState {
    object Idle : TransactionsState()
    object Loading : TransactionsState()
    object LoadingMore : TransactionsState()
    data class Success(val transactions: List<TransactionItem>, val isEndOfPage: Boolean = false) : TransactionsState()
    data class Error(val message: String) : TransactionsState()
}

class TransactionsViewModel(private val repository: TransactionRepository) : ViewModel() {
    private val _transactionsState = MutableLiveData<TransactionsState>(TransactionsState.Idle)
    val transactionsState: LiveData<TransactionsState> = _transactionsState

    private var currentTransactions = mutableListOf<TransactionItem>()
    private var currentPage = 1
    private var lastPage = 1
    private var isFetching = false

    private var currentType: String? = null
    private var currentCategoryId: Int? = null
    private var currentSearch: String? = null
    private var currentStartDate: String? = null
    private var currentEndDate: String? = null

    fun loadTransactions(
        type: String? = null,
        categoryId: Int? = null,
        search: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        isRefresh: Boolean = true
    ) {
        if (isFetching) return
        
        if (isRefresh) {
            currentPage = 1
            currentTransactions.clear()
            _transactionsState.value = TransactionsState.Loading
            
            // Default to current month if no dates provided (Sync with Web logic)
            val calendar = java.util.Calendar.getInstance()
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            
            if (startDate == null && endDate == null) {
                // Start of Month
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                val start = sdf.format(calendar.time)
                
                // End of Month
                calendar.set(java.util.Calendar.DAY_OF_MONTH, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                val end = sdf.format(calendar.time)
                
                currentStartDate = start
                currentEndDate = end
            } else {
                currentStartDate = startDate
                currentEndDate = endDate
            }

            // Save other filters
            currentType = type
            currentCategoryId = categoryId
            currentSearch = search
        } else {
            if (currentPage >= lastPage) return
            _transactionsState.value = TransactionsState.LoadingMore
        }

        isFetching = true
        viewModelScope.launch {
            try {
                val response = repository.getTransactions(
                    page = if (isRefresh) 1 else currentPage + 1,
                    type = currentType,
                    categoryId = currentCategoryId,
                    search = currentSearch,
                    startDate = currentStartDate,
                    endDate = currentEndDate
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status.equals("success", ignoreCase = true)) {
                        val paginatedData = body.data
                        if (paginatedData != null) {
                            currentPage = paginatedData.current_page
                            lastPage = paginatedData.last_page
                            
                            val newData = paginatedData.data
                            currentTransactions.addAll(newData)
                            
                            _transactionsState.value = TransactionsState.Success(
                                transactions = ArrayList(currentTransactions),
                                isEndOfPage = currentPage >= lastPage
                            )
                        }
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
            } finally {
                isFetching = false
            }
        }
    }

    fun loadNextPage() {
        if (!isFetching && currentPage < lastPage) {
            loadTransactions(isRefresh = false)
        }
    }
}
