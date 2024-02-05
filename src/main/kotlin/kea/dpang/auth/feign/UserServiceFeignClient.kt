package kea.dpang.auth.feign

import kea.dpang.auth.base.BaseResponse
import kea.dpang.auth.feign.dto.DeleteUserRequestDto
import kea.dpang.auth.feign.dto.RegisterUserRequestDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "user-server")
interface UserServiceFeignClient {

    @PostMapping("/api/users/register")
    fun registerUser(@RequestBody dto: RegisterUserRequestDto): ResponseEntity<BaseResponse>

    @DeleteMapping("/api/users/{userId}")
    fun deleteUserInfo(@PathVariable userId: Long, @RequestBody dto: DeleteUserRequestDto): ResponseEntity<BaseResponse>
}