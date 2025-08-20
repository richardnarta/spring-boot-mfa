package com.example.mfa.users.service

import com.example.mfa.users.model.core.UpdateUserData
import com.example.mfa.users.model.database.MfaDetail
import com.example.mfa.users.model.database.MfaStatus
import com.example.mfa.users.model.database.UserDto
import com.example.mfa.users.repository.mongodb.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Optional
import java.util.UUID

@Service
class SharedUserService(
    private val userRepository: UserRepository,
) {
    fun getUserByEmail(email: String): UserDto? {
        return userRepository.findByEmail(email)
    }

    fun getUserByUsername(username: String): UserDto? {
        return userRepository.findByUsername(username)
    }

    fun getUserById(id: String): Optional<UserDto?> {
        return userRepository.findById(UUID.fromString(id))
    }

    fun updateUserData(userId: String, data: UpdateUserData): UserDto? {
        val userToUpdate = getUserById(userId).orElse(null)

        return if (userToUpdate != null) {
            val updatedUser = userToUpdate.copy(
                email = data.email ?: userToUpdate.email,
                username = data.username ?: userToUpdate.username,
                hashedPassword = data.hashedPassword ?: userToUpdate.hashedPassword,
                name = data.name ?: userToUpdate.name,
                role = data.role ?: userToUpdate.role,
                mfaStatus = data.mfaStatus ?: userToUpdate.mfaStatus,
                mfaDetail = data.mfaDetail ?: userToUpdate.mfaDetail,
                updatedAt = Instant.now()
            )

            userRepository.save(updatedUser)
        } else {
            null
        }
    }
}