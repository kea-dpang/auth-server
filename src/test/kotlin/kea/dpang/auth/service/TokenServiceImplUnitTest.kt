package kea.dpang.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kea.dpang.auth.base.Role
import kea.dpang.auth.dto.Token
import kea.dpang.auth.entity.User
import kea.dpang.auth.exception.InvalidRefreshTokenException
import kea.dpang.auth.exception.TokenNotFoundException
import kea.dpang.auth.exception.UserNotFoundException
import kea.dpang.auth.redis.entity.RefreshToken
import kea.dpang.auth.redis.repository.RefreshTokenRepository
import kea.dpang.auth.repository.UserRepository
import kea.dpang.auth.utils.JwtTokenProvider
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.EmptyResultDataAccessException
import java.util.*

class TokenServiceImplUnitTest : BehaviorSpec({
    val mockJwtTokenProvider = mockk<JwtTokenProvider>()
    val mockUserRepository = mockk<UserRepository>()
    val mockRefreshTokenRepository = mockk<RefreshTokenRepository>()

    lateinit var tokenService: TokenServiceImpl

    beforeTest {
        tokenService = TokenServiceImpl(mockRefreshTokenRepository, mockUserRepository, mockJwtTokenProvider)
    }

    afterTest {
        clearMocks(mockJwtTokenProvider, mockUserRepository, mockRefreshTokenRepository)
    }

    Given("사용자 ID와 매칭되는 유저가 존재하고, 해당 유저의 리프레시 토큰이 이미 Redis에 존재하는 경우") {
        val identifier = 1L
        val user = User(identifier, "test@email.com", "password", Role.USER)
        val jwtToken = Token("new_access_token", "new_refresh_token")
        val refreshToken = RefreshToken(identifier, jwtToken.refreshToken)

        every { mockUserRepository.findById(identifier) } returns Optional.of(user)
        every { mockJwtTokenProvider.createTokens(any(), any()) } returns jwtToken
        every { mockRefreshTokenRepository.existsById(identifier) } returns true
        every { mockRefreshTokenRepository.deleteById(identifier) } just Runs
        every { mockRefreshTokenRepository.save(refreshToken) } returns refreshToken

        When("토큰 생성을 요청하면") {
            val result = tokenService.createToken(identifier)

            Then("새로운 토큰이 생성되어야 하고, 기존 토큰은 삭제되어야 한다") {
                result.accessToken shouldBe "new_access_token"
                result.refreshToken shouldBe "new_refresh_token"
                verify { mockRefreshTokenRepository.deleteById(identifier) }
                verify { mockRefreshTokenRepository.save(refreshToken) }
            }
        }
    }

    Given("사용자 ID와 매칭되는 유저가 존재하지만, 해당 유저의 리프레시 토큰이 Redis에 존재하지 않는 경우") {
        val identifier = 1L
        val user = User(identifier, "test@email.com", "password", Role.USER)
        val jwtToken = Token("new_access_token", "new_refresh_token")
        val refreshToken = RefreshToken(identifier, jwtToken.refreshToken)

        every { mockUserRepository.findById(identifier) } returns Optional.of(user)
        every { mockJwtTokenProvider.createTokens(any(), any()) } returns jwtToken
        every { mockRefreshTokenRepository.existsById(identifier) } returns false
        every { mockRefreshTokenRepository.save(refreshToken) } returns refreshToken

        When("토큰 생성을 요청하면") {
            val result = tokenService.createToken(identifier)

            Then("새로운 토큰이 생성되고, Redis에 저장되어야 한다") {
                result.accessToken shouldBe "new_access_token"
                result.refreshToken shouldBe "new_refresh_token"
                verify(exactly = 0) { mockRefreshTokenRepository.deleteById(identifier) }
                verify { mockRefreshTokenRepository.save(refreshToken) }
            }
        }
    }

    Given("사용자 ID와 매칭되는 유저가 존재하지 않는 경우") {
        val identifier = 1L

        every { mockUserRepository.findById(identifier) } returns Optional.empty()

        When("토큰 생성을 요청하면") {
            Then("UserNotFoundException이 발생해야 한다") {
                shouldThrow<UserNotFoundException> {
                    tokenService.createToken(identifier)
                }
            }
        }
    }


    Given("사용자 ID와 매칭되는 유저와 토큰이 존재하는 경우") {
        val userIdx = 1L
        val token = "valid_refresh_token"
        val user = User(userIdx, "test@email.com", "password", Role.USER)
        val refreshToken = RefreshToken(userIdx, token)

        every { mockJwtTokenProvider.getClientIdFromToken(token) } returns userIdx
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

        every { mockJwtTokenProvider.getClientIdFromToken(invalidToken) } returns invalidUserIdx
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

        every { mockJwtTokenProvider.getClientIdFromToken(invalidToken) } returns invalidUserIdx
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

        every { mockJwtTokenProvider.getClientIdFromToken(clientToken) } returns userIdx
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

    Given("사용자가 토큰을 제거하려고 요청할 때") {
        val identifier = 1L

        When("사용자 식별자가 주어지면") {
            every { mockRefreshTokenRepository.deleteById(identifier) } returns Unit

            tokenService.removeToken(identifier)

            Then("해당 식별자의 토큰이 제거되어야 한다") {
                verify { mockRefreshTokenRepository.deleteById(identifier) }
            }
        }

        When("사용자의 토큰이 없는 경우") {
            every { mockRefreshTokenRepository.deleteById(identifier) } throws EmptyResultDataAccessException(0)

            Then("TokenNotFoundException이 발생해야 한다") {
                shouldThrow<TokenNotFoundException> {
                    tokenService.removeToken(identifier)
                }
            }
        }
    }

})

