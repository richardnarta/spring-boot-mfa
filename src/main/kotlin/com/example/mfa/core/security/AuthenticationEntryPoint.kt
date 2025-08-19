package com.example.mfa.core.security

import com.example.mfa.core.model.BaseResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
class AuthenticationEntryPoint : org.springframework.security.web.AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: org.springframework.security.core.AuthenticationException?
    ) {
        val message = authException?.message ?: "Authentication is required to access this resource."

        val errorResponse = BaseResponse<Unit>(
            error = true,
            message = message
        )

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        ObjectMapper().writeValue(response.outputStream, errorResponse)
    }
}