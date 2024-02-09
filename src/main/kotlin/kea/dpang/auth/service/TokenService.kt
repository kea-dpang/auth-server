package kea.dpang.auth.service

import kea.dpang.auth.dto.Token

interface TokenService {

    /**
     * 토큰 생성 메서드.
     *
     * 사용자 식별자를 기반으로 새로운 인증 토큰을 반환합니다.
     */
    fun createToken(identifier: Long): Token

    /**
     * 토큰 갱신 메서드.
     *
     * 사용자의 기존 리프레시 토큰을 입력받아 새로운 인증 토큰을 반환합니다.
     */
    fun refreshToken(identifier: Long, refreshToken: String): Token

    /**
     * 토큰 제거 메서드.
     *
     * 사용자의 리프레시 토큰을 입력받아 해당 토큰을 완전히 제거합니다.
     * 이 메서드는 로그아웃 및 회원 탈퇴 시 사용됩니다.
     */
    fun removeToken(identifier: Long)

}