package kea.dpang.auth.service

import kea.dpang.auth.base.Role
import kea.dpang.auth.exception.InvalidPasswordException
import kea.dpang.auth.exception.UserNotFoundException
import kea.dpang.auth.repository.UserRepository
import kea.dpang.auth.repository.VerificationCodeRepository
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Slf4j
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    override fun register(email: String, password: String, role: Role) {
        TODO("Not yet implemented")
    }

    override fun verifyUser(email: String, password: String): Int {
        TODO("Not yet implemented")
    }

    override fun requestPasswordReset(email: String) {
        TODO("Not yet implemented")
    }

    override fun verifyCodeAndResetPassword(email: String, code: String, newPassword: String) {
        TODO("Not yet implemented")
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