package kea.dpang.auth.entity

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("VerificationCode")
data class VerificationCode(

    @Id
    @Indexed
    var id: String? = null,

    var code: String? = null,

    @TimeToLive
    var expiration: Long? = 300L // 5분
)
