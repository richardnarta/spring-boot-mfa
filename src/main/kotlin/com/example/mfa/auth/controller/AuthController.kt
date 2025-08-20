package com.example.mfa.auth.controller

import com.example.mfa.auth.model.request.LoginPayload
import com.example.mfa.auth.model.request.LogoutAllPayload
import com.example.mfa.auth.model.request.LogoutPayload
import com.example.mfa.auth.model.request.RefreshPayload
import com.example.mfa.auth.model.response.*
import com.example.mfa.auth.service.AuthService
import com.example.mfa.core.model.BaseResponse
import com.example.mfa.core.model.TokenPayload
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@Tag(name = "Authentication")
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    @Operation(
        summary = "User Login",
        description = "Authenticates a user and returns access and refresh tokens. " +
                "Login can be performed with either a username OR an email, but not both.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Login successful. The response body will vary based on the user's MFA status.",
        content = [Content(
            schema = Schema(oneOf = [BaseLoginResponse::class, BaseLoginOtpResponse::class])
        )]
    )
    @ResponseStatus(HttpStatus.OK)
    fun login(
        @Valid @RequestBody payload: LoginPayload,
        request: HttpServletRequest
    ): BaseResponse<*> {
        val requestInfo = authService.getRequestInfo(request)
        val loginData = authService.login(payload, requestInfo)
        return if (loginData != null) {
            BaseResponse(
                data = LoginResponse(
                    accessToken = loginData.accessToken,
                    refreshToken = loginData.refreshToken,
                    user = loginData.user
                )
            )
        } else {
            BaseResponse(
                data = OtpRequestResponse(
                    verificationToken = authService.createVerificationToken(payload)
                )
            )
        }
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh Access Token",
        description = "Provides a new access and refresh token pair in exchange for a valid, non-revoked refresh token. " +
                "The refresh token provided in the request will be invalidated upon successful use.",
    )
    @ResponseStatus(HttpStatus.OK)
    fun refreshToken(
        @Valid @RequestBody payload: RefreshPayload,
        request: HttpServletRequest
    ): BaseResponse<RefreshResponse> {
        val requestInfo = authService.getRequestInfo(request)
        val loginData = authService.refresh(payload, requestInfo)
        return BaseResponse(
            data = RefreshResponse(
                accessToken = loginData.accessToken,
                refreshToken = loginData.refreshToken
            )
        )
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "User Logout",
        description = "Logs the user out by invalidating their refresh token. " +
                "This endpoint is protected and requires a valid access token in the Authorization header."
    )
    @ResponseStatus(HttpStatus.OK)
    fun logout(
        @Valid @RequestBody payload: LogoutPayload
    ): BaseResponse<Unit> {
        val accessTokenPayload = SecurityContextHolder.getContext().authentication.principal as TokenPayload
        authService.logout(payload, accessTokenPayload)
        return BaseResponse(
            message = "Logout successful."
        )
    }

    @PostMapping("/logout-all")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Logout From All Devices",
        description = "Logs the user out from all active sessions by invalidating all of their refresh tokens. " +
                "This is a protected endpoint that requires a valid access token and forces the user to re-enter their password to confirm their identity."
    )
    @ResponseStatus(HttpStatus.OK)
    fun logoutAll(
        @Valid @RequestBody payload: LogoutAllPayload,
    ): BaseResponse<Unit> {
        val accessTokenPayload = SecurityContextHolder.getContext().authentication.principal as TokenPayload
        authService.logoutAll(payload, accessTokenPayload)
        return BaseResponse(
            message = "Logout successful on all devices."
        )
    }
}