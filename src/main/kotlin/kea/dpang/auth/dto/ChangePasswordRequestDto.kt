package kea.dpang.auth.dto

data class ChangePasswordRequestDto(
    val email: String,
    val oldPassword: String,
    val newPassword: String
)