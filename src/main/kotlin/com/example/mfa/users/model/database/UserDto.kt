package com.example.mfa.users.model.database

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.*

@Document("users")
data class UserDto(
    @Id
    val id: UUID? = UUID.randomUUID(),

    @Indexed(unique = true, sparse = true)
    val email: String?,
    @Indexed(unique = true, sparse = true)
    val username: String?,

    val hashedPassword: String,

    val name: String,
    val role: Role = Role.USER,
    val createdAt: Instant? = Instant.now(),
    val updatedAt: Instant? = null,
    val deletedAt: Instant? = null,
)

enum class Role {
    ADMIN,
    USER
}
