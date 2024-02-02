package kea.dpang.auth.exception

class TokenNotFoundException(identifier: Long) : RuntimeException("식별자에 해당하는 토큰을 찾을 수 없습니다: $identifier")