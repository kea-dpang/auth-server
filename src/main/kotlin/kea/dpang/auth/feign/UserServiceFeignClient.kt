package kea.dpang.auth.feign

import kea.dpang.auth.base.BaseResponse
import kea.dpang.auth.base.SuccessResponse
import kea.dpang.auth.feign.dto.DeleteUserRequestDto
import kea.dpang.auth.feign.dto.RegisterUserRequestDto
import kea.dpang.auth.feign.dto.UserDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@FeignClient(name = "user-server")
interface UserServiceFeignClient {

    @PostMapping("/api/users/register")
    fun registerUser(@RequestBody dto: RegisterUserRequestDto): ResponseEntity<BaseResponse>

    @GetMapping("/api/users/{userId}")
    fun getUserInfo(@PathVariable userId: Long): ResponseEntity<SuccessResponse<UserDto>>

    @DeleteMapping("/api/users/{userId}")
    fun deleteUserInfo(@PathVariable userId: Long, @RequestBody dto: DeleteUserRequestDto): ResponseEntity<BaseResponse>
}