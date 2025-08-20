package com.example.mfa.auth.model.request

import com.example.mfa.core.validation.Required
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Pattern

data class AccRecoveryPayload(
    @field:Required("backup_code is required and cannot be blank.")
    @field:Pattern(
        regexp = "^([A-Z2-9]{4}-)[A-Z2-9]{4}$",
        message = "Backup code must be in the format XXXX-XXXX."
    )
    @field:JsonProperty("backup_code") val backupCode: String? = null,

    @field:Required("verification_token is required and cannot be blank.")
    @field:JsonProperty("verification_token") val verificationToken: String? = null,
)
