package kea.dpang.auth.dto

data class LoginResponseDto(
    val user: UserInfo,
    val token: Token
)