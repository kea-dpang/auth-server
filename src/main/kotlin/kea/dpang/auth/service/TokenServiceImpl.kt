package kea.dpang.auth.service

import kea.dpang.auth.dto.Token
import kea.dpang.auth.repository.RefreshTokenRepository

class TokenServiceImpl(
    private val tokenRepository: RefreshTokenRepository
) : TokenService {

    override fun createToken(identifier: String): Token {
        TODO("Not yet implemented")
    }

    override fun refreshToken(token: String): Token {
        TODO("Not yet implemented")
    }

    override fun removeToken(token: String) {
        TODO("Not yet implemented")
    }
}