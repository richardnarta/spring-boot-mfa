package com.example.mfa.core.docs

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "MFA Authentication API",
        version = "v1.0",
        description = "API documentation for the Multi-Factor Authentication service."
    ),
    tags = [
        Tag(
            name = "Authentication",
            description = "Endpoints for user authentication."
        ),
        Tag(
            name = "Users",
            description = "Endpoints for creating and managing users."
        )
    ]
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT auth description",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    `in` = SecuritySchemeIn.HEADER
)
class OpenApiConfig