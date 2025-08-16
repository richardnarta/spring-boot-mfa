package com.example.mfa.users.model.request

import com.example.mfa.core.validation.RequireAtLeastOne
import com.example.mfa.core.validation.ValueOfEnum
import com.example.mfa.users.model.database.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@RequireAtLeastOne(
    fields = ["username", "email"],
    message = "either username or email must be provided."
)
data class NewUserPayload(
    @field:Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
    val username: String? = null,

    @field:Email(message = "please provide a valid email address")
    val email: String? = null,

    @field:NotBlank(message = "password cannot be blank")
    @field:NotNull(message = "password is required")
    @field:Size(min = 8, message = "password must be at least 8 characters long")
    val password: String? = null,

    @field:NotBlank(message = "name cannot be blank")
    @field:NotNull(message = "name is required")
    val name: String? = null,

    @field:NotNull(message = "role is required")
    @field:ValueOfEnum(
        enumClass = Role::class,
        message = "role must be one of: USER, ADMIN"
    )
    val role: String? = null,
)