package kea.dpang.auth.dto

import kea.dpang.auth.base.Role

data class Token(
    val role: Role,
    val accessToken: String,
    val refreshToken: String
)
