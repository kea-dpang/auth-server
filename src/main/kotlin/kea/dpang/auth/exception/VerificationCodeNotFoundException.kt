package kea.dpang.auth.exception

class VerificationCodeNotFoundException(email: String) : RuntimeException("해당 이메일에 대한 인증 코드를 찾을 수 없습니다: $email")
