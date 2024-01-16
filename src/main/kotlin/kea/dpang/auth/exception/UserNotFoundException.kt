package kea.dpang.auth.exception

class UserNotFoundException(email: String) : RuntimeException("해당 사용자를 찾을 수 없습니다: $email")
