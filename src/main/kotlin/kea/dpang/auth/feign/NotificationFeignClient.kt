package kea.dpang.auth.feign

import kea.dpang.auth.dto.feign.EmailNotificationDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@FeignClient(name = "notificationService", url = "\${services.notification.url}")
fun interface NotificationFeignClient {

    @PostMapping("/api/notifications/email")
    fun sendEmailVerificationCode(@RequestBody dto: EmailNotificationDto) : ResponseEntity<Any>
}
