package com.example.mfa.core.docs

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
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
class OpenApiConfig {
    @Bean
    fun openApiCustomizer(): OpenApiCustomizer {
        return OpenApiCustomizer { openApi: OpenAPI ->
            val desiredPathOrder = listOf(
                "/auth/login",
                "/auth/refresh",
                "/auth/logout",
                "/auth/logout-all",
                "/auth/mfa/totp",
                "/auth/mfa/totp/activation",
                "/auth/mfa/totp/verification",
                "/auth/mfa/backup-code/verification"
            )
            val existingPaths = openApi.paths ?: Paths()
            val orderedPaths = Paths()

            desiredPathOrder.forEach { pathKey ->
                if (existingPaths.containsKey(pathKey)) {
                    orderedPaths.addPathItem(pathKey, existingPaths[pathKey])
                }
            }
            existingPaths.forEach { (pathKey, pathItem) ->
                if (!orderedPaths.containsKey(pathKey)) {
                    orderedPaths.addPathItem(pathKey, pathItem)
                }
            }
            openApi.paths = orderedPaths

            val methodOrder = listOf(
                PathItem.HttpMethod.GET,
                PathItem.HttpMethod.POST,
                PathItem.HttpMethod.PUT,
                PathItem.HttpMethod.PATCH,
                PathItem.HttpMethod.DELETE
            )

            openApi.paths.values.forEach { pathItem ->
                val operations = pathItem.readOperationsMap()
                val sortedOperations = LinkedHashMap<PathItem.HttpMethod, Operation>()

                methodOrder.forEach { method ->
                    if (operations.containsKey(method)) {
                        sortedOperations[method] = operations[method]!!
                    }
                }
                operations.forEach { (method, operation) ->
                    if (!sortedOperations.containsKey(method)) {
                        sortedOperations[method] = operation
                    }
                }

                clearOperations(pathItem)
                sortedOperations.forEach { (method, operation) ->
                    setOperation(pathItem, method, operation)
                }
            }
        }
    }

    private fun clearOperations(pathItem: PathItem) {
        pathItem.get = null
        pathItem.post = null
        pathItem.put = null
        pathItem.delete = null
        pathItem.patch = null
        pathItem.head = null
        pathItem.options = null
        pathItem.trace = null
    }

    private fun setOperation(pathItem: PathItem, method: PathItem.HttpMethod, operation: Operation) {
        when (method) {
            PathItem.HttpMethod.GET -> pathItem.get = operation
            PathItem.HttpMethod.POST -> pathItem.post = operation
            PathItem.HttpMethod.PUT -> pathItem.put = operation
            PathItem.HttpMethod.DELETE -> pathItem.delete = operation
            PathItem.HttpMethod.PATCH -> pathItem.patch = operation
            PathItem.HttpMethod.HEAD -> pathItem.head = operation
            PathItem.HttpMethod.OPTIONS -> pathItem.options = operation
            PathItem.HttpMethod.TRACE -> pathItem.trace = operation
        }
    }
}