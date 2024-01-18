package kea.dpang.auth.controller

import kea.dpang.auth.base.BaseResponse
import kea.dpang.auth.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService
) {

    @PostMapping("/send-verification-code")
    fun sendVerificationCode(
        @RequestParam email: String
    ): ResponseEntity<BaseResponse> {

        userService.requestPasswordReset(email)

        val baseResponse = BaseResponse(
            status = HttpStatus.OK.value(),
            message = "인증코드 전송에 성공하였습니다."
        )

        return ResponseEntity<BaseResponse>(baseResponse, HttpStatus.OK)
    }
}
