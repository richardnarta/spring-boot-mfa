package com.example.mfa.users.service

import com.example.mfa.core.security.HashEncoder
import com.example.mfa.exception.AlreadyExistsException
import com.example.mfa.users.model.database.UserDto
import com.example.mfa.users.model.request.NewUserPayload
import com.example.mfa.users.model.toRole
import com.example.mfa.users.repository.mongodb.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: HashEncoder
) {
    fun getUsers(pageable: Pageable): Page<UserDto> {
        return userRepository.findAll(pageable)
    }

    fun createUser(payload: NewUserPayload): UserDto {
        if (payload.email != null && userRepository.existsByEmail(payload.email)) {
            throw AlreadyExistsException("User with email ${payload.email} already exists")
        }
        if (payload.username != null && userRepository.existsByUsername(payload.username)) {
            throw AlreadyExistsException("User with username ${payload.username} already exists")
        }

        val newUser = UserDto(
            email = payload.email.toString(),
            username = payload.username.toString(),
            hashedPassword = passwordEncoder.encode(payload.password.toString()),
            name = payload.name.toString(),
            role = payload.role.toRole()
        )

        return userRepository.save(newUser)
    }
}