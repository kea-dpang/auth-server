package kea.dpang.auth.exception

class TokenNotFoundException(identifier: Long) : RuntimeException("Token not found for identifier: $identifier")