package com.example.cashflowin.api

import com.example.cashflowin.api.model.CategoryRequest
import com.example.cashflowin.api.model.CategoryResponse
import retrofit2.Response

class CategoryRepository(private val apiService: ApiService) {

    suspend fun getCategories(type: String? = null): Response<CategoryResponse> {
        return apiService.getCategories(type)
    }

    suspend fun addCategory(request: CategoryRequest): Response<Any> {
        return apiService.addCategory(request)
    }

    suspend fun updateCategory(id: Int, request: CategoryRequest): Response<Any> {
        return apiService.updateCategory(id, request)
    }

    suspend fun deleteCategory(id: Int): Response<Any> {
        return apiService.deleteCategory(id)
    }
}