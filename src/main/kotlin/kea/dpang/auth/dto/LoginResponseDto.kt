package kea.dpang.auth.dto

data class LoginResponseDto(
    val userIdx: Long,
    val userName: String,
    val token: Token
)