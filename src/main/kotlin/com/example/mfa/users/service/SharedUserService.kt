package com.example.mfa.users.service

import com.example.mfa.users.model.database.UserDto
import com.example.mfa.users.repository.mongodb.UserRepository
import org.springframework.stereotype.Service
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
}