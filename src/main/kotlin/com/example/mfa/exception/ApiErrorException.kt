package com.example.mfa.exception

import org.springframework.http.HttpStatus

sealed class ApiErrorException(
    val status: HttpStatus,
    override val message: String
) : RuntimeException(message)

class BadRequestException(message: String) :
    ApiErrorException(HttpStatus.BAD_REQUEST, message)

class UnauthorizedException(message: String = "Authentication is required to access this resource.") :
    ApiErrorException(HttpStatus.UNAUTHORIZED, message)

class ForbiddenException(message: String = "You do not have permission to access this resource.") :
    ApiErrorException(HttpStatus.FORBIDDEN, message)

class NotFoundException(message: String) :
    ApiErrorException(HttpStatus.NOT_FOUND, message)

class AlreadyExistsException(message: String) :
    ApiErrorException(HttpStatus.CONFLICT, message)

class TooManyRequestException(message: String) :
    ApiErrorException(HttpStatus.TOO_MANY_REQUESTS, message)

class InternalServerErrorException(message: String = "An unknown error occurred") :
    ApiErrorException(HttpStatus.INTERNAL_SERVER_ERROR, message)