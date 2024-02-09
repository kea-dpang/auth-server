package kea.dpang.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import kea.dpang.auth.base.BaseResponse
import kea.dpang.auth.base.Role
import kea.dpang.auth.base.SuccessResponse
import kea.dpang.auth.dto.*
import kea.dpang.auth.service.TokenService
import kea.dpang.auth.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val tokenService: TokenService
) {

    @PostMapping("/send-verification-code")
    @Operation(summary = "인증코드 전송", description = "비밀번호 재설정을 위한 인증코드를 전송합니다.")
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
    @Operation(summary = "비밀번호 재설정", description = "인증코드를 확인하고 일치하는 경우 새로운 비밀번호로 재설정합니다.")
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

    @PreAuthorize("#clientId == #userId")
    @PostMapping("/users/{userId}/change-password")
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인하고 일치하는 경우 새로운 비밀번호로 변경합니다.")
    fun changePassword(
        @Parameter(hidden = true)
        @RequestHeader("X-DPANG-CLIENT-ID") clientId: Long,
        @Parameter(hidden = true)
        @RequestHeader("X-DPANG-CLIENT-ROLE") role: String,
        @Parameter(description = "사용자 ID") @PathVariable userId: Long,
        @RequestBody changePasswordRequestDto: ChangePasswordRequestDto
    ): ResponseEntity<BaseResponse> {

        userService.changePassword(
            userId,
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
    @Operation(summary = "로그인", description = "이메일과 비밀번호를 입력받아 로그인합니다.")
    fun login(
        @RequestBody loginRequestDto: LoginRequestDto
    ): ResponseEntity<SuccessResponse<LoginResponseDto>> {

        val userIdx = userService.verifyUser(
            loginRequestDto.email,
            loginRequestDto.password
        )

        val token = tokenService.createToken(userIdx)

        val userDto = userService.getUserInfo(userIdx)

        val mileageInfo = userService.getUserMileageInfo(userIdx)

        val loginResponseDto = LoginResponseDto(
            user = UserInfo(
                userIdx = userIdx,
                userName = userDto.name,
                mileage = mileageInfo.mileage,
                personalChargedMileage = mileageInfo.personalChargedMileage
            ),
            token = token
        )

        return ResponseEntity.ok(SuccessResponse(HttpStatus.OK.value(), "로그인에 성공하였습니다.", loginResponseDto))
    }

    @PreAuthorize("#clientId == #userId")
    @PostAuthorize("#role==returnObject.body.data.role")
    @PostMapping("/users/{userId}/renew-token")
    @Operation(summary = "토큰 갱신", description = "기존 토큰을 갱신합니다.")
    fun renewToken(
        @Parameter(hidden = true)
        @RequestHeader("X-DPANG-CLIENT-ID") clientId: Long,
        @Parameter(hidden = true)
        @RequestHeader("X-DPANG-CLIENT-ROLE") role: String,
        @Parameter(description = "사용자 ID") @PathVariable userId: Long,
        @RequestBody renewTokenRequestDto: RenewTokenRequestDto
    ): ResponseEntity<SuccessResponse<Token>> {

        val token = tokenService.refreshToken(userId, renewTokenRequestDto.refreshToken)

        return ResponseEntity.ok(SuccessResponse(HttpStatus.OK.value(), "토큰이 성공적으로 갱신되었습니다.", token))
    }

    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "회원가입을 진행합니다.")
    fun join(
        @RequestBody registerRequestDto: RegisterRequestDto
    ): ResponseEntity<BaseResponse> {

        userService.register(
            email = registerRequestDto.email,
            password = registerRequestDto.password,
            role = registerRequestDto.role ?: Role.USER,
            name = registerRequestDto.name,
            employeeNumber = registerRequestDto.employeeNumber,
            joinDate = registerRequestDto.joinDate
        )

        val baseResponse = BaseResponse(
            status = HttpStatus.CREATED.value(),
            message = "회원가입이 성공적으로 완료되었습니다."
        )

        return ResponseEntity<BaseResponse>(baseResponse, HttpStatus.CREATED)
    }

    @PreAuthorize("#clientId == #id")
    @DeleteMapping("/users/{id}")
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다.")
    fun deleteUser(
        @Parameter(hidden = true)
        @RequestHeader("X-DPANG-CLIENT-ID") clientId: Long,
        @Parameter(hidden = true)
        @RequestHeader("X-DPANG-CLIENT-ROLE") role: String,
        @PathVariable id: Long,
        @RequestBody dto: DeleteUserRequestDto
    ): ResponseEntity<BaseResponse> {
        userService.deleteAccount(id, dto.password, dto.reason, dto.message)
        tokenService.removeToken(id)

        val baseResponse = BaseResponse(
            status = HttpStatus.NO_CONTENT.value(),
            message = "회원 탈퇴가 성공적으로 완료되었습니다."
        )

        return ResponseEntity(baseResponse, HttpStatus.NO_CONTENT)
    }

}
