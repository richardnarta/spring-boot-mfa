package com.example.mfa.auth.model.response

import com.example.mfa.users.model.response.UserResponse
import com.fasterxml.jackson.annotation.JsonProperty

data class MfaActivationResponse(
    @field:JsonProperty("new_access_token") val accessToken: String,
    @field:JsonProperty("new_refresh_token") val refreshToken: String,
    @field:JsonProperty("new_user_info") val user: UserResponse,
    @field:JsonProperty("backup_codes") val backupCodes: List<String>,
)
