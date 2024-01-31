package kea.dpang.auth.exception

class EmailAlreadyExistsException(email: String) : RuntimeException("이미 존재하는 이메일입니다. (email: $email)")