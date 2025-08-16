package com.example.mfa.users.repository.mongodb

import com.example.mfa.users.model.database.UserDto
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : MongoRepository<UserDto, UUID> {
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
}