package kea.dpang.auth.service

import kea.dpang.auth.dto.Token
import kea.dpang.auth.exception.InvalidRefreshTokenException
import kea.dpang.auth.exception.UserNotFoundException
import kea.dpang.auth.redis.entity.RefreshToken
import kea.dpang.auth.redis.repository.RefreshTokenRepository
import kea.dpang.auth.repository.UserRepository
import kea.dpang.auth.utils.JwtTokenProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class TokenServiceImpl(
    private val tokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider
) : TokenService {

    override fun createToken(identifier: Long): Token {
        // 사용자 정보를 조회한다.
        val user = userRepository.findById(identifier).orElseThrow {
            UserNotFoundException(identifier.toString())
        }

        // 사용자의 권한을 설정한다.
        val authorities = listOf(SimpleGrantedAuthority(user.role.toString()))
        val authentication: Authentication =
            UsernamePasswordAuthenticationToken(user.email, user.password, authorities)

        // JWT 토큰을 생성한다.
        val jwtToken = jwtTokenProvider.createTokens(authentication, identifier)

        // 이미 Redis에 해당 사용자의 리프레시 토큰이 존재한다면 삭제합니다.
        if (tokenRepository.existsById(identifier)) {
            tokenRepository.deleteById(identifier)
        }

        // Redis에 새로운 리프레시 토큰을 저장합니다.
        val refreshToken = RefreshToken(identifier, jwtToken.refreshToken)
        tokenRepository.save(refreshToken)

        return jwtToken
    }

    override fun refreshToken(accessToken: String): Token {
        // 액세스 토큰에서 사용자 ID를 추출한다.
        val userIdx = jwtTokenProvider.getClientIdFromAccessToken(accessToken)!!

        // MySQL에서 사용자 정보를 가져온다.
        val user = userRepository.findById(userIdx).orElseThrow {
            UserNotFoundException(userIdx)
        }

        // Redis에서 리프레시 토큰을 가져온다.
        val refreshTokenInRedis = tokenRepository.findById(userIdx)

        // Redis의 리프레시 토큰과 클라이언트의 리프레시 토큰이 일치하면 기존 토큰을 파기하고 새로운 토큰을 발급한다.
        if (refreshTokenInRedis.isPresent && refreshTokenInRedis.get().refreshToken == accessToken) {
            val authorities = listOf(SimpleGrantedAuthority(user.role.toString()))
            val authentication: Authentication =
                UsernamePasswordAuthenticationToken(user.email, user.password, authorities)

            // 리프레시 토큰 파기
            tokenRepository.deleteById(userIdx)

            return jwtTokenProvider.createTokens(authentication, userIdx)

        } else {
            throw InvalidRefreshTokenException(accessToken)
        }
    }

    override fun removeToken(token: String) {
        TODO("Not yet implemented")
    }
}