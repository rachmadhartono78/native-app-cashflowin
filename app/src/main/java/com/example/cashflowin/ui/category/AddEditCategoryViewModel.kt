package com.example.cashflowin.ui.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflowin.api.CategoryRepository
import com.example.cashflowin.api.model.CategoryRequest
import kotlinx.coroutines.launch

sealed class CategorySubmitState {
    object Idle : CategorySubmitState()
    object Loading : CategorySubmitState()
    object Success : CategorySubmitState()
    object UpdateSuccess : CategorySubmitState()
    object DeleteSuccess : CategorySubmitState()
    data class Error(val message: String) : CategorySubmitState()
}

class AddEditCategoryViewModel(private val repository: CategoryRepository) : ViewModel() {
    private val _submitState = MutableLiveData<CategorySubmitState>(CategorySubmitState.Idle)
    val submitState: LiveData<CategorySubmitState> = _submitState

    fun saveCategory(name: String, type: String) {
        _submitState.value = CategorySubmitState.Loading
        viewModelScope.launch {
            try {
                val request = CategoryRequest(name, type)
                val response = repository.addCategory(request)
                
                if (response.isSuccessful) {
                    _submitState.value = CategorySubmitState.Success
                } else {
                    _submitState.value = CategorySubmitState.Error("Failed to save category. Code: ${response.code()}")
                }
            } catch (e: Exception) {
                _submitState.value = CategorySubmitState.Error(e.message ?: "Network error")
            }
        }
    }

    fun updateCategory(id: Int, name: String, type: String) {
        _submitState.value = CategorySubmitState.Loading
        viewModelScope.launch {
            try {
                val request = CategoryRequest(name, type) 
                val response = repository.updateCategory(id, request)
                
                if (response.isSuccessful) {
                    _submitState.value = CategorySubmitState.UpdateSuccess
                } else {
                    _submitState.value = CategorySubmitState.Error("Failed to update category. Code: ${response.code()}")
                }
            } catch (e: Exception) {
                _submitState.value = CategorySubmitState.Error(e.message ?: "Network error")
            }
        }
    }

    fun deleteCategory(id: Int) {
        _submitState.value = CategorySubmitState.Loading
        viewModelScope.launch {
            try {
                val response = repository.deleteCategory(id)
                
                if (response.isSuccessful) {
                    _submitState.value = CategorySubmitState.DeleteSuccess
                } else {
                    _submitState.value = CategorySubmitState.Error("Failed to delete category. Code: ${response.code()}")
                }
            } catch (e: Exception) {
                _submitState.value = CategorySubmitState.Error(e.message ?: "Network error")
            }
        }
    }
}
