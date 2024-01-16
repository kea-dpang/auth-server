package kea.dpang.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kea.dpang.auth.entity.User
import kea.dpang.auth.entity.VerificationCode
import kea.dpang.auth.exception.InvalidPasswordException
import kea.dpang.auth.exception.InvalidVerificationCodeException
import kea.dpang.auth.exception.UserNotFoundException
import kea.dpang.auth.exception.VerificationCodeNotFoundException
import kea.dpang.auth.repository.UserRepository
import kea.dpang.auth.repository.VerificationCodeRepository

import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class UserServiceTest : BehaviorSpec({
    val mockUserRepository = mockk<UserRepository>()
    val mockVerificationCodeRepository = mockk<VerificationCodeRepository>()
    val mockPasswordEncoder = mockk<PasswordEncoder>()
    val userService = UserServiceImpl(mockUserRepository, mockVerificationCodeRepository, mockPasswordEncoder)

    Given("사용자가 비밀번호를 재설정하려고 할 때") {
        val email = "test@example.com"
        val code = "123456"
        val newPassword = "newPassword"
        val encodedPassword = "encodedNewPassword"
        val user = User(email = email, password = "oldPassword")
        val verificationCode = VerificationCode(id = email, code = code)

        every { mockUserRepository.findByEmail(email) } returns Optional.of(user)
        every { mockVerificationCodeRepository.findById(email) } returns Optional.of(verificationCode)
        every { mockPasswordEncoder.encode(newPassword) } returns encodedPassword
        every { mockUserRepository.save(any()) } answers { firstArg() }
        every { mockVerificationCodeRepository.delete(any()) } just Runs

        When("입력받은 인증 코드가 저장된 인증 코드와 일치하는 경우") {
            userService.verifyCodeAndResetPassword(email, code, newPassword)

            Then("비밀번호가 성공적으로 재설정되어야 한다") {
                verify { mockVerificationCodeRepository.delete(verificationCode) }
                user.password shouldBe encodedPassword
            }
        }

        When("입력받은 인증 코드가 저장된 인증 코드와 일치하지 않는 경우") {
            val wrongCode = "wrongCode"

            Then("InvalidVerificationCodeException이 발생해야 한다") {
                shouldThrow<InvalidVerificationCodeException> {
                    userService.verifyCodeAndResetPassword(email, wrongCode, newPassword)
                }
            }
        }

        When("해당 이메일에 대한 인증 코드가 없는 경우") {
            every { mockVerificationCodeRepository.findById(email) } returns Optional.empty()

            Then("VerificationCodeNotFoundException이 발생해야 한다") {
                shouldThrow<VerificationCodeNotFoundException> {
                    userService.verifyCodeAndResetPassword(email, code, newPassword)
                }
            }
        }

        When("해당 이메일을 가진 사용자가 없는 경우") {
            every { mockUserRepository.findByEmail(email) } returns Optional.empty()

            Then("UserNotFoundException이 발생해야 한다") {
                shouldThrow<UserNotFoundException> {
                    userService.verifyCodeAndResetPassword(email, code, newPassword)
                }
            }
        }
    }

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
