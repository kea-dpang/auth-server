package kea.dpang.auth.service

import kea.dpang.auth.base.Role
import kea.dpang.auth.dto.feign.EmailNotificationDto
import kea.dpang.auth.exception.InvalidPasswordException
import kea.dpang.auth.exception.InvalidVerificationCodeException
import kea.dpang.auth.exception.UserNotFoundException
import kea.dpang.auth.exception.VerificationCodeNotFoundException
import kea.dpang.auth.feign.NotificationFeignClient
import kea.dpang.auth.redis.entity.VerificationCode
import kea.dpang.auth.redis.repository.VerificationCodeRepository
import kea.dpang.auth.repository.UserRepository
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import kotlin.random.Random

@Slf4j
@Service
class UserServiceImpl(
    private val notificationFeignClient: NotificationFeignClient,
    private val userRepository: UserRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    override fun register(email: String, password: String, role: Role) {
        TODO("Not yet implemented")
    }

    override fun verifyUser(email: String, password: String): Long {
        // 이메일로 사용자 조회
        val user = userRepository.findByEmail(email).orElseThrow {
            UserNotFoundException(email)
        }

        // 입력받은 비밀번호와 저장된 비밀번호 비교
        if (!passwordEncoder.matches(password, user.password)) {
            throw InvalidPasswordException(email)
        }

        // 비밀번호가 일치하면 사용자의 고유 식별자 반환
        return user.userIdx!!
    }

    override fun requestPasswordReset(email: String) {
        // 자연수 4자리 인증번호 생성 (0부터 9999까지)
        val verificationCode = String.format("%04d", Random.nextInt(10000))

        // 이메일 전송을 위한 DTO 객체 생성
        val dto = EmailNotificationDto(
            to = email,
            title = "비밀번호 재설정 인증번호 안내",
            body = "비밀번호 재설정을 위한 인증번호는 $verificationCode 입니다."
        )

        // 이메일 전송
        val response: ResponseEntity<Any> = notificationFeignClient.sendEmailVerificationCode(dto)

        // 이메일 전송이 성공하면 인증번호 저장
        if (response.statusCode.is2xxSuccessful) {

            // 인증번호 저장을 위한 VerificationCode 객체 생성
            val verificationCodeEntity = VerificationCode(
                email = email,
                code = verificationCode
            )

            // 인증번호 저장
            verificationCodeRepository.save(verificationCodeEntity)
        }
    }

    override fun verifyCodeAndResetPassword(email: String, code: String, newPassword: String) {
        // 이메일로 사용자 조회
        val user = userRepository.findByEmail(email).orElseThrow {
            UserNotFoundException(email)
        }

        // 이메일로 인증 코드 조회
        val storedCode = verificationCodeRepository.findById(email).orElseThrow {
            VerificationCodeNotFoundException(email)
        }

        // 입력받은 인증 코드와 저장된 인증 코드 비교
        if (storedCode.code != code) {
            throw InvalidVerificationCodeException(email)
        }

        // 인증 코드가 일치하면 비밀번호 변경
        user.password = passwordEncoder.encode(newPassword)

        // 인증 코드 삭제
        verificationCodeRepository.delete(storedCode)
    }

    override fun changePassword(email: String, oldPassword: String, newPassword: String) {
        logger.info("비밀번호 변경 요청. 이메일: $email")

        val user = userRepository.findByEmail(email).orElseThrow {
            logger.error("해당 이메일을 가진 사용자를 찾을 수 없음: $email")
            UserNotFoundException(email)
        }

        // 기존 비밀번호 확인
        if (!passwordEncoder.matches(oldPassword, user.password)) {
            logger.error("비밀번호 불일치: $email")
            throw InvalidPasswordException(email)
        }

        // 새 비밀번호 암호화 후 저장
        user.password = passwordEncoder.encode(newPassword)

        logger.info("비밀번호 변경 완료: $email")
    }

    override fun deleteAccount(identifier: Int) {
        TODO("Not yet implemented")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

}