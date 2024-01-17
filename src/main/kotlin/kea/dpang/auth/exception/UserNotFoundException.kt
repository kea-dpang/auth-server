package kea.dpang.auth.exception

class UserNotFoundException : RuntimeException {
    constructor(email: String) : super("해당 이메일을 가진 사용자를 찾을 수 없습니다: $email")
    constructor(identifier: Long) : super("해당 식별자를 가진 사용자를 찾을 수 없습니다: $identifier")
}
