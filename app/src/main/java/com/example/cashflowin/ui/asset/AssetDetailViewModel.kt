package com.example.cashflowin.ui.asset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.AssetRepository
import com.example.cashflowin.api.model.TransactionItem
import kotlinx.coroutines.launch

sealed class MutationsState {
    object Loading : MutationsState()
    data class Success(val transactions: List<TransactionItem>) : MutationsState()
    data class Error(val message: String) : MutationsState()
}

class AssetDetailViewModel(private val repository: AssetRepository) : ViewModel() {

    private val _mutationsState = MutableLiveData<MutationsState>()
    val mutationsState: LiveData<MutationsState> = _mutationsState

    fun getMutations(assetId: Int) {
        _mutationsState.value = MutationsState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getTransactions(assetId = assetId)
                if (response.isSuccessful && response.body() != null) {
                    val transactions = response.body()?.data?.data ?: emptyList()
                    _mutationsState.value = MutationsState.Success(transactions)
                } else {
                    _mutationsState.value = MutationsState.Error("Gagal memuat mutasi")
                }
            } catch (e: Exception) {
                _mutationsState.value = MutationsState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }
}

class AssetDetailViewModelFactory(private val repository: AssetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssetDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AssetDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}