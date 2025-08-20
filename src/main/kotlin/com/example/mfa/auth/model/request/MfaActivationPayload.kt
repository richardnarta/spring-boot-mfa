package com.example.mfa.auth.model.request

import com.example.mfa.core.validation.Required
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Pattern

data class MfaActivationPayload(
    @field:Required("otp_code is required and cannot be blank.")
    @field:Pattern(regexp = "^\\d{6}$", message = "OTP code must be exactly 6 digits.")
    @field:JsonProperty("otp_code") val otpCode: String? = null,
)
