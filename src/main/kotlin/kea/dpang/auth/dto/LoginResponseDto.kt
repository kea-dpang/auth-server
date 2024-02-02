package kea.dpang.auth.dto

data class LoginResponseDto(
    val userIdx: Long,
    val token: Token
)