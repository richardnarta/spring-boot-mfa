package com.example.mfa.users.model.request

import com.example.mfa.core.validation.RequireAtLeastOne
import com.example.mfa.core.validation.Required
import com.example.mfa.core.validation.ValueOfEnum
import com.example.mfa.users.model.database.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

@RequireAtLeastOne(
    fields = ["username", "email"],
    message = "Either username or email must be provided."
)
data class NewUserPayload(
    @field:Size(min = 3, max = 50, message = "username must be between 3 and 50 characters.")
    val username: String? = null,

    @field:Email(message = "Please provide a valid email address.")
    val email: String? = null,

    @field:Required("password is required and cannot be blank.")
    @field:Size(min = 8, message = "password must be at least 8 characters long.")
    val password: String? = null,

    @field:Required("name is required and cannot be blank.")
    val name: String? = null,

    @field:Required("role is required and cannot be blank.")
    @field:ValueOfEnum(
        enumClass = Role::class,
        message = "role must be one of: USER, ADMIN"
    )
    val role: String? = null,
)