package com.example.mfa.users.model.core

import com.example.mfa.users.model.database.MfaDetail
import com.example.mfa.users.model.database.MfaStatus
import com.example.mfa.users.model.database.Role

data class UpdateUserData(
    val email: String? = null,
    val username: String? = null,
    val hashedPassword: String? = null,
    val name: String? = null,
    val role: Role? = null,
    val mfaStatus: MfaStatus? = null,
    val mfaDetail: MfaDetail? = null,
)
