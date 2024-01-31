package kea.dpang.auth.service

import kea.dpang.auth.dto.Token
import kea.dpang.auth.exception.InvalidRefreshTokenException
import kea.dpang.auth.exception.TokenNotFoundException
import kea.dpang.auth.exception.UserNotFoundException
import kea.dpang.auth.redis.entity.RefreshToken
import kea.dpang.auth.redis.repository.RefreshTokenRepository
import kea.dpang.auth.repository.UserRepository
import kea.dpang.auth.utils.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TokenServiceImpl(
    private val tokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider
) : TokenService {

    override fun createToken(identifier: Long): Token {
        // 사용자 정보를 조회한다.
        val user = userRepository.findById(identifier).orElseThrow {
            logger.error("사용자를 찾을 수 없음: $identifier")
            UserNotFoundException(identifier.toString())
        }

        // 사용자의 권한을 설정한다.
        val authorities = listOf(SimpleGrantedAuthority(user.role.toString()))
        val authentication: Authentication =
            UsernamePasswordAuthenticationToken(user.email, user.password, authorities)

        // JWT 토큰을 생성한다.
        val jwtToken = jwtTokenProvider.createTokens(authentication, identifier)

        logger.info("사용자($identifier) 토큰 생성 완료")

        // 이미 Redis에 해당 사용자의 리프레시 토큰이 존재한다면 삭제한다.
        if (tokenRepository.existsById(identifier)) {
            tokenRepository.deleteById(identifier)
        }

        // Redis에 새로운 리프레시 토큰을 저장한다.
        val refreshToken = RefreshToken(identifier, jwtToken.refreshToken)
        tokenRepository.save(refreshToken)

        logger.info("사용자($identifier) 리프레시 토큰 저장 완료")

        return jwtToken
    }

    override fun refreshToken(accessToken: String): Token {
        // 액세스 토큰에서 사용자 ID를 추출한다.
        val userIdx = jwtTokenProvider.getClientIdFromAccessToken(accessToken)!!
        logger.info("사용자($userIdx) 토큰 재발급 요청")

        // MySQL에서 사용자 정보를 가져온다.
        val user = userRepository.findById(userIdx).orElseThrow {
            logger.error("사용자를 찾을 수 없음: $userIdx")
            UserNotFoundException(userIdx)
        }

        // Redis에서 리프레시 토큰을 가져온다.
        val refreshTokenInRedis = tokenRepository.findById(userIdx)
        logger.info("사용자($userIdx) 리프레시 토큰 조회 완료")

        // Redis의 리프레시 토큰과 클라이언트의 리프레시 토큰이 일치하면 기존 토큰을 파기하고 새로운 토큰을 발급한다.
        if (refreshTokenInRedis.isPresent && refreshTokenInRedis.get().refreshToken == accessToken) {
            logger.info("사용자($userIdx) 리프레시 토큰 일치")

            val authorities = listOf(SimpleGrantedAuthority(user.role.toString()))
            val authentication: Authentication =
                UsernamePasswordAuthenticationToken(user.email, user.password, authorities)

            // Redis에 저장된 기존 리프레시 토큰을 파기한다.
            tokenRepository.deleteById(userIdx)

            logger.info("사용자($userIdx) 리프레시 토큰 파기 완료")

            // JWT 토큰을 생성한다.
            val jwtToken = jwtTokenProvider.createTokens(authentication, userIdx)

            // Redis에 새로운 리프레시 토큰을 저장한다.
            val refreshToken = RefreshToken(userIdx, jwtToken.refreshToken)
            tokenRepository.save(refreshToken)

            logger.info("사용자($userIdx) 리프레시 토큰 재발급 및 저장 완료")

            // 생성된 리프레시 토큰을 반환한다.
            return jwtToken

        } else {
            throw InvalidRefreshTokenException(accessToken)
        }
    }

    override fun removeToken(identifier: Long) {
        try {
            tokenRepository.deleteById(identifier)
            logger.info("사용자($identifier) 리프레시 토큰 삭제 완료")

        } catch (e: EmptyResultDataAccessException) { // 토큰이 없는 경우에 대한 처리
            throw TokenNotFoundException(identifier)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TokenServiceImpl::class.java)
    }
}