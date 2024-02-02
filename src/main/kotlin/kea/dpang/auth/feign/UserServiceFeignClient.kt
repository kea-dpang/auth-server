package kea.dpang.auth.feign

import kea.dpang.auth.base.BaseResponse
import kea.dpang.auth.feign.dto.RegisterUserRequestDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "user-server")
fun interface UserServiceFeignClient {

    @PostMapping("/api/users/register")
    fun registerUser(@RequestBody dto: RegisterUserRequestDto) : ResponseEntity<BaseResponse>
}