package com.example.mfa.exception

import com.example.mfa.core.model.BaseResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationError(e: MethodArgumentNotValidException): ResponseEntity<BaseResponse<List<String>>> {
        val errors = e.bindingResult.allErrors.map { error ->
            error.defaultMessage ?: "invalid value"
        }

        val errorResponse = BaseResponse(
            error = true,
            message = "Validation failed, please check the errors.",
            data = errors
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(ApiErrorException::class)
    fun handleApiErrorException(e: ApiErrorException): ResponseEntity<BaseResponse<Unit>> {
        val errorResponse = BaseResponse<Unit>(
            error = true,
            message = e.message
        )
        return ResponseEntity(errorResponse, e.status)
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<BaseResponse<Unit>> {
        val errorResponse = BaseResponse<Unit>(
            error = true,
            message = "An unexpected server error occurred."
        )
        return ResponseEntity.internalServerError().body(errorResponse)
    }
}