package kea.dpang.auth.redis.entity

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("VerificationCode", timeToLive = 60 * 5) // 5분
data class VerificationCode(

    @Id @Indexed
    var email: String, // 사용자 이메일

    var code: String, // 사용자 인증 번호
)
