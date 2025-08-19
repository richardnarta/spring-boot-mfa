package com.example.mfa.auth.model.response

import com.fasterxml.jackson.annotation.JsonProperty

data class RefreshResponse(
    @field:JsonProperty("new_access_token") val accessToken: String,
    @field:JsonProperty("new_refresh_token") val refreshToken: String,
)
