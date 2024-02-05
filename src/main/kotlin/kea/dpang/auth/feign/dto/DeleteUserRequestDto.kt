package kea.dpang.auth.feign.dto

data class DeleteUserRequestDto (
    val reason: List<String>, // 탈퇴 사유
    val message: String // 남기실 말씀
)