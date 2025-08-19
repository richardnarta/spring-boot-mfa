package com.example.mfa.auth.model.core

import com.example.mfa.users.model.response.User

data class LoginData(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
)
