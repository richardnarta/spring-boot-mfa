package com.example.mfa.auth.service

import com.example.mfa.auth.model.core.LoginData
import com.example.mfa.auth.model.core.MfaActivationData
import com.example.mfa.auth.model.core.RequestInfo
import com.example.mfa.auth.model.request.AccRecoveryPayload
import com.example.mfa.auth.model.request.MfaActivationPayload
import com.example.mfa.auth.model.request.MfaRevocationPayload
import com.example.mfa.auth.model.request.TotpVerificationPayload
import com.example.mfa.core.model.TokenPayload
import com.example.mfa.core.redis.RedisAuthRepository
import com.example.mfa.core.security.Encryptor
import com.example.mfa.core.security.HashEncoder
import com.example.mfa.core.security.JwtService
import com.example.mfa.exception.BadRequestException
import com.example.mfa.exception.ForbiddenException
import com.example.mfa.exception.InternalServerErrorException
import com.example.mfa.exception.NotFoundException
import com.example.mfa.exception.TooManyRequestException
import com.example.mfa.exception.UnauthorizedException
import com.example.mfa.users.model.core.UpdateUserData
import com.example.mfa.users.model.database.MfaDetail
import com.example.mfa.users.model.database.MfaStatus
import com.example.mfa.users.model.database.UserDto
import com.example.mfa.users.service.SharedUserService
import org.apache.commons.codec.binary.Base32
import org.springframework.stereotype.Service
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

@Service
class MfaService(
    private val userService: SharedUserService,
    private val authRedisRepository: RedisAuthRepository,
    private val encryptor: Encryptor,
    private val authService: AuthService,
    private val hashEncoder: HashEncoder,
    private val jwtService: JwtService
) {
    fun setupTOTP(accessTokenPayload: TokenPayload): String {
        val secret = generateBase32Secret()

        val user = userService.getUserById(accessTokenPayload.userId).orElse(null) ?: throw
            NotFoundException("User with ID ${accessTokenPayload.userId} is not found.")

        if (user.mfaStatus == MfaStatus.ENABLED)
            throw BadRequestException("MFA is enabled.")

        val redisKey = "mfa_totp_setup:${user.id.toString()}"
        val issuer = "CloudMFA"
        val accountName = user.name

        val otpAuthUri = "otpauth://totp/${issuer}:${accountName}?secret=${secret}&issuer=${issuer}"

        authRedisRepository.set(redisKey, encryptor.encrypt(secret), 5L * 60L * 1000L)

        return otpAuthUri
    }

    fun activateMfa(
        payload: MfaActivationPayload,
        accessTokenPayload: TokenPayload,
        requestInfo: RequestInfo
    ): MfaActivationData {
        val user = userService.getUserById(accessTokenPayload.userId).orElse(null) ?: throw
            NotFoundException("User with ID ${accessTokenPayload.userId} is not found.")

        val redisKey = "mfa_totp_setup:${user.id.toString()}"
        val encryptedSecret = authRedisRepository.get(redisKey, String::class.java) ?: throw
            ForbiddenException("Your session to activate mfa is expired. Please try again.")

        val secret = encryptor.decrypt(encryptedSecret)

        val now = Instant.now()

        val currentCode = generateTotp(secret, now)
        val previousCode = generateTotp(secret, now.minusSeconds(30))

        if (payload.otpCode == currentCode || payload.otpCode == previousCode) {
            val tokens = authRedisRepository.sMembers(
                "sessions-user:${user.id.toString()}",
                String::class.java
            )

            tokens?.map { jti ->
                authRedisRepository.del("session:$jti")
            }
            authRedisRepository.del("sessions-user:${user.id.toString()}")

            val backupCode = generateBackupCodes()

            val updatedUser = userService.updateUserData(
                user.id.toString(),
                UpdateUserData(
                    mfaStatus = MfaStatus.ENABLED,
                    mfaDetail =MfaDetail(
                        encryptedSecret = encryptedSecret,
                        backupCodes = hashBackupCodes(backupCode).toSet()
                    )
                )
            ) ?: throw InternalServerErrorException()

            authRedisRepository.del("mfa_totp_setup:${updatedUser.id.toString()}")
            val newUserToken = authService.generateLoginData(updatedUser, requestInfo)

            return MfaActivationData(
                user = newUserToken.user,
                accessToken = newUserToken.accessToken,
                refreshToken = newUserToken.refreshToken,
                backupCode = backupCode
            )
        } else {
            throw UnauthorizedException("OTP code is invalid.")
        }
    }

    fun verifyTotp(payload: TotpVerificationPayload, requestInfo: RequestInfo): LoginData {
        val user = verifyVerificationToken(payload.verificationToken.toString())

        val secret = encryptor.decrypt(
            user.mfaDetail?.encryptedSecret ?:
            throw InternalServerErrorException("An error occurred when verifying OTP.")
        )

        val now = Instant.now()

        val currentCode = generateTotp(secret, now)
        val previousCode = generateTotp(secret, now.minusSeconds(30))

        if (payload.otpCode == currentCode || payload.otpCode == previousCode) {
            if (authRedisRepository.sCard("sessions-user:${user.id}") >= 5) throw
                TooManyRequestException("Session limit exceeded. Please log out from another device.")

            return authService.generateLoginData(user, requestInfo)
        } else {
            throw UnauthorizedException("OTP code is invalid.")
        }
    }

    fun verifyBackupCode(payload: AccRecoveryPayload, requestInfo: RequestInfo): LoginData {
        val user = verifyVerificationToken(payload.verificationToken.toString())

        val matchingCodeHash = user.mfaDetail?.backupCodes?.find { storedHash ->
            hashEncoder.matches(payload.backupCode.toString(), storedHash)
        } ?: throw UnauthorizedException("Invalid backup code.")

        val updatedBackupCodes = user.mfaDetail.backupCodes.minus(matchingCodeHash)
        val updatedMfaDetail = user.mfaDetail.copy(backupCodes = updatedBackupCodes)
        userService.updateUserData(user.id.toString(), UpdateUserData(mfaDetail = updatedMfaDetail))

        return authService.generateLoginData(user, requestInfo)
    }

    fun revokeMfa(payload: MfaRevocationPayload, accessTokenPayload: TokenPayload, requestInfo: RequestInfo): LoginData {
        val user = userService.getUserById(accessTokenPayload.userId).orElse(null) ?: throw
        NotFoundException("User with ID ${accessTokenPayload.userId} is not found.")

        if (user.mfaStatus == MfaStatus.DISABLED)
            throw BadRequestException("MFA is disabled.")

        if (!hashEncoder.matches(
                payload.password.toString(),
                user.hashedPassword
            )
        ) {
            throw UnauthorizedException("Passwords do not match.")
        }

        val tokens = authRedisRepository.sMembers(
            "sessions-user:${user.id.toString()}",
            String::class.java
        )

        tokens?.map { jti ->
            authRedisRepository.del("session:$jti")
        }
        authRedisRepository.del("sessions-user:${user.id.toString()}")

        userService.updateUserData(
            user.id.toString(),
            UpdateUserData(
                mfaStatus = MfaStatus.DISABLED,
                mfaDetail = null
            )
        )

        return authService.generateLoginData(user, requestInfo)
    }

    private fun verifyVerificationToken(token: String): UserDto {
        if (!jwtService.validateVerificationToken(token)) throw
        UnauthorizedException("Verification token is invalid.")

        val verificationTokenPayload = jwtService.getPayloadFromToken(
            token
        ) ?: throw UnauthorizedException("Verification token is invalid.")

        return userService.getUserById(verificationTokenPayload.userId).orElse(null) ?: throw
            NotFoundException("User with ID ${verificationTokenPayload.userId} is not found.")
    }

    private fun generateTotp(
        base32Secret: String,
        time: Instant,
        timeStepSeconds: Int = 30,
        numDigits: Int = 6
    ): String {
        val timeStep = time.epochSecond / timeStepSeconds

        val keyBytes = Base32().decode(base32Secret)

        val timeStepBytes = ByteBuffer.allocate(8).putLong(timeStep).array()

        val hmac = Mac.getInstance("HmacSHA1")
        val keySpec = SecretKeySpec(keyBytes, "HmacSHA1")
        hmac.init(keySpec)
        val hash = hmac.doFinal(timeStepBytes)

        val offset = hash.last().toInt() and 0x0F
        val truncatedHash = ByteBuffer.wrap(hash, offset, 4).int
        val positiveInt = truncatedHash and 0x7FFFFFFF

        val divisor = 10.0.pow(numDigits).toInt()
        val otp = positiveInt % divisor
        return otp.toString().padStart(numDigits, '0')
    }

    private fun generateBase32Secret(): String {
        val bytes = ByteArray(20)
        SecureRandom().nextBytes(bytes)
        return Base32().encodeToString(bytes)
    }

    private fun generateBackupCodes(
        count: Int = 8,
        length: Int = 8,
        groupSize: Int = 4
    ): List<String> {
        val allowedChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val secureRandom = SecureRandom()

        val codes = mutableSetOf<String>()

        while (codes.size < count) {
            val codeBuilder = StringBuilder(length)

            repeat(length) {
                val randomIndex = secureRandom.nextInt(allowedChars.length)
                codeBuilder.append(allowedChars[randomIndex])
            }

            for (i in (length - groupSize) downTo groupSize step groupSize) {
                codeBuilder.insert(i, '-')
            }

            codes.add(codeBuilder.toString())
        }

        return codes.toList()
    }

    private fun hashBackupCodes(
        codes: List<String>
    ): List<String> {
        val hashedCodes = mutableSetOf<String>()

        codes.map {
            hashedCodes.add(
                hashEncoder.encode(it)
            )
        }

        return hashedCodes.toList()
    }
}