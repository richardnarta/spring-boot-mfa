package com.example.mfa.auth.model.response

import com.fasterxml.jackson.annotation.JsonProperty

data class OtpRequestResponse(
    @field:JsonProperty("verification_token") val verificationToken: String,
)
