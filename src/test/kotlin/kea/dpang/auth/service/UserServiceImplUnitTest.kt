package kea.dpang.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kea.dpang.auth.entity.User
import kea.dpang.auth.exception.InvalidPasswordException
import kea.dpang.auth.exception.UserNotFoundException
import kea.dpang.auth.repository.UserRepository
import kea.dpang.auth.repository.VerificationCodeRepository

import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class UserServiceTest : BehaviorSpec({
    val mockUserRepository = mockk<UserRepository>()
    val mockVerificationCodeRepository = mockk<VerificationCodeRepository>()
    val mockPasswordEncoder = mockk<PasswordEncoder>()
    val userService = UserServiceImpl(mockUserRepository, mockVerificationCodeRepository, mockPasswordEncoder)

    Given("사용자가 비밀번호를 변경하려고 할 때") {
        val email = "test@example.com"
        val oldPassword = "oldPassword"
        val newPassword = "newPassword"
        val encodedPassword = "encodedNewPassword"
        val user = User(email = email, password = oldPassword)

        every { mockUserRepository.findByEmail(email) } returns Optional.of(user)
        every { mockPasswordEncoder.matches(oldPassword, user.password) } returns true
        every { mockPasswordEncoder.encode(newPassword) } returns encodedPassword

        When("기존 비밀번호가 올바른 경우") {
            userService.changePassword(email, oldPassword, newPassword)

            Then("비밀번호가 성공적으로 변경되어야 한다") {
                user.password shouldBe encodedPassword
            }
        }

        When("기존 비밀번호가 잘못된 경우") {
            every { mockPasswordEncoder.matches(any(), any()) } returns false

            Then("InvalidPasswordException이 발생해야 한다") {
                shouldThrow<InvalidPasswordException> {
                    userService.changePassword(email, "wrongPassword", newPassword)
                }
            }
        }

        When("해당 이메일을 가진 사용자가 없는 경우") {
            every { mockUserRepository.findByEmail(email) } returns Optional.empty()

            Then("UserNotFoundException이 발생해야 한다") {
                shouldThrow<UserNotFoundException> {
                    userService.changePassword(email, oldPassword, newPassword)
                }
            }
        }
    }
})
