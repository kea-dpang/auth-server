package kea.dpang.auth.utils

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import kea.dpang.auth.dto.Token
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Collectors
import javax.crypto.SecretKey


@Component
class JwtTokenProvider @Autowired constructor(
    @Value("\${jwt.secret-key}") secretKey: String
) {

    // JWT 비밀 키 값
    private lateinit var secretKey: SecretKey

    init {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey))
    }

    companion object {
        // 액세스 토큰과 리프레시 토큰의 만료 시간을 전역 변수로 관리한다.
        private const val ONE_HOUR = 1000L * 60 * 60 // 1 hour in milliseconds
        private const val ONE_DAY = ONE_HOUR * 24 // 1 day in milliseconds
        private const val ACCESS_TOKEN_EXPIRE_TIME = ONE_HOUR * 3 // 3 hours
        private const val REFRESH_TOKEN_EXPIRE_TIME = ONE_DAY * 5 // 5 days
    }

    /**
     * Authentication 객체와 사용자 ID를 입력받아, JWT 액세스 토큰과 리프레시 토큰을 생성한다.
     *
     * @param authentication 인증 정보를 담고 있는 객체. 이 객체에는 사용자의 권한 정보 등이 포함되어 있다.
     * @param userIdx 사용자의 고유 ID입니다. 이 ID는 액세스 토큰에 포함된다.
     * @return 생성된 JWT 액세스 토큰과 리프레시 토큰을 담고 있는 Token 객체
     * @throws Exception 토큰 생성 중 발생한 예외를 처리하기 위해 사용된다.
     */
    @Throws(Exception::class)
    fun createTokens(authentication: Authentication, userIdx: Int): Token {
        // 생성된 액세스 토큰과 리프레시 토큰을 포함하는 Token 객체를 반환한다.
        return Token(
            accessToken = createAccessToken(authentication, userIdx),
            refreshToken = createRefreshToken(authentication)
        )
    }

    /**
     * 액세스 토큰을 생성하는 메소드
     *
     * @param authentication 사용자 인증 정보를 담고 있는 Authentication 객체
     * @param userIdx 사용자의 고유 ID. 이 ID는 액세스 토큰에 포함된다.
     * @throws Exception 토큰 생성 중 발생한 예외를 처리하기 위해 사용된다.
     * @return 생성된 JWT 액세스 토큰
     */
    @Throws(Exception::class)
    fun createAccessToken(authentication: Authentication, userIdx: Int): String {
        // 사용자의 역할을 가져와서 콤마로 분리된 문자열로 변환한다.
        val roles = authentication.authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","))

        // 액세스 토큰을 생성하고 반환한다.
        return Jwts.builder()
            // Header: 토큰의 타입(JWT)과 서명에 사용된 알고리즘(HS512) 정보를 담는다.
            .header()
            .add("type", "JWT") // 토큰의 타입 지정. 여기서는 JWT를 사용.
            .and()

            // Payload: 토큰에 담을 클레임(데이터)을 지정. 클레임에는 사용자의 이름, 역할, ID 등이 포함될 수 있다.
            .issuer("DPANG-AUTH-SERVER") // iss 클레임: 토큰 발급자를 지정
            .subject(authentication.name) // sub 클레임: 토큰 제목을 지정
//            .audience().add("your-audience").and()  // aud 클레임: 토큰 대상자를 지정
            .expiration(Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME)) // exp 클레임: 토큰 만료 시간을 지정
//            .notBefore(Date(System.currentTimeMillis())) // nbf 클레임: 토큰 활성 날짜를 지정. 헤딩 시간 이전에는 토큰이 활성화되지 않는다.
            .issuedAt(Date()) // iat 클레임: 토큰 발급 시간을 지정
//            .id(UUID.randomUUID().toString()) // jti 클레임: JWT 토큰 식별자를 지정
            .claim("role", roles) // 사용자 정의 클레임: 사용자의 역할
            .claim("client-id", userIdx) // 사용자 정의 클레임: 사용자의 식별자

            // Signature: header와 payload를 암호화한 결과. 이 부분이 토큰의 무결성을 보장하는 부분
            .signWith(secretKey) // signWith 메소드를 사용해 서명 알고리즘과 키를 지정
            .compact()
    }

    /**
     * 리프레시 토큰을 생성하는 메소드
     *
     * @param authentication 사용자 인증 정보를 담고 있는 Authentication 객체
     * @throws Exception 토큰 생성 중 발생한 예외를 처리하기 위해 사용된다.
     * @return 생성된 JWT 리프레시 토큰
     */
    @Throws(Exception::class)
    fun createRefreshToken(authentication: Authentication): String {
        return Jwts.builder()
            .subject(authentication.name) // sub 클레임: 토큰 제목을 지정
            .issuer("DPANG-AUTH-SERVER") // iss 클레임: 토큰 발급자를 지정
            .issuedAt(Date()) // iat 클레임: 토큰 발급 시간을 지정
            .expiration(Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME)) // exp 클레임: 토큰 만료 시간을 지정
            .signWith(secretKey) // signWith 메소드를 사용해 서명 알고리즘과 키를 지정
            .compact() // 마지막으로 compact 메소드를 호출해 모든 부분을 합쳐서 하나의 JWT 토큰 문자열을 생성한다
    }

    /**
     * Access token에서 식별자를 추출하는 메소드
     *
     * @param token Access token
     * @return 토큰에서 추출된 식별자
     */
    fun getClientIdFromAccessToken(token: String?): Int? {
        // JJWT에서 제공하는 parserBuilder를 통해 JwtParser를 생성
        val jwtParser: JwtParser = Jwts.parser()
            .verifyWith(secretKey)
            .build()

        // 토큰을 파싱하여 Claims 객체 획득
        val jwsClaims = jwtParser.parseSignedClaims(token)
        val claims = jwsClaims.payload

        // Claims에서 client-id 추출
        return claims["client-id", Int::class.java]
    }

}
