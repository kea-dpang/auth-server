package kea.dpang.auth.exception

class InvalidVerificationCodeException(email: String) : RuntimeException("입력받은 인증 코드가 일치하지 않습니다: $email")
