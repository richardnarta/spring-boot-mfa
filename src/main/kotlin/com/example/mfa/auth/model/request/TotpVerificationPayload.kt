package com.example.mfa.auth.model.request

import com.example.mfa.core.validation.Required
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Pattern

data class TotpVerificationPayload(
    @field:Required("otp_code is required and cannot be blank.")
    @field:Pattern(regexp = "^\\d{6}$", message = "OTP code must be exactly 6 digits.")
    @field:JsonProperty("otp_code") val otpCode: String? = null,

    @field:Required("verification_token is required and cannot be blank.")
    @field:JsonProperty("verification_token") val verificationToken: String? = null,
)
