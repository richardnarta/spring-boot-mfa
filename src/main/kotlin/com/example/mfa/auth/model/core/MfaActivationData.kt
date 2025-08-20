package com.example.mfa.auth.model.core

import com.example.mfa.users.model.response.UserResponse

data class MfaActivationData(
    val user: UserResponse,
    val accessToken: String,
    val refreshToken: String,
    val backupCode: List<String>
)
