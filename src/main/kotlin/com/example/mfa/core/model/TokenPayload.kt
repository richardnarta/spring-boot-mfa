package com.example.mfa.core.model

import com.example.mfa.users.model.database.Role

data class TokenPayload(
    val userId: String,
    val role: Role,
    val jti: String,
)
