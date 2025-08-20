package com.example.mfa.auth.model.response

import com.fasterxml.jackson.annotation.JsonProperty

data class MfaSetupResponse(
    @field:JsonProperty("mfa_uri") val uri: String
)
