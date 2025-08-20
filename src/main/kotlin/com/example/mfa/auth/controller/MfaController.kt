package com.example.mfa.auth.controller

import com.example.mfa.auth.model.request.AccRecoveryPayload
import com.example.mfa.auth.model.request.MfaActivationPayload
import com.example.mfa.auth.model.request.MfaRevocationPayload
import com.example.mfa.auth.model.request.TotpVerificationPayload
import com.example.mfa.auth.model.response.LoginResponse
import com.example.mfa.auth.model.response.MfaActivationResponse
import com.example.mfa.auth.model.response.MfaSetupResponse
import com.example.mfa.auth.service.AuthService
import com.example.mfa.auth.service.MfaService
import com.example.mfa.core.model.BaseResponse
import com.example.mfa.core.model.TokenPayload
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@Tag(name = "Authentication")
@RequestMapping("/auth/mfa")
class MfaController(
    private val mfaService: MfaService,
    private val authService: AuthService,
) {
    @PostMapping("/totp")
    @Operation(
        summary = "Initiate new MFA TOTP Setup",
        description = "Generates a corresponding QR code URI for the authenticated user. " +
                "The user should scan this URI with their authenticator app (e.g., Google Authenticator). " +
                "After scanning, the user must call the `/auth/mfa/totp/activation` endpoint with a valid code to complete the setup process."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.OK)
    fun setupTOTP(): BaseResponse<MfaSetupResponse> {
        val accessTokenPayload = SecurityContextHolder.getContext().authentication.principal as TokenPayload
        val qrUri = mfaService.setupTOTP(accessTokenPayload)
        return BaseResponse(
            message = "Please bind your authenticator service and verify the TOTP code within 5 minutes.",
            data = MfaSetupResponse(
                uri = qrUri
            )
        )
    }

    @PostMapping("/totp/activation")
    @Operation(
        summary = "Activate new MFA TOTP setup",
        description = "Verifies a TOTP code provided by the user's authenticator app against the MFA initialization. " +
                "If the code is correct, MFA is permanently enabled for the user's account, all other active sessions are terminated, " +
                "and a new set of tokens and one-time-use backup codes are returned."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.OK)
    fun activateTOTP(
        @Valid @RequestBody payload: MfaActivationPayload,
        request: HttpServletRequest
    ): BaseResponse<MfaActivationResponse> {
        val requestInfo = authService.getRequestInfo(request)
        val accessTokenPayload = SecurityContextHolder.getContext().authentication.principal as TokenPayload
        val activationData = mfaService.activateMfa(
            payload,
            accessTokenPayload,
            requestInfo
        )

        return BaseResponse(
            message = "Successfully activated Multi-factor Authentication on your account.",
            data = MfaActivationResponse(
                accessToken = activationData.accessToken,
                refreshToken = activationData.refreshToken,
                user = activationData.user,
                backupCodes = activationData.backupCode
            )
        )
    }

    @PostMapping("/totp/verification")
    @Operation(
        summary = "Verify TOTP and Complete Login",
        description = "Complete the login process for a user with MFA enabled. " +
                "This process requires the temporary verification token (from the initial login attempt) and the current TOTP code from the user's authenticator app."
    )
    @ResponseStatus(HttpStatus.OK)
    fun verifyTOTP(
        @Valid @RequestBody payload: TotpVerificationPayload,
        request: HttpServletRequest
    ): BaseResponse<LoginResponse> {
        val requestInfo = authService.getRequestInfo(request)
        val loginData = mfaService.verifyTotp(payload, requestInfo)

        return BaseResponse(
            message = "Successfully verified authentication on the user's account.",
            data = LoginResponse(
                accessToken = loginData.accessToken,
                refreshToken = loginData.refreshToken,
                user = loginData.user
            )
        )
    }

    @PostMapping("/backup-code/verification")
    @Operation(
        summary = "Verify Backup Code and Complete Login",
        description = "Completes the login process using a single-use backup code. " +
                "This process requires the temporary verification token (from the initial login attempt) and a valid backup code."
    )
    @ResponseStatus(HttpStatus.OK)
    fun verifyBackupCode(
        @Valid @RequestBody payload: AccRecoveryPayload,
        request: HttpServletRequest
    ): BaseResponse<LoginResponse> {
        val requestInfo = authService.getRequestInfo(request)
        val loginData = mfaService.verifyBackupCode(payload, requestInfo)

        return BaseResponse(
            message = "Successfully verified backup code on your account. It is highly recommended to reconfigure your MFA setup and generate new backup codes.",
            data = LoginResponse(
                accessToken = loginData.accessToken,
                refreshToken = loginData.refreshToken,
                user = loginData.user
            )
        )
    }

    @DeleteMapping("/totp")
    @Operation(
        summary = "Disable MFA TOTP",
        description = "Disables Multi-Factor Authentication for the user's account. This process invalidates the user's authenticator app setup with its backup codes, and terminates all other active sessions. " +
                "The user must re-authenticate with their password to perform this action."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.OK)
    fun revokeMfa(
        @Valid @RequestBody payload: MfaRevocationPayload,
        request: HttpServletRequest
    ): BaseResponse<LoginResponse> {
        val requestInfo = authService.getRequestInfo(request)
        val accessTokenPayload = SecurityContextHolder.getContext().authentication.principal as TokenPayload
        val loginData = mfaService.revokeMfa(payload, accessTokenPayload, requestInfo)

        return BaseResponse(
            message = "Successfully revoked Multi-factor Authentication on your account.",
            data = LoginResponse(
                accessToken = loginData.accessToken,
                refreshToken = loginData.refreshToken,
                user = loginData.user
            )
        )
    }
}