package kea.dpang.auth.exception

class InvalidPasswordException(email: String) : RuntimeException("비밀번호가 일치하지 않습니다: $email")
