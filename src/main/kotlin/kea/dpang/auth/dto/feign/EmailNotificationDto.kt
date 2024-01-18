package kea.dpang.auth.dto.feign

data class EmailNotificationDto(
    val to: String,  // 이메일 보내는 대상
    val title: String,  // 이메일 제목
    val body: String  // 이메일 내용
)
