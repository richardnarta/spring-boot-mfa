package com.example.mfa.auth.model.core

data class SessionData(
    val refreshToken: String,
    val requestInfo: RequestInfo
)

data class RequestInfo(
    val ipAddress: String,
    val location: String,
    val device: String,
)
