package com.example.mfa.users.model

import com.example.mfa.users.model.database.Role
import com.example.mfa.users.model.database.UserDto
import com.example.mfa.users.model.response.User

fun UserDto.toUser(): User {
    return User(
        id = id.toString(),
        name = name,
        role = role.getRole(),
        email = email.toString(),
        username = username.toString(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}

fun Role.getRole(): String {
    return when (this) {
        Role.ADMIN -> "admin"
        Role.USER -> "user"
    }
}

fun String?.toRole(): Role {
    return when (this) {
        "admin" -> Role.ADMIN
        else -> Role.USER
    }
}