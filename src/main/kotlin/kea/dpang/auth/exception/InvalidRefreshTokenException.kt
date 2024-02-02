package kea.dpang.auth.exception

class InvalidRefreshTokenException(token: String) : RuntimeException("유효하지 않은 리프레시 토큰: $token")