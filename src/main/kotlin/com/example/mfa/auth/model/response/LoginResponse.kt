package com.example.mfa.auth.model.response

import com.example.mfa.users.model.response.User
import com.fasterxml.jackson.annotation.JsonProperty

data class LoginResponse(
    @field:JsonProperty("access_token") val accessToken: String,
    @field:JsonProperty("refresh_token") val refreshToken: String,
    @field:JsonProperty("user_info") val user: User,
)
