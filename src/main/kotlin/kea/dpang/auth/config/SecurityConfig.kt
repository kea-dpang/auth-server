package kea.dpang.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic { it.disable() } // rest api 이므로 기본설정 사용안함. 기본설정은 비인증시 로그인폼 화면으로 리다이렉트 된다.
            .csrf { it.disable() } // rest api이므로 csrf 보안이 필요없으므로 disable처리.
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // jwt token으로 인증 -> 세션은 필요없으므로 생성안함.
            .cors { it.disable() } // CORS(Cross-Origin Resource Sharing) 설정 비활성화.
            .headers { headers -> headers.frameOptions { it.disable() } } // X-Frame-Options 비활성화 (IFrame 사용 가능하도록).
            .formLogin { it.disable() } // formLogin 대신 Jwt를 사용하기 때문에 disable로 설정
            .logout { it.disable() } // 로그아웃 기능 비활성화.
            .authorizeHttpRequests { request ->
                request
                    .requestMatchers("/api/auth/**").permitAll()
            }
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}