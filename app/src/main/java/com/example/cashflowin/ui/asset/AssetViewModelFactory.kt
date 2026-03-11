package com.example.cashflowin.ui.asset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cashflowin.api.AssetRepository

class AssetViewModelFactory(private val repository: AssetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssetsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AssetsViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(AddEditAssetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditAssetViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(TransferAssetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransferAssetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}