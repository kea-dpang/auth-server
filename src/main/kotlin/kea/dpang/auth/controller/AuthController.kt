package kea.dpang.auth.controller

import kea.dpang.auth.base.BaseResponse
import kea.dpang.auth.dto.ChangePasswordRequestDto
import kea.dpang.auth.dto.ResetPasswordRequestDto
import kea.dpang.auth.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    @PostMapping("/reset-password")
    fun resetPassword(
        @RequestBody resetPasswordRequestDto: ResetPasswordRequestDto
    ): ResponseEntity<BaseResponse> {

        userService.resetPassword(
            resetPasswordRequestDto.email,
            resetPasswordRequestDto.code,
            resetPasswordRequestDto.newPassword
        )

        val baseResponse = BaseResponse(
            status = HttpStatus.OK.value(),
            message = "비밀번호가 성공적으로 변경되었습니다."
        )

        return ResponseEntity<BaseResponse>(baseResponse, HttpStatus.OK)
    }

    @PostMapping("/change-password")
    fun changePassword(
        @RequestBody changePasswordRequestDto: ChangePasswordRequestDto
    ): ResponseEntity<BaseResponse> {

        userService.changePassword(
            changePasswordRequestDto.email,
            changePasswordRequestDto.oldPassword,
            changePasswordRequestDto.newPassword
        )

        val baseResponse = BaseResponse(
            status = HttpStatus.OK.value(),
            message = "비밀번호가 성공적으로 변경되었습니다."
        )

        return ResponseEntity<BaseResponse>(baseResponse, HttpStatus.OK)
    }
}
