package kea.dpang.auth.exception

class InvalidRefreshTokenException(token: String) : RuntimeException("Invalid refresh token: $token")
