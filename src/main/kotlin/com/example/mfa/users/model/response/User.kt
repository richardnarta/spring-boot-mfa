package com.example.mfa.users.model.response

import com.fasterxml.jackson.annotation.JsonProperty

data class User(
    @field:JsonProperty("user_id") val id: String,
    @field:JsonProperty("user_name") val name: String,
    @field:JsonProperty("user_role") val role: String,
    @field:JsonProperty("user_email") val email: String,
    @field:JsonProperty("user_username") val username: String,
    @field:JsonProperty("user_created_at") val createdAt: String,
    @field:JsonProperty("user_updated_at") val updatedAt: String
)
