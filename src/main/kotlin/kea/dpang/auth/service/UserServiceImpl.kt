package kea.dpang.auth.service

import kea.dpang.auth.base.Role
import kea.dpang.auth.entity.User
import kea.dpang.auth.exception.*
import kea.dpang.auth.feign.dto.EmailNotificationDto
import kea.dpang.auth.feign.NotificationFeignClient
import kea.dpang.auth.redis.entity.VerificationCode
import kea.dpang.auth.redis.repository.VerificationCodeRepository
import kea.dpang.auth.repository.UserRepository
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.random.Random

// Todo: Redis, Database등 서비스간의 연결 상태가 불안할 때의 처리
//  해당 상황이 발생했을 때 5XX번대 오류가 뜨지 않고 403이 뜨는 오류 또한 수정 필요

@Slf4j
@Service
class UserServiceImpl(
    private val notificationFeignClient: NotificationFeignClient,
    private val userRepository: UserRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    @Transactional
    override fun register(
        email: String,
        password: String,
        role: Role,
        name: String,
        employeeNumber: String,
        joinDate: LocalDate
    ) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            logger.error("이미 존재하는 이메일: $email")
            throw EmailAlreadyExistsException(email)
        }

        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(password)

        // 사용자 정보 저장을 위한 User 객체 생성
        val user = User(
            email = email,
            password = encodedPassword,
            role = role
        )

        // 사용자 정보 저장
        userRepository.save(user)

        logger.info("사용자 정보 저장 완료: $email")

        // Todo: 이메일, 이름, 사번, 입사일을 사용자 서버에 전송해서 사용자 정보 저장

    }

    @Transactional(readOnly = true)
    override fun verifyUser(email: String, password: String): Long {
        // 이메일로 사용자 조회
        val user = userRepository.findByEmail(email).orElseThrow {
            logger.error("해당 이메일을 가진 사용자를 찾을 수 없음: $email")
            UserNotFoundException(email)
        }

        // 입력받은 비밀번호와 저장된 비밀번호 비교
        if (!passwordEncoder.matches(password, user.password)) {
            logger.error("비밀번호 불일치: $email")
            throw InvalidPasswordException(email)
        }

        // 비밀번호가 일치하면 사용자의 고유 식별자 반환
        logger.info("사용자 인증 완료: $email")
        return user.userIdx!!
    }

    @Transactional
    override fun requestPasswordReset(email: String) {
        try {
            // 자연수 4자리 인증번호 생성 (0부터 9999까지)
            val verificationCode = String.format("%04d", Random.nextInt(10000))

            // 이메일 전송을 위한 DTO 객체 생성
            val dto = EmailNotificationDto(
                to = email,
                title = "비밀번호 재설정 인증번호 안내",
                body = "비밀번호 재설정을 위한 인증번호는 $verificationCode 입니다."
            )

            // 이메일 전송
            logger.info("비밀번호 재설정 인증번호 전송 요청. 이메일: $email")
            val response = notificationFeignClient.sendEmailVerificationCode(dto)

            // 이메일 전송이 성공하면 인증번호 저장
            if (response.statusCode.is2xxSuccessful) {
                logger.info("비밀번호 재설정 인증번호 전송 완료. 이메일: $email")

                // 인증번호 저장을 위한 VerificationCode 객체 생성
                val verificationCodeEntity = VerificationCode(
                    email = email,
                    code = verificationCode
                )

                // 인증번호 저장
                verificationCodeRepository.save(verificationCodeEntity)

                logger.info("비밀번호 재설정 인증번호 저장 완료. 이메일: $email")
            }

        } catch (e: Exception) {
            logger.error("비밀번호 재설정 인증번호 전송 중 알 수 없는 예외 발생", e)
            throw e
        }
    }

    @Transactional
    override fun resetPassword(email: String, code: String, newPassword: String) {
        // 이메일로 사용자 조회
        val user = userRepository.findByEmail(email).orElseThrow {
            logger.error("해당 이메일을 가진 사용자를 찾을 수 없음: $email")
            UserNotFoundException(email)
        }

        // 이메일로 인증 코드 조회
        val storedCode = verificationCodeRepository.findById(email).orElseThrow {
            logger.error("해당 이메일을 가진 사용자의 인증 코드를 찾을 수 없음: $email")
            VerificationCodeNotFoundException(email)
        }

        // 입력받은 인증 코드와 저장된 인증 코드 비교
        if (storedCode.code != code) {
            logger.error("인증 코드 불일치: $email")
            throw InvalidVerificationCodeException(email)
        }

        // 인증 코드가 일치하면 비밀번호 변경
        user.password = passwordEncoder.encode(newPassword)

        // 인증 코드 삭제
        verificationCodeRepository.delete(storedCode)

        logger.info("비밀번호 재설정 완료: $email")
    }

    @Transactional
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

    @Transactional
    override fun deleteAccount(userId: Long) {
        userRepository.deleteById(userId)
        logger.info("사용자 계정이 성공적으로 삭제되었습니다. 사용자 ID: {}", userId)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

}