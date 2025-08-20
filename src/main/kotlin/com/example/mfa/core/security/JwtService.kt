package com.example.mfa.core.security

import com.example.mfa.auth.model.core.SessionData
import com.example.mfa.core.model.TokenPayload
import com.example.mfa.core.redis.RedisAuthRepository
import com.example.mfa.users.model.database.Role
import com.example.mfa.users.model.toRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService(
    @param:Value("\${jwt.secret.key}") private val jwtSecret: String,
    @param:Value("\${jwt.access.token.validity.minutes}") private val accessTokenLifetime: String,
    @param:Value("\${jwt.refresh.token.validity.days}") private val refreshTokenLifetime: String,
    private val authRedisRepository: RedisAuthRepository
) {
    private val secretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    val accessTokenValidityMs = try {
        accessTokenLifetime.toLong() * 60L * 1000L
    } catch (_: Exception) {
        15L * 60L * 1000L
    }
    val refreshTokenValidityMs = try {
        refreshTokenLifetime.toLong() * 24 * 60 * 60 * 1000L
    } catch (_: Exception) {
        30L * 24 * 60 * 60 * 1000L
    }

    private fun generateToken(
        userId: String,
        role: Role,
        type: String,
        expiry: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        val jti = UUID.randomUUID().toString()

        return Jwts.builder()
            .subject(userId)
            .claim("type", type)
            .claim("role", role.name)
            .claim("jti", jti)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateAccessToken(userId: String, role: Role): String {
        return generateToken(userId, role, "access", accessTokenValidityMs)
    }

    fun generateRefreshToken(userId: String, role: Role): String {
        return generateToken(userId, role, "refresh", refreshTokenValidityMs)
    }

    fun generateVerificationToken(userId: String, role: Role): String {
        return generateToken(userId, role, "verification", 5L * 60L * 1000L)
    }

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token)
        val tokenType = claims?.get("type") as? String ?: return false
        return tokenType == "access"
    }

    fun validateRefreshToken(token: String): Boolean {
        return try {
            val claims = parseAllClaims(token)
            authRedisRepository.get(
                "session:${claims?.get("jti")}",
                SessionData::class.java
            ) ?: return false
            val tokenType = claims?.get("type") as? String ?: return false
            tokenType == "refresh"
        } catch (_: Exception) { false }
    }

    fun validateVerificationToken(token: String): Boolean {
        return try {
            val claims = parseAllClaims(token)
            val tokenType = claims?.get("type") as? String ?: return false
            tokenType == "verification"
        } catch (_: Exception) { false }
    }

    fun getPayloadFromToken(token: String): TokenPayload? {
        return try {
            val claims = parseAllClaims(token) ?: return null
            TokenPayload(
                userId = claims.subject.toString(),
                role = (claims["role"] as? String).toRole(),
                jti = (claims["jti"] as? String).toString()
            )
        } catch (_: Exception) { null }
    }

    private fun parseAllClaims(token: String): Claims? {
        val rawToken = if(token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")
        } else token
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(rawToken)
            .payload
    }
}