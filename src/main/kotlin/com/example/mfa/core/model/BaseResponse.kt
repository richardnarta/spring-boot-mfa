package com.example.mfa.core.model

import com.fasterxml.jackson.annotation.JsonProperty

open class BaseResponse<T>(
    val error: Boolean = false,
    val message: String? = "success",
    val data: T? = null
)

data class PaginatedResponse<T>(
    val items: List<T>,
    @field:JsonProperty("total_items") val totalItems: Long,
    @field:JsonProperty("current_page") val currentPage: Int,
    @field:JsonProperty("total_pages") val totalPages: Int,
)