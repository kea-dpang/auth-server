package kea.dpang.auth.feign

import kea.dpang.auth.base.BaseResponse
import kea.dpang.auth.config.FeignConfig
import kea.dpang.auth.feign.dto.EmailNotificationRequestDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@FeignClient(name = "notification-server", configuration = [FeignConfig::class])
fun interface NotificationServiceFeignClient {

    @PostMapping("/api/notifications/email")
    fun sendEmailVerificationCode(@RequestBody dto: EmailNotificationRequestDto) : ResponseEntity<BaseResponse>
}
