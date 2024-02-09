package kea.dpang.auth.dto

data class ChangePasswordRequestDto(
    val oldPassword: String,
    val newPassword: String
)