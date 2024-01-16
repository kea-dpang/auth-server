package kea.dpang.auth.service

import kea.dpang.auth.base.Role
import kea.dpang.auth.repository.UserRepository
import kea.dpang.auth.repository.VerificationCodeRepository
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val verificationCodeRepository: VerificationCodeRepository
) : UserService {

    override fun register(id: String, password: String, role: Role) {
        TODO("Not yet implemented")
    }

    override fun verifyUser(id: String, password: String): Int {
        TODO("Not yet implemented")
    }

    override fun requestPasswordReset(email: String) {
        TODO("Not yet implemented")
    }

    override fun verifyCodeAndResetPassword(id: String, code: String, newPassword: String) {
        TODO("Not yet implemented")
    }

    override fun changePassword(id: String, oldPassword: String, newPassword: String) {
        TODO("Not yet implemented")
    }

    override fun deleteAccount(identifier: Int) {
        TODO("Not yet implemented")
    }
}