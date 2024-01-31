package kea.dpang.auth.dto

data class LoginRequestDto(
    val email: String,
    val password: String
)