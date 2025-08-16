package com.example.mfa.users.controller

import com.example.mfa.core.model.BaseResponse
import com.example.mfa.core.model.PaginatedResponse
import com.example.mfa.users.model.request.NewUserPayload
import com.example.mfa.users.model.response.User
import com.example.mfa.users.model.toUser
import com.example.mfa.users.service.UserService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*

@RestController
@Tag(name = "Users", description = "Endpoints for managing users")
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping
    fun getUsers(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "limit", defaultValue = "10") limit: Int,
    ): BaseResponse<PaginatedResponse<User>> {
        val pagedUsers = userService.getUsers(
            PageRequest.of(
                page,
                limit
            )
        )

        val data = PaginatedResponse(
            items = pagedUsers.content.map { it.toUser() },
            totalItems = pagedUsers.totalElements,
            totalPages = pagedUsers.totalPages,
            currentPage = pagedUsers.number
        )

        return BaseResponse(
            data = data,
        )
    }

    @PostMapping
    fun postUser(
        @Valid @RequestBody body: NewUserPayload
    ): BaseResponse<User> {
        val newUser = userService.createUser(body)

        return BaseResponse(
            data = newUser.toUser()
        )
    }
}