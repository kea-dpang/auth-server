package kea.dpang.auth.dto

data class ResetPasswordRequestDto(
    val email: String,
    val code: String,
    val newPassword: String
)