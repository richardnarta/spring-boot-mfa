package com.example.mfa.core.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.security.SignatureException

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val entryPoint: AuthenticationEntryPoint
): OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader("Authorization")
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substring(7)
                if (jwtService.validateAccessToken(token)) {
                    val tokenPayload = jwtService.getPayloadFromToken(token)
                    val auth = UsernamePasswordAuthenticationToken(
                        tokenPayload,
                        null,
                        arrayListOf()
                    )
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            val authException = when (e) {
                is ExpiredJwtException -> CredentialsExpiredException("Token has expired.")
                is SignatureException -> BadCredentialsException("Invalid token signature.")
                is MalformedJwtException -> BadCredentialsException("Token is malformed.")
                else -> InsufficientAuthenticationException("Invalid or missing authentication token.")
            }
            entryPoint.commence(request, response, authException)
        }
    }
}