package kea.dpang.auth.dto

data class DeleteUserRequestDto(
    val password: String, // 비밀번호
    val reason: String, // 탈퇴 사유
    val message: String // 남기실 말씀
)
