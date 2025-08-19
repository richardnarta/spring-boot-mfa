package com.example.mfa.auth.model.request

import com.example.mfa.core.validation.Required
import com.fasterxml.jackson.annotation.JsonProperty

data class RefreshPayload(
    @field:Required("refresh_token is required and cannot be blank.")
    @field:JsonProperty("refresh_token") val refreshToken: String? = null,
)
