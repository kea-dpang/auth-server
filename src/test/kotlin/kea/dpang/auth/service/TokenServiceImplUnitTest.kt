package kea.dpang.auth.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kea.dpang.auth.base.Role
import kea.dpang.auth.dto.Token
import kea.dpang.auth.entity.User
import kea.dpang.auth.exception.InvalidRefreshTokenException
import kea.dpang.auth.exception.UserNotFoundException
import kea.dpang.auth.redis.entity.RefreshToken
import kea.dpang.auth.redis.repository.RefreshTokenRepository
import kea.dpang.auth.repository.UserRepository
import kea.dpang.auth.utils.JwtTokenProvider
import org.junit.jupiter.api.assertThrows
import java.util.*

class TokenServiceImplUnitTest : BehaviorSpec({
    val mockJwtTokenProvider = mockk<JwtTokenProvider>()
    val mockUserRepository = mockk<UserRepository>()
    val mockRefreshTokenRepository = mockk<RefreshTokenRepository>()

    val tokenService = TokenServiceImpl(mockRefreshTokenRepository, mockUserRepository, mockJwtTokenProvider)

    Given("사용자 ID와 매칭되는 유저와 토큰이 존재하는 경우") {
        val userIdx = 1L
        val token = "valid_refresh_token"
        val user = User(userIdx, "test@email.com", "password", Role.USER)
        val refreshToken = RefreshToken(userIdx, token)

        every { mockJwtTokenProvider.getClientIdFromAccessToken(token) } returns userIdx
        every { mockUserRepository.findById(userIdx) } returns Optional.of(user)
        every { mockRefreshTokenRepository.findById(userIdx) } returns Optional.of(refreshToken)
        every { mockJwtTokenProvider.createTokens(any(), any()) } returns Token("new_access_token", "new_refresh_token")
        every { mockRefreshTokenRepository.deleteById(userIdx) } just Runs

        When("토큰 재발급을 요청하면") {
            val result = tokenService.refreshToken(token)

            Then("새로운 토큰이 발급되어야 한다") {
                result.accessToken shouldBe "new_access_token"
                result.refreshToken shouldBe "new_refresh_token"
                verify { mockRefreshTokenRepository.deleteById(userIdx) }
            }
        }
    }

    Given("사용자 ID와 매칭되는 유저가 없는 경우") {
        val invalidUserIdx = 2L
        val invalidToken = "invalid_refresh_token"

        every { mockJwtTokenProvider.getClientIdFromAccessToken(invalidToken) } returns invalidUserIdx
        every { mockUserRepository.findById(invalidUserIdx) } returns Optional.empty()

        When("토큰 재발급을 요청하면") {
            Then("UserNotFoundException이 발생해야 한다") {
                assertThrows<UserNotFoundException> {
                    tokenService.refreshToken(invalidToken)
                }
            }
        }
    }

    Given("사용자 ID와 매칭되는 리프레시 토큰이 없는 경우") {
        val invalidUserIdx = 2L
        val invalidToken = "invalid_refresh_token"
        val user = User(invalidUserIdx, "test2@email.com", "password", Role.USER)

        every { mockJwtTokenProvider.getClientIdFromAccessToken(invalidToken) } returns invalidUserIdx
        every { mockUserRepository.findById(invalidUserIdx) } returns Optional.of(user)
        every { mockRefreshTokenRepository.findById(invalidUserIdx) } returns Optional.empty()

        When("토큰 재발급을 요청하면") {
            Then("InvalidRefreshTokenException이 발생해야 한다") {
                assertThrows<InvalidRefreshTokenException> {
                    tokenService.refreshToken(invalidToken)
                }
            }
        }
    }

    Given("사용자 ID와 매칭되는 리프레시 토큰이 존재하지만 클라이언트의 리프레시 토큰과 일치하지 않는 경우") {
        val userIdx = 3L
        val clientToken = "client_refresh_token"
        val serverToken = "server_refresh_token"
        val user = User(userIdx, "test3@email.com", "password", Role.USER)
        val refreshToken = RefreshToken(userIdx, serverToken)

        every { mockJwtTokenProvider.getClientIdFromAccessToken(clientToken) } returns userIdx
        every { mockUserRepository.findById(userIdx) } returns Optional.of(user)
        every { mockRefreshTokenRepository.findById(userIdx) } returns Optional.of(refreshToken)

        When("토큰 재발급을 요청하면") {
            Then("InvalidRefreshTokenException이 발생해야 한다") {
                assertThrows<InvalidRefreshTokenException> {
                    tokenService.refreshToken(clientToken)
                }
            }
        }
    }

})

