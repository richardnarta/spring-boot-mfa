package com.example.mfa.auth.model.request

import com.example.mfa.core.validation.MustOnlyOneOf
import com.example.mfa.core.validation.Required
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

@MustOnlyOneOf(
    fields = ["username", "email"],
    message = "You must provide either a username or an email only."
)
data class LoginPayload(
    @field:Size(min = 3, max = 50, message = "username must be between 3 and 50 characters.")
    val username: String? = null,

    @field:Email(message = "Please provide a valid email address.")
    val email: String? = null,

    @field:Required("password is required and cannot be blank.")
    @field:Size(min = 8, message = "password must be at least 8 characters long.")
    val password: String? = null,
)
