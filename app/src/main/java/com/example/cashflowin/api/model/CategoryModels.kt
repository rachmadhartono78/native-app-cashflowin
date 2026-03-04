package com.example.cashflowin.api.model

data class CategoryRequest(
    val name: String,
    val type: String
)

data class CategoryResponse(
    val status: String,
    val data: List<CategoryInfo>,
    val message: String? = null
)

data class CategoryInfo(
    val id: Int,
    val name: String,
    val type: String? = null,
    val color: String? = null,
    val icon: String? = null
)
