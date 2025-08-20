package com.example.mfa.auth.service

import com.example.mfa.auth.model.core.LoginData
import com.example.mfa.auth.model.core.RequestInfo
import com.example.mfa.auth.model.core.SessionData
import com.example.mfa.auth.model.request.LoginPayload
import com.example.mfa.auth.model.request.LogoutAllPayload
import com.example.mfa.auth.model.request.LogoutPayload
import com.example.mfa.auth.model.request.RefreshPayload
import com.example.mfa.auth.util.RequestUtil.extractIpAddress
import com.example.mfa.auth.util.RequestUtil.parseUserAgent
import com.example.mfa.core.model.TokenPayload
import com.example.mfa.core.redis.RedisAuthRepository
import com.example.mfa.core.security.HashEncoder
import com.example.mfa.core.security.JwtService
import com.example.mfa.exception.*
import com.example.mfa.users.model.database.MfaStatus
import com.example.mfa.users.model.database.UserDto
import com.example.mfa.users.model.toUser
import com.example.mfa.users.service.SharedUserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtService: JwtService,
    private val hashEncoder: HashEncoder,
    private val authRedisRepository: RedisAuthRepository,
    private val userService: SharedUserService,
    private val geoLocationService: GeoLocationService
) {
    fun login(payload: LoginPayload, requestInfo: RequestInfo): LoginData? {
        val user = verifyUserIdentity(
            payload.email,
            payload.username,
            payload.password
        )

        if (user.mfaStatus == MfaStatus.ENABLED) return null

        if (authRedisRepository.sCard("sessions-user:${user.id}") >= 5) throw
            TooManyRequestException("Session limit exceeded. Please log out from another device.")

        return generateLoginData(user, requestInfo)
    }

    fun refresh(payload: RefreshPayload, requestInfo: RequestInfo): LoginData {
        val refreshToken = payload.refreshToken.toString()

        if (!jwtService.validateRefreshToken(
                refreshToken
            )
        ) {
            throw UnauthorizedException("Refresh token is invalid.")
        }

        val refreshTokenPayload = jwtService.getPayloadFromToken(refreshToken) ?: throw
            UnauthorizedException("Invalid refresh token.")

        val user = userService.getUserById(refreshTokenPayload.userId).orElse(null) ?: throw
            NotFoundException("User with ID ${refreshTokenPayload.userId} is not found.")

        revokeRefreshToken(user, refreshTokenPayload)

        return generateLoginData(user, requestInfo)
    }

    fun logout(payload: LogoutPayload, accessTokenPayload: TokenPayload) {
        val refreshToken = payload.refreshToken.toString()

        if (!jwtService.validateRefreshToken(
                refreshToken
            )
        ) {
            return
        }

        val refreshTokenPayload = jwtService.getPayloadFromToken(refreshToken) ?: throw
            UnauthorizedException("Invalid refresh token.")

        val user = userService.getUserById(refreshTokenPayload.userId).orElse(null) ?: throw
            NotFoundException("User with ID ${accessTokenPayload.userId} is not found.")

        if (refreshTokenPayload.userId != accessTokenPayload.userId) {
            throw ForbiddenException("Token's ownership does not match.")
        } else {
            revokeRefreshToken(user, refreshTokenPayload)
        }
    }

    fun logoutAll(payload: LogoutAllPayload, accessTokenPayload: TokenPayload) {
        val user = verifyUserIdentity(
            payload.email,
            payload.username,
            payload.password
        )

        if (user.id.toString() != accessTokenPayload.userId) {
            throw ForbiddenException("Token's ownership does not match.")
        } else {
            val tokens = authRedisRepository.sMembers(
                "sessions-user:${user.id.toString()}",
                String::class.java
            )

            tokens?.map { jti ->
                authRedisRepository.del("session:$jti")
            }
            authRedisRepository.del("sessions-user:${user.id.toString()}")
        }
    }

    fun createVerificationToken(payload: LoginPayload): String {
        val user = verifyUserIdentity(
            payload.email,
            payload.username,
            payload.password
        )

        return jwtService.generateVerificationToken(user.id.toString(), user.role)
    }

    private fun verifyUserIdentity(email: String?, username: String?, password: String?): UserDto {
        val user = if (!email.isNullOrEmpty()) {
            userService.getUserByEmail(email) ?: throw NotFoundException(
                "Email $email not found."
            )
        } else if (!username.isNullOrEmpty()) {
            userService.getUserByUsername(username) ?: throw NotFoundException(
                "Username $username not found."
            )
        } else {
            throw NotFoundException("User not found. Please try again later.")
        }

        if (!hashEncoder.matches(
                password.toString(),
                user.hashedPassword
            )
        ) {
            throw UnauthorizedException("Passwords do not match.")
        }

        return user
    }

    fun generateLoginData(
        user: UserDto,
        requestInfo: RequestInfo
    ): LoginData {
        val newAccessToken = jwtService.generateAccessToken(user.id.toString(), user.role)
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toString(), user.role)

        val refreshTokenPayload = jwtService.getPayloadFromToken(newRefreshToken) ?: throw
            InternalServerErrorException()

        authRedisRepository.sAdd(
            "sessions-user:${user.id}",
            refreshTokenPayload.jti
        )

        authRedisRepository.set(
            "session:${refreshTokenPayload.jti}",
            SessionData(
                refreshToken = newRefreshToken,
                requestInfo = requestInfo
            ),
            jwtService.refreshTokenValidityMs
        )

        return LoginData(
            user = user.toUser(),
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    private fun revokeRefreshToken(user: UserDto, refreshTokenPayload: TokenPayload) {
        authRedisRepository.sRem("sessions-user:${user.id}", refreshTokenPayload.jti)
        authRedisRepository.del("session:${refreshTokenPayload.jti}")
    }

    fun getRequestInfo(request: HttpServletRequest): RequestInfo {
        val ipAddress = extractIpAddress(request)
        val device = parseUserAgent(request.getHeader("User-Agent"))
        val location = geoLocationService.getLocation(ipAddress)

        return RequestInfo(
            ipAddress = ipAddress,
            location = location,
            device = device
        )
    }
}