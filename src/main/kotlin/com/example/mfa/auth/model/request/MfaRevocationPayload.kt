package com.example.mfa.auth.model.request

import com.example.mfa.core.validation.Required
import jakarta.validation.constraints.Size

data class MfaRevocationPayload(
    @field:Required("password is required and cannot be blank.")
    @field:Size(min = 8, message = "password must be at least 8 characters long.")
    val password: String? = null,
)
