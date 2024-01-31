package kea.dpang.auth.dto

data class Token(
    val accessToken: String,
    val refreshToken: String
)
