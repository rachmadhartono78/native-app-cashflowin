package com.example.cashflowin.api.model

data class CategoryRequest(
    val name: String,
    val type: String
)

data class CategoryResponse(
    val status: String,
    val data: List<CategoryInfo>
)

data class CategoryInfo(
    val id: Int,
    val name: String,
    val type: String
)
