package com.example.cashflowin.ui.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.ApiClient
import com.example.cashflowin.api.model.CategoryResponse
import kotlinx.coroutines.launch

sealed class CategoriesState {
    object Idle : CategoriesState()
    object Loading : CategoriesState()
    data class Success(val response: CategoryResponse) : CategoriesState()
    data class Error(val message: String) : CategoriesState()
}

class CategoriesViewModel : ViewModel() {
    private val _categoriesState = MutableLiveData<CategoriesState>(CategoriesState.Idle)
    val categoriesState: LiveData<CategoriesState> = _categoriesState

    fun loadCategories(token: String) {
        _categoriesState.value = CategoriesState.Loading
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                val response = ApiClient.instance.getCategories(bearerToken)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == "success") {
                        _categoriesState.value = CategoriesState.Success(body)
                    } else {
                        _categoriesState.value = CategoriesState.Error("Failed to fetch categories")
                    }
                } else if (response.code() == 401) {
                    _categoriesState.value = CategoriesState.Error("UNAUTHORIZED")
                } else {
                    _categoriesState.value = CategoriesState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                _categoriesState.value = CategoriesState.Error(e.message ?: "Network error occurred")
            }
        }
    }
}
