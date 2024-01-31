package kea.dpang.auth.controller

import kea.dpang.auth.base.BaseResponse
import kea.dpang.auth.base.SuccessResponse
import kea.dpang.auth.dto.*
import kea.dpang.auth.service.TokenService
import kea.dpang.auth.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val tokenService: TokenService
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

    @PostMapping("/login")
    fun login(
        @RequestBody loginRequestDto: LoginRequestDto
    ): ResponseEntity<SuccessResponse<Token>> {

        val userId = userService.verifyUser(
            loginRequestDto.email,
            loginRequestDto.password
        )

        val token = tokenService.createToken(userId)

        return ResponseEntity.ok(SuccessResponse(HttpStatus.OK.value(), "로그인에 성공하였습니다.", token))
    }

    @PostMapping("/renew-token")
    fun renewToken(
        @RequestBody renewTokenRequestDto: RenewTokenRequestDto
    ): ResponseEntity<SuccessResponse<Token>> {

        val token = tokenService.refreshToken(renewTokenRequestDto.accessToken)

        return ResponseEntity.ok(SuccessResponse(HttpStatus.OK.value(), "토큰이 성공적으로 갱신되었습니다.", token))
    }

}
