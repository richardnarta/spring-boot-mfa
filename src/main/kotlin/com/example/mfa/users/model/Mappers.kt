package com.example.mfa.users.model

import com.example.mfa.users.model.database.Role
import com.example.mfa.users.model.database.UserDto
import com.example.mfa.users.model.response.User

fun UserDto.toUser(): User {
    return User(
        id = id.toString(),
        name = name,
        role = role.name,
        email = email.toString(),
        username = username.toString(),
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