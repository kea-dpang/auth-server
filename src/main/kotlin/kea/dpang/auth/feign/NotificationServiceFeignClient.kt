package kea.dpang.auth.feign

import feign.Headers
import kea.dpang.auth.base.BaseResponse
import kea.dpang.auth.feign.dto.EmailNotificationRequestDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@Headers("X-DPANG-CLIENT-ROLE: SYSTEM")
@FeignClient(name = "notification-server")
fun interface NotificationServiceFeignClient {

    @Headers("X-DPANG-CLIENT-ROLE: SYSTEM")
    @PostMapping("/api/notifications/email")
    fun sendEmailVerificationCode(@RequestBody dto: EmailNotificationRequestDto) : ResponseEntity<BaseResponse>
}
