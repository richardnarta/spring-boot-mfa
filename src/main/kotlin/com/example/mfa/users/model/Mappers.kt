package com.example.mfa.users.model

import com.example.mfa.users.model.database.MfaStatus
import com.example.mfa.users.model.database.Role
import com.example.mfa.users.model.database.UserDto
import com.example.mfa.users.model.response.UserResponse

fun UserDto.toUser(): UserResponse {
    return UserResponse(
        id = id.toString(),
        name = name,
        role = role.name,
        email = email.toString(),
        username = username.toString(),
        mfaStatus = mfaStatus.toBoolean(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}

fun String?.toRole(): Role {
    return when (this) {
        "ADMIN" -> Role.ADMIN
        else -> Role.USER
    }
}

fun MfaStatus.toBoolean(): Boolean {
    return when (this) {
        MfaStatus.ENABLED -> true
        else -> false
    }
}